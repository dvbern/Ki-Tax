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

package ch.dvbern.ebegu.kafka;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.ReceivedEvent;
import ch.dvbern.ebegu.services.ReceivedEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseEventHandler<T> {

	private static final Logger LOG = LoggerFactory.getLogger(BaseEventHandler.class);

	@Inject
	private ReceivedEventService receivedEventService;

	public void onEvent(
		@Nonnull String eventId,
		@Nonnull String key,
		@Nonnull LocalDateTime eventTime,
		@Nonnull String eventType,
		@Nonnull T dto,
		@Nonnull String clientName
	) {

		ReceivedEvent receivedEvent = new ReceivedEvent(eventId, key, eventType, eventTime, dto.toString());

		if (!receivedEventService.saveReceivedEvent(receivedEvent)) {
			LOG.info("Event with UUID '{}' and timestamp '{}' of type '{}' was already retrieved, ignoring it",
				eventId, eventTime, eventType);

			return;
		}

		LOG.info("Received '{}' event -- key: '{}', event type: '{}'",
			dto.getClass().getSimpleName(), key, eventType);

		EventType.of(eventType).ifPresentOrElse(
			type -> processEvent(eventTime, type, dto, clientName),
			() -> LOG.warn("Unknown event type '{}'", eventType)
		);
	}

	protected abstract void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull T dto,
		@Nonnull String clientName);
}
