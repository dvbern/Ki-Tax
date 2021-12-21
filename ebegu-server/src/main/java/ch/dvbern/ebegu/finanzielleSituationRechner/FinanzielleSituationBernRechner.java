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

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.Gesuch;

public class FinanzielleSituationBernRechner extends AbstractFinanzielleSituationRechner {

	/**
	 * Berechnet das FinazDaten DTO fuer die Finanzielle Situation
	 *
	 * @param gesuch das Gesuch dessen finazDatenDTO gesetzt werden soll
	 */
	@Override
	public void calculateFinanzDaten(@Nonnull Gesuch gesuch, BigDecimal minimumEKV) {
		FinanzDatenDTO finanzDatenDTOAlleine = new FinanzDatenDTO();
		FinanzDatenDTO finanzDatenDTOZuZweit = new FinanzDatenDTO();

		// Finanzielle Situation berechnen
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOAlleine =
			calculateResultateFinanzielleSituation(gesuch, false);
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTOZuZweit =
			calculateResultateFinanzielleSituation(gesuch, true);

		finanzDatenDTOAlleine.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr());
		finanzDatenDTOZuZweit.setMassgebendesEinkBjVorAbzFamGr(finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr());

		//Berechnung wird nur ausgefuehrt wenn Daten vorhanden, wenn es keine gibt machen wir nichts
		EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
		if (ekvInfo != null && ekvInfo.getEinkommensverschlechterung()) {
			FinanzielleSituationResultateDTO resultateEKV1Alleine =
				calculateResultateEinkommensverschlechterung(gesuch, 1, false);
			FinanzielleSituationResultateDTO resultateEKV1ZuZweit =
				calculateResultateEinkommensverschlechterung(gesuch, 1, true);
			BigDecimal massgebendesEinkommenBasisjahrAlleine =
				finanzielleSituationResultateDTOAlleine.getMassgebendesEinkVorAbzFamGr();
			BigDecimal massgebendesEinkommenBasisjahrZuZweit =
				finanzielleSituationResultateDTOZuZweit.getMassgebendesEinkVorAbzFamGr();

			if (ekvInfo.getEkvFuerBasisJahrPlus1() != null && ekvInfo.getEkvFuerBasisJahrPlus1()) {
				finanzDatenDTOAlleine.setEkv1Erfasst(true);
				finanzDatenDTOZuZweit.setEkv1Erfasst(true);
				if (ekvInfo.getEkvBasisJahrPlus1Annulliert()) {
					finanzDatenDTOAlleine.setEkv1Annulliert(Boolean.TRUE);
					finanzDatenDTOZuZweit.setEkv1Annulliert(Boolean.TRUE);
				}
				// In der EKV 1 vergleichen wir immer mit dem Basisjahr
				handleEKV1(finanzDatenDTOAlleine, resultateEKV1Alleine.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrAlleine, minimumEKV);
				handleEKV1(finanzDatenDTOZuZweit, resultateEKV1ZuZweit.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrZuZweit, minimumEKV);
			}

			BigDecimal massgebendesEinkommenVorjahrAlleine;
			if (finanzDatenDTOAlleine.isEkv1AcceptedAndNotAnnuliert()) {
				massgebendesEinkommenVorjahrAlleine = resultateEKV1Alleine.getMassgebendesEinkVorAbzFamGr();
			} else {
				massgebendesEinkommenVorjahrAlleine = massgebendesEinkommenBasisjahrAlleine;
			}
			BigDecimal massgebendesEinkommenVorjahrZuZweit;
			if (finanzDatenDTOZuZweit.isEkv1AcceptedAndNotAnnuliert()) {
				massgebendesEinkommenVorjahrZuZweit = resultateEKV1ZuZweit.getMassgebendesEinkVorAbzFamGr();
			} else {
				massgebendesEinkommenVorjahrZuZweit = massgebendesEinkommenBasisjahrZuZweit;
			}

			if (ekvInfo.getEkvFuerBasisJahrPlus2() != null && ekvInfo.getEkvFuerBasisJahrPlus2()) {
				finanzDatenDTOAlleine.setEkv2Erfasst(true);
				finanzDatenDTOZuZweit.setEkv2Erfasst(true);
				if (ekvInfo.getEkvBasisJahrPlus2Annulliert()) {
					finanzDatenDTOAlleine.setEkv2Annulliert(Boolean.TRUE);
					finanzDatenDTOZuZweit.setEkv2Annulliert(Boolean.TRUE);
				}
				FinanzielleSituationResultateDTO resultateEKV2Alleine =
					calculateResultateEinkommensverschlechterung(gesuch, 2, false);
				FinanzielleSituationResultateDTO resultateEKV2ZuZweit =
					calculateResultateEinkommensverschlechterung(gesuch, 2, true);
				// In der EKV 2 vergleichen wir immer mit dem Basisjahr
				handleEKV2(finanzDatenDTOAlleine,
					resultateEKV2Alleine.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrAlleine,
					minimumEKV);
				handleEKV2(finanzDatenDTOZuZweit,
					resultateEKV2ZuZweit.getMassgebendesEinkVorAbzFamGr(),
					massgebendesEinkommenBasisjahrZuZweit,
					minimumEKV);
			} else {
				finanzDatenDTOAlleine.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahrAlleine);
				finanzDatenDTOZuZweit.setMassgebendesEinkBjP2VorAbzFamGr(massgebendesEinkommenVorjahrZuZweit);
			}
		}
		gesuch.setFinanzDatenDTO_alleine(finanzDatenDTOAlleine);
		gesuch.setFinanzDatenDTO_zuZweit(finanzDatenDTOZuZweit);
	}
}
