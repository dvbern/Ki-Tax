/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.outbox.verfuegung.VerfuegungEventConverter;
import ch.dvbern.ebegu.outbox.verfuegung.VerfuegungVerfuegtEvent;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.VerfuegungUtil;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service zum berechnen und speichern der Verfuegung
 */
@Stateless
@Local(VerfuegungService.class)
public class VerfuegungServiceBean extends AbstractBaseService implements VerfuegungService {

	private static final Logger LOG = LoggerFactory.getLogger(VerfuegungServiceBean.class);

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private RulesService rulesService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GeneratedDokumentService generatedDokumentService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private MailService mailService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private VerfuegungEventConverter verfuegungEventConverter;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	public Verfuegung verfuegen(
		@Nonnull String gesuchId,
		@Nonnull String betreuungId,
		@Nullable String manuelleBemerkungen,
		boolean ignorieren,
		boolean ignorierenMahlzeiten,
		boolean sendEmail) {
		// verfuegung in das preview Feld der Betreuung berechnen lassen
		Betreuung betreuungMitVerfuegungPreview = (Betreuung) calculateAndExtractPlatz(gesuchId, betreuungId);
		Objects.requireNonNull(betreuungMitVerfuegungPreview);
		Verfuegung verfuegungPreview = betreuungMitVerfuegungPreview.getVerfuegungPreview();
		Objects.requireNonNull(verfuegungPreview);
		// Die manuelle Bemerkungen sind das einzige Attribut, welches wir vom Client uebernehmen
		String bemerkungen = manuelleBemerkungen == null ? verfuegungPreview.getGeneratedBemerkungen() :
			manuelleBemerkungen;
		verfuegungPreview.setManuelleBemerkungen(bemerkungen);

		final Verfuegung persistedVerfuegung = persistVerfuegung(betreuungMitVerfuegungPreview,
			Betreuungsstatus.VERFUEGT);
		setZahlungsstatus(persistedVerfuegung, ignorieren, ignorierenMahlzeiten);
		//noinspection ResultOfMethodCallIgnored
		wizardStepService.updateSteps(gesuchId, null, null, WizardStepName.VERFUEGEN);

		// Dokument erstellen
		generateVerfuegungDokument(betreuungMitVerfuegungPreview);

		if (applicationPropertyService.isPublishSchnittstelleEventsAktiviert(persistedVerfuegung.getPlatz().extractGesuch().extractMandant())
			&& persistedVerfuegung.getBetreuung() != null && !persistedVerfuegung.getBetreuung().isAngebotMittagstisch()) {
			Optional<VerfuegungVerfuegtEvent> verfuegungEvent = verfuegungEventConverter.of(persistedVerfuegung);

			verfuegungEvent.ifPresent(verfuegungVerfuegtEvent -> this.event.fire(verfuegungVerfuegtEvent));
			persistedVerfuegung.setEventPublished(true);
		} else {
			persistedVerfuegung.setSkipPreUpdate(true);
			persistedVerfuegung.setEventPublished(false);
		}
		persistence.merge(persistedVerfuegung);

		if (sendEmail) {
			mailService.sendInfoBetreuungVerfuegt(betreuungMitVerfuegungPreview);
		}

		return persistedVerfuegung;
	}

	@Override
	public void gesuchAutomatischVerfuegen(@Nonnull Gesuch mutation) {
		Preconditions.checkArgument(mutation.isMutation());
		Preconditions.checkArgument(mutation.getStatus() == AntragStatus.IN_BEARBEITUNG_JA);
		Preconditions.checkArgument(mutation.isNewlyCreatedMutation());

		acceptFinSit(mutation);
		setGesuchGeprueft(mutation);
		gesuchService.verfuegenStarten(mutation);

		Preconditions.checkArgument(mutation.getStatus() == AntragStatus.VERFUEGEN);

		mutation.getKindContainers().forEach(kindContainer -> {
			kindContainer.getBetreuungen().forEach(betreuung -> {
				verfuegen(mutation.getId(), betreuung.getId(), null, false, false, true);
			});
		});
	}

	private void acceptFinSit(Gesuch mutation) {
		mutation.setFinSitStatus(FinSitStatus.AKZEPTIERT);
		gesuchService.updateGesuch(mutation, false, null);
	}

	private Gesuch setGesuchGeprueft(Gesuch mutation) {
		mutation.setStatus(AntragStatus.GEPRUEFT);
		return gesuchService.updateGesuch(mutation, true, null);
	}

	@Nonnull
	@Override
	public AnmeldungTagesschule anmeldungTagesschuleUebernehmen(@Nonnull AnmeldungTagesschule anmeldungTagesschule) {
		AnmeldungTagesschule betreuungMitVerfuegungPreview = (AnmeldungTagesschule) calculateAndExtractPlatz(
			anmeldungTagesschule.extractGesuch().getId(),
			anmeldungTagesschule.getId());
		// Wir muessen uns merken, ob dies die gueltige Anmeldung ist, da beim persistieren der Verfügung das Flag
		// automatisch gesetzt wird.
		boolean isGueltigeAnmeldung = betreuungMitVerfuegungPreview.isGueltig();
		Objects.requireNonNull(betreuungMitVerfuegungPreview);
		Verfuegung verfuegungPreview = betreuungMitVerfuegungPreview.getVerfuegungPreview();
		Objects.requireNonNull(verfuegungPreview);

		// Hier wird die Verfügung automatisch auf gueltig gsetzt. Im Fall der Tagesschulen werden aber auch die
		// Vorgänger-Verfügungen
		// übernommen, diese dürfen aber nicht auf gueltig gesetzt werden!
		final Verfuegung persistedVerfuegung = persistVerfuegung(betreuungMitVerfuegungPreview,
			Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN);

		// Da die Module auch beim Uebernehmen noch verändert worden sein können, muss die Anmeldung zuerst nochmals
		// gespeichert werden. Hier muss der neue Status schon gesetzt sein, damit er auf die bestehenden
		// Anemldungen auf anderen Mutationen / Erstgesuch kopiert wirdd
		betreuungService.saveAnmeldungTagesschule(anmeldungTagesschule);

		AnmeldungTagesschule persistedAnmeldung = persistedVerfuegung.getAnmeldungTagesschule();
		Objects.requireNonNull(persistedAnmeldung);
		// Das Flag wieder auf den korrekten, vorher gemerkten Wert zurücksetzen
		persistedAnmeldung.setGueltig(isGueltigeAnmeldung);

		// Uebernommen werden jeweils auch die Vorgänger. Das Mail soll aber nur für die aktuell gültige Anmeldung
		// geschickt werden!
		if (isGueltigeAnmeldung) {
			try {
				// Bei Uebernahme einer Anmeldung muss eine E-Mail geschickt werden
				GemeindeStammdaten gemeindeStammdaten =
					gemeindeService.getGemeindeStammdatenByGemeindeId(anmeldungTagesschule.extractGesuch().getDossier().getGemeinde().getId()).get();
				if (gemeindeStammdaten.getBenachrichtigungTsEmailAuto() && !persistedAnmeldung.isTagesschuleTagi()) {
					mailService.sendInfoSchulamtAnmeldungTagesschuleUebernommen(persistedAnmeldung);
				}
			} catch (MailException e) {
				logExceptionAccordingToEnvironment(e,
					"Mail InfoSchulamtAnmeldungUebernommen konnte nicht verschickt werden fuer Betreuung",
					anmeldungTagesschule.getId());
			}
		}

		// Dokument erstellen
		generateAnmeldebestaetigungDokument(persistedAnmeldung);

		// Rekursiv alle Vorgänger ebenfalls auf UEBERNOMMEN setzen
		setVorgaengerAnmeldungTagesschuleAufUebernommen(persistedAnmeldung);

		return persistedAnmeldung;
	}

	@Nonnull
	@Override
	public AnmeldungFerieninsel anmeldungFerieninselUebernehmen(@Nonnull AnmeldungFerieninsel anmeldungFerieninsel) {
		// momentan wird nichts verfügt, wir setzen lediglich den status der betreuung auf uebernommen
		anmeldungFerieninsel.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN);
		try {
			// Bei Uebernahme einer Anmeldung muss eine E-Mail geschickt werden
			mailService.sendInfoSchulamtAnmeldungFerieninselUebernommen(anmeldungFerieninsel);
		} catch (MailException e) {
			LOG.error("Mail InfoSchulamtFerieninselUebernommen konnte nicht versendet werden fuer "
					+ "AnmeldungFerieninsel {}",
				anmeldungFerieninsel.getId(), e);
		}
		return betreuungService.saveAnmeldungFerieninsel(anmeldungFerieninsel);
	}

	private void setVorgaengerAnmeldungTagesschuleAufUebernommen(@Nonnull AnmeldungTagesschule anmeldung) {
		if (!anmeldung.getBetreuungsstatus().isIgnoriert()) {
			anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN);
		}
		// Rekursiv alle Vorgänger ungültig setzen
		if (anmeldung.getVorgaengerId() != null) {
			final Optional<AnmeldungTagesschule> vorgaengerOpt =
				betreuungService.findAnmeldungTagesschule(anmeldung.getVorgaengerId());
			vorgaengerOpt.ifPresent(this::setVorgaengerAnmeldungTagesschuleAufUebernommen);
		}
	}

	@Override
	@Nonnull
	public AnmeldungTagesschule anmeldungSchulamtAusgeloestAbschliessen(
		@Nonnull String gesuchId,
		@Nonnull String betreuungId
	) {
		AnmeldungTagesschule betreuungMitVerfuegungPreview = (AnmeldungTagesschule) calculateAndExtractPlatz(gesuchId,
			betreuungId);
		Objects.requireNonNull(betreuungMitVerfuegungPreview);
		Verfuegung verfuegungPreview = betreuungMitVerfuegungPreview.getVerfuegungPreview();
		Objects.requireNonNull(verfuegungPreview);

		final Verfuegung persistedVerfuegung = persistVerfuegung(betreuungMitVerfuegungPreview,
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);

		AnmeldungTagesschule persistedAnmeldung = persistedVerfuegung.getAnmeldungTagesschule();
		Objects.requireNonNull(persistedAnmeldung);

		return persistedAnmeldung;
	}

	/**
	 * Generiert das Verfuegungsdokument.
	 *
	 * @param betreuung Betreuung, fuer die das Dokument generiert werden soll.
	 */
	private void generateVerfuegungDokument(@Nonnull Betreuung betreuung) {
		try {
			Gesuch gesuch = betreuung.extractGesuch();
			generatedDokumentService.getVerfuegungDokumentAccessTokenGeneratedDokument(gesuch, betreuung, "", true);
		} catch (IOException | MimeTypeParseException | MergeDocException e) {
			throw new EbeguRuntimeException(
				"generateVerfuegungDokument",
				"Verfuegung-Dokument konnte nicht erstellt werden"
					+ betreuung.getId(),
				e);
		}
	}

	/**
	 * Generiert das Anmeldebestaetigungsdokument.
	 *
	 * @param anmeldung AbstractAnmeldung, fuer die das Dokument generiert werden soll.
	 */
	private void generateAnmeldebestaetigungDokument(@Nonnull AbstractAnmeldung anmeldung) {
		try {
			Gesuch gesuch = anmeldung.extractGesuch();

			generatedDokumentService.getAnmeldeBestaetigungDokumentAccessTokenGeneratedDokument(gesuch, anmeldung,
				true, true);
		} catch (MimeTypeParseException | MergeDocException e) {
			throw new EbeguRuntimeException(
				"AnmeldebestaetigungsDokument",
				"Anmeldebestaetigung-Dokument konnte nicht erstellt werden"
					+ anmeldung.getId(), e);
		}
	}

	/**
	 * Aendert den Status der Zahlung auf NEU oder IGNORIEREND fuer alle Zahlungen wo etwas korrigiert wurde.
	 * Wird auf NEU gesetzt wenn ignorieren==false, sonst wird es auf IGNORIEREND gesetzt.
	 */
	private void setZahlungsstatus(@Nonnull Verfuegung verfuegung, boolean ignorieren, boolean ignorierenMahlzeiten) {
		Betreuung betreuung = verfuegung.getBetreuung();
		Objects.requireNonNull(betreuung);
		Gesuch gesuch = betreuung.extractGesuch();

		// Zahlungsstatus muss nur bei Mutationen und Angebote der Art KITA und TAGESELTERN aktualisiert werden
		if (!gesuch.isMutation() || !betreuung.isAngebotAuszuzahlen()) {
			return;
		}

		findVorgaengerAusbezahlteVerfuegung(ZahlungslaufTyp.GEMEINDE_INSTITUTION, betreuung)
			.ifPresent(vorgaenger -> setZahlungsstatus(
				ZahlungslaufTyp.GEMEINDE_INSTITUTION,
				verfuegung,
				vorgaenger,
				ignorieren));

		findVorgaengerAusbezahlteVerfuegung(ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER, betreuung)
			.ifPresent(vorgaenger -> setZahlungsstatus(
				ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER,
				verfuegung,
				vorgaenger,
				ignorierenMahlzeiten));
	}

	private void setZahlungsstatus(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull Verfuegung verfuegung,
		@Nonnull Verfuegung vorgaenger,
		boolean ignorieren) {

		verfuegung.getZeitabschnitte()
			.forEach(zeitabschnitt -> setZahlungsstatus(
				zahlungslaufTyp,
				zeitabschnitt,
				findZeitabschnitteOnVerfuegung(zeitabschnitt.getGueltigkeit(), vorgaenger),
				ignorieren));
	}

	private void setZahlungsstatus(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull List<VerfuegungZeitabschnitt> vorgaengerList,
		boolean ignorieren) {

		final ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);

		Optional<VerfuegungZeitabschnitt> zeitabschnittSameGueltigkeitSameBetrag =
			VerfuegungUtil.findZeitabschnittSameGueltigkeitSameBetrag(zahlungslaufHelper, vorgaengerList, zeitabschnitt);

		// Folgende Informationen werden fuer die Berechnung des Status benoetigt:
		boolean sameGueltigkeitSameBetrag = zeitabschnittSameGueltigkeitSameBetrag.isPresent();
		// Alles ausser NEU
		boolean zeitraumBereitsVerrechnet = areAllZeitabschnitteVerrechnet(zahlungslaufHelper, vorgaengerList);
		boolean voraengerIgnoriertUndAusbezahlt = isThereAnyIgnoriert(zahlungslaufHelper, vorgaengerList);

		LOG.debug(
			"Verfüge {}, sameGueltigkeitSameBetrag={}, zeitraumBereitsVerrechnet={}, "
				+ "voraengerIgnoriertUndAusbezahlt={}",
			zeitabschnitt.getGueltigkeit().toRangeString(),
			sameGueltigkeitSameBetrag,
			zeitraumBereitsVerrechnet,
			voraengerIgnoriertUndAusbezahlt);

		// Es gelten folgende Regeln:
		// - Wenn ein Zeitraum bereits einmal ignoriert und im Zahlungslauf behandelt wurde, muss er auch kuenftig
		//   immer ignoriert werden
		// - Wenn ein Zeitraum noch nie verrechnet wurde, erhaelt er den Status neu
		// - Wenn der Zeitraum verrechnet wurde -> VERRECHNEND (wir muessen nochmals auszahlen), *ausser*
		//     es wurde das "ignorieren" Flag gesetzt -> IGNORIEREND
		// 	   => Wenn der Betrag nicht geändert hat, sollten wir nicht auf IGNORIEREND setzen, egal wie das
		// 	   Flag war sondern auf VERRECHNEND.
		//     Wichtig ist, dass auf dieser Verfuegung (die noch nicht ausbezahlt war) nie ein "behandelter"
		//     Status gesetzt wird da wir sonst bei einer weiteren Mutation die falsche Vorgängerverfügung
		//     verwenden!
		if (voraengerIgnoriertUndAusbezahlt) {
			zahlungslaufHelper.setZahlungsstatus(zeitabschnitt, VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT);
		} else if (!zeitraumBereitsVerrechnet) {
			zahlungslaufHelper.setZahlungsstatus(zeitabschnitt, VerfuegungsZeitabschnittZahlungsstatus.NEU);
		} else if (!sameGueltigkeitSameBetrag) {
			// Wenn der Betrag und die Gueltigkeit gleich bleibt: Wir wurden gar nicht gefragt, ob wir
			// ignorieren wollen -> wir lassen den letzten bekannten Status!
			if (ignorieren) {
				zahlungslaufHelper.setZahlungsstatus(zeitabschnitt, VerfuegungsZeitabschnittZahlungsstatus.IGNORIEREND);
			} else {
				zahlungslaufHelper.setZahlungsstatus(zeitabschnitt, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNEND);
			}
		} else {
			// Es war verrechnet UND derselbe Betrag. Wir muessen den Status trotzdem auf etwas
			// "nicht-behandeltes"
			// zuruecksetzen!
			// Was ist das Problem, wenn wir hier "VERRECHNET" setzen würden?
			// - Gesuch verfügen und auszahlen
			// - Gesuch mutieren mit Korrektur der fin. Sit. --> Bei Frage: Korrigieren -> noch nicht ausbezahlen
			// - Gesuch erneut mutieren mit Korrektur des Namens --> Frage Korrigieren erscheint nicht mehr!!
			if (zahlungslaufHelper.getZahlungsstatus(zeitabschnitt).isVerrechnet()) {
				zahlungslaufHelper.setZahlungsstatus(zeitabschnitt, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNEND);
			}
		}

		VerfuegungZeitabschnitt firstVorgaenger = CollectionUtils.isNotEmpty(vorgaengerList) ? vorgaengerList.get(0) : null;

		VerfuegungsZeitabschnittZahlungsstatus statusFirstVorgaenger =
			firstVorgaenger != null ? zahlungslaufHelper.getZahlungsstatus(firstVorgaenger) : null;

		LOG.debug(
			"Zeitabschnitt {} VORHER={} NEU={}",
			zeitabschnitt.getGueltigkeit().toRangeString(),
			statusFirstVorgaenger,
			zahlungslaufHelper.getZahlungsstatus(zeitabschnitt));
	}

	private boolean areAllZeitabschnitteVerrechnet(@Nonnull ZahlungslaufHelper helper, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte.stream()
			.allMatch(verfuegungZeitabschnitt -> helper.getZahlungsstatus(verfuegungZeitabschnitt)
				.isBereitsBehandeltInZahlungslauf());
	}

	private boolean isThereAnyIgnoriert(@Nonnull ZahlungslaufHelper helper, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte.stream()
			.anyMatch(verfuegungZeitabschnitt -> helper.getZahlungsstatus(verfuegungZeitabschnitt).isIgnoriert());
	}

	private void setVerfuegungsKategorien(Verfuegung verfuegung) {
		if (!verfuegung.isKategorieNichtEintreten()) {
			for (VerfuegungZeitabschnitt zeitabschnitt : verfuegung.getZeitabschnitte()) {
				if (zeitabschnitt.getRelevantBgCalculationInput().isKategorieKeinPensum()) {
					verfuegung.setKategorieKeinPensum(true);
				}
				if (zeitabschnitt.getRelevantBgCalculationInput().isKategorieMaxEinkommen()) {
					verfuegung.setKategorieMaxEinkommen(true);
				}
			}
			// Wenn es keines der anderen ist, ist es "normal"
			if (!verfuegung.isKategorieKeinPensum() &&
				!verfuegung.isKategorieMaxEinkommen() &&
				!verfuegung.isKategorieNichtEintreten()) {
				verfuegung.setKategorieNormal(true);
			}
		}
	}

	@Nonnull
	@Override
	public Verfuegung nichtEintreten(@Nonnull String gesuchId, @Nonnull String betreuungId) {

		Betreuung betreuungMitVerfuegungPreview = (Betreuung) calculateAndExtractPlatz(gesuchId, betreuungId);
		Objects.requireNonNull(betreuungMitVerfuegungPreview);
		Verfuegung verfuegungPreview = betreuungMitVerfuegungPreview.getVerfuegungPreview();
		Objects.requireNonNull(verfuegungPreview);

		// Bei Nicht-Eintreten muss der Anspruch auf der Verfuegung auf 0 gesetzt werden, da diese u.U. bei Mutationen
		// als Vergleichswert hinzugezogen werden
		verfuegungPreview.getZeitabschnitte()
			.forEach(z -> {
				z.getBgCalculationResultAsiv().setAnspruchZeroAndSaveRestanspruch();
				if (z.getBgCalculationResultGemeinde() != null) {
					z.getBgCalculationResultGemeinde().setAnspruchZeroAndSaveRestanspruch();
				}
			});
		verfuegungPreview.setKategorieNichtEintreten(true);
		initializeVorgaengerVerfuegungen(betreuungMitVerfuegungPreview.extractGesuch());
		Verfuegung persistedVerfuegung = persistVerfuegung(betreuungMitVerfuegungPreview,
			Betreuungsstatus.NICHT_EINGETRETEN);
		wizardStepService.updateSteps(gesuchId, null, null, WizardStepName.VERFUEGEN);
		// Dokument erstellen
		try {
			generatedDokumentService.getNichteintretenDokumentAccessTokenGeneratedDokument(betreuungMitVerfuegungPreview, true);
		} catch (IOException | MimeTypeParseException | MergeDocException e) {
			throw new EbeguRuntimeException("nichtEintreten", "Nichteintretensverfuegung-Dokument konnte nicht "
				+ "erstellt werden" + betreuungId, e);
		}
		return persistedVerfuegung;
	}

	@Nonnull
	private Verfuegung persistVerfuegung(@Nonnull AbstractPlatz platzWithPreviewVerfuegung,
		@Nonnull Betreuungsstatus betreuungsstatus) {
		// preview verfuegung als definitive verfuegung einhaengen
		Verfuegung verfuegung = platzWithPreviewVerfuegung.getVerfuegungPreview();
		Objects.requireNonNull(verfuegung);
		platzWithPreviewVerfuegung.setVerfuegung(verfuegung);
		platzWithPreviewVerfuegung.setVerfuegungPreview(null);
		verfuegung.setPlatz(platzWithPreviewVerfuegung);

		setVerfuegungsKategorien(verfuegung);
		AbstractPlatz platz = verfuegung.getPlatz();
		Objects.requireNonNull(platz);
		platz.setBetreuungsstatus(betreuungsstatus);

		// Gueltigkeit auf dem neuen setzen, auf der bisherigen entfernen
		updateGueltigFlagOnPlatzAndVorgaenger(platz);

		verfuegung.getZeitabschnitte().forEach(verfZeitabsch -> verfZeitabsch.setVerfuegung(verfuegung));
		authorizer.checkWriteAuthorization(verfuegung);

		Verfuegung persist = persistence.persist(verfuegung);
		persistence.merge(platz);
		return persist;
	}

	@Nonnull
	@Override
	public Optional<Verfuegung> findVerfuegung(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Verfuegung a = persistence.find(Verfuegung.class, id);
		authorizer.checkReadAuthorization(a);
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	public Collection<Verfuegung> getAllVerfuegungen() {
		Collection<Verfuegung> verfuegungen = criteriaQueryHelper.getAll(Verfuegung.class);
		authorizer.checkReadAuthorizationVerfuegungen(verfuegungen);
		return verfuegungen;
	}

	@Nonnull
	@Override
	public Gesuch calculateVerfuegung(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		finanzielleSituationService.calculateFinanzDaten(gesuch);

		Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		Gemeinde gemeinde = gesuch.extractGemeinde();
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();
		KitaxUebergangsloesungParameter kitaxParameter = loadKitaxUebergangsloesungParameter(gemeinde.getMandant());
		List<Rule> rules = rulesService.getRulesForGesuchsperiode(gemeinde, gesuchsperiode, kitaxParameter, sprache.getLocale());

		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED,
			gemeinde.getMandant(), true);
		Map<EinstellungKey, Einstellung> einstellungMap = einstellungService.loadRuleParameters(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), BetreuungsgutscheinEvaluator.getRequiredParametersForAbschlussRules());
		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput, einstellungMap);
		BGRechnerParameterDTO calculatorParameters = loadCalculatorParameters(gemeinde, gesuchsperiode);

		// Finde und setze die letzte Verfuegung für die Betreuung für den Merger und Vergleicher.
		// Bei GESCHLOSSEN_OHNE_VERFUEGUNG wird solange ein Vorgänger gesucht, bis  dieser gefunden wird. (Rekursiv)
		initializeVorgaengerVerfuegungen(gesuch);

		bgEvaluator.evaluate(gesuch, calculatorParameters, kitaxParameter, sprache.getLocale());
		authorizer.checkReadAuthorizationForAnyPlaetze(gesuch.extractAllPlaetze()); // plaetze pruefen
		// reicht hier glaub
		return gesuch;
	}

	@Override
	@Nonnull
	public Verfuegung getEvaluateFamiliensituationVerfuegung(@Nonnull Gesuch gesuch) {
		this.finanzielleSituationService.calculateFinanzDaten(gesuch);

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		KitaxUebergangsloesungParameter kitaxParameter = loadKitaxUebergangsloesungParameter(gesuch.extractGemeinde().getMandant());

		final List<Rule> rules = rulesService
			.getRulesForGesuchsperiode(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), kitaxParameter, sprache.getLocale());
		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED,
			gesuch.extractGemeinde().getMandant(), true);
		Map<EinstellungKey, Einstellung> einstellungMap = einstellungService.loadRuleParameters(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), BetreuungsgutscheinEvaluator.getRequiredParametersForAbschlussRules());

		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput, einstellungMap);

		initializeVorgaengerVerfuegungen(gesuch);

		return bgEvaluator.evaluateFamiliensituation(gesuch, sprache.getLocale());
	}

	@Override
	public void initializeVorgaengerVerfuegungen(@Nonnull Gesuch gesuch) {
		gesuch.getKindContainers()
			.stream()
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.forEach(this::setVorgaengerVerfuegungen);
		gesuch.getKindContainers()
			.stream()
			.flatMap(kindContainer -> kindContainer.getAnmeldungenTagesschule().stream())
			.forEach(this::setVorgaengerVerfuegungen);
	}

	private void setVorgaengerVerfuegungen(@Nonnull AbstractPlatz platz) {
		Map<ZahlungslaufTyp, Verfuegung> vorgaengerAusbezahlteVerfuegung = new HashMap<>();
		if (platz instanceof Betreuung) {
			vorgaengerAusbezahlteVerfuegung = findVorgaengerAusbezahlteVerfuegungForAllZahlungslaufTypes((Betreuung) platz);
		}

		Verfuegung vorgaengerVerfuegung = findVorgaengerVerfuegung(platz)
			.orElse(null);

		platz.initVorgaengerVerfuegungen(vorgaengerVerfuegung, vorgaengerAusbezahlteVerfuegung);
	}

	/**
	 * @return gibt die Verfuegung der vorherigen verfuegten Betreuung zurueck, die ausbezahlt ist.
	 */
	@Nonnull
	private Map<ZahlungslaufTyp, Verfuegung> findVorgaengerAusbezahlteVerfuegungForAllZahlungslaufTypes(
		@Nonnull Betreuung betreuung
	) {
		Map<ZahlungslaufTyp, Verfuegung> result = new HashMap<>();
		Arrays.stream(ZahlungslaufTyp.values()).iterator().forEachRemaining(zahlungslaufTyp -> {
			final Optional<Verfuegung> vorgaengerAusbezahlteVerfuegung =
				findVorgaengerAusbezahlteVerfuegung(zahlungslaufTyp, betreuung);
			vorgaengerAusbezahlteVerfuegung.ifPresent(verfuegung -> result.put(zahlungslaufTyp, verfuegung));
		});
		return result;
	}

	/**
	 * @return gibt die Verfuegung der vorherigen verfuegten Betreuung zurueck, die ausbezahlt ist.
	 */
	@Nonnull
	private Optional<Verfuegung> findVorgaengerAusbezahlteVerfuegung(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull Betreuung betreuung
	) {
		Objects.requireNonNull(betreuung, "betreuung darf nicht null sein");
		if (betreuung.getVorgaengerId() == null) {
			return Optional.empty();
		}

		// Achtung, hier wird persistence.find() verwendet, da ich fuer das Vorgaengergesuch evt. nicht
		// Leseberechtigt bin, fuer die Mutation aber schon!
		Betreuung vorgaengerbetreuung = persistence.find(Betreuung.class, betreuung.getVorgaengerId());
		if (vorgaengerbetreuung != null) {
			if (vorgaengerbetreuung.getBetreuungsstatus() != Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG
				&& isAusbezahlt(zahlungslaufTyp, vorgaengerbetreuung)) {
				// Hier kann aus demselben Grund die Berechtigung fuer die Vorgaengerverfuegung nicht geprueft werden
				return Optional.ofNullable(vorgaengerbetreuung.getVerfuegung());
			}
			return findVorgaengerAusbezahlteVerfuegung(zahlungslaufTyp, vorgaengerbetreuung);
		}
		return Optional.empty();
	}

	private boolean isAusbezahlt(@Nonnull ZahlungslaufTyp zahlungslaufTyp, @Nonnull Betreuung betreuung) {
		if (betreuung.getVerfuegung() == null) {
			return false;
		}
		final ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);
		return betreuung.getVerfuegung().getZeitabschnitte()
			.stream()
			.anyMatch(zeitabschnitt -> zahlungslaufHelper.getZahlungsstatus(zeitabschnitt).isBereitsBehandeltInZahlungslauf());
	}

	@Override
	public Optional<LocalDate> findVorgaengerVerfuegungDate(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung darf nicht null sein");

		Optional<LocalDate> letztesVerfDatum = findVorgaengerVerfuegung(betreuung)
			.flatMap(vorgaengerVerfuegung -> {
				authorizer.checkReadAuthorization(vorgaengerVerfuegung);

				return Optional.ofNullable(vorgaengerVerfuegung.getTimestampErstellt())
					.map(LocalDateTime::toLocalDate);
			});

		return letztesVerfDatum;
	}

	@Override
	public void findVerrechnetenOrIgnoriertenZeitabschnittOnVorgaengerVerfuegung(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Betreuung betreuungNeu,
		@Nonnull List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte
	) {
		final ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);

		findVorgaengerAusbezahlteVerfuegung(zahlungslaufTyp, betreuungNeu)
			.map(verfuegung -> findZeitabschnitteOnVerfuegung(zeitabschnittNeu.getGueltigkeit(), verfuegung))
			.ifPresent(zeitabschnitte -> zeitabschnitte.forEach(zeitabschnitt -> {
				VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus = zahlungslaufHelper.getZahlungsstatus(zeitabschnitt);

				if ((zahlungsstatus.isVerrechnet() || zahlungsstatus.isIgnoriert())){
					if(isNotInZeitabschnitteList(zeitabschnitt, vorgaengerZeitabschnitte, zahlungslaufHelper)) {
						// Diesen ins Result, iteration weiterführen und von allen den Vorgänger suchen bis VERRECHNET oder
						// kein Vorgaenger
						vorgaengerZeitabschnitte.add(zeitabschnitt);
					}
				} else {
					Betreuung vorgaengerBetreuung = zeitabschnitt.getVerfuegung().getBetreuung();
					Objects.requireNonNull(vorgaengerBetreuung);
					// Es gab keine bereits Verrechneten Zeitabschnitte auf dieser Verfuegung -> eins weiter
					// zurueckgehen
					findVerrechnetenOrIgnoriertenZeitabschnittOnVorgaengerVerfuegung(
						zahlungslaufTyp,
						zeitabschnittNeu,
						vorgaengerBetreuung,
						vorgaengerZeitabschnitte);
				}
			}));
	}

	@Override
	public void findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Betreuung betreuungNeu,
		@Nonnull List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte
	) {
		final ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);

		findVorgaengerAusbezahlteVerfuegung(zahlungslaufTyp, betreuungNeu)
			.map(verfuegung -> findZeitabschnitteOnVerfuegung(zeitabschnittNeu.getGueltigkeit(), verfuegung))
			.ifPresent(zeitabschnitte -> zeitabschnitte.forEach(zeitabschnitt -> {
				VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus = zahlungslaufHelper.getZahlungsstatus(zeitabschnitt);

				if ((zahlungsstatus.isVerrechnet())){
					if(isNotInZeitabschnitteList(zeitabschnitt, vorgaengerZeitabschnitte, zahlungslaufHelper)) {
						// Diesen ins Result, iteration weiterführen und von allen den Vorgänger suchen bis VERRECHNET oder
						// kein Vorgaenger
						vorgaengerZeitabschnitte.add(zeitabschnitt);
					}
				} else {
					Betreuung vorgaengerBetreuung = zeitabschnitt.getVerfuegung().getBetreuung();
					Objects.requireNonNull(vorgaengerBetreuung);
					// Es gab keine bereits Verrechneten Zeitabschnitte auf dieser Verfuegung -> eins weiter
					// zurueckgehen
					findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(
						zahlungslaufTyp,
						zeitabschnittNeu,
						vorgaengerBetreuung,
						vorgaengerZeitabschnitte);
				}
			}));
	}

	@Nonnull
	@Override
	public List<VerfuegungZeitabschnitt> findZeitabschnitteByYear(int year) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = cb.createQuery(VerfuegungZeitabschnitt.class);
		List<Predicate> predicatesToUse = new ArrayList<>();

		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);

		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);

		ParameterExpression<Integer> parameterYear = cb.parameter(Integer.class, "year");
		predicatesToUse.add(cb.equal(
			cb.function(
				"YEAR",
				Integer.class,
				root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)
			),
			parameterYear
		));

		Objects.requireNonNull(principalBean.getMandant());
		Predicate mandantPredicate = cb.equal(
			joinVerfuegung.get(Verfuegung_.betreuung)
				.get(Betreuung_.kind)
				.get(KindContainer_.gesuch)
				.get(Gesuch_.dossier)
				.get(Dossier_.fall)
				.get(Fall_.mandant),
			principalBean.getMandant());
		predicatesToUse.add(mandantPredicate);

		predicatesToUse.add(cb.isTrue(joinBetreuung.get(Betreuung_.gueltig)));

		predicatesToUse.add(cb.equal(joinBetreuung.get(Betreuung_.betreuungsstatus), Betreuungsstatus.VERFUEGT));

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse));

		TypedQuery<VerfuegungZeitabschnitt> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(parameterYear, year);

		return typedQuery.getResultList();
	}

	@Nonnull
	@Override
	public List<VerfuegungZeitabschnitt> findZeitabschnitteByYear(int year,
		@Nonnull Gemeinde einschraenkenAufGemeinde) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = cb.createQuery(VerfuegungZeitabschnitt.class);
		List<Predicate> predicatesToUse = new ArrayList<>();

		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);

		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);

		ParameterExpression<Integer> parameterYear = cb.parameter(Integer.class, "year");
		ParameterExpression<Gemeinde> parameterGemeinde = cb.parameter(Gemeinde.class, "gemeinde");

		predicatesToUse.add(cb.equal(
			cb.function(
				"YEAR",
				Integer.class,
				root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)
			),
			parameterYear
		));

		predicatesToUse.add(cb.equal(joinBetreuung.get(Betreuung_.betreuungsstatus), Betreuungsstatus.VERFUEGT));

		predicatesToUse.add(cb.isTrue(joinBetreuung.get(Betreuung_.gueltig)));
		Join<Gesuch, Dossier> joinDossier = joinBetreuung
			.join(Betreuung_.kind, JoinType.LEFT)
			.join(KindContainer_.gesuch, JoinType.LEFT)
			.join(Gesuch_.dossier, JoinType.LEFT);
		predicatesToUse.add(cb.equal(joinDossier.get(Dossier_.gemeinde), parameterGemeinde));

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse));

		TypedQuery<VerfuegungZeitabschnitt> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(parameterYear, year);
		typedQuery.setParameter(parameterGemeinde, einschraenkenAufGemeinde);

		return typedQuery.getResultList();
	}

	/**
	 * Mithilfe der Methode isSamePersistedValues schaut ob der uebergebene Zeitabschnitt bereits in der uebergebenen
	 * Liste existiert.
	 * Alle Felder muessen verglichen werden.
	 */
	private boolean isNotInZeitabschnitteList(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte,
		@Nonnull ZahlungslaufHelper zahlungslaufHelper
	) {
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : vorgaengerZeitabschnitte) {
			if (zahlungslaufHelper.isSamePersistedValues(verfuegungZeitabschnitt, zeitabschnitt)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Findet das anspruchberechtigtes Pensum zum Zeitpunkt des neuen Zeitabschnitt-Start
	 */
	@Nonnull
	private List<VerfuegungZeitabschnitt> findZeitabschnitteOnVerfuegung(
		@Nonnull DateRange newVerfuegungGueltigkeit,
		@Nonnull Verfuegung lastVerfuegung
	) {
		List<VerfuegungZeitabschnitt> lastVerfuegungsZeitabschnitte = new ArrayList<>();
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : lastVerfuegung.getZeitabschnitte()) {
			final DateRange gueltigkeitExistingZeitabschnitt = verfuegungZeitabschnitt.getGueltigkeit();
			if (gueltigkeitExistingZeitabschnitt.getOverlap(newVerfuegungGueltigkeit).isPresent()) {
				lastVerfuegungsZeitabschnitte.add(verfuegungZeitabschnitt);
			}
		}
		return lastVerfuegungsZeitabschnitte;
	}

	@Nonnull
	private AbstractPlatz calculateAndExtractPlatz(@Nonnull String gesuchId, @Nonnull String platzId) {
		Gesuch gesuch = gesuchService.findGesuch(gesuchId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("calculateAndExtractVerfuegung", gesuchId));
		// Wir muessen hier die Berechnung der Verfuegung nochmals neu vornehmen
		Gesuch gesuchWithCalcVerfuegung = calculateVerfuegung(gesuch);
		// Die berechnete Verfügung ermitteln
		AbstractPlatz verfuegungToPersist = gesuchWithCalcVerfuegung.extractAllPlaetze().stream()
			.filter(betreuung -> platzId.equals(betreuung.getId()))
			.findFirst()
			.orElseThrow(() -> new EbeguEntityNotFoundException("calculateAndExtractVerfuegung", platzId));
		return verfuegungToPersist;
	}

	@Nonnull
	public Verfuegung calculateFamGroessenVerfuegung(@Nonnull Gesuch gesuch, @Nonnull Sprache sprache) {
		finanzielleSituationService.calculateFinanzDaten(gesuch);

		// Die Betreuungen mit ihren Vorgängern initialisieren, damit der MutationsMerger funktioniert!
		initializeVorgaengerVerfuegungen(gesuch);

		final BetreuungsgutscheinEvaluator evaluator = initEvaluatorForFamGroessenVerfuegung(gesuch, sprache.getLocale());
		return evaluator.evaluateFamiliensituation(gesuch, sprache.getLocale());
	}

	@Nonnull
	private BetreuungsgutscheinEvaluator initEvaluatorForFamGroessenVerfuegung(@Nonnull Gesuch gesuch, @Nonnull Locale locale) {
		KitaxUebergangsloesungParameter kitaxParameter = loadKitaxUebergangsloesungParameter(Objects.requireNonNull(
				gesuch.getFall().getMandant()));
		List<Rule> rules =
			rulesService.getRulesForGesuchsperiode(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), kitaxParameter, locale);
		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED,
			gesuch.getFall().getMandant(),true);
		Map<EinstellungKey, Einstellung> einstellungMap = einstellungService.loadRuleParameters(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), BetreuungsgutscheinEvaluator.getRequiredParametersForAbschlussRules());
		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput, einstellungMap);
		loadCalculatorParameters(gesuch.extractGemeinde(), gesuch.getGesuchsperiode());
		return bgEvaluator;
	}

	@Nonnull
	public BGRechnerParameterDTO loadCalculatorParameters(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EinstellungKey, Einstellung> paramMap =
			einstellungService.getAllEinstellungenByGemeindeAsMap(gemeinde, gesuchsperiode);
		return new BGRechnerParameterDTO(paramMap, gesuchsperiode, gemeinde);
	}

	@Nonnull
	public KitaxUebergangsloesungParameter loadKitaxUebergangsloesungParameter(@Nonnull Mandant mandant) {
		Collection<KitaxUebergangsloesungInstitutionOeffnungszeiten> oeffnungszeiten =
			criteriaQueryHelper.getAll(KitaxUebergangsloesungInstitutionOeffnungszeiten.class);
		return new KitaxUebergangsloesungParameter(
			applicationPropertyService.getStadtBernAsivStartDatum(mandant),
			applicationPropertyService.isStadtBernAsivConfigured(mandant),
			oeffnungszeiten
		);
	}

	/**
	 * @return gibt die Verfuegung der vorherigen verfuegten Betreuung zurueck.
	 */
	@Nonnull
	protected Optional<Verfuegung> findVorgaengerVerfuegung(@Nonnull AbstractPlatz abstractPlatz) {
		final Optional<AbstractPlatz> vorgaengerPlatz = findVorgaengerPlatz(abstractPlatz);
		return vorgaengerPlatz.map(AbstractPlatz::getVerfuegung);
	}
}
