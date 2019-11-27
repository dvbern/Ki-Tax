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

package ch.dvbern.ebegu.outbox.verfuegung;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.rechner.AbstractBGRechner;
import ch.dvbern.ebegu.rechner.BGCalculationResult;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.rechner.BGRechnerFactory.getRechner;
import static java.util.Objects.requireNonNull;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
public class VerfuegungEventGenerator extends AbstractBaseService {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private VerfuegungEventConverter verfuegungEventConverter;

	/**
	 * First, add Zeitenheiten fields to all Verfuegungen in the database, then convert to events.
	 * Reason: when converting to events, we also get vorgänger Verfügung Zeitabschnitte. Thus, the initialisation must
	 * happen on all Verfügungen, before they are converted.
	 */
	@TransactionTimeout(value = 3, unit = TimeUnit.HOURS)
	@Schedule(info = "Migration-aid, pushes already existing Verfuegungen to outbox", hour = "5", persistent = true)
	public void migrate() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Verfuegung> query = cb.createQuery(Verfuegung.class);
		Root<Verfuegung> root = query.from(Verfuegung.class);

		Predicate isNotPublished = cb.isFalse(root.get(Verfuegung_.eventPublished));

		query.where(isNotPublished);

		List<Verfuegung> verfuegungen = persistence.getEntityManager().createQuery(query)
			.getResultList();

		verfuegungen.forEach(verfuegung -> {
			withZeiteinheiten(verfuegung);
			verfuegung.setSkipPreUpdate(true);
		});

		persistence.getEntityManager().flush();

		publishExistingVerfuegungen();
	}

	/**
	 * Each new Verfuegung is published to Kafka via the outbox event system. However, there are already Verfuegungn
	 * in the database which have not been published, because the outbox event system has been added later. Thus,
	 * fetch all these Verfuegungen and publish them once.
	 */
	private void publishExistingVerfuegungen() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Verfuegung> query = cb.createQuery(Verfuegung.class);
		Root<Verfuegung> root = query.from(Verfuegung.class);
		Path<Betreuung> betreuungPath = root.get(Verfuegung_.betreuung);

		ParameterExpression<Betreuungsstatus> statusParam = cb.parameter(Betreuungsstatus.class);
		Predicate isVerfuegt = cb.equal(betreuungPath.get(AbstractPlatz_.betreuungsstatus), statusParam);

		Predicate isGueltig = cb.isTrue(betreuungPath.get(AbstractPlatz_.gueltig));

		Predicate isNotPublished = cb.isFalse(root.get(Verfuegung_.eventPublished));

		query.where(isGueltig, isNotPublished, isVerfuegt);

		List<Verfuegung> verfuegungen = persistence.getEntityManager().createQuery(query)
			.setParameter(statusParam, Betreuungsstatus.VERFUEGT)
			.getResultList();

		verfuegungen.forEach(verfuegung -> {
			event.fire(verfuegungEventConverter.of(verfuegung));

			verfuegung.setSkipPreUpdate(true);
			verfuegung.setEventPublished(true);
			persistence.merge(verfuegung);
		});
	}

	private void withZeiteinheiten(@Nonnull Verfuegung verfuegung) {
		AbstractBGRechner rechner = requireNonNull(getRechner(verfuegung.getBetreuung()));
		BGRechnerParameterDTO parameterDTO = getParameter(verfuegung);

		verfuegung.getZeitabschnitte()
			.forEach(zeitabschnitt -> zeitabschnittWithZeiteinheiten(rechner, parameterDTO, zeitabschnitt));
	}

	@Nonnull
	private BGRechnerParameterDTO getParameter(@Nonnull Verfuegung verfuegung) {
		Gesuch gesuch = verfuegung.getBetreuung().getKind().getGesuch();
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();
		Gemeinde gemeinde = gesuch.extractGemeinde();

		return loadCalculatorParameters(gemeinde, gesuchsperiode);
	}

	/**
	 * Updates a VerfuegungZeitabschnitt with the Zeiteinheiten fields.
	 */
	private void zeitabschnittWithZeiteinheiten(
		@Nonnull AbstractBGRechner rechner,
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {

		BGCalculationResult result = rechner.calculate(zeitabschnitt, parameterDTO);
		zeitabschnitt.setVerfuegteAnzahlZeiteinheiten(result.getVerfuegteAnzahlZeiteinheiten());
		zeitabschnitt.setAnspruchsberechtigteAnzahlZeiteinheiten(result.getAnspruchsberechtigteAnzahlZeiteinheiten());
		zeitabschnitt.setZeiteinheit(result.getZeiteinheit());
	}
}
