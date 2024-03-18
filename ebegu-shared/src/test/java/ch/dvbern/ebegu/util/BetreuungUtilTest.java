/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util;

import org.junit.Test;

import static java.math.BigDecimal.*;
import static org.junit.Assert.*;

public class BetreuungUtilTest {

	@Test
	public void getFallnummerFromBGNummer() {
		assertEquals(108L, BetreuungUtil.getFallnummerFromBGNummer("18.000108.003.1.2").longValue());
		assertEquals(123456L, BetreuungUtil.getFallnummerFromBGNummer("18.123456.003.1.2").longValue());
	}

	@Test
	public void getYearFromBGNummer() {
		assertEquals(2018, BetreuungUtil.getYearFromBGNummer("18.000108.003.1.2"));
	}

	@Test
	public void getGemeindeFromBGNummer() {
		assertEquals(3, BetreuungUtil.getGemeindeFromBGNummer("18.000108.003.1.2"));
	}

	@Test
	public void getKindNummerFromBGNummer() {
		assertEquals(1, BetreuungUtil.getKindNummerFromBGNummer("18.000108.003.1.2"));
		assertEquals(2, BetreuungUtil.getKindNummerFromBGNummer("18.000108.003.2.2"));
		assertEquals(88, BetreuungUtil.getKindNummerFromBGNummer("18.000108.003.88.2"));
	}

	@Test
	public void getBetreuungNummerFromBGNummer() {
		assertEquals(2, BetreuungUtil.getBetreuungNummerFromBGNummer("18.000108.003.1.2"));
		assertEquals(1, BetreuungUtil.getBetreuungNummerFromBGNummer("18.000108.003.2.1"));
		assertEquals(99, BetreuungUtil.getBetreuungNummerFromBGNummer("18.000108.003.88.99"));
	}

	@Test
	public void validateBGNummer() {
		assertTrue("18.000108.1.2", BetreuungUtil.validateBGNummer("18.000108.003.1.2"));
		assertTrue("88.999999.77.66", BetreuungUtil.validateBGNummer("88.999999.003.77.66"));
		assertTrue("88.999999.7.66", BetreuungUtil.validateBGNummer("88.999999.003.7.66"));
		assertTrue("88.999999.77.6", BetreuungUtil.validateBGNummer("88.999999.003.77.6"));
		assertFalse("1.000108.1.2", BetreuungUtil.validateBGNummer("1.000108.003.1.2"));
		assertFalse("88.99999.77.66", BetreuungUtil.validateBGNummer("88.99999.003.77.66"));
		assertFalse("88.999999.66", BetreuungUtil.validateBGNummer("88.999999.003.66"));
		assertFalse("88.999999.66", BetreuungUtil.validateBGNummer("88.999999.003.66"));
	}

	@Test
	public void hardcodedMittagstischMultiplierCalculationShouldBeHardcodedValue() {
		assertEquals(BetreuungUtil.getMittagstischMultiplier(), valueOf(0.205));
	}
}
