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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Massenversand;
import ch.dvbern.ebegu.entities.Massenversand_;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(MassenversandService.class)
public class MassenversandServiceBean extends AbstractBaseService implements MassenversandService {

	@Inject
	private Persistence persistence;

	@Inject
	private GesuchService gesuchService;

	@Override
	public void createMassenversand(@Nonnull Massenversand massenversand) {
		persistence.persist(massenversand);
	}

	@Override
	public List<String> getMassenversandTexteForGesuch(@Nonnull String gesuchId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Massenversand> query = cb.createQuery(Massenversand.class);

		Root<Massenversand> root = query.from(Massenversand.class);
		ListJoin<Massenversand, Gesuch> gesuche = root.join(Massenversand_.gesuche);

		Predicate predicate = cb.equal(gesuche.get(AbstractEntity_.id), gesuchId);
		query.where(predicate);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		List<Massenversand> massenversaende = persistence.getCriteriaResults(query);
		List<String> result = massenversaende.stream()
			.map(Massenversand::getDescription)
			.collect(Collectors.toList());

		return result;
	}

	@Override
	public void removeMassenversandGesucheForFall(@Nonnull final Fall fall) {

		List<String> gesuchIds = gesuchService.getAllGesuchIDsForFall(fall.getId());

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Massenversand> query = cb.createQuery(Massenversand.class);
		Root<Massenversand> root = query.from(Massenversand.class);
		Join<Massenversand, Gesuch> join = root.join(Massenversand_.gesuche);

		Predicate predGesuchId = join.get(Gesuch_.id).in(gesuchIds);
		query.where(predGesuchId);
		final List<Massenversand> massenversandList = persistence.getCriteriaResults(query);

		for (Massenversand massenversand : massenversandList) {
			List<Gesuch> filtered = massenversand.getGesuche()
				.stream()
				.filter(g -> !gesuchIds.contains(g.getId()))
				.collect(Collectors.toList());
			massenversand.setGesuche(filtered);
			persistence.merge(massenversand);
		}
	}
}
