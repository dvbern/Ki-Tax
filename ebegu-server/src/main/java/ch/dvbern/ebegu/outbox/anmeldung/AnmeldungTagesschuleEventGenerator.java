/*
 *
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.anmeldung;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Schedule;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.services.util.PredicateHelper.NEW;

public class AnmeldungTagesschuleEventGenerator {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private AnmeldungTagesschuleEventConverter anmeldungTagesschuleEventConverter;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	/**
	 * This is a job starting every night and exports all anmeldungen for which event_published
	 * value is false
	 */
	@Schedule(info = "Migration-aid, pushes Anmeldungen waiting for confirmation and not yet published",
		hour = "5")
	public void publishWartendeAnmeldungen() {
		if (!ebeguConfiguration.isAnmeldungTagesschuleApiEnabled()) {
			return;
		}

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<AnmeldungTagesschule> query = cb.createQuery(AnmeldungTagesschule.class);
		Root<AnmeldungTagesschule> root = query.from(AnmeldungTagesschule.class);
		List<Predicate> predicates = new ArrayList<>();

		//Institution Stammdaten Join and check angebot Typ, muss Kita oder TFO sein
		Join<AnmeldungTagesschule, InstitutionStammdaten> institutionStammdatenJoin =
			root.join(AnmeldungTagesschule_.institutionStammdaten);
		Predicate isKitaOderTFO =
			institutionStammdatenJoin.get(InstitutionStammdaten_.betreuungsangebotTyp)
				.in(BetreuungsangebotTyp.getBetreuungsgutscheinTypes());
		predicates.add(isKitaOderTFO);

		//Event muss noch nicht plubliziert sein
		Predicate isNotPublished = cb.isFalse(root.get(AnmeldungTagesschule_.eventPublished));
		predicates.add(isNotPublished);

		query.where(predicates.toArray(NEW));

		List<AnmeldungTagesschule> anmeldungTagesschuleList = persistence.getEntityManager().createQuery(query)
			.getResultList();

		anmeldungTagesschuleList.stream()
			.filter(anmeldungTagesschule -> applicationPropertyService.isPublishSchnittstelleEventsAktiviert(
				anmeldungTagesschule.extractGesuch().extractMandant()))
			.forEach(anmeldungTagesschule -> {
				event.fire(anmeldungTagesschuleEventConverter.of(anmeldungTagesschule));
				anmeldungTagesschule.setEventPublished(true);
				persistence.merge(anmeldungTagesschule);
			});
	}
}
