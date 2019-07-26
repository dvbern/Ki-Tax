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

package ch.dvbern.ebegu.outbox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import com.google.common.base.Objects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
public class OutboxEvent extends AbstractEntity {

	private static final long serialVersionUID = 5098873376230031363L;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotNull String aggregateType;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotNull String aggregateId;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotNull String type;

	@Nonnull
	@Lob
	@Column(nullable = false, updatable = false)
	private final @NotNull byte[] payload;

	/**
	 * @deprecated just for JPA
	 */
	@Deprecated
	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "just for JPA")
	public OutboxEvent() {
		this.aggregateType = null;
		this.aggregateId = null;
		this.type = null;
		this.payload = null;
	}

	public OutboxEvent(
		@Nonnull String aggregateType,
		@Nonnull String aggregateId,
		@Nonnull String type,
		@Nonnull byte[] jsonPayload) {
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.type = type;
		this.payload = jsonPayload;
	}

	@Override
	public boolean isSame(@Nullable AbstractEntity other) {
		return this.equals(other);
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		if (!super.equals(o)) {
			return false;
		}

		OutboxEvent that = (OutboxEvent) o;

		return Objects.equal(getAggregateType(), that.getAggregateType()) &&
			Objects.equal(getAggregateId(), that.getAggregateId()) &&
			Objects.equal(getType(), that.getType()) &&
			Objects.equal(getPayload(), that.getPayload());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), getAggregateType(), getAggregateId(), getType(), getPayload());
	}

	@Nonnull
	public String getAggregateType() {
		return aggregateType;
	}

	@Nonnull
	public String getAggregateId() {
		return aggregateId;
	}

	@Nonnull
	public String getType() {
		return type;
	}

	@Nonnull
	public byte[] getPayload() {
		return payload;
	}
}
