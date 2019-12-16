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

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.util.MathUtil.roundToFrankenRappen;

/**
 * Superklasse für BG-Rechner
 */
public abstract class AbstractBGRechner {

	protected static final MathUtil EXACT = MathUtil.EXACT;

	/**
	 * Diese Methode fuehrt die Berechnung fuer die uebergebenen Verfuegungsabschnitte durch.
	 */
	@Nonnull
	public BGCalculationResult calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		// Benoetigte Daten
		boolean unter12Monate = verfuegungZeitabschnitt.isBabyTarif();
		boolean eingeschult = verfuegungZeitabschnitt.isEingeschult();
		// Die Institution muss die besonderen Bedürfnisse bestätigt haben
		boolean besonderebeduerfnisse = verfuegungZeitabschnitt.isBesondereBeduerfnisseBestaetigt();
		LocalDate von = verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb();
		LocalDate bis = verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis();
		BigDecimal massgebendesEinkommen = verfuegungZeitabschnitt.getMassgebendesEinkommen();
		BigDecimal vollkostenProMonat = verfuegungZeitabschnitt.getMonatlicheBetreuungskosten();
		BigDecimal betreuungspensum = verfuegungZeitabschnitt.getBetreuungspensumProzent();

		// Inputdaten validieren
		BigDecimal bgPensum = verfuegungZeitabschnitt.getBgPensum();
		checkArguments(von, bis, bgPensum, massgebendesEinkommen);

		// Zwischenresultate
		BigDecimal verguenstigungProZeiteinheit = getVerguenstigungProZeiteinheit(
			parameterDTO,
			unter12Monate,
			eingeschult,
			besonderebeduerfnisse,
			massgebendesEinkommen,
			verfuegungZeitabschnitt.isBezahltVollkosten());

		BigDecimal anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(von, bis);

		BigDecimal verfuegteZeiteinheiten =
			getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(parameterDTO, anteilMonat, bgPensum);

		BigDecimal anspruchPensum = EXACT.from(verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
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

		// Resultat
		BGCalculationResult result = new BGCalculationResult();
		result.setZeiteinheitenRoundingStrategy(zeiteinheitenRoundingStrategy());
		result.setMinimalerElternbeitrag(minBetrag);
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(verguenstigungVorVollkostenUndMinimalbetrag);
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(verguenstigungVorMinimalbetrag);
		result.setVerguenstigung(verguenstigung);
		result.setVollkosten(vollkosten);
		result.setElternbeitrag(elternbeitrag);

		// Die Stundenwerte (Betreuungsstunden, Anspruchsstunden und BG-Stunden) müssen gerundet werden
		result.setVerfuegteAnzahlZeiteinheiten(verfuegteZeiteinheiten);
		result.setAnspruchsberechtigteAnzahlZeiteinheiten(anspruchsberechtigteZeiteinheiten);
		result.setZeiteinheit(getZeiteinheit());
		result.setBetreuungspensumZeiteinheit(betreuungspensumZeiteinheit);

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
