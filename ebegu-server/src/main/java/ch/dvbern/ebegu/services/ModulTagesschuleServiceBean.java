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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer Modul
 */
@Stateless
@Local(ModulTagesschuleService.class)
public class ModulTagesschuleServiceBean extends AbstractBaseService implements ModulTagesschuleService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	public ModulTagesschule saveModul(@Nonnull ModulTagesschule modulTagesschule) {
		Objects.requireNonNull(modulTagesschule);
		return persistence.merge(modulTagesschule);
	}

	@Nonnull
	@Override
	public Optional<ModulTagesschule> findModul(@Nonnull String modulTagesschuleId) {
		Objects.requireNonNull(modulTagesschuleId, "id muss gesetzt sein");
		ModulTagesschule modul = persistence.find(ModulTagesschule.class, modulTagesschuleId);
		return Optional.ofNullable(modul);
	}

	@Override
	public void removeModul(@Nonnull String modulTagesschuleId) {
		Objects.requireNonNull(modulTagesschuleId);
		Optional<ModulTagesschule> modulOptional = findModul(modulTagesschuleId);
		ModulTagesschule modulToRemove = modulOptional.orElseThrow(() -> new EbeguEntityNotFoundException("removeModul", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			modulTagesschuleId));
		persistence.remove(modulToRemove);
	}

	@Override
	public Collection<EinstellungenTagesschule> findEinstellungenTagesschuleByGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		return
			criteriaQueryHelper.getEntitiesByAttribute(
				EinstellungenTagesschule.class, gesuchsperiode, EinstellungenTagesschule_.gesuchsperiode);
	}

	@Override
	public void copyModuleTagesschuleToNewGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiodeToCreate,
		@Nonnull Gesuchsperiode lastGesuchsperiode
	) {
		Collection<EinstellungenTagesschule> lastEinstellungenTagesschule =
			findEinstellungenTagesschuleByGesuchsperiode(lastGesuchsperiode);
		lastEinstellungenTagesschule.forEach(lastEinstellung -> {
			EinstellungenTagesschule newEinstellung = lastEinstellung.copyForGesuchsperiode(gesuchsperiodeToCreate);
			persistence.merge(newEinstellung);
		});
	}
}
