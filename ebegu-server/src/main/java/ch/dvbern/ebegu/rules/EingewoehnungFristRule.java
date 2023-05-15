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

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

public class EingewoehnungFristRule extends AbstractAbschlussRule {

	private Locale locale;
	private Boolean eingewoehnungAktiviert;

	protected EingewoehnungFristRule(@Nonnull Locale locale, boolean isDebug, Boolean eingewoehnungAktiviert) {
		super(isDebug);
		this.locale = locale;
		this.eingewoehnungAktiviert = eingewoehnungAktiviert;
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

		VerfuegungZeitabschnitt eingewoehnung = createEingewoehnungAbschnitt(
				eingewohenungAbschnittHelper.zeitabschnittOhneAnspruch,
				eingewohenungAbschnittHelper.zeitabschnittMitAnspruch,
				gp);

		removeOverlappingGueltigkeiten(
				zeitabschnitte,
				eingewoehnung);
		zeitabschnitte.add(eingewoehnung);

		return zeitabschnitte
					   .stream()
					   //nach Entfernen der Überlappungen, kann es ZAs geben, die ungültig sind
					   .filter(zeitabschnitt -> zeitabschnitt.getGueltigkeit().isValid())
					   .sorted()
					   .collect(Collectors.toList());
	}

	private void removeOverlappingGueltigkeiten(
			List<VerfuegungZeitabschnitt> zeitabschnitte,
			VerfuegungZeitabschnitt eingewoehnung) {

		DateRange gueltigkeitEingewoehnung = eingewoehnung.getGueltigkeit();

		zeitabschnitte.forEach(verfuegungZeitabschnitt -> {
			//Es dürfen keine Zeitabschnitte mit der Gültigkeit der Eingewöhnung überlappen, wenn sie überlappen...
			if (verfuegungZeitabschnitt.getGueltigkeit().intersects(gueltigkeitEingewoehnung)) {
				//...und der überlappende Zeitabschnitt einen Anspruch hat, muss der Eingewöhnungszeitbaschnitt gekürzt werden
				if (hasBetreuungAndAnspruch(verfuegungZeitabschnitt)) {
					eingewoehnung.getGueltigkeit().setGueltigAb(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().plusDays(1));
				} else {
					//... wenn der Zeitabschnitt keinen Anspuch hat, muss dieser gekürzt werden
					verfuegungZeitabschnitt.getGueltigkeit().setGueltigBis(gueltigkeitEingewoehnung.getGueltigAb().minusDays(1));
				}
			}
		});
	}

	private VerfuegungZeitabschnitt createEingewoehnungAbschnitt(
		@Nonnull VerfuegungZeitabschnitt baseAbschnitt,
		@Nonnull VerfuegungZeitabschnitt abschnittMitAnspruch,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		VerfuegungZeitabschnitt eingewoehnung = new VerfuegungZeitabschnitt(baseAbschnitt);
		eingewoehnung.setAnspruchspensumProzentForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput()
			.getAnspruchspensumProzent());
		eingewoehnung.setErwerbspensumGS1ForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput()
			.getErwerbspensumGS1());
		eingewoehnung.setErwerbspensumGS2ForAsivAndGemeinde(abschnittMitAnspruch.getRelevantBgCalculationInput()
			.getErwerbspensumGS2());
		eingewoehnung.getRelevantBgCalculationInput().addBemerkung(MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG, locale);
		eingewoehnung.getBemerkungenDTOList().removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH);
		eingewoehnung.setGueltigkeit(getGultigkeitOfEingewohenungAbschnitt(abschnittMitAnspruch, gesuchsperiode));
		return eingewoehnung;
	}

	private DateRange getGultigkeitOfEingewohenungAbschnitt(
			VerfuegungZeitabschnitt abschnittMitAnspruch,
			Gesuchsperiode gesuchsperiode) {

		//grundsätzlich ist die Eingewöhnung gültig von 1 Monat vor Anspruch bis ein Tag vor Anspruch
		LocalDate eingewohenungGueltigAb = abschnittMitAnspruch.getGueltigkeit().getGueltigAb().minusMonths(1);
		LocalDate eingewoehnungGueltigBis = abschnittMitAnspruch.getGueltigkeit().getGueltigAb().minusDays(1);

		if (eingewohenungGueltigAb.isBefore(gesuchsperiode.getGueltigkeit().getGueltigAb())) {
			eingewohenungGueltigAb = gesuchsperiode.getGueltigkeit().getGueltigAb();
		}

		return new DateRange(eingewohenungGueltigAb, eingewoehnungGueltigBis);
	}

	private boolean hasBetreuungAndAnspruch(VerfuegungZeitabschnitt zeitabschnittToCheck) {
		return !MathUtil.isZero(zeitabschnittToCheck.getRelevantBgCalculationInput().getBgPensumProzent());
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



