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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Entity;

import ch.dvbern.ebegu.enums.AntragCopyType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

/**
 * Sozialhilfe Zeiträume des Sozialhilfe Bezüger
 */
@Entity
@Audited
public class SozialhilfeZeitraum extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -9132257320978372420L;

	public SozialhilfeZeitraum() {
	}

	@Nonnull
	public SozialhilfeZeitraum copySozialhilfeZeitraum(@Nonnull SozialhilfeZeitraum target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractDateRangedEntity(target, copyType);
		return target;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
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
		final SozialhilfeZeitraum otherSozialhilfeZeitraum = (SozialhilfeZeitraum) other;
		return Objects.equals(this.getGueltigkeit(), otherSozialhilfeZeitraum.getGueltigkeit());
	}
}
