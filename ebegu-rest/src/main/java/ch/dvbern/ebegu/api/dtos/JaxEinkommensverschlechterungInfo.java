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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer Familiensituationen
 */
@XmlRootElement(name = "einkommensverschlechterunginfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxEinkommensverschlechterungInfo extends JaxAbstractDTO {

	private static final long serialVersionUID = 735198205986579755L;

	@NotNull
	private Boolean einkommensverschlechterung = Boolean.FALSE;

	@NotNull
	private Boolean ekvFuerBasisJahrPlus1;

	@NotNull
	private Boolean ekvFuerBasisJahrPlus2;

	@Nullable
	@SuppressWarnings("checkstyle:MemberName")
	private Boolean gemeinsameSteuererklaerung_BjP1;

	@Nullable
	@SuppressWarnings("checkstyle:MemberName")
	private Boolean gemeinsameSteuererklaerung_BjP2;

	@NotNull
	private Boolean ekvBasisJahrPlus1Annulliert;

	@NotNull
	private Boolean ekvBasisJahrPlus2Annulliert;

	public Boolean getEinkommensverschlechterung() {
		return einkommensverschlechterung;
	}

	public void setEinkommensverschlechterung(final Boolean einkommensverschlechterung) {
		this.einkommensverschlechterung = einkommensverschlechterung;
	}

	public Boolean getEkvFuerBasisJahrPlus1() {
		return ekvFuerBasisJahrPlus1;
	}

	public void setEkvFuerBasisJahrPlus1(final Boolean ekvFuerBasisJahrPlus1) {
		this.ekvFuerBasisJahrPlus1 = ekvFuerBasisJahrPlus1;
	}

	public Boolean getEkvFuerBasisJahrPlus2() {
		return ekvFuerBasisJahrPlus2;
	}

	public void setEkvFuerBasisJahrPlus2(final Boolean ekvFuerBasisJahrPlus2) {
		this.ekvFuerBasisJahrPlus2 = ekvFuerBasisJahrPlus2;
	}

	@Nullable
	@SuppressWarnings("checkstyle:MethodName")
	public Boolean getGemeinsameSteuererklaerung_BjP1() {
		return gemeinsameSteuererklaerung_BjP1;
	}

	@SuppressWarnings({"checkstyle:MethodName", "checkstyle:ParameterName"})
	public void setGemeinsameSteuererklaerung_BjP1(@Nullable Boolean gemeinsameSteuererklaerung_BjP1) {
		this.gemeinsameSteuererklaerung_BjP1 = gemeinsameSteuererklaerung_BjP1;
	}

	@Nullable
	@SuppressWarnings({"checkstyle:MethodName", "checkstyle:ParameterName"})
	public Boolean getGemeinsameSteuererklaerung_BjP2() {
		return gemeinsameSteuererklaerung_BjP2;
	}

	@SuppressWarnings({"checkstyle:MethodName", "checkstyle:ParameterName"})
	public void setGemeinsameSteuererklaerung_BjP2(@Nullable Boolean gemeinsameSteuererklaerung_BjP2) {
		this.gemeinsameSteuererklaerung_BjP2 = gemeinsameSteuererklaerung_BjP2;
	}

	public Boolean getEkvBasisJahrPlus1Annulliert() {
		return ekvBasisJahrPlus1Annulliert;
	}

	public void setEkvBasisJahrPlus1Annulliert(Boolean ekvBasisJahrPlus1Annulliert) {
		this.ekvBasisJahrPlus1Annulliert = ekvBasisJahrPlus1Annulliert;
	}

	public Boolean getEkvBasisJahrPlus2Annulliert() {
		return ekvBasisJahrPlus2Annulliert;
	}

	public void setEkvBasisJahrPlus2Annulliert(Boolean ekvBasisJahrPlus2Annulliert) {
		this.ekvBasisJahrPlus2Annulliert = ekvBasisJahrPlus2Annulliert;
	}
}
