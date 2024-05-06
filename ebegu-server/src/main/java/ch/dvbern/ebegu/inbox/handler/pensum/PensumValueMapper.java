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

package ch.dvbern.ebegu.inbox.handler.pensum;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import lombok.Value;

@Value
public class PensumValueMapper implements PensumMapper<AbstractMahlzeitenPensum> {

	private final BigDecimal maxTageProMonat;
	private final BigDecimal maxStundenProMonat;

	@Override
	public void toAbstractMahlzeitenPensum(
		@Nonnull AbstractMahlzeitenPensum target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO
	) {
		switch (zeitabschnittDTO.getPensumUnit()) {
		case DAYS:
			target.applyPensumFromDays(zeitabschnittDTO.getBetreuungspensum(), maxTageProMonat);
			return;
		case HOURS:
			target.applyPensumFromHours(zeitabschnittDTO.getBetreuungspensum(), maxStundenProMonat);
			return;
		case PERCENTAGE:
			target.applyPensumFromPercentage(zeitabschnittDTO.getBetreuungspensum());
			return;
		default:
			throw new IllegalArgumentException("Unsupported pensum unit: " + zeitabschnittDTO.getPensumUnit());
		}
	}
}
