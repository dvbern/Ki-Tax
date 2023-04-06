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
 * Regel für Wohnsitz in Bern (Zuzug und Wegzug):
 * - Durch Adresse definiert
 * - Anspruch vom ersten Tag des Zuzugs
 * - Anspruch bis 2 Monate nach Wegzug, auf Ende Monat
 * Verweis 16.8 Der zivilrechtliche Wohnsitz
 */
public class WohnsitzCalcRule extends AbstractCalcRule {

	public WohnsitzCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.WOHNSITZ, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Override
	protected void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		if (areNotInBern(inputData)) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.addBemerkung(
				MsgKey.WOHNSITZ_MSG,
				getLocale(),
				platz.extractGesuch().getDossier().getGemeinde().getName());
		}
	}

	/**
	 * Nur GS 1 ist relevant. GS 2 muss per Definition bei GS 1 wohnen
	 */
	private boolean areNotInBern(@Nonnull BGCalculationInput inputData) {
		return inputData.isWohnsitzNichtInGemeindeGS1();
	}
}
