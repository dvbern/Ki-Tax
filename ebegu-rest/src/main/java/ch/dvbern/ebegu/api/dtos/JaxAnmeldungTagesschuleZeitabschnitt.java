/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

public class JaxAnmeldungTagesschuleZeitabschnitt extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = 3939072050781289383L;

	@NotNull
	private BigDecimal massgebendesEinkommenInklAbzugFamgr;

	@NotNull
	private BigDecimal verpflegungskosten;

	@NotNull
	private BigDecimal betreuungsstundenProWoche;

	@NotNull
	private BigDecimal betreuungsminutenProWoche;

	@NotNull
	private BigDecimal gebuehrProStunde;

	@NotNull
	private BigDecimal totalKostenProWoche;

	@NotNull
	private boolean pedagogischBetreut;

	public BigDecimal getMassgebendesEinkommenInklAbzugFamgr() {
		return massgebendesEinkommenInklAbzugFamgr;
	}

	public void setMassgebendesEinkommenInklAbzugFamgr(BigDecimal massgebendesEinkommenInklAbzugFamgr) {
		this.massgebendesEinkommenInklAbzugFamgr = massgebendesEinkommenInklAbzugFamgr;
	}

	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	public BigDecimal getBetreuungsstundenProWoche() {
		return betreuungsstundenProWoche;
	}

	public void setBetreuungsstundenProWoche(BigDecimal betreuungsstundenProWoche) {
		this.betreuungsstundenProWoche = betreuungsstundenProWoche;
	}

	public BigDecimal getBetreuungsminutenProWoche() {
		return betreuungsminutenProWoche;
	}

	public void setBetreuungsminutenProWoche(BigDecimal betreuungsminutenProWoche) {
		this.betreuungsminutenProWoche = betreuungsminutenProWoche;
	}

	public BigDecimal getGebuehrProStunde() {
		return gebuehrProStunde;
	}

	public void setGebuehrProStunde(BigDecimal gebuehrProStunde) {
		this.gebuehrProStunde = gebuehrProStunde;
	}

	public BigDecimal getTotalKostenProWoche() {
		return totalKostenProWoche;
	}

	public void setTotalKostenProWoche(BigDecimal totalKostenProWoche) {
		this.totalKostenProWoche = totalKostenProWoche;
	}

	public boolean isPedagogischBetreut() {
		return pedagogischBetreut;
	}

	public void setPedagogischBetreut(boolean pedagogischBetreut) {
		this.pedagogischBetreut = pedagogischBetreut;
	}
}
