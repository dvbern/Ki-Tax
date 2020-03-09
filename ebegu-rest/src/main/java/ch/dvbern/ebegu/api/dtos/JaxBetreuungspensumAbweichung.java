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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.BetreuungspensumAbweichungStatus;

/**
 * DTO fuer Daten des Betreuungspensum
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBetreuungspensumAbweichung extends JaxAbstractMahlzeitenPensumDTO {

	private static final long serialVersionUID = 4496021781469239269L;

	private BetreuungspensumAbweichungStatus status;

	private BigDecimal vertraglichesPensum;

	private BigDecimal vertraglicheKosten;

	private Integer vertraglicheHauptmahlzeiten;

	private Integer vertraglicheNebenmahlzeiten;

	public BetreuungspensumAbweichungStatus getStatus() {
		return status;
	}
	
	public void setStatus(BetreuungspensumAbweichungStatus status) {
		this.status = status;
	}

	public BigDecimal getVertraglichesPensum() {
		return vertraglichesPensum;
	}

	public void setVertraglichesPensum(BigDecimal vertraglichesPensum) {
		this.vertraglichesPensum = vertraglichesPensum;
	}

	public BigDecimal getVertraglicheKosten() {
		return vertraglicheKosten;
	}

	public void setVertraglicheKosten(BigDecimal vertraglicheKosten) {
		this.vertraglicheKosten = vertraglicheKosten;
	}

	public Integer getVertraglicheHauptmahlzeiten() {
		return vertraglicheHauptmahlzeiten;
	}

	public void setVertraglicheHauptmahlzeiten(Integer vertraglicheHauptmahlzeiten) {
		this.vertraglicheHauptmahlzeiten = vertraglicheHauptmahlzeiten;
	}

	public Integer getVertraglicheNebenmahlzeiten() {
		return vertraglicheNebenmahlzeiten;
	}

	public void setVertraglicheNebenmahlzeiten(Integer vertraglicheNebenmahlzeiten) {
		this.vertraglicheNebenmahlzeiten = vertraglicheNebenmahlzeiten;
	}
}
