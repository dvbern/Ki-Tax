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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

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
	private Boolean automatischePruefungErlaubt;

	@Nullable
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private SteuerdatenAnfrageStatus steuerdatenAbfrageStatus;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahrMinus2;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geschaeftsgewinnBasisjahrMinus1;

	@Nullable
	@Column(nullable = true)
	private Boolean quellenbesteuert;

	@Nullable
	@Column(nullable = true)
	private Boolean gemeinsameStekVorjahr;

	@Nullable
	@Column(nullable = true)
	private Boolean alleinigeStekVorjahr;

	@Nullable
	@Column(nullable = true)
	private Boolean veranlagt;

	@Nullable
	@Column(nullable = true)
	private BigDecimal unterhaltsBeitraege;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzuegeKinderAusbildung;

	@Nullable
	@Column(nullable = true)
	private BigDecimal bruttoLohn;

	@Nullable
	@OneToOne(optional = true, orphanRemoval = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_finanzielle_situation_stuerdaten_response"))
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private SteuerdatenResponse steuerdatenResponse;

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

	public void setSteuerdatenZugriff(@Nullable Boolean steuerdatenZugriff) {
		this.steuerdatenZugriff = steuerdatenZugriff;
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

	@Nullable
	public Boolean getQuellenbesteuert() {
		return quellenbesteuert;
	}

	public void setQuellenbesteuert(@Nullable Boolean quellenbesteuert) {
		this.quellenbesteuert = quellenbesteuert;
	}

	@Nullable
	public Boolean getGemeinsameStekVorjahr() {
		return gemeinsameStekVorjahr;
	}

	public void setGemeinsameStekVorjahr(@Nullable Boolean gemeinsameStekVorjahr) {
		this.gemeinsameStekVorjahr = gemeinsameStekVorjahr;
	}

	@Nullable
	public Boolean getAlleinigeStekVorjahr() {
		return alleinigeStekVorjahr;
	}

	public void setAlleinigeStekVorjahr(@Nullable Boolean alleinigeStekVorjahr) {
		this.alleinigeStekVorjahr = alleinigeStekVorjahr;
	}

	@Nullable
	public Boolean getVeranlagt() {
		return veranlagt;
	}

	public void setVeranlagt(@Nullable Boolean veranlagt) {
		this.veranlagt = veranlagt;
	}

	@Nullable
	public BigDecimal getUnterhaltsBeitraege() {
		return unterhaltsBeitraege;
	}

	public void setUnterhaltsBeitraege(@Nullable BigDecimal unterhaltsBeitraege) {
		this.unterhaltsBeitraege = unterhaltsBeitraege;
	}

	@Nullable
	public BigDecimal getAbzuegeKinderAusbildung() {
		return abzuegeKinderAusbildung;
	}

	public void setAbzuegeKinderAusbildung(@Nullable BigDecimal abzuegeKinderAusbildung) {
		this.abzuegeKinderAusbildung = abzuegeKinderAusbildung;
	}

	@Nullable
	public BigDecimal getBruttoLohn() {
		return bruttoLohn;
	}

	public void setBruttoLohn(@Nullable BigDecimal bruttoLohn) {
		this.bruttoLohn = bruttoLohn;
	}

	@Nullable
	public SteuerdatenAnfrageStatus getSteuerdatenAbfrageStatus() {
		return steuerdatenAbfrageStatus;
	}

	public void setSteuerdatenAbfrageStatus(@Nullable SteuerdatenAnfrageStatus steuerdatenAbfrageStatus) {
		this.steuerdatenAbfrageStatus = steuerdatenAbfrageStatus;
	}

	@Nullable
	public Boolean isAutomatischePruefungErlaubt() {
		return automatischePruefungErlaubt;
	}

	public void setAutomatischePruefungErlaubt(@Nullable Boolean automatischePruefungErlaubt) {
		this.automatischePruefungErlaubt = automatischePruefungErlaubt;
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
			target.setGemeinsameStekVorjahr(this.getGemeinsameStekVorjahr());
			target.setAlleinigeStekVorjahr(this.getAlleinigeStekVorjahr());
			target.setQuellenbesteuert(this.quellenbesteuert);
			target.setVeranlagt(this.getVeranlagt());
			target.setUnterhaltsBeitraege(this.getUnterhaltsBeitraege());
			target.setAbzuegeKinderAusbildung(this.getAbzuegeKinderAusbildung());
			target.setBruttoLohn(this.getBruttoLohn());
			if (this.getSelbstdeklaration() != null) {
				target.setSelbstdeklaration(this.getSelbstdeklaration().copySelbsteklaration(new FinanzielleSituationSelbstdeklaration(), copyType));
			}
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
			Objects.equals(isAutomatischePruefungErlaubt(), otherFinSit.isAutomatischePruefungErlaubt()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus1(), otherFinSit.getGeschaeftsgewinnBasisjahrMinus1()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus2(), otherFinSit.getGeschaeftsgewinnBasisjahrMinus2()) &&
			Objects.equals(getAlleinigeStekVorjahr(), otherFinSit.getAlleinigeStekVorjahr()) &&
			Objects.equals(getGemeinsameStekVorjahr(), otherFinSit.getGemeinsameStekVorjahr()) &&
			Objects.equals(getQuellenbesteuert(), otherFinSit.getQuellenbesteuert()) &&
			Objects.equals(getVeranlagt(), otherFinSit.getVeranlagt()) &&
			Objects.equals(getSelbstdeklaration(), otherFinSit.getSelbstdeklaration()) &&
			MathUtil.isSame(getBruttoLohn(), otherFinSit.getBruttoLohn()) &&
			MathUtil.isSame(getAbzuegeKinderAusbildung(), otherFinSit.getAbzuegeKinderAusbildung()) &&
			MathUtil.isSame(getUnterhaltsBeitraege(), otherFinSit.getUnterhaltsBeitraege());
	}

	@Nullable
	public SteuerdatenResponse getSteuerdatenResponse() {
		return steuerdatenResponse;
	}

	public void setSteuerdatenResponse(@Nullable SteuerdatenResponse steuerdatenResponse) {
		this.steuerdatenResponse = steuerdatenResponse;
	}
}
