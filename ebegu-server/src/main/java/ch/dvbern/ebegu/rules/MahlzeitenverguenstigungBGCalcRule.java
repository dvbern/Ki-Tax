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
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.util.MahlzeitenverguenstigungParameter;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;

/**
 * Regel die angewendet wird um die Mahlzeitenvergünstigung zu berechnen
 */
public final class MahlzeitenverguenstigungBGCalcRule extends AbstractCalcRule {

	private final MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams;

	protected MahlzeitenverguenstigungBGCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		@Nonnull MahlzeitenverguenstigungParameter mahlzeitenverguenstigungParams
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

		Familiensituation familiensituation = platz.extractGesuch().extractFamiliensituation();

		boolean verguenstigungBeantrag = familiensituation != null && !familiensituation.isKeineMahlzeitenverguenstigungBeantragt();

		if (!verguenstigungBeantrag) {
			return;
		}

		final BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		final boolean sozialhilfeempfaenger = inputData.isSozialhilfeempfaenger();

		if (!mahlzeitenverguenstigungParams.isEnabled() ||
			!validateInput(inputData) ||
			!mahlzeitenverguenstigungParams.hasAnspruch(massgebendesEinkommen, sozialhilfeempfaenger)) {
			return;
		}

		BigDecimal verguenstigungProHauptmahlzeit =
			mahlzeitenverguenstigungParams.getVerguenstigungProHauptmahlzeitWithParam(massgebendesEinkommen, sozialhilfeempfaenger);
		BigDecimal verguenstigungProNebenmahlzeit =
			mahlzeitenverguenstigungParams.getVerguenstigungProNebenmahlzeitWithParam(massgebendesEinkommen, sozialhilfeempfaenger);

		BigDecimal verguenstigungProHauptmahlzeitEffektiv = BigDecimal.ZERO;
		BigDecimal verguenstigungProNebenmahlzeitEffektiv = BigDecimal.ZERO;

		// Wenn die Vergünstigung pro Hauptmahlzeit grösser 0 ist
		if (verguenstigungProHauptmahlzeit.compareTo(BigDecimal.ZERO) > 0) {

			// vergünstigung für Hauptmahlzeiten ist gegeben

			// vergünstigung pro hauptmahlzeit berechnen
			verguenstigungProHauptmahlzeitEffektiv = mahlzeitenverguenstigungParams.getVerguenstigungEffektiv(verguenstigungProHauptmahlzeit,
				inputData.getTarifHauptmahlzeit(),
				mahlzeitenverguenstigungParams.getMinimalerElternbeitragHauptmahlzeit());

			// total vergünstigung berechnen
			BigDecimal verguenstigungTotal = inputData.getAnzahlHauptmahlzeiten().multiply(verguenstigungProHauptmahlzeitEffektiv);

			verguenstigungTotal = verguenstigungTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO :
				verguenstigungTotal;

			inputData.getParent().setVerguenstigungHauptmahlzeitenTotalForAsivAndGemeinde(verguenstigungTotal);
		}

		// Wenn die Vergünstigung pro Nebenmahlzeit grösser 0 ist
		if (verguenstigungProNebenmahlzeit.compareTo(BigDecimal.ZERO) > 0) {

			// vergünstigung für Nebenmahlzeiten ist gegeben

			// vergünstigung pro hauptmahlzeit berechnen
			verguenstigungProNebenmahlzeitEffektiv = mahlzeitenverguenstigungParams.getVerguenstigungEffektiv(verguenstigungProNebenmahlzeit,
				inputData.getTarifNebenmahlzeit(),
				mahlzeitenverguenstigungParams.getMinimalerElternbeitragNebenmahlzeit());

			// total vergünstigung berechnen
			BigDecimal verguenstigungTotal = inputData.getAnzahlNebenmahlzeiten().multiply(verguenstigungProNebenmahlzeitEffektiv);
			verguenstigungTotal = verguenstigungTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO :
				verguenstigungTotal;

			inputData.getParent().setVerguenstigungNebenmahlzeitenTotalForAsivAndGemeinde(verguenstigungTotal);
		}

		if (verguenstigungProHauptmahlzeitEffektiv.compareTo(BigDecimal.ZERO) > 0 ||
			verguenstigungProNebenmahlzeitEffektiv.compareTo(BigDecimal.ZERO) > 0) {
			addBemerkung(inputData);
		}
	}

	private void addBemerkung(@Nonnull BGCalculationInput inputData) {
		inputData.addBemerkung(MsgKey.MAHLZEITENVERGUENSTIGUNG_BG, getLocale());
	}

	private boolean validateInput(@Nonnull BGCalculationInput inputData) {
		return (inputData.getAnzahlHauptmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifHauptmahlzeit().compareTo(BigDecimal.ZERO) > 0) ||
			(inputData.getAnzahlNebenmahlzeiten().compareTo(BigDecimal.ZERO) > 0 &&
			inputData.getTarifNebenmahlzeit().compareTo(BigDecimal.ZERO) > 0);
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		return einstellungMap.get(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED).getValueAsBoolean();
	}
}
