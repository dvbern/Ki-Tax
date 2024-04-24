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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DateUtil;

public class GeschwisterbonusSchwyzAbschnittRule extends AbstractAbschnittRule {

	protected GeschwisterbonusSchwyzAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(RuleKey.GESCHWISTERBONUS, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung geschwisterbonus = einstellungMap.get(EinstellungKey.GESCHWISTERNBONUS_TYP);
		return GeschwisterbonusTyp.getEnumValue(geschwisterbonus) == GeschwisterbonusTyp.LUZERN;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Nonnull
	@Override
	List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		var gesuch = platz.extractGesuch();
		final DateRange gpGueltigkeit = platz.extractGesuchsperiode().getGueltigkeit();
		var createdAbschnitte = gesuch.getKindContainers().stream()
			.filter(kindContainer -> !kindContainer.isSame(platz.getKind()))
			.filter(kindContainer -> !kindContainer.getBetreuungen().isEmpty())
			.filter(kindContainer -> contributesToGeschwisterbonus(kindContainer, gpGueltigkeit))
			.flatMap(this::createAbschnittGeburtstagTuplesForBetreuungen)
			.map(this::limitGueltigkeitWithGeburtstag)
			.map(this::setAnzahlGeschwister)
			.map(VerfuegungZeitabschnittGeburtsdatumTuple::getZeitabschnitt)
			.filter(verfuegungZeitabschnitt -> verfuegungZeitabschnitt.getGueltigkeit().isValid())
			.collect(Collectors.toList());

		final List<VerfuegungZeitabschnitt> mergedAbschnitte = mergeZeitabschnitte(createdAbschnitte);
		mergedAbschnitte.forEach(abschnitt -> abschnitt.getBgCalculationInputAsiv().addBemerkung(MsgKey.GESCHWISTERBONUS_SCHWYZ, getLocale(), abschnitt.getBgCalculationInputAsiv().getAnzahlGeschwister()));
		return mergedAbschnitte;
	}

	private static boolean contributesToGeschwisterbonus(KindContainer kindContainer, DateRange gpGueltigkeit) {
		var geburtsdatum = kindContainer.getKindJA().getGeburtsdatum();
		return DateUtil.isSameDateOrAfter(geburtsdatum, gpGueltigkeit.getGueltigAb().minusYears(18))
			&& DateUtil.isSameDateOrBefore(geburtsdatum, gpGueltigkeit.getGueltigBis());
	}

	@Nonnull
	private Stream<VerfuegungZeitabschnittGeburtsdatumTuple> createAbschnittGeburtstagTuplesForBetreuungen(KindContainer kindContainer) {
		return kindContainer.getBetreuungen()
			.stream()
			.flatMap(betreuung -> betreuung.getBetreuungspensumContainers().stream())
			.map(betreuungspensumContainer -> new VerfuegungZeitabschnittGeburtsdatumTuple(
				createZeitabschnittWithinValidityPeriodOfRule(betreuungspensumContainer.getGueltigkeit()),
				kindContainer.getKindJA().getGeburtsdatum()));
	}

	@Nonnull
	private VerfuegungZeitabschnittGeburtsdatumTuple limitGueltigkeitWithGeburtstag(
		VerfuegungZeitabschnittGeburtsdatumTuple zeitabschnittGeburtsdatumTuple) {
		var geburtsdatum = zeitabschnittGeburtsdatumTuple.getGeburtsdatum();
		var gueltigkeit = zeitabschnittGeburtsdatumTuple.getZeitabschnitt().getGueltigkeit();

		if (isBornDuringGueltigkeit(geburtsdatum, gueltigkeit)) {
			gueltigkeit.setGueltigAb(mapDateIntoGueltigkeit(geburtsdatum, gueltigkeit));
		}
		if (reaches18During(geburtsdatum, gueltigkeit)) {
			gueltigkeit.setGueltigBis(mapDateIntoGueltigkeit(geburtsdatum, gueltigkeit));
		}
		if (reaches18Before(geburtsdatum, gueltigkeit)) {
			gueltigkeit.setGueltigBis(gueltigkeit.getGueltigAb().minusDays(1));
		}

		return zeitabschnittGeburtsdatumTuple;
	}

	private boolean isBornDuringGueltigkeit(LocalDate geburtsdatum, DateRange gueltigkeit) {
		return gueltigkeit.contains(geburtsdatum);
	}

	private boolean reaches18During(LocalDate geburtsdatum, DateRange gueltigkeit) {
		return gueltigkeit.contains(geburtsdatum.plusYears(18));
	}

	private boolean reaches18Before(LocalDate geburtsdatum, DateRange gueltigkeit) {
		return gueltigkeit.getGueltigAb().isAfter(geburtsdatum.plusYears(18));
	}

	private LocalDate mapDateIntoGueltigkeit(LocalDate date, DateRange gueltigkeit) {
		return date.getMonthValue() >= 8 ?
			LocalDate.of(gueltigkeit.getGueltigAb().getYear(), date.getMonth(), date.getDayOfMonth()) :
			LocalDate.of(gueltigkeit.getGueltigBis().getYear(), date.getMonth(), date.getDayOfMonth());
	}

	@Nonnull
	private VerfuegungZeitabschnittGeburtsdatumTuple setAnzahlGeschwister(
		VerfuegungZeitabschnittGeburtsdatumTuple zeitabschnittGeburtsdatumTuple) {
		zeitabschnittGeburtsdatumTuple.getZeitabschnitt().setAnzahlGeschwister(1);
		return zeitabschnittGeburtsdatumTuple;
	}

	private static class VerfuegungZeitabschnittGeburtsdatumTuple {
		private final VerfuegungZeitabschnitt zeitabschnitt;
		private final LocalDate geburtsdatum;

		private VerfuegungZeitabschnittGeburtsdatumTuple(VerfuegungZeitabschnitt zeitabschnitt, LocalDate geburtsdatum) {
			this.zeitabschnitt = zeitabschnitt;
			this.geburtsdatum = geburtsdatum;
		}

		public VerfuegungZeitabschnitt getZeitabschnitt() {
			return zeitabschnitt;
		}

		public LocalDate getGeburtsdatum() {
			return geburtsdatum;
		}
	}
}
