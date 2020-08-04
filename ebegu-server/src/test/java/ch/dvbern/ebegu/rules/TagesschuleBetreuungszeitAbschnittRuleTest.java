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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.FinSitStatus;
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
		anmeldungTagesschule = TestDataUtil.createAnmeldungTagesschuleWithModules(kindContainer, gesuch.getGesuchsperiode());//initAnmedlungTagesschule();
	}

	@Test
	public void testEinkommen120000(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(120000, FinSitStatus.AKZEPTIERT);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt abschnittAnspruch = zeitabschnitte.get(0);
		assertZeitabschnitt_120000(abschnittAnspruch);
	}

	@Test
	public void testEinkommen100000(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(100000, FinSitStatus.AKZEPTIERT);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt abschnittAnspruch = zeitabschnitte.get(0);
		assertZeitabschnitt_100000(abschnittAnspruch);
	}

	@Test
	public void testMaxEinkommen(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(200000, FinSitStatus.AKZEPTIERT);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt abschnittMaxTarifEinkommenZuHoch = zeitabschnitte.get(0);
		assertZeitabschnitt_MaxTarif(abschnittMaxTarifEinkommenZuHoch, 200000);
	}

	@Test
	public void testMinEinkommen(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(10, FinSitStatus.AKZEPTIERT);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		assertZeitabschnitt_MinTarif(zeitabschnitt, 10);
	}

	@Test
	public void testZuSpaetEingereicht() {
		// Gesuch 10 Tage nach Beginn der GP eingereicht -> Anspruch beginnt im Folgemonat
		LocalDate startGP = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb();
		gesuch.setEingangsdatum(startGP.plusDays(10));

		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(100000, FinSitStatus.AKZEPTIERT);
		Assert.assertEquals(2, zeitabschnitte.size());

		VerfuegungZeitabschnitt abschnittZuSpaet = zeitabschnitte.get(0);
		Assert.assertEquals(startGP, abschnittZuSpaet.getGueltigkeit().getGueltigAb());
		assertZeitabschnitt_MaxTarif(abschnittZuSpaet, 100000);

		VerfuegungZeitabschnitt abschnittVerguenstigt = zeitabschnitte.get(1);
		Assert.assertEquals(startGP.plusMonths(1), abschnittVerguenstigt.getGueltigkeit().getGueltigAb());
		assertZeitabschnitt_100000(abschnittVerguenstigt);
	}

	@Test
	public void testFinSitNichtAkzeptiert(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(100000, null);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt abschnittFinSitNichtAkzeptiert = zeitabschnitte.get(0);
		assertZeitabschnitt_100000(abschnittFinSitNichtAkzeptiert);
	}

	@Test
	public void testFinSitAbgelehnt(){
		List<VerfuegungZeitabschnitt> zeitabschnitte = calculate(100000, FinSitStatus.ABGELEHNT);
		Assert.assertEquals(1, zeitabschnitte.size());

		VerfuegungZeitabschnitt abschnittFinSitAbgelehnt = zeitabschnitte.get(0);
		assertZeitabschnitt_MaxTarif(abschnittFinSitAbgelehnt, 160000);
	}

	private List<VerfuegungZeitabschnitt> calculate(long einkommen, FinSitStatus finSitStatus) {
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		Assert.assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.setFinSitStatus(finSitStatus);
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(BigDecimal.valueOf(einkommen));
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setBruttovermoegen(BigDecimal.ZERO);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper.calculate(anmeldungTagesschule);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			verfuegungZeitabschnitt.initBGCalculationResult();
			rechner.calculateAsiv(verfuegungZeitabschnitt.getBgCalculationInputAsiv(), getParameter());
		}
		return zeitabschnitte;
	}

	private void assertZeitabschnitt_100000(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		TSCalculationResult resultMitBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuung);
		Assert.assertNotNull(resultOhneBetreuung);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(100000)), zeitabschnitt.getMassgebendesEinkommen());

		Assert.assertEquals("04:00", resultMitBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(6.36)), resultMitBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultMitBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(45.44)), resultMitBetreuung.getTotalKostenProWoche());

		Assert.assertEquals("02:00", resultOhneBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(3.38)), resultOhneBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(26.76)), resultOhneBetreuung.getTotalKostenProWoche());
	}

	private void assertZeitabschnitt_120000(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		TSCalculationResult resultMitBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuung);
		Assert.assertNotNull(resultOhneBetreuung);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(120000)), zeitabschnitt.getMassgebendesEinkommen());

		Assert.assertEquals("04:00", resultMitBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(8.32)), resultMitBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20.00)), resultMitBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(53.28)), resultMitBetreuung.getTotalKostenProWoche());

		Assert.assertEquals("02:00", resultOhneBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(4.29)), resultOhneBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(28.58)), resultOhneBetreuung.getTotalKostenProWoche());
	}

	private void assertZeitabschnitt_MaxTarif(@Nonnull VerfuegungZeitabschnitt zeitabschnitt, int massgebendesEinkommen) {
		TSCalculationResult resultMitBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuung);
		Assert.assertNotNull(resultOhneBetreuung);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(massgebendesEinkommen)), zeitabschnitt.getMassgebendesEinkommen());

		Assert.assertEquals("04:00", resultMitBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMaxTarifTagesschuleMitPaedagogischerBetreuung(), resultMitBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultMitBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(68.96)), resultMitBetreuung.getTotalKostenProWoche());

		Assert.assertEquals("02:00", resultOhneBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMaxTarifTagesschuleOhnePaedagogischerBetreuung(), resultOhneBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(32.22)), resultOhneBetreuung.getTotalKostenProWoche());
	}

	private void assertZeitabschnitt_MinTarif(@Nonnull VerfuegungZeitabschnitt zeitabschnitt, int massgebendesEinkommen) {
		TSCalculationResult resultMitBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung();
		TSCalculationResult resultOhneBetreuung = zeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
		Assert.assertNotNull(resultMitBetreuung);
		Assert.assertNotNull(resultOhneBetreuung);

		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(massgebendesEinkommen)), zeitabschnitt.getMassgebendesEinkommen());

		Assert.assertEquals("04:00", resultMitBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMinTarifTagesschule(), resultMitBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultMitBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(23.12)), resultMitBetreuung.getTotalKostenProWoche());

		Assert.assertEquals("02:00", resultOhneBetreuung.getBetreuungszeitProWocheFormatted());
		Assert.assertEquals(getParameter().getMinTarifTagesschule(), resultOhneBetreuung.getGebuehrProStunde());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(20)), resultOhneBetreuung.getVerpflegungskosten());
		Assert.assertEquals(MathUtil.toTwoKommastelle(MATH.from(21.56)), resultOhneBetreuung.getTotalKostenProWoche());
	}
}
