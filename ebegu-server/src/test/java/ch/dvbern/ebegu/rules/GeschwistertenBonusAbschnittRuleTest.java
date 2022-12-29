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
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.needle4j.annotation.ObjectUnderTest;

public class GeschwistertenBonusAbschnittRuleTest {

	@ObjectUnderTest
	private GeschwisterbonusAbschnittRule ruleToTest;

	@Nonnull
	private Betreuung betreuung;

	@Before
	public void setUp() {
		Mandant luzern = new Mandant();
		luzern.setMandantIdentifier(MandantIdentifier.LUZERN);
		DateRange validy = new DateRange(LocalDate.of(1000, 1, 1), LocalDate.of(3000, 1, 1));
		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.GESUCHSPERIODE_17_18.getGueltigAb(),
			Constants.GESUCHSPERIODE_17_18.getGueltigBis(),
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
		ruleToTest = new GeschwisterbonusAbschnittRule(EinschulungTyp.VORSCHULALTER, validy, Constants.DEUTSCH_LOCALE);
		assert betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA() != null;
	}

	@Test
	public void oneKindShouldHaveNeitherBonus() {
		executeRulesAndAssertFalse(betreuung);
	}

	@Test
	public void kindWithOlderGeschwisterWithoutBGShouldHaveNeitherBonus() {
		addOldestKindToGesuch();
		executeRulesAndAssertFalse(betreuung);
	}

	@Test
	public void kindWithOlderGeschwisterWithBGShouldHaveBonus() {
		KindContainer olderKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung olderKindBetreuung = olderKind.getBetreuungen().stream().findFirst().orElseThrow();
		// Younger Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertTrue(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
		// Older Kind
		executeRulesAndAssertFalse(olderKindBetreuung);
	}

	@Test
	public void kindWithOlderGeschwisterWithBGOnlyInAugustShouldHaveBonusOnlyInAugust() {
		KindContainer olderKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung olderKindBetreuung = olderKind.getBetreuungen().stream().findFirst().orElseThrow();
		BetreuungspensumContainer betreuungspensumContainer =
			olderKindBetreuung.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		DateRange augustOnly = new DateRange();
		augustOnly.setGueltigAb(Constants.GESUCHSPERIODE_17_18_AB);
		augustOnly.setGueltigBis(LocalDate.of(2017, 8, 30));
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(augustOnly);

		// Younger Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			if (verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == Month.AUGUST) {
				Assert.assertTrue(verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind2());
			} else {
				Assert.assertFalse(verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind2());
			}
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
	}

	@Test
	public void kindWithTwoOlderGeschwisterWithBGShouldHaveBonus3() {
		// middle Kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		// Oldest kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		// Younger Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertTrue(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
	}

	@Test
	public void kindWithTwoOlderGeschwisterWithBGInAugustShouldHaveBonus3InAugust() {
		// middle Kind
		KindContainer middleKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung middleKindBetreuung = middleKind.getBetreuungen().stream().findFirst().orElseThrow();
		BetreuungspensumContainer betreuungspensumContainerMiddleKid =
			middleKindBetreuung.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		DateRange augustOnly = new DateRange();
		augustOnly.setGueltigAb(Constants.GESUCHSPERIODE_17_18_AB);
		augustOnly.setGueltigBis(LocalDate.of(2017, 8, 30));
		betreuungspensumContainerMiddleKid.getBetreuungspensumJA().setGueltigkeit(augustOnly);

		// Oldest kind
		KindContainer olderKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung olderKindBetreuung = olderKind.getBetreuungen().stream().findFirst().orElseThrow();
		BetreuungspensumContainer betreuungspensumContainer =
			olderKindBetreuung.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		augustOnly.setGueltigAb(Constants.GESUCHSPERIODE_17_18_AB);
		augustOnly.setGueltigBis(LocalDate.of(2017, 8, 30));
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(augustOnly);

		// Younger Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			if (verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == Month.AUGUST) {
				Assert.assertTrue(verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind3());
			} else {
				Assert.assertFalse(verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind3());
			}
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
		});
	}

	@Test
	public void kindWithTwoOlderGeschwisterWithBGInAugustForOneOfThemShouldHaveBonus3InAugustOnlyAndBonus2After() {
		// middle Kind
		KindContainer middleKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung middleKindBetreuung = middleKind.getBetreuungen().stream().findFirst().orElseThrow();
		BetreuungspensumContainer betreuungspensumContainerMiddleKid =
			middleKindBetreuung.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		DateRange augustOnly = new DateRange();
		augustOnly.setGueltigAb(Constants.GESUCHSPERIODE_17_18_AB);
		augustOnly.setGueltigBis(LocalDate.of(2017, 8, 30));
		betreuungspensumContainerMiddleKid.getBetreuungspensumJA().setGueltigkeit(augustOnly);

		// Oldest kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		// Younger Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			if (verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == Month.AUGUST) {
				Assert.assertTrue(verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind3());
				Assert.assertFalse(verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind2());
			} else {
				Assert.assertFalse(verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind3());
				Assert.assertTrue(verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind2());
			}
		});
	}


	@Test
	public void kindWithTwoOrMoreOlderGeschwisterWithBGShouldHaveBonus3() {
		// middle Kind
		KindContainer thirdOldest = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung thirdOldestBetreuung = thirdOldest.getBetreuungen().stream().findFirst().orElseThrow();
		// Second-oldest kind
		KindContainer secondOldest = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung secondOldestBetreuung = secondOldest.getBetreuungen().stream().findFirst().orElseThrow();
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		// Youngest Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertTrue(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
		// second-youngest Kind
		verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(thirdOldestBetreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertTrue(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
		// Second-oldest kind
		verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(secondOldestBetreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertTrue(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
	}

	@Test
	public void kindWithTwoYoungerGeschwisterWithBGShouldHaveNeitherBonus() {
		// middle Kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		KindContainer oldestKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung oldestKindBetreuung = oldestKind.getBetreuungen().stream().findFirst().orElseThrow();

		executeRulesAndAssertFalse(oldestKindBetreuung);
	}

	@Test
	public void kindWithOlderAndYoungerGeschwisterWithBGShouldHaveBonus2() {
		KindContainer olderKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung olderKindBetreuung = olderKind.getBetreuungen().stream().findFirst().orElseThrow();
		// oldest Kind
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(olderKindBetreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertTrue(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
	}

	@Test
	public void kindWithOlderGeschwisterWithoutBGAndYoungerGeschwisterWithBGShouldNotHaveBonus2() {
		createOldestKindForGesuch(betreuung.extractGesuch());
		createYoungestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		executeRulesAndAssertFalse(betreuung);
	}

	@Test
	public void kindWithOlderGeschwisterWithBGAndYoungerGeschwisterWithBGShouldHaveBonus2() {
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		createYoungestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertTrue(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
	}

	@Test
	public void equallyOldKinderShouldCheckTimestampCreated() {
		KindContainer olderKind = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());
		Betreuung olderKindBetreuung = olderKind.getBetreuungen().stream().findFirst().orElseThrow();

		olderKind.getKindJA().setGeburtsdatum(betreuung.getKind().getKindJA().getGeburtsdatum());
		assert betreuung.getKind().getKindJA().getTimestampErstellt() != null;
		olderKind.getKindJA()
			.setTimestampErstellt(betreuung.getKind().getKindJA().getTimestampErstellt().minusSeconds(1));

		// Younger Kind by timestamp
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertTrue(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
		// Older Kind by timestamp
		BGCalculationInput olderInputData = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		executeRulesAndAssertFalse(olderKindBetreuung);
	}

	@Test
	public void testKindergartenKindHasNoBonus() {
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN2);
		executeRulesAndAssertFalse(betreuung);
	}

	@Test
	public void testKindWithOlderKindergartenGeschwisterShouldHaveNoBonus() {
		KindContainer kindContainer = createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		kindContainer.getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);
		executeRulesAndAssertFalse(betreuung);

		kindContainer.getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN2);
		executeRulesAndAssertFalse(betreuung);
	}

	@Test
	public void testSchulKindHasNoBonus() {
		createOldestKindWithBetreuungForGesuch(betreuung.extractGesuch());

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE1);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE2);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE3);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE4);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE5);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE6);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE7);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE8);
		executeRulesAndAssertFalse(betreuung);

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE9);
		executeRulesAndAssertFalse(betreuung);
	}

	private void executeRulesAndAssertFalse(@Nonnull Betreuung betreuungToExecute) {
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuungToExecute);
		verfuegungZeitabschnittList.stream().forEach(verfuegungZeitabschnitt -> {
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertFalse(verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
	}

	private void addOldestKindToGesuch() {
		final Gesuch gesuch = betreuung.extractGesuch();
		KindContainer toAdd = createOldestKindForGesuch(gesuch);
		gesuch.getKindContainers().add(toAdd);
	}

	private KindContainer createOldestKindForGesuch(Gesuch gesuch) {
		KindContainer toAdd = TestDataUtil.createDefaultKindContainer();
		Optional<KindContainer> currentOldest =
			findOldestKind(gesuch);
		currentOldest.ifPresent(kind -> toAdd.getKindJA()
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

		toAdd.getBetreuungen().add(createBetreuungMitBetreuungpensum(toAdd));
		return toAdd;
	}

	private KindContainer createYoungestKindWithBetreuungForGesuch(Gesuch gesuch) {
		KindContainer toAdd = createYoungestKindForGesuch(gesuch);
		gesuch.getKindContainers().add(toAdd);

		toAdd.getBetreuungen().add(createBetreuungMitBetreuungpensum(toAdd));
		return toAdd;
	}

	private Betreuung createBetreuungMitBetreuungpensum(KindContainer kindContainer) {
		Betreuung betreuungToAdd = TestDataUtil.createDefaultBetreuungOhneBetreuungPensum(kindContainer);
		Betreuungspensum betreuungspensum = new Betreuungspensum();
		betreuungspensum.setGueltigkeit(Constants.GESUCHSPERIODE_17_18);
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);
		betreuungspensumContainer.setBetreuung(betreuungToAdd);
		betreuungToAdd.getBetreuungspensumContainers().add(betreuungspensumContainer);
		return betreuungToAdd;
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
