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

package ch.dvbern.ebegu.enums;

/**
 * Enum fuer die Kategorien der Verechnung kiBon
 */
public enum VerrechnungKibonKategorie {
	BG, 			// BG plus eventuell weitere Angebote, *ausser* TS
	TS, 			// TS plus eventuell weitere Angebote, *ausser* BG
	BG_TS, 			// Mischgesuche BG und TS, plus eventuell weitere Angebote
	KEIN_ANGEBOT, 	// Gar kein Angebot
	FI, 			// Weder BG noch TS, nur FI
	TAGI, 			// Weder BG noch TS, nur TAGI
	FI_TAGI 		// Weder BG noch TS, FI und TAGI
}
