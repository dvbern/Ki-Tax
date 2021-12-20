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

package ch.dvbern.ebegu.testfaelle;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.util.MandantConstants;

public final class TestfallDependenciesFactory {

	public static InstitutionStammdatenBuilder getInstitutionsStammdatenBuilder(
			InstitutionStammdatenService institutionStammdatenService,
			Mandant mandant) {
		switch (mandant.getId()) {
		case MandantConstants.MANDANT_BE: {
			return new InstitutionStammdatenBuilderBe(institutionStammdatenService);
		}
		case MandantConstants.MANDANT_LU: {
			return new InstitutionStammdatenBuilderLu(institutionStammdatenService);
		}
		case MandantConstants.MANDANT_SO: {
			return new InstitutionStammdatenBuilderSo(institutionStammdatenService);
		}
		default: {
			throw new EbeguRuntimeException(
					"getInstitutionsStammdatenBuilder",
					"Testfaelle not implemented for mandant {}",
					mandant.getName());
		}
		}
	}
}
