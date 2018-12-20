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

package ch.dvbern.ebegu.i18n;

import java.util.Locale;

/**
 * * A {@link ThreadLocal}um zu speichern was fuer eine Sprache wir verwenden fuer den aktuellen Request
 */
public final class LocaleThreadLocal {

	public static final ThreadLocal<Locale> THREAD_LOCAL = new ThreadLocal<>();

	private LocaleThreadLocal() {
	}

	public static Locale get() {
		return (THREAD_LOCAL.get() == null) ? Locale.getDefault() : THREAD_LOCAL.get();
	}

	public static void set(Locale locale) {
		THREAD_LOCAL.set(locale);
	}

	public static void unset() {
		THREAD_LOCAL.remove();
	}
}
