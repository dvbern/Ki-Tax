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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.entities.RueckforderungFormular;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxRueckforderungZahlung extends JaxAbstractDTO {

	private static final long serialVersionUID = -5491428455809796796L;

	@Nonnull
	private RueckforderungFormular rueckforderungFormular;

	@Nonnull
	private BigDecimal betrag;

	private boolean zahlungAusgefuehrt = false;

	@Nonnull
	private LocalDateTime datumAusgefuehrt;

	@Nonnull
	public RueckforderungFormular getRueckforderungFormular() {
		return rueckforderungFormular;
	}

	public void setRueckforderungFormular(@Nonnull RueckforderungFormular rueckforderungFormular) {
		this.rueckforderungFormular = rueckforderungFormular;
	}

	@Nonnull
	public BigDecimal getBetrag() {
		return betrag;
	}

	public void setBetrag(@Nonnull BigDecimal betrag) {
		this.betrag = betrag;
	}

	public boolean isZahlungAusgefuehrt() {
		return zahlungAusgefuehrt;
	}

	public void setZahlungAusgefuehrt(boolean zahlungAusgefuehrt) {
		this.zahlungAusgefuehrt = zahlungAusgefuehrt;
	}

	@Nonnull
	public LocalDateTime getDatumAusgefuehrt() {
		return datumAusgefuehrt;
	}

	public void setDatumAusgefuehrt(@Nonnull LocalDateTime datumAusgefuehrt) {
		this.datumAusgefuehrt = datumAusgefuehrt;
	}
}
