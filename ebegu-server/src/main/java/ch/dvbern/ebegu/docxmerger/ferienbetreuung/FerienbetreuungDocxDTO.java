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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.docxmerger.ferienbetreuung;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public class FerienbetreuungDocxDTO {
	@Nullable private String userName;
	@Nullable private String userEmail;
	@Nullable private String gemeindeAnschrift;
	@Nullable private String gemeindeStrasse;
	@Nullable private String gemeindeNr;
	@Nullable private String gemeindePLZ;
	@Nullable private String gemeindeOrt;
	@Nullable private String fallNummer;
	@Nullable private String gemeindeName;
	@Nullable private BigDecimal betreuungsstunden;
	@Nullable private BigDecimal betreuungsstundenProg;
	@Nullable private String normlohnkosten;
	@Nullable private String normlohnkostenProg;
	@Nullable private BigDecimal normlohnkostenTotal;
	@Nullable private BigDecimal normlohnkostenTotalProg;
	@Nullable private BigDecimal elterngebuehren;
	@Nullable private BigDecimal elterngebuehrenProg;
	@Nullable private BigDecimal lastenausgleichsberechtigterBetrag;
	@Nullable private BigDecimal lastenausgleichsberechtigterBetragProg;
	@Nullable private BigDecimal ersteRate;
	@Nullable private BigDecimal ersteRateProg;
	@Nullable private BigDecimal zweiteRate;
	@Nullable private BigDecimal auszahlungTotal;
	@Nullable private String textPaedagogischOderNicht;

	public FerienbetreuungDocxDTO() {
	}

	@Nullable
	public String getUserName() {
		return userName;
	}

	public void setUserName(@Nullable String userName) {
		this.userName = userName;
	}

	@Nullable
	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(@Nullable String userEmail) {
		this.userEmail = userEmail;
	}

	@Nullable
	public String getGemeindeAnschrift() {
		return gemeindeAnschrift;
	}

	public void setGemeindeAnschrift(@Nullable String gemeindeAnschrift) {
		this.gemeindeAnschrift = gemeindeAnschrift;
	}

	@Nullable
	public String getGemeindeStrasse() {
		return gemeindeStrasse;
	}

	public void setGemeindeStrasse(@Nullable String gemeindeStrasse) {
		this.gemeindeStrasse = gemeindeStrasse;
	}

	@Nullable
	public String getGemeindeNr() {
		return gemeindeNr;
	}

	public void setGemeindeNr(@Nullable String gemeindeNr) {
		this.gemeindeNr = gemeindeNr;
	}

	@Nullable
	public String getGemeindePLZ() {
		return gemeindePLZ;
	}

	public void setGemeindePLZ(@Nullable String gemeindePLZ) {
		this.gemeindePLZ = gemeindePLZ;
	}

	@Nullable
	public String getGemeindeOrt() {
		return gemeindeOrt;
	}

	public void setGemeindeOrt(@Nullable String gemeindeOrt) {
		this.gemeindeOrt = gemeindeOrt;
	}

	@Nullable
	public String getGemeindeName() {
		return gemeindeName;
	}

	public void setGemeindeName(@Nullable String gemeindeName) {
		this.gemeindeName = gemeindeName;
	}

	@Nullable
	public BigDecimal getBetreuungsstunden() {
		return betreuungsstunden;
	}

	public void setBetreuungsstunden(@Nullable BigDecimal betreuungsstunden) {
		this.betreuungsstunden = betreuungsstunden;
	}

	@Nullable
	public BigDecimal getBetreuungsstundenProg() {
		return betreuungsstundenProg;
	}

	public void setBetreuungsstundenProg(@Nullable BigDecimal betreuungsstundenProg) {
		this.betreuungsstundenProg = betreuungsstundenProg;
	}

	@Nullable
	public String getNormlohnkosten() {
		return normlohnkosten;
	}

	public void setNormlohnkosten(@Nullable String normlohnkosten) {
		this.normlohnkosten = normlohnkosten;
	}

	@Nullable
	public String getNormlohnkostenProg() {
		return normlohnkostenProg;
	}

	public void setNormlohnkostenProg(@Nullable String normlohnkostenProg) {
		this.normlohnkostenProg = normlohnkostenProg;
	}

	@Nullable
	public BigDecimal getNormlohnkostenTotal() {
		return normlohnkostenTotal;
	}

	public void setNormlohnkostenTotal(@Nullable BigDecimal normlohnkostenTotal) {
		this.normlohnkostenTotal = normlohnkostenTotal;
	}

	@Nullable
	public BigDecimal getNormlohnkostenTotalProg() {
		return normlohnkostenTotalProg;
	}

	public void setNormlohnkostenTotalProg(@Nullable BigDecimal normlohnkostenTotalProg) {
		this.normlohnkostenTotalProg = normlohnkostenTotalProg;
	}

	@Nullable
	public BigDecimal getElterngebuehren() {
		return elterngebuehren;
	}

	public void setElterngebuehren(@Nullable BigDecimal elterngebuehren) {
		this.elterngebuehren = elterngebuehren;
	}

	@Nullable
	public BigDecimal getElterngebuehrenProg() {
		return elterngebuehrenProg;
	}

	public void setElterngebuehrenProg(@Nullable BigDecimal elterngebuehrenProg) {
		this.elterngebuehrenProg = elterngebuehrenProg;
	}

	@Nullable
	public BigDecimal getLastenausgleichsberechtigterBetrag() {
		return lastenausgleichsberechtigterBetrag;
	}

	public void setLastenausgleichsberechtigterBetrag(@Nullable BigDecimal lastenausgleichsberechtigterBetrag) {
		this.lastenausgleichsberechtigterBetrag = lastenausgleichsberechtigterBetrag;
	}

	@Nullable
	public BigDecimal getLastenausgleichsberechtigterBetragProg() {
		return lastenausgleichsberechtigterBetragProg;
	}

	public void setLastenausgleichsberechtigterBetragProg(@Nullable BigDecimal lastenausgleichsberechtigterBetragProg) {
		this.lastenausgleichsberechtigterBetragProg = lastenausgleichsberechtigterBetragProg;
	}

	@Nullable
	public BigDecimal getErsteRate() {
		return ersteRate;
	}

	public void setErsteRate(@Nullable BigDecimal ersteRate) {
		this.ersteRate = ersteRate;
	}

	@Nullable
	public BigDecimal getErsteRateProg() {
		return ersteRateProg;
	}

	public void setErsteRateProg(@Nullable BigDecimal ersteRateProg) {
		this.ersteRateProg = ersteRateProg;
	}

	@Nullable
	public BigDecimal getZweiteRate() {
		return zweiteRate;
	}

	public void setZweiteRate(@Nullable BigDecimal zweiteRate) {
		this.zweiteRate = zweiteRate;
	}

	@Nullable
	public BigDecimal getAuszahlungTotal() {
		return auszahlungTotal;
	}

	public void setAuszahlungTotal(@Nullable BigDecimal auszahlungTotal) {
		this.auszahlungTotal = auszahlungTotal;
	}

	@Nullable
	public String getTextPaedagogischOderNicht() {
		return textPaedagogischOderNicht;
	}

	public void setTextPaedagogischOderNicht(@Nullable String textPaedagogischOderNicht) {
		this.textPaedagogischOderNicht = textPaedagogischOderNicht;
	}

	@Nullable
	public String getFallNummer() {
		return fallNummer;
	}

	public void setFallNummer(@Nullable String fallNummer) {
		this.fallNummer = fallNummer;
	}
}
