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

package ch.dvbern.ebegu.services;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.PensumAusserordentlicherAnspruch;
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
 * Service fuer pensumAusserordentlicherAnspruch
 */
@Stateless
@Local(PensumAusserordentlicherAnspruchService.class)
public class PensumAusserordentlicherAnspruchServiceBean extends AbstractBaseService implements PensumAusserordentlicherAnspruchService {

	@Inject
	private Persistence persistence;

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS })
	public PensumAusserordentlicherAnspruch savePensumAusserordentlicherAnspruch(@Nonnull PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch) {
		Objects.requireNonNull(pensumAusserordentlicherAnspruch);
		return persistence.merge(pensumAusserordentlicherAnspruch);
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<PensumAusserordentlicherAnspruch> findPensumAusserordentlicherAnspruch(@Nonnull String pensumAusserordentlicherAnspruchId) {
		Objects.requireNonNull(pensumAusserordentlicherAnspruchId, "id muss gesetzt sein");
		PensumAusserordentlicherAnspruch a = persistence.find(PensumAusserordentlicherAnspruch.class, pensumAusserordentlicherAnspruchId);
		return Optional.ofNullable(a);
	}
}
