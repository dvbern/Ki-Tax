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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static ch.dvbern.ebegu.enums.EinstellungKey.KINDERABZUG_TYP;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;


/**
 * Umsetzung der ASIV Revision
 * <p>
 * 2. Immer aktuelle Familiengrösse
 * <p>
 * Gem. neuer ASIV Verordnung müssen die Kinder für die Berechnung der Familiengrösse ab dem Beginn den Monats NACH dem
 * Ereigniseintritt (e.g. Geburt) berücksichtigt werden. Dasselbe gilt bei der Aenderung des Zivilstands. Bei einer Mutation
 * der Familiensituation ist das Datum "Aendern per" relevant.
 */
@SuppressWarnings("MethodParameterNamingConvention")
public class FamilienabzugAbschnittRuleBern extends AbstractFamilienabzugAbschnittRuleASIV {

	private static final Logger LOG = LoggerFactory.getLogger(FamilienabzugAbschnittRuleBern.class);


	public FamilienabzugAbschnittRuleBern(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap,
		DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(einstellungMap, validityPeriod, locale);
	}


	protected Map.Entry<Double, Integer> addAbzugFromKinder(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate stichtag,
		@Nonnull Double famGrBeruecksichtigungAbzug,
		int famGrAnzahlPersonen
	) {
		LocalDate dateToCompare = getRelevantDateForKinder(gesuch.getGesuchsperiode(), stichtag);

		// Ermitteln, ob der KinderabzugErstesHalbjahr oder KinderabzugZweitesHalbjahr zum Zug kommen soll
		boolean isErstesHalbjahr = gesuch.getGesuchsperiode().getBasisJahrPlus1() == stichtag.getYear();
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			Kind kind = kindContainer.getKindJA();
			if (kind != null && (dateToCompare == null || kind.getGeburtsdatum().isBefore(dateToCompare))) {
				Kinderabzug kinderabzug =
					isErstesHalbjahr ? kind.getKinderabzugErstesHalbjahr() : kind.getKinderabzugZweitesHalbjahr();
				if (kinderabzug == Kinderabzug.HALBER_ABZUG) {
					famGrBeruecksichtigungAbzug += 0.5;
					famGrAnzahlPersonen++;
				} else if (kinderabzug == Kinderabzug.GANZER_ABZUG) {
					famGrBeruecksichtigungAbzug += 1;
					famGrAnzahlPersonen++;
				}
			}
		}

		return new AbstractMap.SimpleEntry(famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
	}
}
