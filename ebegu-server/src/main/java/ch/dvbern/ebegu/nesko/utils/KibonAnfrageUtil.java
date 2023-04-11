/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.nesko.utils;

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;

public final class KibonAnfrageUtil {

	private KibonAnfrageUtil(){
	}

	public static boolean hasGesuchOneGSWithGeburstdatum(@Nonnull Gesuch gesuch, LocalDate geburtsdatum) {
		if (gesuch.getGesuchsteller1() != null &&
			gesuch.getGesuchsteller1().getGesuchstellerJA().getGeburtsdatum().equals(geburtsdatum)) {
			return true;
		}
		return (gesuch.getGesuchsteller2() != null &&
			gesuch.getGesuchsteller2().getGesuchstellerJA().getGeburtsdatum().equals(geburtsdatum));
	}

	public static boolean hasGesuchSteuerdatenResponseWithZpvNummer(@Nonnull Gesuch gesuch, int zpvNummer) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());

		if (isZpvNrFromAntragsteller(gesuch.getGesuchsteller1().getFinanzielleSituationContainer(), zpvNummer)) {
			return true;
		}

		if (gesuch.getGesuchsteller2() != null &&
			gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null) {
			return isZpvNrFromAntragsteller(gesuch.getGesuchsteller2().getFinanzielleSituationContainer(), zpvNummer);
		}

		return false;
	}

	private static boolean isZpvNrFromAntragsteller(@Nullable FinanzielleSituationContainer finanzielleSituationContainer, int zpvNummer) {
		if (finanzielleSituationContainer == null) {
			return false;
		}

		SteuerdatenResponse steuerdatenResponse =
			finanzielleSituationContainer
			.getFinanzielleSituationJA()
			.getSteuerdatenResponse();

		if (steuerdatenResponse == null || steuerdatenResponse.getZpvNrAntragsteller() == null) {
			return false;
		}

		return steuerdatenResponse.getZpvNrAntragsteller().equals(zpvNummer);
	}
}
