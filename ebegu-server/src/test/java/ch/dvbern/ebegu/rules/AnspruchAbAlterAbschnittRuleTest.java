/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.needle4j.annotation.ObjectUnderTest;

public class AnspruchAbAlterAbschnittRuleTest {

	@ObjectUnderTest
	private AnspruchAbAlterAbschnittRule ruleToTest;

	@Nonnull
	private Betreuung betreuung;

	private static final int BASIS_JAHR = 2022;
	private static final LocalDate GP_PERIODE_START = LocalDate.of(BASIS_JAHR, 8, 1);
	private static final LocalDate BIRTHDAY_JULY_16 = LocalDate.of(BASIS_JAHR, 6, 16);
	private static final LocalDate GP_PERIODE_END = LocalDate.of(BASIS_JAHR + 1, 7, 31);

	@Before
	public void setUp() {
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
				GP_PERIODE_START,
				GP_PERIODE_END,
				BetreuungsangebotTyp.KITA,
				60,
				new BigDecimal(2000),
				mandant);
		betreuung.extractGesuch().getGesuchsperiode().setGueltigkeit(new DateRange(GP_PERIODE_START, GP_PERIODE_END));
	}

	@Test
	public void testAlterAb0KindBornDuringGPShouldNotCreateAbschnitte() {
		setUpRule(0);
		setChildBirthDate(GP_PERIODE_START);
		List<VerfuegungZeitabschnitt> createdAbschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		Assert.assertTrue(createdAbschnitte.isEmpty());
	}

	@Test
	public void testAlterAb0KindBornBeforeGPShouldNotCreateAbschnitte() {
		setUpRule(0);
		setChildBirthDate(GP_PERIODE_START.minusMonths(2));
		List<VerfuegungZeitabschnitt> createdAbschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		Assert.assertTrue(createdAbschnitte.isEmpty());
	}

	@Test
	public void testAlterAb0KindBornAfterGPShouldNotCreateAbschnitte() {
		setUpRule(0);
		setChildBirthDate(GP_PERIODE_START.plusMonths(2));
		List<VerfuegungZeitabschnitt> createdAbschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		Assert.assertTrue(createdAbschnitte.isEmpty());
	}

	@Test
	public void testAlterAb3KindBornInJulyBeforeGPShouldHaveOneAbschnittWithoutAnspruchUntilOctober() {
		final LocalDate geburtsdatum = BIRTHDAY_JULY_16;
		// Erster voller Tag als 3-Monatiges und damit Anspruch am 17.10
		final LocalDate firstDayOfAnspruch = geburtsdatum.plusMonths(3);
		setUpRule(3);
		setChildBirthDate(geburtsdatum);
		List<VerfuegungZeitabschnitt> createdAbschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		Assert.assertEquals(1, createdAbschnitte.size());
		assertAnspruchZero(createdAbschnitte.get(0));
		Assert.assertEquals(GP_PERIODE_START, createdAbschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(firstDayOfAnspruch.minusDays(1), createdAbschnitte.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	public void testAlterAb3KindBornOnFirst3MonthsBeforeGPShouldCreateNoAbschnitte() {
		// Erster voller Tag als 3-Monatiges und damit Anspruch am 1.8.
		final LocalDate geburtsdatum = GP_PERIODE_START.minusMonths(3);
		setUpRule(3);
		setChildBirthDate(geburtsdatum);
		List<VerfuegungZeitabschnitt> createdAbschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		Assert.assertTrue(createdAbschnitte.isEmpty());
	}

	@Test
	public void testAlterAb3KindBornAfterGPStartShouldHaveAbschnittWithoutAnspruch() {
		final LocalDate geburtsdatum = GP_PERIODE_START.plusMonths(3);
		final LocalDate firstDayOfAnspruch = geburtsdatum.plusMonths(3);
		setUpRule(3);
		setChildBirthDate(geburtsdatum);
		List<VerfuegungZeitabschnitt> createdAbschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		Assert.assertEquals(1, createdAbschnitte.size());
		assertAnspruchZero(createdAbschnitte.get(0));
		Assert.assertEquals(GP_PERIODE_START, createdAbschnitte.get(0).getGueltigkeit().getGueltigAb());
		Assert.assertEquals(firstDayOfAnspruch.minusDays(1), createdAbschnitte.get(0).getGueltigkeit().getGueltigBis());
	}

	@Test
	public void testAlterAb3KindOneYearOldShouldCreateNoAbschnitt() {
		setUpRule(3);
		setChildBirthDate(GP_PERIODE_START.minusYears(1));
		List<VerfuegungZeitabschnitt> createdAbschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		Assert.assertTrue(createdAbschnitte.isEmpty());
	}


	@Test
	public void testAlterAb3KindBornAfterGPShoulCreateNoAbschnitt() {
		setUpRule(3);
		setChildBirthDate(GP_PERIODE_START.plusYears(1));
		List<VerfuegungZeitabschnitt> createdAbschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		Assert.assertTrue(createdAbschnitte.isEmpty());
	}

	private void setChildBirthDate(LocalDate geburtsdatum) {
		betreuung.getKind().getKindJA().setGeburtsdatum(geburtsdatum);
	}

	private static void assertAnspruchZero(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		Assert.assertEquals(0, verfuegungZeitabschnitt.getBgCalculationInputAsiv().getAnspruchspensumProzent());
	}

	private void setUpRule(int anspruchAbInMonths) {
		DateRange validy = new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME);
		ruleToTest = new AnspruchAbAlterAbschnittRule(validy, Constants.DEUTSCH_LOCALE, anspruchAbInMonths);
	}


}
