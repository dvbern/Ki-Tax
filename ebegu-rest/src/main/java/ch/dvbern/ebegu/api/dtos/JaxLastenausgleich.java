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

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * DTO fuer eine Durchführung eines Lastenausgleichs
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxLastenausgleich extends JaxAbstractDTO {

	private static final long serialVersionUID = 4048884525294262338L;

	@NotNull @Nonnull
	private Integer jahr = 0;

	@NotNull @Nonnull
	private BigDecimal totalAlleGemeinden = BigDecimal.ZERO;

	@NotNull @Nonnull
	private List<JaxLastenausgleichDetail> lastenausgleichDetails = new ArrayList<>();


	@Nonnull
	public Integer getJahr() {
		return jahr;
	}

	public void setJahr(@Nonnull Integer jahr) {
		this.jahr = jahr;
	}

	@Nonnull
	public BigDecimal getTotalAlleGemeinden() {
		return totalAlleGemeinden;
	}

	public void setTotalAlleGemeinden(@Nonnull BigDecimal totalAlleGemeinden) {
		this.totalAlleGemeinden = totalAlleGemeinden;
	}

	@Nonnull
	public List<JaxLastenausgleichDetail> getLastenausgleichDetails() {
		return lastenausgleichDetails;
	}

	public void setLastenausgleichDetails(@Nonnull List<JaxLastenausgleichDetail> lastenausgleichDetails) {
		this.lastenausgleichDetails = lastenausgleichDetails;
	}
}
