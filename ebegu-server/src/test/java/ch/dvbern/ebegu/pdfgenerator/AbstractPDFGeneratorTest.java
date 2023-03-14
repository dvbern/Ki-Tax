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

package ch.dvbern.ebegu.pdfgenerator;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinConfigurator;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.RuleParameterUtil;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractPDFGeneratorTest {

	protected BetreuungsgutscheinEvaluator evaluator;

	protected Gemeinde gemeindeOfEvaluator = TestDataUtil.createGemeindeLondon();
	protected Gesuchsperiode gesuchsperiodeOfEvaluator = TestDataUtil.createGesuchsperiode1718();

	@BeforeEach
	public void setUpCalcuator() {
		evaluator = createEvaluator(gesuchsperiodeOfEvaluator, gemeindeOfEvaluator);
	}

	public static BetreuungsgutscheinEvaluator createEvaluator(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde bern
	) {
		Map<EinstellungKey, Einstellung> einstellungen = EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(gesuchsperiode);
		RuleParameterUtil ruleParameterUtil = new RuleParameterUtil(einstellungen, TestDataUtil.geKitaxUebergangsloesungParameter());
		BetreuungsgutscheinConfigurator configurator = new BetreuungsgutscheinConfigurator();
		List<Rule> rules = configurator.configureRulesForMandant(bern, ruleParameterUtil, Constants.DEFAULT_LOCALE);
		return new BetreuungsgutscheinEvaluator(rules, einstellungen);
	}
}
