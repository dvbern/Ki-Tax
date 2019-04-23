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

package ch.dvbern.ebegu.mocks;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.metamodel.Attribute;

import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;

public class CriteriaQueryHelperMock extends CriteriaQueryHelper {

	@Nonnull
	@Override
	public <A, E> Collection<E> getEntitiesByAttribute(@Nonnull Class<E> entityClass, @Nullable A attributeValue, @Nonnull Attribute<E, A> attribute) {
		return Collections.EMPTY_LIST;
	}
}
