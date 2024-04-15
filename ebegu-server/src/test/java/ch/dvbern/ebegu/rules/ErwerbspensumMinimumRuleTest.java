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

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnittBemerkung;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static ch.dvbern.ebegu.enums.EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM;
import static ch.dvbern.ebegu.enums.EinstellungKey.KITAPLUS_ZUSCHLAG_AKTIVIERT;
import static ch.dvbern.ebegu.util.Constants.ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests für ErwerbspensumMinimumRule
 */
public class ErwerbspensumMinimumRuleTest extends AbstractBGRechnerTest {

	private Map<EinstellungKey, Einstellung> einstellungenMap;

	@Before
	public void init() {
		einstellungenMap = EbeguRuleTestsHelper.getAllEinstellungen(TestDataUtil.createGesuchsperiode1718());
		einstellungenMap.get(ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM).setValue(AnspruchBeschaeftigungAbhaengigkeitTyp.MINIMUM.name());
	}

	@Test
	public void test1GSKeinErwerbspensum() {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);
		assertKeinAnspruch(result);
	}

	@Test
	public void test1GSErwerbspensumLessThanMinimum() {
		final Betreuung betreuung = createBetreuungWithPensum(10, 0, false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);
		assertKeinAnspruch(result);
	}

	@Test
	public void test1GSErwerbspensumMinimum() {
		Betreuung betreuung = createBetreuungWithPensum(20,0, false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung,einstellungenMap);
		assert100ProzentAnspruch(result);
	}

	@Test
	public void test1GSErwerbspensumMaximum() {
		Betreuung betreuung = createBetreuungWithPensum(100,0, false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung,einstellungenMap);
		assert100ProzentAnspruch(result);
	}

	@Test
	public void test1GSErwerbspensumMoreThanMaximum() {
		Betreuung betreuung = createBetreuungWithPensum(120,0, false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung,einstellungenMap);
		assert100ProzentAnspruch(result);
	}

	@Test
	public void test2GSKeinErwerbspensum() {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(true);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);
		assertKeinAnspruch(result);
	}

	@Test
	public void test2GSErwerbspensumLessThanMinimum() {
		final Betreuung betreuung = createBetreuungWithPensum(10, 100, true);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);
		assertKeinAnspruch(result);
	}

	@Test
	public void test2GSErwerbspensumLessThanMinimumInTotal() {
		final Betreuung betreuung = createBetreuungWithPensum(10, 120, true);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);
		assertKeinAnspruch(result);
	}

	@Test
	public void test2GSErwerbspensumMinimum() {
		Betreuung betreuung = createBetreuungWithPensum(20,100, true);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung,einstellungenMap);
		assert100ProzentAnspruch(result);
	}

	@Test
	public void test2GSErwerbspensumMaximum() {
		Betreuung betreuung = createBetreuungWithPensum(100,100, true);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung,einstellungenMap);
		assert100ProzentAnspruch(result);
	}

	@Test
	public void test2GSErwerbspensumMoreThanMaximum() {
		Betreuung betreuung = createBetreuungWithPensum(120,100, false);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung,einstellungenMap);
		assert100ProzentAnspruch(result);
	}

	private void assertKeinAnspruch(List<VerfuegungZeitabschnitt> result) {
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		assertTrue(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_MINIMUM_NICHT_ERRECHT));
	}

	private void assert100ProzentAnspruch(List<VerfuegungZeitabschnitt> result) {
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(100, result.get(0).getAnspruchberechtigtesPensum());
		assertFalse(result.get(0).getBemerkungenDTOList().containsMsgKey(MsgKey.ERWERBSPENSUM_MINIMUM_NICHT_ERRECHT));
	}

	private Betreuung createBetreuungWithPensum(int pensumGS1, int pensumGS2, boolean gs2) {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(gs2);
		Gesuch gesuch = betreuung.extractGesuch();

		Assert.assertNotNull(gesuch.getGesuchsteller1());

		ErwerbspensumContainer ewpGS1 =
			TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, pensumGS1);
		gesuch.getGesuchsteller1().setErwerbspensenContainers(Set.of(ewpGS1));

		if (gs2) {
			ErwerbspensumContainer ewpGS2 =
				TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, pensumGS2);
			Assert.assertNotNull(gesuch.getGesuchsteller2());
			gesuch.getGesuchsteller2().setErwerbspensenContainers(Set.of(ewpGS2));
		}

		return betreuung;
	}


}
