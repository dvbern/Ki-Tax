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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Als allerletzte Reduktionsregel läuft eine Regel die das Feld "AnspruchberechtigtesPensum"
 * mit dem Feld "AnspruchspensumRest" vergleicht. Wenn letzteres -1 ist gilt der Wert im Feld "AnspruchsberechtigtesPensum,
 * ansonsten wir das Minimum der beiden Felder in das Feld "AnspruchberechtigtesPensum" gesetzt wenn es sich um eine
 * Kita/Kleinkinder-Betreuung handelt
 * Dadurch wird das Anspruchspensum limitiert auf den Maximal moeglichen Restanspruch
 */
public class RestanspruchLimitCalcRule extends AbstractCalcRule {

	public RestanspruchLimitCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.RESTANSPRUCH, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		// Fuer Kleinkinderangebote den Restanspruch bereucksichtigen, fuer Schulkinder wird nichts gemacht
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	protected void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		int anspruchberechtigtesPensum = inputData.getAnspruchspensumProzent();
		int verfuegbarerRestanspruch = inputData.getAnspruchspensumRest();
		//wir muessen nur was machen wenn wir schon einen Restanspruch gesetzt haben
		if (verfuegbarerRestanspruch != -1 && verfuegbarerRestanspruch < anspruchberechtigtesPensum) {
			if (addVerfuegungsbemerkungRestanspruch(inputData)) {
				inputData.addBemerkung(
					MsgKey.RESTANSPRUCH_MSG,
					getLocale(),
					anspruchberechtigtesPensum,
					verfuegbarerRestanspruch);
			}
			inputData.setAnspruchspensumProzent(verfuegbarerRestanspruch);
		}
	}

	private boolean addVerfuegungsbemerkungRestanspruch(@Nonnull BGCalculationInput inputData) {
		// Die Restanspruchsbemerkung soll nur hinzugefuegt werden, wenn es die "relevante"
		// Berechnung fuer diesen Abschnitt ist
		if (inputData.getParent().isHasGemeindeSpezifischeBerechnung()) {
			return inputData.getRuleValidity() == RuleValidity.GEMEINDE;
		}
		return inputData.getRuleValidity() == RuleValidity.ASIV;
	}
}
