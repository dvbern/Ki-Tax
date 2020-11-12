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

	private BigDecimal vertraglicheHauptmahlzeiten;

	private BigDecimal vertraglicheNebenmahlzeiten;

	private BigDecimal vertraglicherTarifHaupt;

	private BigDecimal vertraglicherTarifNeben;

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

	public BigDecimal getVertraglicheHauptmahlzeiten() {
		return vertraglicheHauptmahlzeiten;
	}

	public void setVertraglicheHauptmahlzeiten(BigDecimal vertraglicheHauptmahlzeiten) {
		this.vertraglicheHauptmahlzeiten = vertraglicheHauptmahlzeiten;
	}

	public BigDecimal getVertraglicheNebenmahlzeiten() {
		return vertraglicheNebenmahlzeiten;
	}

	public void setVertraglicheNebenmahlzeiten(BigDecimal vertraglicheNebenmahlzeiten) {
		this.vertraglicheNebenmahlzeiten = vertraglicheNebenmahlzeiten;
	}

	public BigDecimal getVertraglicherTarifHaupt() {
		return vertraglicherTarifHaupt;
	}

	public void setVertraglicherTarifHaupt(BigDecimal vertraglicherTarifHaupt) {
		this.vertraglicherTarifHaupt = vertraglicherTarifHaupt;
	}

	public BigDecimal getVertraglicherTarifNeben() {
		return vertraglicherTarifNeben;
	}

	public void setVertraglicherTarifNeben(BigDecimal vertraglicherTarifNeben) {
		this.vertraglicherTarifNeben = vertraglicherTarifNeben;
	}
}
