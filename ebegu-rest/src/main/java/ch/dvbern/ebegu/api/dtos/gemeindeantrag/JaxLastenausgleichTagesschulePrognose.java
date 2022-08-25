/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JaxLastenausgleichTagesschulePrognose implements Serializable {


	private static final long serialVersionUID = -1194090533065145523L;

	@Nonnull
	private BigDecimal prognose;

	@Nullable
	private String bemerkungen;

	@Nonnull
	public BigDecimal getPrognose() {
		return prognose;
	}

	public void setPrognose(@Nonnull BigDecimal prognose) {
		this.prognose = prognose;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}
}
