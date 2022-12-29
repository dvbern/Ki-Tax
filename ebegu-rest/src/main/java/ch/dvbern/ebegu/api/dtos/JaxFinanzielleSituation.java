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

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;

/**
 * DTO fuer Finanzielle Situation
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxFinanzielleSituation extends JaxAbstractFinanzielleSituation {

	private static final long serialVersionUID = -403919135454757656L;

	@NotNull
	private Boolean steuerveranlagungErhalten;

	@NotNull
	private Boolean steuererklaerungAusgefuellt;

	@Nullable
	private Boolean quellenbesteuert;

	@Nullable
	private Boolean gemeinsameStekVorjahr;

	@Nullable
	private Boolean alleinigeStekVorjahr;

	@Nullable
	private Boolean veranlagt;

	@Nullable
	private Boolean veranlagtVorjahr;

	@Nullable
	private Boolean steuerdatenZugriff;

	@Nullable
	private Boolean automatischePruefungErlaubt;

	@Nullable
	private BigDecimal geschaeftsgewinnBasisjahrMinus2;

	@Nullable
	private BigDecimal geschaeftsgewinnBasisjahrMinus1;

	@Nullable
	private BigDecimal unterhaltsBeitraege;

	@Nullable
	private BigDecimal abzuegeKinderAusbildung;

	@Nullable
	private BigDecimal bruttoLohn;

	@Nullable
	private SteuerdatenAnfrageStatus steuerdatenAbfrageStatus;

	@Nullable
	private Boolean momentanSelbststaendig;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime timestampSteuerdatenResponse;

	@Nullable
	private Veranlagungsstand steuerdatenVeranlagungsstand;

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
	public Boolean getSteuerdatenZugriff() {
		return steuerdatenZugriff;
	}

	public void setSteuerdatenZugriff(@Nullable Boolean steuerdatenZugriff) {
		this.steuerdatenZugriff = steuerdatenZugriff;
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

	@Nullable
	public SteuerdatenAnfrageStatus getSteuerdatenAbfrageStatus() {
		return steuerdatenAbfrageStatus;
	}

	public void setSteuerdatenAbfrageStatus(@Nullable SteuerdatenAnfrageStatus steuerdatenAbfrageStatus) {
		this.steuerdatenAbfrageStatus = steuerdatenAbfrageStatus;
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
	public LocalDateTime getTimestampSteuerdatenResponse() {
		return timestampSteuerdatenResponse;
	}

	public void setTimestampSteuerdatenResponse(@Nullable LocalDateTime timestampSteuerdatenResponse) {
		this.timestampSteuerdatenResponse = timestampSteuerdatenResponse;
	}

	@Nullable
	public Veranlagungsstand getSteuerdatenVeranlagungsstand() {
		return steuerdatenVeranlagungsstand;
	}

	public void setSteuerdatenVeranlagungsstand(@Nullable Veranlagungsstand steuerdatenVeranlagungsstand) {
		this.steuerdatenVeranlagungsstand = steuerdatenVeranlagungsstand;
	}
}
