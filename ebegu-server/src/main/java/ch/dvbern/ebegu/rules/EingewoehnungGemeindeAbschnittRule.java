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
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;

public class EingewoehnungGemeindeAbschnittRule extends EingewoehnungAbschnittRule {

	private final Integer maximalpensumFreiwilligenarbeit;

	protected EingewoehnungGemeindeAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		@Nonnull Integer maximalpensumFreiwilligenarbeit) {
		super(RuleValidity.GEMEINDE, validityPeriod, locale, maximalpensumFreiwilligenarbeit);
		this.maximalpensumFreiwilligenarbeit = maximalpensumFreiwilligenarbeit;
	}

	@Nullable
	protected VerfuegungZeitabschnitt createZeitabschnittEingewoehnung(
		@Nonnull DateRange gueltigkeit,
		@Nonnull Erwerbspensum erwerbspensum, boolean isGesuchsteller1) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		if (erwerbspensum.getTaetigkeit().equals(Taetigkeit.FREIWILLIGENARBEIT)) {
			BGCalculationInput inputGemeinde = zeitabschnitt.getBgCalculationInputGemeinde();
			Integer limitedPensum = erwerbspensum.getPensum();
			if (limitedPensum > maximalpensumFreiwilligenarbeit) {
				limitedPensum = maximalpensumFreiwilligenarbeit;
			}
			if (limitedPensum > 0) {
				if (isGesuchsteller1) {
					inputGemeinde.setErwerbspensumGS1(limitedPensum);
				} else {
					inputGemeinde.setErwerbspensumGS2(limitedPensum);
				}
				inputGemeinde.getTaetigkeiten().add(erwerbspensum.getTaetigkeit());
				inputGemeinde.addBemerkung(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG, getLocale());
				zeitabschnitt.setHasGemeindeSpezifischeBerechnung(true);
			}
		}
		return zeitabschnitt;
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		// Die Regel muss beachtet werden, wenn das PensumFreiwilligenarbeit > 0 ist
		Einstellung param_MaxAbzugFreiwilligenarbeit =
			einstellungMap.get(EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT);
		Objects.requireNonNull(
			param_MaxAbzugFreiwilligenarbeit,
			"Parameter GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT muss gesetzt sein");
		return param_MaxAbzugFreiwilligenarbeit.getValueAsInteger() > 0;
	}
}
