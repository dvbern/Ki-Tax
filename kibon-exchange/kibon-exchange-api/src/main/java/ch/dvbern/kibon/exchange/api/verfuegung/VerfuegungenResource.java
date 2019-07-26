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

package ch.dvbern.kibon.exchange.api.verfuegung;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import ch.dvbern.kibon.exchange.api.verfuegung.model.ws.VerfuegungenDTO;

@Path("/verfuegungen")
public interface VerfuegungenResource {

	@GET
	@Nonnull
	VerfuegungenDTO getAll(
		@QueryParam("after_id") @Nullable Long afterId,
		@QueryParam("limit") @Nullable Integer limit,
		@QueryParam("$filter") @Nullable String filter
	);
}
