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

package ch.dvbern.ebegu.services.util;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.lib.cdipersistence.Persistence;

public final class DetachUtil {

	private DetachUtil() {
	}

	/**
	 * Hack, welcher das Gesuch detached, damit es auf keinen Fall gespeichert wird. Vorher muessen die Lazy geloadeten
	 * Listen geladen werden, da danach keine Session mehr zur Verfuegung steht!
	 */
	public static void loadRelationsAndDetach(Gesuch gesuch, Persistence persistence) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (AnmeldungTagesschule anmeldungTagesschule : kindContainer.getAnmeldungenTagesschule()) {
				anmeldungTagesschule.getAnmeldungTagesschuleZeitabschnitts().size();
				if (anmeldungTagesschule.getBelegungTagesschule() != null) {
					anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().size();
				}
			}
			kindContainer.getAnmeldungenFerieninsel().size();
		}
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			betreuung.getBetreuungspensumContainers().size();
			betreuung.getAbwesenheitContainers().size();
		}
		if (gesuch.getGesuchsteller1() != null) {
			gesuch.getGesuchsteller1().getAdressen().size();
			gesuch.getGesuchsteller1().getErwerbspensenContainers().size();
		}
		if (gesuch.getGesuchsteller2() != null) {
			gesuch.getGesuchsteller2().getAdressen().size();
			gesuch.getGesuchsteller2().getErwerbspensenContainers().size();
		}
		persistence.getEntityManager().detach(gesuch);
	}
}
