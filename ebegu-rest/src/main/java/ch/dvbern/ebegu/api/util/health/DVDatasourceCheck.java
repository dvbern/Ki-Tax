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

package ch.dvbern.ebegu.api.util.health;

import java.sql.Connection;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;

import ch.dvbern.ebegu.dbschema.dbprovider.DBProvider;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Health
public class DVDatasourceCheck implements HealthCheck {


	private static final Logger LOGGER = LoggerFactory.getLogger(DVDatasourceCheck.class);

	@Inject
	private DBProvider dbProvider;

	@Override
	public HealthCheckResponse call() {


		final DataSource datasource = dbProvider.getDatasource();

		 boolean datasourceWorks = false;
		if (datasource != null) {
			Connection connection = null;
			try {
				connection = datasource.getConnection();
				final String s = connection.nativeSQL("SELECT 1 + 1;");
				datasourceWorks = s != null;
				connection.close();


			} catch (SQLException e) {
				LOGGER.warn("Datasource check failed" , e);
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException ignore) {
						// ignore
					}
				}
			}
		}

		return HealthCheckResponse.builder().name("dv-datasource-check")
				.withData("datasource", datasourceWorks)
				.state(datasourceWorks)
				.build();
	}
}
