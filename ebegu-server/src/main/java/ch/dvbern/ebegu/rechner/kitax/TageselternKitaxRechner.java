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

package ch.dvbern.ebegu.rechner.kitax;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.util.MathUtil;

import static java.math.BigDecimal.ZERO;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot Tageseltern.
 */
public class TageselternKitaxRechner extends AbstractKitaxRechner {

	public TageselternKitaxRechner(KitaxParameterDTO kitaxParameter) {
		super(kitaxParameter);
	}

	@Nonnull
	@Override
	protected Optional<BGCalculationResult> calculateGemeinde(@Nonnull BGCalculationInput input, @Nonnull BGRechnerParameterDTO parameterDTO) {
		// Benoetigte Daten
		LocalDate von = input.getParent().getGueltigkeit().getGueltigAb();
		LocalDate bis = input.getParent().getGueltigkeit().getGueltigBis();
		BigDecimal bgPensum = MathUtil.EXACT.pctToFraction(input.getBgPensumProzent());
		BigDecimal massgebendesEinkommen = input.getMassgebendesEinkommen();

		// Inputdaten validieren
		checkArguments(von, bis, bgPensum, massgebendesEinkommen);

		// Zwischenresultate
		BigDecimal anteilMonat = calculateAnteilMonat(von, bis);
		BigDecimal anzahlTageProMonat = MathUtil.EXACT.divide(kitaxParameter.getMaxTageKita(), ZWOELF);
		BigDecimal betreuungsstundenProMonat = MathUtil.EXACT.multiply(anzahlTageProMonat, kitaxParameter.getMaxStundenProTagKita(), bgPensum);
		BigDecimal betreuungsstundenIntervall = MathUtil.EXACT.multiply(betreuungsstundenProMonat, anteilMonat);

		// Kosten Betreuungsstunde
		BigDecimal kostenProBetreuungsstunde = calculateKostenBetreuungsstunde(kitaxParameter.getKostenProStundeMaximalTageseltern(), massgebendesEinkommen, bgPensum, kitaxParameter);

		// Vollkosten und Elternbeitrag
		BigDecimal vollkosten = MathUtil.EXACT.multiply(kitaxParameter.getKostenProStundeMaximalTageseltern(), betreuungsstundenIntervall);
		BigDecimal elternbeitrag;
		if (input.isBezahltVollkosten()) {
			elternbeitrag = vollkosten;
		} else {
			elternbeitrag = MathUtil.EXACT.multiply(kostenProBetreuungsstunde, betreuungsstundenIntervall);
		}

		BigDecimal verguenstigung = ZERO;
		if (vollkosten != null && elternbeitrag != null) {
			verguenstigung = vollkosten.subtract(elternbeitrag);
		}

		Objects.requireNonNull(elternbeitrag);
		Objects.requireNonNull(vollkosten);

		// Runden und auf Zeitabschnitt zurückschreiben
		// TODO KITAX: Berechnen wir den Elternbeitrag aufgrund der (fixen) Vollkosten, speichern als Kosten aber den eingegebenen Betrag aus Betreuung?
		//		verfuegungZeitabschnitt.setVollkosten(MathUtil.roundToFrankenRappen(vollkosten));
		//		verfuegungZeitabschnitt.setElternbeitrag(MathUtil.roundToFrankenRappen(elternbeitrag));
		//		verfuegungZeitabschnitt.setBetreuungsstunden(MathUtil.EINE_NACHKOMMASTELLE.from(betreuungsstundenIntervall));

		// TODO Resultat erstellen und benoetigte Daten aus Input kopieren
				BGCalculationResult result = new BGCalculationResult();
				VerfuegungZeitabschnitt.initBGCalculationResult(input, result);

		result.setMinimalerElternbeitrag(BigDecimal.ZERO);
		result.setMinimalerElternbeitragGekuerzt(BigDecimal.ZERO);
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigung);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigung);
		result.setVerguenstigung(verguenstigung);
		result.setElternbeitrag(elternbeitrag);
		result.setVollkosten(vollkosten);

		result.setZeiteinheit(PensumUnits.PERCENTAGE);
		//TODO es fehlen die Werte in Zeiteinheiten



		//
		//		result.setZeiteinheitenRoundingStrategy(zeiteinheitenRoundingStrategy());
		//		result.setMinimalerElternbeitrag(minBetrag);
		//		result.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigungVorVollkostenUndMinimalbetrag);
		//		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigungVorMinimalbetrag);
		//		result.setVerguenstigung(verguenstigung);
		//		result.setVollkosten(vollkosten);
		//		result.setElternbeitrag(elternbeitrag);
		//		result.setMinimalerElternbeitragGekuerzt(minimalerElternbeitragGekuerzt);
		//
		//		// Die Stundenwerte (Betreuungsstunden, Anspruchsstunden und BG-Stunden) müssen gerundet werden
		//		result.setBgPensumZeiteinheit(verfuegteZeiteinheiten);
		//		result.setAnspruchspensumZeiteinheit(anspruchsberechtigteZeiteinheiten);
		//		result.setZeiteinheit(getZeiteinheit());
		//		result.setBetreuungspensumZeiteinheit(betreuungspensumZeiteinheit);
//		return verfuegungZeitabschnitt;

		return Optional.of(result);
	}
}
