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
@SuppressWarnings("PMD.CollapsibleIfStatements") // wegen besserer Lesbarkeit
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
			VerfuegungZeitabschnitt zeitabschnitt = copy(verfuegungZeitabschnitt);
			VerfuegungZeitabschnitt vorangehenderAbschnitt =
				findZeitabschnittInVorgaenger(zeitabschnittStart, vorgaengerVerfuegung);

			if (vorangehenderAbschnitt != null) {
				handleVerminderungEinkommen(zeitabschnitt, vorangehenderAbschnitt, mutationsEingansdatum);
				handleAnpassungAnspruch(zeitabschnitt, vorangehenderAbschnitt, mutationsEingansdatum);
			}
			monatsSchritte.add(zeitabschnitt);
		}

		return monatsSchritte;
	}

	private static void handleVerminderungEinkommen(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull VerfuegungZeitabschnitt vorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		// Massgebendes Einkommen
		BigDecimal massgebendesEinkommen = zeitabschnitt.getMassgebendesEinkommen();

		if (massgebendesEinkommen.compareTo(vorangehenderAbschnitt.getMassgebendesEinkommen()) < 0) {
			// Massgebendes Einkommen wird kleiner, der Anspruch also höher: Darf nicht rückwirkend sein!
			if (zeitabschnitt.getGueltigkeit().getGueltigAb().isBefore(mutationsEingansdatum)) {
				// Der Stichtag fuer diese Erhöhung ist noch nicht erreicht -> Wir arbeiten mit dem alten Wert!
				// Sobald der Stichtag erreicht ist, müssen wir nichts mehr machen, da dieser Merger *nach* den Monatsabschnitten läuft
				// Wir haben also nie Abschnitte, die über die Monatsgrenze hinausgehen
				zeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(vorangehenderAbschnitt.getMassgebendesEinkommenVorAbzFamgr());
				zeitabschnitt.setFamGroesse(vorangehenderAbschnitt.getFamGroesse());
				zeitabschnitt.setAbzugFamGroesse(vorangehenderAbschnitt.getAbzugFamGroesse());
			}
		}
	}

	private static void handleAnpassungAnspruch(VerfuegungZeitabschnitt zeitabschnitt, VerfuegungZeitabschnitt vorangehenderAbschnitt, LocalDate mutationsEingansdatum) {
		final int anspruchberechtigtesPensum = zeitabschnitt.getAnspruchberechtigtesPensum();
		final int anspruchAufVorgaengerVerfuegung = vorangehenderAbschnitt.getAnspruchberechtigtesPensum();

		if (anspruchberechtigtesPensum > anspruchAufVorgaengerVerfuegung) {
			//Anspruch wird erhöht
			//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
			if (!isMeldungRechzeitig(zeitabschnitt, mutationsEingansdatum)) {
				//Meldung nicht Rechtzeitig: Der Anspruch kann sich erst auf den Folgemonat des Eingangsdatum erhöhen
				zeitabschnitt.setAnspruchberechtigtesPensum(anspruchAufVorgaengerVerfuegung);
				zeitabschnitt.addBemerkung(RuleKey.ANSPRUCHSBERECHNUNGSREGELN_MUTATIONEN, MsgKey.ANSPRUCHSAENDERUNG_MSG);
			}
		} else if (anspruchberechtigtesPensum < anspruchAufVorgaengerVerfuegung) {
			// Anspruch wird kleiner
			//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
			if (!isMeldungRechzeitig(zeitabschnitt, mutationsEingansdatum)) {
				//Meldung nicht Rechtzeitig: Reduktionen des Anspruchs sind auch rückwirkend erlaubt -> keine Aenderungen
				zeitabschnitt.addBemerkung(RuleKey.ANSPRUCHSBERECHNUNGSREGELN_MUTATIONEN, MsgKey.REDUCKTION_RUECKWIRKEND_MSG);
			}
		}

		//SCHULKINDER: Sonderregel bei zu Mutation von zu spaet eingereichten Schulkindangeboten
		//fuer Abschnitte ab dem Folgemonat des Mutationseingangs rechnen wir bisher, fuer alle vorherigen folgende Sonderregel
		if (!isMeldungRechzeitig(zeitabschnitt, mutationsEingansdatum)) {

			// Wenn der Benutzer vorher keine Verfuenstigung bekam weil er zu spaet eingereicht hat DANN bezahlt er auch in Mutation vollkosten
			if (vorangehenderAbschnitt.getVerguenstigung().compareTo(BigDecimal.ZERO) == 0
				&& vorangehenderAbschnitt.isZuSpaetEingereicht()) {
				zeitabschnitt.setBezahltVollkosten(true);
				zeitabschnitt.setZuSpaetEingereicht(true);
				zeitabschnitt.addBemerkung(RuleKey.EINREICHUNGSFRIST, MsgKey.EINREICHUNGSFRIST_VOLLKOSTEN_MSG);
			}
		}
	}

	private static boolean isMeldungRechzeitig(VerfuegungZeitabschnitt verfuegungZeitabschnitt, @Nonnull LocalDate mutationsEingansdatum) {
		return verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().withDayOfMonth(1).isAfter((mutationsEingansdatum));
	}

	/**
	 * Hilfsmethode welche in der Vorgaengerferfuegung den gueltigen Zeitabschnitt fuer einen bestimmten Stichtag sucht
	 */
	@Nullable
	private static VerfuegungZeitabschnitt findZeitabschnittInVorgaenger(@Nonnull LocalDate stichtag, @Nullable Verfuegung vorgaengerVerf) {
		if (vorgaengerVerf == null) {
			return null;
		}
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : vorgaengerVerf.getZeitabschnitte()) {
			final DateRange gueltigkeit = verfuegungZeitabschnitt.getGueltigkeit();
			if (gueltigkeit.contains(stichtag) || gueltigkeit.startsSameDay(stichtag)) {
				return verfuegungZeitabschnitt;
			}
		}

		LOG.error("Vorgaengerzeitabschnitt fuer Mutation konnte nicht gefunden werden {}", stichtag);
		return null;
	}

	private static VerfuegungZeitabschnitt copy(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(verfuegungZeitabschnitt);
		return zeitabschnitt;
	}

}
