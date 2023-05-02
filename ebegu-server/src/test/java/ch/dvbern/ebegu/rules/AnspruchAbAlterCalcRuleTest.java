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
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
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

import static ch.dvbern.ebegu.enums.MsgKey.ANSPRUCH_AB_ALTER_NICHT_ERFUELLT;

public class AnspruchAbAlterCalcRuleTest {

	@ObjectUnderTest
	private AnspruchAbAlterCalcRule ruleToTest;

	@Nonnull
	private Betreuung betreuung;

	@Nonnull
	private BGCalculationInput inputData;

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
		inputData.setAnspruchspensumProzent(50);

		DateRange validity = new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME);
		ruleToTest = new AnspruchAbAlterCalcRule(validity, Locale.GERMAN, 3);
	}

	@Test
	public void testRequiredAgeForAnspruchReached() {
		inputData.setRequiredAgeForAnspruchNotReached(false);
		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertNotEquals(0,inputData.getAnspruchspensumProzent());
		Assert.assertFalse(inputData.getParent().getBemerkungenDTOList().containsMsgKey(ANSPRUCH_AB_ALTER_NICHT_ERFUELLT));
	}

	@Test
	public void testRequiredAgeForAnspruchNotReached() {
		inputData.setRequiredAgeForAnspruchNotReached(true);
		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertEquals(0,inputData.getAnspruchspensumProzent());
		Assert.assertTrue(inputData.getParent().getBemerkungenDTOList().containsMsgKey(ANSPRUCH_AB_ALTER_NICHT_ERFUELLT));
	}

}
