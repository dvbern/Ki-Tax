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

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;

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
	Zahlungsauftrag zahlungsauftragErstellen(@Nonnull String gemeindeId, @Nonnull LocalDate datumFaelligkeit, @Nonnull String beschreibung);

	/**
	 * Aktualisiert das Fälligkeitsdatum und die Beschreibung im übergebenen Auftrag. Die Zahlungspositionen werden
	 * *nicht* neu generiert
	 */
	Zahlungsauftrag zahlungsauftragAktualisieren(String auftragId, LocalDate datumFaelligkeit, String beschreibung);

	/**
	 * Ermittelt alle im aktuellen Monat gueltigen Verfuegungen, sowie aller seit dem letzten Auftrag eingeganegenen
	 * Mutationen.
	 * Der Zahlungsauftrag hat den initialen Status ENTWURF
	 */
	Zahlungsauftrag zahlungsauftragErstellen(@Nonnull String gemeindeId, @Nonnull LocalDate datumFaelligkeit, @Nonnull String beschreibung,
		@Nonnull LocalDateTime datumGeneriert);

	/**
	 * Nachdem alle Daten kontrolliert wurden, wird der Zahlungsauftrag ausgeloest. Danach kann er nicht mehr
	 * geloescht werden
	 */
	Zahlungsauftrag zahlungsauftragAusloesen(String auftragId);

	/**
	 * Sucht einen einzelnen Zahlungsauftrag.
	 */
	Optional<Zahlungsauftrag> findZahlungsauftrag(String auftragId);

	/**
	 * Gibt die Zahlung mit der uebergebenen Id zurueck.
	 */
	Optional<Zahlung> findZahlung(String zahlungId);

	/**
	 * Loescht ALLE Zahlungsauftraege
	 */
	void deleteAllZahlungsauftraege();

	/**
	 * Gibt alle Zahlungsauftraege zurueck
	 */
	Collection<Zahlungsauftrag> getAllZahlungsauftraege();

	/**
	 * Eine Kita kann/muss den Zahlungseingang bestaetigen
	 */
	Zahlung zahlungBestaetigen(String zahlungId);

	/**
	 * Gibt alle Zahlungsaufträge des übergebenen Zeitraums zurück. Es werden nur Zahlungsaufträge aufgefuehrt, fuer die der eingeloggte Benutzer berechtigt
	 * ist (d.h. für die Gemeinde).
	 */
	Collection<Zahlungsauftrag> getZahlungsauftraegeInPeriode(LocalDate von, @Nonnull LocalDate bis);

	/**
	 * Entfernt alle Zahlungspositionen des übergebenen Gesuchs
	 */
	void deleteZahlungspositionenOfGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Kontrolliert die Zahlungen Stand heute: Es werden die Zahlen aus der letzt gueltigen Verfuegung jedes Falls
	 * verglichen mit den tatsaechlich erfolgten Zahlungen.
	 */
	void zahlungenKontrollieren(@Nonnull String gemeindeId);
}
