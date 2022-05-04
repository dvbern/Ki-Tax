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
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAusweisDokument;
import ch.dvbern.ebegu.entities.GesuchstellerAusweisDokument_;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer die Dokumente der Ferienbetreuungen
 */
@Stateless
@Local(GesuchstellerAusweisDokumentService.class)
public class GesuchstellerAusweisAusweisDokumentServiceBean extends AbstractBaseService
	implements GesuchstellerAusweisDokumentService {

	public static final String ID_MUSS_GESETZT_SEIN = "id muss gesetzt sein";

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	public GesuchstellerAusweisDokument saveDokument(@Nonnull GesuchstellerAusweisDokument gesuchstellerAusweisDokument) {
		Objects.requireNonNull(gesuchstellerAusweisDokument);
		authorizer.checkWriteAuthorization(gesuchstellerAusweisDokument.getGesuch());

		return persistence.merge(gesuchstellerAusweisDokument);
	}

	@Nonnull
	@Override
	public Optional<GesuchstellerAusweisDokument> findDokument(@Nonnull String dokumentId) {
		Objects.requireNonNull(dokumentId, ID_MUSS_GESETZT_SEIN);
		GesuchstellerAusweisDokument dokument = persistence.find(GesuchstellerAusweisDokument.class, dokumentId);
		authorizer.checkReadAuthorization(dokument.getGesuch());
		return Optional.ofNullable(dokument);
	}

	@Override
	public void removeDokument(@Nonnull GesuchstellerAusweisDokument dokument) {
		authorizer.checkWriteAuthorization(dokument.getGesuch());
		persistence.remove(dokument);
	}

	@Nonnull
	@Override
	public List<GesuchstellerAusweisDokument> findDokumente(@Nonnull String gesuchId) {
		final Gesuch gesuch = persistence.find(Gesuch.class, gesuchId);
		if (gesuch == null) {
			return Collections.emptyList();
		}
		authorizer.checkReadAuthorization(gesuch);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GesuchstellerAusweisDokument> query = cb.createQuery(GesuchstellerAusweisDokument.class);
		Root<GesuchstellerAusweisDokument> root = query.from(GesuchstellerAusweisDokument.class);

		ParameterExpression<String>
				gesuchstellerContainerIdParam = cb.parameter(String.class, "gesuchstellerContainerId");

		Predicate predicateRueckfoderungFormularId =
				cb.equal(root.get(GesuchstellerAusweisDokument_.gesuch).get(
						AbstractEntity_.id), gesuchstellerContainerIdParam);
		query.where(predicateRueckfoderungFormularId);
		query.orderBy(cb.asc(root.get(GesuchstellerAusweisDokument_.timestampUpload)));
		TypedQuery<GesuchstellerAusweisDokument> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(gesuchstellerContainerIdParam, gesuchId);

		return q.getResultList();
	}

}


