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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.Contract;

public enum MathUtil {

	GANZZAHL(19, 0, RoundingMode.HALF_UP),
	EINE_NACHKOMMASTELLE(19, 1, RoundingMode.HALF_UP),
	ZWEI_NACHKOMMASTELLE(19, 2, RoundingMode.HALF_UP),
	VIER_NACHKOMMASTELLE(19, 4, RoundingMode.HALF_DOWN),
	DEFAULT(19, 2, RoundingMode.HALF_UP),
	EXACT(30, 10, RoundingMode.HALF_UP);

	private final int precision;
	private final int scale;

	public static final BigDecimal HUNDRED = BigDecimal.valueOf(100, 0);
	public static final BigDecimal ROUNDING_INCREMENT = new BigDecimal("0.05");

	@Nonnull
	private final RoundingMode roundingMode;

	MathUtil(int precision, int scale, @Nonnull RoundingMode roundingMode) {
		this.precision = precision;
		this.scale = scale;
		this.roundingMode = roundingMode;
	}

	public static boolean isEven(int number) {
		return number % 2 == 0;
	}

	@Nonnull
	private BigDecimal validatePrecision(@Nonnull BigDecimal value) {
		if (value.precision() > precision) {
			throw new PrecisionTooLargeException(value, precision);
		}
		return value;
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null->null; !null->!null")
	public BigDecimal from(@Nullable BigDecimal src) {
		if (src == null) {
			return null;
		}
		return fromNullSafe(src);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nonnull
	public BigDecimal fromNullSafe(@Nonnull BigDecimal src) {
		BigDecimal val = BigDecimal.ZERO
			.setScale(scale, roundingMode)
			.add(src)
			.setScale(scale, roundingMode);
		return validatePrecision(val);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null->null; !null->!null")
	public BigDecimal from(@Nullable BigInteger src) {
		if (src == null) {
			return null;
		}
		BigDecimal val = new BigDecimal(src)
			.setScale(scale, roundingMode);
		return validatePrecision(val);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null->null; !null->!null")
	public BigDecimal from(@Nullable Long src) {
		if (src == null) {
			return null;
		}
		BigDecimal val = new BigDecimal(src)
			.setScale(scale, roundingMode);
		return validatePrecision(val);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nonnull
	public BigDecimal fromNullSafe(@Nonnull Integer src) {
		BigDecimal val = new BigDecimal(src)
			.setScale(scale, roundingMode);
		return validatePrecision(val);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nonnull
	public BigDecimal fromNullSafe(@Nonnull String src) {
		BigDecimal val = new BigDecimal(src)
			.setScale(scale, roundingMode);
		return validatePrecision(val);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null->null; !null->!null")
	public BigDecimal from(@Nullable Integer src) {
		if (src == null) {
			return null;
		}
		return fromNullSafe(src);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null->null; !null->!null")
	public BigDecimal from(@Nullable String src) {
		if (src == null) {
			return null;
		}
		return fromNullSafe(src);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null->null; !null->!null")
	public BigDecimal from(@Nullable Double src) {
		if (src == null) {
			return null;
		}
		return fromNullSafe(src);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nonnull
	public BigDecimal fromNullSafe(@Nonnull Double src) {
		BigDecimal val = new BigDecimal(String.valueOf(src))
			.setScale(scale, roundingMode);
		return validatePrecision(val);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nonnull
	@Contract("null,null->null; null,!null->null; !null,null->null; !null,!null->!null")
	public BigDecimal addNullSafe(@Nonnull BigDecimal value, @Nullable BigDecimal augment) {
		if (augment == null) {
			return value;
		}
		BigDecimal result = value
			.add(augment)
			.setScale(scale, roundingMode);
		return validatePrecision(result);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null,null->null; null,!null->null; !null,null->null; !null,!null->!null")
	public BigDecimal add(@Nullable BigDecimal value, @Nullable BigDecimal augment) {
		if (value == null || augment == null) {
			return null;
		}
		return addNullSafe(value, augment);
	}

	/**
	 * adds augement parameters to value, null values are treated as zero
	 *
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nonnull
	public BigDecimal addNullSafe(@Nonnull BigDecimal value, @Nonnull BigDecimal... augment) {
		BigDecimal result = value;
		for (BigDecimal valueToAdd : augment) {
			if (valueToAdd != null) {
				result = result
					.add(valueToAdd)
					.setScale(scale, roundingMode);
			}
		}
		return validatePrecision(result);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nonnull
	public BigDecimal subtractNullSafe(@Nonnull BigDecimal value, @Nullable BigDecimal subtrahend) {
		if (subtrahend == null) {
			return value;
		}
		BigDecimal result = value
			.subtract(subtrahend)
			.setScale(scale, roundingMode);
		return validatePrecision(result);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null,null->null; null,!null->null; !null,null->null; !null,!null->!null")
	public BigDecimal subtract(@Nullable BigDecimal value, @Nullable BigDecimal subtrahend) {
		if (value == null || subtrahend == null) {
			return null;
		}
		return subtractNullSafe(value, subtrahend);
	}

	@Nonnull
	public BigDecimal subtractMultiple(@Nonnull BigDecimal value, @Nonnull BigDecimal... subtrahends) {
		BigDecimal result = value;
		for (BigDecimal subtrahend : subtrahends) {
			if (subtrahend != null) {
				result = subtract(result, subtrahend);
			}
		}
		return result;
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null,null->null; null,!null->null; !null,null->null; !null,!null->!null")
	public BigDecimal multiply(@Nullable BigDecimal value, @Nullable BigDecimal multiplicand) {
		if (value == null || multiplicand == null) {
			return null;
		}
		return multiplyNullSafe(value, multiplicand);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nonnull
	public BigDecimal multiplyNullSafe(@Nonnull BigDecimal value, @Nonnull BigDecimal multiplicand) {
		BigDecimal result = value
			.multiply(multiplicand)
			.setScale(scale, roundingMode);
		return validatePrecision(result);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting values exceeds the defined precision
	 */
	@Nullable
	public BigDecimal multiply(@Nullable BigDecimal... values) {
		if (values == null || values.length == 0) {
			return null;
		}
		return multiplyNullSafe(values);
	}

	@Nonnull
	public BigDecimal multiplyNullSafe(@Nonnull BigDecimal... values) {
		BigDecimal result = Arrays.stream(values)
			.filter(Objects::nonNull)
			.reduce(BigDecimal.ONE, this::multiplyNullSafe);

		return validatePrecision(result);
	}

	@Nonnull
	public BigDecimal divideNullSafe(@Nonnull BigDecimal dividend, @Nonnull BigDecimal divisor) {

		if (0 == BigDecimal.ZERO.compareTo(divisor)) {
			throw new IllegalArgumentException("Divide by zero: " + dividend + '/' + divisor);
		}
		BigDecimal result = dividend.divide(divisor, scale, roundingMode);
		return validatePrecision(result);
	}

	/**
	 * @throws PrecisionTooLargeException if the resulting value exceeds the defined precision
	 */
	@Nullable
	@Contract("null,null->null; null,!null->null; !null,null->null; !null,!null->!null")
	public BigDecimal divide(@Nullable BigDecimal dividend, @Nullable BigDecimal divisor) {
		if (dividend == null || divisor == null) {
			return null;
		}
		return divideNullSafe(dividend, divisor);
	}

	/**
	 * Konvertiert eine Prozentzahl (z.B. 34%) in eine Bruchzahl (z.B. 0.34), i.E.: dividiert durch 100
	 */
	@Nullable
	@Contract("null -> null; !null -> !null")
	public BigDecimal pctToFraction(@Nullable BigDecimal value) {
		if (value == null) {
			return null;
		}
		return divide(value, HUNDRED);
	}

	/**
	 * Konvertiert eine Bruchzahl (z.B. 0.34) in eine Prozentzahl (z.B. 34%), i.E.: multipliziert mit 100
	 */
	@Nullable
	@Contract("null -> null; !null -> !null")
	public BigDecimal fractionToPct(@Nullable BigDecimal value) {
		if (value == null) {
			return null;
		}
		return multiply(value, HUNDRED);
	}

	/**
	 * Rundet einen BigDecimal auf 2 Nachkommastellen und auf 5 Rappen.
	 */
	@Nonnull
	public static BigDecimal roundToFrankenRappen(@Nullable BigDecimal amount) {
		if (amount == null) {
			return BigDecimal.ZERO;
		}
		// Ab welcher Nachkommastelle soll gerundet werden???
		// Wir runden zuerst die vierte auf die dritte...
		BigDecimal roundedUp = amount.multiply(MathUtil.HUNDRED).divide(MathUtil.HUNDRED, 3, BigDecimal.ROUND_HALF_UP);
		// ... dann davon auf 5-Rappen runden
		BigDecimal divided = GANZZAHL.divide(roundedUp, ROUNDING_INCREMENT);
		return DEFAULT.multiply(divided, ROUNDING_INCREMENT);
	}

	/**
	 * Vergleicht zwei optionale BigDecimal.
	 *
	 * @return TRUE, wenn beide Werte NULL sind, oder wenn beide BigDecimal (via compareTo) identisch sind. Sonst FALSE
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static boolean isSame(@Nonnull Optional<BigDecimal> a, @Nonnull Optional<BigDecimal> b) {
		return a.isPresent() && b.isPresent() && a.get().compareTo(b.get()) == 0 || !a.isPresent() && !b.isPresent();
	}

	/**
	 * Vergleicht zwei BigDecimal.
	 *
	 * @return TRUE, wenn beide Werte NULL sind, oder wenn beide BigDecimal (via compareTo) identisch sind. Sonst FALSE
	 */
	public static boolean isSame(@Nullable BigDecimal a, @Nullable BigDecimal b) {
		return (a == null && b == null) || (a != null && b != null && (a.compareTo(b) == 0));
	}

	/**
	 * Vergleicht zwei BigDecimal. Null wird gleich wie 0 behandelt!
	 */
	public static boolean isSameWithNullAsZero(@Nullable BigDecimal a, @Nullable BigDecimal b) {
		if (a == null) {
			a = BigDecimal.ZERO;
		}
		if (b == null) {
			b = BigDecimal.ZERO;
		}
		return a.compareTo(b) == 0;
	}

	/**
	 * Rundet die eingegebene Nummer in 10er Schritten.
	 * Beispiel
	 * 20 bis 24 = 20
	 * 25 bis 29 = 30
	 */
	public static int roundIntToTens(int value) {
		return (int) (Math.round((double) value / 10) * 10);
	}

	/**
	 * Rundet die eingegebene Nummer in 5er Schritten.
	 * Beispiel
	 * 20 bis 22 = 20
	 * 23 bis 25 = 25
	 */
	public static int roundIntToFives(int value) {
		return (int) (Math.round((double) value / 5) * 5);
	}

	/**
	 * rundet auf die naechste Ganzzahl groesser gleich 0
	 */
	public static BigDecimal positiveNonNullAndRound(@Nullable BigDecimal value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		// Returns the maximum of this BigDecimal and val.
		value = value.setScale(0, RoundingMode.HALF_UP);
		return value.max(BigDecimal.ZERO);
	}

	/**
	 * rundet auf die naechste Ganzzahl groesser gleich 0
	 */
	public static BigDecimal positiveNonNull(BigDecimal value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		return value.max(BigDecimal.ZERO);
	}

	public static boolean isPositive(@Nonnull BigDecimal value) {
		return value.compareTo(BigDecimal.ZERO) > 0;
	}

	public static boolean isNegative(@Nonnull BigDecimal value) {
		return value.compareTo(BigDecimal.ZERO) < 0;
	}

	@Nonnull
	public static BigDecimal toOneKommastelle(@Nonnull BigDecimal value) {
		return MathUtil.EINE_NACHKOMMASTELLE.from(value);
	}

	@Nonnull
	public static BigDecimal toTwoKommastelle(@Nonnull BigDecimal value) {
		return MathUtil.DEFAULT.from(value);
	}

	@Nonnull
	public static BigDecimal minimum(@Nonnull BigDecimal value1, @Nonnull BigDecimal minimum) {
		if (value1.compareTo(minimum) > 0) {
			return value1;
		}
		return minimum;
	}

	@Nonnull
	public static BigDecimal maximum(@Nonnull BigDecimal value1, @Nonnull BigDecimal maximum) {
		if (value1.compareTo(maximum) < 0) {
			return value1;
		}
		return maximum;
	}

	@Nonnull
	public static BigDecimal minimumMaximum(@Nonnull BigDecimal value, @Nonnull BigDecimal minimum, @Nonnull BigDecimal maximum) {
		value = minimum(value, minimum);
		value = maximum(value, maximum);
		return value;
	}

	@Nonnull
	public static BigDecimal roundToNearestQuarter(@Nonnull BigDecimal value) {
		value = MathUtil.GANZZAHL.multiply(value, new BigDecimal(4));
		return MathUtil.ZWEI_NACHKOMMASTELLE.divide(value, new BigDecimal(4));
	}

	/**
	 * Returns TRUE when {@code value1} is equal to {@code value2} within a range of +/- {@code error}.
	 * The comparison for equality is done by BigDecimals {@link BigDecimal#compareTo(BigDecimal)} method.
	 */
	public static boolean isClose(@Nonnull BigDecimal value1, @Nonnull BigDecimal value2, @Nonnull BigDecimal error) {
		BigDecimal actualDelta = value1.subtract(value2, MathContext.DECIMAL128)
			.abs()
			.subtract(error, MathContext.DECIMAL128)
			.stripTrailingZeros();

		return actualDelta.compareTo(BigDecimal.ZERO) <= 0;
	}
}
