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
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractBetreuungsPensum;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
@AllArgsConstructor
public class PensumMapperFactory {

	@Inject
	private PensumValueMapperFactory pensumValueMapperFactory;
	@Inject
	private EingewoehnungMapperFactory eingewoehnungMapperFactory;

	@Nonnull
	public PensumMapper<Betreuungspensum> createForPlatzbestaetigung(@Nonnull ProcessingContext ctx) {
		return createPensumMapper(ctx);
	}

	@Nonnull
	public PensumMapper<BetreuungsmitteilungPensum> createForBetreuungsmitteilung(@Nonnull ProcessingContext ctx) {
		return createPensumMapper(ctx);
	}

	@Nonnull
	<T extends AbstractBetreuungsPensum> PensumMapper<T> createPensumMapper(@Nonnull ProcessingContext ctx) {
		if (ctx.getBetreuung().isAngebotMittagstisch()) {
			return (target, zeitabschnittDTO) -> {
				PensumMapper.GUELTIGKEIT_MAPPER.toAbstractMahlzeitenPensum(target, zeitabschnittDTO);
				target.setMonatlicheHauptmahlzeiten(zeitabschnittDTO.getAnzahlHauptmahlzeiten());
				target.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
				// this transformation should be at the end
				PensumUtil.transformMittagstischPensum(target);
			};
		}

		return PensumMapper.combine(
			PensumMapper.GUELTIGKEIT_MAPPER,
			PensumMapper.KOSTEN_MAPPER,
			BetreuteTageMapperFactory.createForBetreuteTage(ctx),
			pensumValueMapperFactory.createForPensum(ctx),
			// the following mappers are (currently) not possible for Mittagstisch
			eingewoehnungMapperFactory.createForEingewoehnung(ctx),
			MahlzeitVerguenstigungMapperFactory.createForMahlzeitenVerguenstigung(ctx),
			BetreuungInFerienzeitMapperFactory.createForBetreuungInFerienzeit(ctx)
		);
	}
}
