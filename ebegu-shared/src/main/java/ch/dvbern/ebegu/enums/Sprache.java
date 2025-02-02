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

package ch.dvbern.ebegu.enums;

import java.util.Locale;

import ch.dvbern.ebegu.util.Constants;

/**
 * Enum fuer die Sprache
 */
public enum Sprache {

	DEUTSCH(Constants.DEUTSCH_LOCALE),
	FRANZOESISCH(Constants.FRENCH_LOCALE);

	private Locale locale;

	Sprache(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	public static Sprache fromLocale(Locale locale) {
		if (locale.equals(Locale.FRENCH)) {
			return FRANZOESISCH;
		}
		return Sprache.DEUTSCH;
	}
}
