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

package ch.dvbern.ebegu.services.gemeindeantrag;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service fuer den Lastenausgleich der Tagesschulen
 */
public interface LastenausgleichTagesschuleAngabenGemeindeService {

	/**
	 * Erstellt fuer jede Gemeinde in der übergebenen Liste einen LastenausgleichTagesschule fuer die angegebene Periode
	 */
	@Nonnull
	List<? extends GemeindeAntrag> createLastenausgleichTagesschuleGemeinde(
			@Nonnull Gesuchsperiode gesuchsperiode,
			@Nonnull List<Gemeinde> gemeindeList);

	/**
	 * Sucht den LastenausgleichTagesschuleAngabenGemeindeContainer mit der uebergebenen ID
	 */
	@Nonnull
	Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull String id);

	/**
	 * Sucht den LastenausgleichTagesschuleAngabenGemeindeContainer mit der uebergebenen gemeinde und gesuchsperiode
	 */
	@Nonnull
	Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Sucht den LastenausgleichTagesschuleAngabenGemeindeContainer mit der uebergebenen gemeinde und gesuchsperiode
	 * und entfernt ihn
	 */
	@Nonnull
	void deleteAntragIfExists(
		@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Sucht den LastenausgleichTagesschuleAngabenGemeindeContainer mit der uebergebenen gemeinde und gesuchsperiode
	 * und entfernt ihn
	 */
	@Nonnull
	void deleteAntragIfExistsAndIsNotAbgeschlossen(
		@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Speichert den LastenausgleichTagesschule, ohne Eintrag in die StatusHistory-Tabelle
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	/**
	 * Gibt den LastenausgleichTagesschuleAngabenGemeindeContainer frei fuer die Bearbeitung durch die Institutionen.
	 * Der Status wird von OFFEN auf IN_BEARBEITUNG_GEMEINDE gesetzt.
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	/**
	 * Reicht den Lastenausgleich ein, inkl. kopieren der Daten vom Korrektur- in den Deklarations-Container,
	 * falls die Vorbedingungen dazu erfuellt sind.
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeEinreichen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	/**
	 * Gibt alle Lastenausgleiche der Tagesschulen für die Benutzerin zurück
	 * @return
	 * @param gesuchPeriodeId
	 */
	@Nonnull
	List<LastenausgleichTagesschuleAngabenGemeindeContainer> getAllLastenausgleicheTagesschulen(String gesuchPeriodeId);

	/**
	 * Gibt die gefilterten Lastenausgleiche der Tagesschulen für die Benutzerin zurück
	 * @return
	 */
	@Nonnull
	List<LastenausgleichTagesschuleAngabenGemeindeContainer> getLastenausgleicheTagesschulen(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String status,
		@Nullable String timestampMutiert,
		@Nullable Benutzer verantwortlicher
	);

	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindePruefen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	/**
	 * Speichert interne Kommentare in einem LastenausgleichTagesschuleAngabeGemeindeContainer
	 */
	void saveKommentar(
		@Nonnull String containerId,
		@Nonnull String kommentar
	);

	void saveVerantwortlicher(
		@Nonnull String containerId,
		@Nullable String username
	);

	/**
	 * Schliesst das Angaben Gemeinde Formular ab
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFormularAbschliessen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeWiederOeffnen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	void deleteLastenausgleicheTagesschuleForGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode);

	@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeZurueckAnGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container);

	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeZurueckInPruefungKanton(
			@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container);

	@Nonnull
	boolean selectedForZweitpruefung(@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container);

	@Nullable
	LastenausgleichTagesschuleAngabenGemeindeContainer findContainerOfPreviousPeriode(@Nonnull String currentAntragId);

	@Nullable Number calculateErwarteteBetreuungsstunden(String containerId);
	@Nullable Number calculateErwarteteBetreuungsstundenPrognose(String containerId);

	void savePrognose(@Nonnull String containerId, @Nonnull BigDecimal prognose, @Nullable String bemerkungen);

	@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeAbschliessen(
			@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container
	);
}
