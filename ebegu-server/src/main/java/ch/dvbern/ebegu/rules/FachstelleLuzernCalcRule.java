/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.FachstellenTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

public class FachstelleLuzernCalcRule extends AbstractFachstellenCalcRule {

	public FachstelleLuzernCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.FACHSTELLE, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		int pensum = inputData.getFachstellenpensum();
		Betreuung betreuung = (Betreuung) platz;

		if(pensum > 0) {
			int beteruungspensumIntValue = MathUtil.GANZZAHL.from(pensum).intValue();
			PensumFachstelle pensumFachstelle = findPensumFachstelleForGueltigkeit(platz.getKind().getKindJA(), inputData.getParent().getGueltigkeit());
			inputData.setAnspruchspensumProzent(beteruungspensumIntValue);
			inputData.addBemerkung(
				MsgKey.FACHSTELLE_MSG,
				getLocale(),
				getIndikationName(pensumFachstelle, betreuung),
				getFachstelleName(pensumFachstelle.getFachstelle())
			);
		}
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		return getFachstellenTypFromEinstellungen(einstellungMap) == FachstellenTyp.LUZERN;
	}
}
