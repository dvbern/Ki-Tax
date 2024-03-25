/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.validators.finsit;


import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.util.EbeguUtil;
import lombok.RequiredArgsConstructor;

/**
 * Dieser Validator die Komplettheit und Gültigkeit eines FinanzielleSituationContainer
 */
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CheckFinanzielleSituationCompleteSchwyzValidator implements
	ConstraintValidator<CheckFinanzielleSituationSchwyzComplete, FinanzielleSituation> {

	// We need to pass to EinstellungService a new EntityManager to avoid errors like ConcurrentModificatinoException. So we create it here
	// and pass it to the methods of EinstellungService we need to call.
	//http://stackoverflow.com/questions/18267269/correct-way-to-do-an-entitymanager-query-during-hibernate-validation
	@PersistenceUnit(unitName = "ebeguPersistenceUnit")
	private EntityManagerFactory entityManagerFactory;

	private final GesuchService gesuchService;

	@Override
	public void initialize(CheckFinanzielleSituationSchwyzComplete constraintAnnotation) {
		//nop
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public boolean isValid(FinanzielleSituation finanzielleSituation, ConstraintValidatorContext context) {
		Gesuch gesuch = gesuchService.findGesuchForFinSit(finanzielleSituation.getId(), createEntityManager()).orElseThrow(() -> new EbeguEntityNotFoundException(
			"CheckFinanzielleSituationCompleteValidator.isValid",
			"Could not find gesuch of FinanzielleSituation {}",
			finanzielleSituation.getId()));
		if (gesuch.getFinSitTyp() != FinanzielleSituationTyp.SCHWYZ) {
			return true;
		}
		return EbeguUtil.isFinSitSchwyzVollstaendig(finanzielleSituation);
	}

	private EntityManager createEntityManager() {
		if (entityManagerFactory != null) {
			return entityManagerFactory.createEntityManager(); // creates a new EntityManager
		}
		return null;
	}



}
