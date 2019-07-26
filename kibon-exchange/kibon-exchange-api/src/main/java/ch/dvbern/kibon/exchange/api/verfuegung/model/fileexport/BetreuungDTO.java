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

package ch.dvbern.kibon.exchange.api.verfuegung.model.fileexport;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.kibon.exchange.api.verfuegung.model.BetreuungsAngebot;
import ch.dvbern.kibon.exchange.api.institution.model.InstitutionDTO;

public class BetreuungDTO implements Serializable {

	private static final long serialVersionUID = -806547086175525904L;

	@Nonnull
	private @NotNull BetreuungsAngebot betreuungsArt;

	@Nonnull
	private @NotNull @Valid InstitutionDTO institution;

	public BetreuungDTO() {
		this.betreuungsArt = BetreuungsAngebot.KITA;
		this.institution = new InstitutionDTO();
	}

	public BetreuungDTO(@Nonnull BetreuungsAngebot betreuungsArt, @Nonnull InstitutionDTO institution) {
		this.betreuungsArt = betreuungsArt;
		this.institution = institution;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		BetreuungDTO betreuung = (BetreuungDTO) o;

		return getBetreuungsArt() == betreuung.getBetreuungsArt() &&
			getInstitution().equals(betreuung.getInstitution());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBetreuungsArt(), getInstitution());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", BetreuungDTO.class.getSimpleName() + '[', "]")
			.add("betreuungsArt=" + betreuungsArt)
			.add("institution=" + institution)
			.toString();
	}

	@Nonnull
	public BetreuungsAngebot getBetreuungsArt() {
		return betreuungsArt;
	}

	public void setBetreuungsArt(@Nonnull BetreuungsAngebot betreuungsArt) {
		this.betreuungsArt = betreuungsArt;
	}

	@Nonnull
	public InstitutionDTO getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull InstitutionDTO institution) {
		this.institution = institution;
	}
}
