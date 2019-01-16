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

import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

public class GutscheineStartdatumCalcRule extends AbstractCalcRule {

	public GutscheineStartdatumCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BEGU_STARTDATUM, RuleType.REDUKTIONSREGEL, validityPeriod, locale);
	}

	@Override
	protected void executeRule(
		@Nonnull Betreuung betreuung,
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {

		if (!verfuegungZeitabschnitt.isAbschnittLiegtNachBEGUStartdatum()) {
			verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(0);
			verfuegungZeitabschnitt.addBemerkung(
				RuleKey.BEGU_STARTDATUM,
				MsgKey.BETREUUNG_VOR_BEGU_START,
				getLocale(),
				betreuung.extractGesuch().getDossier().getGemeinde().getName());
		}
	}
}
