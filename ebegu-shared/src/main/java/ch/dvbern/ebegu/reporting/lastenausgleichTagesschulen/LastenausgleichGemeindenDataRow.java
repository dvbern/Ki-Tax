/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class LastenausgleichGemeindenDataRow {

	@Nullable
	private String nameGemeinde;

	@Nullable
	private Long bfsNummer;

	@Nullable
	private String gemeindeFallNummer;

	@Nullable
	private String periode;

	@Nullable
	private String status;

	@Nullable
	private Boolean alleAnmeldungenKibon;

	@Nullable
	private Boolean bedarfAbgeklaert;

	@Nullable
	private Boolean ferienbetreuung;

	@Nullable
	private Boolean zugangAlle;

	@Nullable
	private String grundZugangEingeschraenkt;

	@Nullable
	private BigDecimal betreuungsstundenFaktor1;

	@Nullable
	private BigDecimal betreuungsstundenFaktor15;

	@Nullable
	private BigDecimal betreuungsstundenPaed;

	@Nullable
	private BigDecimal betreuungsstundenNichtPaed;

	@Nullable
	private BigDecimal elterngebuehrenBetreuung;

	@Nullable
	private Boolean schliessungCovid;

	@Nullable
	private BigDecimal elterngebuehrenCovid;

	@Nullable
	private BigDecimal ersteRate;

	@Nullable
	private BigDecimal gesamtkosten;

	@Nullable
	private BigDecimal elterngebuehrenVerpflegung;

	@Nullable
	private BigDecimal einnahmenDritte;

	@Nullable
	private Boolean ueberschussVorjahr;

	@Nullable
	private String ueberschussVerwendung;

	@Nullable
	private String bemerkungenKosten;

	@Nullable
	private Boolean betreuungsstundenDokumentiert;

	@Nullable
	private Boolean ElterngebuehrenTSV;

	@Nullable
	private Boolean elterngebuehrenBelege;

	@Nullable
	private Boolean elterngebuehrenMaximaltarif;

	@Nullable
	private Boolean betreuungPaedagogisch;

	@Nullable
	private Boolean ausbildungBelegt;

	@Nullable
	private String bemerkungenGemeinde;

	@Nullable
	private String betreuungsstundenPrognoseKibon;

	@Nullable
	private BigDecimal betreuungsstundenPrognose;

	private Set<LastenausgleichTagesschulenDataRow> lastenausgleichTagesschulenDaten = new HashSet<>();

	@Nullable
	public String getNameGemeinde() {
		return nameGemeinde;
	}

	public void setNameGemeinde(@Nullable String nameGemeinde) {
		this.nameGemeinde = nameGemeinde;
	}

	@Nullable
	public Long getBfsNummer() {
		return bfsNummer;
	}

	public void setBfsNummer(@Nullable Long bfsNummer) {
		this.bfsNummer = bfsNummer;
	}

	@Nullable
	public String getGemeindeFallNummer() {
		return gemeindeFallNummer;
	}

	public void setGemeindeFallNummer(@Nullable String gemeindeFallNummer) {
		this.gemeindeFallNummer = gemeindeFallNummer;
	}

	@Nullable
	public String getPeriode() {
		return periode;
	}

	public void setPeriode(@Nullable String periode) {
		this.periode = periode;
	}

	@Nullable
	public String getStatus() {
		return status;
	}

	public void setStatus(@Nullable String status) {
		this.status = status;
	}

	@Nullable
	public Boolean getAlleAnmeldungenKibon() {
		return alleAnmeldungenKibon;
	}

	public void setAlleAnmeldungenKibon(@Nullable Boolean alleAnmeldungenKibon) {
		this.alleAnmeldungenKibon = alleAnmeldungenKibon;
	}

	@Nullable
	public Boolean getBedarfAbgeklaert() {
		return bedarfAbgeklaert;
	}

	public void setBedarfAbgeklaert(@Nullable Boolean bedarfAbgeklaert) {
		this.bedarfAbgeklaert = bedarfAbgeklaert;
	}

	@Nullable
	public Boolean getFerienbetreuung() {
		return ferienbetreuung;
	}

	public void setFerienbetreuung(@Nullable Boolean ferienbetreuung) {
		this.ferienbetreuung = ferienbetreuung;
	}

	@Nullable
	public Boolean getZugangAlle() {
		return zugangAlle;
	}

	public void setZugangAlle(@Nullable Boolean zugangAlle) {
		this.zugangAlle = zugangAlle;
	}

	@Nullable
	public String getGrundZugangEingeschraenkt() {
		return grundZugangEingeschraenkt;
	}

	public void setGrundZugangEingeschraenkt(@Nullable String grundZugangEingeschraenkt) {
		this.grundZugangEingeschraenkt = grundZugangEingeschraenkt;
	}

	@Nullable
	public BigDecimal getBetreuungsstundenFaktor1() {
		return betreuungsstundenFaktor1;
	}

	public void setBetreuungsstundenFaktor1(@Nullable BigDecimal betreuungsstundenFaktor1) {
		this.betreuungsstundenFaktor1 = betreuungsstundenFaktor1;
	}

	@Nullable
	public BigDecimal getBetreuungsstundenFaktor15() {
		return betreuungsstundenFaktor15;
	}

	public void setBetreuungsstundenFaktor15(@Nullable BigDecimal betreuungsstundenFaktor15) {
		this.betreuungsstundenFaktor15 = betreuungsstundenFaktor15;
	}

	@Nullable
	public BigDecimal getBetreuungsstundenPaed() {
		return betreuungsstundenPaed;
	}

	public void setBetreuungsstundenPaed(@Nullable BigDecimal betreuungsstundenPaed) {
		this.betreuungsstundenPaed = betreuungsstundenPaed;
	}

	@Nullable
	public BigDecimal getBetreuungsstundenNichtPaed() {
		return betreuungsstundenNichtPaed;
	}

	public void setBetreuungsstundenNichtPaed(@Nullable BigDecimal betreuungsstundenNichtPaed) {
		this.betreuungsstundenNichtPaed = betreuungsstundenNichtPaed;
	}

	@Nullable
	public BigDecimal getElterngebuehrenBetreuung() {
		return elterngebuehrenBetreuung;
	}

	public void setElterngebuehrenBetreuung(@Nullable BigDecimal elterngebuehrenBetreuung) {
		this.elterngebuehrenBetreuung = elterngebuehrenBetreuung;
	}

	@Nullable
	public Boolean getSchliessungCovid() {
		return schliessungCovid;
	}

	public void setSchliessungCovid(@Nullable Boolean schliessungCovid) {
		this.schliessungCovid = schliessungCovid;
	}

	@Nullable
	public BigDecimal getElterngebuehrenCovid() {
		return elterngebuehrenCovid;
	}

	public void setElterngebuehrenCovid(@Nullable BigDecimal elterngebuehrenCovid) {
		this.elterngebuehrenCovid = elterngebuehrenCovid;
	}

	@Nullable
	public BigDecimal getErsteRate() {
		return ersteRate;
	}

	public void setErsteRate(@Nullable BigDecimal ersteRate) {
		this.ersteRate = ersteRate;
	}

	@Nullable
	public BigDecimal getGesamtkosten() {
		return gesamtkosten;
	}

	public void setGesamtkosten(@Nullable BigDecimal gesamtkosten) {
		this.gesamtkosten = gesamtkosten;
	}

	@Nullable
	public BigDecimal getElterngebuehrenVerpflegung() {
		return elterngebuehrenVerpflegung;
	}

	public void setElterngebuehrenVerpflegung(@Nullable BigDecimal elterngebuehrenVerpflegung) {
		this.elterngebuehrenVerpflegung = elterngebuehrenVerpflegung;
	}

	@Nullable
	public BigDecimal getEinnahmenDritte() {
		return einnahmenDritte;
	}

	public void setEinnahmenDritte(@Nullable BigDecimal einnahmenDritte) {
		this.einnahmenDritte = einnahmenDritte;
	}

	@Nullable
	public Boolean getUeberschussVorjahr() {
		return ueberschussVorjahr;
	}

	public void setUeberschussVorjahr(@Nullable Boolean ueberschussVorjahr) {
		this.ueberschussVorjahr = ueberschussVorjahr;
	}

	@Nullable
	public String getUeberschussVerwendung() {
		return ueberschussVerwendung;
	}

	public void setUeberschussVerwendung(@Nullable String ueberschussVerwendung) {
		this.ueberschussVerwendung = ueberschussVerwendung;
	}

	@Nullable
	public String getBemerkungenKosten() {
		return bemerkungenKosten;
	}

	public void setBemerkungenKosten(@Nullable String bemerkungenKosten) {
		this.bemerkungenKosten = bemerkungenKosten;
	}

	@Nullable
	public Boolean getBetreuungsstundenDokumentiert() {
		return betreuungsstundenDokumentiert;
	}

	public void setBetreuungsstundenDokumentiert(@Nullable Boolean betreuungsstundenDokumentiert) {
		this.betreuungsstundenDokumentiert = betreuungsstundenDokumentiert;
	}

	@Nullable
	public Boolean getElterngebuehrenTSV() {
		return ElterngebuehrenTSV;
	}

	public void setElterngebuehrenTSV(@Nullable Boolean elterngebuehrenTSV) {
		ElterngebuehrenTSV = elterngebuehrenTSV;
	}

	@Nullable
	public Boolean getElterngebuehrenBelege() {
		return elterngebuehrenBelege;
	}

	public void setElterngebuehrenBelege(@Nullable Boolean elterngebuehrenBelege) {
		this.elterngebuehrenBelege = elterngebuehrenBelege;
	}

	@Nullable
	public Boolean getElterngebuehrenMaximaltarif() {
		return elterngebuehrenMaximaltarif;
	}

	public void setElterngebuehrenMaximaltarif(@Nullable Boolean elterngebuehrenMaximaltarif) {
		this.elterngebuehrenMaximaltarif = elterngebuehrenMaximaltarif;
	}

	@Nullable
	public Boolean getBetreuungPaedagogisch() {
		return betreuungPaedagogisch;
	}

	public void setBetreuungPaedagogisch(@Nullable Boolean betreuungPaedagogisch) {
		this.betreuungPaedagogisch = betreuungPaedagogisch;
	}

	@Nullable
	public Boolean getAusbildungBelegt() {
		return ausbildungBelegt;
	}

	public void setAusbildungBelegt(@Nullable Boolean ausbildungBelegt) {
		this.ausbildungBelegt = ausbildungBelegt;
	}

	@Nullable
	public String getBemerkungenGemeinde() {
		return bemerkungenGemeinde;
	}

	public void setBemerkungenGemeinde(@Nullable String bemerkungenGemeinde) {
		this.bemerkungenGemeinde = bemerkungenGemeinde;
	}

	@Nullable
	public String getBetreuungsstundenPrognoseKibon() {
		return betreuungsstundenPrognoseKibon;
	}

	public void setBetreuungsstundenPrognoseKibon(@Nullable String betreuungsstundenPrognoseKibon) {
		this.betreuungsstundenPrognoseKibon = betreuungsstundenPrognoseKibon;
	}

	public Set<LastenausgleichTagesschulenDataRow> getLastenausgleichTagesschulenDaten() {
		return lastenausgleichTagesschulenDaten;
	}

	public void setLastenausgleichTagesschulenDaten(Set<LastenausgleichTagesschulenDataRow> lastenausgleichTagesschulenDaten) {
		this.lastenausgleichTagesschulenDaten = lastenausgleichTagesschulenDaten;
	}

	@Nullable
	public BigDecimal getBetreuungsstundenPrognose() {
		return betreuungsstundenPrognose;
	}

	public void setBetreuungsstundenPrognose(@Nullable BigDecimal betreuungsstundenPrognose) {
		this.betreuungsstundenPrognose = betreuungsstundenPrognose;
	}
}
