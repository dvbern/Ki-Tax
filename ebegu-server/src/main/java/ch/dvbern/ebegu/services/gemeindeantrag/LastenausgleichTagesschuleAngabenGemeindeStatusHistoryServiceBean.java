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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistory;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistory_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer den Lastenausgleich der Tagesschulen, StatusHistory
 */
@Stateless
@Local(LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService.class)
public class LastenausgleichTagesschuleAngabenGemeindeStatusHistoryServiceBean extends AbstractBaseService implements LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService {

	private static final Logger LOG = LoggerFactory.getLogger(LastenausgleichTagesschuleAngabenGemeindeStatusHistoryServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private BenutzerService benutzerService;


	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeStatusHistory saveLastenausgleichTagesschuleStatusChange(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		Objects.requireNonNull(fallContainer);

		final Benutzer currentBenutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new EbeguEntityNotFoundException("saveLastenausgleichTagesschuleStatusChange", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
		// Den letzten Eintrag beenden, falls es schon einen gab
		LastenausgleichTagesschuleAngabenGemeindeStatusHistory lastStatusChange = findLastLastenausgleichTagesschuleStatusChange(fallContainer);
		if (lastStatusChange != null) {
			lastStatusChange.setTimestampBis(LocalDateTime.now());
		}
		// Und den neuen speichern
		final LastenausgleichTagesschuleAngabenGemeindeStatusHistory newStatusHistory = new LastenausgleichTagesschuleAngabenGemeindeStatusHistory();
		newStatusHistory.setStatus(fallContainer.getStatus());
		newStatusHistory.setAngabenGemeindeContainer(fallContainer);
		newStatusHistory.setTimestampVon(LocalDateTime.now());
		newStatusHistory.setBenutzer(currentBenutzer);

		return persistence.persist(newStatusHistory);
	}

	@Override
	public List<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> findHistoryForContainer(@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container) {
		final CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> query =
			createQueryAllStatusHistoryForFall(container);
		return persistence.getEntityManager().createQuery(query).getResultList();
	}

	@Nullable
	private LastenausgleichTagesschuleAngabenGemeindeStatusHistory findLastLastenausgleichTagesschuleStatusChange(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		Objects.requireNonNull(fallContainer);

		try {
			final CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> query = createQueryAllStatusHistoryForFall(fallContainer);

			LastenausgleichTagesschuleAngabenGemeindeStatusHistory result = persistence.getEntityManager().createQuery(query).setFirstResult(0).setMaxResults(1).getSingleResult();
			return result;
		} catch (NoResultException e) {
			LOG.debug("No last status change found for LastenausgleichTagesschuleFallContainer {}", fallContainer, e);
			return null;
		}
	}

	/**
	 * Gibt alle LastenausgleichTagesschuleFallStatusHistory des gegebenen Falls zurueck. Sortiert nach timestampVon DESC
	 */
	@Nonnull
	private CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> createQueryAllStatusHistoryForFall(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		Objects.requireNonNull(fallContainer);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> query = cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeStatusHistory.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeStatusHistory> root = query.from(LastenausgleichTagesschuleAngabenGemeindeStatusHistory.class);

		Predicate predicateInstitution = cb.equal(root.get(LastenausgleichTagesschuleAngabenGemeindeStatusHistory_.angabenGemeindeContainer), fallContainer);

		query.where(predicateInstitution);
		query.orderBy(cb.desc(root.get(LastenausgleichTagesschuleAngabenGemeindeStatusHistory_.timestampVon)));
		return query;
	}
}


