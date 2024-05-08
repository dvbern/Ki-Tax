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
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.EingewoehnungDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;

@FunctionalInterface
public interface PensumMapper<T extends AbstractMahlzeitenPensum> {

	/**
	 * read a @{link ZeitabschnittDTO} and write the values to the @{link AbstractMahlzeitenPensum}
	 */
	void toAbstractMahlzeitenPensum(@Nonnull T target, @Nonnull ZeitabschnittDTO zeitabschnittDTO);

	@SafeVarargs
	@Nonnull
	static <T extends AbstractMahlzeitenPensum> PensumMapper<T> combine(@Nonnull PensumMapper<? super T>... mappers) {
		return (target, zeitabschnittDTO) -> Arrays.stream(mappers)
			.forEach(m -> m.toAbstractMahlzeitenPensum(target, zeitabschnittDTO));
	}

	static <T extends AbstractMahlzeitenPensum> PensumMapper<T> nop() {
		return (target, zeitabschnittDTO) -> {
		};
	}

	PensumMapper<AbstractMahlzeitenPensum> GUELTIGKEIT_MAPPER = (target, zeitabschnittDTO) -> {
		target.getGueltigkeit().setGueltigAb(zeitabschnittDTO.getVon());
		target.getGueltigkeit().setGueltigBis(zeitabschnittDTO.getBis());
	};

	PensumMapper<AbstractMahlzeitenPensum> KOSTEN_MAPPER = (target, zeitabschnittDTO) ->
		target.setMonatlicheBetreuungskosten(zeitabschnittDTO.getBetreuungskosten());

	PensumMapper<AbstractMahlzeitenPensum> BETREUTE_TAGE_MAPPER = (target, zeitabschnittDTO) ->
		target.setBetreuteTage(zeitabschnittDTO.getBetreuteTage());
}
