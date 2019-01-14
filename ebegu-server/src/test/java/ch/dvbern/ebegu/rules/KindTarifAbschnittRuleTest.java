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
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KindTarifAbschnittRuleTest {

	private Betreuung betreuung;
	private LocalDate GP_START;
	private LocalDate GP_END;

	@Before
	public void init() {
		betreuung = createKindWithBetreuung();
		GP_START = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb();
		GP_END = betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis();
	}

	@Test
	public void kindVonAnfangAnAelterNichtEingeschult() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(changeBetreuung(GP_START.minusYears(2), EinschulungTyp.VORSCHULALTER));
		assertNotNull(result);
		assertEquals(1, result.size());
		assertZeitabschnitt(result.get(0), GP_START, GP_END, false, false);
	}

	@Test
	public void kindVonAnfangAnAelterEingeschult() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(changeBetreuung(GP_START.minusYears(2), EinschulungTyp.KINDERGARTEN1));
		assertNotNull(result);
		assertEquals(1, result.size());
		assertZeitabschnitt(result.get(0), GP_START, GP_END, false, true);
	}

	@Test
	public void kindErstNachPeriodeAelterNichtEingeschult() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(changeBetreuung(GP_START.minusDays(1), EinschulungTyp.VORSCHULALTER));
		assertNotNull(result);
		assertEquals(1, result.size());
		assertZeitabschnitt(result.get(0), GP_START, GP_END, true, false);
	}

	@Test
	public void kindErstNachPeriodeAelterEingeschult() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(changeBetreuung(GP_START.minusDays(1), EinschulungTyp.KINDERGARTEN1));
		assertNotNull(result);
		assertEquals(1, result.size());
		assertZeitabschnitt(result.get(0), GP_START, GP_END, true, true);
	}

	@Test
	public void kindWaehrendPeriodeAelterNichtEingeschult() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(changeBetreuung(GP_START.minusMonths(2), EinschulungTyp.VORSCHULALTER));
		assertNotNull(result);
		assertEquals(2, result.size());
		assertZeitabschnitt(result.get(0), GP_START, GP_END.minusMonths(1), true, false);
		assertZeitabschnitt(result.get(1), GP_END.minusMonths(1).plusDays(1), GP_END, false, false);
	}

	@Test
	public void kindWaehrendPeriodeAelterEingeschult() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(changeBetreuung(GP_START.minusMonths(2), EinschulungTyp.KINDERGARTEN1));
		assertNotNull(result);
		assertEquals(2, result.size());
		assertZeitabschnitt(result.get(0), GP_START, GP_END.minusMonths(1), true, true);
		assertZeitabschnitt(result.get(1), GP_END.minusMonths(1).plusDays(1), GP_END, false, true);
	}

	private Betreuung createKindWithBetreuung() {
		final Betreuung betreuungToCreate = TestDataUtil.createGesuchWithBetreuungspensum(false);
		final Gesuch gesuch = betreuungToCreate.extractGesuch();
		gesuch.setDossier(TestDataUtil.createDefaultDossier());
		return betreuungToCreate;
	}

	private Betreuung changeBetreuung(@Nonnull LocalDate geburtsdatum, @Nonnull EinschulungTyp einschulungTyp) {
		betreuung.getKind().getKindJA().setGeburtsdatum(geburtsdatum);
		betreuung.getKind().getKindJA().setEinschulungTyp(einschulungTyp);
		return betreuung;
	}

	private void assertZeitabschnitt(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		LocalDate expectedDateFrom,
		LocalDate expectedDateTo,
		boolean expectedBabyTarif,
		boolean expectedEingeschult) {

		assertEquals(expectedDateFrom, zeitabschnitt.getGueltigkeit().getGueltigAb());
		assertEquals(expectedDateTo, zeitabschnitt.getGueltigkeit().getGueltigBis());
		assertEquals(expectedBabyTarif, zeitabschnitt.isBabyTarif());
		assertEquals(expectedEingeschult, zeitabschnitt.isEingeschult());
	}
}
