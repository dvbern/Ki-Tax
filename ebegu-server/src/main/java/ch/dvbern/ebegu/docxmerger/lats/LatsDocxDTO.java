/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.docxmerger.lats;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

public class LatsDocxDTO {
	@Nonnull private String gemeindeName;
	@Nonnull private BigDecimal geleisteteBetreuungsstunden;

	public LatsDocxDTO(@Nonnull String gemeindeName, @Nonnull BigDecimal geleisteteBetreuungsstunden) {
		this.gemeindeName = gemeindeName;
		this.geleisteteBetreuungsstunden = geleisteteBetreuungsstunden;
	}

	@Nonnull
	public String getGemeindeName() {
		return gemeindeName;
	}

	public void setGemeindeName(@Nonnull String gemeindeName) {
		this.gemeindeName = gemeindeName;
	}

	@Nonnull
	public BigDecimal getGeleisteteBetreuungsstunden() {
		return geleisteteBetreuungsstunden;
	}

	public void setGeleisteteBetreuungsstunden(@Nonnull BigDecimal geleisteteBetreuungsstunden) {
		this.geleisteteBetreuungsstunden = geleisteteBetreuungsstunden;
	}
}
