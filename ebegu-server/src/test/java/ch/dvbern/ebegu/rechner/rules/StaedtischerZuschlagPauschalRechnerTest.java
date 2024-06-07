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

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.BGRechnerParameterGemeindeDTO;
import ch.dvbern.ebegu.rules.RuleValidity;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StaedtischerZuschlagPauschalRechnerTest {

	@Test
	void mustReturnCorrectPauschalZuschlagForKita() {
		// given

		var testee = new StaedtischerZuschlagPauschalRechner();
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);

		BGRechnerParameterDTO rechnerParameterDTO = new BGRechnerParameterDTO();
		BGRechnerParameterGemeindeDTO gemeindeParameter = new BGRechnerParameterGemeindeDTO();
		BigDecimal zusaetzlicherGutscheinBetragKita = new BigDecimal("23.5");
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinBetragKita(zusaetzlicherGutscheinBetragKita);
		rechnerParameterDTO.setGemeindeParameter(gemeindeParameter);

		// when
		BigDecimal zuschlagKita =
				testee.calculate(input, rechnerParameterDTO);

		// verify
		assertThat(zuschlagKita, is(zusaetzlicherGutscheinBetragKita));
	}

	@Test
	void mustReturnCorrectPauschalZuschlagForTfo() {
		// given
		var testee = new StaedtischerZuschlagPauschalRechner();
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESFAMILIEN);

		BGRechnerParameterDTO rechnerParameterDTO = new BGRechnerParameterDTO();
		BGRechnerParameterGemeindeDTO gemeindeParameter = new BGRechnerParameterGemeindeDTO();
		BigDecimal zusaetzlicherGutscheinBetragTfo = new BigDecimal("23.5");
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinBetragTfo(zusaetzlicherGutscheinBetragTfo);
		rechnerParameterDTO.setGemeindeParameter(gemeindeParameter);

		// when
		BigDecimal zuschlagKita =
				testee.calculate(input, rechnerParameterDTO);

		// verify
		assertThat(zuschlagKita, is(zusaetzlicherGutscheinBetragTfo));
	}

}
