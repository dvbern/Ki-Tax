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
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.Sprache;

/**
 * Service zum Verwalten von Gesuchsperiode
 */
public interface GesuchsperiodeService {

	/**
	 * Erstellt eine neue Gesuchsperiode in der DB, falls der key noch nicht existiert
	 */
	@Nonnull
	Gesuchsperiode saveGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Erstellt eine neue Gesuchsperiode in der DB, falls der key noch nicht existiert.
	 * Aufgrund des letzten Status wird geprüft, ob der Statusübergang zulässig ist und ob
	 * evt. weitere Aktionen durchgeführt werden müssen (z.B. E-Mails etc.)
	 */
	@Nonnull
	Gesuchsperiode saveGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull GesuchsperiodeStatus statusBisher);

	/**
	 * @param key PK (id) der Gesuchsperiode
	 * @return Gesuchsperiode mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Gesuchsperiode> findGesuchsperiode(@Nonnull String key);

	/**
	 * Gibt alle existierenden Gesuchsperioden zurueck.
	 *
	 * @return Liste aller Gesuchsperiodeen aus der DB
	 */
	@Nonnull
	Collection<Gesuchsperiode> getAllGesuchsperioden();

	/**
	 * @param key PK (id) der Gesuchsperiode
	 * @return Diese und alle zukünftigen Gesuchsperioden
	 */
	Collection<Gesuchsperiode> findThisAndFutureGesuchsperioden(@Nonnull String key);

	/**
	 * Loescht alle Gesuchsperioden inkl. Gesuche und Dokumente, wenn die Gesuchsperiode mehr als 10 Jahre alt ist.
	 */
	void removeGesuchsperiode(@Nonnull String gesuchsPeriodeId);

	/**
	 * Gibt alle aktiven Gesuchsperioden zurueck.
	 *
	 * @return Liste aller Gesuchsperiodeen aus der DB
	 */
	@Nonnull
	Collection<Gesuchsperiode> getAllActiveGesuchsperioden();

	/**
	 * Gibt alle Gesuchsperioden zurueck, deren Status nicht Geschlossen ist.
	 */
	@Nonnull
	Collection<Gesuchsperiode> getAllNichtAbgeschlosseneGesuchsperioden();

	/**
	 * Gibt alle Gesuchsperioden zurueck, deren Status Aktiv oder Inaktiv ist.
	 */
	@Nonnull
	Collection<Gesuchsperiode> getAllAktivUndInaktivGesuchsperioden();

	/**
	 * Gibt alle Gesuchsperioden zurueck, die Aktiv oder Inaktiv und nicht Entwurf sind, und für die
	 * das angegebene
	 * Dossier noch kein Gesuch freigegeben hat.
	 */
	@Nonnull
	Collection<Gesuchsperiode> getAllAktivInaktivNichtVerwendeteGesuchsperioden(@Nonnull String dossierId);

	/**
	 * Gibt alle aktiven Gesuchsperioden zurueck, deren Ende-Datum noch nicht erreicht ist, und für die das angegebene
	 * Dossier noch kein Gesuch freigegeben hat.
	 */
	@Nonnull
	Collection<Gesuchsperiode> getAllAktiveNichtVerwendeteGesuchsperioden(@Nonnull String dossierId);

	/**
	 * Gibt die Gesuchsperiode zurueck, welche am uebergebenen Stichtag aktuell war/ist
	 */
	@Nonnull
	Optional<Gesuchsperiode> getGesuchsperiodeAm(@Nonnull LocalDate stichtag, @Nonnull Mandant mandant);

	/**
	 * Gibt alle Gesuchsperioden zurueck, welche im angegebenen Zeitraum liegen (nicht zwingend vollständig)
	 */
	@Nonnull
	Collection<Gesuchsperiode> getGesuchsperiodenBetween(@Nonnull LocalDate datumVon, @Nonnull LocalDate datumBis);

	/**
	 * Gibt die neuste Gesuchsperiode zurueck anhand des Datums gueltigBis.
	 */
	@Nonnull
	Optional<Gesuchsperiode> findNewestGesuchsperiode();

	/**
	 * Fügt eine Erläuterung zur Verfügung einer Gesuchsperiode abhängig der Sprache an.
	 */
	@Nonnull
	Gesuchsperiode uploadGesuchsperiodeDokument(
		@Nonnull String gesuchsperiodeId,
		@Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp,
		@Nonnull byte[] content);

	/**
	 * Löscht eine Erläuterung zur Verfügung einer Gesuchsperiode abhängig der Sprache.
	 */
	@Nonnull
	Gesuchsperiode removeGesuchsperiodeDokument(@Nonnull String gesuchsperiodeId, @Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp);

	/**
	 * retuns true id the VerfuegungErlaeuterung exists for the given language
	 */
	boolean existDokument(@Nonnull String gesuchsperiodeId, @Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp);

	byte[] downloadGesuchsperiodeDokument(@Nonnull String gesuchsperiodeId, @Nonnull Sprache sprache,
		@Nonnull DokumentTyp dokumentTyp);
}
