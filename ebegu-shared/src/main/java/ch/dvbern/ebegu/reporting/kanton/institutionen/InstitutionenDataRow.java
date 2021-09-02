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

package ch.dvbern.ebegu.reporting.kanton.institutionen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Nullable;

public class InstitutionenDataRow {
	private String typ = null;
	private String traegerschaft = null;
	@Nullable
	private String traegerschaftEmail = null;
	@Nullable
	private String familienportalEmail = null;
	private Boolean emailBenachrichtigungenKiBon = null;
	private String email = null;
	private String name = null;
	private String anschrift = null;
	private String strasse = null;
	private String plz = null;
	private String ort = null;
	@Nullable
	private String gemeinde = null;
	@Nullable private
	Long bfsGemeinde = null;
	private String telefon = null;
	private String url = null;
	private String oeffnungstageProJahr = null;
	@Nullable 
	private LocalDate gueltigAb = null;
	@Nullable
	private LocalDate gueltigBis = null;
	@Nullable
	private String grundSchliessung = null;
	private String oeffnungstage = null;
	private String oeffnungszeitAb = null;
	private String oeffnungszeitBis = null;
	private String ausserordentlicheOeffnungszeiten = null;
	@Nullable
	private String oeffnungsAbweichungen = null;
	private Boolean baby = null;
	private Boolean vorschulkind = null;
	private Boolean kindergarten = null;
	private Boolean schulkind = null;
	private Boolean subventioniert = null;
	private BigDecimal kapazitaet = null;
	private BigDecimal reserviertFuerFirmen = null;
	private LocalDateTime zuletztGeaendert = null;
	private BigDecimal auslastung = null;
	private BigDecimal anzahlKinderWarteliste = null;
	private BigDecimal summePensumWarteliste = null;
	private BigDecimal dauerWarteliste = null;

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	public String getStrasse() {
		return strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public String getPlz() {
		return plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
	}

	public String getOrt() {
		return ort;
	}

	public void setOrt(String ort) {
		this.ort = ort;
	}

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Nullable
	public LocalDate getGueltigAb() {
		return gueltigAb;
	}

	public void setGueltigAb(@Nullable LocalDate gueltigAb) {
		this.gueltigAb = gueltigAb;
	}

	@Nullable
	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(@Nullable LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	public String getOeffnungstage() {
		return oeffnungstage;
	}

	public void setOeffnungstage(String oeffnungstage) {
		this.oeffnungstage = oeffnungstage;
	}

	@Nullable
	public
	String getOeffnungsAbweichungen() {
		return oeffnungsAbweichungen;
	}

	public void setOeffnungsAbweichungen(@Nullable String oeffnungsAbweichungen) {
		this.oeffnungsAbweichungen = oeffnungsAbweichungen;
	}

	public Boolean getBaby() {
		return baby;
	}

	public void setBaby(Boolean baby) {
		this.baby = baby;
	}

	public Boolean getVorschulkind() {
		return vorschulkind;
	}

	public void setVorschulkind(Boolean vorschulkind) {
		this.vorschulkind = vorschulkind;
	}

	public Boolean getSchulkind() {
		return schulkind;
	}

	public void setSchulkind(Boolean schulkind) {
		this.schulkind = schulkind;
	}

	public BigDecimal getKapazitaet() {
		return kapazitaet;
	}

	public void setKapazitaet(BigDecimal kapazitaet) {
		this.kapazitaet = kapazitaet;
	}

	public BigDecimal getReserviertFuerFirmen() {
		return reserviertFuerFirmen;
	}

	public void setReserviertFuerFirmen(BigDecimal reserviertFuerFirmen) {
		this.reserviertFuerFirmen = reserviertFuerFirmen;
	}

	public LocalDateTime getZuletztGeaendert() {
		return zuletztGeaendert;
	}

	public void setZuletztGeaendert(LocalDateTime zuletztGeaendert) {
		this.zuletztGeaendert = zuletztGeaendert;
	}

	public Boolean getSubventioniert() {
		return subventioniert;
	}

	public void setSubventioniert(Boolean subventioniert) {
		this.subventioniert = subventioniert;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAnschrift() {
		return anschrift;
	}

	public void setAnschrift(String anschrift) {
		this.anschrift = anschrift;
	}

	public Boolean getKindergarten() {
		return kindergarten;
	}

	public void setKindergarten(Boolean kindergarten) {
		this.kindergarten = kindergarten;
	}

	@Nullable
	public String getTraegerschaftEmail() {
		return traegerschaftEmail;
	}

	public void setTraegerschaftEmail(@Nullable String traegerschaftEmail) {
		this.traegerschaftEmail = traegerschaftEmail;
	}

	@Nullable
	public String getFamilienportalEmail() {
		return familienportalEmail;
	}

	public void setFamilienportalEmail(@Nullable String familienportalEmail) {
		this.familienportalEmail = familienportalEmail;
	}

	public Boolean getEmailBenachrichtigungenKiBon() {
		return emailBenachrichtigungenKiBon;
	}

	public void setEmailBenachrichtigungenKiBon(Boolean emailBenachrichtigungenKiBon) {
		this.emailBenachrichtigungenKiBon = emailBenachrichtigungenKiBon;
	}

	@Nullable
	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable String gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nullable
	public Long getBfsGemeinde() {
		return bfsGemeinde;
	}

	public void setBfsGemeinde(@Nullable Long bfsGemeinde) {
		this.bfsGemeinde = bfsGemeinde;
	}

	public String getOeffnungstageProJahr() {
		return oeffnungstageProJahr;
	}

	public void setOeffnungstageProJahr(String oeffnungstageProJahr) {
		this.oeffnungstageProJahr = oeffnungstageProJahr;
	}

	public String getOeffnungszeitAb() {
		return oeffnungszeitAb;
	}

	public void setOeffnungszeitAb(String oeffnungszeitAb) {
		this.oeffnungszeitAb = oeffnungszeitAb;
	}

	public String getOeffnungszeitBis() {
		return oeffnungszeitBis;
	}

	public void setOeffnungszeitBis(String oeffnungszeitBis) {
		this.oeffnungszeitBis = oeffnungszeitBis;
	}

	public String getAusserordentlicheOeffnungszeiten() {
		return ausserordentlicheOeffnungszeiten;
	}

	public void setAusserordentlicheOeffnungszeiten(String ausserordentlicheOeffnungszeiten) {
		this.ausserordentlicheOeffnungszeiten = ausserordentlicheOeffnungszeiten;
	}

	public BigDecimal getAuslastung() {
		return auslastung;
	}

	public void setAuslastung(BigDecimal auslastung) {
		this.auslastung = auslastung;
	}

	public BigDecimal getAnzahlKinderWarteliste() {
		return anzahlKinderWarteliste;
	}

	public void setAnzahlKinderWarteliste(BigDecimal anzahlKinderWarteliste) {
		this.anzahlKinderWarteliste = anzahlKinderWarteliste;
	}

	public BigDecimal getSummePensumWarteliste() {
		return summePensumWarteliste;
	}

	public void setSummePensumWarteliste(BigDecimal summePensumWarteliste) {
		this.summePensumWarteliste = summePensumWarteliste;
	}

	public BigDecimal getDauerWarteliste() {
		return dauerWarteliste;
	}

	public void setDauerWarteliste(BigDecimal dauerWarteliste) {
		this.dauerWarteliste = dauerWarteliste;
	}

	public String getGrundSchliessung() {
		return grundSchliessung;
	}

	public void setGrundSchliessung(@Nullable String grundSchliessung) {
		this.grundSchliessung = grundSchliessung;
	}
}
