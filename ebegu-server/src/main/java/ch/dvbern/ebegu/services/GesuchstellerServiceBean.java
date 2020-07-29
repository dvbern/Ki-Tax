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
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.validationgroups.GesuchstellerSaveValidationGroup;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.Validate;

/**
 * Service fuer Gesuchsteller
 */
@Stateless
@Local(GesuchstellerService.class)
public class GesuchstellerServiceBean extends AbstractBaseService implements GesuchstellerService {

	@Inject
	private Persistence persistence;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	public GesuchstellerContainer saveGesuchsteller(
		@Nonnull GesuchstellerContainer gesuchsteller,
		@Nonnull final Gesuch gesuch, @Nonnull Integer gsNumber, boolean umzug) {
		Objects.requireNonNull(gesuchsteller);
		Objects.requireNonNull(gesuch);
		Objects.requireNonNull(gsNumber);

		validateGesuchstellerEmail(gesuch);

		createFinSitInMutationIfNotExisting(gesuchsteller, gesuch, gsNumber);
		createEKVInMutationIfNotExisting(gesuchsteller, gesuch, gsNumber);

		if (!gesuchsteller.isNew()) {
			// Den Lucene-Index manuell nachführen, da es bei unidirektionalen Relationen nicht automatisch geschieht!
			updateLuceneIndex(GesuchstellerContainer.class, gesuchsteller.getId());
		}
		final GesuchstellerContainer mergedGesuchsteller = persistence.merge(gesuchsteller);
		if (gsNumber == 1) {
			gesuch.setGesuchsteller1(mergedGesuchsteller);
		} else if (gsNumber == 2) {
			gesuch.setGesuchsteller2(mergedGesuchsteller);
		}
		updateWizStepsForGesuchstellerView(gesuch, gsNumber, umzug, mergedGesuchsteller.getGesuchstellerJA());
		return mergedGesuchsteller;
	}

	private void validateGesuchstellerEmail(@Nonnull Gesuch gesuch) {
		// Gesamt-Validierung durchführen
		Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Gesuch>> constraintViolations =
			validator.validate(gesuch, GesuchstellerSaveValidationGroup.class);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	/**
	 * Bei Mutationen fuer den GS2 muss eine leere Einkommensverschlechterung hinzugefuegt werden, wenn sie noch nicht
	 * existiert. Dies aber
	 * nur wenn die Einkommensverschlechterung required ist.
	 */
	private void createEKVInMutationIfNotExisting(
		@Nonnull GesuchstellerContainer gesuchsteller,
		Gesuch gesuch,
		Integer gsNumber) {
		if (gesuch.isMutation() && gesuch.extractEinkommensverschlechterungInfo() == null
			&& gsNumber == 2 && gesuchsteller.getEinkommensverschlechterungContainer() == null
			&& EbeguUtil.isFinanzielleSituationRequired(gesuch)) {

			EinkommensverschlechterungContainer evContainer = new EinkommensverschlechterungContainer();
			evContainer.setGesuchsteller(gesuchsteller);
			gesuchsteller.setEinkommensverschlechterungContainer(evContainer);
			GesuchstellerContainer gs1Container = gesuch.getGesuchsteller1();
			if (gs1Container != null && gs1Container.getEinkommensverschlechterungContainer() != null) {
				if (gs1Container.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1()
					!= null) {
					final Einkommensverschlechterung ekvJABasisJahrPlus1 = new Einkommensverschlechterung();
					gesuchsteller.getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(ekvJABasisJahrPlus1);
				}
				if (gs1Container.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2()
					!= null) {
					final Einkommensverschlechterung ekvJABasisJahrPlus2 = new Einkommensverschlechterung();
					gesuchsteller.getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(ekvJABasisJahrPlus2);
				}
			}
		}
	}

	/**
	 * Bei Mutationen fuer den GS2 muss eine leere Finanzielle Situation hinzugefuegt werden, wenn sie noch nicht
	 * existiert. Dies aber nur wenn die FinSit required ist.
	 */
	private void createFinSitInMutationIfNotExisting(
		@Nonnull GesuchstellerContainer gesuchsteller,
		@Nonnull Gesuch gesuch,
		@Nonnull Integer gsNumber) {
		if (gesuch.isMutation() && gsNumber == 2 && gesuchsteller.getFinanzielleSituationContainer() == null
			&& EbeguUtil.isFinanzielleSituationRequired(gesuch)) {

			// Die Felder SteuerveranlagungErhalten und SteuererklaerungAusgefuellt muessen u.U. vom GS1 kopiert werden
			// (bei gemeinsamer Steuererklaerung)
			Familiensituation familiensituation = gesuch.extractFamiliensituation();
			Objects.requireNonNull(familiensituation);
			boolean gemeinsameStek = familiensituation.getGemeinsameSteuererklaerung() != null ?
				familiensituation.getGemeinsameSteuererklaerung() : false;
			GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
			Objects.requireNonNull(gesuchsteller1, "Gesuchsteller 1 muss zu diesem Zeitpunkt gesetzt sein");
			Objects.requireNonNull(gesuchsteller1.getFinanzielleSituationContainer(), "Finanzielle Situation GS1 muss zu diesem Zeitpunkt gesetzt sein");

			boolean stvErhaltenGs2 = false; 	// by default
			boolean stekAusgefuelltGs2 = false; // by default
			if (gemeinsameStek && gesuchsteller1.getFinanzielleSituationContainer().getFinanzielleSituationJA() != null) {
				stvErhaltenGs2 = gesuchsteller1.getFinanzielleSituationContainer().getFinanzielleSituationJA().getSteuerveranlagungErhalten();
				stekAusgefuelltGs2 = gesuchsteller1.getFinanzielleSituationContainer().getFinanzielleSituationJA().getSteuererklaerungAusgefuellt();
			}

			final FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
			final FinanzielleSituation finanzielleSituationJA = new FinanzielleSituation();
			finanzielleSituationJA.setSteuerveranlagungErhalten(stvErhaltenGs2);
			finanzielleSituationJA.setSteuererklaerungAusgefuellt(stekAusgefuelltGs2);
			finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituationJA); // alle Werte by default
			// auf null -> nichts eingetragen
				finanzielleSituationContainer.setJahr(gesuchsteller1
				.getFinanzielleSituationContainer()
				.getJahr()); // copy it from GS1
			finanzielleSituationContainer.setGesuchsteller(gesuchsteller);
			gesuchsteller.setFinanzielleSituationContainer(finanzielleSituationContainer);
		}
	}

	private void updateWizStepsForGesuchstellerView(
		Gesuch gesuch,
		Integer gsNumber,
		boolean umzug,
		Gesuchsteller gesuchsteller) {
		// Wenn beide Gesuchsteller ausgefuellt werden muessen (z.B bei einer Mutation die die Familiensituation aendert
		// (i.e. von 1GS auf 2GS) wollen wir den Benutzer zwingen beide Gesuchsteller Seiten zu besuchen bevor wir auf
		// ok setzten.
		// Ansonsten setzten wir es sofort auf ok
		if (umzug) {
			wizardStepService.updateSteps(gesuch.getId(), null, gesuchsteller, WizardStepName.UMZUG);
		} else {
			WizardStep existingWizStep =
				wizardStepService.findWizardStepFromGesuch(gesuch.getId(), WizardStepName.GESUCHSTELLER);
			WizardStepStatus gesuchStepStatus = existingWizStep != null ? existingWizStep.getWizardStepStatus() : null;
			if (WizardStepStatus.NOK == gesuchStepStatus || WizardStepStatus.IN_BEARBEITUNG == gesuchStepStatus) {
				if (isSavingLastNecessaryGesuchsteller(gesuch, gsNumber)) {
					wizardStepService.updateSteps(gesuch.getId(), null, gesuchsteller, WizardStepName.GESUCHSTELLER);
				}
			} else {
				wizardStepService.updateSteps(gesuch.getId(), null, gesuchsteller, WizardStepName.GESUCHSTELLER);
			}
		}
	}

	/**
	 * Wenn aufgrund der Familiensituation 2 GS noetig sind kommt hier true zurueck wenn gsNumber = 2 ist. sonst false
	 * Wenn aufgrund der Familiensitation 1 GS noetig ist kommt hier true zurueck wenn gsNumber = 1
	 */
	private boolean isSavingLastNecessaryGesuchsteller(Gesuch gesuch, Integer gsNumber) {
		LocalDate bis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		boolean gs2 = Objects.requireNonNull(gesuch.extractFamiliensituation()).hasSecondGesuchsteller(bis);
		return (gs2 && gsNumber == 2) || (!gs2 && gsNumber == 1);
	}

	@Nonnull
	@Override
	public Optional<GesuchstellerContainer> findGesuchsteller(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		GesuchstellerContainer a = persistence.find(GesuchstellerContainer.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	public Collection<GesuchstellerContainer> getAllGesuchsteller() {
		return new ArrayList<>(criteriaQueryHelper.getAll(GesuchstellerContainer.class));
	}

	@Override
	public void removeGesuchsteller(@Nonnull GesuchstellerContainer gesuchsteller) {
		Objects.requireNonNull(gesuchsteller);
		GesuchstellerContainer gesuchstellerToRemove = findGesuchsteller(gesuchsteller.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"removeGesuchsteller",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsteller));
		persistence.remove(gesuchstellerToRemove);
	}

	@Nullable
	@Override
	public Gesuch findGesuchOfGesuchsteller(@Nonnull String gesuchstellerContainerID) {
		Validate.notEmpty(gesuchstellerContainerID, "gesuchstellerContainerID must be set");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<String> gesuchsteller1ID = cb.parameter(String.class, "gesuchsteller1ID");
		ParameterExpression<String> gesuchsteller2ID = cb.parameter(String.class, "gesuchsteller2ID");
		Predicate predicateGs1 = cb.equal(root.get(Gesuch_.gesuchsteller1).get(AbstractEntity_.id), gesuchsteller1ID);
		Predicate predicateGs2 = cb.equal(root.get(Gesuch_.gesuchsteller2).get(AbstractEntity_.id), gesuchsteller2ID);
		Predicate predicateGs1OrGs2 = cb.or(predicateGs1, predicateGs2);
		query.where(predicateGs1OrGs2);
		TypedQuery<Gesuch> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(gesuchsteller1ID, gesuchstellerContainerID);
		typedQuery.setParameter(gesuchsteller2ID, gesuchstellerContainerID);
		return typedQuery.getSingleResult();
	}
}
