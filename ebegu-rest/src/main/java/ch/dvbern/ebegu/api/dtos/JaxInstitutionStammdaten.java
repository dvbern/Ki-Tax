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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by imanol on 17.03.16.
 * DTO fuer InstitutionStammdaten
 */
@XmlRootElement(name = "institutionStammdaten")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxInstitutionStammdaten extends JaxAbstractInstitutionStammdaten {

	private static final long serialVersionUID = -1893677808323618626L;
	@Nullable
	private String administratoren;
	@Nullable
	private String sachbearbeiter;


	@Nullable
	public String getAdministratoren() {
		return administratoren;
	}

	public void setAdministratoren(@Nullable String administratoren) {
		this.administratoren = administratoren;
	}

	@Nullable
	public String getSachbearbeiter() {
		return sachbearbeiter;
	}

	public void setSachbearbeiter(@Nullable String sachbearbeiter) {
		this.sachbearbeiter = sachbearbeiter;
	}

}
