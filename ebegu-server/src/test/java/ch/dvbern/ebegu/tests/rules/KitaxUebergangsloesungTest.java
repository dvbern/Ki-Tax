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
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testet die "Hacks", welche fuer die Uebergangsloesung der Stadt Bern mit Ki-Tax gemacht wurden
 */
public class KitaxUebergangsloesungTest extends AbstractBGRechnerTest {

	private final Gesuch gesuch = prepareGesuch();
	private final BGRechnerParameterDTO parameter = getParameter();


	@Test
	public void uebergangsloesungStadtBern() {
		// "Normalfall": Bern wechselt unter dem Jahr zu ASIV, die Konfiguration ist noch nicht klar
		parameter.setStadtBernAsivConfiguered(false);
		parameter.setStadtBernAsivStartDate(LocalDate.of(2021, Month.JANUARY, 1));
		List<VerfuegungZeitabschnitt> abschnitte = evaluateGesuch(parameter);

		// Wir erwarten fuer die Monate vor dem Stichtag eine Berechnung nach Ki-Tax, die ASIV Werte muessen immer 0 sein
		VerfuegungZeitabschnitt august = abschnitte.get(0);
		BGCalculationResult augustGemeinde = august.getBgCalculationResultGemeinde();
		BGCalculationResult augustAsiv = august.getBgCalculationResultAsiv();
		Assert.assertNotNull(augustGemeinde);
		assertResult(augustGemeinde, 50, 1162.70, 920.15, 242.55);
		assertResult(augustAsiv, 0, 0, 0, 0);

		// Bis und mit Dezember erwarten wir dieselben Resultate
		assertAbschnitteSameData(august, abschnitte.get(1), abschnitte.get(2), abschnitte.get(3), abschnitte.get(4));

		// Ab Januar erwarten wir keinen Anspruch, da die Konfiguration noch nicht gesetzt ist
		VerfuegungZeitabschnitt januar = abschnitte.get(5);
		BGCalculationResult januarGemeinde = januar.getBgCalculationResultGemeinde();
		BGCalculationResult januarAsiv = januar.getBgCalculationResultAsiv();
		Assert.assertNotNull(januarGemeinde);
		assertResult(januarGemeinde, 0, 0, 0, 0);
		assertResult(januarAsiv, 0, 0, 0, 0);

		// Fuer den Rest des Jahres bleiben die Werte gleich
		assertAbschnitteSameData(januar, abschnitte.get(6), abschnitte.get(7), abschnitte.get(8), abschnitte.get(9), abschnitte.get(10), abschnitte.get(11));

		// Jetzt setzen wir das Flag auf TRUE, damit sollten sie Monate nach dem Stichtag normal nach ASIV berechnet werden
		parameter.setStadtBernAsivConfiguered(true);
		parameter.setStadtBernAsivStartDate(LocalDate.of(2021, Month.JANUARY, 1));
		abschnitte = evaluateGesuch(parameter);

		// Ab Januar erwarten wir die Berechnung nach ASIV
		januar = abschnitte.get(5);
		januarGemeinde = januar.getBgCalculationResultGemeinde();
		januarAsiv = januar.getBgCalculationResultAsiv();
		Assert.assertNotNull(januarGemeinde);
		assertResult(januarGemeinde, 70, 2000, 938.10, 0);
		assertResult(januarAsiv, 70, 2000, 938.10, 0);
	}

	@Nonnull
	private Gesuch prepareGesuch() {
		Gesuch lauraWalther = TestDataUtil.createTestgesuchLauraWalther(TestDataUtil.createCustomGesuchsperiode(2020, 2021));
		Assert.assertNotNull(lauraWalther.getGesuchsteller1());
		Assert.assertNotNull(lauraWalther.getGesuchsteller1().getFinanzielleSituationContainer());
		lauraWalther.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(BigDecimal.ZERO);
		lauraWalther.getDossier().setGemeinde(TestDataUtil.createGemeindeParis());
		return lauraWalther;
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> evaluateGesuch(@Nonnull BGRechnerParameterDTO parameter) {
		evaluator.evaluate(gesuch, parameter, Constants.DEFAULT_LOCALE);
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
		double expectedElternbeitrag) {
		Assert.assertEquals(expectedAnspruch, result.getAnspruchspensumProzent());
		Assert.assertEquals(MathUtil.DEFAULT.from(expectedVollkosten), result.getVollkosten());
		Assert.assertEquals(MathUtil.DEFAULT.from(expectedVerguenstigung), result.getVerguenstigungOhneBeruecksichtigungVollkosten());
		Assert.assertEquals(MathUtil.DEFAULT.from(expectedVerguenstigung), result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag());
		Assert.assertEquals(MathUtil.DEFAULT.from(expectedVerguenstigung), result.getVerguenstigung());
		Assert.assertEquals(MathUtil.DEFAULT.from(expectedElternbeitrag), result.getElternbeitrag());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), result.getMinimalerElternbeitrag());
		Assert.assertEquals(MathUtil.DEFAULT.from(0), result.getMinimalerElternbeitragGekuerzt());
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
