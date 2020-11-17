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

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Before;
import org.junit.Test;

import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests fuer UnbezahlterUrlaubRule
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public class UnbezahlterUrlaubRuleTest extends AbstractEbeguRuleTest {

	private Betreuung betreuung;
	private LocalDate GP_START;
	private LocalDate GP_END;

	@Before
	public void init() {
		betreuung = createGesuchWithBetreuung();
		GP_START = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb();
		GP_END = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis();
	}

	@Test
	public void testKeinErwerbspensum() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertZeitabschnitt(result.get(0), 80, 0, 0, MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH);
	}

	@Test
	public void testNormalesErwerbspensum() {
		addErwerbspensum(GP_START, GP_END, null, null);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertZeitabschnitt(result.get(0), 80, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, null);
	}

	@Test
	public void testUrlaubZuKurz() {
		addErwerbspensum(GP_START, GP_END, GP_START, GP_START.plusMonths(2));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertZeitabschnitt(result.get(0), 80, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, null);
	}

	@Test
	public void testUrlaubAmAnfangDesEwp() {
		addErwerbspensum(GP_START, GP_END, GP_START.minusMonths(4), GP_START.plusMonths(1));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(4, result.size());
		assertZeitabschnitt(result.get(0), 80, 0, 0, MsgKey.UNBEZAHLTER_URLAUB_MSG);
		assertZeitabschnitt(result.get(2), 80, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, null);
	}

	@Test
	public void testUrlaubMittendrinn() {
		addErwerbspensum(GP_START, GP_END, GP_START.plusMonths(1), GP_START.plusMonths(5));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(5, result.size());
		assertZeitabschnitt(result.get(0), 80, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, null);
		assertZeitabschnitt(result.get(1), 80, 0, 0, MsgKey.UNBEZAHLTER_URLAUB_MSG);
		assertZeitabschnitt(result.get(3), 80, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, null);
	}

	@Test
	public void testMehrereErwerbspensenOhneUrlaub() {
		addErwerbspensum(GP_START, GP_END, null, null);
		addErwerbspensum(GP_START, GP_END, null, null);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertZeitabschnitt(result.get(0), 80, 100, 80, null);
	}

	@Test
	public void testMehrereErwerbspensenEinesUrlaub() {
		addErwerbspensum(GP_START, GP_END, null, null);
		addErwerbspensum(GP_START, GP_END, GP_START.plusMonths(1), GP_START.plusMonths(5));

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);
		assertNotNull(result);
		assertEquals(5, result.size());
		assertZeitabschnitt(result.get(0), 80, 100, 80, null);
		assertZeitabschnitt(result.get(1), 80, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, 50 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, MsgKey.UNBEZAHLTER_URLAUB_MSG);
		assertZeitabschnitt(result.get(3), 80, 100, 80, null);
	}

	private Betreuung createGesuchWithBetreuung() {
		final Betreuung betreuungToCreate = TestDataUtil.createGesuchWithBetreuungspensum(false);
		final Gesuch gesuch = betreuungToCreate.extractGesuch();
		BetreuungspensumContainer betPensContainer = TestDataUtil.createBetPensContainer(betreuungToCreate);
		betPensContainer.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(Constants.START_OF_TIME);
		betreuungToCreate.getBetreuungspensumContainers().add(betPensContainer);
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.setDossier(TestDataUtil.createDefaultDossier());
		gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();
		return betreuungToCreate;
	}

	private void addErwerbspensum(
		@Nonnull LocalDate ewpStart,
		@Nonnull LocalDate ewpEnd,
		@Nullable LocalDate urlaubStart,
		@Nullable LocalDate urlaubEnd) {

		ErwerbspensumContainer erwerbspensum = TestDataUtil.createErwerbspensum(ewpStart, ewpEnd, 50);
		assertNotNull(erwerbspensum.getErwerbspensumJA());
		if (urlaubStart != null) {
			if (urlaubEnd == null) {
				urlaubEnd = Constants.END_OF_TIME;
			}
			TestDataUtil.addUnbezahlterUrlaubToErwerbspensum(
				erwerbspensum.getErwerbspensumJA(),
				urlaubStart,
				urlaubEnd);
		}
		Gesuch gesuch = betreuung.extractGesuch();
		assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(erwerbspensum);
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
	}
}
