/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraum;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.needle4j.annotation.ObjectUnderTest;

import static org.junit.Assert.assertNotNull;

public class SozialhilfeCalcRuleTest {

	@ObjectUnderTest
	private SozialhilfeKeinAnspruchCalcRule ruleToTest;

	private Betreuung defaultBetreuungForTest;
	private Map<EinstellungKey, Einstellung> einstellungen;

	private static final int DEFAULT_ERWERBSPENSUM = 80;

	@Before
	public void setUp() {
		DateRange validy = new DateRange(LocalDate.of(1000, 1, 1), LocalDate.of(3000, 1, 1));
		ruleToTest = new SozialhilfeKeinAnspruchCalcRule(validy, Constants.DEUTSCH_LOCALE);

		Mandant luzern = new Mandant();
		luzern.setMandantIdentifier(MandantIdentifier.LUZERN);

		defaultBetreuungForTest = createDefaultBetruungForTest();

		einstellungen = EbeguRuleTestsHelper.getAllEinstellungen(defaultBetreuungForTest.extractGesuchsperiode());
		einstellungen.get(EinstellungKey.ERWERBSPENSUM_ZUSCHLAG).setValue("0");
	}

	private Betreuung createDefaultBetruungForTest() {
		final Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);

		ErwerbspensumContainer erwerbspensum = TestDataUtil.createErwerbspensum(
			betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb(),
			betreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis(),
			DEFAULT_ERWERBSPENSUM);

		Objects.requireNonNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setErwerbspensenContainers(Set.of(erwerbspensum));

		return betreuung;
	}

	@Test
	public void ruleNotApplicableIfEinstellungNotActive()  {
		setHasSozialhilfeEmpfaengerAnspruchKonfig(false);
		Assert.assertFalse(ruleToTest.isRelevantForGemeinde(einstellungen));
	}

	@Test
	public void ruleApplicableIfEinstellungActive()  {
		setHasSozialhilfeEmpfaengerAnspruchKonfig(true);
		Assert.assertTrue(ruleToTest.isRelevantForGemeinde(einstellungen));
	}

	@Test
	public void testSozialhilfeEmpfaengerAnspruchZero() {
		setHasSozialhilfeEmpfaengerAnspruchKonfig(true);
		setDefaultBetreuungSozialhilfeEmpfaenger(true);

		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.GEMEINDE);
		ruleToTest.executeRule(defaultBetreuungForTest, input);
		Assert.assertEquals(0, input.getAnspruchspensumProzent());
		Assert.assertTrue(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.SOZIALHILFEEMPFAENGER_HABEN_KEINEN_ANSPRUCH));
	}

	@Test
	public void testNotSozialhilfeEmpfaengerHasAnspruch() {
		setHasSozialhilfeEmpfaengerAnspruchKonfig(true);
		setDefaultBetreuungSozialhilfeEmpfaenger(false);

		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.GEMEINDE);
		ruleToTest.executeRule(defaultBetreuungForTest, input);
		Assert.assertFalse(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.SOZIALHILFEEMPFAENGER_HABEN_KEINEN_ANSPRUCH));
	}

	@Test
	public void testSozialhilfeEmpfaengerAnspruchZeroWithRuleEngine() {
		setHasSozialhilfeEmpfaengerAnspruchKonfig(true);
		setDefaultBetreuungSozialhilfeEmpfaenger(true);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(defaultBetreuungForTest, einstellungen);
		assertNotNull(result);

		result.forEach(zeitabschnitt -> {
			BGCalculationInput input = zeitabschnitt.getRelevantBgCalculationInput();
			Assert.assertTrue(input.isSozialhilfeempfaenger());
			Assert.assertEquals(0, input.getAnspruchspensumProzent());
			Assert.assertTrue(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.SOZIALHILFEEMPFAENGER_HABEN_KEINEN_ANSPRUCH));
		});
	}

	@Test
	public void testNotSozialhilfeEmpfaengerHasAnspruchWithRuleEngine() {
		setHasSozialhilfeEmpfaengerAnspruchKonfig(true);
		setDefaultBetreuungSozialhilfeEmpfaenger(false);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(defaultBetreuungForTest, einstellungen);
		assertNotNull(result);

		result.forEach(zeitabschnitt -> {
			    BGCalculationInput input = zeitabschnitt.getRelevantBgCalculationInput();
				Assert.assertFalse(input.isSozialhilfeempfaenger());
				Assert.assertEquals(DEFAULT_ERWERBSPENSUM, input.getAnspruchspensumProzent());
				Assert.assertFalse(input.getParent().getBemerkungenDTOList().containsMsgKey(MsgKey.SOZIALHILFEEMPFAENGER_HABEN_KEINEN_ANSPRUCH));
			});
	}

	private void setDefaultBetreuungSozialhilfeEmpfaenger(boolean isSozialhilfeEmpfaenger) {
		Gesuch gesuch = defaultBetreuungForTest.extractGesuch();

		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());

		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setSozialhilfeBezueger(isSozialhilfeEmpfaenger);

		if (isSozialhilfeEmpfaenger) {
			// Create SozialhilfeZeitraum über gesamte Gesuchsperiode
			SozialhilfeZeitraum sozialhilfeZeitraum = new SozialhilfeZeitraum();
			sozialhilfeZeitraum.setGueltigkeit(defaultBetreuungForTest.extractGesuchsperiode().getGueltigkeit());
			SozialhilfeZeitraumContainer sozialhilfeZeitraumContainer = new SozialhilfeZeitraumContainer();
			sozialhilfeZeitraumContainer.setSozialhilfeZeitraumJA(sozialhilfeZeitraum);
			gesuch.getFamiliensituationContainer().setSozialhilfeZeitraumContainers(Set.of(sozialhilfeZeitraumContainer));
		}
	}

	private void setHasSozialhilfeEmpfaengerAnspruchKonfig(boolean hasSozialhilfeEmpfaengerAnspruch) {
		einstellungen
			.get(EinstellungKey.GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER)
			.setValue(String.valueOf(hasSozialhilfeEmpfaengerAnspruch));
	}


}
