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
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		BigDecimal pauschalabzugProPersonFamiliengroesse6) {
		super(RuleKey.FAMILIENSITUATION, RuleType.GRUNDREGEL_DATA, validityPeriod);
		this.pauschalabzugProPersonFamiliengroesse3 = pauschalabzugProPersonFamiliengroesse3;
		this.pauschalabzugProPersonFamiliengroesse4 = pauschalabzugProPersonFamiliengroesse4;
		this.pauschalabzugProPersonFamiliengroesse5 = pauschalabzugProPersonFamiliengroesse5;
		this.pauschalabzugProPersonFamiliengroesse6 = pauschalabzugProPersonFamiliengroesse6;
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull Betreuung betreuung) {

		Gesuch gesuch = betreuung.extractGesuch();
		final List<VerfuegungZeitabschnitt> familienAbzugZeitabschnitt = createInitialenFamilienAbzug(gesuch);

		Map<LocalDate, Map.Entry<Double, Integer>> famGrMap = new TreeMap<>();

		//Suchen aller Geburtstage innerhalb der Gesuchsperiode und speichern in der Liste mit Familiengrösse
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			final LocalDate geburtsdatum = kindContainer.getKindJA().getGeburtsdatum();
			if (gesuch.getGesuchsperiode().getGueltigkeit().contains(geburtsdatum)) {
				final LocalDate beginMonatNachGeb = geburtsdatum.plusMonths(1).withDayOfMonth(1);
				famGrMap.put(beginMonatNachGeb, calculateFamiliengroesse(gesuch, beginMonatNachGeb));
			}
		}

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		if (familiensituation != null && familiensituation.getAenderungPer() != null) {
			// die familiensituation aendert sich jetzt erst ab dem naechsten Monat, deswegen .plusMonths(1).withDayOfMonth(1)
			final LocalDate aenderungPerBeginningNextMonth = familiensituation.getAenderungPer().plusMonths(1).withDayOfMonth(1);
			famGrMap.put(aenderungPerBeginningNextMonth, calculateFamiliengroesse(gesuch, aenderungPerBeginningNextMonth));
		}

		// aufsteigend durch die Geburtstage gehen und immer den letzen Abschnitt  unterteilen in zwei Abschnitte
		for (Map.Entry<LocalDate, Map.Entry<Double, Integer>> entry : famGrMap.entrySet()) {
			final VerfuegungZeitabschnitt lastVerfuegungZeitabschnitt = familienAbzugZeitabschnitt.get(familienAbzugZeitabschnitt.size() - 1);
			lastVerfuegungZeitabschnitt.getGueltigkeit().setGueltigBis(entry.getKey().minusDays(1));

			final VerfuegungZeitabschnitt verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
			verfuegungZeitabschnitt.getGueltigkeit().setGueltigAb(entry.getKey());
			verfuegungZeitabschnitt.getGueltigkeit().setGueltigBis(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis());
			verfuegungZeitabschnitt.setAbzugFamGroesse(calculateAbzugAufgrundFamiliengroesse(entry.getValue().getKey(), entry.getValue().getValue()));
			verfuegungZeitabschnitt.setFamGroesse(new BigDecimal(String.valueOf(entry.getValue().getKey())));

			familienAbzugZeitabschnitt.add(verfuegungZeitabschnitt);
		}

		return familienAbzugZeitabschnitt;
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
		initialFamAbzug.setAbzugFamGroesse(abzugAufgrundFamiliengroesse);
		initialFamAbzug.setFamGroesse(new BigDecimal(String.valueOf(famGrBeruecksichtigungAbzug)));

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
	Map.Entry<Double, Integer> calculateFamiliengroesse(Gesuch gesuch, @Nullable LocalDate date) {

		Double famGrBeruecksichtigungAbzug = 0.0;
		Integer famGrAnzahlPersonen = 0;
		if (gesuch != null) {

			if (gesuch.extractFamiliensituation() != null) { // wenn die Familiensituation nicht vorhanden ist, kann man nichts machen (die Daten wurden falsch eingegeben)
				if (gesuch.extractFamiliensituationErstgesuch() != null && date != null && (
					gesuch.extractFamiliensituation().getAenderungPer() == null //wenn aenderung per nicht gesetzt ist nehmen wir wert aus erstgesuch
						|| date.isBefore(gesuch.extractFamiliensituation().getAenderungPer().plusMonths(1).withDayOfMonth(1)))) {

					famGrBeruecksichtigungAbzug = famGrBeruecksichtigungAbzug + (gesuch.extractFamiliensituationErstgesuch().hasSecondGesuchsteller() ? 2 : 1);
				} else {
					famGrBeruecksichtigungAbzug = famGrBeruecksichtigungAbzug + (gesuch.extractFamiliensituation().hasSecondGesuchsteller() ? 2 : 1);
				}
			} else {
				LOG.warn("Die Familiengroesse kann noch nicht richtig berechnet werden weil die Familiensituation nicht richtig ausgefuellt ist. Antragnummer: {}", gesuch.getJahrFallAndGemeindenummer());
			}
			// es gibt keine 'halben' Eltern, deswegen sind die Werte hier gleich.
			famGrAnzahlPersonen = famGrBeruecksichtigungAbzug.intValue();

			LocalDate dateToCompare = getRelevantDateForKinder(gesuch.getGesuchsperiode(), date);

			for (KindContainer kindContainer : gesuch.getKindContainers()) {
				if (kindContainer.getKindJA() != null && (dateToCompare == null || kindContainer.getKindJA().getGeburtsdatum().isBefore(dateToCompare))) {
					if (kindContainer.getKindJA().getKinderabzug() == Kinderabzug.HALBER_ABZUG) {
						famGrBeruecksichtigungAbzug += 0.5;
						famGrAnzahlPersonen++;
					} else if (kindContainer.getKindJA().getKinderabzug() == Kinderabzug.GANZER_ABZUG) {
						famGrBeruecksichtigungAbzug++;
						famGrAnzahlPersonen++;
					}
				}
			}
		}
		return new AbstractMap.SimpleEntry(famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
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
