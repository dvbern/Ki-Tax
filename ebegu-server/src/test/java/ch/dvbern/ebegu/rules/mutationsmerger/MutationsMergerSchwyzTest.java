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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.rules.MonatsRule;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests fuer Verfügungsmuster
 */
class MutationsMergerSchwyzTest {

	private static final LocalDate EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT = START_PERIODE.plusMonths(1).plusDays(5);

	private static final MonatsRule MONATS_RULE = new MonatsRule(false);

	private static final MutationsMerger MUTATIONS_MERGER = new MutationsMerger(Locale.GERMAN, false, false);

	private static final BigDecimal DEFAULT_MASGEBENDES_EINKOMMEN = Objects.requireNonNull(MathUtil.DEFAULT.from(50000));
	private static final BigDecimal ZWEI_HUNDERT_THAUSEND = Objects.requireNonNull(MathUtil.DEFAULT.from(200000));

	@Test
	void test_hoereMassgegebeneseinkommens_steigtFolgeMonatNachMutation() {
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(
			100,
			DEFAULT_MASGEBENDES_EINKOMMEN);
		//EK Mutation = 50000
		Betreuung mutierteBetreuung =
			MutationsMergerTestUtil.prepareData(ZWEI_HUNDERT_THAUSEND, AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.SCHWYZ));
		//Mutation im August eingereicht, BG soltle von 17.9 zu 1.9 nicht rueckwirkend angepasst werden
		mutierteBetreuung.extractGesuch().setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		List<VerfuegungZeitabschnitt> zeitabschnitts = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> zeitabschnitteAfterMonatsRule =
			EbeguRuleTestsHelper.runSingleAbschlussRule(MONATS_RULE, mutierteBetreuung, zeitabschnitts);

		// Der FinSit Rechner von Bern ist verwendet, wir wollen hier aber die MonatMerger Von Schwyz testen
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.SCHWYZ);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			MUTATIONS_MERGER,
			mutierteBetreuung,
			zeitabschnitteAfterMonatsRule);

		//Zeitabschnitt Flag zuSpät = false
		assertThat(zeitabschnitte.size(), is(12));
		checkAllMassgegebenesEinommenBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT,
			MathUtil.GANZZAHL.from(DEFAULT_MASGEBENDES_EINKOMMEN));
		checkAllMassgegebenesEinkommenAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT,
			MathUtil.GANZZAHL.from(ZWEI_HUNDERT_THAUSEND));
	}

	@Test
	void test_kleinereMassgegebeneseinkommens_steigtFolgeMonatNachMutation() {
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil.prepareErstGesuchVerfuegung(100, ZWEI_HUNDERT_THAUSEND);
		//EK Mutation = 50000
		Betreuung mutierteBetreuung =
			MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(TestDataUtil.createMandant(MandantIdentifier.SCHWYZ));
		//Mutation im August eingereicht, BG soltle von 17.9 zu 1.9 rueckwirkend angepasst werden
		mutierteBetreuung.extractGesuch().setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstGesuch, null);
		List<VerfuegungZeitabschnitt> zeitabschnitts = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> zeitabschnitteAfterMonatsRule =
			EbeguRuleTestsHelper.runSingleAbschlussRule(MONATS_RULE, mutierteBetreuung, zeitabschnitts);

		// Der FinSit Rechner von Bern ist verwendet, wir wollen hier aber die MonatMerger Von Schwyz testen
		mutierteBetreuung.extractGesuch().setFinSitTyp(FinanzielleSituationTyp.SCHWYZ);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			MUTATIONS_MERGER,
			mutierteBetreuung,
			zeitabschnitteAfterMonatsRule);

		//Zeitabschnitt Flag zuSpät = false
		assertThat(zeitabschnitte.size(), is(12));
		checkAllMassgegebenesEinommenBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT,
			MathUtil.GANZZAHL.from(ZWEI_HUNDERT_THAUSEND));
		checkAllMassgegebenesEinkommenAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT,
			MathUtil.GANZZAHL.from(DEFAULT_MASGEBENDES_EINKOMMEN));
	}

	@Test
	void mutationAnpassungHoehereBeitrag_steigt_folgeMonat() {
		Bedarfsstufe bedarfsstufeErstGesuch = Bedarfsstufe.BEDARFSSTUFE_1;
		Bedarfsstufe bedarfsstufeMutation = Bedarfsstufe.BEDARFSSTUFE_2;
		Betreuung betreuung = prepareDataMitBedarfsstufe(AntragTyp.ERSTGESUCH, bedarfsstufeErstGesuch);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(betreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(zabetrErtgesuch);
		betreuung.setVerfuegung(verfuegungErstgesuch);
		betreuung.extractGesuch().setTimestampVerfuegt(LocalDateTime.now());
		verfuegungErstgesuch.setBetreuung(betreuung);
		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(AntragTyp.MUTATION, bedarfsstufeMutation);
		mutierteBetreuung.extractGesuch().setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zeitabschnitts = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			MUTATIONS_MERGER,
			mutierteBetreuung,
			zeitabschnitts);

		checkAllBedarfsstufeBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT,
			bedarfsstufeErstGesuch);
		checkAllBedarfsstufeAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT,
			bedarfsstufeMutation);
	}

	@Test
	void mutationAnpassungHoehereBeitrag_sink_folgeMonat() {
		Bedarfsstufe bedarfsstufeErstGesuch = Bedarfsstufe.BEDARFSSTUFE_2;
		Bedarfsstufe bedarfsstufeMutation = Bedarfsstufe.BEDARFSSTUFE_1;
		Betreuung betreuung = prepareDataMitBedarfsstufe(AntragTyp.ERSTGESUCH, bedarfsstufeErstGesuch);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(betreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		verfuegungErstgesuch.setZeitabschnitte(zabetrErtgesuch);
		betreuung.setVerfuegung(verfuegungErstgesuch);
		betreuung.extractGesuch().setTimestampVerfuegt(LocalDateTime.now());
		verfuegungErstgesuch.setBetreuung(betreuung);
		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(AntragTyp.MUTATION, bedarfsstufeMutation);
		mutierteBetreuung.extractGesuch().setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		List<VerfuegungZeitabschnitt> zeitabschnitts = EbeguRuleTestsHelper.calculate(mutierteBetreuung);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.runSingleAbschlussRule(
			MUTATIONS_MERGER,
			mutierteBetreuung,
			zeitabschnitts);

		checkAllBedarfsstufeBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT,
			bedarfsstufeErstGesuch);
		checkAllBedarfsstufeAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT,
			bedarfsstufeMutation);
	}

	private Betreuung prepareDataMitBedarfsstufe(AntragTyp antragTyp, Bedarfsstufe bedarfsstufe) {
		Mandant schwyz = TestDataUtil.createMandant(MandantIdentifier.SCHWYZ);
		Betreuung betreuung = MutationsMergerTestUtil.prepareData(DEFAULT_MASGEBENDES_EINKOMMEN, antragTyp);
		betreuung.extractGesuch()
			.getFall()
			.setMandant(schwyz);
		betreuung.getInstitutionStammdaten().getInstitution().setMandant(schwyz);
		betreuung.getKind().getKindJA().setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		betreuung.setBedarfsstufe(bedarfsstufe);
		return betreuung;
	}

	private void checkAllMassgegebenesEinommenBefore(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		BigDecimal massgegebenesEinkommenVorAbzugFamgr) {

		checkAllBefore(zeitabschnitte, mutationDatum, za ->
			assertThat(
				"Falsches MassgebendesEinkommenVorAbzFamgr in Zeitabschnitt " + za,
				za.getMassgebendesEinkommenVorAbzFamgr(),
				is(massgegebenesEinkommenVorAbzugFamgr)));
	}

	private void checkAllBedarfsstufeBefore(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		Bedarfsstufe bedarfsstufe) {

		checkAllBefore(zeitabschnitte, mutationDatum, za ->
			assertThat(
				"Falsches HoehereBeitrag in Zeitabschnitt " + za,
				za.getBedarfsstufe(),
				is(bedarfsstufe)));
	}

	private void checkAllBefore(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		Consumer<? super VerfuegungZeitabschnitt> assertion) {

		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().getGueltigAb().getMonth().getValue() <= mutationDatum.getMonth().getValue() &&
				za.getGueltigkeit().getGueltigAb().getYear() == mutationDatum.getYear()).
			forEach(assertion);
	}

	private void checkAllMassgegebenesEinkommenAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		BigDecimal massgegebenesEinkommenVorAbzugFamgr) {

		checkAllAfter(zeitabschnitte, mutationDatum, za ->
			assertThat(
				"Falsches MassgebendesEinkommenVorAbzFamgr in Zeitabschnitt " + za,
				za.getMassgebendesEinkommenVorAbzFamgr(), is(massgegebenesEinkommenVorAbzugFamgr)));
	}

	private void checkAllBedarfsstufeAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		Bedarfsstufe bedarfsstufe) {
		checkAllAfter(zeitabschnitte, mutationDatum, za ->
			assertThat("Falsches Bedarfsstufe in Zeitabschnitt " + za, za.getBedarfsstufe(), is(bedarfsstufe)));
	}

	private void checkAllAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		java.util.function.Consumer<? super VerfuegungZeitabschnitt> assertion) {
		zeitabschnitte.stream().
			filter(za -> za.getGueltigkeit().getGueltigAb().getMonth().getValue() > mutationDatum.getMonth().getValue() &&
				za.getGueltigkeit().getGueltigAb().getYear() == mutationDatum.getYear()).
			forEach(assertion);
	}
}
