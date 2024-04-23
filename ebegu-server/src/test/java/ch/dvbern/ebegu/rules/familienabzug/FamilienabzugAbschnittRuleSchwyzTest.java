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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FamilienabzugAbschnittRuleSchwyzTest {

	private final FamilienabzugAbschnittRuleSchwyz famabAbschnittRule_SCHWYZ =
		new FamilienabzugAbschnittRuleSchwyz(
			getEinstellungMapForSchwyz(),
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE);

	@Test
	public void singleGS_kinderZaehltGanz_test() {
		Gesuch gesuch = createGesuchWithOneGesuchstellerAndOneKinderWithAbzug(Kinderabzug.GANZER_ABZUG);
		final Entry<Double, Integer> famGroesse =
			famabAbschnittRule_SCHWYZ.calculateFamiliengroesse(gesuch, LocalDate.now());
		double famGrBeruecksichtigungAbzug = famGroesse.getKey();
		double famGrAnzahlEltern = famGroesse.getValue();
		Assertions.assertEquals(2.0, famGrBeruecksichtigungAbzug);
		Assertions.assertEquals(1.0, famGrAnzahlEltern);
	}

	@Test
	public void singleGS_kinderZaehltHalb_test() {
		Gesuch gesuch = createGesuchWithOneGesuchstellerAndOneKinderWithAbzug(Kinderabzug.HALBER_ABZUG);
		final Entry<Double, Integer> famGroesse =
			famabAbschnittRule_SCHWYZ.calculateFamiliengroesse(gesuch, LocalDate.now());
		double famGrBeruecksichtigungAbzug = famGroesse.getKey();
		double famGrAnzahlEltern = famGroesse.getValue();
		Assertions.assertEquals(1.5, famGrBeruecksichtigungAbzug);
		Assertions.assertEquals(1.0, famGrAnzahlEltern);
	}

	@Test
	public void zweiGS_kinderZaehltGanz_test() {
		Gesuch gesuch = createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(Kinderabzug.GANZER_ABZUG);
		final Entry<Double, Integer> famGroesse =
			famabAbschnittRule_SCHWYZ.calculateFamiliengroesse(gesuch, LocalDate.now());
		double famGrBeruecksichtigungAbzug = famGroesse.getKey();
		double famGrAnzahlEltern = famGroesse.getValue();
		Assertions.assertEquals(3.0, famGrBeruecksichtigungAbzug);
		Assertions.assertEquals(2.0, famGrAnzahlEltern);
	}

	@Test
	public void zweiGS_kinderZaehltHalb_test() {
		Gesuch gesuch = createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(Kinderabzug.HALBER_ABZUG);
		final Entry<Double, Integer> famGroesse =
			famabAbschnittRule_SCHWYZ.calculateFamiliengroesse(gesuch, LocalDate.now());
		double famGrBeruecksichtigungAbzug = famGroesse.getKey();
		double famGrAnzahlEltern = famGroesse.getValue();
		Assertions.assertEquals(2.5, famGrBeruecksichtigungAbzug);
		Assertions.assertEquals(2.0, famGrAnzahlEltern);
	}

	@Test
	public void singleGS_zweiKinderZaehltGanz_test() {
		Gesuch gesuch = createGesuchWithOneGesuchstellerAndOneKinderWithAbzug(Kinderabzug.GANZER_ABZUG);
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		gesuch.getKindContainers().add(kind);
		final Entry<Double, Integer> famGroesse =
			famabAbschnittRule_SCHWYZ.calculateFamiliengroesse(gesuch, LocalDate.now());
		double famGrBeruecksichtigungAbzug = famGroesse.getKey();
		double famGrAnzahlEltern = famGroesse.getValue();
		Assertions.assertEquals(3.0, famGrBeruecksichtigungAbzug);
		Assertions.assertEquals(1.0, famGrAnzahlEltern);
	}

	@Test
	public void zweiGS_zweiKinderZaehltGanz_test() {
		Gesuch gesuch = createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(Kinderabzug.GANZER_ABZUG);
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		gesuch.getKindContainers().add(kind);
		final Entry<Double, Integer> famGroesse =
			famabAbschnittRule_SCHWYZ.calculateFamiliengroesse(gesuch, LocalDate.now());
		double famGrBeruecksichtigungAbzug = famGroesse.getKey();
		double famGrAnzahlEltern = famGroesse.getValue();
		Assertions.assertEquals(4.0, famGrBeruecksichtigungAbzug);
		Assertions.assertEquals(2.0, famGrAnzahlEltern);
	}

	@Test
	public void singleGS_zweiKinderEineZaehltHalb_test() {
		Gesuch gesuch = createGesuchWithOneGesuchstellerAndOneKinderWithAbzug(Kinderabzug.GANZER_ABZUG);
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.HALBER_ABZUG);
		gesuch.getKindContainers().add(kind);
		final Entry<Double, Integer> famGroesse =
			famabAbschnittRule_SCHWYZ.calculateFamiliengroesse(gesuch, LocalDate.now());
		double famGrBeruecksichtigungAbzug = famGroesse.getKey();
		double famGrAnzahlEltern = famGroesse.getValue();
		Assertions.assertEquals(2.5, famGrBeruecksichtigungAbzug);
		Assertions.assertEquals(1.0, famGrAnzahlEltern);
	}

	@Test
	public void zweiGS_zweiKinderEineZaehltHalb_test() {
		Gesuch gesuch = createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(Kinderabzug.GANZER_ABZUG);
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.HALBER_ABZUG);
		gesuch.getKindContainers().add(kind);
		final Entry<Double, Integer> famGroesse =
			famabAbschnittRule_SCHWYZ.calculateFamiliengroesse(gesuch, LocalDate.now());
		double famGrBeruecksichtigungAbzug = famGroesse.getKey();
		double famGrAnzahlEltern = famGroesse.getValue();
		Assertions.assertEquals(3.5, famGrBeruecksichtigungAbzug);
		Assertions.assertEquals(2.0, famGrAnzahlEltern);
	}

	@ParameterizedTest
	@CsvSource({
		"1.0, 1, 0",
		"1.5, 1, 3350",
		"2.0, 1, 6700",
		"2.5, 1, 10050",
		"3.0, 1, 13400",
		"2.0, 2, 0",
		"2.5, 2, 3350",
		"3.0, 2, 6700",
		"3.5, 2, 10050",
		"4.0, 2, 13400",
	})
	public void calculateAbzugAufgrundFamiliengroesseTest(
		double famGrBeruecksichtigungAbzug,
		int famGrAnzahlEltern,
		BigDecimal resultExpected) {
		BigDecimal totalAbzuege =
			famabAbschnittRule_SCHWYZ.calculateAbzugAufgrundFamiliengroesse(famGrBeruecksichtigungAbzug, famGrAnzahlEltern);
		Assertions.assertEquals(resultExpected, totalAbzuege);
	}

	@Nonnull
	private Gesuch createGesuchWithOneGesuchstellerAndOneKinderWithAbzug(Kinderabzug kinderabzug) {
		return createGesuchWithKinderWithAbzug(false, kinderabzug);
	}

	@Nonnull
	private Gesuch createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(Kinderabzug kinderabzug) {
		return createGesuchWithKinderWithAbzug(true, kinderabzug);
	}

	@Nonnull
	private Gesuch createGesuchWithKinderWithAbzug(boolean zweiGS, Kinderabzug kinderabzug) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		GesuchstellerContainer gesuchsteller = new GesuchstellerContainer();
		gesuchsteller.setGesuchstellerJA(new Gesuchsteller());
		gesuch.setGesuchsteller1(gesuchsteller);
		if (zweiGS) {
			gesuch.setGesuchsteller2(gesuchsteller);
		}
		Familiensituation famSit = new Familiensituation();
		famSit.setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		famSit.setGesuchstellerKardinalitaet(zweiGS ?
			EnumGesuchstellerKardinalitaet.ZU_ZWEIT :
			EnumGesuchstellerKardinalitaet.ALLEINE);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(famSit);
		gesuch.setKindContainers(new HashSet<>());
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.getKindJA().setKinderabzugErstesHalbjahr(kinderabzug);
		gesuch.getKindContainers().add(kind);
		return gesuch;
	}

	private Map<EinstellungKey, Einstellung> getEinstellungMapForSchwyz() {
		Map<EinstellungKey, Einstellung> einstellungMapForSchwyz = new HashMap<>();
		Einstellung einstellungMinimalKonkubinat =
			new Einstellung(EinstellungKey.MINIMALDAUER_KONKUBINAT, "2", new Gesuchsperiode());
		einstellungMapForSchwyz.put(EinstellungKey.MINIMALDAUER_KONKUBINAT, einstellungMinimalKonkubinat);
		Einstellung einstellungKinderabzugTyp =
			new Einstellung(EinstellungKey.KINDERABZUG_TYP, KinderabzugTyp.SCHWYZ.name(), new Gesuchsperiode());
		einstellungMapForSchwyz.put(EinstellungKey.KINDERABZUG_TYP, einstellungKinderabzugTyp);

		return einstellungMapForSchwyz;
	}
}
