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
public class JaxAbstractBetreuungspensumDTO extends JaxAbstractPensumDTO {

	private static final long serialVersionUID = -7598774821364548948L;

	@NotNull
	private PensumUnits unitForDisplay = PensumUnits.PERCENTAGE;

	public PensumUnits getUnitForDisplay() {
		return unitForDisplay;
	}

	public void setUnitForDisplay(PensumUnits unitForDisplay) {
		this.unitForDisplay = unitForDisplay;
	}
}
