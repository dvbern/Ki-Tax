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

package ch.dvbern.ebegu.services.sozialdienst;

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
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFallDokument;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFallDokument_;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.SozialdienstFallDokumentService;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(SozialdienstFallDokumentService.class)
public class SozialdienstFallDokumentServiceBean extends AbstractBaseService implements SozialdienstFallDokumentService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Override
	@Nonnull
	public Optional<SozialdienstFallDokument> findDokument(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		SozialdienstFallDokument doc = persistence.find(SozialdienstFallDokument.class, key);
		if (doc == null) {
			return Optional.empty();
		}
		authorizer.checkReadAuthorization(doc.getSozialdienstFall());
		return Optional.of(doc);
	}

	@Nonnull
	@Override
	public List<SozialdienstFallDokument> findDokumente(@Nonnull String sozialdienstFallId) {
		final SozialdienstFall
			sozialdienstFall = persistence.find(SozialdienstFall.class, sozialdienstFallId);
		if (sozialdienstFall == null) {
			return Collections.emptyList();
		}
		authorizer.checkReadAuthorization(sozialdienstFall);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<SozialdienstFallDokument> query = cb.createQuery(SozialdienstFallDokument.class);
		Root<SozialdienstFallDokument> root = query.from(SozialdienstFallDokument.class);

		ParameterExpression<String>
			sozialdienstFallFormularIdParam = cb.parameter(String.class, "sozialdienstFallId");

		Predicate predicateRueckfoderungFormularId =
			cb.equal(root.get(SozialdienstFallDokument_.sozialdienstFall).get(
				AbstractEntity_.id), sozialdienstFallFormularIdParam);
		query.where(predicateRueckfoderungFormularId);
		query.orderBy(cb.asc(root.get(SozialdienstFallDokument_.timestampUpload)));
		TypedQuery<SozialdienstFallDokument> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(sozialdienstFallFormularIdParam, sozialdienstFallId);

		return q.getResultList();
	}

	@Override
	public void removeDokument(@Nonnull SozialdienstFallDokument dokument) {
		authorizer.checkWriteAuthorization(dokument.getSozialdienstFall());
		persistence.remove(dokument);
	}

	@Nonnull
	@Override
	public SozialdienstFallDokument saveVollmachtDokument(@Nonnull SozialdienstFallDokument sozialdienstFallDokument) {
		Objects.requireNonNull(sozialdienstFallDokument);
		authorizer.checkWriteAuthorization(sozialdienstFallDokument.getSozialdienstFall());

		sozialdienstFallDokument.setTimestampUpload(LocalDateTime.now());

		final SozialdienstFallDokument mergedRueckforderungDokument = persistence.merge(sozialdienstFallDokument);

		return mergedRueckforderungDokument;
	}

	@Override
	public void removeDokumenteForSozialdienstFall(@Nonnull String sozialdienstFallId) {
		for(SozialdienstFallDokument dokument: findDokumente(sozialdienstFallId)) {
			removeDokument(dokument);
		}
	}
}
