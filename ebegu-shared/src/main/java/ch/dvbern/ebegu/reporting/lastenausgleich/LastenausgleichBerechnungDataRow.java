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

/**
 * DTO für den Lastenausgleich von KiBon
 */
public class LastenausgleichBerechnungDataRow {

	private String gemeinde;
	private String bfsNummer;
	private String verrechnungsjahr;

	private BigDecimal totalBelegungMitSelbstbehalt;
	private BigDecimal totalGutscheineMitSelbstbehalt;
	private BigDecimal totalAnrechenbar;
	private BigDecimal kostenPro100ProzentPlatz;
	private BigDecimal selbstbehaltGemeinde;
	private BigDecimal eingabeLastenausgleich;
	private BigDecimal totalBelegungOhneSelbstbehalt;
	private BigDecimal totalGutscheineOhneSelbstbehalt;
	private BigDecimal kostenFuerSelbstbehalt;
	private boolean korrektur;

	public LastenausgleichBerechnungDataRow() {}

	protected LastenausgleichBerechnungDataRow(LastenausgleichBerechnungDataRow row) {
		this.gemeinde = row.getGemeinde();
		this.bfsNummer = row.getBfsNummer();
		this.verrechnungsjahr = row.getVerrechnungsjahr();
		this.setTotalBelegungMitSelbstbehalt(row.getTotalBelegungMitSelbstbehalt());
		this.setTotalGutscheineMitSelbstbehalt(row.getTotalGutscheineMitSelbstbehalt());
		this.totalAnrechenbar = row.getTotalAnrechenbar();
		this.kostenPro100ProzentPlatz = row.getKostenPro100ProzentPlatz();
		this.selbstbehaltGemeinde = row.getSelbstbehaltGemeinde();
		this.eingabeLastenausgleich = row.getEingabeLastenausgleich();
		this.totalBelegungOhneSelbstbehalt = row.getTotalBelegungOhneSelbstbehalt();
		this.totalGutscheineOhneSelbstbehalt = row.getTotalGutscheineOhneSelbstbehalt();
		this.kostenFuerSelbstbehalt = row.getKostenFuerSelbstbehalt();
		this.korrektur = row.isKorrektur();
	}

	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(String gemeinde) {
		this.gemeinde = gemeinde;
	}

	public String getBfsNummer() {
		return bfsNummer;
	}

	public void setBfsNummer(String bfsNummer) {
		this.bfsNummer = bfsNummer;
	}

	public String getVerrechnungsjahr() {
		return verrechnungsjahr;
	}

	public void setVerrechnungsjahr(String verrechnungsjahr) {
		this.verrechnungsjahr = verrechnungsjahr;
	}

	public BigDecimal getTotalAnrechenbar() {
		return totalAnrechenbar;
	}

	public void setTotalAnrechenbar(BigDecimal totalAnrechenbar) {
		this.totalAnrechenbar = totalAnrechenbar;
	}

	public BigDecimal getKostenPro100ProzentPlatz() {
		return kostenPro100ProzentPlatz;
	}

	public void setKostenPro100ProzentPlatz(BigDecimal kostenPro100ProzentPlatz) {
		this.kostenPro100ProzentPlatz = kostenPro100ProzentPlatz;
	}

	public BigDecimal getSelbstbehaltGemeinde() {
		return selbstbehaltGemeinde;
	}

	public void setSelbstbehaltGemeinde(BigDecimal selbstbehaltGemeinde) {
		this.selbstbehaltGemeinde = selbstbehaltGemeinde;
	}

	public BigDecimal getEingabeLastenausgleich() {
		return eingabeLastenausgleich;
	}

	public void setEingabeLastenausgleich(BigDecimal eingabeLastenausgleich) {
		this.eingabeLastenausgleich = eingabeLastenausgleich;
	}

	public boolean isKorrektur() {
		return korrektur;
	}

	public void setKorrektur(boolean korrektur) {
		this.korrektur = korrektur;
	}

	public BigDecimal getTotalBelegungMitSelbstbehalt() {
		return totalBelegungMitSelbstbehalt;
	}

	public void setTotalBelegungMitSelbstbehalt(BigDecimal totalBelegungMitSelbstbehalt) {
		this.totalBelegungMitSelbstbehalt = totalBelegungMitSelbstbehalt;
	}

	public BigDecimal getTotalGutscheineMitSelbstbehalt() {
		return totalGutscheineMitSelbstbehalt;
	}

	public void setTotalGutscheineMitSelbstbehalt(BigDecimal totalGutscheineMitSelbstbehalt) {
		this.totalGutscheineMitSelbstbehalt = totalGutscheineMitSelbstbehalt;
	}

	public BigDecimal getTotalBelegungOhneSelbstbehalt() {
		return totalBelegungOhneSelbstbehalt;
	}

	public void setTotalBelegungOhneSelbstbehalt(BigDecimal totalBelegungOhneSelbstbehalt) {
		this.totalBelegungOhneSelbstbehalt = totalBelegungOhneSelbstbehalt;
	}

	public BigDecimal getTotalGutscheineOhneSelbstbehalt() {
		return totalGutscheineOhneSelbstbehalt;
	}

	public void setTotalGutscheineOhneSelbstbehalt(BigDecimal totalGutscheineOhneSelbstbehalt) {
		this.totalGutscheineOhneSelbstbehalt = totalGutscheineOhneSelbstbehalt;
	}

	public BigDecimal getKostenFuerSelbstbehalt() {
		return kostenFuerSelbstbehalt;
	}

	public void setKostenFuerSelbstbehalt(BigDecimal kostenFuerSelbstbehalt) {
		this.kostenFuerSelbstbehalt = kostenFuerSelbstbehalt;
	}
}
