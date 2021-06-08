/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Gueltigkeit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.Audited;

/**
 * Abstrakte Entitaet. Muss von Entitaeten erweitert werden, die eine Periode (DateRange) mit datumVon und datumBis haben.
 */
@MappedSuperclass
@Audited
public class AbstractDateRangedEntity extends AbstractMutableEntity implements Gueltigkeit {

	private static final long serialVersionUID = -7541083148864749528L;

	@NotNull
	@Nonnull
	@Embedded
	@Valid
	private DateRange gueltigkeit = new DateRange();

	public AbstractDateRangedEntity() {
	}

	@Override
	@Nonnull
	public DateRange getGueltigkeit() {
		return gueltigkeit;
	}

	@Override
	public void setGueltigkeit(@Nonnull DateRange gueltigkeit) {
		this.gueltigkeit = new DateRange(gueltigkeit);
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
		if (!(other instanceof AbstractDateRangedEntity)) {
			return false;
		}
		final AbstractDateRangedEntity otherAbstractDateRangedEntity = (AbstractDateRangedEntity) other;
		return Objects.equals(this.getGueltigkeit(), otherAbstractDateRangedEntity.getGueltigkeit());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("gueltigkeit", gueltigkeit)
			.toString();
	}

	@Nonnull
	public AbstractDateRangedEntity copyAbstractDateRangedEntity(@Nonnull AbstractDateRangedEntity target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		target.setGueltigkeit(new DateRange(this.getGueltigkeit()));
		return target;
	}
}
