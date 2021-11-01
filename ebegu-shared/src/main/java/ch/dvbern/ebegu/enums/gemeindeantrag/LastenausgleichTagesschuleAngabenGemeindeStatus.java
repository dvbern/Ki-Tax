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

package ch.dvbern.ebegu.enums.gemeindeantrag;

public enum LastenausgleichTagesschuleAngabenGemeindeStatus {

	NEU, // Bis die erste Frage nach "alle Anmeldungen in kiBon" beantwortet ist
	IN_BEARBEITUNG_GEMEINDE,
	IN_PRUEFUNG_KANTON,
	ZWEITPRUEFUNG, // Zufaellig ausgewaehlte werden zur Zweitpruefung gesetzt
	GEPRUEFT,
	ABGESCHLOSSEN; // Ausbezahlt

	public boolean atLeastGeprueft() {
		return this.equals(GEPRUEFT) || this.equals(ABGESCHLOSSEN);
	}

	public boolean atLeastInPruefungKanton() {
		return !this.equals(NEU) && !this.equals(IN_BEARBEITUNG_GEMEINDE);
	}
}
