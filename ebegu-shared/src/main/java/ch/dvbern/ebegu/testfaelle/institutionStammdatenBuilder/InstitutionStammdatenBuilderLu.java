/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;

public class InstitutionStammdatenBuilderLu extends InstitutionStammdatenBuilder {
	public InstitutionStammdatenBuilderLu(InstitutionStammdatenService institutionStammdatenService) {
		super(institutionStammdatenService, "97882a4e-3261-11ec-a17e-b89a2ae4a038", "6d6afdb2-3261-11ec-a17e-b89a2ae4a038",
				"16075d77-30a5-11ec-a86f-b89a2ae4a038", "b8e3acdc-3261-11ec-a17e-b89a2ae4a038");
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
