/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.zahlungen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO für den Lastenausgleich von KiBon
 */
public class ZahlungenDataRow {
	private String zahlungslaufTitle;
	private String gemeinde;
	private String institution;
	private LocalDateTime timestampZahlungslauf;
	private String kindVorname;
	private String kindNachname;
	private String referenznummer;
	private LocalDate zeitabschnittVon;
	private LocalDate zeitabschnittBis;
	private BigDecimal bgPensum;
	private BigDecimal betrag;
	private Boolean korrektur;
	private Boolean ignorieren;
	private String ibanEltern;
	private String kontoEltern;

	public String getZahlungslaufTitle() {
		return zahlungslaufTitle;
	}

	public void setZahlungslaufTitle(String zahlungslaufTitle) {
		this.zahlungslaufTitle = zahlungslaufTitle;
	}

	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(String gemeinde) {
		this.gemeinde = gemeinde;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public LocalDateTime getTimestampZahlungslauf() {
		return timestampZahlungslauf;
	}

	public void setTimestampZahlungslauf(LocalDateTime timestampZahlungslauf) {
		this.timestampZahlungslauf = timestampZahlungslauf;
	}

	public String getKindVorname() {
		return kindVorname;
	}

	public void setKindVorname(String kindVorname) {
		this.kindVorname = kindVorname;
	}

	public String getKindNachname() {
		return kindNachname;
	}

	public void setKindNachname(String kindNachname) {
		this.kindNachname = kindNachname;
	}

	public String getReferenznummer() {
		return referenznummer;
	}

	public void setReferenznummer(String referenznummer) {
		this.referenznummer = referenznummer;
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

	public BigDecimal getBetrag() {
		return betrag;
	}

	public void setBetrag(BigDecimal betrag) {
		this.betrag = betrag;
	}

	public Boolean getKorrektur() {
		return korrektur;
	}

	public void setKorrektur(Boolean korrektur) {
		this.korrektur = korrektur;
	}

	public Boolean getIgnorieren() {
		return ignorieren;
	}

	public void setIgnorieren(Boolean ignorieren) {
		this.ignorieren = ignorieren;
	}

	public String getIbanEltern() {
		return ibanEltern;
	}

	public void setIbanEltern(String ibanEltern) {
		this.ibanEltern = ibanEltern;
	}

	public String getKontoEltern() {
		return kontoEltern;
	}

	public void setKontoEltern(String kontoEltern) {
		this.kontoEltern = kontoEltern;
	}
}
