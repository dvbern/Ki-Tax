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

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bis 12 Monate gilt der BabyTarif (1.5), danach der "Normaltarif" (1.0) bis zum Schuleintritt, nach Schuleintritt 0.75
 */
@SuppressWarnings("MethodParameterNamingConvention")
public class KindTarifAbschnittRule extends AbstractAbschnittRule {

	private final long dauerBabyTarif;

	public KindTarifAbschnittRule(DateRange validityPeriod, @Nonnull Locale locale, long dauerBabyTarif) {
		super(RuleKey.KIND_TARIF, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
		this.dauerBabyTarif = dauerBabyTarif;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}


	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> zeitabschnittList = new ArrayList<>();

		// Relevant sind der Geburtstag des Kindes sowie der Einschulungstyp
		Kind kind = platz.getKind().getKindJA();
		final LocalDate geburtsdatum = kind.getGeburtsdatum();
		LocalDate stichtagBabyTarifEnde = geburtsdatum.plusMonths(dauerBabyTarif).with(TemporalAdjusters.lastDayOfMonth());
		DateRange gesuchsperiode = platz.extractGesuchsperiode().getGueltigkeit();

		EinschulungTyp einschulungTyp = kind.getEinschulungTyp() != null ? kind.getEinschulungTyp() : EinschulungTyp.VORSCHULALTER;
		if (gesuchsperiode.contains(stichtagBabyTarifEnde)) {
			DateRange abschnittBaby = new DateRange(gesuchsperiode.getGueltigAb(), stichtagBabyTarifEnde);
			zeitabschnittList.add(createZeitabschnitt(abschnittBaby, true, einschulungTyp));

			DateRange abschnittKind = new DateRange(stichtagBabyTarifEnde.plusDays(1), gesuchsperiode.getGueltigBis());
			VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnitt(abschnittKind, false, einschulungTyp);
			zeitabschnitt.getRelevantBgCalculationInput().addBemerkung(MsgKey.KLEINKIND_TARIF, getLocale(), dauerBabyTarif);
			zeitabschnittList.add(zeitabschnitt);
		} else {
			boolean baby = stichtagBabyTarifEnde.isAfter(gesuchsperiode.getGueltigBis());
			zeitabschnittList.add(createZeitabschnitt(gesuchsperiode, baby, einschulungTyp));
		}

		return zeitabschnittList;
	}

	private VerfuegungZeitabschnitt createZeitabschnitt(
		@Nonnull DateRange gueltigkeit,
		boolean baby,
		@Nonnull EinschulungTyp einschulungTyp
	) {
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		verfuegungZeitabschnitt.setBabyTarifForAsivAndGemeinde(baby);
		verfuegungZeitabschnitt.setEinschulungTypForAsivAndGemeinde(einschulungTyp);
		return verfuegungZeitabschnitt;
	}
}
