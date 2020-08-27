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

package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractEntity;

public interface BaseService {

	/**
	 * Behandlung des Version-Attributes: Dieses wird neu auf den Client geschickt, um
	 * OptimisticLocking von Hibernate verwenden zu koennen.
	 * Da es aber bei attachten entities nicht möglich ist die Version manuell zu setzen
	 * müssen wir die entities detachen um die Version vom Client reinschreiben zu können.
	 *
	 * So merkt hibernate beim mergen wenn die Versionsnummer in der Zwischenzeit
	 * incremented wurde und höher ist als die die auf den client ging. Falls dies
	 * der Fall ist, wird eine OptimisticLockingException geworfen.
	 *
	 * Damit nach dem Speichern die richtige (in der Regel inkrementierte) Version
	 * auf den Client geht muss das betroffene Entity wirklich schon gemerged
	 * worden sein oder man muss die Version manuell um eins erhöhen im dto.
	 * Gelöst wird das aktuell indem em.flush() gemacht wird vor dem erstellen des
	 * Rückgabe-DTOs.
	 */
	@Nonnull
    <T extends AbstractEntity> T checkVersionSaveAndFlush(@Nonnull T entity, long version);
}
