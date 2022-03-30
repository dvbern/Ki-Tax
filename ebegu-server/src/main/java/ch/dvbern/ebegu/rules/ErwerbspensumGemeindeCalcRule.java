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

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.types.DateRange;

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;

/**
 * Berechnet die hoehe des ErwerbspensumRule eines bestimmten Erwerbspensums
 * Diese Rule muss immer am Anfang kommen, d.h. sie setzt den initialen Anspruch
 * Die weiteren Rules müssen diesen Wert gegebenenfalls korrigieren.
 * ACHTUNG! Diese Regel gilt nur fuer Angebote vom Typ isAngebotJugendamtKleinkind
 * Verweis 16.9.2
 */
public class ErwerbspensumGemeindeCalcRule extends ErwerbspensumCalcRule {

	public ErwerbspensumGemeindeCalcRule(
		@Nonnull DateRange validityPeriod,
		int minErwerbspensumNichtEingeschult,
		int minErwerbspensumEingeschult,
		int paramMinDauerKonkubinat,
		@Nonnull Locale locale
	) {
		super(RuleValidity.GEMEINDE, validityPeriod, minErwerbspensumNichtEingeschult, minErwerbspensumEingeschult, paramMinDauerKonkubinat, locale);
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		// Die Regel muss beachtet werden, wenn die Minimalpensen der Gemeinde ueberschrieben wurden
		Einstellung minEWP_nichtEingeschultAsiv = einstellungMap.get(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		Objects.requireNonNull(minEWP_nichtEingeschultAsiv, "Parameter MIN_ERWERBSPENSUM_NICHT_EINGESCHULT muss gesetzt sein");
		Einstellung minEWP_nichtEingeschultGmde = einstellungMap.get(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		Objects.requireNonNull(minEWP_nichtEingeschultGmde, "Parameter MIN_ERWERBSPENSUM_NICHT_EINGESCHULT muss gesetzt sein");
		if (minEWP_nichtEingeschultAsiv.getValueAsInteger().compareTo(minEWP_nichtEingeschultGmde.getValueAsInteger()) != 0) {
			return true;
		}
		Einstellung minEWP_eingeschultAsiv = einstellungMap.get(MIN_ERWERBSPENSUM_EINGESCHULT);
		Einstellung minEWP_eingeschultGmde = einstellungMap.get(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT);
		Objects.requireNonNull(minEWP_eingeschultAsiv, "Parameter MIN_ERWERBSPENSUM_EINGESCHULT muss gesetzt sein");
		Objects.requireNonNull(minEWP_eingeschultGmde, "Parameter MIN_ERWERBSPENSUM_EINGESCHULT muss gesetzt sein");
		if (minEWP_eingeschultAsiv.getValueAsInteger().compareTo(minEWP_eingeschultGmde.getValueAsInteger()) != 0) {
			return true;
		}
		return false;
	}
}
