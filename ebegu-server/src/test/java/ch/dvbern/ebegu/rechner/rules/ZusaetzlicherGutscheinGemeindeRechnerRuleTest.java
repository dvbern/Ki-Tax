/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner.rules;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.gemeindekonfiguration.GemeindeZusaetzlicherGutscheinTyp;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.BGRechnerParameterGemeindeDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.MathUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.ebegu.enums.EinschulungTyp.KLASSE1;
import static ch.dvbern.ebegu.enums.EinschulungTyp.VORSCHULALTER;
import static ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp.TAGESSCHULE;
import static org.hamcrest.MatcherAssert.assertThat;

class ZusaetzlicherGutscheinGemeindeRechnerRuleTest {

	private ZusaetzlicherGutscheinGemeindeRechnerRule rule = new ZusaetzlicherGutscheinGemeindeRechnerRule(Locale.GERMAN);
	private BGRechnerParameterDTO londonDTO = new BGRechnerParameterDTO();
	private BGRechnerParameterDTO parisDTO = new BGRechnerParameterDTO();

	@BeforeEach
	void init() {
		BGRechnerParameterGemeindeDTO londonGemeindeDTO = new BGRechnerParameterGemeindeDTO();
		londonGemeindeDTO.setGemeindeZusaetzlicherGutscheinEnabled(false);
		londonGemeindeDTO.setGemeindeZusaetzlicherGutscheinTyp(GemeindeZusaetzlicherGutscheinTyp.PAUSCHAL);
		this.londonDTO.setGemeindeParameter(londonGemeindeDTO);

		BGRechnerParameterGemeindeDTO parisGemeindeDTO = new BGRechnerParameterGemeindeDTO();
		parisGemeindeDTO.setGemeindeZusaetzlicherGutscheinEnabled(true);
		parisGemeindeDTO.setGemeindeZusaetzlicherGutscheinBetragKita(MathUtil.DEFAULT.from(30));
		parisGemeindeDTO.setGemeindeZusaetzlicherGutscheinBetragTfo(MathUtil.DEFAULT.from(0.3));
		parisGemeindeDTO.setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita(VORSCHULALTER);
		parisGemeindeDTO.setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo(VORSCHULALTER);
		parisGemeindeDTO.setGemeindeZusaetzlicherGutscheinTyp(GemeindeZusaetzlicherGutscheinTyp.PAUSCHAL);
		parisDTO.setGemeindeParameter(parisGemeindeDTO);
	}

	@Test
	void isConfigueredForGemeinde() {
		Assertions.assertFalse(rule.isConfigueredForGemeinde(londonDTO));
		Assertions.assertTrue(rule.isConfigueredForGemeinde(parisDTO));
	}

	@Test
	void isRelevantForVerfuegung() {
		Assertions.assertFalse(rule.isRelevantForVerfuegung(prepareInput(KLASSE1, BetreuungsangebotTyp.KITA), parisDTO));
		Assertions.assertTrue(rule.isRelevantForVerfuegung(prepareInput(VORSCHULALTER, BetreuungsangebotTyp.KITA), parisDTO));
	}

	@Test
	void isRelevantForVerfuegungUngueltigesAngebot() {
		Assertions.assertFalse(rule.isRelevantForVerfuegung(prepareInput(VORSCHULALTER, TAGESSCHULE), parisDTO));
	}

	@Test
	void prepareParameter() {
		RechnerRuleParameterDTO result = new RechnerRuleParameterDTO();
		// London: Nichts gesetzt
		rule.prepareParameter(prepareInput(VORSCHULALTER, BetreuungsangebotTyp.KITA), londonDTO, result);
		Assertions.assertEquals(BigDecimal.ZERO, result.getZusaetzlicherGutscheinGemeindeBetrag());
		// Paris: 30
		rule.prepareParameter(prepareInput(VORSCHULALTER, BetreuungsangebotTyp.KITA), parisDTO, result);
		Assertions.assertEquals(MathUtil.DEFAULT.from(30), result.getZusaetzlicherGutscheinGemeindeBetrag());
	}

	private BGCalculationInput prepareInput(
		@Nonnull EinschulungTyp einschulungTyp,
		@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setEinschulungTyp(einschulungTyp);
		input.setBetreuungsangebotTyp(betreuungsangebotTyp);
		input.setAnspruchspensumProzent(100);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(100));
		input.setBetreuungInGemeinde(true);
		return input;
	}

	@ParameterizedTest
	@MethodSource("zuschlagRechnerSource")
	void getZuschlagRechnerMustCreateCorrectRechner(
		GemeindeZusaetzlicherGutscheinTyp zusaetzlicherGutscheinTyp,
		Class<StaedtischerZuschlagRechner> rechnerClass) {
		assertThat(rule.getZuschlagRechner(zusaetzlicherGutscheinTyp).getClass(), Matchers.is(rechnerClass));
	}

	public static Stream<Arguments> zuschlagRechnerSource() {
		return Stream.of(
			Arguments.of(GemeindeZusaetzlicherGutscheinTyp.PAUSCHAL, StaedtischerZuschlagPauschalRechner.class),
			Arguments.of(GemeindeZusaetzlicherGutscheinTyp.LINEAR, StaedtischerZuschlagLinearRechner.class)
		);
	}

	@ParameterizedTest
	@MethodSource("msgKeyPauschalSource")
	void getMessageKeyPauschalMustReturnCorrectKey(BetreuungsangebotTyp betreuungsangebotTyp, MsgKey rechnerClass) {
		assertThat(rule.getZuschlagMessageKeyForPauschal(betreuungsangebotTyp), Matchers.is(rechnerClass));
	}

	public static Stream<Arguments> msgKeyPauschalSource() {
		return Stream.of(
			Arguments.of(BetreuungsangebotTyp.KITA, MsgKey.ZUSATZGUTSCHEIN_PAUSCHAL_JA_KITA),
			Arguments.of(BetreuungsangebotTyp.TAGESFAMILIEN, MsgKey.ZUSATZGUTSCHEIN_PAUSCHAL_JA_TFO)
			);
	}

	@ParameterizedTest
	@MethodSource("msgKeyLinearSource")
	void getMessageKeyLinearMustReturnCorrectKey(BigDecimal staedtischerZuschlag, MsgKey rechnerClass) {
		assertThat(rule.getZuschlagMessageKeyForLinear(staedtischerZuschlag), Matchers.is(rechnerClass));
	}

	public static Stream<Arguments> msgKeyLinearSource() {
		return Stream.of(
			Arguments.of(
				BigDecimal.ONE,
				MsgKey.ZUSATZGUTSCHEIN_LINEAR_JA),
			Arguments.of(
				BigDecimal.ZERO,
				MsgKey.ZUSATZGUTSCHEIN_LINEAR_NEIN)
		);
	}
}
