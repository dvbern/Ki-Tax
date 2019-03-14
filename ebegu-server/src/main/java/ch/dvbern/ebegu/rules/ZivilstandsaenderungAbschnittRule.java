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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.RuleUtil;

/**
 * Umsetzung der ASIV Revision: Finanzielle Situation bei Mutation der Familiensituation anpassen
 * <p>
 * Gem. neuer ASIV Verordnung muss bei einem Wechsel von einem auf zwei Gesuchsteller oder umgekehrt die
 * finanzielle Situation ab dem Folgemonat angepasst werden.
 * </p>
 */
public class ZivilstandsaenderungAbschnittRule extends AbstractAbschnittRule {

	public ZivilstandsaenderungAbschnittRule(
		DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(RuleKey.ZIVILSTANDSAENDERUNG, RuleType.GRUNDREGEL_DATA, validityPeriod, locale);
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull Betreuung betreuung) {

		Gesuch gesuch = betreuung.extractGesuch();
		final List<VerfuegungZeitabschnitt> zivilstandsaenderungAbschnitte = new ArrayList<>();

		// Ueberpruefen, ob die Gesuchsteller-Kardinalität geändert hat. Nur dann muss evt. anders berechnet werden!
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);
		Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();

		LocalDate gesuchsperiodeBis = betreuung.extractGesuch().getGesuchsperiode().getGueltigkeit().getGueltigBis();

		RuleKey ruleKey = RuleKey.ZIVILSTANDSAENDERUNG;

		if (familiensituation.getAenderungPer() != null && familiensituationErstgesuch != null &&
			familiensituation.hasSecondGesuchsteller(gesuchsperiodeBis) != familiensituationErstgesuch.hasSecondGesuchsteller(gesuchsperiodeBis)) {

			// Die Zivilstandsaenderung gilt ab anfang nächstem Monat, die Bemerkung muss aber "per Heirat/Trennung" erfolgen
			final LocalDate stichtag = getStichtagForEreignis(familiensituation.getAenderungPer());
			// Bemerkung erstellen
			MsgKey msgKey = null;
			if (familiensituation.hasSecondGesuchsteller(gesuchsperiodeBis)) {
				// Heirat
				msgKey = MsgKey.FAMILIENSITUATION_HEIRAT_MSG;
			} else {
				// Trennung
				msgKey = MsgKey.FAMILIENSITUATION_TRENNUNG_MSG;
			}

			zivilstandsaenderungAbschnitte.add(
				createVerfuegungZeitabschnittForZivilstand(
					familiensituationErstgesuch,
					gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
					stichtag.minusDays(1)
				)
			);

			VerfuegungZeitabschnitt abschnittNachMutation = createVerfuegungZeitabschnittForZivilstand(
				familiensituation,
				stichtag,
				gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
			);
			abschnittNachMutation.addBemerkung(ruleKey, msgKey, getLocale());
			zivilstandsaenderungAbschnitte.add(abschnittNachMutation);

		} else if (familiensituation.getFamilienstatus() == EnumFamilienstatus.KONKUBINAT_KEIN_KIND
			&& familiensituation.getStartKonkubinat() != null
			&& gesuch.getGesuchsperiode().getGueltigkeit().contains(familiensituation.getStartKonkubinat().plusYears(5))
		) {
			final LocalDate startKonkubinatPlusFive = RuleUtil
				.getStichtagForEreignis(familiensituation.getStartKonkubinat().plusYears(5));

			zivilstandsaenderungAbschnitte.add(
				createVerfuegungZeitabschnittForZivilstand(
					familiensituation,
					gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
					startKonkubinatPlusFive.minusDays(1)
				)
			);

			final VerfuegungZeitabschnitt abschnittKonkubinat2GS = createVerfuegungZeitabschnittForZivilstand(
				familiensituation,
				startKonkubinatPlusFive,
				gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
			);
			abschnittKonkubinat2GS.addBemerkung(ruleKey, MsgKey.FAMILIENSITUATION_KONKUBINAT_MSG, getLocale());
			zivilstandsaenderungAbschnitte.add(abschnittKonkubinat2GS);

		} else {
			zivilstandsaenderungAbschnitte.add(
				createVerfuegungZeitabschnittForZivilstand(
					familiensituation,
					gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
					gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
				)
			);
		}
		return zivilstandsaenderungAbschnitte;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createVerfuegungZeitabschnittForZivilstand(
		@Nonnull Familiensituation familiensituation,
		@Nonnull LocalDate dateAb,
		@Nonnull LocalDate dateBis
	) {
		VerfuegungZeitabschnitt abschnitt = new VerfuegungZeitabschnitt(new DateRange(dateAb, dateBis));
		abschnitt.setHasSecondGesuchstellerForFinanzielleSituation(
			familiensituation.hasSecondGesuchsteller(
				// it must be checked at the end of the zeitabschnitt
				abschnitt.getGueltigkeit().getGueltigBis()
			)
		);
		return abschnitt;
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
