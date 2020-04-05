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

package ch.dvbern.ebegu.rules.initalizer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractAbschlussRule;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

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
public final class RestanspruchInitializer extends AbstractAbschlussRule {


	public RestanspruchInitializer() {
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte = new ArrayList<>();
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitte) {
			VerfuegungZeitabschnitt restanspruchsAbschnitt = new VerfuegungZeitabschnitt(zeitabschnitt.getGueltigkeit());
			restanspruchsAbschnitt.setHasGemeindeSpezifischeBerechnung(zeitabschnitt.isHasGemeindeSpezifischeBerechnung());
			restanspruchUebernehmen(platz, zeitabschnitt.getBgCalculationInputAsiv(), restanspruchsAbschnitt.getBgCalculationInputAsiv());
			restanspruchUebernehmen(platz, zeitabschnitt.getBgCalculationInputGemeinde(), restanspruchsAbschnitt.getBgCalculationInputGemeinde());
			restanspruchZeitabschnitte.add(restanspruchsAbschnitt);
		}
		return restanspruchZeitabschnitte;
	}

	private void restanspruchUebernehmen(@Nonnull AbstractPlatz betreuung, @Nonnull BGCalculationInput sourceZeitabschnitt, BGCalculationInput targetZeitabschnitt) {
		//Die  vom der letzen Berechnung uebernommenen Zeitabschnitte betrachten und den restanspruch berechnen.
		Objects.requireNonNull(betreuung.getBetreuungsangebotTyp());
		if (betreuung.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			int anspruchberechtigtesPensum = sourceZeitabschnitt.getAnspruchspensumProzent();
			BigDecimal betreuungspensum = sourceZeitabschnitt.getBetreuungspensumProzent();
			int anspruchspensumRest = sourceZeitabschnitt.getAnspruchspensumRest();
			//wenn nicht der ganze anspruch gebraucht wird gibt es einen rest, ansonsten ist rest 0
			if (anspruchberechtigtesPensum == 0 && anspruchspensumRest != -1) {
				// Der Restanspruch war schon initialisiert und bleibt gleich wie auf der vorherigen Betreuung
				targetZeitabschnitt.setAnspruchspensumRest(anspruchspensumRest);
			} else if (betreuungspensum.compareTo(BigDecimal.valueOf(anspruchberechtigtesPensum)) < 0) {
				targetZeitabschnitt.setAnspruchspensumRest(anspruchberechtigtesPensum - betreuungspensum.intValue());
			} else {
				targetZeitabschnitt.setAnspruchspensumRest(0);
			}
		}
	}
}
