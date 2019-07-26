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
import ch.dvbern.ebegu.enums.PensumUnits;

/**
 * DTO fuer Daten des Betreuungspensum
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBetreuungspensumAbweichung extends JaxAbstractDecimalPensumDTO {

	private static final long serialVersionUID = 4496021781469239269L;

	private BetreuungspensumAbweichungStatus status;

	private JaxBetreuung betreuung; // TODO KIBON-621 is this an overkill?

	private BigDecimal originalPensumMerged;

	private BigDecimal originalKostenMerged ;

	public BetreuungspensumAbweichungStatus getStatus() {
		return status;
	}

	public void setStatus(BetreuungspensumAbweichungStatus status) {
		this.status = status;
	}

	public JaxBetreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(JaxBetreuung betreuung) {
		this.betreuung = betreuung;
	}

	public BigDecimal getOriginalPensumMerged() {
		return originalPensumMerged;
	}

	public void setOriginalPensumMerged(BigDecimal originalPensumMerged) {
		this.originalPensumMerged = originalPensumMerged;
	}

	public BigDecimal getOriginalKostenMerged() {
		return originalKostenMerged;
	}

	public void setOriginalKostenMerged(BigDecimal originalKostenMerged) {
		this.originalKostenMerged = originalKostenMerged;
	}
}
