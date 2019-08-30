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

import javax.annotation.Nonnull;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.ExternalClientType;

@XmlRootElement(name = "externalClient")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxExternalClient extends JaxAbstractDTO {

	private static final long serialVersionUID = 4045567623627216320L;

	@Nonnull
	private @NotEmpty String clientName;

	@Nonnull
	private @NotNull ExternalClientType type;

	public JaxExternalClient() {
		this.clientName = "";
		this.type = ExternalClientType.EXCHANGE_SERVICE_USER;
	}

	@Nonnull
	public String getClientName() {
		return clientName;
	}

	public void setClientName(@Nonnull String clientName) {
		this.clientName = clientName;
	}

	@Nonnull
	public ExternalClientType getType() {
		return type;
	}

	public void setType(@Nonnull ExternalClientType type) {
		this.type = type;
	}
}
