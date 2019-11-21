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

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import com.google.common.base.Objects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.avro.Schema;

/**
 * An OutboxEvent is used to persist an {@link ExportedEvent}, allowing that the event is persisted in the same
 * database transaction as the business entities.<br>
 *
 * The {@link OutboxEventKafkaProducer} will periodically check the OutboxEvent table and send the events to Kafka.
 */
@Entity
public class OutboxEvent extends AbstractEntity {

	private static final long serialVersionUID = 5098873376230031363L;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String aggregateType;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String aggregateId;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String type;

	@Nonnull
	@Lob
	@Column(nullable = false, updatable = false)
	private final @NotNull byte[] payload;

	@Nonnull
	@Lob
	@Convert(converter = SchemaConverter.class)
	@Column(nullable = false, updatable = false)
	private final @NotNull Schema avroSchema;

	/**
	 * just for JPA
	 */
	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "just for JPA")
	protected OutboxEvent() {
		this.aggregateType = "";
		this.aggregateId = "";
		this.type = "";
		this.payload = null;
		this.avroSchema = null;
	}

	public OutboxEvent(
		@Nonnull String aggregateType,
		@Nonnull String aggregateId,
		@Nonnull String type,
		@Nonnull byte[] jsonPayload,
		@Nonnull Schema avroSchema) {
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.type = type;
		this.payload = Arrays.copyOf(jsonPayload, jsonPayload.length);
		this.avroSchema = avroSchema;
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
			Objects.equal(getAvroSchema(), that.getAvroSchema());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
			super.hashCode(),
			getAggregateType(),
			getAggregateId(),
			getType(),
			getPayload(),
			getAvroSchema());
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
		return Arrays.copyOf(payload, payload.length);
	}

	@Nonnull
	public Schema getAvroSchema() {
		return avroSchema;
	}
}
