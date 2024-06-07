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

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class KitaPlusZuschlagCalcRule extends AbstractCalcRule {

	protected KitaPlusZuschlagCalcRule(
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale) {
		super(RuleKey.KITAPLUS_ZUSCHLAG, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	void executeRule(
			@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		Betreuung betreuung = (Betreuung) platz;
		boolean hasZuschlag = getHasKitaPlusZuschlagNullsafe(betreuung) && isKitaPlusZuschlagBestaetigt(betreuung);
		inputData.setKitaPlusZuschlag(hasZuschlag);
		if (hasZuschlag) {
			inputData.addBemerkung(MsgKey.KITAPLUS_ZUSCHLAG, getLocale());
		}
	}

	private boolean isKitaPlusZuschlagBestaetigt(Betreuung betreuung) {
		if (betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA() == null) {
			return false;
		}
		return betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA().isKitaPlusZuschlagBestaetigt();
	}

	private boolean getHasKitaPlusZuschlagNullsafe(Betreuung betreuung) {
		return Boolean.TRUE.equals(
				Objects.requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA())
						.getKitaPlusZuschlag());
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung kitaPlusZuschlagAktiv = einstellungMap.get(EinstellungKey.KITAPLUS_ZUSCHLAG_AKTIVIERT);
		return  kitaPlusZuschlagAktiv.getValueAsBoolean();
	}
}
