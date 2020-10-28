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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.reporting.mahlzeiten;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;

public class MahlzeitenverguenstigungDataRow {

	@Nullable private String bgNummer;
	@Nullable private BetreuungsangebotTyp betreuungsTyp;
	@Nullable private String institution;
	@Nullable private String traegerschaft;

	@Nullable private String gs1Name;
	@Nullable private String gs1Vorname;

	@Nullable private String gs2Name;
	@Nullable private String gs2Vorname;

	@Nullable private String kindName;
	@Nullable private String kindVorname;
	@Nullable private LocalDate kindGeburtsdatum;

	@Nullable private LocalDate zeitabschnittVon;
	@Nullable private LocalDate zeitabschnittBis;

	@Nullable private BigDecimal anzahlHauptmahlzeiten;
	@Nullable private BigDecimal anzahlNebenmahlzeiten;
	@Nullable private BigDecimal kostenHauptmahlzeiten;
	@Nullable private BigDecimal kostenNebenmahlzeiten;
	@Nullable private BigDecimal berechneteMahlzeitenverguenstigung;


	public MahlzeitenverguenstigungDataRow() {
	}

	@Nullable
	public String getBgNummer() {
		return bgNummer;
	}

	public void setBgNummer(@Nullable String bgNummer) {
		this.bgNummer = bgNummer;
	}

	@Nullable
	public BetreuungsangebotTyp getBetreuungsTyp() {
		return betreuungsTyp;
	}

	public void setBetreuungsTyp(@Nullable BetreuungsangebotTyp betreuungsTyp) {
		this.betreuungsTyp = betreuungsTyp;
	}

	@Nullable
	public String getInstitution() {
		return institution;
	}

	public void setInstitution(@Nullable String institution) {
		this.institution = institution;
	}

	@Nullable
	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Nullable
	public String getGs1Name() {
		return gs1Name;
	}

	public void setGs1Name(@Nullable String gs1Name) {
		this.gs1Name = gs1Name;
	}

	@Nullable
	public String getGs1Vorname() {
		return gs1Vorname;
	}

	public void setGs1Vorname(@Nullable String gs1Vorname) {
		this.gs1Vorname = gs1Vorname;
	}

	@Nullable
	public String getGs2Name() {
		return gs2Name;
	}

	public void setGs2Name(@Nullable String gs2Name) {
		this.gs2Name = gs2Name;
	}

	@Nullable
	public String getGs2Vorname() {
		return gs2Vorname;
	}

	public void setGs2Vorname(@Nullable String gs2Vorname) {
		this.gs2Vorname = gs2Vorname;
	}

	@Nullable
	public String getKindName() {
		return kindName;
	}

	public void setKindName(@Nullable String kindName) {
		this.kindName = kindName;
	}

	@Nullable
	public String getKindVorname() {
		return kindVorname;
	}

	public void setKindVorname(@Nullable String kindVorname) {
		this.kindVorname = kindVorname;
	}

	@Nullable
	public LocalDate getKindGeburtsdatum() {
		return kindGeburtsdatum;
	}

	public void setKindGeburtsdatum(@Nullable LocalDate kindGeburtsdatum) {
		this.kindGeburtsdatum = kindGeburtsdatum;
	}

	@Nullable
	public LocalDate getZeitabschnittVon() {
		return zeitabschnittVon;
	}

	public void setZeitabschnittVon(@Nullable LocalDate zeitabschnittVon) {
		this.zeitabschnittVon = zeitabschnittVon;
	}

	@Nullable
	public LocalDate getZeitabschnittBis() {
		return zeitabschnittBis;
	}

	public void setZeitabschnittBis(@Nullable LocalDate zeitabschnittBis) {
		this.zeitabschnittBis = zeitabschnittBis;
	}

	@Nullable
	public BigDecimal getAnzahlHauptmahlzeiten() {
		return anzahlHauptmahlzeiten;
	}

	public void setAnzahlHauptmahlzeiten(@Nullable BigDecimal anzahlHauptmahlzeiten) {
		this.anzahlHauptmahlzeiten = anzahlHauptmahlzeiten;
	}

	@Nullable
	public BigDecimal getAnzahlNebenmahlzeiten() {
		return anzahlNebenmahlzeiten;
	}

	public void setAnzahlNebenmahlzeiten(@Nullable BigDecimal anzahlNebenmahlzeiten) {
		this.anzahlNebenmahlzeiten = anzahlNebenmahlzeiten;
	}

	@Nullable
	public BigDecimal getKostenHauptmahlzeiten() {
		return kostenHauptmahlzeiten;
	}

	public void setKostenHauptmahlzeiten(@Nullable BigDecimal kostenHauptmahlzeiten) {
		this.kostenHauptmahlzeiten = kostenHauptmahlzeiten;
	}

	@Nullable
	public BigDecimal getKostenNebenmahlzeiten() {
		return kostenNebenmahlzeiten;
	}

	public void setKostenNebenmahlzeiten(@Nullable BigDecimal kostenNebenmahlzeiten) {
		this.kostenNebenmahlzeiten = kostenNebenmahlzeiten;
	}

	@Nullable
	public BigDecimal getBerechneteMahlzeitenverguenstigung() {
		return berechneteMahlzeitenverguenstigung;
	}

	public void setBerechneteMahlzeitenverguenstigung(@Nullable BigDecimal berechneteMahlzeitenverguenstigung) {
		this.berechneteMahlzeitenverguenstigung = berechneteMahlzeitenverguenstigung;
	}
}
