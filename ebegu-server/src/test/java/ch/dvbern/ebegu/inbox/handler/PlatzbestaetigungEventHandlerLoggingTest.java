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

package ch.dvbern.ebegu.inbox.handler;

import java.time.LocalDateTime;

import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import org.easymock.Capture;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.newCapture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
public class PlatzbestaetigungEventHandlerLoggingTest extends EasyMockSupport {

	private static final String REF_NR = "1.2.3.4";
	private static final String CLIENT_NAME = "client";
	private static final String KEY = "fake";

	@TestSubject
	private final PlatzbestaetigungEventHandler handler = partialMockBuilder(PlatzbestaetigungEventHandler.class)
		.addMockedMethod("attemptProcessing")
		.strictMock();

	@Mock
	private BetreuungMonitoringService betreuungMonitoringService;

	@Mock
	private Logger logger;

	@AfterEach
	void tearDown() {
		verifyAll();
	}

	@Test
	void shouldLogAndPresistFailedEvents() {
		Processing result = Processing.failure("test");
		expect(handler.attemptProcessing(anyObject(), anyObject()))
			.andReturn(result);

		BetreuungEventDTO dto = mock(BetreuungEventDTO.class);
		expect(dto.getRefnr()).andReturn(REF_NR).anyTimes();

		logger.warn("Platzbestaetigung Event für Betreuung mit RefNr: {} nicht verarbeitet: {}", REF_NR, result);

		Capture<BetreuungMonitoring> captured = newCapture();
		//noinspection DataFlowIssue
		expect(betreuungMonitoringService.saveBetreuungMonitoring(capture(captured)))
			.andReturn(mock(BetreuungMonitoring.class));

		replayAll();

		handler.processEvent(LocalDateTime.now(), EventType.PLATZBESTAETIGUNG_BETREUUNG, KEY, dto, CLIENT_NAME);

		assertThat(
			captured.getValue().getInfoText(),
			is("Eine Platzbestaetigung Event wurde nicht verarbeitet: " + result)
		);
	}

	@Test
	void shouldLogAndPresistIgnoredEvents() {
		Processing result = Processing.ignore("test");
		expect(handler.attemptProcessing(anyObject(), anyObject()))
			.andReturn(result);

		BetreuungEventDTO dto = mock(BetreuungEventDTO.class);
		expect(dto.getRefnr()).andReturn(REF_NR).anyTimes();

		logger.info(
			"Platzbestaetigung Event für Betreuung mit RefNr: {} wurde ignoriert und nicht verarbeitet: {}",
			REF_NR,
			result);

		Capture<BetreuungMonitoring> captured = newCapture();
		//noinspection DataFlowIssue
		expect(betreuungMonitoringService.saveBetreuungMonitoring(capture(captured)))
			.andReturn(mock(BetreuungMonitoring.class));

		replayAll();

		handler.processEvent(LocalDateTime.now(), EventType.PLATZBESTAETIGUNG_BETREUUNG, KEY, dto, CLIENT_NAME);

		assertThat(
			captured.getValue().getInfoText(),
			is("Eine Platzbestaetigung Event wurde ignoriert: " + result)
		);
	}

	@SuppressWarnings("TestMethodWithoutAssertion")
	@Test
	void shouldLogSuccessEvents() {
		Processing result = Processing.success();
		expect(handler.attemptProcessing(anyObject(), anyObject()))
			.andReturn(result);

		BetreuungEventDTO dto = mock(BetreuungEventDTO.class);
		expect(dto.getRefnr()).andReturn(REF_NR).anyTimes();

		logger.debug("Platzbestaetigung Event für Betreuung mit RefNr: {} erfolgreich verarbeitet: {}", REF_NR, result);


		replayAll();

		handler.processEvent(LocalDateTime.now(), EventType.PLATZBESTAETIGUNG_BETREUUNG, KEY, dto, CLIENT_NAME);
	}
}
