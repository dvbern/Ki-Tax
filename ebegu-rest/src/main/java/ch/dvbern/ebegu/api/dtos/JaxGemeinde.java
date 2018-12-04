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
}
