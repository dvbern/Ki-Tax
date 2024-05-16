/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.ebegu.util.Constants.DEUTSCH_LOCALE;
import static ch.dvbern.ebegu.util.Constants.FRENCH_LOCALE;
import static org.hamcrest.MatcherAssert.assertThat;

class ResourceBundleEncodingTest {

	public static Stream<Arguments> bundleLocaleSource() {
		var localesDeutsch = MandantIdentifier.getAll().stream().map(new MandantLocaleVisitor(DEUTSCH_LOCALE)::process);
		var localesFrench = MandantIdentifier.getAll().stream().map(new MandantLocaleVisitor(FRENCH_LOCALE)::process);
		Stream<Arguments> serverMessages = Stream.concat(localesFrench, localesDeutsch)
			.map(locale -> Arguments.of(Constants.SERVER_MESSAGE_BUNDLE_NAME, locale));

		Stream<Arguments> validationMessages = ValidationMessageUtil.BUNDLES.keySet()
			.stream()
			.map(locale -> Arguments.of(Constants.VALIDATION_MESSAGE_BUNDLE_NAME, locale));

		return Stream.concat(serverMessages, validationMessages);
	}

	@ParameterizedTest
	@MethodSource("bundleLocaleSource")
	void checkTextResourceEncoding(String bundleName, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);

		bundle.keySet().forEach(key -> {
			String resource = bundle.getString(key);

			// Check for replacement character
			// see https://en.wikipedia.org/wiki/Specials_(Unicode_block)#Replacement_character
			assertThat(
				String.format(
					"key '%s' in bundle '%s' contains invalid UTF-8 chars: '%s'",
					key,
					bundle.getBaseBundleName(),
					resource),
				!resource.contains("�"));

			// Because of the fallback to ISO-8859-1 (see https://docs.oracle.com/javase/9/intl/internationalization-enhancements-jdk-9.htm#GUID-974CF488-23E8-4963-A322-82006A7A14C7)
			// we must also check for UTF-8 umlauts read with ISO-8859-1 encoding:
			//
			// 'ä' stored in UTF-8 is will end up as 'Ã¤' if the file is read as ISO-8859-1.
			// Equally, 'é' will end up as 'Ã©'.
			assertThat(
				String.format(
					"Bundle '%s' with locale '%s' is read with ISO-8859-1 encoding, which means that it contains at least one "
							+ "ISO-8859-1 character",
					bundle.getBaseBundleName(),
					bundle.getLocale()),
				!resource.contains("Ã"));
		});
	}

}
