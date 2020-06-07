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

import javax.annotation.Nonnull;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class MahlzeitenverguenstigungParameter {

	private boolean enabled = false;

	private boolean enabledFuerSozHilfeBez = false;

	private RangeMap<BigDecimal, BigDecimal> verguenstigungProHauptmahlzeit = TreeRangeMap.create();

	private RangeMap<BigDecimal, BigDecimal> verguenstigungProNebenmahlzeit = TreeRangeMap.create();

	private BigDecimal minimalerElternbeitragHauptmahlzeit = BigDecimal.ZERO;

	private BigDecimal minimalerElternbeitragNebenmahlzeit = BigDecimal.ZERO;

	public MahlzeitenverguenstigungParameter(
		boolean enabled,
		boolean enabledFuerSozHilfeBez,
		BigDecimal maxEinkommenStufe1,
		BigDecimal maxEinkommenStufe2,
		BigDecimal verguenstigungStufe1Hauptmahlzeit,
		BigDecimal verguenstigungStufe2Hauptmahlzeit,
		BigDecimal verguenstigungStufe3Hauptmahlzeit,
		BigDecimal verguenstigungStufe1Nebenmahlzeit,
		BigDecimal verguenstigungStufe2Nebenmahlzeit,
		BigDecimal verguenstigungStufe3Nebenmahlzeit,
		BigDecimal minimalerElternbeitragHauptmahlzeit,
		BigDecimal minimalerElternbeitragNebenmahlzeit
	) {
		this.enabled = enabled;
		this.enabledFuerSozHilfeBez = enabledFuerSozHilfeBez;
		verguenstigungProHauptmahlzeit.put(Range.closed(BigDecimal.valueOf(Integer.MAX_VALUE).negate(), maxEinkommenStufe1), verguenstigungStufe1Hauptmahlzeit);
		verguenstigungProHauptmahlzeit.put(Range.closed(maxEinkommenStufe1.add(BigDecimal.ONE), maxEinkommenStufe2), verguenstigungStufe2Hauptmahlzeit);
		verguenstigungProHauptmahlzeit.put(Range.closed(maxEinkommenStufe2.add(BigDecimal.ONE), BigDecimal.valueOf(Integer.MAX_VALUE)),	verguenstigungStufe3Hauptmahlzeit);

		verguenstigungProNebenmahlzeit.put(Range.closed(BigDecimal.valueOf(Integer.MAX_VALUE).negate(), maxEinkommenStufe1), verguenstigungStufe1Nebenmahlzeit);
		verguenstigungProNebenmahlzeit.put(Range.closed(maxEinkommenStufe1.add(BigDecimal.ONE), maxEinkommenStufe2), verguenstigungStufe2Nebenmahlzeit);
		verguenstigungProNebenmahlzeit.put(Range.closed(maxEinkommenStufe2.add(BigDecimal.ONE), BigDecimal.valueOf(Integer.MAX_VALUE)),	verguenstigungStufe3Nebenmahlzeit);

		this.minimalerElternbeitragHauptmahlzeit = minimalerElternbeitragHauptmahlzeit;
		this.minimalerElternbeitragNebenmahlzeit = minimalerElternbeitragNebenmahlzeit;
	}

	public boolean hasAnspruch(BigDecimal massgebendesEinkommen, boolean sozialhilfebezueger) {

		if (getVerguenstigungProHauptmahlzeitWithParam(massgebendesEinkommen, sozialhilfebezueger).compareTo(BigDecimal.ZERO) > 0 ||
			getVerguenstigungProNebenmahlzeitWithParam(massgebendesEinkommen, sozialhilfebezueger).compareTo(BigDecimal.ZERO) > 0 ) {
			return true;
		}

		return false;
	}

	public BigDecimal getVerguenstigungProHauptmahlzeitWithParam(BigDecimal massgebendesEinkommen, boolean sozialhilfeBezueger) {

		BigDecimal verguenstigung = verguenstigungProHauptmahlzeit.get(massgebendesEinkommen);

		// falls es sich um einen Sozialhilfebezüger handelt und Die Vergünstigung für diese aktiv ist, nehmen wir
		// die Vergünstigung der Stufe 0
		if (sozialhilfeBezueger && enabledFuerSozHilfeBez) {
			verguenstigung = verguenstigungProHauptmahlzeit.get(BigDecimal.ZERO);
		}

		// falls es sich um einen Sozialhilfebezüger handelt aber die Mahlzeitenvergünstigung nicht an diese
		// ausbezahlt wird, geben wir 0 zurück
		if (sozialhilfeBezueger && !enabledFuerSozHilfeBez) {
			return BigDecimal.ZERO;
		}

		// falls keine Vergünstigung deklariert ist, geben wir 0 zurück
		if (verguenstigung == null) {
			return BigDecimal.ZERO;
		}

		return verguenstigung;
	}

	public BigDecimal getVerguenstigungProNebenmahlzeitWithParam(BigDecimal massgebendesEinkommen, boolean sozialhilfeBezueger) {

		BigDecimal verguenstigung = verguenstigungProNebenmahlzeit.get(massgebendesEinkommen);

		// falls es sich um einen Sozialhilfebezüger handelt und Die Vergünstigung für diese aktiv ist, nehmen wir
		// die Vergünstigung der Stufe 0
		if (sozialhilfeBezueger && enabledFuerSozHilfeBez) {
			verguenstigung = verguenstigungProNebenmahlzeit.get(BigDecimal.ZERO);
		}

		// falls es sich um einen Sozialhilfebezüger handelt aber die Mahlzeitenvergünstigung nicht an diese
		// ausbezahlt wird, geben wir 0 zurück
		if (sozialhilfeBezueger && !enabledFuerSozHilfeBez) {
			return BigDecimal.ZERO;
		}

		// falls keine Vergünstigung deklariert ist, geben wir 0 zurück
		if (verguenstigung == null) {
			return BigDecimal.ZERO;
		}

		return verguenstigung;
	}

	public BigDecimal getVerguenstigungEffektiv(
		@Nonnull BigDecimal verguenstigung, @Nonnull BigDecimal tarifProMahlzeit, @Nonnull BigDecimal minimalerElternbeitrag
	) {
		if ((tarifProMahlzeit.subtract(verguenstigung)).subtract(minimalerElternbeitrag).compareTo(BigDecimal.ZERO) >= 0) {
			return verguenstigung;
		}

		if (tarifProMahlzeit.subtract(minimalerElternbeitrag).compareTo(BigDecimal.ZERO) > 0) {
			return tarifProMahlzeit.subtract(minimalerElternbeitrag);
		}

		return BigDecimal.ZERO;
	}

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
}
