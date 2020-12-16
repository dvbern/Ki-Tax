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
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractAbschlussRule;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.FERIENINSEL;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

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

	private static final Logger LOG = LoggerFactory.getLogger(RestanspruchInitializer.class);

	public RestanspruchInitializer(boolean isDebug) {
		super(isDebug);
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE, FERIENINSEL);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		if (!platz.getBetreuungsangebotTyp().isJugendamt()) {
			// Im Fall des RestanspruchInitializer darf bei nicht-gebrauch nicht einfach die erhaltene Liste zurueckgegeben werden, sondern
			// es muss *immer* eine neue Liste erstellt werden
			return createInitialenRestanspruch(platz.extractGesuchsperiode(), zeitabschnitte.get(0).isHasGemeindeSpezifischeBerechnung());
		}
		List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte = new ArrayList<>();

		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitte) {
			VerfuegungZeitabschnitt restanspruchsAbschnitt = new VerfuegungZeitabschnitt(zeitabschnitt.getGueltigkeit());
			restanspruchsAbschnitt.setHasGemeindeSpezifischeBerechnung(zeitabschnitt.isHasGemeindeSpezifischeBerechnung());
			restanspruchUebernehmenVerfuegt(
				zeitabschnitt.getBgCalculationResultAsiv(),
				restanspruchsAbschnitt.getBgCalculationInputAsiv(),
				platz);
			if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung()) {
				Objects.requireNonNull(zeitabschnitt.getBgCalculationResultGemeinde());
				restanspruchUebernehmenVerfuegt(
					zeitabschnitt.getBgCalculationResultGemeinde(),
					restanspruchsAbschnitt.getBgCalculationInputGemeinde(),
					platz);
			} else {
				restanspruchsAbschnitt.getBgCalculationInputGemeinde().setAnspruchspensumRest(-1);
			}
			restanspruchZeitabschnitte.add(restanspruchsAbschnitt);
		}
		return restanspruchZeitabschnitte;
	}

	private void restanspruchUebernehmenVerfuegt(
		@Nonnull BGCalculationResult sourceZeitabschnitt,
		@Nonnull BGCalculationInput targetZeitabschnitt,
		@Nonnull AbstractPlatz platz
	) {
		int anspruchberechtigtesPensum = sourceZeitabschnitt.getAnspruchspensumProzent();
		BigDecimal betreuungspensum = sourceZeitabschnitt.getBetreuungspensumProzent();
		if (sourceZeitabschnitt.getAnspruchspensumRest() != null) {
			// Wir haben einen Restanspruch nach dem neuen System gespeichert
			final BigDecimal anspruchspensumRest = sourceZeitabschnitt.getAnspruchspensumRest();
			final int restanspruchNeu = calculateRestanspruch(betreuungspensum.intValue(), anspruchberechtigtesPensum, anspruchspensumRest.intValue());
			targetZeitabschnitt.setAnspruchspensumRest(restanspruchNeu);
		} else {
			if (betreuungspensum.compareTo(BigDecimal.ZERO) == 0) {
				// In der Vorgaengerverfuegung bestand keine Betreuung mehr fuer diesen Abschnitt. Wir setzen
				// den Restanspruch auf -1 (= noch nicht berechnet)
				targetZeitabschnitt.setAnspruchspensumRest(-1);
			}
			//wenn nicht der ganze anspruch gebraucht wird gibt es einen rest, ansonsten ist rest 0
			else if (betreuungspensum.compareTo(BigDecimal.valueOf(anspruchberechtigtesPensum)) < 0) {
				targetZeitabschnitt.setAnspruchspensumRest(anspruchberechtigtesPensum - betreuungspensum.intValue());
			} else {
				targetZeitabschnitt.setAnspruchspensumRest(0);
			}
		}
	}

	private int calculateRestanspruch(int betreuungspensum, int anspruchberechtigtesPensum, int anspruchspensumRest) {
		//wenn nicht der ganze anspruch gebraucht wird gibt es einen rest, ansonsten ist rest 0
		if (anspruchberechtigtesPensum == 0) {
			// Der Anspruch fuer diese Kita war 0, d.h. der Restanspruch bleibt gleich wie auf der vorherigen Betreuung
			return anspruchspensumRest;
		} else if (betreuungspensum < anspruchberechtigtesPensum) {
			return anspruchberechtigtesPensum - betreuungspensum;
		} else {
			return 0;
		}
	}

	@Nonnull
	public static List<VerfuegungZeitabschnitt> createInitialenRestanspruch(@Nonnull Gesuchsperiode gesuchsperiode, boolean hasGemeindeSpezifischeBerechnung) {
		List<VerfuegungZeitabschnitt> restanspruchZeitabschnitte = new ArrayList<>();
		VerfuegungZeitabschnitt initialerRestanspruch = new VerfuegungZeitabschnitt(gesuchsperiode.getGueltigkeit());
		// Damit wir erkennen, ob schon einmal ein "Rest" durch eine Rule gesetzt wurde
		initialerRestanspruch.setAnspruchspensumRestForAsivAndGemeinde(-1);
		initialerRestanspruch.setHasGemeindeSpezifischeBerechnung(hasGemeindeSpezifischeBerechnung);
		restanspruchZeitabschnitte.add(initialerRestanspruch);
		return restanspruchZeitabschnitte;
	}
}
