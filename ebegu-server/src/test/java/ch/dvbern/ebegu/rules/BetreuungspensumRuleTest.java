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
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.initalizer.RestanspruchInitializer;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculate;
import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculateWithRemainingRestanspruch;

/**
 * Tests für Betreuungspensum-Regel
 */
public class BetreuungspensumRuleTest {

	private final RestanspruchInitializer restanspruchInitializer = new RestanspruchInitializer();

	@Test
	public void testKitaNormalfall() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 60, new BigDecimal(500.50));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60, 0));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(60, result.get(0).getBetreuungspensum());
		Assert.assertEquals(new BigDecimal(500.50), result.get(0).getMonatlicheBetreuungskosten());
		Assert.assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());
		result = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung, result);
		Assert.assertEquals(0, result.get(0).getAnspruchspensumRest());
	}

	@Test
	public void testKitaZuwenigAnspruch() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 80,  new BigDecimal(200));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60, 0));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(80, result.get(0).getBetreuungspensum());
		Assert.assertEquals(new BigDecimal(200), result.get(0).getMonatlicheBetreuungskosten());
		Assert.assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());
		result = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung, result);
		Assert.assertEquals(0, result.get(0).getAnspruchspensumRest());
	}

	@Test
	public void testKitaMitRestanspruch() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 60, new BigDecimal(500));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 0));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(80), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(60, result.get(0).getBetreuungspensum());
		Assert.assertEquals(80, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(60, result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());
		Assert.assertEquals(new BigDecimal(500), result.get(0).getMonatlicheBetreuungskosten());
		result = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung, result);
		Assert.assertEquals(20, result.get(0).getAnspruchspensumRest());
	}

	@Test
	public void testZweiKitas() {
		Betreuung betreuung1 = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 60, new BigDecimal(600));
		Betreuung betreuung2 = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 40, new BigDecimal(400));
		Assert.assertNotNull(betreuung1.getKind().getGesuch().getGesuchsteller1());
		Assert.assertNotNull(betreuung2.getKind().getGesuch().getGesuchsteller1());
		betreuung1.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 0));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung1);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(80), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(60, result.get(0).getBetreuungspensum());
		Assert.assertEquals(80, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(60, result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());  //restanspruch wurde noch nie berechnet
		// Anspruchsrest fuer naechste Betreuung setzten
		List<VerfuegungZeitabschnitt> abschnForNxtBetr = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung1, result);
		//Nach dem Berechnen des Rests ist der Rest im  im Feld AnspruchspensumRest gesetzt, Anspruchsberechtigtes Pensum ist noch 0 da noch nciht berechnet fuer 2.Betr.
		Assert.assertEquals(20, abschnForNxtBetr.get(0).getAnspruchspensumRest());
		Assert.assertEquals(0, abschnForNxtBetr.get(0).getAnspruchberechtigtesPensum());

		// Kita 2: Reicht nicht mehr ganz
		betreuung2.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 0));
		List<VerfuegungZeitabschnitt> resultBetr2 = calculateWithRemainingRestanspruch(betreuung2, 20);

		Assert.assertNotNull(resultBetr2);
		Assert.assertEquals(1, resultBetr2.size());
		Assert.assertEquals(Integer.valueOf(80), resultBetr2.get(0).getErwerbspensumGS1());
		Assert.assertEquals(40, resultBetr2.get(0).getBetreuungspensum());
		Assert.assertEquals(20, resultBetr2.get(0).getAnspruchberechtigtesPensum()); // Nach der Berechnung des Anspruchs kann der Anspruch nicht hoeher sein als der Restanspruch (20)
		Assert.assertEquals(20, resultBetr2.get(0).getBgPensum());
		Assert.assertEquals(20, resultBetr2.get(0).getAnspruchspensumRest()); //Restanspruch wurde noch nicht neu berechnet fuer naechste betreuung
		resultBetr2 = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung2, resultBetr2);
		Assert.assertEquals(0, resultBetr2.get(0).getAnspruchspensumRest()); // Nach dem initialisieren fuer das nachste Betreuungspensum ist der noch verbleibende restanspruch 0
	}

	@Test
	public void testRestanspruchKitaTagiKita() {
		//Teste ob der Restanspruch richtig berechnet wird wenn die erste Betreuung eine Kita ist, gefolgt von einer Tagi fuer Schulkinder, gefolgt von einer Kita
		Betreuung betreuung1 = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 60, new BigDecimal(600));
		Betreuung betreuung2 = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.TAGI,	40, new BigDecimal(400));
		Betreuung betreuung3 = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.KITA, 40, new BigDecimal(400));
		Assert.assertNotNull(betreuung1.getKind().getGesuch().getGesuchsteller1());
		Assert.assertNotNull(betreuung2.getKind().getGesuch().getGesuchsteller1());
		Assert.assertNotNull(betreuung3.getKind().getGesuch().getGesuchsteller1());
		betreuung1.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 0));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung1);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(80), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(60, result.get(0).getBetreuungspensum());
		Assert.assertEquals(80, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(60, result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());  //restanspruch wurde noch nie berechnet
		// Anspruchsrest fuer naechste Betreuung setzten
		result = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung1, result);
		//Nach dem Berechnen des Rests ist der Rest im  im Feld AnspruchspensumRest gesetzt, Anspruchsberechtigtes Pensum ist noch 0 da noch nciht berechnet fuer 2.Betr.
		Assert.assertEquals(20, result.get(0).getAnspruchspensumRest());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());

		//Tagi fuer Schulkinder, Restanspruch bleibt gleich
		betreuung2.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 0));
		result = calculateWithRemainingRestanspruch(betreuung2, 20);
		Assert.assertEquals(20, result.get(0).getAnspruchspensumRest());  //restanspruch ist immer noch 20%
		// Anspruchsrest fuer naechste Betreuung setzten
		result = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung2, result);
		//Nach dem Berechnen des Rests ist dieser immer noch gleich gross da Tagi fuer Schulkinder keinen Einfluss hat
		Assert.assertEquals(20, result.get(0).getAnspruchspensumRest());

		// Kita 2: Reicht nicht mehr ganz
		betreuung3.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 80, 0));
		result = calculateWithRemainingRestanspruch(betreuung3, 20);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(80), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(40, result.get(0).getBetreuungspensum());
		Assert.assertEquals(20, result.get(0).getAnspruchberechtigtesPensum()); // Nach der Berechnung des Anspruchs kann der Anspruch nicht hoeher sein als der Restanspruch (20)
		String bemerkungen = result.get(0).getBemerkungen();
		Assert.assertNotNull(bemerkungen);
		Assert.assertTrue(bemerkungen.contains("RESTANSPRUCH"));
		Assert.assertEquals(20, result.get(0).getBgPensum());
		Assert.assertEquals(20, result.get(0).getAnspruchspensumRest()); //Restanspruch wurde noch nicht neu berechnet fuer naechste betreuung
		result = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung3, result);
		Assert.assertEquals(0, result.get(0).getAnspruchspensumRest()); // Nach dem initialisieren fuer das nachste Betreuungspensum ist der noch verbleibende restanspruch 0
	}

	@Test
	public void testTagesfamilienOhneErwerbspensum() {
		// Tageseltern Kleinkind haben die gleichen Regeln wie Kita
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.TAGESFAMILIEN, 80, new BigDecimal(800));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(0), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(80, result.get(0).getBetreuungspensum());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(0, result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());
		Assert.assertEquals(new BigDecimal(800), result.get(0).getMonatlicheBetreuungskosten());
		result = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung, result);
		Assert.assertEquals(0, result.get(0).getAnspruchspensumRest());
	}

	@Test
	public void testTagesfamilienMitTiefemErwerbspensum() {
		// Tageseltern Kleinkinder haben die gleichen Regeln wie Kita
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, BetreuungsangebotTyp.TAGESFAMILIEN, 80, new BigDecimal(800));
		Assert.assertNotNull(betreuung.getKind().getGesuch().getGesuchsteller1());
		betreuung.getKind().getGesuch().getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60, 0));
		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(Integer.valueOf(60), result.get(0).getErwerbspensumGS1());
		Assert.assertEquals(80, result.get(0).getBetreuungspensum());
		Assert.assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertEquals(60, result.get(0).getBgPensum());
		Assert.assertEquals(-1, result.get(0).getAnspruchspensumRest());
		Assert.assertEquals(new BigDecimal(800), result.get(0).getMonatlicheBetreuungskosten());
		result = restanspruchInitializer.createVerfuegungsZeitabschnitte(betreuung, result);
		Assert.assertEquals(0, result.get(0).getAnspruchspensumRest());
	}
}
