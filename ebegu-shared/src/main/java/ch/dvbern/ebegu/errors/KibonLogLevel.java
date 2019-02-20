/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.errors;

/**
 * LogLevel, welches beim Werfen einer EbeguRuntimeException definiert werden kann.
 * Der AbstractEbeguExceptionMapper loggt dann den Fehler im der entsprechenden Level. Wenn kein Level definiert wird,
 * wird WARN verwendet, es kann aber auch explizit NONE definiert werden, z.B. für Validierungsfehler
 */
public enum KibonLogLevel {

	ERROR,
	WARN,
	INFO,
	DEBUG,
	NONE

}
