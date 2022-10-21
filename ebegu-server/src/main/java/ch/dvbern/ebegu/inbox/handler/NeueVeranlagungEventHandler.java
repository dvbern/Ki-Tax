/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.inbox.handler;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class NeueVeranlagungEventHandler extends BaseEventHandler<String> {

	private static final Logger LOG = LoggerFactory.getLogger(NeueVeranlagungEventHandler.class);


	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String key,
		@Nonnull String dto,
		@Nonnull String clientName) {
		}
}
