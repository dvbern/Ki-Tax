/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractPersonEntity;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

public class GeschwisterbonusAbschnittRule extends AbstractAbschnittRule {

	private EinschulungTyp einstellungBgAusstellenBisStufe;

	protected GeschwisterbonusAbschnittRule(
		@Nonnull EinschulungTyp einstellungBgAusstellenBisStufe,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(RuleKey.GESCHWISTERBONUS, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
		this.einstellungBgAusstellenBisStufe = einstellungBgAusstellenBisStufe;
	}

	@Nonnull
	@Override
	List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts = new ArrayList<>();
		if (!kindCouldHaveBG(platz.getKind().getKindJA())) {
			return verfuegungZeitabschnitts;
		}
		Betreuung betreuung = (Betreuung) platz;
		betreuung.getBetreuungspensumContainers().forEach(betreuungspensumContainer ->
				createEineVerfuegungZeitabschnittProMonat(
					betreuungspensumContainer.getBetreuungspensumJA().getGueltigkeit().getGueltigAb(),
					betreuungspensumContainer.getBetreuungspensumJA().getGueltigkeit().getGueltigBis(),
					verfuegungZeitabschnitts)
		);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt: verfuegungZeitabschnitts) {
			boolean hasBonusKind2 = getHasGeschwistersBonusKind2(betreuung, verfuegungZeitabschnitt.getGueltigkeit());
			if (hasBonusKind2) {
				verfuegungZeitabschnitt.setGeschwisternBonusKind2ForAsivAndGemeinde(hasBonusKind2);
				verfuegungZeitabschnitt.getRelevantBgCalculationInput().addBemerkung(MsgKey.GESCHWSTERNBONUS_KIND_2, getLocale());
			}
			boolean hasBonusKind3 = getHasGeschwistersBonusKind3(betreuung, verfuegungZeitabschnitt.getGueltigkeit());
			if (hasBonusKind3) {
				verfuegungZeitabschnitt.setGeschwisternBonusKind3ForAsivAndGemeinde(hasBonusKind3);
				verfuegungZeitabschnitt.getRelevantBgCalculationInput().addBemerkung(MsgKey.GESCHWSTERNBONUS_KIND_3, getLocale());
			}
		}
		return verfuegungZeitabschnitts;

	}

	private boolean kindCouldHaveBG(Kind kind) {
		if (kind.getEinschulungTyp() == null) {
			return false;
		}
		return kind.getEinschulungTyp().getOrdinalitaet() <= this.einstellungBgAusstellenBisStufe.getOrdinalitaet();
	}

	private boolean getHasGeschwistersBonusKind2(Betreuung betreuung, DateRange gueltigkeit) {
		List<Kind> kinderList = getRelevantKinderSortedByAgeFromBetreuung(betreuung, gueltigkeit);
		return kinderList.indexOf(betreuung.getKind().getKindJA()) == 1;
	}

	private boolean getHasGeschwistersBonusKind3(Betreuung betreuung, DateRange gueltigkeit) {
		List<Kind> kinderList = getRelevantKinderSortedByAgeFromBetreuung(betreuung, gueltigkeit);
		return kinderList.indexOf(betreuung.getKind().getKindJA()) >= 2;
	}

	private List<Kind> getRelevantKinderSortedByAgeFromBetreuung(Betreuung betreuung, DateRange gueltigkeit) {
		return betreuung.extractGesuch()
			.getKindContainers()
			.stream()
			.filter(kindContainer -> !kindContainer.getBetreuungen().isEmpty()
				&& kindContainer.getBetreuungen()
				.stream()
				.anyMatch(kindBetreuung -> atLeastOneBetreuungspensumContainerOverlap(gueltigkeit, kindBetreuung))
			)
			.map(KindContainer::getKindJA)
			.filter(this::kindCouldHaveBG)
			.sorted(
				Comparator
					.comparing(AbstractPersonEntity::getGeburtsdatum)
					.thenComparing(AbstractEntity::getTimestampErstellt))
			.collect(Collectors.toList());
	}

	private static boolean atLeastOneBetreuungspensumContainerOverlap(DateRange gueltigkeit, Betreuung kindBetreuung) {
		return kindBetreuung.getBetreuungspensumContainers()
			.stream()
			.anyMatch(
				betreuungspensumContainer -> gueltigkeit.getOverlap(
					betreuungspensumContainer.getBetreuungspensumJA().getGueltigkeit()
				).isPresent()
			);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(BetreuungsangebotTyp.KITA, BetreuungsangebotTyp.TAGESFAMILIEN);
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung geschwisternbonusAktiv = einstellungMap.get(EinstellungKey.GESCHWISTERNBONUS_AKTIVIERT);
		return geschwisternbonusAktiv.getValueAsBoolean();
	}

	private List<VerfuegungZeitabschnitt> createEineVerfuegungZeitabschnittProMonat(
		LocalDate gueltigAb,
		LocalDate gueltigBis,
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts
	) {
		if (gueltigAb.getMonth().equals(gueltigBis.getMonth())) {
			verfuegungZeitabschnitts.add(new VerfuegungZeitabschnitt(new DateRange(gueltigAb, gueltigBis)));
			return verfuegungZeitabschnitts;
		}
		LocalDate startOfNextMonth = gueltigAb.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
		verfuegungZeitabschnitts.add(new VerfuegungZeitabschnitt(new DateRange(gueltigAb, startOfNextMonth.minusDays(1))));
		return createEineVerfuegungZeitabschnittProMonat(startOfNextMonth, gueltigBis, verfuegungZeitabschnitts);
	}
}
