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

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;

/**
 * Tests fuer Verfügungsmuster
 */
class MutationsMergerFinanzielleSituationSchwyzTest {

	//01.09.XXXX
	private static final LocalDate START_VERFUEGUNG_ABSCHNITT_ERSTGESUCH = START_PERIODE.plusMonths(1);
	//30.09.XXXX
	private static final LocalDate END_VERFUEGUNG_ABSCHNITT_ERSTGESUCH = START_PERIODE.plusMonths(2).minusDays(1);

	private static final LocalDate EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT = START_PERIODE.plusMonths(1).plusDays(5);


	private static final MutationsMergerFinanzielleSituationSchwyz MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ = new MutationsMergerFinanzielleSituationSchwyz(
		Locale.GERMAN);

	private static final BigDecimal HUNDERT_THAUSEND = Objects.requireNonNull(MathUtil.DEFAULT.from(100000));
	private static final BigDecimal ZWEI_HUNDERT_THAUSEND = Objects.requireNonNull(MathUtil.DEFAULT.from(200000));

	@Test
	void test_hoereMassgegebeneseinkommens_MutationGleicheMonat_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZWEI_HUNDERT_THAUSEND);
		BGCalculationResult resultVorgaenger = initResultData(HUNDERT_THAUSEND);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ.handleEinkommen(bgCalculationInput, resultVorgaenger, new Betreuung(), EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT);
		Assertions.assertEquals(HUNDERT_THAUSEND, bgCalculationInput.getMassgebendesEinkommen());
	}

	@Test
	void test_hoereMassgegebeneseinkommens_MutationGleicheTag_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZWEI_HUNDERT_THAUSEND);
		BGCalculationResult resultVorgaenger = initResultData(HUNDERT_THAUSEND);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ.handleEinkommen(bgCalculationInput, resultVorgaenger, new Betreuung(), EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.withDayOfMonth(1));
		Assertions.assertEquals(HUNDERT_THAUSEND, bgCalculationInput.getMassgebendesEinkommen());
	}

	@Test
	void test_kleinerMassgegebeneseinkommens_MutationGleicheMonat_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(HUNDERT_THAUSEND);
		BGCalculationResult resultVorgaenger = initResultData(ZWEI_HUNDERT_THAUSEND);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ.handleEinkommen(bgCalculationInput, resultVorgaenger, new Betreuung(), EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT);
		Assertions.assertEquals(ZWEI_HUNDERT_THAUSEND, bgCalculationInput.getMassgebendesEinkommen());
	}

	@Test
	void test_kleinerMassgegebeneseinkommens_MutationGleicheTag_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(HUNDERT_THAUSEND);
		BGCalculationResult resultVorgaenger = initResultData(ZWEI_HUNDERT_THAUSEND);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ.handleEinkommen(bgCalculationInput, resultVorgaenger, new Betreuung(), EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.withDayOfMonth(1));
		Assertions.assertEquals(ZWEI_HUNDERT_THAUSEND, bgCalculationInput.getMassgebendesEinkommen());
	}

	@Test
	void test_hoereMassgegebeneseinkommens_MutationAbFolgeMonat_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZWEI_HUNDERT_THAUSEND);
		BGCalculationResult resultVorgaenger = initResultData(HUNDERT_THAUSEND);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ.handleEinkommen(bgCalculationInput, resultVorgaenger, new Betreuung(), EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.plusMonths(1));
		Assertions.assertEquals(HUNDERT_THAUSEND, bgCalculationInput.getMassgebendesEinkommen());
	}

	@Test
	void test_kleinerMassgegebeneseinkommens_MutationAbFolgeMonat_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(HUNDERT_THAUSEND);
		BGCalculationResult resultVorgaenger = initResultData(ZWEI_HUNDERT_THAUSEND);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ.handleEinkommen(bgCalculationInput, resultVorgaenger, new Betreuung(), EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.plusMonths(1));
		Assertions.assertEquals(ZWEI_HUNDERT_THAUSEND, bgCalculationInput.getMassgebendesEinkommen());
	}

	@Test
	void test_hoereMassgegebeneseinkommens_MutationBevorMonat_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZWEI_HUNDERT_THAUSEND);
		BGCalculationResult resultVorgaenger = initResultData(HUNDERT_THAUSEND);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ.handleEinkommen(bgCalculationInput, resultVorgaenger, new Betreuung(), EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.minusMonths(1));
		Assertions.assertEquals(ZWEI_HUNDERT_THAUSEND, bgCalculationInput.getMassgebendesEinkommen());
	}

	@Test
	void test_kleinerMassgegebeneseinkommens_MutationBevorMonat_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(HUNDERT_THAUSEND);
		BGCalculationResult resultVorgaenger = initResultData(ZWEI_HUNDERT_THAUSEND);
		MUTATIONS_MERGER_FINANZIELLE_SITUATION_SCHWYZ.handleEinkommen(bgCalculationInput, resultVorgaenger, new Betreuung(), EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.minusMonths(1));
		Assertions.assertEquals(HUNDERT_THAUSEND, bgCalculationInput.getMassgebendesEinkommen());
	}

	private BGCalculationInput initInputData(BigDecimal massgegebendeseinkommen) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(new DateRange(START_VERFUEGUNG_ABSCHNITT_ERSTGESUCH, END_VERFUEGUNG_ABSCHNITT_ERSTGESUCH));
		BGCalculationInput input = new BGCalculationInput(verfuegungZeitabschnitt, RuleValidity.ASIV);
		input.setMassgebendesEinkommenVorAbzugFamgr(massgegebendeseinkommen);
		input.setEinkommensjahr(2018);
		input.setFamGroesse(BigDecimal.ZERO);
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setSozialhilfeempfaenger(false);
		return input;
	}

	private BGCalculationResult initResultData(BigDecimal massgegebendeseinkommen) {
		BGCalculationResult result = new BGCalculationResult();
		result.setMassgebendesEinkommenVorAbzugFamgr(massgegebendeseinkommen);
		result.setEinkommensjahr(2018);
		result.setFamGroesse(BigDecimal.ZERO);
		result.setAbzugFamGroesse(BigDecimal.ZERO);
		result.setSozialhilfeAkzeptiert(true);
		return result;
	}

}
