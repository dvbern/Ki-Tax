
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

package ch.dvbern.ebegu.dto.personensuche;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * DTO für Adressen aus dem EWK
 */
@XmlRootElement(name = "ewkAdresse")
@XmlAccessorType(XmlAccessType.FIELD)
public class EWKAdresse implements Serializable {

	private static final long serialVersionUID = -2070439419700535368L;

	protected String adresszusatz1;
	protected String adresszusatz2;
	protected String hausnummer;
	protected String wohnungsnummer;
	protected String strasse;
	protected String postleitzahl;
	protected String ort;
	protected String gebiet;
	protected Long wohnungsId;
	protected Long gebaeudeId;

	public EWKAdresse() {
	}

	public String getAdresszusatz1() {
		return adresszusatz1;
	}

	public void setAdresszusatz1(String adresszusatz1) {
		this.adresszusatz1 = adresszusatz1;
	}

	public String getAdresszusatz2() {
		return adresszusatz2;
	}

	public void setAdresszusatz2(String adresszusatz2) {
		this.adresszusatz2 = adresszusatz2;
	}

	public String getHausnummer() {
		return hausnummer;
	}

	public void setHausnummer(String hausnummer) {
		this.hausnummer = hausnummer;
	}

	public String getWohnungsnummer() {
		return wohnungsnummer;
	}

	public void setWohnungsnummer(String wohnungsnummer) {
		this.wohnungsnummer = wohnungsnummer;
	}

	public String getStrasse() {
		return strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public String getPostleitzahl() {
		return postleitzahl;
	}

	public void setPostleitzahl(String postleitzahl) {
		this.postleitzahl = postleitzahl;
	}

	public String getOrt() {
		return ort;
	}

	public void setOrt(String ort) {
		this.ort = ort;
	}

	public String getGebiet() {
		return gebiet;
	}

	public void setGebiet(String gebiet) {
		this.gebiet = gebiet;
	}

	public Long getWohnungsId() {
		return wohnungsId;
	}

	public void setWohnungsId(Long wohnungsId) {
		this.wohnungsId = wohnungsId;
	}

	public Long getGebaeudeId() {
		return gebaeudeId;
	}

	public void setGebaeudeId(Long gebaeudeId) {
		this.gebaeudeId = gebaeudeId;
	}
}
