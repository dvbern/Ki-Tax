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

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
public class RueckforderungMitteilung extends AbstractEntity implements Comparable {

	private static final long serialVersionUID = 5010422246166625084L;

	@NotNull
	@ManyToOne()
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_RueckforderungMitteilung_Benutzer_id"), nullable = false)
	private Benutzer absender;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private String betreff;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private String inhalt;

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private LocalDateTime sendeDatum;

	public RueckforderungMitteilung() {}

	public RueckforderungMitteilung(RueckforderungMitteilung toCopy) {
		this.absender = toCopy.getAbsender();
		this.betreff = toCopy.getBetreff();
		this.inhalt = toCopy.getInhalt();
		this.sendeDatum = toCopy.getSendeDatum();
	}

	public Benutzer getAbsender() {
		return absender;
	}

	public void setAbsender(Benutzer absender) {
		this.absender = absender;
	}

	@Nonnull
	public String getBetreff() {
		return betreff;
	}

	public void setBetreff(@Nonnull String betreff) {
		this.betreff = betreff;
	}

	@Nonnull
	public String getInhalt() {
		return inhalt;
	}

	public void setInhalt(@Nonnull String inhalt) {
		this.inhalt = inhalt;
	}

	@Nonnull
	public LocalDateTime getSendeDatum() {
		return sendeDatum;
	}

	public void setSendeDatum(@Nonnull LocalDateTime sendeDatum) {
		this.sendeDatum = sendeDatum;
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
		if (!(other instanceof RueckforderungMitteilung)) {
			return false;
		}
		final RueckforderungMitteilung otherRueckforderungMitteilung = (RueckforderungMitteilung) other;
		return this.absender.getId().equals(otherRueckforderungMitteilung.getAbsender().getId())
			&& this.betreff.equals(otherRueckforderungMitteilung.getBetreff())
			&& this.inhalt.equals(otherRueckforderungMitteilung.getInhalt())
			&& this.sendeDatum.equals(otherRueckforderungMitteilung.getSendeDatum());
	}

	@Override
	public int compareTo(@Nonnull Object o) {
		if (this == o) {
			return 1;
		}
		if (!getClass().equals(o.getClass())) {
			return -1;
		}
		if (!(o instanceof RueckforderungMitteilung)) {
			return -1;
		}
		final RueckforderungMitteilung otherRueckforderungMitteilung = (RueckforderungMitteilung) o;
		if (this.sendeDatum.isBefore(otherRueckforderungMitteilung.sendeDatum)) {
			return -1;
		}
		return 1;
	}
}
