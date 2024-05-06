/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.enums;

import java.util.List;

/**
 * Enum fuer Feld EinschulungTyp
 */
public enum EinschulungTyp {

	VORSCHULALTER(0),
	KINDERGARTEN1(1),
	FREIWILLIGER_KINDERGARTEN(1),
	KINDERGARTEN2(2),
	OBLIGATORISCHER_KINDERGARTEN(2),
	PRIMARSTUFE(3),
	PRIMAR_SEKUNDAR_STUFE(3),
	KLASSE1(3),
	SEKUNDAR_UND_HOEHER_STUFE(4),
	KLASSE2(4),
	KLASSE3(5),
	KLASSE4(6),
	KLASSE5(7),
	KLASSE6(8),
	KLASSE7(9),
	KLASSE8(10),
	KLASSE9(11);

	private final int ordinalitaet;

	EinschulungTyp(int ordinalitaet) {
		this.ordinalitaet = ordinalitaet;
	}

	public boolean isEingeschult() {
		return this != VORSCHULALTER && this != FREIWILLIGER_KINDERGARTEN;
	}

	// oh mann...
	public boolean isEingeschultAppenzell() {
		return this.getOrdinalitaet() >= 1;
	}

	public boolean isKindergarten() {
		return this == KINDERGARTEN1
			|| this == KINDERGARTEN2;
	}

	public boolean isPrimarstufe() {
		return this == KLASSE1
			|| this == KLASSE2
			|| this == KLASSE3
			|| this == KLASSE4
			|| this == KLASSE5
			|| this == KLASSE6;
	}

	public boolean isSekundarstufe() {
		return this == KLASSE7
			|| this == KLASSE8
			|| this == KLASSE9;
	}

	public int getOrdinalitaet() {
		return ordinalitaet;
	}
}
