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

package ch.dvbern.ebegu.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;

/**
 * Comparator, der die Betreuungen nach folgender Regel sortiert:
 * 1. Die Kita mit dem früherem Startdatum wird zuerst berücksichtigt.
 * 2. Falls beide Angebote dasselbe Startdatum haben, wird die Kita mit dem höheren Pensum berücksichtigt.
 * 3. Falls beide Angebote dasselbe Startdatum und dasselbe Pensum haben, wird die Kita zuerst berücksichtigt, die als erstes erfasst wurde.
 */
public class BetreuungComparator implements Comparator<AbstractPlatz>, Serializable {

	private static final long serialVersionUID = -309383917391346314L;

	@Override
	public int compare(AbstractPlatz platz1, AbstractPlatz platz2) {
		// Reihenfolge ist nur fuer Betreuungen relevant für Restanspruch, daher werden nur Betreuungen verglichen.
		// Anmeldungen Tagesschule beliben immer am gleichen Ort in Relation zu Betreuungen
		if (!(platz1 instanceof Betreuung && platz2 instanceof Betreuung)) {
			return 0;
		}

		Betreuung betreuung1 = (Betreuung) platz1;
		Betreuung betreuung2 = (Betreuung) platz2;

		// Neue Sortierung: Nach Beginn des ersten Betreuungspensums
		List<BetreuungspensumContainer> betreuungenSorted1 = new LinkedList<>(betreuung1.getBetreuungspensumContainers());
		List<BetreuungspensumContainer> betreuungenSorted2 = new LinkedList<>(betreuung2.getBetreuungspensumContainers());

		Collections.sort(betreuungenSorted1, new BetreuungspensumContainerComparator());
		Collections.sort(betreuungenSorted2, new BetreuungspensumContainerComparator());

		if (betreuungenSorted1.isEmpty() || betreuungenSorted2.isEmpty()) {
			return 0;
		}
		//jeweils das erste pensum vergleichen
		Betreuungspensum firstBetreuungspensum1 = betreuungenSorted1.get(0).getBetreuungspensumJA();
		Betreuungspensum firstBetreuungspensum2 = betreuungenSorted2.get(0).getBetreuungspensumJA();

		// Regel 0: Untenstehendes gilt nur fuer JA-Angebote Kleinkind!
		if (betreuung1.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			// 1. ist JA-Kleinkind, aber 2. nicht
			if (!betreuung2.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
				return -1;
			}
		} else if (betreuung2.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			// 2. ist JA-Kleinkind, aber 1. nicht
			return 1;
		}

		// Regel 1: Betreuung, die zuerst beginnt
		int result = firstBetreuungspensum1.getGueltigkeit().getGueltigAb().compareTo(firstBetreuungspensum2.getGueltigkeit().getGueltigAb());
		if (result == 0) {
			// Regel 2: Höheres Pensum
			result = firstBetreuungspensum2.getPensum().compareTo(firstBetreuungspensum1.getPensum()); // Absteigend
			if (result == 0) {
				// Regel 3: Reihenfolge der Erfassung
				result = betreuung1.getBetreuungNummer().compareTo(betreuung2.getBetreuungNummer());
			}
		}
		return result;
	}
}
