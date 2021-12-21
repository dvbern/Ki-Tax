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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

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
}
