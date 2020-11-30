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

package ch.dvbern.ebegu.reporting.lastenausgleich;

import java.time.LocalDate;

import javax.annotation.Nullable;

/**
 * DTO für den Lastenausgleich von KiBon
 */
public class KindMitZemisNummerDataRow {

	private @Nullable Long fall;
	private @Nullable String periode;
	private @Nullable String gemeinde;
	private @Nullable String name;
	private @Nullable String vorname;
	private @Nullable Integer kindNummer;
	private @Nullable LocalDate geburtsdatum;
	private @Nullable String zemisNummer;
	private boolean keinSelbstbehaltFuerGemeinde;

	public KindMitZemisNummerDataRow() {}

	@Nullable
	public Long getFall() {
		return fall;
	}

	public void setFall(@Nullable Long fall) {
		this.fall = fall;
	}

	@Nullable
	public String getPeriode() {
		return periode;
	}

	public void setPeriode(@Nullable String periode) {
		this.periode = periode;
	}

	@Nullable
	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable String gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nullable
	public String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	@Nullable
	public String getVorname() {
		return vorname;
	}

	@Nullable
	public Integer getKindNummer() {
		return kindNummer;
	}

	public void setKindNummer(@Nullable Integer kindNummer) {
		this.kindNummer = kindNummer;
	}

	public void setVorname(@Nullable String vorname) {
		this.vorname = vorname;
	}

	@Nullable
	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(@Nullable LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	@Nullable
	public String getZemisNummer() {
		return zemisNummer;
	}

	public void setZemisNummer(@Nullable String zemisNummer) {
		this.zemisNummer = zemisNummer;
	}

	public boolean isKeinSelbstbehaltFuerGemeinde() {
		return keinSelbstbehaltFuerGemeinde;
	}

	public void setKeinSelbstbehaltFuerGemeinde(boolean keinSelbstbehaltFuerGemeinde) {
		this.keinSelbstbehaltFuerGemeinde = keinSelbstbehaltFuerGemeinde;
	}
}
