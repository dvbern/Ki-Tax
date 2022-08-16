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

package ch.dvbern.ebegu.outbox.gemeinde;

import java.util.List;

import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
public class GemeindeEventGenerator {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private GemeindeEventConverter gemeindeEventConverter;

	@Schedule(info = "Migration-aid, pushes already existing Gemeinden to outbox", hour = "5", persistent = true)
	public void publishExistingGemeinden() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Gemeinde> query = cb.createQuery(Gemeinde.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);

		Predicate isNotPublished = cb.isFalse(root.get(Gemeinde_.eventPublished));

		query.where(isNotPublished);

		List<Gemeinde> gemeinden = persistence.getEntityManager().createQuery(query)
			.getResultList();

		gemeinden.forEach(gemeinde -> {
			event.fire(gemeindeEventConverter.of(gemeinde));
			gemeinde.setEventPublished(true);
			persistence.merge(gemeinde);
		});
	}
}
