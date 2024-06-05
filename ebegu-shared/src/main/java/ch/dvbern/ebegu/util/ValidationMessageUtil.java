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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import ch.dvbern.ebegu.i18n.LocaleThreadLocal;

/**
 * Util welche einfach erlaubt eine Validation-Message zu übersetzen. Es wird immer die Sprache verwendet, welche vom
 * Client gekommen ist (aus LocaleThreadLocal.get())
 */
public final class ValidationMessageUtil {

	static final Map<String, ResourceBundle> BUNDLES = new HashMap<>();

	static {
		BUNDLES.put(Constants.DEUTSCH_LOCALE.getLanguage(), ResourceBundle.getBundle(Constants.VALIDATION_MESSAGE_BUNDLE_NAME, Constants.DEUTSCH_LOCALE));
		BUNDLES.put(Constants.FRENCH_LOCALE.getLanguage(), ResourceBundle.getBundle(Constants.VALIDATION_MESSAGE_BUNDLE_NAME, Constants.FRENCH_LOCALE));
	}

	private ValidationMessageUtil() {
	}

	public static String getMessage(String key) {
		Locale clientLocale = LocaleThreadLocal.get();
		ResourceBundle clientLocaleBundle = BUNDLES.get(clientLocale.getLanguage());
		if (clientLocaleBundle == null) {
			clientLocaleBundle = BUNDLES.get(Constants.DEFAULT_LOCALE.getLanguage());
		}
		return readStringFromBundleOrReturnKey(clientLocaleBundle, key);
	}

	/**
	 * Da wir aller wahrscheinlichkeit eine Exceptionmessage uebersetzten wollen macht es nicht gross Sinn hier falls ein
	 * Key fehlt MissingResourceException werfen zu lassen.
	 */
	private static String readStringFromBundleOrReturnKey(ResourceBundle bundle, String key) {
		try {
			if (key.startsWith("{") && key.endsWith("}")) {
				key = key.substring(1, key.length()-1);
			}
			return bundle.getString(key);
		} catch (MissingResourceException ignore) {
			return "???" + key + "???";
		}
	}
}
