/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Setzt die Information ueber Sozialhilfe in die benoetigten Zeitabschnitte.
 * Das Massgebende Einkommen wird fruehestens auf den Beginn des Folgemonats nach dem Ereignis angepasst.
 */
public class SozialhilfeAbschnittRule extends AbstractAbschnittRule {

	public SozialhilfeAbschnittRule(DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.SOZIALHILFE, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		FamiliensituationContainer familiensituation = platz.extractGesuch().getFamiliensituationContainer();
		if (familiensituation == null || familiensituation.getFamiliensituationJA() == null) {
			return new ArrayList<>();
		}

		if (Boolean.TRUE.equals(familiensituation.getFamiliensituationJA().getSozialhilfeBezueger()) &&
			familiensituation.getSozialhilfeZeitraumContainers().isEmpty()) {
			return Stream.of(createZeitabschnitt(platz.extractGesuchsperiode().getGueltigkeit())).collect(Collectors.toList());
		}

		return familiensituation.getSozialhilfeZeitraumContainers().stream()
			.map(SozialhilfeZeitraumContainer::getSozialhilfeZeitraumJA)
			.filter(Objects::nonNull)
			.map(AbstractDateRangedEntity::getGueltigkeit)
			.map(this::createZeitabschnitt)
			.collect(Collectors.toList());
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitabschnitt(@Nonnull DateRange gueltigkeit) {
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		verfuegungZeitabschnitt.setGueltigkeit(gueltigkeit);
		verfuegungZeitabschnitt.setSozialhilfeempfaengerForAsivAndGemeinde(true);
		return verfuegungZeitabschnitt;
	}
}
