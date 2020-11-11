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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import javax.annotation.Nullable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests für MathUtil
 */
public class MathUtilTest {

	private static final RoundingMode DFLT_ROUNDING = RoundingMode.HALF_UP;
	private static final int DFLT_SCALE = 2;

	public static void assertCompare(@Nullable BigDecimal ref, @Nullable BigDecimal other) {
		String msg = "ref != other: " + ref + " != " + other;
		assertTrue(msg, ref == null ? other == null : other != null);
		if (ref != null) {
			assertEquals(msg, 0, ref.compareTo(other));
		}
	}

	@Test
	public void testFrom_long() {
		assertNull(MathUtil.DEFAULT.from((Long) null));

		BigDecimal val = MathUtil.DEFAULT.from(123L);
		assertEquals(5, val.precision());
		assertEquals(DFLT_SCALE, val.scale());
		assertCompare(val, new BigDecimal(123L).setScale(DFLT_SCALE, DFLT_ROUNDING));
	}

	@Test
	public void testFrom_Double() {
		assertNull(MathUtil.DEFAULT.from((Double) null));

		BigDecimal val = MathUtil.DEFAULT.from(123.45);
		assertEquals(5, val.precision());
		assertEquals(DFLT_SCALE, val.scale());

		assertCompare(val, BigDecimal.valueOf(123.45).setScale(DFLT_SCALE, DFLT_ROUNDING));
	}

	@Test
	public void testFrom_BigDecimal() {
		assertNull(MathUtil.DEFAULT.from((BigDecimal) null));

		BigDecimal val = MathUtil.DEFAULT.from(BigDecimal.valueOf(123.45).setScale(DFLT_SCALE, DFLT_ROUNDING));
		assertEquals(5, val.precision());
		assertEquals(DFLT_SCALE, val.scale());

		assertCompare(val, BigDecimal.valueOf(123.45).setScale(DFLT_SCALE, DFLT_ROUNDING));
	}

	@Test
	public void testFrom_BigInteger() {
		assertNull(MathUtil.DEFAULT.from((BigInteger) null));

		BigDecimal val = MathUtil.DEFAULT.from(BigInteger.valueOf(123));
		assertEquals(5, val.precision());
		assertEquals(DFLT_SCALE, val.scale());

		assertCompare(val, BigDecimal.valueOf(123).setScale(DFLT_SCALE, DFLT_ROUNDING));
	}

	@Test(expected = PrecisionTooLargeException.class)
	public void test_LargeMathUtil1() {
		MathUtil.DEFAULT.from(new BigDecimal("12345678901234567890"));
	}

	@Test
	public void testAdd() {
		BigDecimal val = MathUtil.DEFAULT.add(MathUtil.DEFAULT.from(123L), MathUtil.DEFAULT.from(456L));
		assertCompare(val, BigDecimal.valueOf(123L + 456L).setScale(DFLT_SCALE, DFLT_ROUNDING));
		assertCompare(BigDecimal.valueOf(579.00), val);
		assertEquals(5, val.precision());
		assertEquals(DFLT_SCALE, val.scale());
	}

	@Test
	public void testSubtract() {
		BigDecimal val = MathUtil.DEFAULT.subtract(MathUtil.DEFAULT.from(456L), MathUtil.DEFAULT.from(123L));
		assertCompare(val, BigDecimal.valueOf(456L - 123L).setScale(DFLT_SCALE, DFLT_ROUNDING));
		assertCompare(BigDecimal.valueOf(333.00), val);
		assertEquals(5, val.precision());
		assertEquals(DFLT_SCALE, val.scale());
	}

	@Test
	public void testMultiply() {
		BigDecimal val = MathUtil.DEFAULT.multiply(MathUtil.DEFAULT.from(123L), MathUtil.DEFAULT.from(456L));
		assertCompare(val, BigDecimal.valueOf(123L * 456L).setScale(DFLT_SCALE, DFLT_ROUNDING));
		assertCompare(BigDecimal.valueOf(56088.00), val);
		assertEquals(7, val.precision());
		assertEquals(DFLT_SCALE, val.scale());
	}

	@Test
	public void testMultiplyWithManyArguments() {
		BigDecimal val = MathUtil.DEFAULT.multiply(MathUtil.DEFAULT.from(123L), MathUtil.DEFAULT.from(456L), MathUtil.DEFAULT.from(789L));
		assertCompare(val, BigDecimal.valueOf(123L * 456L * 789L).setScale(DFLT_SCALE, DFLT_ROUNDING));
		assertCompare(BigDecimal.valueOf(44253432.00), val);
		assertEquals(10, val.precision());
		assertEquals(DFLT_SCALE, val.scale());
	}

	@Test
	public void testDivide() {
		BigDecimal val = MathUtil.DEFAULT.divide(MathUtil.DEFAULT.from(123L), MathUtil.DEFAULT.from(456L));
		assertCompare(val, BigDecimal.valueOf(123.0 / 456.0).setScale(DFLT_SCALE, DFLT_ROUNDING));
		// 0.27 (gerundet von 0.2697368421)
		assertCompare(BigDecimal.valueOf(0.27), val);
		assertEquals(2, val.precision());
		assertEquals(DFLT_SCALE, val.scale());
	}

	@Test
	public void testRoundToFrankenRappen() {
		assertEquals(new BigDecimal("1.05"), MathUtil.roundToFrankenRappen(new BigDecimal("1.051")));
		assertEquals(new BigDecimal("1.10"), MathUtil.roundToFrankenRappen(new BigDecimal("1.075")));
		assertEquals(new BigDecimal("1.10"), MathUtil.roundToFrankenRappen(new BigDecimal("1.0749")));
		assertEquals(new BigDecimal("1.05"), MathUtil.roundToFrankenRappen(new BigDecimal("1.0744")));
	}

	@Test
	public void testRoundIntToTens() {
		assertEquals(0, MathUtil.roundIntToTens(-1));
		assertEquals(0, MathUtil.roundIntToTens(0));
		assertEquals(0, MathUtil.roundIntToTens(1));
		assertEquals(10, MathUtil.roundIntToTens(5)); // special case giving errors in Java6
		assertEquals(10, MathUtil.roundIntToTens(10));
		assertEquals(10, MathUtil.roundIntToTens(11));
		assertEquals(10, MathUtil.roundIntToTens(14));
		assertEquals(20, MathUtil.roundIntToTens(15));
		assertEquals(20, MathUtil.roundIntToTens(16));
		assertEquals(20, MathUtil.roundIntToTens(19));
		assertEquals(152360, MathUtil.roundIntToTens(152362));
		assertEquals(152370, MathUtil.roundIntToTens(152365));
	}

	@Test
	public void testRoundIntToFives() {
		assertEquals(0, MathUtil.roundIntToFives(-1));
		assertEquals(0, MathUtil.roundIntToFives(0));
		assertEquals(0, MathUtil.roundIntToFives(1));
		assertEquals(10, MathUtil.roundIntToFives(8)); // special case giving errors in Java6
		assertEquals(10, MathUtil.roundIntToFives(9));
		assertEquals(10, MathUtil.roundIntToFives(10));
		assertEquals(10, MathUtil.roundIntToFives(11));
		assertEquals(10, MathUtil.roundIntToFives(12));
		assertEquals(15, MathUtil.roundIntToFives(13));
		assertEquals(15, MathUtil.roundIntToFives(14));
		assertEquals(15, MathUtil.roundIntToFives(17));
		assertEquals(152360, MathUtil.roundIntToFives(152362));
		assertEquals(152365, MathUtil.roundIntToFives(152363));
	}

	@Test
	public void testRoundToNearestQuarter() {
		assertEquals(new BigDecimal("0.00"), MathUtil.roundToNearestQuarter(new BigDecimal(0)));
		assertEquals(new BigDecimal("0.00"), MathUtil.roundToNearestQuarter(new BigDecimal("0.12")));
		assertEquals(new BigDecimal("0.25"), MathUtil.roundToNearestQuarter(new BigDecimal("0.13")));
		assertEquals(new BigDecimal("0.25"), MathUtil.roundToNearestQuarter(new BigDecimal("0.25")));
		assertEquals(new BigDecimal("1.00"), MathUtil.roundToNearestQuarter(new BigDecimal("0.9")));
		assertEquals(new BigDecimal("500.00"), MathUtil.roundToNearestQuarter(new BigDecimal("499.994")));
	}

	@Test
	public void testIsClose() {
		assertTrue(MathUtil.isClose(BigDecimal.valueOf(1.01), BigDecimal.valueOf(1.02), BigDecimal.valueOf(0.01)));
		assertTrue(MathUtil.isClose(BigDecimal.valueOf(1.01), BigDecimal.valueOf(1.01), BigDecimal.ZERO));
		assertTrue(MathUtil.isClose(BigDecimal.valueOf(1.01), BigDecimal.valueOf(1.009), BigDecimal.valueOf(0.01)));
		assertTrue(MathUtil.isClose(BigDecimal.valueOf(1.01), BigDecimal.valueOf(5), BigDecimal.TEN));
		assertFalse(MathUtil.isClose(BigDecimal.valueOf(1.01), BigDecimal.valueOf(1.02), BigDecimal.valueOf(0.001)));
		assertFalse(MathUtil.isClose(BigDecimal.valueOf(3.01), BigDecimal.valueOf(1.02), BigDecimal.valueOf(1.01)));
	}

	@Test
	public void roundToFivesUp() {
		assertEquals(MathUtil.GANZZAHL.from(5.00), MathUtil.roundToFivesUp(MathUtil.DEFAULT.from(4.99)));
		assertEquals(MathUtil.GANZZAHL.from(5.00), MathUtil.roundToFivesUp(MathUtil.DEFAULT.from(1.2)));
		assertEquals(MathUtil.GANZZAHL.from(5.00), MathUtil.roundToFivesUp(MathUtil.DEFAULT.from(0.01)));
		assertEquals(MathUtil.GANZZAHL.from(0.00), MathUtil.roundToFivesUp(MathUtil.DEFAULT.from(0)));
		assertEquals(MathUtil.GANZZAHL.from(5.00), MathUtil.roundToFivesUp(MathUtil.DEFAULT.from(5.00)));
		assertEquals(MathUtil.GANZZAHL.from(10.00), MathUtil.roundToFivesUp(MathUtil.DEFAULT.from(5.01)));
	}
}
