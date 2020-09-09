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
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;

/**
 * Service zum Verwalten von Zahlungen
 */
public interface ZahlungService {

	/**
	 * Ermittelt alle im aktuellen Monat gueltigen Verfuegungen, sowie aller seit dem letzten Auftrag eingeganegenen
	 * Mutationen.
	 * Der Zahlungsauftrag hat den initialen Status ENTWURF
	 * Als datumGeneriert wird "Jetzt" verwendet
	 */
	@Nonnull
	Zahlungsauftrag zahlungsauftragErstellen(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung);

	/**
	 * Ermittelt alle im aktuellen Monat gueltigen Verfuegungen, sowie aller seit dem letzten Auftrag eingeganegenen
	 * Mutationen.
	 * Der Zahlungsauftrag hat den initialen Status ENTWURF
	 */
	@Nonnull
	Zahlungsauftrag zahlungsauftragErstellen(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung,
		@Nonnull LocalDateTime datumGeneriert);

	/**
	 * Aktualisiert das Fälligkeitsdatum und die Beschreibung im übergebenen Auftrag. Die Zahlungspositionen werden
	 * *nicht* neu generiert
	 */
	@Nonnull
	Zahlungsauftrag zahlungsauftragAktualisieren(
		@Nonnull String auftragId,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung);

	/**
	 * Nachdem alle Daten kontrolliert wurden, wird der Zahlungsauftrag ausgeloest. Danach kann er nicht mehr
	 * geloescht werden
	 */
	@Nonnull
	Zahlungsauftrag zahlungsauftragAusloesen(@Nonnull String auftragId);

	/**
	 * Sucht einen einzelnen Zahlungsauftrag.
	 */
	@Nonnull
	Optional<Zahlungsauftrag> findZahlungsauftrag(@Nonnull String auftragId);

	/**
	 * Gibt die Zahlung mit der uebergebenen Id zurueck.
	 */
	@Nonnull
	Optional<Zahlung> findZahlung(@Nonnull String zahlungId);

	/**
	 * Loescht ALLE Zahlungsauftraege
	 */
	void deleteAllZahlungsauftraege();

	/**
	 * Gibt alle Zahlungsauftraege zurueck
	 */
	@Nonnull
	Collection<Zahlungsauftrag> getAllZahlungsauftraege();

	/**
	 * Eine Kita kann/muss den Zahlungseingang bestaetigen
	 */
	@Nonnull
	Zahlung zahlungBestaetigen(@Nonnull String zahlungId);

	/**
	 * Gibt alle Zahlungsaufträge des übergebenen Zeitraums zurück. Es werden nur Zahlungsaufträge aufgefuehrt, fuer die der eingeloggte Benutzer berechtigt
	 * ist (d.h. für die Gemeinde).
	 */
	@Nonnull
	Collection<Zahlungsauftrag> getZahlungsauftraegeInPeriode(@Nonnull LocalDate von, @Nonnull LocalDate bis);

	/**
	 * Entfernt alle Zahlungspositionen des übergebenen Gesuchs
	 */
	void deleteZahlungspositionenOfGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Kontrolliert die Zahlungen Stand heute: Es werden die Zahlen aus der letzt gueltigen Verfuegung jedes Falls
	 * verglichen mit den tatsaechlich erfolgten Zahlungen.
	 */
	void zahlungenKontrollieren(@Nonnull ZahlungslaufTyp zahlungslaufTyp, @Nonnull String gemeindeId);
}
