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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

/**
 * Entität für die Einkommensverschlechterung
 *
 * @author gapa
 * @version 1.0
 */
@Audited
@Entity
public class Einkommensverschlechterung extends AbstractFinanzielleSituation {

	private static final long serialVersionUID = -8959552696602183511L;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahrMinus1;

	@Nullable
	@Column(name = "bruttolohn_abrechnung_1", nullable = true)
	private BigDecimal bruttolohnAbrechnung1;

	@Nullable
	@Column(name = "bruttolohn_abrechnung_2", nullable = true)
	private BigDecimal bruttolohnAbrechnung2;

	@Nullable
	@Column(name = "bruttolohn_abrechnung_3", nullable = true)
	private BigDecimal bruttolohnAbrechnung3;

	@Nullable
	@Column
	private Boolean extraLohn;

	public Einkommensverschlechterung() {
	}

	@Override
	public Boolean getSteuerveranlagungErhalten() {
		return false;
	}

	@Override
	public Boolean getSteuererklaerungAusgefuellt() {
		return false;
	}

	@Override
	public Boolean getSteuerdatenZugriff() {
		return false;
	}

	@Override
	@Nullable
	public SteuerdatenAnfrageStatus getSteuerdatenAbfrageStatus() {
		return null;
	}

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

	@Nonnull
	public Einkommensverschlechterung copyEinkommensverschlechterung(
		@Nonnull Einkommensverschlechterung target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			super.copyAbstractFinanzielleSituation(target, copyType);
			target.setGeschaeftsgewinnBasisjahrMinus1(this.getGeschaeftsgewinnBasisjahrMinus1());
			target.setBruttolohnAbrechnung1(this.getBruttolohnAbrechnung1());
			target.setBruttolohnAbrechnung2(this.getBruttolohnAbrechnung2());
			target.setBruttolohnAbrechnung3(this.getBruttolohnAbrechnung3());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@SuppressWarnings("checkstyle:CyclomaticComplexity")
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
		if (!(other instanceof Einkommensverschlechterung)) {
			return false;
		}
		final Einkommensverschlechterung otherEinkommensverschlechterung = (Einkommensverschlechterung) other;
		return MathUtil.isSame(
			getGeschaeftsgewinnBasisjahrMinus1(),
			otherEinkommensverschlechterung.getGeschaeftsgewinnBasisjahrMinus1()) &&
			MathUtil.isSame(getBruttolohnAbrechnung1(), otherEinkommensverschlechterung.getBruttolohnAbrechnung1()) &&
			MathUtil.isSame(getBruttolohnAbrechnung2(), otherEinkommensverschlechterung.getBruttolohnAbrechnung2()) &&
			MathUtil.isSame(getBruttolohnAbrechnung3(), otherEinkommensverschlechterung.getBruttolohnAbrechnung3());
	}
}
