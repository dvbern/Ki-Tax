/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.interceptors.UpdateRueckfordFormStatusInterceptor;
import ch.dvbern.lib.cdipersistence.Persistence;

import ch.dvbern.ebegu.entities.RueckforderungFormular;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Stateless
@Local(RueckforderungFormularService.class)
public class RueckforderungFormularServiceBean extends AbstractBaseService implements RueckforderungFormularService {

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN })
	public List<RueckforderungFormular> initializeRueckforderungFormulare() {

		Collection<InstitutionStammdaten> institutionenStammdatenCollection = institutionStammdatenService.getAllInstitutionStammdaten();
		Collection<RueckforderungFormular> rueckforderungFormularCollection = getAllRueckforderungFormulare();

		List<RueckforderungFormular> rueckforderungFormulare = new ArrayList<>();
		for (InstitutionStammdaten institutionStammdaten : institutionenStammdatenCollection) {
			// neues Formular erstellen falls es sich un eine kita oder TFO handelt und noch kein Formular existiert
			if ((institutionStammdaten.getBetreuungsangebotTyp().equals(BetreuungsangebotTyp.KITA) ||
				institutionStammdaten.getBetreuungsangebotTyp().equals(BetreuungsangebotTyp.TAGESFAMILIEN)) &&
				!isFormularExisting(institutionStammdaten, rueckforderungFormularCollection)) {

				RueckforderungFormular formular = new RueckforderungFormular();
				formular.setInstitutionStammdaten(institutionStammdaten);
				formular.setStatus(RueckforderungStatus.NEU);
				rueckforderungFormulare.add(createRueckforderungFormular(formular));
			}
		}
		return rueckforderungFormulare;
	}

	/**
	 * Falls in der Liste der Rückforderungsformulare die Institution bereits existiert, wird true zurückgegeben
	 */
	private boolean isFormularExisting(InstitutionStammdaten stammdaten,
		Collection<RueckforderungFormular> rueckforderungFormularCollection) {
		List<RueckforderungFormular> filteredFormulare = rueckforderungFormularCollection.stream().filter(formular -> {
			return formular.getInstitutionStammdaten().getId().equals(stammdaten.getId());
		}).collect(Collectors.toList());
		return filteredFormulare.size() > 0;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN })
	public RueckforderungFormular createRueckforderungFormular(@Nonnull RueckforderungFormular rueckforderungFormular) {
		return persistence.persist(rueckforderungFormular);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	public Collection<RueckforderungFormular> getAllRueckforderungFormulare(){
		return criteriaQueryHelper.getAll(RueckforderungFormular.class);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	@Interceptors(UpdateRueckfordFormStatusInterceptor.class)
	public Optional<RueckforderungFormular> findRueckforderungFormular(String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		RueckforderungFormular rueckforderungFormular = persistence.find(RueckforderungFormular.class, id);
		return Optional.ofNullable(rueckforderungFormular);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION})
	public RueckforderungFormular save(RueckforderungFormular rueckforderungFormular) {
		Objects.requireNonNull(rueckforderungFormular);
		final RueckforderungFormular mergedRueckforderungFormular = persistence.merge(rueckforderungFormular);
		return mergedRueckforderungFormular;
	}
}
