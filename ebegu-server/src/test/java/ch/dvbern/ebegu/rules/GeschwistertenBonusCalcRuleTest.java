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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.apache.maven.wagon.InputData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.needle4j.annotation.ObjectUnderTest;

public class GeschwistertenBonusCalcRuleTest {

	@ObjectUnderTest
	private GeschwisterbonusCalcRule ruleToTest;

	@Nonnull
	private Betreuung betreuung;

	@Nonnull
	private BGCalculationInput inputData;

	@Before
	public void setUp() {
		Mandant luzern = new Mandant();
		luzern.setMandantIdentifier(MandantIdentifier.LUZERN);
		DateRange validy = new DateRange(LocalDate.of(1000, 1, 1), LocalDate.of(3000, 1, 1));
		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
				Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
				Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
				BetreuungsangebotTyp.KITA,
				60,
				new BigDecimal(2000),
				luzern);
		ErwerbspensumContainer erwerbspensumContainer = TestDataUtil.createErwerbspensumContainer();
		Assert.assertNotNull(erwerbspensumContainer.getErwerbspensumJA());
		erwerbspensumContainer.getErwerbspensumJA().setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		Assert.assertNotNull(betreuung.extractGesuch().getGesuchsteller1());
		Assert.assertNotNull(Objects.requireNonNull(betreuung.extractGesuch().getGesuchsteller1())
				.getErwerbspensenContainers());
		Objects.requireNonNull(betreuung.extractGesuch().getGesuchsteller1())
				.addErwerbspensumContainer(erwerbspensumContainer);
		ruleToTest = new GeschwisterbonusCalcRule(validy, Constants.DEUTSCH_LOCALE);
		assert betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA() != null;
		inputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
	}

	@Test
	public void oneKindShouldHaveNeitherBonus() {
		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertFalse(inputData.isGeschwisternBonusKind2());
		Assert.assertFalse(inputData.isGeschwisternBonusKind3());
	}

	@Test
	public void kindWithOlderGeschwisterWithoutBGShouldHaveNeitherBonus() {
		addOldestKindToGesuch();
		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertFalse(inputData.isGeschwisternBonusKind2());
		Assert.assertFalse(inputData.isGeschwisternBonusKind3());
	}

	@Test
	public void kindWithOlderGeschwisterWithBGShouldHaveBonus() {
		KindContainer olderKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung olderKindBetreuung = olderKind.getBetreuungen().stream().findFirst().orElseThrow();
		// Younger Kind
		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertTrue(inputData.isGeschwisternBonusKind2());
		Assert.assertFalse(inputData.isGeschwisternBonusKind3());
		// Older Kind
		BGCalculationInput olderInputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		ruleToTest.executeRule(olderKindBetreuung, olderInputData);
		Assert.assertFalse(olderInputData.isGeschwisternBonusKind2());
		Assert.assertFalse(olderInputData.isGeschwisternBonusKind3());
	}

	@Test
	public void kindWithTwoOlderGeschwisterWithBGShouldHaveBonus3() {
		// middle Kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		// Oldest kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		// Younger Kind
		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertFalse(inputData.isGeschwisternBonusKind2());
		Assert.assertTrue(inputData.isGeschwisternBonusKind3());
	}

	@Test
	public void kindWithTwoOrMoreOlderGeschwisterWithBGShouldHaveBonus3() {
		// middle Kind
		KindContainer second = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung secondBetreuung = second.getBetreuungen().stream().findFirst().orElseThrow();
		// Second-oldest kind
		KindContainer secondOldest = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung secondOldestBetreuung = secondOldest.getBetreuungen().stream().findFirst().orElseThrow();
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		// Youngest Kind
		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertFalse(inputData.isGeschwisternBonusKind2());
		Assert.assertTrue(inputData.isGeschwisternBonusKind3());
		// second-youngest Kind
		BGCalculationInput secondInputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		ruleToTest.executeRule(secondBetreuung, secondInputData);
		Assert.assertFalse(secondInputData.isGeschwisternBonusKind2());
		Assert.assertTrue(secondInputData.isGeschwisternBonusKind3());
		// Second-oldest kind
		BGCalculationInput secondOldestInputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		ruleToTest.executeRule(secondOldestBetreuung, secondOldestInputData);
		Assert.assertTrue(secondOldestInputData.isGeschwisternBonusKind2());
		Assert.assertFalse(secondOldestInputData.isGeschwisternBonusKind3());
	}

	@Test
	public void kindWithTwoYoungerGeschwisterWithBGShouldHaveNeitherBonus() {
		// middle Kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		KindContainer oldestKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung oldestKindBetreuung = oldestKind.getBetreuungen().stream().findFirst().orElseThrow();

		BGCalculationInput oldestInputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		ruleToTest.executeRule(oldestKindBetreuung, oldestInputData);
		Assert.assertFalse(oldestInputData.isGeschwisternBonusKind2());
		Assert.assertFalse(oldestInputData.isGeschwisternBonusKind3());
	}

	@Test
	public void kindWithOlderAndYoungerGeschwisterWithBGShouldHaveBonus2() {
		KindContainer olderKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung olderKindBetreuung = olderKind.getBetreuungen().stream().findFirst().orElseThrow();

		// oldest Kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		BGCalculationInput olderInputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		ruleToTest.executeRule(olderKindBetreuung, olderInputData);
		Assert.assertTrue(olderInputData.isGeschwisternBonusKind2());
		Assert.assertFalse(olderInputData.isGeschwisternBonusKind3());
	}

	@Test
	public void kindWithOlderGeschwisterWithoutBGAndYoungerGeschwisterWithBGShouldHaveBonus2() {
		createOldestKindForGesuch(betreuung.extractGesuch());
		createYoungestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertFalse(inputData.isGeschwisternBonusKind2());
		Assert.assertFalse(inputData.isGeschwisternBonusKind3());
	}

	@Test
	public void kindWithOlderGeschwisterWithBGAndYoungerGeschwisterWithBGShouldHaveBonus2() {
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		createYoungestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertTrue(inputData.isGeschwisternBonusKind2());
		Assert.assertFalse(inputData.isGeschwisternBonusKind3());
	}

	@Test
	public void equallyOldKinderShouldCheckTimestampCreated() {
		KindContainer olderKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung olderKindBetreuung = olderKind.getBetreuungen().stream().findFirst().orElseThrow();

		olderKind.getKindJA().setGeburtsdatum(betreuung.getKind().getKindJA().getGeburtsdatum());
		assert betreuung.getKind().getKindJA().getTimestampErstellt() != null;
		olderKind.getKindJA().setTimestampErstellt(betreuung.getKind().getKindJA().getTimestampErstellt().minusSeconds(1));

		// Younger Kind by timestamp
		ruleToTest.executeRule(betreuung, inputData);
		Assert.assertTrue(inputData.isGeschwisternBonusKind2());
		Assert.assertFalse(inputData.isGeschwisternBonusKind3());
		// Older Kind by timestamp
		BGCalculationInput olderInputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		ruleToTest.executeRule(olderKindBetreuung, olderInputData);
		Assert.assertFalse(olderInputData.isGeschwisternBonusKind2());
		Assert.assertFalse(olderInputData.isGeschwisternBonusKind3());
	}

	private void addOldestKindToGesuch() {
		final Gesuch gesuch = betreuung.extractGesuch();
		KindContainer toAdd = createOldestKindForGesuch(gesuch);
		gesuch.getKindContainers().add(toAdd);
	}

	private KindContainer createOldestKindForGesuch(Gesuch gesuch) {
		KindContainer toAdd = TestDataUtil.createDefaultKindContainer();
		Optional<KindContainer> currentYoungest =
				findOldestKind(gesuch);
		currentYoungest.ifPresent(kind -> toAdd.getKindJA()
				.setGeburtsdatum(kind.getKindJA().getGeburtsdatum().minusDays(1)));
		toAdd.setGesuch(gesuch);
		return toAdd;
	}

	private Optional<KindContainer> findOldestKind(Gesuch gesuch) {
		return gesuch
				.getKindContainers()
				.stream()
				.min(Comparator.comparing((KindContainer a) -> a.getKindJA()
						.getGeburtsdatum()));
	}

	private KindContainer createOldestKindWithBetreuungForGesuch(Gesuch gesuch) {
		KindContainer toAdd = createOldestKindForGesuch(gesuch);
		gesuch.getKindContainers().add(toAdd);

		toAdd.getBetreuungen().add(TestDataUtil.createDefaultBetreuung(toAdd));
		return toAdd;
	}

	private KindContainer createYoungestKindWithBetreuungForGesuch(Gesuch gesuch) {
		KindContainer toAdd = createYoungestKindForGesuch(gesuch);
		gesuch.getKindContainers().add(toAdd);

		toAdd.getBetreuungen().add(TestDataUtil.createDefaultBetreuung(toAdd));
		return toAdd;
	}


	private KindContainer createYoungestKindForGesuch(Gesuch gesuch) {
		KindContainer toAdd = TestDataUtil.createDefaultKindContainer();
		Optional<KindContainer> currentYoungest =
				findYoungestKind(gesuch);
		currentYoungest.ifPresent(kind -> toAdd.getKindJA()
				.setGeburtsdatum(kind.getKindJA().getGeburtsdatum().plusDays(1)));
		toAdd.setGesuch(gesuch);
		return toAdd;
	}

	private Optional<KindContainer> findYoungestKind(Gesuch gesuch) {
		return gesuch
				.getKindContainers()
				.stream()
				.max(Comparator.comparing((KindContainer a) -> a.getKindJA()
						.getGeburtsdatum()));
	}
}
