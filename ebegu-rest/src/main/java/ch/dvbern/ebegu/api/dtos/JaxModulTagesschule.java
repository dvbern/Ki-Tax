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

import java.math.BigDecimal;
import java.time.DayOfWeek;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.ModulTagesschuleIntervall;
import ch.dvbern.ebegu.enums.ModulTagesschuleName;

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
	private String bezeichnung;

	@NotNull @Nonnull
	private String zeitVon;

	@NotNull @Nonnull
	private String zeitBis;

	@Nullable
	private BigDecimal verpflegungskosten;

	@NotNull @Nonnull
	private ModulTagesschuleIntervall intervall;

	@NotNull @Nonnull
	private boolean wirdPaedagogischBetreut;


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
	public String getZeitVon() {
		return zeitVon;
	}

	public void setZeitVon(@Nonnull String von) {
		this.zeitVon = von;
	}

	@Nonnull
	public String getZeitBis() {
		return zeitBis;
	}

	public void setZeitBis(@Nonnull String bis) {
		this.zeitBis = bis;
	}

	@Nonnull
	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(@Nonnull String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	@Nullable
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nullable BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nonnull
	public ModulTagesschuleIntervall getIntervall() {
		return intervall;
	}

	public void setIntervall(@Nonnull ModulTagesschuleIntervall intervall) {
		this.intervall = intervall;
	}

	public boolean isWirdPaedagogischBetreut() {
		return wirdPaedagogischBetreut;
	}

	public void setWirdPaedagogischBetreut(boolean wirdPaedagogischBetreut) {
		this.wirdPaedagogischBetreut = wirdPaedagogischBetreut;
	}
}
