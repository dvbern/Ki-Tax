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
import ch.dvbern.ebegu.enums.EinschulungTyp;
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
	private static final int DEFAULT_MIN_BESCHAEFTIGUNGSPENSUM_VORSCHULE = 20;
	private static final int DEFAULT_MIN_BESCHAEFTIGUNGSPENSUM_KINDERGARTEN = 40;
	private static final int DEFAULT_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM = 20;
	private static final DateRange DEFAULT_DATE_RANGE = new DateRange(LocalDate.MIN, LocalDate.MAX);

	@BeforeEach
	public void setUp() {
		ruleToTest = new FKJVAusserordentlicherAnspruchCalcRule(
				DEFAULT_MIN_BESCHAEFTIGUNGSPENSUM_VORSCHULE,
				DEFAULT_MIN_BESCHAEFTIGUNGSPENSUM_KINDERGARTEN,
				DEFAULT_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM,
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
	class EineAntragstellendeTest {

		@Nested
		class VorschulalterTest {
			@Test
			public void test0BeschaeftigungspensumFKJVKeinAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(0);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(0, bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test20BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(20);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test60BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(60);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}
		}

		@Nested
		class Kindergartenalter1Test {

			@BeforeEach
			public void setUpKindergartenalter() {
				betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);
			}

			@Test
			public void zeroBeschaeftigungspensumFKJVKeinAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(0);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(0, bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test19BeschaeftigungspensumFKJVKeinAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(19);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						0,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test20BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(20);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test40BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(40);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test60BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(60);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}
		}

		@Nested
		class Kindergartenalter2Test {

			@BeforeEach
			public void setUpKindergartenalter() {
				betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN2);
			}

			@Test
			public void zeroBeschaeftigungspensumFKJVKeinAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(0);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(0, bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test19BeschaeftigungspensumFKJVKeinAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(19);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						0,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test20BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(20);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test40BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(40);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test60BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(60);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}
		}

	}

	@Nested
	class FKJVZweiAntragstellendeTest {

		@BeforeEach
		public void setUpGS2() {
			betreuung.extractGesuch().setGesuchsteller2(TestDataUtil.createDefaultGesuchstellerContainer());
		}

		@Nested
		class VorschulalterTest {

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
			public void test81BeschaeftigungspensumFKJVKeinAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(50);
				bgCalculationInput.setErwerbspensumGS2(31);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						0,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

			@Test
			public void test100BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(50);
				bgCalculationInput.setErwerbspensumGS2(50);
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

			@Test
			public void testGSNullBeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(null);
				bgCalculationInput.setErwerbspensumGS2(100);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(DEFAULT_AUSSERORDENTLICHER_ANSPRUCH, bgCalculationInput.getAusserordentlicherAnspruch());
			}
		}

		@Nested
		class Kindergartenalter1Test {

			@BeforeEach
			public void setUpKindergartenalter1() {
				betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);
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
			public void test100BeschaeftigungspensumFKJVKeinAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(50);
				bgCalculationInput.setErwerbspensumGS2(50);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						0,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}
			@Test
			public void test120BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(60);
				bgCalculationInput.setErwerbspensumGS2(60);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

		}

		@Nested
		class Kindergartenalter2Test {

			@BeforeEach
			public void setUpKindergartenalter2() {
				betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN2);
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
			public void test100BeschaeftigungspensumFKJVKeinAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(50);
				bgCalculationInput.setErwerbspensumGS2(50);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						0,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}
			@Test
			public void test120BeschaeftigungspensumFKJVAnspruch() {
				bgCalculationInput.setErwerbspensumGS1(60);
				bgCalculationInput.setErwerbspensumGS2(60);
				ruleToTest.executeRule(betreuung, bgCalculationInput);

				Assert.assertEquals(
						DEFAULT_AUSSERORDENTLICHER_ANSPRUCH,
						bgCalculationInput.getAusserordentlicherAnspruch());
			}

		}

	}
}
