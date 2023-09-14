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

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

/**
 * DTO fuer Finanzielle Situation
 */
@XmlRootElement(name = "finanzielleSituation")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxFinanzielleSituationContainer extends JaxAbstractDTO {

	private static final long serialVersionUID = -4812537186224986782L;

	@NotNull
	private Integer jahr;

	@Valid
	private JaxFinanzielleSituation finanzielleSituationGS;

	@Valid
	private JaxFinanzielleSituation finanzielleSituationJA;

	public Integer getJahr() {
		return jahr;
	}

	public void setJahr(Integer jahr) {
		this.jahr = jahr;
	}

	@Nullable
	public JaxFinanzielleSituation getFinanzielleSituationGS() {
		return finanzielleSituationGS;
	}

	public void setFinanzielleSituationGS(@Nullable JaxFinanzielleSituation finanzielleSituationGS) {
		this.finanzielleSituationGS = finanzielleSituationGS;
	}

	@Nullable
	public JaxFinanzielleSituation getFinanzielleSituationJA() {
		return finanzielleSituationJA;
	}

	public void setFinanzielleSituationJA(@Nullable JaxFinanzielleSituation finanzielleSituationJA) {
		this.finanzielleSituationJA = finanzielleSituationJA;
	}
}
