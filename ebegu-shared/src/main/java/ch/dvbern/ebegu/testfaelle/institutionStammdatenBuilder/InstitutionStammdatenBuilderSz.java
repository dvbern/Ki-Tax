/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;

import java.util.ArrayList;
import java.util.List;

public class InstitutionStammdatenBuilderSz extends InstitutionStammdatenBuilder {
	public InstitutionStammdatenBuilderSz(InstitutionStammdatenService institutionStammdatenService) {
		super(
				institutionStammdatenService,
				"cfeeb01a-b3e8-11ee-829a-0242ac160002",
				"d968ba59-b3e8-11ee-829a-0242ac160002",
				"9fdc2b4d-b3e8-11ee-829a-0242ac160002");
		setIdInstitutionStammdatenMittagstisch("28026216-dc73-11ee-8dae-0242ac160002");
	}

	@Override
	public List<InstitutionStammdaten> buildStammdaten() {
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(findInstitutionWeissenstein());
		institutionStammdatenList.add(findInstitutionBruennen());
		institutionStammdatenList.add(findInstitutionTagesfamilien());
		institutionStammdatenList.add(findInstitutionMittagstisch());

		return institutionStammdatenList;
	}
}
