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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer die Resultate der Tagesschule-Berechnungen
 */
@XmlRootElement(name = "tsCalculationResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxTsCalculationResult extends JaxAbstractDTO {

	private static final long serialVersionUID = -2142241603064978250L;

	private Integer betreuungszeitProWoche = 0;

	private BigDecimal verpflegungskosten = BigDecimal.ZERO;

	private BigDecimal gebuehrProStunde = BigDecimal.ZERO;

	private BigDecimal totalKostenProWoche = BigDecimal.ZERO;

	private String betreuungszeitProWocheFormatted;

	public Integer getBetreuungszeitProWoche() {
		return betreuungszeitProWoche;
	}

	public void setBetreuungszeitProWoche(Integer betreuungszeitProWoche) {
		this.betreuungszeitProWoche = betreuungszeitProWoche;
	}

	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	public BigDecimal getGebuehrProStunde() {
		return gebuehrProStunde;
	}

	public void setGebuehrProStunde(BigDecimal gebuehrProStunde) {
		this.gebuehrProStunde = gebuehrProStunde;
	}

	public BigDecimal getTotalKostenProWoche() {
		return totalKostenProWoche;
	}

	public void setTotalKostenProWoche(BigDecimal totalKostenProWoche) {
		this.totalKostenProWoche = totalKostenProWoche;
	}

	public String getBetreuungszeitProWocheFormatted() {
		return betreuungszeitProWocheFormatted;
	}

	public void setBetreuungszeitProWocheFormatted(String betreuungszeitProWocheFormatted) {
		this.betreuungszeitProWocheFormatted = betreuungszeitProWocheFormatted;
	}
}
