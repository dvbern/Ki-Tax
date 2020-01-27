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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static java.util.Objects.requireNonNull;

/**
 * Regel für Abwesenheiten. Sie beachtet:
 * - Ab dem 31. Tag einer Abwesenheit (Krankheit oder Unfall des Kinds und bei Mutterschaft ausgeschlossen) entfällt der Gutschein.
 * Der Anspruch bleibt in dieser Zeit bestehen. D.h. ab dem 31. Tag einer Abwesenheit, wird den Eltern der Volltarif verrechnet.
 * - Hier wird mit Tagen und nicht mit Nettoarbeitstage gerechnet. D.h. eine Abwesenheit von 30 Tagen ist ok. Beim 31. Tag entfällt der Gutschein.
 * - Wann dieses Ereignis gemeldet wird, spielt keine Rolle.
 * Verweis 16.14.4
 */
public class AbwesenheitCalcRule extends AbstractCalcRule {

	public AbwesenheitCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.ABWESENHEIT, RuleType.REDUKTIONSREGEL, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return Arrays.asList(KITA, TAGESFAMILIEN);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		requireNonNull(platz.getBetreuungsangebotTyp());
		if (verfuegungZeitabschnitt.getBgCalculationInputAsiv().isLongAbwesenheit()) {
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().setBezahltVollkosten(true);
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().addBemerkung(RuleKey.ABWESENHEIT, MsgKey.ABWESENHEIT_MSG, getLocale());
		}
	}
}
