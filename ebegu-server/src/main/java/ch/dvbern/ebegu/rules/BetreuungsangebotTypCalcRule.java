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

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

/**
 * Regel für Betreuungsangebot: Es werden nur die Nicht-Schulamt-Angebote berechnet.
 */
public class BetreuungsangebotTypCalcRule extends AbstractCalcRule {

	public BetreuungsangebotTypCalcRule(DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BETREUUNGSANGEBOT_TYP, RuleType.REDUKTIONSREGEL, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(TAGESSCHULE);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		// bei tagesschule hat man grundsaetzlich 100 anspruch
		verfuegungZeitabschnitt.getBgCalculationResultAsiv().setAnspruchspensumProzent(100);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv().addBemerkung(RuleKey.BETREUUNGSANGEBOT_TYP, MsgKey.BETREUUNGSANGEBOT_MSG, getLocale());
		// Damit der Gesuchsteller im Entwurf die "richtigen" provisorischen Daten sieht, wird bei *noch* nicht akzeptiert
		// nicht auf Vollkosten gesetzt, erst beim eigentlichen Ablehnen
		if (platz.extractGesuch().getFinSitStatus() != null
			&& platz.extractGesuch().getFinSitStatus() == FinSitStatus.ABGELEHNT) {
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().setBezahltVollkosten(true);
		}
	}
}
