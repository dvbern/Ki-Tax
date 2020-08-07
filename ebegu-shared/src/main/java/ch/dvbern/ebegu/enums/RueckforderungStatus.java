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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.enums;

public enum RueckforderungStatus {
	NEU,
	EINGELADEN,
	IN_BEARBEITUNG_INSTITUTION_STUFE_1,
	IN_PRUEFUNG_KANTON_STUFE_1,
	GEPRUEFT_STUFE_1,
	IN_BEARBEITUNG_INSTITUTION_STUFE_2,
	VERFUEGT_PROVISORISCH,
	IN_PRUEFUNG_KANTON_STUFE_2,
	VERFUEGT,
	ABGESCHLOSSEN_OHNE_GESUCH;

	public static boolean isStatusForInstitutionAuthorized(RueckforderungStatus status) {
		return status == RueckforderungStatus.NEU ||
			status == RueckforderungStatus.EINGELADEN ||
			status == RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1 ||
			status == RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1 ||
			status == RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2 ||
			status == RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2 ||
			status == RueckforderungStatus.ABGESCHLOSSEN_OHNE_GESUCH;
	}

	public static boolean isStatusForKantonAuthorized(RueckforderungStatus status) {
		return status == RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1 ||
			status == RueckforderungStatus.GEPRUEFT_STUFE_1 ||
			status == RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2 ||
			status == RueckforderungStatus.VERFUEGT ||
			status == RueckforderungStatus.VERFUEGT_PROVISORISCH;
	}
}


