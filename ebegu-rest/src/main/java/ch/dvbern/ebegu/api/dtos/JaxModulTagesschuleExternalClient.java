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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

public class JaxModulTagesschuleExternalClient extends JaxAbstractDTO {

	private static final long serialVersionUID = 7800625749130268039L;

	@Nonnull @NotNull
	private JaxExternalClient externalClient;

	@Nonnull @NotNull
	private String identifier;

	@Nonnull
	public JaxExternalClient getExternalClient() {
		return externalClient;
	}

	public void setExternalClient(@Nonnull JaxExternalClient externalClient) {
		this.externalClient = externalClient;
	}

	public @Nonnull String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(@Nonnull String identifier) {
		this.identifier = identifier;
	}
}
