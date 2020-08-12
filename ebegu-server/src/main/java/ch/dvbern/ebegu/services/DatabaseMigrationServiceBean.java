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

package ch.dvbern.ebegu.services;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service zum Ausfuehren von manuellen DB-Migrationen.
 * Hier koennen Skripts hinzugefuegt werden, die dann Asynchron ausgefuehrt werden. Vor allem fuer Scripts gemeint, die eine
 * Aenderung in der Datenbank bedeuten.
 */
@Stateless
@Local(DatabaseMigrationService.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "LocalVariableNamingConvention", "PMD.NcssTypeCount", "InstanceMethodNamingConvention" })
public class DatabaseMigrationServiceBean extends AbstractBaseService implements DatabaseMigrationService {

	@Inject
	private Persistence persistence;

	@Override
	@Asynchronous
	@TransactionTimeout(value = Constants.MAX_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Future<Boolean> processScript(@Nonnull String scriptId) {

		// *****************************
		// Neue Scripts hier hinzufügen.
		// *****************************

		// to avoid errors due to missing Context because Principal is set as RequestScoped
		persistence.getEntityManager().flush();
		return new AsyncResult<>(Boolean.TRUE);
	}

}
