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

package ch.dvbern.ebegu.enums;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Displayable;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;

public enum RollenAbhaengigkeit {
	GEMEINDE(Gemeinde.class),
	INSTITUTION(Institution.class),
	TRAEGERSCHAFT(Traegerschaft.class),
	KANTON(Mandant.class),
	SOZIALDIENST(Sozialdienst.class),
	NONE(null);

	@Nullable
	private final Class<? extends Displayable> associatedEntityClass;

	RollenAbhaengigkeit(@Nullable Class<? extends Displayable> associatedEntityClass) {
		this.associatedEntityClass = associatedEntityClass;
	}

	@Nonnull
	public <T extends Displayable> Optional<Class<T>> getAssociatedEntityClass() {
		//noinspection unchecked
		return Optional.ofNullable((Class<T>) this.associatedEntityClass);
	}
}
