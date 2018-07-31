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

	@Column(nullable = true)
	private BigDecimal nettolohnJan;

	@Column(nullable = true)
	private BigDecimal nettolohnFeb;

	@Column(nullable = true)
	private BigDecimal nettolohnMrz;

	@Column(nullable = true)
	private BigDecimal nettolohnApr;

	@Column(nullable = true)
	private BigDecimal nettolohnMai;

	@Column(nullable = true)
	private BigDecimal nettolohnJun;

	@Column(nullable = true)
	private BigDecimal nettolohnJul;

	@Column(nullable = true)
	private BigDecimal nettolohnAug;

	@Column(nullable = true)
	private BigDecimal nettolohnSep;

	@Column(nullable = true)
	private BigDecimal nettolohnOkt;

	@Column(nullable = true)
	private BigDecimal nettolohnNov;

	@Column(nullable = true)
	private BigDecimal nettolohnDez;

	@Column(nullable = true)
	private BigDecimal nettolohnZus;

	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahrMinus1;

	public Einkommensverschlechterung() {
	}

	@Nullable
	public BigDecimal getNettolohnJan() {
		return nettolohnJan;
	}

	public void setNettolohnJan(@Nullable final BigDecimal nettolohnJan) {
		this.nettolohnJan = nettolohnJan;
	}

	@Nullable
	public BigDecimal getNettolohnFeb() {
		return nettolohnFeb;
	}

	public void setNettolohnFeb(@Nullable final BigDecimal nettolohnFeb) {
		this.nettolohnFeb = nettolohnFeb;
	}

	@Nullable
	public BigDecimal getNettolohnMrz() {
		return nettolohnMrz;
	}

	public void setNettolohnMrz(@Nullable final BigDecimal nettolohnMrz) {
		this.nettolohnMrz = nettolohnMrz;
	}

	@Nullable
	public BigDecimal getNettolohnApr() {
		return nettolohnApr;
	}

	public void setNettolohnApr(@Nullable final BigDecimal nettolohnApr) {
		this.nettolohnApr = nettolohnApr;
	}

	@Nullable
	public BigDecimal getNettolohnMai() {
		return nettolohnMai;
	}

	public void setNettolohnMai(@Nullable final BigDecimal nettolohnMai) {
		this.nettolohnMai = nettolohnMai;
	}

	@Nullable
	public BigDecimal getNettolohnJun() {
		return nettolohnJun;
	}

	public void setNettolohnJun(@Nullable final BigDecimal nettolohnJun) {
		this.nettolohnJun = nettolohnJun;
	}

	@Nullable
	public BigDecimal getNettolohnJul() {
		return nettolohnJul;
	}

	public void setNettolohnJul(@Nullable final BigDecimal nettolohnJul) {
		this.nettolohnJul = nettolohnJul;
	}

	@Nullable
	public BigDecimal getNettolohnAug() {
		return nettolohnAug;
	}

	public void setNettolohnAug(@Nullable final BigDecimal nettolohnAug) {
		this.nettolohnAug = nettolohnAug;
	}

	@Nullable
	public BigDecimal getNettolohnSep() {
		return nettolohnSep;
	}

	public void setNettolohnSep(@Nullable final BigDecimal nettolohnSep) {
		this.nettolohnSep = nettolohnSep;
	}

	@Nullable
	public BigDecimal getNettolohnOkt() {
		return nettolohnOkt;
	}

	public void setNettolohnOkt(@Nullable final BigDecimal nettolohnOkt) {
		this.nettolohnOkt = nettolohnOkt;
	}

	@Nullable
	public BigDecimal getNettolohnNov() {
		return nettolohnNov;
	}

	public void setNettolohnNov(@Nullable final BigDecimal nettolohnNov) {
		this.nettolohnNov = nettolohnNov;
	}

	@Nullable
	public BigDecimal getNettolohnDez() {
		return nettolohnDez;
	}

	public void setNettolohnDez(@Nullable final BigDecimal nettolohnDez) {
		this.nettolohnDez = nettolohnDez;
	}

	@Nullable
	public BigDecimal getGeschaeftsgewinnBasisjahrMinus1() {
		return geschaeftsgewinnBasisjahrMinus1;
	}

	public void setGeschaeftsgewinnBasisjahrMinus1(@Nullable BigDecimal geschaeftsgewinnBasisjahrMinus1) {
		this.geschaeftsgewinnBasisjahrMinus1 = geschaeftsgewinnBasisjahrMinus1;
	}

	@Nullable
	public BigDecimal getNettolohnZus() {
		return nettolohnZus;
	}

	public void setNettolohnZus(@Nullable BigDecimal nettolohnZus) {
		this.nettolohnZus = nettolohnZus;
	}

	@Override
	public BigDecimal getNettolohn() {

		return MathUtil.DEFAULT.add(nettolohnJan, nettolohnFeb, nettolohnMrz, nettolohnApr,
			nettolohnMai, nettolohnJun, nettolohnJul, nettolohnAug, nettolohnSep,
			nettolohnOkt, nettolohnNov, nettolohnDez, nettolohnZus);
	}

	public Einkommensverschlechterung copyEinkommensverschlechterung(@Nonnull Einkommensverschlechterung target, @Nonnull AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			super.copyAbstractFinanzielleSituation(target, copyType);
			target.setNettolohnJan(this.getNettolohnJan());
			target.setNettolohnFeb(this.getNettolohnFeb());
			target.setNettolohnMrz(this.getNettolohnMrz());
			target.setNettolohnApr(this.getNettolohnApr());
			target.setNettolohnMai(this.getNettolohnMai());
			target.setNettolohnJun(this.getNettolohnJun());
			target.setNettolohnJul(this.getNettolohnJul());
			target.setNettolohnAug(this.getNettolohnAug());
			target.setNettolohnSep(this.getNettolohnSep());
			target.setNettolohnOkt(this.getNettolohnOkt());
			target.setNettolohnNov(this.getNettolohnNov());
			target.setNettolohnDez(this.getNettolohnDez());
			target.setNettolohnZus(this.getNettolohnZus());
			target.setGeschaeftsgewinnBasisjahrMinus1(this.getGeschaeftsgewinnBasisjahrMinus1());
			break;
		case ERNEUERUNG:
			break;
		}
		return target;
	}

//	public Einkommensverschlechterung copyForMutation(Einkommensverschlechterung mutation) {
//		super.copyForMutation(mutation);
//		mutation.setNettolohnJan(this.getNettolohnJan());
//		mutation.setNettolohnFeb(this.getNettolohnFeb());
//		mutation.setNettolohnMrz(this.getNettolohnMrz());
//		mutation.setNettolohnApr(this.getNettolohnApr());
//		mutation.setNettolohnMai(this.getNettolohnMai());
//		mutation.setNettolohnJun(this.getNettolohnJun());
//		mutation.setNettolohnJul(this.getNettolohnJul());
//		mutation.setNettolohnAug(this.getNettolohnAug());
//		mutation.setNettolohnSep(this.getNettolohnSep());
//		mutation.setNettolohnOkt(this.getNettolohnOkt());
//		mutation.setNettolohnNov(this.getNettolohnNov());
//		mutation.setNettolohnDez(this.getNettolohnDez());
//		mutation.setNettolohnZus(this.getNettolohnZus());
//		mutation.setGeschaeftsgewinnBasisjahrMinus1(this.getGeschaeftsgewinnBasisjahrMinus1());
//		return mutation;
//	}

	@SuppressWarnings("OverlyComplexMethod")
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
		return MathUtil.isSame(getNettolohnJan(), otherEinkommensverschlechterung.getNettolohnJan()) &&
			MathUtil.isSame(getNettolohnFeb(), otherEinkommensverschlechterung.getNettolohnFeb()) &&
			MathUtil.isSame(getNettolohnMrz(), otherEinkommensverschlechterung.getNettolohnMrz()) &&
			MathUtil.isSame(getNettolohnApr(), otherEinkommensverschlechterung.getNettolohnApr()) &&
			MathUtil.isSame(getNettolohnMai(), otherEinkommensverschlechterung.getNettolohnMai()) &&
			MathUtil.isSame(getNettolohnJun(), otherEinkommensverschlechterung.getNettolohnJun()) &&
			MathUtil.isSame(getNettolohnJul(), otherEinkommensverschlechterung.getNettolohnJul()) &&
			MathUtil.isSame(getNettolohnAug(), otherEinkommensverschlechterung.getNettolohnAug()) &&
			MathUtil.isSame(getNettolohnSep(), otherEinkommensverschlechterung.getNettolohnSep()) &&
			MathUtil.isSame(getNettolohnOkt(), otherEinkommensverschlechterung.getNettolohnOkt()) &&
			MathUtil.isSame(getNettolohnNov(), otherEinkommensverschlechterung.getNettolohnNov()) &&
			MathUtil.isSame(getNettolohnDez(), otherEinkommensverschlechterung.getNettolohnDez()) &&
			MathUtil.isSame(getNettolohnZus(), otherEinkommensverschlechterung.getNettolohnZus()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus1(), otherEinkommensverschlechterung.getGeschaeftsgewinnBasisjahrMinus1());
	}
}
