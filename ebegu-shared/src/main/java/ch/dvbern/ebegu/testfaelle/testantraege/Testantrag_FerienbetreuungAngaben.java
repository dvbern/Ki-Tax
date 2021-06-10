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

import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngaben;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;

public class Testantrag_FerienbetreuungAngaben {

	private FerienbetreuungAngaben angaben;

	public Testantrag_FerienbetreuungAngaben(
		FerienbetreuungAngabenStatus status) {
		this.angaben = new FerienbetreuungAngaben();

		this.angaben.setFerienbetreuungAngabenAngebot(
			(new Testantrag_FerienbetreuungAngabenAngebot(status)).getAngebot()
		);
		this.angaben.setFerienbetreuungAngabenNutzung(
			(new Testantrag_FerienbetreuungAngabenNutzung(status)).getNutzung()
		);
		this.angaben.setFerienbetreuungAngabenKostenEinnahmen(
			(new Testantrag_FerienbetreuungAngabenKostenEinnahmen(status)).getKostenEinnahmen()
		);
		this.angaben.setFerienbetreuungAngabenStammdaten(
			(new Testantrag_FerienbetreuungAngabenStammdaten(status)).getStammdaten()
		);
	}

	public FerienbetreuungAngaben getAngaben() {
		return angaben;
	}
}
