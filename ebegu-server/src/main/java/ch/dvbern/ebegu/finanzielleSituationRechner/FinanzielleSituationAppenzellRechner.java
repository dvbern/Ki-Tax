/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang.NotImplementedException;

public class FinanzielleSituationAppenzellRechner extends AbstractFinanzielleSituationRechner {

	@Override
	public void setFinanzielleSituationParameters(
		@Nonnull Gesuch gesuch,
		final FinanzielleSituationResultateDTO finSitResultDTO,
		boolean hasSecondGesuchsteller) {
		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());
		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		FinanzielleSituation finanzielleSituationGS2 = null;
		if (hasSecondGesuchsteller) {
			finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
		}
		calculateFinSit(finanzielleSituationGS1, finanzielleSituationGS2, finSitResultDTO);
	}

	/**
	 * calculate massgebendes einkommen for each antragsteller separately and stores variables in finSitResultDTO
	 */
	private void calculateFinSit(
		@Nullable FinanzielleSituation finanzielleSituationGS1,
		@Nullable FinanzielleSituation finanzielleSituationGS2,
		@Nonnull FinanzielleSituationResultateDTO finSitResultDTO
	) {
		var einkommenGS1 = calcEinkommen(finanzielleSituationGS1);
		var aufrechnungFaktorenGS1 = calcAufrechnungFaktoren(finanzielleSituationGS1);
		var vermoegen15PercentGS1 = calcualteSteuerbaresVermoegen15Prozent(finanzielleSituationGS1);
		var massgebendesEinkommenGS1 = calculateMassgebendesEinkommen(
			einkommenGS1,
			aufrechnungFaktorenGS1,
			vermoegen15PercentGS1
		);

		finSitResultDTO.setEinkommenGS1(einkommenGS1);
		finSitResultDTO.setVermoegenXPercentAnrechenbarGS1(vermoegen15PercentGS1);
		finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS1(massgebendesEinkommenGS1);

		var einkommenGS2 = calcEinkommen(finanzielleSituationGS2);
		var aufrechnungFaktorenGS2 = calcAufrechnungFaktoren(finanzielleSituationGS2);
		var vermoegen15PercenGS2 = calcualteSteuerbaresVermoegen15Prozent(finanzielleSituationGS2);
		var massgebendesEinkommenGS2 = calculateMassgebendesEinkommen(
			einkommenGS2,
			aufrechnungFaktorenGS2,
			vermoegen15PercenGS2
		);

		finSitResultDTO.setEinkommenGS2(einkommenGS2);
		finSitResultDTO.setVermoegenXPercentAnrechenbarGS2(vermoegen15PercenGS2);
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
			calculateEKVFinSit(
				einkommensverschlechterungGS1Bjp2,
				einkommensverschlechterungGS2Bjp2,
				einkVerResultDTO);
		} else {
			calculateEKVFinSit(
				einkommensverschlechterungGS1Bjp1,
				einkommensverschlechterungGS2Bjp1,
				einkVerResultDTO);
		}
	}

	@SuppressWarnings("PMD.UnusedFormalParameter")
	private void calculateEKVFinSit(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2,
		@Nonnull FinanzielleSituationResultateDTO finSitResultDTO
	) {
		// TODO noch nicht definiert, wird aber keine finanzielleVerhaeltnisse sein
	}

	private BigDecimal calculateMassgebendesEinkommen(
		@Nonnull BigDecimal einkommen,
		@Nonnull BigDecimal aufrechnungFaktoren,
		@Nonnull BigDecimal vermoegen15Percent
	) {
		BigDecimal anrechenbaresEinkommen = add(einkommen, vermoegen15Percent);
		return MathUtil.positiveNonNullAndRound(add(anrechenbaresEinkommen, aufrechnungFaktoren));
	}

	@Nonnull
	private BigDecimal calcEinkommen(@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation1) {
		BigDecimal total = BigDecimal.ZERO;
		if(abstractFinanzielleSituation1 != null) {
			total =  add(total, abstractFinanzielleSituation1.getSteuerbaresEinkommen());
		}

		return MathUtil.positiveNonNullAndRound(total);
	}

	private BigDecimal calcAufrechnungFaktoren(@Nullable FinanzielleSituation finanzielleSituation1) {
		BigDecimal total = BigDecimal.ZERO;
		if(finanzielleSituation1 != null && finanzielleSituation1.getFinSitZusatzangabenAppenzell() != null) {
			total =  add(total, calcAufrechnungFaktoren(finanzielleSituation1.getFinSitZusatzangabenAppenzell()));
		}

		return MathUtil.positiveNonNullAndRound(total);
	}

	private BigDecimal calcAufrechnungFaktoren(@Nonnull FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell){
		BigDecimal total = BigDecimal.ZERO;
		total = add(total, finSitZusatzangabenAppenzell.getSaeule3a());
		total = add(total, finSitZusatzangabenAppenzell.getSaeule3aNichtBvg());
		total = add(total, finSitZusatzangabenAppenzell.getBeruflicheVorsorge());
		total = add(total, finSitZusatzangabenAppenzell.getLiegenschaftsaufwand());
		total = add(total, finSitZusatzangabenAppenzell.getEinkuenfteBgsa());
		total = add(total, finSitZusatzangabenAppenzell.getVorjahresverluste());
		total = add(total, finSitZusatzangabenAppenzell.getPolitischeParteiSpende());
		total = add(total, finSitZusatzangabenAppenzell.getLeistungAnJuristischePersonen());
		return MathUtil.positiveNonNullAndRound(total);
	}

	@Override
	public boolean acceptEKV(
		BigDecimal massgebendesEinkommenBasisjahr,
		BigDecimal massgebendesEinkommenJahr,
		BigDecimal minimumEKV) {

		boolean result = massgebendesEinkommenBasisjahr.compareTo(BigDecimal.ZERO) > 0;
		if (result) {
			BigDecimal differenzGerundet = getCalculatedProzentualeDifferenzRounded(massgebendesEinkommenBasisjahr, massgebendesEinkommenJahr);
			// wenn es gibt mehr als minimumEKV in einer positive oder negative Richtung ist der EKV akkzeptiert
			return differenzGerundet.compareTo(minimumEKV.negate()) <= 0 || differenzGerundet.compareTo(minimumEKV) >= 0;
		}
		return false;
	}

	private BigDecimal calcualteSteuerbaresVermoegen15Prozent(@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation) {
		if (abstractFinanzielleSituation == null || isNullOrZero(abstractFinanzielleSituation.getSteuerbaresVermoegen())) {
			return BigDecimal.ZERO;
		}

		return MathUtil.EXACT.multiply(abstractFinanzielleSituation.getSteuerbaresVermoegen(), BigDecimal.valueOf(0.15));
	}

	private boolean isNullOrZero(BigDecimal number) {
		return number == null || number.compareTo(BigDecimal.ZERO) == 0;
	}

	@Override
	public boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		// bei Bern rechnen wir nie nach Veranlagung.
		throw new NotImplementedException();
	}
}