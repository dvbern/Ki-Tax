/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.mutationsmerger;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractAbschlussRule;
import ch.dvbern.ebegu.types.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp.getBerechnetesAngebotTypes;

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
 * Verspätete Meldung: Wird die Änderung des Arbeitspensums im Monat des Ereignis oder noch später gemeldet, erfolgt eine
 * ERHÖHUNG des Anspruchs erst auf den Folgemonat
 * <p>
 * Im Falle einer Herabsetzung des Arbeitspensums, wird der Anspruch zusammen mit dem Ereigniseintritt angepasst
 * <p>
 * Dieselbe Regeln gilt für sämtliche Berechnungen des Anspruchs, d.h. auch für Fachstellen. Grundsätzlich lässt sich sagen:
 * Der Anspruch kann sich erst auf den Folgemonat des Eingangsdatum erhöhen
 * Reduktionen des Anspruchs sind auch rückwirkend erlaubt
 * <p>
 * Regel für das Setzten des Flags auszahlungAnEltern bei einer Mutation:
 * - Flag hat denselben Wert im aktuellen als auch im Vorgänger Zeitabschnitt -> keine Aktion
 * - Flag hat nicht denselben Wert im aktuellen wie im Vorgänger Zeitabschnitt
 * - Wenn Zahlung bereits ausgeführt -> Wert des Vorgängers übernehmen
 * - Wenn Zahlung noch nicht ausgeführt -> Wert des aktuellen überhnehmen
 */
@SuppressWarnings("PMD.CollapsibleIfStatements") // wegen besserer Lesbarkeit
public final class MutationsMerger extends AbstractAbschlussRule {

	private static final Logger LOG = LoggerFactory.getLogger(MutationsMerger.class.getSimpleName());

	private Locale locale;
	private Boolean pauschaleRueckwirkendAuszahlen;

	public MutationsMerger(@Nonnull Locale locale, boolean isDebug, Boolean pauschaleRueckwirkendAuszahlen) {
		super(isDebug);
		this.locale = locale;
		this.pauschaleRueckwirkendAuszahlen = pauschaleRueckwirkendAuszahlen;
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return getBerechnetesAngebotTypes();
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		if (platz.extractGesuch().getTyp().isGesuch()) {
			return zeitabschnitte;
		}
		final Verfuegung vorgaengerVerfuegung = platz.getVorgaengerVerfuegung();

		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {

			final LocalDate zeitabschnittStart = verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb();
			VerfuegungZeitabschnitt vorangehenderAbschnitt =
				findZeitabschnittInVorgaenger(zeitabschnittStart, vorgaengerVerfuegung);
			VerfuegungZeitabschnitt vorgaengerZeitabschnittVerfugegungAusbezahlt =
				getVorgaengerZeitabschnittVerfugegungAusbezahlt(platz, zeitabschnittStart);

			if (vorangehenderAbschnitt != null) {
				BGCalculationInput inputAsiv = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
				BGCalculationResult resultAsivVorangehenderAbschnitt = vorangehenderAbschnitt.getBgCalculationResultAsiv();

				handleMutation(inputAsiv, resultAsivVorangehenderAbschnitt, platz);
				handleAuszahlungAnElternFlag(
					verfuegungZeitabschnitt,
					vorgaengerZeitabschnittVerfugegungAusbezahlt != null ?
						vorgaengerZeitabschnittVerfugegungAusbezahlt :
						vorangehenderAbschnitt);

				BGCalculationInput inputGemeinde = verfuegungZeitabschnitt.getBgCalculationInputGemeinde();
				BGCalculationResult resultGemeindeVorangehenderAbschnitt =
					vorangehenderAbschnitt.getBgCalculationResultGemeinde();

				if (vorangehenderAbschnitt.isHasGemeindeSpezifischeBerechnung() && resultGemeindeVorangehenderAbschnitt != null) {
					handleMutation(inputGemeinde, resultGemeindeVorangehenderAbschnitt, platz);
				}
			}
		}
		return zeitabschnitte;
	}

	@Nullable
	private VerfuegungZeitabschnitt getVorgaengerZeitabschnittVerfugegungAusbezahlt(
		AbstractPlatz platz,
		LocalDate zeitabschnittStart) {

		if (!(platz instanceof Betreuung)) {
			return null;
		}

		Map<ZahlungslaufTyp, Verfuegung> allVorgaengerVerfugegungAusbezahlt =
			((Betreuung) platz).getVorgaengerAusbezahlteVerfuegungProAuszahlungstyp();

		if (allVorgaengerVerfugegungAusbezahlt == null) {
			return null;
		}
		if (allVorgaengerVerfugegungAusbezahlt.isEmpty()) {
			return null;
		}

		Verfuegung vorgaengerVerfuegungAusbezahlt =
			allVorgaengerVerfugegungAusbezahlt.values().stream().reduce(null, (prev, cur) -> {
				if (prev == null) {
					return cur;
				}
				Objects.requireNonNull(cur.getTimestampMutiert());
				Objects.requireNonNull(prev.getTimestampMutiert());
				return cur.getTimestampMutiert().isBefore(prev.getTimestampMutiert()) ? cur : prev;
			});

		return findZeitabschnittInVorgaenger(zeitabschnittStart, vorgaengerVerfuegungAusbezahlt);
	}

	private void handleMutation(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz) {

		final LocalDate mutationsEingansdatum = platz.extractGesuch().getRegelStartDatum();
		Objects.requireNonNull(mutationsEingansdatum);

		handleFinanzielleSituation(inputAktuel, resultVorgaenger, platz, mutationsEingansdatum);
		handleAnpassungErweiterteBeduerfnisse(inputAktuel, resultVorgaenger, mutationsEingansdatum);
		handleEinreichfrist(inputAktuel, resultVorgaenger, platz, mutationsEingansdatum);
		handleAnspruch(inputAktuel, resultVorgaenger, platz, mutationsEingansdatum);
		handleBedarfsstufe(inputAktuel, resultVorgaenger, mutationsEingansdatum);
	}

	private void handleBedarfsstufe(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		LocalDate mutationsEingansdatum) {
		if (!isMeldungZuSpaet(inputAktuel.getParent().getGueltigkeit(), mutationsEingansdatum) ||
			inputAktuel.getBedarfsstufe() == resultVorgaenger.getBedarfsstufe()) {
			return;
		}

		inputAktuel.setBedarfsstufe(resultVorgaenger.getBedarfsstufe());
		inputAktuel.getParent().getBemerkungenDTOList().removeBemerkungByMsgKey(MsgKey.BEDARFSSTUFE_MSG);
		inputAktuel.getParent().getBemerkungenDTOList().removeBemerkungByMsgKey(MsgKey.BEDARFSSTUFE_NICHT_GEWAEHRT_MSG);
		inputAktuel.addBemerkungWithGueltigkeitOfAbschnitt(MsgKey.BEDARFSSTUFE_AENDERUNG_MSG, locale, inputAktuel.getBedarfsstufe());
	}

	private void handleAnspruch(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {
		new MutationsMergerAnspruchHandlerVisitor(locale)
			.getAnspruchHandler(platz.extractGesuch().extractMandant().getMandantIdentifier())
			.handleAnpassungAnspruch(inputAktuel, resultVorgaenger, mutationsEingansdatum);
	}

	private void handleFinanzielleSituation(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum) {

		new MutationsMergerFinanzielleSituationVisitor(locale)
			.getMutationsMergerFinanzielleSituation(platz.extractGesuch().getFinSitTyp())
			.handleFinanzielleSituation(inputAktuel, resultVorgaenger, platz, mutationsEingansdatum);
	}

	private void handleAuszahlungAnElternFlag(
		VerfuegungZeitabschnitt aktuellerAbschnitt,
		VerfuegungZeitabschnitt vorangehenderAbschnitt) {

		if (vorangehenderAbschnitt.getZahlungsstatusInstitution().hasZeitabschnittOrVorgaengerBeenAusbezahlt()
		   || vorangehenderAbschnitt.getZahlungsstatusAntragsteller().hasZeitabschnittOrVorgaengerBeenAusbezahlt()) {
			aktuellerAbschnitt.setAuszahlungAnEltern(vorangehenderAbschnitt.isAuszahlungAnEltern());
		}
	}

	private void handleEinreichfrist(BGCalculationInput input,
									 BGCalculationResult resultVorgaenger,
									 AbstractPlatz platz,
									 LocalDate mutationsEingansdatum) {
		//Wenn das Eingangsdatum der Meldung nach der Gültigkeit des Zeitabschnitts ist, soll das Flag
		// ZuSpaetEingereicht gesetzt werden
		if (isMeldungZuSpaet(input.getParent().getGueltigkeit(), mutationsEingansdatum)) {
			input.setZuSpaetEingereicht(true);
		}

		if (platz.isAngebotSchulamt() && platz.hasVorgaenger() && input.isZuSpaetEingereicht()) {
			input.setZuSpaetEingereicht(resultVorgaenger.isZuSpaetEingereicht());
		}
	}

	private boolean isMeldungZuSpaet(@Nonnull DateRange gueltigkeit, @Nonnull LocalDate mutationsEingansdatum) {
		return !gueltigkeit.getGueltigAb().withDayOfMonth(1).isAfter((mutationsEingansdatum));
	}

	private void handleAnpassungErweiterteBeduerfnisse(
		@Nonnull BGCalculationInput inputData,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		// Es muss nur etwas gemacht werden, wenn im alten Abschnitt kein Zuschlag war, neu aber schon, UND
		// zu spät eingereicht
		if (inputData.isBesondereBeduerfnisseBestaetigt()
			&& !resultVorangehenderAbschnitt.isBesondereBeduerfnisseBestaetigt()
			&& !inputData.getParent().getGueltigkeit().getGueltigAb().isAfter(mutationsEingansdatum)
			&& !pauschaleRueckwirkendAuszahlen
		) {
			inputData.setBesondereBeduerfnisseBestaetigt(false);
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(MsgKey.ANSPRUCHSAENDERUNG_MSG, locale);
		}
	}

	/**
	 * Hilfsmethode welche in der Vorgaengerferfuegung den gueltigen Zeitabschnitt fuer einen bestimmten Stichtag sucht
	 */
	@Nullable
	private VerfuegungZeitabschnitt findZeitabschnittInVorgaenger(
		@Nonnull LocalDate stichtag,
		@Nullable Verfuegung vorgaengerVerf
	) {
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
}
