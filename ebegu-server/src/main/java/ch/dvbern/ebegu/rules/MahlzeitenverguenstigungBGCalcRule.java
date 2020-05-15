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
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungData;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Regel die angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungBGCalcRule extends AbstractCalcRule {

	protected MahlzeitenverguenstigungData mahlzeitenverguenstigungParams;

	protected MahlzeitenverguenstigungBGCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		@Nonnull MahlzeitenverguenstigungData mahlzeitenverguenstigungParams
	) {

		super(RuleKey.MAHLZEITENVERGUENSTIGUNG, RuleType.GRUNDREGEL_CALC, RuleValidity.GEMEINDE, validityPeriod, locale);
		this.mahlzeitenverguenstigungParams = mahlzeitenverguenstigungParams;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		// TODO KIBON-1233 prüfen, ob der Antragsteller eine Vergünstigung überhaupt gewünscht hat
		if (!mahlzeitenverguenstigungParams.isEnabled() || !validateInput(inputData)) {
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
			BigDecimal multiplier = getVerguenstigungToUse(verguenstigungProHauptmahlzeit,
				inputData.getTarifHauptmahlzeit(),
				mahlzeitenverguenstigungParams.getMinimalerElternbeitragHauptmahlzeit());

			// total vergünstigung berechnen
			BigDecimal verguenstigungTotal = inputData.getAnzahlHauptmahlzeiten().multiply(multiplier);

			verguenstigungTotal = verguenstigungTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO :
				verguenstigungTotal;

			// am schluss verrechnen wir den betrag noch mit dem BG Pensum und runden auf 5 Rappen
			verguenstigungTotal =
				MathUtil.roundToFrankenRappen(verguenstigungTotal.multiply(inputData.getBgPensumProzent()
					.divide(new BigDecimal(100))));

			inputData.getParent().setVerguenstigungHauptmahlzeitenTotal(verguenstigungTotal);
		}

		// Wenn die Vergünstigung pro Nebenmahlzeit grösser 0 ist
		if (verguenstigungProNebenmahlzeit != null && verguenstigungProNebenmahlzeit.compareTo(BigDecimal.ZERO) > 0) {

			// vergünstigung für Nebenmahlzeiten ist gegeben

			// vergünstigung pro hauptmahlzeit berechnen
			BigDecimal multiplier = getVerguenstigungToUse(verguenstigungProNebenmahlzeit,
				inputData.getTarifNebenmahlzeit(),
				mahlzeitenverguenstigungParams.getMinimalerElternbeitragNebenmahlzeit());

			// total vergünstigung berechnen
			BigDecimal verguenstigungTotal = inputData.getAnzahlNebenmahlzeiten().multiply(multiplier);
			verguenstigungTotal = verguenstigungTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO :
				verguenstigungTotal;

			// am schluss verrechnen wir den betrag noch mit dem BG Pensum und runden auf 5 Rappen
			verguenstigungTotal =
				MathUtil.roundToFrankenRappen(verguenstigungTotal.multiply(inputData.getBgPensumProzent())
					.divide(new BigDecimal(100)));

			inputData.getParent().setVerguenstigungNebenmahlzeitenTotal(verguenstigungTotal);
		}

		verguenstigungProHauptmahlzeit = verguenstigungProHauptmahlzeit == null ? BigDecimal.ZERO :	verguenstigungProHauptmahlzeit;
		verguenstigungProNebenmahlzeit = verguenstigungProNebenmahlzeit == null ? BigDecimal.ZERO : verguenstigungProNebenmahlzeit;

		if (verguenstigungProHauptmahlzeit.compareTo(BigDecimal.ZERO) > 0 ||
			verguenstigungProNebenmahlzeit.compareTo(verguenstigungProNebenmahlzeit) > 0) {
			addBemerkung(inputData, verguenstigungProHauptmahlzeit, verguenstigungProNebenmahlzeit);
		}
	}

	private BigDecimal getVerguenstigungToUse(BigDecimal verguenstigung, BigDecimal tarifProMahlzeit,
		BigDecimal minimalerElternbeitrag) {
		if ((tarifProMahlzeit.subtract(verguenstigung)).subtract(minimalerElternbeitrag).compareTo(BigDecimal.ZERO) >= 0) {
			return verguenstigung;
		}
		return tarifProMahlzeit.subtract(minimalerElternbeitrag);
	}

	private void addBemerkung(BGCalculationInput inputData, BigDecimal haupt, BigDecimal neben) {
		inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG_JA, getLocale(), haupt, neben);
	}

	private boolean validateInput(BGCalculationInput inputData) {
		return inputData.getAnzahlHauptmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getAnzahlNebenmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifHauptmahlzeit().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifNebenmahlzeit().compareTo(BigDecimal.ZERO) > 0;
	}
}
