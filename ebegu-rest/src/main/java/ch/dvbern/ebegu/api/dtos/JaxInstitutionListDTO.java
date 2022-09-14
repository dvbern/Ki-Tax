/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;

/**
 * Created by imanol on 17.03.16.
 * DTO fuer InstitutionList
 */
@XmlRootElement(name = "institution")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxInstitutionListDTO extends JaxInstitution {

	private static final long serialVersionUID = -1393677898323418626L;


	@NotNull
	private BetreuungsangebotTyp betreuungsangebotTyp;

	@Nullable
	private JaxGemeinde gemeinde;

	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	@Nullable
	public JaxGemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable JaxGemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}
}
