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

package ch.dvbern.ebegu.dbschema;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.sql.DataSource;

import ch.dvbern.ebegu.dbschema.dbprovider.DBProvider;
import org.flywaydb.core.Flyway;

/**
 * Dieses Bean sorgt dafuer, dass beim Startup des Java EE Servers migrate ausgefuehrt wird.
 */
@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN) //flyway managed transactions selber
public class MigrateSchema {

	@Inject
	private Instance<DBProvider> dbProviderInst;


	private DataSource resolveDB() {

		DBProvider provider = dbProviderInst.get();
		return provider.getDatasource();
	}


	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
	@PostConstruct
	public void migrateSchema() {

		final DataSource dataSource = resolveDB();
		final Flyway flyway =
			Flyway.configure().outOfOrder(true).encoding("UTF-8").table("schema_version").dataSource(dataSource).load();

		flyway.migrate();
	}
}
