/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.ExternalClient_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.InstitutionExternalClient_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ExternalClientInstitutionType;
import ch.dvbern.ebegu.enums.ExternalClientType;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(ExternalClientService.class)
public class ExternalClientServiceBean extends AbstractBaseService implements ExternalClientService {

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Nonnull
	@Override
	public Collection<ExternalClient> getAllForGemeinde() {
		return criteriaQueryHelper.getEntitiesByAttribute(
			ExternalClient.class, ExternalClientType.GEMEINDE_SCOLARIS_SERVICE, ExternalClient_.type);
	}

	@Nonnull
	@Override
	public Collection<ExternalClient> getAllForInstitution(@Nonnull Institution institution) {
		InstitutionStammdaten institutionStammdaten =
			institutionStammdatenService.fetchInstitutionStammdatenByInstitution(institution.getId(), true);
		Objects.requireNonNull(institutionStammdaten);

		Set<ExternalClientInstitutionType> types = new HashSet<>();
		// EXCHANGE_SERVICE_USER is allowed for both roles
		types.add(ExternalClientInstitutionType.EXCHANGE_SERVICE_INSTITUTION);

		if (institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.KITA
			|| institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESFAMILIEN
		) {
			types.add(ExternalClientInstitutionType.EXCHANGE_SERVICE_INSTITUTION_BG);
		} else if (institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESSCHULE) {
			types.add(ExternalClientInstitutionType.EXCHANGE_SERVICE_INSTITUTION_TS);
		}

		return getAllForTypes(types);
	}

	@Nonnull
	@Override
	public Optional<ExternalClient> findExternalClient(@Nonnull String id) {
		ExternalClient externalClient = persistence.find(ExternalClient.class, id);
		return Optional.ofNullable(externalClient);
	}

	@Nonnull
	@Override
	public Collection<InstitutionExternalClient> getInstitutionExternalClientForInstitution(@Nonnull Institution institution) {
		return criteriaQueryHelper.getEntitiesByAttribute(
			InstitutionExternalClient.class, institution, InstitutionExternalClient_.institution);
	}

	@Nonnull
	@Override
	public Collection<ExternalClient> getAll() {
		Set<ExternalClientInstitutionType> types = new HashSet<>();
		types.addAll(Arrays.asList(ExternalClientInstitutionType.values()));
		return getAllForTypes(types);
	}

	@Nonnull
	private Collection<ExternalClient> getAllForTypes(Set<ExternalClientInstitutionType> types) {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<ExternalClient> query = builder.createQuery(ExternalClient.class);
		final Root<ExternalClient> root = query.from(ExternalClient.class);
		Predicate typePredicate = root.get(ExternalClient_.institutionType).in(types);
		query.where(typePredicate);

		return persistence.getEntityManager().createQuery(query)
			.getResultList();
	}
}
