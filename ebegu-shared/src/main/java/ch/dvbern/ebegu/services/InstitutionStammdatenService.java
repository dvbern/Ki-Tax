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

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Service zum Verwalten von InstitutionStammdaten
 */
public interface InstitutionStammdatenService {

	/**
	 * Erstellt eine InstitutionStammdaten in der DB. Wenn eine InstitutionStammdaten mit demselben ID bereits
	 * existiert wird diese dann aktualisiert.
	 *
	 * @param institutionStammdaten Die InstitutionStammdaten als DTO
	 */
	InstitutionStammdaten saveInstitutionStammdaten(InstitutionStammdaten institutionStammdaten);

	void fireStammdatenChangedEvent(@Nonnull InstitutionStammdaten updatedStammdaten);

	/**
	 * @param institutionStammdatenID PK (id) der InstitutionStammdaten
	 * @return InstitutionStammdaten mit dem gegebenen key oder null falls nicht vorhanden
	 */
	Optional<InstitutionStammdaten> findInstitutionStammdaten(String institutionStammdatenID);

	/**
	 * @return Aller InstitutionStammdaten aus der DB für den Mandanten des Principal
	 */
	@Nonnull
	Collection<InstitutionStammdaten> getAllInstitutionStammdaten();

	/**
	 * @return Aller InstitutionStammdaten aus der DB ohne Berechtigungspruefung, nur fuer Batchjob.
	 */
	@Nonnull
	Collection<InstitutionStammdaten> getAllInstitonStammdatenForBatchjobs();

	/**
	 * totally removes a InstitutionStammdaten from the Database. It takes the InstitutionStammdaten based on the
	 * given Institution ID
	 */
	void removeInstitutionStammdatenByInstitution(@Nonnull String institutionId);

	/**
	 * @param gesuchsperiodeId Id der gewuenschten Gesuchsperiode
	 * @param gemeindeId Id der gewuenschten Gemeinde
	 * @return Alle aktiven InstitutionStammdaten bei denen eine Ueberschneidung der Gueltigkeit zwischen datumVon und
	 * datumBis liegt und die (falls TS oder FI oder BG bei Filterung durch Gemeinde) zur übergebenen Gemeinde gehören
	 */
	Collection<InstitutionStammdaten> getAllActiveInstitutionStammdatenByGesuchsperiodeAndGemeinde(
		@Nonnull String gesuchsperiodeId,
		@Nonnull String gemeindeId);

	/**
	 * Gibt die Stammdaten dieser Institution zurueck.
	 * Falls die Institution keine Stammdaten hat gibt sie null zurück, dabei wird keine Ausnahme geworfen.
	 */
	InstitutionStammdaten fetchInstitutionStammdatenByInstitution(String institutionId, boolean doAuthCheck);

	/**
	 * Gibt alle Betreuungsangebotstypen zurueck, welche die Institutionen anbieten, fuer welche der
	 * aktuell eingeloggte Benutzer berechtigt ist. Sollte der Benutzer ein Admin oder Sachbearbeiter vom Schulamt
	 * sein, wird dann direkt TAGESSCHULE und FERIENINSEL zurueckgegeben.
	 */
	Collection<BetreuungsangebotTyp> getBetreuungsangeboteForInstitutionenOfCurrentBenutzer();

	/**
	 * Gibt alle Tagesschulen für den momentan eingeloggten Benutzer zurück. Für Administratoren werden alle
	 * Tagesschulen zurückgegeben.
	 */
	Collection<InstitutionStammdaten> getTagesschulenForCurrentBenutzer();

	Collection<InstitutionStammdaten> getAllInstitutionStammdatenForTraegerschaft(@Nonnull Traegerschaft trageschaft);

	/**
	 * Findet über den GeoAdmin Service für Adresse in den InsitutionStammdaten sowie für jeden Betreuungsstandort die
	 * politische Gemeinde und speichert diese ab. Dazu wird der GeoAdminService genutzt
	 *
	 * @return Set containing all changed InstitutionStammdaten
	 */
	@Nonnull
	Set<InstitutionStammdaten> updateGemeindeForBGInstitutionen();

	/**
	 * Gibt alle Tagesschulen zurueck, welche in der uebergebenen Gesuchsperiode aktiv waren
	 * fuer die gewuenschte Gemeinde
	 */
	@Nonnull
	Collection<InstitutionStammdaten> getAllTagesschulenForGesuchsperiodeAndGemeinde(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde);

	@Nonnull
	Collection<InstitutionStammdaten> getAllTagesschulenForGemeinde(@Nonnull Gemeinde gemeinde);

	boolean isGueltigkeitDecrease(@Nonnull DateRange current, @Nonnull DateRange change);

	/**
	 * Nur für Batchjob.
	 * Löscht von allen Institutionen die Felder:
	 * * Auslastung der Institutionen
	 * * Anzahl Kinder auf Warteliste
	 * * Pensum Warteliste
	 * * Dauer Warteliste
	 */
	void deleteInstitutionKennzahlenFields();
}
