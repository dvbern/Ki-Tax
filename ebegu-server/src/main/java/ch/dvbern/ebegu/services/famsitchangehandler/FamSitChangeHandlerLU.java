/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.famsitchangehandler;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchstellerService;

public class FamSitChangeHandlerLU extends SharedFamSitChangeDefaultHandler {

	public FamSitChangeHandlerLU(
		GesuchstellerService gesuchstellerService, EinstellungService einstellungService) {
		super(gesuchstellerService, einstellungService);
	}

	@Override
	public void handleFamSitChange(
		Gesuch gesuch,
		FamiliensituationContainer mergedFamiliensituationContainer,
		Familiensituation oldFamiliensituation) {
		if (oldFamiliensituation == null
			|| mergedFamiliensituationContainer.getFamiliensituationJA() == null
			|| gesuch.getGesuchsteller1() == null) {
			return;
		}

		boolean isKonkubinat = oldFamiliensituation.getFamilienstatus() == EnumFamilienstatus.KONKUBINAT
			|| oldFamiliensituation.getFamilienstatus() == EnumFamilienstatus.KONKUBINAT_KEIN_KIND;

		// KONKUBINAT => VERHEIRATET: beide Container löschen
		if (isKonkubinat
			&& mergedFamiliensituationContainer.getFamiliensituationJA().getFamilienstatus()
			== EnumFamilienstatus.VERHEIRATET
			&& gesuch.getGesuchsteller2() != null) {
			gesuch.getGesuchsteller1().setFinanzielleSituationContainer(null);
			gesuch.getGesuchsteller2().setFinanzielleSituationContainer(null);
		}

		// ALLEINERZIEHEND => VERHEIRATET: Container GS1 löschen
		boolean isAlleinerziehend = oldFamiliensituation.getFamilienstatus() == EnumFamilienstatus.ALLEINERZIEHEND;
		if (isAlleinerziehend && mergedFamiliensituationContainer.getFamiliensituationJA().getFamilienstatus()
			== EnumFamilienstatus.VERHEIRATET
			&& gesuch.getGesuchsteller1() != null) {
			gesuch.getGesuchsteller1().setFinanzielleSituationContainer(null);
		}

		// VERHEIRATET => KONKUBINAT: Container GS1 löschen
		// VERHEIRATET => ALLEINERZIEHEND: Container GS1 löschen
		boolean oldIsVerheiratet = oldFamiliensituation.getFamilienstatus() == EnumFamilienstatus.VERHEIRATET;
		boolean newIsKonkubinatOrAlleinerziehend =
			mergedFamiliensituationContainer.getFamiliensituationJA().getFamilienstatus()
				== EnumFamilienstatus.KONKUBINAT
				|| mergedFamiliensituationContainer.getFamiliensituationJA().getFamilienstatus()
				== EnumFamilienstatus.KONKUBINAT_KEIN_KIND
				|| mergedFamiliensituationContainer.getFamiliensituationJA().getFamilienstatus()
				== EnumFamilienstatus.ALLEINERZIEHEND;

		if (oldIsVerheiratet && newIsKonkubinatOrAlleinerziehend) {
			gesuch.getGesuchsteller1().setFinanzielleSituationContainer(null);
		}
	}

	@Override
	protected void adaptFinSitDataInMutation(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation,
		Familiensituation newFamiliensituation,
		LocalDate gesuchsperiodeBis) {
		super.adaptFinSitDataInMutation(gesuch, familiensituationContainer, loadedFamiliensituation, newFamiliensituation, gesuchsperiodeBis);

		if (gesuch.getFinSitTyp() == FinanzielleSituationTyp.LUZERN &&
			isScheidung(loadedFamiliensituation, newFamiliensituation)) {
			gesuch.setFinSitAenderungGueltigAbDatum(newFamiliensituation.getAenderungPer());
		}
	}

	private boolean isScheidung(
		@Nonnull Familiensituation oldFamiliensituation,
		Familiensituation newFamiliensituation) {
		if (oldFamiliensituation.getFamilienstatus() != EnumFamilienstatus.VERHEIRATET) {
			return false;
		}

		return newFamiliensituation.getFamilienstatus() == EnumFamilienstatus.ALLEINERZIEHEND;
	}
}
