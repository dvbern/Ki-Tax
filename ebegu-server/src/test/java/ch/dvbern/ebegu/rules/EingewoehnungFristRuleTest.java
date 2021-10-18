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

import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

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

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

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

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(TestDataUtil.START_PERIODE, result.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.START_PERIODE.plusMonths(1).minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis());
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

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
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

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
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

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
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

		List<VerfuegungZeitabschnitt> result =
			EbeguRuleTestsHelper.calculate(betreuung, EbeguRuleTestsHelper.getEinstellungenRulesParis(
				gesuch.getGesuchsperiode()));
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

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
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

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
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

	private Betreuung createGesuch(final boolean gs2, final boolean eingewoehnung) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(gs2);
		betreuung.setEingewoehnung(eingewoehnung);
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, gs2);
		return betreuung;
	}
}
