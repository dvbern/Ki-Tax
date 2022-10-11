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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.AbholungTagesschule;
import ch.dvbern.ebegu.enums.reporting.FleischOption;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

/**
 * DTO fuer Daten der Belegungen.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBelegungTagesschule extends JaxAbstractDTO {

	private static final long serialVersionUID = -1297972380574937397L;

	@NotNull @Nonnull
	private Set<JaxBelegungTagesschuleModul> belegungTagesschuleModule = new LinkedHashSet<>();

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate eintrittsdatum;

	@NotNull @Nonnull
	private String planKlasse;

	@NotNull @Nonnull
	private FleischOption fleischOption;

	@NotNull @Nonnull
	private String allergienUndUnvertraeglichkeiten;

	@NotNull @Nonnull
	private String notfallnummer;

	@NotNull @Nonnull
	private AbholungTagesschule abholungTagesschule;

	@Nullable
	private String bemerkung;

	private boolean abweichungZweitesSemester = false;
	private boolean keineKesbPlatzierung = true;


	public Set<JaxBelegungTagesschuleModul> getBelegungTagesschuleModule() {
		return belegungTagesschuleModule;
	}

	public void setBelegungTagesschuleModule(Set<JaxBelegungTagesschuleModul> belegungTagesschuleModule) {
		this.belegungTagesschuleModule = belegungTagesschuleModule;
	}

	public LocalDate getEintrittsdatum() {
		return eintrittsdatum;
	}

	public void setEintrittsdatum(LocalDate eintrittsdatum) {
		this.eintrittsdatum = eintrittsdatum;
	}

	public String getPlanKlasse() {
		return planKlasse;
	}

	public void setPlanKlasse(String planKlasse) {
		this.planKlasse = planKlasse;
	}

	public String getAllergienUndUnvertraeglichkeiten(){
		return allergienUndUnvertraeglichkeiten;
	}

	public void setAllergienUndUnvertraeglichkeiten(String allergienUndUnvertraeglichkeiten) {
		this.allergienUndUnvertraeglichkeiten = allergienUndUnvertraeglichkeiten;
	}

	public AbholungTagesschule getAbholungTagesschule() {
		return abholungTagesschule;
	}

	public void setAbholungTagesschule(AbholungTagesschule abholungTagesschule) {
		this.abholungTagesschule = abholungTagesschule;
	}

	@Nullable
	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(@Nullable String bemerkung) {
		this.bemerkung = bemerkung;
	}

	public boolean isAbweichungZweitesSemester() {
		return abweichungZweitesSemester;
	}

	public void setAbweichungZweitesSemester(boolean abweichungZweitesSemester) {
		this.abweichungZweitesSemester = abweichungZweitesSemester;
	}

	public boolean isKeineKesbPlatzierung() {
		return keineKesbPlatzierung;
	}

	public void setKeineKesbPlatzierung(boolean keineKesbPlatzierung) {
		this.keineKesbPlatzierung = keineKesbPlatzierung;
	}

	@Nonnull
	public FleischOption getFleischOption() {
		return fleischOption;
	}

	public void setFleischOption(@Nonnull final FleischOption fleischOption) {
		this.fleischOption = fleischOption;
	}

	@Nonnull
	public String getNotfallnummer() {
		return notfallnummer;
	}

	public void setNotfallnummer(@Nonnull final String notfallnummer) {
		this.notfallnummer = notfallnummer;
	}
}
