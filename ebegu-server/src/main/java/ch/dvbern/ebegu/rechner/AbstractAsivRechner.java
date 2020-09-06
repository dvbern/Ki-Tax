/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Superklasse für BG-Rechner
 */
public abstract class AbstractAsivRechner extends AbstractRechner {

	protected static final MathUtil EXACT = MathUtil.EXACT;


	/**
	 * Diese Methode fuehrt die Berechnung fuer die uebergebenen Verfuegungsabschnitte durch.
	 */
	@Override
	@Nonnull
	public BGCalculationResult calculateAsiv(
		@Nonnull BGCalculationInput input,
		@Nonnull BGRechnerParameterDTO parameterDTO
	) {
		// Benoetigte Daten
		boolean unter12Monate = input.isBabyTarif();
		boolean eingeschult = input.getEinschulungTyp() != null && input.getEinschulungTyp().isEingeschult();
		// Die Institution muss die besonderen Bedürfnisse bestätigt haben
		boolean besonderebeduerfnisse = input.isBesondereBeduerfnisseBestaetigt();
		LocalDate von = input.getParent().getGueltigkeit().getGueltigAb();
		LocalDate bis = input.getParent().getGueltigkeit().getGueltigBis();
		BigDecimal massgebendesEinkommen = input.getMassgebendesEinkommen();
		BigDecimal vollkostenProMonat = input.getMonatlicheBetreuungskosten();
		BigDecimal betreuungspensum = input.getBetreuungspensumProzent();

		// Inputdaten validieren
		BigDecimal bgPensum = input.getBgPensumProzent();
		checkArguments(von, bis, bgPensum, massgebendesEinkommen);

		// Zwischenresultate
		BigDecimal verguenstigungProZeiteinheit = getVerguenstigungProZeiteinheit(
			parameterDTO,
			unter12Monate,
			eingeschult,
			besonderebeduerfnisse,
			massgebendesEinkommen,
			input.isBezahltVollkosten());

		BigDecimal anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(von, bis);

		BigDecimal verfuegteZeiteinheiten =
			getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(parameterDTO, anteilMonat, bgPensum);

		BigDecimal anspruchPensum = EXACT.from(input.getAnspruchspensumProzent());
		BigDecimal anspruchsberechtigteZeiteinheiten =
			getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(parameterDTO, anteilMonat, anspruchPensum);

		BigDecimal betreuungspensumZeiteinheit =
			getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(parameterDTO, anteilMonat, betreuungspensum);

		BigDecimal minBetrag = EXACT.multiply(verfuegteZeiteinheiten, getMinimalBeitragProZeiteinheit(parameterDTO));
		BigDecimal verguenstigungVorVollkostenUndMinimalbetrag =
			EXACT.multiplyNullSafe(verfuegteZeiteinheiten, verguenstigungProZeiteinheit);

		BigDecimal anteilVerguenstigesPensumAmBetreuungspensum = BigDecimal.ZERO;
		if (betreuungspensum.compareTo(BigDecimal.ZERO) > 0) {
			anteilVerguenstigesPensumAmBetreuungspensum =
				EXACT.divide(bgPensum, betreuungspensum);
		}
		BigDecimal vollkostenFuerVerguenstigtesPensum =
			EXACT.multiply(vollkostenProMonat, anteilVerguenstigesPensumAmBetreuungspensum);
		BigDecimal vollkosten = EXACT.multiply(anteilMonat, vollkostenFuerVerguenstigtesPensum);
		BigDecimal vollkostenMinusMinimaltarif = EXACT.subtract(vollkosten, minBetrag);
		BigDecimal verguenstigungVorMinimalbetrag = vollkosten.min(verguenstigungVorVollkostenUndMinimalbetrag);

		BigDecimal verguenstigung = verguenstigungVorVollkostenUndMinimalbetrag.min(vollkostenMinusMinimaltarif);
		BigDecimal elternbeitrag = EXACT.subtract(vollkosten, verguenstigung);

		BigDecimal minimalerElternbeitragGekuerzt = MathUtil.DEFAULT.from(0);
		BigDecimal vollkostenMinusVerguenstigung = MathUtil.DEFAULT.subtract(vollkosten, verguenstigungVorMinimalbetrag);
		if (vollkostenMinusVerguenstigung.compareTo(minBetrag) <= 0) {
			minimalerElternbeitragGekuerzt = MathUtil.DEFAULT.subtract(minBetrag, vollkostenMinusVerguenstigung);
		}

		// Resultat erstellen und benoetigte Daten aus Input kopieren
		BGCalculationResult result = new BGCalculationResult();
		VerfuegungZeitabschnitt.initBGCalculationResult(input, result);

		result.setZeiteinheitenRoundingStrategy(zeiteinheitenRoundingStrategy());
		result.setMinimalerElternbeitrag(minBetrag);
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigungVorVollkostenUndMinimalbetrag);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigungVorMinimalbetrag);
		result.setVerguenstigung(verguenstigung);
		result.setVollkosten(vollkosten);
		result.setElternbeitrag(elternbeitrag);
		result.setMinimalerElternbeitragGekuerzt(minimalerElternbeitragGekuerzt);

		// Die Stundenwerte (Betreuungsstunden, Anspruchsstunden und BG-Stunden) müssen gerundet werden
		result.setBgPensumZeiteinheit(verfuegteZeiteinheiten);
		result.setAnspruchspensumZeiteinheit(anspruchsberechtigteZeiteinheiten);
		result.setZeiteinheit(getZeiteinheit());
		result.setBetreuungspensumZeiteinheit(betreuungspensumZeiteinheit);

		handleAnteileMahlzeitenverguenstigung(result, anteilMonat, anteilVerguenstigesPensumAmBetreuungspensum);

		return result;
	}

	/**
	 * Depending on the type of Zeiteinheit, we may want to apply another rounding strategy
	 */
	@Nonnull
	protected Function<BigDecimal, BigDecimal> zeiteinheitenRoundingStrategy() {
		return MathUtil::toTwoKommastelle;
	}

	/**
	 * Checkt die für alle Angebote benoetigten Argumente auf Null.
	 * Stellt sicher, dass der Zeitraum innerhalb eines Monates liegt
	 * Wenn nicht wird eine Exception geworfen
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	protected void checkArguments(
		@Nullable LocalDate von, @Nullable LocalDate bis,
		@Nullable BigDecimal anspruch, @Nullable BigDecimal massgebendesEinkommen) {
		// Inputdaten validieren
		if (von == null || bis == null || anspruch == null || massgebendesEinkommen == null) {
			throw new IllegalArgumentException(
				"BG Rechner kann nicht verwendet werden, da Inputdaten fehlen: von/bis, Anpsruch, massgebendes "
					+ "Einkommen");
		}
		// Max. 1 Monat
		if (von.getMonth() != bis.getMonth()) {
			throw new IllegalArgumentException(
				"BG Rechner duerfen nicht für monatsuebergreifende Zeitabschnitte verwendet werden!");
		}
	}

	@Nonnull
	BigDecimal getVerguenstigungProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nonnull Boolean eingeschult,
		@Nonnull Boolean besonderebeduerfnisse,
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean bezahltVollkosten) {

		// BezahltVollkosten ist/darf nur TRUE sein, wenn keine erweiterteBetreuung besteht!
		if (bezahltVollkosten) {
			return BigDecimal.ZERO;
		}

		BigDecimal maximaleVerguenstigungProTag =
			getMaximaleVerguenstigungProZeiteinheit(parameterDTO, unter12Monate, eingeschult);
		BigDecimal minEinkommen = parameterDTO.getMinMassgebendesEinkommen();
		BigDecimal maxEinkommen = parameterDTO.getMaxMassgebendesEinkommen();

		BigDecimal beruecksichtigtesEinkommen = EXACT.subtract(massgebendesEinkommen, minEinkommen);
		BigDecimal product = EXACT.multiplyNullSafe(maximaleVerguenstigungProTag, beruecksichtigtesEinkommen);
		BigDecimal augment = EXACT.divide(product, EXACT.subtract(minEinkommen, maxEinkommen));
		BigDecimal verguenstigungProTag = EXACT.add(augment, maximaleVerguenstigungProTag);
		// Max und Min beachten
		verguenstigungProTag = verguenstigungProTag.min(maximaleVerguenstigungProTag);
		verguenstigungProTag = verguenstigungProTag.max(BigDecimal.ZERO);
		// (Fixen) Zuschlag fuer Besondere Beduerfnisse
		BigDecimal zuschlagFuerBesondereBeduerfnisse =
			getZuschlagFuerBesondereBeduerfnisse(parameterDTO, besonderebeduerfnisse);
		return EXACT.add(verguenstigungProTag, zuschlagFuerBesondereBeduerfnisse);
	}

	@Nonnull
	protected abstract BigDecimal getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull BigDecimal anteilMonat,
		@Nonnull BigDecimal bgPensum);

	@Nonnull
	protected abstract BigDecimal getMinimalBeitragProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO);

	@Nonnull
	protected abstract BigDecimal getMaximaleVerguenstigungProZeiteinheit(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nonnull Boolean eingeschult);

	@Nonnull
	protected abstract BigDecimal getZuschlagFuerBesondereBeduerfnisse(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean besonderebeduerfnisse);

	@Nonnull
	protected abstract PensumUnits getZeiteinheit();
}
