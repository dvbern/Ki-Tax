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

import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;

/**
 * DTO für den Lastenausgleich von KiBon
 */
public class LastenausgleichSelbstbehaltDataRow {

	private String bgNummer;
	private String kindName;
	private String kindVorname;
	private LocalDate kindGeburtsdatum;
	private LocalDate zeitabschnittVon;
	private LocalDate zeitabschnittBis;
	private BigDecimal bgPensum;
	private String institution;
	private BetreuungsangebotTyp betreuungsTyp;
	@Nullable
	private EinschulungTyp tarif;
	private Boolean zusatz;
	private BigDecimal gutschein;
	private @Nullable Boolean keinSelbstbehaltDurchGemeinde;

	public String getBgNummer() {
		return bgNummer;
	}

	public void setBgNummer(String bgNummer) {
		this.bgNummer = bgNummer;
	}

	public String getKindName() {
		return kindName;
	}

	public void setKindName(String kindName) {
		this.kindName = kindName;
	}

	public String getKindVorname() {
		return kindVorname;
	}

	public void setKindVorname(String kindVorname) {
		this.kindVorname = kindVorname;
	}

	public LocalDate getKindGeburtsdatum() {
		return kindGeburtsdatum;
	}

	public void setKindGeburtsdatum(LocalDate kindGeburtsdatum) {
		this.kindGeburtsdatum = kindGeburtsdatum;
	}

	public LocalDate getZeitabschnittVon() {
		return zeitabschnittVon;
	}

	public void setZeitabschnittVon(LocalDate zeitabschnittVon) {
		this.zeitabschnittVon = zeitabschnittVon;
	}

	public LocalDate getZeitabschnittBis() {
		return zeitabschnittBis;
	}

	public void setZeitabschnittBis(LocalDate zeitabschnittBis) {
		this.zeitabschnittBis = zeitabschnittBis;
	}

	public BigDecimal getBgPensum() {
		return bgPensum;
	}

	public void setBgPensum(BigDecimal bgPensum) {
		this.bgPensum = bgPensum;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public BetreuungsangebotTyp getBetreuungsTyp() {
		return betreuungsTyp;
	}

	public void setBetreuungsTyp(BetreuungsangebotTyp betreuungsTyp) {
		this.betreuungsTyp = betreuungsTyp;
	}

	@Nullable
	public EinschulungTyp getTarif() {
		return tarif;
	}

	public void setTarif(@Nullable EinschulungTyp tarif) {
		this.tarif = tarif;
	}

	public Boolean getZusatz() {
		return zusatz;
	}

	public void setZusatz(Boolean zusatz) {
		this.zusatz = zusatz;
	}

	public BigDecimal getGutschein() {
		return gutschein;
	}

	public void setGutschein(BigDecimal gutschein) {
		this.gutschein = gutschein;
	}

	@Nullable
	public Boolean getKeinSelbstbehaltDurchGemeinde() {
		return keinSelbstbehaltDurchGemeinde;
	}

	public void setKeinSelbstbehaltDurchGemeinde(@Nullable Boolean keinSelbstbehaltDurchGemeinde) {
		this.keinSelbstbehaltDurchGemeinde = keinSelbstbehaltDurchGemeinde;
	}
}
