/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import javax.annotation.Nonnull;

public final class RuleUtil {

	private RuleUtil() {
	}

	/**
	 * Berechnet das Datum, ab wann eine Regel aufgrund es übergebenen Datums angewendet werden soll.
	 * Aktuell ist dies der erste Tag des Folgemonats. Auch bei Ereignis am 1. wird der 1. des Folgemonats genommen.
	 * Achtung, dieser Stichtag kommt nicht zwingend schlussendlich zum Einsatz, z.B. bei verspäteter Einreichung
	 * des Gesuchs.
	 */
	@Nonnull
	public static LocalDate getStichtagForEreignis(@Nonnull LocalDate ereignisdatum) {
		return ereignisdatum.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
	}
}
