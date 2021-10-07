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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

public abstract class EingewoehnungAbschnittRule extends AbstractEinwoehnungAbschnittRule {

	private final Integer maximalpensumFreiwilligenarbeit;

	protected EingewoehnungAbschnittRule(
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,  @Nonnull Integer maximalpensumFreiwilligenarbeit) {
		super(RuleKey.ERWERBSPENSUM, RuleType.GRUNDREGEL_DATA, ruleValidity, validityPeriod, locale);
		this.maximalpensumFreiwilligenarbeit = maximalpensumFreiwilligenarbeit;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	protected List<VerfuegungZeitabschnitt> getErwerbspensumAbschnittForGesuchsteller(
		@Nonnull Gesuch gesuch,
		@Nonnull GesuchstellerContainer gesuchsteller,
		@Nonnull AbstractPlatz platz, boolean gs2) {

		List<VerfuegungZeitabschnitt> ewpAbschnitte = new ArrayList<>();
		Set<ErwerbspensumContainer> ewpContainers = gesuchsteller.getErwerbspensenContainersNotEmpty();

		LocalDate startPensum = findSmallerStartPensumDate(ewpContainers);

		if (startPensum != null) {
			ewpContainers.stream()
				.map(ErwerbspensumContainer::getErwerbspensumJA)
				.filter(Objects::nonNull)
				.filter(erwerbspensumJA -> erwerbspensumJA.getGueltigkeit().getGueltigAb().isEqual(startPensum))
				.map(erwerbspensumJA -> toVerfuegungZeitabschnitt(gesuch, erwerbspensumJA, platz, gs2))
				.filter(Objects::nonNull)
				.forEach(zeitabschnitt -> {
					ewpAbschnitte.add(zeitabschnitt);
				});
		}

		return ewpAbschnitte;
	}

	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(
		Gesuch gesuch,
		Erwerbspensum erwerbspensum,
		AbstractPlatz platz,
		boolean gs2) {
		if (getValidTaetigkeiten().contains(erwerbspensum.getTaetigkeit())) {
			final DateRange gueltigkeit = new DateRange(erwerbspensum.getGueltigkeit());
			if (!platz.isAngebotSchulamt()) {
				Betreuung betreuung = (Betreuung) platz;
				if (betreuung.isEingewoehnung()) {
					if (gesuch.getGesuchsperiode()
						.getGueltigkeit()
						.getGueltigAb()
						.isBefore(gueltigkeit.getGueltigAb())) {
						if (gueltigkeit.getGueltigAb()
							.minusMonths(1)
							.isAfter(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb())) {
							gueltigkeit.setGueltigAb(gueltigkeit.getGueltigAb().minusMonths(1));
						} else {
							gueltigkeit.setGueltigAb(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
						}
						gueltigkeit.setGueltigBis(erwerbspensum.getGueltigkeit().getGueltigAb().minusDays(1));

						// Wir merken uns hier den eingegebenen Wert, auch wenn dieser (mit Zuschlag) über 100% liegt
						Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
						Familiensituation familiensituation = gesuch.extractFamiliensituation();
						if (gs2
							&& gesuch.isMutation()
							&& familiensituationErstgesuch != null
							&& familiensituation != null) {

							getGueltigkeitFromFamiliensituation(
								gueltigkeit,
								familiensituationErstgesuch,
								familiensituation);

							return createZeitabschnittEingewoehnung(gueltigkeit, erwerbspensum, false);
						}
						if (gs2 && !gesuch.isMutation()) {
							return createZeitabschnittEingewoehnung(gueltigkeit, erwerbspensum, false);
						}
						if (!gs2) {
							return createZeitabschnittEingewoehnung(gueltigkeit, erwerbspensum, true);
						}
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Dort muss man die Freiwilligenarbeit berucksichtigen nur wenn es einen Zuschlag bei der Gemeinde gibt
	 * Es gilt fuer ASIV und Gemeinde, bei ASIV wenn der startpensum ist einen Freiwilligenarbeit
	 * wird dann nix gemacht und der Gemeinde Rules wird der Einwoehnung berucksichtigen
	 * @return
	 */
	@Nonnull
	protected List<Taetigkeit> getValidTaetigkeiten() {
		List<Taetigkeit> taetigkeiten = Taetigkeit.getTaetigkeitenForAsiv();
		if (maximalpensumFreiwilligenarbeit > 0) {
			taetigkeiten.add(Taetigkeit.FREIWILLIGENARBEIT);
		}
		return  taetigkeiten;
	}

	@Nullable
	protected VerfuegungZeitabschnitt createZeitabschnittEingewoehnung(
		@Nonnull DateRange gueltigkeit,
		@Nonnull Erwerbspensum erwerbspensum, boolean isGesuchsteller1) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		if (erwerbspensum.getTaetigkeit().equals(Taetigkeit.FREIWILLIGENARBEIT)) {
			BGCalculationInput inputGemeinde = zeitabschnitt.getBgCalculationInputGemeinde();
			Integer limitedPensum = erwerbspensum.getPensum();
			if (limitedPensum > maximalpensumFreiwilligenarbeit) {
				limitedPensum = maximalpensumFreiwilligenarbeit;
			}
			if (limitedPensum > 0) {
				if (isGesuchsteller1) {
					inputGemeinde.setErwerbspensumGS1(limitedPensum);
				} else {
					inputGemeinde.setErwerbspensumGS2(limitedPensum);
				}
				inputGemeinde.getTaetigkeiten().add(erwerbspensum.getTaetigkeit());
				inputGemeinde.addBemerkung(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG, getLocale());
				zeitabschnitt.setHasGemeindeSpezifischeBerechnung(true);
			}
		} else {
			zeitabschnitt.addTaetigkeitForAsivAndGemeinde(erwerbspensum.getTaetigkeit());
			if (isGesuchsteller1) {
				zeitabschnitt.setErwerbspensumGS1ForAsivAndGemeinde(erwerbspensum.getPensum());
			} else {
				zeitabschnitt.setErwerbspensumGS2ForAsivAndGemeinde(erwerbspensum.getPensum());
			}
			zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG, getLocale());

		}
		return zeitabschnitt;
	}


	private LocalDate findSmallerStartPensumDate(Set<ErwerbspensumContainer> ewpContainers) {
		LocalDate startPensum = null;
		for (ErwerbspensumContainer ewpContainer : ewpContainers) {
			Erwerbspensum erwerbspensum = ewpContainer.getErwerbspensumJA();
			if (erwerbspensum != null && getValidTaetigkeiten().contains(erwerbspensum.getTaetigkeit())) {
				if (startPensum == null || startPensum.isAfter(erwerbspensum.getGueltigkeit().getGueltigAb())) {
					startPensum = erwerbspensum.getGueltigkeit().getGueltigAb();
				}
			}
		}
		return startPensum;
	}
}
