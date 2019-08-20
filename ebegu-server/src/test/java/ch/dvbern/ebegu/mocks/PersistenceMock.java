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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.persistence.PersistenceService;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PersistenceMock extends PersistenceService {

	private static final long serialVersionUID = 8904148825617296264L;

	private static final Map<Class, Collection> ELEMENTS = new HashMap<>();

	@Override
	public <T> T persist(T entity) {
		if (entity instanceof AbstractEntity) {
			((AbstractEntity)entity).setTimestampErstellt(LocalDateTime.now());
			((AbstractEntity)entity).setUserErstellt("PersistenceMock");
		}
		if (!ELEMENTS.containsKey(entity.getClass())) {
			ELEMENTS.put(entity.getClass(), new ArrayList<>());
		}
		ELEMENTS.get(entity.getClass()).add(entity);
		return entity;
	}

	@Override
	public <T> T merge(T entity) {
		if (entity instanceof AbstractEntity) {
			((AbstractEntity)entity).setTimestampMutiert(LocalDateTime.now());
			((AbstractEntity)entity).setUserMutiert("PersistenceMock");
		}
		if (ELEMENTS.containsKey(entity.getClass())) {
			ELEMENTS.get(entity.getClass()).remove(entity);
			ELEMENTS.get(entity.getClass()).add(entity);
		} else {
			persist(entity);
		}
		return entity;
	}

	@Override
	public <T> void remove(T entity) {
		if (ELEMENTS.containsKey(entity.getClass())) {
			ELEMENTS.get(entity.getClass()).remove(entity);
		}
	}

	@Nullable
	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		Collection<T> collection = getElementsOfClass(entityClass);
		for (T o : collection) {
			if (o instanceof AbstractEntity) {
				if (primaryKey.equals(((AbstractEntity)o).getId())) {
					return o;
				}
			}
		}
		return null;
	}

	@Nonnull
	public <T> Collection<T> getElementsOfClass(Class<T> c) {
		Collection collection = ELEMENTS.get(c);
		if (collection != null) {
			return collection;
		}
		return Collections.EMPTY_LIST;
	}
}
