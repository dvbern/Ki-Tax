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

package ch.dvbern.ebegu.dto.suchfilter.smarttable;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Mandant;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Klasse zur internen Verwendung des gesamten SmartTable-Filterobjekts mit Mandant
 */
public class BenutzerTableMandantFilterDTO extends BenutzerTableFilterDTO {

	private static final long serialVersionUID = 404959569485575365L;

	@Nonnull
	private Mandant mandant;

	public BenutzerTableMandantFilterDTO(@Nonnull Mandant mandant) {
		super();
		this.mandant = mandant;
	}

	public BenutzerTableMandantFilterDTO(BenutzerTableFilterDTO benutzerSearch, @Nonnull Mandant mandant) {
		super();
		this.setPagination(benutzerSearch.getPagination());
		this.setSearch(benutzerSearch.getSearch());
		this.setSort(benutzerSearch.getSort());
		this.mandant = mandant;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("pagination", getPagination())
			.append("search", getSearch())
			.append("sort", getSort())
			.append("mandant", getMandant())
			.toString();
	}
	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(@Nonnull Mandant mandant) {
		this.mandant = mandant;
	}
}
