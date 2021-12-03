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
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;

/**
 * Service zum Verwalten von Modulen
 */
public interface ModulTagesschuleService {

	/**
	 * Aktualisiert das ModulTagesschule in der DB. Sollte das Modul nicht existieren wird es erstellt.
	 *
	 * @param modulTagesschule das zu aktualisierende/erstellende Modul
	 * @return Das aktualisierte Modul
	 */
	@Nonnull
	ModulTagesschule saveModul(@Nonnull ModulTagesschule modulTagesschule);

	/**
	 * Laedt das ModulTagesschule mit der id aus der DB.
	 *
	 * @param modulTagesschuleId PK des Moduls
	 * @return Modul mit der gegebenen id
	 */
	@Nonnull
	Optional<ModulTagesschule> findModul(@Nonnull String modulTagesschuleId);

	/**
	 * Laedt ModulTagessschuleGroup mit der id aus der DB
	 *
	 * @param modulTagesschuleGroupId PK der Group
	 * @return Modul mit der gegebenen id
	 */
	Optional<ModulTagesschuleGroup> findModulTagesschuleGroup(@Nonnull String modulTagesschuleGroupId);

	/**
	 * entfernt ein Modul aus der Database
	 *
	 * @param modulTagesschuleId des zu entfernenden Moduls
	 */
	void removeModul(@Nonnull String modulTagesschuleId);

	/**
	 * Gibt die Tagesschule Einstellungen fuer die uebergebene GP zuruedk
	 */
	Collection<EinstellungenTagesschule> findEinstellungenTagesschuleByGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Kopiert alle vorhandenen ModulTagesschule zur neuen Gesuchsperiode
	 */
	void copyModuleTagesschuleToNewGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiodeToCreate,
		@Nonnull Gesuchsperiode lastGesuchsperiode);

	@Nonnull
	Set<ModulTagesschuleGroup> findModulTagesschuleGroup(@Nonnull AnmeldungTagesschule anmeldung);
}
