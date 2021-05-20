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
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;

/**
 * Service zum Verwalten von Institutionen
 */
public interface InstitutionService {

	/**
	 * Aktualisiert die Institution in der DB
	 *
	 * @param institution Die Institution als DTO
	 */
	@Nonnull
	Institution updateInstitution(@Nonnull Institution institution);

	/**
	 * Speichert die Institution neu in der DB
	 *
	 * @param institution Die Institution als DTO
	 */
	@Nonnull
	Institution createInstitution(@Nonnull Institution institution);

	/**
	 * @param id PK (id) der Institution
	 * @return Institution mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Institution> findInstitution(@Nonnull String id, boolean doAuthCheck);

	/**
	 * @param traegerschaftId Der ID der Traegerschaft, fuer welche die Institutionen gesucht werden muessen
	 * @return Liste mit allen Institutionen der gegebenen Traegerschaft
	 */
	@Nonnull
	Collection<Institution> getAllInstitutionenFromTraegerschaft(String traegerschaftId);

	/**
	 * @return Alle Institutionen in der DB
	 */
	@Nonnull
	Collection<Institution> getAllInstitutionen();

	/**
	 * @return Alle Institutionen in der DB ohne Berechtigungspruefung, nur fuer Batchjob.
	 */
	@Nonnull
	Collection<Institution> getAllInstitutionenForBatchjobs();

	/**
	 * Gibt alle aktiven Institutionen zurueck, fuer welche der aktuell eingeloggte Benutzer *schreib*-berechtigt ist.
	 *
	 * @param restrictedForSCH true wenn nur die Institutionen der Art TAGESSCHULE oder FERIENINSEL geholt werden.
	 *                            Dieses Parameter
	 *                         gilt nur fuer die Rolen vom Schulamt
	 */
	Collection<Institution> getInstitutionenEditableForCurrentBenutzer(boolean restrictedForSCH);

	/**
	 * Gibt alle aktiven Institutionen zurueck, fuer welche der aktuell eingeloggte Benutzer *lese*-berechtigt ist.
	 *
	 * @param restrictedForSCH true wenn nur die Institutionen der Art TAGESSCHULE oder FERIENINSEL geholt werden.
	 *                            Dieses Parameter
	 *                         gilt nur fuer die Rolen vom Schulamt
	 */
	Collection<Institution> getInstitutionenReadableForCurrentBenutzer(boolean restrictedForSCH);

	/**
	 * returns all types of Angebot that are offered by this Institution
	 */
	BetreuungsangebotTyp getAngebotFromInstitution(@Nonnull String institutionId);

	/**
	 * Will take all Institutions and check whether its Stammdaten has to be checked (stammdaten haven't been saved
	 * for a long time) or not.
	 * If it does it will set the Flag stammdatenCheckRequired to true. It will set it to false otherwise.
	 */
	void calculateStammdatenCheckRequired();

	/**
	 * Updates the Flag stammdatenCheckRequired to false and updates the Stammdaten so timestamp_mutiert gets updated
	 */
	void deactivateStammdatenCheckRequired(@Nonnull String institutionId);

	/**
	 * Updates the Flag stammdatenCheckRequired to the given value
	 */
	void updateStammdatenCheckRequired(@Nonnull String institutionId, boolean isCheckRequired);

	/**
	 * Removes the institution given by the id totally from the DB if this isn't linked to any other object
	 */
	void removeInstitution(@Nonnull String institutionId);

	void saveInstitutionExternalClients(@Nonnull Institution institution,
		@Nonnull Collection<InstitutionExternalClient> institutionExternalClients);

	Map<Institution, InstitutionStammdaten> getInstitutionenInstitutionStammdatenEditableForCurrentBenutzer(boolean restrictedForSCH);


	@Nonnull
	boolean isCurrentUserTagesschuleNutzende(@Nonnull boolean restrictedForSCH);

	Map<Institution, InstitutionStammdaten> getInstitutionenInstitutionStammdatenForGemeinde(Gemeinde gemeinde);
}
