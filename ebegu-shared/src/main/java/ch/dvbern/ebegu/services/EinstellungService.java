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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

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
	@Nonnull
	Einstellung saveEinstellung(@Nonnull Einstellung einstellung);

	/**
	 * Sucht eine Einstellung nach ID
	 */
	@Nonnull
	Optional<Einstellung> findEinstellung(@Nonnull String id);

	/**
	 * Sucht eine Einstellung nach folgendem Schema:
	 * (1) Wenn Einstellung dem gewünschten Key spezifisch für die gewünschte Gemeinde vorhanden ist, wird diese
	 * zurueckgegeben
	 * (2) Wenn nicht, wird geschaut, ob es eine spezifische Einstellung für den Mandanten der gewünschten Gemeinde
	 * gibt
	 * (3) Wenn nicht, wird die allgemeine, systemweite Einstellung zurückgegeben
	 *
	 * @throws NoEinstellungFoundException, wenn auf *keiner* Stufe ein Resultat gefunden wird
	 */
	@Nonnull
	Einstellung findEinstellung(
		@Nonnull EinstellungKey key,
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Suche analog {@link #findEinstellung(EinstellungKey, Gemeinde, Gesuchsperiode)}
	 * Ein externes EntityManager wird uebergeben. Damit vermeiden wir Fehler  ConcurrentModificationException in
	 * hibernate
	 */
	@Nonnull
	Einstellung findEinstellung(
		@Nonnull EinstellungKey key,
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull EntityManager em);

	/**
	 * Gibt alle Einstellungen der uebergebenen Gesuchsperiode zurueck. Es werden die System Defaults zurueckgegeben
	 */
	@Nonnull
	Collection<Einstellung> getAllEinstellungenBySystem(@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Gibt alle Einstellungen der uebergebenen Gesuchsperiode zurueck. Es werden die Defaults des Mandanten
	 * zurückgegeben
	 * (falls vorhanden), sonst die System Defaults.
	 * In der zurueckgegebenen Liste ist jeder EinstellungKey genau einmal vorhanden, jeweils mit dem spezifischsten
	 * Wert fuer
	 * den Mandanten. z.B. Key A als Mandanteinstellung, Key B als System Default, Key C als Mandanteinstellung
	 */
	@Nonnull
	Collection<Einstellung> getAllEinstellungenByMandant(@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Gibt alle Einstellungen der uebergebenen Gesuchsperiode *fuer eine bestimmte Gemeinde* zurueck. D.h. in der
	 * zurueckgegebenen Map ist jeder EinstellungKey genau einmal vorhanden, jeweils mit dem spezifischsten Wert fuer
	 * die Gemeinde. z.B. Key A als Gemeindeeinstellung, Key B als System Default, Key C als Mandanteinstellung
	 */
	@Nonnull
	Map<EinstellungKey, Einstellung> getAllEinstellungenByGemeindeAsMap(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Kopiert alle Einstellungen der alten in die neue Gesuchsperiode. Es werden sowohl Gemeinde-, Mandant-, wie auch
	 * System-Einstellungen kopiert.
	 */
	void copyEinstellungenToNewGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiodeToCreate,
		@Nonnull Gesuchsperiode lastGesuchsperiode);

	/**
	 * Löscht alle Einstellungen der uebergebenen Gesuchsperiode
	 */
	void deleteEinstellungenOfGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Return alle Einstellungen für eine gegebene Key und eventuel Gesuchsperiode
	 * @param key
	 * @return
	 */
	@Nonnull
	List<Einstellung> findEinstellungen(@Nonnull EinstellungKey key, @Nullable Gesuchsperiode gesuchsperiode);

	Map<EinstellungKey, Einstellung> loadRuleParameters(Gemeinde gemeinde, Gesuchsperiode gesuchsperiode, Set<EinstellungKey> keysToLoad);
}
