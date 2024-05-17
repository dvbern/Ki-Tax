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

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.util.EbeguUtil;

import static ch.dvbern.ebegu.services.util.ErwerbspensumHelper.isKonkubinatOhneKindAndGS2ErwerbspensumOmittable;

public class SharedFamSitChangeDefaultHandler implements FamSitChangeHandler {

	private final GesuchstellerService gesuchstellerService;
	private final EinstellungService einstellungService;

	public SharedFamSitChangeDefaultHandler(
		GesuchstellerService gesuchstellerService,
		EinstellungService einstellungService) {
		this.gesuchstellerService = gesuchstellerService;
		this.einstellungService = einstellungService;
	}
	protected void handleFamSitChange(
		Gesuch gesuch,
		FamiliensituationContainer mergedFamiliensituationContainer,
		Familiensituation oldFamiliensituation) {
		// noop
	}

	protected void removeGS2DataOnChangeFrom2To1GS(
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

	protected void handlePossibleGS2Tausch(Gesuch gesuch, Familiensituation newFamiliensituation) {
		if (isGesuchBeendenBeiTauschGS2Active(gesuch)
			&& isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(newFamiliensituation, gesuch.getGesuchsperiode())
			&& gesuch.getGesuchsteller2() != null
			&& !gesuch.getGesuchsteller2().getErwerbspensenContainers().isEmpty()) {
			gesuch.getGesuchsteller2().getErwerbspensenContainers().clear();
		}
	}

	protected void handlePossibleKinderabzugFragenReset(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		@Nullable Familiensituation oldFamiliensituation) {
		if (needKinderabzugResetBern(gesuch, oldFamiliensituation, newFamiliensituation)
			|| needsKinderabzugResetSchwyz(newFamiliensituation, oldFamiliensituation)) {
			resetFragenKinderabzugAndSetToUeberpruefen(gesuch);
		}
	}

	private boolean needKinderabzugResetBern(Gesuch gesuch, @Nullable Familiensituation oldFamiliensituation, Familiensituation newFamiliensituation) {
		return gesuch.getFinSitTyp() == FinanzielleSituationTyp.BERN_FKJV &&
			oldFamiliensituation != null &&
			oldFamiliensituation.getFamilienstatus() != newFamiliensituation.getFamilienstatus() &&
			!Objects.equals(newFamiliensituation.getPartnerIdentischMitVorgesuch(), Boolean.FALSE);
	}

	private static boolean needsKinderabzugResetSchwyz(Familiensituation newFamiliensituation, @Nullable Familiensituation oldFamiliensituation) {
		return newFamiliensituation.getFamilienstatus() == EnumFamilienstatus.SCHWYZ
			&& oldFamiliensituation != null
			&& (oldFamiliensituation.getFamilienstatus() != newFamiliensituation.getFamilienstatus()
			|| oldFamiliensituation.getGesuchstellerKardinalitaet() != newFamiliensituation.getGesuchstellerKardinalitaet());
	}

	@Override
	public void adaptFinSitDataOnFamSitChange(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation) {
		Familiensituation newFamiliensituation = familiensituationContainer.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();

		if (gesuch.isMutation()) {
			adaptFinSitDataInMutation(gesuch, familiensituationContainer, loadedFamiliensituation, newFamiliensituation, gesuchsperiodeBis);
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

	@Override
	public void handleFamSitChangeAfterSave(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		FamiliensituationContainer mergedFamiliensituationContainer,
		Familiensituation oldFamiliensituation) {
		removeGS2DataOnChangeFrom2To1GS(gesuch, newFamiliensituation, mergedFamiliensituationContainer, oldFamiliensituation);
		handleFamSitChange(gesuch, mergedFamiliensituationContainer, oldFamiliensituation);
		handlePossibleGS2Tausch(gesuch, newFamiliensituation);
		handlePossibleKinderabzugFragenReset(gesuch, newFamiliensituation, oldFamiliensituation);
	}

	/**
	 * @param gesuch is required in overriding methods
	 * @param loadedFamiliensituation is required in overriding methods
	 */
	protected void adaptFinSitDataInMutation(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation,
		Familiensituation newFamiliensituation,
		LocalDate gesuchsperiodeBis) {
		handleOnGsToTwoGSFinSitDataChange(familiensituationContainer, newFamiliensituation, gesuchsperiodeBis);
	}

	protected static void handleOnGsToTwoGSFinSitDataChange(
		FamiliensituationContainer familiensituationContainer,
		Familiensituation newFamiliensituation,
		LocalDate gesuchsperiodeBis) {
		if (EbeguUtil.fromOneGSToTwoGS(familiensituationContainer, gesuchsperiodeBis) &&
			newFamiliensituation.getGemeinsameSteuererklaerung() == null) {
			newFamiliensituation.setGemeinsameSteuererklaerung(false);
		}
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

}
