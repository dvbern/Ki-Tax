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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

/**
 * Gemeinsame Basisklasse für FinanzielleSituation und Einkommensverschlechterung
 *
 * @author gapa
 * @version 1.0
 */
@Audited
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractFinanzielleSituation extends AbstractMutableEntity {

	private static final long serialVersionUID = 2596930494846119259L;

	@NotNull
	@Column(nullable = false)
	private Boolean steuerveranlagungErhalten;

	@NotNull
	@Column(nullable = false)
	private Boolean steuererklaerungAusgefuellt;

	@Column(nullable = true)
	private BigDecimal familienzulage;

	@Column(nullable = true)
	private BigDecimal ersatzeinkommen;

	@Column(nullable = true)
	private BigDecimal erhalteneAlimente;

	@Column(nullable = true)
	private BigDecimal bruttovermoegen;

	@Column(nullable = true)
	private BigDecimal schulden;

	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahr;

	@Column(nullable = true)
	private BigDecimal geleisteteAlimente;

	public AbstractFinanzielleSituation() {
	}

	@Nullable
	public abstract BigDecimal getNettolohn();

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

	public BigDecimal getFamilienzulage() {
		return familienzulage;
	}

	public void setFamilienzulage(final BigDecimal familienzulage) {
		this.familienzulage = familienzulage;
	}

	public BigDecimal getErsatzeinkommen() {
		return ersatzeinkommen;
	}

	public void setErsatzeinkommen(final BigDecimal ersatzeinkommen) {
		this.ersatzeinkommen = ersatzeinkommen;
	}

	public BigDecimal getErhalteneAlimente() {
		return erhalteneAlimente;
	}

	public void setErhalteneAlimente(final BigDecimal erhalteneAlimente) {
		this.erhalteneAlimente = erhalteneAlimente;
	}

	public BigDecimal getBruttovermoegen() {
		return bruttovermoegen;
	}

	public void setBruttovermoegen(final BigDecimal bruttovermoegen) {
		this.bruttovermoegen = bruttovermoegen;
	}

	public BigDecimal getSchulden() {
		return schulden;
	}

	public void setSchulden(final BigDecimal schulden) {
		this.schulden = schulden;
	}

	public BigDecimal getGeschaeftsgewinnBasisjahr() {
		return geschaeftsgewinnBasisjahr;
	}

	public void setGeschaeftsgewinnBasisjahr(final BigDecimal geschaeftsgewinnBasisjahr) {
		this.geschaeftsgewinnBasisjahr = geschaeftsgewinnBasisjahr;
	}

	public BigDecimal getGeleisteteAlimente() {
		return geleisteteAlimente;
	}

	public void setGeleisteteAlimente(final BigDecimal geleisteteAlimente) {
		this.geleisteteAlimente = geleisteteAlimente;
	}

	@Nonnull
	public AbstractFinanzielleSituation copyAbstractFinanzielleSituation(@Nonnull AbstractFinanzielleSituation target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setSteuerveranlagungErhalten(this.getSteuerveranlagungErhalten());
			target.setSteuererklaerungAusgefuellt(this.getSteuererklaerungAusgefuellt());
			target.setFamilienzulage(this.getFamilienzulage());
			target.setErsatzeinkommen(this.getErsatzeinkommen());
			target.setErhalteneAlimente(this.getErhalteneAlimente());
			target.setBruttovermoegen(this.getBruttovermoegen());
			target.setSchulden(this.getSchulden());
			target.setGeschaeftsgewinnBasisjahr(this.getGeschaeftsgewinnBasisjahr());
			target.setGeleisteteAlimente(this.getGeleisteteAlimente());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@SuppressWarnings("OverlyComplexMethod")
	@Override
	public boolean isSame(AbstractEntity other) {
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof AbstractFinanzielleSituation)) {
			return false;
		}
		final AbstractFinanzielleSituation otherFinSituation = (AbstractFinanzielleSituation) other;
		return Objects.equals(getSteuerveranlagungErhalten(), otherFinSituation.getSteuerveranlagungErhalten()) &&
			Objects.equals(getSteuererklaerungAusgefuellt(), otherFinSituation.getSteuererklaerungAusgefuellt()) &&
			MathUtil.isSame(getFamilienzulage(), otherFinSituation.getFamilienzulage()) &&
			MathUtil.isSame(getErsatzeinkommen(), otherFinSituation.getErsatzeinkommen()) &&
			MathUtil.isSame(getErhalteneAlimente(), otherFinSituation.getErhalteneAlimente()) &&
			MathUtil.isSame(getBruttovermoegen(), otherFinSituation.getBruttovermoegen()) &&
			MathUtil.isSame(getSchulden(), otherFinSituation.getSchulden()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahr(), otherFinSituation.getGeschaeftsgewinnBasisjahr()) &&
			MathUtil.isSame(getGeleisteteAlimente(), otherFinSituation.getGeleisteteAlimente());
	}

	@Nonnull
	public final BigDecimal getZwischentotalEinkommen() {
		return MathUtil.DEFAULT.addNullSafe(BigDecimal.ZERO, getNettolohn(), getFamilienzulage(), getErsatzeinkommen(), getErhalteneAlimente(), getGeschaeftsgewinnBasisjahr());
	}

	@Nonnull
	public final BigDecimal getZwischentotalVermoegen() {
		BigDecimal vermoegenPlus = MathUtil.DEFAULT.addNullSafe(BigDecimal.ZERO, getBruttovermoegen());
		return MathUtil.DEFAULT.subtractNullSafe(vermoegenPlus, getSchulden());
	}

	@Nonnull
	public final BigDecimal getZwischetotalAbzuege() {
		return MathUtil.DEFAULT.addNullSafe(BigDecimal.ZERO, getGeleisteteAlimente());
	}
}
