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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

/**
 * Umsetzung der ASIV Revision
 * <p>
 * 2. Immer aktuelle Familiengrösse
 * <p>
 * Gem. neuer ASIV Verordnung müssen die Kinder für die Berechnung der Familiengrösse ab dem Beginn den Monats NACH dem
 * Ereigniseintritt (e.g. Geburt) berücksichtigt werden. Dasselbe gilt bei der Aenderung des Zivilstands. Bei einer Mutation
 * der Familiensituation ist das Datum "Aendern per" relevant.
 */
@SuppressWarnings("MethodParameterNamingConvention")
public class FamilienabzugAbschnittRule extends AbstractAbschnittRule {

	private static final Logger LOG = LoggerFactory.getLogger(FamilienabzugAbschnittRule.class);

	private final BigDecimal pauschalabzugProPersonFamiliengroesse3;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse4;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse5;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse6;

	public FamilienabzugAbschnittRule(DateRange validityPeriod,
		BigDecimal pauschalabzugProPersonFamiliengroesse3,
		BigDecimal pauschalabzugProPersonFamiliengroesse4,
		BigDecimal pauschalabzugProPersonFamiliengroesse5,
		BigDecimal pauschalabzugProPersonFamiliengroesse6,
		@Nonnull Locale locale
	) {
		super(RuleKey.FAMILIENSITUATION, RuleType.GRUNDREGEL_DATA, validityPeriod, locale);
		this.pauschalabzugProPersonFamiliengroesse3 = pauschalabzugProPersonFamiliengroesse3;
		this.pauschalabzugProPersonFamiliengroesse4 = pauschalabzugProPersonFamiliengroesse4;
		this.pauschalabzugProPersonFamiliengroesse5 = pauschalabzugProPersonFamiliengroesse5;
		this.pauschalabzugProPersonFamiliengroesse6 = pauschalabzugProPersonFamiliengroesse6;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN, TAGESSCHULE);
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz) {

		Gesuch gesuch = platz.extractGesuch();
		final List<VerfuegungZeitabschnitt> familienAbzugZeitabschnitt = createInitialenFamilienAbzug(gesuch);

		Map<LocalDate, Map.Entry<Double, Integer>> famGrMap = new TreeMap<>();

		// Grundsätzilch muessen wir die Familiengroesse pro Halbjahr einzeln berechnen
		LocalDate startErstesHalbjahr = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb();
		LocalDate startZweitesHalbjahr = startErstesHalbjahr.plusMonths(5);
		famGrMap.put(startErstesHalbjahr, calculateFamiliengroesse(gesuch, startErstesHalbjahr));
		famGrMap.put(startZweitesHalbjahr, calculateFamiliengroesse(gesuch, startZweitesHalbjahr));

		//Suchen aller Geburtstage innerhalb der Gesuchsperiode und speichern in der Liste mit Familiengrösse
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			final LocalDate geburtsdatum = kindContainer.getKindJA().getGeburtsdatum();
			if (gesuch.getGesuchsperiode().getGueltigkeit().contains(geburtsdatum)) {
				final LocalDate beginMonatNachGeb = getStichtagForEreignis(geburtsdatum);
				famGrMap.put(beginMonatNachGeb, calculateFamiliengroesse(gesuch, beginMonatNachGeb));
			}
		}

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		// add famGr if Konkubinat_kein_kind
		if (familiensituation != null
			&& familiensituation.getFamilienstatus() == EnumFamilienstatus.KONKUBINAT_KEIN_KIND
			&& familiensituation.getStartKonkubinat() != null
		) {
			// die familiensituation aendert sich jetzt erst ab dem naechsten Monat, deswegen getStichtagForEreignis
			addFamiliengroesseForKonkubinat(gesuch, famGrMap, familiensituation);
		}

		// add famGr for AenderungPer
		if (familiensituation != null && familiensituation.getAenderungPer() != null) {
			// die familiensituation aendert sich jetzt erst ab dem naechsten Monat, deswegen getStichtagForEreignis
			final LocalDate aenderungPerBeginningNextMonth = getStichtagForEreignis(familiensituation.getAenderungPer());
			famGrMap.put(aenderungPerBeginningNextMonth, calculateFamiliengroesse(gesuch, aenderungPerBeginningNextMonth));
		}

		// aufsteigend durch die unterschiedlichen famGr gehen und immer den letzen Abschnitt  unterteilen in zwei Abschnitte
		for (Map.Entry<LocalDate, Map.Entry<Double, Integer>> entry : famGrMap.entrySet()) {
			final VerfuegungZeitabschnitt lastVerfuegungZeitabschnitt = familienAbzugZeitabschnitt.get(familienAbzugZeitabschnitt.size() - 1);
			lastVerfuegungZeitabschnitt.getGueltigkeit().setGueltigBis(entry.getKey().minusDays(1));

			final VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
			verfuegungZeitabschnitt.getGueltigkeit().setGueltigAb(entry.getKey());
			verfuegungZeitabschnitt.getGueltigkeit().setGueltigBis(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis());
			verfuegungZeitabschnitt.setAbzugFamGroesseForAsivAndGemeinde(calculateAbzugAufgrundFamiliengroesse(entry.getValue().getKey(), entry.getValue().getValue()));
			verfuegungZeitabschnitt.setFamGroesseForAsivAndGemeinde(new BigDecimal(String.valueOf(entry.getValue().getKey())));

			familienAbzugZeitabschnitt.add(verfuegungZeitabschnitt);
		}

		return familienAbzugZeitabschnitt;
	}

	private void addFamiliengroesseForKonkubinat(
		@Nonnull Gesuch gesuch,
		@Nonnull Map<LocalDate, Entry<Double, Integer>> famGrMap,
		@Nonnull Familiensituation familiensituation
	) {
		Objects.requireNonNull(familiensituation.getStartKonkubinat());

		final LocalDate konkubinatBeginningNextMonth =
			getStichtagForEreignis(familiensituation.getStartKonkubinat().plusYears(5));
		final LocalDate konkubinatDate = familiensituation.getStartKonkubinat().plusYears(5).plusMonths(1);

		Double famGrBeruecksichtigungAbzug = 0.0;

		famGrBeruecksichtigungAbzug = calculateGanzFamiliensituation(
			gesuch,
			familiensituation,
			konkubinatBeginningNextMonth,
			konkubinatDate,
			famGrBeruecksichtigungAbzug);

		// es gibt keine 'halben' Eltern, deswegen sind die Werte hier gleich.
		int famGrAnzahlPersonen = famGrBeruecksichtigungAbzug.intValue();

		final Entry<Double, Integer> finalResult = addAbzugFromKinder(
			gesuch,
			konkubinatBeginningNextMonth,
			famGrBeruecksichtigungAbzug,
			famGrAnzahlPersonen);

		famGrMap.put(konkubinatBeginningNextMonth, finalResult);
	}

	@Nonnull
	private Double calculateGanzFamiliensituation(
		@Nonnull Gesuch gesuch,
		@Nonnull Familiensituation familiensituation,
		@Nonnull LocalDate eventDate,
		@Nonnull LocalDate dateForCheckSecondGesuchsteller,
		@Nonnull Double famGrBeruecksichtigungAbzug
	) {
		Familiensituation familiensituationErstgesuch = gesuch.extractFamiliensituationErstgesuch();
		LocalDate familiensituationGueltigAb = familiensituation.getAenderungPer();
		if (familiensituationErstgesuch != null && (
			familiensituationGueltigAb == null //wenn aenderung per nicht gesetzt ist nehmen wir wert aus erstgesuch
				|| eventDate.isBefore(getStichtagForEreignis(familiensituationGueltigAb)))) {

			return famGrBeruecksichtigungAbzug
				+ (familiensituationErstgesuch.hasSecondGesuchsteller(dateForCheckSecondGesuchsteller) ? 2 : 1);
		}
		return famGrBeruecksichtigungAbzug
			+ (familiensituation.hasSecondGesuchsteller(dateForCheckSecondGesuchsteller) ? 2 : 1);
	}

	private Map.Entry<Double, Integer> addAbzugFromKinder(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate konkubinatBeginningNextMonth,
		@Nonnull Double famGrBeruecksichtigungAbzug,
		int famGrAnzahlPersonen
	) {
		LocalDate dateToCompare = getRelevantDateForKinder(gesuch.getGesuchsperiode(), konkubinatBeginningNextMonth);

		// Ermitteln, ob der KinderabzugErstesHalbjahr oder KinderabzugZweitesHalbjahr zum Zug kommen soll
		boolean isErstesHalbjahr = gesuch.getGesuchsperiode().getBasisJahrPlus1() == konkubinatBeginningNextMonth.getYear();
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			Kind kind = kindContainer.getKindJA();
			if (kind != null && (dateToCompare == null || kind.getGeburtsdatum().isBefore(dateToCompare))) {
				Kinderabzug kinderabzug = isErstesHalbjahr ? kind.getKinderabzugErstesHalbjahr() : kind.getKinderabzugZweitesHalbjahr();
				if (kinderabzug == Kinderabzug.HALBER_ABZUG) {
					famGrBeruecksichtigungAbzug += 0.5;
					famGrAnzahlPersonen++;
				} else if (kinderabzug == Kinderabzug.GANZER_ABZUG) {
					famGrBeruecksichtigungAbzug += 1;
					famGrAnzahlPersonen++;
				}
			}
		}

		return new AbstractMap.SimpleEntry(famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
	}

	public List<VerfuegungZeitabschnitt> createInitialenFamilienAbzug(Gesuch gesuch) {
		VerfuegungZeitabschnitt initialFamAbzug = new VerfuegungZeitabschnitt(gesuch.getGesuchsperiode().getGueltigkeit());
		//initial gilt die Familiengroesse die am letzten Tag vor dem Start der neuen Gesuchsperiode vorhanden war
		Double famGrBeruecksichtigungAbzug = 0.0;
		Integer famGrAnzahlPersonen = 0;

		if (gesuch.getGesuchsperiode() != null) {
			Map.Entry<Double, Integer> famGr = calculateFamiliengroesse(gesuch, gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
			famGrBeruecksichtigungAbzug = famGr.getKey();
			famGrAnzahlPersonen = famGr.getValue();
		}

		BigDecimal abzugAufgrundFamiliengroesse = getAbzugFamGroesse(gesuch, famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
		initialFamAbzug.setAbzugFamGroesseForAsivAndGemeinde(abzugAufgrundFamiliengroesse);
		initialFamAbzug.setFamGroesseForAsivAndGemeinde(new BigDecimal(String.valueOf(famGrBeruecksichtigungAbzug)));

		List<VerfuegungZeitabschnitt> initialFamAbzugList = new ArrayList<>();
		initialFamAbzugList.add(initialFamAbzug);
		return initialFamAbzugList;
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}

	private BigDecimal getAbzugFamGroesse(Gesuch gesuch, double famGrBeruecksichtigungAbzug, int famGrAnzahlPersonen) {
		return gesuch.getGesuchsperiode() == null ? BigDecimal.ZERO :
			calculateAbzugAufgrundFamiliengroesse(famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
	}

	/**
	 * Die Familiengroesse wird folgendermassen kalkuliert:
	 * Familiengrösse = Gesuchsteller1 + Gesuchsteller2 (falls vorhanden) + Faktor Steuerabzug pro Kind (0, 0.5, oder 1)
	 * <p>
	 * Der Faktor wird gemaess Wert des Felds kinderabzug von Kind berechnet:
	 * KEIN_ABZUG = 0
	 * HALBER_ABZUG = 0.5
	 * GANZER_ABZUG = 1
	 * <p>
	 * Nur die Kinder die vor dem uebergebenen Datum geboren sind werden mitberechnet
	 * <p>
	 *
	 * @param gesuch das Gesuch aus welchem die Daten geholt werden
	 * @param date das Datum fuer das die familiengroesse kalkuliert werden muss
	 * @return die familiengroesse:
	 * key: Familienabzug unter Berücksichtigung des halben oder ganzen Familienabzug als Double
	 * value: Familienabzug unter der Anzahl Personen in der Familie als Integer
	 */
	Map.Entry<Double, Integer> calculateFamiliengroesse(@Nonnull Gesuch gesuch, @Nonnull LocalDate date) {

		Double famGrBeruecksichtigungAbzug = 0.0;

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		if (familiensituation != null) { // wenn die Familiensituation nicht vorhanden ist, kann man nichts machen (die Daten wurden falsch eingegeben)
			famGrBeruecksichtigungAbzug = calculateGanzFamiliensituation(gesuch, familiensituation, date, date, famGrBeruecksichtigungAbzug);
		} else {
			LOG.warn("Die Familiengroesse kann noch nicht richtig berechnet werden weil die Familiensituation nicht richtig ausgefuellt ist. Antragnummer: {}", gesuch.getJahrFallAndGemeindenummer());
		}

		// es gibt keine 'halben' Eltern, deswegen sind die Werte hier gleich.
		int famGrAnzahlPersonen = famGrBeruecksichtigungAbzug.intValue();

		return addAbzugFromKinder(gesuch, date, famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
	}

	/**
	 * This method will check if date is before the beginning of the Gesuchsperiode. In that case the beginning of
	 * the Gesuchsperiode will be returned. In case not, the date itself will be returned. If the passed date is null
	 * then null will be returned regardless of the comparisson between boths dates.
	 */
	@Nullable
	private LocalDate getRelevantDateForKinder(@NotNull Gesuchsperiode gesuchsperiode, @Nullable LocalDate date) {
		LocalDate dateToCompare = null;
		if (date != null) {
			dateToCompare = (date.isBefore(gesuchsperiode.getGueltigkeit().getGueltigAb()))
				? gesuchsperiode.getGueltigkeit().getGueltigAb() : date;
		}
		return dateToCompare;
	}

	/**
	 * Berechnete Familiengrösse (halber Abzug berücksichtigen) multipliziert mit dem ermittelten Personen-Haushalt-Pauschalabzug
	 * (Anzahl Personen in Familie)
	 *
	 * @return abzug aufgrund Familiengrösse
	 */
	BigDecimal calculateAbzugAufgrundFamiliengroesse(double famGrBeruecksichtigungAbzug, int famGrAnzahlPersonen) {

		BigDecimal abzugFromServer = BigDecimal.ZERO;
		// Unter 3 Personen gibt es keinen Abzug!
		if (famGrAnzahlPersonen == 3) {
			abzugFromServer = pauschalabzugProPersonFamiliengroesse3;
		} else if (famGrAnzahlPersonen == 4) {
			abzugFromServer = pauschalabzugProPersonFamiliengroesse4;
		} else if (famGrAnzahlPersonen == 5) {
			abzugFromServer = pauschalabzugProPersonFamiliengroesse5;
		} else if (famGrAnzahlPersonen > 5) {
			abzugFromServer = pauschalabzugProPersonFamiliengroesse6;
		}

		// Ein Bigdecimal darf nicht aus einem double erzeugt werden, da das Ergebnis nicht genau die gegebene Nummer waere
		// deswegen muss man hier familiengroesse als String uebergeben. Sonst bekommen wir PMD rule AvoidDecimalLiteralsInBigDecimalConstructor
		// Wir runden die Zahl ausserdem zu einer Ganzzahl weil wir fuer das Massgebende einkommen mit Ganzzahlen rechnen
		return MathUtil.GANZZAHL.from(new BigDecimal(String.valueOf(famGrBeruecksichtigungAbzug)).multiply(abzugFromServer));
	}
}
