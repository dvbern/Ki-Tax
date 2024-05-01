/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.tests.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import au.com.origin.snapshots.jackson.serializers.v1.DeterministicJacksonSnapshotSerializer;
import ch.dvbern.ebegu.entities.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HibernateSnapshotSerializer extends DeterministicJacksonSnapshotSerializer {

	@Override
	public void configure(ObjectMapper objectMapper) {
		super.configure(objectMapper);
		objectMapper.setSerializationInclusion(Include.ALWAYS);

		// Ignore Hibernate Lists to prevent infinite recursion
		objectMapper.addMixIn(List.class, IgnoreTypeMixin.class);
		objectMapper.addMixIn(Set.class, IgnoreTypeMixin.class);

		// Ignore Fields of the AbstractEntity, which we don't want to snapshot
		objectMapper.addMixIn(AbstractEntity.class, IgnoreHibernateEntityFields.class);
	}

	@SuppressWarnings("EmptyClass")
	@JsonIgnoreType
	static class IgnoreTypeMixin {
	}

	@SuppressWarnings({ "AbstractClassNeverImplemented", "unused" })
	abstract static class IgnoreHibernateEntityFields {
		@JsonIgnore
		abstract Long getId();

		@JsonIgnore
		abstract long getVersion();

		@JsonIgnore
		abstract LocalDateTime getTimestampErstellt();

		@JsonIgnore
		abstract LocalDateTime getTimestampMutiert();

		@JsonIgnore
		abstract String getUserErstellt();

		@JsonIgnore
		abstract String getUserMutiert();

		@JsonIgnore
		abstract boolean isSkipPreUpdate();

		@JsonIgnore
		abstract boolean isNew();
	}
}
