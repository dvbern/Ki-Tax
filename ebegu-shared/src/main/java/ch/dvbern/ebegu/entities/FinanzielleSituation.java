/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.util.MathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
	private LocalDateTime steuerdatenAbfrageTimestamp;

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
	private Boolean veranlagtVorjahr;

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
	@Column(nullable = true)
	private Boolean momentanSelbststaendig;

	@Nullable
	@Column(nullable = true)
	@Min(0)
	private BigDecimal ersatzeinkommenSelbststaendigkeitBasisjahrMinus2;

	@Nullable
	@OneToOne(optional = true, orphanRemoval = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_finanzielle_situation_stuerdaten_response"))
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private SteuerdatenResponse steuerdatenResponse;

	public FinanzielleSituation() {
	}

	@Override
	public Boolean getSteuerveranlagungErhalten() {
		return steuerveranlagungErhalten;
	}

	public void setSteuerveranlagungErhalten(final Boolean steuerveranlagungErhalten) {
		this.steuerveranlagungErhalten = steuerveranlagungErhalten;
	}

	@Override
	public Boolean getSteuererklaerungAusgefuellt() {
		return steuererklaerungAusgefuellt;
	}

	public void setSteuererklaerungAusgefuellt(final Boolean steuererklaerungAusgefuellt) {
		this.steuererklaerungAusgefuellt = steuererklaerungAusgefuellt;
	}

	@Override
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
	public Boolean getVeranlagtVorjahr() {
		return veranlagtVorjahr;
	}

	public void setVeranlagtVorjahr(@Nullable Boolean veranlagtVorjahr) {
		this.veranlagtVorjahr = veranlagtVorjahr;
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

	@Override
	@Nullable
	public SteuerdatenAnfrageStatus getSteuerdatenAbfrageStatus() {
		return steuerdatenAbfrageStatus;
	}

	public void setSteuerdatenAbfrageStatus(@Nullable SteuerdatenAnfrageStatus steuerdatenAbfrageStatus) {
		this.steuerdatenAbfrageStatus = steuerdatenAbfrageStatus;
	}

	@Nullable
	public LocalDateTime getSteuerdatenAbfrageTimestamp() {
		return steuerdatenAbfrageTimestamp;
	}

	public void setSteuerdatenAbfrageTimestamp(@Nullable LocalDateTime steuerdatenAbfrageTimestamp) {
		this.steuerdatenAbfrageTimestamp = steuerdatenAbfrageTimestamp;
	}

	@Nullable
	public Boolean getAutomatischePruefungErlaubt() {
		return automatischePruefungErlaubt;
	}

	public void setAutomatischePruefungErlaubt(@Nullable Boolean automatischePruefungErlaubt) {
		this.automatischePruefungErlaubt = automatischePruefungErlaubt;
	}

	@Nullable
	public Boolean getMomentanSelbststaendig() {
		return momentanSelbststaendig;
	}

	public void setMomentanSelbststaendig(@Nullable Boolean momentanSelbststaendig) {
		this.momentanSelbststaendig = momentanSelbststaendig;
	}

	@Nullable
	public BigDecimal getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2() {
		return ersatzeinkommenSelbststaendigkeitBasisjahrMinus2;
	}

	public void setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(
		@Nullable BigDecimal ersatzeinkommenSelbststaendigkeitBasisjahrMinus2) {
		this.ersatzeinkommenSelbststaendigkeitBasisjahrMinus2 = ersatzeinkommenSelbststaendigkeitBasisjahrMinus2;
	}

	@Nonnull
	public FinanzielleSituation copyFinanzielleSituation(@Nonnull FinanzielleSituation target, @Nonnull AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_AR_2023:
			super.copyAbstractFinanzielleSituation(target, copyType);
			target.setSteuerveranlagungErhalten(this.getSteuerveranlagungErhalten());
			target.setSteuererklaerungAusgefuellt(this.getSteuererklaerungAusgefuellt());
			target.setGeschaeftsgewinnBasisjahrMinus1(this.getGeschaeftsgewinnBasisjahrMinus1());
			target.setGeschaeftsgewinnBasisjahrMinus2(this.getGeschaeftsgewinnBasisjahrMinus2());
			target.setSteuerdatenZugriff(this.getSteuerdatenZugriff());
			target.setGemeinsameStekVorjahr(this.getGemeinsameStekVorjahr());
			target.setAlleinigeStekVorjahr(this.getAlleinigeStekVorjahr());
			target.setQuellenbesteuert(this.getQuellenbesteuert());
			target.setVeranlagt(this.getVeranlagt());
			target.setVeranlagtVorjahr(this.getVeranlagtVorjahr());
			target.setUnterhaltsBeitraege(this.getUnterhaltsBeitraege());
			target.setAbzuegeKinderAusbildung(this.getAbzuegeKinderAusbildung());
			target.setBruttoLohn(this.getBruttoLohn());
			target.setSteuerdatenAbfrageStatus(this.getSteuerdatenAbfrageStatus());
			target.setSteuerdatenAbfrageTimestamp(this.getSteuerdatenAbfrageTimestamp());
			target.setAutomatischePruefungErlaubt(this.getAutomatischePruefungErlaubt());
			target.setMomentanSelbststaendig(this.getMomentanSelbststaendig());
			target.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(this.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
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
		final FinanzielleSituation otherFinSit = (FinanzielleSituation) other;
		return Objects.equals(getSteuerveranlagungErhalten(), otherFinSit.getSteuerveranlagungErhalten()) &&
				Objects.equals(getSteuererklaerungAusgefuellt(), otherFinSit.getSteuererklaerungAusgefuellt()) &&
				Objects.equals(getSteuerdatenZugriff(), otherFinSit.getSteuerdatenZugriff()) &&
				Objects.equals(getAutomatischePruefungErlaubt(), otherFinSit.getAutomatischePruefungErlaubt()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus1(), otherFinSit.getGeschaeftsgewinnBasisjahrMinus1()) &&
			MathUtil.isSame(getGeschaeftsgewinnBasisjahrMinus2(), otherFinSit.getGeschaeftsgewinnBasisjahrMinus2()) &&
			Objects.equals(getAlleinigeStekVorjahr(), otherFinSit.getAlleinigeStekVorjahr()) &&
			Objects.equals(getGemeinsameStekVorjahr(), otherFinSit.getGemeinsameStekVorjahr()) &&
			Objects.equals(getQuellenbesteuert(), otherFinSit.getQuellenbesteuert()) &&
			Objects.equals(getVeranlagt(), otherFinSit.getVeranlagt()) &&
			Objects.equals(getVeranlagtVorjahr(), otherFinSit.getVeranlagtVorjahr()) &&
			Objects.equals(getSelbstdeklaration(), otherFinSit.getSelbstdeklaration()) &&
			MathUtil.isSame(getBruttoLohn(), otherFinSit.getBruttoLohn()) &&
			MathUtil.isSame(getAbzuegeKinderAusbildung(), otherFinSit.getAbzuegeKinderAusbildung()) &&
			MathUtil.isSame(getUnterhaltsBeitraege(), otherFinSit.getUnterhaltsBeitraege()) &&
			Objects.equals(getMomentanSelbststaendig(), otherFinSit.getMomentanSelbststaendig()) &&
			MathUtil.isSame(getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(), otherFinSit.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2());
	}

	@Nullable
	public SteuerdatenResponse getSteuerdatenResponse() {
		return steuerdatenResponse;
	}

	public void setSteuerdatenResponse(@Nullable SteuerdatenResponse steuerdatenResponse) {
		this.steuerdatenResponse = steuerdatenResponse;
	}
}
