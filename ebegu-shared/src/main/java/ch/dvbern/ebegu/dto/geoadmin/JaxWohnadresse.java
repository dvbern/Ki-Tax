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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.dto.geoadmin;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response von GeoAdmin API.
 */
@XmlRootElement(name = "wohnadresse")
public class JaxWohnadresse implements Serializable {

	private static final long serialVersionUID = 852465265241772700L;

	@Nonnull
	private final String externalId;
	@Nonnull
	private final String strasse;
	@Nonnull
	private final String hausnummer;
	@Nonnull
	private final String gemeinde;
	@Nonnull
	private final Long gemeindeBfsNr;
	@Nonnull
	private final String ort;
	@Nonnull
	private final String plz;

	// Das Flag ist z.B. gesetzt, wenn man nach Freiburg gesucht hat, im Ergebnis aber Fribourg geliefert wird
	private final boolean fuzzy;

	public JaxWohnadresse(
		boolean fuzzy,
		@Nonnull String externalId,
		@Nonnull String strasse,
		@Nonnull String hausnummer,
		@Nonnull String plz,
		@Nonnull String ort,
		@Nonnull Long gemeindeBfsNr,
		@Nonnull String gemeinde) {
		this.externalId = externalId;
		this.strasse = strasse;
		this.hausnummer = hausnummer;
		this.gemeinde = gemeinde;
		this.gemeindeBfsNr = gemeindeBfsNr;
		this.ort = ort;
		this.plz = plz;
		this.fuzzy = fuzzy;
	}

	public boolean isFuzzy() {
		return fuzzy;
	}

	@Nonnull
	public String getExternalId() {
		return externalId;
	}

	@Nonnull
	public String getStrasse() {
		return strasse;
	}

	@Nonnull
	public String getHausnummer() {
		return hausnummer;
	}

	@Nonnull
	public String getOrt() {
		return ort;
	}

	@Nonnull
	public String getPlz() {
		return plz;
	}

	@Nonnull
	public String getGemeinde() {
		return gemeinde;
	}

	@Nonnull
	public Long getGemeindeBfsNr() {
		return gemeindeBfsNr;
	}
}
