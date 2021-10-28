/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;

/**
 * Tests für AusserordentlichenAnspruch-Regel
 */
public class AusserordentlicherAnspruchRuleTest {

	@Test
	public void ausserordentlicherAnspruchNormalfall() {
		Betreuung betreuung = createBetreuung(60, 10, 30);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assert.assertEquals(Integer.valueOf(10), zeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(30, zeitabschnitt.getBgCalculationInputAsiv().getAusserordentlicherAnspruch());
		Assert.assertEquals(MathUtil.DEFAULT.from(60), zeitabschnitt.getBetreuungspensumProzent());
		Assert.assertEquals(30, zeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(30), zeitabschnitt.getBgPensum());
	}

	@Test
	public void ausserordentlicherAnspruchMehrAlsBetreuung() {
		Betreuung betreuung = createBetreuung(30, 10, 60);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assert.assertEquals(Integer.valueOf(10), zeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(60, zeitabschnitt.getBgCalculationInputAsiv().getAusserordentlicherAnspruch());
		Assert.assertEquals(MathUtil.DEFAULT.from(30), zeitabschnitt.getBetreuungspensumProzent());
		Assert.assertEquals(60, zeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(30), zeitabschnitt.getBgPensum());
	}

	@Test
	public void ausserordentlicherAnspruchWenigerAlsBerechneterAnspruch() {
		Betreuung betreuung = createBetreuung(80, 60, 30);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assert.assertEquals(Integer.valueOf(60), zeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(0, zeitabschnitt.getBgCalculationInputAsiv().getAusserordentlicherAnspruch());
		Assert.assertEquals(MathUtil.DEFAULT.from(80), zeitabschnitt.getBetreuungspensumProzent());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, zeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS), zeitabschnitt.getBgPensum());
	}

	@Test
	public void effektivesPensumGleichMinErforderlichesPensum() {
		Betreuung betreuung = createBetreuung(60, MIN_ERWERBSPENSUM_NICHT_EINGESCHULT , 30);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assert.assertEquals(Integer.valueOf(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT), zeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(30, zeitabschnitt.getBgCalculationInputAsiv().getAusserordentlicherAnspruch());
		Assert.assertEquals(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, zeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS), zeitabschnitt.getBgPensum());
	}

	@Test
	public void effektivesPensumKleinerMinErforderlichesPensum() {
		Betreuung betreuung = createBetreuung(60, 5, 30);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assert.assertEquals(Integer.valueOf(5), zeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(0, zeitabschnitt.getBgCalculationInputAsiv().getAusserordentlicherAnspruch());
		Assert.assertEquals(0 , zeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(0 ), zeitabschnitt.getBgPensum());
	}

	@Test
	public void effektivesPensumGroesserMinErforderlichesPensum() {
		Betreuung betreuung = createBetreuung(60, 10, 30);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assert.assertEquals(Integer.valueOf(10), zeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1());
		Assert.assertEquals(30, zeitabschnitt.getBgCalculationInputAsiv().getAusserordentlicherAnspruch());
		Assert.assertEquals(30 , zeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertEquals(MathUtil.DEFAULT.from(30 ), zeitabschnitt.getBgPensum());
	}

	private Betreuung createBetreuung(int betreuungspensum, int erwerbspensum, int ausserordentlicherAnspruch) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
			BetreuungsangebotTyp.KITA, betreuungspensum, new BigDecimal(2000));
		ErwerbspensumContainer erwerbspensumContainer =
			TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, erwerbspensum);
		GesuchstellerContainer gesuchsteller1 = betreuung.extractGesuch().getGesuchsteller1();
		Assert.assertNotNull(gesuchsteller1);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensumContainer);
		betreuung.getKind().getKindJA().setPensumAusserordentlicherAnspruch(
			TestDataUtil.createAusserordentlicherAnspruch(ausserordentlicherAnspruch));
		return betreuung;
	}
}
