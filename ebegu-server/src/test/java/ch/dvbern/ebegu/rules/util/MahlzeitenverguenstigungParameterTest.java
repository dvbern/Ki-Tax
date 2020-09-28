/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.util;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

public class MahlzeitenverguenstigungParameterTest {

	@Test
	public void mahlzeitenverguenstigungVerguenstigungEffektivTest() {
		MahlzeitenverguenstigungParameter params = new MahlzeitenverguenstigungParameter(
			true,
			false,
			BigDecimal.valueOf(51000),
			BigDecimal.valueOf(70000),
			BigDecimal.valueOf(6),
			BigDecimal.valueOf(3),
			BigDecimal.valueOf(0),
			BigDecimal.valueOf(2)
		);


		BigDecimal verguenstigung = params.getVerguenstigungProMahlzeitWithParam(BigDecimal.valueOf(50000), false);

		Assert.assertEquals(verguenstigung, BigDecimal.valueOf(6));

		BigDecimal verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(9),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(6));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(8),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(6));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(7),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(5));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(6),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(4));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(5),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(3));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(4),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(2));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(3),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(1));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(2),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(0));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(1),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(0));

		verguenstigungEffektiv = params.getVerguenstigungEffektiv(verguenstigung, BigDecimal.valueOf(0),
			params.getMinimalerElternbeitragMahlzeit());

		Assert.assertEquals(verguenstigungEffektiv, BigDecimal.valueOf(0));
	}
}
