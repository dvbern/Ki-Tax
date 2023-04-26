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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
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
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.enums.EinstellungKey.FKJV_EINGEWOEHNUNG;
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
		einstellungenMap.get(FKJV_EINGEWOEHNUNG).setValue("true");
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

		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil
			.createErwerbspensum(TestDataUtil.START_PERIODE.plusMonths(1), TestDataUtil.ENDE_PERIODE, 40));

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(betreuung);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());

		Assert.assertEquals(60, result.get(1).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE.plusMonths(1), result.get(1).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.ENDE_PERIODE,
			result.get(1).getGueltigkeit().getGueltigBis());
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
		einstellungenMap.get(FKJV_EINGEWOEHNUNG).setValue("true");
		return EbeguRuleTestsHelper.calculate(betreuung, EbeguRuleTestsHelper.getAllEinstellungen(betreuung.extractGesuchsperiode()), einstellungenMap);
	}
}
