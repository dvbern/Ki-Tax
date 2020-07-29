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
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;

import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(SozialhilfeZeitraumService.class)
public class SozialhilfeZeitraumServiceBean extends AbstractBaseService implements SozialhilfeZeitraumService {

	@Inject
	private Persistence persistence;


	@Nonnull
	@Override
	public SozialhilfeZeitraumContainer saveSozialhilfeZeitraum(@Nonnull @Valid SozialhilfeZeitraumContainer sozialhilfeZeitraumContainer) {
		Objects.requireNonNull(sozialhilfeZeitraumContainer);
		final SozialhilfeZeitraumContainer mergedSozialhilfeZeitraum = persistence.merge(sozialhilfeZeitraumContainer);
		mergedSozialhilfeZeitraum.getFamiliensituationContainer().addSozialhilfeZeitraumContainer(mergedSozialhilfeZeitraum);
		return mergedSozialhilfeZeitraum;
	}

	@Nonnull
	@Override
	public Optional<SozialhilfeZeitraumContainer> findSozialhilfeZeitraum(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		SozialhilfeZeitraumContainer shzCnt = persistence.find(SozialhilfeZeitraumContainer.class, key);
		return Optional.ofNullable(shzCnt);
	}

	@Override
	public void removeSozialhilfeZeitraum(@Nonnull String sozialhilfeZeitraumContainerID) {
		Objects.requireNonNull(sozialhilfeZeitraumContainerID);
		SozialhilfeZeitraumContainer shzCont =
			this.findSozialhilfeZeitraum(sozialhilfeZeitraumContainerID).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"removeSozialhilfeZeitraum",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				sozialhilfeZeitraumContainerID)
		);
		FamiliensituationContainer famSit = shzCont.getFamiliensituationContainer();
		persistence.remove(shzCont);

		// the sozialhilfeContainer needs to be removed from the famSit object as well
		if(!famSit.getSozialhilfeZeitraumContainers().isEmpty()){
			famSit.getSozialhilfeZeitraumContainers().removeIf(shz -> shz.getId().equalsIgnoreCase(sozialhilfeZeitraumContainerID));
		}
	}
}
