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

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;

public class Testantrag_FB {

	private final FerienbetreuungAngabenContainer container;

	public Testantrag_FB(
		Gesuchsperiode gesuchsperiode,
		Gemeinde gemeinde,
		FerienbetreuungAngabenStatus status) {
		this.container = new FerienbetreuungAngabenContainer();

		this.container.setGemeinde(gemeinde);
		this.container.setGesuchsperiode(gesuchsperiode);
		this.container.setStatus(FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE);

		this.container.setAngabenDeklaration(
			(new Testantrag_FerienbetreuungAngaben(status)).getAngaben()
		);

		if(status == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON) {
			this.container.copyForFreigabe();
			this.container.setStatus(FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON);
		}

	}

	public FerienbetreuungAngabenContainer getContainer() {
		return container;
	}
}
