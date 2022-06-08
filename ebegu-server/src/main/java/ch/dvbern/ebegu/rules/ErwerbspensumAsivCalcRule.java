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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.types.DateRange;

/**
 * Berechnet die hoehe des ErwerbspensumRule eines bestimmten Erwerbspensums
 * Diese Rule muss immer am Anfang kommen, d.h. sie setzt den initialen Anspruch
 * Die weiteren Rules müssen diesen Wert gegebenenfalls korrigieren.
 * ACHTUNG! Diese Regel gilt nur fuer Angebote vom Typ isAngebotJugendamtKleinkind
 * Verweis 16.9.2
 */
public class ErwerbspensumAsivCalcRule extends ErwerbspensumCalcRule {

	public ErwerbspensumAsivCalcRule(
		@Nonnull DateRange validityPeriod,
		int minErwerbspensumNichtEingeschult,
		int minErwerbspensumEingeschult,
		int paramMinDauerKonkubinat,
		@Nonnull Locale locale
	) {
		super(RuleValidity.ASIV, validityPeriod, minErwerbspensumNichtEingeschult, minErwerbspensumEingeschult, paramMinDauerKonkubinat, locale);
	}
}
