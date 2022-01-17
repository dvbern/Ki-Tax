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
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
	private BigDecimal bruttoertraegeVermoegen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettoertraegeErbengemeinschaft;

	@Nullable
	@Column(nullable = true)
	private BigDecimal nettoVermoegen;

	@Nullable
	@Column(nullable = true)
	private Boolean einkommenInVereinfachtemVerfahrenAbgerechnet;

	@Nullable
	@Column(nullable = true)
	private BigDecimal amountEinkommenInVereinfachtemVerfahrenAbgerechnet;

	@Nullable
	@Column(nullable = true)
	private BigDecimal gewinnungskosten;

	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_finanziellesituation_selbstdeklaration_id"), nullable = true)
	private FinanzielleSituationSelbstdeklaration selbstdeklaration;

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
	public FinanzielleSituationSelbstdeklaration getSelbstdeklaration() {
		return selbstdeklaration;
	}

	public void setSelbstdeklaration(@Nullable FinanzielleSituationSelbstdeklaration selbstdeklaration) {
		this.selbstdeklaration = selbstdeklaration;
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
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus2(), otherFinSit.getGeschaeftsgewinnBasisjahrMinus2()) &&
			Objects.equals(getAlleinigeStekVorjahr(), otherFinSit.getAlleinigeStekVorjahr()) &&
			Objects.equals(getGemeinsameStekVorjahr(), otherFinSit.getGemeinsameStekVorjahr()) &&
			Objects.equals(getQuellenbesteuert(), otherFinSit.getQuellenbesteuert()) &&
			Objects.equals(getVeranlagt(), otherFinSit.getVeranlagt()) &&
			Objects.equals(getSelbstdeklaration(), otherFinSit.getSelbstdeklaration());
	}

	@Nullable
	public BigDecimal getBruttoertraegeVermoegen() {
		return bruttoertraegeVermoegen;
	}

	public void setBruttoertraegeVermoegen(@Nullable BigDecimal bruttoertraegeVermoegen) {
		this.bruttoertraegeVermoegen = bruttoertraegeVermoegen;
	}

	@Nullable
	public BigDecimal getNettoertraegeErbengemeinschaft() {
		return nettoertraegeErbengemeinschaft;
	}

	public void setNettoertraegeErbengemeinschaft(@Nullable BigDecimal nettoertraegeErbengemeinschaft) {
		this.nettoertraegeErbengemeinschaft = nettoertraegeErbengemeinschaft;
	}

	@Nullable
	public BigDecimal getNettoVermoegen() {
		return nettoVermoegen;
	}

	public void setNettoVermoegen(@Nullable BigDecimal nettoVermoegen) {
		this.nettoVermoegen = nettoVermoegen;
	}

	@Nullable
	public Boolean getEinkommenInVereinfachtemVerfahrenAbgerechnet() {
		return einkommenInVereinfachtemVerfahrenAbgerechnet;
	}

	public void setEinkommenInVereinfachtemVerfahrenAbgerechnet(
			@Nullable Boolean einkommenInVereinfachtemVerfahrenAbgerechnet) {
		this.einkommenInVereinfachtemVerfahrenAbgerechnet = einkommenInVereinfachtemVerfahrenAbgerechnet;
	}

	@Nullable
	public BigDecimal getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet() {
		return amountEinkommenInVereinfachtemVerfahrenAbgerechnet;
	}

	public void setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(
			@Nullable BigDecimal amountEinkommenInVereinfachtemVerfahrenAbgerechnet) {
		this.amountEinkommenInVereinfachtemVerfahrenAbgerechnet = amountEinkommenInVereinfachtemVerfahrenAbgerechnet;
	}

	@Nullable
	public BigDecimal getGewinnungskosten() {
		return gewinnungskosten;
	}

	public void setGewinnungskosten(@Nullable BigDecimal gewinnungskosten) {
		this.gewinnungskosten = gewinnungskosten;
	}
}
