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

package ch.dvbern.ebegu.inbox.handler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PlatzbestaetigungEventHandlerTest {

	private PlatzbestaetigungEventHandler handler = new PlatzbestaetigungEventHandler();
	private Gesuch gesuch_1GS;

	@Before
	public void setUp() {
		Gesuchsperiode gesuchsperiode1718 = TestDataUtil.createGesuchsperiode1718();
		Gemeinde bern = TestDataUtil.createGemeindeParis();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall01_WaeltiDagmar testfall_1GS =
			new Testfall01_WaeltiDagmar(gesuchsperiode1718, institutionStammdatenList);
		testfall_1GS.createFall();
		testfall_1GS.createGesuch(LocalDate.of(2016, Month.DECEMBER, 12));
		gesuch_1GS = testfall_1GS.fillInGesuch();
	}

	@Test
	public void testIsSame() {
		List<Betreuung> betreuungs = gesuch_1GS.extractAllBetreuungen();
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		//first with tarif = null
		Assert.assertTrue(handler.isSame(betreuungEventDTO, betreuungs.get(0)));
		//then with tarif the same
		betreuungEventDTO.getZeitabschnitte().get(0).setTarifProHauptmahlzeiten(BigDecimal.ZERO);
		betreuungEventDTO.getZeitabschnitte().get(0).setTarifProNebenmahlzeiten(BigDecimal.ZERO);
		Assert.assertTrue(handler.isSame(betreuungEventDTO, betreuungs.get(0)));
		//then with different Betreuung
		Assert.assertFalse(handler.isSame(betreuungEventDTO, betreuungs.get(1)));
	}

	@Test
	public void testMapZeitabschnittBetreuungMitteilungPensum() {
		List<Betreuung> betreuungs = gesuch_1GS.extractAllBetreuungen();
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		ZeitabschnittDTO zeitabschnittDTO = betreuungEventDTO.getZeitabschnitte().get(0);
		zeitabschnittDTO.setTarifProHauptmahlzeiten(BigDecimal.ZERO);
		zeitabschnittDTO.setTarifProNebenmahlzeiten(BigDecimal.ZERO);
		BetreuungsmitteilungPensum betreuungsmitteilungPensum =
			handler.mapZeitabschnitt(new BetreuungsmitteilungPensum(), zeitabschnittDTO, betreuungs.get(0));
		Assert.assertNotNull(betreuungsmitteilungPensum);
		Assert.assertEquals(0, betreuungsmitteilungPensum.getMonatlicheBetreuungskosten()
			.compareTo(zeitabschnittDTO.getBetreuungskosten()));
		Assert.assertEquals(0, betreuungsmitteilungPensum.getPensum()
			.compareTo(zeitabschnittDTO.getBetreuungspensum()));
		Assert.assertEquals(0, betreuungsmitteilungPensum.getMonatlicheHauptmahlzeiten()
			.compareTo(zeitabschnittDTO.getAnzahlHauptmahlzeiten()));
		Assert.assertEquals(0, betreuungsmitteilungPensum.getMonatlicheNebenmahlzeiten()
			.compareTo(zeitabschnittDTO.getAnzahlNebenmahlzeiten()));
		Assert.assertEquals(0, betreuungsmitteilungPensum.getTarifProHauptmahlzeit()
			.compareTo(zeitabschnittDTO.getTarifProHauptmahlzeiten()));
		Assert.assertEquals(0, betreuungsmitteilungPensum.getTarifProNebenmahlzeit()
			.compareTo(zeitabschnittDTO.getTarifProNebenmahlzeiten()));
		Assert.assertTrue(betreuungsmitteilungPensum.getGueltigkeit()
			.getGueltigAb()
			.isEqual(zeitabschnittDTO.getVon()));
		Assert.assertTrue(betreuungsmitteilungPensum.getGueltigkeit()
			.getGueltigBis()
			.isEqual(zeitabschnittDTO.getBis()));
	}

	@Test
	public void testMapZeitabschnittBetreuungPensum() {
		List<Betreuung> betreuungs = gesuch_1GS.extractAllBetreuungen();
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		ZeitabschnittDTO zeitabschnittDTO = betreuungEventDTO.getZeitabschnitte().get(0);
		zeitabschnittDTO.setTarifProHauptmahlzeiten(BigDecimal.ZERO);
		zeitabschnittDTO.setTarifProNebenmahlzeiten(BigDecimal.ZERO);
		Betreuungspensum betreuungsPensum =
			handler.mapZeitabschnitt(new Betreuungspensum(), zeitabschnittDTO, betreuungs.get(0));
		Assert.assertNotNull(betreuungsPensum);
		Assert.assertEquals(0, betreuungsPensum.getMonatlicheBetreuungskosten()
			.compareTo(zeitabschnittDTO.getBetreuungskosten()));
		Assert.assertEquals(0, betreuungsPensum.getPensum()
			.compareTo(zeitabschnittDTO.getBetreuungspensum()));
		Assert.assertEquals(0, betreuungsPensum.getMonatlicheHauptmahlzeiten()
			.compareTo(zeitabschnittDTO.getAnzahlHauptmahlzeiten()));
		Assert.assertEquals(0, betreuungsPensum.getMonatlicheNebenmahlzeiten()
			.compareTo(zeitabschnittDTO.getAnzahlNebenmahlzeiten()));
		Assert.assertEquals(0, betreuungsPensum.getTarifProHauptmahlzeit()
			.compareTo(zeitabschnittDTO.getTarifProHauptmahlzeiten()));
		Assert.assertEquals(0, betreuungsPensum.getTarifProNebenmahlzeit()
			.compareTo(zeitabschnittDTO.getTarifProNebenmahlzeiten()));
		Assert.assertTrue(betreuungsPensum.getGueltigkeit().getGueltigAb().isEqual(zeitabschnittDTO.getVon()));
		Assert.assertTrue(betreuungsPensum.getGueltigkeit().getGueltigBis().isEqual(zeitabschnittDTO.getBis()));
	}

	@Test
	public void testMapWrongZeitabschnitt() {
		List<Betreuung> betreuungs = gesuch_1GS.extractAllBetreuungen();
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		ZeitabschnittDTO zeitabschnittDTO = betreuungEventDTO.getZeitabschnitte().get(0);
		zeitabschnittDTO.setPensumUnit(Zeiteinheit.HOURS);
		BetreuungsmitteilungPensum betreuungsmitteilungPensum =
			handler.mapZeitabschnitt(new BetreuungsmitteilungPensum()
				, zeitabschnittDTO, betreuungs.get(0));
		Assert.assertNull(betreuungsmitteilungPensum);
	}

	@Test
	public void testMapZeitAbschnitteToImport() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		ZeitabschnittDTO zeitabschnittDTO = createZeitabschnittDTO();
		zeitabschnittDTO.setVon(LocalDate.of(2020, 11, 1));
		zeitabschnittDTO.setBis(LocalDate.of(2021, 6, 30));
		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2021, 5, 31)))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTO));

		List<ZeitabschnittDTO> zeitabschnitteToImport = handler.mapZeitabschnitteToImport(
			betreuungEventDTO,
			Objects.requireNonNull(betreuung));

		Assert.assertEquals(LocalDate.of(2020, 8, 1), zeitabschnitteToImport.get(0).getVon());
		Assert.assertEquals(LocalDate.of(2020, 12, 31), zeitabschnitteToImport.get(0).getBis());

		Assert.assertEquals(LocalDate.of(2021, 1, 1), zeitabschnitteToImport.get(1).getVon());
		Assert.assertEquals(LocalDate.of(2021, 6, 30), zeitabschnitteToImport.get(1).getBis());
	}

	/**
	 * Eine BetreuungEventDTO mit genau eine Zeitabschnitt
	 */
	private BetreuungEventDTO createBetreuungEventDTO() {
		BetreuungEventDTO betreuungEventDTO = new BetreuungEventDTO();
		betreuungEventDTO.setRefnr("20.007305.002.1.3");
		betreuungEventDTO.setGemeindeBfsNr(99999L);
		betreuungEventDTO.setGemeindeName("Testgemeinde");
		betreuungEventDTO.setInstitutionId("1234-5678-9101-1121");
		betreuungEventDTO.setAusserordentlicherBetreuungsaufwand(false);
		ZeitabschnittDTO zeitabschnittDTO = createZeitabschnittDTO();
		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTO));
		return betreuungEventDTO;
	}

	private ZeitabschnittDTO createZeitabschnittDTO() {
		return ZeitabschnittDTO.newBuilder()
			.setBetreuungskosten(new BigDecimal(2000.00).setScale(2))
			.setBetreuungspensum(new BigDecimal(80))
			.setAnzahlHauptmahlzeiten(BigDecimal.ZERO)
			.setAnzahlNebenmahlzeiten(BigDecimal.ZERO)
			.setPensumUnit(Zeiteinheit.PERCENTAGE)
			.setVon(LocalDate.of(2017, 8, 01))
			.setBis(LocalDate.of(2018, 1, 31))
			.build();
	}
}
