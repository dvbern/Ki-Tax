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

package ch.dvbern.ebegu.types;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class InstitutionExternalClientId implements Serializable {

	@Column(name = "institution_id")
	private String institutionId;

	@Column(name = "external_client_id")
	private String externalClientId;

	private InstitutionExternalClientId() {}

	public InstitutionExternalClientId(
		String institutionId,
		String externalClientId) {
		this.institutionId = institutionId;
		this.externalClientId = externalClientId;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass())
			return false;

		InstitutionExternalClientId that = (InstitutionExternalClientId) o;
		return Objects.equals(institutionId, that.institutionId) &&
			Objects.equals(externalClientId, that.externalClientId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(institutionId, externalClientId);
	}
}
