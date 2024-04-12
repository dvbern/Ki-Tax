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

import java.util.Arrays;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.EingewoehnungDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;

@FunctionalInterface
public interface PensumMapper {

	/**
	 * read a @{link ZeitabschnittDTO} and write the values to the @{link AbstractMahlzeitenPensum}
	 */
	void toAbstractMahlzeitenPensum(@Nonnull AbstractMahlzeitenPensum target, @Nonnull ZeitabschnittDTO zeitabschnittDTO);

	@Nonnull
	static PensumMapper combine(@Nonnull PensumMapper... mappers) {
		return (target, zeitabschnittDTO) -> Arrays.stream(mappers)
			.forEach(m -> m.toAbstractMahlzeitenPensum(target, zeitabschnittDTO));
	}

	PensumMapper GUELTIGKEIT_MAPPER = (target, zeitabschnittDTO) -> {
		target.getGueltigkeit().setGueltigAb(zeitabschnittDTO.getVon());
		target.getGueltigkeit().setGueltigBis(zeitabschnittDTO.getBis());
	};

	PensumMapper KOSTEN_MAPPER = (target, zeitabschnittDTO) ->
		target.setMonatlicheBetreuungskosten(zeitabschnittDTO.getBetreuungskosten());

	PensumMapper MITTAGSTISCH_MAPPER = (target, zeitabschnittDTO) -> {
		GUELTIGKEIT_MAPPER.toAbstractMahlzeitenPensum(target, zeitabschnittDTO);
		target.setMonatlicheHauptmahlzeiten(zeitabschnittDTO.getAnzahlHauptmahlzeiten());
		target.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
		PensumUtil.transformMittagstischPensum(target);
	};

	PensumMapper EINGEWOEHNUNG_PAUSCHALE_MAPPER = (target, zeitabschnittDTO) -> {
		EingewoehnungDTO eingewoehnung = zeitabschnittDTO.getEingewoehnung();
		if (eingewoehnung == null) {
			target.setEingewoehnungPauschale(null);

			return;
		}

		EingewoehnungPauschale pauschale = new EingewoehnungPauschale();
		pauschale.setPauschale(eingewoehnung.getPauschale());
		pauschale.getGueltigkeit().setGueltigAb(eingewoehnung.getVon());
		pauschale.getGueltigkeit().setGueltigBis(eingewoehnung.getBis());
		target.setEingewoehnungPauschale(pauschale);
	};
}
