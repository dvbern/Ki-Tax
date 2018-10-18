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

package ch.dvbern.ebegu.dto.dataexport.v1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.lib.date.converters.LocalDateXMLConverter;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * This is a DTO that is used to export the relevant Information about a {@link ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ZeitabschnittExportDTO {

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate von;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate bis;

	//betreuungspensum.
	private BigDecimal effektiveBetreuungPct;

	//Anspruch
	private int anspruchPct;

	//BG Pensum
	private BigDecimal verguenstigtPct;

	private BigDecimal vollkosten;

	private BigDecimal verguenstigung;

	public ZeitabschnittExportDTO(
		LocalDate von,
		LocalDate bis,
		BigDecimal effektiveBetr,
		int anspruchPct,
		BigDecimal vergPct,
		BigDecimal vollkosten,
		BigDecimal verguenstigung
	) {
		this.von = von;
		this.bis = bis;
		this.effektiveBetreuungPct = effektiveBetr;
		this.anspruchPct = anspruchPct;
		this.verguenstigtPct = vergPct;
		this.vollkosten = vollkosten;
		this.verguenstigung = verguenstigung;

	}

	public ZeitabschnittExportDTO() {
	}

	public LocalDate getVon() {
		return von;
	}

	public void setVon(LocalDate von) {
		this.von = von;
	}

	public LocalDate getBis() {
		return bis;
	}

	public void setBis(LocalDate bis) {
		this.bis = bis;
	}

	public BigDecimal getEffektiveBetreuungPct() {
		return effektiveBetreuungPct;
	}

	public void setEffektiveBetreuungPct(BigDecimal effektiveBetreuungPct) {
		this.effektiveBetreuungPct = effektiveBetreuungPct;
	}

	public int getAnspruchPct() {
		return anspruchPct;
	}

	public void setAnspruchPct(int anspruchPct) {
		this.anspruchPct = anspruchPct;
	}

	public BigDecimal getVerguenstigtPct() {
		return verguenstigtPct;
	}

	public void setVerguenstigtPct(BigDecimal verguenstigtPct) {
		this.verguenstigtPct = verguenstigtPct;
	}

	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
	}

	public BigDecimal getVerguenstigung() {
		return verguenstigung;
	}

	public void setVerguenstigung(BigDecimal verguenstigung) {
		this.verguenstigung = verguenstigung;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ZeitabschnittExportDTO that = (ZeitabschnittExportDTO) o;
		return Objects.equals(getEffektiveBetreuungPct(), that.getEffektiveBetreuungPct()) &&
			getAnspruchPct() == that.getAnspruchPct() &&
			Objects.equals(getVerguenstigtPct(), that.getVerguenstigtPct()) &&
			Objects.equals(getVon(), that.getVon()) &&
			Objects.equals(getBis(), that.getBis()) &&
			Objects.equals(getVollkosten(), that.getVollkosten()) &&
			Objects.equals(getVerguenstigung(), that.getVerguenstigung());
	}

	@Override
	public int hashCode() {
		return Objects.hash(von, bis, effektiveBetreuungPct, anspruchPct, verguenstigtPct, vollkosten, verguenstigung);
	}
}
