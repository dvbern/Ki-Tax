/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxFinanzielleSituationAufteilungDTO implements Serializable {

	private static final long serialVersionUID = -3837866698539834925L;

	@Nullable
	private BigDecimal bruttoertraegeVermoegenGS1;
	@Nullable
	private BigDecimal abzugSchuldzinsenGS1;
	@Nullable
	private BigDecimal gewinnungskostenGS1;
	@Nullable
	private BigDecimal geleisteteAlimenteGS1;
	@Nullable
	private BigDecimal nettovermoegenGS1;
	@Nullable
	private BigDecimal nettoertraegeErbengemeinschaftGS1;
	@Nullable
	private BigDecimal bruttoertraegeVermoegenGS2;
	@Nullable
	private BigDecimal abzugSchuldzinsenGS2;
	@Nullable
	private BigDecimal gewinnungskostenGS2;
	@Nullable
	private BigDecimal geleisteteAlimenteGS2;
	@Nullable
	private BigDecimal nettovermoegenGS2;
	@Nullable
	private BigDecimal nettoertraegeErbengemeinschaftGS2;

	@Nullable
	public BigDecimal getBruttoertraegeVermoegenGS1() {
		return bruttoertraegeVermoegenGS1;
	}

	public void setBruttoertraegeVermoegenGS1(@Nullable BigDecimal bruttoertraegeVermoegenGS1) {
		this.bruttoertraegeVermoegenGS1 = bruttoertraegeVermoegenGS1;
	}

	@Nullable
	public BigDecimal getAbzugSchuldzinsenGS1() {
		return abzugSchuldzinsenGS1;
	}

	public void setAbzugSchuldzinsenGS1(@Nullable BigDecimal abzugSchuldzinsenGS1) {
		this.abzugSchuldzinsenGS1 = abzugSchuldzinsenGS1;
	}

	@Nullable
	public BigDecimal getGewinnungskostenGS1() {
		return gewinnungskostenGS1;
	}

	public void setGewinnungskostenGS1(@Nullable BigDecimal gewinnungskostenGS1) {
		this.gewinnungskostenGS1 = gewinnungskostenGS1;
	}

	@Nullable
	public BigDecimal getGeleisteteAlimenteGS1() {
		return geleisteteAlimenteGS1;
	}

	public void setGeleisteteAlimenteGS1(@Nullable BigDecimal geleisteteAlimenteGS1) {
		this.geleisteteAlimenteGS1 = geleisteteAlimenteGS1;
	}

	@Nullable
	public BigDecimal getNettovermoegenGS1() {
		return nettovermoegenGS1;
	}

	public void setNettovermoegenGS1(@Nullable BigDecimal nettovermoegenGS1) {
		this.nettovermoegenGS1 = nettovermoegenGS1;
	}

	@Nullable
	public BigDecimal getNettoertraegeErbengemeinschaftGS1() {
		return nettoertraegeErbengemeinschaftGS1;
	}

	public void setNettoertraegeErbengemeinschaftGS1(@Nullable BigDecimal nettoertraegeErbengemeinschaftGS1) {
		this.nettoertraegeErbengemeinschaftGS1 = nettoertraegeErbengemeinschaftGS1;
	}

	@Nullable
	public BigDecimal getBruttoertraegeVermoegenGS2() {
		return bruttoertraegeVermoegenGS2;
	}

	public void setBruttoertraegeVermoegenGS2(@Nullable BigDecimal bruttoertraegeVermoegenGS2) {
		this.bruttoertraegeVermoegenGS2 = bruttoertraegeVermoegenGS2;
	}

	@Nullable
	public BigDecimal getAbzugSchuldzinsenGS2() {
		return abzugSchuldzinsenGS2;
	}

	public void setAbzugSchuldzinsenGS2(@Nullable BigDecimal abzugSchuldzinsenGS2) {
		this.abzugSchuldzinsenGS2 = abzugSchuldzinsenGS2;
	}

	@Nullable
	public BigDecimal getGewinnungskostenGS2() {
		return gewinnungskostenGS2;
	}

	public void setGewinnungskostenGS2(@Nullable BigDecimal gewinnungskostenGS2) {
		this.gewinnungskostenGS2 = gewinnungskostenGS2;
	}

	@Nullable
	public BigDecimal getGeleisteteAlimenteGS2() {
		return geleisteteAlimenteGS2;
	}

	public void setGeleisteteAlimenteGS2(@Nullable BigDecimal geleisteteAlimenteGS2) {
		this.geleisteteAlimenteGS2 = geleisteteAlimenteGS2;
	}

	@Nullable
	public BigDecimal getNettovermoegenGS2() {
		return nettovermoegenGS2;
	}

	public void setNettovermoegenGS2(@Nullable BigDecimal nettovermoegenGS2) {
		this.nettovermoegenGS2 = nettovermoegenGS2;
	}

	@Nullable
	public BigDecimal getNettoertraegeErbengemeinschaftGS2() {
		return nettoertraegeErbengemeinschaftGS2;
	}

	public void setNettoertraegeErbengemeinschaftGS2(@Nullable BigDecimal nettoertraegeErbengemeinschaftGS2) {
		this.nettoertraegeErbengemeinschaftGS2 = nettoertraegeErbengemeinschaftGS2;
	}
}
