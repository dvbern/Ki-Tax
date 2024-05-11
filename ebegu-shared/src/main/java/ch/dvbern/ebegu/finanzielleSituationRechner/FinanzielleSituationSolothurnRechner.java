/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.MathUtil;

public class FinanzielleSituationSolothurnRechner extends AbstractFinanzielleSituationRechner {

	@Override
	public void setFinanzielleSituationParameters(
		@Nonnull Gesuch gesuch,
		final FinanzielleSituationResultateDTO finSitResultDTO,
		boolean hasSecondGesuchsteller) {

		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());
		finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS1(calcMassgebendesEinkommenAlleine(finanzielleSituationGS1));

		if(hasSecondGesuchsteller) {
			final FinanzielleSituation finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
			finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS2(calcMassgebendesEinkommenAlleine(finanzielleSituationGS2));
		}

		finSitResultDTO.setMassgebendesEinkVorAbzFamGr(calculateMassgebendesEinkommenZusammen(finSitResultDTO));
	}

	@Override
	public void setEinkommensverschlechterungParameters(
		@Nonnull Gesuch gesuch, int basisJahrPlus,
		final FinanzielleSituationResultateDTO einkVerResultDTO, boolean hasSecondGesuchsteller) {
		Einkommensverschlechterung einkommensverschlechterungGS1Bjp1 =
			getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 1);
		Einkommensverschlechterung einkommensverschlechterungGS1Bjp2 =
			getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 2);

		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		Einkommensverschlechterung einkommensverschlechterungGS2Bjp1 = null;
		Einkommensverschlechterung einkommensverschlechterungGS2Bjp2 = null;
		if (hasSecondGesuchsteller) {
			einkommensverschlechterungGS2Bjp1 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 1);
			einkommensverschlechterungGS2Bjp2 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 2);
		}

		if (basisJahrPlus == 2) {
			calculateZusammen(
				einkVerResultDTO,
				einkommensverschlechterungGS1Bjp2,
				einkommensverschlechterungGS2Bjp2);
		} else {
			calculateZusammen(
				einkVerResultDTO,
				einkommensverschlechterungGS1Bjp1,
				einkommensverschlechterungGS2Bjp1);
		}
	}

	@Override
	public boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		return finanzielleSituation.getSteuerveranlagungErhalten();
	}

	@Override
	public boolean acceptEKV(
		BigDecimal massgebendesEinkommenBasisjahr,
		BigDecimal massgebendesEinkommenJahr,
		BigDecimal minimumProzentFuerEKV) {

		boolean result = massgebendesEinkommenBasisjahr.compareTo(BigDecimal.ZERO) > 0;
		if (result) {
			BigDecimal differenzGerundet = getCalculatedProzentualeDifferenzRounded(massgebendesEinkommenBasisjahr, massgebendesEinkommenJahr);
			// wenn es gibt mehr als minimumEKV in einer positive oder negative Richtung ist der EKV akkzeptiert
			return differenzGerundet.compareTo(minimumProzentFuerEKV.negate()) <= 0 || differenzGerundet.compareTo(
					minimumProzentFuerEKV) >= 0;
		}
		return false;
	}

	private void calculateZusammen(FinanzielleSituationResultateDTO einkVerResultDTO, Einkommensverschlechterung einkommensverschlechterungGS1, Einkommensverschlechterung einkommensverschlechterungGS2) {
		// Jaehrlicher BruttoLohn Berechnen
		var resGS1Exact = calculateJaehrlicherBruttolohn(einkommensverschlechterungGS1);
		einkVerResultDTO.setBruttolohnJahrGS1(MathUtil.GANZZAHL.from(resGS1Exact));
		var resGS2Exact = calculateJaehrlicherBruttolohn(einkommensverschlechterungGS2);
		einkVerResultDTO.setBruttolohnJahrGS2(MathUtil.GANZZAHL.from(resGS2Exact));
		// Massgegebeneseinkommens bevor Einbeziehen Vermoegen
		final BigDecimal abzugNettoLohnGS1 =  MathUtil.GANZZAHL.from(percent(einkVerResultDTO.getBruttolohnJahrGS1(), 25));
		final BigDecimal abzugNettoLohnGS2 =  MathUtil.GANZZAHL.from(percent(einkVerResultDTO.getBruttolohnJahrGS2(), 25));
		final BigDecimal nettoLohnGS1 =  subtract(einkVerResultDTO.getBruttolohnJahrGS1(), abzugNettoLohnGS1);
		final BigDecimal nettoLohnGS2 =  subtract(einkVerResultDTO.getBruttolohnJahrGS2(), abzugNettoLohnGS2);
		// Massgegebeneseinkommens mit Einbeziehen Vermoegen
		final BigDecimal massgebendesEinkVorAbzFamGrGS1 = calculateMassgegebendesEinkVorAbzFamGrEKV(nettoLohnGS1, einkommensverschlechterungGS1);
		final BigDecimal massgebendesEinkVorAbzFamGrGS2 = calculateMassgegebendesEinkVorAbzFamGrEKV(nettoLohnGS2, einkommensverschlechterungGS2);
		einkVerResultDTO.setMassgebendesEinkVorAbzFamGrGS1(massgebendesEinkVorAbzFamGrGS1);
		einkVerResultDTO.setMassgebendesEinkVorAbzFamGrGS2(massgebendesEinkVorAbzFamGrGS2);
		einkVerResultDTO.setMassgebendesEinkVorAbzFamGr(calculateMassgebendesEinkommenZusammen(einkVerResultDTO));
	}

	private BigDecimal calculateJaehrlicherBruttolohn(@Nullable Einkommensverschlechterung einkommensverschlechterung)  {
		if (einkommensverschlechterung == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal total3Monaten = MathUtil.EXACT.addNullSafe(BigDecimal.ZERO, einkommensverschlechterung.getBruttolohnAbrechnung1(), einkommensverschlechterung.getBruttolohnAbrechnung2(), einkommensverschlechterung.getBruttolohnAbrechnung3());
		BigDecimal durchschnitt = MathUtil.EXACT.divideNullSafe(total3Monaten, new BigDecimal(3));
		return MathUtil.EXACT.multiplyNullSafe(durchschnitt, einkommensverschlechterung.getExtraLohn() != null && einkommensverschlechterung.getExtraLohn() ? new BigDecimal(13) : new BigDecimal(12));
	}

	private BigDecimal calculateMassgegebendesEinkVorAbzFamGrEKV(@Nonnull BigDecimal nettoLohn, @Nullable Einkommensverschlechterung einkommensverschlechterung) {
		final BigDecimal nettoVermoegenGS1 = einkommensverschlechterung != null ? MathUtil.GANZZAHL.from(percent(einkommensverschlechterung.getNettoVermoegen(), 5)) : BigDecimal.ZERO;
		return add(nettoLohn, nettoVermoegenGS1);
	}

	private BigDecimal calculateMassgebendesEinkommenZusammen(FinanzielleSituationResultateDTO finSitResultDTO) {
		return MathUtil.EXACT.addNullSafe(finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS1(), finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS2());
	}

	private BigDecimal calcMassgebendesEinkommenAlleine(@Nullable FinanzielleSituation finanzielleSituation) {
		if(finanzielleSituation == null) {
			return BigDecimal.ZERO;
		}

		if(calculateByVeranlagung(finanzielleSituation)) {
			return calcuateMassgebendesEinkommenBasedOnNettoeinkommen(finanzielleSituation);
		}

		return calcuateMassgebendesEinkommenBasedOnBruttoEinkommen(finanzielleSituation);
	}

	private BigDecimal calcuateMassgebendesEinkommenBasedOnBruttoEinkommen(FinanzielleSituation finanzielleSituation) {
		if(isNullOrZero(finanzielleSituation.getBruttoLohn())) {
			return BigDecimal.ZERO;
		}

		var bruttovermoegenMultiplicated =
			MathUtil.EXACT.multiply(finanzielleSituation.getBruttoLohn(), BigDecimal.valueOf(0.75));
		BigDecimal steuerbaresVermoegen5Prozent =
			calcualteStuerbaresVermoegen5Prozent(finanzielleSituation.getSteuerbaresVermoegen());
		return MathUtil.EXACT.addNullSafe(bruttovermoegenMultiplicated, steuerbaresVermoegen5Prozent);
	}

	private BigDecimal calcuateMassgebendesEinkommenBasedOnNettoeinkommen(FinanzielleSituation finanzielleSituation) {
		if(isNullOrZero(finanzielleSituation.getNettolohn())) {
			return BigDecimal.ZERO;
		}

		BigDecimal nettoLohn = finanzielleSituation.getNettolohn();
		BigDecimal abzuegeKinderAusbildung = isNullOrZero(finanzielleSituation.getAbzuegeKinderAusbildung()) ?
			BigDecimal.ZERO :
			finanzielleSituation.getAbzuegeKinderAusbildung();
		BigDecimal abzuegeUnterhaltsBeitraege = isNullOrZero(finanzielleSituation.getUnterhaltsBeitraege()) ?
			BigDecimal.ZERO :
			finanzielleSituation.getUnterhaltsBeitraege();
		BigDecimal steuerbaresVermoegen5Prozent = calcualteStuerbaresVermoegen5Prozent(finanzielleSituation.getSteuerbaresVermoegen());

		return MathUtil.EXACT.subtractNullSafe(nettoLohn, abzuegeKinderAusbildung)
			.subtract(abzuegeUnterhaltsBeitraege)
			.add(steuerbaresVermoegen5Prozent);
	}

	private BigDecimal calcualteStuerbaresVermoegen5Prozent(BigDecimal steuerbaresVermoegen) {
		if (isNullOrZero(steuerbaresVermoegen)) {
			return BigDecimal.ZERO;
		}

		return MathUtil.EXACT.multiply(steuerbaresVermoegen, BigDecimal.valueOf(0.05));
	}

	private boolean isNullOrZero(BigDecimal number) {
		return number == null || number.compareTo(BigDecimal.ZERO) == 0;
	}
}
