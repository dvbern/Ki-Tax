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

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.validation.constraints.NotEmpty;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static ch.dvbern.ebegu.util.Constants.TEN_MB;

@Entity
public class ReceivedEvent extends AbstractEntity {

	private static final long serialVersionUID = 4998440001747583997L;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String eventId;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String eventKey;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String eventType;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final LocalDateTime eventTimestamp;

	@Nonnull
	@Column(nullable = false, length = TEN_MB) // 10 megabytes
	@Lob
	@Basic(fetch = FetchType.LAZY)
	private final String eventDTO;

	/**
	 * just for JPA
	 */
	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "just for JPA")
	protected ReceivedEvent() {
		this.eventId = "";
		this.eventKey = "";
		this.eventType = "";
		this.eventTimestamp = null;
		this.eventDTO = null;
	}

	public ReceivedEvent(@Nonnull @NotEmpty String eventId, @Nonnull @NotEmpty String eventKey,
		@Nonnull @NotEmpty String eventType,
		@Nonnull LocalDateTime eventTimestamp, @Nonnull String eventDTO) {
		this.eventId = eventId;
		this.eventKey = eventKey;
		this.eventType = eventType;
		this.eventTimestamp = eventTimestamp;
		this.eventDTO = eventDTO;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return this.equals(other);
	}


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

		ReceivedEvent that = (ReceivedEvent) o;

		return Objects.equal(getEventDTO(), that.getEventDTO()) &&
			Objects.equal(getEventId(), that.getEventId()) &&
			Objects.equal(getEventType(), that.getEventType()) &&
			Objects.equal(getEventTimestamp(), that.getEventTimestamp()) &&
			Objects.equal(getEventKey(), that.getEventKey());
	}

	@Nonnull
	public String getEventId() {
		return eventId;
	}

	@Nonnull
	public String getEventKey() {
		return eventKey;
	}

	@Nonnull
	public String getEventType() {
		return eventType;
	}

	@Nonnull
	public LocalDateTime getEventTimestamp() {
		return eventTimestamp;
	}

	@Nonnull
	public String getEventDTO() {
		return eventDTO;
	}
}
