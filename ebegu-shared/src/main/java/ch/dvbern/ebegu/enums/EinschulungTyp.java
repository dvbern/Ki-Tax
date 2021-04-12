/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

package ch.dvbern.ebegu.enums;

/**
 * Enum fuer Feld EinschulungTyp
 */
public enum EinschulungTyp {

	VORSCHULALTER,
	KINDERGARTEN1,
	KINDERGARTEN2,
	KLASSE1,
	KLASSE2,
	KLASSE3,
	KLASSE4,
	KLASSE5,
	KLASSE6,
	KLASSE7,
	KLASSE8,
	KLASSE9;

	public boolean isEingeschult() {
		return this != VORSCHULALTER;
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
}
