/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.cdipersistence.Persistence;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Service fuer familiensituation
 */
@Stateless
@Local(FamiliensituationService.class)
public class FamiliensituationServiceBean extends AbstractBaseService implements FamiliensituationService {

	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	@Inject
	private GesuchstellerService gesuchstellerService;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private SozialhilfeZeitraumService sozialhilfeZeitraumService;

	@Inject
	private EinstellungService einstellungService;

	@Override
	public FamiliensituationContainer saveFamiliensituation(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation //OLD Familiensituation
	) {
		Objects.requireNonNull(familiensituationContainer);
		Objects.requireNonNull(gesuch);

		// Falls noch nicht vorhanden, werden die GemeinsameSteuererklaerung fuer FS und EV auf false gesetzt
		Familiensituation newFamiliensituation = familiensituationContainer.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		if (gesuch.isMutation()) {
			if (EbeguUtil.fromOneGSToTwoGS(familiensituationContainer, gesuchsperiodeBis) &&
				newFamiliensituation.getGemeinsameSteuererklaerung() == null) {
				newFamiliensituation.setGemeinsameSteuererklaerung(false);
			}

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
		final FamiliensituationContainer mergedFamiliensituationContainer = persistence.merge
			(familiensituationContainer);
		gesuch.setFamiliensituationContainer(mergedFamiliensituationContainer);

		// get old FamSit to compare with
		Familiensituation oldFamiliensituation;
		if (mergedFamiliensituationContainer != null
			&& mergedFamiliensituationContainer.getFamiliensituationErstgesuch() != null) {
			// Bei Mutation immer die Situation vom Erstgesuch als  Basis fuer Wizardstepanpassung
			oldFamiliensituation = mergedFamiliensituationContainer.getFamiliensituationErstgesuch();
		} else {
			oldFamiliensituation = loadedFamiliensituation;
		}

		//Alle Daten des GS2 loeschen wenn man von 2GS auf 1GS wechselt und GS2 bereits erstellt wurde
		Objects.requireNonNull(mergedFamiliensituationContainer);
		if (gesuch.getGesuchsteller2() != null
			&& isNeededToRemoveGesuchsteller2(gesuch, mergedFamiliensituationContainer.extractFamiliensituation(),
			oldFamiliensituation)
		) {
			gesuchstellerService.removeGesuchsteller(gesuch.getGesuchsteller2());
			gesuch.setGesuchsteller2(null);
			newFamiliensituation.setGemeinsameSteuererklaerung(false);
		}

		if (gesuch.getFinSitTyp() == FinanzielleSituationTyp.LUZERN) {
			changeFamSitLuzern(gesuch, mergedFamiliensituationContainer, oldFamiliensituation);
		}

		if (gesuch.getFinSitTyp() == FinanzielleSituationTyp.APPENZELL) {
			changeFamSitAR(gesuch, mergedFamiliensituationContainer, oldFamiliensituation);
		}

		if (isGesuchBeendenBeiTauschGS2Active(gesuch)
			&& isKonkubinatOhneKindWithOneErwerbspensumRequired(newFamiliensituation, gesuch)
			&& gesuch.getGesuchsteller2() != null
			&& !gesuch.getGesuchsteller2().getErwerbspensenContainers().isEmpty()) {
			gesuch.getGesuchsteller2().getErwerbspensenContainers().clear();
		}

		//bei änderung der Familiensituation müssen die Fragen zum Kinderabzug im FKJV resetet werden
		if (gesuch.getFinSitTyp() == FinanzielleSituationTyp.BERN_FKJV &&
			oldFamiliensituation != null &&
			oldFamiliensituation.getFamilienstatus() != newFamiliensituation.getFamilienstatus() &&
			!Objects.equals(newFamiliensituation.getPartnerIdentischMitVorgesuch(), Boolean.FALSE)) {
			resetFragenKinderabzugAndSetToUeberpruefen(gesuch);
		}

		wizardStepService.updateSteps(gesuch.getId(), oldFamiliensituation, newFamiliensituation, WizardStepName
			.FAMILIENSITUATION);
		return mergedFamiliensituationContainer;
	}

	private boolean isGesuchBeendenBeiTauschGS2Active(Gesuch gesuch) {
		Einstellung einstellung = einstellungService.findEinstellung(EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode());

		return Boolean.TRUE.equals(einstellung.getValueAsBoolean());
	}

	private boolean isKonkubinatOhneKindWithOneErwerbspensumRequired(Familiensituation familiensituation, Gesuch gesuch) {
		if (familiensituation.getFamilienstatus() != EnumFamilienstatus.KONKUBINAT_KEIN_KIND) {
			return false;
		}
		return familiensituation.isKonkubinatReachingMinDauerIn(gesuch.getGesuchsperiode())
			&& Objects.equals(familiensituation.getGeteilteObhut(), Boolean.FALSE)
			&& familiensituation.getUnterhaltsvereinbarung() == UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
	}


	private boolean isScheidung(
		@NotNull Familiensituation oldFamiliensituation,
		@NotNull Familiensituation newFamiliensituation) {
		if (oldFamiliensituation.getFamilienstatus() != EnumFamilienstatus.VERHEIRATET) {
			return false;
		}

		return newFamiliensituation.getFamilienstatus() == EnumFamilienstatus.ALLEINERZIEHEND;
	}

	private void changeFamSitLuzern(
		@Nonnull Gesuch gesuch,
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
	private void changeFamSitAR(
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

	private void resetFragenKinderabzugAndSetToUeberpruefen(Gesuch gesuch) {
		gesuch.getKindContainers()
			.forEach(kindContainer -> {
				if (kindContainer.getKindJA() != null) {
					kindContainer.getKindJA().setGemeinsamesGesuch(null);
					kindContainer.getKindJA().setInPruefung(true);
				}
			});
	}

	@Nonnull
	@Override
	public Optional<FamiliensituationContainer> findFamiliensituation(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		FamiliensituationContainer a = persistence.find(FamiliensituationContainer.class, key);
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	public Collection<FamiliensituationContainer> getAllFamiliensituatione() {
		return new ArrayList<>(criteriaQueryHelper.getAll(FamiliensituationContainer.class));
	}

	@Override
	public void removeFamiliensituation(@Nonnull FamiliensituationContainer familiensituation) {
		Objects.requireNonNull(familiensituation);
		FamiliensituationContainer familiensituationToRemove =
			findFamiliensituation(familiensituation.getId()).orElseThrow(() -> new EbeguEntityNotFoundException(
				"removeFall", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, familiensituation));
		for (SozialhilfeZeitraumContainer sozialhilfeZeitraumCtn :
			familiensituationToRemove.getSozialhilfeZeitraumContainers()) {
			sozialhilfeZeitraumService.removeSozialhilfeZeitraum(sozialhilfeZeitraumCtn.getId());
		}
		persistence.remove(familiensituationToRemove);
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
