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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;

public class PlatzbestaetigungProcessingContext {

	@Nonnull
	private final Betreuung betreuung;
	@Nonnull
	private final BetreuungEventDTO dto;

	private boolean isReadyForBestaetigen = true;

	public PlatzbestaetigungProcessingContext(
		@Nonnull Betreuung betreuung,
		@Nonnull BetreuungEventDTO dto) {
		this.betreuung = betreuung;
		this.dto = dto;
	}

	public void requireHumanConfirmation() {
		isReadyForBestaetigen = false;
	}

	@Nonnull
	public Betreuung getBetreuung() {
		return betreuung;
	}

	@Nonnull
	public BetreuungEventDTO getDto() {
		return dto;
	}

	public boolean isReadyForBestaetigen() {
		return isReadyForBestaetigen;
	}
}
