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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DateUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class BetreuungTest {

	private static final LocalDate AUG_1 = LocalDate.of(2023, 8,1);
	private static final LocalDate AUG_15 = LocalDate.of(2023, 8,15);
	private static final LocalDate AUG_16 = LocalDate.of(2023, 8,16);
	private static final LocalDate OCT_1 = LocalDate.of(2023, 10,1);
	private static final LocalDate OCT_16 = LocalDate.of(2023, 10,16);
	private static final LocalDate NOV_30 = LocalDate.of(2023, 11,30);
	private static final LocalDate JUL_31 = LocalDate.of(2024, 7,31);

	@Nested
	class AbweichungenTest {

		@Test
		void oneMultiMonthPensum_whenFillAbweichungen_shouldHaveAbweichungWithPensumForFullPensum() {
			final BigDecimal pensum = BigDecimal.valueOf(80);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(pensum, AUG_1, NOV_30));

			List<BetreuungspensumAbweichung> abweichungen = betreuung.fillAbweichungen(BigDecimal.ONE);
			final BetreuungspensumAbweichung abweichungAug = abweichungen.get(0);
			final BetreuungspensumAbweichung abweichungSep = abweichungen.get(1);
			final BetreuungspensumAbweichung abweichungOct = abweichungen.get(2);
			final BetreuungspensumAbweichung abweichungNov = abweichungen.get(3);
			final BetreuungspensumAbweichung abweichungDec = abweichungen.get(4);


			assertThat(abweichungen.size(), is(12));
			assertThat(abweichungAug.getVertraglichesPensum(), comparesEqualTo(pensum));
			assertThat(abweichungSep.getVertraglichesPensum(), comparesEqualTo(pensum));
			assertThat(abweichungOct.getVertraglichesPensum(), comparesEqualTo(pensum));
			assertThat(abweichungNov.getVertraglichesPensum(), comparesEqualTo(pensum));
			assertThat(abweichungDec.getVertraglichesPensum(), nullValue());
		}

		@Test
		void monthWithPartialPensum_whenFillAbweichungen_shouldHaveAbweichungWithPartialPensumForEntireFirstMonth() {
			final BigDecimal monthAnteil = DateUtil.calculateAnteilMonatInklWeekend(AUG_1, AUG_15);
			final BigDecimal pensum = BigDecimal.valueOf(80);
			Betreuung betreuung = setupBetreuung();
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(pensum, AUG_1, AUG_15));

			List<BetreuungspensumAbweichung> abweichungen = betreuung.fillAbweichungen(BigDecimal.ONE);
			final BetreuungspensumAbweichung abweichungAug = abweichungen.get(0);
			final BetreuungspensumAbweichung abweichungSep = abweichungen.get(1);

			assertThat(abweichungen.size(), is(12));
			assertThat(abweichungAug.getVertraglichesPensum(), comparesEqualTo(pensum.multiply(monthAnteil)));
			assertThat(abweichungSep.getVertraglichesPensum(), nullValue());
		}

		@Test
		void monthWithMultiplePensen_whenFillAbweichungen_shouldHaveAbweichungWithAddedPensenForEntireFirstMonthOnly() {
			Betreuung betreuung = setupBetreuung();
			final BigDecimal pensum = BigDecimal.valueOf(80);
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(pensum, AUG_1, AUG_15));
			betreuung.getBetreuungspensumContainers().add(createBetreuungspensum(pensum, AUG_16, NOV_30));

			List<BetreuungspensumAbweichung> abweichungen = betreuung.fillAbweichungen(BigDecimal.ONE);
			final BetreuungspensumAbweichung abweichungAug = abweichungen.get(0);

			assertThat(abweichungen.size(), is(12));
			assertThat(abweichungAug.getVertraglichesPensum(), comparesEqualTo(pensum));
		}

		@Nested
		class EingewoehnungPauschaleTest {

			@Test
			void pensumOverMonthEnd_whenFillAbweichung_shouldHaveAbweichungWithEingewoehnungPauschaleForEntireFirstMonth() {
				final BigDecimal pensum = BigDecimal.valueOf(80);
				final BigDecimal pauschale = BigDecimal.valueOf(500);
				final BetreuungspensumContainer betreuungspensum = createBetreuungspensum(pensum, AUG_15, NOV_30);
				Betreuung betreuung = setupBetreuung();
				betreuungspensum.getBetreuungspensumJA().setEingewoehnungPauschale(createEinewoehnungPauschale(pauschale, AUG_1, AUG_15));
				betreuung.getBetreuungspensumContainers().add(betreuungspensum);

				List<BetreuungspensumAbweichung> abweichungen = betreuung.fillAbweichungen(BigDecimal.ONE);
				final EingewoehnungPauschale eingewoehnungPauschaleAug = abweichungen.get(0).getVertraglicheEingewoehnungPauschale();

				assertThat(eingewoehnungPauschaleAug, notNullValue());
				assertThat(eingewoehnungPauschaleAug.getPauschale(), comparesEqualTo(pauschale));
				assertThat(eingewoehnungPauschaleAug.getGueltigkeit().getGueltigAb(), is(AUG_1));
				assertThat(eingewoehnungPauschaleAug.getGueltigkeit().getGueltigBis(), is(AUG_15));
			}

			@Test
			void pensumOverMonthEndNotFirstMonth_whenFillAbweichung_shouldHaveAbweichungWithEingewoehnungPauschaleForEntireFirstMonth() {
				final BigDecimal pensum = BigDecimal.valueOf(80);
				final BigDecimal pauschale = BigDecimal.valueOf(500);
				final BetreuungspensumContainer betreuungspensum = createBetreuungspensum(pensum, OCT_16, NOV_30);
				Betreuung betreuung = setupBetreuung();
				betreuungspensum.getBetreuungspensumJA().setEingewoehnungPauschale(createEinewoehnungPauschale(pauschale, AUG_1, AUG_15));
				betreuung.getBetreuungspensumContainers().add(betreuungspensum);

				List<BetreuungspensumAbweichung> abweichungen = betreuung.fillAbweichungen(BigDecimal.ONE);
				final BetreuungspensumAbweichung abweichungAug = abweichungen.get(0);
				final BetreuungspensumAbweichung abweichungSep = abweichungen.get(1);
				final BetreuungspensumAbweichung abweichungOct = abweichungen.get(2);
				final EingewoehnungPauschale eingewoehnungPauschaleOct = abweichungOct.getVertraglicheEingewoehnungPauschale();

				assertThat(abweichungAug.getVertraglicheEingewoehnungPauschale(), nullValue());
				assertThat(abweichungSep.getVertraglicheEingewoehnungPauschale(), nullValue());
				assertThat(eingewoehnungPauschaleOct, notNullValue());
				assertThat(eingewoehnungPauschaleOct.getPauschale(), comparesEqualTo(pauschale));
				assertThat(eingewoehnungPauschaleOct.getGueltigkeit().getGueltigAb(), is(AUG_1));
				assertThat(eingewoehnungPauschaleOct.getGueltigkeit().getGueltigBis(), is(AUG_15));
			}

			@Test
			void multiplePensumInMonth_whenFillAbweichung_shouldHaveAbweichungWithBothEingewoehnungPauschaleAddedForEntireFirstMonth() {
				final BigDecimal pensum = BigDecimal.valueOf(80);
				final BigDecimal pauschale = BigDecimal.valueOf(500);
				final BetreuungspensumContainer betreuungspensum1 = createBetreuungspensum(pensum, AUG_1, AUG_15);
				final BetreuungspensumContainer betreuungspensum2 = createBetreuungspensum(pensum, AUG_16, NOV_30);
				Betreuung betreuung = setupBetreuung();
				betreuungspensum1.getBetreuungspensumJA().setEingewoehnungPauschale(createEinewoehnungPauschale(pauschale, AUG_1, AUG_15));
				betreuungspensum2.getBetreuungspensumJA().setEingewoehnungPauschale(createEinewoehnungPauschale(pauschale, AUG_16, OCT_16));
				betreuung.getBetreuungspensumContainers().add(betreuungspensum1);
				betreuung.getBetreuungspensumContainers().add(betreuungspensum2);

				List<BetreuungspensumAbweichung> abweichungen = betreuung.fillAbweichungen(BigDecimal.ONE);
				final BetreuungspensumAbweichung abweichungAug = abweichungen.get(0);
				final BetreuungspensumAbweichung abweichungSep = abweichungen.get(1);
				final EingewoehnungPauschale eingewoehnungPauschaleAug = abweichungAug.getVertraglicheEingewoehnungPauschale();
				final BigDecimal pauschaleTwoTimes = pauschale.add(pauschale);

				assertThat(eingewoehnungPauschaleAug, notNullValue());
				assertThat(eingewoehnungPauschaleAug.getPauschale(), comparesEqualTo(pauschaleTwoTimes));
				assertThat(eingewoehnungPauschaleAug.getGueltigkeit().getGueltigAb(), is(AUG_1));
				assertThat(eingewoehnungPauschaleAug.getGueltigkeit().getGueltigBis(), is(OCT_16));
				assertThat(abweichungSep.getVertraglicheEingewoehnungPauschale(), nullValue());
			}

			@Test
			void multipleOneMonthPensenWithGap_whenFillAbweichung_shouldHaveAbweichungWithEingewoehnungPauschaleForBothMonths() {
				final BigDecimal pensum = BigDecimal.valueOf(80);
				final BigDecimal pauschale = BigDecimal.valueOf(500);
				final BetreuungspensumContainer betreuungspensum1 = createBetreuungspensum(pensum, AUG_1, AUG_15);
				final BetreuungspensumContainer betreuungspensum2 = createBetreuungspensum(pensum, OCT_1, OCT_16);
				Betreuung betreuung = setupBetreuung();
				betreuungspensum1.getBetreuungspensumJA().setEingewoehnungPauschale(createEinewoehnungPauschale(pauschale, AUG_1, AUG_15));
				betreuungspensum2.getBetreuungspensumJA().setEingewoehnungPauschale(createEinewoehnungPauschale(pauschale, OCT_1, OCT_16));
				betreuung.getBetreuungspensumContainers().add(betreuungspensum1);
				betreuung.getBetreuungspensumContainers().add(betreuungspensum2);

				List<BetreuungspensumAbweichung> abweichungen = betreuung.fillAbweichungen(BigDecimal.ONE);
				final BetreuungspensumAbweichung abweichungAug = abweichungen.get(0);
				final BetreuungspensumAbweichung abweichungOct = abweichungen.get(2);
				final EingewoehnungPauschale eingewoehnungPauschaleAug = abweichungAug.getVertraglicheEingewoehnungPauschale();
				final EingewoehnungPauschale eingewoehnungPauschaleOct = abweichungOct.getVertraglicheEingewoehnungPauschale();

				assertThat(eingewoehnungPauschaleAug, notNullValue());
				assertThat(eingewoehnungPauschaleAug.getPauschale(), comparesEqualTo(pauschale));
				assertThat(eingewoehnungPauschaleAug.getGueltigkeit().getGueltigAb(), is(AUG_1));
				assertThat(eingewoehnungPauschaleAug.getGueltigkeit().getGueltigBis(), is(AUG_15));
				assertThat(eingewoehnungPauschaleOct, notNullValue());
				assertThat(eingewoehnungPauschaleOct.getPauschale(), comparesEqualTo(pauschale));
				assertThat(eingewoehnungPauschaleOct.getGueltigkeit().getGueltigAb(), is(OCT_1));
				assertThat(eingewoehnungPauschaleOct.getGueltigkeit().getGueltigBis(), is(OCT_16));
			}

			private EingewoehnungPauschale createEinewoehnungPauschale(BigDecimal pauschale, LocalDate von, LocalDate bis) {
				EingewoehnungPauschale eingewoehnungPauschale = new EingewoehnungPauschale();
				eingewoehnungPauschale.setPauschale(pauschale);
				eingewoehnungPauschale.setGueltigkeit(new DateRange(von, bis));
				return eingewoehnungPauschale;
			}
		}
	}

	@Nonnull
	private static Betreuung setupBetreuung() {
		final Betreuung betreuung = new Betreuung();
		betreuung.setKind(setupKind());
		betreuung.setInstitutionStammdaten(setupInstitutionStammdaten());
		return betreuung;

	}

	private static InstitutionStammdaten setupInstitutionStammdaten() {
		InstitutionStammdaten institutionStammdaten = new InstitutionStammdaten();
		institutionStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);

		return institutionStammdaten;
	}

	private static BetreuungspensumContainer createBetreuungspensum(BigDecimal pensum, LocalDate von, LocalDate bis) {
		Betreuungspensum betreuungspensum = new Betreuungspensum(new DateRange(von, bis));
		betreuungspensum.setPensum(pensum);

		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);

		return betreuungspensumContainer;
	}

	private static KindContainer setupKind() {
		KindContainer kindContainer = new KindContainer();
		Gesuch gesuch = new Gesuch();
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(new DateRange(AUG_1, JUL_31));

		gesuch.setGesuchsperiode(gesuchsperiode);
		kindContainer.setGesuch(gesuch);

		return kindContainer;
	}
}
