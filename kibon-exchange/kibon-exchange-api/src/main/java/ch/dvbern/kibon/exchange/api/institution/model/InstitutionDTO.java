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

package ch.dvbern.kibon.exchange.api.institution.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class InstitutionDTO implements Serializable {

	private static final long serialVersionUID = 5304104104672134122L;

	@Nonnull
	private @Size(min = 1) @NotNull String id;

	@Nonnull
	private @Size(min = 1) @NotNull String name;

	@Nullable
	private String traegerschaft = null;

	@Nonnull
	private @NotNull @Valid AdresseDTO adresse;

	public InstitutionDTO() {
		this.id = "";
		this.name = "";
		this.adresse = new AdresseDTO();
	}

	public InstitutionDTO(
		@Nonnull String id,
		@Nonnull String name,
		@Nonnull String traegerschaft,
		@Nonnull AdresseDTO adresse) {
		this.id = id;
		this.name = name;
		this.traegerschaft = traegerschaft;
		this.adresse = adresse;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		InstitutionDTO that = (InstitutionDTO) o;

		return getId().equals(that.getId()) &&
			getName().equals(that.getName()) &&
			Objects.equals(getTraegerschaft(), that.getTraegerschaft()) &&
			getAdresse().equals(that.getAdresse());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getName(), getTraegerschaft(), getAdresse());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", InstitutionDTO.class.getSimpleName() + '[', "]")
			.add("id='" + id + '\'')
			.add("name='" + name + '\'')
			.add("traegerschaft='" + traegerschaft + '\'')
			.toString();
	}

	@Nonnull
	public String getId() {
		return id;
	}

	public void setId(@Nonnull String id) {
		this.id = id;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nullable
	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Nonnull
	public AdresseDTO getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull AdresseDTO adresse) {
		this.adresse = adresse;
	}
}
