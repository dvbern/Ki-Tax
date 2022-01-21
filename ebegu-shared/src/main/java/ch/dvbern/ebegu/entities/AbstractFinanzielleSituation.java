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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

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

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettolohn;

	@Nullable
	@Column(nullable = true)
	private BigDecimal familienzulage;

	@Nullable
	@Column(nullable = true)
	private BigDecimal ersatzeinkommen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal erhalteneAlimente;

	@Nullable
	@Column(nullable = true)
	private BigDecimal bruttovermoegen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal schulden;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahr;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geleisteteAlimente;

	@Nullable
	@Column(nullable = true)
	private BigDecimal steuerbaresEinkommen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal steuerbaresVermoegen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzuegeLiegenschaft;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsverlust;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkaeufeVorsorge;

	@Nullable
	@Transient
	private BigDecimal durchschnittlicherGeschaeftsgewinn;

	public AbstractFinanzielleSituation() {
	}

	public abstract Boolean getSteuerveranlagungErhalten();

	public abstract Boolean getSteuererklaerungAusgefuellt();

	@Nullable
	public BigDecimal getNettolohn() {
		return nettolohn;
	}

	public void setNettolohn(@Nullable final BigDecimal nettolohn) {
		this.nettolohn = nettolohn;
	}

	@Nullable
	public BigDecimal getFamilienzulage() {
		return familienzulage;
	}

	public void setFamilienzulage(@Nullable final BigDecimal familienzulage) {
		this.familienzulage = familienzulage;
	}

	@Nullable
	public BigDecimal getErsatzeinkommen() {
		return ersatzeinkommen;
	}

	public void setErsatzeinkommen(@Nullable final BigDecimal ersatzeinkommen) {
		this.ersatzeinkommen = ersatzeinkommen;
	}

	@Nullable
	public BigDecimal getErhalteneAlimente() {
		return erhalteneAlimente;
	}

	public void setErhalteneAlimente(@Nullable final BigDecimal erhalteneAlimente) {
		this.erhalteneAlimente = erhalteneAlimente;
	}

	@Nullable
	public BigDecimal getBruttovermoegen() {
		return bruttovermoegen;
	}

	public void setBruttovermoegen(@Nullable final BigDecimal bruttovermoegen) {
		this.bruttovermoegen = bruttovermoegen;
	}

	@Nullable
	public BigDecimal getSchulden() {
		return schulden;
	}

	public void setSchulden(@Nullable final BigDecimal schulden) {
		this.schulden = schulden;
	}

	@Nullable
	public BigDecimal getGeschaeftsgewinnBasisjahr() {
		return geschaeftsgewinnBasisjahr;
	}

	public void setGeschaeftsgewinnBasisjahr(@Nullable final BigDecimal geschaeftsgewinnBasisjahr) {
		this.geschaeftsgewinnBasisjahr = geschaeftsgewinnBasisjahr;
	}

	@Nullable
	public BigDecimal getGeleisteteAlimente() {
		return geleisteteAlimente;
	}

	public void setGeleisteteAlimente(@Nullable final BigDecimal geleisteteAlimente) {
		this.geleisteteAlimente = geleisteteAlimente;
	}

	@Nullable
	public BigDecimal getDurchschnittlicherGeschaeftsgewinn() {
		return durchschnittlicherGeschaeftsgewinn;
	}

	public void setDurchschnittlicherGeschaeftsgewinn(@Nullable BigDecimal durchschnittlicherGeschaeftsgewinn) {
		this.durchschnittlicherGeschaeftsgewinn = durchschnittlicherGeschaeftsgewinn;
	}

	@Nullable
	public BigDecimal getSteuerbaresEinkommen() {
		return steuerbaresEinkommen;
	}

	public void setSteuerbaresEinkommen(@Nullable BigDecimal steuerbaresEinkommen) {
		this.steuerbaresEinkommen = steuerbaresEinkommen;
	}

	@Nullable
	public BigDecimal getSteuerbaresVermoegen() {
		return steuerbaresVermoegen;
	}

	public void setSteuerbaresVermoegen(@Nullable BigDecimal steuerbaresVermoegen) {
		this.steuerbaresVermoegen = steuerbaresVermoegen;
	}

	@Nullable
	public BigDecimal getAbzuegeLiegenschaft() {
		return abzuegeLiegenschaft;
	}

	public void setAbzuegeLiegenschaft(@Nullable BigDecimal abzuegeLiegenschaft) {
		this.abzuegeLiegenschaft = abzuegeLiegenschaft;
	}

	@Nullable
	public BigDecimal getGeschaeftsverlust() {
		return geschaeftsverlust;
	}

	public void setGeschaeftsverlust(@Nullable BigDecimal geschaeftsverlust) {
		this.geschaeftsverlust = geschaeftsverlust;
	}

	@Nullable
	public BigDecimal getEinkaeufeVorsorge() {
		return einkaeufeVorsorge;
	}

	public void setEinkaeufeVorsorge(@Nullable BigDecimal einkaeufeVorsorge) {
		this.einkaeufeVorsorge = einkaeufeVorsorge;
	}

	@Nonnull
	public AbstractFinanzielleSituation copyAbstractFinanzielleSituation(
		@Nonnull AbstractFinanzielleSituation target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setNettolohn(this.getNettolohn());
			target.setFamilienzulage(this.getFamilienzulage());
			target.setErsatzeinkommen(this.getErsatzeinkommen());
			target.setErhalteneAlimente(this.getErhalteneAlimente());
			target.setBruttovermoegen(this.getBruttovermoegen());
			target.setSchulden(this.getSchulden());
			target.setGeschaeftsgewinnBasisjahr(this.getGeschaeftsgewinnBasisjahr());
			target.setGeleisteteAlimente(this.getGeleisteteAlimente());
			target.setSteuerbaresEinkommen(this.getSteuerbaresEinkommen());
			target.setSteuerbaresVermoegen(this.getSteuerbaresVermoegen());
			target.setAbzuegeLiegenschaft(this.getAbzuegeLiegenschaft());
			target.setGeschaeftsverlust(this.getGeschaeftsverlust());
			target.setEinkaeufeVorsorge(this.getEinkaeufeVorsorge());
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
		return MathUtil.isSame(getNettolohn(), otherFinSituation.getNettolohn()) &&
			MathUtil.isSame(getFamilienzulage(), otherFinSituation.getFamilienzulage()) &&
			MathUtil.isSame(getErsatzeinkommen(), otherFinSituation.getErsatzeinkommen()) &&
			MathUtil.isSame(getErhalteneAlimente(), otherFinSituation.getErhalteneAlimente()) &&
			MathUtil.isSame(getBruttovermoegen(), otherFinSituation.getBruttovermoegen()) &&
			MathUtil.isSame(getSchulden(), otherFinSituation.getSchulden()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahr(), otherFinSituation.getGeschaeftsgewinnBasisjahr()) &&
			MathUtil.isSame(getGeleisteteAlimente(), otherFinSituation.getGeleisteteAlimente()) &&
			MathUtil.isSame(getSteuerbaresEinkommen(), otherFinSituation.getSteuerbaresEinkommen()) &&
			MathUtil.isSame(getSteuerbaresVermoegen(), otherFinSituation.getSteuerbaresVermoegen()) &&
			MathUtil.isSame(getAbzuegeLiegenschaft(), otherFinSituation.getAbzuegeLiegenschaft()) &&
			MathUtil.isSame(getGeschaeftsverlust(), otherFinSituation.getGeschaeftsverlust()) &&
			MathUtil.isSame(getEinkaeufeVorsorge(), otherFinSituation.getEinkaeufeVorsorge());
	}

	@Nonnull
	public final BigDecimal getZwischentotalEinkommen() {
		return MathUtil.DEFAULT.addNullSafe(
			BigDecimal.ZERO,
			getNettolohn(),
			getFamilienzulage(),
			getErsatzeinkommen(),
			getErhalteneAlimente(),
			getDurchschnittlicherGeschaeftsgewinn());
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
