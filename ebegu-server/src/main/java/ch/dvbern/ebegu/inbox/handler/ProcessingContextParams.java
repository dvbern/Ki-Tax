/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.betreuung.BetreuungEinstellungen;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import lombok.Value;

@Value
public class ProcessingContextParams {

	@Nonnull
	BetreuungEventDTO dto;

	@Nonnull
	BetreuungEinstellungen einstellungen;

	@Nonnull
	EventMonitor eventMonitor;

	boolean singleClientForPeriod;

	@Nonnull
	DateRange gueltigkeitInPeriode;
}
