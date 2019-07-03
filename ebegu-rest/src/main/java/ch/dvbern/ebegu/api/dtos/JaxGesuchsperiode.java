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

import java.time.LocalDate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.lib.date.converters.LocalDateXMLConverter;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;

/**
 * DTO fuer Gesuchsperiode
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxGesuchsperiode extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = -2495737706808699744L;

	@NotNull
	private GesuchsperiodeStatus status;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate datumFreischaltungTagesschule;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate datumErsterSchultag;


	public GesuchsperiodeStatus getStatus() {
		return status;
	}

	public void setStatus(GesuchsperiodeStatus status) {
		this.status = status;
	}

	@Nullable
	public LocalDate getDatumFreischaltungTagesschule() {
		return datumFreischaltungTagesschule;
	}

	public void setDatumFreischaltungTagesschule(@Nullable LocalDate datumFreischaltungTagesschule) {
		this.datumFreischaltungTagesschule = datumFreischaltungTagesschule;
	}

	@Nullable
	public LocalDate getDatumErsterSchultag() {
		return datumErsterSchultag;
	}

	public void setDatumErsterSchultag(@Nullable LocalDate datumErsterSchultag) {
		this.datumErsterSchultag = datumErsterSchultag;
	}
}
