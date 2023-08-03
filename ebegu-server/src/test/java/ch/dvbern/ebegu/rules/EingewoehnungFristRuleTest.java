/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EingewoehnungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_EINGEWOEHNUNG;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static ch.dvbern.ebegu.enums.EinstellungKey.EINGEWOEHNUNG_TYP;
import static org.junit.Assert.assertNotNull;

public class EingewoehnungFristRuleTest {

	@Test
	/**
	 * Normalenfall, keine Eingewoehnung
	 */
	public void testEingewoehnungFristRule1GesuchstellerOhne() {
		Betreuung betreuung = createGesuch(false, false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 100));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	/**
	 * Normalenfall, Eingewoehnung, 1 Erwerbspensum Begin Anfang September
	 */
	public void testEingewoehnungFristRule1Gesuchsteller() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 100));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	/**
	 * Normalenfall, Eingewoehnung, 1 Erwerbspensum Begin Mitte August
	 */
	public void testEingewoehnungFristRuleAnspruchAbMitteAugust() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		LocalDate AUG_15 = TestDataUtil.START_PERIODE.plusDays(15);

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
				.createErwerbspensum(AUG_15, TestDataUtil.ENDE_PERIODE, 100));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		Assert.assertEquals(3, result.size());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(AUG_15.minusDays(1), result.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertEquals(AUG_15, result.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(Integer.valueOf(2016), result.get(0).getEinkommensjahr());
	}

	@Test
	/**
	 *  Eingewoehnung, 2 Erwerbspensum, beides Begin Anfang September, beides verlaengert
	 */
	public void testEingewoehnungFristRule1GesuchstellerManyErwerbspensenGleicheStartdatum() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 50));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 10));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);
		// beide Erwerbspensum fangen gleich an, Sie muessen beide verleangert werden und summiert 50 + 10 +
		// Zuschlag 20
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(80, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	/**
	 *  Eingewoehnung, 2 Erwerbspensum, eine Begin Anfang September, eine spaeter, nur die erste velaengert als genuegen
	 */
	public void testEingewoehnungFristRule1GesuchstellerManyErwerbspensenNichtGleicheStartdatum() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 50));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(2), TestDataUtil.ENDE_PERIODE, 10));

		List<VerfuegungZeitabschnitt> result =calculateMitEingewoehnung(betreuung);
		// nur die erste ist verlaengert: 50  + Zuschlag 20
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(70, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	/**
	 *  Eingewoehnung, 2 Erwerbspensum, Freiwilligarbeit Begin Anfang September, eine andere Taetigkeit spaeter
	 *  Es gibt keinen Zusaetzliche Anspruch bei die Gemeinde, so ASIV ignoriert die FreiwilligeArbeit Taetigkeit
	 */
	public void testEingewoehnungFristRule1GesuchstellerFreiwilligeArbeitOhneGemeindeZuschlag() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(
				TestDataUtil.START_PERIODE.plusMonths(1),
				TestDataUtil.ENDE_PERIODE,
				60,
				Taetigkeit.FREIWILLIGENARBEIT));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(2), TestDataUtil.ENDE_PERIODE, 40));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);
		// die Erwerbspensum 2 ist von einer Monat verlaengert anstatt die erste
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(60, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE.plusMonths(1), result.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(2).minusDays(1),
			result.get(1).getGueltigkeit().getGueltigBis());
	}

	@Test
	/**
	 *  Eingewoehnung, 2 Erwerbspensum, Freiwilligarbeit Begin Anfang September, eine andere Taetigkeit spaeter
	 *  Es gibt einen Zusaetzliche Anspruch bei die Gemeinde von 20, so die FreiwilligeArbeit Taetigkeit
	 *  ist verlaengert
	 */
	public void testEingewoehnungFristRule1GesuchstellerFreiwilligeArbeitMitGemeindeZuschlag() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(
				TestDataUtil.START_PERIODE.plusMonths(1),
				TestDataUtil.ENDE_PERIODE,
				60,
				Taetigkeit.FREIWILLIGENARBEIT));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(2), TestDataUtil.ENDE_PERIODE, 40));
		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(EINGEWOEHNUNG_TYP).setValue(EingewoehnungTyp.FKJV.toString());
		List<VerfuegungZeitabschnitt> result =
			EbeguRuleTestsHelper.calculate(betreuung, EbeguRuleTestsHelper.getEinstellungenRulesParis(
				gesuch.getGesuchsperiode()), einstellungenMap);
		// Freiwilligenarbeit hat 20 Porcent zuschlag, das heisst das die erste Erwerbspensum ist dieses Mal erweitert
		// von einen Monat
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(40, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	/**
	 *  Eingewoehnung, 2 Gesuchstellende, beide Erwerspensen mit gleiche Startdatum
	 */
	public void testEingewoehnungFristRule2GesuchstellerGleicheStart() {
		Betreuung betreuung = createGesuch(true, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 100));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 40));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);
		//2 Gesuchstellende, 140% => 40% brechtigt + 20 zuschlag
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	/**
	 *  Eingewoehnung, 2 Gesuchstellende
	 */
	public void testEingewoehnungFristRule2Gesuchsteller1StartBevor() {
		Betreuung betreuung = createGesuch(true, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 100));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(2), TestDataUtil.ENDE_PERIODE, 40));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);
		//Nur 1 Gesuchsteller Erwerbspensum mit 100 ist verlaengert, minimum 120 nicht erreicht, 0 Anspruch
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertEquals(60, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE.plusMonths(1), result.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(2).minusDays(1),
			result.get(1).getGueltigkeit().getGueltigBis());
	}

	@Test
	/**
	 *  Eingewoehnung, 2 Gesuchstellende, unterschiedliche Erwerbspensen, Betreuung startet waehrend Periode
	 */
	public void testEingewoehnungFristRuleNachGPStart2Gesuchsteller() {
		Betreuung betreuung = createGesuch(true, true);
		Gesuch gesuch = betreuung.extractGesuch();

		Betreuungspensum eingewoehnung = new Betreuungspensum();
		eingewoehnung.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.START_PERIODE.plusMonths(2).minusDays(1)));
		eingewoehnung.setPensum(new BigDecimal(40));
		eingewoehnung.setMonatlicheBetreuungskosten(new BigDecimal(1500));

		betreuung.getBetreuungspensumContainers()
				.stream()
				.findFirst()
				.get()
				.getBetreuungspensumJA()
				.getGueltigkeit()
				.setGueltigAb(TestDataUtil.START_PERIODE.plusMonths(2));

		BetreuungspensumContainer eingewoehnungContainer = new BetreuungspensumContainer();
		eingewoehnungContainer.setBetreuungspensumJA(eingewoehnung);

		betreuung.getBetreuungspensumContainers().add(eingewoehnungContainer);

		// 1.8 - 31.8. 70% + 20% Zuschlag => 90%, kein Anspruch
		// 1.9 - 30.9. 90% + 20% Zuschlag => 110%, kein Anspruch
		// 1.9 - ...   140% + 20% Zuschlag => 160%, 60% Anspruch
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
				.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 20));
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
				.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(2), TestDataUtil.ENDE_PERIODE, 50));
		assertNotNull(gesuch.getGesuchsteller2());
		gesuch.getGesuchsteller2().addErwerbspensumContainer(TestDataUtil
				.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 70));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);
		//2 Gesuchstellende, 140% => 40% brechtigt + 20 zuschlag
		Assert.assertEquals(3, result.size());
		Assert.assertEquals(60, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(60, result.get(2).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
				TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
				result.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	public void eingewoehungFristRuleEingagsdatumZuSpaet() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setEingangsdatum(TestDataUtil.START_PERIODE.plusDays(1));

		LocalDate SEP_06 = TestDataUtil.START_PERIODE.plusMonths(1).plusDays(5);

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(SEP_06, TestDataUtil.ENDE_PERIODE, 40));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		Assert.assertEquals(4, result.size());

		VerfuegungZeitabschnitt za01 = result.get(0);
		Assert.assertEquals(0, za01.getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, za01.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.START_PERIODE.with(TemporalAdjusters.lastDayOfMonth()),
				za01.getGueltigkeit().getGueltigBis());

		VerfuegungZeitabschnitt za02 = result.get(1);
		Assert.assertEquals(60, za02.getAnspruchberechtigtesPensum());
		Assert.assertEquals(SEP_06.with(TemporalAdjusters.firstDayOfMonth()), za02.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(SEP_06.minusDays(1), za02.getGueltigkeit().getGueltigBis());

		VerfuegungZeitabschnitt za03 = result.get(2);
		Assert.assertEquals(60, za03.getAnspruchberechtigtesPensum());
		Assert.assertEquals(SEP_06, za03.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(SEP_06.with(TemporalAdjusters.lastDayOfMonth()), za03.getGueltigkeit().getGueltigBis());

		VerfuegungZeitabschnitt za04 = result.get(3);
		Assert.assertEquals(60, za04.getAnspruchberechtigtesPensum());
		Assert.assertEquals(SEP_06.with(TemporalAdjusters.firstDayOfNextMonth()), za04.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, za04.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void eingewoehungFristRuleErwerbsensumMitUnterbruch() {
		LocalDate SEP_30 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.SEPTEMBER, 30);
		LocalDate NOV_15 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.NOVEMBER, 15);
		LocalDate DEC_15 = NOV_15.plusMonths(1);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		//Betreuung ab 15.11.
		betreuung.getBetreuungspensumContainers().stream()
				.findFirst()
				.get()
				.getBetreuungspensumJA()
				.getGueltigkeit()
				.setGueltigAb(NOV_15);

		assertNotNull(gesuch.getGesuchsteller1());

		//ewp 1.8. - 30.9
		ErwerbspensumContainer ewp1 = TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE , SEP_30, 40);

		//ewp 15.12 - 31.07
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(DEC_15, TestDataUtil.ENDE_PERIODE, 40);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp1);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		Assert.assertEquals(6, result.size());

		//01.08-30.09 Anspruch 60 (40+ 20 Zuschlag)
		Assert.assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(SEP_30,	result.get(0).getGueltigkeit().getGueltigBis());

		//01.10-14.11, Anspruch 0
		Assert.assertEquals(0, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(SEP_30.plusDays(1), result.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(NOV_15.minusDays(1),	result.get(1).getGueltigkeit().getGueltigBis());

		//15.11- 30.11, Anspruch 60 Eingewöhnung
		//01.12- 15.12, Anspruch 60 Eingewöhnung
		Assert.assertEquals(60, result.get(2).getAnspruchberechtigtesPensum());
		Assert.assertEquals(NOV_15, result.get(2).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(NOV_15.with(TemporalAdjusters.lastDayOfMonth()),result.get(2).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(result.get(2).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		Assert.assertEquals(60, result.get(3).getAnspruchberechtigtesPensum());
		Assert.assertEquals(DEC_15.with(TemporalAdjusters.firstDayOfMonth()), result.get(3).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(DEC_15.minusDays(1), result.get(3).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(result.get(3).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//15.12- 31.12, Anspruch 60
		//01.1 - 31.07, Anspruch 60
		Assert.assertEquals(60, result.get(4).getAnspruchberechtigtesPensum());
		Assert.assertEquals(DEC_15, result.get(4).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(DEC_15.with(TemporalAdjusters.lastDayOfMonth()), result.get(4).getGueltigkeit().getGueltigBis());

		Assert.assertEquals(60, result.get(5).getAnspruchberechtigtesPensum());
		Assert.assertEquals(DEC_15.with(TemporalAdjusters.firstDayOfNextMonth()), result.get(5).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE,result.get(5).getGueltigkeit().getGueltigBis());
	}

	@Test
	public void eingewoehungFristRuleErwerbsensumBetreuungNicht30TageVorher() {
		LocalDate OCT_10 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.OCTOBER, 10);
		LocalDate START_EINGEWOEHNUNG = OCT_10.minusDays(30);
		LocalDate SEP_25 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.SEPTEMBER, 25);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		//Betreuung ab 25.9.
		betreuung.getBetreuungspensumContainers().stream()
				.findFirst()
				.get()
				.getBetreuungspensumJA()
				.getGueltigkeit()
				.setGueltigAb(SEP_25);

		assertNotNull(gesuch.getGesuchsteller1());

		//ewp ab 10.10
		ErwerbspensumContainer ewp1 = TestDataUtil.createErwerbspensum(OCT_10, TestDataUtil.ENDE_PERIODE, 40);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp1);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		Assert.assertEquals(6, result.size());

		//01.08-09.09 Anspruch 0%, Betreuung 0%
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(0, result.get(0).getBetreuungspensumProzent().intValue());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(START_EINGEWOEHNUNG.minusDays(1),	result.get(0).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//10.09.-24.09 Anspruch 60%, Eingewöhnung, Betreuung 0%
		Assert.assertEquals(60, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(0, result.get(1).getBetreuungspensumProzent().intValue());
		Assert.assertEquals(START_EINGEWOEHNUNG, result.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(SEP_25.minusDays(1),	result.get(1).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(result.get(1).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//25.09.-30.09. Anspruch 60%, Eingewöhnung, Betreuung 80%
		Assert.assertEquals(60, result.get(2).getAnspruchberechtigtesPensum());
		Assert.assertEquals(80, result.get(2).getBetreuungspensumProzent().intValue());
		Assert.assertEquals(SEP_25, result.get(2).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(SEP_25.with(TemporalAdjusters.lastDayOfMonth()),	result.get(2).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(result.get(2).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//01.10.-09.10. Anspruch 60%, Eingewöhnung, Betreuung 80%
		Assert.assertEquals(60, result.get(3).getAnspruchberechtigtesPensum());
		Assert.assertEquals(80, result.get(3).getBetreuungspensumProzent().intValue());
		Assert.assertEquals(OCT_10.with(TemporalAdjusters.firstDayOfMonth()), result.get(3).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(OCT_10.minusDays(1),	result.get(3).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(result.get(3).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//11.10.-31.10, Anspruch 60, Betreuung 80%
		Assert.assertEquals(60, result.get(4).getAnspruchberechtigtesPensum());
		Assert.assertEquals(80, result.get(4).getBetreuungspensumProzent().intValue());
		Assert.assertEquals(OCT_10, result.get(4).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(OCT_10.with(TemporalAdjusters.lastDayOfMonth()),	result.get(4).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(result.get(4).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//01.11-31.07, Anspruch 60, Betreuung 80%
		Assert.assertEquals(60, result.get(5).getAnspruchberechtigtesPensum());
		Assert.assertEquals(80, result.get(5).getBetreuungspensumProzent().intValue());
		Assert.assertEquals(OCT_10.with(TemporalAdjusters.firstDayOfNextMonth()), result.get(5).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE,	result.get(5).getGueltigkeit().getGueltigBis());
		Assert.assertFalse(result.get(5).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));
	}

	@Test
	public void eingewoehungFristRuleBetreuungMitUnterbruchEWPAbZweiterBetreuung() {
		LocalDate SEP_30 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.SEPTEMBER, 30);
		LocalDate NOV_15 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.NOVEMBER, 15);
		LocalDate DEC_15 = NOV_15.plusMonths(1);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		final BetreuungspensumContainer firstPensum = betreuung.getBetreuungspensumContainers().stream()
			.findFirst()
			.get();

		final BetreuungspensumContainer secondPensum =
			firstPensum.copyBetreuungspensumContainer(
				new BetreuungspensumContainer(),
				AntragCopyType.MUTATION,
				betreuung);

		// BetreuungsPensum 1 bis 30.9.
		firstPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigBis(SEP_30);

		//BetreuungsPensum 2 ab 15.12.
		secondPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigAb(DEC_15);

		var betreuungsPensen = new HashSet<BetreuungspensumContainer>();
		betreuungsPensen.add(firstPensum);
		betreuungsPensen.add(secondPensum);

		betreuung.setBetreuungspensumContainers(betreuungsPensen);

		assertNotNull(gesuch.getGesuchsteller1());

		//ewp 15.12 - 31.07
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(DEC_15, TestDataUtil.ENDE_PERIODE, 40);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		Assert.assertEquals(5, result.size());

		//01.08-30.9, Betreuung 80, Anspruch 0
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(80, result.get(0).getBetreuungspensumProzent().intValue());
		Assert.assertEquals(betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb(), result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(SEP_30, result.get(0).getGueltigkeit().getGueltigBis());

		//30.9 - 14.11., Betreuung 0, Anspruch 0
		Assert.assertEquals(0, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(0, result.get(1).getBetreuungspensumProzent().intValue());
		Assert.assertEquals(SEP_30.plusDays(1), result.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(NOV_15.minusDays(1), result.get(1).getGueltigkeit().getGueltigBis());

		//15.11 - 14.12, Anspruch 60 Eingewöhnung, Betreuung 0
		Assert.assertEquals(60, result.get(2).getAnspruchberechtigtesPensum());
		Assert.assertEquals(NOV_15, result.get(2).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(NOV_15.plusMonths(1).minusDays(1),
			result.get(2).getGueltigkeit().getGueltigBis());
		Assert.assertTrue(result.get(2).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//15.12- 31.12, Anspruch 60
		//01.1 - 31.07, Anspruch 60
		Assert.assertEquals(60, result.get(3).getAnspruchberechtigtesPensum());
		Assert.assertEquals(DEC_15, result.get(3).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(DEC_15.with(TemporalAdjusters.lastDayOfMonth()), result.get(3).getGueltigkeit().getGueltigBis());

		Assert.assertEquals(60, result.get(4).getAnspruchberechtigtesPensum());
		Assert.assertEquals(DEC_15.with(TemporalAdjusters.firstDayOfNextMonth()), result.get(4).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE,result.get(4).getGueltigkeit().getGueltigBis());
	}


	@Test
	public void eingewoehungFristRuleAnspruchOverlappingEingewoehnungAndMonthStartPensumSteigend() {
		LocalDate SEP_30 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.SEPTEMBER, 30);
		LocalDate DEC_8 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.DECEMBER, 8);
		LocalDate DEC_15 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.DECEMBER, 15);
		LocalDate DEC_22 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.DECEMBER, 22);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		final BetreuungspensumContainer firstPensum = betreuung.getBetreuungspensumContainers().stream()
			.findFirst()
			.orElseThrow();

		// BetreuungsPensum 15.12. - 31.7.
		firstPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigAb(DEC_15);
		firstPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigBis(TestDataUtil.ENDE_PERIODE);


		var betreuungsPensen = new HashSet<BetreuungspensumContainer>();
		betreuungsPensen.add(firstPensum);
		betreuung.setBetreuungspensumContainers(betreuungsPensen);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();
		//ewp 1.8. - 8.12.
		ErwerbspensumContainer ewp = TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, DEC_8, 40);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp);
		//ewp 22.12 -
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(DEC_22, TestDataUtil.ENDE_PERIODE, 60);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		assertThat(result.size(), is(6));

		//01.08- 21.11., Betreuung 0, Anspruch 60
		assertThat(result.get(0).getAnspruchberechtigtesPensum(), is(60));
		assertThat(result.get(0).getBetreuungspensumProzent().intValue(), is(0));
		assertThat(result.get(0).getGueltigkeit().getGueltigAb(), is(TestDataUtil.START_PERIODE));
		assertThat(result.get(0).getGueltigkeit().getGueltigBis(), is(DEC_22.minusMonths(1).minusDays(1)));

		//22.11. - 8.12.  Betreuung 0, Anspruch 60 Eingewoehnung Anspruch 80
		assertThat(result.get(1).getAnspruchberechtigtesPensum(), is(80));
		assertThat(result.get(1).getBetreuungspensumProzent().intValue(), is(0));
		assertThat(result.get(1).getGueltigkeit().getGueltigAb(), is(DEC_22.minusMonths(1)));
		assertThat(result.get(1).getGueltigkeit().getGueltigBis(), is(DEC_8));
		Assert.assertTrue(result.get(1).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//9.12 - 14.12., kein Anspruch Eingewöhnung Anspruch 80, Betreuung 0
		assertThat(result.get(2).getAnspruchberechtigtesPensum(), is(80));
		assertThat(result.get(2).getGueltigkeit().getGueltigAb(), is(DEC_8.plusDays(1)));
		assertThat(result.get(2).getGueltigkeit().getGueltigBis(), is(DEC_15.minusDays(1)));
		Assert.assertTrue(result.get(2).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));
		Assert.assertTrue(result.get(2).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));

		//15.12 - 21.12, kein Anspruch Eingewöhnung Anspruch 80, Betreuung 60
		assertThat(result.get(3).getAnspruchberechtigtesPensum(), is(80));
		assertThat(result.get(3).getGueltigkeit().getGueltigAb(), is(DEC_15));
		assertThat(result.get(3).getGueltigkeit().getGueltigBis(), is(DEC_22.minusDays(1)));
		Assert.assertTrue(result.get(3).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));
		Assert.assertTrue(result.get(3).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));

		// 22.12. Anspruch 80, Betreuung 60
		assertThat(result.get(4).getAnspruchberechtigtesPensum(), is(80));
		assertThat(result.get(4).getGueltigkeit().getGueltigAb(), is(DEC_22));
		assertThat(result.get(4).getGueltigkeit().getGueltigBis(), is(DEC_15.with(TemporalAdjusters.lastDayOfMonth())));
	}


	@Test
	public void eingewoehungFristRuleAnspruchOverlappingEingewoehnungAndMonthStartPensumSinkend() {
		LocalDate SEP_30 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.SEPTEMBER, 30);
		LocalDate DEC_8 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.DECEMBER, 8);
		LocalDate DEC_15 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.DECEMBER, 15);
		LocalDate DEC_22 = LocalDate.of(TestDataUtil.START_PERIODE.getYear(), Month.DECEMBER, 22);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		final BetreuungspensumContainer firstPensum = betreuung.getBetreuungspensumContainers().stream()
			.findFirst()
			.orElseThrow();

		// BetreuungsPensum 15.12. - 31.7.
		firstPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigAb(DEC_15);
		firstPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigBis(TestDataUtil.ENDE_PERIODE);


		var betreuungsPensen = new HashSet<BetreuungspensumContainer>();
		betreuungsPensen.add(firstPensum);
		betreuung.setBetreuungspensumContainers(betreuungsPensen);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();
		//ewp 1.8. - 8.12.
		ErwerbspensumContainer ewp = TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, DEC_8, 80);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp);
		//ewp 22.12 -
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(DEC_22, TestDataUtil.ENDE_PERIODE, 60);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		assertThat(result.size(), is(4));

		//01.08- 14.12., Betreuung 0, Anspruch 100
		assertThat(result.get(0).getAnspruchberechtigtesPensum(), is(100));
		assertThat(result.get(0).getBetreuungspensumProzent().intValue(), is(0));
		assertThat(result.get(0).getGueltigkeit().getGueltigAb(), is(TestDataUtil.START_PERIODE));
		assertThat(result.get(0).getGueltigkeit().getGueltigBis(), is(DEC_22.minusMonths(1).minusDays(1)));

		//22.11. - 8.12.  Betreuung 0, Anspruch 100 Eingewoehnung Anspruch 80
		assertThat(result.get(1).getAnspruchberechtigtesPensum(), is(100));
		assertThat(result.get(1).getBetreuungspensumProzent().intValue(), is(0));
		assertThat(result.get(1).getGueltigkeit().getGueltigAb(), is(DEC_22.minusMonths(1)));
		assertThat(result.get(1).getGueltigkeit().getGueltigBis(), is(DEC_15.minusDays(1)));
		Assert.assertTrue(result.get(1).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		//15.12 - 31.12, Anspruch 100 Eingewöhnung Anspruch 80, Betreuung 60
		assertThat(result.get(2).getAnspruchberechtigtesPensum(), is(100));
		assertThat(result.get(2).getGueltigkeit().getGueltigAb(), is(DEC_15));
		assertThat(result.get(2).getGueltigkeit().getGueltigBis(), is(DEC_22.with(TemporalAdjusters.lastDayOfYear())));
		Assert.assertTrue(result.get(2).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG));

		// 1.1. - 31.7. Anspruch 80, Betreuung 60
		assertThat(result.get(3).getAnspruchberechtigtesPensum(), is(80));
		assertThat(result.get(3).getGueltigkeit().getGueltigAb(), is(DEC_22.with(TemporalAdjusters.lastDayOfYear()).plusDays(1)));
		assertThat(result.get(3).getGueltigkeit().getGueltigBis(), is(TestDataUtil.ENDE_PERIODE));
	}

	private Betreuung createGesuch(final boolean gs2, final boolean eingewoehnung) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(gs2);
		betreuung.setEingewoehnung(eingewoehnung);

		BetreuungspensumContainer container  = TestDataUtil.createBetPensContainer(betreuung);
		container.getGueltigkeit().setGueltigAb(TestDataUtil.START_PERIODE);
		container.getGueltigkeit().setGueltigBis(TestDataUtil.ENDE_PERIODE);
		betreuung.getBetreuungspensumContainers().add(container);

		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, gs2);
		return betreuung;
	}

	private List<VerfuegungZeitabschnitt> calculateMitEingewoehnung(@Nonnull Betreuung betreuung) {
		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(EINGEWOEHNUNG_TYP).setValue(EingewoehnungTyp.FKJV.toString());
		return EbeguRuleTestsHelper.calculate(betreuung, EbeguRuleTestsHelper.getAllEinstellungen(betreuung.extractGesuchsperiode()), einstellungenMap);
	}
}
