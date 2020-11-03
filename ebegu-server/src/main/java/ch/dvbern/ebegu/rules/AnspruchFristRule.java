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

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Sonderregel die nach der eigentlichen Berechnung angewendet wird.
 * Erst jetzt können wir genau sagen, wann der Anspruch in welche Richtung ändert: Falls der Anspruch gemäss
 * Ereignisdatum bzw. Einreichedatum innerhalb eines Monats sinken würde, so gilt der alte Anspruch noch bis Ende
 * Monat!
 */
public final class AnspruchFristRule extends AbstractAbschlussRule {

	public AnspruchFristRule(boolean isDebug) {
		super(isDebug);
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	@Nonnull
	public List<VerfuegungZeitabschnitt> execute(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		List<VerfuegungZeitabschnitt> result = new LinkedList<>();
		VerfuegungZeitabschnitt vorangehenderAbschnitt = null;
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitte) {
			result.add(zeitabschnitt);
			// Es muessen nur Abschnitte beachtet werden, die *innerhalb* des Monats anfangen!
			if (vorangehenderAbschnitt != null && zeitabschnitt.getGueltigkeit().getGueltigAb().getDayOfMonth() > 1) {
				// Der Anspruch ist kleiner als der Anspruch von vorangehenderAbschnitt
				int anspruchNeuAsiv = zeitabschnitt.getBgCalculationInputAsiv().getAnspruchspensumProzent();
				int anspruchNeuGemeinde = zeitabschnitt.getBgCalculationInputGemeinde().getAnspruchspensumProzent();
				int anspruchVorherAsiv = vorangehenderAbschnitt.getBgCalculationInputAsiv().getAnspruchspensumProzent();
				int anspruchVorherGemeinde = vorangehenderAbschnitt.getBgCalculationInputGemeinde().getAnspruchspensumProzent();
				boolean anspruchVerminderungAsiv = anspruchNeuAsiv < anspruchVorherAsiv;
				boolean anspruchVerminderungGemeinde = anspruchNeuGemeinde < anspruchVorherGemeinde;
				if (anspruchVerminderungAsiv || anspruchVerminderungGemeinde) {
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
						if (anspruchVerminderungAsiv) {
							zeitabschnitt.getBgCalculationInputAsiv().setAnspruchspensumProzent(anspruchVorherAsiv);
						}
						if (anspruchVerminderungGemeinde) {
							zeitabschnitt.getBgCalculationInputGemeinde().setAnspruchspensumProzent(anspruchVorherGemeinde);
						}
						zeitabschnitt.getBemerkungenList().addAllBemerkungen(vorangehenderAbschnitt.getBemerkungenList());

						vorangehenderAbschnitt = zeitabschnittNaechsterMonat;
					} else {
						// we need to set both anspruch and bemerkung so both Zeitabschnitte are the same
						if (anspruchVerminderungAsiv) {
							zeitabschnitt.getBgCalculationInputAsiv().setAnspruchspensumProzent(anspruchVorherAsiv);
						}
						if (anspruchVerminderungGemeinde) {
							zeitabschnitt.getBgCalculationInputGemeinde().setAnspruchspensumProzent(anspruchVorherGemeinde);
						}
						zeitabschnitt.getBemerkungenList().clear();
						zeitabschnitt.getBemerkungenList().addAllBemerkungen(vorangehenderAbschnitt.getBemerkungenList());
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
