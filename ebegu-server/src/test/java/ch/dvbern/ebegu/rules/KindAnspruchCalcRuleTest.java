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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KindAnspruchCalcRuleTest {

	@Nonnull
	private Betreuung betreuung;

	@Nonnull
	private Map<EinstellungKey, Einstellung> einstellungen;

	@Before
	public void setUp() {
		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
			Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
			BetreuungsangebotTyp.KITA,
			60,
			new BigDecimal(2000));
		// erwerbspensum fuer GS
		final Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		GesuchstellerContainer gesuchsteller1 = betreuung.extractGesuch().getGesuchsteller1();
		gesuchsteller1.addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 100));

		// Einstellung mit KinderAbzuzTyp = SCHWYZ
		einstellungen = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(betreuung.extractGesuch().getGesuchsperiode());
		einstellungen.get(EinstellungKey.KINDERABZUG_TYP).setValue(KinderabzugTyp.SCHWYZ.name());
	}

	@Test
	public void testKindBeitragsberechtigt() {
		betreuung.getKind().getKindJA().setUnterhaltspflichtig(true);
		betreuung.getKind().getKindJA().setLebtKindAlternierend(true);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungen);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.KEIN_ANSPRUCH_NICHT_BEITRAGSBERECHTIGT));
	}

	@Test
	public void testKindNichtBeitragsberechtigtKindNichtAlternierend() {
		betreuung.getKind().getKindJA().setUnterhaltspflichtig(true);
		betreuung.getKind().getKindJA().setLebtKindAlternierend(false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungen);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).getBemerkungenDTOList().isEmpty());
		Assert.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.KEIN_ANSPRUCH_NICHT_BEITRAGSBERECHTIGT));
	}

	@Test
	public void testKindNichtBeitragsberechtigtKindNichtUnterhaltspflichtig() {
		betreuung.getKind().getKindJA().setUnterhaltspflichtig(false);
		betreuung.getKind().getKindJA().setLebtKindAlternierend(null);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungen);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).getBemerkungenDTOList().isEmpty());
		Assert.assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.KEIN_ANSPRUCH_NICHT_BEITRAGSBERECHTIGT));
	}
}
