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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.types.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.EinstellungKey.MINIMALDAUER_KONKUBINAT;

public abstract class AbstractFamilienabzugAbschnittRule extends AbstractAbschnittRule {
	private static final Logger LOG = LoggerFactory.getLogger(FamilienabzugAbschnittRuleBern.class);

	private final Integer paramMinDauerKonkubinat;

	protected AbstractFamilienabzugAbschnittRule(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(RuleKey.FAMILIENSITUATION, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
		paramMinDauerKonkubinat = einstellungMap.get(MINIMALDAUER_KONKUBINAT).getValueAsInteger();
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
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
			final LocalDate dateWith18 = geburtsdatum.plusYears(18);
			if (gesuch.getGesuchsperiode().getGueltigkeit().contains(dateWith18)) {
				final LocalDate beginMonatNach18Geb = getStichtagForEreignis(dateWith18);
				famGrMap.put(beginMonatNach18Geb, calculateFamiliengroesse(gesuch, beginMonatNach18Geb));
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
			final VerfuegungZeitabschnitt lastVerfuegungZeitabschnitt =
				familienAbzugZeitabschnitt.get(familienAbzugZeitabschnitt.size() - 1);
			lastVerfuegungZeitabschnitt.getGueltigkeit().setGueltigBis(entry.getKey().minusDays(1));

			final VerfuegungZeitabschnitt verfuegungZeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(
				entry.getKey(),
				gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis());
			verfuegungZeitabschnitt.setAbzugFamGroesseForAsivAndGemeinde(calculateAbzugAufgrundFamiliengroesse(entry.getValue()
				.getKey(), entry.getValue().getValue()));
			verfuegungZeitabschnitt.setFamGroesseForAsivAndGemeinde(new BigDecimal(String.valueOf(entry.getValue().getKey())));

			familienAbzugZeitabschnitt.add(verfuegungZeitabschnitt);
		}

		return familienAbzugZeitabschnitt;
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
	protected Map.Entry<Double, Integer> calculateFamiliengroesse(@Nonnull Gesuch gesuch, @Nonnull LocalDate date) {

		Double famGrBeruecksichtigungAbzug = 0.0;

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		if (familiensituation
			!= null) { // wenn die Familiensituation nicht vorhanden ist, kann man nichts machen (die Daten wurden falsch eingegeben)
			famGrBeruecksichtigungAbzug =
				calculateGanzFamiliensituation(gesuch, familiensituation, date, date, famGrBeruecksichtigungAbzug);
		} else {
			LOG.warn(
				"Die Familiengroesse kann noch nicht richtig berechnet werden weil die Familiensituation nicht richtig "
					+ "ausgefuellt ist. Antragnummer: {}",
				gesuch.getJahrFallAndGemeindenummer());
		}

		// es gibt keine 'halben' Eltern, deswegen sind die Werte hier gleich.
		int famGrAnzahlPersonen = famGrBeruecksichtigungAbzug.intValue();

		return addAbzugFromKinder(gesuch, date, famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
	}

	private void addFamiliengroesseForKonkubinat(
		@Nonnull Gesuch gesuch,
		@Nonnull Map<LocalDate, Entry<Double, Integer>> famGrMap,
		@Nonnull Familiensituation familiensituation
	) {
		Objects.requireNonNull(familiensituation.getStartKonkubinat());

		final LocalDate konkubinatBeginningNextMonth =
			getStichtagForEreignis(familiensituation.getStartKonkubinat().plusYears(paramMinDauerKonkubinat));
		final LocalDate konkubinatDate = familiensituation.getStartKonkubinat().plusYears(paramMinDauerKonkubinat).plusMonths(1);

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
	protected Double calculateGanzFamiliensituation(
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

	public List<VerfuegungZeitabschnitt> createInitialenFamilienAbzug(Gesuch gesuch) {
		VerfuegungZeitabschnitt initialFamAbzug =
			createZeitabschnittWithinValidityPeriodOfRule(gesuch.getGesuchsperiode().getGueltigkeit());
		//initial gilt die Familiengroesse die am letzten Tag vor dem Start der neuen Gesuchsperiode vorhanden war
		Double famGrBeruecksichtigungAbzug = 0.0;
		Integer famGrAnzahlPersonen = 0;

		if (gesuch.getGesuchsperiode() != null) {
			Map.Entry<Double, Integer> famGr =
				calculateFamiliengroesse(gesuch, gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
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
	 * This method will check if date is before the beginning of the Gesuchsperiode. In that case the beginning of
	 * the Gesuchsperiode will be returned. In case not, the date itself will be returned. If the passed date is null
	 * then null will be returned regardless of the comparisson between boths dates.
	 */
	@Nullable
	protected LocalDate getRelevantDateForKinder(@NotNull Gesuchsperiode gesuchsperiode, @Nullable LocalDate date) {
		LocalDate dateToCompare = null;
		if (date != null) {
			dateToCompare = (date.isBefore(gesuchsperiode.getGueltigkeit().getGueltigAb()))
				? gesuchsperiode.getGueltigkeit().getGueltigAb() : date;
		}
		return dateToCompare;
	}

	public boolean isMinDauerKonkubinatErreicht(
		Familiensituation familiensituation,
		LocalDate dateToCompare) {

		if (EnumFamilienstatus.KONKUBINAT_KEIN_KIND != familiensituation.getFamilienstatus()) {
			return false;
		}

		return isKonkubinatMinReached(familiensituation, dateToCompare);
	}

	private boolean isKonkubinatMinReached(Familiensituation familiensituation, LocalDate dateToCompare) {
		assert EnumFamilienstatus.KONKUBINAT_KEIN_KIND == familiensituation.getFamilienstatus();
		assert familiensituation.getStartKonkubinat() != null;

		LocalDate dateKonkubinatMinDauerReached = familiensituation.getStartKonkubinat().plusYears(paramMinDauerKonkubinat);
		//min date is reached when datetocompare isAfter dateKonkubinatMinDauerReached
		return dateToCompare.isAfter(dateKonkubinatMinDauerReached);
	}

	protected abstract BigDecimal calculateAbzugAufgrundFamiliengroesse(double famGrBeruecksichtigungAbzug, int famGrAnzahlPersonen);

	protected abstract Map.Entry<Double, Integer> addAbzugFromKinder(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate stichtag,
		@Nonnull Double famGrBeruecksichtigungAbzug,
		int famGrAnzahlPersonen
	);
}
