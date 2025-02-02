/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.EJBAccessException;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.AntragStatusHistory_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchDeletionLog;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Kind_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AnmeldungMutationZustand;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.SozialdienstFallStatus;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguExistingAntragRuntimeException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.gesuch.freigabe.GesuchValidatorService;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.interceptors.UpdateStatusInterceptor;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.validationgroups.CheckFachstellenValidationGroup;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setGemeindeFilterForCurrentUser;

/**
 * Service fuer Gesuch
 */
@Stateless
@Local(GesuchService.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "LocalVariableNamingConvention", "PMD.NcssTypeCount" })
public class GesuchServiceBean extends AbstractBaseService implements GesuchService {

	private static final Logger LOG = LoggerFactory.getLogger(GesuchServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private AntragStatusHistoryService antragStatusHistoryService;
	@Inject
	private MahnungService mahnungService;
	@Inject
	private GeneratedDokumentService generatedDokumentService;
	@Inject
	private DokumentGrundService dokumentGrundService;
	@Inject
	private Authorizer authorizer;
	@Inject
	private BooleanAuthorizer booleanAuthorizer;
	@Inject
	private PrincipalBean principalBean;
	@Inject
	private MailService mailService;
	@Inject
	private ApplicationPropertyService applicationPropertyService;
	@Inject
	private ZahlungService zahlungService;
	@Inject
	private SuperAdminService superAdminService;
	@Inject
	private FileSaverService fileSaverService;
	@Inject
	private MitteilungService mitteilungService;
	@Inject
	private BetreuungService betreuungService;
	@Inject
	private GesuchDeletionLogService gesuchDeletionLogService;
	@Inject
	private DossierService dossierService;
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private GesuchService self;
	@Inject
	private VerfuegungService verfuegungService;
	@Inject
	private EinstellungService einstellungService;
	@Inject
	private MassenversandService massenversandService;
	@Inject
	private InternePendenzService internePendenzService;
	@Inject
	private Validator validator;
	@Inject
	private GesuchValidatorService gesuchValidationService;


	@Nonnull
	@Override
	public Gesuch createGesuch(@Nonnull Gesuch gesuchToCreate) {
		Objects.requireNonNull(gesuchToCreate);
		Gesuch gesuchToPersist = gesuchToCreate;

		Gemeinde gemeindeOfGesuchToCreate = gesuchToCreate.extractGemeinde();
		Gesuchsperiode gesuchsperiodeOfGesuchToCreate = gesuchToCreate.getGesuchsperiode();
		AntragTyp typOfGesuchToCreate = gesuchToCreate.getTyp();
		Eingangsart eingangsart = calculateEingangsart(gesuchToCreate);
		AntragStatus initialStatus = calculateInitialStatus(gesuchToCreate);
		LocalDate eingangsdatum = gesuchToCreate.getEingangsdatum();
		LocalDate regelnGueltigAb = gesuchToCreate.getRegelnGueltigAb();
		boolean newlyCreatedMutation = gesuchToCreate.isNewlyCreatedMutation();
		StringBuilder logInfo = new StringBuilder();
		logInfo.append("CREATE GESUCH fuer Gemeinde: ").append(gemeindeOfGesuchToCreate.getName())
			.append(" Gesuchsperiode: ").append(gesuchsperiodeOfGesuchToCreate.getGesuchsperiodeString())
			.append(" Typ: ").append(typOfGesuchToCreate)
			.append(" Eingangsart: ").append(eingangsart)
			.append(" Einangsdatum: ").append(eingangsdatum);

		if (typOfGesuchToCreate == AntragTyp.MUTATION) {
			logInfo.append('\n').append("Es ist eine Mutation");
			gesuchToPersist = createMutation(gesuchToCreate, gesuchsperiodeOfGesuchToCreate, eingangsart, logInfo);
		} else if (typOfGesuchToCreate == AntragTyp.ERNEUERUNGSGESUCH) {
			logInfo.append('\n').append("Es ist ein Erneuerungsgesuch (im gleichen Dossier)");
			gesuchToPersist = createErneuerungsgesuch(gesuchToCreate, gesuchsperiodeOfGesuchToCreate, eingangsart,
				logInfo);
		} else if (typOfGesuchToCreate == AntragTyp.ERSTGESUCH) {
			logInfo.append('\n').append("Es ist entweder das erste Gesuch überhaupt oder das erste in einem neuen "
				+ "Dossier");
			gesuchToPersist = createErstgesuch(gesuchToCreate, gesuchsperiodeOfGesuchToCreate, eingangsart, logInfo);
			//  Jetzt wurde das Gesuch so kopiert, wie es in der "alten" Gemeinde war. Wir müssen
			// sicherstellen, dass diese Daten auch in der neuen Gemeinde gültig sind
			stripGesuchOfInvalidData(gesuchToPersist);
		}
		gesuchToPersist.setEingangsart(eingangsart);
		gesuchToPersist.setStatus(initialStatus);
		if (eingangsdatum != null) {
			gesuchToPersist.setEingangsdatum(eingangsdatum);
		}
		if (regelnGueltigAb != null) {
			gesuchToPersist.setRegelnGueltigAb(regelnGueltigAb);
		}
		gesuchToPersist.setBegruendungMutation(gesuchToCreate.getBegruendungMutation());

		updateGesuchWithConfiguration(gesuchToPersist);

		authorizer.checkReadAuthorization(gesuchToPersist);

		// Vor dem Speichern noch pruefen, dass noch kein Gesuch dieses Typs fuer das Dossier und die Periode existiert
		ensureUniqueErstgesuchProDossierAndGesuchsperiode(gesuchToPersist);
		Gesuch persistedGesuch = persistence.persist(gesuchToPersist);

		// restore transient field
		persistedGesuch.setNewlyCreatedMutation(newlyCreatedMutation);

		// Die WizardSteps werden direkt erstellt wenn das Gesuch erstellt wird. So vergewissern wir uns dass es kein
		// Gesuch ohne WizardSteps gibt
		wizardStepService.createWizardStepList(persistedGesuch);
		checkCorruptData(persistedGesuch);
		antragStatusHistoryService.saveStatusChange(persistedGesuch, null);
		LOG.info(logInfo.toString());
		return persistedGesuch;
	}

	private void checkCorruptData(Gesuch persistedGesuch) {
		// We check whether the mutation we create has corrupt data in its children. If that is the case, we set
		// the children in Prüfung
		for (KindContainer kindContainer : persistedGesuch.getKindContainers()) {
			if (hasCorruptKindData(kindContainer)) {
				kindContainer.getKindJA().setInPruefung(true);
				wizardStepService.updateSteps(persistedGesuch.getId(), kindContainer, kindContainer, WizardStepName.KINDER);
			}

		}

	}

	private boolean hasCorruptKindData(KindContainer kind) {
		Set<ConstraintViolation<KindContainer>> fachstellenViolations = validator.validate(kind, CheckFachstellenValidationGroup.class);
		return !fachstellenViolations.isEmpty();
	}

	private void updateGesuchWithConfiguration(Gesuch gesuch) {
		Collection<Einstellung> einstellungList =
			einstellungService.getAllEinstellungenByMandant(gesuch.getGesuchsperiode());

		setFKJVFamiliensituationFlag(gesuch, einstellungList);
		setMinDauerKonkubiat(gesuch, einstellungList);
		setFinSitTyp(gesuch, einstellungList);
	}

	private void setFKJVFamiliensituationFlag(Gesuch gesuch, Collection<Einstellung> einstellungList) {
		if (gesuch.getFamiliensituationContainer() == null
			|| gesuch.getFamiliensituationContainer().getFamiliensituationJA() == null) {
			return;
		}

		Einstellung einstellung = getEinstellungByKeyFromList(EinstellungKey.FKJV_FAMILIENSITUATION_NEU, einstellungList);
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setFkjvFamSit(einstellung.getValueAsBoolean());
	}

	private void setMinDauerKonkubiat(Gesuch gesuch, Collection<Einstellung> einstellungList) {
		if (gesuch.getFamiliensituationContainer() == null
			|| gesuch.getFamiliensituationContainer().getFamiliensituationJA() == null) {
			return;
		}

		Einstellung minimalDauerKonkubinat = getEinstellungByKeyFromList(EinstellungKey.MINIMALDAUER_KONKUBINAT, einstellungList);
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setMinDauerKonkubinat(minimalDauerKonkubinat.getValueAsInteger());
	}

	private void setFinSitTyp(Gesuch gesuchToCreate, Collection<Einstellung> einstellungList) {
		Einstellung finSitTyp = getEinstellungByKeyFromList(EinstellungKey.FINANZIELLE_SITUATION_TYP, einstellungList);

		try {
			gesuchToCreate.setFinSitTyp(FinanzielleSituationTyp.valueOf(finSitTyp.getValue()));
		} catch (IllegalArgumentException e) {
			throw new EbeguRuntimeException("setFinSitTyp", "wrong finSitTyp: " + finSitTyp, e);
		}
	}

	private Einstellung getEinstellungByKeyFromList(
		EinstellungKey key,
		Collection<Einstellung> einstellungList) {

		return einstellungList
			.stream()
			.filter(einstellung -> einstellung.getKey() == key)
			.findFirst()
			.orElseThrow(() ->
				new EbeguRuntimeException("getEinstellungByKeyFromList()", "Keine Einstellung für Key " + key + " gfunden"));
	}

	private void stripGesuchOfInvalidData(@Nonnull Gesuch gesuch) {
		Einstellung freiwilligenarbeitEnabled = einstellungService.findEinstellung(
			EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode());

		if (!freiwilligenarbeitEnabled.getValueAsBoolean()) {
			stripFreiwilligenarbeitFromErwerbspensen(gesuch.getGesuchsteller1());
			stripFreiwilligenarbeitFromErwerbspensen(gesuch.getGesuchsteller2());
		}

		Einstellung mahlzeitenverguenstigungEnabled = einstellungService.findEinstellung(
			EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode());

		if (!mahlzeitenverguenstigungEnabled.getValueAsBoolean()) {
			stripMahlzeitenverguenstigungInfos(gesuch.getFamiliensituationContainer());
		}
	}

	private void stripFreiwilligenarbeitFromErwerbspensen(@Nullable GesuchstellerContainer gesuchstellerContainer) {
		if (gesuchstellerContainer == null) {
			return;
		}
		Set<ErwerbspensumContainer> validErwerbspensen = new HashSet<>();
		for (ErwerbspensumContainer erwerbspensumContainer : gesuchstellerContainer.getErwerbspensenContainers()) {
			if (erwerbspensumContainer.getErwerbspensumJA() != null &&
				erwerbspensumContainer.getErwerbspensumJA().getTaetigkeit() != Taetigkeit.FREIWILLIGENARBEIT) {
				validErwerbspensen.add(erwerbspensumContainer);
			}
		}
		gesuchstellerContainer.setErwerbspensenContainers(validErwerbspensen);
	}

	private void stripMahlzeitenverguenstigungInfos(@Nullable FamiliensituationContainer familiensituationContainer) {
		if (familiensituationContainer == null) {
			return;
		}
		Familiensituation familiensituation = familiensituationContainer.getFamiliensituationJA();
		if (familiensituation == null) {
			return;
		}
		familiensituation.setKeineMahlzeitenverguenstigungBeantragt(false);
		familiensituation.setAuszahlungsdaten(null);
		familiensituation.setAbweichendeZahlungsadresse(false);
	}

	@Nonnull
	private Gesuch createMutation(
		@Nonnull Gesuch gesuchToCreate,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Eingangsart eingangsart,
		@Nonnull StringBuilder logInfo
	) {
		if (isThereAnyOpenMutation(gesuchToCreate.getDossier(), gesuchsperiode)) {
			throw new EbeguExistingAntragRuntimeException(
				"antragMutieren", ErrorCodeEnum.ERROR_EXISTING_ONLINE_MUTATION,
				null, gesuchToCreate.getDossier().getId(), gesuchsperiode.getId());
		}
		Optional<Gesuch> gesuchForMutationOpt = getNeustesVerfuegtesGesuchFuerGesuch(gesuchsperiode,
			gesuchToCreate.getDossier(), true);
		if (gesuchForMutationOpt.isPresent()) {
			logInfo.append('\n').append("... und es gibt ein Gesuch zu kopieren");
			Gesuch gesuchForMutation = gesuchForMutationOpt.get();

			// Falls im "alten" Gesuch noch Tagesschule-Anmeldungen im status AUSGELOEST sind, müssen
			// diese nun gespeichert (im gleichen Status, Verfügung erstellen) werden, damit künftig für
			// die Berechnung die richtige FinSit verwendet wird!
			zuMutierendeAnmeldungenAbschliessen(gesuchForMutation);

			Gesuch mutation = gesuchForMutation.copyForMutation(
				new Gesuch(),
				eingangsart,
				gesuchToCreate.getRegelStartDatum() != null ? gesuchToCreate.getRegelStartDatum() : LocalDate.now(),
				getNextLaufnummerForGesuch(gesuchForMutation));

			// wenn eine Anmeldung kopiert wird, dann wird die neuste Mutation auf gueltig gesetzt und der Vorgänger auf
			// ungültig. Falls der Vorgänger aber im Status MUTATION_IGNORIERT war, müssen wir weiter zurück um
			// sicherzustellen, dass dessen Vorgänger auch nicht gültig ist
			vorgaengerVonGueltigenAnmeldungenUngueltigSetzen(mutation);

			return mutation;
		}
		return gesuchToCreate;
	}

	private int getNextLaufnummerForGesuch(Gesuch gesuch) {
		Gesuch neustesGesuch = getNeustesGesuchFuerGesuch(gesuch)
			.orElse(gesuch);
		return neustesGesuch.getLaufnummer() + 1;
	}

	@Nonnull
	private Gesuch createErneuerungsgesuch(
		@Nonnull Gesuch gesuchToCreate, @Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Eingangsart eingangsart,
		@Nonnull StringBuilder logInfo) {
		Optional<Gesuch> gesuchForErneuerungOptional = getGesuchFuerErneuerungsantrag(gesuchToCreate.getDossier());
		if (gesuchForErneuerungOptional.isPresent()) {
			logInfo.append('\n').append("... und es gibt ein Gesuch zu kopieren");
			Gesuch gesuchForErneuerung = gesuchForErneuerungOptional.get();
			return gesuchForErneuerung.copyForErneuerung(
				new Gesuch(),
				gesuchsperiode,
				eingangsart,
				gesuchToCreate.getRegelStartDatum() != null ? gesuchToCreate.getRegelStartDatum() : LocalDate.now());
		}
		return gesuchToCreate;
	}

	@Nonnull
	private Gesuch createErstgesuch(
		@Nonnull Gesuch gesuchToCreate, @Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Eingangsart eingangsart, @Nonnull StringBuilder logInfo) {
		List<String> existingForFall = getAllGesuchIDsForFall(gesuchToCreate.getFall().getId());
		if (CollectionUtils.isNotEmpty(existingForFall)) {
			// Es ist das erste in einem neuen Dossier
			return createErstgesuchInNeuemDossier(gesuchToCreate, gesuchsperiode, eingangsart, logInfo);
		}
		return gesuchToCreate;
	}

	@Nonnull
	private Gesuch createErstgesuchInNeuemDossier(
		@Nonnull Gesuch gesuchToCreate,
		@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull Eingangsart eingangsart, @Nonnull StringBuilder logInfo) {
		Optional<Gesuch> gesuchToCopyOptional =
			getNeustesGeprueftesGesuchInAnotherDossier(gesuchToCreate.getDossier());
		if (gesuchToCopyOptional.isPresent()) {
			logInfo.append('\n').append("Es ist das erste Gesuch in einem neuen Dossier!");
			Gesuch gesuchToCopy = gesuchToCopyOptional.get();

			if (gesuchsperiode.equals(gesuchToCopy.getGesuchsperiode())) {
				return gesuchToCopy.copyForMutationNeuesDossier(gesuchToCreate, eingangsart,
					gesuchToCreate.getDossier());
			}

			return gesuchToCopy.copyForErneuerungsgesuchNeuesDossier(
				gesuchToCreate,
				eingangsart,
				gesuchToCreate.getDossier(),
				gesuchsperiode,
				gesuchToCreate.getRegelStartDatum() != null ?
					gesuchToCreate.getRegelStartDatum() :
					LocalDate.now());
		}
		return gesuchToCreate;
	}

	@Nonnull
	@Override
	public Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory, @Nullable Benutzer saveAsUser) {
		return updateGesuch(gesuch, saveInStatusHistory, saveAsUser, true);
	}

	@Nonnull
	@Override
	public Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory) {
		return updateGesuch(gesuch, saveInStatusHistory, null, true);
	}

	@Nonnull
	@Override
	public Gesuch updateGesuch(
		@Nonnull Gesuch gesuch,
		boolean saveInStatusHistory,
		@Nullable Benutzer saveAsUser,
		boolean doAuthCheck) {

		if (doAuthCheck) {
			authorizer.checkWriteAuthorization(gesuch);
		}
		Objects.requireNonNull(gesuch);
		final Gesuch gesuchToMerge = removeFinanzieleSituationIfNeeded(gesuch);
		final Gesuch merged = persistence.merge(gesuchToMerge);
		if (saveInStatusHistory) {
			antragStatusHistoryService.saveStatusChange(merged, saveAsUser);
		}

		if (gesuch.getStatus() == AntragStatus.VERFUEGEN
			|| gesuch.getStatus() == AntragStatus.NUR_SCHULAMT
			|| gesuch.getStatus() == AntragStatus.KEIN_KONTINGENT
		) {
			KindContainer[] kindArray =
				gesuch.getKindContainers().toArray(new KindContainer[gesuch.getKindContainers().size()]);
			for (int i = 0; i < gesuch.getKindContainers().size(); i++) {
				KindContainer kindContainerToWorkWith = kindArray[i];
				AnmeldungTagesschule[] anmeldungTagesschuleArray =
					kindContainerToWorkWith.getAnmeldungenTagesschule()
						.toArray(new AnmeldungTagesschule[kindContainerToWorkWith.getAnmeldungenTagesschule().size()]);
				for (int j = 0; j < kindContainerToWorkWith.getAnmeldungenTagesschule().size(); j++) {
					AnmeldungTagesschule anmeldungTagesschule = anmeldungTagesschuleArray[j];
					// Alle Anmeldungen, die mindestens AKZEPTIERT waren, werden nun "verfügt"
					if (anmeldungTagesschule.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT) {
						this.verfuegungService.anmeldungTagesschuleUebernehmen(anmeldungTagesschule);
					}
				}
				AnmeldungFerieninsel[] anmeldungFerieninselArray =
					kindContainerToWorkWith.getAnmeldungenFerieninsel()
						.toArray(new AnmeldungFerieninsel[kindContainerToWorkWith.getAnmeldungenFerieninsel().size()]);
				for (int j = 0; j < kindContainerToWorkWith.getAnmeldungenFerieninsel().size(); j++) {
					AnmeldungFerieninsel anmeldungFerieninsel = anmeldungFerieninselArray[j];
					// Alle Anmeldungen, die mindestens AKZEPTIERT waren, werden nun "verfügt"
					if (anmeldungFerieninsel.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT) {
						this.verfuegungService.anmeldungFerieninselUebernehmen(anmeldungFerieninsel);
					}
				}
			}
		}

		return merged;
	}

	private Gesuch removeFinanzieleSituationIfNeeded(@NotNull Gesuch gesuch) {
		if (!EbeguUtil.isFinanzielleSituationRequired(gesuch)) {
			resetFieldsFamiliensituation(gesuch);
			removeFinanzielleSituationGS(gesuch.getGesuchsteller1());
			removeFinanzielleSituationGS(gesuch.getGesuchsteller2());
			gesuch.setEinkommensverschlechterungInfoContainer(null);
		}
		return gesuch;
	}

	private void removeFinanzielleSituationGS(@Nullable GesuchstellerContainer gesuchsteller) {
		if (gesuchsteller != null) {
			gesuchsteller.setFinanzielleSituationContainer(null);
			gesuchsteller.setEinkommensverschlechterungContainer(null);
		}
	}

	private void resetFieldsFamiliensituation(@NotNull Gesuch gesuch) {
		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		if (familiensituation != null) {
			if (Objects.equals(true, familiensituation.getSozialhilfeBezueger())) {
				familiensituation.setVerguenstigungGewuenscht(null);
			}
			familiensituation.setGemeinsameSteuererklaerung(null);
		}
	}

	@Nonnull
	@Override
	@Interceptors(UpdateStatusInterceptor.class)
	public Optional<Gesuch> findGesuch(@Nonnull String key) {
		return findGesuch(key, true);
	}

	@Nonnull
	@Override
	@Interceptors(UpdateStatusInterceptor.class)
	public Optional<Gesuch> findGesuch(@Nonnull String key, boolean doAuthCheck) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Gesuch gesuch = persistence.find(Gesuch.class, key);
		if (doAuthCheck) {
			authorizer.checkReadAuthorization(gesuch);
		}
		return Optional.ofNullable(gesuch);
	}

	@Override
	@Nonnull
	public Gesuch findGesuchForFreigabe(
		@Nonnull String gesuchId, @Nonnull Integer anzahlZurueckgezogen,
		boolean checkAnzahlZurueckgezogen) {
		Objects.requireNonNull(gesuchId, "gesuchId muss gesetzt sein");
		Gesuch gesuch = persistence.find(Gesuch.class, gesuchId);
		if (gesuch == null) {
			throw new EbeguRuntimeException("", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		}
		if (checkAnzahlZurueckgezogen && !Objects.equals(
			anzahlZurueckgezogen,
			gesuch.getAnzahlGesuchZurueckgezogen())) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"findGesuchForFreigabe",
				ErrorCodeEnum.ERROR_GESUCH_DURCH_GS_ZURUECKGEZOGEN);
		}
		authorizer.checkReadAuthorizationForFreigabe(gesuch);
		return gesuch;
	}

	@Override
	public List<Gesuch> findReadableGesuche(@Nullable Collection<String> gesuchIds) {
		if (gesuchIds == null || gesuchIds.isEmpty()) {
			return Collections.emptyList();
		}
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateId = root.get(AbstractEntity_.id).in(gesuchIds);
		predicates.add(predicateId);
		Predicate predicateMandant = cb.equal(root.get(Gesuch_.dossier).get(Dossier_.fall).get(Fall_.mandant), principalBean.getMandant());
		predicates.add(predicateMandant);
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		query.orderBy(cb.asc(root.get(Gesuch_.dossier).get(Dossier_.fall).get(AbstractEntity_.id)));
		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query);
		return criteriaResults.stream()
			.filter(gesuch -> this.booleanAuthorizer.hasReadAuthorization(gesuch))
			.collect(Collectors.toList());
	}

	@Nonnull
	@Override
	public Collection<Gesuch> getAllGesuche() {
		return new ArrayList<>(criteriaQueryHelper.getAll(Gesuch.class));
	}

	@Override
	public void removeGesuch(@Nonnull String gesuchId, GesuchDeletionCause deletionCause) {
		Objects.requireNonNull(gesuchId);
		// Gesuch loeschen ist auch moeglich, wenn der Zugriff darauf nicht erlaubt ist:
		// Beim Loeschen einer Online-Mutation durch den Admin. Darum hier kein Auth-Check
		Gesuch gesToRemove = findGesuch(gesuchId, false)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"removeGesuch",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchId));
		// Da die Auth Pruefung nicht auf dem Gesuch selber gemacht werden kann (siehe oben)
		// machen wir sie auf dem Dossier: Der Benutzer muss grundsaetzlich fuer dieses Dossier
		// zustaendig sein
		authorizer.checkWriteAuthorizationDossier(gesToRemove.getDossier());
		//Remove all depending objects
		wizardStepService.removeSteps(gesToRemove);  //wizard steps removen
		mahnungService.removeAllMahnungenFromGesuch(gesToRemove);
		generatedDokumentService.removeAllGeneratedDokumenteFromGesuch(gesToRemove);
		dokumentGrundService.removeAllDokumentGrundeFromGesuch(gesToRemove);
		fileSaverService.removeAllFromSubfolder(gesToRemove.getId());
		antragStatusHistoryService.removeAllAntragStatusHistoryFromGesuch(gesToRemove);
		zahlungService.deleteZahlungspositionenOfGesuch(gesToRemove);
		massenversandService.removeMassenversandGesucheForGesuch(gesToRemove);
		// Wir loeschen hier alle Mitteilungn und Abweichungen.
		// Im Fall einer Loeschung einer OnlineMutation sind die Mitteilungen und auch die Abweichungen
		// zu diesem Zeitpunkt bereits auf das Vorgaenger Gesuch umgehaengt.
		mitteilungService.removeAllBetreuungMitteilungenForGesuch(gesToRemove);
		mitteilungService.removeAllBetreuungspensumAbweichungenForGesuch(gesToRemove);
		internePendenzService.deleteAllInternePendenz(gesToRemove);

		resetMutierteAnmeldungen(gesToRemove);

		// Jedes Loeschen eines Gesuchs muss protokolliert werden
		GesuchDeletionLog gesuchDeletionLog = new GesuchDeletionLog(gesToRemove, deletionCause);
		gesuchDeletionLogService.saveGesuchDeletionLog(gesuchDeletionLog);

		//Finally remove the Gesuch when all other objects are really removed
		persistence.remove(gesToRemove);
	}

	/**
	 * Nimmt alle Anmeldungen vom eingegebenen Gesuch und setzt alle vorgaengerAnmeldungen auf
	 * AnmeldungMutationZustand.AKTUELLE_ANMELDUNG.
	 * Wenn eine Mutation geloescht wird, ist das vorgaengerGesuch und deshalb auch die vorgaengerAnmeldungen AKTUELL
	 *
	 * @param currentGesuch das zurzeit neueste Gesuch
	 */
	private void resetMutierteAnmeldungen(@Nonnull Gesuch currentGesuch) {
		currentGesuch.extractAllAnmeldungen().stream()
			.filter(betreuung -> betreuung.getVorgaengerId() != null)
			.filter(betreuung -> betreuung.getBetreuungsangebotTyp().isSchulamt())
			.forEach(betreuung -> {
				AbstractAnmeldung vorgaenger =  betreuungService.findVorgaengerAnmeldungNotIgnoriert(betreuung);
				vorgaenger.setAnmeldungMutationZustand(AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
				vorgaenger.setGueltig(true); // Die alte Anmeldung ist wieder die gueltige
				if (vorgaenger.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST
					&& vorgaenger.getBetreuungsangebotTyp().isTagesschule()) {
					// Sonderfall: Wenn die Anmeldung auf dem Vorgänger im Status AUSGELOEST war, wurde beim erstellen
					// der Mutation eine Verfügung gespeichert. Diese muss nun wieder gelöscht werden
					vorgaenger.setVerfuegung(null);
				}
				persistence.merge(vorgaenger);
			});
	}

	private void zuMutierendeAnmeldungenAbschliessen(@Nonnull Gesuch currentGesuch) {
		currentGesuch.extractAllAnmeldungen().stream()
			.filter(anmeldung -> anmeldung.getBetreuungsangebotTyp().isTagesschule())
			.forEach(anmeldung -> {
				AbstractAnmeldung anmeldungToAbschliessen = anmeldung;

				if (anmeldung.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_MUTATION_IGNORIERT) {
					anmeldungToAbschliessen = betreuungService.findVorgaengerAnmeldungNotIgnoriert(anmeldung);
				}

				if (anmeldungToAbschliessen.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST) {
					this.verfuegungService.anmeldungSchulamtAusgeloestAbschliessen(
						anmeldungToAbschliessen.extractGesuch().getId(),
						anmeldungToAbschliessen.getId());
				}
			});
	}

	private void vorgaengerVonGueltigenAnmeldungenUngueltigSetzen(@Nonnull Gesuch currentGesuch) {
		currentGesuch.extractAllAnmeldungen().stream()
			.filter(AbstractPlatz::isGueltig)
			.forEach(anmeldung -> {
				betreuungService.updateGueltigFlagOnPlatzAndVorgaenger(anmeldung);
			});
	}

	@Override
	@Nonnull
	public List<Gesuch> getAntraegeOfDossier(@Nonnull Dossier dossier) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		// Fall
		Predicate predicate = cb.equal(root.get(Gesuch_.dossier), dossier);
		// Keine Papier-Antraege, die noch nicht verfuegt sind
		Predicate predicatePapier = cb.equal(root.get(Gesuch_.eingangsart), Eingangsart.PAPIER);
		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates()).not();
		Predicate predicateUnverfuegtesPapiergesuch =
			CriteriaQueryHelper.concatenateExpressions(cb, predicatePapier, predicateStatus);
		if (predicateUnverfuegtesPapiergesuch != null) {
			Predicate predicateNichtUnverfuegtePapierGesuch = predicateUnverfuegtesPapiergesuch.not();
			query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));
			query.where(predicate, predicateNichtUnverfuegtePapierGesuch);

			List<Gesuch> gesuche = persistence.getCriteriaResults(query);
			authorizer.checkReadAuthorizationGesuche(gesuche);
			return gesuche;
		}
		return Collections.emptyList();
	}

	/**
	 * Diese Methode sucht alle Antraege die zu dem gegebenen Dossier gehoeren.
	 * Die Antraege werden aber je nach Benutzerrolle gefiltert.
	 * - SACHBEARBEITER_TRAEGERSCHAFT oder SACHBEARBEITER_INSTITUTION - werden nur diejenige Antraege zurueckgegeben,
	 * die mindestens ein Angebot fuer die InstituionEn des Benutzers haben
	 * - SACHBEARBEITER_TS/ADMIN_TS - werden nur diejenige Antraege zurueckgegeben, die mindestens ein Angebot von Typ
	 * Schulamt haben
	 * - SACHBEARBEITER_BG oder ADMIN_BG - werden nur diejenige Antraege zurueckgegeben, die ein Angebot von einem
	 * anderen
	 * Typ als Schulamt haben oder ueberhaupt kein Angebot
	 */
	@Nonnull
	@Override
	public List<JaxAntragDTO> getAllAntragDTOForDossier(String dossierId) {
		authorizer.checkReadAuthorizationDossier(dossierId);

		final Optional<Benutzer> optBenutzer = benutzerService.getCurrentBenutzer();
		if (optBenutzer.isPresent()) {
			final Benutzer benutzer = optBenutzer.get();

			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<JaxAntragDTO> query = cb.createQuery(JaxAntragDTO.class);
			Root<Gesuch> root = query.from(Gesuch.class);

			Join<InstitutionStammdaten, Institution> institutionJoin = null;
			Join<Betreuung, InstitutionStammdaten> institutionstammdatenJoin = null;

			if (principalBean.isCallerInAnyOfRole(
				UserRole.ADMIN_TRAEGERSCHAFT,
				UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
				UserRole.ADMIN_INSTITUTION,
				UserRole.SACHBEARBEITER_INSTITUTION,
				UserRole.SACHBEARBEITER_TS,
				UserRole.ADMIN_TS,
				UserRole.ADMIN_BG,
				UserRole.SACHBEARBEITER_BG,
				UserRole.ADMIN_GEMEINDE,
				UserRole.SACHBEARBEITER_GEMEINDE)) {
				// Join all the relevant relations only when the User belongs to Admin, JA, Schulamt, Institution or
				// Traegerschaft
				SetJoin<Gesuch, KindContainer> kindContainers = root.join(Gesuch_.kindContainers, JoinType.LEFT);
				SetJoin<KindContainer, Betreuung> betreuungen =
					kindContainers.join(KindContainer_.betreuungen, JoinType.LEFT);
				institutionstammdatenJoin = betreuungen.join(Betreuung_.institutionStammdaten, JoinType.LEFT);
				institutionJoin = institutionstammdatenJoin.join(InstitutionStammdaten_.institution, JoinType.LEFT);
			}
			Join<Gesuch, Dossier> dossierJoin = root.join(Gesuch_.dossier);
			Join<Dossier, Fall> fallJoin = dossierJoin.join(Dossier_.fall);

			Join<Fall, Benutzer> besitzerJoin = fallJoin.join(Fall_.besitzer, JoinType.LEFT);

			query.multiselect(
				root.get(AbstractEntity_.id),
				root.get(Gesuch_.gesuchsperiode).get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
				root.get(Gesuch_.gesuchsperiode).get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
				root.get(Gesuch_.eingangsdatum),
				root.get(Gesuch_.eingangsdatumSTV),
				root.get(Gesuch_.typ),
				root.get(Gesuch_.status),
				root.get(Gesuch_.laufnummer),
				root.get(Gesuch_.eingangsart),
				besitzerJoin.get(Benutzer_.username) //wir machen hier extra vorher einen left join
			).distinct(true);

			ParameterExpression<String> dossierIdParam = cb.parameter(String.class, "dossierId");

			List<Predicate> predicatesToUse = new ArrayList<>();
			Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier).get(AbstractEntity_.id), dossierIdParam);
			predicatesToUse.add(dossierPredicate);

			// Alle AUSSER Superadmin, Gesuchsteller, Institution und Trägerschaft muessen im Status eingeschraenkt
			// werden,
			// d.h. sie duerfen IN_BEARBEITUNG_GS und FREIGABEQUITTUNG NICHT sehen
			if (!(principalBean.isCallerInAnyOfRole(
				UserRole.SUPER_ADMIN,
				UserRole.GESUCHSTELLER,
				UserRole.ADMIN_SOZIALDIENST,
				UserRole.SACHBEARBEITER_SOZIALDIENST,
				UserRole.ADMIN_TRAEGERSCHAFT,
				UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
				UserRole.ADMIN_INSTITUTION,
				UserRole.SACHBEARBEITER_INSTITUTION))) {
				// Nur GS darf ein Gesuch sehen, das sich im Status BEARBEITUNG_GS oder FREIGABEQUITTUNG befindet
				predicatesToUse.add(root.get(Gesuch_.status)
					.in(
						AntragStatus.IN_BEARBEITUNG_GS,
						AntragStatus.IN_BEARBEITUNG_SOZIALDIENST,
						AntragStatus.FREIGABEQUITTUNG)
					.not());
			}

			if (institutionJoin != null) {
				// only if the institutionJoin was set
				if (principalBean.isCallerInAnyOfRole(ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT)) {
					predicatesToUse.add(cb.equal(
						institutionJoin.get(Institution_.traegerschaft),
						benutzer.getTraegerschaft()));
				}
				if (principalBean.isCallerInAnyOfRole(ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION)) {
					// es geht hier nicht um die institutionJoin des zugewiesenen benutzers sondern um die
					// institutionJoin des eingeloggten benutzers
					predicatesToUse.add(cb.equal(institutionJoin, benutzer.getInstitution()));
				}
			}
			if (principalBean.isCallerInRole(UserRole.GESUCHSTELLER)) {
				// Keine Papier-Antraege, die noch nicht verfuegt sind
				Predicate predicatePapier = cb.equal(root.get(Gesuch_.eingangsart), Eingangsart.PAPIER);
				Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates()).not();
				Predicate predicateUnverfuegtesPapiergesuch =
					CriteriaQueryHelper.concatenateExpressions(cb, predicatePapier, predicateStatus);
				if (predicateUnverfuegtesPapiergesuch != null) {
					Predicate predicateNichtUnverfuegtePapierGesuch = predicateUnverfuegtesPapiergesuch.not();
					predicatesToUse.add(predicateNichtUnverfuegtePapierGesuch);
				}
			}

			query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse));

			query.orderBy(cb.asc(root.get(Gesuch_.laufnummer)));
			TypedQuery<JaxAntragDTO> q = persistence.getEntityManager().createQuery(query);
			q.setParameter(dossierIdParam, dossierId);

			return q.getResultList();
		}
		return new ArrayList<>();
	}

	@Override
	@Nonnull
	public List<String> getAllGesuchIDsForFall(String fallId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		query.select(root.get(AbstractEntity_.id));

		ParameterExpression<String> fallIdParam = cb.parameter(String.class, "fallId");

		Path<Fall> fallPath = root.get(Gesuch_.dossier).get(Dossier_.fall);
		Predicate fallPredicate = cb.equal(fallPath.get(AbstractEntity_.id), fallIdParam);
		query.where(fallPredicate);
		query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));
		TypedQuery<String> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(fallIdParam, fallId);

		return q.getResultList();
	}

	@Override
	@Nonnull
	public List<String> getAllGesuchIDsForDossier(@Nonnull String dossierId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		query.select(root.get(AbstractEntity_.id));

		ParameterExpression<String> dossierIdParam = cb.parameter(String.class, "dossierId");

		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier).get(AbstractEntity_.id), dossierIdParam);
		query.where(dossierPredicate);
		query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));
		TypedQuery<String> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(dossierIdParam, dossierId);

		return q.getResultList();
	}

	@Override
	@Nonnull
	public List<Gesuch> getAllGesuchForDossier(@Nonnull String dossierId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<String> dossierIdParam = cb.parameter(String.class, "dossierId");

		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier).get(AbstractEntity_.id), dossierIdParam);
		query.where(dossierPredicate);
		query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));
		TypedQuery<Gesuch> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(dossierIdParam, dossierId);

		return q.getResultList();
	}

	@Override
	@Nonnull
	public List<Gesuch> getAllGesucheForDossierAndPeriod(
		@Nonnull Dossier dossier,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		authorizer.checkReadAuthorizationDossier(dossier);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier), dossier);
		Predicate gesuchsperiodePredicate = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		query.where(dossierPredicate, gesuchsperiodePredicate);
		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	public List<String> getAllGesucheIdsForDossierAndPeriod(
		@Nonnull Dossier dossier,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		authorizer.checkReadAuthorizationDossier(dossier);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(String.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		query.select(root.get(AbstractEntity_.id));
		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier), dossier);
		Predicate gesuchsperiodePredicate = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		query.where(dossierPredicate, gesuchsperiodePredicate);
		return persistence.getCriteriaResults(query);
	}

	@Override
	public Gesuch antragFreigabequittungErstellen(@Nonnull Gesuch gesuch, AntragStatus statusToChangeTo) {
		authorizer.checkWriteAuthorization(gesuch);

		gesuchValidationService.validateGesuchComplete(gesuch);

		gesuch.setFreigabeDatum(LocalDate.now());

		if (AntragStatus.FREIGEGEBEN == statusToChangeTo) {
			// Nur wenn das Gesuch direkt freigegeben wird, muessen wir das Eingangsdatum auch setzen
			gesuch.setEingangsdatum(LocalDate.now());
		}

		gesuch.setStatus(statusToChangeTo);

		// Step Freigabe gruen
		wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.FREIGABE);

		return updateGesuch(gesuch, true, null);
	}

	@Nonnull
	@Override
	public Gesuch antragZurueckziehen(@Nonnull String gesuchId) {
		Optional<Gesuch> gesuchOptional = Optional.ofNullable(persistence.find(Gesuch.class, gesuchId));
		if (gesuchOptional.isPresent()) {
			Gesuch gesuch = gesuchOptional.get();

			if (gesuch.getTyp() == AntragTyp.MUTATION
				|| Eingangsart.ONLINE != gesuch.getEingangsart()
				|| gesuch.getStatus() != AntragStatus.FREIGABEQUITTUNG) {
				throw new EbeguRuntimeException(
					KibonLogLevel.WARN,
					"antragZurueckziehen",
					"Only Online Erst-/Erneuerungsgesuche "
						+ "can be reverted");
			}

			LOG.info("Freigabe des Gesuchs {} wurde zurückgezogen", gesuch.getJahrFallAndGemeindenummer());

			// Den Gesuchsstatus auf In Bearbeitung GS oder Sozialdienst zurücksetzen
			if (gesuch.getFall().isSozialdienstFall()) {
				gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_SOZIALDIENST);
			} else {
				gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
			}
			// Das Freigabedatum muss wieder zurückgesetzt werden, falls es ein Online Gesuch ist
			gesuch.setFreigabeDatum(null);

			// jedesmal wenn die Freigabe zurueckgezogen wird, erhöhen wir den Counter um 1, damit wir wissen,
			// ob der Gesuchsteller die richtige Freigabequittung eingeschickt hat.
			gesuch.setAnzahlGesuchZurueckgezogen(gesuch.getAnzahlGesuchZurueckgezogen() + 1);

			// bestehende Freigabequittung löschen
			generatedDokumentService.removeFreigabequittungFromGesuch(gesuch);

			// den WizardStep anpassen
			wizardStepService.unsetWizardStepFreigabe(gesuch.getId());

			final Gesuch merged = persistence.merge(gesuch);
			antragStatusHistoryService.saveStatusChange(merged, null);
			return merged;
		}
		throw new EbeguEntityNotFoundException("antragZurueckziehen", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchId);
	}



	@Override
	@Nonnull
	public Gesuch setKeinKontingent(@Nonnull Gesuch gesuch) {
		gesuch.setStatus(AntragStatus.KEIN_KONTINGENT);
		return updateGesuch(gesuch, true, null);
	}

	@Override
	@Nonnull
	public Gesuch setBeschwerdeHaengigForPeriode(@Nonnull Gesuch gesuch) {
		final List<Gesuch> allGesucheForDossier =
			getAllGesucheForDossierAndPeriod(gesuch.getDossier(), gesuch.getGesuchsperiode());
		allGesucheForDossier.iterator().forEachRemaining(gesuchLoop -> {
			if (gesuch.equals(gesuchLoop)) {
				gesuchLoop.setStatus(AntragStatus.BESCHWERDE_HAENGIG);
				updateGesuch(gesuchLoop, true, null);
			}
			// Flag nicht über Service setzen, da u.U. Gesuch noch inBearbeitungGS
			gesuchLoop.setGesperrtWegenBeschwerde(true);
			persistence.merge(gesuchLoop);
		});
		return gesuch;
	}

	@Override
	@Nonnull
	public Gesuch setAbschliessen(@Nonnull Gesuch gesuch) {
		if (gesuch.hasOnlyBetreuungenOfSchulamt()) {
			gesuch.setTimestampVerfuegt(LocalDateTime.now());
			gesuch.setStatus(AntragStatus.NUR_SCHULAMT);
			wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.VERFUEGEN);

			final Gesuch persistedGesuch = updateGesuch(gesuch, true);

			if (gesuch.getVorgaengerId() != null) {
				final Optional<Gesuch> vorgaengerOpt = findGesuch(gesuch.getVorgaengerId());
				vorgaengerOpt.ifPresent(this::setGesuchAndVorgaengerUngueltig);
			}

			// neues Gesuch erst nachdem das andere auf ungültig gesetzt wurde setzen wegen unique key
			gesuch.setGueltig(true);

			createFinSitDokument(persistedGesuch, "setAbschliessen");

			return persistedGesuch;
		}
		throw new EbeguRuntimeException(
			"setAbschliessen",
			ErrorCodeEnum.ERROR_INVALID_EBEGUSTATE,
			"Nur reine Schulamt-Gesuche können abgeschlossen werden");
	}

	@Override
	@Nonnull
	public Gesuch removeBeschwerdeHaengigForPeriode(@Nonnull Gesuch gesuch) {
		Dossier dossier = gesuch.getDossier();
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();
		final List<Gesuch> allGesucheForDossier = getAllGesucheForDossierAndPeriod(dossier, gesuchsperiode);
		allGesucheForDossier.iterator().forEachRemaining(gesuchLoop -> {
			if (gesuch.equals(gesuchLoop) && AntragStatus.BESCHWERDE_HAENGIG == gesuchLoop.getStatus()) {
				final AntragStatusHistory lastStatusChange =
					antragStatusHistoryService.findLastStatusChangeBeforeBeschwerde(gesuchLoop);
				gesuchLoop.setStatus(lastStatusChange.getStatus());
				updateGesuch(gesuchLoop, true, null);
			}
			// Flag nicht über Service setzen, da u.U. Gesuch noch inBearbeitungGS
			gesuchLoop.setGesperrtWegenBeschwerde(false);
			persistence.merge(gesuchLoop);
		});
		return gesuch;
	}

	private boolean isThereAnyOpenMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		List<Gesuch> criteriaResults = findExistingOpenMutationen(dossier, gesuchsperiode);
		return !criteriaResults.isEmpty();
	}

	/**
	 * Diese Methode gibt eine Liste zurueck. Diese Liste sollte aber maximal eine Mutation enthalten, da es
	 * unmoeglich ist,
	 * mehrere offene Mutationen in einem Dossier fuer dieselbe Gesuchsperiode zu haben. Rechte werden nicht
	 * beruecksichtigt
	 * d.h. alle Gesuche werden geguckt und daher die richtige letzte Mutation wird zurueckgegeben.
	 */
	private List<Gesuch> findExistingOpenMutationen(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateMutation = root.get(Gesuch_.typ).in(AntragTyp.MUTATION);
		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates()).not();
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateMutation, predicateStatus, predicateGesuchsperiode, predicateDossier);
		query.select(root);
		return persistence.getCriteriaResults(query);
	}

	private List<Gesuch> findExistingFolgegesuch(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateMutation = root.get(Gesuch_.typ).in(AntragTyp.ERNEUERUNGSGESUCH);
		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getInBearbeitungGSStates());
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateMutation, predicateStatus, predicateGesuchsperiode, predicateDossier);
		query.select(root);
		return persistence.getCriteriaResults(query);
	}

	private void ensureUniqueErstgesuchProDossierAndGesuchsperiode(@Nonnull Gesuch gesuchToPersist) {
		if (gesuchToPersist.getTyp() != AntragTyp.MUTATION) {
			// Von allem ausser MUTATION darf es pro Dossier und Gesuchsperiode nur einen Antrag geben
			List<Gesuch> existingGesuch = findExistingGesuch(
				gesuchToPersist.getDossier(),
				gesuchToPersist.getGesuchsperiode(),
				gesuchToPersist.getTyp());
			if (!existingGesuch.isEmpty()) {
				String message = MessageFormat.format(
					"Es gibt schon ein Gesuch dieses Typs fuer die Gesuchsperiode {0} und Dossier {1} / {2}: ",
					gesuchToPersist.getGesuchsperiode().getGesuchsperiodeString(),
					String.valueOf(gesuchToPersist.getDossier().getFall().getFallNummer()),
					gesuchToPersist.getDossier().getGemeinde().getName());
				throw new EbeguRuntimeException(
					"ensureUniqueErstgesuchProDossierAndGesuchsperiode",
					message,
					ErrorCodeEnum.ERROR_ERSTGESUCH_ALREADY_EXISTS);
			}
		}
	}

	private List<Gesuch> findExistingGesuch(
		@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode, @Nonnull AntragTyp typ
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateTyp = cb.equal(root.get(Gesuch_.typ), typ);
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateTyp, predicateGesuchsperiode, predicateDossier);
		query.select(root);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	private Eingangsart calculateEingangsart(Gesuch gesuchToCreate) {
		Eingangsart eingangsart;
		if (this.principalBean.isCallerInAnyOfRole(
			UserRole.GESUCHSTELLER,
			UserRole.ADMIN_SOZIALDIENST,
			UserRole.SACHBEARBEITER_SOZIALDIENST)
			|| (gesuchToCreate.getFall().getSozialdienstFall() != null
			&& this.principalBean.isCallerInRole(UserRole.SUPER_ADMIN))) {
			eingangsart = Eingangsart.ONLINE;
		} else {
			eingangsart = Eingangsart.PAPIER;
		}
		return eingangsart;
	}

	@Nonnull
	private AntragStatus calculateInitialStatus(Gesuch gesuch) {
		AntragStatus status;
		if (this.principalBean.isCallerInRole(UserRole.GESUCHSTELLER)) {
			status = AntragStatus.IN_BEARBEITUNG_GS;
		} else if (gesuch.getFall().getSozialdienstFall() != null
			&& this.principalBean.isCallerInAnyOfRole(
			UserRole.ADMIN_SOZIALDIENST,
			UserRole.SACHBEARBEITER_SOZIALDIENST,
			UserRole.SUPER_ADMIN)) {
			status = AntragStatus.IN_BEARBEITUNG_SOZIALDIENST;
		} else {
			status = AntragStatus.IN_BEARBEITUNG_JA;
		}
		return status;
	}

	@Override
	@Nonnull
	public Collection<Gesuch> getNeuesteVerfuegtesGesuchProDossierFuerGemeindeUndGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde
	) {
		final Query nativeQuery = persistence.getEntityManager().createNativeQuery(
			"select g.* "
				+ "from gesuch g "
				+ "inner join dossier d on g.dossier_id = d.id "
				+ "inner join fall f on d.fall_id = f.id "
				+ "inner join gesuchsperiode gp on g.gesuchsperiode_id = gp.id "
				+ "inner join gemeinde gem on d.gemeinde_id = gem.id "
				+ "where g.timestamp_verfuegt is not null and g.gueltig is true "
				+ "and gem.id = UNHEX(REPLACE(?1, '-','')) "
				+ "and gp.id = UNHEX(REPLACE(?2, '-',''));", Gesuch.class
		);
		nativeQuery.setParameter(1, gemeinde.getId());
		nativeQuery.setParameter(2, gesuchsperiode.getId());
		final List<Gesuch> resultList = nativeQuery.getResultList();
		return resultList;
	}

	@Override
	@Nonnull
	public Optional<Gesuch> getNeustesVerfuegtesGesuchFuerGesuch(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier,
		boolean doAuthCheck) {

		if (doAuthCheck) {
			authorizer.checkReadAuthorizationDossier(dossier);
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<Dossier> dossierParam = cb.parameter(Dossier.class, "dossierId");
		ParameterExpression<Gesuchsperiode> gesuchsperiodeParam = cb.parameter(Gesuchsperiode.class, "gp");

		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates());
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiodeParam);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossierParam);
		Predicate predicateGueltig = cb.equal(root.get(Gesuch_.gueltig), Boolean.TRUE);

		query.where(predicateStatus, predicateGesuchsperiode, predicateGueltig, predicateDossier);
		query.select(root);

		TypedQuery<Gesuch> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(dossierParam, dossier);
		typedQuery.setParameter(gesuchsperiodeParam, gesuchsperiode);

		List<Gesuch> criteriaResults = typedQuery.getResultList();

		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		Gesuch gesuch = criteriaResults.get(0);
		if (doAuthCheck) {
			authorizer.checkReadAuthorization(gesuch);
		}
		return Optional.of(gesuch);
	}

	private Optional<Gesuch> getNeustesGeprueftesGesuchInAnotherDossier(@Nonnull Dossier dossier) {

		authorizer.checkReadAuthorizationFall(dossier.getFall());

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Join<Gesuch, Dossier> joinDossier = root.join(Gesuch_.dossier);

		ParameterExpression<Fall> fallParam = cb.parameter(Fall.class, "fallId");

		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllGepruefteStatus());
		Predicate predicateNotDossier = cb.equal(root.get(Gesuch_.dossier), dossier).not();
		Predicate predicateFall = cb.equal(joinDossier.get(Dossier_.fall), fallParam);

		query.where(predicateStatus, predicateNotDossier, predicateFall);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		query.select(root);

		TypedQuery<Gesuch> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(fallParam, dossier.getFall());

		List<Gesuch> criteriaResults = typedQuery.getResultList();

		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		Gesuch gesuch = criteriaResults.get(0);
		authorizer.checkReadAuthorization(gesuch);
		return Optional.of(gesuch);
	}

	@Override
	@Nonnull
	public Optional<Gesuch> getNeustesGesuchFuerGesuch(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		return getNeustesGesuchForDossierAndGesuchsperiode(gesuch.getGesuchsperiode(), gesuch.getDossier(), true);
	}

	@Override
	public boolean isNeustesGesuch(@Nonnull Gesuch gesuch) {
		final Optional<String> idOfNeuestesGesuchOptional =
			getIdOfNeuestesGesuchForDossierAndGesuchsperiode(gesuch.getGesuchsperiode(), gesuch.getDossier());
		return idOfNeuestesGesuchOptional.isPresent() && Objects.equals(
			idOfNeuestesGesuchOptional.get(),
			gesuch.getId());
	}

	@Nonnull
	@Override
	public Optional<String> getIdOfNeuestesGesuchForDossierAndGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier
	) {
		// Da wir nur die ID zurueckgeben, koennen wir den AuthCheck weglassen
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(String.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateGesuchsperiode, predicateDossier);
		query.select(root.get(Gesuch_.id));
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		final List<String> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(criteriaResults.get(0));
	}

	/**
	 * Da es eine private Methode ist, ist es sicher, als Parameter zu fragen, ob man nach ReadAuthorization pruefen
	 * muss.
	 * Das Interface sollte aber diese Moeglichkeit nur versteckt durch bestimmte Methoden anbieten.
	 */
	@Nonnull
	private Optional<Gesuch> getNeustesGesuchForDossierAndGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier,
		boolean checkReadAuthorization) {

		authorizer.checkReadAuthorizationDossier(dossier);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateGesuchsperiode, predicateDossier);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		Gesuch gesuch = criteriaResults.get(0);
		if (checkReadAuthorization) {
			authorizer.checkReadAuthorization(gesuch);
		}
		return Optional.of(gesuch);
	}

	/**
	 * Sucht
	 */
	@Override
	@Nonnull
	public Optional<String> getIdOfNeuestesGesuchForDossier(@Nonnull Dossier dossier) {
		authorizer.checkReadAuthorizationDossier(dossier);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateFall = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.select(root);
		query.where(predicateFall);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		// look for the first gesuch which the user can read
		for (Gesuch gesuch : criteriaResults) {
			try {
				authorizer.checkReadAuthorization(gesuch);
				return Optional.of(gesuch.getId());

			} catch (EJBAccessException e) {
				// nop
			}
		}

		return Optional.empty();
	}

	@Override
	@Nonnull
	public Optional<Gesuch> getNeustesGesuchFuerFallnumerForSchulamtInterface(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Long fallnummer) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		final Join<Gesuch, Dossier> joinDossier = root.join(Gesuch_.dossier);
		Predicate predicateGemeinde = cb.equal(joinDossier.get(Dossier_.gemeinde), gemeinde);
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateFallNummer = cb.equal(
			root.get(Gesuch_.dossier).get(Dossier_.fall).get(Fall_.fallNummer),
			fallnummer);
		// zuerst dies klaeren
		Predicate predicateFinSit = root.get(Gesuch_.finSitStatus).isNotNull();

		query.where(predicateGemeinde, predicateGesuchsperiode, predicateFallNummer, predicateFinSit);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		Gesuch gesuch = criteriaResults.get(0);
		return Optional.of(gesuch);
	}

	@Nonnull
	private Optional<Gesuch> getGesuchFuerErneuerungsantrag(@Nonnull Dossier dossier) {
		authorizer.checkReadAuthorizationDossier(dossier);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateDossier);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		Gesuch gesuch = criteriaResults.get(0);
		return Optional.of(gesuch);
	}

	@Override
	public int findGesucheNichtFreigegebenAndWarn(@Nonnull Mandant mandant) {

		Integer anzahlTageBisWarnungFreigabe =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_WARNUNG_FREIGABE, mandant);
		Integer anzahlTageBisLoeschungNachWarnungFreigabe =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE, mandant);
		if (anzahlTageBisWarnungFreigabe == null || anzahlTageBisLoeschungNachWarnungFreigabe == null) {
			throw new EbeguRuntimeException(
				"warnGesuchNichtFreigegeben",
				ApplicationPropertyKey.ANZAHL_TAGE_BIS_WARNUNG_FREIGABE.name() + " or " +
					ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE.name() + " not defined");
		}

		// Stichtag ist EndeTag -> Plus 1 Tag und dann less statt lessOrEqual
		LocalDateTime stichtag = LocalDate.now().minusDays(anzahlTageBisWarnungFreigabe).atStartOfDay().plusDays(1);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		// Status in Bearbeitung GS oder SZ/UD
		Predicate predicateStatus =
			root.get(Gesuch_.status).in(AntragStatus.IN_BEARBEITUNG_GS, AntragStatus.IN_BEARBEITUNG_SOZIALDIENST);
		// Irgendwann am Stichtag erstellt:
		Predicate predicateDatum = cb.lessThan(root.get(AbstractEntity_.timestampErstellt), stichtag);
		// Noch nicht gewarnt
		Predicate predicateNochNichtGewarnt = cb.isNull(root.get(Gesuch_.datumGewarntNichtFreigegeben));

		query.where(predicateStatus, predicateDatum, predicateNochNichtGewarnt);
		query.select(root);
		query.distinct(true);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Gesuch> gesucheNichtAbgeschlossenSeit = persistence.getCriteriaResults(query);

		int anzahl = gesucheNichtAbgeschlossenSeit.size();
		for (Gesuch gesuch : gesucheNichtAbgeschlossenSeit) {
			self.warnGesuchNichtFreigegeben(anzahlTageBisLoeschungNachWarnungFreigabe, gesuch);
		}
		return anzahl;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void warnGesuchNichtFreigegeben(Integer anzahlTageBisLoeschungNachWarnungFreigabe, Gesuch gesuch) {
		try {
			gesuch.setDatumGewarntNichtFreigegeben(LocalDate.now());
			updateGesuch(gesuch, false, null);
			mailService.sendWarnungGesuchNichtFreigegeben(gesuch, anzahlTageBisLoeschungNachWarnungFreigabe);
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail WarnungGesuchNichtFreigegeben konnte nicht verschickt werden fuer Gesuch",
				gesuch.getId());
		}
	}

	@Override
	public int findGesucheWithoutFreigabequittungenAndWarn(@Nonnull Mandant mandant) {

		Integer anzahlTageBisWarnungQuittung =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG, mandant);
		Integer anzahlTageBisLoeschungNachWarnungFreigabe =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG, mandant);
		if (anzahlTageBisWarnungQuittung == null) {
			throw new EbeguRuntimeException(
				"warnFreigabequittungFehlt",
				ApplicationPropertyKey.ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG.name() + " not defined");
		}
		if (anzahlTageBisLoeschungNachWarnungFreigabe == null) {
			throw new EbeguRuntimeException(
				"warnFreigabequittungFehlt",
				ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG.name() + " not defined");
		}

		LocalDate stichtag = LocalDate.now().minusDays(anzahlTageBisWarnungQuittung);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate predicateStatus = cb.equal(root.get(Gesuch_.status), AntragStatus.FREIGABEQUITTUNG);
		Predicate predicateDatum = cb.lessThanOrEqualTo(root.get(Gesuch_.freigabeDatum), stichtag);
		// Noch nicht gewarnt
		Predicate predicateNochNichtGewarnt = cb.isNull(root.get(Gesuch_.datumGewarntFehlendeQuittung));

		query.where(predicateStatus, predicateDatum, predicateNochNichtGewarnt);
		query.select(root);
		query.distinct(true);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Gesuch> gesucheNichtAbgeschlossenSeit = persistence.getCriteriaResults(query);

		int anzahl = gesucheNichtAbgeschlossenSeit.size();
		for (Gesuch gesuch : gesucheNichtAbgeschlossenSeit) {
			self.sendWarnungFreigabequittung(anzahlTageBisLoeschungNachWarnungFreigabe, gesuch);
		}
		return anzahl;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void sendWarnungFreigabequittung(Integer anzahlTageBisLoeschungNachWarnungFreigabe, Gesuch gesuch) {
		try {
			gesuch.setDatumGewarntFehlendeQuittung(LocalDate.now());
			gesuch = updateGesuch(gesuch, false, null);
			mailService.sendWarnungFreigabequittungFehlt(gesuch, anzahlTageBisLoeschungNachWarnungFreigabe);
		} catch (Exception e) {
			logExceptionAccordingToEnvironment(
				e,
				"Mail WarnungFreigabequittungFehlt konnte nicht verschickt werden fuer Gesuch",
				gesuch.getId());
		}
	}

	@Override
	public int deleteGesucheOhneFreigabeOderQuittung(@Nonnull Mandant mandant) {

		List<Gesuch> criteriaResults = getGesucheOhneFreigabeOderQuittung(mandant);
		int anzahl = criteriaResults.size();
		List<Betreuung> betreuungen = new ArrayList<>();
		for (Gesuch gesuch : criteriaResults) {
			try {
				betreuungen.addAll(gesuch.extractAllBetreuungen());
				GesuchDeletionCause typ;
				if (gesuch.getStatus() == AntragStatus.IN_BEARBEITUNG_GS
					|| gesuch.getStatus() == AntragStatus.IN_BEARBEITUNG_SOZIALDIENST) {
					typ = GesuchDeletionCause.BATCHJOB_NICHT_FREIGEGEBEN;
				} else {
					typ = GesuchDeletionCause.BATCHJOB_KEINE_QUITTUNG;
				}
				self.removeGesuchAndPersist(gesuch, typ);
				mailService.sendInfoGesuchGeloescht(gesuch);
			} catch (MailException e) {
				logExceptionAccordingToEnvironment(
					e,
					"Mail InfoGesuchGeloescht konnte nicht verschickt werden fuer Gesuch",
					gesuch.getId());
			}
		}
		mailService.sendInfoBetreuungGeloescht(betreuungen);
		return anzahl;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void removeGesuchAndPersist(Gesuch gesuch, GesuchDeletionCause typ) {
		removeGesuch(gesuch.getId(), typ);
	}

	@Override
	public List<Gesuch> getGesucheOhneFreigabeOderQuittung(@Nonnull Mandant mandant) {
		Integer anzahlTageBisLoeschungNachWarnungFreigabe =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE, mandant);
		Integer anzahlTageBisLoeschungNachWarnungQuittung =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG, mandant);
		if (anzahlTageBisLoeschungNachWarnungFreigabe == null || anzahlTageBisLoeschungNachWarnungQuittung == null) {
			throw new EbeguRuntimeException(
				"warnGesuchNichtFreigegeben",
				ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE.name() + " or " +
					ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG.name() + " not defined");
		}

		// Stichtag ist EndeTag -> Plus 1 Tag und dann less statt lessOrEqual
		LocalDate stichtagFehlendeFreigabe = LocalDate.now()
			.minusDays(anzahlTageBisLoeschungNachWarnungFreigabe).plusDays(1);
		LocalDate stichtagFehlendeQuittung = LocalDate.now()
			.minusDays(anzahlTageBisLoeschungNachWarnungQuittung);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		// Entweder IN_BEARBEITUNG_GS und vor stichtagFehlendeFreigabe erstellt
		Predicate predicateStatusNichtFreigegeben =
			root.get(Gesuch_.status).in(AntragStatus.IN_BEARBEITUNG_GS, AntragStatus.IN_BEARBEITUNG_SOZIALDIENST);
		Predicate predicateGewarntNichtFreigegeben = cb.isNotNull(root.get(Gesuch_.datumGewarntNichtFreigegeben));
		Predicate predicateDatumNichtFreigegeben =
			cb.lessThan(root.get(Gesuch_.datumGewarntNichtFreigegeben), stichtagFehlendeFreigabe);
		Predicate predicateNichtFreigegeben =
			cb.and(predicateStatusNichtFreigegeben, predicateDatumNichtFreigegeben, predicateGewarntNichtFreigegeben);

		// Oder FREIGABEQUITTUNG und vor stichtagFehlendeQuittung freigegeben
		Predicate predicateStatusFehlendeQuittung = cb.equal(root.get(Gesuch_.status), AntragStatus.FREIGABEQUITTUNG);
		Predicate predicateGewarntFehlendeQuittung = cb.isNotNull(root.get(Gesuch_.datumGewarntFehlendeQuittung));
		Predicate predicateDatumFehlendeQuittung =
			cb.lessThanOrEqualTo(root.get(Gesuch_.datumGewarntFehlendeQuittung), stichtagFehlendeQuittung);

		Predicate predicateFehlendeQuittung =
			cb.and(predicateStatusFehlendeQuittung, predicateDatumFehlendeQuittung, predicateGewarntFehlendeQuittung);

		Predicate predicateFehlendeFreigabeOrQuittung = cb.or(predicateNichtFreigegeben, predicateFehlendeQuittung);

		query.where(predicateFehlendeFreigabeOrQuittung);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		return persistence.getCriteriaResults(query);
	}

	@Override
	public boolean canGesuchsperiodeBeClosed(@Nonnull Gesuchsperiode gesuchsperiode) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		// Status verfuegt
		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllErledigtePendenzStatus()).not();
		// Gesuchsperiode
		final Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		query.where(predicateStatus, predicateGesuchsperiode);
		query.select(root);
		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query);
		return criteriaResults.isEmpty();
	}

	@Override
	public void removeOnlineMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		logDeletingOfGesuchstellerAntrag(dossier, gesuchsperiode);
		final Gesuch onlineMutation = findOnlineMutation(dossier, gesuchsperiode);
		moveBetreuungmitteilungenAndAbweichungenToPreviousAntrag(onlineMutation);
		List<Betreuung> betreuungen = new ArrayList<>(onlineMutation.extractAllBetreuungen());
		superAdminService.removeGesuch(onlineMutation.getId());

		mailService.sendInfoBetreuungGeloescht(betreuungen);
	}

	@Override
	public Gesuch findOnlineMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		List<Gesuch> criteriaResults = findExistingOpenMutationen(dossier, gesuchsperiode);
		// It should be impossible that there are more than one open Mutation
		return getExactlyOneGesuchFromResult(criteriaResults, "findOnlineMutation");
	}

	private Gesuch getExactlyOneGesuchFromResult(@Nonnull List<Gesuch> result, @Nonnull String callingMethodName) {
		if (result.size() > 1) {
			throw new EbeguRuntimeException(callingMethodName, ErrorCodeEnum.ERROR_TOO_MANY_RESULTS);
		}
		if (result.isEmpty()) {
			throw new EbeguEntityNotFoundException(callingMethodName, ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		}
		return result.get(0);
	}

	/**
	 * Takes all Betreuungsmitteilungen and Abweichungen of the given Gesuch and links them to the previous
	 * corresponding Betreuung
	 * (vorgaengerId),
	 * so that the mitteilungen don't get lost.
	 */
	private void moveBetreuungmitteilungenAndAbweichungenToPreviousAntrag(@Nonnull Gesuch onlineMutation) {
		if (onlineMutation.hasVorgaenger()) {
			for (Betreuung betreuung : onlineMutation.extractAllBetreuungen()) {
				if (betreuung.hasVorgaenger()) {
					@SuppressWarnings("ConstantConditions") // wird in hasVorgaenger() geprueft
					final Optional<Betreuung> optVorgaengerBetreuung =
						betreuungService.findBetreuung(betreuung.getVorgaengerId());
					final Betreuung vorgaengerBetreuung = optVorgaengerBetreuung
						.orElseThrow(() -> new EbeguEntityNotFoundException(
							"moveBetreuungmitteilungenToPreviousAntrag",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
							betreuung.getVorgaengerId()));

					// Diese Methode wird gebraucht, um eine OnlineMutation des GS zu loeschen. Fuer diese ist nie
					// jemand
					// regulaer berechtigt. Wir pruefen daher, ob wir fuer den Vorgaenger, auf die wir kopieren wollen,
					// berechtigt sind.
					authorizer.checkWriteAuthorization(vorgaengerBetreuung);

					// Es muessen alle Mitteilungen umgehaengt werden
					final Collection<Betreuungsmitteilung> mitteilungen =
						mitteilungService.findAllBetreuungsmitteilungenForBetreuung(betreuung);
					mitteilungen.forEach(mitteilung -> mitteilung.setBetreuung(vorgaengerBetreuung)); // should be
					// saved automatically

					// Es muessen alle Abweichungen umgehaengt werden
					final Collection<BetreuungspensumAbweichung> abweichungen =
						mitteilungService.findAllBetreuungspensumAbweichungenForBetreuung(betreuung);
					abweichungen.forEach(abweichung -> abweichung.setBetreuung(vorgaengerBetreuung));
				}
			}
		} else {
			throw new EbeguEntityNotFoundException(
				"moveBetreuungmitteilungenToPreviousAntrag",
				ErrorCodeEnum.ERROR_VORGAENGER_MISSING,
				onlineMutation.getId());
		}
	}

	@Override
	public void removeOnlineFolgegesuch(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		logDeletingOfGesuchstellerAntrag(dossier, gesuchsperiode);
		Gesuch gesuch = findOnlineFolgegesuch(dossier, gesuchsperiode);
		List<Betreuung> betreuungen = new ArrayList<>(gesuch.extractAllBetreuungen());
		superAdminService.removeGesuch(gesuch.getId());

		mailService.sendInfoBetreuungGeloescht(betreuungen);
	}

	@Override
	public Gesuch findOnlineFolgegesuch(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		List<Gesuch> criteriaResults = findExistingFolgegesuch(dossier, gesuchsperiode);
		if (criteriaResults.size() > 1) {
			// It should be impossible that there are more than one open Folgegesuch for one period
			throw new EbeguRuntimeException("findOnlineFolgegesuch", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS);
		}
		return criteriaResults.get(0);
	}

	@Override
	public void removeAntragForced(@Nonnull Gesuch gesuch) {
		if (gesuch.getStatus().isAnyStatusOfVerfuegt()) {
			throw new EbeguRuntimeException("removeAntrag", ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED,
				gesuch.getStatus());
		}

		if (gesuch.isMutation()) {
			moveBetreuungmitteilungenAndAbweichungenToPreviousAntrag(gesuch);
		}
		removeGesuch(gesuch.getId(), GesuchDeletionCause.SUPERADMIN);
	}

	@Override
	public void removeAntrag(@Nonnull Gesuch gesuch) {
		// Jedes Loeschen eines Antrags muss geloggt werden!
		logDeletingOfAntrag(gesuch);
		boolean isRolleGesuchsteller = principalBean.isCallerInRole(UserRole.GESUCHSTELLER);
		boolean isRolleSozialdiesnt = principalBean.isCallerInAnyOfRole(UserRole.getAllSozialdienstRoles());
		if (isRolleGesuchsteller || isRolleSozialdiesnt) {
			// Gesuchsteller:
			// Antrag muss Online sein, und darf noch nicht freigegeben sein
			if (gesuch.getEingangsart().isPapierGesuch()) {
				throw new EbeguRuntimeException(
					"removeGesuchstellerAntrag",
					ErrorCodeEnum.ERROR_DELETION_NOT_ALLOWED_FOR_GS);
			}
			if ((isRolleGesuchsteller && gesuch.getStatus() != AntragStatus.IN_BEARBEITUNG_GS)
				|| (isRolleSozialdiesnt && gesuch.getStatus() != AntragStatus.IN_BEARBEITUNG_SOZIALDIENST)) {
				throw new EbeguRuntimeException("removeGesuchstellerAntrag",
					ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED, gesuch.getStatus());
			}
			if(checkIsSZFallAndEntgezogen(gesuch)) {
				throw new EbeguRuntimeException("removeSozialdienstAntrag",
					ErrorCodeEnum.ERROR_UD_FALL_ENTZOGEN_DELETION_NOT_ALLOWED, gesuch.getStatus());
			}
		} else {
			// Alle anderen berechtigten Rollen:
			// Antrag muss Papier sein, und darf noch nicht verfuegen/verfuegt sein
			if (gesuch.getEingangsart().isOnlineGesuch()) {
				throw new EbeguRuntimeException("removeAntrag", ErrorCodeEnum.ERROR_DELETION_NOT_ALLOWED_FOR_JA);
			}
			if (gesuch.getStatus().isAnyStatusOfVerfuegtOrVefuegen()) {
				throw new EbeguRuntimeException("removeAntrag", ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED,
					gesuch.getStatus());
			}
			if(checkIsSZFallAndEntgezogen(gesuch)) {
				throw new EbeguRuntimeException("removeAntrag",
					ErrorCodeEnum.ERROR_UD_FALL_ENTZOGEN_DELETION_NOT_ALLOWED, gesuch.getStatus());
			}
		}
		// Entscheiden, was geloescht werden soll
		if (isRolleGesuchsteller) {
			// Als Gesuchsteller wird IMMER nur das jeweilige Gesuch gelöscht
			superAdminService.removeGesuch(gesuch.getId());
		} else {
			Collection<Gesuch> gesucheByDossier = findGesucheByDossier(gesuch.getDossier().getId());
			if (gesucheByDossier.size() <= 1) {
				// Das zu löschende Gesuch ist das letzte dieses Dossiers. Wir löschen auch das Dossier
				Collection<Dossier> dossiersByFall = dossierService.findDossiersByFall(gesuch.getFall().getId());
				if (dossiersByFall.size() <= 1) {
					// Das zu löschende Dossier ist das letzte dieses Falls. Wir löschen auch den Fall
					superAdminService.removeFall(gesuch.getFall());
				} else {
					superAdminService.removeDossier(gesuch.getDossier().getId());
				}
			} else {
				superAdminService.removeGesuch(gesuch.getId());
			}
		}
	}

	@Override
	public Gesuch closeWithoutAngebot(@Nonnull Gesuch gesuch) {
		if (gesuch.getStatus() != AntragStatus.GEPRUEFT) {
			throw new EbeguRuntimeException("closeWithoutAngebot", ErrorCodeEnum.ERROR_ONLY_IN_GEPRUEFT_ALLOWED);
		}
		if (!gesuch.extractAllBetreuungen().isEmpty()) {
			throw new EbeguRuntimeException("closeWithoutAngebot", ErrorCodeEnum.ERROR_ONLY_IF_NO_BETERUUNG);
		}
		gesuch.setStatus(AntragStatus.KEIN_ANGEBOT);
		postGesuchVerfuegen(gesuch);
		wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.VERFUEGEN);
		Gesuch persistedGesuch = updateGesuch(gesuch, true, null);
		// Das Dokument der Finanziellen Situation erstellen
		createFinSitDokument(persistedGesuch, "closeWithoutAngebot");
		return persistedGesuch;
	}

	@Override
	public Gesuch verfuegenStarten(@Nonnull Gesuch gesuch) {
		if (gesuch.getStatus() != AntragStatus.GEPRUEFT) {
			throw new EbeguRuntimeException("verfuegenStarten", ErrorCodeEnum.ERROR_ONLY_IN_GEPRUEFT_ALLOWED);
		}
		gesuchValidationService.validateGesuchComplete(gesuch);

		if (gesuch.hasOnlyBetreuungenOfSchulamt()) {
			throw new EbeguRuntimeException("verfuegenStarten", ErrorCodeEnum.ERROR_ONLY_SCHULAMT_NOT_ALLOWED);
		}

		gesuch.setStatus(AntragStatus.VERFUEGEN);

		Gesuch persistedGesuch = superAdminService.updateGesuch(gesuch, true, principalBean.getBenutzer());

		createFinSitDokument(persistedGesuch, "verfuegenStarten");

		return persistedGesuch;
	}


	@Override
	public void postGesuchVerfuegen(@Nonnull Gesuch gesuch) {
		Optional<Gesuch> neustesVerfuegtesGesuchFuerGesuch =
			getNeustesVerfuegtesGesuchFuerGesuch(gesuch.getGesuchsperiode(), gesuch.getDossier(), false);
		if (AntragStatus.getFirstStatusOfVerfuegt().contains(gesuch.getStatus())
			&& gesuch.getTimestampVerfuegt() == null) {
			// Status ist neuerdings verfuegt, aber das Datum noch nicht gesetzt -> dies war der Statuswechsel
			gesuch.setTimestampVerfuegt(LocalDateTime.now());
			if (neustesVerfuegtesGesuchFuerGesuch.isPresent() && !neustesVerfuegtesGesuchFuerGesuch.get()
				.getId()
				.equals(gesuch.getId())) {
				setGesuchAndVorgaengerUngueltig(neustesVerfuegtesGesuchFuerGesuch.get());
			}

			Benutzer verantwortlicherBG = gesuch.getDossier().getVerantwortlicherBG();
			Benutzer verantwortlicherTS = gesuch.getDossier().getVerantwortlicherTS();
			GemeindeStammdaten gemeindeStammdaten =
				gemeindeService.getGemeindeStammdatenByGemeindeId(gesuch.extractGemeinde().getId())
					.orElseThrow(() -> new EbeguRuntimeException(
						"postGesuchVerfuegen",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						gesuch.extractGemeinde().getId()));

			// Email an Verantwortlicher TS senden, falls dieser gesetzt und nicht identisch mit Verantwortlicher BG
			// ist
			// und falls Einstellung gesetzt ist
			if (gemeindeStammdaten.getTsVerantwortlicherNachVerfuegungBenachrichtigen()
				&& verantwortlicherTS != null
				&& verantwortlicherBG != null
				&& !verantwortlicherBG.getId().equals(verantwortlicherTS.getId())) {
				try {
					mailService.sendInfoGesuchVerfuegtVerantwortlicherTS(gesuch, verantwortlicherTS);
				} catch (MailException e) {
					LOG.error("Mail InfoGesuchVerfuegtVerantwortlicherTS konnte nicht versendet werden fuer Gesuch {}",
						gesuch.getId(), e);
				}
			}
			// neues Gesuch erst nachdem das andere auf ungültig gesetzt wurde setzen wegen unique key
			gesuch.setGueltig(true);
		}
	}

	/**
	 * Setzt das Gesuch auf ungueltig, falls es gueltig ist
	 */
	private void setGesuchAndVorgaengerUngueltig(@Nonnull Gesuch gesuch) {
		gesuch.setGueltig(false);
		updateGesuch(gesuch, false, null, false);
		// Sicherstellen, dass das Gesuch welches nicht mehr gültig ist zuerst gespeichert wird da sonst unique
		// key Probleme macht!
		persistence.getEntityManager().flush();
		// Rekursiv alle Vorgänger ungültig setzen
		if (gesuch.getVorgaengerId() != null) {
			final Optional<Gesuch> vorgaengerOpt = findGesuch(gesuch.getVorgaengerId());
			vorgaengerOpt.ifPresent(this::setGesuchAndVorgaengerUngueltig);
		}
	}

	@Override
	public Gesuch updateBetreuungenStatus(@NotNull Gesuch gesuch) {
		gesuch.setGesuchBetreuungenStatus(GesuchBetreuungenStatus.ALLE_BESTAETIGT);
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			if (Betreuungsstatus.ABGEWIESEN == betreuung.getBetreuungsstatus()) {
				gesuch.setGesuchBetreuungenStatus(GesuchBetreuungenStatus.ABGEWIESEN);
				break;
			}
			if (Betreuungsstatus.WARTEN == betreuung.getBetreuungsstatus() ||
				Betreuungsstatus.UNBEKANNTE_INSTITUTION == betreuung.getBetreuungsstatus()) {
				gesuch.setGesuchBetreuungenStatus(GesuchBetreuungenStatus.WARTEN);
			}
		}
		return persistence.merge(gesuch);
	}

	@Nonnull
	@Override
	public Optional<Gesuch> getNeuestesGesuchForDossierAndPeriod(
		@Nonnull Dossier dossier,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		authorizer.checkReadAuthorizationDossier(dossier);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier), dossier);
		Predicate gesuchsperiodePredicate = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		query.where(dossierPredicate, gesuchsperiodePredicate);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);

		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(criteriaResults.get(0));
	}

	private void logDeletingOfGesuchstellerAntrag(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		String sb = "Online Gesuch wird gelöscht:"
			+ "Benutzer: " + principalBean.getBenutzer().getUsername()
			+ ", Fall: " + dossier.getFall().getFallNummer()
			+ ", Gemeinde: " + dossier.getGemeinde().getGemeindeNummer()
			+ ", Gesuchsperiode: " + gesuchsperiode.getGesuchsperiodeString();
		LOG.info(sb);
	}

	private void logDeletingOfAntrag(@Nonnull Gesuch gesuch) {
		String sb = "Gesuch wird gelöscht:"
			+ "Benutzer: " + principalBean.getBenutzer().getUsername()
			+ ", Fall: " + gesuch.getFall().getFallNummer()
			+ ", Gesuchsperiode: " + gesuch.getGesuchsperiode().getGesuchsperiodeString()
			+ ", Gesuch-Id: " + gesuch.getId();
		LOG.info(sb);
	}

	@Override
	public int changeFinSitStatus(@Nonnull String antragId, @Nonnull FinSitStatus finSitStatus) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaUpdate<Gesuch> update = cb.createCriteriaUpdate(Gesuch.class);
		Root<Gesuch> root = update.from(Gesuch.class);
		update.set(Gesuch_.finSitStatus, finSitStatus);

		Predicate predGesuch = cb.equal(root.get(AbstractEntity_.id), antragId);
		update.where(predGesuch);

		return persistence.getEntityManager().createQuery(update).executeUpdate();
	}

	@Override
	public Gesuch sendGesuchToSTV(@Nonnull Gesuch gesuch, @Nullable String bemerkungen) {
		if (AntragStatus.VERFUEGT != gesuch.getStatus() && AntragStatus.NUR_SCHULAMT != gesuch.getStatus()) {
			// Wir vergewissern uns dass das Gesuch im Status VERFUEGT ist, da sonst kann es nicht zum STV geschickt
			// werden
			throw new EbeguRuntimeException(
				"sendGesuchToSTV",
				ErrorCodeEnum.ERROR_ONLY_VERFUEGT_OR_SCHULAMT_ALLOWED,
				"Status ist: " + gesuch.getStatus());
		}
		gesuch.setStatus(AntragStatus.PRUEFUNG_STV);
		gesuch.setEingangsdatumSTV(LocalDate.now());
		if (StringUtils.isNotEmpty(bemerkungen)) {
			gesuch.setBemerkungenSTV(bemerkungen);
		}
		return updateGesuch(gesuch, true, null);
	}

	@Override
	public Gesuch gesuchBySTVFreigeben(@Nonnull Gesuch gesuch) {
		if (AntragStatus.IN_BEARBEITUNG_STV != gesuch.getStatus()) {
			// Wir vergewissern uns dass das Gesuch im Status IN_BEARBEITUNG_STV ist, da sonst kann es nicht fuer das
			// JA freigegeben werden
			throw new EbeguRuntimeException(
				"gesuchBySTVFreigeben",
				ErrorCodeEnum.ERROR_ONLY_IN_BEARBEITUNG_STV_ALLOWED,
				"Status ist: " + gesuch.getStatus());
		}

		gesuch.setStatus(AntragStatus.GEPRUEFT_STV);
		gesuch.setGeprueftSTV(true);

		return updateGesuch(gesuch, true, null);
	}

	@Override
	public Gesuch stvPruefungAbschliessen(@Nonnull Gesuch gesuch) {
		final AntragStatusHistory lastStatusChange =
			antragStatusHistoryService.findLastStatusChangeBeforePruefungSTV(gesuch);
		gesuch.setStatus(lastStatusChange.getStatus());
		return updateGesuch(gesuch, true, null);
	}

	@Override
	@Nonnull
	public List<Gesuch> getGepruefteFreigegebeneGesucheForGesuchsperiode(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {

		Map<String, Gesuch> neustesGeprueftesFreigegebensGesuchCache = new HashMap<>();
		List<Gesuch> gesuches = getNeustesGeprueftesFreigegebensGesuchFuerPeriode(gesuchsperiode.getId());
		gesuches.forEach(
			gueltigeGesuch -> {
				Gesuch gesuch = neustesGeprueftesFreigegebensGesuchCache.get(gueltigeGesuch.getDossier().getId());
				if (gesuch != null && gueltigeGesuch.getTimestampErstellt() != null
					&& gesuch.getTimestampErstellt() != null
					&& gesuch.getTimestampErstellt().isAfter(gueltigeGesuch.getTimestampErstellt())) {
					return;
				}
				neustesGeprueftesFreigegebensGesuchCache.put(
					gueltigeGesuch.getDossier().getId(),
					gueltigeGesuch);
			}
		);

		// We first look for all Gesuche that belongs to the gesuchsperiode and were geprueft/nurschulamt/freigegeben
		// between
		// the given dates.
		final List<Tuple> allTuples =
			getGepruefteFreigegebeneGesucheForGesuchsperiodeTuples(datumVon, datumBis, gesuchsperiode);

		List<Gesuch> gesuche = new ArrayList<>();
		allTuples.forEach(tuple -> {
			final String dossierIdValue = String.valueOf(tuple.get(0));
			Gesuch gesuch = neustesGeprueftesFreigegebensGesuchCache.get(dossierIdValue);
			if (gesuch != null) {
				gesuche.add(gesuch);
			}
		});

		return gesuche;
	}

	private List<Tuple> getGepruefteFreigegebeneGesucheForGesuchsperiodeTuples(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {

		Objects.requireNonNull(datumVon);
		Objects.requireNonNull(datumBis);
		Objects.requireNonNull(gesuchsperiode);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();
		Root<Gesuch> root = query.from(Gesuch.class);

		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getGepruefteFreigegebeneGesucheForGesuchsperiodeTuples", "No User is logged in"));

		Join<Gesuch, AntragStatusHistory> antragStatusHistoryJoin = root.join(
			Gesuch_.antragStatusHistories,
			JoinType.LEFT);
		Join<Gesuch, Dossier> joinDossier = root.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(Dossier_.gemeinde, JoinType.LEFT);

		// Prepare TypedParameters
		ParameterExpression<Gesuchsperiode> gesuchsperiodeIdParam = cb.parameter(
			Gesuchsperiode.class,
			"gesuchsperiode");
		ParameterExpression<LocalDateTime> datumVonParam = cb.parameter(LocalDateTime.class, "datumVon");
		ParameterExpression<LocalDateTime> datumBisParam = cb.parameter(LocalDateTime.class, "datumBis");
		ParameterExpression<AntragStatus> geprueftParam = cb.parameter(AntragStatus.class, "geprueft");
		ParameterExpression<AntragStatus> freigegebenParam = cb.parameter(AntragStatus.class, "freigegeben");
		ParameterExpression<AntragStatus> nurSchulamtParam = cb.parameter(AntragStatus.class, "nurschulamt");
		ParameterExpression<Eingangsart> papierParam = cb.parameter(Eingangsart.class, "papier");
		ParameterExpression<Eingangsart> onlineParam = cb.parameter(Eingangsart.class, "online");

		// Predicates
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(getStatusTransitionPredicate(cb, root, antragStatusHistoryJoin, datumVonParam, datumBisParam,
			geprueftParam, freigegebenParam, nurSchulamtParam, papierParam, onlineParam));
		predicates.add(cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiodeIdParam));
		// An Erstgesuch is not MUTATION (i.e. ERSTGESUCH or ERNEUERUNGSGESUCH)
		predicates.add(cb.equal(root.get(Gesuch_.typ), AntragTyp.MUTATION).not());
		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, joinGemeinde, predicates);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));

		query.groupBy(
			root.get(Gesuch_.dossier).get(AbstractEntity_.id),
			root.get(Gesuch_.gesuchsperiode).get(AbstractEntity_.id)
		);
		query.multiselect(
			root.get(Gesuch_.dossier).get(AbstractEntity_.id),
			root.get(Gesuch_.gesuchsperiode).get(AbstractEntity_.id),
			cb.max(root.get(Gesuch_.laufnummer))
		);

		TypedQuery<Tuple> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(geprueftParam, AntragStatus.GEPRUEFT);
		typedQuery.setParameter(freigegebenParam, AntragStatus.FREIGEGEBEN);
		typedQuery.setParameter(nurSchulamtParam, AntragStatus.NUR_SCHULAMT);
		typedQuery.setParameter(papierParam, Eingangsart.PAPIER);
		typedQuery.setParameter(onlineParam, Eingangsart.ONLINE);
		typedQuery.setParameter(gesuchsperiodeIdParam, gesuchsperiode);
		typedQuery.setParameter(datumVonParam, datumVon.atStartOfDay());
		typedQuery.setParameter(datumBisParam, datumBis.atStartOfDay().plusDays(1));

		return typedQuery.getResultList();
	}

	/**
	 * Returns the newest Gesuch for the given dossier and period that has been at least freigegeben (for
	 * Onlinegesuche)
	 * or at least Geprueft (for Papiergesuche)
	 */
	@Nonnull
	public List<Gesuch> getNeustesGeprueftesFreigegebensGesuchFuerPeriode(
		@Nonnull String gesuchsperiodeId) {
		Objects.requireNonNull(gesuchsperiodeId);

		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getGepruefteFreigegebeneGesucheForGesuchsperiodeTuples", "No User is logged in"));

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<String> gesuchsperiodeParam = cb.parameter(String.class, "gesuchsperiodeId");
		//noinspection rawtypes
		ParameterExpression<Collection> statusParam = cb.parameter(Collection.class, "status");

		List<Predicate> predicates = new ArrayList<>();

		Predicate predicateStatus = root.get(Gesuch_.status).in(statusParam);
		predicates.add(predicateStatus);
		Predicate predicateGesuchsperiode = cb.equal(
			root.get(Gesuch_.gesuchsperiode).get(AbstractEntity_.id),
			gesuchsperiodeParam);
		predicates.add(predicateGesuchsperiode);

		if (user.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Join<Gesuch, Dossier> joinDossier = root.join(Gesuch_.dossier, JoinType.LEFT);
			Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(Dossier_.gemeinde, JoinType.LEFT);
			Predicate inGemeinde = joinGemeinde.in(user.extractGemeindenForUser());
			predicates.add(inGemeinde);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		query.select(root);

		TypedQuery<Gesuch> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(gesuchsperiodeParam, gesuchsperiodeId);
		typedQuery.setParameter(statusParam, AntragStatus.getAllFreigegebeneStatus());

		return typedQuery.getResultList();
	}

	/**
	 * Will create a Predicate to look for all Onlinegesuche that marked as FREIGEGEBEN between Von and Bis
	 * and all Papiergesuche that were marked as GEPRUEFT between Von and Bis
	 */
	private Predicate getStatusTransitionPredicate(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Root<Gesuch> root,
		@Nonnull Join<Gesuch, AntragStatusHistory> antragStatusHistoryJoin,
		@Nonnull ParameterExpression<LocalDateTime> datumVonParam,
		@Nonnull ParameterExpression<LocalDateTime> datumBisParam,
		@Nonnull ParameterExpression<AntragStatus> geprueftParam,
		@Nonnull ParameterExpression<AntragStatus> freigegebenParam,
		@Nonnull ParameterExpression<AntragStatus> nurSchulamtParam,
		@Nonnull ParameterExpression<Eingangsart> papierParam,
		@Nonnull ParameterExpression<Eingangsart> onlineParam
	) {
		final Predicate predicateStatusSetBetweenVonAndBis = cb.between(
			antragStatusHistoryJoin.get(AntragStatusHistory_.timestampVon),
			datumVonParam,
			datumBisParam
		);
		final Predicate predicateGeprueft = cb.and(
			cb.or(
				cb.equal(antragStatusHistoryJoin.get(AntragStatusHistory_.status), geprueftParam),
				cb.equal(antragStatusHistoryJoin.get(AntragStatusHistory_.status), nurSchulamtParam)
			),
			predicateStatusSetBetweenVonAndBis
		);
		final Predicate predicateFreigegeben = cb.and(
			cb.equal(antragStatusHistoryJoin.get(AntragStatusHistory_.status), freigegebenParam),
			predicateStatusSetBetweenVonAndBis
		);

		final Predicate predicatePapier = cb.equal(root.get(Gesuch_.eingangsart), papierParam);
		final Predicate predicateOnline = cb.equal(root.get(Gesuch_.eingangsart), onlineParam);

		return cb.or(
			cb.and(predicateGeprueft, predicatePapier),
			cb.and(predicateFreigegeben, predicateOnline)
		);
	}

	@Override
	public List<Gesuch> getAllGesuchForAmtAfterGP(@Nonnull Gesuchsperiode gesuchsperiode) {
		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getAllGesuchForAmtAfterGP", "No User is logged in"));

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<LocalDate> gesuchsperiodeGueltigAbParam = cb.parameter(LocalDate.class, "gueltigAb");
		//noinspection rawtypes
		ParameterExpression<Collection> freigegebenParam = cb.parameter(Collection.class, "freigegeben");

		List<Predicate> predicates = new ArrayList<>();
		Predicate freigegebenPredicate = root.get(Gesuch_.status).in(freigegebenParam);
		predicates.add(freigegebenPredicate);
		Predicate gesuchsperiodePredicate = cb.greaterThan(
			root.get(Gesuch_.gesuchsperiode).get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			gesuchsperiodeGueltigAbParam);
		predicates.add(gesuchsperiodePredicate);

		if (user.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Join<Gesuch, Dossier> joinDossier = root.join(Gesuch_.dossier, JoinType.LEFT);
			Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(Dossier_.gemeinde, JoinType.LEFT);
			Predicate inGemeinde = joinGemeinde.in(user.extractGemeindenForUser());
			predicates.add(inGemeinde);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		query.select(root);

		TypedQuery<Gesuch> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(
			gesuchsperiodeGueltigAbParam,
			gesuchsperiode.getGueltigkeit().getGueltigAb());
		typedQuery.setParameter(freigegebenParam, AntragStatus.getAllFreigegebeneStatus());

		return typedQuery.getResultList();
	}

	private void createFinSitDokument(Gesuch persistedGesuch, String methodname) {
		if (EbeguUtil.isFinanzielleSituationRequired(persistedGesuch)) {
			try {
				// Das Erstellen des FinSitDokumentes wirft eine Exception, wenn die FinSit nicht benötigt wird
				generatedDokumentService.getFinSitDokumentAccessTokenGeneratedDokument(persistedGesuch, true);
			} catch (MimeTypeParseException | MergeDocException e) {
				throw new EbeguRuntimeException(methodname, "FinSit-Dokument konnte nicht erstellt werden"
					+ persistedGesuch.getId(), e);
			}
		}
	}

	@Nonnull
	public Collection<Gesuch> findGesucheByDossier(@Nonnull String dossierId) {
		final Dossier dossier =
			dossierService.findDossier(dossierId).orElseThrow(() -> new EbeguEntityNotFoundException(
				"findGesucheByDossier",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dossierId));
		return criteriaQueryHelper.getEntitiesByAttribute(Gesuch.class, dossier, Gesuch_.dossier);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createMutationAndAskForPlatzbestaetigung(@Nonnull Gesuch gesuch) {
		// Falls im "alten" Gesuch noch Tagesschule-Anmeldungen im status AUSGELOEST sind, müssen
		// diese nun gespeichert (im gleichen Status, Verfügung erstellen) werden, damit künftig für
		// die Berechnung die richtige FinSit verwendet wird!
		zuMutierendeAnmeldungenAbschliessen(gesuch);

		// Die Mutation erstellen
		Gesuch mutation = gesuch.copyForMutation(
			new Gesuch(),
			Eingangsart.PAPIER,
			gesuch.getRegelStartDatum() != null ? gesuch.getRegelStartDatum() : LocalDate.now());
		mutation.setTyp(AntragTyp.MUTATION);
		mutation.setEingangsdatum(LocalDate.now());
		mutation.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		mutation.setEingangsart(Eingangsart.PAPIER);
		mutation.setGesuchsperiode(gesuch.getGesuchsperiode());
		mutation.setDossier(gesuch.getDossier());
		Gesuch persistedGesuch = persistence.persist(mutation);
		// Die WizardSteps werden direkt erstellt wenn das Gesuch erstellt wird. So vergewissern wir uns dass es kein
		// Gesuch ohne WizardSteps gibt
		wizardStepService.createWizardStepList(persistedGesuch);
		antragStatusHistoryService.saveStatusChange(persistedGesuch, gesuch.getDossier().getVerantwortlicherBG());

		// Die Betreuungen werden defaultmaessig mit BESTAETIGT uebernommen.
		// Damit eine Ueberpruefung der Angaben erwzungen werden kann, wird
		// hier eine neue Platzbestaetigung ausgeloest.
		persistedGesuch.extractAllBetreuungen()
			.forEach(betreuung -> betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN));
	}

	@Override
	public List<Gesuch> findGesucheForZemisList(@Nonnull Integer lastenausgleichJahr) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Join<Gesuch, KindContainer> joinKindContainer = root.join(Gesuch_.kindContainers, JoinType.INNER);
		Join<KindContainer, Kind> joinKind = joinKindContainer.join(KindContainer_.kindJA, JoinType.INNER);
		Join<Gesuch, Gesuchsperiode> joinGesuchsperiode = root.join(Gesuch_.gesuchsperiode, JoinType.INNER);

		Predicate predicateZemis = cb.isNotNull(joinKind.get(Kind_.ZEMIS_NUMMER));
		Predicate predicateHasBetreuung = cb.isNotEmpty(joinKindContainer.get(KindContainer_.BETREUUNGEN));
		Predicate predicateGueltig = cb.isTrue(root.get(Gesuch_.gueltig));

		// für den Lastenausgleich 2020 müssen Kinder der Periode 19/20 und 20/21 zurückgegeben werden
		Predicate predicateYear0 = cb.equal(
			cb.function(
				"YEAR",
				Integer.class,
				joinGesuchsperiode.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)
			),
			lastenausgleichJahr - 1
		);
		Predicate predicateYear1 = cb.equal(
			cb.function(
				"YEAR",
				Integer.class,
				joinGesuchsperiode.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)
			),
			lastenausgleichJahr
		);
		Predicate predicateYears = cb.or(predicateYear0, predicateYear1);

		query.where(predicateZemis, predicateGueltig, predicateYears, predicateHasBetreuung);
		query.distinct(true);
		return persistence.getCriteriaResults(query);
	}

	@Override
	public Gesuch findGesuchOfGS(GesuchstellerContainer container) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		query.where(
				cb.or(
						cb.equal(
								root.get(Gesuch_.gesuchsteller1),
								container
						),
						cb.equal(
								root.get(Gesuch_.gesuchsteller2),
								container
						)
				)
		);

		final List<Gesuch> results = persistence.getCriteriaResults(query);
		Gesuch gesuch = getExactlyOneGesuchFromResult(results, "findGesuchOfGS");
		authorizer.checkReadAuthorization(gesuch);

		return gesuch;
	}

	@Override
	public Gesuch updateMarkiertFuerKontroll(@NotNull Gesuch gesuch, Boolean markiertFuerKontroll) {
		var gesuche = this.getAllGesucheForDossierAndPeriod(gesuch.getDossier(), gesuch.getGesuchsperiode());
		for (var g : gesuche) {
			if (g.getStatus().isAnyOfInBearbeitungGSOrSZD()) {
				continue;
			}
			g.setMarkiertFuerKontroll(markiertFuerKontroll);
		}
		return persistence.merge(gesuch);
	}

	@Override
	public Gesuch mutationIgnorieren(Gesuch gesuch) {

		gesuchValidationService.validateGesuchComplete(gesuch);
		KindContainer[] kindArray =
			gesuch.getKindContainers().toArray(new KindContainer[gesuch.getKindContainers().size()]);
		for (int i = 0; i < gesuch.getKindContainers().size(); i++) {
			KindContainer kindContainerToWorkWith = kindArray[i];
			List<Betreuung> betreuungList = new ArrayList<>(kindContainerToWorkWith.getBetreuungen());
			for (int j = 0; j < betreuungList.size(); j++) {
				Betreuung betreuung = betreuungList.get(j);
				this.betreuungService.schliessenOnly(betreuung);
			}
			for (AnmeldungTagesschule anmeldung : kindContainerToWorkWith.getAnmeldungenTagesschule()) {
				this.betreuungService.anmeldungMutationIgnorieren(anmeldung);
			}
			for (AnmeldungFerieninsel anmeldung : kindContainerToWorkWith.getAnmeldungenFerieninsel()) {
				this.betreuungService.anmeldungMutationIgnorieren(anmeldung);
			}
		}

		gesuch.setStatus(AntragStatus.IGNORIERT);
		gesuch.setTimestampVerfuegt(LocalDateTime.now());
		wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.VERFUEGEN);
		Gesuch persistedGesuch = superAdminService.updateGesuch(gesuch, true, principalBean.getBenutzer());

		return persistedGesuch;
	}

	@Override
	public Gesuch findErstgesuchForGesuch(@Nonnull Gesuch gesuch) {
		if (gesuch.getTyp().isGesuch()) {
			return gesuch;
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get(Gesuch_.dossier), gesuch.getDossier()));
		predicates.add(cb.equal(root.get(Gesuch_.gesuchsperiode), gesuch.getGesuchsperiode()));

		Predicate predicateErstOrErnerungsGesuch = cb.or(
			cb.equal(root.get(Gesuch_.typ), AntragTyp.ERSTGESUCH),
			cb.equal(root.get(Gesuch_.typ), AntragTyp.ERNEUERUNGSGESUCH)
		);
		predicates.add(predicateErstOrErnerungsGesuch);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		List<Gesuch> queryResult = persistence.getCriteriaResults(query);

		return getExactlyOneGesuchFromResult(queryResult, "findErstgesuchForGesuch");
	}

	@Nonnull
	@Override
	public Optional<Gesuch> getNeustesVerfuegtesGesuchFuerGesuch(@Nonnull Gesuch gesuch) {
		return getNeustesVerfuegtesGesuchFuerGesuch(gesuch.getGesuchsperiode(), gesuch.getDossier(), true);
	}

	private boolean checkIsSZFallAndEntgezogen(Gesuch gesuch) {
		return gesuch.getFall().getSozialdienstFall() != null
			&& gesuch.getFall().getSozialdienstFall().getStatus() == SozialdienstFallStatus.ENTZOGEN;
	}

	@Nonnull
	public Gesuch findVorgaengerGesuchNotIgnoriert(@Nonnull String gesuchId) {
		var gesuch = findGesuch(gesuchId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"findVorgaengerGesuchNotIgnoriert",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			gesuchId));
		if (gesuch.getStatus() != AntragStatus.IGNORIERT) {
			authorizer.checkReadAuthorization(gesuch);
			return gesuch;
		}
		if (gesuch.getVorgaengerId() == null) {
			throw new EbeguRuntimeException(
				"findVorgaengerGesuchNotIgnoriert",
				"Kein Vorgänger gefunden, der nicht ignoriert war"
			);
		}
		return findVorgaengerGesuchNotIgnoriert(gesuch.getVorgaengerId());
	}

	@Override
	public Optional<Gesuch> findGesuchForFinSit(@Nonnull String finSitId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		Join<Gesuch, GesuchstellerContainer> gesuchsteller1 = root.join(Gesuch_.gesuchsteller1, JoinType.LEFT);
		Join<GesuchstellerContainer, FinanzielleSituationContainer> finSit1Cont =
			gesuchsteller1.join(GesuchstellerContainer_.finanzielleSituationContainer, JoinType.LEFT);
		Join<FinanzielleSituationContainer, FinanzielleSituation> finSit1Ja =
			finSit1Cont.join(FinanzielleSituationContainer_.finanzielleSituationJA, JoinType.LEFT);

		Predicate predicateGS1 = cb.equal(finSit1Ja.get(AbstractEntity_.id),
			finSitId
		);

		Join<Gesuch, GesuchstellerContainer> gesuchsteller2 = root.join(Gesuch_.gesuchsteller2, JoinType.LEFT);
		Join<GesuchstellerContainer, FinanzielleSituationContainer> finSit2Cont =
			gesuchsteller2.join(GesuchstellerContainer_.finanzielleSituationContainer, JoinType.LEFT);
		Join<FinanzielleSituationContainer, FinanzielleSituation> finSit2Ja =
			finSit2Cont.join(FinanzielleSituationContainer_.finanzielleSituationJA, JoinType.LEFT);

		Predicate predicateGS2 = cb.equal(finSit2Ja.get(AbstractEntity_.id),
			finSitId
		);

		query.where(cb.or(predicateGS1, predicateGS2));
		return Optional.ofNullable(persistence.getCriteriaSingleResult(query));
	}

}


