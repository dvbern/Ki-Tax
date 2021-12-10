/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.tests.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testet die "Hacks", welche fuer die Uebergangsloesung der Stadt Bern mit Ki-Tax gemacht wurden:
 * - Vor dem Stichtag (STADT_BERN_ASIV_START_DATUM) wird gemaess FEBR (Ki-Tax) gerechnet
 * - Nach dem Stichtag wird
 * -- falls die ASIV Konfiguration komplett ist (STADT_BERN_ASIV_CONFIGURED) normal gemaess ASIV gerechnet, bzw. inklusive der Gemeinde-spezfischen Wuensche
 * -- falls die Konfiguration noch nicht komplett ist, wird nichts berechnet (Anspruch 0, mit Bemerkung)
 */
public class KitaxUebergangsloesungTest extends AbstractBGRechnerTest {

	private final Gesuch gesuch = prepareGesuch();
	private final BGRechnerParameterDTO parameter = getParameter();
	private final KitaxUebergangsloesungParameter kitaxParameter = TestDataUtil.geKitaxUebergangsloesungParameter();

	// Fall Laura Walther, mit Anpassung Nettolohn GS1 = 0

	// Erwartete Testresultate gemaess FEBR (Ki-Tax)
	private double expectedVollkostenFEBR = 1191.50;
	private double expectedVerguenstigungFEBR = 941.55;
	private double expectedElternbeitragFEBR = 249.95;

	// Erwartete Testresultate gemaess ASIV (ohne Zusatzwuensche)
	private double expectedVollkostenASIV = 2000.00;
	private double expectedVerguenstigungASIV = 828.10;
	private double expectedElternbeitragASIV = expectedVollkostenASIV - expectedVerguenstigungASIV;
	private double expectedElternbeitragMinASIV = 70.00;
	private double expectedElternbeitragMinGekuerztASIV = 0.00;

	// Zusatzwuensche der Stadt Bern fuer ASIV
	private double expectedZusatzgutscheinBern = 110;
	private double expectedVerguenstigungBernASIV = expectedVerguenstigungASIV + expectedZusatzgutscheinBern;
	private double expectedElternbeitragBernASIV = expectedElternbeitragASIV - expectedZusatzgutscheinBern;

	private int anspruchOhneZuschlag = 50;
	private int anspruchFEBR = anspruchOhneZuschlag + 20;
	private int anspruchASIV = anspruchOhneZuschlag + 20;


	@Test
	public void uebergangsloesungStadtBern() {
		// Konfigurationen fuer Zusatzwuensche Bern
		parameter.getGemeindeParameter().setGemeindeZusaetzlicherGutscheinEnabled(true);
		parameter.getGemeindeParameter().setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita(EinschulungTyp.KINDERGARTEN1);
		parameter.getGemeindeParameter().setGemeindeZusaetzlicherGutscheinBetragKita(MathUtil.DEFAULT.from(11));

		// "Normalfall": Bern wechselt unter dem Jahr zu ASIV, die Konfiguration ist noch nicht klar
		kitaxParameter.setStadtBernAsivConfiguered(false);
		kitaxParameter.setStadtBernAsivStartDate(LocalDate.of(2021, Month.JANUARY, 1));
		List<VerfuegungZeitabschnitt> abschnitte = evaluateGesuch(parameter, kitaxParameter);

		// Wir erwarten fuer die Monate vor dem Stichtag eine Berechnung nach Ki-Tax, die ASIV Werte muessen immer 0 sein
		VerfuegungZeitabschnitt august = abschnitte.get(0);
		BGCalculationResult augustGemeinde = august.getBgCalculationResultGemeinde();
		BGCalculationResult augustAsiv = august.getBgCalculationResultAsiv();
		Assert.assertNotNull(augustGemeinde);
		assertResult(augustGemeinde, anspruchFEBR, expectedVollkostenFEBR, expectedVerguenstigungFEBR, expectedElternbeitragFEBR, 0, 0);
		assertResult(augustAsiv, 0, 0, 0, 0, 0, 0);

		// Bis und mit Dezember erwarten wir dieselben Resultate
		assertAbschnitteSameData(august, abschnitte.get(1), abschnitte.get(2), abschnitte.get(3), abschnitte.get(4));

		// Ab Januar erwarten wir keinen Anspruch, da die Konfiguration noch nicht gesetzt ist
		VerfuegungZeitabschnitt januar = abschnitte.get(5);
		BGCalculationResult januarGemeinde = januar.getBgCalculationResultGemeinde();
		BGCalculationResult januarAsiv = januar.getBgCalculationResultAsiv();
		Assert.assertNotNull(januarGemeinde);
		assertResult(januarGemeinde, 0, 0, 0, 0, 0, 0);
		assertResult(januarAsiv, 0, 0, 0, 0, 0, 0);

		// Fuer den Rest des Jahres bleiben die Werte gleich
		assertAbschnitteSameData(januar, abschnitte.get(6), abschnitte.get(7), abschnitte.get(8), abschnitte.get(9), abschnitte.get(10), abschnitte.get(11));

		// Jetzt setzen wir das Flag auf TRUE, damit sollten sie Monate nach dem Stichtag normal nach ASIV berechnet werden
		kitaxParameter.setStadtBernAsivConfiguered(true);
		kitaxParameter.setStadtBernAsivStartDate(LocalDate.of(2021, Month.JANUARY, 1));
		abschnitte = evaluateGesuch(parameter, kitaxParameter);

		// Ab Januar erwarten wir die Berechnung nach ASIV
		januar = abschnitte.get(5);
		januarGemeinde = januar.getBgCalculationResultGemeinde();
		januarAsiv = januar.getBgCalculationResultAsiv();
		Assert.assertNotNull(januarGemeinde);
		assertResult(januarGemeinde, anspruchASIV, expectedVollkostenASIV, expectedVerguenstigungBernASIV, expectedElternbeitragBernASIV,
			expectedElternbeitragMinASIV, expectedElternbeitragMinGekuerztASIV);
		assertResult(januarAsiv, anspruchASIV, expectedVollkostenASIV, expectedVerguenstigungASIV, expectedElternbeitragASIV,
			expectedElternbeitragMinASIV, expectedElternbeitragMinGekuerztASIV);
	}

	@Nonnull
	private Gesuch prepareGesuch() {
		FinanzielleSituationBernRechner finanzielleSituationBernRechner = new FinanzielleSituationBernRechner();
		Gesuch lauraWalther = TestDataUtil.createTestgesuchLauraWalther(TestDataUtil.createCustomGesuchsperiode(2020, 2021), finanzielleSituationBernRechner);
		Assert.assertNotNull(lauraWalther.getGesuchsteller1());
		Assert.assertNotNull(lauraWalther.getGesuchsteller1().getFinanzielleSituationContainer());
		lauraWalther.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(BigDecimal.ZERO);
		lauraWalther.getDossier().setGemeinde(TestDataUtil.createGemeindeParis());
		TestDataUtil.calculateFinanzDaten(lauraWalther, finanzielleSituationBernRechner);
		return lauraWalther;
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> evaluateGesuch(@Nonnull BGRechnerParameterDTO parameter, @Nonnull KitaxUebergangsloesungParameter kitaxParameter) {
		evaluator.evaluate(gesuch, parameter, kitaxParameter, Constants.DEFAULT_LOCALE);
		Assert.assertNotNull(gesuch.getKindContainers());
		Assert.assertEquals(1, gesuch.getKindContainers().size());
		KindContainer kind = gesuch.getKindContainers().iterator().next();
		Assert.assertNotNull(kind.getBetreuungen());
		Assert.assertEquals(1, kind.getBetreuungen().size());
		Betreuung betreuung = kind.getBetreuungen().iterator().next();
		Verfuegung verfuegung = betreuung.getVerfuegungOrVerfuegungPreview();
		Assert.assertNotNull(verfuegung);
		Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());
		return verfuegung.getZeitabschnitte();
	}

	private void assertResult(@Nonnull BGCalculationResult result, int expectedAnspruch, double expectedVollkosten, double expectedVerguenstigung,
		double expectedElternbeitrag, double expectedMinElternbeitrag, double expectedMinElternbeitragGekuerzt) {
		Assert.assertEquals(expectedAnspruch, result.getAnspruchspensumProzent());
		Assert.assertTrue(MathUtil.isSame(MathUtil.DEFAULT.from(expectedVollkosten), result.getVollkosten()));
		Assert.assertTrue(MathUtil.isSame(MathUtil.DEFAULT.from(expectedVerguenstigung), result.getVerguenstigungOhneBeruecksichtigungVollkosten()));
		Assert.assertTrue(MathUtil.isSame(MathUtil.DEFAULT.from(expectedVerguenstigung), result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()));
		Assert.assertTrue(MathUtil.isSame(MathUtil.DEFAULT.from(expectedVerguenstigung), result.getVerguenstigung()));
		Assert.assertTrue(MathUtil.isSame(MathUtil.DEFAULT.from(expectedElternbeitrag), result.getElternbeitrag()));
		Assert.assertTrue(MathUtil.isSame(MathUtil.DEFAULT.from(expectedMinElternbeitrag), result.getMinimalerElternbeitrag()));
		Assert.assertTrue(MathUtil.isSame(MathUtil.DEFAULT.from(expectedMinElternbeitragGekuerzt), result.getMinimalerElternbeitragGekuerzt()));
	}

	private void assertAbschnitteSameData(@Nonnull VerfuegungZeitabschnitt... abschnitte) {
		VerfuegungZeitabschnitt last = abschnitte[0];
		BGCalculationResult lastResultGemeinde = last.getBgCalculationResultGemeinde();
		BGCalculationResult lastResultAsiv = last.getBgCalculationResultAsiv();
		Assert.assertNotNull(lastResultGemeinde);
		Assert.assertNotNull(lastResultAsiv);
		for (VerfuegungZeitabschnitt zeitabschnitt : abschnitte) {
			BGCalculationResult currentResultGemeinde = zeitabschnitt.getBgCalculationResultGemeinde();
			BGCalculationResult currentResultAsiv = zeitabschnitt.getBgCalculationResultAsiv();
			Assert.assertTrue(lastResultGemeinde.isSame(currentResultGemeinde));
			Assert.assertTrue(lastResultAsiv.isSame(currentResultAsiv));
		}
	}
}
