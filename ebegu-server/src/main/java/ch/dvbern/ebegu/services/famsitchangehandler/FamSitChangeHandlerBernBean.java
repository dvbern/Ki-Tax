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

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.util.EbeguUtil;

import static ch.dvbern.ebegu.services.util.ErwerbspensumHelper.isKonkubinatOhneKindAndGS2ErwerbspensumOmittable;

public class FamSitChangeHandlerBernBean implements FamSitChangeHandler {

	private final GesuchstellerService gesuchstellerService;
	private final EinstellungService einstellungService;
	private final FinanzielleSituationService finanzielleSituationService;

	public FamSitChangeHandlerBernBean(
		GesuchstellerService gesuchstellerService,
		EinstellungService einstellungService,
		FinanzielleSituationService finanzielleSituationService) {
		this.gesuchstellerService = gesuchstellerService;
		this.einstellungService = einstellungService;
		this.finanzielleSituationService = finanzielleSituationService;
	}
	@Override
	public void handleFamSitChange(
		Gesuch gesuch,
		FamiliensituationContainer mergedFamiliensituationContainer,
		Familiensituation oldFamiliensituation) {
		// noop
	}

	@Override
	public void removeGS2DataOnChangeFrom2To1GS(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		FamiliensituationContainer mergedFamiliensituationContainer,
		Familiensituation oldFamiliensituation) {
		final Familiensituation mergedFinSitJA = mergedFamiliensituationContainer.extractFamiliensituation();
		Objects.requireNonNull(mergedFinSitJA);

		if (gesuch.getGesuchsteller2() != null
			&& isNeededToRemoveGesuchsteller2(gesuch, mergedFinSitJA,
			oldFamiliensituation)
		) {
			gesuchstellerService.removeGesuchsteller(gesuch.getGesuchsteller2());
			gesuch.setGesuchsteller2(null);
			newFamiliensituation.setGemeinsameSteuererklaerung(false);
		}
	}

	@Override
	public void handlePossibleGS2Tausch(Gesuch gesuch, Familiensituation newFamiliensituation) {
		if (isGesuchBeendenBeiTauschGS2Active(gesuch)
			&& isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(newFamiliensituation, gesuch.getGesuchsperiode())
			&& gesuch.getGesuchsteller2() != null
			&& !gesuch.getGesuchsteller2().getErwerbspensenContainers().isEmpty()) {
			gesuch.getGesuchsteller2().getErwerbspensenContainers().clear();
		}
	}

	@Override
	public void handlePossibleKinderabzugFragenReset(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		Familiensituation oldFamiliensituation) {
		if (gesuch.getFinSitTyp() == FinanzielleSituationTyp.BERN_FKJV
			|| newFamiliensituation.getFamilienstatus() == EnumFamilienstatus.SCHWYZ &&
			oldFamiliensituation != null &&
			(oldFamiliensituation.getFamilienstatus() != newFamiliensituation.getFamilienstatus()
				|| oldFamiliensituation.getGesuchstellerKardinalitaet() != newFamiliensituation.getGesuchstellerKardinalitaet()) &&
			!Objects.equals(newFamiliensituation.getPartnerIdentischMitVorgesuch(), Boolean.FALSE)) {
			resetFragenKinderabzugAndSetToUeberpruefen(gesuch);
		}
	}

	@Override
	public void adaptFinSitDataOnFamSitChange(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation) {

		Familiensituation newFamiliensituation = familiensituationContainer.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();

		adaptSchwyzFinSitDataOnFamSitChange(gesuch, loadedFamiliensituation, newFamiliensituation);


		if (gesuch.isMutation()) {
			if (EbeguUtil.fromOneGSToTwoGS(familiensituationContainer, gesuchsperiodeBis) &&
				newFamiliensituation.getGemeinsameSteuererklaerung() == null) {
				newFamiliensituation.setGemeinsameSteuererklaerung(false);
			}

			Objects.requireNonNull(loadedFamiliensituation);

			if (gesuch.getFinSitTyp() == FinanzielleSituationTyp.LUZERN &&
				isScheidung(loadedFamiliensituation, newFamiliensituation)) {
				gesuch.setFinSitAenderungGueltigAbDatum(newFamiliensituation.getAenderungPer());
			}
		} else {
			Familiensituation familiensituationErstgesuch =
				familiensituationContainer.getFamiliensituationErstgesuch();
			if (familiensituationErstgesuch != null &&
				(!familiensituationErstgesuch.hasSecondGesuchsteller(gesuchsperiodeBis)
					&& !newFamiliensituation.hasSecondGesuchsteller(gesuchsperiodeBis))) {
				// if there is no GS2 the field gemeinsameSteuererklaerung must be set to null
				newFamiliensituation.setGemeinsameSteuererklaerung(null);
			}
		}
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


	private void resetFragenKinderabzugAndSetToUeberpruefen(Gesuch gesuch) {
		gesuch.getKindContainers()
			.forEach(kindContainer -> {
				if (kindContainer.getKindJA() != null) {
					kindContainer.getKindJA().setGemeinsamesGesuch(null);
					kindContainer.getKindJA().setInPruefung(true);
				}
			});
	}

	private boolean isGesuchBeendenBeiTauschGS2Active(Gesuch gesuch) {
		Einstellung einstellung = einstellungService.findEinstellung(
			EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode());

		return Boolean.TRUE.equals(einstellung.getValueAsBoolean());
	}

	/**
	 * Wenn die neue Familiensituation nur 1GS hat und der zweite GS schon existiert, wird dieser
	 * und seine Daten endgueltig geloescht. Dies gilt aber nur fuer ERSTGESUCH.
	 * Bei Mutation oder nach Freigabe kann der GS2 geloescht werden wenn er gar nicht mehr ins Gesuch
	 * beruecksichtig wird. D.H. alle Daten die die FamSit / FinSit Regeln betreffen muessen geprueft werden
	 */
	private boolean isNeededToRemoveGesuchsteller2(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		Familiensituation familiensituationErstgesuch
	) {
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		LocalDate gesuchsperiodeAb = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb();
		return gesuch.getGesuchsteller2() != null && ((!gesuch.isMutation()
			&& !newFamiliensituation.hasSecondGesuchsteller(gesuchsperiodeBis))
			|| (gesuch.isMutation() && isChanged1To2Reverted(gesuch, newFamiliensituation,
			familiensituationErstgesuch))
			|| (gesuch.isMutation() && (newFamiliensituation.getAenderungPer() != null
			&& newFamiliensituation.getAenderungPer().isBefore(gesuchsperiodeAb)) && (
			(gesuch.getRegelnGueltigAb() != null && gesuch.getRegelnGueltigAb().isBefore(gesuchsperiodeAb))
				|| (gesuch.getRegelnGueltigAb() == null && gesuch.getEingangsdatum() != null && gesuch.getEingangsdatum().isBefore(gesuchsperiodeAb)))));
	}

	private boolean isChanged1To2Reverted(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		Familiensituation familiensituationErstgesuch
	) {
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		return gesuch.getGesuchsteller2() != null && !familiensituationErstgesuch.hasSecondGesuchsteller(
			gesuchsperiodeBis)
			&& !newFamiliensituation.hasSecondGesuchsteller(gesuchsperiodeBis);
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
