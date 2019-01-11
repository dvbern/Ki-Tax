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

/**
 * Regel für den ausserordentlichen Anspruch. Sie beachtet:
 * Ausserordentliches Pensum übersteuert den Anspruch, der aus anderen Reglen berechnet wurde, AUSSER dieser wäre
 * höher. Diese Regel kann also den Anspruch nur hinaufsetzen, nie hinunter.
 */
public class AusserordentlicherAnspruchCalcRule extends AbstractCalcRule {

	public AusserordentlicherAnspruchCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.AUSSERORDENTLICHER_ANSPRUCH, RuleType.GRUNDREGEL_CALC, validityPeriod, locale);
	}

	@Override
	protected void executeRule(
		@Nonnull Betreuung betreuung,
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		if (betreuung.getBetreuungsangebotTyp() != null && betreuung.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			int ausserordentlicherAnspruch = verfuegungZeitabschnitt.getAusserordentlicherAnspruch();
			int pensumAnspruch = verfuegungZeitabschnitt.getAnspruchberechtigtesPensum();
			// Es wird der grössere der beiden Werte genommen!
			if (ausserordentlicherAnspruch > pensumAnspruch) {
				verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(ausserordentlicherAnspruch);
				verfuegungZeitabschnitt.addBemerkung(
					RuleKey.AUSSERORDENTLICHER_ANSPRUCH,
					MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG,
					getLocale());
			}
		}
	}
}
