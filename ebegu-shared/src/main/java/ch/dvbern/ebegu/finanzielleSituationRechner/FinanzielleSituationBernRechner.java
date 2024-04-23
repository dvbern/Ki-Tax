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

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import org.apache.commons.lang.NotImplementedException;

public class FinanzielleSituationBernRechner extends AbstractFinanzielleSituationRechner {

	/**
	 * Nimmt das uebergebene FinanzielleSituationResultateDTO und mit den Daten vom Gesuch, berechnet alle im
	 * FinanzielleSituationResultateDTO benoetigten Daten und setzt sie direkt im dto.
	 */
	public void setFinanzielleSituationParameters(
		@Nonnull Gesuch gesuch,
		final FinanzielleSituationResultateDTO finSitResultDTO,
		boolean hasSecondGesuchsteller) {
		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());
		finSitResultDTO.setGeschaeftsgewinnDurchschnittGesuchsteller1(calcGeschaeftsgewinnDurchschnitt(
			finanzielleSituationGS1));

		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		FinanzielleSituation finanzielleSituationGS2 = null;
		if (hasSecondGesuchsteller && gesuch.getGesuchsteller2() != null) {
			finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
			finSitResultDTO.setGeschaeftsgewinnDurchschnittGesuchsteller2(calcGeschaeftsgewinnDurchschnitt(
				finanzielleSituationGS2));
		}

		calculateZusammen(finSitResultDTO,
			finanzielleSituationGS1, finSitResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller1(),
			finanzielleSituationGS2, finSitResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller2());
	}

	/**
	 * Nimmt das uebergebene FinanzielleSituationResultateDTO und mit den Daten vom Gesuch, berechnet alle im
	 * FinanzielleSituationResultateDTO benoetigten Daten.
	 */
	public void setEinkommensverschlechterungParameters(
		@Nonnull Gesuch gesuch, int basisJahrPlus,
		final FinanzielleSituationResultateDTO einkVerResultDTO, boolean hasSecondGesuchsteller) {
		Einkommensverschlechterung einkommensverschlechterungGS1Bjp1 =
			getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 1);
		Einkommensverschlechterung einkommensverschlechterungGS1Bjp2 =
			getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 2);
		final FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());
		BigDecimal geschaeftsgewinnDurchschnittGesuchsteller1 = calcGeschaeftsgewinnDurchschnitt(
			finanzielleSituationGS1,
			einkommensverschlechterungGS1Bjp1,
			einkommensverschlechterungGS1Bjp2,
			gesuch.extractEinkommensverschlechterungInfo(),
			basisJahrPlus);
		einkVerResultDTO.setGeschaeftsgewinnDurchschnittGesuchsteller1(geschaeftsgewinnDurchschnittGesuchsteller1);

		// Die Daten fuer GS 2 werden nur beruecksichtigt, wenn es (aktuell) zwei Gesuchsteller hat
		Einkommensverschlechterung einkommensverschlechterungGS2Bjp1 = null;
		Einkommensverschlechterung einkommensverschlechterungGS2Bjp2 = null;
		if (hasSecondGesuchsteller) {
			einkommensverschlechterungGS2Bjp1 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 1);
			einkommensverschlechterungGS2Bjp2 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 2);
			final FinanzielleSituation finanzielleSituationGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
			einkVerResultDTO.setGeschaeftsgewinnDurchschnittGesuchsteller2(
				calcGeschaeftsgewinnDurchschnitt(finanzielleSituationGS2, einkommensverschlechterungGS2Bjp1,
					einkommensverschlechterungGS2Bjp2, gesuch.extractEinkommensverschlechterungInfo(), basisJahrPlus));
		}

		if (basisJahrPlus == 2) {
			calculateZusammen(
				einkVerResultDTO,
				einkommensverschlechterungGS1Bjp2, einkVerResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller1(),
				einkommensverschlechterungGS2Bjp2, einkVerResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller2());
		} else {
			calculateZusammen(
				einkVerResultDTO,
				einkommensverschlechterungGS1Bjp1, einkVerResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller1(),
				einkommensverschlechterungGS2Bjp1, einkVerResultDTO.getGeschaeftsgewinnDurchschnittGesuchsteller2());
		}
	}

	@Override
	public boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		// bei Bern rechnen wir nie nach Veranlagung.
		throw new NotImplementedException();
	}
}
