/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner.kitax;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Regelwerk;
import ch.dvbern.ebegu.rechner.AbstractBernRechner;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;

public class EmptyKitaxBernRechner extends AbstractBernRechner {

	private Locale locale;
	private MsgKey bemerkung;

	public EmptyKitaxBernRechner(@Nonnull Locale locale, MsgKey bemerkung) {
		this.locale = locale;
		this.bemerkung = bemerkung;
	}

	@Nonnull
	@Override
	protected Optional<BGCalculationResult> calculateGemeinde(@Nonnull BGCalculationInput input, @Nonnull BGRechnerParameterDTO parameterDTO) {
		return Optional.empty();
	}

	@Nonnull
	@Override
	protected BGCalculationResult calculateAsiv(@Nonnull BGCalculationInput input, @Nonnull BGRechnerParameterDTO parameterDTO) {
		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(input, result);

		input.getParent().setRegelwerk(Regelwerk.FEBR);
		result.setAnspruchZeroAndSaveRestanspruch();
		result.setVerguenstigungMahlzeitenTotal(BigDecimal.ZERO);
		// Wir loeschen alle Bemerkungen, die den Zeitraum nach dem Stichtag betreffen
		input.getParent().getBemerkungenDTOList().clear();
		// Bemerkung
		input.addBemerkung(bemerkung, locale);
		return result;
	}
}
