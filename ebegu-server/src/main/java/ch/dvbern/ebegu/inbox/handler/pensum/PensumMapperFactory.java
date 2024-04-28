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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PensumMapperFactory {

	@Nonnull
	public static PensumMapper<Betreuungspensum> createForPlatzbestaetigung(@Nonnull ProcessingContext ctx) {
		return PensumMapper.combine(
			PensumMapperFactory.createPensumMapper(ctx),
			PensumMapper.BETREUUNG_IN_FERIENZEIT_MAPPER
		);
	}

	@Nonnull
	public static PensumMapper<BetreuungsmitteilungPensum> createForBetreuungsmitteilung(@Nonnull ProcessingContext ctx) {
		return PensumMapper.combine(PensumMapperFactory.createPensumMapper(ctx));
	}

	@Nonnull
	static PensumMapper<AbstractMahlzeitenPensum> createPensumMapper(@Nonnull ProcessingContext ctx) {
		if (ctx.getBetreuung().isAngebotMittagstisch()) {
			return PensumMapper.MITTAGSTISCH_MAPPER;
		}

		if (ctx.isMahlzeitVerguenstigungEnabled()) {
			return PensumMapper.combine(
				defaultMapper(ctx),
				new MahlzeitVerguenstigungMapper(ctx)
			);
		}

		return defaultMapper(ctx);
	}

	@Nonnull
	private static PensumMapper<AbstractMahlzeitenPensum> defaultMapper(@Nonnull ProcessingContext ctx) {
		PensumValueMapper pensumValueMapper = new PensumValueMapper(ctx.getMaxTageProMonat(), ctx.getMaxStundenProMonat());

		return PensumMapper.combine(
			PensumMapper.GUELTIGKEIT_MAPPER,
			PensumMapper.KOSTEN_MAPPER,
			pensumValueMapper,
			PensumMapper.EINGEWOEHNUNG_PAUSCHALE_MAPPER
		);
	}
}
