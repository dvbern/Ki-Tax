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

package ch.dvbern.kibon.exchange.commons.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ObjectMapperUtil {

	public static final ObjectMapper MAPPER = new ObjectMapper()
		.registerModule(new JavaTimeModule())
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		// does not work in Jackson 2.9.9 see JacksonBigDecimalAsPlainTest
		//.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, false)
		;

	/**
	 * eventId: can be used by consumers to detect duplicates
	 */
	public static final String MESSAGE_HEADER_EVENT_ID = "eventId";
	/**
	 * eventType: the type of event, e.g. "Order Created" or "Order Line Canceled". Allows consumers to trigger
	 * suitable event handlers.
	 */
	public static final String MESSAGE_HEADER_EVENT_TYPE = "eventType";

	private ObjectMapperUtil() {
		// util
	}
}
