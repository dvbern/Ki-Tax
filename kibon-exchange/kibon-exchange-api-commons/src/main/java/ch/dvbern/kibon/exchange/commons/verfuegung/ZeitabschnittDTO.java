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

package ch.dvbern.kibon.exchange.commons.verfuegung;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ZeitabschnittDTO implements Serializable {

	private static final long serialVersionUID = -5429450279561253396L;

	@Nonnull
	private final @NotNull LocalDate von;

	@Nonnull
	private final @NotNull LocalDate bis;

	@Nullable
	private final @Min(0) Integer verfuegungNr;

	@Nonnull
	private final @NotNull @DecimalMin("0") @DecimalMax("100")  BigDecimal effektiveBetreuungPct;

	private final @NotNull @Min(0) @Max(100) Integer anspruchPct;

	@Nonnull
	private final @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal verguenstigtPct;
	@Nonnull
	private final @NotNull @DecimalMin("0") BigDecimal vollkosten;
	@Nonnull
	private final @DecimalMin("0") BigDecimal betreuungsgutschein;
	@Nonnull
	private final @DecimalMin("0") BigDecimal minimalerElternbeitrag;
	@Nonnull
	private final @NotNull @DecimalMin("0") BigDecimal verguenstigung;

	public ZeitabschnittDTO() {
		this(
			LocalDate.MIN,
			LocalDate.MIN,
			0,
			BigDecimal.ZERO,
			0,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO);
	}

	public ZeitabschnittDTO(
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		@Nullable Integer verfuegungNr,
		@Nonnull BigDecimal effektiveBetr,
		@Nonnull Integer anspruchPct,
		@Nonnull BigDecimal vergPct,
		@Nonnull BigDecimal vollkosten,
		@Nonnull BigDecimal betreuungsgutschein,
		@Nonnull BigDecimal minimalerElternbeitrag,
		@Nonnull BigDecimal verguenstigung
	) {
		this.von = von;
		this.bis = bis;
		this.verfuegungNr = verfuegungNr;
		this.effektiveBetreuungPct = effektiveBetr;
		this.anspruchPct = anspruchPct;
		this.verguenstigtPct = vergPct;
		this.vollkosten = vollkosten;
		this.betreuungsgutschein = betreuungsgutschein;
		this.minimalerElternbeitrag = minimalerElternbeitrag;
		this.verguenstigung = verguenstigung;
	}

	@Nonnull
	public LocalDate getVon() {
		return von;
	}

	@Nonnull
	public LocalDate getBis() {
		return bis;
	}

	@Nullable
	public Integer getVerfuegungNr() {
		return verfuegungNr;
	}

	@Nonnull
	public BigDecimal getEffektiveBetreuungPct() {
		return effektiveBetreuungPct;
	}

	@Nonnull
	public Integer getAnspruchPct() {
		return anspruchPct;
	}

	@Nonnull
	public BigDecimal getVerguenstigtPct() {
		return verguenstigtPct;
	}

	@Nonnull
	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	@Nonnull
	public BigDecimal getBetreuungsgutschein() {
		return betreuungsgutschein;
	}

	@Nonnull
	public BigDecimal getMinimalerElternbeitrag() {
		return minimalerElternbeitrag;
	}

	@Nonnull
	public BigDecimal getVerguenstigung() {
		return verguenstigung;
	}
}
