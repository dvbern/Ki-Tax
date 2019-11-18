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
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

/**
 * DTO fuer Mahnungen
 */
@XmlRootElement(name = "mahnung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxMahnung extends JaxAbstractDTO {

	private static final long serialVersionUID = -1217019901364130097L;

	@Nullable
	private JaxGesuch gesuch;

	@Nonnull
	private MahnungTyp mahnungTyp;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate datumFristablauf = null;

	@Nullable
	private String bemerkungen;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime timestampAbgeschlossen;

	@NotNull
	private Boolean abgelaufen = false;

	@Nullable
	public JaxGesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(@Nullable JaxGesuch gesuch) {
		this.gesuch = gesuch;
	}

	@Nonnull
	public MahnungTyp getMahnungTyp() {
		return mahnungTyp;
	}

	public void setMahnungTyp(@Nonnull MahnungTyp mahnungTyp) {
		this.mahnungTyp = mahnungTyp;
	}

	@Nullable
	public LocalDate getDatumFristablauf() {
		return datumFristablauf;
	}

	public void setDatumFristablauf(@Nullable LocalDate datumFristablauf) {
		this.datumFristablauf = datumFristablauf;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Nullable
	public LocalDateTime getTimestampAbgeschlossen() {
		return timestampAbgeschlossen;
	}

	public void setTimestampAbgeschlossen(@Nullable LocalDateTime timestampAbgeschlossen) {
		this.timestampAbgeschlossen = timestampAbgeschlossen;
	}

	public Boolean getAbgelaufen() {
		return abgelaufen;
	}

	public void setAbgelaufen(Boolean abgelaufen) {
		this.abgelaufen = abgelaufen;
	}
}
