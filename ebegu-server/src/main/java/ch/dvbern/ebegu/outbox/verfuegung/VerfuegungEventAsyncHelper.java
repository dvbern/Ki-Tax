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

import java.util.Optional;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.TransactionSynchronizationRegistry;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@Stateless
public class VerfuegungEventAsyncHelper {

	private static final Logger LOG = LoggerFactory.getLogger(VerfuegungEventAsyncHelper.class);

	@Resource
	private TransactionSynchronizationRegistry txReg;

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private VerfuegungEventConverter verfuegungEventConverter;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void convert(String id) {
		Verfuegung verfuegung = persistence.find(Verfuegung.class, id);

		Mandant mandant = verfuegung.getPlatz().extractGesuch().extractMandant();

		if (!applicationPropertyService.isPublishSchnittstelleEventsAktiviert(mandant)) {
			return;
		}

		LOG.info(
			"Converting {} in Thread {} and Transaction {}",
			requireNonNull(verfuegung.getBetreuung()).getReferenzNummer(),
			Thread.currentThread(),
			txReg.getTransactionKey());

		Optional<VerfuegungVerfuegtEvent> eventOpt = verfuegungEventConverter.of(verfuegung);

		eventOpt.ifPresent(verfuegungVerfuegtEvent -> {
			this.event.fire(verfuegungVerfuegtEvent);
			verfuegung.setSkipPreUpdate(true);
			verfuegung.setEventPublished(true);
			persistence.merge(verfuegung);
		});
	}
}
