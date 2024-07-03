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

package ch.dvbern.ebegu.rules.familienabzug;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests fuer FamilienabzugAbschnittRule
 */
public class FamilienabzugAbschnittRuleBernTest {
	private final BigDecimal pauschalabzugProPersonFamiliengroesse3 = MathUtil.DEFAULT.from(Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS);
	private final BigDecimal pauschalabzugProPersonFamiliengroesse4 = MathUtil.DEFAULT.from(Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS);
	private final BigDecimal pauschalabzugProPersonFamiliengroesse5 = MathUtil.DEFAULT.from(Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS);
	private final BigDecimal pauschalabzugProPersonFamiliengroesse6 = MathUtil.DEFAULT.from(Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS);

	private static final double DELTA = 1.0e-15;
	public static final LocalDate DATE_2005 = LocalDate.of(2005, 12, 31);

	private final FamilienabzugAbschnittRuleASIV famabAbschnittRule =
		new FamilienabzugAbschnittRuleASIV(getEinstellungMapForAsiv(), Constants.DEFAULT_GUELTIGKEIT,  Constants.DEFAULT_LOCALE);

	private final FamilienabzugAbschnittRuleFKJV famabAbschnittRule_FKJV2 =
		new FamilienabzugAbschnittRuleFKJV(getEinstellungMapForFKJV2(), Constants.DEFAULT_GUELTIGKEIT, Constants.DEFAULT_LOCALE);

	@Test
	void test2PKeinAbzug() {
		Betreuung betreuung = TestDataUtil.createGesuchWithoutBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		final KindContainer defaultKindContainer = TestDataUtil.createDefaultKindContainer();
		gesuch.getKindContainers().add(defaultKindContainer);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = zeitabschnitte.iterator().next();
		Assertions.assertEquals(0, verfuegungZeitabschnitt.getAbzugFamGroesse().intValue());
	}

	@Test
	void test3P_Abzug() {
		Betreuung betreuung = TestDataUtil.createGesuchWithoutBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		final KindContainer defaultKindContainer1 = TestDataUtil.createDefaultKindContainer();
		final KindContainer defaultKindContainer2 = TestDataUtil.createDefaultKindContainer();

		gesuch.getKindContainers().add(defaultKindContainer1);
		gesuch.getKindContainers().add(defaultKindContainer2);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = zeitabschnitte.iterator().next();
		Assertions.assertEquals(
			0,
			verfuegungZeitabschnitt.getAbzugFamGroesse()
				.compareTo(pauschalabzugProPersonFamiliengroesse3.multiply(BigDecimal.valueOf(3))));
	}

	@Test
	void test4P_Abzug() {
		Betreuung betreuung = TestDataUtil.createGesuchWithoutBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		final KindContainer defaultKindContainer1 = TestDataUtil.createDefaultKindContainer();
		final KindContainer defaultKindContainer2 = TestDataUtil.createDefaultKindContainer();

		gesuch.getKindContainers().add(defaultKindContainer1);
		gesuch.getKindContainers().add(defaultKindContainer2);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = zeitabschnitte.iterator().next();
		Assertions.assertEquals(
			0,
			verfuegungZeitabschnitt.getAbzugFamGroesse()
				.compareTo(pauschalabzugProPersonFamiliengroesse4.multiply(BigDecimal.valueOf(4))));
	}

	@Test
	void test3P_Abzug_Kind_waehrendPeriode() {
		Betreuung betreuung = TestDataUtil.createGesuchWithoutBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		final KindContainer defaultKindContainer1 = TestDataUtil.createDefaultKindContainer();
		final KindContainer defaultKindContainer2 = TestDataUtil.createDefaultKindContainer();
		final LocalDate geburtsdatum = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 1, 10);
		defaultKindContainer2.getKindJA().setGeburtsdatum(geburtsdatum);

		gesuch.getKindContainers().add(defaultKindContainer1);
		gesuch.getKindContainers().add(defaultKindContainer2);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(2, zeitabschnitte.size());

		final Iterator<VerfuegungZeitabschnitt> iterator = zeitabschnitte.iterator();
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt1 = iterator.next();
		Assertions.assertEquals(
			0,
			verfuegungZeitabschnitt1.getAbzugFamGroesse()
				.compareTo(pauschalabzugProPersonFamiliengroesse3.multiply(BigDecimal.valueOf(3))));
		final LocalDate withDayOfMonth = geburtsdatum.plusMonths(1).withDayOfMonth(1);
		Assertions.assertEquals(0, verfuegungZeitabschnitt1.getGueltigkeit().getGueltigBis().compareTo(
			withDayOfMonth.minusDays(1)));

		final VerfuegungZeitabschnitt verfuegungZeitabschnitt2 = iterator.next();
		Assertions.assertEquals(
			0,
			verfuegungZeitabschnitt2.getAbzugFamGroesse()
				.compareTo(pauschalabzugProPersonFamiliengroesse4.multiply(BigDecimal.valueOf(4))));
		Assertions.assertEquals(0, verfuegungZeitabschnitt2.getGueltigkeit().getGueltigAb().compareTo(withDayOfMonth));
	}

	@Test
	void test3P_Abzug_Zwiling_waehrendPeriode() {
		Betreuung betreuung = TestDataUtil.createGesuchWithoutBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		final KindContainer defaultKindContainer1 = TestDataUtil.createDefaultKindContainer();
		final KindContainer defaultKindContainer2 = TestDataUtil.createDefaultKindContainer();
		final LocalDate geburtsdatum = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 1, 10);
		defaultKindContainer2.getKindJA().setGeburtsdatum(geburtsdatum);

		final KindContainer defaultKindContainer3 = TestDataUtil.createDefaultKindContainer();
		defaultKindContainer3.getKindJA().setGeburtsdatum(geburtsdatum);

		gesuch.getKindContainers().add(defaultKindContainer1);
		gesuch.getKindContainers().add(defaultKindContainer2);
		gesuch.getKindContainers().add(defaultKindContainer3);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(2, zeitabschnitte.size());

		final Iterator<VerfuegungZeitabschnitt> iterator = zeitabschnitte.iterator();
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt1 = iterator.next();
		Assertions.assertEquals(
			0,
			verfuegungZeitabschnitt1.getAbzugFamGroesse()
				.compareTo(pauschalabzugProPersonFamiliengroesse3.multiply(BigDecimal.valueOf(3))));
		final LocalDate withDayOfMonth = geburtsdatum.plusMonths(1).withDayOfMonth(1);
		Assertions.assertEquals(0, verfuegungZeitabschnitt1.getGueltigkeit().getGueltigBis().compareTo(
			withDayOfMonth.minusDays(1)));

		final VerfuegungZeitabschnitt verfuegungZeitabschnitt2 = iterator.next();
		Assertions.assertEquals(
			0,
			verfuegungZeitabschnitt2.getAbzugFamGroesse()
				.compareTo(pauschalabzugProPersonFamiliengroesse5.multiply(BigDecimal.valueOf(5))));
		Assertions.assertEquals(0, verfuegungZeitabschnitt2.getGueltigkeit().getGueltigAb().compareTo(withDayOfMonth));
	}

	@Test
	void testCalculateFamiliengroesseNoGesuchsteller() {
		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(TestDataUtil.createDefaultDossier());
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, DATE_2005).getKey();
		Assertions.assertEquals(0, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseOneGesuchsteller() {
		Gesuch gesuch = createGesuchWithOneGS();
		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, DATE_2005).getKey();
		Assertions.assertEquals(1, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseOneGesuchstellerErstges() {
		Gesuch gesuch = createGesuchWithOneGS();
		//aktuell alleinerziehend
		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, DATE_2005).getKey();
		Assertions.assertEquals(1, familiengroesse, DELTA);
		// jetzt wechseln auf verheiratet
		Familiensituation erstFamiliensituation = new Familiensituation();
		erstFamiliensituation.setAenderungPer(null); //im erstgesuch immer null
		erstFamiliensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(erstFamiliensituation);
		double newFamGr = famabAbschnittRule.calculateFamiliengroesse(gesuch, DATE_2005).getKey();
		Assertions.assertEquals(2, newFamGr, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseTwoGesuchsteller() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, DATE_2005).getKey();
		Assertions.assertEquals(2, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithGanzerAbzugKind() {
		Gesuch gesuch = createGesuchWithKind(Kinderabzug.GANZER_ABZUG, null, LocalDate.of(2006, 5, 25));

		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, LocalDate.now()).getKey();
		Assertions.assertEquals(3, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithHalberAbzugKind() {
		Gesuch gesuch = createGesuchWithKind(Kinderabzug.HALBER_ABZUG, null, LocalDate.of(2006, 5, 25));

		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, LocalDate.now()).getKey();
		Assertions.assertEquals(2.5, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithKeinAbzugKind() {
		Gesuch gesuch = createGesuchWithKind(Kinderabzug.KEIN_ABZUG, null, LocalDate.of(2006, 5, 25));

		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, LocalDate.now()).getKey();
		Assertions.assertEquals(2, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithWrongGeburtsdatum() {
		//das Kind war noch nicht geboren, innerhalb der Gesuchsperiode. So it cannot be counted for the abschnitt
		Gesuch gesuch = createGesuchWithKind(Kinderabzug.GANZER_ABZUG, null, LocalDate.of(2017, 10, 25));

		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, LocalDate.of(2017, 9, 25))
			.getKey();
		Assertions.assertEquals(2, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithGeburtsdatumAusserhalbPeriode() {
		// The Kind was born before the period start but after the date given as parameter. In this case the actual date
		// is not important because within the period the Kind already exists and the familiy has already changed, so
		// the Kind must be counted as well and the familiengroesse must be 3
		Gesuch gesuch = createGesuchWithKind(Kinderabzug.GANZER_ABZUG, null, LocalDate.of(2015, 10, 25));

		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, LocalDate.of(2014, 9, 25))
			.getKey();
		Assertions.assertEquals(3, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithCorrectGeburtsdatum() {
		//das Kind war schon geboren
		Gesuch gesuch = createGesuchWithKind(Kinderabzug.GANZER_ABZUG, null, LocalDate.of(2006, 5, 25));

		double familiengroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, LocalDate.now()).getKey();
		Assertions.assertEquals(3, familiengroesse, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithTwoKinder() {
		//das Kind war schon geboren
		Gesuch gesuch = createGesuchWithKind(Kinderabzug.GANZER_ABZUG, Kinderabzug.HALBER_ABZUG, LocalDate.of(2006, 5, 25));

		final Entry<Double, Integer> famGroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		Assertions.assertEquals(3.5, familiengroesse, DELTA);
		Assertions.assertEquals(4, familienMitglieder, DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithTwoKinderKeinAbzug() {
		//das Kind war schon geboren
		Gesuch gesuch = createGesuchWithKind(Kinderabzug.HALBER_ABZUG, Kinderabzug.KEIN_ABZUG, LocalDate.of(2006, 5, 25));

		final Entry<Double, Integer> famGroesse = famabAbschnittRule.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		Assertions.assertEquals(2.5, familiengroesse, DELTA);
		Assertions.assertEquals(3, familienMitglieder, DELTA);
	}

	@Test
	void testCalculateAbzugAufgrundFamiliengroesseZero() {
		Assertions.assertEquals(0, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(0, 0).intValue());
		Assertions.assertEquals(0, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(1, 1).intValue());
		Assertions.assertEquals(0, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(1.5, 2).intValue());
	}

	@Test
	void testCalculateAbzugAufgrundFamiliengroesse_EBEGU_1185_NR32_Familiengroesse_Berechnung() {

		/* Beispiel Nr. 1:
		 * 1 Erwachsene Person (Alleinerziehend) und 1 Kind zu 50% in den Steuern abzugsberechtigt. Die Anzahl der Personen,
		 * die im Haushalt wohnen, beträgt zwei, die anrechenbare Familiengrösse ist 1,5. Es ist damit kein Abzug möglich,
		 * da 2-Personenhaushalt. Daher Fr. 0.00 in der Berechnung.
		 */
		Assertions.assertEquals(0, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(1.5, 2).intValue());

		/* Beispiel Nr. 2:
		 * 1 Erwachsene Person (Alleinerziehend) und 2 Kindern zu je 50% Abzugsmöglichkeit in den Steuern. Die Anzahl der
		 * Personen,
		 * die im gleichen Haushalt wohnen, beträgt somit 3 Personen und es wird nun der Ansatz 3-Personenhaushalt von
		 * Fr. 3'800.00 angenommen. Die anrechenbare Familiengrösse ist 2 und dieser Wert wird mit dem Ansatz von
		 * 3-Personenhaushalt
		 * von Fr. 3'800.00 multipliziert; Ergebnis Fr. 7'600.00
		 */
		Assertions.assertEquals(7600, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(2, 3).intValue());

		/* Beispiel Nr. 3:
		 * 1 Erwachsene Person (Alleinerziehend) mit 3 Kindern, für die Kinder ist je 50% Kinderabzug möglich. Es sind insgesamt
		 * 4 Personen im gleichen Haushalt wohnhaft, somit wird die Pauschale einer 4-Personenhaushalt von Fr. 5'960.00 genommen.
		 * Die anrechenbare Familiengrösse beträgt 2,5 und dieser Wert wird mit der Pauschale 4-Personenhaushalt von
		 * Fr. 6'000.00 multipliziert; Ergebnis Fr. 15'000.00.
		 */
		Assertions.assertEquals(15000, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(2.5, 4).intValue());

		/*
		 * Beispiel Nr. 4:
		 * 1 Erwachsene Person (Alleinerziehend) mit 4 Kindern, für das erste Kind ist kein Abzug in der Steuererklärung möglich,
		 * für das zweite Kind ist 100% möglich und für das dritte Kind 50%. Insgesamt sind 3 Personen im gleichen Haushalt
		 * wohnhaft. Deshalb wird die Pauschale 3-Personenhaushalt genommen
		 * (das erste Kind hat unter der Frage Kinderabzug "nein" stehen und zählt damit nicht dazu).
		 * Die anrechenbare Familiengrösse beträgt 2,5 und diese Familiengrösse wird mit der
		 * Pauschale 3-Personenhaushalt von Fr. 3'800.00 multipliziert; Ergebnis Fr. 9'500.00.
		 */
		Assertions.assertEquals(9500, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(2.5, 3).intValue());

		/*
		 * Beispiel Nr. 5:
		 * 2 Erwachsene Personen (Konkubinat) und 4 Kindern, zwei eigene Kinder sind je 100% abzugsberechtigt in der
		 * Steuererklärung und für zwei Kindern sind zu je 50% Abzug möglich. Insgesamt leben 6 Personen im gleichen Haushalt,
		 * es wird nun der Ansatz von 6 Personenhaushalt von Fr. 7'700.00 genommen. Die anrechenbare Familiengrösse von 4,5
		 * wird mit der Pauschale 6-Personenhaushalt von Fr. 7'700.00 multipliziert; Ergebnis Fr. 34'650.00.
		 */
		Assertions.assertEquals(34650, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(4.5, 6).intValue());

		/*
		 * Beispiel Nr. 6:
		 * 2 Erwachsene Personen (verheiratet) und 2 Kindern mit je 100% Abzugsmöglichkeit in den Steuern. Somit beträgt
		 * die Anzahl der Personen im gleichen Haushalt 4. Damit wird der Pauschalabzug von 4-Personenhaushalt angewendet.
		 * Die anrechenbare Familiengrösse 4 wird mit 4-Personhaushalt von Fr. 6000.00 multipliziert; Ergebnis Fr. 24000.00.
		 */
		Assertions.assertEquals(24000, famabAbschnittRule.calculateAbzugAufgrundFamiliengroesse(4.0, 4).intValue());
	}

	@Test
	void testCalculateFamiliengroesseWithMutation1GSTo2GS() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		final LocalDate date = LocalDate.of(1980, Month.MARCH, 25);
		gesuch.extractFamiliensituation().setAenderungPer(date);

		Familiensituation famSitErstgesuch = new Familiensituation();
		famSitErstgesuch.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(famSitErstgesuch);

		Assertions.assertEquals(1, famabAbschnittRule.calculateFamiliengroesse(gesuch, date.minusMonths(1)).getKey(), DELTA);
		Assertions.assertEquals(1, famabAbschnittRule.calculateFamiliengroesse(gesuch, date.withDayOfMonth(31)).getKey(), DELTA);
		Assertions.assertEquals(
			2,
			famabAbschnittRule.calculateFamiliengroesse(gesuch, date.plusMonths(1).withDayOfMonth(1)).getKey(),
			DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithMutation2GSTo1GS() {
		Gesuch gesuch = createGesuchWithOneGS();
		final LocalDate date = LocalDate.of(1980, Month.MARCH, 25);
		gesuch.extractFamiliensituation().setAenderungPer(date);

		Familiensituation famSitErstgesuch = new Familiensituation();
		famSitErstgesuch.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(famSitErstgesuch);

		Assertions.assertEquals(2, famabAbschnittRule.calculateFamiliengroesse(gesuch, date.minusMonths(1)).getKey(), DELTA);
		Assertions.assertEquals(2, famabAbschnittRule.calculateFamiliengroesse(gesuch, date.withDayOfMonth(31)).getKey(), DELTA);
		Assertions.assertEquals(
			1,
			famabAbschnittRule.calculateFamiliengroesse(gesuch, date.plusMonths(1).withDayOfMonth(1)).getKey(),
			DELTA);
	}

	@Test
	void testCalculateFamiliengroesseWithMutation2GSTo2GS() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		final LocalDate date = LocalDate.of(1980, Month.MARCH, 25);
		gesuch.extractFamiliensituation().setAenderungPer(date);

		Familiensituation famSitErstgesuch = new Familiensituation();
		famSitErstgesuch.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(famSitErstgesuch);

		Assertions.assertEquals(2, famabAbschnittRule.calculateFamiliengroesse(gesuch, date.minusMonths(1)).getKey(), DELTA);
		Assertions.assertEquals(2, famabAbschnittRule.calculateFamiliengroesse(gesuch, date.withDayOfMonth(31)).getKey(), DELTA);
		Assertions.assertEquals(
			2,
			famabAbschnittRule.calculateFamiliengroesse(gesuch, date.plusMonths(1).withDayOfMonth(1)).getKey(),
			DELTA);
	}

	@Test
	void testFamiliensituationMutiert1GSTo2GS() {
		Betreuung betreuung = TestDataUtil.createGesuchWithoutBetreuungspensum(true);
		Gesuch gesuch = betreuung.extractGesuch();
		final LocalDate date = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, Month.MARCH, 25); // gesuchsperiode ist 2017/2018
		gesuch.extractFamiliensituation().setAenderungPer(date);

		Familiensituation famSitErstgesuch = new Familiensituation();
		famSitErstgesuch.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		gesuch.getFamiliensituationContainer().setFamiliensituationErstgesuch(famSitErstgesuch);

		gesuch.setKindContainers(new HashSet<>());
		final KindContainer defaultKindContainer = TestDataUtil.createDefaultKindContainer();
		gesuch.getKindContainers().add(defaultKindContainer);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(2, zeitabschnitte.size());

		final VerfuegungZeitabschnitt zeitabschnitt0 = zeitabschnitte.get(0);
		Assertions.assertEquals(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
			zeitabschnitt0.getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(date.withDayOfMonth(31), zeitabschnitt0.getGueltigkeit().getGueltigBis());
		Assertions.assertEquals(0, zeitabschnitt0.getAbzugFamGroesse().intValue());
		Assertions.assertEquals(BigDecimal.valueOf(2.0), zeitabschnitt0.getFamGroesse());

		final VerfuegungZeitabschnitt zeitabschnitt1 = zeitabschnitte.get(1);
		Assertions.assertEquals(date.plusMonths(1).withDayOfMonth(1), zeitabschnitt1.getGueltigkeit().getGueltigAb());
		Assertions.assertEquals(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
			zeitabschnitt1.getGueltigkeit().getGueltigBis());
		Assertions.assertEquals(11400, zeitabschnitt1.getAbzugFamGroesse().intValue());
		Assertions.assertEquals(BigDecimal.valueOf(3.0), zeitabschnitt1.getFamGroesse());
	}

	@Nested
	class KindMitAlternierenderObhutTests {

		@Nested
		class OhneFamilienergaenzenderBetreuungTest {
			@Test
			void kinderAbzugFKJV2_HalbesKindZaehltGanzFuerPauschale() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.HALBER_ABZUG, date);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setGemeinsamesGesuch(false);

				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse =
					famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				double familienMitglieder = famGroesse.getValue();
				Assertions.assertEquals(2.5, familiengroesse, DELTA);
				Assertions.assertEquals(3, familienMitglieder, DELTA);
			}

			@Test
			void kinderAbzugFKJV2_HalbesKindWennKeineGemeinsameGesuch() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
				familiensituation.setGeteilteObhut(Boolean.TRUE);
				familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
				familiensituation.setStartKonkubinat(LocalDate.of(2015, 10, 1));
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.HALBER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
				kind.getKindJA().setGemeinsamesGesuch(false);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse =
					famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(2.5, familiengroesse, DELTA);
			}

			@Test
			void kinderAbzugFKJV2_GanzesKindWennGemeinsamesGesuch() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
				familiensituation.setGeteilteObhut(Boolean.TRUE);
				familiensituation.setStartKonkubinat(LocalDate.of(2015, 10, 1));
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.HALBER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
				kind.getKindJA().setGemeinsamesGesuch(true);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse =
					famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(3, familiengroesse, DELTA);
			}

			@Test
			void kinderAbzugFKJV2_HalbesKindWennKeineUnterhaltsvereinbarungAbgeschlossenUndNichtGemeinsamGesuch() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
				familiensituation.setGeteilteObhut(Boolean.FALSE);
				familiensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG);
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.GANZER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
				kind.getKindJA().setGemeinsamesGesuch(false);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(2.5, familiengroesse, DELTA);
			}
			@Test
			void kinderAbzugFKJV2_GanzesKindWennKeineUnterhaltsvereinbarungAbgeschlossenUndGemeinsamGesuch() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
				familiensituation.setGeteilteObhut(Boolean.FALSE);
				familiensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG);
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.GANZER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
				kind.getKindJA().setGemeinsamesGesuch(true);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(3.0, familiengroesse, DELTA);
			}
			@Test
			void kinderAbzugFKJV2_HalbesKindWennJaUnterhaltsvereinbarungGesuch() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
				familiensituation.setGeteilteObhut(Boolean.FALSE);
				familiensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG);
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.GANZER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(1.5, familiengroesse, DELTA);
			}
			@Test
			void kinderAbzugFKJV2_HalbesKindWennUnterhaltsvereinbarungNichtMoeglichGesuch() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
				familiensituation.setGeteilteObhut(Boolean.FALSE);
				familiensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH);
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.GANZER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(1.5, familiengroesse, DELTA);
			}
		}

		@Nested
		class MitFamilienergaenzenderBetreuungTest {
			@Test
			void kindFKJVMitBetreuungUndFamSitAlleineShouldCountHalf() {
				Gesuch gesuch = createGesuchWithOneGS();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
				familiensituation.setGeteilteObhut(Boolean.TRUE);
				familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.HALBER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(true);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse =
					famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(1.5, familiengroesse, DELTA);
			}

			@Test
			void kindFKJVMitBetreuungNichtGemeinsamenGesuchUndFamSitZuZweitShouldCountHalf() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
				familiensituation.setGeteilteObhut(Boolean.TRUE);
				familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.HALBER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(true);
				kind.getKindJA().setGemeinsamesGesuch(false);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse =
					famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(2.5, familiengroesse, DELTA);
			}

			@Test
			void kindFKJVMitBetreuungGemeinsamenGesuchUndFamSitZuZweitShouldCountFull() {
				Gesuch gesuch = createGesuchWithTwoGesuchsteller();
				// prepare famsit, so gemeinsamesGesuch can be answered in the gui
				Objects.requireNonNull(gesuch.getFamiliensituationContainer());
				final Familiensituation familiensituation = gesuch.getFamiliensituationContainer().getFamiliensituationJA();
				Objects.requireNonNull(familiensituation);
				familiensituation.setFkjvFamSit(true);
				familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
				familiensituation.setGeteilteObhut(Boolean.TRUE);
				familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
				Set<KindContainer> kindContainers = new LinkedHashSet<>();
				final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
				KindContainer kind = createKindContainer(Kinderabzug.HALBER_ABZUG, date);
				kind.getKindJA().setObhutAlternierendAusueben(true);
				kind.getKindJA().setFamilienErgaenzendeBetreuung(true);
				kind.getKindJA().setGemeinsamesGesuch(true);
				kindContainers.add(kind);
				gesuch.setKindContainers(kindContainers);

				final Entry<Double, Integer> famGroesse =
					famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
				double familiengroesse = famGroesse.getKey();
				Assertions.assertEquals(3.0, familiengroesse, DELTA);
			}
		}

	}

	@Test
	void kinderAbzugFKJV2_GanzesKindZaehltGanzFuerPauschale() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		Set<KindContainer> kindContainers = new LinkedHashSet<>();
		final LocalDate date = LocalDate.of(2015, Month.MARCH, 25);
		KindContainer kind = createKindContainer(Kinderabzug.GANZER_ABZUG, date);
		kind.getKindJA().setObhutAlternierendAusueben(false);

		kindContainers.add(kind);
		gesuch.setKindContainers(kindContainers);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		Assertions.assertEquals(3, familiengroesse, DELTA);
		Assertions.assertEquals(3, familienMitglieder, DELTA);
	}

	@Test
	void kinderAbzugFKJV2_KindUeber18ZaehltNicht() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		Set<KindContainer> kindContainers = new LinkedHashSet<>();
		final LocalDate u18 = LocalDate.of(2015, Month.MARCH, 25);
		KindContainer kindU18 = createKindContainer(Kinderabzug.GANZER_ABZUG, u18);
		kindU18.getKindJA().setObhutAlternierendAusueben(false);

		final LocalDate over18 = LocalDate.of(2000, Month.MARCH, 25);
		KindContainer kindOver18 = createKindContainer(Kinderabzug.GANZER_ABZUG, over18);
		kindOver18.getKindJA().setInErstausbildung(false);

		kindContainers.add(kindU18);
		kindContainers.add(kindOver18);

		gesuch.setKindContainers(kindContainers);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		Assertions.assertEquals(3, familiengroesse, DELTA);
		Assertions.assertEquals(3, familienMitglieder, DELTA);
	}

	@Test
	public void kinderAbzugFKJV_obhutAlternierend_keienBetreuung_gemeinsamesGesuch() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setObhutAlternierendAusueben(true);
		kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
		kind.getKindJA().setGemeinsamesGesuch(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(3.0));
		assertThat(familienMitglieder, is(3.0));
	}

	@Test
	public void kinderAbzugFKJV_obhutAlternierend_keienBetreuung_keinGemeinsamesGesuch() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setObhutAlternierendAusueben(true);
		kind.getKindJA().setFamilienErgaenzendeBetreuung(false);
		kind.getKindJA().setGemeinsamesGesuch(false);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(2.5));
		assertThat(familienMitglieder, is(3.0));
	}

	@Test
	public void kinderAbzugFKJV_obhutAlternierend_betreuut_verheiratet() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setObhutAlternierendAusueben(true);
		kind.getKindJA().setFamilienErgaenzendeBetreuung(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(2.5));
		assertThat(familienMitglieder, is(3.0));
	}

	@Test
	public void kinderAbzugFKJV_obhutAlternierend_betreuut_alleinerziehnd_gemeinsam() {
		Gesuch gesuch = createGesuchWithOneGS();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setObhutAlternierendAusueben(true);
		kind.getKindJA().setFamilienErgaenzendeBetreuung(true);
		kind.getKindJA().setGemeinsamesGesuch(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(2.0));
		assertThat(familienMitglieder, is(2.0));
	}

	@Test
	public void kinderAbzugFKJV_obhutAlternierend_betreuut_konkubinatMindauerNichtErreicht_Alleine() {
		Gesuch gesuch = createGesuchWithOneGS();
		gesuch.setKindContainers(new LinkedHashSet<>());
		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setStartKonkubinat(LocalDate.now().minusYears(1));

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setObhutAlternierendAusueben(true);
		kind.getKindJA().setFamilienErgaenzendeBetreuung(true);
		kind.getKindJA().setGemeinsamesGesuch(false);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(1.5));
		assertThat(familienMitglieder, is(2.0));
	}

	@Test
	public void kinderAbzugFKJV_obhutAlternierend_betreuut_konkubinatMindauerErreicht() {
		Gesuch gesuch = createGesuchWithOneGS();
		gesuch.setKindContainers(new LinkedHashSet<>());
		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setStartKonkubinat(LocalDate.now().minusYears(3));

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setObhutAlternierendAusueben(true);
		kind.getKindJA().setFamilienErgaenzendeBetreuung(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(1.5));
		assertThat(familienMitglieder, is(2.0));
	}

	@Test
	public void kinderAbzugFKJV_pflegeKind_entschaedigungErhalten() {
		//Pflegekind, für welches Entschädigung für Pflege erhalten wird zählt nicht zur Familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setPflegekind(true);
		kind.getKindJA().setPflegeEntschaedigungErhalten(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(2.0));
		assertThat(familienMitglieder, is(2.0));
	}

	@Test
	public void kinderAbzugFKJV_pflegeKind_keineEntschaedigungErhalten() {
		//Pflegekind, für welches keine Entschädigung für Pflege erhalten wird zählt ganz zur Familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setPflegekind(true);
		kind.getKindJA().setPflegeEntschaedigungErhalten(false);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(3.0));
		assertThat(familienMitglieder, is(3.0));
	}

	@Test
	public void kinderAbzugFKJV_nichtInErstausbildung_unter18() {
		//Kind u18, welches nicht in Erstausbildung ist zählt ganz zur Familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(false);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(3.0));
		assertThat(familienMitglieder, is(3.0));
	}

	@Test
	public void kinderAbzugFKJV_nichtInErstausbildung_ueber18() {
		//Kind ueber 18, welches nicht in Erstausbildung ist zählt nicht zur Familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2000, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(false);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(2.0));
		assertThat(familienMitglieder, is(2.0));
	}

	@Test
	public void kinderAbzugFKJV_inErstausbildung_keineAlimenteBezahlen() {
		//Kind, welches in Erstausbildung ist und keine Alimente bezahlt werden zählt nicht zur familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2000, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(true);
		kind.getKindJA().setAlimenteBezahlen(false);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(2.0));
		assertThat(familienMitglieder, is(2.0));
	}

	@Test
	public void kinderAbzugFKJV_inErstausbildung_alimenteBezahlen_ueber18() {
		//Kind über 18, welches in Erstausbildung ist und Alimente bezahlt werden zählt zur familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2000, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(true);
		kind.getKindJA().setAlimenteBezahlen(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(3.0));
		assertThat(familienMitglieder, is(3.0));
	}

	@Test
	public void kinderAbzugFKJV_inErstausbildung_alimenteBezahlen_unter18() {
		//Kind unter 18, welches in Erstausbildung ist und Alimente bezahlt werden zählt nicht zur familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(true);
		kind.getKindJA().setAlimenteBezahlen(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(2.0));
		assertThat(familienMitglieder, is(2.0));
	}

	@Test
	public void kinderAbzugFKJV_inErstausbildung_keineAlimenteErhalten() {
		//Kind, welches in Erstausbildung ist und keine Alimente erhalten werden zählt zur familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2000, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(true);
		kind.getKindJA().setAlimenteErhalten(false);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(3.0));
		assertThat(familienMitglieder, is(3.0));
	}

	@Test
	public void kinderAbzugFKJV_inErstausbildung_alimenteErhalten_ueber18() {
		//Kind über 18, welches in Erstausbildung ist und Alimente erhalten werden zählt nicht zur familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2000, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(true);
		kind.getKindJA().setAlimenteErhalten(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(2.0));
		assertThat(familienMitglieder, is(2.0));
	}

	@Test
	public void kinderAbzugFKJV_inErstausbildung_alimenteErhalten_unter18() {
		//Kind unter 18, welches in Erstausbildung ist und Alimente erhalten werden zählt ganz zur familiengrösse
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2020, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(true);
		kind.getKindJA().setAlimenteErhalten(true);
		gesuch.getKindContainers().add(kind);

		final Entry<Double, Integer> famGroesse = famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		double familiengroesse = famGroesse.getKey();
		double familienMitglieder = famGroesse.getValue();
		assertThat(familiengroesse, is(3.0));
		assertThat(familienMitglieder, is(3.0));
	}

	@Test()
	public void kinderAbzugFKJV_inErstausbildung_keineFrageZuAlimentenBeantwortet_throwsExcption() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2000, Month.MARCH, 25));
		kind.getKindJA().setInErstausbildung(true);
		gesuch.getKindContainers().add(kind);

		assertThrows(EbeguRuntimeException.class, () -> {
			famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		});
	}

	@Test()
	public void kinderAbzugFKJV_keineFrageBeantwortet_throwsExcption() {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		gesuch.setKindContainers(new LinkedHashSet<>());

		KindContainer kind = createKindContainer(LocalDate.of(2000, Month.MARCH, 25));
		gesuch.getKindContainers().add(kind);

		assertThrows(EbeguRuntimeException.class, () -> {
			famabAbschnittRule_FKJV2.calculateFamiliengroesse(gesuch, LocalDate.now());
		});
	}

	@Nonnull
	private Gesuch createGesuchWithOneGS() {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		GesuchstellerContainer gesuchsteller = new GesuchstellerContainer();
		gesuchsteller.setGesuchstellerJA(new Gesuchsteller());
		gesuch.setGesuchsteller1(gesuchsteller);
		Familiensituation famSit = new Familiensituation();
		famSit.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(famSit);
		return gesuch;
	}

	@Nonnull
	private Gesuch createGesuchWithTwoGesuchsteller() {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		GesuchstellerContainer gesuchsteller = new GesuchstellerContainer();
		gesuchsteller.setGesuchstellerJA(new Gesuchsteller());
		gesuch.setGesuchsteller1(gesuchsteller);
		gesuch.setGesuchsteller2(gesuchsteller);
		Familiensituation famSit = new Familiensituation();
		famSit.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(famSit);
		return gesuch;
	}

	@Nonnull
	private Gesuch createGesuchWithKind(Kinderabzug abzug1, @Nullable Kinderabzug abzug2, LocalDate kindGeburtsdatum) {
		Gesuch gesuch = createGesuchWithTwoGesuchsteller();
		Set<KindContainer> kindContainers = new LinkedHashSet<>();
		kindContainers.add(createKindContainer(abzug1, kindGeburtsdatum));
		if (abzug2 != null) {
			kindContainers.add(createKindContainer(abzug2, kindGeburtsdatum));
		}
		gesuch.setKindContainers(kindContainers);
		return gesuch;
	}

	@Nonnull
	private KindContainer createKindContainer(Kinderabzug abzug, LocalDate kindGeburtsdatum) {
		KindContainer kindContainer = new KindContainer();
		Kind kindJA = new Kind();
		kindJA.setKinderabzugErstesHalbjahr(abzug);
		kindJA.setKinderabzugZweitesHalbjahr(abzug);
		kindJA.setGeburtsdatum(kindGeburtsdatum);
		kindContainer.setKindJA(kindJA);
		return kindContainer;
	}

	@Nonnull
	private KindContainer createKindContainer(LocalDate kindGeburtsdatum) {
		KindContainer kindContainer = new KindContainer();
		Kind kindJA = new Kind();
		kindJA.setGeburtsdatum(kindGeburtsdatum);
		kindContainer.setKindJA(kindJA);
		return kindContainer;
	}

	private Map<EinstellungKey, Einstellung> getEinstellungMapForAsiv() {
		Map<EinstellungKey, Einstellung> einstellungMapForAsiv = new HashMap<>();
		einstellungMapForAsiv.putAll(getDefaultEinstellungMap());
		Einstellung einstellungMinimalKonkubinat = new Einstellung(EinstellungKey.MINIMALDAUER_KONKUBINAT, "5", new Gesuchsperiode());
		einstellungMapForAsiv.put(EinstellungKey.MINIMALDAUER_KONKUBINAT, einstellungMinimalKonkubinat);
		Einstellung einstellungKinderabzugTyp = new Einstellung(EinstellungKey.KINDERABZUG_TYP, KinderabzugTyp.ASIV.name(), new Gesuchsperiode());
		einstellungMapForAsiv.put(EinstellungKey.KINDERABZUG_TYP, einstellungKinderabzugTyp);

		return einstellungMapForAsiv;
	}

	private Map<EinstellungKey, Einstellung> getEinstellungMapForFKJV2() {
		Map<EinstellungKey, Einstellung> einstellungMapForFKJV2 = new HashMap<>();

		einstellungMapForFKJV2.putAll(getDefaultEinstellungMap());
		Einstellung einstellungMinimalKonkubinat = new Einstellung(EinstellungKey.MINIMALDAUER_KONKUBINAT, "2", new Gesuchsperiode());
		einstellungMapForFKJV2.put(EinstellungKey.MINIMALDAUER_KONKUBINAT, einstellungMinimalKonkubinat);
		Einstellung einstellungKinderabzugTyp = new Einstellung(EinstellungKey.KINDERABZUG_TYP, KinderabzugTyp.FKJV_2.name(), new Gesuchsperiode());
		einstellungMapForFKJV2.put(EinstellungKey.KINDERABZUG_TYP, einstellungKinderabzugTyp);

		return einstellungMapForFKJV2;
	}

	private Map<EinstellungKey, Einstellung> getDefaultEinstellungMap() {
		Map<EinstellungKey, Einstellung> defaultEinstellungMap = new HashMap<>();
		Einstellung einstellungPauschalabzugProPersonFamiliengroesse3 =
			new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
				Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS,
				new Gesuchsperiode());
		defaultEinstellungMap.put(
			EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
			einstellungPauschalabzugProPersonFamiliengroesse3);
		Einstellung einstellungPauschalabzugProPersonFamiliengroesse4 =
			new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
				Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS,
				new Gesuchsperiode());
		defaultEinstellungMap.put(
			EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
			einstellungPauschalabzugProPersonFamiliengroesse4);
		Einstellung einstellungPauschalabzugProPersonFamiliengroesse5 =
			new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
				Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS,
				new Gesuchsperiode());
		defaultEinstellungMap.put(
			EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
			einstellungPauschalabzugProPersonFamiliengroesse5);
		Einstellung einstellungPauschalabzugProPersonFamiliengroesse6 =
			new Einstellung(EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
				Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS,
				new Gesuchsperiode());
		defaultEinstellungMap.put(
			EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
			einstellungPauschalabzugProPersonFamiliengroesse6);

		return defaultEinstellungMap;
	}
}
