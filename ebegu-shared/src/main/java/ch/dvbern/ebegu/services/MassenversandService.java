/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Massenversand;

public interface MassenversandService {
	/**
	 * Speichert einen Massenversand in der Datenbank
	 */
	void createMassenversand(@Nonnull Massenversand massenversand);

	/**
	 * Gibt die Texte aller Massenversände zurück, welche zum übergebenen Gesuch verschickt wurden
	 */
	List<String> getMassenversandTexteForGesuch(@Nonnull String gesuchId);

	/**
	 * Löscht alle Massenversand-Gesuch Verbindungen für einen Fall
	 */
	void removeMassenversandGesucheForFall(@Nonnull Fall fall);

	/**
	 * Löscht alle Massenversand-Gesuch Verbindungen für einen Fall
	 */
	void removeMassenversandGesucheForGesuch(@Nonnull Gesuch gesuch);
}
