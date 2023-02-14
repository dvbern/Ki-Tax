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
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
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

	@Nonnull
	private Gesuch gesuch;

	private static final DateRange AUGUST = new DateRange(
		Constants.GESUCHSPERIODE_17_18_AB,
		Constants.GESUCHSPERIODE_17_18_AB.with(TemporalAdjusters.lastDayOfMonth()));

	@Before
	public void setUp() {
		DateRange validy = new DateRange(LocalDate.of(1000, 1, 1), LocalDate.of(3000, 1, 1));
		betreuung = createBetreuung();
		gesuch = betreuung.extractGesuch();
		ruleToTest = new GeschwisterbonusAbschnittRule(EinschulungTyp.VORSCHULALTER, validy, Constants.DEUTSCH_LOCALE);
	}

	private Betreuung createBetreuung() {
		Mandant luzern = new Mandant();
		luzern.setMandantIdentifier(MandantIdentifier.LUZERN);

		return EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.GESUCHSPERIODE_17_18.getGueltigAb(),
			Constants.GESUCHSPERIODE_17_18.getGueltigBis(),
			BetreuungsangebotTyp.KITA,
			60,
			new BigDecimal(2000),
			luzern);
	}

	@Test
	public void oneKindShouldHaveNeitherBonus() {
		assertNoGeschwisternBonus(executeRule(betreuung));
	}

	@Test
	public void twoKidsWithBetreuung() {
		Betreuung betreuungOldestKind = createBetreuungWithOldestKindAndAddToGesuch();
		assertNoGeschwisternBonus(executeRule(betreuungOldestKind));
		assertGeschwisternBonus2(executeRule(betreuung));
	}

	@Test
	public void threeKidsWithBereuung() {
		Betreuung betreuungOldestKind = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung betreuungYoungestKind = createBetreuungWithYoungestKindAndAddToGesuch();


		assertNoGeschwisternBonus(executeRule(betreuungOldestKind));
		assertGeschwisternBonus2(executeRule(betreuung));
		assertGeschwisternBonus3(executeRule(betreuungYoungestKind));
	}

	@Test
	public void fourKidsWithBetreuung() {
		Betreuung thirdOldest = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung secondOldest = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung oldest = createBetreuungWithOldestKindAndAddToGesuch();

		assertGeschwisternBonus3(executeRule(betreuung)); //Test Youngest Kind
		assertGeschwisternBonus3(executeRule(thirdOldest));
		assertGeschwisternBonus2(executeRule(secondOldest));
		assertNoGeschwisternBonus(executeRule(oldest));
	}

	@Test
	public void kindWithOlderGeschwisterWithBGOnlyInAugustShouldHaveBonusOnlyInAugust() {
		Betreuung olderKindBetreuung = createBetreuungWithOldestKindAndAddToGesuch();
		BetreuungspensumContainer betreuungspensumContainer =
			olderKindBetreuung.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(AUGUST);

		// Younger Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
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
	public void kindWithTwoOlderGeschwisterWithBGInAugustShouldHaveBonus3InAugust() {
		// middle Kind
		Betreuung middleKindBetreuung = createBetreuungWithOldestKindAndAddToGesuch();
		BetreuungspensumContainer betreuungspensumContainerMiddleKid =
			middleKindBetreuung.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		betreuungspensumContainerMiddleKid.getBetreuungspensumJA().setGueltigkeit(AUGUST);

		// Oldest kind
		Betreuung olderKindBetreuung = createBetreuungWithOldestKindAndAddToGesuch();
		BetreuungspensumContainer betreuungspensumContainer =
			olderKindBetreuung.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(AUGUST);

		// Younger Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
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
		Betreuung middleKindBetreuung = createBetreuungWithOldestKindAndAddToGesuch();
		BetreuungspensumContainer betreuungspensumContainerMiddleKid =
			middleKindBetreuung.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		betreuungspensumContainerMiddleKid.getBetreuungspensumJA().setGueltigkeit(AUGUST);

		// Oldest kind
		createBetreuungWithOldestKindAndAddToGesuch();

		// Younger Kind
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList =
			ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
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
	public void equallyOldKinderShouldCheckTimestampCreated() {
		Betreuung olderKindBetreuung = createBetreuungWithOldestKindAndAddToGesuch();
		olderKindBetreuung.getKind().getKindJA().setGeburtsdatum(betreuung.getKind().getKindJA().getGeburtsdatum());

		assert betreuung.getKind().getKindJA().getTimestampErstellt() != null;
		olderKindBetreuung.getKind().getKindJA()
			.setTimestampErstellt(betreuung.getKind().getKindJA().getTimestampErstellt().minusSeconds(1));

		// Younger Kind by timestamp
		assertGeschwisternBonus2(executeRule(betreuung));
		// Older Kind by timestamp
		assertNoGeschwisternBonus(executeRule(olderKindBetreuung));
	}

	@Test
	public void testKindergartenKindHasNoBonus() {
		Betreuung secondKind = createBetreuungWithYoungestKindAndAddToGesuch();
		Betreuung youngestKind = createBetreuungWithYoungestKindAndAddToGesuch();

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN2);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));
	}

	@Test
	public void testSchulKindHasNoBonus() {
		Betreuung secondKind = createBetreuungWithYoungestKindAndAddToGesuch();
		Betreuung youngestKind = createBetreuungWithYoungestKindAndAddToGesuch();

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE1);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE2);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE3);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE4);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE5);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE6);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE7);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE8);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE9);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoGeschwisternBonus(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));
	}

	private Betreuung createBetreuungWithYoungestKindAndAddToGesuch() {
		KindContainer kind = createYoungestKindForGesuch();
		gesuch.getKindContainers().add(kind);
		Betreuung betreuungY = createBetreuungMitBetreuungpensum(kind);
		kind.getBetreuungen().add(betreuungY);
		return betreuungY;
	}

	private Betreuung createBetreuungWithOldestKindAndAddToGesuch() {
		KindContainer kind = createOldestKindForGesuch();
		gesuch.getKindContainers().add(kind);
		Betreuung betreuungY = createBetreuungMitBetreuungpensum(kind);
		kind.getBetreuungen().add(betreuungY);
		return betreuungY;
	}

	private List<VerfuegungZeitabschnitt> executeRule(@Nonnull Betreuung betreuungToExecute) {
		return ruleToTest.createVerfuegungsZeitabschnitte(betreuungToExecute);
	}

	private void assertNoZeitabschnitteCreatedInRule(@Nonnull List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList) {
		Assert.assertTrue(verfuegungZeitabschnittList.isEmpty());
	}

	private void assertNoGeschwisternBonus(@Nonnull List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList) {
		Assert.assertFalse(verfuegungZeitabschnittList.isEmpty());
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
			Assert.assertFalse("GeschwisternBonus 2 gewährt, obwohl kein Geschwisternbonus erwartet",
				verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind2());
			Assert.assertFalse("GeschwisternBonus 3 gewährt, obwohl kein Geschwisternbonus erwartet",
				verfuegungZeitabschnitt
					.getRelevantBgCalculationInput()
					.isGeschwisternBonusKind3());
		});
	}

	private void assertGeschwisternBonus2(@Nonnull List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList) {
		Assert.assertFalse(verfuegungZeitabschnittList.isEmpty());
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
			Assert.assertTrue("GeschwisternBonus 2 nicht gewährt, obwohl erwartet",
				verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertFalse("GeschwisternBonus 3 gewährt, obwohl Geschwisternbonus 2 erwartet",
				verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
	}

	private void assertGeschwisternBonus3(@Nonnull List<VerfuegungZeitabschnitt> verfuegungZeitabschnittList) {
		Assert.assertFalse(verfuegungZeitabschnittList.isEmpty());
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
			Assert.assertFalse("GeschwisternBonus 2 gewährt, obwohl Geschwisternbonus 3 erwartet",
				verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind2());
			Assert.assertTrue("GeschwisternBonus 3 nicht gewährt, obwohl erwartet",
				verfuegungZeitabschnitt
				.getRelevantBgCalculationInput()
				.isGeschwisternBonusKind3());
		});
	}

	private KindContainer createOldestKindForGesuch() {
		KindContainer toAdd = TestDataUtil.createDefaultKindContainer();
		Optional<KindContainer> currentOldest =
			findOldestKind();
		currentOldest.ifPresent(kind -> toAdd.getKindJA()
			.setGeburtsdatum(kind.getKindJA().getGeburtsdatum().minusDays(1)));
		toAdd.setGesuch(gesuch);
		return toAdd;
	}

	private Optional<KindContainer> findOldestKind() {
		return gesuch
			.getKindContainers()
			.stream()
			.min(Comparator.comparing((KindContainer a) -> a.getKindJA()
				.getGeburtsdatum()));
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

	private KindContainer createYoungestKindForGesuch() {
		KindContainer toAdd = TestDataUtil.createDefaultKindContainer();
		Optional<KindContainer> currentYoungest = findYoungestKind();
		currentYoungest.ifPresent(kind -> toAdd.getKindJA()
			.setGeburtsdatum(kind.getKindJA().getGeburtsdatum().plusDays(1)));
		toAdd.setGesuch(gesuch);
		return toAdd;
	}

	private Optional<KindContainer> findYoungestKind() {
		return gesuch
			.getKindContainers()
			.stream()
			.max(Comparator.comparing((KindContainer a) -> a.getKindJA()
				.getGeburtsdatum()));
	}
}
