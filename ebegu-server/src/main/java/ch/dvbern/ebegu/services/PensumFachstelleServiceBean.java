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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer pensumFachstelle
 */
@Stateless
@Local(PensumFachstelleService.class)
public class PensumFachstelleServiceBean extends AbstractBaseService implements PensumFachstelleService {

	@Inject
	private Persistence persistence;

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS })
	public PensumFachstelle savePensumFachstelle(@Nonnull PensumFachstelle pensumFachstelle) {
		// WARNING! This service should be used only for tests. The PensumFachstelle will only be validated with
		// CheckPensumFachstelleValidator when saving the KindContainer
		Objects.requireNonNull(pensumFachstelle);
		return persistence.merge(pensumFachstelle);
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<PensumFachstelle> findPensumFachstelle(@Nonnull String pensumFachstelleId) {
		Objects.requireNonNull(pensumFachstelleId, "id muss gesetzt sein");
		PensumFachstelle a = persistence.find(PensumFachstelle.class, pensumFachstelleId);
		return Optional.ofNullable(a);
	}
}
