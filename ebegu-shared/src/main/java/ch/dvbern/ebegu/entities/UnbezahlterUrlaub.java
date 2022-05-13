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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Entity;

import ch.dvbern.ebegu.enums.AntragCopyType;
import org.hibernate.envers.Audited;

/**
 * Entity fuer einen unbezahlten Urlaub
 */
@Audited
@Entity
public class UnbezahlterUrlaub extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -5808034280025121320L;

	public UnbezahlterUrlaub() {
	}

	@Nonnull
	public UnbezahlterUrlaub copyUnbezahlterUrlaub(@Nonnull UnbezahlterUrlaub target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractDateRangedEntity(target, copyType);
		return target;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		//noinspection SimplifiableIfStatement
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		final UnbezahlterUrlaub otherUrlaub = (UnbezahlterUrlaub) other;
		return Objects.equals(this.getGueltigkeit(), otherUrlaub.getGueltigkeit());
	}
}
