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
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import org.jetbrains.annotations.Nullable;

public abstract class InstitutionStammdatenBuilder {

	private final InstitutionStammdatenService institutionStammdatenService;

	private final String idInstitutionStammdatenWeissenstein;
	private final String idInstitutionStammdatenBruennen;
	private final String idInstitutionStammdatenTagesfamilie;
	private final String idInstitutionStammdatenTagesschule;
	private final String idInstitutionStammdatenFerieninsel;

	protected InstitutionStammdatenBuilder(
			InstitutionStammdatenService institutionStammdatenService,
			String idInstitutionStammdatenWeissenstein,
			String idInstitutionStammdatenBruennen,
			String idInstitutionStammdatenTagesfamilie,
			String idInstitutionStammdatenTagesschule,
			@Nullable String idInstitutionStammdatenFerieninsel) {
		this.institutionStammdatenService = institutionStammdatenService;
		this.idInstitutionStammdatenWeissenstein = idInstitutionStammdatenWeissenstein;
		this.idInstitutionStammdatenBruennen = idInstitutionStammdatenBruennen;
		this.idInstitutionStammdatenTagesfamilie = idInstitutionStammdatenTagesfamilie;
		this.idInstitutionStammdatenTagesschule = idInstitutionStammdatenTagesschule;
		this.idInstitutionStammdatenFerieninsel = idInstitutionStammdatenFerieninsel;
	}

	protected InstitutionStammdatenBuilder(
			InstitutionStammdatenService institutionStammdatenService,
			String idInstitutionStammdatenWeissenstein,
			String idInstitutionStammdatenBruennen,
			String idInstitutionStammdatenTagesfamilie,
			String idInstitutionStammdatenTagesschule) {
		this(institutionStammdatenService, idInstitutionStammdatenWeissenstein, idInstitutionStammdatenBruennen, idInstitutionStammdatenTagesfamilie, idInstitutionStammdatenTagesschule, null);
	}

	public List<InstitutionStammdaten> buildStammdaten() {
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(findInstitutionWeissenstein());
		institutionStammdatenList.add(findInstitutionBruennen());
		institutionStammdatenList.add(findInstitutionTagesfamilien());
		institutionStammdatenList.add(findInstitutionTagesschule());

		return institutionStammdatenList;
	}

	protected InstitutionStammdaten findInstitutionWeissenstein() {
		return institutionStammdatenService.findInstitutionStammdaten(idInstitutionStammdatenWeissenstein)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
						"findInstitutionWeissenstein",
						idInstitutionStammdatenWeissenstein));
	}

	protected InstitutionStammdaten findInstitutionBruennen() {
		return institutionStammdatenService.findInstitutionStammdaten(idInstitutionStammdatenBruennen)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
						"findInstitutionBruennen",
						idInstitutionStammdatenWeissenstein));
	}

	protected InstitutionStammdaten findInstitutionTagesfamilien() {
		return institutionStammdatenService.findInstitutionStammdaten(idInstitutionStammdatenTagesfamilie)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
						"findInstitutionTagesfamilien",
					idInstitutionStammdatenTagesfamilie));
	}

	protected InstitutionStammdaten findInstitutionTagesschule() {
		return institutionStammdatenService.findInstitutionStammdaten(idInstitutionStammdatenTagesschule)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
						"findInstitutionTagesschule",
					idInstitutionStammdatenTagesschule));
	}

	protected InstitutionStammdaten findInstitutionFerieninsel() {
		assert idInstitutionStammdatenFerieninsel != null;
		return institutionStammdatenService.findInstitutionStammdaten(idInstitutionStammdatenFerieninsel)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
						"findInstitutionFerieninsel",
					idInstitutionStammdatenFerieninsel));
	}

	public InstitutionStammdatenService getInstitutionStammdatenService() {
		return institutionStammdatenService;
	}

	public String getIdInstitutionStammdatenWeissenstein() {
		return idInstitutionStammdatenWeissenstein;
	}

	public String getIdInstitutionStammdatenBruennen() {
		return idInstitutionStammdatenBruennen;
	}

	public String getIdInstitutionStammdatenTagesfamilie() {
		return idInstitutionStammdatenTagesfamilie;
	}

	public String getIdInstitutionStammdatenTagesschule() {
		return idInstitutionStammdatenTagesschule;
	}

	@Nullable
	public String getIdInstitutionStammdatenFerieninsel() {
		return idInstitutionStammdatenFerieninsel;
	}
}
