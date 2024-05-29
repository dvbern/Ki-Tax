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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;

public class FamSitChangeHandlerSchwyz extends SharedFamSitChangeDefaultHandler {
	private final FinanzielleSituationService finanzielleSituationService;

	public FamSitChangeHandlerSchwyz(
		GesuchstellerService gesuchstellerService,
		EinstellungService einstellungService,
		FinanzielleSituationService finanzielleSituationService) {
		super(gesuchstellerService, einstellungService);
		this.finanzielleSituationService = finanzielleSituationService;
	}

	@Override
	public void adaptFinSitDataOnFamSitChange(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation) {
		Familiensituation newFamiliensituation = familiensituationContainer.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);

		adaptSchwyzFinSitDataOnFamSitChange(gesuch, loadedFamiliensituation, newFamiliensituation);


		super.adaptFinSitDataOnFamSitChange(gesuch, familiensituationContainer, loadedFamiliensituation);
	}

	private void adaptSchwyzFinSitDataOnFamSitChange(Gesuch gesuch, @Nullable Familiensituation loadedFamiliensituation, Familiensituation newFamiliensituation) {
		if (loadedFamiliensituation == null || gesuch.getFinSitTyp() != FinanzielleSituationTyp.SCHWYZ) {
			return;
		}
		if (isSchwyzChangeFromGemeinsamStekToAlleine(loadedFamiliensituation, newFamiliensituation)
			&& gesuch.getGesuchsteller1() != null
			&& gesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null) {

			finanzielleSituationService.resetCompleteSchwyzFinSitData(gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA(), gesuch.getGesuchsteller1());

			gesuch.setEinkommensverschlechterungInfoContainer(null);

			if (gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer() != null) {
				finanzielleSituationService.resetCompleteSchwyzFinSitData(gesuch.getGesuchsteller1()
					.getEinkommensverschlechterungContainer()
					.getEkvJABasisJahrPlus1(), gesuch.getGesuchsteller1());
			}
		}
	}

	private static boolean isSchwyzChangeFromGemeinsamStekToAlleine(@Nonnull Familiensituation loadedFamiliensituation, Familiensituation newFamiliensituation) {
		return loadedFamiliensituation.getGesuchstellerKardinalitaet() == EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			&& newFamiliensituation.getGesuchstellerKardinalitaet() == EnumGesuchstellerKardinalitaet.ALLEINE
			&& Boolean.TRUE.equals(loadedFamiliensituation.getGemeinsameSteuererklaerung());
	}

	@Override
	protected boolean isNeededToRemoveGesuchsteller2(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		@Nullable Familiensituation familiensituationErstgesuch
	) {
		if (familiensituationErstgesuch == null) {
			return false;
		}
		final LocalDate gpEnd = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		if (gesuch.isMutation()) {
			return newFamiliensituation.getAenderungPer() != null &&
				gpEnd.isAfter(newFamiliensituation.getAenderungPer()) ||
				gpEnd.isEqual(newFamiliensituation.getAenderungPer());
		}
		return familiensituationErstgesuch.hasSecondGesuchsteller(gpEnd) && !newFamiliensituation.hasSecondGesuchsteller(gpEnd);
	}
}
