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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

/**
 * Service zum berechnen und speichern der Verfuegung
 */
public interface VerfuegungService {

	/**
	 * Speichert die Verfuegung neu in der DB falls der Key noch nicht existiert.
	 * Die Betreuung erhaelt den Status VERFUEGT
	 *
	 * @param gesuchId ID des Gesuchs, zu welcher die Verfügung gehört
	 * @param betreuungId ID der Betreuung, welche verfügt werden soll
	 * @param manuelleBemerkungen, user-provided Bemerkung zur Verfügung
	 * @param ignorieren true wenn die ausbezahlten Zeitabschnitte nicht neu berechnet werden muessen
	 * @param sendEmail true wenn eine Info EMail versendet werden soll
	 */
	@Nonnull
	Verfuegung verfuegen(@Nonnull String gesuchId, @Nonnull String betreuungId, @Nullable String manuelleBemerkungen, boolean ignorieren, boolean sendEmail);

	/**
	 * Speichert die Verfuegung neu in der DB falls der Key noch nicht existiert.
	 * Die Betreuung erhaelt den Status NICHT_EINGETRETEN
	 *
	 * @param gesuchId ID des Gesuchs, zu welcher die Verfügung gehört
	 * @param betreuungId ID der Betreuung, welche verfügt werden soll
	 */
	@Nonnull
	Verfuegung nichtEintreten(@Nonnull String gesuchId, @Nonnull String betreuungId);

	/**
	 * @param id PK (id) der Verfuegung
	 * @return Verfuegung mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Verfuegung> findVerfuegung(@Nonnull String id);

	/**
	 * @return Liste aller Verfuegung aus der DB
	 */
	@Nonnull
	Collection<Verfuegung> getAllVerfuegungen();

	/**
	 * Berechnet die Verfuegung fuer ein Gesuch
	 *
	 * @return gibt die Betreuung mit der berechneten angehangten Verfuegung zurueck
	 */
	@Nonnull
	Gesuch calculateVerfuegung(@Nonnull Gesuch gesuch);

	/**
	 * Berechnet alle Verfügungen fuer ein Gesuch und gibt *die erste* zurück
	 */
	@Nonnull
	Verfuegung getEvaluateFamiliensituationVerfuegung(@Nonnull Gesuch gesuch);

	/**
	 * Initialises für jede Betreuung die transienten Felder hinter
	 * {@link Betreuung#getVorgaengerAusbezahlteVerfuegung()} und {@link Betreuung#getVorgaengerVerfuegung()}
	 */
	void initializeVorgaengerVerfuegungen(@Nonnull Gesuch gesuch);

	/**
	 * genau wie findVorgaengerVerfuegung gibt aber nur deren TimestampErstellt zurueck wenn vorhanden
	 */
	Optional<LocalDate> findVorgaengerVerfuegungDate(@Nonnull Betreuung betreuung);

	/**
	 * Sucht den Zeitabschnitt / die Zeitabschnitte mit demselben Zeitraum auf der Vorgängerverfügung,
	 * und die verrechnet oder ignoriert sind. Rekursive Methode, die die gegebene Liste mit den richtigen Objekten
	 * ausfuellt
	 */
	void findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(
		@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Betreuung betreuungNeu,
		@Nonnull List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte);

	/**
	 * Returns all Zeitabschnitte within the given year that are gueltig
	 */
	@Nonnull
	List<VerfuegungZeitabschnitt> findZeitabschnitteByYear(int year);
}
