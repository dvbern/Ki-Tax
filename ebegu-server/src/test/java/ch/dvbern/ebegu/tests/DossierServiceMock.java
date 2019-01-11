/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.services.DossierServiceBean;
import ch.dvbern.ebegu.util.Constants;

public class DossierServiceMock extends DossierServiceBean {

	@Override
	public LocalDate getErstesEinreichungsdatum(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		// return START_OF_TIME so this value will have no influence on the calculations
		return Constants.START_OF_TIME;
	}
}
