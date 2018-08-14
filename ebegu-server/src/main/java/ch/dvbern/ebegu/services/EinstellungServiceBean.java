/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Einstellung_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.errors.NoEinstellungFoundException;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer Einstellungen
 */
@Stateless
@Local(EinstellungService.class)
@RolesAllowed({ ADMIN, SUPER_ADMIN })
public class EinstellungServiceBean extends AbstractBaseService implements EinstellungService {

	@Inject
	private Persistence persistence;


	@Override
	@Nonnull
	public Einstellung saveEinstellung(@Nonnull Einstellung einstellung) {
		Objects.requireNonNull(einstellung);
		return persistence.merge(einstellung);
	}

	@Override
	@Nonnull
	public Einstellung findEinstellung(@Nonnull EinstellungKey key, @Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Einstellung> query = cb.createQuery(Einstellung.class);

		Root<Einstellung> root = query.from(Einstellung.class);
		// MUSS Kriterien
		final Predicate predicateStatus = cb.equal(root.get(Einstellung_.key), key);
		final Predicate predicateGesuchsperiode = cb.equal(root.get(Einstellung_.gesuchsperiode), gesuchsperiode);
		// Gemeinde: die richtige oder nicht gesetzt
		final Predicate predicateGemeinde = cb.equal(root.get(Einstellung_.gemeinde), gemeinde);
		final Predicate predicateGemeindeNull = cb.isNull(root.get(Einstellung_.gemeinde));
		final Predicate predicateGemeindeOrNull = cb.or(predicateGemeinde, predicateGemeindeNull);

		// Mandant: der richtige oder nicht gesetzt
		final Predicate predicateMandant = cb.equal(root.get(Einstellung_.mandant), gemeinde.getMandant());
		final Predicate predicateMandantNull = cb.isNull(root.get(Einstellung_.mandant));
		final Predicate predicateMandantOrNull = cb.or(predicateMandant, predicateMandantNull);

		query.where(predicateStatus, predicateGesuchsperiode, predicateGemeindeOrNull, predicateMandantOrNull);
		query.orderBy(cb.desc(root.get(Einstellung_.gemeinde)), cb.desc(root.get(Einstellung_.mandant)));
		query.select(root);
		List<Einstellung> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			throw new NoEinstellungFoundException(key, gemeinde);
		}
		return criteriaResults.get(0);
	}
}
