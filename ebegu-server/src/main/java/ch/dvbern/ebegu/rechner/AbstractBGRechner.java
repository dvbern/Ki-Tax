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
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Superklasse für BG-Rechner
 */
public abstract class AbstractBGRechner {

	protected static final MathUtil MATH = MathUtil.EXACT;

	/**
	 * Diese Methode fuehrt die Berechnung fuer  die uebergebenen Verfuegungsabschnitte durch.
	 */
	@Nonnull
	public VerfuegungZeitabschnitt calculate(
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
		BigDecimal betreuungspensum = verfuegungZeitabschnitt.getBetreuungspensum();

		// Inputdaten validieren
		checkArguments(von, bis, verfuegungZeitabschnitt.getBgPensum(), massgebendesEinkommen);

		// Zwischenresultate
		BigDecimal verguenstigungProTag = getVerguenstigungProZeiteinheit(
			parameterDTO,
			unter12Monate,
			eingeschult,
			besonderebeduerfnisse,
			massgebendesEinkommen,
			verfuegungZeitabschnitt.isBezahltVollkosten());

		BigDecimal anteilMonat = getAnteilMonat(parameterDTO, von, bis);

		BigDecimal stundenGemaessPensumUndAnteilMonat =
			getAnzahlZeiteinheitenGemaessPensumUndAnteilMonat(
				parameterDTO,
				von,
				bis,
				verfuegungZeitabschnitt.getBgPensum());

		BigDecimal minBetrag =
			MATH.multiply(stundenGemaessPensumUndAnteilMonat, getMinimalBeitragProZeiteinheit(parameterDTO));
		BigDecimal verguenstigungVorVollkostenUndMinimalbetrag =
			MATH.multiplyNullSafe(stundenGemaessPensumUndAnteilMonat, verguenstigungProTag);

		BigDecimal anteilVerguenstigesPensumAmBetreuungspensum = BigDecimal.ZERO;
		if (betreuungspensum.compareTo(BigDecimal.ZERO) > 0) {
			anteilVerguenstigesPensumAmBetreuungspensum =
				MATH.divide(verfuegungZeitabschnitt.getBgPensum(), betreuungspensum);
		}
		BigDecimal vollkostenFuerVerguenstigtesPensum =
			MathUtil.DEFAULT.multiply(vollkostenProMonat, anteilVerguenstigesPensumAmBetreuungspensum);
		BigDecimal vollkosten = MATH.multiply(anteilMonat, vollkostenFuerVerguenstigtesPensum);
		BigDecimal vollkostenMinusMinimaltarif = MATH.subtract(vollkosten, minBetrag);
		BigDecimal verguenstigungVorMinimalbetrag = vollkosten.min(verguenstigungVorVollkostenUndMinimalbetrag);

		// Resultat
		BigDecimal verguenstigung = verguenstigungVorVollkostenUndMinimalbetrag.min(vollkostenMinusMinimaltarif);
		verguenstigung = MathUtil.roundToFrankenRappen(verguenstigung);
		BigDecimal elternbeitrag = MATH.subtract(vollkosten, verguenstigung);
		// Runden und auf Zeitabschnitt zurückschreiben
		verfuegungZeitabschnitt.setMinimalerElternbeitrag(MathUtil.roundToFrankenRappen(minBetrag));
		verfuegungZeitabschnitt.setVerguenstigungOhneBeruecksichtigungVollkosten(
			verguenstigungVorVollkostenUndMinimalbetrag);
		verfuegungZeitabschnitt.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(MathUtil.roundToFrankenRappen(verguenstigungVorMinimalbetrag));
		verfuegungZeitabschnitt.setVerguenstigung(verguenstigung);
		verfuegungZeitabschnitt.setVollkosten(MathUtil.roundToFrankenRappen(vollkosten));
		verfuegungZeitabschnitt.setElternbeitrag(MathUtil.roundToFrankenRappen(elternbeitrag));
		return verfuegungZeitabschnitt;
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

	/**
	 * Berechnet die Anzahl Tage zwischen zwei Daten
	 */
	protected long daysBetween(@Nonnull LocalDate start, @Nonnull LocalDate end) {
		return Stream.iterate(start, d -> d.plusDays(1))
			.limit(start.until(end.plusDays(1), ChronoUnit.DAYS))
			.count();
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

}
