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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.testfaelle.testantraege;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionStatus;

public class Testantrag_LastenausgleichTagesschuleAngabenInstitutionContainer
	extends ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer {

	public Testantrag_LastenausgleichTagesschuleAngabenInstitutionContainer(
		Testantrag_LATS testantrag_lats,
		Institution institution) {
		this.setAngabenGemeinde(testantrag_lats);
		this.setInstitution(institution);
		this.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN);

		this.setAngabenDeklaration(new Testantrag_LastenausgleichTagesschuleAngabenInstitution());
	}
}
