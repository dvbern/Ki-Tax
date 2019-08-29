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
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.ModulTagesschuleName;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;

/**
 * DTO fuer Module fuer die Tagesschulen
 */
@XmlRootElement(name = "modulTagesschule")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxModulTagesschule extends JaxAbstractDTO {

	private static final long serialVersionUID = -1893537808325618626L;

	@NotNull @Nonnull
	private String gesuchsperiodeId;

	@NotNull @Nonnull
	private DayOfWeek wochentag;

	@NotNull @Nonnull
	private ModulTagesschuleName modulTagesschuleName;

	@NotNull @Nonnull
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime zeitVon;

	@NotNull @Nonnull
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime zeitBis;

	@Nonnull
	public String getGesuchsperiodeId() {
		return gesuchsperiodeId;
	}

	public void setGesuchsperiodeId(@Nonnull String gesuchsperiodeId) {
		this.gesuchsperiodeId = gesuchsperiodeId;
	}

	@Nonnull
	public DayOfWeek getWochentag() {
		return wochentag;
	}

	public void setWochentag(@Nonnull DayOfWeek wochentag) {
		this.wochentag = wochentag;
	}

	@Nonnull
	public ModulTagesschuleName getModulTagesschuleName() {
		return modulTagesschuleName;
	}

	public void setModulTagesschuleName(@Nonnull ModulTagesschuleName modulTagesschuleName) {
		this.modulTagesschuleName = modulTagesschuleName;
	}

	@Nonnull
	public LocalDateTime getZeitVon() {
		return zeitVon;
	}

	public void setZeitVon(@Nonnull LocalDateTime zeitVon) {
		this.zeitVon = zeitVon;
	}

	@Nonnull
	public LocalDateTime getZeitBis() {
		return zeitBis;
	}

	public void setZeitBis(@Nonnull LocalDateTime zeitBis) {
		this.zeitBis = zeitBis;
	}
}
