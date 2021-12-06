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
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests fuer Verfügungsmuster
 */
public class MutationsMergerTest {

	private static final boolean IS_DEBUG = false;
	private MonatsRule monatsRule = new MonatsRule(IS_DEBUG);
	private MutationsMerger mutationsMerger = new MutationsMerger(Locale.GERMAN, IS_DEBUG, false);

	@Test
	public void test_Reduktion_Rechtzeitig_aenderungUndEingangsdatumGleich() {

		final LocalDate eingangsdatumMutation = TestDataUtil.START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(6);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			erstgesuchBetreuung,
			zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.setVorgaengerId(erstgesuchBetreuung.getId());
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMutation);
		List<VerfuegungZeitabschnitt> zaBetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zaBetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 80);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

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

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			erstgesuchBetreuung,
			zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.setVorgaengerId(erstgesuchBetreuung.getId());
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);

		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 40);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, 100);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, 40);

	}

	@Test
	public void reduktionRueckwirkendAenderungVorEingangsdatum() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(5).minusDays(1);

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			erstgesuchBetreuung,
			zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.setVorgaengerId(erstgesuchBetreuung.getId());
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 40);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

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
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung();
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

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
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung();
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

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
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung();
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

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
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		verfuegungsZeitabschnitteMutiert = splitUpAnsprechberechtigtesPensumAbDatum(
			verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum, 100);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum
			.withDayOfMonth(aenderungsDatumPensum.lengthOfMonth()).plusDays(1), 100);

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung();
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

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
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 80);
		verfuegungsZeitabschnitteMutiert = splitUpAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert,
			aenderungsDatumPensum, 100);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, aenderungsDatumPensum
			.withDayOfMonth(aenderungsDatumPensum.lengthOfMonth()).plusDays(1), 100);

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung();
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte =
			EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger, mutierteBetreuung,
				verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, 80);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, 100);

	}

	@Test
	public void test_betreuung_besondere_bedurfinisse_aenderungVorEingangsdatum_kein_pauschale_ruckwirkend() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(5).minusDays(1);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 100);
		setBesondereBedurfnisseBestaetigt(verfuegungsZeitabschnitteMutiert);

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung();

		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, 80);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, 100);
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, false);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, true);
	}

	@Test
	public void test_betreuung_besondere_bedurfinisse_aenderungVorEingangsdatum_pauschale_rueckwirkend() {

		final LocalDate eingangsdatumMuation = TestDataUtil.START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = TestDataUtil.START_PERIODE.plusMonths(5).minusDays(1);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, 100);
		setBesondereBedurfnisseBestaetigt(verfuegungsZeitabschnitteMutiert);

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung();

		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		//mutationMerger mit Pauschale Rueckwirkend
		MutationsMerger mutationsMergerMitPauschaleRueckwirkend = new MutationsMerger(Locale.GERMAN, IS_DEBUG, true);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMergerMitPauschaleRueckwirkend,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, 80);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, 100);
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, true);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, true);
	}

	@Test
	public void test_Mutation_nichtZuSpaet() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung();
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		//Mutation pünktlich eingereicht
		mutierteBetreuung.extractGesuch().setEingangsdatum(TestDataUtil.START_PERIODE.minusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(1, zeitabschnitteMutation.size());
		Assert.assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_Flag_zuSpaet_oneMonth() {
		//Erstgesuch pünklich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung();
		//Mutation 15 Tage zu spät
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(TestDataUtil.START_PERIODE.plusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		Assert.assertEquals(2, zeitaschnitteMutation.size());

		VerfuegungZeitabschnitt zeitabschnitt1 = zeitaschnitteMutation.get(0);
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitaschnitteMutation.get(1);

		//Zeitabschnitt1 Flag zuSpät = true, Gültig ab Start der Periode, Gültig bis Ende des 1. Monats
		Assert.assertTrue(zeitabschnitt1.isZuSpaetEingereicht());
		Assert.assertEquals(TestDataUtil.START_PERIODE, zeitabschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.START_PERIODE.with(TemporalAdjusters.lastDayOfMonth()), zeitabschnitt1.getGueltigkeit().getGueltigBis());

		//Zeitabschnitt2 Flag zuSpät = false, Gültig ab 1. Tag des 2. Monats und gültig bis Ende der Periode
		Assert.assertFalse(zeitaschnitteMutation.get(1).isZuSpaetEingereicht());
		Assert.assertEquals(TestDataUtil.START_PERIODE.plusMonths(1), zeitabschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, zeitabschnitt2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void test_Mutation_Flag_zuSpaet_twoMonth() {
		//Erstgesuch pünklich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung();
		//Mutation 1 Monat und 15 Tage zu spät
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(TestDataUtil.START_PERIODE.plusMonths(1).plusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(2, zeitaschnitteMutation.size());
		VerfuegungZeitabschnitt zeitabschnitt1 = zeitaschnitteMutation.get(0);
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitaschnitteMutation.get(1);

		//Zeitabschnitt1 Flag zuSpät = true, Gültig ab Start der Periode, Gültig bis Ende des 2. Monats
		Assert.assertTrue(zeitabschnitt1.isZuSpaetEingereicht());
		Assert.assertEquals(TestDataUtil.START_PERIODE, zeitabschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.START_PERIODE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()), zeitabschnitt1.getGueltigkeit().getGueltigBis());

		//Zeitabschnitt2 Flag zuSpät = false, Gültig ab 1. Tag des 3. Monats und gültig bis Ende der Periode
		Assert.assertFalse(zeitaschnitteMutation.get(1).isZuSpaetEingereicht());
		Assert.assertEquals(TestDataUtil.START_PERIODE.plusMonths(2), zeitabschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, zeitabschnitt2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void test_Mutation_erstGesuch_zu_spaet_keine_Bemerkung() {
		// Zu spät eingereichtes Erstgesuch vorbereiten
		Betreuung erstGesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, 2021, 2022);
		erstGesuchBetreuung.extractGesuch().setEingangsdatum(LocalDate.of(2021, Month.AUGUST, 15));
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstGesuchBetreuung);

		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, erstGesuchBetreuung, zabetrErtgesuch);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstGesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		VerfuegungZeitabschnitt verfuegungZeitabschnittAugust = verfuegungErstgesuch.getZeitabschnitte().get(0);
		Assert.assertTrue(verfuegungZeitabschnittAugust.isZuSpaetEingereicht());
		Assert.assertEquals(0, verfuegungZeitabschnittAugust.getAnspruchberechtigtesPensum());

		Betreuung mutation = prepareMutation(LocalDate.of(2021, Month.OCTOBER, 1), 2020, 2021);
		mutation.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutation);
		VerfuegungZeitabschnitt mutationZeitabschnittAugust = zeitaschnitteMutation.get(0);
		Assert.assertEquals(0, mutationZeitabschnittAugust.getAnspruchberechtigtesPensum());
		Assert.assertNotNull(mutationZeitabschnittAugust.getVerfuegungenZeitabschnittBemerkungenAsString());
		Assert.assertFalse(mutationZeitabschnittAugust.getVerfuegungenZeitabschnittBemerkungenAsString()
			.contains("Ihre Anpassung hat eine Erhöhung des Betreuungsgutscheins zur Folge, die Anpassung erfolgt auf den Folgemonat nach Einreichung aller Belege (Art 34r ASIV)."));
	}

	private Verfuegung prepareErstGesuchVerfuegung() {
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, 2021, 2022);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, erstgesuchBetreuung, zabetrErtgesuch);
		setAnsprechberechtigtesPensumAbDatum(verfuegungsZeitabschnitteErstgesuch, TestDataUtil.START_PERIODE, 80);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		return verfuegungErstgesuch;
	}

	private List<VerfuegungZeitabschnitt> splitUpAnsprechberechtigtesPensumAbDatum(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate aenderungsDatumPensum,
		int ansprechberechtigtesPensum
	) {

		List<VerfuegungZeitabschnitt> zeitabschnitteSplitted = new ArrayList<>();
		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().endsBefore(aenderungsDatumPensum)).
			forEach(zeitabschnitteSplitted::add);

		VerfuegungZeitabschnitt zeitabschnitToSplit = zeitabschnitte.stream()
			.
				filter(za -> za.getGueltigkeit().contains(aenderungsDatumPensum))
			.findFirst()
			.orElseThrow(IllegalArgumentException::new);
		VerfuegungZeitabschnitt zeitabschnitSplit1 = new VerfuegungZeitabschnitt(zeitabschnitToSplit);
		zeitabschnitSplit1.getGueltigkeit().setGueltigBis(aenderungsDatumPensum.minusDays(1));
		zeitabschnitteSplitted.add(zeitabschnitSplit1);

		VerfuegungZeitabschnitt zeitabschnitSplit2 = new VerfuegungZeitabschnitt(zeitabschnitToSplit);
		zeitabschnitSplit2.getGueltigkeit().setGueltigAb(aenderungsDatumPensum);
		zeitabschnitSplit2.getBgCalculationInputAsiv().setAnspruchspensumProzent(ansprechberechtigtesPensum);
		zeitabschnitteSplitted.add(zeitabschnitSplit2);

		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().startsAfter(aenderungsDatumPensum)).
			forEach(zeitabschnitteSplitted::add);

		return zeitabschnitteSplitted;
	}

	private void checkAllBefore(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate endsBeforeOrAt,
		int anspruchberechtigtesPensum) {

		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().endsBefore(endsBeforeOrAt) || za.getGueltigkeit()
				.endsSameDay(endsBeforeOrAt)).
			forEach(za ->
				Assert.assertEquals("Falsches anspruchberechtiges Pensum in Zeitabschnitt " + za,
					anspruchberechtigtesPensum, za.getAnspruchberechtigtesPensum())
			);
	}

	private void checkAllAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate startAfterOrAt,
		int anspruchberechtigtesPensum) {
		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().startsAfter(startAfterOrAt) || za.getGueltigkeit()
				.startsSameDay(startAfterOrAt)).
			forEach(za ->
				Assert.assertEquals("Falsches anspruchberechtiges Pensum in Zeitabschnitt " + za,
					anspruchberechtigtesPensum, za.getAnspruchberechtigtesPensum())
			);
	}

	private void checkAllBefore(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate endsBeforeOrAt,
		boolean besondereBedurfnisseBestaetigt) {

		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().endsBefore(endsBeforeOrAt) || za.getGueltigkeit()
				.endsSameDay(endsBeforeOrAt)).
			forEach(za ->
				Assert.assertEquals("BesondereBedurfnisse sind falsch gesetzt in Zeitabschnitt " + za,
					besondereBedurfnisseBestaetigt, za.isBesondereBeduerfnisseBestaetigt())
			);
	}

	private void checkAllAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate startAfterOrAt,
		boolean besondereBedurfnisseBestaetigt) {
		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().startsAfter(startAfterOrAt) || za.getGueltigkeit()
				.startsSameDay(startAfterOrAt)).
			forEach(za ->
				Assert.assertEquals("BesondereBedurfnisse sind falsch gesetzt in Zeitabschnitt " + za,
					besondereBedurfnisseBestaetigt, za.isBesondereBeduerfnisseBestaetigt())
			);
	}

	private void setAnsprechberechtigtesPensumAbDatum(
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert,
		LocalDate datumAb,
		int anspruchberechtigtesPensum
	) {
		verfuegungsZeitabschnitteMutiert.stream()
			.filter(v -> v.getGueltigkeit().startsSameDay(datumAb) || v.getGueltigkeit().startsAfter(datumAb))
			.forEach(v -> {
				v.getBgCalculationInputAsiv().setAnspruchspensumProzent(anspruchberechtigtesPensum);
				v.getBgCalculationResultAsiv().setAnspruchspensumProzent(anspruchberechtigtesPensum);
			});
	}

	private void setBesondereBedurfnisseBestaetigt(
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert
	) {
		verfuegungsZeitabschnitteMutiert.stream()
			.forEach(v -> {
				v.getBgCalculationInputAsiv().setBesondereBeduerfnisseBestaetigt(true);
			});
	}

	private Betreuung prepareData(BigDecimal massgebendesEinkommen, AntragTyp antragTyp) {
		Betreuung betreuung =
			EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
				BetreuungsangebotTyp.KITA, 100, new BigDecimal(2000));
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
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
			TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));
		return betreuung;
	}

	private Betreuung prepareData(BigDecimal massgebendesEinkommen, AntragTyp antragTyp, int startYear, int endYear) {
		Betreuung betreuung =
			EbeguRuleTestsHelper.createBetreuungWithPensum(LocalDate.of(startYear, Month.AUGUST, 1), LocalDate.of(endYear, Month.JULY, 31),
				BetreuungsangebotTyp.KITA, 100, new BigDecimal(2000));
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
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
				LocalDate.of(startYear, Month.AUGUST, 1), LocalDate.of(endYear, Month.JULY, 31), 100));
		return betreuung;
	}

	@Nonnull
	private Betreuung prepareMutation(@Nonnull LocalDate eingangsdatumMuation) {
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		mutierteBetreuung.initVorgaengerVerfuegungen(null, null);

		return mutierteBetreuung;
	}

	@Nonnull
	private Betreuung prepareMutation(@Nonnull LocalDate eingangsdatumMuation, int startYear, int endYear) {
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, startYear, endYear);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		mutierteBetreuung.initVorgaengerVerfuegungen(null, null);

		return mutierteBetreuung;
	}
}
