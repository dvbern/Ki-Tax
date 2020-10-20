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

package ch.dvbern.ebegu.outbox.platzbestaetigung;

import java.util.ArrayList;
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

import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.services.util.PredicateHelper.NEW;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
public class BetreuungAnfrageEventGenerator {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private BetreuungAnfrageEventConverter betreuungAnfrageEventConverter;

	/**
	 * This is a job starting every night, there must be no more need for this job after the first execution
	 * but this could be a great help if we want to re-export something, then we just have to change the database
	 * column event_published value and it is re-exported automatically during the following night
	 */
	@Schedule(info = "Migration-aid, pushes Betreuung waiting for Platzbestaetigung and not yet published",
		hour = "4",
		persistent = true)
	public void publishWartendeBetreuung() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);
		Root<Betreuung> root = query.from(Betreuung.class);
		List<Predicate> predicates = new ArrayList<>();

		//Institution Stammdaten Join and check angebot Typ, muss Kita oder TFO sein
		Join<Betreuung, InstitutionStammdaten> institutionStammdatenJoin =
			root.join(Betreuung_.institutionStammdaten);
		Predicate isKitaOderTFO =
			institutionStammdatenJoin.get(InstitutionStammdaten_.betreuungsangebotTyp)
				.in(BetreuungsangebotTyp.getBetreuungsgutscheinTypes());
		predicates.add(isKitaOderTFO);

		//Event muss noch nicht plubliziert sein
		Predicate isNotPublished = cb.isFalse(root.get(Betreuung_.eventPublished));
		predicates.add(isNotPublished);

		//Status muss warten sein
		Predicate statusWarten = cb.equal(root.get(AbstractPlatz_.betreuungsstatus), Betreuungsstatus.WARTEN);
		predicates.add(statusWarten);

		query.where(predicates.toArray(NEW));

		List<Betreuung> betreuungs = persistence.getEntityManager().createQuery(query)
			.getResultList();

		betreuungs.forEach(betreuung -> {
			event.fire(betreuungAnfrageEventConverter.of(betreuung));
			betreuung.setEventPublished(true);
			persistence.merge(betreuung);
		});
	}
}
