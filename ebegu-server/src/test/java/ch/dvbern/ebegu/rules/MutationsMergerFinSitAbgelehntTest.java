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
import java.time.LocalDateTime;
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
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests fuer den Mutationsmerger im Spezialfall, dass in der Mutation die FinSit abgelehnt wurde.
 */
public class MutationsMergerFinSitAbgelehntTest {

	private static final boolean IS_DEBUG = false;
	private final MonatsRule monatsRule = new MonatsRule(IS_DEBUG);
	private final MutationsMerger mutationsMerger = new MutationsMerger(Locale.GERMAN, IS_DEBUG);

	private static final BigDecimal EINKOMMEN_HOEHER = MathUtil.DEFAULT.fromNullSafe(120000);
	private static final BigDecimal EINKOMMEN_TIEFER = MathUtil.DEFAULT.fromNullSafe(80000);

	private static final LocalDate DATUM_FRUEHER = TestDataUtil.START_PERIODE.plusMonths(6);
	private static final LocalDate DATUM_SPAETER = TestDataUtil.START_PERIODE.plusMonths(7);


	@Test
	public void finSitAbgelehntInMutationErhoehungRechtzeitigEingereichtKita() {
		// Einkommen steigt in Mutation mit abgelehnter FinSit
		// Rechtzeitig eingereicht
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculateMutationWithFinSitAbgelehnt(DATUM_FRUEHER, DATUM_SPAETER, EINKOMMEN_TIEFER, EINKOMMEN_HOEHER);

		// Unabhaengig von Einreichefrist und Erhoehung/Senkung des Einkommens:
		// Weil FinSit abgelehnt, muss ueber die gesamte GP das alte Einkommen gelten!
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllZeitabschnitte(zeitabschnitte, EINKOMMEN_TIEFER);
	}

	@Test
	public void finSitAbgelehntInMutationSenkungRechtzeitigEingereichtKita() {
		// Einkommen sinkt in Mutation mit abgelehnter FinSit
		// Rechtzeitig eingereicht
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculateMutationWithFinSitAbgelehnt(DATUM_FRUEHER, DATUM_SPAETER, EINKOMMEN_HOEHER, EINKOMMEN_TIEFER);

		// Unabhaengig von Einreichefrist und Erhoehung/Senkung des Einkommens:
		// Weil FinSit abgelehnt, muss ueber die gesamte GP das alte Einkommen gelten!
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllZeitabschnitte(zeitabschnitte, EINKOMMEN_HOEHER);
	}

	@Test
	public void finSitAbgelehntInMutationErhoehungZuSpaetEingereichtKita() {
		// Einkommen steigt in Mutation mit abgelehnter FinSit
		// Zu spaet eingereicht
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculateMutationWithFinSitAbgelehnt(DATUM_SPAETER, DATUM_FRUEHER, EINKOMMEN_TIEFER, EINKOMMEN_HOEHER);

		// Unabhaengig von Einreichefrist und Erhoehung/Senkung des Einkommens:
		// Weil FinSit abgelehnt, muss ueber die gesamte GP das alte Einkommen gelten!
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllZeitabschnitte(zeitabschnitte, EINKOMMEN_TIEFER);
	}

	@Test
	public void finSitAbgelehntInMutationSenkungZuSpaetEingereichtKita() {
		// Einkommen sinkt in Mutation mit abgelehnter FinSit
		// Zu spaet eingereicht
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculateMutationWithFinSitAbgelehnt(DATUM_SPAETER, DATUM_FRUEHER, EINKOMMEN_HOEHER, EINKOMMEN_TIEFER);

		// Unabhaengig von Einreichefrist und Erhoehung/Senkung des Einkommens:
		// Weil FinSit abgelehnt, muss ueber die gesamte GP das alte Einkommen gelten!
		Assert.assertNotNull(zeitabschnitte);
		Assert.assertEquals(12, zeitabschnitte.size());
		checkAllZeitabschnitte(zeitabschnitte, EINKOMMEN_HOEHER);
	}

	private List<VerfuegungZeitabschnitt> calculateMutationWithFinSitAbgelehnt(
		@Nonnull LocalDate eingangsdatumMutation, @Nonnull LocalDate aenderungAb,
		@Nonnull BigDecimal einkommenVorher, @Nonnull BigDecimal einkommenAbgelehnt
	) {
		// Mutiertes Gesuch vorbereiten
		Betreuung mutierteBetreuung = prepareMutation(eingangsdatumMutation);
		List<VerfuegungZeitabschnitt> zabetrMutiert = EbeguRuleTestsHelper.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert = EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, mutierteBetreuung, zabetrMutiert);
		setMassgebendesEinkommenAbDatum(verfuegungsZeitabschnitteMutiert, TestDataUtil.START_PERIODE, einkommenVorher);
		setMassgebendesEinkommenAbDatum(verfuegungsZeitabschnitteMutiert, aenderungAb, einkommenAbgelehnt);
		mutierteBetreuung.extractGesuch().setFinSitStatus(FinSitStatus.ABGELEHNT);

		// Erstgesuch Gesuch vorbereiten
		final Verfuegung verfuegungErstgesuch = createErstgesuch(einkommenVorher);
		mutierteBetreuung.initVorgaengerVerfuegungen(verfuegungErstgesuch, null);
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(verfuegungsZeitabschnitteMutiert);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : verfuegungsZeitabschnitteMutiert) {
			verfuegungZeitabschnitt.setVerfuegung(verfuegung);
		}
		verfuegung.setPlatz(mutierteBetreuung);
				mutierteBetreuung.setVerfuegung(verfuegung);

		// mergen
		final List<VerfuegungZeitabschnitt> abschnitte =
			EbeguRuleTestsHelper.runSingleAbschlussRule(mutationsMerger, mutierteBetreuung, verfuegungsZeitabschnitteMutiert);
		return abschnitte;
	}

	private Verfuegung createErstgesuch(
		@Nonnull BigDecimal massgebendesEinkommen
	) {
		Betreuung erstgesuchBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.ERSTGESUCH);
		List<VerfuegungZeitabschnitt> zabetrErtgesuch = EbeguRuleTestsHelper.calculate(erstgesuchBetreuung);
		Verfuegung verfuegungErstgesuch = new Verfuegung();
		final List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteErstgesuch = EbeguRuleTestsHelper.runSingleAbschlussRule(monatsRule, erstgesuchBetreuung, zabetrErtgesuch);
		setMassgebendesEinkommenAbDatum(verfuegungsZeitabschnitteErstgesuch, TestDataUtil.START_PERIODE, massgebendesEinkommen);
		verfuegungErstgesuch.setZeitabschnitte(verfuegungsZeitabschnitteErstgesuch);
		verfuegungErstgesuch.setPlatz(erstgesuchBetreuung);
		erstgesuchBetreuung.setVerfuegung(verfuegungErstgesuch);
		erstgesuchBetreuung.extractGesuch().setFinSitStatus(FinSitStatus.AKZEPTIERT);
		erstgesuchBetreuung.extractGesuch().setTimestampVerfuegt(LocalDateTime.now());
		return verfuegungErstgesuch;
	}

	private void checkAllZeitabschnitte(List<VerfuegungZeitabschnitt> zeitabschnitte, BigDecimal massgebendesEinkommen) {
		zeitabschnitte.
			forEach(za ->
				Assert.assertEquals("Falsches Massgebendes Einkommen in Zeitabschnitt " + za,
					massgebendesEinkommen, za.getMassgebendesEinkommen())
			);
	}

	private void setMassgebendesEinkommenAbDatum(
		List<VerfuegungZeitabschnitt> verfuegungsZeitabschnitteMutiert,
		LocalDate datumAb,
		BigDecimal massgebendesEinkommen
	) {
		verfuegungsZeitabschnitteMutiert.stream()
			.filter(v -> v.getGueltigkeit().startsSameDay(datumAb) || v.getGueltigkeit().startsAfter(datumAb))
			.forEach(v -> {
				v.getBgCalculationInputAsiv().setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
				v.getBgCalculationInputAsiv().setAbzugFamGroesse(BigDecimal.ZERO);
				v.getBgCalculationResultAsiv().setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
				v.getBgCalculationResultAsiv().setAbzugFamGroesse(BigDecimal.ZERO);
			});
	}

	private Betreuung prepareData(BigDecimal massgebendesEinkommen, AntragTyp antragTyp) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
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

	@Nonnull
	private Betreuung prepareMutation(@Nonnull LocalDate eingangsdatumMuation) {
		Betreuung mutierteBetreuung = prepareData(MathUtil.DEFAULT.from(50000), AntragTyp.MUTATION);
		mutierteBetreuung.extractGesuch().setEingangsdatum(eingangsdatumMuation);
		mutierteBetreuung.initVorgaengerVerfuegungen(null, null);

		return mutierteBetreuung;
	}
}
