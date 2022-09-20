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

package ch.dvbern.ebegu.outbox.institution;

import java.util.List;

import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
public class InstitutionEventGenerator {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private InstitutionEventConverter institutionEventConverter;

	@Schedule(info = "Migration-aid, pushes already existing institutions to outbox", hour = "5", persistent = true)
	public void publishExistingInstitutionen() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);

		Join<InstitutionStammdaten, Institution> institutionJoin = root.join(InstitutionStammdaten_.institution);

		Predicate isNotPublished = cb.isFalse(institutionJoin.get(Institution_.eventPublished));

		query.where(isNotPublished);

		List<InstitutionStammdaten> institutions = persistence.getEntityManager().createQuery(query)
			.getResultList();

		institutions.forEach(stammdaten -> {
			if (!stammdaten.getInstitution().getStatus().equals(InstitutionStatus.NUR_LATS)){
				event.fire(institutionEventConverter.of(stammdaten));
				Institution institution = stammdaten.getInstitution();
				institution.setSkipPreUpdate(true);
				institution.setEventPublished(true);
				persistence.merge(institution);
			}
		});
	}
}
