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

package ch.dvbern.ebegu.services;

import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;

/**
 * Service zum Verwalten von Einstellungen.
 */
public interface EinstellungService {

	/**
	 * Speichert eine Einstellung
	 */
	Einstellung saveEinstellung(@Nonnull Einstellung einstellung);

	/**
	 * Sucht eine Einstellung nach folgendem Schema:
	 * (1) Wenn Einstellung dem gewünschten Key spezifisch für die gewünschte Gemeinde vorhanden ist, wird diese zurueckgegeben
	 * (2) Wenn nicht, wird geschaut, ob es eine spezifische Einstellung für den Mandanten der gewünschten Gemeinde gibt
	 * (3) Wenn nicht, wird die allgemeine, systemweite Einstellung zurückgegeben
	 * @throws Exception, wenn auf *keiner* Stufe ein Resultat gefunden wird
	 */
	Optional<Einstellung> findEinstellung(@Nonnull EinstellungKey key, @Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);

}
