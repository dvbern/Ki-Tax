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
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.BGRechnerParameterGemeindeDTO;
import ch.dvbern.ebegu.rules.RuleValidity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

class StaedtischerZuschlagLinearRechnerTest {

	@Test
	@DisplayName(
			"Falls Min und Max Einkommen auf Gemeindeebene nicht definiert sind, sollen die Mandatseinstellungen "
					+ "genommen werden")
	void mustUseMandantMinUndMaxMassgebendesEinkommen() {
		// given
		var testee = new StaedtischerZuschlagLinearRechner();
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESFAMILIEN);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(50000));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setAnspruchspensumProzent(80);
		input.setBetreuungspensumProzent(new BigDecimal("80"));

		BGRechnerParameterDTO rechnerParameterDTO = new BGRechnerParameterDTO();
		rechnerParameterDTO.setOeffnungstageTFO(BigDecimal.valueOf(240));
		rechnerParameterDTO.setOeffnungsstundenTFO(BigDecimal.valueOf(10));
		rechnerParameterDTO.setMinMassgebendesEinkommen(new BigDecimal("43000"));
		rechnerParameterDTO.setMaxMassgebendesEinkommen(new BigDecimal("140000"));

		BGRechnerParameterGemeindeDTO gemeindeParameter = new BGRechnerParameterGemeindeDTO();
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinLinearTfoMax(new BigDecimal("3.1"));

		rechnerParameterDTO.setGemeindeParameter(gemeindeParameter);

		// when
		BigDecimal zuschlag = testee.calculate(input, rechnerParameterDTO);

		// verify
		assertThat(zuschlag, is(closeTo(new BigDecimal("460.20"), new BigDecimal("0.05"))));
	}

	@ParameterizedTest(name = "Einkommen: {0}, Zuschlag: {1}")
	@MethodSource("calculationSource")
	@DisplayName("Linearer Zuschlag fuer TFO")
	void mustCalculateLinearerZuschlagTfo(BigDecimal massgebendesEinkommen, BigDecimal erwarteterZuschlag) {
		// given
		var testee = new StaedtischerZuschlagLinearRechner();
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESFAMILIEN);
		input.setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setAnspruchspensumProzent(80);
		input.setBetreuungspensumProzent(new BigDecimal("80"));

		BGRechnerParameterDTO rechnerParameterDTO = new BGRechnerParameterDTO();
		rechnerParameterDTO.setOeffnungstageTFO(BigDecimal.valueOf(240));
		rechnerParameterDTO.setOeffnungsstundenTFO(BigDecimal.valueOf(10));

		BGRechnerParameterGemeindeDTO gemeindeParameter = new BGRechnerParameterGemeindeDTO();
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen(new BigDecimal("43000"));
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen(new BigDecimal("140000"));
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinLinearTfoMax(new BigDecimal("3.1"));

		rechnerParameterDTO.setGemeindeParameter(gemeindeParameter);

		// when
		BigDecimal zuschlag = testee.calculate(input, rechnerParameterDTO);

		// verify
		assertThat(zuschlag, is(closeTo(erwarteterZuschlag, new BigDecimal("0.05"))));
	}

	@ParameterizedTest(name = "Einkommen: {0}, Zuschlag: {1}")
	@MethodSource("calculationSource")
	@DisplayName("Linearer Zuschlag fuer KITA")
	void mustCalculateLinearerZuschlagKita(BigDecimal massgebendesEinkommen, BigDecimal erwarteterZuschlag) {
		// given
		var testee = new StaedtischerZuschlagLinearRechner();
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		input.setMassgebendesEinkommenVorAbzugFamgr(massgebendesEinkommen);
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setAnspruchspensumProzent(80);
		input.setBetreuungspensumProzent(new BigDecimal("80"));

		BGRechnerParameterDTO rechnerParameterDTO = new BGRechnerParameterDTO();
		rechnerParameterDTO.setOeffnungstageKita(BigDecimal.valueOf(240));

		BGRechnerParameterGemeindeDTO gemeindeParameter = new BGRechnerParameterGemeindeDTO();
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen(new BigDecimal("43000"));
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen(new BigDecimal("140000"));
		gemeindeParameter.setGemeindeZusaetzlicherGutscheinLinearKitaMax(new BigDecimal("31"));

		rechnerParameterDTO.setGemeindeParameter(gemeindeParameter);

		// when
		BigDecimal zuschlag = testee.calculate(input, rechnerParameterDTO);

		// verify
		assertThat(zuschlag, is(closeTo(erwarteterZuschlag, new BigDecimal("0.05"))));
	}

	public static Stream<Arguments> calculationSource() {
		return Stream.of(
				// Einkommen zwischen Minimum und Maximum
				Arguments.of(new BigDecimal(50000), new BigDecimal("460.20")),
				// Zuschlag darf bei Einkommen tiefer als Minimum nicht grösser werden als das Maximum
				Arguments.of(new BigDecimal("25000"), new BigDecimal("496.00")),
				// Zuschlag muss bei Einkommen nahe dem Maxmium sehr klein werden
				Arguments.of(new BigDecimal("139500"), new BigDecimal("2.55")),
				// Zuschlag muss bei Einkommen nahe dem Minimum nahe beim Maximalwert liegen
				Arguments.of(new BigDecimal("43500"), new BigDecimal("493.40"))
		);
	}
}