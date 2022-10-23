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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO für den Lastenausgleich von KiBon
 */
public class LastenausgleichBGZeitabschnittDataRow {

	private String referenznummer;
	private long bfsNummer;
	private String nameGemeinde;
	private String nachname;
	private String vorname;
	private LocalDate geburtsdatum;
	private LocalDate von;
	private LocalDate bis;
	private String institution;
	private String betreuungsangebotTyp;
	private BigDecimal bgPensum;
	private Boolean keinSelbstbehaltDurchGemeinde;
	private BigDecimal gutschein;

	public LastenausgleichBGZeitabschnittDataRow() {}

	public String getReferenznummer() {
		return referenznummer;
	}

	public void setReferenznummer(String referenznummer) {
		this.referenznummer = referenznummer;
	}

	public long getBfsNummer() {
		return bfsNummer;
	}

	public void setBfsNummer(long bfsNummer) {
		this.bfsNummer = bfsNummer;
	}

	public String getNameGemeinde() {
		return nameGemeinde;
	}

	public void setNameGemeinde(String nameGemeinde) {
		this.nameGemeinde = nameGemeinde;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	public LocalDate getVon() {
		return von;
	}

	public void setVon(LocalDate von) {
		this.von = von;
	}

	public LocalDate getBis() {
		return bis;
	}

	public void setBis(LocalDate bis) {
		this.bis = bis;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(String betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	public BigDecimal getBgPensum() {
		return bgPensum;
	}

	public void setBgPensum(BigDecimal bgPensum) {
		this.bgPensum = bgPensum;
	}

	public Boolean getKeinSelbstbehaltDurchGemeinde() {
		return keinSelbstbehaltDurchGemeinde;
	}

	public void setKeinSelbstbehaltDurchGemeinde(Boolean keinSelbstbehaltDurchGemeinde) {
		this.keinSelbstbehaltDurchGemeinde = keinSelbstbehaltDurchGemeinde;
	}

	public BigDecimal getGutschein() {
		return gutschein;
	}

	public void setGutschein(BigDecimal gutschein) {
		this.gutschein = gutschein;
	}
}
