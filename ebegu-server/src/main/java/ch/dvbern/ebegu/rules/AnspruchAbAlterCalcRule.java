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

package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

public class AnspruchAbAlterCalcRule extends AbstractCalcRule {

	private final int requiredAgeInMonths;

	protected AnspruchAbAlterCalcRule(
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale,
			int requiredAgeInMonths) {
		super(RuleKey.ANSPRUCH_AB_X_MONATEN, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
		this.requiredAgeInMonths = requiredAgeInMonths;
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		if (inputData.isRequiredAgeForAnspruchNotReached()) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.addBemerkung(MsgKey.ANSPRUCH_AB_ALTER_NICHT_ERFUELLT, getLocale(), requiredAgeInMonths);
		}
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

}
