/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Setzt das massgebende Einkommen in die benoetigten Zeitabschnitte.
 * Das Massgebende Einkommen wird fruehestens auf den Beginn des Folgemonats nach dem Ereignis angepasst.
 */
public class EinkommenAbschnittRule extends AbstractAbschnittRule {

	public EinkommenAbschnittRule(DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.EINKOMMEN, RuleType.GRUNDREGEL_DATA, validityPeriod, locale);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull Betreuung betreuung) {
		List<VerfuegungZeitabschnitt> einkommensAbschnitte = new ArrayList<>();
		// Nur ausführen wenn Finanzdaten gesetzt
		// Der {@link FinanzielleSituationRechner} wurde verwendet um das jeweils geltende  Einkommen auszurechnen. Das heisst im DTO ist schon
		// jeweils das zu verwendende Einkommen gesetzt
		FinanzDatenDTO finanzDatenDTOAlleine = betreuung.extractGesuch().getFinanzDatenDTO_alleine();
		FinanzDatenDTO finanzDatenDTOZuZweit = betreuung.extractGesuch().getFinanzDatenDTO_zuZweit();

		if (finanzDatenDTOAlleine != null && finanzDatenDTOZuZweit != null) {
			VerfuegungZeitabschnitt lastAbschnitt;

			// Abschnitt Finanzielle Situation (Massgebendes Einkommen fuer die Gesuchsperiode)
			VerfuegungZeitabschnitt abschnittFinanzielleSituation = new VerfuegungZeitabschnitt(betreuung.extractGesuchsperiode().getGueltigkeit());
			einkommensAbschnitte.add(abschnittFinanzielleSituation);
			lastAbschnitt = abschnittFinanzielleSituation;

			// Einkommensverschlechterung 1: In mind. 1 Kombination eingegeben
			if (finanzDatenDTOAlleine.isEkv1Erfasst() || finanzDatenDTOZuZweit.isEkv1Erfasst()) {
				int jahr = betreuung.extractGesuchsperiode().getBasisJahrPlus1();
				DateRange rangeEKV1 = new DateRange(LocalDate.of(jahr, Month.JANUARY, 1), LocalDate.of(jahr, Month.DECEMBER, 31));
				VerfuegungZeitabschnitt abschnittEinkommensverschlechterung1 = new VerfuegungZeitabschnitt(rangeEKV1);

				// EKV1 fuer alleine erfasst
				abschnittEinkommensverschlechterung1.setEkv1Alleine(finanzDatenDTOAlleine.isEkv1Erfasst());
				// EKV1 fuer zu Zweit erfasst
				abschnittEinkommensverschlechterung1.setEkv1ZuZweit(finanzDatenDTOZuZweit.isEkv1Erfasst());

				einkommensAbschnitte.add(abschnittEinkommensverschlechterung1);
				// Den vorherigen Zeitabschnitt erst nach der EKV 1 beginnen
				lastAbschnitt.getGueltigkeit().startsDayAfter(abschnittEinkommensverschlechterung1.getGueltigkeit());
				lastAbschnitt = abschnittEinkommensverschlechterung1;
			}

			// Einkommensverschlechterung 2: In mind. 1 Kombination akzeptiert
			if (finanzDatenDTOAlleine.isEkv2Erfasst() || finanzDatenDTOZuZweit.isEkv2Erfasst()) {
				int jahr = betreuung.extractGesuchsperiode().getBasisJahrPlus2();
				DateRange rangeEKV2 = new DateRange(LocalDate.of(jahr, Month.JANUARY, 1), LocalDate.of(jahr, Month.DECEMBER, 31));
				VerfuegungZeitabschnitt abschnittEinkommensverschlechterung2 = new VerfuegungZeitabschnitt(rangeEKV2);

				// EKV2 fuer alleine erfasst
				abschnittEinkommensverschlechterung2.setEkv2Alleine(finanzDatenDTOAlleine.isEkv2Erfasst());
				// EKV2 fuer zu Zweit erfasst
				abschnittEinkommensverschlechterung2.setEkv2ZuZweit(finanzDatenDTOZuZweit.isEkv2Erfasst());

				einkommensAbschnitte.add(abschnittEinkommensverschlechterung2);
				// Den vorherigen Zeitabschnitt beenden
				lastAbschnitt.getGueltigkeit().endOnDayBefore(abschnittEinkommensverschlechterung2.getGueltigkeit());
			}
		}
		return einkommensAbschnitte;
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}

}
