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
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionStatus;

public class Testantrag_LastenausgleichTagesschuleAngabenInstitutionContainer{

	private LastenausgleichTagesschuleAngabenInstitutionContainer container;

	public Testantrag_LastenausgleichTagesschuleAngabenInstitutionContainer(
		LastenausgleichTagesschuleAngabenGemeindeContainer testantrag_lats,
		LastenausgleichTagesschuleAngabenGemeindeStatus status,
		Institution institution) {
		this.container = new LastenausgleichTagesschuleAngabenInstitutionContainer();
		this.container.setAngabenGemeinde(testantrag_lats);
		this.container.setInstitution(institution);
		// set status to offen to calculate deklaration correctly
		this.container.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN);

		this.container.setAngabenDeklaration((new Testantrag_LastenausgleichTagesschuleAngabenInstitution()).getAngabenInstitution());
		// now set correct state and copy to korrektur if necessary
		if(status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE) {
			this.container.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN);
		} else {
			this.container.copyForFreigabe();
			this.container.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT);
		}
	}

	public final LastenausgleichTagesschuleAngabenInstitutionContainer getContainer() {
		return container;
	}
}
