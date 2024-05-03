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

package ch.dvbern.ebegu.inbox.handler;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;

public class ProcessingContext {

	@Nonnull
	private final Betreuung betreuung;
	@Nullable
	private final Betreuungsmitteilung latestOpenBetreuungsmitteilung;
	@Nonnull
	private final BetreuungEventDTO dto;
	@Nonnull
	private final DateRange gueltigkeitInPeriode;
	@Nonnull
	private final EventMonitor eventMonitor;

	@Nonnull
	private final Set<String> humanConfirmationMessages = new HashSet<>();

	private boolean isReadyForBestaetigen = true;

	private final boolean singleClientForPeriod;

	public ProcessingContext(
		@Nonnull Betreuung betreuung,
		@Nullable Betreuungsmitteilung latestOpenBetreuungsmitteilung,
		@Nonnull BetreuungEventDTO dto,
		@Nonnull DateRange clientGueltigkeitInPeriode,
		@Nonnull EventMonitor eventMonitor,
		boolean singleClientForPeriod) {
		this.betreuung = betreuung;
		this.latestOpenBetreuungsmitteilung = latestOpenBetreuungsmitteilung;
		this.dto = dto;
		this.gueltigkeitInPeriode = clientGueltigkeitInPeriode;
		this.eventMonitor = eventMonitor;
		this.singleClientForPeriod = singleClientForPeriod;
	}

	public void requireHumanConfirmation() {
		isReadyForBestaetigen = false;
	}

	@Nonnull
	public Betreuung getBetreuung() {
		return betreuung;
	}

	@Nullable
	public Betreuungsmitteilung getLatestOpenBetreuungsmitteilung() {
		return latestOpenBetreuungsmitteilung;
	}

	@Nonnull
	public BetreuungEventDTO getDto() {
		return dto;
	}

	@Nonnull
	public DateRange getGueltigkeitInPeriode() {
		return gueltigkeitInPeriode;
	}

	public boolean isGueltigkeitCoveringPeriode() {
		return gueltigkeitInPeriode.equals(betreuung.extractGesuchsperiode().getGueltigkeit());
	}

	public boolean isReadyForBestaetigen() {
		return isReadyForBestaetigen;
	}

	@Nonnull
	public String getHumanConfirmationMessages() {
		return String.join(", ", humanConfirmationMessages);
	}

	public void addHumanConfirmationMessage(@Nonnull String message) {
		this.humanConfirmationMessages.add(message);
	}

	@Nonnull
	public EventMonitor getEventMonitor() {
		return eventMonitor;
	}

	public boolean isSingleClientForPeriod() {
		return singleClientForPeriod;
	}
}
