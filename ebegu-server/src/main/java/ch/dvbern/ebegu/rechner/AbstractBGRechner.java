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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Superklasse für BG-Rechner
 */
public abstract class AbstractBGRechner {

	protected static final MathUtil MATH = MathUtil.EXACT;

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

		BigDecimal anteilMonat = getAnteilMonat(parameterDTO, von, bis);

		BigDecimal verfuegteZeiteinheiten =
			getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(parameterDTO, von, bis, bgPensum);

		BigDecimal anspruchPensum = MATH.from(verfuegungZeitabschnitt.getAnspruchberechtigtesPensum());
		BigDecimal anspruchsberechtigteZeiteinheiten =
			getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(parameterDTO, von, bis, anspruchPensum);

		BigDecimal betreuungspensumZeiteinheit = getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(parameterDTO, von, bis, betreuungspensum);

		BigDecimal minBetrag = MATH.multiply(verfuegteZeiteinheiten, getMinimalBeitragProZeiteinheit(parameterDTO));
		BigDecimal verguenstigungVorVollkostenUndMinimalbetrag =
			MATH.multiplyNullSafe(verfuegteZeiteinheiten, verguenstigungProZeiteinheit);

		BigDecimal anteilVerguenstigesPensumAmBetreuungspensum = BigDecimal.ZERO;
		if (betreuungspensum.compareTo(BigDecimal.ZERO) > 0) {
			anteilVerguenstigesPensumAmBetreuungspensum =
				MATH.divide(bgPensum, betreuungspensum);
		}
		BigDecimal vollkostenFuerVerguenstigtesPensum =
			MATH.multiply(vollkostenProMonat, anteilVerguenstigesPensumAmBetreuungspensum);
		BigDecimal vollkosten = MATH.multiply(anteilMonat, vollkostenFuerVerguenstigtesPensum);
		BigDecimal vollkostenMinusMinimaltarif = MATH.subtract(vollkosten, minBetrag);
		BigDecimal verguenstigungVorMinimalbetrag = vollkosten.min(verguenstigungVorVollkostenUndMinimalbetrag);

		BigDecimal verguenstigung = verguenstigungVorVollkostenUndMinimalbetrag.min(vollkostenMinusMinimaltarif);
		verguenstigung = MathUtil.roundToFrankenRappen(verguenstigung);
		BigDecimal elternbeitrag = MATH.subtract(vollkosten, verguenstigung);

		// Resultat
		BGCalculationResult result = new BGCalculationResult();
		result.setMinimalerElternbeitrag(MathUtil.roundToFrankenRappen(minBetrag));
		result.setVerguenstigungOhneBeruecksichtigungVollkosten(
			MathUtil.DEFAULT.from(verguenstigungVorVollkostenUndMinimalbetrag));
		result.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(MathUtil.roundToFrankenRappen(
			verguenstigungVorMinimalbetrag));
		result.setVerguenstigung(MathUtil.DEFAULT.from(verguenstigung));
		result.setVollkosten(MathUtil.roundToFrankenRappen(vollkosten));
		result.setElternbeitrag(MathUtil.roundToFrankenRappen(elternbeitrag));

		result.setVerfuegteAnzahlZeiteinheiten(MathUtil.DEFAULT.from(verfuegteZeiteinheiten));
		result.setAnspruchsberechtigteAnzahlZeiteinheiten(MathUtil.DEFAULT.from(anspruchsberechtigteZeiteinheiten));
		result.setZeiteinheit(getZeiteinheit());
		result.setBetreuungspensumZeiteinheit(betreuungspensumZeiteinheit);

		return result;
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
	protected BigDecimal getVerguenstigungProZeiteinheit(
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

		BigDecimal op1 = MATH.divide(maximaleVerguenstigungProTag, MATH.subtract(minEinkommen, maxEinkommen));
		BigDecimal op2 = MATH.subtract(massgebendesEinkommen, minEinkommen);
		BigDecimal augment = MATH.multiplyNullSafe(op1, op2);
		BigDecimal verguenstigungProTag = MATH.add(augment, maximaleVerguenstigungProTag);
		// Max und Min beachten
		verguenstigungProTag = verguenstigungProTag.min(maximaleVerguenstigungProTag);
		verguenstigungProTag = verguenstigungProTag.max(BigDecimal.ZERO);
		// (Fixen) Zuschlag fuer Besondere Beduerfnisse
		BigDecimal zuschlagFuerBesondereBeduerfnisse =
			getZuschlagFuerBesondereBeduerfnisse(parameterDTO, besonderebeduerfnisse);
		return MATH.add(verguenstigungProTag, zuschlagFuerBesondereBeduerfnisse);
	}

	@Nonnull
	protected abstract BigDecimal getAnteilMonat(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis);

	@Nonnull
	protected abstract BigDecimal getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis,
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
