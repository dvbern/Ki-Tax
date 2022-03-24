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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.entities.WizardStep_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.SozialdienstFallStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.rules.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DokumenteUtil;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer Gesuch
 */
@Stateless
@Local(WizardStepService.class)
public class WizardStepServiceBean extends AbstractBaseService implements WizardStepService {

	private static final Logger LOG = LoggerFactory.getLogger(WizardStepServiceBean.class);

	@Inject
	private Persistence persistence;
	@Inject
	private KindService kindService;
	@Inject
	private ErwerbspensumService erwerbspensumService;
	@Inject
	private DokumentGrundService dokumentGrundService;
	@Inject
	private DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;
	@Inject
	private AntragStatusHistoryService antragStatusHistoryService;
	@Inject
	private Authorizer authorizer;
	@Inject
	private PrincipalBean principalBean;
	@Inject
	private GeneratedDokumentService generatedDokumentService;
	@Inject
	private MailService mailService;
	@Inject
	private GesuchService gesuchService;
	@Inject
	private GemeindeService gemeindeService;

	@Override
	@Nonnull
	public WizardStep saveWizardStep(@Nonnull WizardStep wizardStep) {
		Objects.requireNonNull(wizardStep);
		return persistence.merge(wizardStep);
	}

	@Override
	@Nonnull
	public Optional<WizardStep> findWizardStep(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		WizardStep a = persistence.find(WizardStep.class, key);
		authorizer.checkReadAuthorization(a);
		return Optional.ofNullable(a);
	}

	@Override
	public List<WizardStep> findWizardStepsFromGesuch(String gesuchId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<WizardStep> query = cb.createQuery(WizardStep.class);
		Root<WizardStep> root = query.from(WizardStep.class);
		Predicate predWizardStepFromGesuch = cb.equal(root.get(WizardStep_.gesuch).get(Gesuch_.id), gesuchId);

		query.where(predWizardStepFromGesuch);
		final List<WizardStep> criteriaResults = persistence.getCriteriaResults(query);
		criteriaResults.forEach(result -> authorizer.checkReadAuthorization(result));
		return criteriaResults;
	}

	@Override
	public WizardStep findWizardStepFromGesuch(String gesuchId, WizardStepName stepName) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<WizardStep> query = cb.createQuery(WizardStep.class);
		Root<WizardStep> root = query.from(WizardStep.class);
		Predicate predWizardStepFromGesuch = cb.equal(root.get(WizardStep_.gesuch).get(Gesuch_.id), gesuchId);
		Predicate predWizardStepName = cb.equal(root.get(WizardStep_.wizardStepName), stepName);

		query.where(predWizardStepFromGesuch, predWizardStepName);
		final WizardStep result = persistence.getCriteriaSingleResult(query);
		authorizer.checkReadAuthorization(result);
		return result;
	}

	@Override
	public List<WizardStep> updateSteps(
		String gesuchId,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		WizardStepName stepName) {
		return updateSteps(gesuchId, oldEntity, newEntity, stepName, null);
	}

	@Override
	public List<WizardStep> updateSteps(
		String gesuchId, @Nullable AbstractEntity oldEntity, @Nullable AbstractEntity newEntity,
		WizardStepName stepName, @Nullable Integer substep) {
		final List<WizardStep> wizardSteps = findWizardStepsFromGesuch(gesuchId);
		updateAllStatus(wizardSteps, oldEntity, newEntity, stepName, substep);
		wizardSteps.forEach(this::saveWizardStep);
		return wizardSteps;
	}

	@Nonnull
	@Override
	public List<WizardStep> createWizardStepList(Gesuch gesuch) {
		List<WizardStep> wizardStepList = new ArrayList<>();
		if (AntragTyp.MUTATION == gesuch.getTyp()) {
			if (gesuch.getDossier().getFall().getSozialdienstFall() != null) {
				wizardStepList.add(saveWizardStep(createWizardStepObject(
					gesuch,
					WizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
					WizardStepStatus.OK,
					true)));
			}
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.GESUCH_ERSTELLEN,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.FAMILIENSITUATION,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.GESUCHSTELLER,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.UMZUG,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.KINDER,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.BETREUUNG,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.ABWESENHEIT,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.ERWERBSPENSUM,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				this.getFinSitWizardStepNameForGesuch(gesuch),
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				this.getEKVWizardStepNameForGesuch(gesuch),
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.DOKUMENTE,
				WizardStepStatus.OK,
				true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.FREIGABE,
				WizardStepStatus.OK,
				true)));
			// Verfuegen muss WARTEN sein, da die Betreuungen nochmal verfuegt werden muessen
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.VERFUEGEN,
				WizardStepStatus.WARTEN,
				true)));
		} else { // GESUCH
			if (gesuch.getDossier().getFall().getSozialdienstFall() != null) {
				wizardStepList.add(saveWizardStep(createWizardStepObject(
					gesuch,
					WizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
					gesuch.getDossier().getFall().getSozialdienstFall().getStatus() == SozialdienstFallStatus.AKTIV ?
						WizardStepStatus.OK : WizardStepStatus.IN_BEARBEITUNG,
					true)));
			}
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.GESUCH_ERSTELLEN,
				gesuch.getDossier().getFall().getSozialdienstFall() != null ?
					WizardStepStatus.UNBESUCHT :
					WizardStepStatus.OK,
				gesuch.getDossier().getFall().getSozialdienstFall() != null ? false : true)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.FAMILIENSITUATION,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.GESUCHSTELLER,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.UMZUG,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.KINDER,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.BETREUUNG,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.ABWESENHEIT,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.ERWERBSPENSUM,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				this.getFinSitWizardStepNameForGesuch(gesuch),
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				this.getEKVWizardStepNameForGesuch(gesuch),
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.DOKUMENTE,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.FREIGABE,
				WizardStepStatus.UNBESUCHT,
				false)));
			wizardStepList.add(saveWizardStep(createWizardStepObject(
				gesuch,
				WizardStepName.VERFUEGEN,
				WizardStepStatus.UNBESUCHT,
				false)));
		}
		return wizardStepList;
	}

	/**
	 * Hier wird es geschaut, was fuer ein Objekttyp aktualisiert wurde. Dann wird die entsprechende Logik
	 * durchgefuehrt, um zu wissen welche anderen
	 * Steps von diesen Aenderungen beeinflusst wurden. Mit dieser Information werden alle betroffenen Status
	 * dementsprechend geaendert.
	 * Dazu werden die Angaben in oldEntity mit denen in newEntity verglichen und dann wird entsprechend reagiert
	 */
	private void updateAllStatus(
		List<WizardStep> wizardSteps, @Nullable AbstractEntity oldEntity, @Nullable AbstractEntity newEntity,
		WizardStepName stepName, @Nullable Integer substep) {
		if (WizardStepName.FAMILIENSITUATION == stepName
			&& oldEntity instanceof Familiensituation
			&& newEntity instanceof Familiensituation) {
			updateAllStatusForFamiliensituation(
				wizardSteps,
				(Familiensituation) oldEntity,
				(Familiensituation) newEntity);
		} else if (WizardStepName.GESUCHSTELLER == stepName) {
			updateAllStatusForGesuchsteller(wizardSteps);
		} else if (WizardStepName.UMZUG == stepName) {
			updateAllStatusForUmzug(wizardSteps);
		} else if (WizardStepName.BETREUUNG == stepName) {
			updateAllStatusForBetreuung(wizardSteps);
		} else if (WizardStepName.ABWESENHEIT == stepName) {
			updateAllStatusForAbwesenheit(wizardSteps);
		} else if (WizardStepName.KINDER == stepName) {
			updateAllStatusForKinder(wizardSteps);
		} else if (WizardStepName.ERWERBSPENSUM == stepName) {
			updateAllStatusForErwerbspensum(wizardSteps);
		} else if (stepName.isEKVWizardStepName()
			&& newEntity instanceof EinkommensverschlechterungInfoContainer) {
			updateAllStatusForEinkommensverschlechterungInfo(
				wizardSteps,
				(EinkommensverschlechterungInfoContainer) oldEntity,
				(EinkommensverschlechterungInfoContainer) newEntity);
		} else if (stepName.isEKVWizardStepName()
			&& newEntity instanceof EinkommensverschlechterungContainer) {
			updateAllStatusForEinkommensverschlechterung(wizardSteps);
		} else if (WizardStepName.DOKUMENTE == stepName) {
			updateAllStatusForDokumente(wizardSteps);
		} else if (WizardStepName.VERFUEGEN == stepName) {
			updateAllStatusForVerfuegen(wizardSteps);
		} else if (stepName.isFinSitWizardStepName()) {
			updateAllStatusForFinSit(wizardSteps, substep);
		} else {
			updateStatusSingleStep(wizardSteps, stepName);
		}
	}

	/**
	 * Wenn die Seite schon besucht ist dann soll der Status auf ok/mutiert oder notOK (bei wechsel ekv von nein auf
	 * ja) gesetzt werden
	 */
	private void updateAllStatusForEinkommensverschlechterungInfo(
		List<WizardStep> wizardSteps,
		EinkommensverschlechterungInfoContainer oldEntity,
		EinkommensverschlechterungInfoContainer newEntity
	) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()
				&& wizardStep.getWizardStepName().isEKVWizardStepName()) {

				if (!newEntity.getEinkommensverschlechterungInfoJA().getEinkommensverschlechterung()) {
					setWizardStepOkOrMutiert(wizardStep);

				} else if (oldEntity == null || !oldEntity.getEinkommensverschlechterungInfoJA()
					.getEinkommensverschlechterung()
					|| (!oldEntity.getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus1()
					&& newEntity.getEinkommensverschlechterungInfoJA()
					.getEkvFuerBasisJahrPlus1())
					|| (!oldEntity.getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus2()
					&& newEntity.getEinkommensverschlechterungInfoJA()
					.getEkvFuerBasisJahrPlus2())) {
					// beim Wechseln von KEIN_EV auf EV oder von KEIN_EV_FUER_BASISJAHR2 auf EV_FUER_BASISJAHR2
					wizardStep.setWizardStepStatus(WizardStepStatus.NOK);

				} else if (wizardStep.getGesuch().isMutation()
					&& WizardStepStatus.NOK != wizardStep.getWizardStepStatus()) {
					setWizardStepOkOrMutiert(wizardStep);
				}
			}
		}
	}

	private void updateAllStatusForEinkommensverschlechterung(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()
				&& WizardStepStatus.NOK != wizardStep.getWizardStepStatus()
				&& wizardStep.getWizardStepName().isEKVWizardStepName()
				&& wizardStep.getGesuch().isMutation()) {

				setWizardStepOkOrMutiert(wizardStep);
			}
		}
	}

	private void updateAllStatusForDokumente(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()
				&& WizardStepName.DOKUMENTE == wizardStep.getWizardStepName()) {

				// The language to use is not important since the results are only used to calculate the state and
				// won't be displayed
				final Set<DokumentGrund> dokumentGrundsMerged = DokumenteUtil
					.mergeNeededAndPersisted(
						dokumentenverzeichnisEvaluator.calculate(wizardStep.getGesuch(), Constants.DEFAULT_LOCALE),
						dokumentGrundService.findAllDokumentGrundByGesuch(wizardStep.getGesuch()));

				boolean allNeededDokumenteUploaded = true;
				for (DokumentGrund dokumentGrund : dokumentGrundsMerged) {
					if (!DokumentGrundTyp.isSonstigeOrPapiergesuch(dokumentGrund.getDokumentGrundTyp())
						&& dokumentGrund.isNeeded() && dokumentGrund.isEmpty()) {
						allNeededDokumenteUploaded = false;
						break;
					}
				}

				if (allNeededDokumenteUploaded) {
					setWizardStepOkOrMutiert(wizardStep);
				} else {
					if (wizardStep.getGesuch().isMutation()) {
						wizardStep.setWizardStepStatus(WizardStepStatus.MUTIERT);
					} else {
						wizardStep.setWizardStepStatus(WizardStepStatus.IN_BEARBEITUNG);
					}
				}
			}
		}
	}

	/**
	 * Holt alle Erwerbspensen und Betreuungen von der Datenbank. Nur die Betreuungen vom Typ anders als TAGESSCHULE
	 * und TAGESFAMILIEN werden
	 * beruecksichtigt
	 * Wenn die Anzahl solcher Betreuungen grosser als 0 ist, dann wird es geprueft, ob es Erwerbspensen gibt, wenn
	 * nicht der Status aendert auf NOK.
	 * In allen anderen Faellen wird der Status auf OK gesetzt
	 */
	private void updateAllStatusForErwerbspensum(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.ERWERBSPENSUM == wizardStep.getWizardStepName()) {
				checkStepStatusForErwerbspensum(wizardStep, false);
			}
		}
	}

	/**
	 * Wenn der Status aller Betreuungen des Gesuchs VERFUEGT ist, dann wechseln wir den Staus von VERFUEGEN auf OK.
	 * Der Status des Gesuchs wechselt auch dann auf VERFUEGT, da alle Angebote sind verfuegt
	 */
	private void updateAllStatusForVerfuegen(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.VERFUEGEN == wizardStep.getWizardStepName()
				&& WizardStepStatus.OK != wizardStep.getWizardStepStatus()) {
				List<Betreuung> alleBetreuungen = wizardStep.getGesuch().extractAllBetreuungen();
				if (alleBetreuungen
					.stream()
					.allMatch(betreuung -> betreuung.getBetreuungsstatus().isGeschlossen())) {
					gesuchVerfuegen(wizardStep);
				}
			}
		}
	}

	/**
	 * In dieser Methode werden alle Sachen gemacht, die gebraucht werden, um ein Gesuch zu verfuegen.
	 */
	private void gesuchVerfuegen(@NotNull WizardStep verfuegenWizardStep) {
		if (verfuegenWizardStep.getWizardStepName() == WizardStepName.VERFUEGEN) {
			verfuegenWizardStep.setWizardStepStatus(WizardStepStatus.OK);
			verfuegenWizardStep.getGesuch().setStatus(AntragStatus.VERFUEGT);
			gesuchService.postGesuchVerfuegen(verfuegenWizardStep.getGesuch());

			// Hier wird das Gesuch oder die Mutation effektiv verfügt. Daher müssen hier noch andere Services gerufen
			// werden!
			try {
				generatedDokumentService.getBegleitschreibenDokument(verfuegenWizardStep.getGesuch(), true);
			} catch (MimeTypeParseException | MergeDocException e) {
				LOG.error("Error updating Deckblatt Dokument", e);
			}

			try {
				GemeindeStammdaten gemeindeStammdaten =
					gemeindeService.getGemeindeStammdatenByGemeindeId(verfuegenWizardStep.getGesuch()
						.getDossier()
						.getGemeinde()
						.getId()).get();
				if (gemeindeStammdaten.getBenachrichtigungBgEmailAuto()) {
					if (!verfuegenWizardStep.getGesuch().isMutation()) {
						// Erstgesuch
						mailService.sendInfoVerfuegtGesuch(verfuegenWizardStep.getGesuch());
					} else {
						// Mutation
						mailService.sendInfoVerfuegtMutation(verfuegenWizardStep.getGesuch());
					}
				}
			} catch (MailException e) {
				logExceptionAccordingToEnvironment(e, "Error sending Mail zu gesuchsteller", "");
			}

			antragStatusHistoryService.saveStatusChange(verfuegenWizardStep.getGesuch(), null);
		}
	}

	/**
	 * Wenn der Status von Gesuchsteller auf OK gesetzt wird, koennen wir davon ausgehen, dass die benoetigten GS
	 * eingetragen wurden. Deswegen kann man die steps fuer die FINANZIELLE_SITUATION_X und EINKOMMENSVERSCHLECHTERUNG aktivieren
	 */
	private void updateAllStatusForGesuchsteller(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.GESUCHSTELLER == wizardStep.getWizardStepName()) {
				setWizardStepOkOrMutiert(wizardStep);
			} else if ((wizardStep.getWizardStepName().isFinSitWizardStepName()
				|| wizardStep.getWizardStepName().isEKVWizardStepName()
				|| WizardStepName.ERWERBSPENSUM == wizardStep.getWizardStepName())
				&& !wizardStep.getVerfuegbar()
				&& WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()) {
				wizardStep.setVerfuegbar(true);
			}
		}
	}

	private void updateAllStatusForUmzug(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.UMZUG == wizardStep.getWizardStepName()) {
				setWizardStepOkOrMutiert(wizardStep);
			}
		}
	}

	private void updateAllStatusForAbwesenheit(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.ABWESENHEIT == wizardStep.getWizardStepName()) {
				setWizardStepOkOrMutiert(wizardStep);
			}
		}
	}

	private void updateAllStatusForFinSit(List<WizardStep> wizardSteps, @Nullable Integer substep) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()) {
				final Gesuch gesuch = wizardStep.getGesuch();
				if (wizardStep.getWizardStepName().isFinSitWizardStepName()) {
					if (gesuch.isMutation()) {
						// Problem: Es kann in der Mutation sowohl eine Aenderung (Status MUTIERT) als auch ein Fehler
						// (Status NOK)
						// gleichzeitig auftreten! Wir zeigen zuerst den Status NOK an
						setStatusDueToFinSitRequired(wizardStep, gesuch);
						if (WizardStepStatus.OK == wizardStep.getWizardStepStatus()) {
							// Wenn es okay war, koennen wir gegebenenfalls das MUTIERT setzen
							setWizardStepOkOrMutiert(wizardStep);
						}
					} else if (Objects.equals(1, substep)) { //only for substep 1 (finanziellesituationstart)
						setStatusDueToFinSitRequired(wizardStep, gesuch);
					}
				}
				if (wizardStep.getWizardStepName().isEKVWizardStepName()
					&& Objects.equals(1, substep)) {
					setStatusDueToFinSitRequired(wizardStep, gesuch);
				}
			}
		}
	}

	private void setStatusDueToFinSitRequired(WizardStep wizardStep, Gesuch gesuch) {
		if (!EbeguUtil.isFinanzielleSituationRequired(gesuch) && EbeguUtil.isFamilienSituationVollstaendig(gesuch)) {
			setWizardStepOkay(gesuch.getId(), wizardStep.getWizardStepName());

		} else if (!EbeguUtil.isFinanzielleSituationIntroducedAndComplete(
			wizardStep.getGesuch(),
			wizardStep.getWizardStepName())) {
			// the FinSit/EKV is required but has not been created yet or is only partialy filled, so it must be NOK
			wizardStep.setWizardStepStatus(WizardStepStatus.NOK);
		}
	}

	@Override
	public void setWizardStepOkOrMutiert(@NotNull WizardStep wizardStep) {
		wizardStep.setWizardStepStatus(getWizardStepStatusOkOrMutiert(wizardStep));
	}

	@Override
	public void unsetWizardStepFreigabe(@NotNull String gesuchId) {
		final List<WizardStep> wizardSteps = findWizardStepsFromGesuch(gesuchId);
		WizardStep wizardStepFreigabe =
			wizardSteps.stream()
				.filter(step -> step.getWizardStepName() == WizardStepName.FREIGABE)
				.findFirst()
				.get();

		wizardStepFreigabe.setWizardStepStatus(WizardStepStatus.WARTEN);
	}

	private WizardStepStatus getWizardStepStatusOkOrMutiert(WizardStep wizardStep) {
		if (AntragTyp.MUTATION != wizardStep.getGesuch().getTyp()) {
			// just to avoid doing the calculation for Gesuche that are not of Type Mutation if it is not needed
			return WizardStepStatus.OK;
		}

		final List<AbstractMutableEntity> newObjects =
			getStepRelatedObjects(wizardStep.getWizardStepName(), wizardStep.getGesuch());
		Objects.requireNonNull(wizardStep.getGesuch().getVorgaengerId());
		Optional<Gesuch> vorgaengerGesuch =
			this.gesuchService.findGesuch(wizardStep.getGesuch().getVorgaengerId(), false);
		if (!vorgaengerGesuch.isPresent()) {
			throw new EbeguEntityNotFoundException("getWizardStepStatusOkOrMutiert", ErrorCodeEnum
				.ERROR_VORGAENGER_MISSING, "Vorgaenger Gesuch fuer Mutation nicht gefunden");
		}
		final List<AbstractMutableEntity> vorgaengerObjects =
			getStepRelatedObjects(wizardStep.getWizardStepName(), vorgaengerGesuch.get());
		boolean isMutiert = isObjectMutiert(newObjects, vorgaengerObjects);
		if (AntragTyp.MUTATION == wizardStep.getGesuch().getTyp() && isMutiert) {
			return WizardStepStatus.MUTIERT;
		}
		return WizardStepStatus.OK;
	}

	/**
	 * Returns all Objects that are related to the given Step. For instance for the Step GESUCHSTELLER it returns
	 * the object Gesuchsteller1 and Gesuchsteller2. These objects can then be used to check for changes.
	 */
	@SuppressWarnings("OverlyComplexMethod")
	private List<AbstractMutableEntity> getStepRelatedObjects(
		@NotNull WizardStepName wizardStepName,
		@NotNull Gesuch gesuch) {
		List<AbstractMutableEntity> relatedObjects = new ArrayList<>();
		if (WizardStepName.FAMILIENSITUATION == wizardStepName
			&& gesuch.getFamiliensituationContainer() != null) {
			relatedObjects.add(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		} else if (WizardStepName.GESUCHSTELLER == wizardStepName) {
			addRelatedObjectsForGesuchsteller(relatedObjects, gesuch.getGesuchsteller1());
			addRelatedObjectsForGesuchsteller(relatedObjects, gesuch.getGesuchsteller2());
		} else if (WizardStepName.UMZUG == wizardStepName) {
			addRelatedObjectsForUmzug(gesuch.getGesuchsteller1(), relatedObjects);
			addRelatedObjectsForUmzug(gesuch.getGesuchsteller2(), relatedObjects);
		} else if (WizardStepName.KINDER == wizardStepName) {
			relatedObjects.addAll(gesuch.getKindContainers());
		} else if (WizardStepName.BETREUUNG == wizardStepName) {
			relatedObjects.addAll(gesuch.extractAllBetreuungen());
		} else if (WizardStepName.ABWESENHEIT == wizardStepName) {
			relatedObjects.addAll(gesuch.extractAllAbwesenheiten());
		} else if (WizardStepName.ERWERBSPENSUM == wizardStepName) {
			if (gesuch.getGesuchsteller1() != null) {
				relatedObjects.addAll(gesuch.getGesuchsteller1().getErwerbspensenContainers());
			}
			if (gesuch.getGesuchsteller2() != null) {
				relatedObjects.addAll(gesuch.getGesuchsteller2().getErwerbspensenContainers());
			}
		} else if (wizardStepName.isFinSitWizardStepName()) {
			if (gesuch.getGesuchsteller1() != null) {
				relatedObjects.add(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
				relatedObjects.add(gesuch.extractFamiliensituation());
			}
			if (gesuch.getGesuchsteller2() != null) {
				relatedObjects.add(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
			}
		} else if (wizardStepName.isEKVWizardStepName()) {
			if (gesuch != null) {
				final EinkommensverschlechterungInfoContainer ekvInfo =
					gesuch.getEinkommensverschlechterungInfoContainer();
				if (ekvInfo != null) {
					relatedObjects.add(ekvInfo);
					if (ekvInfo.getEinkommensverschlechterungInfoJA().getEinkommensverschlechterung()) {
						if (gesuch.getGesuchsteller1() != null && gesuch.getGesuchsteller1()
							.getEinkommensverschlechterungContainer() != null) {
							relatedObjects.add(gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer());
						}
						if (gesuch.getGesuchsteller2() != null && gesuch.getGesuchsteller2()
							.getEinkommensverschlechterungContainer() != null) {
							relatedObjects.add(gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer());
						}
					}
				}
			}
		} else if (WizardStepName.DOKUMENTE == wizardStepName) {
			relatedObjects.addAll(dokumentGrundService.findAllDokumentGrundByGesuch(gesuch, false));
		}
		return relatedObjects;
	}

	/**
	 * Adds all Adressen of the given Gesuchsteller that are set as umzug
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void addRelatedObjectsForUmzug(
		@Nullable GesuchstellerContainer gesuchsteller,
		List<AbstractMutableEntity> relatedObjects) {
		if (gesuchsteller != null) {
			for (GesuchstellerAdresseContainer adresse : gesuchsteller.getAdressen()) {
				if (!adresse.extractIsKorrespondenzAdresse()
					&& !adresse.extractIsRechnungsAdresse()
					&& !adresse.getGesuchstellerAdresseJA()
					.getGueltigkeit()
					.getGueltigAb()
					.isEqual(Constants.START_OF_TIME)) { // only the first Adresse starts at START_OF_TIME
					relatedObjects.add(adresse);
				}
			}
		}
	}

	/**
	 * Adds the Gesuchsteller itself and her korrespondez- and rechnungsAdresse.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void addRelatedObjectsForGesuchsteller(
		List<AbstractMutableEntity> relatedObjects,
		@Nullable GesuchstellerContainer gesuchsteller) {
		if (gesuchsteller != null) {
			relatedObjects.add(gesuchsteller.getGesuchstellerJA());
			for (GesuchstellerAdresseContainer adresse : gesuchsteller.getAdressen()) {
				// add Korrespondez- and Rechnungsadresse and first Wohnadresse
				if (adresse.extractIsKorrespondenzAdresse()
					|| adresse.extractIsRechnungsAdresse()
					|| adresse.getGesuchstellerAdresseJA()
					.getGueltigkeit()
					.getGueltigAb()
					.isEqual(Constants.START_OF_TIME)) { // only the first Wohnadresse starts at START_OF_TIME
					relatedObjects.add(adresse);
				}
			}
		}
	}

	/**
	 * Returns true when given list have different sizes. If not, it checks whether the content of each object
	 * of the list newEntities is the same as it was in the list oldEntities. Any change will make the method return
	 * true
	 */
	private boolean isObjectMutiert(
		@NotNull List<AbstractMutableEntity> newEntities,
		@NotNull List<AbstractMutableEntity> oldEntities) {
		if (oldEntities.size() != newEntities.size()) {
			return true;
		}
		for (AbstractMutableEntity newEntity : newEntities) {
			if (newEntity != null && newEntity.getVorgaengerId() == null) {
				return true; // if there is no vorgaenger it must have changed
			}
			if (newEntity != null && newEntity.getVorgaengerId() != null) {
				final AbstractEntity vorgaengerEntity =
					persistence.find(newEntity.getClass(), newEntity.getVorgaengerId());
				if (vorgaengerEntity == null || !newEntity.isSame(vorgaengerEntity)) {
					return true;
				}
			}
		}
		return false;
	}

	private void updateAllStatusForBetreuung(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()) {

				if (WizardStepName.BETREUUNG == wizardStep.getWizardStepName()) {
					checkStepStatusForBetreuung(wizardStep, false);

				} else if (!principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())
					&& WizardStepName.ERWERBSPENSUM == wizardStep.getWizardStepName()) {
					// SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION duerfen beim Aendern einer Betreuung
					// den Status von ERWERBPENSUM nicht aendern
					checkStepStatusForErwerbspensum(wizardStep, true);

				} else if (wizardStep.getWizardStepName().isFinSitWizardStepName()) {
					checkFinSitStatusForBetreuungen(wizardStep);

				} else if (wizardStep.getWizardStepName().isEKVWizardStepName()) {
					checkFinSitStatusForBetreuungen(wizardStep);
				}
			}
		}
	}

	private void updateAllStatusForKinder(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()) {
				if (WizardStepName.BETREUUNG == wizardStep.getWizardStepName()) {
					checkStepStatusForBetreuung(wizardStep, true);

				} else if (WizardStepName.ERWERBSPENSUM == wizardStep.getWizardStepName()) {
					checkStepStatusForErwerbspensum(wizardStep, true);
				} else if (WizardStepName.KINDER == wizardStep.getWizardStepName()) {
					final List<KindContainer> kinderFromGesuch = findAllKinderFromGesuch(wizardStep);

					WizardStepStatus status;
					if (kinderFromGesuch.isEmpty()) {
						status = WizardStepStatus.NOK;
					} else if (hasNichtGepruefteKinder(kinderFromGesuch)) {
						status = WizardStepStatus.IN_BEARBEITUNG;
					} else {
						status = getWizardStepStatusOkOrMutiert(wizardStep);
					}
					wizardStep.setWizardStepStatus(status);
				}
			}
		}
	}

	private List<KindContainer> findAllKinderFromGesuch(WizardStep wizardStep) {
		return kindService.findAllKinderFromGesuch(wizardStep.getGesuch().getId())
				.stream()
				.filter(kindContainer -> kindContainer.getKindJA().getFamilienErgaenzendeBetreuung())
				.collect(Collectors.toList());
	}

	private boolean hasNichtGepruefteKinder(List<KindContainer> kinderFromGesuch) {
		return kinderFromGesuch
			.stream()
			.anyMatch(kindContainer -> !kindContainer.getKindJA().isGeprueft());
	}

	private void updateAllStatusForFamiliensituation(
		List<WizardStep> wizardSteps,
		Familiensituation oldEntity,
		Familiensituation newEntity) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT != wizardStep.getWizardStepStatus()) { // vermeide, dass der Status eines unbesuchten Steps geaendert wird
				updateStatusForFamiliensituation(wizardStep, oldEntity, newEntity);
			}
			// Es gibt ein Spezialfall: Falls eine BG Betreuung hinzugefügt wurde, wird die Frage
			// verguenstigungGewuenscht auf der FamSit true gesetzt und die FamSit wird neu gespeichert.
			// in diesem Fall müssen wir den FinSitStatus hier noch einmal für die Betreuungen prüfen.
			checkFinSitStatusForBetreuungen(wizardStep);
		}
	}

	private void updateStatusForFamiliensituation(
		WizardStep wizardStep,
		Familiensituation oldEntity,
		Familiensituation newEntity) {

		LocalDate bis = wizardStep.getGesuch().getGesuchsperiode().getGueltigkeit().getGueltigBis();
		if (WizardStepName.FAMILIENSITUATION == wizardStep.getWizardStepName()) {
			setWizardStepOkOrMutiert(wizardStep);
		} else if (WizardStepName.KINDER == wizardStep.getWizardStepName()) {
			//Nach Update der FamilienSituation kann es sein dass die Kinder View nicht mehr Valid ist
			checkStepStatusForKinderOnChangeFamSit(wizardStep);
		} else if (EbeguUtil.fromOneGSToTwoGS(oldEntity, newEntity, bis)) {
			updateStatusFromOneGSToTwoGS(wizardStep);
			//kann man effektiv sagen dass bei nur einem GS niemals Rote Schritte FinanzielleSituation und EVK
			// gibt
		} else if (!newEntity.hasSecondGesuchsteller(bis)
			&& wizardStep.getGesuch().getGesuchsteller1() != null) { // nur 1 GS
			updateStatusOnlyOneGS(wizardStep);
		}
	}

	private void checkStepStatusForKinderOnChangeFamSit(WizardStep wizardStep) {
		if(hasNichtGepruefteKinder(findAllKinderFromGesuch(wizardStep))) {
			wizardStep.setWizardStepStatus(WizardStepStatus.NOK);
		}
	}

	private void updateStatusOnlyOneGS(WizardStep wizardStep) {
		if (WizardStepName.GESUCHSTELLER == wizardStep.getWizardStepName()) {
			updateStepGesuchstellerOnlyOneGS(wizardStep);
		} else if (wizardStep.getWizardStepName().isFinSitWizardStepName() || wizardStep.getWizardStepName().isEKVWizardStepName()) {
			updateStepFinSitAndEKVOnlyOneGS(wizardStep);
		} else if (WizardStepName.ERWERBSPENSUM == wizardStep.getWizardStepName()) {
			updateStepErwerbspensumOnlyOneGS(wizardStep);
		}
	}

	private void updateStepErwerbspensumOnlyOneGS(WizardStep wizardStep) {
		if(erwerbspensumService.isErwerbspensumRequired(wizardStep.getGesuch())) {
			if (isErwerbespensumContainerEmpty(wizardStep.getGesuch().getGesuchsteller1())) {
				if (wizardStep.getWizardStepStatus() != WizardStepStatus.NOK) {
					// Wenn der Step auf NOK gesetzt wird, muss er enabled sein, damit korrigiert werden
					// kann!
					setVerguegbarAndNOK(wizardStep);
				}
			} else {
				setVerfuegbarAndOK(wizardStep);
			}
		}
	}

	private void updateStepFinSitAndEKVOnlyOneGS(WizardStep wizardStep) {
		setVerfuegbarAndOK(wizardStep);
	}

	private void updateStepGesuchstellerOnlyOneGS(WizardStep wizardStep) {
		if (wizardStep.getGesuch().isMutation()) {
			setWizardStepOkOrMutiert(wizardStep);
		} else if (wizardStep.getWizardStepStatus() == WizardStepStatus.NOK) {
			wizardStep.setWizardStepStatus(WizardStepStatus.OK);
		}
	}

	private void updateStatusFromOneGSToTwoGS(WizardStep wizardStep) {
		//Falls bereits ein GS2 exisitiert müssen die Wizardsteps beim Wechsel von ein GS auf zwei GS nicht updated werden
		if (wizardStep.getGesuch().getGesuchsteller2() != null) {
			return;
		}

		if (WizardStepName.GESUCHSTELLER == wizardStep.getWizardStepName()) {
			updateStepGesuchstellerFromOneGSTOTwoGS(wizardStep);
		} else if (wizardStep.getWizardStepName().isFinSitWizardStepName() || wizardStep.getWizardStepName().isEKVWizardStepName()) {
			updateStepFinSitAndEKVFromOneGSToTwoGS(wizardStep);
		} else if (WizardStepName.ERWERBSPENSUM == wizardStep.getWizardStepName()){
			updateStepErwerbspensumFromOneGSToTwoGS(wizardStep);
		}
	}

	private boolean isErwerbespensumContainerEmpty(GesuchstellerContainer gesuchsteller) {
		if(gesuchsteller == null)  {
			return true;
		}

		return gesuchsteller.getErwerbspensenContainers().isEmpty();
	}

	private void updateStepErwerbspensumFromOneGSToTwoGS(WizardStep wizardStep) {
		if(erwerbspensumService.isErwerbspensumRequired(wizardStep.getGesuch()) &&
			isErwerbspensumRequiredForGS2(wizardStep.getGesuch())) {
			// Wenn der Step auf NOK gesetzt wird, muss er enabled sein, damit korrigiert werden kann!
			setVerguegbarAndNOK(wizardStep);
		}
	}

	private void updateStepFinSitAndEKVFromOneGSToTwoGS(WizardStep wizardStep) {
		if(EbeguUtil.isFinanzielleSituationRequired(wizardStep.getGesuch())) {
			setVerguegbarAndNOK(wizardStep);
		}
	}

	private void updateStepGesuchstellerFromOneGSTOTwoGS(WizardStep wizardStep) {
		setVerguegbarAndNOK(wizardStep);
	}

	private void setVerfuegbarAndOK(WizardStep wizardStep) {
		if (wizardStep.getGesuch().isMutation()) {
			wizardStep.setVerfuegbar(true);
			setWizardStepOkOrMutiert(wizardStep);
		} else if (wizardStep.getWizardStepStatus() == WizardStepStatus.NOK) {
			wizardStep.setVerfuegbar(true);
			wizardStep.setWizardStepStatus(WizardStepStatus.OK);
		}
	}

	private void setVerguegbarAndNOK(WizardStep wizardStep) {
		wizardStep.setWizardStepStatus(WizardStepStatus.NOK);
		wizardStep.setVerfuegbar(true);
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkStepStatusForBetreuung(@Nonnull WizardStep wizardStep, boolean changesBecauseOtherStates) {
		List<AbstractPlatz> allPlaetze = wizardStep.getGesuch().extractAllPlaetze();
		WizardStepStatus status;
		if (changesBecauseOtherStates && wizardStep.getWizardStepStatus() != WizardStepStatus.MUTIERT) {
			status = WizardStepStatus.OK;
		} else {
			status = getWizardStepStatusOkOrMutiert(wizardStep);
		}

		if (allPlaetze.size() <= 0) {
			status = WizardStepStatus.NOK;
		} else {
			for (AbstractPlatz betreuung : allPlaetze) {
				if (Betreuungsstatus.ABGEWIESEN == betreuung.getBetreuungsstatus()) {
					status = WizardStepStatus.NOK;
					break;
				}
				if (Betreuungsstatus.WARTEN == betreuung.getBetreuungsstatus()) {
					status = WizardStepStatus.PLATZBESTAETIGUNG;
				}

				if (Betreuungsstatus.UNBEKANNTE_INSTITUTION == betreuung.getBetreuungsstatus()) {
					status = WizardStepStatus.WARTEN;
				}
			}
		}
		wizardStep.setWizardStepStatus(status);
	}

	/**
	 * Updates the Status of the Steps FINANZIELLE_SITUATION_X or EINKOMMENSVERSCHLECHTERUNG depending on the kind of the
	 * betreuungen.
	 * This should be called after removing or adding a Betreuung. It is also called after a change in the famsit
	 */
	private void checkFinSitStatusForBetreuungen(@Nonnull WizardStep wizardStep) {
		boolean isEkvOrFinSitStep = (wizardStep.getWizardStepName().isEKVWizardStepName()
			|| wizardStep.getWizardStepName().isFinSitWizardStepName());
		if (!isEkvOrFinSitStep) {
			return;
		}
		if (wizardStep.getWizardStepStatus() == WizardStepStatus.IN_BEARBEITUNG || wizardStep.getWizardStepStatus() == WizardStepStatus.UNBESUCHT) {
			return;
		}
		boolean finSitIntroducedAndComplete = EbeguUtil.isFinanzielleSituationIntroducedAndComplete(
			wizardStep.getGesuch(),	wizardStep.getWizardStepName());
		if (finSitIntroducedAndComplete) {
			return;
		}
		boolean finSitRequired = EbeguUtil.isFinanzielleSituationRequired(wizardStep.getGesuch());

		// da FinSit nicht vollständig ist, wird wizardStep immer invalidiert, wenn benötigt
		if (finSitRequired) {
			wizardStep.setWizardStepStatus(WizardStepStatus.NOK);
		}
	}

	/**
	 * Erwerbspensum muss nur erfasst werden, falls mind. 1 Kita oder 1 Tageseltern Kleinkind Angebot erfasst wurde
	 * und mind. eines dieser Kinder keine Fachstelle involviert hat
	 */
	@SuppressWarnings({ "LocalVariableNamingConvention", "NonBooleanMethodNameMayNotStartWithQuestion" })
	private void checkStepStatusForErwerbspensum(WizardStep wizardStep, boolean changesBecauseOtherStates) {
		Gesuch gesuch = wizardStep.getGesuch();
		boolean erwerbspensumRequired = erwerbspensumService.isErwerbspensumRequired(wizardStep.getGesuch());

		WizardStepStatus status = null;
		boolean available = wizardStep.getVerfuegbar();
		if (erwerbspensumRequired) {
			// Wenn das EWP required ist, muss grundsaetzlich der Step available sein
			available = true;
			if (isErwerbespensumContainerEmpty(gesuch.getGesuchsteller1())) {
				// Wenn der Step auf NOK gesetzt wird, muss er enabled sein, damit korrigiert werden kann!
				status = WizardStepStatus.NOK;
			}
			if (status != WizardStepStatus.NOK
				&& gesuch.getGesuchsteller2() != null
				&& isErwerbspensumRequiredForGS2(gesuch)) {
				// Wenn der Step auf NOK gesetzt wird, muss er enabled sein, damit korrigiert werden kann!
				status = WizardStepStatus.NOK;
			}
		} else if (changesBecauseOtherStates && wizardStep.getWizardStepStatus() != WizardStepStatus.MUTIERT) {
			status = WizardStepStatus.OK;
		}
		// Ansonsten OK bzw. MUTIERT
		if (status == null) {
			status = getWizardStepStatusOkOrMutiert(wizardStep);
		}
		wizardStep.setWizardStepStatus(status);
		wizardStep.setVerfuegbar(available);
	}


	/**
	 * Prüft, ob ein Erwerbspensum für den GS2 nötig ist.
	 * Falls ein GS2 vorhanden ist, ist ein Erwerbspesnum grundsätzlich nötig.
	 *
	 * Einzige Ausnahme bietet folgender Spezialfall innerhalb einer FKJV Periode:
	 * Die elterliche Obhut findet nicht in zwei Haushalten statt (Familiensituation#geteilteObhut)
	 * und es wurde keine Unterhaltsvereinbarung abgeschlossen (Familiensituation#unterhaltsvereinbarung).
	 * Sind diese Bedinungen erfüllt gibt es zwei Gesuschsteller, es ist allerdings nur das Erwerbspensum von GS1 relevant
	 */
	private boolean isErwerbspensumRequiredForGS2(Gesuch gesuch) {
		if(isUnterhaltsvereinbarungAbschlossen(gesuch)) {
			return false;
		}

		return gesuch.getGesuchsteller2() == null
			|| gesuch.getGesuchsteller2().getErwerbspensenContainers().isEmpty();
	}

	private boolean isUnterhaltsvereinbarungAbschlossen(Gesuch gesuch) {
		if(gesuch.getFamiliensituationContainer() == null ||
			gesuch.getFamiliensituationContainer().getFamiliensituationJA() == null ||
			gesuch.getFamiliensituationContainer().getFamiliensituationJA().getUnterhaltsvereinbarung() == null) {
			return false;
		}

		return !gesuch.getFamiliensituationContainer().getFamiliensituationJA().getUnterhaltsvereinbarung();
	}



	/**
	 * Der Step mit dem uebergebenen StepName bekommt den Status OK. Diese Methode wird immer aufgerufen, um den
	 * Status vom aktualisierten
	 * Objekt auf OK zu setzen
	 */
	private void updateStatusSingleStep(List<WizardStep> wizardSteps, WizardStepName stepName) {
		for (WizardStep wizardStep : wizardSteps) {
			if (wizardStep.getWizardStepName() == stepName) {
				wizardStep.setWizardStepStatus(WizardStepStatus.OK);
			}
		}
	}

	private WizardStep createWizardStepObject(
		Gesuch gesuch, WizardStepName wizardStepName, WizardStepStatus stepStatus,
		Boolean verfuegbar) {
		final WizardStep wizardStep = new WizardStep();
		wizardStep.setGesuch(gesuch);
		wizardStep.setVerfuegbar(verfuegbar != null ? verfuegbar : false);
		wizardStep.setWizardStepName(wizardStepName);
		wizardStep.setWizardStepStatus(stepStatus);
		return wizardStep;
	}

	@Override
	public void removeSteps(Gesuch gesToRemove) {
		List<WizardStep> wizardStepsFromGesuch = findWizardStepsFromGesuch(gesToRemove.getId());
		for (WizardStep wizardStep : wizardStepsFromGesuch) {
			authorizer.checkWriteAuthorization(wizardStep);
			persistence.remove(WizardStep.class, wizardStep.getId());
		}
	}

	@Override
	public void setWizardStepOkay(@Nonnull String gesuchId, @Nonnull WizardStepName stepName) {
		final WizardStep freigabeStep = findWizardStepFromGesuch(gesuchId, stepName);
		Objects.requireNonNull(freigabeStep, stepName.name() + " WizardStep fuer gesuch nicht gefunden " + gesuchId);
		if (WizardStepStatus.OK != freigabeStep.getWizardStepStatus()) {
			freigabeStep.setWizardStepStatus(WizardStepStatus.OK);
			saveWizardStep(freigabeStep);
		}
	}

	@Override
	@Nonnull
	public WizardStepName getFinSitWizardStepNameForGesuch(@Nonnull Gesuch gesuch) {
		switch (gesuch.getFinSitTyp()) {
		case BERN:
		case BERN_FKJV:
			return WizardStepName.FINANZIELLE_SITUATION;
		case LUZERN:
			return WizardStepName.FINANZIELLE_SITUATION_LUZERN;
		case SOLOTHURN:
			return WizardStepName.FINANZIELLE_SITUATION_SOLOTHURN;
		default:
			throw new EbeguRuntimeException("getFinSitWizardStepNameForGesuch", "no WizardStepName found for typ " + gesuch.getFinSitTyp());
		}
	}

	@Override
	@Nonnull
	public WizardStepName getEKVWizardStepNameForGesuch(@Nonnull Gesuch gesuch) {
		switch (gesuch.getFinSitTyp()) {
		case BERN:
		case BERN_FKJV:
			return WizardStepName.EINKOMMENSVERSCHLECHTERUNG;
		case LUZERN:
			return WizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN;
		case SOLOTHURN:
			return WizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN;
		default:
			throw new EbeguRuntimeException("getEKVWizardStepNameForGesuch", "no WizardStepName found for typ " + gesuch.getFinSitTyp());
		}
	}
}
