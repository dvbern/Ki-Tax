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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

/**
 * Entität für die Finanzielle Situation
 */
@Audited
@Entity
public class FinanzielleSituation extends AbstractFinanzielleSituation {

	private static final long serialVersionUID = -4401110366293613225L;

	@NotNull
	@Column(nullable = false)
	private Boolean steuerveranlagungErhalten;

	@NotNull
	@Column(nullable = false)
	private Boolean steuererklaerungAusgefuellt;

	@Nullable
	@Column(nullable = true)
	private Boolean steuerdatenZugriff;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahrMinus2;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahrMinus1;

	public FinanzielleSituation() {
	}


	public Boolean getSteuerveranlagungErhalten() {
		return steuerveranlagungErhalten;
	}

	public void setSteuerveranlagungErhalten(final Boolean steuerveranlagungErhalten) {
		this.steuerveranlagungErhalten = steuerveranlagungErhalten;
	}

	public Boolean getSteuererklaerungAusgefuellt() {
		return steuererklaerungAusgefuellt;
	}

	public void setSteuererklaerungAusgefuellt(final Boolean steuererklaerungAusgefuellt) {
		this.steuererklaerungAusgefuellt = steuererklaerungAusgefuellt;
	}

	@Nullable
	public Boolean getSteuerdatenZugriff() {
		return steuerdatenZugriff;
	}

	public void setSteuerdatenZugriff(@Nullable Boolean steuerdatenZurgiff) {
		this.steuerdatenZugriff = steuerdatenZurgiff;
	}

	@Nullable
	public BigDecimal getGeschaeftsgewinnBasisjahrMinus2() {
		return geschaeftsgewinnBasisjahrMinus2;
	}

	public void setGeschaeftsgewinnBasisjahrMinus2(@Nullable final BigDecimal geschaeftsgewinnBasisjahrMinus2) {
		this.geschaeftsgewinnBasisjahrMinus2 = geschaeftsgewinnBasisjahrMinus2;
	}

	@Nullable
	public BigDecimal getGeschaeftsgewinnBasisjahrMinus1() {
		return geschaeftsgewinnBasisjahrMinus1;
	}

	public void setGeschaeftsgewinnBasisjahrMinus1(@Nullable final BigDecimal geschaeftsgewinnBasisjahrMinus1) {
		this.geschaeftsgewinnBasisjahrMinus1 = geschaeftsgewinnBasisjahrMinus1;
	}

	@Nonnull
	public FinanzielleSituation copyFinanzielleSituation(@Nonnull FinanzielleSituation target, @Nonnull AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			super.copyAbstractFinanzielleSituation(target, copyType);
			target.setSteuerveranlagungErhalten(this.getSteuerveranlagungErhalten());
			target.setSteuererklaerungAusgefuellt(this.getSteuererklaerungAusgefuellt());
			target.setGeschaeftsgewinnBasisjahrMinus1(this.getGeschaeftsgewinnBasisjahrMinus1());
			target.setGeschaeftsgewinnBasisjahrMinus2(this.getGeschaeftsgewinnBasisjahrMinus2());
			target.setSteuerdatenZugriff(this.getSteuerdatenZugriff());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
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
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof FinanzielleSituation)) {
			return false;
		}
		final FinanzielleSituation otherFinSit = (FinanzielleSituation) other;
		return Objects.equals(getSteuerveranlagungErhalten(), otherFinSit.getSteuerveranlagungErhalten()) &&
			Objects.equals(getSteuererklaerungAusgefuellt(), otherFinSit.getSteuererklaerungAusgefuellt()) &&
			Objects.equals(getSteuerdatenZugriff(), otherFinSit.getSteuerdatenZugriff()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus1(), otherFinSit.getGeschaeftsgewinnBasisjahrMinus1()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus2(), otherFinSit.getGeschaeftsgewinnBasisjahrMinus2());
	}
}
