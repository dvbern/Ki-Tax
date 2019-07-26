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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AdresseDTO implements Serializable {

	private static final long serialVersionUID = 73480274021479940L;

	@Nonnull
	private @NotNull String strasse;

	@Nullable
	private String hausnummer;

	@Nullable
	private String adresszusatz;

	@Nonnull
	private @NotNull String plz;

	@Nonnull
	private @NotNull String ort;

	/**
	 * ISO 3166-1 Alpha 2 Code
	 */
	@Nonnull
	private @Size(min = 1, max = 2) @NotNull String land;

	public AdresseDTO() {
		this.strasse = "";
		this.hausnummer = null;
		this.adresszusatz = null;
		this.plz = "";
		this.ort = "";
		this.land = "";
	}

	public AdresseDTO(
		@Nonnull String strasse,
		@Nullable String hausnummer,
		@Nullable String adresszusatz,
		@Nonnull String plz,
		@Nonnull String ort,
		@Nonnull String land) {
		this.strasse = strasse;
		this.hausnummer = hausnummer;
		this.adresszusatz = adresszusatz;
		this.plz = plz;
		this.ort = ort;
		this.land = land;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		AdresseDTO adresse = (AdresseDTO) o;

		return getStrasse().equals(adresse.getStrasse()) &&
			Objects.equals(getHausnummer(), adresse.getHausnummer()) &&
			Objects.equals(getAdresszusatz(), adresse.getAdresszusatz()) &&
			getOrt().equals(adresse.getOrt()) &&
			getPlz().equals(adresse.getPlz()) &&
			getLand().equals(adresse.getLand());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getStrasse(), getHausnummer(), getAdresszusatz(), getOrt(), getPlz(), getLand());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", AdresseDTO.class.getSimpleName() + '[', "]")
			.add("strasse='" + strasse + '\'')
			.add("hausnummer='" + hausnummer + '\'')
			.add("plz='" + plz + '\'')
			.add("ort='" + ort + '\'')
			.toString();
	}

	@Nonnull
	public String getStrasse() {
		return strasse;
	}

	public void setStrasse(@Nonnull String strasse) {
		this.strasse = strasse;
	}

	@Nullable
	public String getHausnummer() {
		return hausnummer;
	}

	public void setHausnummer(@Nullable String hausnummer) {
		this.hausnummer = hausnummer;
	}

	@Nullable
	public String getAdresszusatz() {
		return adresszusatz;
	}

	public void setAdresszusatz(@Nullable String adresszusatz) {
		this.adresszusatz = adresszusatz;
	}

	@Nonnull
	public String getOrt() {
		return ort;
	}

	public void setOrt(@Nonnull String ort) {
		this.ort = ort;
	}

	@Nonnull
	public String getPlz() {
		return plz;
	}

	public void setPlz(@Nonnull String plz) {
		this.plz = plz;
	}

	@Nonnull
	public String getLand() {
		return land;
	}

	public void setLand(@Nonnull String land) {
		this.land = land;
	}
}
