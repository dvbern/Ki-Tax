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

import ch.dvbern.ebegu.entities.AbstractBetreuungsPensum;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import lombok.Value;

@Value
public class BetreuungInFerienzeitMapper implements PensumMapper<AbstractBetreuungsPensum> {

	private final ProcessingContext ctx;

	@Override
	public void toAbstractMahlzeitenPensum(
		@Nonnull AbstractBetreuungsPensum target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO
	) {
		target.setBetreuungInFerienzeit(zeitabschnittDTO.getBetreuungInFerienzeit());

		if (target.getBetreuungInFerienzeit() == null) {
			target.setVollstaendig(false);
			ctx.requireHumanConfirmation();
			ctx.addHumanConfirmationMessage("Betreuung in Ferienzeit ist nicht gesetzt. Das Kind hat EinschulungTyp "
				+ ctx.getBetreuung().getKind().getKindJA().getEinschulungTyp()
				+ ". Automatische Bestätigung nicht möglich.");
		}
	}
}
