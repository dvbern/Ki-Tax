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

import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import static java.util.Objects.requireNonNull;

/**
 * Als allerletzte Reduktionsregel läuft eine Regel die das Feld "AnspruchberechtigtesPensum"
 * mit dem Feld "AnspruchspensumRest" vergleicht. Wenn letzteres -1 ist gilt der Wert im Feld "AnspruchsberechtigtesPensum,
 * ansonsten wir das Minimum der beiden Felder in das Feld "AnspruchberechtigtesPensum" gesetzt wenn es sich um eine
 * Kita/Kleinkinder-Betreuung handelt
 * Dadurch wird das Anspruchspensum limitiert auf den Maximal moeglichen Restanspruch
 */
public class RestanspruchLimitCalcRule extends AbstractCalcRule {

	public RestanspruchLimitCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.RESTANSPRUCH, RuleType.REDUKTIONSREGEL, validityPeriod, locale);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		// Fuer Kleinkinderangebote den Restanspruch bereucksichtigen
		requireNonNull(platz.getBetreuungsangebotTyp());
		if (platz.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			int anspruchberechtigtesPensum = verfuegungZeitabschnitt.getAnspruchberechtigtesPensum();
			int verfuegbarerRestanspruch = verfuegungZeitabschnitt.getBgCalculationInput().getAnspruchspensumRest();
			//wir muessen nur was machen wenn wir schon einen Restanspruch gesetzt haben
			if (verfuegbarerRestanspruch != -1 && verfuegbarerRestanspruch < anspruchberechtigtesPensum) {
				verfuegungZeitabschnitt.getBgCalculationInput().addBemerkung(
					RuleKey.RESTANSPRUCH,
					MsgKey.RESTANSPRUCH_MSG,
					getLocale(),
					anspruchberechtigtesPensum,
					verfuegbarerRestanspruch
				);
				verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(verfuegbarerRestanspruch);
			}
		}
	}
	// fuer Schulkinder wird nichts gemacht

}
