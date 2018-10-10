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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.PensumUnits;
import org.hibernate.envers.Audited;

/**
 * Abstrakte Entitaet. Muss von Entitaeten erweitert werden, die ein Pensum (Prozent), ein DateRange und ein
 * PensumUnits beeinhalten.
 */
@MappedSuperclass
@Audited
public class AbstractBetreuungspensumEntity extends AbstractPensumEntity {

	private static final long serialVersionUID = -7136083144964149528L;

	/**
	 * This parameter is used in the client to know in which units the amount must be displayed.
	 * In the database the amount will always be % so it must be task of the client to translate the value
	 * in the DB into the value needed by the user.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	@Nonnull
	private PensumUnits unitForDisplay = PensumUnits.PERCENTAGE;

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof AbstractBetreuungspensumEntity)) {
			return false;
		}
		final AbstractBetreuungspensumEntity otherAbstDateRangedEntity = (AbstractBetreuungspensumEntity) other;
		return super.isSame(otherAbstDateRangedEntity)
			&& this.getUnitForDisplay() == otherAbstDateRangedEntity.getUnitForDisplay();
	}

	public void copyAbstractBetreuungspensumEntity(
		@Nonnull AbstractBetreuungspensumEntity target,
		@Nonnull AntragCopyType copyType) {

		super.copyAbstractPensumEntity(target, copyType);
		target.setUnitForDisplay(this.getUnitForDisplay());
	}

	@Nonnull
	public PensumUnits getUnitForDisplay() {
		return unitForDisplay;
	}

	public void setUnitForDisplay(@Nonnull PensumUnits unitForDisplay) {
		this.unitForDisplay = unitForDisplay;
	}
}
