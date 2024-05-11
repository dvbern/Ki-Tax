/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

public class FinanzielleSituationLuzernRechner extends AbstractFinanzielleSituationRechner {

	@Override
	public void setFinanzielleSituationParameters(
		@Nonnull Gesuch gesuch,
		final FinanzielleSituationResultateDTO finSitResultDTO,
		boolean hasSecondGesuchsteller) {
		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());
		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		FinanzielleSituation finanzielleSituationGS2 = null;
		if (hasSecondGesuchsteller && gesuch.getGesuchsteller2() != null) {
			finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
		}
		calculateFinSit(finanzielleSituationGS1, finanzielleSituationGS2, finSitResultDTO);
	}

	/**
	 * calculate massgebendes einkommen for each antragsteller separately and stores variables in finSitResultDTO
	 */
	@SuppressWarnings("PMD.UnusedPrivateMethod") // false postive
	private void calculateFinSit(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2,
		@Nonnull FinanzielleSituationResultateDTO finSitResultDTO
	) {
		var einkommenGS1 = calcEinkommen(finanzielleSituationGS1, null);
		var abzuegeGS1 = calcAbzuege(finanzielleSituationGS1, null);
		var vermoegen10PercentGS1 = calcVermoegen10Prozent(finanzielleSituationGS1, null);
		var massgebendesEinkommenGS1 = calculateMassgebendesEinkommen(
			einkommenGS1,
			abzuegeGS1,
			vermoegen10PercentGS1
		);

		finSitResultDTO.setEinkommenGS1(einkommenGS1);
		finSitResultDTO.setAbzuegeGS1(abzuegeGS1);
		finSitResultDTO.setVermoegenXPercentAnrechenbarGS1(vermoegen10PercentGS1);
		finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS1(massgebendesEinkommenGS1);

		var einkommenGS2 = calcEinkommen(finanzielleSituationGS2, null);
		var abzuegeGS2 = calcAbzuege(finanzielleSituationGS2, null);
		var vermoegen10PercenGS2 = calcVermoegen10Prozent(finanzielleSituationGS2, null);
		var massgebendesEinkommenGS2 = calculateMassgebendesEinkommen(
			einkommenGS2,
			abzuegeGS2,
			vermoegen10PercenGS2
		);

		finSitResultDTO.setEinkommenGS2(einkommenGS2);
		finSitResultDTO.setAbzuegeGS2(abzuegeGS2);
		finSitResultDTO.setVermoegenXPercentAnrechenbarGS2(vermoegen10PercenGS2);
		finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS2(massgebendesEinkommenGS2);

		finSitResultDTO.setMassgebendesEinkVorAbzFamGr(
			add(finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS1(), finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS2())
		);
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
			calculateFinSit(
				einkommensverschlechterungGS1Bjp2,
				einkommensverschlechterungGS2Bjp2,
				einkVerResultDTO);
		} else {
			calculateFinSit(
				einkommensverschlechterungGS1Bjp1,
				einkommensverschlechterungGS2Bjp1,
				einkVerResultDTO);
		}
	}

	private BigDecimal calculateMassgebendesEinkommen(
		@Nonnull BigDecimal einkommen,
		@Nonnull BigDecimal abzuege,
		@Nonnull BigDecimal vermoegen10Percent
	) {
		BigDecimal anrechenbaresEinkommen = add(einkommen, vermoegen10Percent);
		return MathUtil.positiveNonNullAndRound(
			subtract(anrechenbaresEinkommen, abzuege));
	}

	private BigDecimal getAdditionEinkommenLiegenschaftenGS(@Nullable AbstractFinanzielleSituation finanzielleSituation) {
		if (finanzielleSituation == null || finanzielleSituation.getAbzuegeLiegenschaft() == null) {
			return BigDecimal.ZERO;
		}
		return MathUtil.isPositive(finanzielleSituation.getAbzuegeLiegenschaft()) ?
				BigDecimal.ZERO :
				finanzielleSituation.getAbzuegeLiegenschaft().abs();
	}

	/**
	 * Als Abzuege habe ich EinkaeufeVorsorge genommen
	 * Die Geschäftverlust habe ich bei der Einkommen genommen
	 */
	@Override
	public BigDecimal calcAbzuege(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2
	) {
		BigDecimal totalAbzuege = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			BigDecimal abzuegeGS1;
			if (!calculateByVeranlagung(finanzielleSituationGS1)) {
				abzuegeGS1 = finanzielleSituationGS1.getSelbstdeklaration() != null ? finanzielleSituationGS1.getSelbstdeklaration().calculateAbzuege() : BigDecimal.ZERO;
			} else {
				// Keine Abzüge bei Berechnung nach Veranlagung
				abzuegeGS1 = BigDecimal.ZERO;
			}
			totalAbzuege = totalAbzuege.add(abzuegeGS1);
		}
		if (finanzielleSituationGS2 != null) {
			BigDecimal abzuegeGS2;
			if (!calculateByVeranlagung(finanzielleSituationGS2)) {
				abzuegeGS2 = finanzielleSituationGS2.getSelbstdeklaration() != null ? finanzielleSituationGS2.getSelbstdeklaration().calculateAbzuege() : BigDecimal.ZERO;
			} else {
				// Keine Abzüge bei Berechnung nach Veranlagung
				abzuegeGS2 = BigDecimal.ZERO;
			}
			totalAbzuege = totalAbzuege.add(abzuegeGS2);
		}
		return MathUtil.positiveNonNullAndRound(totalAbzuege);
	}

	private BigDecimal calcVermoegen10Prozent(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2) {
		BigDecimal gs1SteuerbaresVermoegen = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			if (!calculateByVeranlagung(finanzielleSituationGS1)) {
				gs1SteuerbaresVermoegen = finanzielleSituationGS1.getSelbstdeklaration() != null ? finanzielleSituationGS1.getSelbstdeklaration().calculateVermoegen() : BigDecimal.ZERO;
			} else {
				gs1SteuerbaresVermoegen = finanzielleSituationGS1.getSteuerbaresVermoegen();
			}
		}
		BigDecimal gs2SteuerbaresVermoegen = BigDecimal.ZERO;
		if (finanzielleSituationGS2 != null) {
			if (!calculateByVeranlagung(finanzielleSituationGS2)) {
				gs2SteuerbaresVermoegen = finanzielleSituationGS2.getSelbstdeklaration() != null ? finanzielleSituationGS2.getSelbstdeklaration().calculateVermoegen() : BigDecimal.ZERO;
			} else {
				gs2SteuerbaresVermoegen = finanzielleSituationGS2.getSteuerbaresVermoegen();
			}
		}

		final BigDecimal totalBruttovermoegen = add(gs1SteuerbaresVermoegen, gs2SteuerbaresVermoegen);

		BigDecimal total = percent(totalBruttovermoegen, 10);
		return MathUtil.GANZZAHL.from(total);
	}

	@Nonnull
	private BigDecimal calcEinkommen(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation1,
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation2
	) {
		BigDecimal total = BigDecimal.ZERO;
		total = calcEinkommenProGS(abstractFinanzielleSituation1, total);
		total = calcEinkommenProGS(abstractFinanzielleSituation2, total);
		return MathUtil.positiveNonNullAndRound(total);
	}

	@Nonnull
	private BigDecimal calcEinkommenProGS(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation,
		@Nonnull BigDecimal total
	) {
		if (abstractFinanzielleSituation != null) {
			if (!calculateByVeranlagung(abstractFinanzielleSituation)) {
				total = total.add(abstractFinanzielleSituation.getSelbstdeklaration() != null ? abstractFinanzielleSituation.getSelbstdeklaration().calculateEinkuenfte() : BigDecimal.ZERO);
			} else {
				total = add(total, abstractFinanzielleSituation.getSteuerbaresEinkommen());
				total = add(total, abstractFinanzielleSituation.getGeschaeftsverlust());
				total = add(total, abstractFinanzielleSituation.getEinkaeufeVorsorge());
				total = add(total, getAdditionEinkommenLiegenschaftenGS(abstractFinanzielleSituation));
			}
		}
		return total;
	}

	@Override
	public boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		// for Einkommensverschlechterung we always use Selbstdeklaration
		if (!(abstractFinanzielleSituation instanceof FinanzielleSituation)) {
			return false;
		}
		FinanzielleSituation finanzielleSituation = (FinanzielleSituation) abstractFinanzielleSituation;
		if (
			finanzielleSituation.getQuellenbesteuert() == null
			|| finanzielleSituation.getVeranlagt() == null
		) {
			return false;
		}
		boolean veranlagtOrVeranlagtVorjahr = finanzielleSituation.getVeranlagt();
		if (finanzielleSituation.getVeranlagtVorjahr() != null) {
			veranlagtOrVeranlagtVorjahr = veranlagtOrVeranlagtVorjahr || finanzielleSituation.getVeranlagtVorjahr();
		}
		return !finanzielleSituation.getQuellenbesteuert()
			&& isSameVeranlagungAsVorjahr(finanzielleSituation)
			&& veranlagtOrVeranlagtVorjahr;
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

	// one of gemeinsameStekVorjahr or alleinigeStekVorjahr could be null
	private boolean isSameVeranlagungAsVorjahr(@Nonnull FinanzielleSituation finanzielleSituation) {
		boolean same = false;
		if (finanzielleSituation.getGemeinsameStekVorjahr() != null) {
			same = finanzielleSituation.getGemeinsameStekVorjahr();
		}
		if (finanzielleSituation.getAlleinigeStekVorjahr() != null) {
			same |= finanzielleSituation.getAlleinigeStekVorjahr();
		}
		return same;
	}
}
