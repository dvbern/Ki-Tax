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

import java.time.DayOfWeek;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer Module fuer die Tagesschulen
 */
@XmlRootElement(name = "modulTagesschule")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxModulTagesschule extends JaxAbstractDTO {

	private static final long serialVersionUID = -1893537808325618626L;

	@NotNull @Nonnull
	private DayOfWeek wochentag;


	@Nonnull
	public DayOfWeek getWochentag() {
		return wochentag;
	}

	public void setWochentag(@Nonnull DayOfWeek wochentag) {
		this.wochentag = wochentag;
	}
}
