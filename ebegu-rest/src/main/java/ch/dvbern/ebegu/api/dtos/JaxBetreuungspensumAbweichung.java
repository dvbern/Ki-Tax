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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.BetreuungspensumAbweichungStatus;

/**
 * DTO fuer Daten des Betreuungspensum
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBetreuungspensumAbweichung extends JaxAbstractMahlzeitenPensumDTO {

	private static final long serialVersionUID = 4496021781469239269L;

	@Nonnull
	private BetreuungspensumAbweichungStatus status;

	@Nullable
	private BigDecimal vertraglichesPensum;

	@Nullable
	private BigDecimal vertraglicheKosten;

	@Nullable
	private BigDecimal vertraglicheHauptmahlzeiten;

	@Nullable
	private BigDecimal vertraglicheNebenmahlzeiten;

	@Nullable
	private BigDecimal vertraglicherTarifHaupt;

	@Nullable
	private BigDecimal vertraglicherTarifNeben;

	@Nullable
	private BigDecimal multiplier;

	@Nonnull
	private BetreuungsangebotTyp betreuungsangebotTyp;

	@Nonnull
	public BetreuungspensumAbweichungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull BetreuungspensumAbweichungStatus status) {
		this.status = status;
	}

	@Nullable
	public BigDecimal getVertraglichesPensum() {
		return vertraglichesPensum;
	}

	public void setVertraglichesPensum(@Nullable BigDecimal vertraglichesPensum) {
		this.vertraglichesPensum = vertraglichesPensum;
	}

	@Nullable
	public BigDecimal getVertraglicheKosten() {
		return vertraglicheKosten;
	}

	public void setVertraglicheKosten(@Nullable BigDecimal vertraglicheKosten) {
		this.vertraglicheKosten = vertraglicheKosten;
	}

	@Nullable
	public BigDecimal getVertraglicheHauptmahlzeiten() {
		return vertraglicheHauptmahlzeiten;
	}

	public void setVertraglicheHauptmahlzeiten(@Nullable BigDecimal vertraglicheHauptmahlzeiten) {
		this.vertraglicheHauptmahlzeiten = vertraglicheHauptmahlzeiten;
	}

	@Nullable
	public BigDecimal getVertraglicheNebenmahlzeiten() {
		return vertraglicheNebenmahlzeiten;
	}

	public void setVertraglicheNebenmahlzeiten(@Nullable BigDecimal vertraglicheNebenmahlzeiten) {
		this.vertraglicheNebenmahlzeiten = vertraglicheNebenmahlzeiten;
	}

	@Nullable
	public BigDecimal getVertraglicherTarifHaupt() {
		return vertraglicherTarifHaupt;
	}

	public void setVertraglicherTarifHaupt(@Nullable BigDecimal vertraglicherTarifHaupt) {
		this.vertraglicherTarifHaupt = vertraglicherTarifHaupt;
	}

	@Nullable
	public BigDecimal getVertraglicherTarifNeben() {
		return vertraglicherTarifNeben;
	}

	public void setVertraglicherTarifNeben(@Nullable BigDecimal vertraglicherTarifNeben) {
		this.vertraglicherTarifNeben = vertraglicherTarifNeben;
	}

	@Nullable
	public BigDecimal getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(@Nullable BigDecimal multiplier) {
		this.multiplier = multiplier;
	}

	@Nonnull
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}
}
