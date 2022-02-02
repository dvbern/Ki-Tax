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
import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.needle4j.annotation.ObjectUnderTest;

/**
 * Tests fuer KesbPlatzierungCalcRule
 */
public class FKJVAusserordentlicherAnspruchCalcRuleTest {

	@SuppressWarnings({ "InstanceVariableMayNotBeInitialized", "NullableProblems" })
	@Nonnull
	private Betreuung betreuung;

	@ObjectUnderTest
	private FKJVAusserordentlicherAnspruchCalcRule ruleToTest;

	private BGCalculationInput bgCalculationInput;

	private static final int DEFAULT_AUSSERORDENTLICHER_ANSPRUCH = 40;
	private static final DateRange DEFAULT_DATE_RANGE = new DateRange(LocalDate.MIN, LocalDate.MAX);

	@BeforeEach
	public void setUp() {
		ruleToTest = new FKJVAusserordentlicherAnspruchCalcRule(
				DEFAULT_DATE_RANGE,
				Constants.DEUTSCH_LOCALE);
		bgCalculationInput = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		bgCalculationInput.setAusserordentlicherAnspruch(DEFAULT_AUSSERORDENTLICHER_ANSPRUCH);

		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
				Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
				Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
				BetreuungsangebotTyp.KITA,
				60,
				new BigDecimal(2000));
	}

	@Nested
	class FKJVEineAntragstellendeTest {
		@Test
		public void testEineGesuchstellende0BeschaeftigungspensumFKJVKeinAnspruch() {
			bgCalculationInput.setErwerbspensumGS1(0);
			ruleToTest.executeRule(betreuung, bgCalculationInput);

			Assert.assertEquals(0, bgCalculationInput.getAusserordentlicherAnspruch());
		}

		@Test
		public void testEineGesuchstellende50BeschaeftigungspensumFKJVAnspruch() {
			bgCalculationInput.setErwerbspensumGS1(50);
			ruleToTest.executeRule(betreuung, bgCalculationInput);

			Assert.assertEquals(
					DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
					bgCalculationInput.getAusserordentlicherAnspruch());
		}

		@Test
		public void testEineGesuchstellende60BeschaeftigungspensumFKJVAnspruch() {
			bgCalculationInput.setErwerbspensumGS1(60);
			ruleToTest.executeRule(betreuung, bgCalculationInput);

			Assert.assertEquals(
					DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
					bgCalculationInput.getAusserordentlicherAnspruch());
		}
	}

	@Nested
	class FKJVZweiAntragstellendeTest {

		@BeforeEach
		public void setUpGS2() {
			betreuung.extractGesuch().setGesuchsteller2(TestDataUtil.createDefaultGesuchstellerContainer());
		}

		@Test
		public void test80BeschaeftigungspensumFKJVKeinAnspruch() {
			bgCalculationInput.setErwerbspensumGS1(0);
			bgCalculationInput.setErwerbspensumGS2(80);
			ruleToTest.executeRule(betreuung, bgCalculationInput);

			Assert.assertEquals(0, bgCalculationInput.getAusserordentlicherAnspruch());
		}

		@Test
		public void test80BeschaeftigungspensumFKJVKeinAnspruchMessage() {
			bgCalculationInput.setErwerbspensumGS1(0);
			bgCalculationInput.setErwerbspensumGS2(80);
			ruleToTest.executeRule(betreuung, bgCalculationInput);

			Assert.assertNotNull(bgCalculationInput.getParent().getBemerkungenDTOList().findFirstBemerkungByMsgKey(
					MsgKey.KEIN_AUSSERORDENTLICHER_ANSPRUCH_MSG
			));
		}

		@Test
		public void test81BeschaeftigungspensumFKJVAnspruch() {
			bgCalculationInput.setErwerbspensumGS1(50);
			bgCalculationInput.setErwerbspensumGS2(31);
			ruleToTest.executeRule(betreuung, bgCalculationInput);

			Assert.assertEquals(
					DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
					bgCalculationInput.getAusserordentlicherAnspruch());
		}

		@Test
		public void test119BeschaeftigungspensumFKJVAnspruch() {
			bgCalculationInput.setErwerbspensumGS1(60);
			bgCalculationInput.setErwerbspensumGS2(59);
			ruleToTest.executeRule(betreuung, bgCalculationInput);

			Assert.assertEquals(
					DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
					bgCalculationInput.getAusserordentlicherAnspruch());
		}
	}
}
