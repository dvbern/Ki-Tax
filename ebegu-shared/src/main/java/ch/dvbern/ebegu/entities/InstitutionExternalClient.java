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

package ch.dvbern.ebegu.entities;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.InstitutionExternalClientId;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(indexes = {
	@Index(name = "IX_institution_external_clients_institution_id", columnList = "institution_id"),
	@Index(name = "IX_institution_external_clients_external_client_id", columnList = "external_client_id"),
})
public class InstitutionExternalClient implements Serializable {

	private static final long serialVersionUID = 6067667517915309689L;

	@EmbeddedId
	private InstitutionExternalClientId id;

	@NotNull
	@Nonnull
	@Embedded
	private DateRange gueltigkeit = new DateRange();

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_external_clients_institution_id"), insertable = false, updatable = false)
	private Institution institution;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_external_clients_external_client_id"), insertable = false, updatable = false)
	private ExternalClient externalClient;

	@Nonnull
	public DateRange getGueltigkeit() {
		return gueltigkeit;
	}

	public void setGueltigkeit(@Nonnull DateRange gueltigkeit) {
		this.gueltigkeit = gueltigkeit;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public ExternalClient getExternalClient() {
		return externalClient;
	}

	public void setExternalClient(ExternalClient externalClient) {
		this.externalClient = externalClient;
	}

	public InstitutionExternalClientId getId() {
		return id;
	}

	public void setId(InstitutionExternalClientId id) {
		this.id = id;
	}
}
