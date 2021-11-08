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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.util.Constants;

/**
 * DTO fuer Verfuegung Zeitabschnitt Bemerkungen. Gehoert immer zu einem VerfügungZeitabschnitt
 */
@XmlRootElement(name = "verfuegungZeitabschnittBemerkung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxVerfuegungZeitabschnittBemerkung extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = 5412864903064978250L;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@NotNull
	private String bemerkung;

	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(String bemerkung) {
		this.bemerkung = bemerkung;
	}
}
