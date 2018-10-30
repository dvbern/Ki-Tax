/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer Erweiterte Betreuung Container
 */
@XmlRootElement(name = "erweiterteBetreuung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxErweiterteBetreuungContainer extends JaxAbstractDTO {

	private static final long serialVersionUID = 1196725684018427343L;

	@Valid
	private JaxErweiterteBetreuung erweiterteBetreuungGS;

	@Valid
	private JaxErweiterteBetreuung erweiterteBetreuungJA;

	public JaxErweiterteBetreuung getErweiterteBetreuungGS() {
		return erweiterteBetreuungGS;
	}

	public void setErweiterteBetreuungGS(JaxErweiterteBetreuung erweiterteBetreuungGS) {
		this.erweiterteBetreuungGS = erweiterteBetreuungGS;
	}

	public JaxErweiterteBetreuung getErweiterteBetreuungJA() {
		return erweiterteBetreuungJA;
	}

	public void setErweiterteBetreuungJA(JaxErweiterteBetreuung erweiterteBetreuungJA) {
		this.erweiterteBetreuungJA = erweiterteBetreuungJA;
	}
}
