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

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;

public class Testantrag_FerienbetreuungAngabenKostenEinnahmen {

	private FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen;

	public Testantrag_FerienbetreuungAngabenKostenEinnahmen(FerienbetreuungAngabenStatus status) {
		this.kostenEinnahmen = new FerienbetreuungAngabenKostenEinnahmen();

		this.kostenEinnahmen.setPersonalkosten(new BigDecimal(5000));
		this.kostenEinnahmen.setPersonalkostenLeitungAdmin(new BigDecimal(1000));
		this.kostenEinnahmen.setSachkosten(new BigDecimal(8000));
		this.kostenEinnahmen.setVerpflegungskosten(new BigDecimal(250));
		this.kostenEinnahmen.setWeitereKosten(new BigDecimal(250));
		this.kostenEinnahmen.setElterngebuehren(new BigDecimal(8000));
		this.kostenEinnahmen.setWeitereEinnahmen(new BigDecimal(2000));

		if(status == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE) {
			this.kostenEinnahmen.setStatus(FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE);
		} else {
			this.kostenEinnahmen.setStatus(FerienbetreuungFormularStatus.IN_PRUEFUNG_KANTON);
		}
	}

	public FerienbetreuungAngabenKostenEinnahmen getKostenEinnahmen() {
		return kostenEinnahmen;
	}
}
