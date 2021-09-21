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

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PlatzablehnenEventHandler extends BaseEventHandler<String> {

	private static final Logger LOG = LoggerFactory.getLogger(PlatzablehnenEventHandler.class);

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;

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

	private Processing attemptProcessing(EventMonitor eventMonitor) {
		return betreuungService.findBetreuungByBGNummer(eventMonitor.getRefnr(), false)
			.map(betreuung -> processEventForAblehnung(eventMonitor, betreuung))
			.orElseGet(() -> Processing.failure("Betreuung nicht gefunden."));
	}

	private Processing processEventForAblehnung(EventMonitor eventMonitor, Betreuung betreuung) {

		if (betreuung.extractGesuchsperiode().getStatus() != GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (eventMonitor.isTooLate(betreuung.getTimestampMutiert())) {
			return Processing.failure("Die Betreuung wurde verändert, nachdem das AblehnungEvent generiert wurde.");
		}


		return Processing.success();
	}
}
