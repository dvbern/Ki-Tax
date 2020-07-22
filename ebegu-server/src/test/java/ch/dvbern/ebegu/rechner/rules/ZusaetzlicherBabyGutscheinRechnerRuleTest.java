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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.BGRechnerParameterGemeindeDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

public class ZusaetzlicherBabyGutscheinRechnerRuleTest {

	private static final BigDecimal babyGutscheinKita = MathUtil.DEFAULT.from(50);
	private static final BigDecimal babyGutscheinTfo = MathUtil.DEFAULT.from(4.54);

	private ZusaetzlicherBabyGutscheinRechnerRule rule = new ZusaetzlicherBabyGutscheinRechnerRule(Locale.GERMAN);
	private BGRechnerParameterDTO londonDTO = new BGRechnerParameterDTO();
	private BGRechnerParameterDTO parisDTO = new BGRechnerParameterDTO();

	@Before
	public void init() {
		BGRechnerParameterGemeindeDTO londonGemeindeDTO = new BGRechnerParameterGemeindeDTO();
		londonGemeindeDTO.setGemeindeZusaetzlicherBabyGutscheinEnabled(false);
		this.londonDTO.setGemeindeParameter(londonGemeindeDTO);

		BGRechnerParameterGemeindeDTO parisGemeindeDTO = new BGRechnerParameterGemeindeDTO();
		parisGemeindeDTO.setGemeindeZusaetzlicherBabyGutscheinEnabled(true);
		parisGemeindeDTO.setGemeindeZusaetzlicherBabyGutscheinBetragKita(babyGutscheinKita);
		parisGemeindeDTO.setGemeindeZusaetzlicherBabyGutscheinBetragTfo(babyGutscheinTfo);
		parisDTO.setGemeindeParameter(parisGemeindeDTO);
	}

	@Test
	public void isConfigueredForGemeinde() {
		Assert.assertFalse(rule.isConfigueredForGemeinde(londonDTO));
		Assert.assertTrue(rule.isConfigueredForGemeinde(parisDTO));
	}

	@Test
	public void isRelevantForVerfuegungSozialhilfe() {
		Assert.assertFalse(rule.isRelevantForVerfuegung(prepareInput(false, true, KITA), londonDTO));
		Assert.assertFalse(rule.isRelevantForVerfuegung(prepareInput(true, true, KITA), parisDTO));
		Assert.assertTrue(rule.isRelevantForVerfuegung(prepareInput(false, true, KITA), parisDTO));
	}

	@Test
	public void isRelevantForVerfuegungUngueltigesAngebot() {
		Assert.assertFalse(rule.isRelevantForVerfuegung(prepareInput(false, true, TAGESSCHULE), parisDTO));
	}

	@Test
	public void isRelevantForVerfuegungKindalter() {
		Assert.assertFalse(rule.isRelevantForVerfuegung(prepareInput(false, false, KITA), parisDTO));
		Assert.assertTrue(rule.isRelevantForVerfuegung(prepareInput(false, true, KITA), parisDTO));
	}

	@Test
	public void prepareParameter() {
		RechnerRuleParameterDTO result = new RechnerRuleParameterDTO();
		// London: Nichts gesetzt
		rule.prepareParameter(prepareInput(false, true, KITA), londonDTO, result);
		Assert.assertEquals(BigDecimal.ZERO, result.getZusaetzlicherBabyGutscheinBetrag());
		// Paris: 30
		rule.prepareParameter(prepareInput(false, true, KITA), parisDTO, result);
		Assert.assertEquals(babyGutscheinKita, result.getZusaetzlicherBabyGutscheinBetrag());
	}

	private BGCalculationInput prepareInput(@Nonnull Boolean hasSozialhilfe, boolean isBaby, @Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setSozialhilfeempfaenger(hasSozialhilfe);
		input.setBetreuungsangebotTyp(betreuungsangebotTyp);
		input.setAnspruchspensumProzent(100);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(100));
		input.setBetreuungInGemeinde(true);
		input.setBabyTarif(isBaby);
		return input;
	}
}
