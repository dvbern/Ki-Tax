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
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.Constants;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.hibernate.envers.Audited;

@MappedSuperclass
@Audited
public abstract class AbstractMutableEntity extends AbstractEntity {

	private static final long serialVersionUID = -979322154020183445L;

	@Nullable
	@Column(nullable = true, length = Constants.UUID_LENGTH)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	private String vorgaengerId;

	public AbstractMutableEntity() {}

	@Nullable
	public String getVorgaengerId() {
		return vorgaengerId;
	}

	public void setVorgaengerId(@Nullable String vorgaengerId) {
		this.vorgaengerId = vorgaengerId;
	}

	public boolean hasVorgaenger() {
		return vorgaengerId != null;
	}

	@Nonnull
	@CanIgnoreReturnValue
	public AbstractEntity copyAbstractEntity(@Nonnull AbstractMutableEntity target, @Nonnull AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
			target.setVorgaengerId(this.getId());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			target.setVorgaengerId(null);
			break;
		}
		return target;
	}
}
