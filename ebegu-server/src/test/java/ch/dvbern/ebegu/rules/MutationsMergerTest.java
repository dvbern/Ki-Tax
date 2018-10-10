/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests fuer Verfügungsmuster
 */
public class MutationsMergerTest {

	private final MutationsMerger mutationsMerger = new MutationsMerger();
	private final MonatsRule monatsRule = new MonatsRule(Constants.DEFAULT_GUELTIGKEIT);

	@Test
	public void test_Reduktion_Rechtzeitig_aenderungUndEingangsdatumGleich() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(6);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.MUTATION);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = monatsRule.createVerfuegungsZeitabschnitte(mutierteBetreuung, zabetrMutiert);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 80);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(monatsRule.createVerfuegungsZeitabschnitte(erstgesuchBetreuung, zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		final Gesuch erstgesuch = erstgesuchBetreuung.extractGesuch();
		mutierteBetreuung.setVorgaengerVerfuegung(verfuegungErstgesuch);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(mutierteBetreuung, verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, TestDataUtil.START_PERIODE.plusMonths(6), 100);
		checkAllAfter(zeitabschnitte, TestDataUtil.START_PERIODE.plusMonths(6), 80);

	}

	@Test
	public void test_Reduktion_Rechtzeitig_aenderungNachEingangsdatum() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(7).plusDays(1);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.MUTATION);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = monatsRule.createVerfuegungsZeitabschnitte(mutierteBetreuung, zabetrMutiert);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 40);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(monatsRule.createVerfuegungsZeitabschnitte(erstgesuchBetreuung, zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		final Gesuch erstgesuch = erstgesuchBetreuung.extractGesuch();
		mutierteBetreuung.setVorgaengerVerfuegung(verfuegungErstgesuch);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(mutierteBetreuung, verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, 100);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, 40);

	}

	@Test
	public void test_Reduktion_Rückwirkend_aenderungVorEingangsdatum() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(5).minusDays(1);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.MUTATION);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = monatsRule.createVerfuegungsZeitabschnitte(mutierteBetreuung, zabetrMutiert);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 40);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(monatsRule.createVerfuegungsZeitabschnitte(erstgesuchBetreuung, zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		final Gesuch erstgesuch = erstgesuchBetreuung.extractGesuch();
		mutierteBetreuung.setVorgaengerVerfuegung(verfuegungErstgesuch);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(mutierteBetreuung, verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, 100);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, 40);

	}

	@Test
	public void test_Erhoehung_Rechtzeitig_aenderungUndEingangsdatumGleich() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(6);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.MUTATION);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = monatsRule.createVerfuegungsZeitabschnitte(mutierteBetreuung, zabetrMutiert);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch = monatsRule.createVerfuegungsZeitabschnitte(erstgesuchBetreuung, zabetrErtgesuch);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteErstgesuch, TestDataUtil.START_PERIODE, 80);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		final Gesuch erstgesuch = erstgesuchBetreuung.extractGesuch();
		mutierteBetreuung.setVorgaengerVerfuegung(verfuegungErstgesuch);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(mutierteBetreuung, verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, 80);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, 100);

	}

	@Test
	public void test_Erhoehung_Rechtzeitig_aenderungNachEingangsdatum() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(7).plusDays(1);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.MUTATION);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = monatsRule.createVerfuegungsZeitabschnitte(mutierteBetreuung, zabetrMutiert);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch = monatsRule.createVerfuegungsZeitabschnitte(erstgesuchBetreuung, zabetrErtgesuch);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteErstgesuch, TestDataUtil.START_PERIODE, 80);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		final Gesuch erstgesuch = erstgesuchBetreuung.extractGesuch();
		mutierteBetreuung.setVorgaengerVerfuegung(verfuegungErstgesuch);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(mutierteBetreuung, verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, 80);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, 100);

	}

	@Test
	public void test_Erhoehung_Nicht_Rechtzeitig_aenderungVorEingangsdatum() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(5).minusDays(1);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.MUTATION);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = monatsRule.createVerfuegungsZeitabschnitte(mutierteBetreuung, zabetrMutiert);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch = monatsRule.createVerfuegungsZeitabschnitte(erstgesuchBetreuung, zabetrErtgesuch);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteErstgesuch, TestDataUtil.START_PERIODE, 80);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		final Gesuch erstgesuch = erstgesuchBetreuung.extractGesuch();
		mutierteBetreuung.setVorgaengerVerfuegung(verfuegungErstgesuch);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(mutierteBetreuung, verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, 80);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, 100);
	}

	@Test
	public void test_Erhoehung_Rechtzeitig_aenderungNachEingangsdatum_nichtAnMonatsgrenze() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(7).plusDays(15);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.MUTATION);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = monatsRule.createVerfuegungsZeitabschnitte(mutierteBetreuung, zabetrMutiert);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		verfuegungsZeitabschnitteMutiert = splitUpAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum.withDayOfMonth(aenderungsDatumPensum.lengthOfMonth()).plusDays(1), 100);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch = monatsRule.createVerfuegungsZeitabschnitte(erstgesuchBetreuung, zabetrErtgesuch);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteErstgesuch, TestDataUtil.START_PERIODE, 80);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		final Gesuch erstgesuch = erstgesuchBetreuung.extractGesuch();
		mutierteBetreuung.setVorgaengerVerfuegung(verfuegungErstgesuch);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(mutierteBetreuung, verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, 80);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, 100);

	}

	@Test
	public void test_Erhoehung_nicht_Rechtzeitig_aenderungVorEingangsdatum_nichtAnMonatsgrenze() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(5).plusDays(15);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.MUTATION);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = monatsRule.createVerfuegungsZeitabschnitte(mutierteBetreuung, zabetrMutiert);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		verfuegungsZeitabschnitteMutiert = splitUpAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum.withDayOfMonth(aenderungsDatumPensum.lengthOfMonth()).plusDays(1), 100);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), BetreuungsangebotTyp.KITA, 100, AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch = monatsRule.createVerfuegungsZeitabschnitte(erstgesuchBetreuung, zabetrErtgesuch);
		setzeAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteErstgesuch, TestDataUtil.START_PERIODE, 80);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		final Gesuch erstgesuch = erstgesuchBetreuung.extractGesuch();
		mutierteBetreuung.setVorgaengerVerfuegung(verfuegungErstgesuch);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = mutationsMerger.createVerfuegungsZeitabschnitte(mutierteBetreuung, verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, 80);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, 100);

	}

	private List<VerfuegungZeitabschnitt> splitUpAnsprechberechtigtesPensumAbDatum(List<VerfuegungZeitabschnitt> zeitabschnitte, LocalDate aenderungsDatumPensum, int ansprechberechtigtesPensum) {

		List<VerfuegungZeitabschnitt> zeitabschnitteSplitted = new ArrayList<>();
		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().endsBefore(aenderungsDatumPensum)).
			forEach(zeitabschnitteSplitted::add);

		VerfuegungZeitabschnitt zeitabschnitToSplit = zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().contains(aenderungsDatumPensum)).findFirst().get();
		VerfuegungZeitabschnitt zeitabschnitSplit1 = new VerfuegungZeitabschnitt(zeitabschnitToSplit);
		zeitabschnitSplit1.getGueltigkeit().setGueltigBis(aenderungsDatumPensum.minusDays(1));
		zeitabschnitteSplitted.add(zeitabschnitSplit1);

		VerfuegungZeitabschnitt zeitabschnitSplit2 = new VerfuegungZeitabschnitt(zeitabschnitToSplit);
		zeitabschnitSplit2.getGueltigkeit().setGueltigAb(aenderungsDatumPensum);
		zeitabschnitSplit2.setAnspruchberechtigtesPensum(ansprechberechtigtesPensum);
		zeitabschnitteSplitted.add(zeitabschnitSplit2);

		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().startsAfter(aenderungsDatumPensum)).
			forEach(zeitabschnitteSplitted::add);

		return zeitabschnitteSplitted;
	}

	private void checkAllBefore(List<VerfuegungZeitabschnitt> zeitabschnitte, LocalDate endsBeforeOrAt, int anspruchberechtigtesPensum) {

		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().endsBefore(endsBeforeOrAt) || za.getGueltigkeit().endsSameDay(endsBeforeOrAt)).
			forEach(za ->
				Assert.assertEquals("Falsches anspruchberechtiges Pensum in Zeitabschnitt " + za, anspruchberechtigtesPensum, za.getAnspruchberechtigtesPensum())
			);
	}

	private void checkAllAfter(List<VerfuegungZeitabschnitt> zeitabschnitte, LocalDate startAfterOrAt, int anspruchberechtigtesPensum) {
		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().startsAfter(startAfterOrAt) || za.getGueltigkeit().startsSameDay(startAfterOrAt)).
			forEach(za ->
				Assert.assertEquals("Falsches anspruchberechtiges Pensum in Zeitabschnitt " + za, anspruchberechtigtesPensum, za.getAnspruchberechtigtesPensum())
			);
	}

	private void setzeAnsprechberechtigtesPensumAbDatum(List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert, LocalDate datumAb, int anspruchberechtigtesPensum) {
		verfuegungsZeitabschnitteMutiert.stream()
			.filter(v -> v.getGueltigkeit().startsSameDay(datumAb) || v.getGueltigkeit().startsAfter(datumAb))
			.forEach(v -> v.setAnspruchberechtigtesPensum(anspruchberechtigtesPensum));
	}

	private Betreuung prepareData(BigDecimal massgebendesEinkommen, BetreuungsangebotTyp angebot, int pensum, AntragTyp antragTyp) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			angebot, pensum, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setTyp(antragTyp);
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		Set<KindContainer> kindContainers = new LinkedHashSet<>();
		final KindContainer kindContainer = betreuung.getKind();
		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		kindContainer.setBetreuungen(betreuungen);
		kindContainers.add(betreuung.getKind());
		gesuch.setKindContainers(kindContainers);

		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.getFinanzDatenDTO().setMassgebendesEinkBjVorAbzFamGr(massgebendesEinkommen);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100, 0));
		return betreuung;
	}
}
