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

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.cdipersistence.Persistence;

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
	private GesuchstellerService gesuchstellerService;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private SozialhilfeZeitraumService sozialhilfeZeitraumService;

	@Override
	public FamiliensituationContainer saveFamiliensituation(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation
	) {
		Objects.requireNonNull(familiensituationContainer);
		Objects.requireNonNull(gesuch);

		// Falls noch nicht vorhanden, werden die GemeinsameSteuererklaerung fuer FS und EV auf false gesetzt
		Familiensituation newFamiliensituation = familiensituationContainer.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		if (gesuch.isMutation() && EbeguUtil.fromOneGSToTwoGS(familiensituationContainer, gesuchsperiodeBis)) {

			if (newFamiliensituation.getGemeinsameSteuererklaerung() == null) {
				newFamiliensituation.setGemeinsameSteuererklaerung(false);
			}
		} else {
			Familiensituation familiensituationErstgesuch =
				familiensituationContainer.getFamiliensituationErstgesuch();
			if (familiensituationErstgesuch != null &&
				(!familiensituationErstgesuch.hasSecondGesuchsteller(gesuchsperiodeBis)
					&& !newFamiliensituation.hasSecondGesuchsteller(gesuchsperiodeBis))) {
				// if there is no GS2 the field gemeinsameSteuererklaerung must be set to null
				newFamiliensituation.setGemeinsameSteuererklaerung(null);
			}
		}
		final FamiliensituationContainer mergedFamiliensituationContainer = persistence.merge
			(familiensituationContainer);
		gesuch.setFamiliensituationContainer(mergedFamiliensituationContainer);

		// get old FamSit to compare with
		Familiensituation oldFamiliensituation;
		if (mergedFamiliensituationContainer != null
				&& mergedFamiliensituationContainer .getFamiliensituationErstgesuch() != null) {
			// Bei Mutation immer die Situation vom Erstgesuch als  Basis fuer Wizardstepanpassung
			oldFamiliensituation = mergedFamiliensituationContainer.getFamiliensituationErstgesuch();
		} else {
			oldFamiliensituation = loadedFamiliensituation;
		}

		//Alle Daten des GS2 loeschen wenn man von 2GS auf 1GS wechselt und GS2 bereits erstellt wurde
		Objects.requireNonNull(mergedFamiliensituationContainer);
		if (gesuch.getGesuchsteller2() != null
			&& isNeededToRemoveGesuchsteller2(gesuch, mergedFamiliensituationContainer.extractFamiliensituation(),
			oldFamiliensituation)
		) {
			gesuchstellerService.removeGesuchsteller(gesuch.getGesuchsteller2());
			gesuch.setGesuchsteller2(null);
			newFamiliensituation.setGemeinsameSteuererklaerung(false);
		}

		wizardStepService.updateSteps(gesuch.getId(), oldFamiliensituation, newFamiliensituation, WizardStepName
			.FAMILIENSITUATION);
		return mergedFamiliensituationContainer;
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

	/**
	 * Wenn die neue Familiensituation nur 1GS hat und der zweite GS schon existiert, wird dieser
	 * und seine Daten endgueltig geloescht. Dies gilt aber nur fuer ERSTGESUCH. Bei Mutationen wird
	 * der 2GS nie geloescht. Ebenfalls nicht geloescht wird im KorrekturmodusGemeinde
	 */
	private boolean isNeededToRemoveGesuchsteller2(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		Familiensituation familiensituationErstgesuch
	) {
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		return (!EbeguUtil.isKorrekturmodusGemeinde(gesuch) || (gesuch.getGesuchsteller2() != null && gesuch.getGesuchsteller2().getGesuchstellerGS() == null))
			&& ((!gesuch.isMutation() && gesuch.getGesuchsteller2() != null
			&& !newFamiliensituation.hasSecondGesuchsteller(gesuchsperiodeBis))
			|| (gesuch.isMutation() && isChanged1To2Reverted(gesuch, newFamiliensituation,
			familiensituationErstgesuch)));
	}

	private boolean isChanged1To2Reverted(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		Familiensituation familiensituationErstgesuch
	) {
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		return gesuch.getGesuchsteller2() != null && !familiensituationErstgesuch.hasSecondGesuchsteller(gesuchsperiodeBis)
			&& !newFamiliensituation.hasSecondGesuchsteller(gesuchsperiodeBis);
	}
}
