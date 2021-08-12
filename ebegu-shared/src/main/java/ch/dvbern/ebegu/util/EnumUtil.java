/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper-Methoden für Enums.
 */
public final class EnumUtil {


	private EnumUtil() {
		// prevent instantiation
	}

	/**
	 * Gibt true{@code true} zurück wenn der Parameter <tt>toTest</tt> einem der Werte <tt>otherValues</tt>
	 * entspricht
	 */
	@SafeVarargs
	public static <T extends Enum<T>> boolean isOneOf(@Nullable final T toTest, @Nonnull final T... otherValues) {
		return isOneOf(toTest, Arrays.asList(otherValues));
	}

	/**
	 * Gibt true{@code true} zurück wenn der Parameter <tt>toTest</tt> einem der Werte <tt>otherValues</tt>
	 * entspricht
	 */
	public static <T extends Enum<T>> boolean isOneOf(@Nullable final T toTest, @Nonnull final List<T> otherValues) {
		if (toTest == null) {
			return false;
		}
		for (final T enumType : otherValues) {
			if (Objects.equals(toTest, enumType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt true{@code true} zurück wenn der Parameter <tt>toTest</tt> einem der Werte <tt>otherValues</tt>
	 * entspricht
	 */
	@SafeVarargs
	public static <T extends Enum<T>> boolean isOneOf(@Nullable final String toTest, @Nonnull final T... otherValues) {
		return isOneOf(toTest, Arrays.asList(otherValues));
	}

	/**
	 * Gibt true{@code true} zurück wenn der Parameter <tt>toTest</tt> einem der Werte <tt>otherValues</tt>
	 * entspricht
	 */
	public static <T extends Enum<T>> boolean isOneOf(@Nullable final String toTest, @Nonnull final List<T> otherValues) {
		if (toTest == null) {
			return false;
		}
		for (final T enumType : otherValues) {
			if (enumType.name().equals(toTest)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gibt true{@code true} zurück wenn der Parameter <tt>toTest</tt> keinem der Werte <tt>otherValues</tt>
	 * entspricht
	 */
	@SafeVarargs
	public static <T extends Enum<T>> boolean isNoneOf(@Nullable final T toTest, @Nonnull final T... otherValues) {
		return !isOneOf(toTest, otherValues);
	}
}
