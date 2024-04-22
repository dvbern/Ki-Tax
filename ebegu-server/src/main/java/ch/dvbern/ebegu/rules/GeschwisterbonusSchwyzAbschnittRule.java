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
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DateUtil;

public class GeschwisterbonusSchwyzAbschnittRule extends AbstractAbschnittRule {

	protected GeschwisterbonusSchwyzAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(RuleKey.GESCHWISTERBONUS, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Nonnull
	@Override
	List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		var gesuch = platz.extractGesuch();
		final DateRange gpGueltigkeit = platz.extractGesuchsperiode().getGueltigkeit();
		var createdAbschnitte = gesuch.getKindContainers().stream()
			.filter(kindContainer -> {
				var geburtsdatum = kindContainer.getKindJA().getGeburtsdatum();
				return DateUtil.isSameDateOrAfter(geburtsdatum, gpGueltigkeit.getGueltigAb().minusYears(18))
					&& DateUtil.isSameDateOrBefore(geburtsdatum, gpGueltigkeit.getGueltigBis());
			})
			.filter(kindContainer -> !kindContainer.isSame(platz.getKind()))
			.filter(kindContainer -> !kindContainer.getBetreuungen().isEmpty())
			.map(kindContainer -> kindContainer.getKindJA().getGeburtsdatum())
			.flatMap(geburtsdatum -> {
				if (isBornDuringGP(geburtsdatum, gpGueltigkeit)) {
					return Stream.of(
						createZeitabschnittWithoutGeschwister(
							new DateRange(
								gpGueltigkeit.getGueltigAb(),
								mapDateIntoGueltigkeit(geburtsdatum, gpGueltigkeit))),
						createZeitabschnittWithGeschwister(new DateRange(
							mapDateIntoGueltigkeit(geburtsdatum, gpGueltigkeit).plusDays(1),
							gpGueltigkeit.getGueltigBis()))
					);
				}
				if (reaches18DuringGP(geburtsdatum, gpGueltigkeit)) {
					return Stream.of(
						createZeitabschnittWithGeschwister(
							new DateRange(
								gpGueltigkeit.getGueltigAb(),
								mapDateIntoGueltigkeit(geburtsdatum, gpGueltigkeit))),
						createZeitabschnittWithoutGeschwister(new DateRange(
							mapDateIntoGueltigkeit(geburtsdatum, gpGueltigkeit).plusDays(1),
							gpGueltigkeit.getGueltigBis()))
					);
				}

				return Stream.of(createZeitabschnittWithGeschwister(gpGueltigkeit));
			}).collect(Collectors.toList());

		return mergeZeitabschnitte(createdAbschnitte);
	}


	private LocalDate mapDateIntoGueltigkeit(LocalDate date, DateRange gueltigkeit) {
		return date.getMonthValue() >= 8 ?
			LocalDate.of(gueltigkeit.getGueltigAb().getYear(), date.getMonth(), date.getDayOfMonth()) :
			LocalDate.of(gueltigkeit.getGueltigBis().getYear(), date.getMonth(), date.getDayOfMonth());
	}

	private boolean isBornDuringGP(LocalDate geburtsdatum, DateRange gpGueltigkeit) {
		return gpGueltigkeit.contains(geburtsdatum);
	}

	private boolean reaches18DuringGP(LocalDate geburtsdatum, DateRange gpGueltigkeit) {
		return gpGueltigkeit.contains(geburtsdatum.plusYears(18));
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitabschnittWithGeschwister(DateRange gpGueltigkeit) {
		final VerfuegungZeitabschnitt zeitabschnitt =
			createZeitabschnittWithinValidityPeriodOfRule(gpGueltigkeit);
		zeitabschnitt.setAnzahlGeschwister(1);
		return zeitabschnitt;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitabschnittWithoutGeschwister(DateRange gpGueltigkeit) {
		final VerfuegungZeitabschnitt zeitabschnitt =
			createZeitabschnittWithinValidityPeriodOfRule(gpGueltigkeit);
		zeitabschnitt.setAnzahlGeschwister(0);
		return zeitabschnitt;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung geschwisterbonus = einstellungMap.get(EinstellungKey.GESCHWISTERNBONUS_TYP);
		return GeschwisterbonusTyp.getEnumValue(geschwisterbonus) == GeschwisterbonusTyp.LUZERN;
	}
}
