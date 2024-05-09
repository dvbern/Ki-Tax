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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.EingewoehnungDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class EingewoehnungMapper implements PensumMapper<AbstractMahlzeitenPensum> {

	private static final Logger LOG = LoggerFactory.getLogger(EingewoehnungMapper.class);

	private final ProcessingContext ctx;

	private final Validator validator;

	@Override
	public void toAbstractMahlzeitenPensum(
		@Nonnull AbstractMahlzeitenPensum target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO
	) {
		EingewoehnungDTO dto = zeitabschnittDTO.getEingewoehnung();
		if (dto == null) {
			target.setEingewoehnungPauschale(null);

			return;
		}

		EingewoehnungPauschale eingewoehnung = new EingewoehnungPauschale();
		eingewoehnung.setPauschale(dto.getKosten());
		eingewoehnung.getGueltigkeit().setGueltigAb(dto.getVon());
		eingewoehnung.getGueltigkeit().setGueltigBis(dto.getBis());
		Set<ConstraintViolation<EingewoehnungPauschale>> constraintViolations = validator.validate(eingewoehnung);
		if (constraintViolations.isEmpty()) {
			target.setEingewoehnungPauschale(eingewoehnung);
		} else {
			target.setVollstaendig(false);
			ctx.requireHumanConfirmation();
			ctx.addHumanConfirmationMessage("Die Eingewöhnung-Daten sind ungültig: " + constraintViolations);
			String refnr = ctx.getDto().getRefnr();
			LOG.info("PlatzbestaetigungEvent fuer Betreuung mit RefNr: {} hat eine ungültige Eingewöhnung", refnr);
		}
	}
}
