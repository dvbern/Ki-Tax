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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.outbox.verfuegung.VerfuegungEventConverter;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.VerfuegungUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service zum berechnen und speichern der Verfuegung
 */
@Stateless
@Local(VerfuegungService.class)
@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TRAEGERSCHAFT,
	SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, STEUERAMT, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
public class VerfuegungServiceBean extends AbstractBaseService implements VerfuegungService {

	private static final Logger LOG = LoggerFactory.getLogger(VerfuegungServiceBean.class);

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

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
	private Event<ExportedEvent> event;

	@Inject
	private VerfuegungEventConverter verfuegungEventConverter;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Verfuegung verfuegen(@Nonnull Verfuegung verfuegung, @Nonnull String betreuungId, boolean ignorieren) {
		setZahlungsstatus(verfuegung, betreuungId, ignorieren);
		final Verfuegung persistedVerfuegung = persistVerfuegung(verfuegung, betreuungId, Betreuungsstatus.VERFUEGT);
		wizardStepService.updateSteps(persistedVerfuegung.getBetreuung().extractGesuch().getId(), null, null, WizardStepName.VERFUEGEN);

		// Dokument erstellen
		Betreuung betreuung = persistedVerfuegung.getBetreuung();
		generateVerfuegungDokument(betreuung);

		event.fire(verfuegungEventConverter.of(persistedVerfuegung));

		mailService.sendInfoBetreuungVerfuegt(betreuung);
		return persistedVerfuegung;
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public void generateVerfuegungDokument(@Nonnull Betreuung betreuung) {
		try {
			generatedDokumentService
				.getVerfuegungDokumentAccessTokenGeneratedDokument(betreuung.extractGesuch(), betreuung, "", true);
		} catch (IOException | MimeTypeParseException | MergeDocException e) {
			throw new EbeguRuntimeException("generateVerfuegungDokument", "Verfuegung-Dokument konnte nicht erstellt werden"
				+ betreuung.getId(), e);
		}
	}

	@SuppressWarnings("LocalVariableNamingConvention")
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public void setZahlungsstatus(Verfuegung verfuegung, @Nonnull String betreuungId, boolean ignorieren) {
		Betreuung betreuung = persistence.find(Betreuung.class, betreuungId);
		Objects.requireNonNull(betreuung);
		final Gesuch gesuch = betreuung.extractGesuch();

		// Zahlungsstatus muss nur bei Mutationen und Angebote der Art KITA und TAGESELTERN aktualisiert werden
		if (gesuch.isMutation() && betreuung.isAngebotAuszuzahlen()) {
			Optional<Verfuegung> vorgaengerAusbezahlteVerfuegungOpt = findVorgaengerAusbezahlteVerfuegung(betreuung);

			if (vorgaengerAusbezahlteVerfuegungOpt.isPresent()) {

				final Verfuegung vorgaengerAusbezahlteVerfuegung = vorgaengerAusbezahlteVerfuegungOpt.get();

				for (VerfuegungZeitabschnitt verfuegungZeitabschnittNeu : verfuegung.getZeitabschnitte()) {

					List<VerfuegungZeitabschnitt> zeitabschnitteOnVorgaengerAusbezahlteVerfuegung =
						findZeitabschnitteOnVerfuegung(verfuegungZeitabschnittNeu.getGueltigkeit(), vorgaengerAusbezahlteVerfuegung);

					Optional<VerfuegungZeitabschnitt> zeitabschnittSameGueltigkeitSameBetrag = VerfuegungUtil.findZeitabschnittSameGueltigkeitSameBetrag
						(zeitabschnitteOnVorgaengerAusbezahlteVerfuegung, verfuegungZeitabschnittNeu);

					// Folgende Informationen werden fuer die Berechnung des Status benoetigt:
					boolean sameGueltigkeitSameBetrag = zeitabschnittSameGueltigkeitSameBetrag.isPresent();
					boolean zeitraumBereitsVerrechnet = areAllZeitabschnitteVerrechnet(zeitabschnitteOnVorgaengerAusbezahlteVerfuegung); // Alles ausser NEU
					boolean voraengerIgnoriertUndAusbezahlt = isThereAnyIgnoriert(zeitabschnitteOnVorgaengerAusbezahlteVerfuegung);

					LOG.debug("Verfüge {}, sameGueltigkeitSameBetrag={}, zeitraumBereitsVerrechnet={}, voraengerIgnoriertUndAusbezahlt={}",
						verfuegungZeitabschnittNeu.getGueltigkeit().toRangeString(), sameGueltigkeitSameBetrag, zeitraumBereitsVerrechnet,
						voraengerIgnoriertUndAusbezahlt);

					// Es gelten folgende Regeln:
					// - Wenn ein Zeitraum bereits einmal ignoriert und ausbezahlt wurde, muss er auch kuenftig immer ausbezahlt werden
					// - Wenn ein Zeitraum noch nie verrechnet wurde, erhaelt er den Status neu
					// - Wenn der Zeitraum verrechnet wurde -> VERRECHNEND (wir muessen nochmals auszahlen), *ausser* es wurde
					// 		das "ignorieren" Flag gesetzt -> IGNORIEREND
					// 		=> Wenn der Betrag nicht geändert hat, sollten wir nicht auf IGNORIEREND setzen, egal wie das Flag war.
					// 		Wichtig ist, dass auf dieser Verfuegung (die noch nicht ausbezahlt war) nie ein "behandelter" Status gesetzt wird
					//		da wir sonst bei einer weiteren Mutation die falsche Vorgängerverfügung verwenden!
					if (voraengerIgnoriertUndAusbezahlt) {
						verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT);
					} else if (!zeitraumBereitsVerrechnet) {
						verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.NEU);
					} else if (!sameGueltigkeitSameBetrag) {
						// Wenn der Betrag und die Gueltigkeit gleich bleibt: Wir wurden gar nicht gefragt, ob wir
						// ignorieren wollen -> wir lassen den letzten bekannten Status!
						if (ignorieren ) {
							verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.IGNORIEREND);
						} else {
							verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.VERRECHNEND);
						}
					} else  {
						// Es war verrechnet UND derselbe Betrag. Wir muessen den Status trotzdem auf etwas "nicht-behandeltes"
						// zuruecksetzen!
						if (verfuegungZeitabschnittNeu.getZahlungsstatus().isVerrechnet()) {
							verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.VERRECHNEND);
						}
					}

					VerfuegungZeitabschnitt vorgaener = CollectionUtils.isNotEmpty(zeitabschnitteOnVorgaengerAusbezahlteVerfuegung) ?
						zeitabschnitteOnVorgaengerAusbezahlteVerfuegung.get(0) : null;
					VerfuegungsZeitabschnittZahlungsstatus statusVorgaenger = vorgaener != null ? vorgaener.getZahlungsstatus() : null;
					LOG.debug("Zeitabschnitt {} VORHER={} NEU={}",
						verfuegungZeitabschnittNeu.getGueltigkeit().toRangeString(),
						statusVorgaenger,
						verfuegungZeitabschnittNeu.getZahlungsstatus());
				}
			}
		}
	}

	private boolean areAllZeitabschnitteVerrechnet(@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte.stream()
			.allMatch(verfuegungZeitabschnitt -> verfuegungZeitabschnitt.getZahlungsstatus().isBereitsBehandeltInZahlungslauf());
	}

	private boolean isThereAnyIgnoriert(@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte.stream()
			.anyMatch(verfuegungZeitabschnitt -> verfuegungZeitabschnitt.getZahlungsstatus().isIgnoriert());
	}

	private void setVerfuegungsKategorien(Verfuegung verfuegung) {
		if (!verfuegung.isKategorieNichtEintreten()) {
			for (VerfuegungZeitabschnitt zeitabschnitt : verfuegung.getZeitabschnitte()) {
				if (zeitabschnitt.isKategorieKeinPensum()) {
					verfuegung.setKategorieKeinPensum(true);
				}
				if (zeitabschnitt.isKategorieMaxEinkommen()) {
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
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Verfuegung nichtEintreten(@Nonnull Verfuegung verfuegung, @Nonnull String betreuungId) {
		// Bei Nicht-Eintreten muss der Anspruch auf der Verfuegung auf 0 gesetzt werden, da diese u.U. bei Mutationen
		// als Vergleichswert hinzugezogen werden
		for (VerfuegungZeitabschnitt zeitabschnitt : verfuegung.getZeitabschnitte()) {
			zeitabschnitt.setAnspruchberechtigtesPensum(0);
		}
		verfuegung.setKategorieNichtEintreten(true);
		initializeVorgaengerVerfuegungen(verfuegung.getBetreuung().extractGesuch());
		final Verfuegung persistedVerfuegung = persistVerfuegung(verfuegung, betreuungId, Betreuungsstatus.NICHT_EINGETRETEN);
		wizardStepService.updateSteps(persistedVerfuegung.getBetreuung().extractGesuch().getId(), null, null, WizardStepName.VERFUEGEN);
		// Dokument erstellen
		Betreuung betreuung = verfuegung.getBetreuung();
		try {
			generatedDokumentService
				.getNichteintretenDokumentAccessTokenGeneratedDokument(betreuung, true);
		} catch (IOException | MimeTypeParseException | MergeDocException e) {
			throw new EbeguRuntimeException("nichtEintreten", "Nichteintretensverfuegung-Dokument konnte nicht "
				+ "erstellt werden" + betreuungId, e);
		}
		return persistedVerfuegung;
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Verfuegung persistVerfuegung(@Nonnull Verfuegung verfuegung, @Nonnull String betreuungId, @Nonnull Betreuungsstatus betreuungsstatus) {
		Objects.requireNonNull(verfuegung);
		Objects.requireNonNull(betreuungId);

		setVerfuegungsKategorien(verfuegung);
		Betreuung betreuung = persistence.find(Betreuung.class, betreuungId);
		betreuung.setBetreuungsstatus(betreuungsstatus);
		// Gueltigkeit auf dem neuen setzen, auf der bisherigen entfernen
		betreuung.setGueltig(true);
		Optional<Verfuegung> vorgaengerVerfuegungOptional = findVorgaengerVerfuegung(betreuung);
		if (vorgaengerVerfuegungOptional.isPresent()) {
			Verfuegung vorgaengerVerfuegung = vorgaengerVerfuegungOptional.get();
			vorgaengerVerfuegung.getBetreuung().setGueltig(false);
		}
		// setting all depending objects
		verfuegung.setBetreuung(betreuung);
		betreuung.setVerfuegung(verfuegung);
		verfuegung.getZeitabschnitte().forEach(verfZeitabsch -> verfZeitabsch.setVerfuegung(verfuegung));
		authorizer.checkWriteAuthorization(verfuegung);

		Verfuegung persist = persistence.persist(verfuegung);
		persistence.merge(betreuung);
		return persist;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Optional<Verfuegung> findVerfuegung(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Verfuegung a = persistence.find(Verfuegung.class, id);
		authorizer.checkReadAuthorization(a);
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Collection<Verfuegung> getAllVerfuegungen() {
		Collection<Verfuegung> verfuegungen = criteriaQueryHelper.getAll(Verfuegung.class);
		authorizer.checkReadAuthorizationVerfuegungen(verfuegungen);
		return verfuegungen;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, STEUERAMT, ADMIN_TS, SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Gesuch calculateVerfuegung(@Nonnull Gesuch gesuch) {
		this.finanzielleSituationService.calculateFinanzDaten(gesuch);

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);

		final List<Rule> rules = rulesService
			.getRulesForGesuchsperiode(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), sprache.getLocale());

		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED, true);
		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput);
		BGRechnerParameterDTO calculatorParameters = loadCalculatorParameters(gesuch.extractGemeinde(), gesuch.getGesuchsperiode());

		// Finde und setze die letzte Verfuegung für die Betreuung für den Merger und Vergleicher.
		// Bei GESCHLOSSEN_OHNE_VERFUEGUNG wird solange ein Vorgänger gesucht, bis  dieser gefunden wird. (Rekursiv)
		initializeVorgaengerVerfuegungen(gesuch);

		bgEvaluator.evaluate(gesuch, calculatorParameters, sprache.getLocale());
		authorizer.checkReadAuthorizationForAnyBetreuungen(gesuch.extractAllBetreuungen()); // betreuungen pruefen reicht hier glaub
		return gesuch;
	}

	@Override
	@Nonnull
	public Verfuegung getEvaluateFamiliensituationVerfuegung(@Nonnull Gesuch gesuch) {
		this.finanzielleSituationService.calculateFinanzDaten(gesuch);

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);

		final List<Rule> rules = rulesService
			.getRulesForGesuchsperiode(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), sprache.getLocale());
		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED, true);
		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput);

		initializeVorgaengerVerfuegungen(gesuch);

		return bgEvaluator.evaluateFamiliensituation(gesuch, sprache.getLocale());
	}

	@Override
	public void initializeVorgaengerVerfuegungen(@Nonnull Gesuch gesuch) {
		gesuch.getKindContainers()
			.stream()
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.forEach(this::setVorgaengerVerfuegungen);
	}

	private void setVorgaengerVerfuegungen(@Nonnull Betreuung betreuung) {
		Verfuegung vorgaengerAusbezahlteVerfuegung = findVorgaengerAusbezahlteVerfuegung(betreuung)
			.orElse(null);

		Verfuegung vorgaengerVerfuegung = findVorgaengerVerfuegung(betreuung)
			.orElse(null);

		betreuung.initVorgaengerVerfuegungen(vorgaengerVerfuegung, vorgaengerAusbezahlteVerfuegung);
	}

	/**
	 * @return gibt die Verfuegung der vorherigen verfuegten Betreuung zurueck.
	 */
	@Nonnull
	private Optional<Verfuegung> findVorgaengerVerfuegung(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung darf nicht null sein");
		if (betreuung.getVorgaengerId() == null) {
			return Optional.empty();
		}

		// Achtung, hier wird persistence.find() verwendet, da ich fuer das Vorgaengergesuch evt. nicht
		// Leseberechtigt bin, fuer die Mutation aber schon!
		Betreuung vorgaengerbetreuung = persistence.find(Betreuung.class, betreuung.getVorgaengerId());
		if (vorgaengerbetreuung != null) {
			if (vorgaengerbetreuung.getBetreuungsstatus() != Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG) {
				// Hier kann aus demselben Grund die Berechtigung fuer die Vorgaengerverfuegung nicht geprueft werden
				return Optional.ofNullable(vorgaengerbetreuung.getVerfuegung());
			}
			return findVorgaengerVerfuegung(vorgaengerbetreuung);
		}
		return Optional.empty();
	}

	/**
	 * @return gibt die Verfuegung der vorherigen verfuegten Betreuung zurueck, die ausbezahlt ist.
	 */
	@Nonnull
	private Optional<Verfuegung> findVorgaengerAusbezahlteVerfuegung(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung darf nicht null sein");
		if (betreuung.getVorgaengerId() == null) {
			return Optional.empty();
		}

		// Achtung, hier wird persistence.find() verwendet, da ich fuer das Vorgaengergesuch evt. nicht
		// Leseberechtigt bin, fuer die Mutation aber schon!
		Betreuung vorgaengerbetreuung = persistence.find(Betreuung.class, betreuung.getVorgaengerId());
		if (vorgaengerbetreuung != null) {
			if (vorgaengerbetreuung.getBetreuungsstatus() != Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG
				&& isAusbezahlt(vorgaengerbetreuung)) {
				// Hier kann aus demselben Grund die Berechtigung fuer die Vorgaengerverfuegung nicht geprueft werden
				return Optional.ofNullable(vorgaengerbetreuung.getVerfuegung());
			}
			return findVorgaengerAusbezahlteVerfuegung(vorgaengerbetreuung);
		}
		return Optional.empty();
	}

	private boolean isAusbezahlt(@Nonnull Betreuung betreuung) {
		if (betreuung.getVerfuegung() == null) {
			return false;
		}
		return betreuung.getVerfuegung().getZeitabschnitte()
			.stream()
			.anyMatch(zeitabschnitt -> zeitabschnitt.getZahlungsstatus().isBereitsBehandeltInZahlungslauf());
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
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
	public void findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(
		@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Betreuung betreuungNeu,
		@Nonnull List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte) {

		findVorgaengerAusbezahlteVerfuegung(betreuungNeu)
			.map(verfuegung -> findZeitabschnitteOnVerfuegung(zeitabschnittNeu.getGueltigkeit(), verfuegung))
			.ifPresent(zeitabschnitte -> zeitabschnitte.forEach(zeitabschnitt -> {

				VerfuegungsZeitabschnittZahlungsstatus zahlungsstatus = zeitabschnitt.getZahlungsstatus();

				if ((zahlungsstatus.isVerrechnet() || zahlungsstatus.isIgnoriert())
					&& isNotInZeitabschnitteList(zeitabschnitt, vorgaengerZeitabschnitte)) {
					// Diesen ins Result, iteration weiterführen und von allen den Vorgänger suchen bis VERRECHNET oder
					// kein Vorgaenger
					vorgaengerZeitabschnitte.add(zeitabschnitt);
				} else {
					Betreuung vorgaengerBetreuung = zeitabschnitt.getVerfuegung().getBetreuung();
					// Es gab keine bereits Verrechneten Zeitabschnitte auf dieser Verfuegung -> eins weiter
					// zurueckgehen
					findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(
						zeitabschnittNeu,
						vorgaengerBetreuung,
						vorgaengerZeitabschnitte);
				}
			}));
	}

	@Nonnull
	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public List<VerfuegungZeitabschnitt> findZeitabschnitteByYear(int year) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = cb.createQuery(VerfuegungZeitabschnitt.class);

		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);

		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);

		ParameterExpression<Integer> parameterYear = cb.parameter(Integer.class, "year");
		Predicate predicateYear = cb.equal(
			cb.function(
				"YEAR",
				Integer.class,
				root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)
			),
			parameterYear
		);

		Predicate predicateGueltig = cb.isTrue(joinBetreuung.get(Betreuung_.gueltig));

		query.where(cb.and(predicateYear, predicateGueltig));

		TypedQuery<VerfuegungZeitabschnitt> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(parameterYear, year);

		return typedQuery.getResultList();
	}

	/**
	 * Mithilfe der Methode isSamePersistedValues schaut ob der uebergebene Zeitabschnitt bereits in der uebergebenen Liste existiert.
	 * Alle Felder muessen verglichen werden.
	 */
	private boolean isNotInZeitabschnitteList(VerfuegungZeitabschnitt zeitabschnitt, List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte) {
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : vorgaengerZeitabschnitte) {
			if (verfuegungZeitabschnitt.isSamePersistedValues(zeitabschnitt)) {
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
}
