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
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.errors.*;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.outbox.anmeldung.AnmeldungTagesschuleEventConverter;
import ch.dvbern.ebegu.outbox.platzbestaetigung.BetreuungAnfrageAddedEvent;
import ch.dvbern.ebegu.outbox.platzbestaetigung.BetreuungAnfrageEventConverter;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.util.FilterFunctions;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.BetreuungUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.validationgroups.BetreuungBestaetigenValidationGroup;
import ch.dvbern.lib.cdipersistence.Persistence;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.collections4.CollectionUtils;
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
import javax.validation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service fuer Betreuung
 */
@Stateless
@Local(BetreuungService.class)
public class BetreuungServiceBean extends AbstractBaseService implements BetreuungService {

	public static final String BETREUUNG_DARF_NICHT_NULL_SEIN = "pensen darf nicht null sein";
	public static final String ID_MUSS_GESETZT_SEIN = "id muss gesetzt sein";
	public static final Institution[] INSTITUTIONS = new Institution[0];

	@Inject
	private Persistence persistence;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private MitteilungService mitteilungService;
	@Inject
	private GesuchService gesuchService;
	@Inject
	private Authorizer authorizer;
	@Inject
	private MailService mailService;
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private InstitutionStammdatenService institutionStammdatenService;
	@Inject
	private PrincipalBean principalBean;
	@Inject
	private GeneratedDokumentService generatedDokumentService;
	@Inject
	private BetreuungAnfrageEventConverter betreuungAnfrageEventConverter;
	@Inject
	private Event<ExportedEvent> event;
	@Inject
	private EbeguConfiguration ebeguConfiguration;
	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;
	@Inject
	private AnmeldungTagesschuleEventConverter anmeldungTagesschuleEventConverter;
	@Inject
	private ApplicationPropertyService applicationPropertyService;
	@Inject
	private EinstellungService einstellungService;

	private static final Logger LOG = LoggerFactory.getLogger(BetreuungServiceBean.class.getSimpleName());

	@Override
	@Nonnull
	public Betreuung saveBetreuung(
		@Valid @Nonnull Betreuung betreuung,
		@Nonnull Boolean isAbwesenheit,
		@Nullable String externalClient) {
		Objects.requireNonNull(betreuung);
		authorizer.checkWriteAuthorization(betreuung);

		checkNotKorrekturmodusWithFerieninselOnly(betreuung.extractGesuch());

		boolean isNew = betreuung.isNew(); // needed hier before it gets saved

		Betreuung mergedBetreuung = persistence.merge(betreuung);

		Mandant mandant = betreuung.extractGemeinde().getMandant();

		// if isNew or not published and Jugendamt -> generate Event for Kafka when API enabled and institution bekannt
		if (exportBetreuung(isNew, mergedBetreuung)) {
			if (ebeguConfiguration.isBetreuungAnfrageApiEnabled()
				&& applicationPropertyService.isPublishSchnittstelleEventsAktiviert(Objects.requireNonNull(mandant))
				&& !Constants.ALL_UNKNOWN_INSTITUTION_IDS.contains(betreuung.getInstitutionStammdaten()
				.getId())) {
				BetreuungAnfrageAddedEvent betreuungAnfrageAddedEvent =
					betreuungAnfrageEventConverter.of(mergedBetreuung);
				this.event.fire(betreuungAnfrageAddedEvent);
				mergedBetreuung.setEventPublished(true);
			} else {
				mergedBetreuung.setEventPublished(false);
			}
		}

		// we need to manually add this new Betreuung to the Kind
		final Set<Betreuung> betreuungen = mergedBetreuung.getKind().getBetreuungen();
		betreuungen.add(mergedBetreuung);
		mergedBetreuung.getKind().setBetreuungen(betreuungen);

		//jetzt noch wizard step updaten
		updateWizardSteps(mergedBetreuung, isAbwesenheit);

		Gesuch mergedGesuch = gesuchService.updateBetreuungenStatus(mergedBetreuung.extractGesuch());

		updateVerantwortliche(mergedGesuch, mergedBetreuung, false, isNew);

		if (!isNew) {
			LOG.info(
				"Betreuung mit RefNr: {} wurde geaendert und gespeichert mit Status: {}",
				mergedBetreuung.getBGNummer(),
				mergedBetreuung.getBetreuungsstatus());

			betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
				mergedBetreuung.getBGNummer(),
				externalClient != null ? externalClient : principalBean.getBenutzer().getUsername(),
				"Die Betreuung wurde geaendert und gespeichert mit Status: " + mergedBetreuung.getBetreuungsstatus(),
				LocalDateTime.now()));
		} else {
			LOG.info(
				"Betreuung mit RefNr: {} wurde erstellt mit Status: {}",
				mergedBetreuung.getBGNummer(),
				mergedBetreuung.getBetreuungsstatus());

			betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
				mergedBetreuung.getBGNummer(),
				externalClient != null ? externalClient : principalBean.getBenutzer().getUsername(),
				"Die Betreuung wurde erstellt mit Status: " + mergedBetreuung.getBetreuungsstatus(),
				LocalDateTime.now()));
		}

		return mergedBetreuung;
	}

	private boolean exportBetreuung(boolean isNew, @Nonnull Betreuung mergedBetreuung) {
		return (isNew || !mergedBetreuung.isEventPublished())
			&& mergedBetreuung.getBetreuungsangebotTyp().isJugendamt();
	}

	@Nonnull
	@Override
	public AnmeldungTagesschule saveAnmeldungTagesschule(
		@Nonnull AnmeldungTagesschule anmeldungTagesschule
	) {
		Objects.requireNonNull(anmeldungTagesschule);
		authorizer.checkWriteAuthorization(anmeldungTagesschule);

		checkNotKorrekturmodusWithFerieninselOnly(anmeldungTagesschule.extractGesuch());

		boolean isNew = anmeldungTagesschule.isNew(); // needed hier before it gets saved

		// Wir setzen auch Schulamt-Betreuungen auf gueltig, for future use
		updateGueltigFlagOnPlatzAndVorgaenger(anmeldungTagesschule);
		final AnmeldungTagesschule mergedBetreuung = persistence.merge(anmeldungTagesschule);

		// We need to update (copy) all other Betreuungen with same BGNummer (on all other Mutationen and Erstgesuch)
		final List<AbstractAnmeldung> betreuungByBGNummer = findAnmeldungenByBGNummer(mergedBetreuung.getBGNummer());
		betreuungByBGNummer.stream()
			.filter(b -> b.getBetreuungsangebotTyp().isTagesschule() && !Objects.equals(
				anmeldungTagesschule.getId(),
				b.getId()))
			.forEach(b -> {
				b.copyAnmeldung(anmeldungTagesschule);
				persistence.merge(b);
			});

		//Export Tagesschule Anmeldung oder moegliche Aenderungen an der exchange-service
		if (isAnmeldungInStatusToFireEvent(mergedBetreuung) && !mergedBetreuung.isKeineDetailinformationen()) {
			fireAnmeldungTagesschuleAddedEvent(mergedBetreuung);
		}

		// we need to manually add this new AnmeldungTagesschule to the Kind
		final Set<AnmeldungTagesschule> betreuungen = mergedBetreuung.getKind().getAnmeldungenTagesschule();
		betreuungen.add(mergedBetreuung);
		mergedBetreuung.getKind().setAnmeldungenTagesschule(betreuungen);

		//jetzt noch wizard step updaten
		updateWizardSteps(mergedBetreuung, false);

		Gesuch mergedGesuch = gesuchService.updateBetreuungenStatus(mergedBetreuung.extractGesuch());

		boolean isAnmeldungSchulamtAusgeloest =
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST == mergedBetreuung.getBetreuungsstatus();

		updateVerantwortliche(mergedGesuch, mergedBetreuung, isAnmeldungSchulamtAusgeloest, isNew);

		return mergedBetreuung;
	}

	@Override
	public void fireAnmeldungTagesschuleAddedEvent(@Nonnull AnmeldungTagesschule anmeldungTagesschule) {
		if (ebeguConfiguration.isAnmeldungTagesschuleApiEnabled() &&
			applicationPropertyService.isPublishSchnittstelleEventsAktiviert(Objects.requireNonNull(
				anmeldungTagesschule.extractGemeinde().getMandant()))) {
			event.fire(anmeldungTagesschuleEventConverter.of(anmeldungTagesschule));
			anmeldungTagesschule.setEventPublished(true);
		} else {
			anmeldungTagesschule.setEventPublished(false);
		}
	}

	private boolean isAnmeldungInStatusToFireEvent(AnmeldungTagesschule betreuung) {
		return Betreuungsstatus.getBetreuungsstatusForFireAnmeldungTagesschuleEvent()
			.contains(betreuung.getBetreuungsstatus());
	}

	@Nonnull
	@Override
	public AnmeldungFerieninsel saveAnmeldungFerieninsel(
		@Nonnull AnmeldungFerieninsel anmeldungFerieninsel) {
		Objects.requireNonNull(anmeldungFerieninsel);
		authorizer.checkWriteAuthorization(anmeldungFerieninsel);

		boolean isNew = anmeldungFerieninsel.isNew(); // needed hier before it gets saved

		// Wir setzen auch Schulamt-Betreuungen auf gueltig, for future use
		updateGueltigFlagOnPlatzAndVorgaenger(anmeldungFerieninsel);
		final AnmeldungFerieninsel mergedBetreuung = persistence.merge(anmeldungFerieninsel);

		// We need to update (copy) all other Betreuungen with same BGNummer (on all other Mutationen and Erstgesuch)
		final List<AbstractAnmeldung> betreuungByBGNummer = findAnmeldungenByBGNummer(mergedBetreuung.getBGNummer());
		betreuungByBGNummer.stream()
			.filter(b -> b.getBetreuungsangebotTyp().isFerieninsel() && !Objects.equals(
				anmeldungFerieninsel.getId(),
				b.getId()))
			.forEach(b -> {
				b.copyAnmeldung(anmeldungFerieninsel);
				persistence.merge(b);
			});

		// we need to manually add this new AnmeldungFerieninsel to the Kind
		final Set<AnmeldungFerieninsel> betreuungen = mergedBetreuung.getKind().getAnmeldungenFerieninsel();
		betreuungen.add(mergedBetreuung);
		mergedBetreuung.getKind().setAnmeldungenFerieninsel(betreuungen);

		//jetzt noch wizard step updaten
		updateWizardSteps(mergedBetreuung, false);

		Gesuch mergedGesuch = gesuchService.updateBetreuungenStatus(mergedBetreuung.extractGesuch());

		boolean isAnmeldungSchulamtAusgeloest =
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST == mergedBetreuung.getBetreuungsstatus();
		updateVerantwortliche(mergedGesuch, mergedBetreuung, isAnmeldungSchulamtAusgeloest, isNew);

		return mergedBetreuung;
	}

	private <T extends AbstractPlatz> T savePlatz(T platz, @Nullable String externalClient) {
		if (platz.getBetreuungsangebotTyp().isFerieninsel()) {
			return (T) saveAnmeldungFerieninsel((AnmeldungFerieninsel) platz);
		} else if (platz instanceof AnmeldungTagesschule) {
			return (T) saveAnmeldungTagesschule((AnmeldungTagesschule) platz);
		} else {
			return (T) saveBetreuung((Betreuung) platz, false, externalClient);
		}
	}

	private void updateWizardSteps(@Nonnull AbstractPlatz mergedBetreuung, @Nonnull Boolean isAbwesenheit) {
		if (isAbwesenheit) {
			wizardStepService.updateSteps(
				mergedBetreuung.getKind().getGesuch().getId(),
				null,
				null,
				WizardStepName.ABWESENHEIT);
		} else {
			wizardStepService.updateSteps(
				mergedBetreuung.getKind().getGesuch().getId(),
				null,
				null,
				WizardStepName.BETREUUNG);
		}
	}

	private void updateVerantwortliche(
		@Nonnull Gesuch mergedGesuch, @Nonnull AbstractPlatz mergedBetreuung,
		boolean isAnmeldungSchulamtAusgeloest, boolean isNew) {
		if (updateVerantwortlicheNeeded(mergedGesuch.getEingangsart(), isAnmeldungSchulamtAusgeloest, isNew)) {
			Optional<GemeindeStammdaten> gemeindeStammdatenOptional =
				gemeindeService.getGemeindeStammdatenByGemeindeId(mergedGesuch
					.getDossier()
					.getGemeinde()
					.getId());

			Benutzer benutzerBG = null;
			Benutzer benutzerTS = null;
			if (gemeindeStammdatenOptional.isPresent()) {
				GemeindeStammdaten gemeindeStammdaten = gemeindeStammdatenOptional.get();
				benutzerBG = gemeindeStammdaten.getDefaultBenutzerWithRoleBG().orElse(null);
				benutzerTS = gemeindeStammdaten.getDefaultBenutzerWithRoleTS().orElse(null);
			}
			gesuchService.setVerantwortliche(benutzerBG, benutzerTS, mergedBetreuung.extractGesuch(), true, true);
		}
	}

	private boolean updateVerantwortlicheNeeded(
		Eingangsart eingangsart,
		boolean isSchulamtAnmeldungAusgeloest,
		boolean isNew) {
		if (!isNew) {
			// nur neue Betreuungen duerfen den Verantwortlichen setzen
			return false;
		}
		if (eingangsart == Eingangsart.PAPIER) {
			// immer bei eingangsart Papier
			return true;
		} else if (isSchulamtAnmeldungAusgeloest) {
			// bei eingangsart Online nur wenn die Anmeldung direkt ausgelöst wurde (über TS oder FI hinzufügen Knöpfe)
			return true;
		}
		return false;
	}

	@Override
	@Nonnull
	public Betreuung betreuungPlatzAbweisen(@Valid @Nonnull Betreuung betreuung, @Nullable String externalClient) {
		Objects.requireNonNull(betreuung, BETREUUNG_DARF_NICHT_NULL_SEIN);
		betreuung.setBetreuungsstatus(Betreuungsstatus.ABGEWIESEN);
		Betreuung persistedBetreuung = saveBetreuung(betreuung, false, externalClient);
		try {
			// Bei Ablehnung einer Betreuung muss eine E-Mail geschickt werden
			mailService.sendInfoBetreuungAbgelehnt(persistedBetreuung);
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoBetreuungAbgelehnt konnte nicht verschickt werden fuer Betreuung",
				betreuung.getId());
		}
		LOG.info("Betreuung mit RefNr: {} wurde abgewiesen", betreuung.getBGNummer());
		betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
			betreuung.getBGNummer(),
			externalClient != null ? externalClient : principalBean.getBenutzer().getUsername(),
			"Die Betreuung wurde abgewiesen",
			LocalDateTime.now()));
		return persistedBetreuung;
	}

	@Override
	@Nonnull
	public Betreuung betreuungPlatzBestaetigen(@Valid @Nonnull Betreuung betreuung, @Nullable String externalClient) {
		Objects.requireNonNull(betreuung, BETREUUNG_DARF_NICHT_NULL_SEIN);

		// Erst jetzt kann der Zeitraum der Betreuung im Vergleich zur Institution geprueft werden
		Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Betreuung>> violations =
			validator.validate(betreuung, BetreuungBestaetigenValidationGroup.class);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		Betreuung persistedBetreuung = saveBetreuung(betreuung, false, externalClient);
		try {
			Gesuch gesuch = betreuung.extractGesuch();
			if (gesuch.areAllBetreuungenBestaetigt()) {
				// Sobald alle Betreuungen bestaetigt sind, eine Mail schreiben
				mailService.sendInfoBetreuungenBestaetigt(gesuch);
			}
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoBetreuungenBestaetigt konnte nicht verschickt werden fuer Betreuung",
				betreuung.getId());
		}
		LOG.info("Betreuung mit RefNr: {} wurde bestaetigt", betreuung.getBGNummer());
		betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
			betreuung.getBGNummer(),
			externalClient != null ? externalClient : principalBean.getBenutzer().getUsername(),
			"Die Betreuung wurde bestaetigt",
			LocalDateTime.now()));
		return persistedBetreuung;
	}

	@Override
	@Nonnull
	public AbstractAnmeldung anmeldungSchulamtModuleAkzeptieren(@Valid @Nonnull AbstractAnmeldung anmeldung) {
		Objects.requireNonNull(anmeldung, BETREUUNG_DARF_NICHT_NULL_SEIN);
		authorizer.checkWriteAuthorization(anmeldung);

		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT);
		AbstractAnmeldung persistedBetreuung = savePlatz(anmeldung, null);
		if (anmeldung.getBetreuungsangebotTyp().isTagesschule()) {
			AnmeldungTagesschule anmeldungTagesschule =
				findAnmeldungTagesschule(anmeldung.getId()).orElseThrow(() -> new EbeguEntityNotFoundException(
					"anmeldungSchulamtModuleAkzeptieren",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"AnmeldungTagesschule: " + anmeldung.getId()));
			try {
				// Bei Akzeptieren einer Anmeldung muss eine E-Mail geschickt werden
				GemeindeStammdaten gemeindeStammdaten =
					gemeindeService.getGemeindeStammdatenByGemeindeId(persistedBetreuung.extractGesuch()
						.getDossier()
						.getGemeinde()
						.getId()).get();
				if (gemeindeStammdaten.getBenachrichtigungTsEmailAuto() && !anmeldungTagesschule.isTagesschuleTagi()) {
					mailService.sendInfoSchulamtAnmeldungTagesschuleAkzeptiert(persistedBetreuung);
				}
			} catch (MailException e) {
				logExceptionAccordingToEnvironment(
					e,
					"Mail InfoSchulamtAnmeldungUebernommen konnte nicht verschickt werden fuer Betreuung",
					anmeldung.getId());
			}
			generateAnmeldebestaetigungDokument(persistedBetreuung, false);
		}

		return persistedBetreuung;
	}

	@Override
	@Nonnull
	public AbstractAnmeldung anmeldungMutationIgnorieren(@Nonnull @Valid AbstractAnmeldung anmeldung) {
		Objects.requireNonNull(anmeldung, BETREUUNG_DARF_NICHT_NULL_SEIN);
		authorizer.checkWriteAuthorization(anmeldung);

		// vorgänger zurücksetzen
		Objects.requireNonNull(anmeldung.getVorgaengerId(), "Eine Anmeldung kann nicht ignoriert werden, wenn sie neu ist");
		setVorgaengerAnmeldungToGueltig(anmeldung);

		anmeldung.setGueltig(false);
		anmeldung.setStatusVorIgnorieren(anmeldung.getBetreuungsstatus());
		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_MUTATION_IGNORIERT);
		anmeldung.setAnmeldungMutationZustand(AnmeldungMutationZustand.IGNORIERT);

		return persistence.merge(anmeldung);
	}

	private void setVorgaengerAnmeldungToGueltig(@Nonnull AbstractAnmeldung anmeldung) {
		AbstractAnmeldung vorgaenger = findVorgaengerAnmeldungNotIgnoriert(anmeldung);
		vorgaenger.setAnmeldungMutationZustand(AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
		vorgaenger.setGueltig(true); // Die alte Anmeldung ist wieder die gueltige
		persistence.merge(vorgaenger);
	}

	@Override
	@Nonnull
	public AbstractAnmeldung findVorgaengerAnmeldungNotIgnoriert(AbstractAnmeldung betreuung) {
		if (betreuung.getVorgaengerId() == null) {
			//Kann eigentlich nicht eintretten, da das Erst-Gesuch nicht ingoriert werden kann und somit immer ein Vorgänger
			//gfunden werden kann, welcher nicht ignoiert ist
			throw new EbeguRuntimeException(
				"findVorgaengerAnmeldungNotIgnoriert",
				ErrorCodeEnum.ERROR_NICHT_IGNORIERTER_VORGAENGER_NOT_FOUND,
				betreuung.getId());
		}

		AbstractAnmeldung vorgaenger = findAnmeldung(betreuung.getVorgaengerId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"findVorgaengerAnmeldungNotIgnoriert",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				betreuung.getVorgaengerId()));

		if (vorgaenger.getBetreuungsstatus().isIgnoriert()) {
			return findVorgaengerAnmeldungNotIgnoriert(vorgaenger);
		}

		return vorgaenger;
	}

	@Nonnull
	@Override
	public List<AnmeldungTagesschule> findAnmeldungenTagesschuleByInstitution(@Nonnull final Institution institution) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<AnmeldungTagesschule> query = cb.createQuery(AnmeldungTagesschule.class);

		Root<AnmeldungTagesschule> root = query.from(AnmeldungTagesschule.class);
		final Join<AnmeldungTagesschule, InstitutionStammdaten> stammdatenJoin = root.join(AbstractPlatz_.INSTITUTION_STAMMDATEN, JoinType.LEFT);
		Predicate predicate = cb.equal(stammdatenJoin.get(InstitutionStammdaten_.INSTITUTION), institution);
		query.where(predicate);
		return persistence.getCriteriaResults(query);
	}

	/**
	 * Generiert das Anmeldebestaetigungsdokument.
	 *
	 * @param anmeldung AbstractAnmeldung, fuer die das Dokument generiert werden soll.
	 */
	private void generateAnmeldebestaetigungDokument(@Nonnull AbstractAnmeldung anmeldung, boolean mitTarif) {
		try {
			Gesuch gesuch = anmeldung.extractGesuch();

			//noinspection ResultOfMethodCallIgnored
			generatedDokumentService.getAnmeldeBestaetigungDokumentAccessTokenGeneratedDokument(gesuch, anmeldung,
				mitTarif, true);
		} catch (MimeTypeParseException | MergeDocException e) {
			throw new EbeguRuntimeException(
				"AnmeldebestaetigungsDokument",
				"Anmeldebestaetigung-Dokument konnte nicht erstellt werden"
					+ anmeldung.getId(), e);
		}
	}

	@Override
	@Nonnull
	public AbstractAnmeldung anmeldungSchulamtAblehnen(@Valid @Nonnull AbstractAnmeldung anmeldung) {
		Objects.requireNonNull(anmeldung, BETREUUNG_DARF_NICHT_NULL_SEIN);
		authorizer.checkWriteAuthorization(anmeldung);

		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT);
		AbstractAnmeldung persistedBetreuung = savePlatz(anmeldung, null);
		boolean tagis = false;
		if (anmeldung.getBetreuungsangebotTyp().isTagesschule()) {
			AnmeldungTagesschule anmeldungTagesschule =
				findAnmeldungTagesschule(anmeldung.getId()).orElseThrow(() -> new EbeguEntityNotFoundException(
					"anmeldungSchulamtModuleAkzeptieren",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"AnmeldungTagesschule: " + anmeldung.getId()));
			tagis = anmeldungTagesschule.isTagesschuleTagi();
		}
		try {
			// Bei Ablehnung einer Anmeldung muss eine E-Mail geschickt werden
			GemeindeStammdaten gemeindeStammdaten =
				gemeindeService.getGemeindeStammdatenByGemeindeId(persistedBetreuung.extractGesuch()
					.getDossier()
					.getGemeinde()
					.getId()).get();
			if (gemeindeStammdaten.getBenachrichtigungTsEmailAuto() && !tagis) {
				mailService.sendInfoSchulamtAnmeldungAbgelehnt(persistedBetreuung);
			}
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoSchulamtAnmeldungAbgelehnt konnte nicht verschickt werden fuer Betreuung",
				anmeldung.getId());
		}
		return persistedBetreuung;
	}

	@Override
	@Nonnull
	public AbstractAnmeldung anmeldungSchulamtFalscheInstitution(@Valid @Nonnull AbstractAnmeldung anmeldung) {
		Objects.requireNonNull(anmeldung, BETREUUNG_DARF_NICHT_NULL_SEIN);
		authorizer.checkWriteAuthorization(anmeldung);

		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION);
		AbstractAnmeldung persistedBetreuung = savePlatz(anmeldung, null);
		return persistedBetreuung;
	}

	@Override
	@Nonnull
	public Optional<Betreuung> findBetreuung(@Nonnull String key) {
		return findBetreuung(key, true);
	}

	@Override
	@Nonnull
	public Optional<AnmeldungTagesschule> findAnmeldungTagesschule(@Nonnull String id) {
		Objects.requireNonNull(id, ID_MUSS_GESETZT_SEIN);
		AnmeldungTagesschule betr = persistence.find(AnmeldungTagesschule.class, id);
		if (betr != null) {
			authorizer.checkReadAuthorization(betr);
		}
		return Optional.ofNullable(betr);
	}

	@Override
	@Nonnull
	public Optional<AnmeldungFerieninsel> findAnmeldungFerieninsel(@Nonnull String id) {
		Objects.requireNonNull(id, ID_MUSS_GESETZT_SEIN);
		AnmeldungFerieninsel betr = persistence.find(AnmeldungFerieninsel.class, id);
		if (betr != null) {
			authorizer.checkReadAuthorization(betr);
		}
		return Optional.ofNullable(betr);
	}

	@Override
	@Nonnull
	public Optional<? extends AbstractAnmeldung> findAnmeldung(@Nonnull String id) {
		Optional<AnmeldungTagesschule> anmeldungTagesschule = findAnmeldungTagesschule(id);
		if (anmeldungTagesschule.isPresent()) {
			return anmeldungTagesschule;
		}
		Optional<AnmeldungFerieninsel> anmeldungFerieninsel = findAnmeldungFerieninsel(id);
		return anmeldungFerieninsel;
	}

	@Override
	@Nonnull
	public Optional<? extends AbstractPlatz> findPlatz(@Nonnull String id) {
		Optional<Betreuung> anmeldungTagesschule = findBetreuung(id);
		if (anmeldungTagesschule.isPresent()) {
			return anmeldungTagesschule;
		}
		Optional<? extends AbstractAnmeldung> anmeldungFerieninsel = findAnmeldung(id);
		return anmeldungFerieninsel;
	}

	@Override
	@Nonnull
	public Optional<Betreuung> findBetreuung(@Nonnull String key, boolean doAuthCheck) {
		Objects.requireNonNull(key, ID_MUSS_GESETZT_SEIN);
		Betreuung betr = persistence.find(Betreuung.class, key);
		if (doAuthCheck && betr != null) {
			authorizer.checkReadAuthorization(betr);
		}
		return Optional.ofNullable(betr);
	}

	@Override
	@Nonnull
	public List<AbstractAnmeldung> findAnmeldungenByBGNummer(@Nonnull String bgNummer) {
		//TODO: refactor with change in KIBON-2169
		Mandant mandant = getMandantFromBgNummer(bgNummer);
		List<AbstractAnmeldung> result = new ArrayList<>();
		result.addAll(findAnmeldungenByBGNummer(AnmeldungTagesschule.class, bgNummer, false, mandant));
		result.addAll(findAnmeldungenByBGNummer(AnmeldungFerieninsel.class, bgNummer, false, mandant));
		return result;
	}

	private Mandant getMandantFromBgNummer(String refnr) {
		final int gemeindeNummer = BetreuungUtil.getGemeindeFromBGNummer(refnr);
		Gemeinde gemeinde = gemeindeService.getGemeindeByGemeindeNummer(gemeindeNummer).orElseThrow(() ->
			new EbeguEntityNotFoundException("getGemeindeByGemeindeNummer", gemeindeNummer));

		return gemeinde.getMandant();
	}

	@Override
	public List<AbstractAnmeldung> findNewestAnmeldungByBGNummer(@Nonnull String bgNummer) {
		Mandant mandant = getMandantFromBgNummer(bgNummer);
		List<AbstractAnmeldung> result = new ArrayList<>();
		result.addAll(findAnmeldungenByBGNummer(AnmeldungTagesschule.class, bgNummer, true, mandant));
		result.addAll(findAnmeldungenByBGNummer(AnmeldungFerieninsel.class, bgNummer, true, mandant));
		return result;
	}

	@Nonnull
	private <T extends AbstractAnmeldung> List<T> findAnmeldungenByBGNummer(
		@Nonnull Class<T> clazz,
		@Nonnull String bgNummer, boolean getOnlyAktuelle, @Nonnull Mandant mandant) {
		final int betreuungNummer = BetreuungUtil.getBetreuungNummerFromBGNummer(bgNummer);
		final int kindNummer = BetreuungUtil.getKindNummerFromBGNummer(bgNummer);
		final int yearFromBGNummer = BetreuungUtil.getYearFromBGNummer(bgNummer);
		// der letzte Tag im Jahr, von der BetreuungsId sollte immer zur richtigen Gesuchsperiode zählen.
		final Optional<Gesuchsperiode> gesuchsperiodeOptional =
			gesuchsperiodeService.getGesuchsperiodeAm(LocalDate.ofYearDay(yearFromBGNummer, 365), mandant);
		Gesuchsperiode gesuchsperiode;
		if (gesuchsperiodeOptional.isPresent()) {
			gesuchsperiode = gesuchsperiodeOptional.get();
		} else {
			return new ArrayList<>();
		}
		final long fallnummer = BetreuungUtil.getFallnummerFromBGNummer(bgNummer);
		final long gemeindeNummer = BetreuungUtil.getGemeindeFromBGNummer(bgNummer);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<T> query = cb.createQuery(clazz);

		Root<T> root = query.from(clazz);
		final Join<T, KindContainer> kindjoin = root.join(AbstractAnmeldung_.kind, JoinType.LEFT);
		final Join<KindContainer, Gesuch> kindContainerGesuchJoin = kindjoin.join(
			KindContainer_.gesuch,
			JoinType.LEFT);
		final Join<Gesuch, Dossier> dossierJoin = kindContainerGesuchJoin.join(Gesuch_.dossier, JoinType.LEFT);
		final Join<Dossier, Fall> gesuchFallJoin = dossierJoin.join(Dossier_.fall);
		final Join<Dossier, Gemeinde> gesuchGemeindeJoin = dossierJoin.join(Dossier_.gemeinde);

		Predicate predBetreuungNummer = cb.equal(root.get(AbstractAnmeldung_.betreuungNummer), betreuungNummer);
		Predicate predBetreuungAusgeloest =
			root.get(AbstractAnmeldung_.betreuungsstatus).in(Betreuungsstatus.getBetreuungsstatusForAnmeldungsstatusAusgeloestNotStorniert());
		Predicate predKindNummer = cb.equal(kindjoin.get(KindContainer_.kindNummer), kindNummer);
		Predicate predFallNummer = cb.equal(gesuchFallJoin.get(Fall_.fallNummer), fallnummer);
		Predicate predGesuchsperiode = cb.equal(kindContainerGesuchJoin.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predGemeineNummer = cb.equal(gesuchGemeindeJoin.get(Gemeinde_.gemeindeNummer), gemeindeNummer);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(predFallNummer);
		predicates.add(predGesuchsperiode);
		predicates.add(predKindNummer);
		predicates.add(predBetreuungNummer);
		predicates.add(predBetreuungAusgeloest);
		predicates.add(predGemeineNummer);

		if (getOnlyAktuelle) {
			Predicate predAktuelleBetreuung =
				cb.equal(
					root.get(AbstractAnmeldung_.anmeldungMutationZustand),
					AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
			Predicate predNormaleBetreuung = cb.isNull(root.get(AbstractAnmeldung_.anmeldungMutationZustand));
			predicates.add(cb.or(predAktuelleBetreuung, predNormaleBetreuung));
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	public Optional<Betreuung> findSameBetreuungInDifferentGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier,
		int betreuungNummer,
		int kindNummer
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);
		Root<Betreuung> root = query.from(Betreuung.class);
		final Join<Betreuung, KindContainer> kindjoin = root.join(Betreuung_.kind, JoinType.LEFT);
		final Join<KindContainer, Gesuch> kindContainerGesuchJoin = kindjoin.join(KindContainer_.gesuch,
			JoinType.LEFT);
		final Join<Gesuch, Dossier> joinGesuchDossier = kindContainerGesuchJoin.join(Gesuch_.dossier, JoinType.LEFT);

		ParameterExpression<Fall> fallParam = cb.parameter(Fall.class, "fall");
		ParameterExpression<Gemeinde> gemeindeParam = cb.parameter(Gemeinde.class, "gemeinde");
		ParameterExpression<Integer> betreuungNummerParam = cb.parameter(Integer.class, "betreuungNummer");
		ParameterExpression<Integer> kindNummerParam = cb.parameter(Integer.class, "kindNummer");
		ParameterExpression<Gesuchsperiode> gesuchsperiodeParam = cb.parameter(Gesuchsperiode.class, "gesuchsperiode");

		Predicate predFallNummer = cb.equal(joinGesuchDossier.get(Dossier_.fall), fallParam);
		Predicate predGemeinde = cb.equal(joinGesuchDossier.get(Dossier_.gemeinde), gemeindeParam);
		Predicate predGueltig = cb.isTrue(root.get(Betreuung_.gueltig));
		Predicate predBetreuungNummer = cb.equal(root.get(Betreuung_.betreuungNummer), betreuungNummerParam);
		Predicate predKindNummer = cb.equal(kindjoin.get(KindContainer_.kindNummer), kindNummerParam);
		Predicate predGesuchsperiode =
			cb.equal(kindContainerGesuchJoin.get(Gesuch_.gesuchsperiode), gesuchsperiodeParam);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(predFallNummer);
		predicates.add(predGemeinde);
		predicates.add(predGueltig);
		predicates.add(predGesuchsperiode);
		predicates.add(predKindNummer);
		predicates.add(predBetreuungNummer);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		TypedQuery<Betreuung> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(fallParam, dossier.getFall());
		q.setParameter(gemeindeParam, dossier.getGemeinde());
		q.setParameter(betreuungNummerParam, betreuungNummer);
		q.setParameter(kindNummerParam, kindNummer);
		q.setParameter(gesuchsperiodeParam, gesuchsperiode);

		// Leider gibt getSingleResult eine Exception, falls es keines gibt.
		final List<Betreuung> resultList = q.getResultList();
		if (CollectionUtils.isNotEmpty(resultList)) {
			if (resultList.size() == 1) {
				return Optional.of(resultList.get(0));
			}
			throw new EbeguRuntimeException("findBetreuungByBGNummer", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS);
		}
		return Optional.empty();
	}

	@Override
	@Nonnull
	public Optional<Betreuung> findBetreuungByBGNummer(
		@Nonnull String bgNummer,
		boolean onlyGueltig,
		@Nonnull Mandant mandant) {
		final int yearFromBGNummer = BetreuungUtil.getYearFromBGNummer(bgNummer);
		// der letzte Tag im Jahr, von der BetreuungsId sollte immer zur richtigen Gesuchsperiode zählen.
		final Optional<Gesuchsperiode> gesuchsperiodeOptional =
			gesuchsperiodeService.getGesuchsperiodeAm(LocalDate.ofYearDay(yearFromBGNummer, 365), mandant);
		if (gesuchsperiodeOptional.isEmpty()) {
			return Optional.empty();
		}

		final Gesuchsperiode gesuchsperiode = gesuchsperiodeOptional.get();
		final int betreuungNummer = BetreuungUtil.getBetreuungNummerFromBGNummer(bgNummer);
		final int kindNummer = BetreuungUtil.getKindNummerFromBGNummer(bgNummer);
		final long fallnummer = BetreuungUtil.getFallnummerFromBGNummer(bgNummer);
		final int gemeindeNummer = BetreuungUtil.getGemeindeFromBGNummer(bgNummer);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);

		Root<Betreuung> root = query.from(Betreuung.class);
		final Join<Betreuung, KindContainer> kindjoin = root.join(Betreuung_.kind, JoinType.LEFT);
		final Join<KindContainer, Gesuch> kindContainerGesuchJoin = kindjoin.join(
			KindContainer_.gesuch,
			JoinType.LEFT);
		final Join<Gesuch, Dossier> joinGesuchDossier = kindContainerGesuchJoin.join(Gesuch_.dossier, JoinType.LEFT);
		final Join<Dossier, Fall> joinDossierFall = joinGesuchDossier.join(Dossier_.fall);
		final Join<Dossier, Gemeinde> joinDossierGemeinde = joinGesuchDossier.join(Dossier_.gemeinde);

		Predicate predBetreuungNummer = cb.equal(root.get(Betreuung_.betreuungNummer), betreuungNummer);
		Predicate predKindNummer = cb.equal(kindjoin.get(KindContainer_.kindNummer), kindNummer);
		Predicate predFallNummer = cb.equal(joinDossierFall.get(Fall_.fallNummer), fallnummer);
		Predicate predGesuchsperiode = cb.equal(kindContainerGesuchJoin.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predGemeinde = cb.equal(joinDossierGemeinde.get(Gemeinde_.gemeindeNummer), gemeindeNummer);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(predFallNummer);
		predicates.add(predGesuchsperiode);
		predicates.add(predKindNummer);
		predicates.add(predBetreuungNummer);
		predicates.add(predGemeinde);
		if (onlyGueltig) {
			Predicate predGueltig = cb.equal(root.get(Betreuung_.gueltig), Boolean.TRUE);
			predicates.add(predGueltig);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		Betreuung resultOrNull = null;
		if (onlyGueltig) {
			resultOrNull = persistence.getCriteriaSingleResult(query);
		} else {
			//wir abholen alle moegliche Betreuung fuer dieser BG Nummer, wir muessen nur die letzte zuruckgeben:
			List<Betreuung> betreuungs = persistence.getCriteriaResults(query);
			for (Betreuung betreuung : betreuungs) {
				if (resultOrNull == null) {
					resultOrNull = betreuung;
				} else if (betreuung.getTimestampErstellt() != null
					&& resultOrNull.getTimestampErstellt() != null
					&& betreuung.getTimestampErstellt().isAfter(resultOrNull.getTimestampErstellt())) {
					resultOrNull = betreuung;
				}
			}
		}

		return Optional.ofNullable(resultOrNull);
	}

	@Override
	@Nonnull
	public Optional<Betreuung> findBetreuungWithBetreuungsPensen(@Nonnull String key) {
		Objects.requireNonNull(key, ID_MUSS_GESETZT_SEIN);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);
		Root<Betreuung> root = query.from(Betreuung.class);
		root.fetch(Betreuung_.betreuungspensumContainers, JoinType.LEFT);
		root.fetch(Betreuung_.abwesenheitContainers, JoinType.LEFT);
		query.select(root);
		Predicate idPred = cb.equal(root.get(AbstractEntity_.id), key);
		query.where(idPred);
		Betreuung result = persistence.getCriteriaSingleResult(query);
		if (result != null) {
			authorizer.checkReadAuthorization(result);
		}
		return Optional.ofNullable(result);
	}

	@Override
	public void removeBetreuung(@Nonnull String betreuungId) {
		Objects.requireNonNull(betreuungId);
		Optional<Betreuung> betrToRemoveOpt = findBetreuung(betreuungId);
		Betreuung betreuungToRemove =
			betrToRemoveOpt.orElseThrow(() -> new EbeguEntityNotFoundException("removeBetreuung", ErrorCodeEnum
				.ERROR_ENTITY_NOT_FOUND, betreuungId));

		handleDependingMitteilungenWhenDeletingBetreuung(betreuungId, betreuungToRemove);

		final String gesuchId = betreuungToRemove.extractGesuch().getId();
		removeBetreuung(betreuungToRemove, null);
		wizardStepService.updateSteps(
			gesuchId,
			null,
			null,
			WizardStepName.BETREUUNG); //auch bei entfernen wizard updaten
		mailService.sendInfoBetreuungGeloescht(Collections.singletonList(betreuungToRemove));
	}

	@Override
	public void removeAnmeldung(@Nonnull String anmeldungId) {
		Objects.requireNonNull(anmeldungId);
		Optional<? extends AbstractAnmeldung> anmeldungToRemoveOpt = findAnmeldung(anmeldungId);
		AbstractAnmeldung anmeldungToRemove =
			anmeldungToRemoveOpt.orElseThrow(() -> new EbeguEntityNotFoundException("removeAnmeldung", ErrorCodeEnum
				.ERROR_ENTITY_NOT_FOUND, anmeldungId));

		final String gesuchId = anmeldungToRemove.extractGesuch().getId();
		removeAnmeldung(anmeldungToRemove);
		wizardStepService.updateSteps(
			gesuchId,
			null,
			null,
			WizardStepName.BETREUUNG); //auch bei entfernen wizard updaten
	}

	/**
	 * removes Betreuungsmitteilungen of this Betreuung and removes the relation of all depending Mitteilunges
	 */
	private void handleDependingMitteilungenWhenDeletingBetreuung(
		@Nonnull String betreuungId,
		@Nonnull Betreuung betreuungToRemove) {
		Collection<Mitteilung> mitteilungenForBetreuung =
			this.mitteilungService.findAllMitteilungenForBetreuung(betreuungToRemove);
		mitteilungenForBetreuung.stream()
			.filter(mitteilung -> mitteilung.getClass().equals(Mitteilung.class))
			.forEach(mitteilung ->
			{
				mitteilung.setBetreuung(null);
				LOG.debug(
					"Betreuung '{}' will be removed. Removing Relation in Mitteilung: '{}'",
					betreuungId,
					mitteilung.getId());
				persistence.merge(mitteilung);
			});

		mitteilungenForBetreuung.stream()
			.filter(mitteilung -> mitteilung.getClass().equals(Betreuungsmitteilung.class))
			.forEach(betMitteilung -> {
				LOG.debug(
					"Betreuung '{}' will be removed. Removing dependent Betreuungsmitteilung: '{}'",
					betreuungId,
					betMitteilung.getId());
				persistence.remove(Betreuungsmitteilung.class, betMitteilung.getId());
			});
	}

	@Override
	@SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION",
		justification = "ErweiterteBetreuungContainer must be set to null")
	public void removeBetreuung(@Nonnull Betreuung betreuung, @Nullable String externalClient) {
		Objects.requireNonNull(betreuung);
		authorizer.checkWriteAuthorization(betreuung);
		final Gesuch gesuch = betreuung.extractGesuch();

		persistence.remove(betreuung);

		// the betreuung needs to be removed from the object as well
		gesuch.getKindContainers()
			.forEach(kind -> kind.getBetreuungen().removeIf(bet -> bet.getId().equalsIgnoreCase(betreuung.getId())));
		LOG.info("Betreuung mit RefNr: {} wurde geloescht", betreuung.getBGNummer());
		betreuungMonitoringService.saveBetreuungMonitoring(new BetreuungMonitoring(
			betreuung.getBGNummer(),
			externalClient != null ? externalClient : principalBean.getBenutzer().getUsername(),
			"Die Betreuung wurde geloescht",
			LocalDateTime.now()));
		gesuchService.updateBetreuungenStatus(gesuch);
	}

	@Override
	@SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION",
		justification = "ErweiterteBetreuungContainer must be set to null")
	public void removeAnmeldung(@Nonnull AbstractAnmeldung anmeldung) {
		Objects.requireNonNull(anmeldung);
		final Gesuch gesuch = anmeldung.extractGesuch();

		persistence.remove(anmeldung);

		// the Anmeldung needs to be removed from the object as well
		gesuch.getKindContainers()
			.forEach(kind -> kind.getAnmeldungenTagesschule()
				.removeIf(bet -> bet.getId().equalsIgnoreCase(anmeldung.getId())));
		gesuch.getKindContainers()
			.forEach(kind -> kind.getAnmeldungenFerieninsel()
				.removeIf(bet -> bet.getId().equalsIgnoreCase(anmeldung.getId())));

		gesuchService.updateBetreuungenStatus(gesuch);
	}

	@Override
	@Nonnull
	public Collection<AbstractPlatz> getPendenzenBetreuungen() {
		Collection<AbstractPlatz> pendenzen = new ArrayList<>();
		Collection<Institution> instForCurrBenutzer =
			institutionService.getInstitutionenEditableForCurrentBenutzer(true);
		if (!instForCurrBenutzer.isEmpty()) {
			pendenzen.addAll(getPendenzenForInstitution((Institution[]) instForCurrBenutzer.toArray(INSTITUTIONS)));
			pendenzen.addAll(getPendenzenAnmeldungTagesschuleForInstitution((Institution[]) instForCurrBenutzer.toArray(
				INSTITUTIONS)));
			pendenzen.addAll(getPendenzenAnmeldungFerieninselForInstitution((Institution[]) instForCurrBenutzer.toArray(
				INSTITUTIONS)));
			return pendenzen;
		}
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Betreuung> findAllBetreuungenWithVerfuegungForDossier(@Nonnull Dossier dossier) {
		Objects.requireNonNull(dossier, "dossier muss gesetzt sein");

		// Einschraenken nach Institutionen fuer die ich lese-berechtigt bin
		Collection<Institution> institutionen = institutionService.getInstitutionenReadableForCurrentBenutzer(false);
		if (institutionen.isEmpty()) {
			// Wenn der Benutzer fuer keine Institution berechtigt ist, darf er auch keine Verfuegungen sehen
			return Collections.emptyList();
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);

		Root<Betreuung> root = query.from(Betreuung.class);
		List<Predicate> predicatesToUse = new ArrayList<>();

		Predicate fallPredicate =
			cb.equal(root.get(Betreuung_.kind).get(KindContainer_.gesuch).get(Gesuch_.dossier), dossier);
		predicatesToUse.add(fallPredicate);

		Predicate predicateBetreuung = root.get(Betreuung_.betreuungsstatus).in(Betreuungsstatus.getBetreuungsstatusWithVerfuegung());
		predicatesToUse.add(predicateBetreuung);

		Predicate verfuegungPredicate = cb.isNotNull(root.get(Betreuung_.verfuegung));
		predicatesToUse.add(verfuegungPredicate);

		Predicate predicateInstitution = root.get(Betreuung_.institutionStammdaten)
			.get(InstitutionStammdaten_.institution)
			.in(Collections.singletonList(institutionen));
		predicatesToUse.add(predicateInstitution);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse))
			.orderBy(cb.desc(root.get(Betreuung_.verfuegung)
				.get(AbstractEntity_.timestampErstellt)));

		List<Betreuung> criteriaResults = persistence.getCriteriaResults(query);

		criteriaResults.forEach(betreuung -> authorizer.checkReadAuthorization(betreuung));

		return criteriaResults;
	}

	/**
	 * Liest alle Betreuungen die zu einer der mitgegebenen Institution gehoeren und die im Status WARTEN sind. Wenn
	 * der Benutzer vom Schulamt
	 * ist, dann werden nur die Institutionsstammdaten der Art FERIENINSEL oder TAGESSCHULE betrachtet.
	 */
	@Nonnull
	private Collection<Betreuung> getPendenzenForInstitution(@Nonnull Institution... institutionen) {
		Objects.requireNonNull(institutionen, "institutionen muss gesetzt sein");

		UserRole role = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();
		if (role.isRoleGemeindeOrTS() || institutionen.length == 0) {
			return Collections.EMPTY_LIST;
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);
		Root<Betreuung> root = query.from(Betreuung.class);

		List<Predicate> predicates = new ArrayList<>();

		if (role.isRoleGemeindeOrTS()) {
			predicates.add(root.get(Betreuung_.betreuungsstatus)
				.in(Collections.singletonList(Betreuungsstatus.getBetreuungsstatusForPendenzSchulamt())));
		} else { // for Institution or Traegerschaft. by default
			predicates.add(root.get(Betreuung_.betreuungsstatus)
				.in(Collections.singletonList(Betreuungsstatus.getBetreuungsstatusForPendenzInstitution())));
		}

		// Institution
		predicates.add(root.get(Betreuung_.institutionStammdaten)
			.get(InstitutionStammdaten_.institution)
			.in(Arrays.asList(institutionen)));
		// Gesuchsperiode darf nicht geschlossen sein
		predicates.add(root.get(Betreuung_.kind)
			.get(KindContainer_.gesuch)
			.get(Gesuch_.gesuchsperiode)
			.get(Gesuchsperiode_.status)
			.in(GesuchsperiodeStatus.AKTIV, GesuchsperiodeStatus.INAKTIV));

		if (role.isRoleGemeindeabhaengig()) {
			Benutzer benutzer = principalBean.getBenutzer();
			final Join<Dossier, Gemeinde> gemeindeJoin =
				root.join(Betreuung_.kind, JoinType.LEFT).join(KindContainer_.gesuch, JoinType.LEFT)
					.join(Gesuch_.dossier, JoinType.LEFT).join(Dossier_.gemeinde, JoinType.LEFT);
			FilterFunctions.setGemeindeFilterForCurrentUser(benutzer, gemeindeJoin, predicates);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		List<Betreuung> betreuungen = persistence.getCriteriaResults(query);
		authorizer.checkReadAuthorizationForAllPlaetze(betreuungen);
		return betreuungen;
	}

	@Nonnull
	private Collection<AnmeldungTagesschule> getPendenzenAnmeldungTagesschuleForInstitution(
		@Nonnull Institution... institutionen) {
		Objects.requireNonNull(institutionen, "institutionen muss gesetzt sein");

		// parameter für "in" operation darf nie leer sein
		if (institutionen.length == 0) {
			return Collections.EMPTY_LIST;
		}

		UserRole role = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<AnmeldungTagesschule> query = cb.createQuery(AnmeldungTagesschule.class);
		Root<AnmeldungTagesschule> root = query.from(AnmeldungTagesschule.class);

		List<Predicate> predicates = new ArrayList<>();

		if (role.isRoleGemeindeOrTS()) {
			predicates.add(root.get(AbstractAnmeldung_.betreuungsstatus)
				.in(Collections.singletonList(Betreuungsstatus.getBetreuungsstatusForPendenzSchulamt())));
		} else { // for Institution or Traegerschaft. by default
			predicates.add(root.get(AbstractAnmeldung_.betreuungsstatus)
				.in(Collections.singletonList(Betreuungsstatus.getBetreuungsstatusForPendenzInstitution())));
		}

		// nur Aktuelle Anmeldungen
		Predicate predAktuelleBetreuung =
			cb.equal(
				root.get(AbstractAnmeldung_.anmeldungMutationZustand),
				AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
		Predicate predNormaleBetreuung = cb.isNull(root.get(AbstractAnmeldung_.anmeldungMutationZustand));
		predicates.add(cb.or(predAktuelleBetreuung, predNormaleBetreuung));

		// Institution
		predicates.add(root.get(AbstractPlatz_.institutionStammdaten)
			.get(InstitutionStammdaten_.institution)
			.in(Arrays.asList(institutionen)));
		// Gesuchsperiode darf nicht geschlossen sein
		predicates.add(root.get(AbstractPlatz_.kind)
			.get(KindContainer_.gesuch)
			.get(Gesuch_.gesuchsperiode)
			.get(Gesuchsperiode_.status)
			.in(GesuchsperiodeStatus.AKTIV, GesuchsperiodeStatus.INAKTIV));

		if (role.isRoleGemeinde()) {
			// SCH darf nur Gesuche sehen, die bereits freigegebn wurden
			predicates.add(root.get(AbstractPlatz_.kind).get(KindContainer_.gesuch).get(Gesuch_.status).in
				(AntragStatus.allowedforRole(UserRole.SACHBEARBEITER_GEMEINDE)));
		}

		if (role.isRoleTsOnly()) {
			// SCH darf nur Gesuche sehen, die bereits freigegebn wurden
			predicates.add(root.get(AbstractPlatz_.kind).get(KindContainer_.gesuch).get(Gesuch_.status).in
				(AntragStatus.allowedforRole(UserRole.SACHBEARBEITER_TS)));
		}

		if (role.isRoleGemeindeabhaengig()) {
			Benutzer benutzer = principalBean.getBenutzer();
			final Join<Dossier, Gemeinde> gemeindeJoin =
				root.join(AbstractPlatz_.kind, JoinType.LEFT).join(KindContainer_.gesuch, JoinType.LEFT)
					.join(Gesuch_.dossier, JoinType.LEFT).join(Dossier_.gemeinde, JoinType.LEFT);
			FilterFunctions.setGemeindeFilterForCurrentUser(benutzer, gemeindeJoin, predicates);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		List<AnmeldungTagesschule> betreuungen = persistence.getCriteriaResults(query);
		authorizer.checkReadAuthorizationForAllPlaetze(betreuungen);
		return betreuungen;
	}

	@Nonnull
	private Collection<AnmeldungFerieninsel> getPendenzenAnmeldungFerieninselForInstitution(
		@Nonnull Institution... institutionen) {
		Objects.requireNonNull(institutionen, "institutionen muss gesetzt sein");

		// parameter für "in" operation darf nie leer sein
		if (institutionen.length == 0) {
			return Collections.EMPTY_LIST;
		}

		UserRole role = principalBean.discoverMostPrivilegedRoleOrThrowExceptionIfNone();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<AnmeldungFerieninsel> query = cb.createQuery(AnmeldungFerieninsel.class);
		Root<AnmeldungFerieninsel> root = query.from(AnmeldungFerieninsel.class);

		List<Predicate> predicates = new ArrayList<>();

		if (role.isRoleGemeindeOrTS()) {
			predicates.add(root.get(AbstractAnmeldung_.betreuungsstatus)
				.in(Collections.singletonList(Betreuungsstatus.getBetreuungsstatusForPendenzSchulamt())));
		} else { // for Institution or Traegerschaft. by default
			predicates.add(root.get(AbstractAnmeldung_.betreuungsstatus)
				.in(Collections.singletonList(Betreuungsstatus.getBetreuungsstatusForPendenzInstitution())));
		}

		// nur Aktuelle Anmeldungen
		Predicate predAktuelleBetreuung =
			cb.equal(
				root.get(AbstractAnmeldung_.anmeldungMutationZustand),
				AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
		Predicate predNormaleBetreuung = cb.isNull(root.get(AbstractAnmeldung_.anmeldungMutationZustand));
		predicates.add(cb.or(predAktuelleBetreuung, predNormaleBetreuung));

		// Institution
		predicates.add(root.get(AbstractPlatz_.institutionStammdaten)
			.get(InstitutionStammdaten_.institution)
			.in(Arrays.asList(institutionen)));
		// Gesuchsperiode darf nicht geschlossen sein
		predicates.add(root.get(AbstractPlatz_.kind)
			.get(KindContainer_.gesuch)
			.get(Gesuch_.gesuchsperiode)
			.get(Gesuchsperiode_.status)
			.in(GesuchsperiodeStatus.AKTIV, GesuchsperiodeStatus.INAKTIV));

		if (role.isRoleGemeinde()) {
			// SCH darf nur Gesuche sehen, die bereits freigegebn wurden
			predicates.add(root.get(AbstractPlatz_.kind).get(KindContainer_.gesuch).get(Gesuch_.status).in
				(AntragStatus.allowedforRole(UserRole.SACHBEARBEITER_GEMEINDE)));
		}

		if (role.isRoleTsOnly()) {
			// SCH darf nur Gesuche sehen, die bereits freigegebn wurden
			predicates.add(root.get(AbstractPlatz_.kind).get(KindContainer_.gesuch).get(Gesuch_.status).in
				(AntragStatus.allowedforRole(UserRole.SACHBEARBEITER_TS)));
		}

		if (role.isRoleGemeindeabhaengig()) {
			Benutzer benutzer = principalBean.getBenutzer();
			final Join<Dossier, Gemeinde> gemeindeJoin =
				root.join(AbstractPlatz_.kind, JoinType.LEFT).join(KindContainer_.gesuch, JoinType.LEFT)
					.join(Gesuch_.dossier, JoinType.LEFT).join(Dossier_.gemeinde, JoinType.LEFT);
			FilterFunctions.setGemeindeFilterForCurrentUser(benutzer, gemeindeJoin, predicates);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		List<AnmeldungFerieninsel> betreuungen = persistence.getCriteriaResults(query);
		authorizer.checkReadAuthorizationForAllPlaetze(betreuungen);
		return betreuungen;
	}

	@Override
	@Nonnull
	public Betreuung schliessenOhneVerfuegen(@Nonnull Betreuung betreuung) {
		betreuung.setBetreuungsstatus(Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG);
		final Betreuung persistedBetreuung = saveBetreuung(betreuung, false, null);
		authorizer.checkWriteAuthorization(persistedBetreuung);
		wizardStepService.updateSteps(persistedBetreuung.extractGesuch().getId(), null, null,
			WizardStepName.VERFUEGEN);
		return persistedBetreuung;
	}

	@Override
	@Nonnull
	public Betreuung schliessenOnly(@Nonnull Betreuung betreuung) {
		betreuung.setBetreuungsstatus(Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG);
		authorizer.checkWriteAuthorization(betreuung);
		return saveBetreuung(betreuung, false, null);
	}

	@Override
	@Nonnull
	public List<Betreuung> getAllBetreuungenWithMissingStatistics(Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);

		Root<Betreuung> root = query.from(Betreuung.class);
		Join<Betreuung, KindContainer> joinKindContainer = root.join(Betreuung_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Fall> joinFall = joinDossier.join(Dossier_.fall, JoinType.LEFT);

		Predicate predicateMutation = cb.equal(joinGesuch.get(Gesuch_.typ), AntragTyp.MUTATION);
		Predicate predicateFlag = cb.isNull(root.get(Betreuung_.betreuungMutiert));
		Predicate predicateStatus = joinGesuch.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtNotIgnoriertStates());
		Predicate predicateMandant = cb.equal(joinFall.get(Fall_.mandant), mandant);

		query.where(predicateMutation, predicateFlag, predicateStatus, predicateMandant);
		query.orderBy(cb.desc(joinGesuch.get(Gesuch_.laufnummer)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	public List<Abwesenheit> getAllAbwesenheitenWithMissingStatistics(Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Abwesenheit> query = cb.createQuery(Abwesenheit.class);

		Root<Abwesenheit> root = query.from(Abwesenheit.class);
		Join<Abwesenheit, AbwesenheitContainer> joinAbwesenheitContainer =
			root.join(Abwesenheit_.abwesenheitContainer, JoinType.LEFT);
		Join<AbwesenheitContainer, Betreuung> joinBetreuung =
			joinAbwesenheitContainer.join(AbwesenheitContainer_.betreuung, JoinType.LEFT);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(Betreuung_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Fall> joinFall = joinDossier.join(Dossier_.fall, JoinType.LEFT);

		Predicate predicateMutation = cb.equal(joinGesuch.get(Gesuch_.typ), AntragTyp.MUTATION);
		Predicate predicateFlag = cb.isNull(joinBetreuung.get(Betreuung_.abwesenheitMutiert));
		Predicate predicateStatus = joinGesuch.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtNotIgnoriertStates());
		Predicate predicateMandant = cb.equal(joinFall.get(Fall_.mandant), mandant);

		query.where(predicateMutation, predicateFlag, predicateStatus, predicateMandant);
		query.orderBy(cb.desc(joinGesuch.get(Gesuch_.laufnummer)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	public void sendInfoOffenePendenzenNeuMitteilungInstitution() {
		Collection<InstitutionStammdaten> activeInstitutionen =
			institutionStammdatenService.getAllInstitonStammdatenForBatchjobs();
		for (InstitutionStammdaten stammdaten : activeInstitutionen) {
			final Institution institution = stammdaten.getInstitution();
			if (stammdaten.getSendMailWennOffenePendenzen()) {
				Collection<AbstractPlatz> pendenzen = new ArrayList<>();
				pendenzen.addAll(getPendenzenForInstitution(institution));
				pendenzen.addAll(getPendenzenAnmeldungTagesschuleForInstitution(institution));
				pendenzen.addAll(getPendenzenAnmeldungFerieninselForInstitution(institution));
				boolean ungelesendeMitteilung = mitteilungService.hasInstitutionOffeneMitteilungen(institution);
				if (CollectionUtils.isNotEmpty(pendenzen) || ungelesendeMitteilung) {
					mailService.sendInfoOffenePendenzenNeuMitteilungInstitution(
						stammdaten,
						CollectionUtils.isNotEmpty(pendenzen),
						ungelesendeMitteilung);
				}
			}
		}
	}

	@Override
	@Nonnull
	public AbstractAnmeldung anmeldungSchulamtStornieren(@Valid @Nonnull AbstractAnmeldung anmeldung) {
		Objects.requireNonNull(anmeldung, BETREUUNG_DARF_NICHT_NULL_SEIN);
		authorizer.checkWriteAuthorization(anmeldung);

		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_STORNIERT);
		AbstractAnmeldung persistedBetreuung = savePlatz(anmeldung, null);
		try {
			// Bei Stornierung einer Anmeldung muss eine E-Mail geschickt werden
			GemeindeStammdaten gemeindeStammdaten =
				gemeindeService.getGemeindeStammdatenByGemeindeId(persistedBetreuung.extractGesuch()
					.getDossier()
					.getGemeinde()
					.getId()).get();
			if (gemeindeStammdaten.getBenachrichtigungTsEmailAuto()) {
				mailService.sendInfoSchulamtAnmeldungStorniert(persistedBetreuung);
			}
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail InfoSchulamtAnmeldungStorniert konnte nicht verschickt werden fuer Betreuung",
				anmeldung.getId());
		}
		return persistedBetreuung;
	}

	private void checkNotKorrekturmodusWithFerieninselOnly(@Nonnull Gesuch gesuch) {
		// Im Korrekturmodus Gemeinde darf bei nur-Ferieninsel-Gesuchen keine Betreuung hinzufuegt
		// werden, da sonst die FinSit ungueltig wird, diese aber durch die Gemeinde nicht korrigiert
		// werden kann
		final boolean korrekturmodusGemeinde = EbeguUtil.isKorrekturmodusGemeinde(gesuch);
		if (korrekturmodusGemeinde) {
			List<AbstractPlatz> allPlaetze = gesuch.extractAllPlaetze();
			BetreuungsangebotTyp dominantType = EbeguUtil.getDominantBetreuungsangebotTyp(allPlaetze);
			if (!allPlaetze.isEmpty() && dominantType == BetreuungsangebotTyp.FERIENINSEL) {
				throw new EbeguRuntimeException(KibonLogLevel.NONE, "checkNotKorrekturmodusWithFerieninselOnly",
					ErrorCodeEnum.ERROR_KORREKTURMODUS_FI_ONLY);
			}
		}
	}

	@Override
	@Nonnull
	public Optional<AnmeldungTagesschule> findAnmeldungenTagesschuleByBGNummer(
		@Nonnull String bgNummer,
		@Nonnull Mandant mandant) {
		final int yearFromBGNummer = BetreuungUtil.getYearFromBGNummer(bgNummer);

		final Optional<Gesuchsperiode> gesuchsperiodeOptional =
			gesuchsperiodeService.getGesuchsperiodeAm(LocalDate.ofYearDay(yearFromBGNummer, 365), mandant);
		Gesuchsperiode gesuchsperiode;
		if (gesuchsperiodeOptional.isPresent()) {
			gesuchsperiode = gesuchsperiodeOptional.get();
		} else {
			return Optional.empty();
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<AnmeldungTagesschule> query = cb.createQuery(AnmeldungTagesschule.class);

		Root<AnmeldungTagesschule> root = query.from(AnmeldungTagesschule.class);
		final Join<AnmeldungTagesschule, KindContainer> kindjoin = root.join(AnmeldungTagesschule_.kind,
			JoinType.LEFT);
		final Join<KindContainer, Gesuch> kindContainerGesuchJoin = kindjoin.join(
			KindContainer_.gesuch,
			JoinType.LEFT);
		final Join<Gesuch, Dossier> dossierJoin = kindContainerGesuchJoin.join(Gesuch_.dossier, JoinType.LEFT);
		final Join<Dossier, Fall> gesuchFallJoin = dossierJoin.join(Dossier_.fall);

		final int betreuungNummer = BetreuungUtil.getBetreuungNummerFromBGNummer(bgNummer);
		final int kindNummer = BetreuungUtil.getKindNummerFromBGNummer(bgNummer);
		final long fallnummer = BetreuungUtil.getFallnummerFromBGNummer(bgNummer);

		Predicate predBetreuungNummer = cb.equal(root.get(AnmeldungTagesschule_.betreuungNummer), betreuungNummer);
		Predicate predKindNummer = cb.equal(kindjoin.get(KindContainer_.kindNummer), kindNummer);
		Predicate predFallNummer = cb.equal(gesuchFallJoin.get(Fall_.fallNummer), fallnummer);
		Predicate predGesuchsperiode = cb.equal(kindContainerGesuchJoin.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(predFallNummer);
		predicates.add(predGesuchsperiode);
		predicates.add(predKindNummer);
		predicates.add(predBetreuungNummer);

		Predicate predGueltig = cb.equal(root.get(AnmeldungTagesschule_.gueltig), Boolean.TRUE);
		predicates.add(predGueltig);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		return Optional.ofNullable(persistence.getCriteriaSingleResult(query));
	}

	@Nonnull
	@Override
	public Set<BetreuungsmitteilungPensum> capBetreuungspensenToGueltigkeit(
		@Nonnull Set<BetreuungsmitteilungPensum> pensen,
		@Nonnull DateRange gueltigkeit) {

		for (Iterator<BetreuungsmitteilungPensum> i = pensen.iterator(); i.hasNext(); ) {
			BetreuungsmitteilungPensum betreuungsmitteilungPensum = i.next();

			capBetreuungsmitteilungStart(betreuungsmitteilungPensum, gueltigkeit);
			capBetreuungsmitteilungEnd(betreuungsmitteilungPensum, gueltigkeit);
			removeInvalidBetreuungsmitteilungPensumFromSet(i, betreuungsmitteilungPensum);
		}

		return pensen;
	}

	@Nonnull
	@Override
	public BigDecimal getMultiplierForAbweichnungen(@Nonnull Betreuung betreuung) {
		Gesuchsperiode gp = betreuung.extractGesuchsperiode();

		if (betreuung.isAngebotMittagstisch()) {
			return BetreuungUtil.getMittagstischMultiplier();
		}


		if (betreuung.isAngebotKita()) {
			Einstellung oeffnungstageKita = getEinstellung(EinstellungKey.OEFFNUNGSTAGE_KITA, gp);
			Einstellung pensumAnzeigeTyp = getEinstellung(EinstellungKey.PENSUM_ANZEIGE_TYP, gp);
			BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp = BetreuungspensumAnzeigeTyp.valueOf(pensumAnzeigeTyp.getValue());
			if(betreuungspensumAnzeigeTyp.equals(BetreuungspensumAnzeigeTyp.NUR_STUNDEN)) {
				return BetreuungUtil.calculateOeffnungszeitPerMonthProcentual(MathUtil.EXACT.multiply(oeffnungstageKita.getValueAsBigDecimal(), BetreuungUtil.ANZAHL_STUNDEN_PRO_TAG_KITA));
			}
			return BetreuungUtil.calculateOeffnungszeitPerMonthProcentual(oeffnungstageKita.getValueAsBigDecimal());
		}

		Einstellung oeffnungstageTFO = getEinstellung(EinstellungKey.OEFFNUNGSTAGE_TFO, gp);
		Einstellung oeffnungsstundenTFO = getEinstellung(EinstellungKey.OEFFNUNGSSTUNDEN_TFO, gp);

		BigDecimal oeffungszeitProJahrTFO = MathUtil.EXACT.multiply(
			oeffnungstageTFO.getValueAsBigDecimal(),
			oeffnungsstundenTFO.getValueAsBigDecimal());
		return BetreuungUtil.calculateOeffnungszeitPerMonthProcentual(oeffungszeitProJahrTFO);
	}

	private Einstellung getEinstellung(EinstellungKey einstellungKey, Gesuchsperiode gesuchsperiode) {
		return einstellungService
			.getEinstellungByMandant(einstellungKey, gesuchsperiode)
			.orElseThrow(() -> new EbeguEntityNotFoundException("getEinstellung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, einstellungKey));
	}

	private void capBetreuungsmitteilungEnd(
		@Nonnull BetreuungsmitteilungPensum betreuungsmitteilungPensum,
		@Nonnull DateRange gueltigkeit) {
		if (betreuungsmitteilungPensum.getGueltigkeit().endsAfter(gueltigkeit)) {
			betreuungsmitteilungPensum.setGueltigkeit(new DateRange(
				betreuungsmitteilungPensum.getGueltigkeit().getGueltigAb(),
				gueltigkeit.getGueltigBis()));
		}
	}

	private void capBetreuungsmitteilungStart(
		@Nonnull BetreuungsmitteilungPensum betreuungsmitteilungPensum,
		@Nonnull DateRange gueltigkeit) {
		if (betreuungsmitteilungPensum.getGueltigkeit().startsBefore(gueltigkeit)) {
			betreuungsmitteilungPensum.setGueltigkeit(new DateRange(
				gueltigkeit.getGueltigAb(),
				betreuungsmitteilungPensum.getGueltigkeit().getGueltigBis()));
		}
	}

	private void removeInvalidBetreuungsmitteilungPensumFromSet(
		Iterator<BetreuungsmitteilungPensum> i,
		BetreuungsmitteilungPensum betreuungsmitteilungPensum) {
		if (!betreuungsmitteilungPensum.getGueltigkeit().isValid()) {
			persistence.remove(betreuungsmitteilungPensum);
			i.remove();
		}
	}
}
