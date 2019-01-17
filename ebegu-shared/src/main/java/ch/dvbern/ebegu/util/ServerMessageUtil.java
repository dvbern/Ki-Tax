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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import org.apache.commons.lang3.StringUtils;

/**
 * Util welche einfach erlaubt eine Message aus dem server Seitigen Message Bundle zu lesen
 */
public final class ServerMessageUtil {

	private static final ResourceBundle bundle_de = ResourceBundle.getBundle(Constants.SERVER_MESSAGE_BUNDLE_NAME, Constants.DEFAULT_LOCALE);
	private static final ResourceBundle bundle_fr = ResourceBundle.getBundle(Constants.SERVER_MESSAGE_BUNDLE_NAME, Constants.FRENCH_LOCALE);

	private ServerMessageUtil() {
	}

	private static ResourceBundle selectBundleToUse(Locale locale) {
		if (locale.getLanguage().equalsIgnoreCase("FR")) {
			return bundle_fr;
		}
		return bundle_de;
	}

	public static String getMessage(String key, Locale locale) {
		ResourceBundle bundle = selectBundleToUse(locale);
		return readStringFromBundleOrReturnKey(bundle, key);
	}

	/**
	 * Da wir aller wahrscheinlichkeit eine Exceptionmessage uebersetzten wollen macht es nicht gross Sinn hier falls ein
	 * Key fehlt MissingResourceException werfen zu lassen.
	 */
	private static String readStringFromBundleOrReturnKey(ResourceBundle bundle, String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException ex) {
			return "???" + key + "???";
		}
	}

	public static String getMessage(String key, Locale locale, Object... args) {
		return MessageFormat.format(getMessage(key, locale), args);
	}

	/**
	 * Uebersetzt einen Enum-Wert
	 */
	@Nonnull
	public static String translateEnumValue(@Nullable final Enum<?> e, Locale locale, Object... args) {
		if (e == null) {
			return StringUtils.EMPTY;
		}
		return getMessage(getKey(e), locale, args);
	}

	/**
	 * Gibt den Bundle-Key für einen Enum-Wert zurück.
	 * Schema: Klassenname_enumWert, also z.B. CodeArtType_MANDANT
	 */
	@Nonnull
	private static String getKey(@Nonnull Enum<?> e) {
		return e.getClass().getSimpleName() + '_' + e.name();
	}
}
