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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.MergeDocException;

/**
 * Service zum Verwalten von Fallen
 */
public interface FallService {

	/**
	 * Erstellt einen neuen Fall in der DB, falls der key noch nicht existiert. Sollte es existieren, aktualisiert es
	 * den Inhalt
	 *
	 * @param fall der Fall als DTO
	 * @return den gespeicherten Fall
	 */
	@Nonnull
	Fall saveFall(@Nonnull Fall fall);

	/**
	 * @param key PK (id) des Falles
	 * @return Fall mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Fall> findFall(@Nonnull String key);

	/**
	 * Gibt den Fall mit der angegebenen Fall-Nummer zurueck
	 */
	@Nonnull
	Optional<Fall> findFallByNumber(@Nonnull Long fallnummer);

	/**
	 * Gibt den Fall des eingeloggten Benutzers zurueck
	 */
	@Nonnull
	Optional<Fall> findFallByCurrentBenutzerAsBesitzer();

	/**
	 * Gibt den Fall zurueck der zum eingeloggten Benutzer gehoert oder ein leeres optional wenn keiner vorhanden
	 */
	@Nonnull
	Optional<Fall> findFallByBesitzer(@Nullable Benutzer benutzer);

	/**
	 * Gibt alle existierenden Faelle zurueck.
	 *
	 * @param doAuthCheck: Definiert, ob die Berechtigungen (Lesen/Schreiben) für alle Faelle geprüft werden muessen.
	 * Falls spaeter sowieso nur IDs (der Gesuche) verwendet werden, kann der Check weggelassen werden.
	 * @return Liste aller Faelle aus der DB
	 */
	@Nonnull
	Collection<Fall> getAllFalle(boolean doAuthCheck);

	/**
	 * Entfernt einen Fall aus der Datenbank, falls er dort existiert.
	 */
	void removeFallIfExists(@Nonnull String fallId, @Nonnull GesuchDeletionCause deletionCause);

	/**
	 * entfernt einen Fall aus der Database
	 */
	void removeFall(@Nonnull Fall fall, @Nonnull GesuchDeletionCause deletionCause);

	/**
	 * Erstellt einen neuen Fall fuer den aktuellen Benutzer und setzt diesen als Besitzer des Falles.
	 * - Nur wenn der aktuellen Benutzer ein GESUCHSTELLER ist und noch keinen Fall zugeordnet hat
	 * - In allen anderen Fällen ein Optional.empty() wird zurueckgegeben
	 */
	@Nonnull
	Optional<Fall> createFallForCurrentGesuchstellerAsBesitzer();

	/**
	 * Gibt die GS1-Emailadresse des neusten Gesuchs fuer diesen Fall zurueck, wenn noch kein Gesuch vorhanden ist,
	 * wird
	 * die E-Mail zurueckgegeben die beim Besitzer des Falls eingegeben wurde (aus IAM importiert)
	 */
	@Nonnull
	Optional<String> getCurrentEmailAddress(@Nonnull String fallID);


	byte[] generateVollmachtDokument(@Nonnull String fallId, @Nonnull Sprache sprache) throws MergeDocException;
}
