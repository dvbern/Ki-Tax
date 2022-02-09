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

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.MathUtil;

public class FinanzielleSituationSolothurnRechner extends AbstractFinanzielleSituationRechner {

	@Override
	public void calculateFinanzDaten(
		@Nonnull Gesuch gesuch,
		BigDecimal minimumEKV) {
		FinanzDatenDTO finanzDatenDTOAlleine = new FinanzDatenDTO();
		FinanzDatenDTO finanzDatenDTOZuZweit = new FinanzDatenDTO();

		// Finanzielle Situation berechnen
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOAlleine =
			calculateResultateFinanzielleSituation(gesuch, false);
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOZuZweit =
			calculateResultateFinanzielleSituation(gesuch, true);

		finanzDatenDTOAlleine.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr());
		finanzDatenDTOZuZweit.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr());

		// TODO Einkommensverschlechterung spaeter berücksichtigen hier

		gesuch.setFinanzDatenDTO_alleine(finanzDatenDTOAlleine);
		gesuch.setFinanzDatenDTO_zuZweit(finanzDatenDTOZuZweit);
	}

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

	private BigDecimal calculateMassgebendesEinkommenZusammen(FinanzielleSituationResultateDTO finSitResultDTO) {
		return MathUtil.EXACT.addNullSafe(finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS1(), finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS2());
	}

	private BigDecimal calcMassgebendesEinkommenAlleine(@Nullable FinanzielleSituation finanzielleSituation) {
		if(finanzielleSituation == null) {
			return BigDecimal.ZERO;
		}

		if(finanzielleSituation.getSteuerveranlagungErhalten()) {
			return calcuateMassgebendesEinkommenBasedOnNettoeinkommen(finanzielleSituation);
		}

		return calcuateMassgebendesEinkommenBasedOnBruttoEinkommen(finanzielleSituation);
	}

	private BigDecimal calcuateMassgebendesEinkommenBasedOnBruttoEinkommen(FinanzielleSituation finanzielleSituation) {
		if(isNullOrZero(finanzielleSituation.getBruttoLohn())) {
			return BigDecimal.ZERO;
		}

		return MathUtil.EXACT.multiply(finanzielleSituation.getBruttoLohn(), BigDecimal.valueOf(0.75));
	}

	private BigDecimal calcuateMassgebendesEinkommenBasedOnNettoeinkommen(FinanzielleSituation finanzielleSituation) {
		if(isNullOrZero(finanzielleSituation.getNettolohn())) {
			return BigDecimal.ZERO;
		}

		BigDecimal nettoLohn = finanzielleSituation.getNettolohn();
		BigDecimal abzuegeKinderAusbildung = isNullOrZero(finanzielleSituation.getAbzuegeKinderAusbildung()) ?
			BigDecimal.ZERO :
			finanzielleSituation.getAbzuegeKinderAusbildung();
		BigDecimal unterhaltsBeitraege = isNullOrZero(finanzielleSituation.getUnterhaltsBeitraege()) ?
			BigDecimal.ZERO :
			finanzielleSituation.getUnterhaltsBeitraege();
		BigDecimal steuerbaresVermoegen5Prozent = calcualteStuerbaresVermoegen5Prozent(finanzielleSituation.getSteuerbaresVermoegen());

		return MathUtil.EXACT.subtractNullSafe(nettoLohn, abzuegeKinderAusbildung)
			.subtract(unterhaltsBeitraege)
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
