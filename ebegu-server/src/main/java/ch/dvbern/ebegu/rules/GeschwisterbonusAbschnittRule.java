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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

public class GeschwisterbonusAbschnittRule extends AbstractAbschnittRule {

	private final EinschulungTyp einstellungBgAusstellenBisStufe;

	protected GeschwisterbonusAbschnittRule(
		@Nonnull EinschulungTyp einstellungBgAusstellenBisStufe,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(RuleKey.GESCHWISTERBONUS, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
		this.einstellungBgAusstellenBisStufe = einstellungBgAusstellenBisStufe;
	}

	@Nonnull
	@Override
	List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts = new ArrayList<>();

		if (!kindCouldHaveBG(platz.getKind().getKindJA())) {
			return verfuegungZeitabschnitts;
		}

		Betreuung betreuung = (Betreuung) platz;
		List<KindContainer> geschwisterOrderedByAge = getGeschwisterOrderdByAge(betreuung);

		//wenn das kind das älteste Kind ist, gibt es sicher kein geschwisternbonus
		var kindNumber = getKindNumber(geschwisterOrderedByAge, betreuung.getKind());
		if (kindNumber == 0) {
			return verfuegungZeitabschnitts;
		}
		// kind muss gefunden werden
		if (kindNumber < 0) {
			throw new EbeguRuntimeException("createVerfuegungsZeitabschnitte", "Kind nicht gefunden");
		}

		betreuung.getBetreuungspensumContainers().forEach(betreuungspensumContainer ->
			createEinZeitabschnittProMonatBisMaximalEndePeriode(
					betreuungspensumContainer.getBetreuungspensumJA().getGueltigkeit(),
					betreuung.extractGesuchsperiode(),
					verfuegungZeitabschnitts));

		verfuegungZeitabschnitts.forEach(zeitabschnitt ->
		 setGeschwisernbonusInZeitabschnitt(zeitabschnitt, betreuung.getKind(), geschwisterOrderedByAge));

		return verfuegungZeitabschnitts;
	}

	private boolean kindCouldHaveBG(Kind kind) {
		if (kind.getEinschulungTyp() == null) {
			return false;
		}

		if (kind.getEinschulungTyp().getOrdinalitaet() <= this.einstellungBgAusstellenBisStufe.getOrdinalitaet()) {
			return true;
		}

		return Boolean.TRUE.equals(kind.getKeinPlatzInSchulhort());
	}

	private List<KindContainer> getGeschwisterOrderdByAge(Betreuung betreuung) {
		return betreuung.extractGesuch().getKindContainers().stream()
			.filter(kindContainer -> kindCouldHaveBG(kindContainer.getKindJA()))
			.sorted(getKindGeburtstagAndTimestampComparator())
			.collect(Collectors.toUnmodifiableList());
	}

	private void setGeschwisernbonusInZeitabschnitt(
		VerfuegungZeitabschnitt zeitabschnitt,
		KindContainer kindToCheckGeschwisternBonus,
		List<KindContainer> geschwisterOrderedByAge) {

		List<KindContainer> kinderWithBetreuungInZeitraum =
			getKinderOrderedByAgeThatHaveBetreuungInSameZeitraum(geschwisterOrderedByAge, zeitabschnitt.getGueltigkeit());

		if (hasKindGeschwisterBonus2(kindToCheckGeschwisternBonus, kinderWithBetreuungInZeitraum)) {
			zeitabschnitt.setGeschwisternBonusKind2ForAsivAndGemeinde(true);
			zeitabschnitt.getRelevantBgCalculationInput().addBemerkung(MsgKey.GESCHWSTERNBONUS_KIND_2, getLocale());
		} else if (hasKindGeschwisterBonus3(kindToCheckGeschwisternBonus, kinderWithBetreuungInZeitraum)) {
			zeitabschnitt.setGeschwisternBonusKind3ForAsivAndGemeinde(true);
			zeitabschnitt.getRelevantBgCalculationInput().addBemerkung(MsgKey.GESCHWSTERNBONUS_KIND_3, getLocale());
		}
	}

	private List<KindContainer> getKinderOrderedByAgeThatHaveBetreuungInSameZeitraum(
		List<KindContainer> kindContainer,
		DateRange gueltigkeit) {

		return kindContainer.stream()
			.filter(container -> atLeastOneBetreuungspensumContainerOverlap(container.getBetreuungen(), gueltigkeit))
			.sorted(getKindGeburtstagAndTimestampComparator())
			.collect(Collectors.toList());
	}

	private boolean atLeastOneBetreuungspensumContainerOverlap(Set<Betreuung> betreuungen, DateRange gueltigkeit) {
		return betreuungen.stream()
				.flatMap(betreuung -> betreuung.getBetreuungspensumContainers().stream())
				.anyMatch(betreuungspensumContainer ->
					gueltigkeit.intersects(betreuungspensumContainer.getBetreuungspensumJA().getGueltigkeit())
				);
	}


	private boolean hasKindGeschwisterBonus2(KindContainer kindToCheck, List<KindContainer> orderedKindByAge) {
		if(!orderedKindByAge.contains(kindToCheck)) {
			return false;
		}

		return getKindNumber(orderedKindByAge, kindToCheck) == 1;
	}

	private boolean hasKindGeschwisterBonus3(KindContainer kindToCheck, List<KindContainer> orderedKindByAge) {
		if(!orderedKindByAge.contains(kindToCheck)) {
			return false;
		}

		return getKindNumber(orderedKindByAge, kindToCheck) >= 2;
	}

	private Comparator<KindContainer> getKindGeburtstagAndTimestampComparator() {
		return (o1, o2) -> {
			if (o1.getKindJA().getGeburtsdatum().isEqual(o2.getKindJA().getGeburtsdatum())) {
				Objects.requireNonNull(o1.getKindJA().getTimestampErstellt());
				Objects.requireNonNull(o2.getKindJA().getTimestampErstellt());

				return o1.getKindJA().getTimestampErstellt().compareTo(o2.getKindJA().getTimestampErstellt());
			}

			return o1.getKindJA().getGeburtsdatum().compareTo(o2.getKindJA().getGeburtsdatum());
		};
	}

	/**
	 * returns the number of the child (0 for oldest, 1 for second oldest, etc...)
	 */
	private int getKindNumber(List<KindContainer> kindOrderedByAge, KindContainer kindToGetNumber) {
		return kindOrderedByAge.indexOf(kindToGetNumber);
	}

	private void createEinZeitabschnittProMonatBisMaximalEndePeriode(
		DateRange gueltigkeit,
		Gesuchsperiode gesuchsperiode,
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts) {
		LocalDate guleitgAb = gueltigkeit.getGueltigAb();
		LocalDate gueltigBis = gueltigkeit.getGueltigBis().isAfter(gesuchsperiode.getGueltigkeit().getGueltigBis()) ?
			gesuchsperiode.getGueltigkeit().getGueltigBis() :
			gueltigkeit.getGueltigBis();

		createEineVerfuegungZeitabschnittProMonat(guleitgAb, gueltigBis, verfuegungZeitabschnitts);
	}

	private void createEineVerfuegungZeitabschnittProMonat(
		LocalDate gueltigAb,
		LocalDate gueltigBis,
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts
	) {
		if (gueltigAb.getMonth() == gueltigBis.getMonth()) {
			verfuegungZeitabschnitts.add(createZeitabschnittWithinValidityPeriodOfRule(new DateRange(gueltigAb, gueltigBis)));
			return;
		}
		LocalDate startOfNextMonth = gueltigAb.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
		verfuegungZeitabschnitts.add(createZeitabschnittWithinValidityPeriodOfRule(new DateRange(gueltigAb, startOfNextMonth.minusDays(1))));
		createEineVerfuegungZeitabschnittProMonat(startOfNextMonth, gueltigBis, verfuegungZeitabschnitts);
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
}
