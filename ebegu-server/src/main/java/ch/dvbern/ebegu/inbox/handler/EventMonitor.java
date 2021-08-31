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
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

public class EventMonitor {

	@Nonnull
	private final BetreuungMonitoringService betreuungMonitoringService;

	@Nonnull
	private final LocalDateTime eventTime;

	@Nonnull
	private final String refnr;

	@Nonnull
	private final String clientName;

	public EventMonitor(
		@Nonnull BetreuungMonitoringService betreuungMonitoringService,
		@Nonnull LocalDateTime eventTime,
		@Nonnull String refnr,
		@Nonnull String clientName) {
		this.betreuungMonitoringService = betreuungMonitoringService;
		this.eventTime = eventTime;
		this.refnr = refnr;
		this.clientName = clientName;
	}

	public boolean isTooLate(@Nullable LocalDateTime lastModification) {
		return lastModification != null && lastModification.isAfter(eventTime);
	}

	@CanIgnoreReturnValue
	@Nonnull
	public BetreuungMonitoring record(@Nonnull String msg) {
		BetreuungMonitoring betreuungMonitoring = new BetreuungMonitoring(refnr, clientName, msg, LocalDateTime.now());

		return betreuungMonitoringService.saveBetreuungMonitoring(betreuungMonitoring);
	}

	@SuppressWarnings("OverloadedVarargsMethod")
	@CanIgnoreReturnValue
	@Nonnull
	public BetreuungMonitoring record(@Nonnull String format, @Nonnull Object... args) {
		return record(String.format(format, args));
	}

	@Nonnull
	public LocalDateTime getEventTime() {
		return eventTime;
	}

	@Nonnull
	public String getRefnr() {
		return refnr;
	}

	@Nonnull
	public String getClientName() {
		return clientName;
	}
}
