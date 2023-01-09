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

import java.math.BigDecimal;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxAbstractFinanzielleSituation;

/**
 * DTO fuer Familiensituationen
 */
@XmlRootElement(name = "einkommensverschlechterung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxEinkommensverschlechterung extends JaxAbstractFinanzielleSituation {

	private static final long serialVersionUID = 3659631207762053261L;

	@Nullable
	private BigDecimal geschaeftsgewinnBasisjahrMinus1;

	@Nullable
	private BigDecimal bruttolohnAbrechnung1;

	@Nullable
	private BigDecimal bruttolohnAbrechnung2;

	@Nullable
	private BigDecimal bruttolohnAbrechnung3;

	@Nullable
	private Boolean extraLohn;

	@Nullable
	public BigDecimal getGeschaeftsgewinnBasisjahrMinus1() {
		return geschaeftsgewinnBasisjahrMinus1;
	}

	public void setGeschaeftsgewinnBasisjahrMinus1(@Nullable BigDecimal geschaeftsgewinnBasisjahrMinus1) {
		this.geschaeftsgewinnBasisjahrMinus1 = geschaeftsgewinnBasisjahrMinus1;
	}

	@Nullable
	public BigDecimal getBruttolohnAbrechnung1() {
		return bruttolohnAbrechnung1;
	}

	public void setBruttolohnAbrechnung1(@Nullable BigDecimal bruttolohnAbrechnung1) {
		this.bruttolohnAbrechnung1 = bruttolohnAbrechnung1;
	}

	@Nullable
	public BigDecimal getBruttolohnAbrechnung2() {
		return bruttolohnAbrechnung2;
	}

	public void setBruttolohnAbrechnung2(@Nullable BigDecimal bruttolohnAbrechnung2) {
		this.bruttolohnAbrechnung2 = bruttolohnAbrechnung2;
	}

	@Nullable
	public BigDecimal getBruttolohnAbrechnung3() {
		return bruttolohnAbrechnung3;
	}

	public void setBruttolohnAbrechnung3(@Nullable BigDecimal bruttolohnAbrechnung3) {
		this.bruttolohnAbrechnung3 = bruttolohnAbrechnung3;
	}

	@Nullable
	public Boolean getExtraLohn() {
		return extraLohn;
	}

	public void setExtraLohn(@Nullable Boolean extraLohn) {
		this.extraLohn = extraLohn;
	}
}
