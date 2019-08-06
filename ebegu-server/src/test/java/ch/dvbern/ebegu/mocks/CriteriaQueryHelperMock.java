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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import de.akquinet.jbosscc.needle.annotation.InjectIntoMany;

public class CriteriaQueryHelperMock extends CriteriaQueryHelper {

	private static final long serialVersionUID = 8699860745231110631L;

	@InjectIntoMany
	private final PersistenceMock persistence = new PersistenceMock();


	@Nonnull
	@Override
	public <A, E> Collection<E> getEntitiesByAttribute(@Nonnull Class<E> entityClass, @Nullable A attributeValue, @Nonnull Attribute<E, A> attribute) {
		// Wir geben der Einfachheit halber einfach alle dieses Typs zurueck
		return getAll(entityClass);
	}

	@Override
	public <T> Collection<T> getAll(Class<T> clazz) {
		//noinspection unchecked
		return persistence.getElementsOfClass(clazz);
	}

	@Nonnull
	@Override
	public <A, E extends AbstractEntity> Optional<E> getEntityByUniqueAttribute(@Nonnull Class<E> entityClazz, @Nullable A attributeValue, @Nonnull SingularAttribute<E, A> attribute) {
		// Wir geben der Einfachheit halber einfach das erste Element zurueck
		return getFirst(entityClazz);
	}

	private <E extends AbstractEntity> Optional<E> getFirst(@Nonnull Class<E> entityClazz) {
		return getAll(entityClazz).stream().findFirst();
	}
}
