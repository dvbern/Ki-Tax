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

import java.time.LocalDate;
import java.util.ArrayList;

import ch.dvbern.kibon.exchange.commons.tagesschulen.AbholungTagesschule;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulAuswahlDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleBestaetigungEventDTO;
import ch.dvbern.kibon.exchange.commons.types.Intervall;

public final class AnmeldungTestUtil {

	public static TagesschuleBestaetigungEventDTO createTagesschuleBestaetigungEventDTO() {
		TagesschuleBestaetigungEventDTO dto = new TagesschuleBestaetigungEventDTO();
		dto.setRefnr("20.007305.002.1.3");
		dto.setBemerkung("Test");
		dto.setAbholung(AbholungTagesschule.ALLEINE_NACH_HAUSE);
		dto.setEintrittsdatum(LocalDate.of(19, 8, 1));
		dto.setModule(new ArrayList<>());
		return dto;
	}

	public static ModulAuswahlDTO createModulAuswahlDTO(String modulId, int weekday) {
		ModulAuswahlDTO modulAuswahlDTO = new ModulAuswahlDTO();
		modulAuswahlDTO.setModulId(modulId);
		modulAuswahlDTO.setWeekday(weekday);
		modulAuswahlDTO.setIntervall(Intervall.WOECHENTLICH);
		return modulAuswahlDTO;
	}
}
