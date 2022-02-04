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
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

public class GeschwisterbonusCalcRule extends AbstractCalcRule {

	private EinschulungTyp einstellungBgAusstellenBisStufe;

	protected GeschwisterbonusCalcRule(
		@Nonnull EinschulungTyp einstellungBgAusstellenBisStufe,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(RuleKey.GESCHWISTERBONUS, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
		this.einstellungBgAusstellenBisStufe = einstellungBgAusstellenBisStufe;
	}

	@Override
	void executeRule(
			@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		Betreuung betreuung = (Betreuung) platz;
		if (!kindCouldHaveBG(platz.getKind().getKindJA())) {
			return;
		}
		boolean hasBonusKind2 = getHasGeschwistersBonusKind2(betreuung);
		if (hasBonusKind2) {
			inputData.addBemerkung(MsgKey.GESCHWSTERNBONUS_KIND_2, getLocale());
		}
		boolean hasBonusKind3 = getHasGeschwistersBonusKind3(betreuung);
		if (hasBonusKind3) {
			inputData.addBemerkung(MsgKey.GESCHWSTERNBONUS_KIND_3, getLocale());
		}
	}

	private boolean kindCouldHaveBG(Kind kind) {
		if (kind.getEinschulungTyp() == null) {
			return false;
		}
		return kind.getEinschulungTyp().ordinal() <= this.einstellungBgAusstellenBisStufe.ordinal();
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
				.filter(this::kindCouldHaveBG)
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
		Einstellung geschwisternbonusAktiv = einstellungMap.get(EinstellungKey.GESCHWISTERNBONUS_AKTIVIERT);
		return geschwisternbonusAktiv.getValueAsBoolean();
	}
}
