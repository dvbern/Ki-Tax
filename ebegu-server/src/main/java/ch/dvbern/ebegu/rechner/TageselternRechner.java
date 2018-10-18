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
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Berechnet die Vollkosten, den Elternbeitrag und die Vergünstigung für einen Zeitabschnitt (innerhalb eines Monats)
 * einer Betreuung für das Angebot Tageseltern.
 */
public class TageselternRechner extends AbstractBGRechner {

	private static final MathUtil MATH = MathUtil.EXACT;

	@Override
	public VerfuegungZeitabschnitt calculate(
		VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		Verfuegung verfuegung,
		BGRechnerParameterDTO parameterDTO
	) {
		Objects.requireNonNull(verfuegung.getBetreuung().getKind().getKindJA().getEinschulungTyp());

		// Benoetigte Daten
		LocalDate geburtsdatum = verfuegung.getBetreuung().getKind().getKindJA().getGeburtsdatum();
		boolean eingeschult = verfuegung.getBetreuung().getKind().getKindJA().getEinschulungTyp().isEingeschult();
		boolean besonderebeduerfnisse = verfuegung.getBetreuung().getErweiterteBeduerfnisse();
		LocalDate von = verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb();
		LocalDate bis = verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis();
		BigDecimal bgPensum = MathUtil.EXACT.pctToFraction(verfuegungZeitabschnitt.getBgPensum());
		BigDecimal massgebendesEinkommen = verfuegungZeitabschnitt.getMassgebendesEinkommen();
		BigDecimal vollkostenProMonat = verfuegungZeitabschnitt.getMonatlicheBetreuungskosten();
		BigDecimal oeffnungstage = parameterDTO.getOeffnungstageTFO();
		BigDecimal oeffnungsstunden = parameterDTO.getOeffnungsstundenTFO();

		// Inputdaten validieren
		checkArguments(von, bis, bgPensum, massgebendesEinkommen);
		Objects.requireNonNull(geburtsdatum, "geburtsdatum darf nicht null sein");

		// Zwischenresultate
		boolean unter12Monate = !von.isAfter(geburtsdatum.plusMonths(12).with(TemporalAdjusters.lastDayOfMonth()));
		BigDecimal verguenstigungProTag = getVerguenstigungProStd(parameterDTO,
			unter12Monate,
			eingeschult,
			besonderebeduerfnisse,
			massgebendesEinkommen);

		LocalDate monatsanfang = von.with(TemporalAdjusters.firstDayOfMonth());
		LocalDate monatsende = bis.with(TemporalAdjusters.lastDayOfMonth());
		long nettoTageMonat = daysBetween(monatsanfang, monatsende);
		long nettoTageIntervall = daysBetween(von, bis);

		long stundenMonat =  nettoTageMonat * oeffnungsstunden.longValue();
		long stundenIntervall = nettoTageIntervall * oeffnungsstunden.longValue();
		BigDecimal anteilMonat = MathUtil.EXACT.divide(MathUtil.EXACT.from(stundenIntervall), MathUtil.EXACT.from(stundenMonat));

		BigDecimal stundenGemaessPensumUndAnteilMonat =
			MATH.multiplyNullSafe(MATH.divide(oeffnungstage, MATH.from(12)), anteilMonat, bgPensum, oeffnungsstunden);


		BigDecimal minBetrag = MATH.multiply(stundenGemaessPensumUndAnteilMonat, parameterDTO.getMinVerguenstigungProStd
			());
		BigDecimal verguenstigungVorVollkostenUndMinimalbetrag =
			MATH.multiplyNullSafe(stundenGemaessPensumUndAnteilMonat, verguenstigungProTag);
		BigDecimal vollkosten = MATH.multiply(anteilMonat, vollkostenProMonat);
		BigDecimal vollkostenMinusMinimaltarif = MATH.subtract(vollkosten, minBetrag);

		// Resultat
		BigDecimal verguenstigung = verguenstigungVorVollkostenUndMinimalbetrag.min(vollkostenMinusMinimaltarif);
		BigDecimal elternbeitrag = MATH.subtract(vollkosten, verguenstigung);
		// Runden und auf Zeitabschnitt zurückschreiben
		if (verfuegungZeitabschnitt.isBezahltVollkosten()) {
			elternbeitrag = vollkosten;
		}
		verfuegungZeitabschnitt.setVollkosten(MathUtil.roundToFrankenRappen(vollkosten));
		verfuegungZeitabschnitt.setElternbeitrag(MathUtil.roundToFrankenRappen(elternbeitrag));
		return verfuegungZeitabschnitt;
	}

	private BigDecimal getMaximaleVerguenstigungProStd(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nonnull Boolean eingeschult) {
		if (unter12Monate) {
			return parameterDTO.getMaxVerguenstigungVorschuleBabyProStd();
		}
		if (eingeschult) {
			return parameterDTO.getMaxVerguenstigungSchuleKindProStd();
		}
		return parameterDTO.getMaxVerguenstigungVorschuleKindProStd();
	}

	@Nonnull
	private BigDecimal getVerguenstigungProStd(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean unter12Monate,
		@Nonnull Boolean eingeschult,
		@Nonnull Boolean besonderebeduerfnisse,
		@Nonnull BigDecimal massgebendesEinkommen) {

		BigDecimal maximaleVerguenstigungProTag =
			getMaximaleVerguenstigungProStd(parameterDTO, unter12Monate, eingeschult);
		BigDecimal minEinkommen = parameterDTO.getMinMassgebendesEinkommen();
		BigDecimal maxEinkommen = parameterDTO.getMaxMassgebendesEinkommen();

		BigDecimal op1 = MATH.divide(
			maximaleVerguenstigungProTag,
			MATH.subtract(minEinkommen, maxEinkommen));
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
	private BigDecimal getZuschlagFuerBesondereBeduerfnisse(
		@Nonnull BGRechnerParameterDTO parameterDTO,
		@Nonnull Boolean besonderebeduerfnisse) {

		return besonderebeduerfnisse ? parameterDTO.getZuschlagBehinderungProStd() : BigDecimal.ZERO;
	}
}
