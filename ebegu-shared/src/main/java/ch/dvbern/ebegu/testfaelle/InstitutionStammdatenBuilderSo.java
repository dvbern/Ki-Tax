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

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;

public class InstitutionStammdatenBuilderSo extends InstitutionStammdatenBuilder {
	public InstitutionStammdatenBuilderSo(InstitutionStammdatenService institutionStammdatenService) {
		super(
				institutionStammdatenService,
				"58b84479-537f-11ec-98e8-f4390979fa3e",
				"6aa08c20-537f-11ec-98e8-f4390979fa3e",
				"50518a55-537f-11ec-98e8-f4390979fa3e",
				"feddca3a-617f-11ec-9b42-b89a2ae4a038");
	}

	@Override
	public List<InstitutionStammdaten> buildStammdaten() {
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(findInstitutionWeissenstein());
		institutionStammdatenList.add(findInstitutionBruennen());
		institutionStammdatenList.add(findInstitutionTagesfamilien());
		institutionStammdatenList.add(findInstitutionTagesschule());

		return institutionStammdatenList;
	}
}
