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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.inbox.handler;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.types.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AnmeldungAblehnenEventHandler extends BaseEventHandler<String> {

	private static final Logger LOG = LoggerFactory.getLogger(AnmeldungAblehnenEventHandler.class);

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;

	@Inject
	private BetreuungEventHelper betreuungEventHelper;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String key,
		@Nonnull String dto,
		@Nonnull String clientName) {
		EventMonitor eventMonitor = new EventMonitor(betreuungMonitoringService, eventTime, key, clientName);
		Processing processing = attemptProcessing(eventMonitor);

		if (!processing.isProcessingSuccess()) {
			String message = processing.getMessage();
			LOG.warn("Ablehnung Event für Betreuung mit RefNr: {} nicht verarbeitet: {}", key, message);
			eventMonitor.record("Ablehnung Event wurde nicht verarbeitet: " + message);
		}
	}

	protected Processing attemptProcessing(EventMonitor eventMonitor) {
		return betreuungService.findAnmeldungenTagesschuleByBGNummer(eventMonitor.getRefnr())
			.map(anmeldungTagesschule -> processEventForAblehnung(eventMonitor, anmeldungTagesschule))
			.orElseGet(() -> Processing.failure("AnmeldungTagesschule nicht gefunden."));
	}

	private Processing processEventForAblehnung(EventMonitor eventMonitor, AnmeldungTagesschule anmeldungTagesschule) {

		if (anmeldungTagesschule.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (eventMonitor.isTooLate(anmeldungTagesschule.getTimestampMutiert())) {
			return Processing.failure(
				"Die AnmeldungTagesschule wurde verändert, nachdem das AblehnungEvent generiert wurde.");
		}

		return betreuungEventHelper.getExternalClient(eventMonitor.getClientName(), anmeldungTagesschule)
			.map(client -> processEventForExternalClient(eventMonitor, anmeldungTagesschule, client.getGueltigkeit()))
			.orElseGet(() -> betreuungEventHelper.clientNotFoundFailure(
				eventMonitor.getClientName(),
				anmeldungTagesschule));
	}

	private Processing processEventForExternalClient(
		EventMonitor eventMonitor,
		AnmeldungTagesschule anmeldungTagesschule,
		DateRange clientGueltigkeit) {
		DateRange gesuchsperiode = anmeldungTagesschule.extractGesuchsperiode().getGueltigkeit();
		Optional<DateRange> overlap = gesuchsperiode.getOverlap(clientGueltigkeit);
		if (overlap.isEmpty()) {
			return Processing.failure("Der Client hat innerhalb der Periode keine Berechtigung.");
		}

		if (isAblehnungErblaubtStatus(anmeldungTagesschule.getBetreuungsstatus())) {
			this.betreuungService.anmeldungSchulamtAblehnen(anmeldungTagesschule);
			LOG.info("Tagesschuleanmeldung mit RefNr: {} wurde automatisch abgelehnt", eventMonitor.getRefnr());
			eventMonitor.record("Tagesschuleanmeldung wurde automatisch abgelehnt");

			return Processing.success();
		}

		return Processing.failure("Die AnmeldungTagesschule hat einen ungültigen Status: "
			+ anmeldungTagesschule.getBetreuungsstatus());
	}

	protected boolean isAblehnungErblaubtStatus(@Nonnull Betreuungsstatus status) {
		return status == Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST
			|| status == Betreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION;
	}

}
