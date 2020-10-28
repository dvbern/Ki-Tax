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

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ZahlungStatus;

/**
 * DTO fuer Zahlungen
 */
@XmlRootElement(name = "zahlung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxZahlung extends JaxAbstractDTO {

	private static final long serialVersionUID = 1661454343875422672L;

	@NotNull
	private String empfaengerName;

	@NotNull
	private String empfaengerId;

	@NotNull
	private BetreuungsangebotTyp betreuungsangebotTyp;

	@NotNull
	private ZahlungStatus status;

	@NotNull
	private BigDecimal betragTotalZahlung;


	@Nonnull
	public String getEmpfaengerName() {
		return empfaengerName;
	}

	public void setEmpfaengerName(@Nonnull String empfaengerName) {
		this.empfaengerName = empfaengerName;
	}

	@Nonnull
	public ZahlungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull ZahlungStatus status) {
		this.status = status;
	}

	@Nonnull
	public BigDecimal getBetragTotalZahlung() {
		return betragTotalZahlung;
	}

	public void setBetragTotalZahlung(@Nonnull BigDecimal betragTotalZahlung) {
		this.betragTotalZahlung = betragTotalZahlung;
	}

	@Nonnull
	public String getEmpfaengerId() {
		return empfaengerId;
	}

	public void setEmpfaengerId(@Nonnull String empfaengerId) {
		this.empfaengerId = empfaengerId;
	}

	@Nonnull
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}
}
