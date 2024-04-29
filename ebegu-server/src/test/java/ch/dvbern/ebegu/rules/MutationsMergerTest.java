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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.getMandantLuzern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

/**
 * Tests fuer Verfügungsmuster
 */
public class MutationsMergerTest {

	private static final boolean IS_DEBUG = false;
	private static final int ERWERBSPENSUM_ZUSCHLAG = 20;
	private MonatsRule monatsRule = new MonatsRule(IS_DEBUG);
	private MutationsMerger mutationsMerger = new MutationsMerger(Locale.GERMAN, IS_DEBUG, false);

	private final LocalDate OCTOBER_31 = START_PERIODE.plusMonths(3).minusDays(1);

	private static final BigDecimal MAX_MASGEBENDES_EINKOMMEN = BigDecimal.valueOf(160000);
	private static final int DEFAULT_PENSUM = 80;

	@Test
	public void test_Reduktion_Rechtzeitig_aenderungUndEingangsdatumGleich() {

		final LocalDate eingangsdatumMutation = START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(6);
		int bpPensumVorMutation = 80;
		int bpPensumNachMutation = 70;

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung =
			prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			erstgesuchBetreuung,
			zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung =
			prepareMutation(eingangsdatumMutation, bpPensumNachMutation, aenderungsDatumPensum, bpPensumVorMutation);
		mutierteBetreuung.setVorgaengerId(erstgesuchBetreuung.getId());
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zaBetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zaBetrMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, START_PERIODE.plusMonths(6), bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, START_PERIODE.plusMonths(6), bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);

	}

	@Test
	public void test_Reduktion_Rechtzeitig_aenderungNachEingangsdatum() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(7).plusDays(1);
		int bpPensumVorMutation = 80;
		int bpPensumNachMutation = 20;

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung =
			prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			erstgesuchBetreuung,
			zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung =
			prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum, bpPensumVorMutation);
		mutierteBetreuung.setVorgaengerId(erstgesuchBetreuung.getId());
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);

	}

	@Test
	public void reduktionRueckwirkendAenderungVorEingangsdatum() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(5).minusDays(1);
		int bpPensumVorMutation = 80;
		int bpPensumNachMutation = 20;

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung =
			prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			erstgesuchBetreuung,
			zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung =
			prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum, bpPensumVorMutation);
		mutierteBetreuung.setVorgaengerId(erstgesuchBetreuung.getId());
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);

	}

	@Test
	public void test_Erhoehung_Rechtzeitig_aenderungUndEingangsdatumGleich() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(6);
		int bpPensumVorMutation = 60;
		int bpPensumNachMutation = 80;

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung =
			prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			erstgesuchBetreuung,
			zabetrErtgesuch));
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum,
			bpPensumVorMutation);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);

	}

	@Test
	public void test_Erhoehung_Rechtzeitig_aenderungNachEingangsdatum() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(7).plusDays(1);
		int bpPensumVorMutation = 60;
		int bpPensumNachMutation = 80;

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung(bpPensumVorMutation);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum,
			bpPensumVorMutation);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);

	}

	@Test
	public void test_Erhoehung_Nicht_Rechtzeitig_aenderungVorEingangsdatum() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(5).minusDays(1);
		int bpPensumVorMutation = 60;
		int bpPensumNachMutation = 80;

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung(bpPensumVorMutation);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum, bpPensumVorMutation);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);
	}

	@Test
	public void test_Erhoehung_Rechtzeitig_aenderungNachEingangsdatum_nichtAnMonatsgrenze() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(7).plusDays(15);
		int bpPensumVorMutation = 60;
		int bpPensumNachMutation = 80;
		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung(bpPensumVorMutation);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum,
			bpPensumVorMutation);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, aenderungsDatumPensum, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, aenderungsDatumPensum, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);

	}

	@Test
	public void test_Erhoehung_nicht_Rechtzeitig_aenderungVorEingangsdatum_nichtAnMonatsgrenze() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(5).plusDays(15);
		int bpPensumVorMutation = 60;
		int bpPensumNachMutation = 80;

		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung(bpPensumVorMutation);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum,
			bpPensumVorMutation);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte =
			EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger, mutierteBetreuung,
				verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);

	}

	@Test
	public void test_betreuung_besondere_bedurfinisse_aenderungVorEingangsdatum_kein_pauschale_ruckwirkend() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(5).minusDays(1);
		int bpPensumVorMutation = 60;
		int bpPensumNachMutation = 80;
		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung(bpPensumVorMutation);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum,
			bpPensumVorMutation);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setBesondereBedurfnisseBestaetigt(verfuegungsZeitabschnitteMutiert);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, false);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, true);
	}

	@Test
	public void test_betreuung_besondere_bedurfinisse_aenderungVorEingangsdatum_pauschale_rueckwirkend() {

		final LocalDate eingangsdatumMuation = START_PERIODE.plusMonths(6).minusDays(1);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(5).minusDays(1);
		int bpPensumVorMutation = 60;
		int bpPensumNachMutation = 80;
		// Erstgesuch Gesuch vorbereiten
		Verfuegung verfuegungErstgesuch = prepareErstGesuchVerfuegung(bpPensumVorMutation);

		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMuation, bpPensumNachMutation, aenderungsDatumPensum,
			bpPensumVorMutation);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setBesondereBedurfnisseBestaetigt(verfuegungsZeitabschnitteMutiert);

		//mutationMerger mit Pauschale Rueckwirkend
		MutationsMerger mutationsMergerMitPauschaleRueckwirkend = new MutationsMerger(Locale.GERMAN, IS_DEBUG, true);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMergerMitPauschaleRueckwirkend,
			mutierteBetreuung,
			verfuegungsZeitabschnitteMutiert);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, true);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, true);
	}

	@Test
	public void test_Mutation_nichtZuSpaet() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		//Mutation pünktlich eingereicht
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.minusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(1, zeitabschnitteMutation.size());
		Assert.assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_ErstgesuchZuSpaetMutation_nichtZuSpaet_gutscheinRuckwirkendAngepasst() {
		//Erstgesuch 16 Oktober eingreicht => Anspruch am 17.9.
		Verfuegung verfuegungErstGesuch =
			prepareErstGesuchVerfuegung(START_PERIODE.plusMonths(2).plusDays(15), TestDataUtil.createMandant(
				MandantIdentifier.APPENZELL_AUSSERRHODEN), 20);
		Assert.assertTrue(verfuegungErstGesuch.getZeitabschnitte().get(0).isZuSpaetEingereicht());
		Assert.assertTrue(verfuegungErstGesuch.getZeitabschnitte().get(1).isZuSpaetEingereicht());
		Assert.assertTrue(verfuegungErstGesuch.getZeitabschnitte()
			.get(2)
			.getGueltigkeit()
			.getGueltigAb()
			.isEqual(START_PERIODE.plusMonths(2).plusDays(15).minusDays(30)));
		Assert.assertFalse(verfuegungErstGesuch.getZeitabschnitte().get(2).isZuSpaetEingereicht());
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 30, START_PERIODE);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		//Mutation im August eingereicht, BG soltle von 17.9 zu 1.9 rueckwirkend angepasst werden
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(2, zeitabschnitteMutation.size());
		Assert.assertTrue(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
		Assert.assertFalse(zeitabschnitteMutation.get(1).isZuSpaetEingereicht());
		Assert.assertTrue(zeitabschnitteMutation.get(1).getGueltigkeit().getGueltigAb().isEqual(START_PERIODE.plusMonths(1)));
	}

	@Test
	public void test_Mutation_nichtZuSpaetAR() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(30);
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 20, START_PERIODE);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		//Mutation pünktlich eingereicht
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.minusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(1, zeitabschnitteMutation.size());
		Assert.assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_nichtZuSpaetWegenAlternativDatumAR() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(30);
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 20, START_PERIODE);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		//Mutation zu spät eingereicht
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(45));
		//Alternatives Datum rechtzeitig gesetzt
		mutierteBetreuung.extractGesuch().setRegelnGueltigAb(START_PERIODE.minusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(1, zeitabschnitteMutation.size());
		Assert.assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_30TageVorZeitabschnittStart_nichtZuSpaetAR() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(80);
		//Mutation 15 Tage nach Zeitabschnitt Start pünktlich eingereicht
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 80, START_PERIODE);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.minusDays(30));
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(1, zeitabschnitteMutation.size());
		Assert.assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_31TageNachZeitabschnittStart_zuSpaetAR() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(80);
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 80, START_PERIODE);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Mutation 15 Tage nach Zeitabschnitt Start pünktlich eingereicht
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(31));

		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(2, zeitabschnitteMutation.size());
		Assert.assertTrue(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
		Assert.assertFalse(zeitabschnitteMutation.get(1).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_Flag_zuSpaet_oneMonthAR() {
		Mandant mandant = TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN);
		//Erstgesuch pünklich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(80);
		//Mutation 15 (45-30) Tage zu spät
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(30).plusDays(15));
		mutierteBetreuung.extractGesuch().getFall().setMandant(mandant);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		Assert.assertEquals(2, zeitaschnitteMutation.size());

		VerfuegungZeitabschnitt zeitabschnitt1 = zeitaschnitteMutation.get(0);
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitaschnitteMutation.get(1);

		//Zeitabschnitt1 Flag zuSpät = true, Gültig ab 30 Tage vor Einreichedatum, also Start der Periode, Gültig bis Ende des 1
		// . Monats
		Assert.assertTrue(zeitabschnitt1.isZuSpaetEingereicht());
		Assert.assertEquals(START_PERIODE, zeitabschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(START_PERIODE.plusMonths(2).minusDays(1), zeitabschnitt1.getGueltigkeit().getGueltigBis());

		//Zeitabschnitt2 Flag zuSpät = false, Gültig ab 1. Tag des 2. Monats und gültig bis Ende der Periode
		Assert.assertFalse(zeitabschnitt2.isZuSpaetEingereicht());
		Assert.assertEquals(START_PERIODE.plusMonths(2), zeitabschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			TestDataUtil.ENDE_PERIODE.with(TemporalAdjusters.lastDayOfMonth()),
			zeitabschnitt2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void test_Mutation_Flag_zuSpaet_oneMonth() {
		//Erstgesuch pünklich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(80);
		//Mutation 15 Tage zu spät
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		Assert.assertEquals(2, zeitaschnitteMutation.size());

		VerfuegungZeitabschnitt zeitabschnitt1 = zeitaschnitteMutation.get(0);
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitaschnitteMutation.get(1);

		//Zeitabschnitt1 Flag zuSpät = true, Gültig ab Start der Periode, Gültig bis Ende des 1. Monats
		Assert.assertTrue(zeitabschnitt1.isZuSpaetEingereicht());
		Assert.assertEquals(START_PERIODE, zeitabschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			START_PERIODE.with(TemporalAdjusters.lastDayOfMonth()),
			zeitabschnitt1.getGueltigkeit().getGueltigBis());

		//Zeitabschnitt2 Flag zuSpät = false, Gültig ab 1. Tag des 2. Monats und gültig bis Ende der Periode
		Assert.assertFalse(zeitaschnitteMutation.get(1).isZuSpaetEingereicht());
		Assert.assertEquals(START_PERIODE.plusMonths(1), zeitabschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, zeitabschnitt2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void test_Mutation_Flag_zuSpaet_twoMonth() {
		//Erstgesuch pünklich
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);
		//Mutation 1 Monat und 15 Tage zu spät
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusMonths(1).plusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		Assert.assertEquals(2, zeitaschnitteMutation.size());
		VerfuegungZeitabschnitt zeitabschnitt1 = zeitaschnitteMutation.get(0);
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitaschnitteMutation.get(1);

		//Zeitabschnitt1 Flag zuSpät = true, Gültig ab Start der Periode, Gültig bis Ende des 2. Monats
		Assert.assertTrue(zeitabschnitt1.isZuSpaetEingereicht());
		Assert.assertEquals(START_PERIODE, zeitabschnitt1.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(
			START_PERIODE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()),
			zeitabschnitt1.getGueltigkeit().getGueltigBis());

		//Zeitabschnitt2 Flag zuSpät = false, Gültig ab 1. Tag des 3. Monats und gültig bis Ende der Periode
		Assert.assertFalse(zeitaschnitteMutation.get(1).isZuSpaetEingereicht());
		Assert.assertEquals(START_PERIODE.plusMonths(2), zeitabschnitt2.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(TestDataUtil.ENDE_PERIODE, zeitabschnitt2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void test_Mutation_erstGesuch_zu_spaet_keine_Bemerkung() {
		// Zu spät eingereichtes Erstgesuch vorbereiten
		Betreuung erstGesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		erstGesuchBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(15));
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstGesuchBetreuung);

		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, erstGesuchBetreuung, zabetrErtgesuch);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstGesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		VerfuegungZeitabschnitt verfuegungZeitabschnittAugust = verfuegungErstgesuch.getZeitabschnitte().get(0);
		Assert.assertTrue(verfuegungZeitabschnittAugust.isZuSpaetEingereicht());
		Assert.assertEquals(0, verfuegungZeitabschnittAugust.getAnspruchberechtigtesPensum());

		Betreuung mutation = prepareMutation(START_PERIODE.plusMonths(2), DEFAULT_PENSUM, START_PERIODE, DEFAULT_PENSUM);
		mutation.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutation);
		VerfuegungZeitabschnitt mutationZeitabschnittAugust = zeitaschnitteMutation.get(0);
		Assert.assertEquals(0, mutationZeitabschnittAugust.getAnspruchberechtigtesPensum());
		Assert.assertNotNull(mutationZeitabschnittAugust.getVerfuegungenZeitabschnittBemerkungenAsString());
		Assert.assertFalse(mutationZeitabschnittAugust.getVerfuegungenZeitabschnittBemerkungenAsString()
			.contains(
				"Ihre Anpassung hat eine Erhöhung des Betreuungsgutscheins zur Folge, die Anpassung erfolgt auf den Folgemonat "
					+ "nach Einreichung aller Belege (Art 34r ASIV)."));
	}

	@Test
	public void test_Mutation_AuszahlungAnEltern_keine_Aenderung() {
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusMonths(1));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitteAfterMonatsRule = EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			mutierteBetreuung,
			zeitaschnitteMutation);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			zeitabschnitteAfterMonatsRule);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		zeitabschnitte
			.forEach(zeitabschnitt -> Assert.assertFalse(zeitabschnitt.getRelevantBgCalculationInput().isAuszahlungAnEltern()));
	}

	@Test
	public void test_Mutation_AuszahlungAnEltern_Aenderung_noch_kein_Auszahlung() {
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusMonths(1));
		mutierteBetreuung.setAuszahlungAnEltern(true);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitteAfterMonatsRule = EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			mutierteBetreuung,
			zeitaschnitteMutation);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			zeitabschnitteAfterMonatsRule);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		zeitabschnitte
			.forEach(zeitabschnitt -> Assert.assertTrue(zeitabschnitt.getRelevantBgCalculationInput().isAuszahlungAnEltern()));
	}

	@Test
	public void finSitFKJV_einkommenChanged() {
		//EK ErstGesuch = 50000
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		//EK Mutation = 40000
		LocalDate October31 = START_PERIODE.plusMonths(3).minusDays(1);
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.getGueltigkeit().getGueltigAb().isBefore(October31)) {
				Assert.assertTrue(zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			} else {
				Assert.assertFalse(zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			}
			assertEqualBigDecimal(BigDecimal.valueOf(40000), zeitabschnitt.getMassgebendesEinkommen());
		});
	}

	@Test
	public void finSitFKJV_familiengroesse_steigt() {
		//Erst Gesuch Fam Groesse = 2
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);

		//Mutation per 31.10 FamGroesse = 3
		final KindContainer defaultKindContainer = TestDataUtil.createDefaultKindContainer();
		defaultKindContainer.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		defaultKindContainer.getKindJA().setKinderabzugZweitesHalbjahr(Kinderabzug.GANZER_ABZUG);
		mutierteBetreuung.extractGesuch().getKindContainers().add(defaultKindContainer);

		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		assertEqualBigDecimal(BigDecimal.valueOf(2), findZeitabschnittByMonth(zeitabschnitte, Month.AUGUST).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2), findZeitabschnittByMonth(zeitabschnitte, Month.SEPTEMBER).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2), findZeitabschnittByMonth(zeitabschnitte, Month.OCTOBER).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.NOVEMBER).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.DECEMBER).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.JANUARY).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.FEBRUARY).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.MARCH).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.APRIL).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.MAY).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.JUNE).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.JULY).getFamGroesse());
	}

	@Test
	public void finSitFKJV_familiengroesse_sinkt() {
		//Erst Gesuch Fam Groesse = 3
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		final KindContainer defaultKindContainer = TestDataUtil.createDefaultKindContainer();
		defaultKindContainer.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		defaultKindContainer.getKindJA().setKinderabzugZweitesHalbjahr(Kinderabzug.GANZER_ABZUG);
		erstgesuchBetreuung.extractGesuch().getKindContainers().add(defaultKindContainer);

		Verfuegung verfuegung = prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);

		//Mutation per 31.10 FamGroesse = 3
		final KindContainer defaultKindContainer1 = TestDataUtil.createDefaultKindContainer();
		defaultKindContainer1.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.HALBER_ABZUG);
		defaultKindContainer1.getKindJA().setKinderabzugZweitesHalbjahr(Kinderabzug.HALBER_ABZUG);
		mutierteBetreuung.extractGesuch().getKindContainers().add(defaultKindContainer1);

		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegung, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.AUGUST).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.SEPTEMBER).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(3), findZeitabschnittByMonth(zeitabschnitte, Month.OCTOBER).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.NOVEMBER).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.DECEMBER).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.JANUARY).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.FEBRUARY).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.MARCH).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.APRIL).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.MAY).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.JUNE).getFamGroesse());
		assertEqualBigDecimal(BigDecimal.valueOf(2.5), findZeitabschnittByMonth(zeitabschnitte, Month.JULY).getFamGroesse());
	}

	@Test
	public void finSitFKJV_einkommenNotChanged() {
		//EK ErstGesuch = 50000
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		//EK Mutation = 50000
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte.forEach(zeitabschnitt -> {
			Assert.assertFalse(zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			assertEqualBigDecimal(BigDecimal.valueOf(50000), zeitabschnitt.getMassgebendesEinkommen());
		});
	}

	@Test
	public void finSitFKJV_ekv() {
		//EK ErstGesuch = 50000
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		//EK Mutation = 50000
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);

		EinkommensverschlechterungContainer ekv = TestDataUtil.createDefaultEinkommensverschlechterungsContainer();
		Objects.requireNonNull(mutierteBetreuung.extractGesuch().getGesuchsteller1()).setEinkommensverschlechterungContainer(ekv);

		Gesuch gesuch = mutierteBetreuung.extractGesuch();
		EinkommensverschlechterungInfoContainer einkommensverschlechterungInfo =
			TestDataUtil.createDefaultEinkommensverschlechterungsInfoContainer(gesuch);
		einkommensverschlechterungInfo.getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(true);
		gesuch.setEinkommensverschlechterungInfoContainer(einkommensverschlechterungInfo);

		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		//gueltig per 31.10.
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte.forEach(zeitabschnitt -> {
			Assert.assertFalse(zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
		});

		assertEqualBigDecimal(
			BigDecimal.valueOf(50000),
			findZeitabschnittByMonth(zeitabschnitte, Month.AUGUST).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(50000),
			findZeitabschnittByMonth(zeitabschnitte, Month.SEPTEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(50000),
			findZeitabschnittByMonth(zeitabschnitte, Month.OCTOBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(3),
			findZeitabschnittByMonth(zeitabschnitte, Month.NOVEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(3),
			findZeitabschnittByMonth(zeitabschnitte, Month.DECEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.JANUARY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.FEBRUARY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.MARCH).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.APRIL).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.MAY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.JUNE).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.JULY).getMassgebendesEinkommen());
	}

	@Test
	public void finSitFKJV_einkommenAndEKVChanged() {
		//EK ErstGesuch = 50000
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		//EK Mutation = 40000
		LocalDate October31 = START_PERIODE.plusMonths(3).minusDays(1);
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);

		EinkommensverschlechterungContainer ekv = TestDataUtil.createDefaultEinkommensverschlechterungsContainer();
		Objects.requireNonNull(mutierteBetreuung.extractGesuch().getGesuchsteller1()).setEinkommensverschlechterungContainer(ekv);

		Gesuch gesuch = mutierteBetreuung.extractGesuch();
		gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
		EinkommensverschlechterungInfoContainer einkommensverschlechterungInfo =
			TestDataUtil.createDefaultEinkommensverschlechterungsInfoContainer(gesuch);
		einkommensverschlechterungInfo.getEinkommensverschlechterungInfoJA().setEkvFuerBasisJahrPlus2(true);
		gesuch.setEinkommensverschlechterungInfoContainer(einkommensverschlechterungInfo);

		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(October31);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.getGueltigkeit().getGueltigAb().isBefore(October31)) {
				Assert.assertTrue(zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			} else {
				Assert.assertFalse(zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			}
		});

		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.AUGUST).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.SEPTEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.OCTOBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(3),
			findZeitabschnittByMonth(zeitabschnitte, Month.NOVEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(3),
			findZeitabschnittByMonth(zeitabschnitte, Month.DECEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.JANUARY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.FEBRUARY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.MARCH).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.APRIL).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.MAY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.JUNE).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(4),
			findZeitabschnittByMonth(zeitabschnitte, Month.JULY).getMassgebendesEinkommen());
	}

	@Test
	public void finSitGueltigAbSetNotFKJV() {
		//EK ErstGesuch = 50000
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		//EK Mutation = 40000 ab 31.10.
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		mutierteBetreuung.extractGesuch()
			.setFinSitAenderungGueltigAbDatum(START_PERIODE.plusMonths(1).minusDays(1)); //31.08
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31); //FinSit GueltigAb 31.10

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte.forEach(zeitabschnitt ->
			Assert.assertFalse(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST)));

		// EK bis Oktober 50000 ab Oktober 40000
		assertEqualBigDecimal(
			BigDecimal.valueOf(50000),
			findZeitabschnittByMonth(zeitabschnitte, Month.AUGUST).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(50000),
			findZeitabschnittByMonth(zeitabschnitte, Month.SEPTEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(50000),
			findZeitabschnittByMonth(zeitabschnitte, Month.OCTOBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.NOVEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.DECEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.JANUARY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.FEBRUARY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.MARCH).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.APRIL).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.MAY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.JUNE).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.JULY).getMassgebendesEinkommen());
	}

	@Test
	public void mutationAenderungToVerguenstigungBeantragt() {
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(160000), AntragTyp.ERSTGESUCH);
		erstgesuchBetreuung.extractGesuch()
			.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setVerguenstigungGewuenscht(false);
		Verfuegung verfuegungErstGesuch = prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		//EK Mutation = 40000 ab 31.10.
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		mutierteBetreuung.extractGesuch()
			.setFinSitAenderungGueltigAbDatum(START_PERIODE.plusMonths(1).minusDays(1)); //31.08
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31); //FinSit GueltigAb 31.10

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		// EK bis Oktober 50000 ab Oktober 40000
		assertEqualBigDecimal(
			MAX_MASGEBENDES_EINKOMMEN,
			findZeitabschnittByMonth(zeitabschnitte, Month.AUGUST).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			MAX_MASGEBENDES_EINKOMMEN,
			findZeitabschnittByMonth(zeitabschnitte, Month.SEPTEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			MAX_MASGEBENDES_EINKOMMEN,
			findZeitabschnittByMonth(zeitabschnitte, Month.OCTOBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.NOVEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.DECEMBER).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.JANUARY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.FEBRUARY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.MARCH).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.APRIL).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.MAY).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.JUNE).getMassgebendesEinkommen());
		assertEqualBigDecimal(
			BigDecimal.valueOf(40000),
			findZeitabschnittByMonth(zeitabschnitte, Month.JULY).getMassgebendesEinkommen());
	}

	@Test
	public void mutationAenderungToNoVerguenstigungBeantragt() {
		//EK ErstGesuch = 50000
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		//EK Mutation = 40000 ab 31.10.
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch()
			.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setVerguenstigungGewuenscht(false);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		mutierteBetreuung.extractGesuch()
			.setFinSitAenderungGueltigAbDatum(START_PERIODE.plusMonths(1).minusDays(1)); //31.08
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31); //FinSit GueltigAb 31.10

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte
			.forEach(zeitabschnitt -> assertEqualBigDecimal(MAX_MASGEBENDES_EINKOMMEN,
				zeitabschnitt.getMassgebendesEinkommen()));
	}

	private void assertEqualBigDecimal(@Nonnull BigDecimal expected, @Nullable BigDecimal actual) {
		Assert.assertNotNull(actual);
		Assert.assertEquals(expected.stripTrailingZeros(), actual.stripTrailingZeros());
	}

	@Test
	public void test_Mutation_AuszahlungAnEltern_Aenderung_mit_Auszahlung() {
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);
		VerfuegungZeitabschnitt verfuegterZaAugust =
			findZeitabschnittByMonth(verfuegungErstGesuch.getZeitabschnitte(), Month.AUGUST);
		verfuegterZaAugust.setZahlungsstatusInstitution(VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET);

		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusMonths(1));
		mutierteBetreuung.setAuszahlungAnEltern(true);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		// mergen
		List<VerfuegungZeitabschnitt> zeitabschnitteAfterMonatsRule = EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			mutierteBetreuung,
			zeitaschnitteMutation);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			mutierteBetreuung,
			zeitabschnitteAfterMonatsRule);

		//ueberprüfen
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());

		Assert.assertFalse(findZeitabschnittByMonth(zeitabschnitte, Month.AUGUST).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.SEPTEMBER).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.OCTOBER).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.NOVEMBER).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.DECEMBER).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.JANUARY).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.FEBRUARY).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.MARCH).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.APRIL).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.MAY).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.JUNE).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		Assert.assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.JULY).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
	}

	@Test
	public void test_Mutation_Tagesschule_ASIV_Gemeinde_zu_spaet_eingereicht() {
		// Erst Gesuch mit Anmeldung und Verfuegung vorbereiten
		Verfuegung ersteVerfuegung =
			prepareErstTagesschuleGesuchVerfuegung(TestDataUtil.START_PERIODE, TestDataUtil.createMandant(
				MandantIdentifier.BERN));
		AnmeldungTagesschule anmeldungTagesschule = TestDataUtil.createGesuchWithAnmeldungTagesschule();
		anmeldungTagesschule.extractGesuch().setTyp(AntragTyp.MUTATION);
		anmeldungTagesschule.initVorgaengerVerfuegungen(ersteVerfuegung, null);
		anmeldungTagesschule.setVorgaengerId(ersteVerfuegung.getAnmeldungTagesschule().getId());
		anmeldungTagesschule.extractGesuch().setEingangsdatum(TestDataUtil.START_PERIODE.plusMonths(1));

		// Noetige Rules durchfuehren
		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(anmeldungTagesschule);
		List<VerfuegungZeitabschnitt> zeitabschnitteAfterMonatsRule = EbeguRuleTestsHelper.runSingleAbschlussRule(
			monatsRule,
			anmeldungTagesschule,
			zeitaschnitteMutation);
		zeitabschnitteAfterMonatsRule.forEach(verfuegungZeitabschnitt -> {
			verfuegungZeitabschnitt.setHasGemeindeSpezifischeBerechnung(true);
			verfuegungZeitabschnitt.setBgCalculationResultGemeinde(new BGCalculationResult());
		});
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			mutationsMerger,
			anmeldungTagesschule,
			zeitabschnitteAfterMonatsRule);
		// der erste Zeitabschnitt sollte zu spaet Eingereicht werden, wie im erst Gesuch
		// die andere sollten nicht zu spaet eingereicht werden wegen der MutationsMerger,
		// nur die die im erst Antrag zu Spaet eingereicht waren mussen weiter zu spaet eingereicht werden und das beim ASIV und
		// Gemeinde Result
		Assert.assertTrue(zeitabschnitte.get(0).getBgCalculationResultAsiv().isZuSpaetEingereicht());
		Assert.assertFalse(zeitabschnitte.get(1).getBgCalculationResultAsiv().isZuSpaetEingereicht());
		Assert.assertTrue(zeitabschnitte.get(0).getBgCalculationResultGemeinde().isZuSpaetEingereicht());
		Assert.assertFalse(zeitabschnitte.get(1).getBgCalculationResultGemeinde().isZuSpaetEingereicht());
	}

	private VerfuegungZeitabschnitt findZeitabschnittByMonth(List<VerfuegungZeitabschnitt> zeitabschnittList, Month month) {
		return zeitabschnittList
			.stream()
			.filter(zeitabschnitt -> zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == month)
			.findFirst()
			.orElseThrow(() -> new EbeguRuntimeException(
				"findZeitabschnittByMonth",
				"Kein Zeitabschnitt für diesen Monat gefunden"));
	}

	@Test
	public void mutationFinSitAbgehelnt_ErstantragNichtAbgelehnt() {
		//EK 50000, Anspruch-Pensum 100%
		Verfuegung verfuegungErstGesuch = prepareErstGesuchVerfuegung(DEFAULT_PENSUM);

		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setFinSitStatus(FinSitStatus.ABGELEHNT);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte
			.forEach(zeitabschnitt -> {
				assertEqualBigDecimal(BigDecimal.valueOf(50000), zeitabschnitt.getMassgebendesEinkommen());
				assertThat(zeitabschnitt.getAnspruchberechtigtesPensum(), is(100));
			});
	}

	@Test
	public void mutationFinSitAbgehelnt_ErstantragAbgelehnt() {
		//EK 50000 Anspruch-Pensum 100% (80% + 20 Zuschlag)
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		erstgesuchBetreuung.extractGesuch().setFinSitStatus(FinSitStatus.ABGELEHNT);
		Verfuegung verfuegungErstGesuch = prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		verfuegungErstGesuch.getZeitabschnitte().forEach(zeitabschnitt -> {
			assertThat(zeitabschnitt.getAnspruchberechtigtesPensum(), is(0));
			assertEqualBigDecimal(MAX_MASGEBENDES_EINKOMMEN, zeitabschnitt.getMassgebendesEinkommen());
		});

		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setFinSitStatus(FinSitStatus.ABGELEHNT);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte
			.forEach(zeitabschnitt -> {
				assertEqualBigDecimal(MAX_MASGEBENDES_EINKOMMEN, zeitabschnitt.getMassgebendesEinkommen());
			});
	}

	@Test
	public void mutationFinSitAbgehelnt_pensumErhoeht() {
		int bpPensumVorMutation = 40;
		int bpPensumNachMutation = 80;
		//Erstrantrag Anspruchpensum 60%, FinSit akzeptiert
		Betreuung erstgesuchBetreuung =
			prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		Verfuegung verfuegungErstGesuch = prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		//Mutaiton per 1.9., Anspruchpensum 100%, FinSitAbgehlent
		Betreuung mutierteBetreuung =
			prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION, bpPensumNachMutation, START_PERIODE);
		mutierteBetreuung.extractGesuch().setFinSitStatus(FinSitStatus.ABGELEHNT);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusMonths(1));

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		checkAllBefore(
			zeitabschnitte,
			START_PERIODE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()),
			bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(
			zeitabschnitte,
			START_PERIODE.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()),
			bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);
	}

	@Test
	public void mutationAnpassungAnspruchSteigtLuzern() {
		int bpPensumVorMutation = 40;
		int bpPensumNachMutation = 60;
		//Erstrantrag Anspruchpensum 60% (40% Pensum + 20% Zuschlag)
		Betreuung erstgesuchBetreuung =
			prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		erstgesuchBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantLuzern());
		Verfuegung verfuegungErstGesuch = prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		//Mutaiton per 1.9., Anspruchpensum 80% (60% + 20% zuschlag)
		Betreuung mutierteBetreuung =
			prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION, bpPensumNachMutation, START_PERIODE);
		mutierteBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantLuzern());
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusMonths(1));

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		checkAllBefore(
			zeitabschnitte,
			START_PERIODE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()),
			bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(
			zeitabschnitte,
			START_PERIODE.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()),
			bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);
	}

	@Test
	public void mutationAnpassungAnspruchSinktLuzern() {
		int bpPensumVorMutation = 40;
		int bpPensumNachMutation = 20;
		//Erstrantrag Anspruchpensum 60% (40% Pensum + 20% Zuschlag)
		Betreuung erstgesuchBetreuung =
			prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		erstgesuchBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantLuzern());
		Verfuegung verfuegungErstGesuch = prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		//Mutaiton per 1.9., Anspruchpensum 40% (20% Pensum + 20% Zuschlag)
		Betreuung mutierteBetreuung =
			prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION, bpPensumNachMutation, START_PERIODE);
		mutierteBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantLuzern());
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusMonths(1));

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		checkAllBefore(
			zeitabschnitte,
			START_PERIODE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()),
			bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(
			zeitabschnitte,
			START_PERIODE.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth()),
			bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);
	}

	private void setErwerbspensumContainer(Betreuung betreuung, int pensum) {
		betreuung.extractGesuch().getGesuchsteller1().getErwerbspensenContainers().clear();
		betreuung.extractGesuch().getGesuchsteller1().addErwerbspensumContainer
			(TestDataUtil.createErwerbspensum(START_PERIODE, TestDataUtil.ENDE_PERIODE, pensum));
	}

	private Verfuegung prepareErstGesuchVerfuegung(int pbPensum) {
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, pbPensum, START_PERIODE);
		return prepareVerfuegungForBetreuung(erstgesuchBetreuung);
	}

	private Verfuegung prepareErstGesuchVerfuegung(LocalDate eingangsdatum, Mandant mandantAR, int bpPensum) {
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensum, START_PERIODE);
		erstgesuchBetreuung.extractGesuch().setEingangsdatum(eingangsdatum);
		erstgesuchBetreuung.extractGesuch().getFall().setMandant(mandantAR);
		return prepareVerfuegungForBetreuung(erstgesuchBetreuung);
	}

	private Verfuegung prepareVerfuegungForBetreuung(Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(betreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, betreuung, zabetrErtgesuch);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		betreuung.setVerfuegung(verfuegungErstgesuch);
		betreuung.extractGesuch().setTimestampVerfuegt(LocalDateTime.now());
		verfuegungErstgesuch.setBetreuung(betreuung);
		return verfuegungErstgesuch;
	}

	private Verfuegung prepareErstTagesschuleGesuchVerfuegung(LocalDate eingangsdatum, Mandant mandantAR) {
		AnmeldungTagesschule anmeldungTagesschule = TestDataUtil.createGesuchWithAnmeldungTagesschule();
		anmeldungTagesschule.extractGesuch().setEingangsdatum(eingangsdatum);
		anmeldungTagesschule.extractGesuch().getFall().setMandant(mandantAR);
		return prepareVerfuegungForTagesschuleMitGemeindeResult(anmeldungTagesschule);
	}

	private Verfuegung prepareVerfuegungForTagesschuleMitGemeindeResult(AnmeldungTagesschule anmeldungTagesschule) {
		List<VerfuegungZeitabschnitt> zeitabschnittsErstGesuch = EbeguRuleTestsHelper.calculate(anmeldungTagesschule);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, anmeldungTagesschule, zeitabschnittsErstGesuch);
		verfuegungsZeitabschnitteErstgesuch.forEach(verfuegungZeitabschnitt -> {
			verfuegungZeitabschnitt.setHasGemeindeSpezifischeBerechnung(true);
			verfuegungZeitabschnitt.setBgCalculationResultGemeinde(new BGCalculationResult());
		});
		verfuegungsZeitabschnitteErstgesuch.stream()
			.filter(verfuegungZeitabschnitt -> verfuegungZeitabschnitt.getBgCalculationResultAsiv().isZuSpaetEingereicht())
			.forEach(
				verfuegungZeitabschnitt -> verfuegungZeitabschnitt.getBgCalculationResultGemeinde().setZuSpaetEingereicht(true)
			);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		anmeldungTagesschule.setVerfuegung(verfuegungErstgesuch);
		verfuegungErstgesuch.setAnmeldungTagesschule(anmeldungTagesschule);
		return verfuegungErstgesuch;
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

	private void setBesondereBedurfnisseBestaetigt(
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert
	) {
		verfuegungsZeitabschnitteMutiert.stream()
			.forEach(v -> {
				v.getBgCalculationInputAsiv().setBesondereBeduerfnisseBestaetigt(true);
			});
	}

	private Betreuung prepareData(BigDecimal massgebendesEinkommen, AntragTyp antragTyp) {
		return prepareData(massgebendesEinkommen, antragTyp, DEFAULT_PENSUM, START_PERIODE);
	}

	private Betreuung prepareData(
		BigDecimal massgebendesEinkommen,
		AntragTyp antragTyp,
		int bpPensum,
		LocalDate aenderungsDatumBpPensum) {
		Betreuung betreuung =
			EbeguRuleTestsHelper.createBetreuungWithPensum(START_PERIODE, TestDataUtil.ENDE_PERIODE,
				BetreuungsangebotTyp.KITA, 100, new BigDecimal(2000));
		final Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setTyp(antragTyp);
		Set<KindContainer> kindContainers = new LinkedHashSet<>();
		final KindContainer kindContainer = betreuung.getKind();
		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		kindContainer.setBetreuungen(betreuungen);
		kindContainers.add(betreuung.getKind());
		gesuch.setKindContainers(kindContainers);

		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		gesuch.getFinanzDatenDTO().setMassgebendesEinkBjVorAbzFamGr(massgebendesEinkommen);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
			aenderungsDatumBpPensum, TestDataUtil.ENDE_PERIODE, bpPensum));

		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettolohn(massgebendesEinkommen);
		return betreuung;
	}

	@Nonnull
	private Betreuung prepareMutation(@Nonnull LocalDate eingangsdatumMuation, int bpPensum, LocalDate aenderungsDatumPensum,
		int bpPensumVorMutation) {
		Betreuung mutierteBetreuung =
			prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, bpPensum, aenderungsDatumPensum);
		mutierteBetreuung.extractGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
			START_PERIODE, aenderungsDatumPensum.minusDays(1), bpPensumVorMutation));
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		return mutierteBetreuung;
	}
}
