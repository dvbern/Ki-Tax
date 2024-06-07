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
 *
 */

package ch.dvbern.ebegu.rechner.rules;

import java.math.BigDecimal;
import java.util.stream.Stream;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.RuleValidity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StaedtischerZuschlagRechnerTest {

	private static final BigDecimal ZUSCHLAG_KITA = new BigDecimal("12.25");
	private static final BigDecimal ZUSCHLAG_TFO = new BigDecimal("2.25");

	public static Stream<Arguments> angebotsTypSource() {
		return Stream.of(
				Arguments.of(BetreuungsangebotTyp.KITA, ZUSCHLAG_KITA),
				Arguments.of(BetreuungsangebotTyp.TAGESFAMILIEN,ZUSCHLAG_TFO )
		);
	}

	static class DummyStaedtischerZuschlagRechner extends StaedtischerZuschlagRechner {
		@Override
		BigDecimal calculateForTfo(BGCalculationInput inputGemeinde, BGRechnerParameterDTO rechnerParameterDTO) {
			return ZUSCHLAG_TFO;
		}

		@Override
		BigDecimal calculateForKita(BGCalculationInput inputGemeinde, BGRechnerParameterDTO rechnerParameterDTO) {
			return ZUSCHLAG_KITA;
		}
	}

	@ParameterizedTest
	@MethodSource("angebotsTypSource")
	void mustThrowIfAngebotsTypNotKita(BetreuungsangebotTyp betreuungsangebotTyp, BigDecimal erwarteterZuschlag) {
		// given
		var testee = new DummyStaedtischerZuschlagRechner();
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setBetreuungsangebotTyp(betreuungsangebotTyp);
		BGRechnerParameterDTO rechnerParameterDTO = new BGRechnerParameterDTO();

		// when
		BigDecimal zuschlag = testee.calculate(input, rechnerParameterDTO);

		// verify
		assertThat(zuschlag, is(erwarteterZuschlag));
	}

}
