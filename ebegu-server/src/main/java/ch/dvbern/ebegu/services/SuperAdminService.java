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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;

/**
 * Interface um gewisse Services als SUPER_ADMIN aufrufen zu koennen
 */
public interface SuperAdminService {

	/**
	 * Entfernt ein Gesuch mit allen seinen Objekten. RunAs(SUPER_ADMIN)
	 */
	void removeGesuch(@Nonnull String gesuchId);

	/**
	 * Entfernt ein Dossier mit allen seinen Gesuchen. RunAs(SUPER_ADMIN)
	 */
	void removeDossier(@Nonnull String dossierId);

	/**
	 * Entfernt einen Fall mitsamt seinen Gesuchen, falls er existiert. RunAs(SUPER_ADMIN)
	 */
	void removeFallIfExists(@Nonnull String fallId);

	/**
	 * Entfernt einen Fall mitsamt seinen Gesuchen. RunAs(SUPER_ADMIN)
	 */
	void removeFall(@Nonnull Fall fall);

	/**
	 * Speichert das Gesuch und speichert den Statuswechsel in der History falls saveInStatusHistory
	 * gesetzt ist.
	 */
	@Nonnull
	Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory, Benutzer saveAsUser);

	/**
	 * Löscht einen Benutzer von der System. Der aktuelle User muss mitgegeben werden, da der Superadmin-Service
	 * im @RunAs Modus laeuft und die Information über den tatsächlich eingeloggten Benutzer verloren geht
	 */
	void removeFallAndBenutzer(@Nonnull String benutzernameToRemove, @Nonnull Benutzer eingeloggterBenutzer);

	/**
	 * Macht eine Massenmutation aller geschlossenen Gesuche der uebergebenen Gemeinde fuer die uebergebene
	 * Gesuchsperiode. Es werden neue Platzbestaetigungen fuer alle Kita/Tfo-Angebote angefordert.
	 * Sendet ein Protokoll im CSV Format an eine konfigurierbare Mailadresse
	 */
	void createMutationForEachClosedAntragOfGemeinde(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);
}
