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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Superklasse fuer alle Institutionen (Institution=Kita/Tageseltern, Tagesschule oder Ferieninsel).
 */
@MappedSuperclass
@Audited
public abstract class AbstractInstitution extends AbstractMutableEntity implements HasMandant, Displayable {

	private static final long serialVersionUID = -9037857320548372570L;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	private String name;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_abstractinstitution_traegerschaft_id"))
	private Traegerschaft traegerschaft;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_abstractinstitution_mandant_id"))
	private Mandant mandant;


	protected AbstractInstitution() {
	}


	@Override
	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Nullable
	public Traegerschaft getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable Traegerschaft traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Override
	@NotNull
	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(@NotNull Mandant mandant) {
		this.mandant = mandant;
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
		if (!(other instanceof AbstractInstitution)) {
			return false;
		}
		final AbstractInstitution otherInstitution = (AbstractInstitution) other;
		return
			Objects.equals(getName(), otherInstitution.getName());
	}
}
