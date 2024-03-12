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
import java.util.Map;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
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
		assertNichtBerechtigtFuerKINDERGARTEN2(result);
	}

	@Test
	public void kindObligatorischerKindergartenAndPlatzInSchulhorstShouldNotBeBerechtigt() {
		final Betreuung betreuung = prepareData(100, EinschulungTyp.OBLIGATORISCHER_KINDERGARTEN);
		final Map<EinstellungKey, Einstellung> einstellungen =
				EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(betreuung.extractGesuchsperiode());
		einstellungen.put(
				EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
				new Einstellung(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
						EinschulungTyp.FREIWILLIGER_KINDERGARTEN.toString(),
						betreuung.extractGesuchsperiode()));
		betreuung.getKind().getKindJA().setKeinPlatzInSchulhort(false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungen);
		assertNichtBerechtigtFuerFREIWILLIGER_KINDERGARTEN(result);
	}

	@Test
	public void kindObligatorischerKindergartenAndKeinPlatzInSchulhorstShouldBeBerechtigt() {
		final Betreuung betreuung = prepareData(100, EinschulungTyp.OBLIGATORISCHER_KINDERGARTEN);
		final Map<EinstellungKey, Einstellung> einstellungen =
				EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(betreuung.extractGesuchsperiode());
		einstellungen.put(
				EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
				new Einstellung(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
						EinschulungTyp.FREIWILLIGER_KINDERGARTEN.toString(),
						betreuung.extractGesuchsperiode()));
		betreuung.getKind().getKindJA().setKeinPlatzInSchulhort(true);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungen);
		assertBerechtigt(result);
	}

	@Test
	public void testPrimarSchulstufeBerechtigt() {
		final Betreuung betreuung = prepareData(100, EinschulungTyp.SEKUNDAR_UND_HOEHER_STUFE);
		var einstellungen = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(betreuung.extractGesuchsperiode());
		einstellungen.get(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE).setValue(
			EinschulungTyp.PRIMARSTUFE.name());
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungen);
		assertBerechtigt(result);
	}

	@Test
	public void testPrimarSchulstufeNichtBerechtigt() {
		final Betreuung betreuung = prepareData(100, EinschulungTyp.PRIMARSTUFE);
		var einstellungen = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(betreuung.extractGesuchsperiode());
		einstellungen.get(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE).setValue(
			EinschulungTyp.PRIMARSTUFE.name());
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungen);
		assertNichtBerechtigtFuerPRIMARSTUFE(result);
	}

	private void assertBerechtigt(List<VerfuegungZeitabschnitt> result) {
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = result.get(0);
		Assert.assertEquals(100, verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertNotNull(verfuegungZeitabschnitt.getVerfuegungZeitabschnittBemerkungList());
		Assert.assertFalse(verfuegungZeitabschnitt.getBemerkungenDTOList().isEmpty());
		Assert.assertEquals(2, result.get(0).getBemerkungenDTOList().uniqueSize());
		Assert.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH));
	}

	private void assertNichtBerechtigtFuerKINDERGARTEN2(List<VerfuegungZeitabschnitt> result) {
		assertNichtBerechtigt(result, MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG);
	}

	private void assertNichtBerechtigtFuerPRIMARSTUFE(List<VerfuegungZeitabschnitt> result) {
		assertNichtBerechtigt(result, MsgKey.SCHULSTUFE_PRIMARSTUFE_MSG);
	}

	private void assertNichtBerechtigtFuerFREIWILLIGER_KINDERGARTEN(List<VerfuegungZeitabschnitt> result) {
		assertNichtBerechtigt(result, MsgKey.SCHULSTUFE_FREIWILLIGER_KINDERGARTEN_MSG);
	}

	private void assertNichtBerechtigt(List<VerfuegungZeitabschnitt> result, MsgKey msgKey) {
		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = result.get(0);
		Assert.assertEquals(0, verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
		Assert.assertNotNull(verfuegungZeitabschnitt.getVerfuegungZeitabschnittBemerkungList());
		Assert.assertFalse(result.get(0).getBemerkungenDTOList().isEmpty());
		Assert.assertEquals(2, result.get(0).getBemerkungenDTOList().uniqueSize());
		Assert.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH));
		Assert.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(msgKey));
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
