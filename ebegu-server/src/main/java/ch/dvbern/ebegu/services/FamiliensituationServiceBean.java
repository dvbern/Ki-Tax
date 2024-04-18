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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.famsitchangehandler.FamSitChangeHandler;
import ch.dvbern.lib.cdipersistence.Persistence;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Service fuer familiensituation
 */
@Stateless
@Local(FamiliensituationService.class)
public class FamiliensituationServiceBean extends AbstractBaseService implements FamiliensituationService {

	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private SozialhilfeZeitraumService sozialhilfeZeitraumService;

	@Inject
	private FamSitChangeHandler famSitChangeHandler;

	@Override
	public FamiliensituationContainer saveFamiliensituation(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation //OLD Familiensituation
	) {
		Objects.requireNonNull(familiensituationContainer);
		Objects.requireNonNull(gesuch);

		// Falls noch nicht vorhanden, werden die GemeinsameSteuererklaerung fuer FS und EV auf false gesetzt
		Familiensituation newFamiliensituation = familiensituationContainer.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);
		famSitChangeHandler.adaptFinSitDataOnFamSitChange(gesuch, familiensituationContainer, loadedFamiliensituation);

		final FamiliensituationContainer mergedFamiliensituationContainer = persistence.merge(familiensituationContainer);
		gesuch.setFamiliensituationContainer(mergedFamiliensituationContainer);

		// get old FamSit to compare with
		Familiensituation oldFamiliensituation = getOldFamiliensituation(loadedFamiliensituation, mergedFamiliensituationContainer);

		famSitChangeHandler.removeGS2DataOnChangeFrom2To1GS(gesuch, newFamiliensituation, mergedFamiliensituationContainer, oldFamiliensituation);
		famSitChangeHandler.handleFamSitChange(gesuch, mergedFamiliensituationContainer, oldFamiliensituation);
		famSitChangeHandler.handlePossibleGS2Tausch(gesuch, newFamiliensituation);
		famSitChangeHandler.handlePossibleKinderabzugFragenReset(gesuch, newFamiliensituation, oldFamiliensituation);

		wizardStepService.updateSteps(gesuch.getId(), oldFamiliensituation, newFamiliensituation, WizardStepName
			.FAMILIENSITUATION);
		return mergedFamiliensituationContainer;
	}

	private static Familiensituation getOldFamiliensituation(
		Familiensituation loadedFamiliensituation,
		FamiliensituationContainer mergedFamiliensituationContainer) {
		Familiensituation oldFamiliensituation;
		if (mergedFamiliensituationContainer != null
			&& mergedFamiliensituationContainer.getFamiliensituationErstgesuch() != null) {
			// Bei Mutation immer die Situation vom Erstgesuch als  Basis fuer Wizardstepanpassung
			oldFamiliensituation = mergedFamiliensituationContainer.getFamiliensituationErstgesuch();
		} else {
			oldFamiliensituation = loadedFamiliensituation;
		}
		return oldFamiliensituation;
	}



	@Nonnull
	@Override
	public Optional<FamiliensituationContainer> findFamiliensituation(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		FamiliensituationContainer a = persistence.find(FamiliensituationContainer.class, key);
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	public Collection<FamiliensituationContainer> getAllFamiliensituatione() {
		return new ArrayList<>(criteriaQueryHelper.getAll(FamiliensituationContainer.class));
	}

	@Override
	public void removeFamiliensituation(@Nonnull FamiliensituationContainer familiensituation) {
		Objects.requireNonNull(familiensituation);
		FamiliensituationContainer familiensituationToRemove =
			findFamiliensituation(familiensituation.getId()).orElseThrow(() -> new EbeguEntityNotFoundException(
				"removeFall", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, familiensituation));
		for (SozialhilfeZeitraumContainer sozialhilfeZeitraumCtn :
			familiensituationToRemove.getSozialhilfeZeitraumContainers()) {
			sozialhilfeZeitraumService.removeSozialhilfeZeitraum(sozialhilfeZeitraumCtn.getId());
		}
		persistence.remove(familiensituationToRemove);
	}

}
