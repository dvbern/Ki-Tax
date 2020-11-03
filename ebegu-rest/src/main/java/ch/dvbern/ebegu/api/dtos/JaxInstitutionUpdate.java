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

package ch.dvbern.ebegu.api.dtos;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class JaxInstitutionUpdate {

	@Nullable
	private String name;

	@Nullable
	private String traegerschaftId;

	@Nonnull
	private @Valid @NotNull JaxInstitutionStammdaten stammdaten = new JaxInstitutionStammdaten();

	@Nullable
	private @Valid List<JaxInstitutionExternalClient> institutionExternalClients = null;

	@Nonnull
	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	@Nonnull
	public Optional<String> getTraegerschaftId() {
		return Optional.ofNullable(traegerschaftId);
	}

	public void setTraegerschaftId(@Nullable String traegerschaftId) {
		this.traegerschaftId = traegerschaftId;
	}

	@Nonnull
	public JaxInstitutionStammdaten getStammdaten() {
		return stammdaten;
	}

	public void setStammdaten(@Nonnull JaxInstitutionStammdaten stammdaten) {
		this.stammdaten = stammdaten;
	}

	@Nullable
	public List<JaxInstitutionExternalClient> getInstitutionExternalClients() {
		return institutionExternalClients;
	}

	public void setInstitutionExternalClients(@Nullable List<JaxInstitutionExternalClient> institutionExternalClients) {
		this.institutionExternalClients = institutionExternalClients;
	}
}
