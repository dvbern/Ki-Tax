/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

/**
 * Sonderregel die nach der eigentlichen Berechnung angewendet wird.
 * Erst jetzt können wir genau sagen, wann der Anspruch in welche Richtung ändert: Falls der Anspruch gemäss
 * Ereignisdatum bzw. Einreichedatum innerhalb eines Monats sinken würde, so gilt der alte Anspruch noch bis Ende
 * Monat!
 */
public final class AnspruchFristRule {

	private AnspruchFristRule() {
	}

	@Nonnull
	public static List<VerfuegungZeitabschnitt> execute(@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		List<VerfuegungZeitabschnitt> result = new LinkedList<>();
		VerfuegungZeitabschnitt vorangehenderAbschnitt = null;
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitte) {
			result.add(zeitabschnitt);
			// Es muessen nur Abschnitte beachtet werden, die *innerhalb* des Monats anfangen!
			if (vorangehenderAbschnitt != null && zeitabschnitt.getGueltigkeit().getGueltigAb().getDayOfMonth() > 1) {
				// Der Anspruch ist kleiner als der Anspruch von vorangehenderAbschnitt
				if (zeitabschnitt.getAnspruchberechtigtesPensum() < vorangehenderAbschnitt.getAnspruchberechtigtesPensum()) {
					// Der Anspruch darf nie innerhalb des Monats kleiner werden
					// d.h. für diesen Abschnitt bis Ende Monat gilt weiterhin der "alte" Anspruch
					if (zeitabschnitt.getGueltigkeit().getGueltigAb().getMonth() != zeitabschnitt.getGueltigkeit().getGueltigBis().getMonth()) {
						// Der Abschnitt geht über die Monatsgrenze hinaus. Es muss ab dem Folgemonat ein neuer Abschnitt
						// mit dem neuen Anspruch erstellt werden
						LocalDate datumAbAlterMonat = zeitabschnitt.getGueltigkeit().getGueltigAb();
						LocalDate datumBisAlterMonat = datumAbAlterMonat.with(TemporalAdjusters.lastDayOfMonth());
						LocalDate datumAbNeuerAbschnitt = datumBisAlterMonat.plusDays(1);
						LocalDate datumBisNeuerAbschnitt = zeitabschnitt.getGueltigkeit().getGueltigBis();

						VerfuegungZeitabschnitt zeitabschnittNaechsterMonat = new VerfuegungZeitabschnitt(zeitabschnitt);
						zeitabschnittNaechsterMonat.getGueltigkeit().setGueltigAb(datumAbNeuerAbschnitt);
						zeitabschnittNaechsterMonat.getGueltigkeit().setGueltigBis(datumBisNeuerAbschnitt);
						result.add(zeitabschnittNaechsterMonat);

						// Den alten Abschnitt per Ende Monat beenden
						zeitabschnitt.getGueltigkeit().setGueltigAb(datumAbAlterMonat);
						zeitabschnitt.getGueltigkeit().setGueltigBis(datumBisAlterMonat);

						// Ab dem naechsten Monat gilt der neue Anspruch
						zeitabschnitt.setAnspruchberechtigtesPensum(vorangehenderAbschnitt.getAnspruchberechtigtesPensum());
						zeitabschnitt.addAllBemerkungen(vorangehenderAbschnitt.getBemerkungenMap());

						vorangehenderAbschnitt = zeitabschnittNaechsterMonat;
					} else {
						// we need to set both anspruch and bemerkung so both zeitabschnite are the same
						zeitabschnitt.setAnspruchberechtigtesPensum(vorangehenderAbschnitt.getAnspruchberechtigtesPensum());
						zeitabschnitt.getBemerkungenMap().clear();
						zeitabschnitt.addAllBemerkungen(vorangehenderAbschnitt.getBemerkungenMap());
						vorangehenderAbschnitt = zeitabschnitt;
					}
				} else {
					vorangehenderAbschnitt = zeitabschnitt;
				}
			} else {
				vorangehenderAbschnitt = zeitabschnitt;
			}
		}
		return result;
	}
}
