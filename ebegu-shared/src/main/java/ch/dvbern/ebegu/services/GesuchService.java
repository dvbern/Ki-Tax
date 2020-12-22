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
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Massenversand;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;

/**
 * Service zum Verwalten von Gesuche
 */
@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public interface GesuchService {

	/**
	 * Erstellt ein neues Gesuch in der DB, falls der key noch nicht existiert
	 *
	 * @param gesuch der Gesuch als DTO
	 * @return das gespeicherte Gesuch
	 */
	@Nonnull
	Gesuch createGesuch(@Nonnull Gesuch gesuchToCreate);

	/**
	 * Aktualisiert das Gesuch in der DB
	 *
	 * @param gesuch das Gesuch als DTO
	 * @param saveInStatusHistory true wenn gewollt, dass die Aenderung in der Status gespeichert wird
	 * @param saveAsUser wenn gesetzt, die Statusaenderung des Gesuchs wird mit diesem User gespeichert, sonst mit
	 * currentUser
	 * @return Das aktualisierte Gesuch
	 */
	@Nonnull
	Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory, @Nullable Benutzer saveAsUser);

	Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory);

	/**
	 * Aktualisiert das Gesuch in der DB
	 *
	 * @param gesuch das Gesuch als DTO
	 * @param saveInStatusHistory true wenn gewollt, dass die Aenderung in der Status gespeichert wird
	 * @param saveAsUser wenn gesetzt, die Statusaenderung des Gesuchs wird mit diesem User gespeichert, sonst mit
	 * currentUser
	 * @param doAuthCheck: Definiert, ob die Berechtigungen (Lesen/Schreiben) geprüft werden muessen.
	 * @return Das aktualisierte Gesuch
	 */
	@Nonnull
	@PermitAll
	Gesuch updateGesuch(
		@Nonnull Gesuch gesuch,
		boolean saveInStatusHistory,
		@Nullable Benutzer saveAsUser,
		boolean doAuthCheck);

	/**
	 * Laedt das Gesuch mit der id aus der DB. ACHTUNG zudem wird hier der Status auf IN_BEARBEITUNG_JA gesetzt
	 * wenn der Benutzer ein JA Mitarbeiter ist und das Gesuch in FREIGEGEBEN ist
	 * Die Berechtigungen werden geprueft
	 *
	 * @param key PK (id) des Gesuches
	 * @return Gesuch mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Gesuch> findGesuch(@Nonnull String key);

	/**
	 * Laedt das Gesuch mit der id aus der DB. ACHTUNG zudem wird hier der Status auf IN_BEARBEITUNG_JA gesetzt
	 * wenn der Benutzer ein JA Mitarbeiter ist und das Gesuch in FREIGEGEBEN ist
	 *
	 * @param key PK (id) des Gesuches
	 * @param doAuthCheck: Definiert, ob die Berechtigungen (Lesen/Schreiben) für dieses Gesuch geprüft werden muessen.
	 * @return Gesuch mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Gesuch> findGesuch(@Nonnull String key, boolean doAuthCheck);

	/**
	 * Spezialmethode fuer die Freigabe. Kann Gesuche lesen die im Status Freigabequittung oder hoeher sind.
	 * Ausserdem wird geprüft, ob es sich um die korrekte Freigabequittung handelt (also seither das Gesuch
	 * nicht durch den Gesuchsteller zurückgezogen wurde)
	 */
	@Nonnull
	Gesuch findGesuchForFreigabe(@Nonnull String gesuchId, @Nonnull Integer anzahlZurueckgezogen, boolean checkAnzahlZurueckgezogen);

	/**
	 * Gibt alle Gesuche zurueck die in der Liste der gesuchIds auftauchen und fuer die der Benutzer berechtigt ist.
	 * Gesuche fuer die der Benutzer nicht berechtigt ist werden uebersprungen
	 */
	List<Gesuch> findReadableGesuche(@Nullable Collection<String> gesuchIds);

	/**
	 * Gibt alle existierenden Gesuche zurueck.
	 *
	 * @return Liste aller Gesuche aus der DB
	 */
	@Nonnull
	Collection<Gesuch> getAllGesuche();

	/**
	 * entfernt ein Gesuch aus der Database. Es wird ein LogEintrag erstellt mit dem Grund des Löschens-
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeGesuch(@Nonnull String gesuchId, GesuchDeletionCause deletionCause);

	/**
	 * Gibt eine Liste von Gesuchen zureck, deren Gesuchsteller 1 den angegebenen Namen und Vornamen hat.
	 * Achtung, damit ist ein Gesuchsteller nicht eindeutig identifiziert!
	 */
	@Nonnull
	List<Gesuch> findGesuchByGSName(String nachname, String vorname);

	/**
	 * Gibt alle Antraege des aktuell eingeloggten Benutzers
	 */
	@Nonnull
	List<Gesuch> getAntraegeOfDossier(@Nonnull Dossier dossier);

	/**
	 * Gibt ein DTO mit saemtlichen Antragen eins bestimmten Dossiers zurueck
	 */
	@Nonnull
	List<JaxAntragDTO> getAllAntragDTOForDossier(String dossierId);

	/**
	 * Gibt das letzte verfuegte Gesuch fuer die uebergebene Gesuchsoperde und den uebergebenen Fall zurueck.
	 */
	@Nonnull
	Optional<Gesuch> getNeustesVerfuegtesGesuchFuerGesuch(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier,
		boolean doAuthCheck);

	/**
	 * Gibt das neueste Gesuch der im selben Fall und Periode wie das gegebene Gesuch ist.
	 * Es wird nach Erstellungsdatum geschaut
	 */
	@Nonnull
	Optional<Gesuch> getNeustesGesuchFuerGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Alle GesucheIDs des Gesuchstellers zurueckgeben fuer admin
	 */
	@Nonnull
	List<String> getAllGesuchIDsForFall(String fallId);

	/**
	 * Alle GesucheIDs des Dossiers zurueckgeben
	 */
	@Nonnull
	List<String> getAllGesuchIDsForDossier(@Nonnull String dossierId);

	/**
	 * Alle Gesuche des Dossiers zurueckgeben
	 */
	@Nonnull
	List<Gesuch> getAllGesuchForDossier(@Nonnull String dossierId);

	/**
	 * Alle Gesuche fuer den gegebenen Fall in der gegebenen Periode
	 */
	@Nonnull
	List<Gesuch> getAllGesucheForDossierAndPeriod(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Das gegebene Gesuch wird mit heutigem Datum freigegeben und den Step FREIGABE auf OK gesetzt
	 */
	Gesuch antragFreigabequittungErstellen(@Nonnull Gesuch gesuch, AntragStatus statusToChangeTo);

	/**
	 * Gibt das Gesuch frei für das Jugendamt/Schulamt: Anpassung des Status inkl Kopieren der Daten des GS aus den
	 * JA-Containern in die GS-Containern. Wird u.a. beim einlesen per Scanner aufgerufen
	 */
	@Nonnull
	Gesuch antragFreigeben(@Nonnull String gesuchId, @Nullable String usernameJA, @Nullable String usernameSCH);

	/**
	 * Zieht die Freigabe wieder zurück.
	 */
	@Nonnull
	Gesuch antragZurueckziehen(@Nonnull String gesuchId);

	/**
	 * Verantwortliche müssen gesetzt werden wenn in einem Papiergesuch oder Papiermutation eine Betreuung hinzugefügt
	 * wird oder eine Online-Mutation freigegeben wird (direkte Freigabe). Beim Einlesen eines Papiergesuchs werden
	 * die Veratnwortliche mittels Dialogfenster durch den Benutzer gesetzt
	 *
	 * @param persist speichert die Verantwortliche direkt auf der DB in Update-Query
	 * @return true if Verantwortliche changed
	 */
	boolean setVerantwortliche(
		@Nullable Benutzer verantwortlicherBG,
		@Nullable Benutzer verantwortlicherTS,
		@Nonnull Gesuch gesuch,
		boolean onlyIfNotSet,
		boolean persist);

	/**
	 * Setzt das gegebene Gesuch als Beschwerde hängig und bei allen Gescuhen der Periode den Flag
	 * gesperrtWegenBeschwerde auf true.
	 *
	 * @return Gibt das aktualisierte gegebene Gesuch zurueck
	 */
	@Nonnull
	Gesuch setBeschwerdeHaengigForPeriode(@Nonnull Gesuch gesuch);

	/**
	 * Setzt das übergebene Gesuch auf den Status "KEIN_KONTINGENT"
	 */
	@Nonnull
	Gesuch setKeinKontingent(@Nonnull Gesuch gesuch);

	/**
	 * Setz "Nur_Schulamt" Gesuche auf den Status NUR_SCHULAMT
	 *
	 * @return Gibt das aktualisierte gegebene Gesuch zurueck
	 */
	@Nonnull
	Gesuch setAbschliessen(@Nonnull Gesuch gesuch);

	/**
	 * Setzt das gegebene Gesuch als VERFUEGT und bei allen Gescuhen der Periode den Flag
	 * gesperrtWegenBeschwerde auf false
	 *
	 * @return Gibt das aktualisierte gegebene Gesuch zurueck
	 */
	@Nonnull
	Gesuch removeBeschwerdeHaengigForPeriode(@Nonnull Gesuch gesuch);

	/**
	 * Gibt zurueck, ob es sich um das neueste Gesuch (egal welcher Status) handelt
	 */
	boolean isNeustesGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Gibt die ID des neuesten Gesuchs fuer ein Dossier und eine Gesuchsperiode zurueck. Dieses kann auch ein
	 * Gesuch sein, fuer welches ich nicht berechtigt bin!
	 */
	@Nonnull
	Optional<String> getIdOfNeuestesGesuchForDossierAndGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier);

	/**
	 * Returns the newest Gesuch for the given Fall. It will return the newest Gesuch for which the user has
	 * read-rights
	 */
	@Nonnull
	Optional<String> getIdOfNeuestesGesuchForDossier(@Nonnull Dossier dossier);

	/**
	 * Gibt das Gesuch zurueck, das mit dem Fall verknuepft ist und das neueste fuer das SchulamtInterface ist. Das
	 * Flag FinSitStatus
	 * muss nicht NULL sein, sonst gilt es als nicht geprueft.
	 */
	Optional<Gesuch> getNeustesGesuchFuerFallnumerForSchulamtInterface(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Long fallnummer);

	/**
	 * Schickt eine E-Mail an alle Gesuchsteller, die ihr Gesuch innerhalb einer konfigurierbaren Frist nach
	 * Erstellung nicht freigegeben haben.
	 * Gibt die Anzahl Warnungen zurueck.
	 */
	int warnGesuchNichtFreigegeben();

	/**
	 * Schickt eine E-Mail an alle Gesuchsteller, die die Freigabequittung innerhalb einer konfigurierbaren Frist nach
	 * Freigabe des Gesuchs nicht geschickt haben.
	 * Gibt die Anzahl Warnungen zurueck.
	 */
	int warnFreigabequittungFehlt();

	/**
	 * Löscht alle Gesuche, die nach einer konfigurierbaren Frist nach Erstellung nicht freigegeben bzw. nach Freigabe
	 * die Quittung nicht geschickt haben. Schickt dem Gesuchsteller eine E-Mail.
	 * Gibt die Anzahl Warnungen zurueck.
	 */
	int deleteGesucheOhneFreigabeOderQuittung();

	/**
	 * Löscht eine Gesuche mit eine neue Transaction
	 * Achtung es muss durch einen anderen EJB angerufen werden, sonst die Transaction parameter ist ignoriert!
	 * @param gesuch
	 * @param typ
	 */
	void removeGesuchAndPersist(Gesuch gesuch,GesuchDeletionCause typ);

	/**
	 * gibt alle Gesuche zurueck die nach einer konfigurierten Frist nach Erstellung nicht freigegeben bzw. nach
	 * Freigabe
	 * die Quittung nicht geschickt haben.
	 */
	List<Gesuch> getGesucheOhneFreigabeOderQuittung();

	/**
	 * Prüft, ob alle Anträge dieser Periode im Status VERFUEGT oder NUR_SCHULAMT sind
	 */
	boolean canGesuchsperiodeBeClosed(@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Sucht die neueste Online Mutation, die zu dem gegebenen Antrag gehoert und loescht sie.
	 * Diese Mutation muss Online und noch nicht freigegeben sein. Diese Methode darf nur bei ADMIN_BG oder SUPER_ADMIN
	 * aufgerufen werden, wegen loescherechten wird es dann immer mir RunAs/SUPER_ADMIN) ausgefuehrt.
	 *
	 * @param dossier Der Antraege, zu denen die Mutation gehoert, die geloescht werden muss
	 * @param gesuchsperiode Gesuchsperiode, in der die Gesuche geloescht werden sollen
	 */
	void removeOnlineMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Sucht die neueste Online Mutation, die zu dem gegebenen Antrag gehoert
	 * Diese Mutation muss Online und noch nicht freigegeben sein. Diese Methode darf nur bei ADMIN_BG oder SUPER_ADMIN
	 * aufgerufen werden, wegen loescherechten wird es dann immer mir RunAs/SUPER_ADMIN) ausgefuehrt.
	 *
	 * @param dossier Der Antraege, zu denen die Mutation gehoert
	 * @param gesuchsperiode Gesuchsperiode
	 */
	Gesuch findOnlineMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Sucht und entfernt ein Folgegesuch fuer den gegebenen Antrag in der gegebenen Gesuchsperiode
	 *
	 * @param dossier Der Antraeg des Falles
	 * @param gesuchsperiode Gesuchsperiode in der das Folgegesuch gesucht werden muss
	 */
	void removeOnlineFolgegesuch(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Loescht ein Gesuch. Einzige Ausnahme: Das Gesuch ist bereits verfuegt.
	 * Nur fuer Superadmin zugelassen
	 */
	void removeAntragForced(@Nonnull Gesuch gesuch);

	/**
	 * Als Gemeinde-Benutzer:
	 * Loescht das angegebene Gesuch falls es sich um ein Papiergesuch handelt, das noch nicht im Status "verfuegen"
	 * oder verfuegt ist. Wenn es sich um ein Papier-Erstgesuch handelt, wird auch der Fall gelöscht.
	 * Als Gesuchsteller:
	 * Loescht das angegebene Gesuch, falls es sich um ein Onlinegesuch handelt, das noch nicht freigegeben wurde. Der
	 * Fall wird dabei nie geloescht.
	 */
	void removeAntrag(@Nonnull Gesuch gesuch);

	/**
	 * Sucht ein Folgegesuch fuer den gegebenen Antrag in der gegebenen Gesuchsperiode
	 *
	 * @param dossier Der Antraeg des Falles
	 * @param gesuchsperiode Gesuchsperiode in der das Folgegesuch gesucht werden muss
	 */
	Gesuch findOnlineFolgegesuch(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Schliesst ein Gesuch, das sich im Status GEPRUEFT befindet und kein Angebot hat
	 * Das Gesuch bekommt den Status KEIN_ANGEBOT und der WizardStep VERFUEGEN den Status OK
	 */
	Gesuch closeWithoutAngebot(@Nonnull Gesuch gesuch);

	/**
	 * Wenn das Gesuch nicht nur Schulangebote hat, wechselt der Status auf VERFUEGEN. Falls es
	 * nur Schulangebote hat, wechselt der Status auf NUR_SCHULAMT, da es keine Verfuegung noetig ist
	 */
	Gesuch verfuegenStarten(@Nonnull Gesuch gesuch);

	/**
	 * Schliesst das Verfuegen ab: Setzt den TimestampVerfuegt und das Gueltig-Flag, bzw. entfernt dieses
	 * beim letzt gueltigen Gesuch
	 */
	void postGesuchVerfuegen(@Nonnull Gesuch gesuch);

	/**
	 * Checks all Betreuungen of the given Gesuch and updates the flag gesuchBetreuungenStatus with the corresponding
	 * value.
	 */
	Gesuch updateBetreuungenStatus(@NotNull Gesuch gesuch);

	@Nonnull
	Optional<Gesuch> getNeuestesGesuchForDossierAndPeriod(
		@Nonnull Dossier dossier,
		@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Setzt den uebergebene FinSitStatus im gegebenen Gesuch
	 *
	 * @return 1 wenn alles ok
	 */
	int changeFinSitStatus(@Nonnull String antragId, @Nonnull FinSitStatus finSitStatus);

	/**
	 * Setzt das Gesuch auf Status PRUEFUNG_STV und aktualisiert die benoetigten Parameter.
	 */
	Gesuch sendGesuchToSTV(@Nonnull Gesuch gesuch, @Nullable String bemerkungen);

	/**
	 * Das Gesuch wird als GEPRUEFT_STV markkiert
	 */
	Gesuch gesuchBySTVFreigeben(@Nonnull Gesuch gesuch);

	/**
	 * Schliesst die Pruefung STV ab und setzt den Status auf den Status, den das Gesuch vor der Pruefung hatte
	 */
	Gesuch stvPruefungAbschliessen(@Nonnull Gesuch gesuch);

	/**
	 * Speichert einen Massenversand in der Datenbank
	 */
	void createMassenversand(@Nonnull Massenversand massenversand);

	/**
	 * Gibt die Texte aller Massenversände zurück, welche zum übergebenen Gesuch verschickt wurden
	 */
	List<String> getMassenversandTexteForGesuch(@Nonnull String gesuchId);

	/**
	 * Returns all Gesuche that have been set as GEPRUEFT (for Papiergesuche) or as FREIGEGEBEN (for Onlinegesuche)
	 * between the given dates and that belongs to the given period.
	 */
	@Nonnull
	List<Gesuch> getGepruefteFreigegebeneGesucheForGesuchsperiode(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull String gesuchsperiodeId);

	/**
	 * Returns true when the fall linked to the Gesuch has another gesuch in a newer Gesuchsperiode.
	 * The status of the Gesuch must be at least freigegeben, because it must be available for Amt-users.
	 * It will check all Gesuche independently if the user is allowed to access them or not though.
	 */
	boolean hasFolgegesuchForAmt(@Nonnull String gesuchId);

	/**
	 * Erstellt eine Mutation und verlangt alle Platzbestaetigungen neu.
	 */
	void createMutationAndAskForPlatzbestaetigung(@Nonnull Gesuch gesuch);

	/**
	 * Findet alle verfügten und gültigen Gesuche von Kindern mit einer ZEMIS Nummer von einer der Lastenausgleichsperioden
	 * haben. Z.B. Lastenausgleich 2020: Gesuche der Periode 2018/19 oder Periode 19/20 werden berücksichtigt
	 */
	List<Gesuch> findGesucheForZemisList(@Nonnull Integer lastenausgleichJahr);
}
