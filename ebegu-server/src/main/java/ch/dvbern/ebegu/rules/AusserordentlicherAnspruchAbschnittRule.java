/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.PensumAusserordentlicherAnspruch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Regel für einen ausserordentlichen Anspruch. Sucht das PensumAusserordentlicherAnspruch falls vorhanden und wenn
 * ja wird ein entsprechender Zeitabschnitt generiert
 */
public class AusserordentlicherAnspruchAbschnittRule extends AbstractAbschnittRule {

	public AusserordentlicherAnspruchAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.AUSSERORDENTLICHER_ANSPRUCH, RuleType.GRUNDREGEL_DATA, validityPeriod, locale);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> betreuungspensumAbschnitte = new ArrayList<>();
		PensumAusserordentlicherAnspruch anspruch = betreuung.getKind().getKindJA().getPensumAusserordentlicherAnspruch();
		if (anspruch != null) {
			betreuungspensumAbschnitte.add(toVerfuegungZeitabschnitt(anspruch));
		}
		return betreuungspensumAbschnitte;
	}

	@Nonnull
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(@Nonnull PensumAusserordentlicherAnspruch anspruch) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(anspruch.getGueltigkeit());
		zeitabschnitt.setAusserordentlicherAnspruch(anspruch.getPensum());
		return zeitabschnitt;
	}
}
