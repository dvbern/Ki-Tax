/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;

/**
 * Testet die MaximalesEinkommen-Regel
 */
class FachstelleBernCalcRuleTest {

	private final BigDecimal EINKOMMEN = MathUtil.DEFAULT.fromNullSafe(100000);
	private final int PENSUM_TOO_LOW = 10;
	private final LocalDate AUG_14 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, 8, 14);

	private Mandant mandant;

	@BeforeEach
	public void setUp() {
		mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
	}

	@Nested
	class AnspruchTest {
		/**
		 * Arbeitspensum: 10%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 1.8. - 31.7.
		 * <p>
		 * => Betreuung 60% Anspruch 40% 1.8. - 31.7.
		 */
		@Test
		void pensumTooLowSprachlicheIntegrationShouldHaveAnspruch() {
			final int betreuungspensum = 60;
			final Betreuung betreuung = createBetreuungAndGesuch(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
				betreuungspensum, PENSUM_TOO_LOW);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			Assertions.assertNotNull(result);
			Assertions.assertEquals(1, result.size());
			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(40, abschnitt.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt.getBetreuungspensumProzent());
		}


		/**
		 * Arbeitspensum: 80%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 1.8. - 31.7.
		 * <p>
		 * => Betreuung 60% Anspruch 100% 1.8. - 31.7.
		 */
		@Test
		void pensumHighEnoughWithSprachlicheIntegrationShouldHaveAnspruchOfPensum() {
			final int betreuungspensum = 60;
			final Betreuung betreuung = createBetreuungAndGesuch(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
				betreuungspensum, 80);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			Assertions.assertNotNull(result);
			Assertions.assertEquals(1, result.size());
			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(100, abschnitt.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt.getBetreuungspensumProzent());
		}

		/**
		 * Arbeitspensum: 80%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 15.8. - 31.7.
		 * <p>
		 * => Betreuung 0% Anspruch 0% 1.8-14.8.
		 *    Betreuung 60% Anspruch 40% 15.8-31.8.
		 *    Betreuung 60% Anspruch 40% 1.9.-31.7.
		 */
		@Test
		void pensumTooLowWithSprachlicheIntegrationShouldHaveAnspruchAfterBetreuungStart() {
			final int betreuungspensum = 60;
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, TestDataUtil.ENDE_PERIODE,
				betreuungspensum,
				PENSUM_TOO_LOW);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			Assertions.assertNotNull(result);
			Assertions.assertEquals(3, result.size());
			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(0, abschnitt.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(0), abschnitt.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(40, abschnitt1.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt1.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(40, abschnitt2.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt2.getBetreuungspensumProzent());
		}

		/**
		 * Arbeitspensum: 80%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 30% 15.8. - 31.7.
		 * <p>
		 * => Betreuung 0% Anspruch 0% 1.8-14.8.
		 *    Betreuung 30% Anspruch 0% 15.8-31.8.
		 *    Betreuung 30% Anspruch 0% 1.9.-31.7.
		 */
		@Test
		void pensumTooLowWithSprachlicheIntegrationShouldNotHaveAnspruchAfterBetreuungTooLowStart() {
			final int betreuungspensum = 30;
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, TestDataUtil.ENDE_PERIODE,
				betreuungspensum, PENSUM_TOO_LOW);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			Assertions.assertNotNull(result);
			Assertions.assertEquals(3, result.size());
			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(0, abschnitt.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(0), abschnitt.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(0, abschnitt1.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt1.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(0, abschnitt2.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt2.getBetreuungspensumProzent());
		}


		/**
		 * Arbeitspensum: 10%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 15.8. - 31.10.
		 * Kita Betreuung 60% 1.1. - 31.7.
		 * <p>
		 * => Betreuung 0% Anspruch 0% 1.8-14.8.
		 *    Betreuung 60% Anspruch 40% 15.8-31.8.
		 *    Betreuung 60% Anspruch 40% 1.9.-31.10.
		 *    Betreuung 0% Anspruch 0% 1.11.-31.12.
		 *    Betreuung 60% Anspruch 40% 1.1.-31.7.
		 */
		@Test
		void pensumTooLowWithSprachlicheIntegrationShouldHaveAnspruchDuringBetreuungenOnlyWithMultiplePensen() {
			final int betreuungspensum = 60;
			final LocalDate OCT_31 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, 10, 31);
			final LocalDate JAN_1 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 1, 1);
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, OCT_31,
				betreuungspensum, PENSUM_TOO_LOW);
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(JAN_1, TestDataUtil.ENDE_PERIODE, betreuungspensum));
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			Assertions.assertNotNull(result);
			Assertions.assertEquals(5, result.size());

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(0, abschnitt.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(0), abschnitt.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(40, abschnitt1.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt1.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(40, abschnitt2.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt2.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt3 = result.get(3);
			Assertions.assertEquals(0, abschnitt3.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(0), abschnitt3.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt4 = result.get(4);
			Assertions.assertEquals(40, abschnitt4.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt4.getBetreuungspensumProzent());
		}

		/**
		 * Arbeitspensum: 10%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 30% 15.8. - 31.10.
		 * Kita Betreuung 30% 1.1. - 31.7.
		 * <p>
		 * => Betreuung 0% Anspruch 0% 1.8-14.8.
		 *    Betreuung 30% Anspruch 0% 15.8-31.8.
		 *    Betreuung 30% Anspruch 0% 1.9.-31.10.
		 *    Betreuung 0% Anspruch 0% 1.11.-31.12.
		 *    Betreuung 30% Anspruch 0% 1.1.-31.7.
		 */
		@Test
		void pensumTooLowWithSprachlicheIntegrationShouldHaveNoAnspruchDuringBetreuungenOnlyWithMultipleLowPensen() {
			final int betreuungspensum = 30;
			final LocalDate OCT_31 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, 10, 31);
			final LocalDate JAN_1 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 1, 1);
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, OCT_31,
				betreuungspensum, PENSUM_TOO_LOW);
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(JAN_1, TestDataUtil.ENDE_PERIODE, betreuungspensum));
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			Assertions.assertNotNull(result);
			Assertions.assertEquals(5, result.size());

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(0, abschnitt.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(0), abschnitt.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(0, abschnitt1.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt1.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(0, abschnitt2.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt2.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt3 = result.get(3);
			Assertions.assertEquals(0, abschnitt3.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(0), abschnitt3.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt4 = result.get(4);
			Assertions.assertEquals(0, abschnitt4.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt4.getBetreuungspensumProzent());
		}

		/**
		 * Arbeitspensum: 80%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 15.8. - 31.10.
		 * Kita Betreuung 60% 1.1. - 31.7.
		 * <p>
		 * => Betreuung 0% Anspruch 100% 1.8-14.8.
		 *    Betreuung 60% Anspruch 100% 15.8-31.8.
		 *    Betreuung 60% Anspruch 100% 1.9.-31.10.
		 *    Betreuung 0% Anspruch 100% 1.11.-31.12.
		 *    Betreuung 60% Anspruch 100% 1.1.-31.7.
		 */
		@Test
		void pensumWithSprachlicheIntegrationShouldHaveAnspruchWithMultiplePensen() {
			final int betreuungspensum = 60;
			final LocalDate OCT_31 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, 10, 31);
			final LocalDate JAN_1 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 1, 1);
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, OCT_31,
				betreuungspensum, 80);
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(JAN_1, TestDataUtil.ENDE_PERIODE, betreuungspensum));
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			Assertions.assertNotNull(result);
			Assertions.assertEquals(5, result.size());

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(100, abschnitt.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(0), abschnitt.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(100, abschnitt1.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt1.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(100, abschnitt2.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt2.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt3 = result.get(3);
			Assertions.assertEquals(100, abschnitt3.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(0), abschnitt3.getBetreuungspensumProzent());

			final VerfuegungZeitabschnitt abschnitt4 = result.get(4);
			Assertions.assertEquals(100, abschnitt4.getAnspruchberechtigtesPensum());
			Assertions.assertEquals(MathUtil.DEFAULT.from(betreuungspensum), abschnitt4.getBetreuungspensumProzent());
		}
	}

	@Nested
	class BemerkungenTest {
		/**
		 * Arbeitspensum: 10%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 1.8. - 31.7.
		 */
		@Test
		void pensumTooLowSprachlicheIntegrationShouldHaveAnsprch() {
			final int betreuungspensum = 60;
			final Betreuung betreuung = createBetreuungAndGesuch(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
				betreuungspensum, PENSUM_TOO_LOW);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(3, abschnitt.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_MSG));
			Assertions.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
			Assertions.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
		}


		/**
		 * Arbeitspensum: 80%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 1.8. - 31.7.
		 */
		@Test
		void pensumHighEnoughWithSprachlicheIntegrationShouldHaveAnsprchOfPensum() {
			final int betreuungspensum = 60;
			final Betreuung betreuung = createBetreuungAndGesuch(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE,
				betreuungspensum, 80);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(2, abschnitt.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
			Assertions.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
		}

		/**
		 * Arbeitspensum: 80%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 15.8. - 31.7.
		 */
		@Test
		void pensumTooLowWithSprachlicheIntegrationShouldHaveAnspruchAfterBetreuungStart() {
			final int betreuungspensum = 60;
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, TestDataUtil.ENDE_PERIODE,
				betreuungspensum,
				PENSUM_TOO_LOW);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(1, abschnitt.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
			Assertions.assertFalse(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(3, abschnitt1.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_MSG));
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(3, abschnitt2.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_MSG));
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
		}

		/**
		 * Arbeitspensum: 80%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 30% 15.8. - 31.7.
		 */
		@Test
		void pensumTooLowWithSprachlicheIntegrationShouldNotHaveAnspruchAfterBetreuungTooLowStart() {
			final int betreuungspensum = 30;
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, TestDataUtil.ENDE_PERIODE,
				betreuungspensum, PENSUM_TOO_LOW);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(1, abschnitt.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
			Assertions.assertFalse(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(2, abschnitt1.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(2, abschnitt2.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
		}


		/**
		 * Arbeitspensum: 10%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 15.8. - 31.10.
		 * Kita Betreuung 60% 1.1. - 31.7.
		 */
		@Test
		void pensumTooLowWithSprachlicheIntegrationShouldHaveAnspruchDuringBetreuungenOnlyWithMultiplePensen() {
			final int betreuungspensum = 60;
			final LocalDate OCT_31 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, 10, 31);
			final LocalDate JAN_1 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 1, 1);
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, OCT_31,
				betreuungspensum, PENSUM_TOO_LOW);
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(JAN_1, TestDataUtil.ENDE_PERIODE, betreuungspensum));
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(1, abschnitt.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
			Assertions.assertFalse(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(3, abschnitt1.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_MSG));
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(3, abschnitt2.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_MSG));
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));

			final VerfuegungZeitabschnitt abschnitt3 = result.get(3);
			Assertions.assertEquals(1, abschnitt3.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt3.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
			Assertions.assertFalse(abschnitt3.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));

			final VerfuegungZeitabschnitt abschnitt4 = result.get(4);
			Assertions.assertEquals(3, abschnitt4.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt4.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_MSG));
			Assertions.assertTrue(abschnitt4.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
			Assertions.assertTrue(abschnitt4.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
		}




		/**
		 * Arbeitspensum: 10%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 30% 15.8. - 31.10.
		 * Kita Betreuung 30% 1.1. - 31.7.
		 */
		@Test
		void pensumTooLowWithSprachlicheIntegrationShouldHaveNoAnspruchDuringBetreuungenOnlyWithMultipleLowPensen() {
			final int betreuungspensum = 30;
			final LocalDate OCT_31 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, 10, 31);
			final LocalDate JAN_1 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 1, 1);
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, OCT_31,
				betreuungspensum, PENSUM_TOO_LOW);
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(JAN_1, TestDataUtil.ENDE_PERIODE, betreuungspensum));
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(1, abschnitt.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
			Assertions.assertFalse(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(2, abschnitt1.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(2, abschnitt2.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));

			final VerfuegungZeitabschnitt abschnitt3 = result.get(3);
			Assertions.assertEquals(1, abschnitt3.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt3.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
			Assertions.assertFalse(abschnitt3.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));

			final VerfuegungZeitabschnitt abschnitt4 = result.get(4);
			Assertions.assertEquals(2, abschnitt4.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt4.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));
			Assertions.assertTrue(abschnitt4.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH));
		}



		/**
		 * Arbeitspensum: 80%
		 * Fachstelle Sprachliche Förderung: 40% 1.8.-31.7.
		 * Kita Betreuung 60% 15.8. - 31.10.
		 * Kita Betreuung 60% 1.1. - 31.7.
		 */
		@Test
		void pensumWithSprachlicheIntegrationShouldHaveAnspruchWithMultiplePensen() {
			final int betreuungspensum = 60;
			final LocalDate OCT_31 = LocalDate.of(TestDataUtil.PERIODE_JAHR_1, 10, 31);
			final LocalDate JAN_1 = LocalDate.of(TestDataUtil.PERIODE_JAHR_2, 1, 1);
			final Betreuung betreuung = createBetreuungAndGesuch(AUG_14, OCT_31,
				betreuungspensum, 80);
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(JAN_1, TestDataUtil.ENDE_PERIODE, betreuungspensum));
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung);

			final VerfuegungZeitabschnitt abschnitt = result.get(0);
			Assertions.assertEquals(2, abschnitt.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
			Assertions.assertFalse(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));

			final VerfuegungZeitabschnitt abschnitt1 = result.get(1);
			Assertions.assertEquals(2, abschnitt1.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
			Assertions.assertTrue(abschnitt1.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));

			final VerfuegungZeitabschnitt abschnitt2 = result.get(2);
			Assertions.assertEquals(2, abschnitt2.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
			Assertions.assertTrue(abschnitt2.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));

			final VerfuegungZeitabschnitt abschnitt3 = result.get(3);
			Assertions.assertEquals(2, abschnitt3.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt3.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
			Assertions.assertTrue(abschnitt.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
			Assertions.assertFalse(abschnitt3.getBemerkungenDTOList().containsMsgKey(MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG));

			final VerfuegungZeitabschnitt abschnitt4 = result.get(4);
			Assertions.assertEquals(2, abschnitt4.getBemerkungenDTOList().uniqueSize());
			Assertions.assertTrue(abschnitt4.getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
			Assertions.assertTrue(abschnitt4.getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
		}
	}


	@Nonnull
	private static BetreuungspensumContainer createBetreuungspensum(LocalDate von, LocalDate bis, int betreuungspensum) {
		final BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		final Betreuungspensum betreuungspensumJA = new Betreuungspensum();
		betreuungspensumJA.setGueltigkeit(new DateRange(von, bis));
		betreuungspensumJA.setPensum(BigDecimal.valueOf(betreuungspensum));
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensumJA);
		return betreuungspensumContainer;
	}

	private Betreuung createBetreuungAndGesuch(
		LocalDate betreuungStart,
		LocalDate betreuungEnde, int betreuungspensum, int erwerbspensum) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			betreuungStart, betreuungEnde, KITA, betreuungspensum, MathUtil.DEFAULT.fromNullSafe(2000));

		prepareGesuch(betreuung, erwerbspensum);
		return betreuung;
	}



	private void prepareGesuch(
		@Nonnull AbstractPlatz platz,
		int erwerbspensum
	) {
		Gesuch gesuch = platz.extractGesuch();
		gesuch.setFinSitStatus(FinSitStatus.AKZEPTIERT);
		TestDataUtil.calculateFinanzDaten(gesuch, new FinanzielleSituationBernRechner());
		Assertions.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(
			TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, erwerbspensum));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Assertions.assertNotNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(EINKOMMEN);
		Objects.requireNonNull(gesuch.extractFamiliensituation()).setVerguenstigungGewuenscht(true);

		PensumFachstelle pensumFachstelle = new PensumFachstelle(platz.getKind().getKindJA());
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SPRACHLICHE_INTEGRATION);
		pensumFachstelle.setPensum(40);
		pensumFachstelle.setGueltigkeit(new DateRange(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE));
		platz.getKind()
			.getKindJA()
			.getPensumFachstelle()
			.add(pensumFachstelle);
	}
}
