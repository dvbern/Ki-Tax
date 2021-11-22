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

package ch.dvbern.ebegu.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.MathUtil;

public class TSCalculationInput {

	@NotNull
	@Nonnull
	private Integer betreuungszeitProWoche = 0;

	@NotNull
	@Nonnull
	private BigDecimal verpflegungskosten = BigDecimal.ZERO;

	@NotNull
	@Nonnull
	private BigDecimal verpflegungskostenVerguenstigt = BigDecimal.ZERO;

	@NotNull
	@Nonnull
	private Map<BigDecimal, Integer> verpflegungskostenUndMahlzeiten = new HashMap<>();

	@NotNull
	@Nonnull
	private Map<BigDecimal, Integer> verpflegungskostenUndMahlzeitenZweiWochen = new HashMap<>();

	public TSCalculationInput() {
	}

	public TSCalculationInput(@Nonnull TSCalculationInput other) {
		this.betreuungszeitProWoche = other.betreuungszeitProWoche;
		this.verpflegungskosten = other.verpflegungskosten;
		this.verpflegungskostenVerguenstigt = other.verpflegungskostenVerguenstigt;
		this.verpflegungskostenUndMahlzeiten = other.verpflegungskostenUndMahlzeiten;
		this.verpflegungskostenUndMahlzeitenZweiWochen = other.verpflegungskostenUndMahlzeitenZweiWochen;
	}

	public void add(@Nonnull TSCalculationInput other) {
		this.betreuungszeitProWoche = this.betreuungszeitProWoche + other.betreuungszeitProWoche;
		this.verpflegungskosten = MathUtil.DEFAULT.addNullSafe(this.verpflegungskosten, other.verpflegungskosten);
		this.verpflegungskostenVerguenstigt = MathUtil.DEFAULT.addNullSafe(this.verpflegungskostenVerguenstigt, other.verpflegungskostenVerguenstigt);
		Map<BigDecimal, Integer> merged = new HashMap<>();

		merged.putAll(this.verpflegungskostenUndMahlzeiten);
		merged.putAll(other.verpflegungskostenUndMahlzeiten);

		this.verpflegungskostenUndMahlzeiten = merged;

		Map<BigDecimal, Integer> mergedZweiWochen = new HashMap<>();

		mergedZweiWochen.putAll(this.verpflegungskostenUndMahlzeitenZweiWochen);
		mergedZweiWochen.putAll(other.verpflegungskostenUndMahlzeitenZweiWochen);

		this.verpflegungskostenUndMahlzeitenZweiWochen = mergedZweiWochen;
	}

	public void calculatePercentage(double percent) {
		this.betreuungszeitProWoche = Math.toIntExact(Math.round(calculatePercentage(this.betreuungszeitProWoche.doubleValue(), percent)));
		this.verpflegungskosten = calculatePercentage(this.verpflegungskosten, percent);
		this.verpflegungskostenVerguenstigt = calculatePercentage(this.verpflegungskostenVerguenstigt, percent);
	}


	private BigDecimal calculatePercentage(BigDecimal value, double percent) {
		return BigDecimal.valueOf(Math.round(calculatePercentage(value.doubleValue(), percent)));
	}


	private double calculatePercentage(double value, double percent) {
		if(value == 0) {
			return 0;
		}

		return value / 100 * percent;
	}


	public boolean isSame(@Nonnull TSCalculationInput other) {
		return Objects.equals(this.betreuungszeitProWoche, other.betreuungszeitProWoche)
			&& MathUtil.isSame(this.verpflegungskosten, other.verpflegungskosten)
			&& MathUtil.isSame(this.verpflegungskostenVerguenstigt, other.verpflegungskostenVerguenstigt)
			&& Objects.equals(this.verpflegungskostenUndMahlzeiten, other.verpflegungskostenUndMahlzeiten)
			&& Objects.equals(this.verpflegungskostenUndMahlzeitenZweiWochen, other.verpflegungskostenUndMahlzeitenZweiWochen);
	}

	@Nonnull
	public TSCalculationInput copy() {
		return new TSCalculationInput(this);
	}

	public boolean shouldCalculate() {
		return betreuungszeitProWoche > 0;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", TSCalculationInput.class.getSimpleName() + '[', "]")
			.add("betreuungszeitProWoche=" + betreuungszeitProWoche)
			.add("verpflegungskosten=" + verpflegungskosten)
			.add("verpflegungskostenVerguenstigt=" + verpflegungskostenVerguenstigt)
			.toString();
	}

	@Nonnull
	public Integer getBetreuungszeitProWoche() {
		return betreuungszeitProWoche;
	}

	public void setBetreuungszeitProWoche(@Nonnull Integer betreuungszeitProWoche) {
		this.betreuungszeitProWoche = betreuungszeitProWoche;
	}

	@Nonnull
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nonnull BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nonnull
	public BigDecimal getVerpflegungskostenVerguenstigt() {
		return verpflegungskostenVerguenstigt;
	}

	public void setVerpflegungskostenVerguenstigt(@Nonnull BigDecimal verpflegungskostenVerguenstigt) {
		this.verpflegungskostenVerguenstigt = verpflegungskostenVerguenstigt;
	}

	@Nonnull
	public Map<BigDecimal, Integer> getVerpflegungskostenUndMahlzeiten() {
		return verpflegungskostenUndMahlzeiten;
	}

	public void setVerpflegungskostenUndMahlzeiten(@Nonnull Map<BigDecimal, Integer> verpflegungskostenUndMahlzeiten) {
		this.verpflegungskostenUndMahlzeiten = verpflegungskostenUndMahlzeiten;
	}

	@Nonnull
	public Map<BigDecimal, Integer> getVerpflegungskostenUndMahlzeitenZweiWochen() {
		return verpflegungskostenUndMahlzeitenZweiWochen;
	}

	public void setVerpflegungskostenUndMahlzeitenZweiWochen(@Nonnull Map<BigDecimal, Integer> verpflegungskostenUndMahlzeitenZweiWochen) {
		this.verpflegungskostenUndMahlzeitenZweiWochen = verpflegungskostenUndMahlzeitenZweiWochen;
	}

}
