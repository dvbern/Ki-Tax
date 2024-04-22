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
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.needle4j.annotation.ObjectUnderTest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class GeschwisterbonusSchwyzAbschnittRuleTest {

	@ObjectUnderTest
	private GeschwisterbonusSchwyzAbschnittRule ruleToTest;

	@Nonnull
	private Betreuung betreuung;

	@Nonnull
	private Gesuch gesuch;
	private static final LocalDate GP_START = Constants.GESUCHSPERIODE_17_18_AB;
	private static final LocalDate GP_END = Constants.GESUCHSPERIODE_17_18_BIS;

	@BeforeEach
	public void setUp() {
		DateRange validity = new DateRange(LocalDate.of(1000, 1, 1), LocalDate.of(3000, 1, 1));
		betreuung = createBetreuung();
		gesuch = betreuung.extractGesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		ruleToTest = new GeschwisterbonusSchwyzAbschnittRule(validity, Constants.DEUTSCH_LOCALE);
	}

	@Test
	void oneKindShouldHaveNoGeschwisterAbschnitt() {
		final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);
		assertThat(verfuegungZeitabschnitte.isEmpty(), is(true));
	}

	@Nested
	class OneGeschwister {
		@Test
		void oneOtherKindOver18DuringPeriode_shouldCreateNoZeitabschnitte() {
			final LocalDate geburtsdatumGeschwister = GP_START.minusYears(20);
			addGeschwister(geburtsdatumGeschwister);

			final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);
			assertThat(verfuegungZeitabschnitte.isEmpty(), is(true));

		}

		@Nested
		class ZeitabschnittGueltigkeitTest {
			@Test
			void oneOtherKindU18ganzePeriode_shouldCreateOneZeitabschnittForEntirePeriode() {
				addGeschwister(GP_END.minusYears(5));

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.size(), is(1));
				assertThat(verfuegungZeitabschnitte.get(0).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(1));
			}

			@Test
			void oneOtherKindBornDuringPeriode_shouldCreateZeitabschnitteBeforeAndFromBirth() {
				final LocalDate geburtsdatumGeschwister = GP_START.plusMonths(2);
				final LocalDate geburtsdatumGeschwisterInGP = LocalDate.of(
					GP_START.getYear(),
					geburtsdatumGeschwister.getMonth(),
					geburtsdatumGeschwister.getDayOfMonth());
				addGeschwister(geburtsdatumGeschwister);
				addGeschwister(geburtsdatumGeschwister);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigAb(), equalTo(GP_START));
				assertThat(
					verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(geburtsdatumGeschwisterInGP));

				assertThat(
					verfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(geburtsdatumGeschwisterInGP.plusDays(1)));
				assertThat(verfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigBis(), equalTo(GP_END));
			}

			@Test
			void oneOtherKindReaching18DuringPeriode_shouldCreateZeitabschnitteBeforeAndFromBirthday() {
				final LocalDate geburtsdatumGeschwister = GP_START.plusMonths(2).minusYears(18);
				final LocalDate geburtsdatumGeschwisterInGP = LocalDate.of(
					GP_START.getYear(),
					geburtsdatumGeschwister.getMonth(),
					geburtsdatumGeschwister.getDayOfMonth());
				addGeschwister(geburtsdatumGeschwister);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigAb(), equalTo(GP_START));
				assertThat(
					verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(geburtsdatumGeschwisterInGP));

				assertThat(
					verfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(geburtsdatumGeschwisterInGP.plusDays(1)));
				assertThat(verfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigBis(), equalTo(GP_END));
			}

			@Test
			void oneOtherKindOver18DuringPeriode_shouldCreateNoZeitabschnitte() {
				final LocalDate geburtsdatumGeschwister = GP_START.minusYears(20);
				addGeschwister(geburtsdatumGeschwister);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);
				assertThat(verfuegungZeitabschnitte.isEmpty(), is(true));

			}
		}

		@Nested
		class AnzahlGeschwisterTest {
			@Test
			void oneOtherKindU18ganzePeriode_shouldHaveAnzahlGeschwister1ForEntirePeriode() {
				addGeschwister(GP_END.minusYears(5));

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.size(), is(1));
				assertThat(verfuegungZeitabschnitte.get(0).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(1));
			}

			@Test
			void oneOtherKindBornDuringPeriode_shouldshouldHaveAnzahlGeschwister1AfterBirth() {
				final LocalDate geburtsdatumGeschwister = GP_START.plusMonths(2);
				addGeschwister(geburtsdatumGeschwister);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(0));
				assertThat(verfuegungZeitabschnitte.get(1).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(1));
			}

			@Test
			void oneOtherKindReaching18DuringPeriode_shouldCreateZeitabschnitteBeforeAndFromBirthday() {
				final LocalDate geburtsdatumGeschwister = GP_START.plusMonths(2).minusYears(18);
				addGeschwister(geburtsdatumGeschwister);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(1));
				assertThat(verfuegungZeitabschnitte.get(1).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(0));
			}
		}
	}

	@Nested
	class TwoGeschwisterTest {
		@Test
		void twoOtherKindOver18DuringPeriode_shouldCreateNoZeitabschnitte() {
			final LocalDate geburtsdatumGeschwister = GP_START.minusYears(20);
			addGeschwister(geburtsdatumGeschwister);
			addGeschwister(geburtsdatumGeschwister);

			final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);
			assertThat(verfuegungZeitabschnitte.isEmpty(), is(true));

		}

		@Nested
		class ZeitabschnittGueltigkeitTest {
			@Test
			void twoOtherKindU18ganzePeriode_shouldCreateOneZeitabschnittForEntirePeriode() {
				addGeschwister(GP_END.minusYears(5));
				addGeschwister(GP_END.minusYears(5));

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigAb(), equalTo(GP_START));
				assertThat(verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigBis(), equalTo(GP_END));
			}

			@Test
			void oneKindU18ganzePeriodeoneOtherKindBornDuringPeriode_shouldCreateZeitabschnitteBeforeAndFromBirth() {
				final LocalDate geburtsdatumGeschwister = GP_START.plusMonths(2);
				addGeschwister(GP_START.minusYears(12));
				addGeschwister(geburtsdatumGeschwister);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigAb(), equalTo(GP_START));
				assertThat(
					verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(geburtsdatumGeschwister));

				assertThat(
					verfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(geburtsdatumGeschwister.plusDays(1)));
				assertThat(verfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigBis(), equalTo(GP_END));
			}

			@Test
			void twoOtherKindReaching18DuringPeriode_shouldCreateZeitabschnitteBeforeAndFromEachBirthday() {
				final LocalDate geburtsdatumGeschwister = GP_START.plusMonths(2).minusYears(18);
				final LocalDate geburtsdatumGeschwister1InGP = LocalDate.of(
					GP_START.getYear(),
					geburtsdatumGeschwister.getMonth(),
					geburtsdatumGeschwister.getDayOfMonth());
				addGeschwister(geburtsdatumGeschwister);
				final LocalDate geburtsdatumGeschwister2 = GP_START.plusMonths(7).minusYears(18);
				final LocalDate geburtsdatumGeschwister2InGP = LocalDate.of(
					GP_END.getYear(),
					geburtsdatumGeschwister2.getMonth(),
					geburtsdatumGeschwister2.getDayOfMonth());
				addGeschwister(geburtsdatumGeschwister);
				addGeschwister(geburtsdatumGeschwister);
				addGeschwister(geburtsdatumGeschwister2);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigAb(), equalTo(GP_START));
				assertThat(
					verfuegungZeitabschnitte.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(geburtsdatumGeschwister1InGP));

				assertThat(
					verfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(geburtsdatumGeschwister1InGP.plusDays(1)));
				assertThat(
					verfuegungZeitabschnitte.get(1).getGueltigkeit().getGueltigBis(),
					equalTo(geburtsdatumGeschwister2InGP));

				assertThat(
					verfuegungZeitabschnitte.get(2).getGueltigkeit().getGueltigAb(),
					equalTo(geburtsdatumGeschwister2InGP.plusDays(1)));
				assertThat(verfuegungZeitabschnitte.get(2).getGueltigkeit().getGueltigBis(), equalTo(GP_END));
			}
		}

		@Nested
		class AnzahlGeschwisterTest {
			@Test
			void twoOtherKindU18ganzePeriode_shouldCreateOneZeitabschnittForEntirePeriode() {
				addGeschwister(GP_END.minusYears(5));
				addGeschwister(GP_END.minusYears(5));

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.size(), is(1));
				assertThat(verfuegungZeitabschnitte.get(0).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(2));
			}

			@Test
			void oneKindU18ganzePeriodeoneOtherKindBornDuringPeriode_shouldCreateZeitabschnitteBeforeAndFromBirth() {
				final LocalDate geburtsdatumGeschwister = GP_START.plusMonths(2);
				addGeschwister(GP_START.minusYears(12));
				addGeschwister(geburtsdatumGeschwister);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(1));
				assertThat(verfuegungZeitabschnitte.get(1).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(2));
			}

			@Test
			void twoOtherKindReaching18DuringPeriode_shouldCreateZeitabschnitteBeforeAndFromEachBirthday() {
				final LocalDate geburtsdatumGeschwister = GP_START.plusMonths(2).minusYears(18);
				final LocalDate geburtsdatumGeschwister2 = GP_START.plusMonths(7).minusYears(18);
				addGeschwister(geburtsdatumGeschwister);
				addGeschwister(geburtsdatumGeschwister2);

				final List<VerfuegungZeitabschnitt> verfuegungZeitabschnitte = executeRule(betreuung);

				assertThat(verfuegungZeitabschnitte.get(0).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(2));
				assertThat(verfuegungZeitabschnitte.get(1).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(1));
				assertThat(verfuegungZeitabschnitte.get(2).getBgCalculationInputAsiv().getAnzahlGeschwister(), is(0));
			}
		}
	}

	private void addGeschwister(LocalDate geburtsdatum) {
		KindContainer kind2 = TestDataUtil.createDefaultKindContainer();
		kind2.getKindJA().setGeburtsdatum(geburtsdatum);
		kind2.getBetreuungen().add(TestDataUtil.createDefaultBetreuung(40, GP_START, GP_END));
		gesuch.getKindContainers().add(kind2);
	}

	private Betreuung createBetreuung() {
		Mandant schwyz = new Mandant();
		schwyz.setMandantIdentifier(MandantIdentifier.SCHWYZ);

		return EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.GESUCHSPERIODE_17_18.getGueltigAb(),
			Constants.GESUCHSPERIODE_17_18.getGueltigBis(),
			BetreuungsangebotTyp.KITA,
			60,
			new BigDecimal(2000),
			schwyz);
	}

	private List<VerfuegungZeitabschnitt> executeRule(Betreuung betreuung) {
		return ruleToTest.createVerfuegungsZeitabschnitte(betreuung);
	}

}
