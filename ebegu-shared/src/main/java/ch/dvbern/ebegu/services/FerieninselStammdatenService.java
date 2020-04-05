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

package ch.dvbern.ebegu.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BelegungFerieninselTag;
import ch.dvbern.ebegu.entities.EinstellungenFerieninsel;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiode;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.Ferienname;

/**
 * Service zum Verwalten von Ferieninsel-Stammdaten
 */
public interface FerieninselStammdatenService {

	@Nonnull
	List<GemeindeStammdatenGesuchsperiodeFerieninsel> findGesuchsperiodeFerieninselByGemeindeAndPeriode(@Nonnull String gemeindeId, @Nonnull String gesuchsperiodeId);

	void initFerieninselStammdaten(@Nonnull GemeindeStammdatenGesuchsperiode gemeindeStammdatenGesuchsperiode);

	/**
	 * Loescht das uebergebene FerieninselStammdaten-Objekt
	 */
	void removeFerieninselStammdaten(@Nonnull String gemeindeStammdatenGesuchsperiodeFerieninselId);
	/**
	 * Speichert ferieninselStammdaten Objekt
	 */
	@Nonnull
	GemeindeStammdatenGesuchsperiodeFerieninsel saveFerieninselStammdaten(
		@Nonnull GemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdaten
	);

	/**
	 * Sucht das FerieninselStammdaten-Objekt mit der uebergebenen Id
	 */
	@Nonnull
	Optional<GemeindeStammdatenGesuchsperiodeFerieninsel> findFerieninselStammdaten(@Nonnull String ferieninselStammdatenId);

	/**
	 * Kopiert alle vorhandenen EinstellungenFerieninsel zur neuen Gesuchsperiode
	 */
	void copyEinstellungenFerieninselToNewGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiodeToCreate,
		@Nonnull Gesuchsperiode lastGesuchsperiode);

	/**
	 * Liefert alle EinstellungenFerieninsel für eine Gesuchsperiode
	 */
	Collection<EinstellungenFerieninsel> findEinstellungenFerieninselByGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Gibt alle existierenden FerieninselStammdaten-Objekte (aller Gesuchsperioden) zurueck.
	 *//*
	@Nonnull
	Collection<GemeindeStammdatenGesuchsperiodeFerieninsel> getAllFerieninselStammdaten();

	*/
	/**
	 * Gibt alle FerieninselStammdaten-Objekte fuer die uebergebene Gesuchsperiode und Ferien zurueck.
	 */
	@Nonnull
	Optional<GemeindeStammdatenGesuchsperiodeFerieninsel> findFerieninselStammdatenForGesuchsperiodeAndFerienname(
		@Nonnull String gemeindeId, @Nonnull String gesuchsperiodeId, @Nonnull Ferienname ferienname);

	/**
	 * Gibt fuer eine Ferieninsel die potentiell buchbaren Daten zurück, also alle Wochentage des Zeitraums ohne
	 * Feiertage
	 */
	@Nonnull
	List<BelegungFerieninselTag> getPossibleFerieninselTage(@Nonnull GemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdaten);

}
