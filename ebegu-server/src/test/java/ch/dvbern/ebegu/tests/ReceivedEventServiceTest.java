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

package ch.dvbern.ebegu.tests;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.ReceivedEvent;
import ch.dvbern.ebegu.services.ReceivedEventService;
import ch.dvbern.ebegu.test.IntegrationTest;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
@Transactional(TransactionMode.DISABLED)
public class ReceivedEventServiceTest extends AbstractEbeguTest {

	@Inject
	private ReceivedEventService receivedEventService;

	@Test
	public void test() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		ReceivedEvent receivedEvent = new ReceivedEvent("e3736eb8-6eef-40ef-9e52-96ab48d8f220", "20.007305.002.1.3",
			"PlatzbestaetigungEvent",
			LocalDateTime.now(), betreuungEventDTO.toString());
		boolean success = receivedEventService.saveReceivedEvent(receivedEvent);
		Assert.assertTrue(success);
		boolean failure = receivedEventService.saveReceivedEvent(receivedEvent);
		Assert.assertFalse(failure);
	}

	/**
	 * Eine BetreuungEventDTO mit genau eine Zeitabschnitt
	 *
	 * @return
	 */
	private BetreuungEventDTO createBetreuungEventDTO() {
		BetreuungEventDTO betreuungEventDTO = new BetreuungEventDTO();
		betreuungEventDTO.setRefnr("20.007305.002.1.3");
		betreuungEventDTO.setGemeindeBfsNr(99999L);
		betreuungEventDTO.setGemeindeName("Paris");
		betreuungEventDTO.setInstitutionId("1234-5678-9101-1121");
		betreuungEventDTO.setAusserordentlicherBetreuungsaufwand(false);
		ZeitabschnittDTO zeitabschnittDTO = ZeitabschnittDTO.newBuilder()
			.setBetreuungskosten(new BigDecimal(1000))
			.setBetreuungspensum(new BigDecimal(16))
			.setAnzahlHauptmahlzeiten(new BigDecimal(10))
			.setAnzahlNebenmahlzeiten(new BigDecimal(10))
			.setPensumUnit(Zeiteinheit.DAYS)
			.setVon(LocalDate.of(2021, 01, 07))
			.setBis(LocalDate.of(2021, 06, 10))
			.build();
		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTO));
		return betreuungEventDTO;
	}
}
