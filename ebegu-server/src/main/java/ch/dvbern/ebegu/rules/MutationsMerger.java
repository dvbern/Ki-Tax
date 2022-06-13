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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

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
 * <p>
 * Regel für das Setzten des Flags auszahlungAnEltern bei einer Mutation:
 * - Flag hat denselben Wert im aktuellen als auch im Vorgänger Zeitabschnitt -> keine Aktion
 * - Flag hat nicht denselben Wert im aktuellen wie im Vorgänger Zeitabschnitt
 * 		- Wenn Zahlung bereits ausgeführt -> Wert des Vorgängers übernehmen
 * 	    - Wenn Zahlung noch nicht ausgeführt -> Wert des aktuellen überhnehmen
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
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		if (platz.extractGesuch().getTyp().isGesuch()) {
			return zeitabschnitte;
		}
		final Verfuegung vorgaengerVerfuegung = platz.getVorgaengerVerfuegung();
		final LocalDate mutationsEingansdatum = platz.extractGesuch().getRegelStartDatum();
		Objects.requireNonNull(mutationsEingansdatum);

		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {

			final LocalDate zeitabschnittStart = verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb();
			VerfuegungZeitabschnitt vorangehenderAbschnitt =
				findZeitabschnittInVorgaenger(zeitabschnittStart, vorgaengerVerfuegung);

			if (vorangehenderAbschnitt != null) {
				BGCalculationInput inputAsiv = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
				BGCalculationResult resultAsivVorangehenderAbschnitt = vorangehenderAbschnitt.getBgCalculationResultAsiv();

				boolean finSitAbgelehnt = FinSitStatus.ABGELEHNT == platz.extractGesuch().getFinSitStatus();
				LocalDateTime timestampVerfuegtVorgaenger = null;
				if (finSitAbgelehnt) {
					// Wenn FinSit abgelehnt, muss immer das letzte verfuegte Einkommen genommen werden
					timestampVerfuegtVorgaenger = vorgaengerVerfuegung.getPlatz().extractGesuch().getTimestampVerfuegt();
					Objects.requireNonNull(timestampVerfuegtVorgaenger);
					handleAbgelehnteFinsit(inputAsiv, resultAsivVorangehenderAbschnitt, timestampVerfuegtVorgaenger);
				} else {
					// Der Spezialfall bei Verminderung des Einkommens gilt nur, wenn die FinSit akzeptiert/null war!
					handleVerminderungEinkommen(inputAsiv,
						resultAsivVorangehenderAbschnitt, mutationsEingansdatum);
				}
				handleAnpassungErweiterteBeduerfnisse(inputAsiv, resultAsivVorangehenderAbschnitt, mutationsEingansdatum);

				handleEinreichfrist(inputAsiv, mutationsEingansdatum);
				handleAuszahlungAnElternFlag(verfuegungZeitabschnitt, vorangehenderAbschnitt);

				if (platz.isAngebotSchulamt() && platz.hasVorgaenger() && inputAsiv.isZuSpaetEingereicht()) {
					inputAsiv.setZuSpaetEingereicht(vorangehenderAbschnitt.isZuSpaetEingereicht());
				}

				handleAnpassungAnspruch(inputAsiv, resultAsivVorangehenderAbschnitt, mutationsEingansdatum);

				BGCalculationInput inputGemeinde = verfuegungZeitabschnitt.getBgCalculationInputGemeinde();
				BGCalculationResult resultGemeindeVorangehenderAbschnitt = vorangehenderAbschnitt.getBgCalculationResultGemeinde();

				if (vorangehenderAbschnitt.isHasGemeindeSpezifischeBerechnung() && resultGemeindeVorangehenderAbschnitt != null) {
					if (finSitAbgelehnt) {
						// Wenn FinSit abgelehnt, muss immer das letzte verfuegte Einkommen genommen werden
						handleAbgelehnteFinsit(inputGemeinde, resultGemeindeVorangehenderAbschnitt, timestampVerfuegtVorgaenger);
					} else {
						// Der Spezialfall bei Verminderung des Einkommens gilt nur, wenn die FinSit akzeptiert/null war!
						handleVerminderungEinkommen(inputGemeinde, resultGemeindeVorangehenderAbschnitt, mutationsEingansdatum);
					}
					handleAnpassungErweiterteBeduerfnisse(inputGemeinde, resultGemeindeVorangehenderAbschnitt, mutationsEingansdatum);
					handleAnpassungAnspruch(inputGemeinde, resultGemeindeVorangehenderAbschnitt, mutationsEingansdatum);
				}
			}
		}
		return zeitabschnitte;
	}

	private void handleAuszahlungAnElternFlag(
		VerfuegungZeitabschnitt aktuellerAbschnitt,
		VerfuegungZeitabschnitt vorangehenderAbschnitt) {

		if (vorangehenderAbschnitt.getZahlungsstatus().isVerrechnet() || vorangehenderAbschnitt.getZahlungsstatus().isVerrechnend()) {
			aktuellerAbschnitt.setAuszahlungAnEltern(vorangehenderAbschnitt.isAuszahlungAnEltern());
		}
	}

	private void handleEinreichfrist(BGCalculationInput inputAsiv, LocalDate mutationsEingansdatum) {
		//Wenn das Eingangsdatum der Meldung nach der Gültigkeit des Zeitabschnitts ist, soll das Flag ZuSpaetEingereicht gesetzt werden
		if(isMeldungZuSpaet(inputAsiv.getParent().getGueltigkeit(), mutationsEingansdatum)) {
			inputAsiv.setZuSpaetEingereicht(true);
		}
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
			inputData.addBemerkung(MsgKey.ANSPRUCHSAENDERUNG_MSG, locale);
		}
	}

	private void handleVerminderungEinkommen(
		@Nonnull BGCalculationInput inputData,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		// Massgebendes Einkommen
		BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommen();
		if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) <= 0) {
			// Massgebendes Einkommen wird kleiner, der Anspruch also höher: Darf nicht rückwirkend sein!
			if (!inputData.getParent().getGueltigkeit().getGueltigAb().isAfter(mutationsEingansdatum)) {
				// Der Stichtag fuer diese Erhöhung ist noch nicht erreicht -> Wir arbeiten mit dem alten Wert!
				// Sobald der Stichtag erreicht ist, müssen wir nichts mehr machen, da dieser Merger *nach* den Monatsabschnitten läuft
				// Wir haben also nie Abschnitte, die über die Monatsgrenze hinausgehen
				inputData.setMassgebendesEinkommenVorAbzugFamgr(resultVorangehenderAbschnitt.getMassgebendesEinkommenVorAbzugFamgr());
				inputData.setEinkommensjahr(resultVorangehenderAbschnitt.getEinkommensjahr());
				inputData.setFamGroesse(resultVorangehenderAbschnitt.getFamGroesse());
				inputData.setAbzugFamGroesse(resultVorangehenderAbschnitt.getAbzugFamGroesse());

				if (resultVorangehenderAbschnitt.getTsCalculationResultMitPaedagogischerBetreuung() != null) {
					inputData.getTsInputMitBetreuung().setVerpflegungskostenVerguenstigt(
						getValueOrZero(
							resultVorangehenderAbschnitt.getTsCalculationResultMitPaedagogischerBetreuung().getVerpflegungskostenVerguenstigt()));
				} else {
					inputData.getTsInputMitBetreuung().setVerpflegungskostenVerguenstigt(BigDecimal.ZERO);
				}
				if (resultVorangehenderAbschnitt.getTsCalculationResultOhnePaedagogischerBetreuung() != null) {
					inputData.getTsInputOhneBetreuung().setVerpflegungskostenVerguenstigt(
						getValueOrZero(
							resultVorangehenderAbschnitt.getTsCalculationResultOhnePaedagogischerBetreuung().getVerpflegungskostenVerguenstigt()));
				} else {
					inputData.getTsInputOhneBetreuung().setVerpflegungskostenVerguenstigt(BigDecimal.ZERO);
				}
				if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) < 0) {
					inputData.addBemerkung(MsgKey.ANSPRUCHSAENDERUNG_MSG, locale);
				}
			}
		}
	}

	@Nonnull
	private BigDecimal getValueOrZero(@Nullable BigDecimal value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		return value;
	}

	private void handleAbgelehnteFinsit(
		@Nonnull BGCalculationInput inputData,
		@Nonnull BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDateTime timestampVerfuegtVorgaenger
	) {
		// Falls die FinSit in der Mutation abgelehnt wurde, muss grundsaetzlich das Einkommen der Vorverfuegung genommen werden,
		// unabhaengig davon, ob das Einkommen steigt oder sinkt und ob es rechtzeitig gemeldet wurde
		BigDecimal massgebendesEinkommen = inputData.getMassgebendesEinkommen();
		BigDecimal massgebendesEinkommenVorher = resultVorangehenderAbschnitt.getMassgebendesEinkommen();

		inputData.setMassgebendesEinkommenVorAbzugFamgr(resultVorangehenderAbschnitt.getMassgebendesEinkommenVorAbzugFamgr());
		inputData.setEinkommensjahr(resultVorangehenderAbschnitt.getEinkommensjahr());
		inputData.setFamGroesse(resultVorangehenderAbschnitt.getFamGroesse());
		inputData.setAbzugFamGroesse(resultVorangehenderAbschnitt.getAbzugFamGroesse());
		if (massgebendesEinkommen.compareTo(massgebendesEinkommenVorher) != 0) {
			// Die Bemerkung immer dann setzen, wenn das Einkommen (egal in welche Richtung) geaendert haette
			String datumLetzteVerfuegung = Constants.DATE_FORMATTER.format(timestampVerfuegtVorgaenger);
			inputData.addBemerkung(MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_MUTATION_MSG, locale, datumLetzteVerfuegung);
		}
	}

	private void handleAnpassungAnspruch(
		@Nonnull BGCalculationInput inputData,
		@Nullable BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		final int anspruchberechtigtesPensum = inputData.getAnspruchspensumProzent();
		final int anspruchAufVorgaengerVerfuegung = resultVorangehenderAbschnitt == null
			? 0
			: resultVorangehenderAbschnitt.getAnspruchspensumProzent();

		DateRange gueltigkeit = inputData.getParent().getGueltigkeit();

		if (anspruchberechtigtesPensum > anspruchAufVorgaengerVerfuegung) {
			//Anspruch wird erhöht
			//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
			if (isMeldungZuSpaet(gueltigkeit, mutationsEingansdatum)) {
				//Meldung nicht Rechtzeitig: Der Anspruch kann sich erst auf den Folgemonat des Eingangsdatum erhöhen
				inputData.setAnspruchspensumProzent(anspruchAufVorgaengerVerfuegung);
				inputData.setRueckwirkendReduziertesPensumRest(anspruchberechtigtesPensum - inputData.getAnspruchspensumProzent());
				//Wenn der Anspruch auf dem Vorgänger 0 ist, weil das Erstgesuch zu spät eingereicht wurde
				//soll die Bemerkung bezüglich der Erhöhung nicht angezeigt werden, da es sich um keine Erhöhung handelt
				if(!isAnspruchZeroBecauseVorgaengerZuSpaet(resultVorangehenderAbschnitt)) {
					inputData.addBemerkung(MsgKey.ANSPRUCHSAENDERUNG_MSG, locale);
				}
			}
		} else if (anspruchberechtigtesPensum < anspruchAufVorgaengerVerfuegung) {
			// Anspruch wird kleiner
			//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
			if (isMeldungZuSpaet(gueltigkeit, mutationsEingansdatum)) {
				//Meldung nicht Rechtzeitig: Reduktionen des Anspruchs sind auch rückwirkend erlaubt -> keine Aenderungen
				inputData.addBemerkung(MsgKey.REDUCKTION_RUECKWIRKEND_MSG, locale);
			}
		}
	}

	private boolean isAnspruchZeroBecauseVorgaengerZuSpaet(BGCalculationResult resultVorangehenderAbschnitt) {
		if(resultVorangehenderAbschnitt == null) {
			return false;
		}

		boolean anspruchsPensumZero = resultVorangehenderAbschnitt.getAnspruchspensumProzent() == 0;
		return anspruchsPensumZero && resultVorangehenderAbschnitt.isZuSpaetEingereicht();
	}

	private boolean isMeldungZuSpaet(@Nonnull DateRange gueltigkeit, @Nonnull LocalDate mutationsEingansdatum) {
		return !gueltigkeit.getGueltigAb().withDayOfMonth(1).isAfter((mutationsEingansdatum));
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
