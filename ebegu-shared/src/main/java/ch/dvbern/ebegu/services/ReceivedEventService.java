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

package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import ch.dvbern.ebegu.entities.ReceivedEvent;

public interface ReceivedEventService {

	/**
	 * @return TRUE, when there is a successfully processed ReceivedEvent with same eventId.
	 */
	boolean isSuccessfullyProcessed(@Nonnull String eventId);

	/**
	 * @return TRUE, whenthere exists a ReceivedEvent with same EventType, EventKey and more recent EventTimestamp
	 */
	boolean isObsolete(@Nonnull ReceivedEvent receivedEvent);

	/**
	 * Persists a sucessfully processed event
	 */
	void processingSuccess(@Nonnull ReceivedEvent receivedEvent);

	/**
	 * Persists a failed event
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	void processingFailure(@Nonnull ReceivedEvent receivedEvent, @Nonnull Throwable e);

}
