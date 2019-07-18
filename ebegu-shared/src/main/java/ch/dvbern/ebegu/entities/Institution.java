/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von Institution in der Datenbank.
 */
@Audited
@Entity
public class Institution extends AbstractInstitution {

	private static final long serialVersionUID = -8706487439884760618L;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private InstitutionStatus status = InstitutionStatus.EINGELADEN;

	@Column(nullable = false)
	private boolean stammdatenCheckRequired = false;


	public Institution() {
	}

	@Nonnull
	public InstitutionStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull InstitutionStatus status) {
		this.status = status;
	}

	public boolean isStammdatenCheckRequired() {
		return stammdatenCheckRequired;
	}

	public void setStammdatenCheckRequired(boolean stammdatenCheckRequired) {
		this.stammdatenCheckRequired = stammdatenCheckRequired;
	}

	public boolean isUnknownInstitution() {
		return this.getName().equals(Constants.UNKNOWN_INSTITUTION_NAME);
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof Institution)) {
			return false;
		}
		final Institution otherInstitution = (Institution) other;
		return super.isSame(otherInstitution)
			&& getStatus() == otherInstitution.getStatus();
	}

}
