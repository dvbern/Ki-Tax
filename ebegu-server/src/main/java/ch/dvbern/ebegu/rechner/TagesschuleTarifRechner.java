/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.MathUtil;

import static java.util.Objects.requireNonNull;

public class TagesschuleTarifRechner {

	private final BigDecimal MATA_MIT_PEDAGOGISCHE_BETREUUNG;
	private final BigDecimal MATA_OHNE_PEDAGOGISCHE_BETREUUNG;
	private final BigDecimal MITA;
	private final BigDecimal MAXIMAL_MASSGEGEBENES_EINKOMMEN;
	private final BigDecimal MINIMAL_MASSGEGEBENES_EINKOMMEN;

	public TagesschuleTarifRechner(@Nonnull final BigDecimal mataMitPedagogischeBetreuung,
		@Nonnull final BigDecimal mataOhnePedagogischeBetreuung, @Nonnull final BigDecimal mita,
		@Nonnull final BigDecimal maxMassgegebenesEinkommen, @Nonnull final BigDecimal minMassgegebenesEinkommen) {
		this.MATA_MIT_PEDAGOGISCHE_BETREUUNG = mataMitPedagogischeBetreuung;
		this.MATA_OHNE_PEDAGOGISCHE_BETREUUNG = mataOhnePedagogischeBetreuung;
		this.MITA = mita;
		this.MAXIMAL_MASSGEGEBENES_EINKOMMEN = maxMassgegebenesEinkommen;
		this.MINIMAL_MASSGEGEBENES_EINKOMMEN = minMassgegebenesEinkommen;
	}

	public BigDecimal calculateTarifProStunde(@Nonnull Gesuch gesuch, boolean paedagogischerBetreuung,
		@Nonnull BigDecimal massengebendesEinkommenMitAbzug) {
		//socialhilfe Bezueger bekommen der Minimal Tarif
		if (gesuch.getFamiliensituationContainer() != null
			&& gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null) {
			if (gesuch.getFamiliensituationContainer().getFamiliensituationJA().getSozialhilfeBezueger() != null
				&& gesuch.getFamiliensituationContainer().getFamiliensituationJA().getSozialhilfeBezueger()) {
				return MathUtil.DEFAULT.from(MITA);
			}
		}

		requireNonNull(gesuch.getGesuchsteller1());
		requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		FinanzielleSituation basisJahrGS1 =
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();

		// if Verlangerung oder der GS es nicht deklarieren will => max tarif
		// TODO add wenn der GS es nicht deklarieren will (noch nicht im DB/GUI)
		if (basisJahrGS1.getSteuerveranlagungErhalten()) {
			if (paedagogischerBetreuung) {
				return MathUtil.DEFAULT.from(MATA_MIT_PEDAGOGISCHE_BETREUUNG);
			} else {
				return MathUtil.DEFAULT.from(MATA_OHNE_PEDAGOGISCHE_BETREUUNG);
			}
		}

		BigDecimal tarifProStunde = calculate(massengebendesEinkommenMitAbzug,
			paedagogischerBetreuung);

		return tarifProStunde;
	}

	private BigDecimal calculate(@Nonnull BigDecimal massengebendesEinkommenMitAbzug,
		boolean paedagogischerBetreuung) {
		BigDecimal mataMinusMita = null;
		if (paedagogischerBetreuung) {
			mataMinusMita = MathUtil.EXACT.subtract(MATA_MIT_PEDAGOGISCHE_BETREUUNG, MITA);
		} else {
			mataMinusMita = MathUtil.EXACT.subtract(MATA_OHNE_PEDAGOGISCHE_BETREUUNG, MITA);
		}
		BigDecimal maxmEMinusMinmE = MathUtil.EXACT.subtract(MAXIMAL_MASSGEGEBENES_EINKOMMEN,
			MINIMAL_MASSGEGEBENES_EINKOMMEN);
		BigDecimal divided = MathUtil.EXACT.divide(mataMinusMita, maxmEMinusMinmE);

		BigDecimal meMinusMinmE = MathUtil.EXACT.subtract(massengebendesEinkommenMitAbzug,
			MINIMAL_MASSGEGEBENES_EINKOMMEN);

		BigDecimal multiplyDividedMeMinusMinmE = MathUtil.EXACT.multiply(divided, meMinusMinmE);

		return MathUtil.DEFAULT.addNullSafe(multiplyDividedMeMinusMinmE, MITA);
	}

}
