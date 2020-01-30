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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rechner.TagesschuleRechner;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TagesschuleBetreuungszeitAbschnittRuleTest extends AbstractBGRechnerTest {

	private TagesschuleRechner rechner = new TagesschuleRechner();
	private Gesuch gesuch;
	private AnmeldungTagesschule anmeldungTagesschule;

	private static final MathUtil MATH = MathUtil.DEFAULT;

	@Before
	public void setUp() {
		gesuch = TestDataUtil.createTestgesuchDagmar();
		KindContainer kindContainer = gesuch.getKindContainers().iterator().next();
		anmeldungTagesschule = TestDataUtil.createAnmeldungTagesschule(kindContainer, gesuch.getGesuchsperiode());//initAnmedlungTagesschule();
	}

	@Test
	public void testEinkommen120000(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(120000);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		TSCalculationResult resultMitBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuung);
		Assert.assertNotNull(resultOhneBetreuung);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(120000)), zeitabschnitt.getMassgebendesEinkommen());

		Assert.assertEquals("07:30", resultMitBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(8.32)), resultMitBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20.00)), resultMitBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(82.40)), resultMitBetreuung.getTotalKostenProWoche());

		Assert.assertEquals("07:30", resultOhneBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(4.29)), resultOhneBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(52.18)), resultOhneBetreuung.getTotalKostenProWoche());
	}

	@Test
	public void testEinkommen100000(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(100000);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		TSCalculationResult resultMitBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuung);
		Assert.assertNotNull(resultOhneBetreuung);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(100000)), zeitabschnitt.getMassgebendesEinkommen());

		Assert.assertEquals("07:30", resultMitBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(6.36)), resultMitBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultMitBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(67.70)), resultMitBetreuung.getTotalKostenProWoche());

		Assert.assertEquals("07:30", resultOhneBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(3.38)), resultOhneBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(45.35)), resultOhneBetreuung.getTotalKostenProWoche());
	}

	@Test
	public void testMaxEinkommen(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(200000);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		TSCalculationResult resultMitBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuung);
		Assert.assertNotNull(resultOhneBetreuung);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(200000)), zeitabschnitt.getMassgebendesEinkommen());

		Assert.assertEquals("07:30", resultMitBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMaxTarifTagesschuleMitPaedagogischerBetreuung(), resultMitBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultMitBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(111.80)), resultMitBetreuung.getTotalKostenProWoche());

		Assert.assertEquals("07:30", resultOhneBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMaxTarifTagesschuleOhnePaedagogischerBetreuung(), resultOhneBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(65.83)), resultOhneBetreuung.getTotalKostenProWoche());
	}

	@Test
	public void testMinEinkommen(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(0);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		TSCalculationResult resultMitBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuung);
		Assert.assertNotNull(resultOhneBetreuung);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(0)), zeitabschnitt.getMassgebendesEinkommen());

		Assert.assertEquals("07:30", resultMitBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMinTarifTagesschule(), resultMitBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultMitBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(25.85)), resultMitBetreuung.getTotalKostenProWoche());

		Assert.assertEquals("07:30", resultOhneBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMinTarifTagesschule(), resultOhneBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(25.85)), resultOhneBetreuung.getTotalKostenProWoche());
	}

	@Test
	public void testZuSpaetEingereicht() {
		// Gesuch 10 Tage nach Beginn der GP eingereicht -> Anspruch beginnt im Folgemonat
		LocalDate startGP = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb();
		gesuch.setEingangsdatum(startGP.plusDays(10));

		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(100000);
		Assert.assertEquals(2, zeitabschnitte.size());

		VerfuegungZeitabschnitt abschnittZuSpaet = zeitabschnitte.get(0);
		Assert.assertEquals(startGP, abschnittZuSpaet.getGueltigkeit().getGueltigAb());

		TSCalculationResult resultMitBetreuungZuSpaet = abschnittZuSpaet.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuungZuSpaet = abschnittZuSpaet.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuungZuSpaet);
		Assert.assertNotNull(resultOhneBetreuungZuSpaet);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(100000)), abschnittZuSpaet.getMassgebendesEinkommen());

		Assert.assertEquals("07:30", resultMitBetreuungZuSpaet.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMaxTarifTagesschuleMitPaedagogischerBetreuung(), resultMitBetreuungZuSpaet.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultMitBetreuungZuSpaet.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(111.80)), resultMitBetreuungZuSpaet.getTotalKostenProWoche());

		Assert.assertEquals("07:30", resultOhneBetreuungZuSpaet.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMaxTarifTagesschuleOhnePaedagogischerBetreuung(), resultOhneBetreuungZuSpaet.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuungZuSpaet.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(65.83)), resultOhneBetreuungZuSpaet.getTotalKostenProWoche());

		VerfuegungZeitabschnitt abschnittVerguenstigt = zeitabschnitte.get(1);
		Assert.assertEquals(startGP.plusMonths(1), abschnittVerguenstigt.getGueltigkeit().getGueltigAb());

		TSCalculationResult resultMitBetreuungVerguenstigt = abschnittVerguenstigt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuungVerguenstigt = abschnittVerguenstigt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuungVerguenstigt);
		Assert.assertNotNull(resultOhneBetreuungVerguenstigt);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(100000)), abschnittVerguenstigt.getMassgebendesEinkommen());

		Assert.assertEquals("07:30", resultMitBetreuungVerguenstigt.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(6.36)), resultMitBetreuungVerguenstigt.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultMitBetreuungVerguenstigt.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(67.70)), resultMitBetreuungVerguenstigt.getTotalKostenProWoche());

		Assert.assertEquals("07:30", resultOhneBetreuungVerguenstigt.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(3.38)), resultOhneBetreuungVerguenstigt.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuungVerguenstigt.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(45.35)), resultOhneBetreuungVerguenstigt.getTotalKostenProWoche());
	}

	private List<VerfuegungZeitabschnitt> calculate(long einkommen) {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		Assert.assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(BigDecimal.valueOf(einkommen));
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setBruttovermoegen(BigDecimal.ZERO);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(anmeldungTagesschule);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			rechner.calculate(verfuegungZeitabschnitt, getParameter());
		}
		return zeitabschnitte;
	}
}
