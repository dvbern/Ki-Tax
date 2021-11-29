/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.ModulTagesschuleIntervall;
import ch.dvbern.ebegu.enums.ModulTagesschuleName;

/**
 * DTO fuer Module fuer die Tagesschulen
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxModulTagesschuleGroup extends JaxAbstractDTO {

	private static final long serialVersionUID = -1893537808325618626L;

	@NotNull @Nonnull
	private ModulTagesschuleName modulTagesschuleName;

	@NotNull @Nonnull
	private String identifier;

	@Nullable
	private String fremdId;

	@NotNull @Nonnull
	private JaxTextRessource bezeichnung = new JaxTextRessource();

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

	private int reihenfolge;

	private Set<JaxModulTagesschule> module = new LinkedHashSet<>();

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

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Nonnull
	public JaxTextRessource getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(@Nonnull JaxTextRessource bezeichnung) {
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

	public int getReihenfolge() {
		return reihenfolge;
	}

	public void setReihenfolge(int reihenfolge) {
		this.reihenfolge = reihenfolge;
	}

	public Set<JaxModulTagesschule> getModule() {
		return module;
	}

	public void setModule(Set<JaxModulTagesschule> module) {
		this.module = module;
	}

	@Nullable
	public String getFremdId() {
		return fremdId;
	}

	public void setFremdId(@Nullable String fremdId) {
		this.fremdId = fremdId;
	}
}
