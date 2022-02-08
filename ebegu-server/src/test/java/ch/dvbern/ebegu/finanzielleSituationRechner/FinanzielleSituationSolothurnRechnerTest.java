/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

public class FinanzielleSituationSolothurnRechnerTest {

	private final FinanzielleSituationSolothurnRechner finSitSoRechner = new FinanzielleSituationSolothurnRechner();

	@Test
	public void testEinGesuchstellerBruttolohnZero() {
		BigDecimal bruttoLohn = BigDecimal.ZERO;
		BigDecimal expectedMassgebendesEinkommen  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituation = createFinSitWithBruttolohn(bruttoLohn);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituation,null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, false);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
	}

	@Test
	public void testEinGesuchstellerBruttolohnNull() {
		BigDecimal bruttoLohn = null;
		BigDecimal expectedMassgebendesEinkommen  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituation = createFinSitWithBruttolohn(bruttoLohn);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituation, null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, false);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
	}

	@Test
	public void testEinGesuchstellerBruttolohn() {
		BigDecimal bruttoLohn = BigDecimal.valueOf(76000);
		BigDecimal expectedMassgebendesEinkommen  = MathUtil.EXACT.multiply(bruttoLohn, BigDecimal.valueOf(0.75));

		FinanzielleSituation finanzielleSituation = createFinSitWithBruttolohn(bruttoLohn);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituation, null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, false);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
	}

	@Test
	public void testZweiGesuchstellerBruttolohn2Zero() {
		BigDecimal bruttoLohn = BigDecimal.ZERO;
		BigDecimal expectedMassgebendesEinkommen  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituation = createFinSitWithBruttolohn(bruttoLohn);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituation, finanzielleSituation);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, true);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	@Test
	public void testZweiGesuchstellerBruttolohn1Zero() {
		BigDecimal bruttoLohnGS1 = BigDecimal.valueOf(46000);
		BigDecimal bruttoLohnGS2 = BigDecimal.ZERO;
		BigDecimal expectedMassgebendesEinkommenGS1  =  MathUtil.EXACT.multiply(bruttoLohnGS1, BigDecimal.valueOf(0.75));
		BigDecimal expectedMassgebendesEinkommenGS2  =  BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithBruttolohn(bruttoLohnGS1);
		FinanzielleSituation finanzielleSituationGS2 = createFinSitWithBruttolohn(bruttoLohnGS2);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, finanzielleSituationGS2);

		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, true);

		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	@Test
	public void testZweiGesuchstellerBruttolohn() {
		BigDecimal bruttoLohnGS1 = BigDecimal.valueOf(64200);
		BigDecimal bruttoLohnGS2 = BigDecimal.valueOf(41300);
		BigDecimal expectedMassgebendesEinkommenGS1  =  MathUtil.EXACT.multiply(bruttoLohnGS1, BigDecimal.valueOf(0.75));
		BigDecimal expectedMassgebendesEinkommenGS2  =  MathUtil.EXACT.multiply(bruttoLohnGS2, BigDecimal.valueOf(0.75));
		BigDecimal expectedMassgebendesEinkommen = MathUtil.EXACT.add(expectedMassgebendesEinkommenGS1, expectedMassgebendesEinkommenGS2);

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithBruttolohn(bruttoLohnGS1);
		FinanzielleSituation finanzielleSituationGS2 = createFinSitWithBruttolohn(bruttoLohnGS2);

		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, finanzielleSituationGS2);

		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, true);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	/**
	 * Nettoeinkommen                              78'000
	 * - Unterhaltsbeitraege                       2'500
	 * - abzuege für Kinder in Ausbildung          1'630
	 * + Steuerbares Vermögen*0.05   23'462*0.05   1'173.10
	 *
	 * = massgebendes Einkommen                   75'043.10
	 */
	@Test
	public void testEinGesuchstellerNettolohn() {
		BigDecimal nettoeinkommen = BigDecimal.valueOf(78000);
		BigDecimal unterhaltsbeitraege = BigDecimal.valueOf(2500);
		BigDecimal abzuegeAusbildung = BigDecimal.valueOf(1630);
		BigDecimal steuerbaresVermoegen = BigDecimal.valueOf(23462);

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.valueOf(75043.10);
		BigDecimal expectedMassgebendesEinkommenGS1  =  BigDecimal.valueOf(75043.10);
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommen, unterhaltsbeitraege, abzuegeAusbildung, steuerbaresVermoegen);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, false);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	@Test
	public void testEinGesuchstellerNettolohnZero() {
		BigDecimal nettoeinkommen = BigDecimal.ZERO;
		BigDecimal unterhaltsbeitraege = BigDecimal.valueOf(2500);
		BigDecimal abzuegeAusbildung = BigDecimal.valueOf(1630);
		BigDecimal steuerbaresVermoegen = BigDecimal.valueOf(1173);

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.ZERO;
		BigDecimal expectedMassgebendesEinkommenGS1  = BigDecimal.ZERO;
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommen, unterhaltsbeitraege, abzuegeAusbildung, steuerbaresVermoegen);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, false);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	@Test
	public void testEinGesuchstellerNettolohnNull() {
		BigDecimal nettoeinkommen = null;
		BigDecimal unterhaltsbeitraege = BigDecimal.valueOf(2500);
		BigDecimal abzuegeAusbildung = BigDecimal.valueOf(1630);
		BigDecimal steuerbaresVermoegen = BigDecimal.valueOf(1173);

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.ZERO;
		BigDecimal expectedMassgebendesEinkommenGS1  = BigDecimal.ZERO;
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommen, unterhaltsbeitraege, abzuegeAusbildung, steuerbaresVermoegen);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, false);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	@Test
	public void testEinGesuchstellerNettolohnParameterNull() {
		BigDecimal nettoeinkommen = BigDecimal.valueOf(68524);
		BigDecimal unterhaltsbeitraege = null;
		BigDecimal abzuegeAusbildung = null;
		BigDecimal steuerbaresVermoegen = null;

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.valueOf(68524);
		BigDecimal expectedMassgebendesEinkommenGS1  = BigDecimal.valueOf(68524);
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommen, unterhaltsbeitraege, abzuegeAusbildung, steuerbaresVermoegen);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, false);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	@Test
	public void testEinGesuchstellerNettolohnParameterZero() {
		BigDecimal nettoeinkommen = BigDecimal.valueOf(68524);
		BigDecimal unterhaltsbeitraege = BigDecimal.ZERO;
		BigDecimal abzuegeAusbildung = BigDecimal.ZERO;
		BigDecimal steuerbaresVermoegen = BigDecimal.ZERO;

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.valueOf(68524);
		BigDecimal expectedMassgebendesEinkommenGS1  = BigDecimal.valueOf(68524);
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommen, unterhaltsbeitraege, abzuegeAusbildung, steuerbaresVermoegen);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, false);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	/**
	 * Nettoeinkommen                              64'251
	 * - Unterhaltsbeitraege                       1'870
	 * - abzuege für Kinder in Ausbildung          0
	 * + Steuerbares Vermögen*0.05   41'205*0.05   2'060.25
	 *
	 * = massgebendes Einkommen                   64'441.25
	 */
	@Test
	public void testZweiGesuchstellerNettolohnGS2Zero() {
		BigDecimal nettoeinkommenGS1 = BigDecimal.valueOf(64251);
		BigDecimal unterhaltsbeitraegeGS1 = BigDecimal.valueOf(1870);
		BigDecimal abzuegeAusbildungGS1 = BigDecimal.valueOf(0);
		BigDecimal steuerbaresVermoegenGS1 = BigDecimal.valueOf(41205);

		BigDecimal nettoeinkommenGS2 = BigDecimal.ZERO;
		BigDecimal unterhaltsbeitraegeGS2 = BigDecimal.valueOf(2500);
		BigDecimal abzuegeAusbildungGS2 = BigDecimal.valueOf(1630);
		BigDecimal steuerbaresVermoegenGS2 = BigDecimal.valueOf(1173);

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.valueOf(64441.25);
		BigDecimal expectedMassgebendesEinkommenGS1  = BigDecimal.valueOf(64441.25);
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommenGS1, unterhaltsbeitraegeGS1, abzuegeAusbildungGS1, steuerbaresVermoegenGS1);
		FinanzielleSituation finanzielleSituationGS2 = createFinSitWithNettolohn(nettoeinkommenGS2, unterhaltsbeitraegeGS2, abzuegeAusbildungGS2, steuerbaresVermoegenGS2);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, finanzielleSituationGS2);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, true);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	@Test
	public void testZweiGesuchstellerNettolohnNoFinSitForGS2() {
		BigDecimal nettoeinkommenGS1 = BigDecimal.valueOf(78000);
		BigDecimal unterhaltsbeitraegeGS1 = BigDecimal.valueOf(2500);
		BigDecimal abzuegeAusbildungGS1 = BigDecimal.valueOf(1630);
		BigDecimal steuerbaresVermoegenGS1 = BigDecimal.valueOf(23462);

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.valueOf(75043.10);
		BigDecimal expectedMassgebendesEinkommenGS1 = BigDecimal.valueOf(75043.10);
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.ZERO;

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommenGS1, unterhaltsbeitraegeGS1, abzuegeAusbildungGS1, steuerbaresVermoegenGS1);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, null);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, true);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}


	/**
	 *
	 * GS1
	 * Nettoeinkommen                              74'504
	 * - Unterhaltsbeitraege                       0
	 * - abzuege für Kinder in Ausbildung          750
	 * + Steuerbares Vermögen*0.05   0*0.05        0
	 * = massgebendes Einkommen                   73'754
	 *
	 * GS2
	 * Nettoeinkommen                              25'841
	 * - Unterhaltsbeitraege                       1'260
	 * - abzuege für Kinder in Ausbildung          0
	 * + Steuerbares Vermögen*0.05   15'785*0.05   789.25
	 * = massgebendes Einkommen                    25'370.25
	 *
	 * Total massgebendes Einkommen = 99'124.25
	 */
	@Test
	public void testZweiGesuchstellerNettolohn() {
		BigDecimal nettoeinkommenGS1 = BigDecimal.valueOf(74504);
		BigDecimal unterhaltsbeitraegeGS1 = BigDecimal.valueOf(0);
		BigDecimal abzuegeAusbildungGS1 = BigDecimal.valueOf(750);
		BigDecimal steuerbaresVermoegenGS1 = BigDecimal.valueOf(0);

		BigDecimal nettoeinkommenGS2 = BigDecimal.valueOf(25841);
		BigDecimal unterhaltsbeitraegeGS2 = BigDecimal.valueOf(1260);
		BigDecimal abzuegeAusbildungGS2 = BigDecimal.valueOf(0);
		BigDecimal steuerbaresVermoegenGS2 = BigDecimal.valueOf(15785);

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.valueOf(99124.25);
		BigDecimal expectedMassgebendesEinkommenGS1  = BigDecimal.valueOf(73754);
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.valueOf(25370.25);

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommenGS1, unterhaltsbeitraegeGS1, abzuegeAusbildungGS1, steuerbaresVermoegenGS1);
		FinanzielleSituation finanzielleSituationGS2 = createFinSitWithNettolohn(nettoeinkommenGS2, unterhaltsbeitraegeGS2, abzuegeAusbildungGS2, steuerbaresVermoegenGS2);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, finanzielleSituationGS2);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, true);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}

	/**
	 *
	 * GS1
	 * Nettoeinkommen                              74'504
	 * - Unterhaltsbeitraege                       0
	 * - abzuege für Kinder in Ausbildung          750
	 * + Steuerbares Vermögen*0.05   0*0.05        0
	 * = massgebendes Einkommen                   73'754
	 *
	 * GS2
	 * Bruttoeinkommen*0.05 46'749 * 0.75          35'061.75
	 *
	 * Total massgebendes Einkommen = 108'815.75
	 */
	@Test
	public void testZweiGesuchstellerNettolohnAndBruttolohn() {
		BigDecimal nettoeinkommenGS1 = BigDecimal.valueOf(74504);
		BigDecimal unterhaltsbeitraegeGS1 = BigDecimal.valueOf(0);
		BigDecimal abzuegeAusbildungGS1 = BigDecimal.valueOf(750);
		BigDecimal steuerbaresVermoegenGS1 = BigDecimal.valueOf(0);

		BigDecimal bruttoeinkommenGS2 = BigDecimal.valueOf(46749);

		BigDecimal expectedMassgebendesEinkommen = BigDecimal.valueOf(108815.75);
		BigDecimal expectedMassgebendesEinkommenGS1  = BigDecimal.valueOf(73754);
		BigDecimal expectedMassgebendesEinkommenGS2  = BigDecimal.valueOf(35061.75);

		FinanzielleSituation finanzielleSituationGS1 = createFinSitWithNettolohn(nettoeinkommenGS1, unterhaltsbeitraegeGS1, abzuegeAusbildungGS1, steuerbaresVermoegenGS1);
		FinanzielleSituation finanzielleSituationGS2 = createFinSitWithBruttolohn(bruttoeinkommenGS2);
		Gesuch gesuch = prepareGesuchWithFinSit(finanzielleSituationGS1, finanzielleSituationGS2);
		FinanzielleSituationResultateDTO resultat = finSitSoRechner.calculateResultateFinanzielleSituation(gesuch, true);

		Assert.assertEquals(expectedMassgebendesEinkommen.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGr().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS1.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS1().stripTrailingZeros());
		Assert.assertEquals(expectedMassgebendesEinkommenGS2.stripTrailingZeros(), resultat.getMassgebendesEinkVorAbzFamGrGS2().stripTrailingZeros());
	}



	private FinanzielleSituation createFinSitWithBruttolohn(BigDecimal bruttoLohn) {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setSteuerveranlagungErhalten(false);
		finanzielleSituation.setBruttoLohn(bruttoLohn);
		return finanzielleSituation;
	}

	private FinanzielleSituation createFinSitWithNettolohn(BigDecimal nettolohn, BigDecimal unterhaltsbeitraege, BigDecimal abzuegeAusbildung, BigDecimal steuerbaresVermoegen) {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setSteuerveranlagungErhalten(true);
		finanzielleSituation.setNettolohn(nettolohn);
		finanzielleSituation.setUnterhaltsBeitraege(unterhaltsbeitraege);
		finanzielleSituation.setAbzuegeKinderAusbildung(abzuegeAusbildung);
		finanzielleSituation.setSteuerbaresVermoegen(steuerbaresVermoegen);
		return finanzielleSituation;
	}

	private Gesuch prepareGesuchWithFinSit(FinanzielleSituation finanzielleSituationGS1, FinanzielleSituation finanzielleSituationGS2) {
		Gesuch gesuch = new Gesuch();

		gesuch.setGesuchsteller1(prepareGesuchstellerContainer(finanzielleSituationGS1));

		if(finanzielleSituationGS2 != null) {
			gesuch.setGesuchsteller2(prepareGesuchstellerContainer(finanzielleSituationGS2));
		}

		return gesuch;
	}

	private GesuchstellerContainer prepareGesuchstellerContainer(FinanzielleSituation finanzielleSituation) {
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();

		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituation);
		gesuchstellerContainer.setFinanzielleSituationContainer(finanzielleSituationContainer);
		return gesuchstellerContainer;
	}

}
