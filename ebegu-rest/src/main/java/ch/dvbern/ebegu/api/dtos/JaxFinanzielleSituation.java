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

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

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
	private Boolean steuerdatenZugriff;

	@Nullable
	private BigDecimal geschaeftsgewinnBasisjahrMinus2;

	@Nullable
	private BigDecimal geschaeftsgewinnBasisjahrMinus1;

	@Nullable
	private BigDecimal bruttoertraegeVermoegen;

	@Nullable
	private BigDecimal nettoertraegeErbengemeinschaft;

	@Nullable
	private BigDecimal nettoVermoegen;

	@Nullable
	private Boolean einkommenInVereinfachtemVerfahrenAbgerechnet;

	@Nullable
	private BigDecimal amountEinkommenInVereinfachtemVerfahrenAbgerechnet;

	@Nullable
	private BigDecimal gewinnungskosten;

	@Nullable
	private BigDecimal abzugSchuldzinsen;

	@Nullable
	private JaxFinanzielleSituationSelbstdeklaration selbstdeklaration;

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
	public JaxFinanzielleSituationSelbstdeklaration getSelbstdeklaration() {
		return selbstdeklaration;
	}

	public void setSelbstdeklaration(@Nullable JaxFinanzielleSituationSelbstdeklaration selbstdeklaration) {
		this.selbstdeklaration = selbstdeklaration;
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

	@Nullable
	public BigDecimal getAbzugSchuldzinsen() {
		return abzugSchuldzinsen;
	}

	public void setAbzugSchuldzinsen(@Nullable BigDecimal abzugSchuldzinsen) {
		this.abzugSchuldzinsen = abzugSchuldzinsen;
	}
}
