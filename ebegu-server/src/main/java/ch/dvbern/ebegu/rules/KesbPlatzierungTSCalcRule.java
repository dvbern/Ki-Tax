/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

import static ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp.TAGESSCHULE;

/**
 * Bemerkung: Bei einer KESB-Platzierung wird der Max-Tarif verwendet. Wir setzen hier das entsprechende Flag für den Rechner
 */
public class KesbPlatzierungTSCalcRule extends AbstractCalcRule {

	public KesbPlatzierungTSCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.KESB_PLATZIERUNG, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return List.of(TAGESSCHULE);
	}

	@Override
	protected void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		AnmeldungTagesschule betreuung = (AnmeldungTagesschule) platz;
		if (betreuung.getBelegungTagesschule() != null && !betreuung.getBelegungTagesschule().isKeineKesbPlatzierung()) {
			inputData.setKesbPlatzierung(true);
		}
	}
}
