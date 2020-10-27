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

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;


/**
 * Superklasse fuer ein Betreuungspensum mit Mahlzeiten
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxAbstractMahlzeitenPensumDTO extends JaxAbstractDecimalPensumDTO {

	private static final long serialVersionUID = -3035958327660443564L;

	@Nonnull
	private BigDecimal monatlicheHauptmahlzeiten;

	@Nonnull
	private BigDecimal monatlicheNebenmahlzeiten;

	@Nonnull
	private BigDecimal tarifProHauptmahlzeit;

	@Nonnull
	private BigDecimal tarifProNebenmahlzeit;

	@Nonnull
	public BigDecimal getMonatlicheHauptmahlzeiten() {
		return monatlicheHauptmahlzeiten;
	}

	public void setMonatlicheHauptmahlzeiten(@Nonnull BigDecimal monatlicheHauptmahlzeiten) {
		this.monatlicheHauptmahlzeiten = monatlicheHauptmahlzeiten;
	}

	@Nonnull
	public BigDecimal getMonatlicheNebenmahlzeiten() {
		return monatlicheNebenmahlzeiten;
	}

	public void setMonatlicheNebenmahlzeiten(@Nonnull BigDecimal monatlicheNebenmahlzeiten) {
		this.monatlicheNebenmahlzeiten = monatlicheNebenmahlzeiten;
	}

	@Nonnull
	public BigDecimal getTarifProHauptmahlzeit() {
		return tarifProHauptmahlzeit;
	}

	public void setTarifProHauptmahlzeit(@Nonnull BigDecimal tarifProHauptmahlzeit) {
		this.tarifProHauptmahlzeit = tarifProHauptmahlzeit;
	}

	@Nonnull
	public BigDecimal getTarifProNebenmahlzeit() {
		return tarifProNebenmahlzeit;
	}

	public void setTarifProNebenmahlzeit(@Nonnull BigDecimal tarifProNebenmahlzeit) {
		this.tarifProNebenmahlzeit = tarifProNebenmahlzeit;
	}
}
