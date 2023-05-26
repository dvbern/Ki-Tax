/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.initalizer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;

/**
 * Hilfsklasse die nach der eigentlich Evaluation einer Betreuung angewendet wird um den Restanspruch zu uebernehmen fuer die
 * Berechnung der nachsten Betreuung.
 * Ermittelt des Restanspruch aus den übergebenen Zeitabschnitten und erstellt neue Abschnitte mit nur dieser Information
 * für die Berechnung der nächsten Betreuung. Diese werden als initiale Zeitabschnitte der nachsten Betreuung verwendet
 * Bei Angeboten fuer Schulkinder ist der Restanspruch nicht tangiert
 * Verweis 15.9.5
 *
 * <h1>Vorgehensskizze Restanspruchberechnung</h1>
 * <ul>
 * <li>Der Restanspruch ist bei der ersten Betreuung auf -1 gesetzt</li>
 * <li>Wir berechnen die Verfügung für diese erste Betreuung. Dabei wird in allen Regeln die den Anspruch benoetigen das Feld AnspruchberechtigtesPensum verwendet (nicht AnspruchspensumRest)</li>
 * <li> Als allerletzte Reduktionsregel läuft eine Regel die das Feld "AnspruchberechtigtesPensum" mit dem Feld<
 * "AnspruchspensumRest" vergleicht. Wenn letzteres -1 ist gilt der Wert im Feld "AnspruchsberechtigtesPensum, ansonsten wir das Minimum der beiden Felder in das Feld "AnspruchberechtigtesPensum" gesetzt. </li>
 * <li>Bevor die nächste Betreuung verfügt wird, berechnen wir den noch verfügbaren Restanspruch indem wir "AnspruchberechtigtesPensum" - "betreuungspensum" rechnen und das Resultat in das Feld "AnspruchspensumRest" schreiben</li>
 * </ul>
 * Die 2. Betreuung wird genau wie die erste durchgeführt. Nun wird allerdings die allerletzte Reduktionsregel den Anspruch reduzieren auf den gesetzten Restanspruch.
 */
public final class RestanspruchInitializerAR extends RestanspruchInitializer {

	public RestanspruchInitializerAR(boolean isDebug) {
		super(isDebug);
	}


	@Override
	protected void restanspruchUebernehmenVerfuegt(
			@Nonnull BGCalculationInput sourceZeitabschnittInput,
			@Nonnull BGCalculationResult sourceZeitabschnitt,
			@Nonnull BGCalculationInput targetZeitabschnitt
	) {
		var faktor = sourceZeitabschnittInput.getBgStundenFaktor();
		BigDecimal anspruchberechtigtesPensumInStunden = sourceZeitabschnitt.getAnspruchspensumZeiteinheit();
		BigDecimal betreuungspensumInStunden = sourceZeitabschnitt.getBetreuungspensumZeiteinheit();
		if (sourceZeitabschnitt.getAnspruchspensumRest() != null) {
			// Wir haben einen Restanspruch nach dem neuen System gespeichert
			final BigDecimal anspruchspensumRestInStunden = sourceZeitabschnitt.getAnspruchspensumRest().multiply(BigDecimal.valueOf(faktor));
			final int restanspruchNeu = calculateRestanspruchInStunden(betreuungspensumInStunden, anspruchberechtigtesPensumInStunden, anspruchspensumRestInStunden, faktor);
			targetZeitabschnitt.setAnspruchspensumRest(restanspruchNeu);
		} else {
			if (betreuungspensumInStunden.compareTo(BigDecimal.ZERO) == 0) {
				// In der Vorgaengerverfuegung bestand keine Betreuung mehr fuer diesen Abschnitt. Wir setzen
				// den Restanspruch auf -1 (= noch nicht berechnet)
				targetZeitabschnitt.setAnspruchspensumRest(-1);
			}
			//wenn nicht der ganze anspruch gebraucht wird gibt es einen rest, ansonsten ist rest 0
			else if (betreuungspensumInStunden.compareTo(anspruchberechtigtesPensumInStunden.add(BigDecimal.valueOf(sourceZeitabschnittInput.getRueckwirkendReduziertesPensumRest()))) < 0) {
				int rueckwirekndRedPeRe = sourceZeitabschnittInput.getRueckwirkendReduziertesPensumRest();
				double hours = Math.max(anspruchberechtigtesPensumInStunden.subtract(betreuungspensumInStunden).doubleValue(), 0.0);

				final int anspruchspensumRest = (int) Math.round(hours / faktor);
				targetZeitabschnitt.setAnspruchspensumRest(anspruchspensumRest);
			} else {
				targetZeitabschnitt.setAnspruchspensumRest(0);
			}
		}
	}

	private int calculateRestanspruchInStunden(BigDecimal betreuungspensumInStunden, BigDecimal anspruchberechtigtesPensumInStunden, BigDecimal anspruchspensumRestInStunden, double faktorProzentToStunden) {
		//wenn nicht der ganze anspruch gebraucht wird gibt es einen rest, ansonsten ist rest 0
		if (anspruchberechtigtesPensumInStunden.compareTo(BigDecimal.ZERO) == 0) {
			// Der Anspruch fuer diese Kita war 0, d.h. der Restanspruch bleibt gleich wie auf der vorherigen Betreuung
			return anspruchspensumRestInStunden.divide(BigDecimal.valueOf(faktorProzentToStunden), RoundingMode.CEILING).intValue() ;
		} else if (betreuungspensumInStunden.compareTo(anspruchberechtigtesPensumInStunden) < 0) {
			return anspruchberechtigtesPensumInStunden.subtract(betreuungspensumInStunden).intValue();
		} else {
			return 0;
		}
	}

}
