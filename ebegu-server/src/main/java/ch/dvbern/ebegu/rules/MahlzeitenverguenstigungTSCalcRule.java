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
import ch.dvbern.ebegu.entities.Familiensituation;
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

		super(RuleKey.MAHLZEITENVERGUENSTIGUNG, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
		this.mahlzeitenverguenstigungParams = mahlzeitenverguenstigungParams;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(BetreuungsangebotTyp.TAGESSCHULE);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {

		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();

		boolean verguenstigungBeantrag = familiensituation == null ? false : !familiensituation.isKeineMahlzeitenverguenstigungBeantragt();

		if (!verguenstigungBeantrag) {
			return;
		}

		if (!mahlzeitenverguenstigungParams.isEnabled()) {
			return;
		}

		BigDecimal verguenstigung = mahlzeitenverguenstigungParams.getVerguenstigungProHauptmahlzeit()
			.get(inputData.getMassgebendesEinkommen());

		// Wenn die Vergünstigung pro Hauptmahlzeit grösser 0 ist
		if (verguenstigung != null && verguenstigung.compareTo(BigDecimal.ZERO) > 0) {

			BigDecimal kostenMitBetreuung = inputData.getTsInputMitBetreuung().getVerpflegungskosten();
			BigDecimal kostenOhneBetreuung = inputData.getTsInputOhneBetreuung().getVerpflegungskosten();
			int anzVerpflegungenMitBetreuung = inputData.getTsInputMitBetreuung().getAnzVerpflegungen();
			int anzVerpflegungenOhneBetreuung = inputData.getTsInputOhneBetreuung().getAnzVerpflegungen();

			if (kostenMitBetreuung.compareTo(BigDecimal.ZERO) > 0 ) {
				inputData.setTsVerpflegungskostenVerguenstigtMitBetreuung(kostenMitBetreuung
					.subtract(verguenstigung.multiply(BigDecimal.valueOf(anzVerpflegungenMitBetreuung))));
			}
			if (kostenOhneBetreuung.compareTo(BigDecimal.ZERO) > 0 ) {
				inputData.setTsVerpflegungskostenVerguenstigtOhneBetreuung(kostenOhneBetreuung
					.subtract(verguenstigung.multiply(BigDecimal.valueOf(anzVerpflegungenOhneBetreuung))));
			}

			inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_TS_JA, getLocale(), verguenstigung);
		}
	}
}
