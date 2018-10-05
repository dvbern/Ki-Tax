/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

public class GutscheineStartdatumAbschnittRule extends AbstractAbschnittRule {

	public GutscheineStartdatumAbschnittRule(
		@Nonnull RuleKey ruleKey,
		@Nonnull RuleType ruleType,
		@Nonnull DateRange validityPeriod) {
		super(ruleKey, ruleType, validityPeriod);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull Betreuung betreuung,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		LocalDate startdatum =
			betreuung.extractGesuch().getDossier().getGemeinde().getBetreuungsgutscheineStartdatum();

		return zeitabschnitte.stream()
			.filter(abschnitt -> !abschnitt.getGueltigkeit().endsBefore(startdatum))
			.map(abschnitt -> {
				DateRange gueltigkeit = abschnitt.getGueltigkeit();

				if (!gueltigkeit.contains(startdatum) || gueltigkeit.startsSameDay(startdatum)) {
					return abschnitt;
				}

				// falls aus einem Grund das Startdatum nicht auf den ersten Tag des Monats fallen sollte
				gueltigkeit.setGueltigAb(startdatum);

				return abschnitt;
			})
			.collect(Collectors.toList());
	}
}
