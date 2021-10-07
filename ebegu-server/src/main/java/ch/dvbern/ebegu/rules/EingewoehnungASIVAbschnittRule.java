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

package ch.dvbern.ebegu.rules;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;

public class EingewoehnungASIVAbschnittRule extends EingewoehnungAbschnittRule {

	protected EingewoehnungASIVAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		@Nonnull Integer maximalpensumFreiwilligenarbeit) {
		super(RuleValidity.ASIV, validityPeriod, locale, maximalpensumFreiwilligenarbeit);
	}

	@Nullable
	protected VerfuegungZeitabschnitt createZeitabschnittEingewoehnung(
		@Nonnull DateRange gueltigkeit,
		@Nonnull Erwerbspensum erwerbspensum, boolean isGesuchsteller1) {
		if (!erwerbspensum.getTaetigkeit().equals(Taetigkeit.FREIWILLIGENARBEIT)) {
			VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);

			zeitabschnitt.addTaetigkeitForAsivAndGemeinde(erwerbspensum.getTaetigkeit());
			if (isGesuchsteller1) {
				zeitabschnitt.setErwerbspensumGS1ForAsivAndGemeinde(erwerbspensum.getPensum());
			} else {
				zeitabschnitt.setErwerbspensumGS2ForAsivAndGemeinde(erwerbspensum.getPensum());
			}
			zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG, getLocale());

			return zeitabschnitt;
		}
		return null;
	}
}
