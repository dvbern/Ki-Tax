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

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule_;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.ejb3.annotation.TransactionTimeout;

import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
public class AnmeldungTagesschuleEventGenerator {

	@Inject
	private Persistence persistence;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private AnmeldungTagesschuleEventAsyncHelper asyncHelper;

	/**
	 * This is a job starting every night and exports all anmeldungen for which event_published
	 * value is false
	 */
	@Schedule(info = "Migration-aid, pushes Anmeldungen waiting for confirmation and not yet published",
		hour = "5")
	@TransactionTimeout(value = 3, unit = TimeUnit.HOURS)
	public void publishWartendeAnmeldungen() {
		if (!ebeguConfiguration.isAnmeldungTagesschuleApiEnabled()) {
			return;
		}

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<AnmeldungTagesschule> root = query.from(AnmeldungTagesschule.class);

		//Event muss noch nicht plubliziert sein
		Predicate isNotPublished = cb.isFalse(root.get(AnmeldungTagesschule_.eventPublished));
		Predicate isInStatusToFireEvent = root.get(AbstractPlatz_.betreuungsstatus)
			.in(Betreuungsstatus.getBetreuungsstatusForFireAnmeldungTagesschuleEvent());

		query.where(isNotPublished, isInStatusToFireEvent);
		query.select(root.get(AbstractEntity_.ID));

		List<String> anmeldungTagesschuleList = persistence.getEntityManager().createQuery(query)
			.getResultList();

		anmeldungTagesschuleList.forEach(anmeldung -> asyncHelper.convert(anmeldung));
	}
}
