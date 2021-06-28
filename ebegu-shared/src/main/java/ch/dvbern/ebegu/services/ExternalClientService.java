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
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;

/**
 * Service to get information about external (3rd-party) clients
 */
public interface ExternalClientService {

	@Nonnull
	Collection<ExternalClient> getAllForGemeinde();

	@Nonnull
	Collection<ExternalClient> getAllForInstitution(@Nonnull Institution institution);

	@Nonnull
	Optional<ExternalClient> findExternalClient(@Nonnull String id);

	@Nonnull
	Collection<InstitutionExternalClient> getInstitutionExternalClientForInstitution(@Nonnull Institution institution);
}
