/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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

	private long gemeindeNummer;

	private boolean enabled;


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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public int compareTo(@Nonnull JaxAbstractDTO o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getId(), o.getId());
		if (o instanceof JaxGemeinde) {
			JaxGemeinde parsedEntity = (JaxGemeinde) o;
			builder.append(this.getName(), parsedEntity.getName());
			builder.append(this.getGemeindeNummer(), parsedEntity.getGemeindeNummer());
			builder.append(this.isEnabled(), parsedEntity.isEnabled());
			return builder.toComparison();
		}

		return builder.toComparison();
	}
}
