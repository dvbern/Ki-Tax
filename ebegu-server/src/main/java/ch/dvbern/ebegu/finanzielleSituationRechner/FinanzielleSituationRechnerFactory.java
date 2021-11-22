/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.services.AbstractBaseService;

public final class FinanzielleSituationRechnerFactory {

	@Nonnull
	public static AbstractFinanzielleSituationRechner getRechner(@Nonnull Gesuch gesuch) {
		if (gesuch.getFinSitTyp().equals(FinanzielleSituationTyp.LUZERN)) {
			return new FinanzielleSituationLuzernRechner();
		}
		// per default ist der Berner Rechner genommen
		return new FinanzielleSituationBernRechner();
	}

}
