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

package ch.dvbern.ebegu.validators.dateranges;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;

/**
 * Validator fuer Datum in Abwesenheiten. Die Zeitraeume duerfen sich nicht ueberschneiden
 */
public class CheckAbwesenheitDatesOverlappingValidator implements ConstraintValidator<CheckAbwesenheitDatesOverlapping, Betreuung> {

	@Override
	public boolean isValid(Betreuung instance, ConstraintValidatorContext context) {
		return isOverlapFree(AbwesenheitContainer::getAbwesenheitJA, instance.getAbwesenheitContainers())
			&& isOverlapFree(AbwesenheitContainer::getAbwesenheitGS, instance.getAbwesenheitContainers());
	}

	private boolean isOverlapFree(
		Function<AbwesenheitContainer, Abwesenheit> mapper,
		Set<AbwesenheitContainer> abwesenheitContainers
	) {
		List<Abwesenheit> gueltigkeitIntervals = abwesenheitContainers.stream()
			.map(mapper)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		return !GueltigkeitsUtil.hasOverlapingGueltigkeit(gueltigkeitIntervals);
	}
}
