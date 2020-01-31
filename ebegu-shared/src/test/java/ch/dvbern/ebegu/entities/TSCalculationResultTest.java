/*
 * Copyright (C)  2020 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test fuer Methoden von   TSCalculationResultTest
 */
public class TSCalculationResultTest {

	@Test
	public void testStundenProWocheAndKostenProWoche(){
		final TSCalculationResult tsCalculationResult = new TSCalculationResult();
		tsCalculationResult.setGebuehrProStunde(BigDecimal.valueOf(15.5));
		tsCalculationResult.setBetreuungszeitProWoche(118);
		Assert.assertEquals("01:58", tsCalculationResult.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(new BigDecimal("30.48"), tsCalculationResult.getTotalKostenProWoche());

		tsCalculationResult.setBetreuungszeitProWoche(20);
		Assert.assertEquals("00:20", tsCalculationResult.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(new BigDecimal("5.17"), tsCalculationResult.getTotalKostenProWoche());


		tsCalculationResult.setBetreuungszeitProWoche(480);
		tsCalculationResult.setVerpflegungskosten(new BigDecimal(25));
		Assert.assertEquals("08:00", tsCalculationResult.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(new BigDecimal("149.00"), tsCalculationResult.getTotalKostenProWoche());

		tsCalculationResult.setBetreuungszeitProWoche(40*60);
		tsCalculationResult.setVerpflegungskosten(new BigDecimal(0));
		Assert.assertEquals("40:00", tsCalculationResult.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(new BigDecimal("620.00"), tsCalculationResult.getTotalKostenProWoche());

		tsCalculationResult.setBetreuungszeitProWoche(0);
		tsCalculationResult.setVerpflegungskosten(new BigDecimal(0));
		Assert.assertEquals("00:00", tsCalculationResult.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(new BigDecimal("0.00"), tsCalculationResult.getTotalKostenProWoche());

		tsCalculationResult.setBetreuungszeitProWoche(0);
		tsCalculationResult.setVerpflegungskosten(new BigDecimal(78.935));
		Assert.assertEquals("00:00", tsCalculationResult.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(new BigDecimal("78.94"), tsCalculationResult.getTotalKostenProWoche());

	}


}
