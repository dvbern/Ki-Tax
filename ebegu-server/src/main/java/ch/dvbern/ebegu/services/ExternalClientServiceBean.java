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

package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.ExternalClient_;
import ch.dvbern.ebegu.enums.ExternalClientType;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;

@Stateless
@Local(ExternalClientService.class)
public class ExternalClientServiceBean extends AbstractBaseService implements ExternalClientService {

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS })
	public Collection<ExternalClient> getAllForGemeinde() {
		return criteriaQueryHelper.getEntitiesByAttribute(
			ExternalClient.class, ExternalClientType.GEMEINDE_SCOLARIS_SERVICE, ExternalClient_.type);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS })
	public Collection<ExternalClient> getAllForInstitution() {
		return criteriaQueryHelper.getEntitiesByAttribute(
			ExternalClient.class, ExternalClientType.EXCHANGE_SERVICE_USER, ExternalClient_.type);
	}
}
