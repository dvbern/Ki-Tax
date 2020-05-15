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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungParameter;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

/**
 * Regel die angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungTSCalcRule extends AbstractCalcRule {

	protected MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams;

	protected MahlzeitenverguenstigungTSCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		@Nonnull MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams
	) {

		super(RuleKey.MAHLZEITENVERGUENSTIGUNG, RuleType.GRUNDREGEL_CALC, RuleValidity.GEMEINDE, validityPeriod, locale);
		this.mahlzeitenverguenstigungParams = mahlzeitenverguenstigungParams;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(BetreuungsangebotTyp.TAGESSCHULE);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		// TODO KIBON-1233 prüfen, ob der Antragsteller eine Vergünstigung überhaupt gewünscht hat
		if (!mahlzeitenverguenstigungParams.isEnabled()) {
			return;
		}

		BigDecimal verguenstigung = mahlzeitenverguenstigungParams.getVerguenstigungProHauptmahlzeit()
			.get(inputData.getMassgebendesEinkommen());

		// Wenn die Vergünstigung pro Hauptmahlzeit grösser 0 ist
		if (verguenstigung != null && verguenstigung.compareTo(BigDecimal.ZERO) > 0) {

			BigDecimal kostenMitBetreuung = inputData.getTsInputMitBetreuung().getVerpflegungskosten();
			BigDecimal kostenOhneBetreuung = inputData.getTsInputOhneBetreuung().getVerpflegungskosten();

			if (kostenMitBetreuung.compareTo(BigDecimal.ZERO) > 0 ) {
				inputData.setTsVerpflegungskostenVerguenstigtMitBetreuung(kostenMitBetreuung.subtract(verguenstigung));
			}
			if (kostenOhneBetreuung.compareTo(BigDecimal.ZERO) > 0 ) {
				inputData.setTsVerpflegungskostenVerguenstigtOhneBetreuung(kostenOhneBetreuung.subtract(verguenstigung));
			}

			inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS_JA, getLocale(), verguenstigung);
		}
	}
}
