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

package ch.dvbern.kibon.exchange.api.verfuegung.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ZeitabschnittDTO implements Serializable {

	private static final long serialVersionUID = -4604369503819718952L;

	private static final Comparator<BigDecimal> BIG_DECIMAL_COMPARATOR =
		Comparator.nullsLast(Comparator.naturalOrder());

	@Nonnull
	private @NotNull LocalDate von;

	@Nonnull
	private @NotNull LocalDate bis;

	/**
	 * Laufnummber der Verfügung, die dieser Zeitabschnitt referenziert.
	 * <p>
	 * Es können Zeitabschnitte aus einer verherigen Verfügung in dem KiTax Export enthalten sein (wenn es ignorierte
	 * Zeitabschnitte gibt).
	 *
	 * @since kiBon (schema 2.0), kiTax (schema 1.1)
	 */
	@Nullable
	private @Min(0) Integer verfuegungNr;

	/**
	 * Bezeichnet das mit der Betreuungsinstitution vereinbarte Betreuungspensum in %.
	 */
	@Nonnull
	private @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal effektiveBetreuungPct;

	/**
	 * Bezeichnet das anspruchsberechtigte Betreuungspensum in %, d.h. das Betreuungspensum, das durch die Gemeinde
	 * maximal durch einen Betreuungsgutschein vergünstigt würde.
	 */
	@Nonnull
	private @NotNull @Min(0) @Max(100) Integer anspruchPct;

	/**
	 * Bezeichnet das vergünstigte Betreuungspensum in %. Das vergünstige Betreuungspensum entspricht dem effektiven
	 * Betreuungspensum bis das anspruchsberechtigte Pensum erreicht wird. Liegt das effektive Betreuungspensum über
	 * dem anspruchsberechtigten Pensum wir nur das anspruchsberechtigte Pensum vergünstigt.
	 */
	@Nonnull
	private @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal verguenstigtPct;

	/**
	 * Bezeichnet die Betreuungskosten in CHF der Institution für das vergünstigte Pensum. Kosten für Mahlzeiten und
	 * allfällige Zusatzleistungen wie Windeln etc. sind in diesem Betrag nicht enthalten.
	 * <p>
	 * Neu werden Betreuungskosten von den Institutionen und nicht mehr vom Kanton festgelegt (bei der
	 * Platzbestätigung)
	 */
	@Nonnull
	private @NotNull @DecimalMin("0") BigDecimal vollkosten;

	/**
	 * Der berechnete Gutschein in CHF bezeichnet die Vergünstigung, welche aufgrund des vergünstigten Pensums und des
	 * für die Berechnung des Gutscheins massgebenden Einkommens resultiert.
	 * <p>
	 * Falls der berechnete Gutschein höher ist als die Betreuungskosten, wird der berechnete Gutschein gekürzt, so
	 * dass der Gutschein die betreuungskosten nicht übersteigt.
	 *
	 * @since kiBon (schema 2.0)
	 */
	@Nullable
	private @DecimalMin("0") BigDecimal betreuungsgutschein;

	/**
	 * Die Eltern müssen einen minimalen Elternbeitrag leisten. Falls dies noch nicht der Fall ist, weil die Kosten
	 * vollständig oder fast vollständig durch den Betreuungsgutschein gedeckt werden, muss die Kita oder
	 * Tagesfamilienorganisation den Eltern den (fehlenden) minimalen Elternbeitrag in Rechnung stellen. Der minimale
	 * Elternbeitrag beträgt 7 Franken pro Betreuungstag (20% Pensum pro Woche) in einer Kita und 70 Rappen pro
	 * Betreuungsstunde bei Tagesfamilien.
	 *
	 * @since kiBon (schema 2.0)
	 */
	@Nullable
	private @DecimalMin("0") BigDecimal minimalerElternbeitrag;

	/**
	 * Bezeichnet den Betrag in CHF, der an die Kita überwiesen wird. Dieser entspricht dem Betreuungsgutschein
	 * abzüglich eines allfälligen minimalen Elternbeitrags (den die Kita bzw. die Tagesfamilienorganisation den
	 * Eltern in Rechnung stellen würde).
	 */
	@Nonnull
	private @NotNull @DecimalMin("0") BigDecimal verguenstigung;

	public ZeitabschnittDTO() {
		this.von = LocalDate.MIN;
		this.bis = LocalDate.MIN;
		this.effektiveBetreuungPct = BigDecimal.ZERO;
		this.anspruchPct = -1;
		this.verguenstigtPct = BigDecimal.ZERO;
		this.vollkosten = BigDecimal.ZERO;
		this.verguenstigung = BigDecimal.ZERO;
	}

	public ZeitabschnittDTO(
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
		@Nullable Integer verfuegungNr,
		@Nonnull BigDecimal effektiveBetreuungPct,
		@Nonnull Integer anspruchPct,
		@Nonnull BigDecimal verguenstigtPct,
		@Nonnull BigDecimal vollkosten,
		@Nullable BigDecimal betreuungsgutschein,
		@Nullable BigDecimal minimalerElternbeitrag,
		@Nonnull BigDecimal verguenstigung) {
		this.von = von;
		this.bis = bis;
		this.verfuegungNr = verfuegungNr;
		this.effektiveBetreuungPct = effektiveBetreuungPct;
		this.anspruchPct = anspruchPct;
		this.verguenstigtPct = verguenstigtPct;
		this.vollkosten = vollkosten;
		this.betreuungsgutschein = betreuungsgutschein;
		this.minimalerElternbeitrag = minimalerElternbeitrag;
		this.verguenstigung = verguenstigung;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		ZeitabschnittDTO that = (ZeitabschnittDTO) o;

		return getVon().equals(that.getVon()) &&
			getBis().equals(that.getBis()) &&
			Objects.equals(getVerfuegungNr(), that.getVerfuegungNr()) &&
			getEffektiveBetreuungPct().compareTo(that.getEffektiveBetreuungPct()) == 0 &&
			getAnspruchPct().equals(that.getAnspruchPct()) &&
			getVerguenstigtPct().compareTo(that.getVerguenstigtPct()) == 0 &&
			getVollkosten().compareTo(that.getVollkosten()) == 0 &&
			BIG_DECIMAL_COMPARATOR.compare(getBetreuungsgutschein(), that.getBetreuungsgutschein()) == 0 &&
			BIG_DECIMAL_COMPARATOR.compare(getMinimalerElternbeitrag(), that.getMinimalerElternbeitrag()) == 0 &&
			getVerguenstigung().compareTo(that.getVerguenstigung()) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			getVon(),
			getBis(),
			getVerfuegungNr(),
			getEffektiveBetreuungPct(),
			getAnspruchPct(),
			getVerguenstigtPct(),
			getVollkosten(),
			getBetreuungsgutschein(),
			getMinimalerElternbeitrag(),
			getVerguenstigung());
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", ZeitabschnittDTO.class.getSimpleName() + '[', "]")
			.add("von=" + von)
			.add("bis=" + bis)
			.toString();
	}

	@Nonnull
	public LocalDate getVon() {
		return von;
	}

	public void setVon(@Nonnull LocalDate von) {
		this.von = von;
	}

	@Nonnull
	public LocalDate getBis() {
		return bis;
	}

	public void setBis(@Nonnull LocalDate bis) {
		this.bis = bis;
	}

	@Nullable
	public Integer getVerfuegungNr() {
		return verfuegungNr;
	}

	public void setVerfuegungNr(@Nullable Integer verfuegungNr) {
		this.verfuegungNr = verfuegungNr;
	}

	@Nonnull
	public BigDecimal getEffektiveBetreuungPct() {
		return effektiveBetreuungPct;
	}

	public void setEffektiveBetreuungPct(@Nonnull BigDecimal effektiveBetreuungPct) {
		this.effektiveBetreuungPct = effektiveBetreuungPct;
	}

	@Nonnull
	public Integer getAnspruchPct() {
		return anspruchPct;
	}

	public void setAnspruchPct(@Nonnull Integer anspruchPct) {
		this.anspruchPct = anspruchPct;
	}

	@Nonnull
	public BigDecimal getVerguenstigtPct() {
		return verguenstigtPct;
	}

	public void setVerguenstigtPct(@Nonnull BigDecimal verguenstigtPct) {
		this.verguenstigtPct = verguenstigtPct;
	}

	@Nonnull
	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(@Nonnull BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
	}

	@Nullable
	public BigDecimal getBetreuungsgutschein() {
		return betreuungsgutschein;
	}

	public void setBetreuungsgutschein(@Nullable BigDecimal betreuungsgutschein) {
		this.betreuungsgutschein = betreuungsgutschein;
	}

	@Nullable
	public BigDecimal getMinimalerElternbeitrag() {
		return minimalerElternbeitrag;
	}

	public void setMinimalerElternbeitrag(@Nullable BigDecimal minimalerElternbeitrag) {
		this.minimalerElternbeitrag = minimalerElternbeitrag;
	}

	@Nonnull
	public BigDecimal getVerguenstigung() {
		return verguenstigung;
	}

	public void setVerguenstigung(@Nonnull BigDecimal verguenstigung) {
		this.verguenstigung = verguenstigung;
	}
}
