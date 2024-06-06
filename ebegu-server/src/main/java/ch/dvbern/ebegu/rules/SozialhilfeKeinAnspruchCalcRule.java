/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Wenn die Gemeinde die Einstellung GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER aktiviert ist,
 * wird das Anspruchspenum für Sozialhilfe Empfänger auf 0 gesetzt
 */
public class SozialhilfeKeinAnspruchCalcRule extends AbstractCalcRule {

	public SozialhilfeKeinAnspruchCalcRule(DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.SOZIALHILFE, RuleType.REDUKTIONSREGEL, RuleValidity.GEMEINDE, validityPeriod, locale);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();
		boolean sozialhilfeEmpfaenger = familiensituation != null && Boolean.TRUE.equals(familiensituation.getSozialhilfeBezueger());

		if (sozialhilfeEmpfaenger) {
			inputData.setAnspruchspensumProzent(0);
			inputData.addBemerkung(MsgKey.SOZIALHILFEEMPFAENGER_HABEN_KEINEN_ANSPRUCH, getLocale());
		}
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung einstellungKeinGutscheinFuerSozialhilfe
			= einstellungMap.get(EinstellungKey.GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER);
		return einstellungKeinGutscheinFuerSozialhilfe.getValueAsBoolean();
	}

}
