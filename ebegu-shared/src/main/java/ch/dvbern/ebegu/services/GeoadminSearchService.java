/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.services;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.geoadmin.JaxWohnadresse;

import static com.google.common.base.Preconditions.checkNotNull;

public interface GeoadminSearchService {

	enum LANG {
		de,
		fr,
		it,
		rm,
		en;

		@Nonnull
		public static LANG fromLocale(@Nonnull Locale locale) {
			checkNotNull(locale);

			String language = locale.getLanguage();

			try {
				LANG lang = LANG.valueOf(language);

				return lang;
			} catch (IllegalArgumentException ignored) {
				// default LANG as per API spec
				return de;
			}
		}
	}

	/**
	 * @param lang Sprachen die vom Service unterstuetzt werden
	 * @param searchText Nicht-Leerer Text, maximal 10 Woerter
	 */
	@Nonnull
	List<JaxWohnadresse> findWohnadressenBySearchText(@Nonnull LANG lang, @Nonnull String searchText);

	@Nonnull
	List<JaxWohnadresse> findWohnadressenByStrasseAndOrt(@Nonnull LANG lang, @Nonnull String strasse, @Nonnull String nr, @Nonnull String plz);
}
