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

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;

public class FamSitChangeHandlerARBean extends FamSitChangeHandlerBernBean {
	public FamSitChangeHandlerARBean(
		GesuchstellerService gesuchstellerService, EinstellungService einstellungService,
		FinanzielleSituationService finanzielleSituationService) {
		super(gesuchstellerService, einstellungService, finanzielleSituationService);
	}

	@Override
	public void handleFamSitChange(
		Gesuch gesuch,
		FamiliensituationContainer mergedFamiliensituationContainer,
		Familiensituation oldFamiliensituation) {
		if (gesuch.getFinSitTyp() == FinanzielleSituationTyp.APPENZELL) {
			handleFamSitChangeAR(gesuch, mergedFamiliensituationContainer, oldFamiliensituation);
		}
	}

	private void handleFamSitChangeAR(
		@Nonnull Gesuch gesuch,
		FamiliensituationContainer mergedFamiliensituationContainer,
		Familiensituation oldFamiliensituation) {

		if (oldFamiliensituation == null
			|| mergedFamiliensituationContainer.getFamiliensituationJA() == null
			|| gesuch.getGesuchsteller1() == null) {
			return;
		}

		if (oldFamiliensituation.isSpezialFallAR()
			&& !mergedFamiliensituationContainer.getFamiliensituationJA().isSpezialFallAR()) {
			resetFinSitARZusatzangabenPartner(gesuch);
			mergedFamiliensituationContainer.getFamiliensituationJA().setGemeinsameSteuererklaerung(null);
		}
	}

	private static void resetFinSitARZusatzangabenPartner(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		final FinanzielleSituationContainer finSitGS1Container =
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer();
		if (finSitGS1Container != null
			&& finSitGS1Container.getFinanzielleSituationJA() != null
			&& finSitGS1Container.getFinanzielleSituationJA().getFinSitZusatzangabenAppenzell() != null) {
			finSitGS1Container
				.getFinanzielleSituationJA()
				.getFinSitZusatzangabenAppenzell()
				.setZusatzangabenPartner(null);
		}
	}
}
