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

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;

/**
 * DTO fuer Lastenausgleich Status Histroy
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxLastenausgleichTagesschulenStatusHistory extends JaxAbstractDTO {

	private static final long serialVersionUID = -1297026889674146397L;

	@Nonnull
	private String containerId;

	@Nonnull
	private JaxBenutzer benutzer;

	@Nonnull
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime timestampVon;

	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime timestampBis;

	@Nonnull
	private LastenausgleichTagesschuleAngabenGemeindeStatus status;

	@Nonnull
	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(@Nonnull String containerId) {
		this.containerId = containerId;
	}

	@Nonnull
	public JaxBenutzer getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(@Nonnull JaxBenutzer benutzer) {
		this.benutzer = benutzer;
	}

	@Nonnull
	public LocalDateTime getTimestampVon() {
		return timestampVon;
	}

	public void setTimestampVon(@Nonnull LocalDateTime timestampVon) {
		this.timestampVon = timestampVon;
	}

	public LocalDateTime getTimestampBis() {
		return timestampBis;
	}

	public void setTimestampBis(LocalDateTime timestampBis) {
		this.timestampBis = timestampBis;
	}

	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull LastenausgleichTagesschuleAngabenGemeindeStatus status) {
		this.status = status;
	}
}
