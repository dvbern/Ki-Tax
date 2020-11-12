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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.util.health;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Health
public class DVBatchJobCheck implements HealthCheck {

	private static final String DATASOURCE_JNDI_NAME = "java:/jdbc/ebegu_ejb_meta";
	private static final String TIMER_STATE_COLUMN = "TIMER_STATE";
	private static final String IN_TIMEOUT_STATE = "IN_TIMEOUT";
	private static final String PREVIOUS_RUN_COLUMN = "PREVIOUS_RUN";
	private static final String TIMEOUT_METHOD_NAME_COLUMN = "TIMEOUT_METHOD_NAME";
	private static final String EJB_TIMER_TABLE_NAME = "jboss_ejb_timer";
	private static final String NEXT_DATE_COLUMN_NAME = "NEXT_DATE";
	private static final String SQL_QUERY = String.format(
		"SELECT jet.* FROM %s jet INNER JOIN (SELECT %s, MAX(%s) AS MaxDateTime FROM %s GROUP BY %s) groupedjet ON jet"
			+ ".%s = groupedjet.%s AND jet.%s = groupedjet.MaxDateTime;",
		EJB_TIMER_TABLE_NAME,
		TIMEOUT_METHOD_NAME_COLUMN,
		NEXT_DATE_COLUMN_NAME,
		EJB_TIMER_TABLE_NAME,
		TIMEOUT_METHOD_NAME_COLUMN,
		TIMEOUT_METHOD_NAME_COLUMN,
		TIMEOUT_METHOD_NAME_COLUMN,
		NEXT_DATE_COLUMN_NAME);

	private static final Logger LOGGER = LoggerFactory.getLogger(DVBatchJobCheck.class);

	@Override
	public HealthCheckResponse call() {
		final DataSource datasource = getDatasource();

		boolean batchOK = true;
		StringBuilder batchKO = new StringBuilder();

		if (datasource != null) {
			Connection connection = null;
			ResultSet resultAllBatch = null;
			PreparedStatement pst = null;
			try {
				connection = datasource.getConnection();
				final String queryAllBatch = connection.nativeSQL(SQL_QUERY);
				pst = connection.prepareStatement(queryAllBatch);
				resultAllBatch = pst.executeQuery();
				while (resultAllBatch.next()) {
					String statusBatchMahnungFristablauf = resultAllBatch.getString(TIMER_STATE_COLUMN);
					if (statusBatchMahnungFristablauf.equals(IN_TIMEOUT_STATE)) {
						Timestamp previousRun = resultAllBatch.getTimestamp(PREVIOUS_RUN_COLUMN);
						if (previousRun != null && previousRun.before(Timestamp.valueOf(LocalDateTime.now()
							.minusDays(1)))) {
							//in Timeout since one Day or more => Problem
							String timeoutBatchName = resultAllBatch.getString(TIMEOUT_METHOD_NAME_COLUMN);
							batchOK = false;
							if (batchKO.length() > 0) {
								batchKO.append(", ");
							}
							batchKO.append(timeoutBatchName);
							batchKO.append(": IN_TIMEOUT since more than one day");
						}
					}
				}
			} catch (SQLException e) {
				LOGGER.warn("Batchjob check failed", e);
			} finally {
				if (resultAllBatch != null) {
					try {
						resultAllBatch.close();
					} catch (SQLException e) {
						// ignore
					}
				}
				if (pst != null) {
					try {
						pst.close();
					} catch (SQLException e) {
						// ignore
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException ignore) {
						// ignore
					}
				}
			}
		}

		return HealthCheckResponse.builder().name("dv-batchjob-check")
			.withData("batchListInTimeout", batchKO.toString())
			.state(batchOK)
			.build();
	}

	/**
	 * Wir verwenden dieser Datasource nur hier und es sollte niemals wirklich zugegriefen werden
	 * deswegen habe ich keine DBProvider oder so gebaut und direkt hier die Verbindung erstellt
	 * wenn die Verbindung nicht erstellt werden kann haben wir einen schlimmen Problem
	 */
	private DataSource getDatasource() {
		try {
			return (DataSource) new InitialContext().lookup(DATASOURCE_JNDI_NAME);
		} catch (NamingException e) {
			final String msg = ("Database Lookup error (missing datasource '" + DATASOURCE_JNDI_NAME) + "')";
			throw new EbeguRuntimeException("getDatasource", msg, e);
		}
	}
}
