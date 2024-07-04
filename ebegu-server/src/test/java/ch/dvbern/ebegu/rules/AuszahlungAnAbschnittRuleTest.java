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

import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AuszahlungAnAbschnittRuleTest {

	AuszahlungAnAbschnittRule ruleToTest = new AuszahlungAnAbschnittRule(
		Constants.DEFAULT_GUELTIGKEIT,
		Constants.DEFAULT_LOCALE);

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void shouldBeSameAsBetreuungAuszahlungAnEltern(boolean auszahlungAnEltern) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		betreuung.setAuszahlungAnEltern(auszahlungAnEltern);

		List<VerfuegungZeitabschnitt> zeitabschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);

		assertThat(zeitabschnitte.get(0).getBgCalculationInputAsiv().isAuszahlungAnEltern(), is(auszahlungAnEltern));
		assertThat(zeitabschnitte.get(0).getBgCalculationInputGemeinde().isAuszahlungAnEltern(), is(auszahlungAnEltern));
	}

	@ParameterizedTest
	@MethodSource("provideGueltigkeiten")
	void shouldHaveGueltigkeitEntireGPNoMatterBetreuungspensum(List<DateRange> betreuungspensenGueltigkeiten) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		betreuung.getBetreuungspensumContainers().clear();
		betreuungspensenGueltigkeiten.forEach(gueltigkeit -> {
			BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
			betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum(gueltigkeit));
			betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
		});

		List<VerfuegungZeitabschnitt> zeitabschnitte = ruleToTest.createVerfuegungsZeitabschnitte(betreuung);

		assertThat(zeitabschnitte.size(), is(1));
		assertThat(zeitabschnitte.get(0).getGueltigkeit().getGueltigAb(), is(Constants.GESUCHSPERIODE_17_18_AB));
		assertThat(zeitabschnitte.get(0).getGueltigkeit().getGueltigBis(), is(Constants.GESUCHSPERIODE_17_18_BIS));

	}

	private static Stream<Arguments> provideGueltigkeiten() {
		return Stream.of(
			Arguments.of(List.of(new DateRange(Constants.GESUCHSPERIODE_17_18_AB, Constants.GESUCHSPERIODE_17_18_BIS))),
			Arguments.of(List.of(new DateRange(
				Constants.GESUCHSPERIODE_17_18_AB,
				Constants.GESUCHSPERIODE_17_18_AB.plusMonths(2)))),
			Arguments.of(List.of(new DateRange(
				Constants.GESUCHSPERIODE_17_18_AB.plusMonths(2),
				Constants.GESUCHSPERIODE_17_18_BIS))),
			Arguments.of(List.of(
				new DateRange(
					Constants.GESUCHSPERIODE_17_18_AB,
					Constants.GESUCHSPERIODE_17_18_AB.plusMonths(2)),
				new DateRange(Constants.GESUCHSPERIODE_17_18_AB.plusMonths(4), Constants.GESUCHSPERIODE_17_18_BIS)))
		);
	}
}
