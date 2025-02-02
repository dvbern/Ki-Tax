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

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.AusserordentlicherAnspruchTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static ch.dvbern.ebegu.enums.EinstellungKey.AUSSERORDENTLICHER_ANSPRUCH_RULE;

public abstract class AbstractAusserordentlicherAnspruchCalcRule extends AbstractCalcRule {

	protected AbstractAusserordentlicherAnspruchCalcRule(
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale) {
		super(RuleKey.AUSSERORDENTLICHER_ANSPRUCH, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
	}
	@Override
	abstract void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData);

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Override
	public abstract boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap);

	protected AusserordentlicherAnspruchTyp getAusserordentlicherAnspruchTypeFromEinstellungen(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap
	) {
		Einstellung ausserOrdentlicherAnspruchRuleTyp = einstellungMap.get(AUSSERORDENTLICHER_ANSPRUCH_RULE);
		Objects.requireNonNull(ausserOrdentlicherAnspruchRuleTyp,"Parameter AUSSERORDENTLICHER_ANSPRUCH_RULE muss gesetzt sein");
		return AusserordentlicherAnspruchTyp.valueOf(ausserOrdentlicherAnspruchRuleTyp.getValue());
	}
}
