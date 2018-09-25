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

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

public final class StreamsUtil {
	private StreamsUtil() {
	}

	public static <T> BinaryOperator<T> toOnlyElement() {
		return toOnlyElementThrowing(() -> new EbeguRuntimeException("toOnlyElement", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS, ""));
	}

	public static <T, E extends RuntimeException> BinaryOperator<T>
	toOnlyElementThrowing(Supplier<E> exception) {
		return (element, otherElement) -> {
			throw exception.get();
		};
	}
}
