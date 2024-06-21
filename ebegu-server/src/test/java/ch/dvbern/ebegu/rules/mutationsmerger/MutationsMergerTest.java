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

package ch.dvbern.ebegu.rules.mutationsmerger;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.rules.MonatsRule;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static ch.dvbern.ebegu.enums.MsgKey.BEDARFSSTUFE_AENDERUNG_MSG;
import static ch.dvbern.ebegu.enums.MsgKey.BEDARFSSTUFE_MSG;
import static ch.dvbern.ebegu.enums.MsgKey.BEDARFSSTUFE_NICHT_GEWAEHRT_MSG;
import static ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe.BEDARFSSTUFE_1;
import static ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe.BEDARFSSTUFE_2;
import static ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe.KEINE;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.getMandantLuzern;
import static ch.dvbern.ebegu.test.TestDataUtil.getMandantSchwyz;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

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

	private static final BigDecimal DEFAULT_MASGEBENDES_EINKOMMEN = BigDecimal.valueOf(50000);
	private static final int DEFAULT_PENSUM = 80;

	@Test
	public void test_Reduktion_Rechtzeitig_aenderungUndEingangsdatumGleich() {

		final LocalDate eingangsdatumMutation = START_PERIODE.plusMonths(6);
		final LocalDate aenderungsDatumPensum = START_PERIODE.plusMonths(6);
		int bpPensumVorMutation = 80;
		int bpPensumNachMutation = 70;

		// Erstgesuch Gesuch vorbereiten
		Betreuung erstgesuchBetreuung =
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
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
		assertNotNull(zeitabschnitte);
		assertEquals(12, zeitabschnitte.size());
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
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
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
		assertNotNull(zeitabschnitte);
		assertEquals(12, zeitabschnitte.size());
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
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
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
		assertNotNull(zeitabschnitte);
		assertEquals(12, zeitabschnitte.size());
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
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
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
		assertNotNull(zeitabschnitte);
		assertEquals(12, zeitabschnitte.size());
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
		Verfuegung verfuegungErstgesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(bpPensumVorMutation, DEFAULT_MASGEBENDES_EINKOMMEN);

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
		assertNotNull(zeitabschnitte);
		assertEquals(13, zeitabschnitte.size());
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
		Verfuegung verfuegungErstgesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(bpPensumVorMutation, DEFAULT_MASGEBENDES_EINKOMMEN);

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
		assertNotNull(zeitabschnitte);
		assertEquals(13, zeitabschnitte.size());
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
		Verfuegung verfuegungErstgesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(bpPensumVorMutation,DEFAULT_MASGEBENDES_EINKOMMEN);

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
		assertNotNull(zeitabschnitte);
		assertEquals(13, zeitabschnitte.size());
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
		Verfuegung verfuegungErstgesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(bpPensumVorMutation,DEFAULT_MASGEBENDES_EINKOMMEN);

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
		assertNotNull(zeitabschnitte);
		assertEquals(13, zeitabschnitte.size());
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
		Verfuegung verfuegungErstgesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(bpPensumVorMutation,DEFAULT_MASGEBENDES_EINKOMMEN);

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
		assertNotNull(zeitabschnitte);
		assertEquals(13, zeitabschnitte.size());
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
		Verfuegung verfuegungErstgesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(bpPensumVorMutation,DEFAULT_MASGEBENDES_EINKOMMEN);

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
		assertNotNull(zeitabschnitte);
		assertEquals(13, zeitabschnitte.size());
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, bpPensumVorMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, bpPensumNachMutation + ERWERBSPENSUM_ZUSCHLAG);
		checkAllBefore(zeitabschnitte, eingangsdatumMuation, true);
		checkAllAfter(zeitabschnitte, eingangsdatumMuation, true);
	}

	@Test
	public void test_Mutation_nichtZuSpaet() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		//Mutation pünktlich eingereicht
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.minusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		assertEquals(1, zeitabschnitteMutation.size());
		assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_ErstgesuchZuSpaetMutation_nichtZuSpaet_gutscheinRuckwirkendAngepasst() {
		//Erstgesuch 16 Oktober eingreicht => Anspruch am 17.9.
		Verfuegung verfuegungErstGesuch =
			prepareErstGesuchVerfuegung(START_PERIODE.plusMonths(2).plusDays(15), TestDataUtil.createMandant(
				MandantIdentifier.APPENZELL_AUSSERRHODEN), 20);
		assertTrue(verfuegungErstGesuch.getZeitabschnitte().get(0).isZuSpaetEingereicht());
		assertTrue(verfuegungErstGesuch.getZeitabschnitte().get(1).isZuSpaetEingereicht());
		assertTrue(verfuegungErstGesuch.getZeitabschnitte()
			.get(2)
			.getGueltigkeit()
			.getGueltigAb()
			.isEqual(START_PERIODE.plusMonths(2).plusDays(15).minusDays(30)));
		assertFalse(verfuegungErstGesuch.getZeitabschnitte().get(2).isZuSpaetEingereicht());
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 30, START_PERIODE);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		//Mutation im August eingereicht, BG soltle von 17.9 zu 1.9 rueckwirkend angepasst werden
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		assertEquals(2, zeitabschnitteMutation.size());
		assertTrue(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
		assertFalse(zeitabschnitteMutation.get(1).isZuSpaetEingereicht());
		assertTrue(zeitabschnitteMutation.get(1).getGueltigkeit().getGueltigAb().isEqual(START_PERIODE.plusMonths(1)));
	}

	@Test
	public void test_Mutation_nichtZuSpaetAR() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil. prepareErstGesuchVerfuegung(80, DEFAULT_MASGEBENDES_EINKOMMEN);
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 20, START_PERIODE);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		//Mutation pünktlich eingereicht
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.minusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		assertEquals(1, zeitabschnitteMutation.size());
		assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_nichtZuSpaetWegenAlternativDatumAR() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil. prepareErstGesuchVerfuegung(80, DEFAULT_MASGEBENDES_EINKOMMEN);
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 20, START_PERIODE);
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
		assertEquals(1, zeitabschnitteMutation.size());
		assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_30TageVorZeitabschnittStart_nichtZuSpaetAR() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil. prepareErstGesuchVerfuegung(80, DEFAULT_MASGEBENDES_EINKOMMEN);
		//Mutation 15 Tage nach Zeitabschnitt Start pünktlich eingereicht
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 80, START_PERIODE);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.minusDays(30));
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		assertEquals(1, zeitabschnitteMutation.size());
		assertFalse(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_31TageNachZeitabschnittStart_zuSpaetAR() {
		//Erstgesuch pünktlich
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil. prepareErstGesuchVerfuegung(80, DEFAULT_MASGEBENDES_EINKOMMEN);
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, 80, START_PERIODE);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		//Mutation 15 Tage nach Zeitabschnitt Start pünktlich eingereicht
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(31));

		//Zeitabschnitt Flag zuSpät = false
		List<VerfuegungZeitabschnitt> zeitabschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		assertEquals(2, zeitabschnitteMutation.size());
		assertTrue(zeitabschnitteMutation.get(0).isZuSpaetEingereicht());
		assertFalse(zeitabschnitteMutation.get(1).isZuSpaetEingereicht());
	}

	@Test
	public void test_Mutation_Flag_zuSpaet_oneMonthAR() {
		Mandant mandant = TestDataUtil.createMandant(MandantIdentifier.APPENZELL_AUSSERRHODEN);
		//Erstgesuch pünklich
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil. prepareErstGesuchVerfuegung(80, DEFAULT_MASGEBENDES_EINKOMMEN);
		//Mutation 15 (45-30) Tage zu spät
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(30).plusDays(15));
		mutierteBetreuung.extractGesuch().getFall().setMandant(mandant);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		assertEquals(2, zeitaschnitteMutation.size());

		VerfuegungZeitabschnitt zeitabschnitt1 = zeitaschnitteMutation.get(0);
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitaschnitteMutation.get(1);

		//Zeitabschnitt1 Flag zuSpät = true, Gültig ab 30 Tage vor Einreichedatum, also Start der Periode, Gültig bis Ende des 1
		// . Monats
		assertTrue(zeitabschnitt1.isZuSpaetEingereicht());
		assertEquals(START_PERIODE, zeitabschnitt1.getGueltigkeit().getGueltigAb());
		assertEquals(START_PERIODE.plusMonths(2).minusDays(1), zeitabschnitt1.getGueltigkeit().getGueltigBis());

		//Zeitabschnitt2 Flag zuSpät = false, Gültig ab 1. Tag des 2. Monats und gültig bis Ende der Periode
		assertFalse(zeitabschnitt2.isZuSpaetEingereicht());
		assertEquals(START_PERIODE.plusMonths(2), zeitabschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(
			TestDataUtil.ENDE_PERIODE.with(TemporalAdjusters.lastDayOfMonth()),
			zeitabschnitt2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void test_Mutation_Flag_zuSpaet_oneMonth() {
		//Erstgesuch pünklich
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(80, DEFAULT_MASGEBENDES_EINKOMMEN);
		//Mutation 15 Tage zu spät
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		assertEquals(2, zeitaschnitteMutation.size());

		VerfuegungZeitabschnitt zeitabschnitt1 = zeitaschnitteMutation.get(0);
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitaschnitteMutation.get(1);

		//Zeitabschnitt1 Flag zuSpät = true, Gültig ab Start der Periode, Gültig bis Ende des 1. Monats
		assertTrue(zeitabschnitt1.isZuSpaetEingereicht());
		assertEquals(START_PERIODE, zeitabschnitt1.getGueltigkeit().getGueltigAb());
		assertEquals(
			START_PERIODE.with(TemporalAdjusters.lastDayOfMonth()),
			zeitabschnitt1.getGueltigkeit().getGueltigBis());

		//Zeitabschnitt2 Flag zuSpät = false, Gültig ab 1. Tag des 2. Monats und gültig bis Ende der Periode
		assertFalse(zeitaschnitteMutation.get(1).isZuSpaetEingereicht());
		assertEquals(START_PERIODE.plusMonths(1), zeitabschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(TestDataUtil.ENDE_PERIODE, zeitabschnitt2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void test_Mutation_Flag_zuSpaet_twoMonth() {
		//Erstgesuch pünklich
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);
		//Mutation 1 Monat und 15 Tage zu spät
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusMonths(1).plusDays(15));
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		assertEquals(2, zeitaschnitteMutation.size());
		VerfuegungZeitabschnitt zeitabschnitt1 = zeitaschnitteMutation.get(0);
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitaschnitteMutation.get(1);

		//Zeitabschnitt1 Flag zuSpät = true, Gültig ab Start der Periode, Gültig bis Ende des 2. Monats
		assertTrue(zeitabschnitt1.isZuSpaetEingereicht());
		assertEquals(START_PERIODE, zeitabschnitt1.getGueltigkeit().getGueltigAb());
		assertEquals(
			START_PERIODE.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()),
			zeitabschnitt1.getGueltigkeit().getGueltigBis());

		//Zeitabschnitt2 Flag zuSpät = false, Gültig ab 1. Tag des 3. Monats und gültig bis Ende der Periode
		assertFalse(zeitaschnitteMutation.get(1).isZuSpaetEingereicht());
		assertEquals(START_PERIODE.plusMonths(2), zeitabschnitt2.getGueltigkeit().getGueltigAb());
		assertEquals(TestDataUtil.ENDE_PERIODE, zeitabschnitt2.getGueltigkeit().getGueltigBis());
	}

	@Test
	public void test_Mutation_erstGesuch_zu_spaet_keine_Bemerkung() {
		// Zu spät eingereichtes Erstgesuch vorbereiten
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		erstGesuchBetreuung.extractGesuch().setEingangsdatum(START_PERIODE.plusDays(15));
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstGesuchBetreuung);

		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch =
			EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, erstGesuchBetreuung, zabetrErtgesuch);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		erstGesuchBetreuung.setVerfuegung(verfuegungErstgesuch);

		VerfuegungZeitabschnitt verfuegungZeitabschnittAugust = verfuegungErstgesuch.getZeitabschnitte().get(0);
		assertTrue(verfuegungZeitabschnittAugust.isZuSpaetEingereicht());
		assertEquals(0, verfuegungZeitabschnittAugust.getAnspruchberechtigtesPensum());

		Betreuung mutation = prepareMutation(START_PERIODE.plusMonths(2), DEFAULT_PENSUM, START_PERIODE, DEFAULT_PENSUM);
		mutation.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);

		List<VerfuegungZeitabschnitt> zeitaschnitteMutation = EbeguRuleTestsHelper.calculate(mutation);
		VerfuegungZeitabschnitt mutationZeitabschnittAugust = zeitaschnitteMutation.get(0);
		assertEquals(0, mutationZeitabschnittAugust.getAnspruchberechtigtesPensum());
		assertNotNull(mutationZeitabschnittAugust.getVerfuegungenZeitabschnittBemerkungenAsString());
		assertFalse(mutationZeitabschnittAugust.getVerfuegungenZeitabschnittBemerkungenAsString()
			.contains(
				"Ihre Anpassung hat eine Erhöhung des Betreuungsgutscheins zur Folge, die Anpassung erfolgt auf den Folgemonat "
					+ "nach Einreichung aller Belege (Art 34r ASIV)."));
	}

	@Test
	public void test_Mutation_AuszahlungAnEltern_keine_Aenderung() {
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
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
		assertNotNull(zeitabschnitte);
		assertEquals(12, zeitabschnitte.size());
		zeitabschnitte
			.forEach(zeitabschnitt -> assertFalse(zeitabschnitt.getRelevantBgCalculationInput().isAuszahlungAnEltern()));
	}

	@Test
	public void test_Mutation_AuszahlungAnEltern_Aenderung_noch_kein_Auszahlung() {
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
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
		assertNotNull(zeitabschnitte);
		assertEquals(12, zeitabschnitte.size());
		zeitabschnitte
			.forEach(zeitabschnitt -> assertTrue(zeitabschnitt.getRelevantBgCalculationInput().isAuszahlungAnEltern()));
	}

	@Test
	public void finSitFKJV_einkommenChanged() {
		//EK ErstGesuch = 50000
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		//EK Mutation = 40000
		LocalDate October31 = START_PERIODE.plusMonths(3).minusDays(1);
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.getGueltigkeit().getGueltigAb().isBefore(October31)) {
				assertTrue(zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			} else {
				assertFalse(zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			}
			assertEqualBigDecimal(BigDecimal.valueOf(40000), zeitabschnitt.getMassgebendesEinkommen());
		});
	}

	@Test
	public void finSitFKJV_familiengroesse_steigt() {
		//Erst Gesuch Fam Groesse = 2
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);

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
		Betreuung erstgesuchBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		final KindContainer defaultKindContainer = TestDataUtil.createDefaultKindContainer();
		defaultKindContainer.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		defaultKindContainer.getKindJA().setKinderabzugZweitesHalbjahr(Kinderabzug.GANZER_ABZUG);
		erstgesuchBetreuung.extractGesuch().getKindContainers().add(defaultKindContainer);

		Verfuegung verfuegung = MutationsMergerTestUtil. prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);

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
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		//EK Mutation = 50000
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte.forEach(zeitabschnitt -> {
			assertFalse(zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			assertEqualBigDecimal(BigDecimal.valueOf(50000), zeitabschnitt.getMassgebendesEinkommen());
		});
	}

	@Test
	public void finSitFKJV_ekv() {
		//EK ErstGesuch = 50000
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		//EK Mutation = 50000
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);

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
			assertFalse(zeitabschnitt.getBemerkungenDTOList()
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
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		//EK Mutation = 40000
		LocalDate October31 = START_PERIODE.plusMonths(3).minusDays(1);
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);

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
				assertTrue(zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST));
			} else {
				assertFalse(zeitabschnitt.getBemerkungenDTOList()
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
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		//EK Mutation = 40000 ab 31.10.
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);

		mutierteBetreuung.extractGesuch()
			.setFinSitAenderungGueltigAbDatum(START_PERIODE.plusMonths(1).minusDays(1)); //31.08
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.BERN);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31); //FinSit GueltigAb 31.10

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);

		zeitabschnitte.forEach(zeitabschnitt ->
			assertFalse(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.FIN_SIT_RUECKWIRKEND_ANGEPASST)));

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
		Betreuung erstgesuchBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(160000), AntragTyp.ERSTGESUCH);
		erstgesuchBetreuung.extractGesuch()
			.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setVerguenstigungGewuenscht(false);
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		//EK Mutation = 40000 ab 31.10.
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
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
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		//EK Mutation = 40000 ab 31.10.
		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
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
		assertNotNull(actual);
		assertEquals(expected.stripTrailingZeros(), actual.stripTrailingZeros());
	}

	@Test
	public void test_Mutation_AuszahlungAnEltern_Aenderung_mit_Auszahlung() {
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);
		VerfuegungZeitabschnitt verfuegterZaAugust =
			findZeitabschnittByMonth(verfuegungErstGesuch.getZeitabschnitte(), Month.AUGUST);
		verfuegterZaAugust.setZahlungsstatusInstitution(VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
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
		assertNotNull(zeitabschnitte);
		assertEquals(12, zeitabschnitte.size());

		assertFalse(findZeitabschnittByMonth(zeitabschnitte, Month.AUGUST).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.SEPTEMBER).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.OCTOBER).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.NOVEMBER).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.DECEMBER).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.JANUARY).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.FEBRUARY).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.MARCH).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.APRIL).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.MAY).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.JUNE).getRelevantBgCalculationInput()
			.isAuszahlungAnEltern());
		assertTrue(findZeitabschnittByMonth(zeitabschnitte, Month.JULY).getRelevantBgCalculationInput()
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
		assertTrue(zeitabschnitte.get(0).getBgCalculationResultAsiv().isZuSpaetEingereicht());
		assertFalse(zeitabschnitte.get(1).getBgCalculationResultAsiv().isZuSpaetEingereicht());
		assertTrue(zeitabschnitte.get(0).getBgCalculationResultGemeinde().isZuSpaetEingereicht());
		assertFalse(zeitabschnitte.get(1).getBgCalculationResultGemeinde().isZuSpaetEingereicht());
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
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(DEFAULT_PENSUM, DEFAULT_MASGEBENDES_EINKOMMEN);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
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
		Betreuung erstgesuchBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		erstgesuchBetreuung.extractGesuch().setFinSitStatus(FinSitStatus.ABGELEHNT);
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil. prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		verfuegungErstGesuch.getZeitabschnitte().forEach(zeitabschnitt -> {
			assertThat(zeitabschnitt.getAnspruchberechtigtesPensum(), is(0));
			assertEqualBigDecimal(MAX_MASGEBENDES_EINKOMMEN, zeitabschnitt.getMassgebendesEinkommen());
		});

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION);
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
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil. prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		//Mutaiton per 1.9., Anspruchpensum 100%, FinSitAbgehlent
		Betreuung mutierteBetreuung =
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION, bpPensumNachMutation, START_PERIODE);
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
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		erstgesuchBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantLuzern());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		//Mutaiton per 1.9., Anspruchpensum 80% (60% + 20% zuschlag)
		Betreuung mutierteBetreuung =
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION, bpPensumNachMutation, START_PERIODE);
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
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensumVorMutation, START_PERIODE);
		erstgesuchBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantLuzern());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstgesuchBetreuung);

		//Mutaiton per 1.9., Anspruchpensum 40% (20% Pensum + 20% Zuschlag)
		Betreuung mutierteBetreuung =
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(40000), AntragTyp.MUTATION, bpPensumNachMutation, START_PERIODE);
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

	@ParameterizedTest
	@EnumSource(value = Bedarfsstufe.class, names = {"KEINE"}, mode = EXCLUDE)
	public void mutationHoehereBeitrageGewaerhtErstantragKeineHoeherenBeitraege(Bedarfsstufe bedarfsstufe) {
		//Erstantrag keine höheren Beiträge
		//Mutations mit höheren Beiträge
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.ERSTGESUCH);
		erstGesuchBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantSchwyz());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.MUTATION);
		mutierteBetreuung.setBedarfsstufe(bedarfsstufe);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);
		mutierteBetreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch,null);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.isZuSpaetEingereicht()) {
				assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), nullValue());
				assertBedarfsstufeAenderungMsg(zeitabschnitt);
			} else {
				assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), is(bedarfsstufe));
				assertBedarfsstufeMsg(zeitabschnitt);
			}
		});
	}

	@Test
	public void mutationHoehereBeitrageNichtGewaerhtErstantragKeineHoeherenBeitraege() {
		//Erstantrag keine höheren Beiträge
		//Mutations höheren Beiträge nicht gewährt (Bedarfsstufe = KEINE)
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.ERSTGESUCH);
		erstGesuchBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantSchwyz());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstGesuchBetreuung);


		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.MUTATION);
		mutierteBetreuung.setBedarfsstufe(Bedarfsstufe.KEINE);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);
		mutierteBetreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch,null);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.isZuSpaetEingereicht()) {
				assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), nullValue());
				assertBedarfsstufeAenderungMsg(zeitabschnitt);
			} else {
				assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), is(Bedarfsstufe.KEINE));
				assertBedarfsstufeNichtGewaehrtMsg(zeitabschnitt);
			}
		});
	}

	@Test
	public void mutationHoehereBeitrageWechselDerBeadarfsstufe() {
		//Erstantrag mit höheren Beiträge Bedarfsstufe 1
		//Mutations mit höheren Beiträge Bedarfsstufe 2
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.ERSTGESUCH);
		erstGesuchBetreuung.setBedarfsstufe(BEDARFSSTUFE_1);
		erstGesuchBetreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		erstGesuchBetreuung.getInstitutionStammdaten().getInstitution().setMandant(getMandantSchwyz());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.MUTATION);
		mutierteBetreuung.setBedarfsstufe(BEDARFSSTUFE_2);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);
		mutierteBetreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch,null);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.isZuSpaetEingereicht()) {
				assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), is(BEDARFSSTUFE_1));
				assertBedarfsstufeAenderungMsg(zeitabschnitt);
			} else {
				assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), is(BEDARFSSTUFE_2));
				assertBedarfsstufeMsg(zeitabschnitt);
			}
		});
	}

	@Test
	public void mutationHoehereBeitrageKeinWechselDerBeadarfsstufe() {
		//Erstantrag mit höheren Beiträge Bedarfsstufe 1
		//Mutations mit höheren Beiträge Bedarfsstufe 1
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.ERSTGESUCH);
		erstGesuchBetreuung.setBedarfsstufe(BEDARFSSTUFE_1);
		erstGesuchBetreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		erstGesuchBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantSchwyz());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.MUTATION);
		mutierteBetreuung.setBedarfsstufe(BEDARFSSTUFE_1);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);
		mutierteBetreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch,null);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), is(BEDARFSSTUFE_1));
			assertBedarfsstufeMsg(zeitabschnitt);
		});
	}

	@Test
	public void mutationHoehereBeitrageWechselDerBeadarfsstufeAufNichtGewaehrt() {
		//Erstantrag mit höheren Beiträge Bedarfsstufe 1
		//Mutations mit höheren Beiträge Bedarfsstufe KEINE
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.ERSTGESUCH);
		erstGesuchBetreuung.setBedarfsstufe(BEDARFSSTUFE_1);
		erstGesuchBetreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		erstGesuchBetreuung.extractGesuch().getDossier().getFall().setMandant(getMandantSchwyz());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.MUTATION);
		mutierteBetreuung.setBedarfsstufe(KEINE);
		mutierteBetreuung.extractGesuch().setEingangsdatum(OCTOBER_31);
		mutierteBetreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch,null);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.isZuSpaetEingereicht()) {
				assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), is(BEDARFSSTUFE_1));
				assertBedarfsstufeAenderungMsg(zeitabschnitt);
			} else {
				assertThat(zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(), is(KEINE));
				assertBedarfsstufeNichtGewaehrtMsg(zeitabschnitt);
			}
		});
	}

	private void assertBedarfsstufeMsg(VerfuegungZeitabschnitt zeitabschnitt) {
		assertTrue(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_MSG));
		assertFalse(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_NICHT_GEWAEHRT_MSG));
		assertFalse(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_AENDERUNG_MSG));
	}

	private void assertBedarfsstufeAenderungMsg(VerfuegungZeitabschnitt zeitabschnitt) {
		assertTrue(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_AENDERUNG_MSG));
		assertFalse(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_MSG));
		assertFalse(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_NICHT_GEWAEHRT_MSG));
	}

	private void assertBedarfsstufeNichtGewaehrtMsg(VerfuegungZeitabschnitt zeitabschnitt) {
		assertFalse(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_AENDERUNG_MSG));
		assertFalse(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_MSG));
		assertTrue(zeitabschnitt.getBemerkungenDTOList().containsMsgKey(BEDARFSSTUFE_NICHT_GEWAEHRT_MSG));
	}

	private Verfuegung prepareErstGesuchVerfuegung(LocalDate eingangsdatum, Mandant mandantAR, int bpPensum) {
		Betreuung erstgesuchBetreuung = MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH, bpPensum, START_PERIODE);
		erstgesuchBetreuung.extractGesuch().setEingangsdatum(eingangsdatum);
		erstgesuchBetreuung.extractGesuch().getFall().setMandant(mandantAR);
		return MutationsMergerTestUtil.prepareVerfuegungForBetreuung(erstgesuchBetreuung);
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
				assertEquals(
					anspruchberechtigtesPensum,
					za.getAnspruchberechtigtesPensum(),
					"Falsches anspruchberechtiges Pensum in Zeitabschnitt " + za)
			);
	}

	private void checkAllAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate startAfterOrAt,
		int anspruchberechtigtesPensum) {
		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().startsAfter(startAfterOrAt) || za.getGueltigkeit()
				.startsSameDay(startAfterOrAt)).
			forEach(za -> assertEquals(
				anspruchberechtigtesPensum,
				za.getAnspruchberechtigtesPensum(),
				"Falsches anspruchberechtiges Pensum in Zeitabschnitt " + za)
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
				assertEquals(
					besondereBedurfnisseBestaetigt,
					za.isBesondereBeduerfnisseBestaetigt(),
					"BesondereBedurfnisse sind falsch gesetzt in Zeitabschnitt " + za)
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
				assertEquals(
					besondereBedurfnisseBestaetigt,
					za.isBesondereBeduerfnisseBestaetigt(),
					"BesondereBedurfnisse sind falsch gesetzt in Zeitabschnitt " + za)
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

	@Nonnull
	protected Betreuung prepareMutation(@Nonnull LocalDate eingangsdatumMuation, int bpPensum, LocalDate aenderungsDatumPensum,
		int bpPensumVorMutation) {
		Betreuung mutierteBetreuung =
			MutationsMergerTestUtil.prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION, bpPensum, aenderungsDatumPensum);
		mutierteBetreuung.extractGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(
			START_PERIODE, aenderungsDatumPensum.minusDays(1), bpPensumVorMutation));
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		return mutierteBetreuung;
	}
}
