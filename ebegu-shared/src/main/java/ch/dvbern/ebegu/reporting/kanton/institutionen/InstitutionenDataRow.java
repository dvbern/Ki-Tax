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
import java.time.LocalDateTime;

public class InstitutionenDataRow {
	private String typ = null;
	private String traegerschaft = null;
	private String name = null;
	private String anschrift = null;
	private String strasse = null;
	private String plz = null;
	private String ort = null;
	private String telefon = null;
	private String email = null;
	private String url = null;
	private String oeffnungstage = null;
	private String oeffnungszeiten = null;
	private String oeffnungsabweichungen = null;
	private Boolean baby = null;
	private Boolean vorschulkind = null;
	private Boolean kindergarten = null;
	private Boolean schulkind = null;
	private Boolean subventioniert = null;
	private BigDecimal kapazitaet = null;
	private BigDecimal reserviertFuerFirmen = null;
	private LocalDateTime zuletztGeaendert = null;

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

	public String getOeffnungstage() {
		return oeffnungstage;
	}

	public void setOeffnungstage(String oeffnungstage) {
		this.oeffnungstage = oeffnungstage;
	}

	public String getOeffnungszeiten() {
		return oeffnungszeiten;
	}

	public void setOeffnungszeiten(String oeffnungszeiten) {
		this.oeffnungszeiten = oeffnungszeiten;
	}

	public String getOeffnungsabweichungen() {
		return oeffnungsabweichungen;
	}

	public void setOeffnungsabweichungen(String oeffnungsabweichungen) {
		this.oeffnungsabweichungen = oeffnungsabweichungen;
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
}
