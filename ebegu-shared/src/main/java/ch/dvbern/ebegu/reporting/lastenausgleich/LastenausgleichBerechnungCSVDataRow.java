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
public class LastenausgleichBerechnungCSVDataRow extends LastenausgleichBerechnungDataRow {

	private BigDecimal totalRevision;
	private BigDecimal totalBelegung;
	private BigDecimal totalGutscheine;

	public LastenausgleichBerechnungCSVDataRow() {};

	public LastenausgleichBerechnungCSVDataRow(LastenausgleichBerechnungDataRow parent) {
		super(parent);
	}

	public BigDecimal getTotalRevision() {
		return totalRevision;
	}

	public void setTotalRevision(BigDecimal totalRevision) {
		this.totalRevision = totalRevision;
	}

	public BigDecimal getTotalBelegung() {
		return totalBelegung;
	}

	public void setTotalBelegung(BigDecimal totalBelegung) {
		this.totalBelegung = totalBelegung;
	}

	public BigDecimal getTotalGutscheine() {
		return totalGutscheine;
	}

	public void setTotalGutscheine(BigDecimal totalGutscheine) {
		this.totalGutscheine = totalGutscheine;
	}
}
