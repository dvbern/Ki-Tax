/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Assert;
import org.junit.Test;

public class SchulstufeCalcRuleTest {

	@Test
	public void kindVorschulalter() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(100, EinschulungTyp.VORSCHULALTER));
		assertBerechtigt(result);
	}

	@Test
	public void kindKindergarten1() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(100, EinschulungTyp.KINDERGARTEN1));
		assertBerechtigt(result);
	}

	@Test
	public void kindKindergarten2() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(100, EinschulungTyp.KINDERGARTEN2));
		assertBerechtigt(result);
	}

	@Test
	public void kindSchule() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(100, EinschulungTyp.KLASSE1));
		assertNichtBerechtigt(result);
	}

	private void assertBerechtigt(List<VerfuegungZeitabschnitt> result) {
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = result.get(0);
		Assert.assertEquals(100, verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertNotNull(verfuegungZeitabschnitt.getBemerkungen());
		Assert.assertFalse(verfuegungZeitabschnitt.getBemerkungenList().isEmpty());
		Assert.assertEquals(2, result.get(0).getBemerkungenList().uniqueSize());
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	private void assertNichtBerechtigt(List<VerfuegungZeitabschnitt> result) {
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = result.get(0);
		Assert.assertEquals(0, verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertNotNull(verfuegungZeitabschnitt.getBemerkungen());
		Assert.assertFalse(result.get(0).getBemerkungenList().isEmpty());
		Assert.assertEquals(2, result.get(0).getBemerkungenList().uniqueSize());
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG));
	}

	private Betreuung prepareData(final int pensum, final EinschulungTyp schulstufe) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		betreuung.getKind().getKindJA().setEinschulungTyp(schulstufe);
		GesuchstellerContainer gesuchsteller1 = betreuung.extractGesuch().getGesuchsteller1();
		Assert.assertNotNull(gesuchsteller1);
		gesuchsteller1.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, pensum));
		return betreuung;
	}
}
