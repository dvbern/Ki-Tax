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

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.RuleUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Umsetzung der ASIV Revision: Finanzielle Situation bei Mutation der Familiensituation anpassen
 * <p>
 * Gem. neuer ASIV Verordnung muss bei einem Wechsel von einem auf zwei Gesuchsteller oder umgekehrt die
 * finanzielle Situation ab dem Folgemonat angepasst werden.
 * </p>
 */
public class ZivilstandsaenderungAbschnittRule extends AbstractAbschnittRule {

	private final Integer paramMinDauerKonkubinat;

	public ZivilstandsaenderungAbschnittRule(
		DateRange validityPeriod,
		Integer paramMinDauerKonkubinat,
		@Nonnull Locale locale
	) {
		super(RuleKey.ZIVILSTANDSAENDERUNG, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
		this.paramMinDauerKonkubinat = paramMinDauerKonkubinat;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {

		Gesuch gesuch = platz.extractGesuch();
		final List<VerfuegungZeitabschnitt> zivilstandsaenderungAbschnitte = new ArrayList<>();

		// Ueberpruefen, ob die Gesuchsteller-Kardinalität geändert hat. Nur dann muss evt. anders berechnet werden!
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);
		Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();

		LocalDate gesuchsperiodeBis = platz.extractGesuch().getGesuchsperiode().getGueltigkeit().getGueltigBis();

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
			if (!gesuch.extractMandant().getMandantIdentifier().equals(MandantIdentifier.SCHWYZ)) {
				abschnittNachMutation.getBgCalculationInputAsiv().addBemerkung(msgKey, getLocale());
			}
			zivilstandsaenderungAbschnitte.add(abschnittNachMutation);

		} else if (familiensituation.getFamilienstatus() == EnumFamilienstatus.KONKUBINAT_KEIN_KIND
			&& familiensituation.getStartKonkubinat() != null
			&& gesuch.getGesuchsperiode().getGueltigkeit().contains(familiensituation.getStartKonkubinat().plusYears(paramMinDauerKonkubinat))
		) {
			final LocalDate startKonkubinatPlusXJahre = RuleUtil
				.getStichtagForEreignis(familiensituation.getStartKonkubinat().plusYears(paramMinDauerKonkubinat));

			zivilstandsaenderungAbschnitte.add(
				createVerfuegungZeitabschnittForZivilstand(
					familiensituation,
					gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
					startKonkubinatPlusXJahre.minusDays(1)
				)
			);

			final VerfuegungZeitabschnitt abschnittKonkubinat2GS = createVerfuegungZeitabschnittForZivilstand(
				familiensituation,
				startKonkubinatPlusXJahre,
				gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
			);
			abschnittKonkubinat2GS.getBgCalculationInputAsiv().addBemerkung(MsgKey.FAMILIENSITUATION_KONKUBINAT_MSG, getLocale());
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
		VerfuegungZeitabschnitt abschnitt = createZeitabschnittWithinValidityPeriodOfRule(new DateRange(dateAb, dateBis));
		abschnitt.setHasSecondGesuchstellerForFinanzielleSituationForAsivAndGemeinde(
			familiensituation.hasSecondGesuchsteller(
				// it must be checked at the end of the zeitabschnitt
				abschnitt.getGueltigkeit().getGueltigBis()
			)
					// beim Spezialfall in Appenzell gibt es nur einen Antragsteller aber zwei finanzielle Situationen.
					|| familiensituation.isSpezialFallAR()
		);
		return abschnitt;
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
