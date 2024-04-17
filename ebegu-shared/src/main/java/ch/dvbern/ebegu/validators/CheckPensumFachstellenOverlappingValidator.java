/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.validators;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Validator for PensumFachstelle, checks that the entered gueltigkeiten are not overlapping
 */
public class CheckPensumFachstellenOverlappingValidator
	implements ConstraintValidator<CheckPensumFachstellenOverlapping, KindContainer> {

	@Override
	public boolean isValid(@Nonnull KindContainer kindContainer, ConstraintValidatorContext context) {
		if (kindContainer.getKindJA() == null) {
			return true;
		}
		final List<DateRange> gueltigkeiten = kindContainer.getKindJA()
			.getPensumFachstelle()
			.stream()
			.map(AbstractDateRangedEntity::getGueltigkeit).collect(Collectors.toList());

		return gueltigkeiten.stream()
			.noneMatch(g1 -> overlapsAnyOtherGueltigkeit(g1, gueltigkeiten));
	}

	private boolean overlapsAnyOtherGueltigkeit(DateRange gueltigkeit, Collection<DateRange> allGueltigkeiten) {
		return allGueltigkeiten.stream()
			.filter(gueltigkeit::intersects)
			.count() > 1;
	}

}
