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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.needle4j.annotation.ObjectUnderTest;

public class AnspruchAbAlterCalcRuleTest {

	@ObjectUnderTest
	private AnspruchAbAlterCalcRule ruleToTest;

	@Nonnull
	private Betreuung betreuung;

	@Nonnull
	private BGCalculationInput inputData;

	private final LocalDate ZEITABSCHNITT_START = LocalDate.of(2023, 8, 1);
	private final LocalDate ZEITABSCHNITT_MIDDLE = LocalDate.of(2023, 8, 15);
	private final LocalDate ZEITABSCHNITT_END = LocalDate.of(2023, 8, 31);

	@Before
	public void setUp() {
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
				Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
				Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
				BetreuungsangebotTyp.KITA,
				60,
				new BigDecimal(2000),
				mandant);
		inputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		inputData.getParent().setGueltigkeit(new DateRange(
				ZEITABSCHNITT_START,
				ZEITABSCHNITT_END
		));
		inputData.setAnspruchspensumProzent(100);
	}

	@Test
	public void testAlterAb0KindBornDuringMonthShouldNotChangeAnspruch() {
		setUpRule(0);
		setChildAgeAt(0, 0, 0, ZEITABSCHNITT_MIDDLE);
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb0KindTurns1MonthDuringMonthShouldNotChangeAnspruch() {
		setUpRule(0);
		setChildAgeAt(0, 1, 0, ZEITABSCHNITT_MIDDLE);
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb0KindTurns1MonthLastOfMonthShouldNotChangeAnspruch() {
		setUpRule(0);
		setChildAgeAt(0, 1, 0, ZEITABSCHNITT_END);
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb0KindTurns1MonthSecondLastOfMonthShouldNotChangeAnspruch() {
		setUpRule(0);
		setChildAgeAt(0, 1, 0, ZEITABSCHNITT_END.minusDays(1));
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb1KindBornDuringMonthShouldHave0Anspruch() {
		setUpRule(1);
		setChildAgeAt(0, 0, 0, ZEITABSCHNITT_MIDDLE);
		ruleToTest.executeRule(betreuung, inputData);
		assertZeroAnspruch();
	}

	@Test
	public void testAlterAb1KindTurns1MonthDuringMonthShouldNotChangeAnspruch() {
		setUpRule(1);
		setChildAgeAt(0, 1, 0, ZEITABSCHNITT_MIDDLE);
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb1KindTurns1MonthSecondLastOfMonthShouldNotChangeAnspruch() {
		setUpRule(1);
		setChildAgeAt(0, 1, 0, ZEITABSCHNITT_END.minusDays(1));
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb1KindTurns1MonthLastOfMonthShouldHave0Anspruch() {
		setUpRule(1);
		setChildAgeAt(0, 1, 0, ZEITABSCHNITT_END);
		ruleToTest.executeRule(betreuung, inputData);
		assertZeroAnspruch();
	}

	@Test
	public void testAlterAb3KindTurns1MonthDuringMonthShouldHave0Anspruch() {
		setUpRule(3);
		setChildAgeAt(0, 1, 0, ZEITABSCHNITT_MIDDLE);
		ruleToTest.executeRule(betreuung, inputData);
		assertZeroAnspruch();
	}

	@Test
	public void testAlterAb3KindTurns1MonthLastOfMonthShouldHave0Anspruch() {
		setUpRule(3);
		setChildAgeAt(0, 1, 0, ZEITABSCHNITT_END);
		ruleToTest.executeRule(betreuung, inputData);
		assertZeroAnspruch();
	}

	@Test
	public void testAlterAb3KindTurns3MonthsLastOfMonthShouldHave0Anspruch() {
		setUpRule(3);
		setChildAgeAt(0, 3, 0, ZEITABSCHNITT_END);
		ruleToTest.executeRule(betreuung, inputData);
		assertZeroAnspruch();
	}

	@Test
	public void testAlterAb3KindTurns3MonthsSecondLastOfMonthShouldNotChangeAnspruch() {
		setUpRule(3);
		setChildAgeAt(0, 3, 0, ZEITABSCHNITT_END.minusDays(1));
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb3Turns1YearDuringMonthShouldNotChangeAnspruch() {
		setUpRule(3);
		setChildAgeAt(0, 3, 0, ZEITABSCHNITT_MIDDLE);
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb3Turns1YearLastOfMonthShouldNotChangeAnspruch() {
		setUpRule(3);
		setChildAgeAt(1, 0, 0, ZEITABSCHNITT_END);
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	@Test
	public void testAlterAb3Turns1YearSecondLastOfMonthShouldNotChangeAnspruch() {
		setUpRule(3);
		setChildAgeAt(0, 3, 0, ZEITABSCHNITT_END.minusDays(1));
		ruleToTest.executeRule(betreuung, inputData);
		assertAnspruch();
	}

	private void setUpRule(int anspruchAbInMonths) {
		DateRange validy = new DateRange(LocalDate.of(1000, 1, 1), LocalDate.of(3000, 1, 1));
		ruleToTest = new AnspruchAbAlterCalcRule(validy, Constants.DEUTSCH_LOCALE, anspruchAbInMonths);
	}

	private void setChildAgeAt(int years, int months, int days, LocalDate referenceDate) {
		var kind = betreuung.getKind().getKindJA();
		var geburtstag = referenceDate.minusYears(years).minusMonths(months).minusDays(days);

		kind.setGeburtsdatum(geburtstag);
	}

	private void assertAnspruch() {
		Assert.assertTrue(inputData.getAnspruchspensumProzent() > 0);
	}

	private void assertZeroAnspruch() {
		Assert.assertEquals(0, inputData.getAnspruchspensumProzent());
		Assert.assertTrue(inputData.getParent().getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ANSPRUCH_AB_ALTER_NICHT_ERFUELLT));
	}

}
