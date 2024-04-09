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
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.util.EbeguUtil.coalesce;
import static java.math.BigDecimal.ZERO;

@Value
public class MahlzeitVerguenstigungMapper implements PensumMapper {

	private static final Logger LOG = LoggerFactory.getLogger(MahlzeitVerguenstigungMapper.class);

	private final ProcessingContext ctx;

	@Override
	public void toAbstractMahlzeitenPensum(
		@Nonnull AbstractMahlzeitenPensum target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO
	) {
		if (ctx.isMahlzeitVerguenstigungEnabled()) {
			target.setMonatlicheHauptmahlzeiten(coalesce(zeitabschnittDTO.getAnzahlHauptmahlzeiten(), ZERO));
			target.setMonatlicheNebenmahlzeiten(coalesce(zeitabschnittDTO.getAnzahlNebenmahlzeiten(), ZERO));

			setTarifeProMahlzeiten(target, zeitabschnittDTO, ctx);
		}
	}

	private static <T extends AbstractMahlzeitenPensum> void setTarifeProMahlzeiten(
		@Nonnull T target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull ProcessingContext ctx) {

		// Die Mahlzeitkosten koennen null sein, wir nehmen dann die default Werten
		if (zeitabschnittDTO.getTarifProHauptmahlzeiten() != null) {
			target.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
		} else {
			target.setVollstaendig(false);
			ctx.requireHumanConfirmation();
			LOG.info(
				"PlatzbestaetigungEvent fuer Betreuung mit RefNr: {} hat kein Hauptmahlzeiten Tarif",
				ctx.getDto().getRefnr());
			ctx.setHumanConfirmationMessage("PlatzbestaetigungEvent hat keinen Hauptmahlzeiten Tarif");
		}
		if (zeitabschnittDTO.getTarifProNebenmahlzeiten() != null) {
			target.setTarifProNebenmahlzeit(zeitabschnittDTO.getTarifProNebenmahlzeiten());
		} else {
			target.setVollstaendig(false);
			ctx.requireHumanConfirmation();
			LOG.info(
				"PlatzbestaetigungEvent fuer Betreuung mit RefNr: {} hat kein Nebenmahlzeiten Tarif",
				ctx.getDto().getRefnr());
			ctx.setHumanConfirmationMessage("PlatzbestaetigungEvent hat keinen Nebenmahlzeiten Tarif");
		}
	}
}
