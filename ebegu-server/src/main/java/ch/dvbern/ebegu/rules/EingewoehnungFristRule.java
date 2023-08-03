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

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EingewoehnungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.*;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

public class EingewoehnungFristRule extends AbstractAbschlussRule {

	private final Locale locale;
	private final Boolean eingewoehnungAktiviert;

	protected EingewoehnungFristRule(@Nonnull Locale locale, boolean isDebug, EingewoehnungTyp eingewoehnungTyp) {
		super(isDebug);
		this.locale = locale;
		this.eingewoehnungAktiviert = eingewoehnungTyp.eingewoehnungAktiviert();
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {

		Betreuung betreuung = (Betreuung) platz;

		if (betreuung.isEingewoehnung() && eingewoehnungAktiviert) {
			return handleEingewoehnung(zeitabschnitte, platz.extractGesuchsperiode());
		}

		return zeitabschnitte;
	}

	private List<VerfuegungZeitabschnitt> handleEingewoehnung(
			@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte,
			Gesuchsperiode gp) {

		EingewohenungAbschnittHelper eingewohenungAbschnittHelper = new EingewohenungAbschnittHelper(zeitabschnitte);

		if (!eingewohenungAbschnittHelper.hasZeitabschnittForEingewoehnung()
					|| eingewohenungAbschnittHelper.isZuSpaetEingereicht()) {
			return zeitabschnitte;
		}

		VerfuegungZeitabschnitt eingewoehnung = createEingewoehnungAbschnitt(eingewohenungAbschnittHelper, gp);
		zeitabschnitte.add(eingewoehnung);
		Collections.sort(zeitabschnitte);

		final List<VerfuegungZeitabschnitt> mergedZeitabschnitte = mergeZeitabschnitte(zeitabschnitte);

		for (VerfuegungZeitabschnitt merged : mergedZeitabschnitte) {
			if (merged.getGueltigkeit().intersects(eingewoehnung.getGueltigkeit())){
				final int eingewoehnungAnspruchspensumProzent =
					eingewoehnung.getRelevantBgCalculationInput().getAnspruchspensumProzent();

				int originalAnspruch = merged.getRelevantBgCalculationInput().getAnspruchspensumProzent() - eingewoehnungAnspruchspensumProzent;

				merged.setAnspruchspensumProzentForAsivAndGemeinde(Math.max(originalAnspruch, eingewoehnungAnspruchspensumProzent));
			}
		}

	 	return mergedZeitabschnitte;
	}

	private VerfuegungZeitabschnitt createEingewoehnungAbschnitt(
		@Nonnull EingewohenungAbschnittHelper eingewoehenungAbschnittHelper,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		VerfuegungZeitabschnitt abschnittMitAnspruch = eingewoehenungAbschnittHelper.zeitabschnittMitAnspruch;
		VerfuegungZeitabschnitt abschnittOhneAnspruch = eingewoehenungAbschnittHelper.zeitabschnittOhneAnspruch;
		VerfuegungZeitabschnitt eingewoehnung =
				new VerfuegungZeitabschnitt(getGultigkeitOfEingewohenungAbschnitt(eingewoehenungAbschnittHelper, gesuchsperiode));
		eingewoehnung.setEinkommensjahrForAsivAndGemeinde(
				abschnittOhneAnspruch.getRelevantBgCalculationInput().getEinkommensjahr());
		eingewoehnung.setAnspruchspensumProzentForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput()
			.getAnspruchspensumProzent());
		eingewoehnung.setErwerbspensumGS1ForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput()
			.getErwerbspensumGS1());
		eingewoehnung.setErwerbspensumGS2ForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput()
			.getErwerbspensumGS2());
		eingewoehnung.getRelevantBgCalculationInput().addBemerkung(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG, locale);
		return eingewoehnung;
	}
	private DateRange getGultigkeitOfEingewohenungAbschnitt(
			EingewohenungAbschnittHelper eingewohenungAbschnittHelper,
			Gesuchsperiode gesuchsperiode) {

		VerfuegungZeitabschnitt abschnittMitAnspruch = eingewohenungAbschnittHelper.zeitabschnittMitAnspruch;
		//grundsätzlich ist die Eingewöhnung gültig von 1 Monat vor Anspruch bis ein Tag vor Anspruch
		LocalDate eingewohenungGueltigAb = abschnittMitAnspruch.getGueltigkeit().getGueltigAb().minusMonths(1);
		LocalDate eingewoehnungGueltigBis = abschnittMitAnspruch.getGueltigkeit().getGueltigAb().minusDays(1);

		if (eingewohenungGueltigAb.isBefore(gesuchsperiode.getGueltigkeit().getGueltigAb())) {
			eingewohenungGueltigAb = gesuchsperiode.getGueltigkeit().getGueltigAb();
		}

		if (eingewohenungAbschnittHelper.anspruchGueltigAb != null &&
			eingewohenungGueltigAb.isBefore(eingewohenungAbschnittHelper.anspruchGueltigAb)) {
			eingewohenungGueltigAb = eingewohenungAbschnittHelper.anspruchGueltigAb;
		}

		return new DateRange(eingewohenungGueltigAb, eingewoehnungGueltigBis);
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	static class EingewohenungAbschnittHelper {

		//der Zeitabschnitt mit Anspruch, welcher verlängert werden muss
		VerfuegungZeitabschnitt zeitabschnittMitAnspruch;

		// der Zeitabschnitt, noch keinen Anspruch hat, aber Anspruch durch Eingewöhnung haben soll
		// Die Gültigkeit des Zeitbaschnittes entspricht nicht der effektven Gültigkeit der Eingewöhnung
		VerfuegungZeitabschnitt zeitabschnittOhneAnspruch;

		LocalDate anspruchGueltigAb;

		EingewohenungAbschnittHelper(List<VerfuegungZeitabschnitt> zeitabschnitte) {
			findRelevantZeitabschnitteForEingewohenung(zeitabschnitte);
		}


		private void findRelevantZeitabschnitteForEingewohenung(List<VerfuegungZeitabschnitt> zeitabschnitte) {
			LinkedList<VerfuegungZeitabschnitt> zeitabschnitteOrderedByGueltigkeit = new LinkedList<>(zeitabschnitte);
			ListIterator<VerfuegungZeitabschnitt> iterator = zeitabschnitteOrderedByGueltigkeit.listIterator(0);

			//wir müssen das erste Paar aufeinander folgende Zeitabschnitte finden, bei welchem, der erste ZA keinen Anspruch
			//aber eine Betruung und der zweite ZA Anspruch und Betreuung hat
			while (iterator.hasNext()) {
				VerfuegungZeitabschnitt zeitabschnittToCheck = iterator.next();

				if (zeitabschnittToCheck.getRelevantBgCalculationInput().isZuSpaetEingereicht()) {
					this.anspruchGueltigAb = zeitabschnittToCheck.getGueltigkeit().getGueltigBis().plusDays(1);
				}

				if (hasBetreuungButNoAnspruch(zeitabschnittToCheck)) {
					//zeitabschnitt ohne Anspruch gefunden...
					while (iterator.hasNext()) {
						//nun solange zum nächsten Zeitabschnitt springen, bis einer mit Anspruch und Betreuung gefunden wurde
						if (hasBetreuungAndAnspruch(iterator.next())) {
							zeitabschnittMitAnspruch = iterator.previous();
							zeitabschnittOhneAnspruch = iterator.previous();
							return;
						}
					}
				}
			}
		}
		private boolean hasBetreuungButNoAnspruch(VerfuegungZeitabschnitt zeitabschnittToCheck) {
			if (zeitabschnittToCheck.getRelevantBgCalculationInput().getAnspruchspensumProzent() > 0) {
				return false;
			}

			return !MathUtil.isZero(zeitabschnittToCheck.getRelevantBgCalculationInput().getBetreuungspensumProzent());
		}

		private boolean hasBetreuungAndAnspruch(VerfuegungZeitabschnitt zeitabschnittToCheck) {
			return !MathUtil.isZero(zeitabschnittToCheck.getRelevantBgCalculationInput().getBgPensumProzent());
		}

		boolean hasZeitabschnittForEingewoehnung() {
			return zeitabschnittOhneAnspruch != null;
		}

		boolean isZuSpaetEingereicht() {
			return zeitabschnittOhneAnspruch.getRelevantBgCalculationInput().isZuSpaetEingereicht();
		}
	}
}



