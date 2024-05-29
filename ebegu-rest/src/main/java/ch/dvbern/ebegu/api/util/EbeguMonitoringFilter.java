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

package ch.dvbern.ebegu.api.util;

import net.bull.javamelody.MonitoringFilter;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Created by imanol on 02.03.16.
 *
 * In REST-Resourcen enthaelt die URL immer auch irgendeine Business-ID, z.B. /api/v1/kinder/123/foo/bar
 * Fuers Monitoring ist die ID (hier: 123) aber nicht relevant und soll durch einen Platzhalter ersetzt werden.
 */
public class EbeguMonitoringFilter extends MonitoringFilter {
	/**
	 * Matches search strings entered by a user and encoded as URI component. This means most characters are
	 * represented by their UTF-8 escape sequence like %20 for a space.
	 */
	private static final String SEARCH_PATTERN = "[a-zA-Z0-9%*()_!.\\-'{}@]+";

	private static final Pattern ID_PATTERN = Pattern.compile("/\\d+");
	private static final Pattern UUID_PATTERN = Pattern.compile("/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", Pattern.CASE_INSENSITIVE);
	private static final Pattern EMAIL_PATTERN = Pattern.compile("/[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}");
	private static final Pattern QUICKSEARCH_PATTERN = Pattern.compile("search/quicksearch/" + SEARCH_PATTERN);
	private static final Pattern USERNAME_SEARCH_PATTERN = Pattern.compile("username/" + SEARCH_PATTERN);
	private static final Pattern EINSTELLUNG_PATTERN = Pattern.compile("key/[A-Z_]+");

	@Nonnull
	@Override
	protected String getRequestName(@Nonnull HttpServletRequest request) {
		String defaultName = super.getRequestName(request);
		String name = UUID_PATTERN.matcher(defaultName).replaceAll("/{uuid}");
		name = ID_PATTERN.matcher(name).replaceAll("/{id}");
		name = QUICKSEARCH_PATTERN.matcher(name).replaceAll("search/quicksearch/{searchString}");
		name = EMAIL_PATTERN.matcher(name).replaceAll("/{email}");
		name = USERNAME_SEARCH_PATTERN.matcher(name).replaceAll("username/{searchString}");
		name = EINSTELLUNG_PATTERN.matcher(name).replaceAll("key/{einstellungString}");
		return name;
	}
}

