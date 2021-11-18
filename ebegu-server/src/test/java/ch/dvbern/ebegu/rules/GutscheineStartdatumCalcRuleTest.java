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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rules.util.BemerkungsMerger;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GutscheineStartdatumCalcRuleTest {

	@Test
	public void executRuleAbschnittVorStartdatum() {
		DateRange period = Constants.DEFAULT_GUELTIGKEIT;
		GutscheineStartdatumCalcRule rule = new GutscheineStartdatumCalcRule(period, Constants.DEFAULT_LOCALE);
		VerfuegungZeitabschnitt zeitabschnitt = initZeitabschnitt(false);
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getKind().setGesuch(TestDataUtil.createDefaultGesuch());

		rule.executeRuleIfApplicable(betreuung, zeitabschnitt);
		BemerkungsMerger.prepareGeneratedBemerkungen(zeitabschnitt);

		assertEquals(0, zeitabschnitt.getAnspruchberechtigtesPensum());
		assertEquals(
			"Für diesen Zeitraum stellt die Gemeinde London noch keine Betreuungsgutscheine aus.",
			zeitabschnitt.getVerfuegungZeitabschnittBemerkungList().get(0).getBemerkung());
	}

	@Test
	public void executRuleAbschnittNachStartdatum() {
		DateRange period = Constants.DEFAULT_GUELTIGKEIT;
		GutscheineStartdatumCalcRule rule = new GutscheineStartdatumCalcRule(period, Constants.DEFAULT_LOCALE);
		VerfuegungZeitabschnitt zeitabschnitt = initZeitabschnitt(true);
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();

		rule.executeRuleIfApplicable(betreuung, zeitabschnitt);
		BemerkungsMerger.prepareGeneratedBemerkungen(zeitabschnitt);

		assertEquals(100, zeitabschnitt.getRelevantBgCalculationInput().getAnspruchspensumProzent());
		assertNotNull(zeitabschnitt.getVerfuegungZeitabschnittBemerkungList());
		assertTrue(zeitabschnitt.getVerfuegungZeitabschnittBemerkungList().isEmpty());
	}

	@Nonnull
	private VerfuegungZeitabschnitt initZeitabschnitt(boolean liegtNachStartdatum) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.getBgCalculationInputAsiv().setAnspruchspensumProzent(100);
		zeitabschnitt.getBgCalculationInputAsiv().setAbschnittLiegtNachBEGUStartdatum(liegtNachStartdatum);
		return zeitabschnitt;
	}
}
