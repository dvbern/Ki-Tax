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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos.finanziellesituation;

import java.math.BigDecimal;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

/**
 * DTO fuer Finanzielle Situation
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxAbstractFinanzielleSituation extends JaxAbstractDTO {

	private static final long serialVersionUID = -4629044440787545634L;


	@Nullable
	private BigDecimal nettolohn;

	private BigDecimal familienzulage;

	private BigDecimal ersatzeinkommen;

	private BigDecimal erhalteneAlimente;

	private BigDecimal bruttovermoegen;

	private BigDecimal schulden;

	private BigDecimal geschaeftsgewinnBasisjahr;

	private BigDecimal geleisteteAlimente;

	private BigDecimal steuerbaresEinkommen;

	private BigDecimal steuerbaresVermoegen;

	private BigDecimal abzuegeLiegenschaft;

	private BigDecimal geschaeftsverlust;

	private BigDecimal einkaeufeVorsorge;

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

	@Nullable
	private JaxFinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell;

	@Nullable
	public BigDecimal getNettolohn() {
		return nettolohn;
	}

	public void setNettolohn(@Nullable final BigDecimal nettolohn) {
		this.nettolohn = nettolohn;
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

	public BigDecimal getSteuerbaresEinkommen() {
		return steuerbaresEinkommen;
	}

	public void setSteuerbaresEinkommen(BigDecimal steuerbaresEinkommen) {
		this.steuerbaresEinkommen = steuerbaresEinkommen;
	}

	public BigDecimal getSteuerbaresVermoegen() {
		return steuerbaresVermoegen;
	}

	public void setSteuerbaresVermoegen(BigDecimal steuerbaresVermoegen) {
		this.steuerbaresVermoegen = steuerbaresVermoegen;
	}

	public BigDecimal getAbzuegeLiegenschaft() {
		return abzuegeLiegenschaft;
	}

	public void setAbzuegeLiegenschaft(BigDecimal abzuegeLiegenschaft) {
		this.abzuegeLiegenschaft = abzuegeLiegenschaft;
	}

	public BigDecimal getGeschaeftsverlust() {
		return geschaeftsverlust;
	}

	public void setGeschaeftsverlust(BigDecimal geschaeftsverlust) {
		this.geschaeftsverlust = geschaeftsverlust;
	}

	public BigDecimal getEinkaeufeVorsorge() {
		return einkaeufeVorsorge;
	}

	public void setEinkaeufeVorsorge(BigDecimal einkaeufeVorsorge) {
		this.einkaeufeVorsorge = einkaeufeVorsorge;
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

	@Nullable
	public JaxFinanzielleSituationSelbstdeklaration getSelbstdeklaration() {
		return selbstdeklaration;
	}

	public void setSelbstdeklaration(@Nullable JaxFinanzielleSituationSelbstdeklaration selbstdeklaration) {
		this.selbstdeklaration = selbstdeklaration;
	}

	@Nullable
	public JaxFinSitZusatzangabenAppenzell getFinSitZusatzangabenAppenzell() {
		return finSitZusatzangabenAppenzell;
	}

	public void setFinSitZusatzangabenAppenzell(@Nullable JaxFinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell) {
		this.finSitZusatzangabenAppenzell = finSitZusatzangabenAppenzell;
	}
}
