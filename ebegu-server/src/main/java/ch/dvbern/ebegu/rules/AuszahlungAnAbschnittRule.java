/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Regel für die Erstellung der Zeitabschnitte der Betreuungspensen
 * Verweis 16.9.3
 */
public class AuszahlungAnAbschnittRule extends AbstractAbschnittRule {


	public AuszahlungAnAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BETREUUNGSPENSUM, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);

	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Nonnull
	@Override
	@SuppressWarnings("PMD.NcssMethodCount")
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		Betreuung betreuung = (Betreuung) platz;
		Gesuchsperiode gesuchsperiode = platz.extractGesuchsperiode();

		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gesuchsperiode.getGueltigkeit());
		zeitabschnitt.setAuszahlungAnEltern(betreuung.isAuszahlungAnEltern());

		return new ArrayList<>(Collections.singleton(zeitabschnitt));
	}
}
