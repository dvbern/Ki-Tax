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

public class InstitutionStammdatenBuilderAr extends InstitutionStammdatenBuilder {
	public InstitutionStammdatenBuilderAr(InstitutionStammdatenService institutionStammdatenService) {
		super(
				institutionStammdatenService,
				"51451c92-39a9-11ed-a63d-b05cda43de9c",
				"56aa13db-39a9-11ed-a63d-b05cda43de9c",
				"458bee8d-39a9-11ed-a63d-b05cda43de9c");
	}

	@Override
	public List<InstitutionStammdaten> buildStammdaten() {
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(findInstitutionWeissenstein());
		institutionStammdatenList.add(findInstitutionBruennen());
		institutionStammdatenList.add(findInstitutionTagesfamilien());

		return institutionStammdatenList;
	}
}
