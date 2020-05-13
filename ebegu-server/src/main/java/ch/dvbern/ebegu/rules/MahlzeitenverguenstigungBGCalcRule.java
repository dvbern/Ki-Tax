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
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungData;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

/**
 * Sonderregel die nach der eigentlich Berechnung angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungBGCalcRule extends AbstractCalcRule {

	protected MahlzeitenverguenstigungData mahlzeitenverguenstigungParams;

	protected MahlzeitenverguenstigungBGCalcRule(
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		MahlzeitenverguenstigungData mahlzeitenverguenstigungParams
	) {
		super(RuleKey.MAHLZEITENVERGUENSTIGUNG, RuleType.GRUNDREGEL_CALC, ruleValidity, validityPeriod, locale);
		this.mahlzeitenverguenstigungParams = mahlzeitenverguenstigungParams;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		//falls vergünstigung aktiv ist
		if (!mahlzeitenverguenstigungParams.isEnabled()) {
			return;
		}

		BigDecimal verguenstigungProHauptmahlzeit = mahlzeitenverguenstigungParams.getVerguenstigungProHauptmahlzeit()
			.get(inputData.getMassgebendesEinkommen());
		BigDecimal verguenstigungProNebenmahlzeit = mahlzeitenverguenstigungParams.getVerguenstigungProNebenmahlzeit()
			.get(inputData.getMassgebendesEinkommen());

		// Wenn die Vergünstigung pro Hauptmahlzeit grösser 0 ist
		if (verguenstigungProHauptmahlzeit != null && verguenstigungProHauptmahlzeit.compareTo(BigDecimal.ZERO) > 0) {

			// vergünstigung für Hauptmahlzeiten ist gegeben

			// vergünstigung pro hauptmahlzeit berechnen
			BigDecimal multiplier = getTarifToUse(verguenstigungProHauptmahlzeit,
				mahlzeitenverguenstigungParams.getTarifProHauptmahlzeit(),
				mahlzeitenverguenstigungParams.getMinimalerElternbeitragHauptmahlzeit());

			// total vergünstigung berechnen
			BigDecimal verguenstigungTotal =
				MathUtil.roundToFrankenRappen(inputData.getAnzahlHauptmahlzeiten().multiply(multiplier));
			inputData.getParent().getBgCalculationResultGemeinde().setVerguenstigungHauptmahlzeitenTotal(verguenstigungTotal);
		}

		// Wenn die Vergünstigung pro Nebenmahlzeit grösser 0 ist
		if (verguenstigungProNebenmahlzeit != null && verguenstigungProNebenmahlzeit.compareTo(BigDecimal.ZERO) > 0) {

			// vergünstigung für Nebenmahlzeiten ist gegeben

			// vergünstigung pro hauptmahlzeit berechnen
			BigDecimal multiplier = getTarifToUse(verguenstigungProNebenmahlzeit,
				mahlzeitenverguenstigungParams.getTarifProNebenmahlzeit(),
				mahlzeitenverguenstigungParams.getMinimalerElternbeitragNebenmahlzeit());

			// total vergünstigung berechnen
			BigDecimal verguenstigungTotal =
				MathUtil.roundToFrankenRappen(inputData.getAnzahlNebenmahlzeiten().multiply(multiplier));
			inputData.getParent().getBgCalculationResultGemeinde().setVerguenstigungNebenmahlzeitenTotal(verguenstigungTotal);
		}
	}

	private BigDecimal getTarifToUse(BigDecimal verguenstigung, BigDecimal tarifProMahlzeit, BigDecimal minimalerElternbeitrag) {
		if ((tarifProMahlzeit.subtract(verguenstigung)).subtract(minimalerElternbeitrag).compareTo(BigDecimal.ZERO) >= 0) {
			return verguenstigung;
		}
		return tarifProMahlzeit.subtract(minimalerElternbeitrag);
	}

	private void addBemerkung(BGCalculationInput inputData) {
		inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG_JA, getLocale());
	}
}
