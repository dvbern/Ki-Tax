/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.reporting.kanton;

import java.math.BigDecimal;
import java.time.LocalDate;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * DTO fuer die Kantonsstatistik
 */
public class KantonDataRow {

	private String gemeinde;
	private String referenzNummer;
	private String gesuchId;
	private String name;
	private String vorname;
	private LocalDate geburtsdatum;
	private LocalDate zeitabschnittVon;
	private LocalDate zeitabschnittBis;
	private BigDecimal bgPensumKanton;
	private BigDecimal bgPensumGemeinde;
	private BigDecimal bgPensumTotal;
	private BigDecimal elternbeitrag;
	private BigDecimal verguenstigungKanton;
	private BigDecimal verguenstigungGemeinde;
	private BigDecimal verguenstigungTotal;
	private String institution;
	private String betreuungsTyp;

	private Boolean babyTarif;

	public String getReferenzNummer() {
		return referenzNummer;
	}

	public void setReferenzNummer(String referenzNummer) {
		this.referenzNummer = referenzNummer;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	public String getGesuchId() {
		return gesuchId;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	public void setGesuchId(String gesuchId) {
		this.gesuchId = gesuchId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public BigDecimal getBgPensumKanton() {
		return bgPensumKanton;
	}

	public void setBgPensumKanton(BigDecimal bgPensumKanton) {
		this.bgPensumKanton = bgPensumKanton;
	}

	public BigDecimal getBgPensumGemeinde() {
		return bgPensumGemeinde;
	}

	public void setBgPensumGemeinde(BigDecimal bgPensumGemeinde) {
		this.bgPensumGemeinde = bgPensumGemeinde;
	}

	public BigDecimal getBgPensumTotal() {
		return bgPensumTotal;
	}

	public void setBgPensumTotal(BigDecimal bgPensumTotal) {
		this.bgPensumTotal = bgPensumTotal;
	}

	public BigDecimal getElternbeitrag() {
		return elternbeitrag;
	}

	public void setElternbeitrag(BigDecimal elternbeitrag) {
		this.elternbeitrag = elternbeitrag;
	}

	public BigDecimal getVerguenstigungKanton() {
		return verguenstigungKanton;
	}

	public void setVerguenstigungKanton(BigDecimal verguenstigungKanton) {
		this.verguenstigungKanton = verguenstigungKanton;
	}

	public BigDecimal getVerguenstigungGemeinde() {
		return verguenstigungGemeinde;
	}

	public void setVerguenstigungGemeinde(BigDecimal verguenstigungGemeinde) {
		this.verguenstigungGemeinde = verguenstigungGemeinde;
	}

	public BigDecimal getVerguenstigungTotal() {
		return verguenstigungTotal;
	}

	public void setVerguenstigungTotal(BigDecimal verguenstigungTotal) {
		this.verguenstigungTotal = verguenstigungTotal;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getBetreuungsTyp() {
		return betreuungsTyp;
	}

	public void setBetreuungsTyp(String betreuungsTyp) {
		this.betreuungsTyp = betreuungsTyp;
	}

	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(String gemeinde) {
		this.gemeinde = gemeinde;
	}

	public Boolean getBabyTarif() {
		return babyTarif;
	}

	public void setBabyTarif(Boolean babyTarif) {
		this.babyTarif = babyTarif;
	}
}
