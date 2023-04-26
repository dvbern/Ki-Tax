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
	private static final DateRange SEPTEMBER = new DateRange(
		Constants.GESUCHSPERIODE_17_18_AB.plusMonths(1),
		Constants.GESUCHSPERIODE_17_18_AB.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));

	private static final LocalDate END_OF_TIME = LocalDate.of(9999, Month.DECEMBER, 31);

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
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
	}

	@Test
	public void twoKidsWithBetreuung() {
		Betreuung betreuungOldestKind = createBetreuungWithOldestKindAndAddToGesuch();
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuungOldestKind));
		assertGeschwisternBonus2(executeRule(betreuung));
	}

	@Test
	public void twoKidsWithBetreuungAusserhalbGP() {
		Betreuung betreuungYoungestKind = createBetreuungWithYoungestKindAndAddToGesuch();

		DateRange ausserhalbGP = new DateRange(Constants.GESUCHSPERIODE_17_18_AB.minusYears(1),
											Constants.GESUCHSPERIODE_17_18_BIS);

		betreuungYoungestKind.getBetreuungspensumContainers()
				.stream()
				.findFirst()
				.orElseThrow()
				.getBetreuungspensumJA()
				.setGueltigkeit(ausserhalbGP);

		//Oldest Betreuung nur im August
		betreuung.getBetreuungspensumContainers()
				.stream()
				.findFirst()
				.orElseThrow()
				.getBetreuungspensumJA()
				.setGueltigkeit(AUGUST);

		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));

		//Zweites Kind soll nur im August Geschwisternbonus erhalten
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittYoungestList =
				ruleToTest.createVerfuegungsZeitabschnitte(betreuungYoungestKind);

		verfuegungZeitabschnittYoungestList.forEach(verfuegungZeitabschnitt -> {
			LocalDate gueltigAb = verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb();
			if (gueltigAb.getMonth() == Month.AUGUST
				&& gueltigAb.getYear() == Constants.GESUCHSPERIODE_17_18_AB.getYear()) {
				assertGeschwisternBonus2(List.of(verfuegungZeitabschnitt));
			} else {
				assertNoGeschwisternBonus(List.of(verfuegungZeitabschnitt));
			}
		});
	}

	@Test
	public void threeKidsWithBereuung() {
		Betreuung betreuungOldestKind = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung betreuungYoungestKind = createBetreuungWithYoungestKindAndAddToGesuch();


		assertNoZeitabschnitteCreatedInRule(executeRule(betreuungOldestKind));
		assertGeschwisternBonus2(executeRule(betreuung));
		assertGeschwisternBonus3(executeRule(betreuungYoungestKind));
	}

	@Test
	public void threeKidsWithBereuungTillEndOfTime() {
		Betreuung betreuungOldestKind = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung betreuungYoungestKind = createBetreuungWithYoungestKindAndAddToGesuch();

		betreuung.getBetreuungspensumContainers().stream()
			.findFirst()
			.orElseThrow()
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigBis(END_OF_TIME);

		assertNoZeitabschnitteCreatedInRule(executeRule(betreuungOldestKind));
		assertGeschwisternBonus2(executeRule(betreuung));
		assertGeschwisternBonus3(executeRule(betreuungYoungestKind));
	}

	@Test
	public void threeKidsWithBereuungKeinPlatzImSchulhorn() {
		Betreuung betreuungOldestKind = createBetreuungWithOldestKindAndAddToGesuch();
		betreuungOldestKind.getKind().getKindJA().setKeinPlatzInSchulhort(true);
		betreuungOldestKind.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE1);
		Betreuung betreuungYoungestKind = createBetreuungWithYoungestKindAndAddToGesuch();


		assertNoZeitabschnitteCreatedInRule(executeRule(betreuungOldestKind));
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
		assertNoZeitabschnitteCreatedInRule(executeRule(oldest));
	}



	@Test
	public void untermonatlicheBetreuung() {
		Betreuung thirdOldest = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung secondOldest = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung oldest = createBetreuungWithOldestKindAndAddToGesuch();

		DateRange midSeptemberTillMidOcober = new DateRange(
			SEPTEMBER.getGueltigAb().plusDays(15),
			SEPTEMBER.getGueltigBis().plusDays(15)
		);

		BetreuungspensumContainer betreuungspensumContainer =
			secondOldest.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(midSeptemberTillMidOcober);

		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittThirdOldestList =
			ruleToTest.createVerfuegungsZeitabschnitte(thirdOldest);
		Assert.assertFalse(verfuegungZeitabschnittThirdOldestList.isEmpty());

		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittSecondOldestList =
			ruleToTest.createVerfuegungsZeitabschnitte(secondOldest);
		Assert.assertFalse(verfuegungZeitabschnittSecondOldestList.isEmpty());

		// Expected Result
		// Other than September, Ocotober = Bonus 2 thirdOldes, Bonus 3 youngest
		// September, October  = Bonus 2 secondOldest, Bonus 3 thirdOldest and Youngest
		assertGeschwisternBonus3(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(oldest));

		verfuegungZeitabschnittThirdOldestList.forEach(zeitabschnitt -> {
			Month month = zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth();
			if (month == Month.SEPTEMBER || month == Month.OCTOBER) {
				assertGeschwisternBonus3(List.of(zeitabschnitt));
			} else {
				assertGeschwisternBonus2(List.of(zeitabschnitt));
			}
		});

		verfuegungZeitabschnittSecondOldestList.forEach(zeitabschnitt -> {
			Month month = zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth();
			if (month == Month.SEPTEMBER || month == Month.OCTOBER) {
				assertGeschwisternBonus2(List.of(zeitabschnitt));
			} else {
				assertNoGeschwisternBonus(List.of(zeitabschnitt));
			}
		});
	}

	@Test
	public void fourKidsOneWithBetreuungOnlyInSeptember() {
		Betreuung thirdOldest = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung secondOldest = createBetreuungWithOldestKindAndAddToGesuch();
		Betreuung oldest = createBetreuungWithOldestKindAndAddToGesuch();

		BetreuungspensumContainer betreuungspensumContainer =
			secondOldest.getBetreuungspensumContainers().stream().findFirst().orElseThrow();
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(SEPTEMBER);

		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittThirdOldestList =
			ruleToTest.createVerfuegungsZeitabschnitte(thirdOldest);
		Assert.assertFalse(verfuegungZeitabschnittThirdOldestList.isEmpty());

		List<VerfuegungZeitabschnitt> verfuegungZeitabschnittSecondOldestList =
			ruleToTest.createVerfuegungsZeitabschnitte(secondOldest);
		Assert.assertFalse(verfuegungZeitabschnittSecondOldestList.isEmpty());

		// Expected Result
		// Other than September = Bonus 2 thirdOldes, Bonus 3 youngest
		// September = Bonus 2 secondOldest, Bonus 3 thirdOldest and Youngest
		assertGeschwisternBonus3(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(oldest));

		verfuegungZeitabschnittThirdOldestList.forEach(zeitabschnitt -> {
			if (zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == Month.SEPTEMBER) {
				assertGeschwisternBonus3(List.of(zeitabschnitt));
			} else {
				assertGeschwisternBonus2(List.of(zeitabschnitt));
			}
		});

		verfuegungZeitabschnittSecondOldestList.forEach(zeitabschnitt -> {
			if (zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == Month.SEPTEMBER) {
				assertGeschwisternBonus2(List.of(zeitabschnitt));
			} else {
				assertNoGeschwisternBonus(List.of(zeitabschnitt));
			}
		});
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

		Assert.assertFalse(verfuegungZeitabschnittList.isEmpty());
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
			if (verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == Month.AUGUST) {
				assertGeschwisternBonus2(List.of(verfuegungZeitabschnitt));
			} else {
				assertNoGeschwisternBonus(List.of(verfuegungZeitabschnitt));
			}
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

		Assert.assertFalse(verfuegungZeitabschnittList.isEmpty());
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
			if (verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == Month.AUGUST) {
				assertGeschwisternBonus3(List.of(verfuegungZeitabschnitt));
			} else {
				assertNoGeschwisternBonus(List.of(verfuegungZeitabschnitt));
			}
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
		Assert.assertFalse(verfuegungZeitabschnittList.isEmpty());
		verfuegungZeitabschnittList.forEach(verfuegungZeitabschnitt -> {
			if (verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() == Month.AUGUST) {
				assertGeschwisternBonus3(List.of(verfuegungZeitabschnitt));
			} else {
				assertGeschwisternBonus2(List.of(verfuegungZeitabschnitt));
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
		assertNoZeitabschnitteCreatedInRule(executeRule(olderKindBetreuung));
	}

	@Test
	public void testKindergartenKindHasNoBonus() {
		Betreuung secondKind = createBetreuungWithYoungestKindAndAddToGesuch();
		Betreuung youngestKind = createBetreuungWithYoungestKindAndAddToGesuch();

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KINDERGARTEN2);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));
	}

	@Test
	public void testSchulKindHasNoBonus() {
		Betreuung secondKind = createBetreuungWithYoungestKindAndAddToGesuch();
		Betreuung youngestKind = createBetreuungWithYoungestKindAndAddToGesuch();

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE1);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE2);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE3);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE4);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE5);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE6);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE7);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE8);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
		assertGeschwisternBonus2(executeRule(youngestKind));

		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.KLASSE9);
		assertNoZeitabschnitteCreatedInRule(executeRule(betreuung));
		assertNoZeitabschnitteCreatedInRule(executeRule(secondKind));
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
