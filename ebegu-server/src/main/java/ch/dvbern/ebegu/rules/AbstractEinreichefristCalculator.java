/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.types.DateRange;

public abstract class AbstractEinreichefristCalculator {

	public LocalDate getStichtagEinreichefrist(@Nonnull LocalDate einreichedatum) {
		return einreichedatum.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
	}

	public void handleEinreichfrist(BGCalculationInput input, LocalDate mutationsEingansdatum) {
		//Wenn das Eingangsdatum der Meldung nach der Gültigkeit des Zeitabschnitts ist, soll das Flag ZuSpaetEingereicht gesetzt werden
		if(isMeldungZuSpaet(input.getParent().getGueltigkeit(), mutationsEingansdatum)) {
			input.setZuSpaetEingereicht(true);
		}
	}

	protected boolean isMeldungZuSpaet(@Nonnull DateRange gueltigkeit, @Nonnull LocalDate mutationsEingansdatum) {
		return !gueltigkeit.getGueltigAb().withDayOfMonth(1).isAfter((mutationsEingansdatum));
	}

	public boolean applyEinreichungsfristAbschnittStueckelung(@Nonnull AbstractPlatz platz) {
		// Die Frist ist per default relevant bei Erstgesuch und "Erst-Betreuung", also wenn ein Platz in einer Mutation
		// neu hinzugekommen ist. Es kann aber sein, dass es zwar einen Vorgaenger gab, dieser aber nicht
		// verfuegt wurde, darum reicht es nicht, nur die VorgaengerID zu pruefen!
		return platz.extractGesuch().getTyp().isGesuch() || Objects.isNull(platz.getVorgaengerVerfuegung());
	}
}
