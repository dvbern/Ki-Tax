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
	@Nullable private String periode;
	@Nullable private String angebot;
	@Nullable private String traegerschaft;
	@Nullable private BigDecimal tageSonderschueler;
	@Nullable private BigDecimal chfSonderschueler;
	@Nullable private BigDecimal tageOhneSonderschueler;
	@Nullable private BigDecimal chfOhneSonderschueler;
	@Nullable private BigDecimal totalTage;
	@Nullable private BigDecimal totalChf;
	@Nullable private String iban;
	@Nullable private String folgePeriode;


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
	public String getFallNummer() {
		return fallNummer;
	}

	public void setFallNummer(@Nullable String fallNummer) {
		this.fallNummer = fallNummer;
	}

	@Nullable
	public String getPeriode() {
		return periode;
	}

	public void setPeriode(@Nullable String periode) {
		this.periode = periode;
	}

	@Nullable
	public String getAngebot() {
		return angebot;
	}

	public void setAngebot(@Nullable String angebot) {
		this.angebot = angebot;
	}

	@Nullable
	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Nullable
	public BigDecimal getTageSonderschueler() {
		return tageSonderschueler;
	}

	public void setTageSonderschueler(@Nullable BigDecimal tageSonderschueler) {
		this.tageSonderschueler = tageSonderschueler;
	}

	@Nullable
	public BigDecimal getChfSonderschueler() {
		return chfSonderschueler;
	}

	public void setChfSonderschueler(@Nullable BigDecimal chfSonderschueler) {
		this.chfSonderschueler = chfSonderschueler;
	}

	@Nullable
	public BigDecimal getTageOhneSonderschueler() {
		return tageOhneSonderschueler;
	}

	public void setTageOhneSonderschuelertage(@Nullable BigDecimal tageOhneSonderschueler) {
		this.tageOhneSonderschueler = tageOhneSonderschueler;
	}

	@Nullable
	public BigDecimal getChfOhneSonderschueler() {
		return chfOhneSonderschueler;
	}

	public void setChfOhneSonderschueler(@Nullable BigDecimal chfOhneSonderschueler) {
		this.chfOhneSonderschueler = chfOhneSonderschueler;
	}

	@Nullable
	public BigDecimal getTotalTage() {
		return totalTage;
	}

	public void setTotalTage(@Nullable BigDecimal totalTage) {
		this.totalTage = totalTage;
	}

	@Nullable
	public BigDecimal getTotalChf() {
		return totalChf;
	}

	public void setTotalChf(@Nullable BigDecimal totalChf) {
		this.totalChf = totalChf;
	}

	@Nullable
	public String getIban() {
		return iban;
	}

	public void setIban(@Nullable String iban) {
		this.iban = iban;
	}

	@Nullable
	public String getFolgePeriode() {
		return folgePeriode;
	}

	public void setFolgePeriode(@Nullable String folgePeriode) {
		this.folgePeriode = folgePeriode;
	}
}
