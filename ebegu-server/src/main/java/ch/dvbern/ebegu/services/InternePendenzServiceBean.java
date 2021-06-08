/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.util.List;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.InternePendenz;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer interne Pendenzen
 */
@Stateless
@Local(InternePendenzService.class)
public class InternePendenzServiceBean extends AbstractBaseService implements InternePendenzService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GesuchService gesuchService;

	@Nonnull
	@Override
	public InternePendenz saveInternePendenz(@Nonnull InternePendenz internePendenz) {
		authorizer.checkWriteAuthorization(internePendenz);
		return persistence.merge(internePendenz);
	}

	@Nonnull
	@Override
	public List<InternePendenz> findInternePendenzenForGesuch(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		return null;
	}

	@Nonnull
	@Override
	public Integer countInternePendenzenForGesuch(@Nonnull Gesuch gesuch) {
		return null;
	}

	@Override
	public void deleteInternePendenz(@Nonnull InternePendenz internePendenz) {
		authorizer.checkWriteAuthorization(internePendenz);
		persistence.remove(internePendenz);
	}
}
