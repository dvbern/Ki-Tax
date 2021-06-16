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

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.suchfilter.smarttable.BenutzerTableFilterDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.UserRole;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Service fuer die Verwaltung von Benutzern
 */
public interface BenutzerService {

	/**
	 * Aktualisiert den Benutzer in der DB or erstellt ihn wenn er noch nicht existiert.
	 * Falls die Berechtigungen geändert haben, werden diese aktualisiert und der Benutzer ausgeloggt.
	 *
	 * @param benutzer die Benutzer als DTO
	 * @return Die aktualisierte Benutzer
	 */
	@Nonnull
	Benutzer saveBenutzerBerechtigungen(@Nonnull Benutzer benutzer, boolean currentBerechtigungChanged);

	/**
	 * Aktualisiert den Benutzer in der DB or erstellt ihn wenn er noch nicht existiert.
	 *
	 * @param benutzer die Benutzer als DTO
	 * @return Die aktualisierte Benutzer
	 */
	@Nonnull
	Benutzer saveBenutzer(@Nonnull Benutzer benutzer);

	/**
	 * Creates a new user of Role userRole with the given adminMail as email and as username and the given
	 * Gemeinde as the only Gemeinde in the current Berechtigung, which will be valid from today on. Name
	 * and Vorname will be set to "UNKNOWN"
	 */
	@Nonnull
	Benutzer createAdminGemeindeByEmail(
		@Nonnull String adminMail,
		@Nonnull UserRole userRole,
		@Nonnull Gemeinde gemeinde);

	/**
	 * Creates a new user of Role ADMIN_INSTITUTION with the given adminMail as email and as username and the given
	 * Institution as the only
	 * Institution in the current Berechtigung, which will be valid from today on. Name and Vorname will be set to
	 * "UNKNOWN"
	 */
	Benutzer createAdminInstitutionByEmail(@Nonnull String adminMail, @Nonnull Institution institution);

	/**
	 * Creates a new user of Role ADMIN_TRAEGERSCHAFT with the given adminMail as email and as username and the given
	 * Traegerschaft as the only
	 * Traegerschaft in the current Berechtigung, which will be valid from today on. Name and Vorname will be set to
	 * "UNKNOWN"
	 */
	Benutzer createAdminTraegerschaftByEmail(@Nonnull String adminMail, @Nonnull Traegerschaft traegerschaft);

	/**
	 * Creates a new user of Role ADMIN_SOZIALDIENST with the given adminMail as email and as username and the given
	 * Sozialdienst as the only Sozialdienst in the current Berechtigung, which will be valid from today on. Name
	 * and Vorname will be set to "UNKNOWN"
	 */
	@Nonnull
	Benutzer createAdminSozialdienstByEmail(@Nonnull String adminMail, @Nonnull Sozialdienst sozialdienst);

	/**
	 * Saves the given Benutzer and sends him an Einladungsemail
	 */
	@Nonnull
	Benutzer einladen(@Nonnull Einladung einladung);

	/**
	 * Sendet einem eingeladenen Benutzer erneut das Einladungsmail
	 */
	@Nonnull
	void erneutEinladen(@Nonnull Benutzer eingeladener);

	void checkBenutzerIsNotGesuchstellerWithFreigegebenemGesuch(@Nonnull Benutzer benutzer);

	@Nonnull
	Optional<Benutzer> findBenutzer(@Nonnull String username);

	@Nonnull
	Optional<Benutzer> findAndLockBenutzer(@Nonnull String username);

	@Nonnull
	Optional<Benutzer> findBenutzerById(@Nonnull String id);

	/**
	 * @param email E-Mail Adresse des Benutzers
	 * @return Benutzer mit der gegebenen E-Mail Adresse oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Benutzer> findBenutzerByEmail(@Nonnull String email);

	/**
	 * Sucht einen Benutzer nach externalUUID: Diese Methode wird nur von den Connectoren gebraucht.
	 * Innerhalb ebegu verwenden wir weiterhin die ID.
	 */
	@Nonnull
	Optional<Benutzer> findBenutzerByExternalUUID(@Nonnull String externalUUID);

	/**
	 * Gibt alle Administratoren einer Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getGemeindeAdministratoren(Gemeinde gemeinde);

	/**
	 * Gibt alle Sachbearbeiter einer Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getGemeindeSachbearbeiter(Gemeinde gemeinde);

	/**
	 * Gibt alle Administratoren einer Institution zurueck.
	 *
	 * @param institution Die Institution (Kita)
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getInstitutionAdministratoren(Institution institution);

	/**
	 * Gibt alle Sachbearbeiter einer Institution zurueck.
	 *
	 * @param institution Die Institution (Kita)
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getInstitutionSachbearbeiter(Institution institution);

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde einer bestimmten Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getBenutzerBgOrGemeinde(Gemeinde gemeinde);

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG, Admin_BG, Sachbearbeiter_TS, Admin_TS oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde einer bestimmten Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getBenutzerTsBgOrGemeinde(Gemeinde gemeinde);

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_TS oder Admin_TS oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde einer bestimmten Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getBenutzerTsOrGemeinde(Gemeinde gemeinde);

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
	 *
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getAllBenutzerBgOrGemeinde();

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG, Sachbearbeiter_TS, Admin_TS
	 * oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
	 *
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getAllBenutzerBgTsOrGemeinde();

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_TS oder Admin_TS oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
	 *
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getAllBenutzerTsOrGemeinde();

	/**
	 * @return Liste saemtlicher Gesuchsteller aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getGesuchsteller();

	/**
	 * entfernt die Benutzer aus der Database
	 *
	 * @param username die Benutzer als DTO
	 */
	void removeBenutzer(@Nonnull String username);

	/**
	 * Gibt den aktuell eingeloggten Benutzer zurueck
	 */
	@Nonnull
	Optional<Benutzer> getCurrentBenutzer();

	/**
	 * inserts a user received from iam or updates it if it alreday exists
	 */
	Benutzer updateOrStoreUserFromIAM(@Nonnull Benutzer benutzer);

	/**
	 * Setzt den uebergebenen Benutzer auf gesperrt. Es werden auch alle möglicherweise noch vorhandenen
	 * AuthentifizierteBenutzer gelöscht.
	 */
	@Nonnull
	Benutzer sperren(@Nonnull String username);

	/**
	 * Reaktiviert den uebergebenen Benutzer wieder.
	 */
	@Nonnull
	Benutzer reaktivieren(@Nonnull String username);

	/**
	 * Sucht Benutzer, welche den übergebenen Filterkriterien entsprechen
	 */
	@Nonnull
	Pair<Long, List<Benutzer>> searchBenutzer(
		@Nonnull BenutzerTableFilterDTO benutzerTableFilterDto,
		@Nonnull Boolean forStatistik);

	/**
	 * Setzt alle Benutzer mit abgelaufenen Rollen auf die Rolle GESUCHSTELLER zurück.
	 *
	 * @return Die Anzahl zurückgesetzter Benutzer
	 */
	int handleAbgelaufeneRollen(@Nonnull LocalDate stichtag);

	/**
	 * Schreibt eine Berechtigungs-History in die DB
	 */
	void saveBerechtigungHistory(@Nonnull Berechtigung berechtigung, boolean deleted);

	/**
	 * Gibt alle BerechtigungsHistories fuer den übergebenen Benutzer zurück
	 */
	@Nonnull
	Collection<BerechtigungHistory> getBerechtigungHistoriesForBenutzer(@Nonnull Benutzer benutzer);

	/**
	 * Gibt zurück, ob der Benutzer mit der übergebenen Username in irgendeiner Gemeinde (für die der eingeloggte
	 * Benutzer nicht zwingend berechtigt sein muss) als Defaultbenutzer gesetzt ist.
	 */
	boolean isBenutzerDefaultBenutzerOfAnyGemeinde(@Nonnull String username);

	/**
	 * Loescht die externalUUID des Benutzers
	 */
	void deleteExternalUUIDInNewTransaction(@Nonnull String id);

	/**
	 * Gibt zurück, ob der Benutzer eine offene Einladung hat
	 */
	Optional<Benutzer> findUserWithInvitationByEmail(@Nonnull Benutzer benutzer);

	/**
	 * Erzeugt einen Einladungslink für einen Benutzer
	 */
	String createInvitationLink(@Nonnull Benutzer eingeladener, @Nonnull Einladung einladung);

}
