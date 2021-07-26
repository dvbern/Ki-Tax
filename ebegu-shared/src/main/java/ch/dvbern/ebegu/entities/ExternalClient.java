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

package ch.dvbern.ebegu.entities;

import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.ExternalClientInstitutionType;
import ch.dvbern.ebegu.enums.ExternalClientType;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

/**
 * Used to list available 3rd party clients.
 */
@Audited
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "clientName", "type" }, name = "UK_external_client"))
public class ExternalClient extends AbstractEntity {

	private static final long serialVersionUID = 7465912998960188302L;

	/**
	 * Wenn ExternalClientType ist EXCHANGE_SERVICE_USER dann es ist einen Keycloack Benutzer
	 * Wenn ExternalClientType ist anders dann es ist einen Schnittstelle Name
	 */
	@Nonnull
	@Column(nullable = false)
	private @NotEmpty String clientName;

	@Nonnull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private @NotNull ExternalClientType type;

	@Nonnull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private @NotNull ExternalClientInstitutionType institutionType;

	public ExternalClient() {
		this.clientName = "";
		this.type = ExternalClientType.EXCHANGE_SERVICE_USER;
	}

	public ExternalClient(@Nonnull String clientName, @Nonnull ExternalClientType type) {
		this.clientName = clientName;
		this.type = type;
	}

	@Override
	public boolean isSame(@Nullable AbstractEntity other) {
		return this.equals(other);
	}

	@Override
	@Nonnull
	public String toString() {
		return new StringJoiner(", ", ExternalClient.class.getSimpleName() + '[', "]")
			.add("clientId='" + clientName + '\'')
			.add("type=" + type)
			.toString();
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

	@Nonnull
	public ExternalClientInstitutionType getInstitutionType() {
		return institutionType;
	}

	public void setInstitutionType(@Nonnull ExternalClientInstitutionType institutionType) {
		this.institutionType = institutionType;
	}
}
