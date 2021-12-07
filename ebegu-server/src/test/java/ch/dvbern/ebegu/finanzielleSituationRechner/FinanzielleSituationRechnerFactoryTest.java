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

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class FinanzielleSituationRechnerFactoryTest {

	@Test
	public void testFinSitTypBern() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN);

		AbstractFinanzielleSituationRechner finanzielleSituationRechner = FinanzielleSituationRechnerFactory.getRechner(gesuch);
		assertThat(finanzielleSituationRechner, instanceOf(FinanzielleSituationBernRechner.class));
	}

	@Test
	public void testFinSitTypLuzern() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		AbstractFinanzielleSituationRechner finanzielleSituationRechner = FinanzielleSituationRechnerFactory.getRechner(gesuch);
		assertThat(finanzielleSituationRechner, instanceOf(FinanzielleSituationLuzernRechner.class));
	}

	@Test
	public void testFinSitTypNull() {
		Gesuch gesuch = new Gesuch();
		AbstractFinanzielleSituationRechner finanzielleSituationRechner = FinanzielleSituationRechnerFactory.getRechner(gesuch);
		assertThat(finanzielleSituationRechner, instanceOf(FinanzielleSituationBernRechner.class));
	}
}
