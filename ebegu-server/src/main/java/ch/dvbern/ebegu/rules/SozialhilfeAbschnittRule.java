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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraum;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Setzt die Information ueber Sozialhilfe in die benoetigten Zeitabschnitte.
 * Das Massgebende Einkommen wird fruehestens auf den Beginn des Folgemonats nach dem Ereignis angepasst.
 */
public class SozialhilfeAbschnittRule extends AbstractAbschnittRule {

	public SozialhilfeAbschnittRule(DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.SOZIALHILFE, RuleType.GRUNDREGEL_DATA, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> einkommensAbschnitte = new ArrayList<>();
		FamiliensituationContainer familiensituation = platz.extractGesuch().getFamiliensituationContainer();
		if (familiensituation != null) {
			Set<SozialhilfeZeitraumContainer> sozialhilfeZeitraumContainers = familiensituation.getSozialhilfeZeitraumContainers();
			for (SozialhilfeZeitraumContainer sozialhilfeZeitraumContainer : sozialhilfeZeitraumContainers) {
				SozialhilfeZeitraum sozialhilfeZeitraum = sozialhilfeZeitraumContainer.getSozialhilfeZeitraumJA();
				if (sozialhilfeZeitraum != null) {
					einkommensAbschnitte.add(createZeitabschnitt(sozialhilfeZeitraum.getGueltigkeit()));
				}
			}
		}
		return einkommensAbschnitte;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createZeitabschnitt(@Nonnull DateRange gueltigkeit) {
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(gueltigkeit);
		verfuegungZeitabschnitt.setSozialhilfeempfaengerForAsivAndGemeinde(true);
		return verfuegungZeitabschnitt;
	}
}
