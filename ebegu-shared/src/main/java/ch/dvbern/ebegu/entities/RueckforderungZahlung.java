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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

@Entity
@Audited
public class RueckforderungZahlung extends AbstractEntity {

	private static final long serialVersionUID = 5952831760433104632L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_rueckforderungZahlung_rueckforderungFormular_id"), nullable =
		false)
	private RueckforderungFormular rueckforderungFormular;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private BigDecimal betrag;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private boolean zahlungAusgefuehrt = false;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime datumAusgefuehrt;

	public RueckforderungFormular getRueckforderungFormular() {
		return rueckforderungFormular;
	}

	public void setRueckforderungFormular(RueckforderungFormular rueckforderungFormular) {
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

	public LocalDateTime getDatumAusgefuehrt() {
		return datumAusgefuehrt;
	}

	public void setDatumAusgefuehrt(LocalDateTime datumAusgefuehrt) {
		this.datumAusgefuehrt = datumAusgefuehrt;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof RueckforderungZahlung)) {
			return false;
		}
		final RueckforderungZahlung otherRueckforderungZahlung = (RueckforderungZahlung) other;
		return this.rueckforderungFormular.getId().equals(otherRueckforderungZahlung.getRueckforderungFormular().getId())
			&& this.betrag.equals(otherRueckforderungZahlung.getBetrag())
			&& this.datumAusgefuehrt.isEqual(otherRueckforderungZahlung.getDatumAusgefuehrt());
	}
}
