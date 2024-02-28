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

package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Regel für den Betreuungsangebot Typ. Schreibt nur diesen ins Zwischenresultat
 */
public class BetreuungsangebotTypAbschnittRule extends AbstractAbschnittRule {

	public BetreuungsangebotTypAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BETREUUNGSANGEBOT_TYP, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> betreuungspensumAbschnitte = new ArrayList<>();
		betreuungspensumAbschnitte.add(toVerfuegungZeitabschnitt(platz));
		return betreuungspensumAbschnitte;
	}

	@Nonnull
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(@Nonnull AbstractPlatz platz) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(platz.extractGesuchsperiode().getGueltigkeit());
		zeitabschnitt.setBetreuungsangebotTypForAsivAndGemeinde(platz.getBetreuungsangebotTyp());
		return zeitabschnitt;
	}
}
