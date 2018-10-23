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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GutscheineStartdatumAbschnittRuleTest {

	@Nonnull
	private Betreuung betreuung = TestDataUtil.createDefaultBetreuung();

	private final GutscheineStartdatumAbschnittRule rule =
		new GutscheineStartdatumAbschnittRule(Constants.DEFAULT_GUELTIGKEIT);

	@Before
	public void setUp() {
		betreuung = TestDataUtil.createDefaultBetreuung();
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		betreuung.getKind().setGesuch(gesuch);
	}

	@Test
	public void testStartdatumVorPeriode() {
		List<VerfuegungZeitabschnitt> results = rule.createVerfuegungsZeitabschnitte(betreuung);

		Assert.assertEquals(1, results.size());
		VerfuegungZeitabschnitt result = results.get(0);

		Assert.assertTrue(result.isAbschnittLiegtNachBEGUStartdatum());
		Assert.assertEquals(Constants.GESUCHSPERIODE_17_18, result.getGueltigkeit());
	}

	@Test
	public void testStartdatumNachPeriode() {
		LocalDate startdatum = LocalDate.of(2050, 8, 1);
		betreuung.getKind().getGesuch().getDossier().getGemeinde().setBetreuungsgutscheineStartdatum(startdatum);

		List<VerfuegungZeitabschnitt> results = rule.createVerfuegungsZeitabschnitte(betreuung);

		Assert.assertEquals(1, results.size());
		VerfuegungZeitabschnitt result = results.get(0);
		Assert.assertFalse(result.isAbschnittLiegtNachBEGUStartdatum());
		Assert.assertEquals(Constants.GESUCHSPERIODE_17_18, result.getGueltigkeit());
	}

	@Test
	public void testSameStartdatum() {
		LocalDate startdatum = Constants.GESUCHSPERIODE_17_18.getGueltigAb();
		betreuung.getKind().getGesuch().getDossier().getGemeinde().setBetreuungsgutscheineStartdatum(startdatum);

		List<VerfuegungZeitabschnitt> results = rule.createVerfuegungsZeitabschnitte(betreuung);

		Assert.assertEquals(1, results.size());
		VerfuegungZeitabschnitt result = results.get(0);
		Assert.assertTrue(result.isAbschnittLiegtNachBEGUStartdatum());
		Assert.assertEquals(Constants.GESUCHSPERIODE_17_18, result.getGueltigkeit());
	}

	@Test
	public void testStartdatumEqualsPeriodeEnddatum() {
		LocalDate startdatum = Constants.GESUCHSPERIODE_17_18_BIS;
		betreuung.getKind().getGesuch().getDossier().getGemeinde().setBetreuungsgutscheineStartdatum(startdatum);

		List<VerfuegungZeitabschnitt> results = rule.createVerfuegungsZeitabschnitte(betreuung);

		Assert.assertEquals(2, results.size());

		VerfuegungZeitabschnitt beforeStartdatumResult = results.get(0);
		Assert.assertFalse(beforeStartdatumResult.isAbschnittLiegtNachBEGUStartdatum());
		DateRange rangeBeforeStartdatum = new DateRange(
			Constants.GESUCHSPERIODE_17_18_AB,
			Constants.GESUCHSPERIODE_17_18_BIS.minusDays(1));
		Assert.assertEquals(rangeBeforeStartdatum, beforeStartdatumResult.getGueltigkeit());

		VerfuegungZeitabschnitt afterStartdatumResult = results.get(1);
		Assert.assertTrue(afterStartdatumResult.isAbschnittLiegtNachBEGUStartdatum());
		DateRange rangeAfterStartdatum = new DateRange(
			Constants.GESUCHSPERIODE_17_18_BIS,
			Constants.GESUCHSPERIODE_17_18_BIS);
		Assert.assertEquals(rangeAfterStartdatum, afterStartdatumResult.getGueltigkeit());
	}
}
