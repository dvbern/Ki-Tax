/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.kibon.exchange.commons.tagesschulen.AbholungTagesschule;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulAuswahlDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleBestaetigungEventDTO;
import ch.dvbern.kibon.exchange.commons.types.Intervall;
import ch.dvbern.kibon.exchange.commons.types.Wochentag;

public final class AnmeldungTestUtil {

	public static final String REFNR = "20.007305.002.1.3";

	private AnmeldungTestUtil() {
	}

	@Nonnull
	public static TagesschuleBestaetigungEventDTO createTagesschuleBestaetigungEventDTO() {
		TagesschuleBestaetigungEventDTO dto = new TagesschuleBestaetigungEventDTO();
		dto.setRefnr(REFNR);
		dto.setBemerkung("Test");
		dto.setAbholung(AbholungTagesschule.ALLEINE_NACH_HAUSE);
		dto.setEintrittsdatum(LocalDate.of(19, 8, 1));
		dto.setModule(new ArrayList<>());

		return dto;
	}

	@Nonnull
	public static ModulAuswahlDTO createModulAuswahlDTO(@Nonnull BelegungTagesschuleModul modul) {
		return createModulAuswahlDTO(modul.getModulTagesschule());
	}

	@Nonnull
	public static ModulAuswahlDTO createModulAuswahlDTO(@Nonnull ModulTagesschule modulTagesschule) {
		String id = modulTagesschule.getModulTagesschuleGroup().getId();

		return createModulAuswahlDTO(id, modulTagesschule.getWochentag());
	}

	@Nonnull
	public static ModulAuswahlDTO createModulAuswahlDTO(@Nonnull String modulId, @Nonnull DayOfWeek weekday) {
		ModulAuswahlDTO modulAuswahlDTO = new ModulAuswahlDTO();
		modulAuswahlDTO.setModulId(modulId);
		modulAuswahlDTO.setWochentag(Wochentag.valueOf(weekday.name()));
		modulAuswahlDTO.setIntervall(Intervall.WOECHENTLICH);

		return modulAuswahlDTO;
	}
}
