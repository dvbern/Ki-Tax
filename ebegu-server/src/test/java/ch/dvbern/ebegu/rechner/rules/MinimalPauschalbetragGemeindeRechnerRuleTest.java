/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
import ch.dvbern.ebegu.enums.EinschulungTyp;
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
import static ch.dvbern.ebegu.enums.EinschulungTyp.KLASSE1;
import static ch.dvbern.ebegu.enums.EinschulungTyp.VORSCHULALTER;

public class MinimalPauschalbetragGemeindeRechnerRuleTest {

	private MinimalPauschalbetragGemeindeRechnerRule rule = new MinimalPauschalbetragGemeindeRechnerRule(Locale.GERMAN);
	private BGRechnerParameterDTO unaktivRule = new BGRechnerParameterDTO();
	private BGRechnerParameterDTO aktivRule = new BGRechnerParameterDTO();

	@Before
	public void init() {
		BGRechnerParameterGemeindeDTO unaktivRuleGemeinde = new BGRechnerParameterGemeindeDTO();
		unaktivRuleGemeinde.setGemeindePauschalbetragEnabled(false);
		this.unaktivRule.setGemeindeParameter(unaktivRuleGemeinde);

		BGRechnerParameterGemeindeDTO aktivRuleGemeinde = new BGRechnerParameterGemeindeDTO();
		aktivRuleGemeinde.setGemeindePauschalbetragEnabled(true);
		aktivRuleGemeinde.setGemeindePauschalbetrag(BigDecimal.TEN);
		aktivRuleGemeinde.setGemeindePauschalbetragMassgebendenEinkommen(new BigDecimal(200000));
		aktivRule.setGemeindeParameter(aktivRuleGemeinde);
	}

	@Test
	public void isConfigueredForGemeinde() {
		Assert.assertFalse(rule.isConfigueredForGemeinde(unaktivRule));
		Assert.assertTrue(rule.isConfigueredForGemeinde(aktivRule));
	}

	@Test
	public void isRelevantForVerfuegung() {
		Assert.assertFalse(rule.isRelevantForVerfuegung(prepareInput(KITA, new BigDecimal(260000)), aktivRule));
		Assert.assertTrue(rule.isRelevantForVerfuegung(prepareInput(KITA, new BigDecimal(100000)), aktivRule));
	}

	@Test
	public void isRelevantForVerfuegungUngueltigesAngebot() {
		Assert.assertFalse(rule.isRelevantForVerfuegung(prepareInput(TAGESSCHULE, BigDecimal.ZERO), aktivRule));
	}

	@Test
	public void prepareParameter() {
		RechnerRuleParameterDTO result = new RechnerRuleParameterDTO();
		// Rule inaktiv: Nichts gesetzt
		rule.prepareParameter(prepareInput(KITA, BigDecimal.ZERO), unaktivRule, result);
		Assert.assertNull(result.getMinimalPauschalBetrag());
		// Rule Aktiv: 10
		rule.prepareParameter(prepareInput(KITA,BigDecimal.ZERO), aktivRule, result);
		Assert.assertEquals(BigDecimal.TEN, result.getMinimalPauschalBetrag());
	}

	private BGCalculationInput prepareInput(@Nonnull
		BetreuungsangebotTyp betreuungsangebotTyp, @Nonnull BigDecimal massgebendenEinkommen) {
		BGCalculationInput input = new BGCalculationInput(new VerfuegungZeitabschnitt(), RuleValidity.ASIV);
		input.setBetreuungsangebotTyp(betreuungsangebotTyp);
		input.setMassgebendesEinkommenVorAbzugFamgr(massgebendenEinkommen);
		input.setBetreuungInGemeinde(true);
		return input;
	}
}
