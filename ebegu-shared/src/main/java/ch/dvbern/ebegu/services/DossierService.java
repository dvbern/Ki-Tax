/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;

/**
 * Service zum Verwalten von Dossiers
 */
public interface DossierService {

	/**
	 * Gibt das Dossier mit der uebergebenen ID zurueck.
	 */
	@Nonnull
	Optional<Dossier> findDossier(@Nonnull String id);

	/**
	 * Gibt das Dossier mit der uebergebenen ID zurueck.
	 * @param doAuthCheck: Definiert, ob die Berechtigungen (Lesen/Schreiben) für dieses Gesuch geprüft werden muessen.
	 */
	@Nonnull
	Optional<Dossier> findDossier(@Nonnull String id, boolean doAuthCheck);


	/**
	 * Gibt eine Liste aller Dossiers des uebergebenen Falls zurück.
	 */
	@Nonnull
	Collection<Dossier> findDossiersByFall(@Nonnull String fallId);

	/**
	 * Gibt eine Liste aller Dossiers der uebergebenen Gemeinde zurück.
	 */
	@Nonnull
	Collection<Dossier> findDossiersByGemeinde(@Nonnull Gemeinde gemeinde);

	/**
	 * Gibt das Dossier (falls vorhanden) fuer die uebergebene Gemeinde und den uebergebenen Fall zurueck
	 */
	@Nonnull
	Optional<Dossier> findDossierByGemeindeAndFall(@Nonnull String gemeindeId, @Nonnull String fallId);

	/**
	 * Speichert ein Dossier bzw. erstellt es wenn es noch nicht existiert.
	 */
	@Nonnull
	Dossier saveDossier(@Nonnull Dossier dossier);

	/**
	 * entfernt ein Dossier aus der Database. Es wird ein LogEintrag erstellt mit dem Grund des Löschens-
	 */
	void removeDossier(@Nonnull String dossierId, @Nonnull GesuchDeletionCause deletionCause);

	/**
	 * Gibt alle existierenden Dossiers zurueck.
	 *
	 * @param doAuthCheck: Definiert, ob die Berechtigungen (Lesen/Schreiben) für alle Dossiers geprüft werden muessen.
	 * @return Liste aller Dossiers aus der DB
	 */
	@Nonnull
	Collection<Dossier> getAllDossiers(boolean doAuthCheck);

	/**
	 * Erstellt ein Dossier und einen Fall (beides, falls noch nicht vorhanden) fuer den eingeloggten
	 * Benutzer als GS (Besitzer) fuer die uebergebene Gemeinde
	 */
	@Nonnull
	Dossier getOrCreateDossierAndFallForCurrentUserAsBesitzer(@Nonnull String gemeindeId);

	/**
	 * Checks whether the given Dossier has at least one Mitteilung or not. Will throw an exception if the dossier is
	 * not found.
	 */
	boolean hasDossierAnyMitteilung(@NotNull Dossier dossier);

	/**
	 * Setzt den BG-Verantwortlichen auf dem Dossier
	 */
	@Nonnull
	Dossier setVerantwortlicherBG(@Nonnull String dossierId, @Nullable Benutzer benutzer);

	/**
	 * Setzt den TS-Verantwortlichen auf dem Dossier
	 */
	@Nonnull
	Dossier setVerantwortlicherTS(@Nonnull String dossierId, @Nullable Benutzer benutzer);

	/**
	 * Gibt das früheste Einreichungsdatum (oder RegelStartDatum) zurück, welches für dieses Dossier
	 * in dieser Gesuchsperiode eingereicht wurde.
	 * Falls noch gar kein Gesuch eingereicht wurde, wird das Tagesdatum zurückgegeben
	 */
	@Nonnull
	LocalDate getErstesEinreichungsdatum(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode);
}
