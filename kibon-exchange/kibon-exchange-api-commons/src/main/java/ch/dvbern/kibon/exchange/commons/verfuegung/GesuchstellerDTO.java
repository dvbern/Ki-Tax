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

package ch.dvbern.kibon.exchange.commons.verfuegung;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class GesuchstellerDTO implements Serializable {

	private static final long serialVersionUID = 6621345766879152649L;

	@Nonnull
	private @NotEmpty String vorname;

	@Nonnull
	private @NotEmpty String nachname;

	@Nullable
	private @Email String email;

	public GesuchstellerDTO() {
		this.vorname = "";
		this.nachname = "";
	}

	public GesuchstellerDTO(@Nonnull String vorname, @Nonnull String nachname, @Nullable String email) {
		this.vorname = vorname;
		this.nachname = nachname;
		this.email = email;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		GesuchstellerDTO that = (GesuchstellerDTO) o;

		return getVorname().equals(that.getVorname()) &&
			getNachname().equals(that.getNachname()) &&
			Objects.equals(getEmail(), that.getEmail());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getVorname(), getNachname(), getEmail());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", GesuchstellerDTO.class.getSimpleName() + '[', "]")
			.add("vorname='" + vorname + '\'')
			.add("nachname='" + nachname + '\'')
			.add("email='" + email + '\'')
			.toString();
	}

	@Nonnull
	public String getVorname() {
		return vorname;
	}

	public void setVorname(@Nonnull String vorname) {
		this.vorname = vorname;
	}

	@Nonnull
	public String getNachname() {
		return nachname;
	}

	public void setNachname(@Nonnull String nachname) {
		this.nachname = nachname;
	}

	@Nullable
	public String getEmail() {
		return email;
	}

	public void setEmail(@Nullable String email) {
		this.email = email;
	}
}
