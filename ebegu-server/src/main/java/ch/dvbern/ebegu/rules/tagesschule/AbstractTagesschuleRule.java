/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.tagesschule;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschuleZeitabschnitt;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

/**
 * Aktuell sind die Tagesschule Tarifen fuer jede Gemeinde gleich berechnet
 * So diese Rules muessen jeweils durchgefuhrt werden
 */
public abstract class AbstractTagesschuleRule {

	/**
	 *  Nimmt eine Liste von Verfügung Zeitabschnitts und gibt zurück eine Liste von Zeitabschnitts
	 *  Ziel ist die Zeitabschnitten auf basis von der FamilienSituationEvaluator richtig zu definieren
	 *  bevor man die Tarife berechnet
	 */
	protected abstract List<VerfuegungZeitabschnitt> executeVerfuegungZeitabschnittRule(@Nonnull Gesuch gesuch,
		@Nonnull List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts);

	/**
	 *  Nimmt eine Liste von Anmeldung Tagesschule Zeitabschnitts und gibt zurück eine Liste von Anmeldung Tagesschule
	 *  Zeitabschnitts, noch nicht noetig
	 */
	protected List<AnmeldungTagesschuleZeitabschnitt> executeAnmeldungTagesschuleZeitabschnittRule(@Nonnull AnmeldungTagesschule anmeldungTagesschule,
		@Nonnull List<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts){
		return anmeldungTagesschuleZeitabschnitts;
	}
}
