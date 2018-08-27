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
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.VerfuegungUtil;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
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

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private MandantService mandantService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private RulesService rulesService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GeneratedDokumentService generatedDokumentService;

	@Inject
	private MailService mailService;

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
		final Gesuch gesuch = betreuung.extractGesuch();

		if (gesuch.isMutation() && betreuung.isAngebotKita()) { // Zahlungsstatus muss nur bei Mutationen und Angebote der Art KITA aktualisiert werden
			Optional<Verfuegung> vorgaengerVerfuegung = findVorgaengerVerfuegung(betreuung);

			if (vorgaengerVerfuegung.isPresent()) {
				for (VerfuegungZeitabschnitt verfuegungZeitabschnittNeu : verfuegung.getZeitabschnitte()) {

					List<VerfuegungZeitabschnitt> zeitabschnitteOnVorgaengerVerfuegung = findZeitabschnitteOnVorgaengerVerfuegung(verfuegungZeitabschnittNeu.getGueltigkeit(), vorgaengerVerfuegung.get());

					Optional<VerfuegungZeitabschnitt> zeitabschnittSameGueltigkeitSameBetrag = VerfuegungUtil.findZeitabschnittSameGueltigkeitSameBetrag
						(zeitabschnitteOnVorgaengerVerfuegung, verfuegungZeitabschnittNeu);

					if (zeitabschnittSameGueltigkeitSameBetrag.isPresent()) {
						// Es hat ueberhaupt nichts geaendert seit dem letztem Gesuch. Falls es schon verrechnet war, bleibt
						// es somit verrechnet. Sonst neu
						if (areAllZeitabschnitteVerrechnet(zeitabschnitteOnVorgaengerVerfuegung)) {
							verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET);
						} else {
							verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.NEU);
						}
					} else { // we only check the status if there has been any verrechnete zeitabschnitt. Otherwise NEU
						// Wenn der alte Abschnitt VERRECHNET war und das Flag ignoriert -> IGNORIEREND
						if (areAllZeitabschnitteVerrechnet(zeitabschnitteOnVorgaengerVerfuegung)) {
							// Es war schon verrechnet: Die neuen Zeitabschnitte muessen entweder ignoriert oder korrigiert werden
							if (ignorieren) {
								verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.IGNORIEREND);
							} else {
								verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.NEU);
							}
						} else {
							// Es war noch nicht verrechnet: Wir muessen es *auf jeden Fall* verrechnen. Das ignorieren bezieht sich nur auf
							// bereits vergangene Auszahlungen. Wir ignorieren die *Korrekturen* und nicht die Daten an sich.
							verfuegungZeitabschnittNeu.setZahlungsstatus(VerfuegungsZeitabschnittZahlungsstatus.NEU);
						}
					}
				}
			}
		}
	}

	private boolean areAllZeitabschnitteVerrechnet(List<VerfuegungZeitabschnitt> zeitabschnitte) {
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			if (!verfuegungZeitabschnitt.getZahlungsstatus().isBereitsBehandeltInZahlungslauf()) {
				return false;
			}
		}
		return true;
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
				if (zeitabschnitt.isKategorieZuschlagZumErwerbspensum()) {
					verfuegung.setKategorieZuschlagZumErwerbspensum(true);
				}
			}
			// Wenn es keines der anderen ist, ist es "normal"
			if (!verfuegung.isKategorieKeinPensum() &&
				!verfuegung.isKategorieMaxEinkommen() &&
				!verfuegung.isKategorieZuschlagZumErwerbspensum() &&
				!verfuegung.isKategorieNichtEintreten()) {
				verfuegung.setKategorieNormal(true);
			}
		}
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Verfuegung nichtEintreten(@Nonnull Verfuegung verfuegung, @Nonnull String betreuungId) {
		// Bei Nich-Eintreten muss der Anspruch auf der Verfuegung auf 0 gesetzt werden, da diese u.U. bei Mutationen
		// als Vergleichswert hinzugezogen werden
		for (VerfuegungZeitabschnitt zeitabschnitt : verfuegung.getZeitabschnitte()) {
			zeitabschnitt.setAnspruchberechtigtesPensum(0);
		}
		verfuegung.setKategorieNichtEintreten(true);
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

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public void removeVerfuegung(@Nonnull Verfuegung verfuegung) {
		Objects.requireNonNull(verfuegung);
		Optional<Verfuegung> entityToRemove = this.findVerfuegung(verfuegung.getId());
		Verfuegung loadedVerf = entityToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeVerfuegung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			verfuegung));
		authorizer.checkWriteAuthorization(loadedVerf);
		loadedVerf.getZeitabschnitte().forEach(verfuegungZeitabschnitt ->
			persistence.remove(verfuegungZeitabschnitt)
		);
		persistence.remove(loadedVerf);
	}

	@SuppressWarnings("OptionalIsPresent")
	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, STEUERAMT, ADMIN_TS, SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Gesuch calculateVerfuegung(@Nonnull Gesuch gesuch) {
		this.finanzielleSituationService.calculateFinanzDaten(gesuch);
		Mandant mandant = mandantService.getFirst();   //gesuch get mandant?
		final List<Rule> rules = rulesService.getRulesForGesuchsperiode(gesuch.extractGemeinde(), gesuch.getGesuchsperiode());
		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED, true);
		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput);
		BGRechnerParameterDTO calculatorParameters = loadCalculatorParameters(mandant, gesuch.getGesuchsperiode());
		// Finde und setze die letzte Verfuegung für die Betreuung für den Merger und Vergleicher.
		// Bei GESCHLOSSEN_OHNE_VERFUEGUNG wird solange ein Vorgänger gesucht, bis  dieser gefunden wird. (Rekursiv)
		gesuch.getKindContainers()
			.stream()
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.forEach(betreuung -> {
					Optional<Verfuegung> vorgaengerVerfuegung = findVorgaengerVerfuegung(betreuung);
					betreuung.setVorgaengerVerfuegung(vorgaengerVerfuegung.orElse(null));
				}
			);

		bgEvaluator.evaluate(gesuch, calculatorParameters);
		authorizer.checkReadAuthorizationForAnyBetreuungen(gesuch.extractAllBetreuungen()); // betreuungen pruefen reicht hier glaub
		return gesuch;
	}

	public Verfuegung getEvaluateFamiliensituationVerfuegung(@Nonnull Gesuch gesuch) {
		this.finanzielleSituationService.calculateFinanzDaten(gesuch);
		final List<Rule> rules = rulesService.getRulesForGesuchsperiode(gesuch.extractGemeinde(), gesuch.getGesuchsperiode());
		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED, true);
		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput);

		return bgEvaluator.evaluateFamiliensituation(gesuch);
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, ADMIN_TS, SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Optional<Verfuegung> findVorgaengerVerfuegung(@Nonnull Betreuung betreuung) {
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

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Optional<LocalDate> findVorgaengerVerfuegungDate(@Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung, "betreuung darf nicht null sein");
		Optional<Verfuegung> vorgaengerVerfuegungOpt = findVorgaengerVerfuegung(betreuung);
		LocalDate letztesVerfDatum = null;
		if (vorgaengerVerfuegungOpt.isPresent()) {
			Verfuegung vorgaengerVerfuegung = vorgaengerVerfuegungOpt.get();
			authorizer.checkReadAuthorization(vorgaengerVerfuegung);
			if (vorgaengerVerfuegung.getTimestampErstellt() != null) {
				letztesVerfDatum = vorgaengerVerfuegung.getTimestampErstellt().toLocalDate();
			}
		}
		return Optional.ofNullable(letztesVerfDatum);
	}

	@Override
	public void findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Betreuung betreuungNeu, @Nonnull List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte) {
		Optional<Verfuegung> vorgaengerVerfuegung = findVorgaengerVerfuegung(betreuungNeu);
		if (vorgaengerVerfuegung.isPresent()) {
			List<VerfuegungZeitabschnitt> zeitabschnitteOnVorgaengerVerfuegung = findZeitabschnitteOnVorgaengerVerfuegung(zeitabschnittNeu.getGueltigkeit(),
				vorgaengerVerfuegung.get());
			for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitteOnVorgaengerVerfuegung) {
				final Betreuung vorgaengerBetreuung = zeitabschnitt.getVerfuegung().getBetreuung();
				if ((zeitabschnitt.getZahlungsstatus().isVerrechnet() || zeitabschnitt.getZahlungsstatus().isIgnoriert()) && isNotInZeitabschnitteList
					(zeitabschnitt, vorgaengerZeitabschnitte)) {
					// Diesen ins Result, for weiterführen und von allen den Vorgänger suchen bis VERRECHNET oder kein Vorgaenger
					vorgaengerZeitabschnitte.add(zeitabschnitt);
				} else {
					// Es gab keine bereits Verrechneten Zeitabschnitte auf dieser Verfuegung -> eins weiter zurueckgehen
					findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(zeitabschnittNeu, vorgaengerBetreuung, vorgaengerZeitabschnitte);
				}
			}
		}
		//noinspection UnnecessaryReturnStatement: Abbruchbedingung: Es gibt keinen Vorgaenger mehr
		return;
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
	private List<VerfuegungZeitabschnitt> findZeitabschnitteOnVorgaengerVerfuegung(@Nonnull DateRange newVerfuegungGueltigkeit,
		@Nonnull Verfuegung lastVerfuegung) {
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
