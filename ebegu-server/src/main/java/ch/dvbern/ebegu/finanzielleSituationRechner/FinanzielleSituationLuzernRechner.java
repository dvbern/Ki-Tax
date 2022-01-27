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

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.MathUtil;

public class FinanzielleSituationLuzernRechner extends AbstractFinanzielleSituationRechner {

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
		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		FinanzielleSituation finanzielleSituationGS2 = null;
		if (hasSecondGesuchsteller && gesuch.getGesuchsteller2() != null) {
			finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
		}
		calculateZusammen(
			finSitResultDTO,
			finanzielleSituationGS1,
			finanzielleSituationGS2);
		finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS1(getMassgegebenesEinkommenAleine(finanzielleSituationGS1));
		finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS2(getMassgegebenesEinkommenAleine(finanzielleSituationGS2));
	}

	private BigDecimal getMassgegebenesEinkommenAleine(@Nullable FinanzielleSituation finanzielleSituation) {
		BigDecimal einkommenAleine = calcEinkommen(finanzielleSituation, null);
		BigDecimal nettoVermoegenXProzent = calcVermoegen10Prozent(finanzielleSituation, null);
		BigDecimal abzuegeAleine = calcAbzuege(finanzielleSituation, null);
		BigDecimal anrechenbaresEinkommen = add(einkommenAleine, nettoVermoegenXProzent);
		return MathUtil.positiveNonNullAndRound(
			subtract(anrechenbaresEinkommen, abzuegeAleine));
	}

	private void calculateZusammen(
		@Nonnull final FinanzielleSituationResultateDTO finSitResultDTO,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2) {

		finSitResultDTO.setEinkommenBeiderGesuchsteller(calcEinkommen(
			finanzielleSituationGS1,
			finanzielleSituationGS2));
		finSitResultDTO.setNettovermoegenXProzent(calcVermoegen10Prozent(
			finanzielleSituationGS1,
			finanzielleSituationGS2));
		finSitResultDTO.setAbzuegeBeiderGesuchsteller(calcAbzuege(finanzielleSituationGS1, finanzielleSituationGS2));

		finSitResultDTO.setAnrechenbaresEinkommen(add(
			finSitResultDTO.getEinkommenBeiderGesuchsteller(), finSitResultDTO.getNettovermoegenXProzent())
		);
		finSitResultDTO.setMassgebendesEinkVorAbzFamGr(
			MathUtil.positiveNonNullAndRound(
				subtract(
					finSitResultDTO.getAnrechenbaresEinkommen(),
					finSitResultDTO.getAbzuegeBeiderGesuchsteller())));
	}

	/**
	 * Als Abzuege habe ich die AbzuegeLiegenschaft und EinkaeufeVorsorge genommen
	 * Die Geschäftverlust habe ich bei der Einkommen genommen
	 */
	@Override
	protected BigDecimal calcAbzuege(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2
	) {
		BigDecimal totalAbzuege = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			BigDecimal abzuegeGS1;
			if (finanzielleSituationGS1.getSelbstdeklaration() != null) {
				abzuegeGS1 = finanzielleSituationGS1.getSelbstdeklaration().calculateAbzuege();
			} else {
				abzuegeGS1 = calcAbzuegeFromVeranlagung(finanzielleSituationGS1);
			}
			totalAbzuege = totalAbzuege.add(abzuegeGS1);
		}
		if (finanzielleSituationGS2 != null) {
			BigDecimal abzuegeGS2;
			if (finanzielleSituationGS2.getSelbstdeklaration() != null) {
				abzuegeGS2 = finanzielleSituationGS2.getSelbstdeklaration().calculateAbzuege();
			} else {
				abzuegeGS2 = calcAbzuegeFromVeranlagung(finanzielleSituationGS2);
			}
			totalAbzuege = totalAbzuege.add(abzuegeGS2);
		}
		return totalAbzuege;
	}

	private BigDecimal calcAbzuegeFromVeranlagung(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		BigDecimal total = BigDecimal.ZERO;
		total = add(total, finanzielleSituation.getAbzuegeLiegenschaft());
		total = add(total, finanzielleSituation.getEinkaeufeVorsorge());
		return total;
	}

	private BigDecimal calcVermoegen10Prozent(
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2) {
		BigDecimal gs1SteuerbaresVermoegen = BigDecimal.ZERO;
		if (finanzielleSituationGS1 != null) {
			if (finanzielleSituationGS1.getSelbstdeklaration() != null) {
				gs1SteuerbaresVermoegen = finanzielleSituationGS1.getSelbstdeklaration().calculateVermoegen();
			} else {
				gs1SteuerbaresVermoegen = finanzielleSituationGS1.getSteuerbaresVermoegen();
			}
		}
		BigDecimal gs2SteuerbaresVermoegen = BigDecimal.ZERO;
		if (finanzielleSituationGS2 != null) {
			if (finanzielleSituationGS2.getSelbstdeklaration() != null) {
				gs2SteuerbaresVermoegen = finanzielleSituationGS2.getSelbstdeklaration().calculateVermoegen();
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
		return total;
	}

	@Nonnull
	private BigDecimal calcEinkommenProGS(
		@Nullable AbstractFinanzielleSituation abstractFinanzielleSituation,
		@Nonnull BigDecimal total
	) {
		if (abstractFinanzielleSituation != null) {
			if (abstractFinanzielleSituation.getSelbstdeklaration() != null) {
				total = total.add(abstractFinanzielleSituation.getSelbstdeklaration().calculateEinkuenfte());
			} else {
				total = add(total, abstractFinanzielleSituation.getSteuerbaresEinkommen());
				total = subtract(total, abstractFinanzielleSituation.getGeschaeftsverlust());
			}
		}
		return total;
	}
}
