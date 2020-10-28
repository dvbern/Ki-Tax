/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Test;

import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;

/**
 * Testet die BetreuungsangebotTyp Regel
 */
public class BetreuungsangebotTypCalcRuleTest {

	@Test
	public void testAngebotKita() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(BetreuungsangebotTyp.KITA));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).getBemerkungenList().isEmpty());
		Assert.assertEquals(2, result.get(0).getBemerkungenList().uniqueSize());
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testAngebotTageselternKleinkind() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(BetreuungsangebotTyp.TAGESFAMILIEN));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(60 + ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).getBemerkungenList().isEmpty());
		Assert.assertEquals(2, result.get(0).getBemerkungenList().uniqueSize());
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	@Test
	public void testAngebotTagesschule() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(BetreuungsangebotTyp.TAGESSCHULE));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).getBemerkungenList().isEmpty());
		Assert.assertEquals(1, result.get(0).getBemerkungenList().uniqueSize());
		Assert.assertTrue(result.get(0).getBemerkungenList().containsMsgKey(MsgKey.BETREUUNGSANGEBOT_MSG));
	}

	private AbstractPlatz prepareData(BetreuungsangebotTyp betreuungsangebotTyp) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(betreuungsangebotTyp);
		Assert.assertNotNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60));
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum());
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(BigDecimal.valueOf(80));
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(BigDecimal.ZERO);
		betreuungspensumContainer.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(BigDecimal.ZERO);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
		if (betreuungsangebotTyp.isTagesschule()) {
			AnmeldungTagesschule anmeldung = TestDataUtil.createAnmeldungTagesschuleWithModules(betreuung.getKind(),
				betreuung.extractGesuchsperiode());
			betreuung.getKind().getAnmeldungenTagesschule().add(anmeldung);
			return anmeldung;
		} else {
			return betreuung;
		}
	}
}
