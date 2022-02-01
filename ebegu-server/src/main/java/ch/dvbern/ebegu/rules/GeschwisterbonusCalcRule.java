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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractPersonEntity;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

public class GeschwisterbonusCalcRule extends AbstractCalcRule {

	protected GeschwisterbonusCalcRule(
			@Nonnull DateRange validityPeriod,
			@Nonnull Locale locale) {
		super(RuleKey.GESCHWISTERBONUS, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	void executeRule(
			@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		Betreuung betreuung = (Betreuung) platz;
		if (kindHasTooHighEinschulungstyp(platz)) {
			return;
		}
		inputData.setGeschwisternBonusKind2(getHasGeschwistersBonusKind2(betreuung));
		inputData.setGeschwisternBonusKind3(getHasGeschwistersBonusKind3(betreuung));
	}

	private boolean kindHasTooHighEinschulungstyp(AbstractPlatz platz) {
		if (platz.getKind().getKindJA().getEinschulungTyp() == null) {
			return false;
		}
		return platz.getKind().getKindJA().getEinschulungTyp().isKindergarten() || platz.getKind().getKindJA().getEinschulungTyp().isEingeschult();
	}

	private boolean getHasGeschwistersBonusKind2(Betreuung betreuung) {
		List<Kind> kinderList = getRelevantKinderSortedByAgeFromBetreuung(betreuung);
		return kinderList.indexOf(betreuung.getKind().getKindJA()) == 1;
	}

	private boolean getHasGeschwistersBonusKind3(Betreuung betreuung) {
		List<Kind> kinderList = getRelevantKinderSortedByAgeFromBetreuung(betreuung);
		return kinderList.indexOf(betreuung.getKind().getKindJA()) >= 2;
	}

	private List<Kind> getRelevantKinderSortedByAgeFromBetreuung(Betreuung betreuung) {
		return betreuung.extractGesuch()
				.getKindContainers()
				.stream()
				.filter(kindContainer -> !kindContainer.getBetreuungen().isEmpty())
				.map(KindContainer::getKindJA)
				.sorted(Comparator.comparing(AbstractPersonEntity::getGeburtsdatum)
						.thenComparing(AbstractEntity::getTimestampErstellt))
				.collect(Collectors.toList());
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(BetreuungsangebotTyp.KITA, BetreuungsangebotTyp.TAGESFAMILIEN);
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		Einstellung kitaPlusZuschlagAktiv = einstellungMap.get(EinstellungKey.KITAPLUS_ZUSCHLAG_AKTIVIERT);
		return kitaPlusZuschlagAktiv.getValueAsBoolean();
	}
}
