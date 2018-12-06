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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sonderregel das Ergenis der aktuellen Berechnung mit der Vorhergehenden merged.
 * <p>
 * Anspruchsberechnungsregeln für Mutationen
 * <p>
 * Entscheidend ist, ob die Meldung des Arbeitspensum frühzeitig gemeldet wird:
 * Eine Änderung des Arbeitspensums ist rechtzeitig, falls die Änderung im Vormonat gemeldet wird.
 * <p>
 * Rechtzeitige Meldung:In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst.
 * <p>
 * Verspätete Meldung: Wird die Änderung des Arbeitspensums im Monat des Ereignis oder noch später gemeldet, erfolgt eine ERHÖHUNG des Anspruchs erst auf den Folgemonat
 * <p>
 * Im Falle einer Herabsetzung des Arbeitspensums, wird der Anspruch zusammen mit dem Ereigniseintritt angepasst
 * <p>
 * Dieselbe Regeln gilt für sämtliche Berechnungen des Anspruchs, d.h. auch für Fachstellen. Grundsätzlich lässt sich sagen:
 * Der Anspruch kann sich erst auf den Folgemonat des Eingangsdatum erhöhen
 * Reduktionen des Anspruchs sind auch rückwirkend erlaubt
 */
public final class MutationsMerger {

	private static final Logger LOG = LoggerFactory.getLogger(MutationsMerger.class.getSimpleName());

	private MutationsMerger() {
	}

	/**
	 * Um code lesbar zu halten wird die Regel PMD.CollapsibleIfStatements ausgeschaltet
	 */
	@Nonnull
	@SuppressWarnings("PMD.CollapsibleIfStatements")
	public static List<VerfuegungZeitabschnitt> execute(@Nonnull Betreuung betreuung, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {

		if (betreuung.extractGesuch().getTyp().isGesuch()) {
			return zeitabschnitte;
		}
		final Verfuegung vorgaengerVerfuegung = betreuung.getVorgaengerVerfuegung();

		final LocalDate mutationsEingansdatum = betreuung.extractGesuch().getRegelStartDatum();
		Objects.requireNonNull(mutationsEingansdatum);

		List<VerfuegungZeitabschnitt> monatsSchritte = new ArrayList<>();

		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			final LocalDate zeitabschnittStart = verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb();
			final int anspruchberechtigtesPensum = verfuegungZeitabschnitt.getAnspruchberechtigtesPensum();

			final int anspruchAufVorgaengerVerfuegung = findAnspruchberechtigtesPensumAt(zeitabschnittStart, vorgaengerVerfuegung);
			VerfuegungZeitabschnitt zeitabschnitt = copy(verfuegungZeitabschnitt);

			if (anspruchberechtigtesPensum > anspruchAufVorgaengerVerfuegung) {
				//Anspruch wird erhöht
				//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
				if (!isMeldungRechzeitig(verfuegungZeitabschnitt, mutationsEingansdatum)) {
					//Meldung nicht Rechtzeitig: Der Anspruch kann sich erst auf den Folgemonat des Eingangsdatum erhöhen
					zeitabschnitt.setAnspruchberechtigtesPensum(anspruchAufVorgaengerVerfuegung);
					zeitabschnitt.addBemerkung(RuleKey.ANSPRUCHSBERECHNUNGSREGELN_MUTATIONEN, MsgKey.ANSPRUCHSAENDERUNG_MSG);
				}
			} else if (anspruchberechtigtesPensum < anspruchAufVorgaengerVerfuegung) {
				// Anspruch wird kleiner
				//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
				if (!isMeldungRechzeitig(verfuegungZeitabschnitt, mutationsEingansdatum)) {
					//Meldung nicht Rechtzeitig: Reduktionen des Anspruchs sind auch rückwirkend erlaubt -> keine Aenderungen
					zeitabschnitt.addBemerkung(RuleKey.ANSPRUCHSBERECHNUNGSREGELN_MUTATIONEN, MsgKey.REDUCKTION_RUECKWIRKEND_MSG);
				}
			}

			//SCHULKINDER: Sonderregel bei zu Mutation von zu spaet eingereichten Schulkindangeboten
			//fuer Abschnitte ab dem Folgemonat des Mutationseingangs rechnen wir bisher, fuer alle vorherigen folgende Sonderregel
			if (!isMeldungRechzeitig(zeitabschnitt, mutationsEingansdatum)
				&& vorgaengerVerfuegung != null) {

				VerfuegungZeitabschnitt zeitabschnittInVorgaenger = findZeitabschnittInVorgaenger(zeitabschnittStart, vorgaengerVerfuegung);
				// Wenn der Benutzer vorher keine Verfuenstigung bekam weil er zu spaet eingereicht hat DANN bezahlt er auch in Mutation vollkosten
				if (zeitabschnittInVorgaenger != null
					&& zeitabschnittInVorgaenger.getVerguenstigung().compareTo(BigDecimal.ZERO) == 0
					&& zeitabschnittInVorgaenger.isZuSpaetEingereicht()) {
					zeitabschnitt.setBezahltVollkosten(true);
					zeitabschnitt.setZuSpaetEingereicht(true);
					zeitabschnitt.addBemerkung(RuleKey.EINREICHUNGSFRIST, MsgKey.EINREICHUNGSFRIST_VOLLKOSTEN_MSG);
				}
			}

			monatsSchritte.add(zeitabschnitt);
		}

		return monatsSchritte;
	}

	private static boolean isMeldungRechzeitig(VerfuegungZeitabschnitt verfuegungZeitabschnitt, @Nonnull LocalDate mutationsEingansdatum) {
		return verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().withDayOfMonth(1).isAfter((mutationsEingansdatum));
	}

	/**
	 * Hilfsmethode welche in der Vorgaengerferfuegung den gueltigen Zeitabschnitt fuer einen bestimmten Stichtag sucht
	 */
	@Nullable
	private static VerfuegungZeitabschnitt findZeitabschnittInVorgaenger(LocalDate stichtag, Verfuegung vorgaengerVerf) {
		Objects.requireNonNull(vorgaengerVerf, "Vorgaengerverfuegung darf nicht null sein");
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : vorgaengerVerf.getZeitabschnitte()) {
			final DateRange gueltigkeit = verfuegungZeitabschnitt.getGueltigkeit();
			if (gueltigkeit.contains(stichtag) || gueltigkeit.startsSameDay(stichtag)) {
				return verfuegungZeitabschnitt;
			}
		}

		LOG.error("Vorgaengerzeitabschnitt fuer Mutation konnte nicht gefunden werden {}", stichtag);
		return null;
	}

	/**
	 * Findet das anspruchberechtigtes Pensum zum Zeitpunkt des neuen Zeitabschnitt-Start
	 */
	private static int findAnspruchberechtigtesPensumAt(LocalDate zeitabschnittStart, @Nullable Verfuegung verfuegungGSM) {

		if (verfuegungGSM != null) {
			for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : verfuegungGSM.getZeitabschnitte()) {
				final DateRange gueltigkeit = verfuegungZeitabschnitt.getGueltigkeit();
				if (gueltigkeit.contains(zeitabschnittStart) || gueltigkeit.startsSameDay(zeitabschnittStart)) {
					return verfuegungZeitabschnitt.getAnspruchberechtigtesPensum();
				}
			}
			LOG.error("Anspruch berechtigtes Pensum beim Gesuch für Mutation konnte nicht gefunden werden");
		}

		return 0;
	}

	private static VerfuegungZeitabschnitt copy(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(verfuegungZeitabschnitt);
		return zeitabschnitt;
	}

}
