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

package ch.dvbern.ebegu.rules;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

/**
 * Abschlussmerger, welcher nach allen Regeln die vorhandenen Abschnitte überprüft und solche mit gleichen *sichtbaren*
 * Daten zusammenmergt.
 */
public final class AbschlussNormalizer extends AbstractAbschlussRule {

	private boolean keepMonate;

	public AbschlussNormalizer(boolean keepMonate, boolean isDebug) {
		super(isDebug);
		this.keepMonate = keepMonate;
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}

	@Nonnull
	@Override
	public List<VerfuegungZeitabschnitt> execute(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		List<VerfuegungZeitabschnitt> result = new ArrayList<>();
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitte) {
			normalizeZeitabschnitte(result, zeitabschnitt);
		}
		return result;
	}

	/**
	 * Stellt sicher, dass zwei aufeinander folgende Zeitabschnitte nie dieselben Daten haben. Falls
	 * dies der Fall wäre, werden sie zu einem neuen Schnitz gemergt.
	 */
	private void normalizeZeitabschnitte(
		@Nonnull List<VerfuegungZeitabschnitt> validZeitabschnitte,
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt
	) {
		// Zuerst vergleichen, ob sich der neue Zeitabschnitt vom letzt hinzugefügten (und angrenzenden) unterscheidet
		int indexOfLast = validZeitabschnitte.size() - 1;
		if (indexOfLast >= 0) {
			VerfuegungZeitabschnitt lastZeitabschnitt = validZeitabschnitte.get(indexOfLast);
			if (lastZeitabschnitt.isSameSichtbareDaten(zeitabschnitt) && zeitabschnitt.getGueltigkeit().startsDayAfter(lastZeitabschnitt.getGueltigkeit())) {
				// Gleiche Berechnungsgrundlagen:
				// Zusammenfuegen, falls sie im gleichen Monat liegen, oder keepMonate = false
				if (!keepMonate || isSameMonth(zeitabschnitt, lastZeitabschnitt)) {
					lastZeitabschnitt.getGueltigkeit().setGueltigBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
					// Die Bemerkungen zusammenfügen mit Vermeidung von Duplikaten
					if (zeitabschnitt.getBemerkungenList() != null) {
						lastZeitabschnitt.getBemerkungenList().mergeBemerkungenMap(zeitabschnitt.getBemerkungenList());
					}
					validZeitabschnitte.remove(indexOfLast);
					validZeitabschnitte.add(lastZeitabschnitt);
				} else {
					// Gleiche Daten, aber nicht zusammenfuegen
					validZeitabschnitte.add(zeitabschnitt);
				}

			} else {
				// Unterschiedliche Daten -> hinzufügen
				validZeitabschnitte.add(zeitabschnitt);
			}
		} else {
			// Erster Eintrag -> hinzufügen
			validZeitabschnitte.add(zeitabschnitt);
		}
	}

	private boolean isSameMonth(@Nonnull VerfuegungZeitabschnitt abschnittA, @Nonnull VerfuegungZeitabschnitt abschnittB) {
		return abschnittA.getGueltigkeit().getGueltigAb().getMonth() == abschnittB.getGueltigkeit().getGueltigAb().getMonth()
			&& abschnittA.getGueltigkeit().getGueltigBis().getMonth() == abschnittB.getGueltigkeit().getGueltigBis().getMonth();
	}
}
