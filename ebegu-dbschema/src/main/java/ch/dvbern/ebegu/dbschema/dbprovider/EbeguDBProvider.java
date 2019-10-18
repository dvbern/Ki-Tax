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

package ch.dvbern.ebegu.dbschema.dbprovider;

import javax.enterprise.context.Dependent;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Creates the standard ebegu database
 */
@Dependent
public class EbeguDBProvider implements DBProvider {

	public static final String DATASOURCE_JNDI_NAME = "java:/jdbc/ebegu";


	@Override
	public DataSource getDatasource() {

		try {
			// CDI Injection does not work at startup time :-(
			return (DataSource) new InitialContext().lookup(DATASOURCE_JNDI_NAME);
		} catch (NamingException e) {
			final String msg = ("Database Lookup error (missing datasource '" + DATASOURCE_JNDI_NAME) + "')";
			throw new RuntimeException(msg, e);
		}
	}
}
