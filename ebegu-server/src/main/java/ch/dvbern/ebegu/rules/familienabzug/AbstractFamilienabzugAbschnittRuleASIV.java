/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.familienabzug;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;

public abstract class AbstractFamilienabzugAbschnittRuleASIV extends AbstractFamilienabzugAbschnittRule {
	private final BigDecimal pauschalabzugProPersonFamiliengroesse3;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse4;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse5;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse6;


	public AbstractFamilienabzugAbschnittRuleASIV(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap,
		DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(einstellungMap, validityPeriod, locale);
		this.pauschalabzugProPersonFamiliengroesse3 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3).getValueAsBigDecimal();
		this.pauschalabzugProPersonFamiliengroesse4 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4).getValueAsBigDecimal();
		this.pauschalabzugProPersonFamiliengroesse5 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5).getValueAsBigDecimal();
		this.pauschalabzugProPersonFamiliengroesse6 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6).getValueAsBigDecimal();
	}

	/**
	 * Berechnete Familiengrösse (halber Abzug berücksichtigen) multipliziert mit dem ermittelten Personen-Haushalt-Pauschalabzug
	 * (Anzahl Personen in Familie)
	 *
	 * @return abzug aufgrund Familiengrösse
	 */
	@Override
	public BigDecimal calculateAbzugAufgrundFamiliengroesse(double famGrBeruecksichtigungAbzug, int famGrAnzahlPersonen) {

		BigDecimal abzugFromServer = BigDecimal.ZERO;
		// Unter 3 Personen gibt es keinen Abzug!
		if (famGrAnzahlPersonen == 3) {
			abzugFromServer = pauschalabzugProPersonFamiliengroesse3;
		} else if (famGrAnzahlPersonen == 4) {
			abzugFromServer = pauschalabzugProPersonFamiliengroesse4;
		} else if (famGrAnzahlPersonen == 5) {
			abzugFromServer = pauschalabzugProPersonFamiliengroesse5;
		} else if (famGrAnzahlPersonen > 5) {
			abzugFromServer = pauschalabzugProPersonFamiliengroesse6;
		}

		// Ein Bigdecimal darf nicht aus einem double erzeugt werden, da das Ergebnis nicht genau die gegebene Nummer waere
		// deswegen muss man hier familiengroesse als String uebergeben. Sonst bekommen wir PMD rule AvoidDecimalLiteralsInBigDecimalConstructor
		// Wir runden die Zahl ausserdem zu einer Ganzzahl weil wir fuer das Massgebende einkommen mit Ganzzahlen rechnen
		return MathUtil.GANZZAHL.from(new BigDecimal(String.valueOf(famGrBeruecksichtigungAbzug)).multiply(abzugFromServer));
	}
}
