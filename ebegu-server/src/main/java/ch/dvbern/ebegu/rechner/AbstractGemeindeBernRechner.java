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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Superklasse für BG-Rechner der Gemeinde: Berechnet sowohl nach ASIV wie auch nach Gemeinde-spezifischen Regeln
 */
public abstract class AbstractGemeindeBernRechner extends AbstractAsivBernRechner {

	private final List<RechnerRule> rechnerRulesForGemeinde;
	private final RechnerRuleParameterDTO rechnerParameter = new RechnerRuleParameterDTO();

	protected AbstractGemeindeBernRechner(List<RechnerRule> rechnerRulesForGemeinde) {
		this.rechnerRulesForGemeinde = rechnerRulesForGemeinde;
	}

	@Nonnull
	@Override
	public BGCalculationResult calculateAsiv(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		// Fuer ASIV alles zuruecksetzen
		prepareRechnerParameterForAsiv();

		return super.calculateAsiv(input, parameterDTO);
	}

	@Nonnull
	@Override
	protected Optional<BGCalculationResult> calculateGemeinde(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		// Fuer Gemeinde die richtigen Werte setzen
		prepareRechnerParameterForGemeinde(input, parameterDTO);

		// Jetzt die Berechnung mit den Input-Werten der Gemeinde durchfuehren
		// Es muss die Methode der superklasse ausgefuehrt werden, sonst werden die Parameter wieder ueberschrieben
		return Optional.of(super.calculateAsiv(input, parameterDTO));
	}

	/**
	 * Dieser Mehtode darf nicht bei der calculateGemeinde verwendet werden
	 */
	private void prepareRechnerParameterForAsiv() {
		rechnerParameter.reset();
	}

	private void prepareRechnerParameterForGemeinde(
		@Nonnull BGCalculationInput inputGemeinde,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		for (RechnerRule rechnerRule : rechnerRulesForGemeinde) {
			// Diese Pruefung erfolgt eigentlich schon aussen... die Rules die reinkommen sind schon konfiguriert fuer
			// Gemeinde
			if (rechnerRule.isConfigueredForGemeinde(parameterDTO)) {
				if (rechnerRule.isRelevantForVerfuegung(inputGemeinde, parameterDTO)) {
					rechnerRule.prepareParameter(inputGemeinde, parameterDTO, rechnerParameter);
				} else {
					//Hier muss man nur der Parameter die nicht relevant ist zuruecksetzen nicht alle parametern
					//sonst man verliert die andere Gemeinde Relevanten Rules
					rechnerRule.resetParameter(rechnerParameter);
				}
			}
		}
	}

	@Nonnull
	@Override
	BigDecimal getVerguenstigungProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		boolean unter12Monate,
	 	boolean besonderebeduerfnisse,
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean bezahltVollkosten,
		@Nullable EinschulungTyp einschulungTyp) {
		// "Normale" Verguentigung pro Zeiteinheit
		BigDecimal verguenstigungProZeiteinheit =
			super.getVerguenstigungProZeiteinheit(parameterDTO, unter12Monate,
				besonderebeduerfnisse,
				massgebendesEinkommen,
				bezahltVollkosten, einschulungTyp);
		// Zusaetzlicher Gutschein Gemeinde
		verguenstigungProZeiteinheit =
			EXACT.addNullSafe(
				verguenstigungProZeiteinheit,
				rechnerParameter.getZusaetzlicherGutscheinGemeindeBetrag());
		// Zusaetzlicher Baby-Gutschein
		verguenstigungProZeiteinheit =
			EXACT.addNullSafe(verguenstigungProZeiteinheit, rechnerParameter.getZusaetzlicherBabyGutscheinBetrag());
		// Minimaler Gutschein der Gemeinde
		verguenstigungProZeiteinheit = getMinimaleVerguenstigungProZeiteinheit(verguenstigungProZeiteinheit);

		return verguenstigungProZeiteinheit;
	}

	protected BigDecimal getMinimaleVerguenstigungProZeiteinheit(BigDecimal verguenstigung) {
		return MathUtil.minimum(verguenstigung, rechnerParameter.getMinimalPauschalBetrag());
	}

	/**
	 * Die Mahlzeitenverguenstigungen mit dem Anteil Monat verrechnen. Die Verguenstigung wurde aufgrund der
	 * *monatlichen*
	 * Mahlzeiten berechnet und ist darum bei untermonatlichen Pensen zu hoch.
	 * Beispiel: Betreuung ueber einen halben Monat:
	 * berechneteVerguenstigung = eingegebeneVerguenstigung * 0.5
	 */
	@Override
	protected void handleAnteileMahlzeitenverguenstigung(
		@Nonnull BGCalculationResult result,
		@Nonnull BigDecimal anteilMonat,
		@Nonnull BigDecimal anteilMonatEffektivAusbezahlt
	) {
		// Falls der Zeitabschnitt untermonatlich ist, muessen sowohl die Anzahl Mahlzeiten wie auch die Kosten
		// derselben mit dem Anteil des Monats korrigiert werden
		final BigDecimal mahlzeitenTotal = rechnerParameter.getVerguenstigungMahlzeitenTotal();
		result.setVerguenstigungMahlzeitenTotal(MathUtil.DEFAULT.multiply(mahlzeitenTotal, anteilMonat,
			anteilMonatEffektivAusbezahlt));
	}
}
