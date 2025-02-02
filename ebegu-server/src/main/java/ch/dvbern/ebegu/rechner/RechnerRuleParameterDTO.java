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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;

/**
 * Kapselung aller Parameter, welche für die Gemeinde Rechner benötigt werden.
 */
public final class RechnerRuleParameterDTO {

	private BigDecimal zusaetzlicherGutscheinGemeindeBetrag = BigDecimal.ZERO;
	private BigDecimal zusaetzlicherBabyGutscheinBetrag = BigDecimal.ZERO;
	private BigDecimal verguenstigungMahlzeitenTotal = BigDecimal.ZERO;
	private BigDecimal minimalPauschalBetrag = BigDecimal.ZERO;

	public void reset() {
		this.zusaetzlicherGutscheinGemeindeBetrag = BigDecimal.ZERO;
		this.zusaetzlicherBabyGutscheinBetrag = BigDecimal.ZERO;
		this.setVerguenstigungMahlzeitenTotal(BigDecimal.ZERO);
	}

	public BigDecimal getZusaetzlicherGutscheinGemeindeBetrag() {
		return zusaetzlicherGutscheinGemeindeBetrag;
	}

	public void setZusaetzlicherGutscheinGemeindeBetrag(BigDecimal zusaetzlicherGutscheinGemeindeBetrag) {
		this.zusaetzlicherGutscheinGemeindeBetrag = zusaetzlicherGutscheinGemeindeBetrag;
	}

	public BigDecimal getZusaetzlicherBabyGutscheinBetrag() {
		return zusaetzlicherBabyGutscheinBetrag;
	}

	public void setZusaetzlicherBabyGutscheinBetrag(BigDecimal zusaetzlicherBabyGutscheinBetrag) {
		this.zusaetzlicherBabyGutscheinBetrag = zusaetzlicherBabyGutscheinBetrag;
	}

	public BigDecimal getVerguenstigungMahlzeitenTotal() {
		return verguenstigungMahlzeitenTotal;
	}

	public void setVerguenstigungMahlzeitenTotal(BigDecimal verguenstigungMahlzeitenTotal) {
		this.verguenstigungMahlzeitenTotal = verguenstigungMahlzeitenTotal;
	}

	public BigDecimal getMinimalPauschalBetrag() {
		return minimalPauschalBetrag;
	}

	public void setMinimalPauschalBetrag(BigDecimal minimalPauschalBetrag) {
		this.minimalPauschalBetrag = minimalPauschalBetrag;
	}
}
