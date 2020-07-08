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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.RueckforderungDokument;
import ch.dvbern.ebegu.entities.RueckforderungDokument_;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(RueckforderungDokumentService.class)
public class RueckforderungDokumentServiceBean extends AbstractBaseService implements RueckforderungDokumentService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Override
	@Nonnull
	public Optional<RueckforderungDokument> findDokument(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		RueckforderungDokument doc = persistence.find(RueckforderungDokument.class, key);
		if (doc == null) {
			return Optional.empty();
		}
		authorizer.checkReadAuthorization(doc.getRueckforderungFormular());
		return Optional.of(doc);
	}

	@Nonnull
	@Override
	public List<RueckforderungDokument> findDokumente(@Nonnull String rueckforderungFormularId) {
		final RueckforderungFormular rueckforderungFormular = persistence.find(RueckforderungFormular.class, rueckforderungFormularId);
		if (rueckforderungFormular == null) {
			return Collections.emptyList();
		}
		authorizer.checkReadAuthorization(rueckforderungFormular);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<RueckforderungDokument> query = cb.createQuery(RueckforderungDokument.class);
		Root<RueckforderungDokument> root = query.from(RueckforderungDokument.class);

		ParameterExpression<String> rueckforderungFormularIdParam = cb.parameter(String.class, "rueckforderungFormularId");

		Predicate predicateRueckfoderungFormularId = cb.equal(root.get(RueckforderungDokument_.rueckforderungFormular).get(AbstractEntity_.id), rueckforderungFormularIdParam);
		query.where(predicateRueckfoderungFormularId);
		query.orderBy(cb.asc(root.get(RueckforderungDokument_.timestampUpload)));
		TypedQuery<RueckforderungDokument> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(rueckforderungFormularIdParam, rueckforderungFormularId);

		return q.getResultList();
	}

	@Override
	public void removeDokument(@Nonnull RueckforderungDokument dokument) {
		authorizer.checkWriteAuthorization(dokument.getRueckforderungFormular());
		persistence.remove(dokument);
	}

	@Nonnull
	@Override
	public RueckforderungDokument saveDokumentGrund(@Nonnull RueckforderungDokument rueckforderungDokument) {
		Objects.requireNonNull(rueckforderungDokument);
		authorizer.checkWriteAuthorization(rueckforderungDokument.getRueckforderungFormular());

		rueckforderungDokument.setTimestampUpload(LocalDateTime.now());

		final RueckforderungDokument mergedRueckforderungDokument = persistence.merge(rueckforderungDokument);

		return mergedRueckforderungDokument;
	}
}
