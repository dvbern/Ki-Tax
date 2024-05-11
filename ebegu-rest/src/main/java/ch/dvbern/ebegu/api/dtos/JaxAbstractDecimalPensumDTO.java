/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import ch.dvbern.ebegu.enums.PensumUnits;

/**
 * Superklasse fuer ein Betreuungspensum
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxAbstractDecimalPensumDTO extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = -7598774821364548948L;

	@NotNull
	@Nonnull
	private PensumUnits unitForDisplay = PensumUnits.PERCENTAGE;

	@NotNull
	@Nonnull
	@Min(0)
	private BigDecimal pensum = BigDecimal.ZERO;

	@Nonnull
	@NotNull
	private BigDecimal monatlicheBetreuungskosten;

	@Nullable
	private BigDecimal stuendlicheVollkosten;

	@Nullable
	private JaxEingewoehnungPauschale eingewoehnungPauschale;

	@Nullable
	private BigDecimal betreuteTage;

	@Nonnull
	public PensumUnits getUnitForDisplay() {
		return unitForDisplay;
	}

	public void setUnitForDisplay(@Nonnull PensumUnits unitForDisplay) {
		this.unitForDisplay = unitForDisplay;
	}

	@Nonnull
	public BigDecimal getPensum() {
		return pensum;
	}

	public void setPensum(@Nonnull BigDecimal pensum) {
		this.pensum = pensum;
	}

	@Nonnull
	public BigDecimal getMonatlicheBetreuungskosten() {
		return monatlicheBetreuungskosten;
	}

	public void setMonatlicheBetreuungskosten(@Nonnull BigDecimal monatlicheBetreuungskosten) {
		this.monatlicheBetreuungskosten = monatlicheBetreuungskosten;
	}

	@Nullable
	public BigDecimal getStuendlicheVollkosten() {
		return stuendlicheVollkosten;
	}

	public void setStuendlicheVollkosten(@Nullable BigDecimal stuendlicheVollkosten) {
		this.stuendlicheVollkosten = stuendlicheVollkosten;
	}

	@Nullable
	public JaxEingewoehnungPauschale getEingewoehnungPauschale() {
		return eingewoehnungPauschale;
	}

	public void setEingewoehnungPauschale(@Nullable JaxEingewoehnungPauschale eingewoehnungPauschale) {
		this.eingewoehnungPauschale = eingewoehnungPauschale;
	}

	@Nullable
	public BigDecimal getBetreuteTage() {
		return betreuteTage;
	}

	public void setBetreuteTage(@Nullable BigDecimal betreuteTage) {
		this.betreuteTage = betreuteTage;
	}
}
