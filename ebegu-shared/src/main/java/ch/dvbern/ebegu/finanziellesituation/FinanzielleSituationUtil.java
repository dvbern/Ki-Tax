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

package ch.dvbern.ebegu.finanziellesituation;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class FinanzielleSituationUtil {

	public static Optional<FinanzielleSituationContainer> findFinanzielleSituation(
		@Nullable GesuchstellerContainer gesuchsteller
	) {
		return Optional.ofNullable(gesuchsteller)
			.map(GesuchstellerContainer::getFinanzielleSituationContainer);
	}

	public static Optional<FinanzielleSituation> findFinanzielleSituationJA(@Nullable GesuchstellerContainer gesuchsteller) {
		return findFinanzielleSituation(gesuchsteller)
			.map(FinanzielleSituationContainer::getFinanzielleSituationJA);
	}

	public static FinanzielleSituationContainer requireFinanzielleSituation(@Nullable GesuchstellerContainer gesuchsteller) {
		return findFinanzielleSituation(gesuchsteller)
			.orElseThrow(() -> new EntityNotFoundException("FinanzielleSituationContainer not found for " + gesuchsteller));
	}

	public static Optional<EinkommensverschlechterungProJahr> findEinkommensverschlechterung(
		@Nullable GesuchstellerContainer gesuchsteller,
		int jahrOffset
	) {
		return Optional.ofNullable(gesuchsteller)
			.map(GesuchstellerContainer::getEinkommensverschlechterungContainer)
			.map(e -> jahrOffset == 1
				? new EinkommensverschlechterungProJahr(e.getEkvGSBasisJahrPlus1(), e.getEkvJABasisJahrPlus1())
				: new EinkommensverschlechterungProJahr(e.getEkvGSBasisJahrPlus2(), e.getEkvJABasisJahrPlus2())
			)
			.filter(e -> e.getGs() != null || e.getJa() != null);
	}
}
