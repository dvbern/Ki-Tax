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

package ch.dvbern.ebegu.services;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;

import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.SocialhilfeZeitraumContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Stateless
@Local(SocialhilfeZeitraumService.class)
@PermitAll
public class SocialhilfeZeitraumServiceBean extends AbstractBaseService implements SocialhilfeZeitraumService {

	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public SocialhilfeZeitraumContainer saveSocialhilfeZeitraum(@Nonnull @Valid SocialhilfeZeitraumContainer socialhilfeZeitraumContainer) {
		Objects.requireNonNull(socialhilfeZeitraumContainer);
		final SocialhilfeZeitraumContainer mergedSocialhilfeZeitraum = persistence.merge(socialhilfeZeitraumContainer);
		mergedSocialhilfeZeitraum.getFamiliensituationContainer().addSocialhilfeZeitraumContainer(mergedSocialhilfeZeitraum);
		return mergedSocialhilfeZeitraum;
	}

	@Nonnull
	@Override
	public Optional<SocialhilfeZeitraumContainer> findSocialhilfeZeitraum(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		SocialhilfeZeitraumContainer shzCnt = persistence.find(SocialhilfeZeitraumContainer.class, key);
		return Optional.ofNullable(shzCnt);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public void removeSocialhilfeZeitraum(@Nonnull String socialhilfeZeitraumContainerID) {
		Objects.requireNonNull(socialhilfeZeitraumContainerID);
		SocialhilfeZeitraumContainer shzCont =
			this.findSocialhilfeZeitraum(socialhilfeZeitraumContainerID).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"removeSocialhilfeZeitraum",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				socialhilfeZeitraumContainerID)
		);
		FamiliensituationContainer famSit = shzCont.getFamiliensituationContainer();
		persistence.remove(shzCont);

		// the socialhilfeContainer needs to be removed from the famSit object as well
		if(!famSit.getSocialhilfeZeitraumContainers().isEmpty()){
			famSit.getSocialhilfeZeitraumContainers().removeIf(shz -> shz.getId().equalsIgnoreCase(socialhilfeZeitraumContainerID));
		}
	}
}
