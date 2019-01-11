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
		GutscheineStartdatumCalcRule rule = new GutscheineStartdatumCalcRule(period);
		VerfuegungZeitabschnitt zeitabschnitt = initZeitabschnitt(false);
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getKind().setGesuch(TestDataUtil.createDefaultGesuch());

		rule.executeRule(betreuung, zeitabschnitt);
		BemerkungsMerger.prepareGeneratedBemerkungen(zeitabschnitt);

		assertEquals(0, zeitabschnitt.getAnspruchberechtigtesPensum());
		assertEquals(
			"BEGU_STARTDATUM: Für diesen Zeitraum stellt die Gemeinde Bern keine Betreuungsgutscheine aus.",
			zeitabschnitt.getBemerkungen());
	}

	@Test
	public void executRuleAbschnittNachStartdatum() {
		DateRange period = Constants.DEFAULT_GUELTIGKEIT;
		GutscheineStartdatumCalcRule rule = new GutscheineStartdatumCalcRule(period);
		VerfuegungZeitabschnitt zeitabschnitt = initZeitabschnitt(true);
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();

		rule.executeRule(betreuung, zeitabschnitt);
		BemerkungsMerger.prepareGeneratedBemerkungen(zeitabschnitt);

		assertEquals(100, zeitabschnitt.getAnspruchberechtigtesPensum());
		assertNotNull(zeitabschnitt.getBemerkungen());
		assertTrue(zeitabschnitt.getBemerkungen().isEmpty());
	}

	@Nonnull
	private VerfuegungZeitabschnitt initZeitabschnitt(boolean liegtNachStartdatum) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setAnspruchberechtigtesPensum(100);
		zeitabschnitt.setAbschnittLiegtNachBEGUStartdatum(liegtNachStartdatum);
		return zeitabschnitt;
	}
}
