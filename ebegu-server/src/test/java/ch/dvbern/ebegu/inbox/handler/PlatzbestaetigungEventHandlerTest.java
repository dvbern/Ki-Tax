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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlatzbestaetigungEventHandlerTest {

	private final PlatzbestaetigungEventHandler handler = new PlatzbestaetigungEventHandler();
	private Gesuch gesuch_1GS = null;

	@BeforeEach
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
		Betreuungsmitteilung betreuungsmitteilung = createBetreuungMitteilung();
		Assertions.assertTrue(handler.isSame(betreuungsmitteilung, betreuungs.get(0)));
		//then with different Betreuung
		Assertions.assertFalse(handler.isSame(betreuungsmitteilung, betreuungs.get(1)));
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
		Assertions.assertNotNull(betreuungsmitteilungPensum);
		Assertions.assertEquals(0, betreuungsmitteilungPensum.getMonatlicheBetreuungskosten()
			.compareTo(zeitabschnittDTO.getBetreuungskosten()));
		Assertions.assertEquals(0, betreuungsmitteilungPensum.getPensum()
			.compareTo(zeitabschnittDTO.getBetreuungspensum()));
		Assertions.assertEquals(0, betreuungsmitteilungPensum.getMonatlicheHauptmahlzeiten()
			.compareTo(zeitabschnittDTO.getAnzahlHauptmahlzeiten()));
		Assertions.assertEquals(0, betreuungsmitteilungPensum.getMonatlicheNebenmahlzeiten()
			.compareTo(zeitabschnittDTO.getAnzahlNebenmahlzeiten()));
		Assertions.assertEquals(0, betreuungsmitteilungPensum.getTarifProHauptmahlzeit()
			.compareTo(zeitabschnittDTO.getTarifProHauptmahlzeiten()));
		Assertions.assertEquals(0, betreuungsmitteilungPensum.getTarifProNebenmahlzeit()
			.compareTo(zeitabschnittDTO.getTarifProNebenmahlzeiten()));
		Assertions.assertTrue(betreuungsmitteilungPensum.getGueltigkeit()
			.getGueltigAb()
			.isEqual(zeitabschnittDTO.getVon()));
		Assertions.assertTrue(betreuungsmitteilungPensum.getGueltigkeit()
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
		Assertions.assertNotNull(betreuungsPensum);
		Assertions.assertEquals(0, betreuungsPensum.getMonatlicheBetreuungskosten()
			.compareTo(zeitabschnittDTO.getBetreuungskosten()));
		Assertions.assertEquals(0, betreuungsPensum.getPensum()
			.compareTo(zeitabschnittDTO.getBetreuungspensum()));
		Assertions.assertEquals(0, betreuungsPensum.getMonatlicheHauptmahlzeiten()
			.compareTo(zeitabschnittDTO.getAnzahlHauptmahlzeiten()));
		Assertions.assertEquals(0, betreuungsPensum.getMonatlicheNebenmahlzeiten()
			.compareTo(zeitabschnittDTO.getAnzahlNebenmahlzeiten()));
		Assertions.assertEquals(0, betreuungsPensum.getTarifProHauptmahlzeit()
			.compareTo(zeitabschnittDTO.getTarifProHauptmahlzeiten()));
		Assertions.assertEquals(0, betreuungsPensum.getTarifProNebenmahlzeit()
			.compareTo(zeitabschnittDTO.getTarifProNebenmahlzeiten()));
		Assertions.assertTrue(betreuungsPensum.getGueltigkeit().getGueltigAb().isEqual(zeitabschnittDTO.getVon()));
		Assertions.assertTrue(betreuungsPensum.getGueltigkeit().getGueltigBis().isEqual(zeitabschnittDTO.getBis()));
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
		Assertions.assertNull(betreuungsmitteilungPensum);
	}

	@Test
	public void testMapZeitAbschnitteToImportGoLive() {
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
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(LocalDate.of(2000, 1, 1));
		gueltigkeit.setGueltigBis(LocalDate.of(2030, 1, 1));

		List<ZeitabschnittDTO> zeitabschnitteToImport = handler.mapZeitabschnitteToImport(
			betreuungEventDTO,
			Objects.requireNonNull(betreuung), gueltigkeit);

		Assertions.assertEquals(LocalDate.of(2020, 8, 1), zeitabschnitteToImport.get(0).getVon());
		Assertions.assertEquals(LocalDate.of(2020, 12, 31), zeitabschnitteToImport.get(0).getBis());

		Assertions.assertEquals(LocalDate.of(2021, 1, 1), zeitabschnitteToImport.get(1).getVon());
		Assertions.assertEquals(LocalDate.of(2021, 6, 30), zeitabschnitteToImport.get(1).getBis());

		Assertions.assertEquals(2, zeitabschnitteToImport.size());
	}

	@Test
	public void testMapZeitAbschnitteToImport() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		//Zeitabschnitt bevor von soll nicht genommen werden
		ZeitabschnittDTO zeitabschnittDTOBevor = createZeitabschnittDTO();
		zeitabschnittDTOBevor.setVon(LocalDate.of(2020, 11, 1));
		zeitabschnittDTOBevor.setBis(LocalDate.of(2020, 11, 30));
		//uberlapende Zeitabschnitt von
		ZeitabschnittDTO zeitabschnittDTOUberVon = createZeitabschnittDTO();
		zeitabschnittDTOUberVon.setVon(LocalDate.of(2020, 12, 1));
		zeitabschnittDTOUberVon.setBis(LocalDate.of(2021, 1, 31));
		//in der mitte Zeitabschnitt
		ZeitabschnittDTO zeitabschnittDTOInMitte = createZeitabschnittDTO();
		zeitabschnittDTOInMitte.setVon(LocalDate.of(2021, 2, 1));
		zeitabschnittDTOInMitte.setBis(LocalDate.of(2021, 3, 31));
		///uberlapende Zeitabschnitt bis
		ZeitabschnittDTO zeitabschnittDTOUberBis = createZeitabschnittDTO();
		zeitabschnittDTOUberBis.setVon(LocalDate.of(2021, 4, 1));
		zeitabschnittDTOUberBis.setBis(LocalDate.of(2021, 6, 30));
		///Zeitabschnitt nach bis soll nicht genommen werden
		ZeitabschnittDTO zeitabschnittDTONachBis = createZeitabschnittDTO();
		zeitabschnittDTONachBis.setVon(LocalDate.of(2021, 7, 1));
		zeitabschnittDTONachBis.setBis(LocalDate.of(2021, 7, 31));

		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2021, 1, 31)))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(
			zeitabschnittDTOBevor,
			zeitabschnittDTOUberVon,
			zeitabschnittDTOInMitte,
			zeitabschnittDTOUberBis,
			zeitabschnittDTONachBis));
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(LocalDate.of(2021, 1, 1));
		gueltigkeit.setGueltigBis(LocalDate.of(2021, 5, 1));

		List<ZeitabschnittDTO> zeitabschnitteToImport = handler.mapZeitabschnitteToImport(
			betreuungEventDTO,
			Objects.requireNonNull(betreuung), gueltigkeit);

		Assertions.assertEquals(LocalDate.of(2020, 8, 1), zeitabschnitteToImport.get(0).getVon());
		Assertions.assertEquals(LocalDate.of(2020, 12, 31), zeitabschnitteToImport.get(0).getBis());

		Assertions.assertEquals(LocalDate.of(2021, 1, 1), zeitabschnitteToImport.get(1).getVon());
		Assertions.assertEquals(LocalDate.of(2021, 1, 31), zeitabschnitteToImport.get(1).getBis());

		Assertions.assertEquals(LocalDate.of(2021, 2, 1), zeitabschnitteToImport.get(2).getVon());
		Assertions.assertEquals(LocalDate.of(2021, 3, 31), zeitabschnitteToImport.get(2).getBis());

		Assertions.assertEquals(LocalDate.of(2021, 4, 1), zeitabschnitteToImport.get(3).getVon());
		Assertions.assertEquals(LocalDate.of(2021, 5, 1), zeitabschnitteToImport.get(3).getBis());

		Assertions.assertEquals(4, zeitabschnitteToImport.size());
	}

	@Test
	public void testMapZeitAbschnitteToImportSplit() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		//in der mitte Zeitabschnitt
		ZeitabschnittDTO zeitabschnittDTOInMitte = createZeitabschnittDTO();
		zeitabschnittDTOInMitte.setVon(LocalDate.of(2021, 1, 1));
		zeitabschnittDTOInMitte.setBis(LocalDate.of(2021, 5, 1));

		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2021, 7, 31)))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTOInMitte));
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(LocalDate.of(2021, 1, 1));
		gueltigkeit.setGueltigBis(LocalDate.of(2021, 5, 1));

		List<ZeitabschnittDTO> zeitabschnitteToImport = handler.mapZeitabschnitteToImport(
			betreuungEventDTO,
			Objects.requireNonNull(betreuung), gueltigkeit);

		Assertions.assertEquals(LocalDate.of(2020, 8, 1), zeitabschnitteToImport.get(0).getVon());
		Assertions.assertEquals(LocalDate.of(2020, 12, 31), zeitabschnitteToImport.get(0).getBis());

		Assertions.assertEquals(LocalDate.of(2021, 5, 2), zeitabschnitteToImport.get(1).getVon());
		Assertions.assertEquals(LocalDate.of(2021, 7, 31), zeitabschnitteToImport.get(1).getBis());

		Assertions.assertEquals(LocalDate.of(2021, 1, 1), zeitabschnitteToImport.get(2).getVon());
		Assertions.assertEquals(LocalDate.of(2021, 5, 1), zeitabschnitteToImport.get(2).getBis());

		Assertions.assertEquals(3, zeitabschnitteToImport.size());
	}

	@Test
	public void testMapZeitAbschnitteToImportSplitBeforeGueltigkeit() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		//in der mitte Zeitabschnitt
		ZeitabschnittDTO zeitabschnittDTOInMitte = createZeitabschnittDTO();
		zeitabschnittDTOInMitte.setVon(LocalDate.of(2020, 1, 1));
		zeitabschnittDTOInMitte.setBis(LocalDate.of(2020, 5, 1));

		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(9999, 12, 31)))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTOInMitte));
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(LocalDate.of(2021, 1, 1));
		gueltigkeit.setGueltigBis(LocalDate.of(2021, 5, 1));

		List<ZeitabschnittDTO> zeitabschnitteToImport = handler.mapZeitabschnitteToImport(
			betreuungEventDTO,
			Objects.requireNonNull(betreuung), gueltigkeit);

		Assertions.assertEquals(LocalDate.of(2020, 8, 1), zeitabschnitteToImport.get(0).getVon());
		Assertions.assertEquals(LocalDate.of(2020, 12, 31), zeitabschnitteToImport.get(0).getBis());

		Assertions.assertEquals(LocalDate.of(2021, 5, 2), zeitabschnitteToImport.get(1).getVon());
		Assertions.assertEquals(LocalDate.of(9999, 12, 31), zeitabschnitteToImport.get(1).getBis());

		Assertions.assertEquals(2, zeitabschnitteToImport.size());
	}
	@Test
	public void testMapZeitAbschnitteToImportShouldSplitIfBetStartsBeforeGueltigkeitAndEndsOnGueltigkeitEnd() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		//in der mitte Zeitabschnitt
		ZeitabschnittDTO zeitabschnittDTOInMitte = createZeitabschnittDTO();
		zeitabschnittDTOInMitte.setVon(LocalDate.of(2020, 1, 1));
		zeitabschnittDTOInMitte.setBis(LocalDate.of(2020, 5, 1));

		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2022, 12, 31)))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTOInMitte));
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(LocalDate.of(2021, 1, 1));
		gueltigkeit.setGueltigBis(LocalDate.of(2022, 12, 31));

		List<ZeitabschnittDTO> zeitabschnitteToImport = handler.mapZeitabschnitteToImport(
			betreuungEventDTO,
			Objects.requireNonNull(betreuung), gueltigkeit);

		Assertions.assertEquals(LocalDate.of(2020, 8, 1), zeitabschnitteToImport.get(0).getVon());
		Assertions.assertEquals(LocalDate.of(2020, 12, 31), zeitabschnitteToImport.get(0).getBis());

		Assertions.assertEquals(1, zeitabschnitteToImport.size());
	}

	@Test
	public void testMapZeitAbschnitteToImportBisEndOfTimeSplitGoLive() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		//in der mitte Zeitabschnitt
		ZeitabschnittDTO zeitabschnittDTOInMitte = createZeitabschnittDTO();
		zeitabschnittDTOInMitte.setVon(LocalDate.of(2020, 1, 1));
		zeitabschnittDTOInMitte.setBis(LocalDate.of(2020, 5, 1));

		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(Constants.END_OF_TIME))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTOInMitte));
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(LocalDate.of(2021, 1, 1));
		gueltigkeit.setGueltigBis(Constants.END_OF_TIME);

		List<ZeitabschnittDTO> zeitabschnitteToImport = handler.mapZeitabschnitteToImport(
			betreuungEventDTO,
			Objects.requireNonNull(betreuung), gueltigkeit);

		Assertions.assertEquals(LocalDate.of(2020, 8, 1), zeitabschnitteToImport.get(0).getVon());
		Assertions.assertEquals(LocalDate.of(2020, 12, 31), zeitabschnitteToImport.get(0).getBis());

		Assertions.assertEquals(1, zeitabschnitteToImport.size());
	}

	@Test
	public void rejectBetreuungEventWhenNotInClientGueltigkeitDoesNotIntersectBetreuungsPeriod() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		ZeitabschnittDTO zeitabschnittDTOInMitte = createZeitabschnittDTO();
		zeitabschnittDTOInMitte.setVon(LocalDate.of(2021, 6, 1));
		zeitabschnittDTOInMitte.setBis(LocalDate.of(2021, 6, 30));

		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				// full period 2020/2021
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2021, 7, 31)))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTOInMitte));
		DateRange gueltigkeit = new DateRange();
		// note: client permission is after Betreuung gueltigkeit, even outside the valid period 2020/2021!
		gueltigkeit.setGueltigAb(LocalDate.of(2021, 8, 1));
		gueltigkeit.setGueltigBis(LocalDate.of(2022, 7, 31));

		List<ZeitabschnittDTO> zeitabschnitteToImport = handler.mapZeitabschnitteToImport(
			betreuungEventDTO,
			Objects.requireNonNull(betreuung), gueltigkeit);

		Assertions.assertEquals(zeitabschnitteToImport.size(), betreuung.getBetreuungspensumContainers().size());
	}

	@Test
	public void testSetZeitabschnitte() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		//Zeitabschnitt bevor von soll nicht genommen werden
		ZeitabschnittDTO zeitabschnittDTOBevor = createZeitabschnittDTO();
		zeitabschnittDTOBevor.setVon(LocalDate.of(2020, 11, 1));
		zeitabschnittDTOBevor.setBis(LocalDate.of(2020, 11, 30));
		//uberlapende Zeitabschnitt von
		ZeitabschnittDTO zeitabschnittDTOUberVon = createZeitabschnittDTO();
		zeitabschnittDTOUberVon.setVon(LocalDate.of(2020, 12, 1));
		zeitabschnittDTOUberVon.setBis(LocalDate.of(2021, 1, 31));
		//in der mitte Zeitabschnitt
		ZeitabschnittDTO zeitabschnittDTOInMitte = createZeitabschnittDTO();
		zeitabschnittDTOInMitte.setVon(LocalDate.of(2021, 2, 1));
		zeitabschnittDTOInMitte.setBis(LocalDate.of(2021, 3, 31));
		///uberlapende Zeitabschnitt bis
		ZeitabschnittDTO zeitabschnittDTOUberBis = createZeitabschnittDTO();
		zeitabschnittDTOUberBis.setVon(LocalDate.of(2021, 4, 1));
		zeitabschnittDTOUberBis.setBis(LocalDate.of(2021, 6, 30));
		///Zeitabschnitt nach bis soll nicht genommen werden
		ZeitabschnittDTO zeitabschnittDTONachBis = createZeitabschnittDTO();
		zeitabschnittDTONachBis.setVon(LocalDate.of(2021, 7, 1));
		zeitabschnittDTONachBis.setBis(LocalDate.of(2021, 7, 31));

		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2021, 3, 31)))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(
			zeitabschnittDTOBevor,
			zeitabschnittDTOUberVon,
			zeitabschnittDTOInMitte,
			zeitabschnittDTOUberBis,
			zeitabschnittDTONachBis));
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(LocalDate.of(2021, 1, 1));
		gueltigkeit.setGueltigBis(LocalDate.of(2021, 5, 1));

		PlatzbestaetigungProcessingContext platzbestaetigungProcessingContext =
			new PlatzbestaetigungProcessingContext(betreuung, betreuungEventDTO, gueltigkeit);

		handler.setZeitabschnitte(platzbestaetigungProcessingContext, true);
		BetreuungspensumContainer[] betreuungspensumContainers =
			new BetreuungspensumContainer[platzbestaetigungProcessingContext.getBetreuung()
				.getBetreuungspensumContainers()
				.size()];
		platzbestaetigungProcessingContext.getBetreuung()
			.getBetreuungspensumContainers()
			.stream()
			.sorted()
			.collect(Collectors.toList())
			.toArray(betreuungspensumContainers);
		Assertions.assertEquals(
			LocalDate.of(2020, 8, 1),
			betreuungspensumContainers[0].getBetreuungspensumJA().getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(
			LocalDate.of(2020, 12, 31),
			betreuungspensumContainers[0].getBetreuungspensumJA().getGueltigkeit().getGueltigBis());

		Assertions.assertEquals(
			LocalDate.of(2021, 1, 1),
			betreuungspensumContainers[1].getBetreuungspensumJA().getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(
			LocalDate.of(2021, 1, 31),
			betreuungspensumContainers[1].getBetreuungspensumJA().getGueltigkeit().getGueltigBis());

		Assertions.assertEquals(
			LocalDate.of(2021, 2, 1),
			betreuungspensumContainers[2].getBetreuungspensumJA().getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(
			LocalDate.of(2021, 3, 31),
			betreuungspensumContainers[2].getBetreuungspensumJA().getGueltigkeit().getGueltigBis());

		Assertions.assertEquals(
			LocalDate.of(2021, 4, 1),
			betreuungspensumContainers[3].getBetreuungspensumJA().getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(
			LocalDate.of(2021, 5, 1),
			betreuungspensumContainers[3].getBetreuungspensumJA().getGueltigkeit().getGueltigBis());

		Assertions.assertEquals(4, betreuungspensumContainers.length);
	}

	@Test
	public void testSetZeitabschnitteSplit() {
		BetreuungEventDTO betreuungEventDTO = createBetreuungEventDTO();
		//in der mitte Zeitabschnitt
		ZeitabschnittDTO zeitabschnittDTOInMitte = createZeitabschnittDTO();
		zeitabschnittDTOInMitte.setVon(LocalDate.of(2021, 1, 1));
		zeitabschnittDTOInMitte.setBis(LocalDate.of(2021, 5, 1));

		Betreuung betreuung = gesuch_1GS.getFirstBetreuung();
		Objects.requireNonNull(betreuung).setBetreuungspensumContainers(
			betreuung
				.getBetreuungspensumContainers()
				.stream()
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2020, 8, 1)))
				.peek(p -> p.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2021, 7, 31)))
				.collect(Collectors.toSet()));

		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnittDTOInMitte));
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(LocalDate.of(2021, 1, 1));
		gueltigkeit.setGueltigBis(LocalDate.of(2021, 5, 1));

		PlatzbestaetigungProcessingContext platzbestaetigungProcessingContext =
			new PlatzbestaetigungProcessingContext(betreuung, betreuungEventDTO, gueltigkeit);

		handler.setZeitabschnitte(platzbestaetigungProcessingContext, true);
		BetreuungspensumContainer[] betreuungspensumContainers =
			new BetreuungspensumContainer[platzbestaetigungProcessingContext.getBetreuung()
				.getBetreuungspensumContainers()
				.size()];
		platzbestaetigungProcessingContext.getBetreuung()
			.getBetreuungspensumContainers()
			.stream()
			.sorted()
			.collect(Collectors.toList())
			.toArray(betreuungspensumContainers);

		Assertions.assertEquals(
			LocalDate.of(2020, 8, 1),
			betreuungspensumContainers[0].getBetreuungspensumJA().getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(
			LocalDate.of(2020, 12, 31),
			betreuungspensumContainers[0].getBetreuungspensumJA().getGueltigkeit().getGueltigBis());
		Assertions.assertEquals(
			LocalDate.of(2021, 1, 1),
			betreuungspensumContainers[1].getBetreuungspensumJA().getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(
			LocalDate.of(2021, 5, 1),
			betreuungspensumContainers[1].getBetreuungspensumJA().getGueltigkeit().getGueltigBis());
		Assertions.assertEquals(
			LocalDate.of(2021, 5, 2),
			betreuungspensumContainers[2].getBetreuungspensumJA().getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(
			LocalDate.of(2021, 7, 31),
			betreuungspensumContainers[2].getBetreuungspensumJA().getGueltigkeit().getGueltigBis());

		Assertions.assertEquals(3, betreuungspensumContainers.length);
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

	private Betreuungsmitteilung createBetreuungMitteilung() {
		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		BetreuungsmitteilungPensum betreuungsmitteilungPensum = new BetreuungsmitteilungPensum();
		betreuungsmitteilungPensum.setMonatlicheBetreuungskosten(new BigDecimal(2000.00).setScale(2));
		betreuungsmitteilungPensum.setPensum(new BigDecimal(80));
		betreuungsmitteilungPensum.setUnitForDisplay(PensumUnits.PERCENTAGE);
		DateRange dateRange = new DateRange();
		dateRange.setGueltigAb(LocalDate.of(2017, 8, 01));
		dateRange.setGueltigBis(LocalDate.of(2018, 1, 31));
		betreuungsmitteilungPensum.setGueltigkeit(dateRange);
		Set<BetreuungsmitteilungPensum> betreuungsmitteilungPensumSet = new HashSet<>();
		betreuungsmitteilungPensumSet.add(betreuungsmitteilungPensum);
		betreuungsmitteilung.setBetreuungspensen(betreuungsmitteilungPensumSet);
		return betreuungsmitteilung;
	}
}
