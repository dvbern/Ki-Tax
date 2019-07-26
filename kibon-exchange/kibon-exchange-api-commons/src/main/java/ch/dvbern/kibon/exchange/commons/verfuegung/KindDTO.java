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
import java.time.LocalDate;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class KindDTO implements Serializable {

	private static final long serialVersionUID = -1270271827440553390L;

	@Nonnull
	private @NotEmpty String vorname;

	@Nonnull
	private @NotEmpty String nachname;

	@Nonnull
	private @NotNull LocalDate geburtsdatum;

	public KindDTO() {
		this.vorname = "";
		this.nachname = "";
		this.geburtsdatum = LocalDate.MIN;
	}

	public KindDTO(@Nonnull String vorname, @Nonnull String nachname, @Nonnull LocalDate geburtsdatum) {
		this.vorname = vorname;
		this.nachname = nachname;
		this.geburtsdatum = geburtsdatum;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		KindDTO kind = (KindDTO) o;

		return getVorname().equals(kind.getVorname()) &&
			getNachname().equals(kind.getNachname()) &&
			getGeburtsdatum().equals(kind.getGeburtsdatum());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getVorname(), getNachname(), getGeburtsdatum());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", KindDTO.class.getSimpleName() + '[', "]")
			.add("vorname='" + vorname + '\'')
			.add("nachname='" + nachname + '\'')
			.add("geburtsdatum=" + geburtsdatum)
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

	@Nonnull
	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(@Nonnull LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}
}
