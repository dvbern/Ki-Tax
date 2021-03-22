/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

public class JaxFerienbetreuungAngabenKostenEinnahmen extends JaxAbstractDTO {

	private static final long serialVersionUID = 6672466831391665338L;

	@Nullable
	private BigDecimal personalkosten;

	@Nullable
	private BigDecimal personalkostenLeitungAdmin;

	@Nullable
	private BigDecimal sachkosten;

	@Nullable
	private BigDecimal verpflegungskosten;

	@Nullable
	private BigDecimal weitereKosten;

	@Nullable
	private String bemerkungenKosten;

	@Nullable
	private BigDecimal elterngebuehren;

	@Nullable
	private BigDecimal weitereEinnahmen;

	@Nullable
	public BigDecimal getPersonalkosten() {
		return personalkosten;
	}

	public void setPersonalkosten(@Nullable BigDecimal personalkosten) {
		this.personalkosten = personalkosten;
	}

	@Nullable
	public BigDecimal getPersonalkostenLeitungAdmin() {
		return personalkostenLeitungAdmin;
	}

	public void setPersonalkostenLeitungAdmin(@Nullable BigDecimal personalkostenLeitungAdmin) {
		this.personalkostenLeitungAdmin = personalkostenLeitungAdmin;
	}

	@Nullable
	public BigDecimal getSachkosten() {
		return sachkosten;
	}

	public void setSachkosten(@Nullable BigDecimal sachkosten) {
		this.sachkosten = sachkosten;
	}

	@Nullable
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nullable BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nullable
	public BigDecimal getWeitereKosten() {
		return weitereKosten;
	}

	public void setWeitereKosten(@Nullable BigDecimal weitereKosten) {
		this.weitereKosten = weitereKosten;
	}

	@Nullable
	public String getBemerkungenKosten() {
		return bemerkungenKosten;
	}

	public void setBemerkungenKosten(@Nullable String bemerkungenKosten) {
		this.bemerkungenKosten = bemerkungenKosten;
	}

	@Nullable
	public BigDecimal getElterngebuehren() {
		return elterngebuehren;
	}

	public void setElterngebuehren(@Nullable BigDecimal elterngebuehren) {
		this.elterngebuehren = elterngebuehren;
	}

	@Nullable
	public BigDecimal getWeitereEinnahmen() {
		return weitereEinnahmen;
	}

	public void setWeitereEinnahmen(@Nullable BigDecimal weitereEinnahmen) {
		this.weitereEinnahmen = weitereEinnahmen;
	}
}
