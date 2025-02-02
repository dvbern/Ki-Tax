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

import ch.dvbern.ebegu.entities.Mandant;
import org.apache.commons.lang3.StringUtils;

/**
 * Util welche einfach erlaubt eine Message aus dem server Seitigen Message Bundle zu lesen
 */
public final class ServerMessageUtil {

	private static final MandantLocaleVisitor MANDANT_LOCALE_VISITOR_DE = new MandantLocaleVisitor(Constants.DEUTSCH_LOCALE);
	private static final MandantLocaleVisitor MANDANT_LOCALE_VISITOR_FR = new MandantLocaleVisitor(Constants.FRENCH_LOCALE);

	private ServerMessageUtil() {
	}

	private static ResourceBundle selectBundleToUse(Locale locale, Mandant mandant) {
		if (locale.getLanguage().equalsIgnoreCase("FR")) {
			return ResourceBundle.getBundle(Constants.SERVER_MESSAGE_BUNDLE_NAME, MANDANT_LOCALE_VISITOR_FR.process(mandant));
		}
		return ResourceBundle.getBundle(Constants.SERVER_MESSAGE_BUNDLE_NAME, MANDANT_LOCALE_VISITOR_DE.process(mandant));
	}

	public static String getMessage(String key, Locale locale, Mandant mandant) {
		ResourceBundle bundle = selectBundleToUse(locale, mandant);
		return readStringFromBundleOrReturnKey(bundle, key);
	}

	/**
	 * Da wir aller wahrscheinlichkeit eine Exceptionmessage uebersetzten wollen macht es nicht gross Sinn hier falls ein
	 * Key fehlt MissingResourceException werfen zu lassen.
	 */
	private static String readStringFromBundleOrReturnKey(ResourceBundle bundle, String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException ignore) {
			return "???" + key + "???";
		}
	}

	public static String getMessage(String key, Locale locale, Mandant mandant, Object... args) {
		return MessageFormat.format(getMessage(key, locale, mandant), args);
	}

	/**
	 * Uebersetzt einen Enum-Wert
	 */
	@Nonnull
	public static String translateEnumValue(@Nullable final Enum<?> e, Locale locale, Mandant mandant, Object... args) {
		if (e == null) {
			return StringUtils.EMPTY;
		}
		return getMessage(getKey(e), locale, mandant, args);
	}

	/**
	 * Gibt den Bundle-Key für einen Enum-Wert zurück.
	 * Schema: Klassenname_enumWert, also z.B. CodeArtType_MANDANT
	 */
	@Nonnull
	private static String getKey(@Nonnull Enum<?> e) {
		return e.getDeclaringClass().getSimpleName() + '_' + e.name();
	}
}
