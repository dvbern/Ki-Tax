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
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import org.hibernate.envers.Audited;

/**
 * Abstrakte Entitaet. Muss von Entitaeten erweitert werden, die ein Pensum (Prozent) und ein DateRange beeinhalten.
 */
@MappedSuperclass
@Audited
public class AbstractPensumEntity extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -7576083148864149528L;

	@Max(100)
	@Min(0)
	@NotNull
	@Column(nullable = false)
	private Long pensum;

	public AbstractPensumEntity() {
	}

	@Nonnull
	public Long getPensum() {
		return pensum;
	}

	public void setPensum(@Nonnull Long pensum) {
		this.pensum = pensum;
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
		if (!(other instanceof AbstractPensumEntity)) {
			return false;
		}
		final AbstractPensumEntity otherAbstDateRangedEntity = (AbstractPensumEntity) other;
		return super.isSame(otherAbstDateRangedEntity)
			&& Objects.equals(this.getPensum(), otherAbstDateRangedEntity.getPensum());
	}

	@Nonnull
	public AbstractPensumEntity copyAbstractPensumEntity(@Nonnull AbstractPensumEntity target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractDateRangedEntity(target, copyType);
		target.setPensum(this.getPensum());
		return target;
	}
}
