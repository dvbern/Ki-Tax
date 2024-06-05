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

import ch.dvbern.ebegu.betreuung.BetreuungEinstellungen;
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
	private final ProcessingContextParams params;

	@Nonnull
	private final Set<String> humanConfirmationMessages = new HashSet<>();

	private boolean isReadyForBestaetigen = true;

	public ProcessingContext(
		@Nonnull Betreuung betreuung,
		@Nullable Betreuungsmitteilung latestOpenBetreuungsmitteilung,
		@Nonnull ProcessingContextParams params
	) {
		this.betreuung = betreuung;
		this.latestOpenBetreuungsmitteilung = latestOpenBetreuungsmitteilung;
		this.params = params;
	}

	public void requireHumanConfirmation() {
		isReadyForBestaetigen = false;
	}

	@Nonnull
	public Betreuung getBetreuung() {
		return betreuung;
	}

	@Nonnull
	public BetreuungEinstellungen getEinstellungen() {
		return params.getEinstellungen();
	}

	@Nullable
	public Betreuungsmitteilung getLatestOpenBetreuungsmitteilung() {
		return latestOpenBetreuungsmitteilung;
	}

	@Nonnull
	public ProcessingContextParams getParams() {
		return params;
	}

	@Nonnull
	public BetreuungEventDTO getDto() {
		return params.getDto();
	}

	@Nonnull
	public DateRange getGueltigkeitInPeriode() {
		return params.getGueltigkeitInPeriode();
	}

	public boolean isGueltigkeitCoveringPeriode() {
		return getGueltigkeitInPeriode().equals(betreuung.extractGesuchsperiode().getGueltigkeit());
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
		return params.getEventMonitor();
	}

	public boolean isSingleClientForPeriod() {
		return params.isSingleClientForPeriod();
	}
}
