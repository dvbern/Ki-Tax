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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

public class GutscheineStartdatumCalcRule extends AbstractCalcRule {

	public GutscheineStartdatumCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BEGU_STARTDATUM, RuleType.REDUKTIONSREGEL, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return Arrays.asList(KITA, TAGESFAMILIEN);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		if (!verfuegungZeitabschnitt.getBgCalculationInputAsiv().isAbschnittLiegtNachBEGUStartdatum()) {
			verfuegungZeitabschnitt.getBgCalculationResultAsiv().setAnspruchspensumProzent(0);
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().addBemerkung(
				RuleKey.BEGU_STARTDATUM,
				MsgKey.BETREUUNG_VOR_BEGU_START,
				getLocale(),
				platz.extractGesuch().getDossier().getGemeinde().getName());
		}
	}
}
