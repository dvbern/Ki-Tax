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

package ch.dvbern.ebegu.rules.mutationsmerger;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Objects;

public class MutationsMergerFinanzielleSituationLuzern extends AbstractMutationsMergerFinanzielleSituation {

	public MutationsMergerFinanzielleSituationLuzern(Locale local) {
		super(local);
	}

	@Override
	protected void handleEinkommen(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {

		LocalDate finSitGueltigAb = platz.extractGesuch().getFinSitAenderungStartDatum();

		if(!isFinSitGueltigInZeitabschnitt(finSitGueltigAb, inputAktuel.getParent())) {
			//Wenn FinSit Daten noch nicht gültig sind in Zeitabschnitt, sollen die Daten aus dem Vorgänger genommen werden
			setFinSitDataFromResultToInput(inputAktuel, resultVorgaenger);
		}
	}


	private boolean isFinSitGueltigInZeitabschnitt(LocalDate finSitGueltigAb, VerfuegungZeitabschnitt zeitabschnitt) {
		Objects.requireNonNull(finSitGueltigAb);

		//finSitGueltigAb, wird erst im Folgemonat berücksitigt
		LocalDate firstDayWhenFitSitIsGueltig = finSitGueltigAb
			.plusMonths(1)
			.with(TemporalAdjusters.firstDayOfMonth());

		return firstDayWhenFitSitIsGueltig.isBefore(zeitabschnitt.getGueltigkeit().getGueltigAb()) ||
			firstDayWhenFitSitIsGueltig.isEqual(zeitabschnitt.getGueltigkeit().getGueltigAb());
	}
}
