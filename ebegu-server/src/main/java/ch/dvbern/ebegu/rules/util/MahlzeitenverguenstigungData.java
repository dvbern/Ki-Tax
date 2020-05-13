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

package ch.dvbern.ebegu.rules.util;

import java.math.BigDecimal;

import com.google.common.collect.RangeMap;

public class MahlzeitenverguenstigungData {

	private boolean enabled;

	private RangeMap<BigDecimal, BigDecimal> verguenstigungProHauptmahlzeit;

	private RangeMap<BigDecimal, BigDecimal> verguenstigungProNebenmahlzeit;

	private BigDecimal minimalerElternbeitragHauptmahlzeit;

	private BigDecimal minimalerElternbeitragNebenmahlzeit;

	private BigDecimal tarifProHauptmahlzeit;

	private BigDecimal tarifProNebenmahlzeit;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public RangeMap<BigDecimal, BigDecimal> getVerguenstigungProHauptmahlzeit() {
		return verguenstigungProHauptmahlzeit;
	}

	public void setVerguenstigungProHauptmahlzeit(RangeMap<BigDecimal, BigDecimal> verguenstigungProHauptmahlzeit) {
		this.verguenstigungProHauptmahlzeit = verguenstigungProHauptmahlzeit;
	}

	public RangeMap<BigDecimal, BigDecimal> getVerguenstigungProNebenmahlzeit() {
		return verguenstigungProNebenmahlzeit;
	}

	public void setVerguenstigungProNebenmahlzeit(RangeMap<BigDecimal, BigDecimal> verguenstigungProNebenmahlzeit) {
		this.verguenstigungProNebenmahlzeit = verguenstigungProNebenmahlzeit;
	}

	public BigDecimal getMinimalerElternbeitragHauptmahlzeit() {
		return minimalerElternbeitragHauptmahlzeit;
	}

	public void setMinimalerElternbeitragHauptmahlzeit(BigDecimal minimalerElternbeitragHauptmahlzeit) {
		this.minimalerElternbeitragHauptmahlzeit = minimalerElternbeitragHauptmahlzeit;
	}

	public BigDecimal getMinimalerElternbeitragNebenmahlzeit() {
		return minimalerElternbeitragNebenmahlzeit;
	}

	public void setMinimalerElternbeitragNebenmahlzeit(BigDecimal minimalerElternbeitragNebenmahlzeit) {
		this.minimalerElternbeitragNebenmahlzeit = minimalerElternbeitragNebenmahlzeit;
	}

	public BigDecimal getTarifProHauptmahlzeit() {
		return tarifProHauptmahlzeit;
	}

	public void setTarifProHauptmahlzeit(BigDecimal tarifProHauptmahlzeit) {
		this.tarifProHauptmahlzeit = tarifProHauptmahlzeit;
	}

	public BigDecimal getTarifProNebenmahlzeit() {
		return tarifProNebenmahlzeit;
	}

	public void setTarifProNebenmahlzeit(BigDecimal tarifProNebenmahlzeit) {
		this.tarifProNebenmahlzeit = tarifProNebenmahlzeit;
	}
}
