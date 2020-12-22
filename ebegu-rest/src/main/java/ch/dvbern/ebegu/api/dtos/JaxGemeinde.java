/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;
import org.apache.commons.lang3.builder.CompareToBuilder;

import static ch.dvbern.ebegu.util.Constants.END_OF_TIME;

/**
 * DTO fuer Gemeinden
 */
@XmlRootElement(name = "gemeinde")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxGemeinde extends JaxAbstractDTO {

	private static final long serialVersionUID = 7980499854206395920L;

	@NotNull
	private String name;

	@Nonnull
	private long gemeindeNummer;

	@Nonnull
	private Long bfsNummer;

	@Nonnull
	private GemeindeStatus status;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate betreuungsgutscheineStartdatum;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate tagesschulanmeldungenStartdatum;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate ferieninselanmeldungenStartdatum;

	@Nonnull
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate gueltigBis = END_OF_TIME;

	private boolean angebotBG = false;
	private boolean angebotTS = false;
	private boolean angebotFI = false;

	// Dieses Feld wird *nur* für die Komponente gemeinde-multiselect.component verwendet
	// Wir haben dort das Problem, dass in gewissen Einzelfällen die Id der Gemeinde (noch) nicht bekannt ist,
	// da diese in kiBon noch nicht registriert ist: Beim Onboarding, Gemeindeauswahl für Tagessschulen, wenn ich
	// eine Gemeinde explizit auswähle, die einem Verbund angehört und selber nicht in kiBon mitmacht.
	@Nonnull
	private String key;


	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	public long getGemeindeNummer() {
		return gemeindeNummer;
	}

	public void setGemeindeNummer(long gemeindeNummer) {
		this.gemeindeNummer = gemeindeNummer;
	}

	@Nonnull
	public GemeindeStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull GemeindeStatus status) {
		this.status = status;
	}

	@Nonnull
	public Long getBfsNummer() {
		return bfsNummer;
	}

	public void setBfsNummer(@Nonnull Long bfsNummer) {
		this.bfsNummer = bfsNummer;
	}

	@Nullable
	public LocalDate getBetreuungsgutscheineStartdatum() {
		return betreuungsgutscheineStartdatum;
	}

	public void setBetreuungsgutscheineStartdatum(@Nonnull LocalDate betreuungsgutscheineStartdatum) {
		this.betreuungsgutscheineStartdatum = betreuungsgutscheineStartdatum;
	}

	public boolean isAngebotBG() {
		return angebotBG;
	}

	public void setAngebotBG(boolean angebotBG) {
		this.angebotBG = angebotBG;
	}

	public boolean isAngebotTS() {
		return angebotTS;
	}

	public void setAngebotTS(boolean angebotTS) {
		this.angebotTS = angebotTS;
	}

	public boolean isAngebotFI() {
		return angebotFI;
	}

	public void setAngebotFI(boolean angebotFI) {
		this.angebotFI = angebotFI;
	}

	@Nonnull
	public LocalDate getGueltigBis() { return gueltigBis; }

	public void setGueltigBis(@Nonnull LocalDate gueltigBis) { this.gueltigBis = gueltigBis; }

	@Nonnull
	public String getKey() {
		return key;
	}

	public void setKey(@Nonnull String key) {
		this.key = key;
	}

	@Override
	public int compareTo(@Nonnull JaxAbstractDTO o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getId(), o.getId());
		if (o instanceof JaxGemeinde) {
			JaxGemeinde parsedEntity = (JaxGemeinde) o;
			builder.append(this.getName(), parsedEntity.getName());
			builder.append(this.getGemeindeNummer(), parsedEntity.getGemeindeNummer());
			builder.append(this.getStatus(), parsedEntity.getStatus());
			builder.append(this.getBetreuungsgutscheineStartdatum(), parsedEntity.getBetreuungsgutscheineStartdatum());
			return builder.toComparison();
		}
		return builder.toComparison();
	}

	@Nullable
	public LocalDate getTagesschulanmeldungenStartdatum() {
		return tagesschulanmeldungenStartdatum;
	}

	public void setTagesschulanmeldungenStartdatum(@Nullable LocalDate tagesschulanmeldungenStartdatum) {
		this.tagesschulanmeldungenStartdatum = tagesschulanmeldungenStartdatum;
	}

	@Nullable
	public LocalDate getFerieninselanmeldungenStartdatum() {
		return ferieninselanmeldungenStartdatum;
	}

	public void setFerieninselanmeldungenStartdatum(@Nullable LocalDate ferieninselanmeldungenStartdatum) {
		this.ferieninselanmeldungenStartdatum = ferieninselanmeldungenStartdatum;
	}
}
