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

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Bis 12 Monate gilt der BabyTarif (1.5), danach der "Normaltarif" (1.0) bis zum Schuleintritt, nach Schuleintritt 0.75
 */
@SuppressWarnings("MethodParameterNamingConvention")
public class KindTarifAbschnittRule extends AbstractAbschnittRule {

	public KindTarifAbschnittRule(DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.KIND_TARIF, RuleType.GRUNDREGEL_DATA, validityPeriod, locale);
	}

	private VerfuegungZeitabschnitt createZeitabschnitt(DateRange gueltigkeit, boolean baby, boolean eingeschult) {
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(gueltigkeit);
		verfuegungZeitabschnitt.setBabyTarif(baby);
		verfuegungZeitabschnitt.setEingeschult(eingeschult);
		return verfuegungZeitabschnitt;
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> zeitabschnittList = new ArrayList<>();

		// Relevant sind der Geburtstag des Kindes sowie der Einschulungstyp
		Kind kind = betreuung.getKind().getKindJA();
		final LocalDate geburtsdatum = kind.getGeburtsdatum();
		LocalDate stichtagBabyTarifEnde = geburtsdatum.plusMonths(12).with(TemporalAdjusters.lastDayOfMonth());
		boolean eingeschult = kind.getEinschulungTyp() != null && kind.getEinschulungTyp().isEingeschult();
		DateRange gesuchsperiode = betreuung.extractGesuchsperiode().getGueltigkeit();

		if (gesuchsperiode.contains(stichtagBabyTarifEnde)) {
			DateRange abschnittBaby = new DateRange(gesuchsperiode.getGueltigAb(), stichtagBabyTarifEnde);
			zeitabschnittList.add(createZeitabschnitt(abschnittBaby, true, eingeschult));

			DateRange abschnittKind = new DateRange(stichtagBabyTarifEnde.plusDays(1), gesuchsperiode.getGueltigBis());
			zeitabschnittList.add(createZeitabschnitt(abschnittKind, false, eingeschult));
		} else {
			boolean baby = stichtagBabyTarifEnde.isAfter(gesuchsperiode.getGueltigBis());
			zeitabschnittList.add(createZeitabschnitt(gesuchsperiode, baby, eingeschult));
		}

		return zeitabschnittList;
	}
}
