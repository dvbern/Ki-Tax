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

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

public class KindAnspruchCalcRule extends AbstractCalcRule {

	private final KinderabzugTyp kinderabzugTyp;

	protected KindAnspruchCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		KinderabzugTyp kinderabzugTyp) {
		super(RuleKey.KIND_ANSPRUCH, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
		this.kinderabzugTyp = kinderabzugTyp;
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		if (kinderabzugTyp.equals(KinderabzugTyp.SCHWYZ) &&
			(platz.getKind().getKindJA().getLebtKindAlternierend() == null ||
				platz.getKind().getKindJA().getLebtKindAlternierend() == false)) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.addBemerkung(MsgKey.KEIN_ANSPRUCH_NICHT_BEITRAGSBERECHTIGT, getLocale());
		}
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

}
