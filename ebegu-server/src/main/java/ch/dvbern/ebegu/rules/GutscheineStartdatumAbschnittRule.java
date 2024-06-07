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

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GutscheineStartdatumAbschnittRule extends AbstractAbschnittRule {

	public GutscheineStartdatumAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BEGU_STARTDATUM, RuleType.REDUKTIONSREGEL, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz) {

		List<VerfuegungZeitabschnitt> betreuungspensumAbschnitte = new ArrayList<>();

		LocalDate startdatum =
			platz.extractGesuch().getDossier().getGemeinde().getBetreuungsgutscheineStartdatum();

		DateRange gueltigkeit = platz.extractGesuchsperiode().getGueltigkeit();

		if (gueltigkeit.endsBefore(startdatum)) {
			betreuungspensumAbschnitte.add(createZeitabschnitt(gueltigkeit, false));

		} else if (gueltigkeit.startsSameDay(startdatum) || gueltigkeit.startsAfter(startdatum)) {
			betreuungspensumAbschnitte.add(createZeitabschnitt(gueltigkeit, true));

		} else if (startdatum.isAfter(gueltigkeit.getGueltigAb())
			&& (startdatum.isBefore(gueltigkeit.getGueltigBis()) || startdatum.isEqual(gueltigkeit.getGueltigBis()))) {

			DateRange precreedingRange = new DateRange(gueltigkeit.getGueltigAb(), startdatum.minusDays(1));
			betreuungspensumAbschnitte.add(createZeitabschnitt(precreedingRange, false));

			DateRange validRange = new DateRange(startdatum, gueltigkeit.getGueltigBis());
			betreuungspensumAbschnitte.add(createZeitabschnitt(validRange, true));
		}

		return betreuungspensumAbschnitte;
	}

	private VerfuegungZeitabschnitt createZeitabschnitt(
		@Nonnull DateRange dateRange,
		boolean abschnittLiegtNachBEGUStartdatum) {

		VerfuegungZeitabschnitt abschnitt = createZeitabschnittWithinValidityPeriodOfRule(dateRange);
		abschnitt.setAbschnittLiegtNachBEGUStartdatumForAsivAndGemeinde(abschnittLiegtNachBEGUStartdatum);

		return abschnitt;
	}
}
