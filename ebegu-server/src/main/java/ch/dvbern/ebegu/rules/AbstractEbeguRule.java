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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.RuleUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This defines a Rule that has a unique Name given by RuleKey. The Rule is valid for a specified validityPeriod and
 * is of a given type
 */
public abstract class AbstractEbeguRule implements Rule {

	private RuleValidity ruleValidity;

	/**
	 * This is the name of the Rule, Can be used to create messages etc.
	 */
	private final RuleKey ruleKey;

	private final RuleType ruleType;

	// language in which the Rules will be applied. Used normally for translating bemerkungen
	private final Locale locale;

	@Valid
	private final DateRange validityPeriod;

	protected AbstractEbeguRule(
		@Nonnull RuleKey ruleKey,
		@Nonnull RuleType ruleType,
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		this.ruleKey = ruleKey;
		this.ruleType = ruleType;
		this.ruleValidity = ruleValidity;
		this.validityPeriod = new DateRange(validityPeriod);
		this.locale = locale;
	}

	@Override
	@Nonnull
	public LocalDate validFrom() {
		return validityPeriod.getGueltigAb();
	}

	@Override
	@Nonnull
	public LocalDate validTo() {
		return validityPeriod.getGueltigBis();
	}

	@Nonnull
	@Override
	public DateRange validityPeriod() {
		return validityPeriod;
	}

	@Override
	public boolean isValid(@Nonnull DateRange dateRange) {
		return validityPeriod.getOverlap(dateRange).isPresent();
	}

	@Override
	@Nonnull
	public RuleType getRuleType() {
		return ruleType;
	}

	@Override
	@Nonnull
	public RuleKey getRuleKey() {
		return ruleKey;
	}

	public Locale getLocale() {
		return locale;
	}

	/**
	 * Stellt die Zeitabschnitte der aktuellen Rule zusammen, falls die Rule für den
	 * aktuellen Betreuungstyp relevant ist
	 */
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitteIfApplicable(@Nonnull AbstractPlatz platz) {
		if (isAnwendbarForAngebot(platz)) {
			// Nach jeder AbschnittRule erhalten wir die *neuen* Zeitabschnitte zurueck. Diese müssen bei ASIV-Regeln
			// immer eine identische ASIV und GEMEINDE-Berechnung haben!
			List<VerfuegungZeitabschnitt> zwischenresultate = createVerfuegungsZeitabschnitte(platz);
			// Wir duerfen nur neue Abschnitte verwenden, welche ueberhaupt gueltig sind
			for (VerfuegungZeitabschnitt zeitabschnitt : zwischenresultate) {
				validateZeitabschnittGueltigkeit(zeitabschnitt);
			}
			// Wenn es eine ASIV Rule ist, gilt sie fuer die Gemeinde genau gleich, die Ergebnisse (nur
			// genau dieser Rule) muessen identisch sein
			if (RuleValidity.ASIV == ruleValidity) {
				assertSimilarAsivAndGemeindeInputs(zwischenresultate);
			}
			return zwischenresultate;
		}
		return new ArrayList<>();
	}

	protected void validateZeitabschnittGueltigkeit(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		boolean valid = true;
		if (zeitabschnitt.getGueltigkeit().startsBefore(this.validityPeriod)) {
			valid = false;
		}
		if (zeitabschnitt.getGueltigkeit().endsAfter(this.validityPeriod)) {
			valid = false;
		}
		if (!valid) {
			String msg =
				"Regel " + this.getClass().getSimpleName() + " has invalid Zeitabschnitte. Rule " +
					this.validityPeriod.toRangeString() + ", Abschnitt: " +
					zeitabschnitt.getGueltigkeit().toRangeString();
			throw new EbeguRuntimeException("validateZeitabschnittGueltigkeit", msg);
		}
	}

	private void assertSimilarAsivAndGemeindeInputs(@Nonnull Collection<VerfuegungZeitabschnitt> zeitabschnitte) {
		boolean hasSameInputAsivAndGemeinde = zeitabschnitte.stream()
			.allMatch(z -> z.getBgCalculationInputAsiv().isSame(z.getBgCalculationInputGemeinde()));

		if (!hasSameInputAsivAndGemeinde) {
			throw new EbeguRuntimeException(
				"createVerfuegungsZeitabschnitteIfApplicable",
				"ASIV Rule setzt nicht beide Input-Objekte!");
		}
	}

	/**
	 * Zuerst muessen die neuen Zeitabschnitte aus den Daten der aktuellen Rule zusammengestellt werden:
	 */
	@Nonnull
	abstract List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz);

	/**
	 * Führt die aktuelle Rule aus, falls die Rule für den
	 * aktuellen Betreuungstyp relevant ist
	 */
	protected void executeRuleIfApplicable(@Nonnull AbstractPlatz platz, @Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		if (isAnwendbarForAngebot(platz) && isValid(verfuegungZeitabschnitt.getGueltigkeit())) {
			for (BGCalculationInput inputDatum : getInputData(verfuegungZeitabschnitt)) {
				executeRule(platz, inputDatum);
				if (ruleValidity == RuleValidity.GEMEINDE) {
					// Wir verlassen uns hier darauf, dass GEMEINDE-Rules nur dann verwendet werden,
					// wenn sich die Berechnung tatsaechlich von ASIV unterscheidet. Siehe auch
					// ch.dvbern.ebegu.rules.BetreuungsgutscheinConfigurator
					inputDatum.getParent().setHasGemeindeSpezifischeBerechnung(true);
				}
			}
		}
	}

	/**
	 * Fuehrt die eigentliche Rule auf einem einzelnen Zeitabschnitt aus.
	 * Hier kann man davon ausgehen, dass die Zeitabschnitte schon validiert und gemergt sind.
	 */
	abstract void executeRule(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData);

	/**
	 * Hauptmethode der Regelberechnung. Diese wird von Aussen aufgerufen
	 */
	@Nonnull
	@Override
	public final List<VerfuegungZeitabschnitt> calculate(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		if (!isAnwendbarForAngebot(platz)) {
			return zeitabschnitte;
		}

		Collections.sort(zeitabschnitte);

		// Zuerst muessen die neuen Zeitabschnitte aus den Daten meiner Rule zusammengestellt werden:

		List<VerfuegungZeitabschnitt> abschnitteCreatedInRule = createVerfuegungsZeitabschnitteIfApplicable(platz);
		Collections.sort(abschnitteCreatedInRule);

		// In dieser Funktion muss sichergestellt werden, dass in der neuen Liste keine Ueberschneidungen mehr bestehen
		// Jetzt muessen diese mit den bestehenden Zeitabschnitten aus früheren Rules gemergt werden
		List<VerfuegungZeitabschnitt> mergedZeitabschnitte = mergeZeitabschnitte(zeitabschnitte, abschnitteCreatedInRule);
		Collections.sort(mergedZeitabschnitte);

		// Die Zeitabschnitte (jetzt ohne Überschneidungen) normalisieren:
		// - Muss innerhalb Gesuchsperiode sein
		// - Müssen sich unterscheiden (d.h. 20+20 vs 40 soll nur einen Schnitz geben)
		Gesuchsperiode gesuchsperiode = platz.extractGesuchsperiode();
		List<VerfuegungZeitabschnitt> normalizedZeitabschn = normalizeZeitabschnitte(mergedZeitabschnitte,
			gesuchsperiode);

		// Die eigentliche Rule anwenden
		for (VerfuegungZeitabschnitt zeitabschnitt : normalizedZeitabschn) {
			executeRuleIfApplicable(platz, zeitabschnitt);
		}
		return normalizedZeitabschn;
	}

	/**
	 *
	 * @param platz (Betreuung, Tageschhulplatz etc)
	 * @return true wenn die Regel anwendbar ist
	 */
	private boolean isAnwendbarForAngebot(@Nonnull AbstractPlatz platz) {
		Objects.requireNonNull(platz);
		Objects.requireNonNull(platz.getBetreuungsangebotTyp());
		return getAnwendbareAngebote().contains(platz.getBetreuungsangebotTyp());
	}

	protected abstract List<BetreuungsangebotTyp> getAnwendbareAngebote();

	/**
	 * Prüft, dass die Zeitabschnitte innerhalb der Gesuchperiode liegen (und kürzt sie falls nötig bzw. lässt
	 * Zeitschnitze weg, welche ganz ausserhalb der Periode liegen)
	 * Stellt ausserdem sicher, dass zwei aufeinander folgende Zeitabschnitte nie dieselben Daten haben. Falls
	 * dies der Fall wäre, werden sie zu einem neuen Schnitz gemergt.
	 */
	@Nonnull
	protected List<VerfuegungZeitabschnitt> normalizeZeitabschnitte(@Nonnull List<VerfuegungZeitabschnitt> mergedZeitabschnitte, @Nonnull Gesuchsperiode gesuchsperiode) {
		List<VerfuegungZeitabschnitt> normalizedZeitabschnitte = new LinkedList<>();
		for (VerfuegungZeitabschnitt zeitabschnitt : mergedZeitabschnitte) {
			// Zuerst überprüfen, ob der Zeitabschnitt innerhalb der Gesuchsperiode liegt
			boolean startsBefore = zeitabschnitt.getGueltigkeit().startsBefore(gesuchsperiode.getGueltigkeit());
			boolean endsAfter = zeitabschnitt.getGueltigkeit().endsAfter(gesuchsperiode.getGueltigkeit());
			if (startsBefore || endsAfter) {
				boolean zeitabschnittInPeriode = false;
				if (startsBefore && zeitabschnitt.getGueltigkeit().getGueltigBis().isAfter(gesuchsperiode.getGueltigkeit().getGueltigAb())) {
					// Datum Von liegt vor der Periode
					// Falls Datum Bis ebenfalls vor der Periode liegt, kann der Abschnitt gelöscht werden, ansonsten muss er verkürzt werden
					zeitabschnitt.getGueltigkeit().setGueltigAb(gesuchsperiode.getGueltigkeit().getGueltigAb());
					zeitabschnittInPeriode = true;
				}
				if (endsAfter && zeitabschnitt.getGueltigkeit().getGueltigAb().isBefore(gesuchsperiode.getGueltigkeit().getGueltigBis())) {
					// Datum Bis liegt nach der Periode
					// Falls Datum Von auch schon nach der Periode lag, kann der Abschnitt gelöscht werden, ansonsten muss er verkürzt werden
					zeitabschnitt.getGueltigkeit().setGueltigBis(gesuchsperiode.getGueltigkeit().getGueltigBis());
					zeitabschnittInPeriode = true;
				}
				if (zeitabschnittInPeriode) {
					addToNormalizedZeitabschnitte(normalizedZeitabschnitte, zeitabschnitt);
				}
			} else {
				addToNormalizedZeitabschnitte(normalizedZeitabschnitte, zeitabschnitt);
			}
		}
		return normalizedZeitabschnitte;
	}

	/**
	 * Stellt sicher, dass zwei aufeinander folgende Zeitabschnitte nie dieselben Daten haben. Falls
	 * dies der Fall wäre, werden sie zu einem neuen Schnitz gemergt.
	 */
	private void addToNormalizedZeitabschnitte(@Nonnull List<VerfuegungZeitabschnitt> validZeitabschnitte, @Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		// Zuerst vergleichen, ob sich der neue Zeitabschnitt vom letzt hinzugefügten (und angrenzenden) unterscheidet
		int indexOfLast = validZeitabschnitte.size() - 1;
		if (indexOfLast >= 0) {
			VerfuegungZeitabschnitt lastZeitabschnitt = validZeitabschnitte.get(indexOfLast);
			if (lastZeitabschnitt.isSame(zeitabschnitt) && zeitabschnitt.getGueltigkeit().startsDayAfter(lastZeitabschnitt.getGueltigkeit())) {
				// Gleiche Berechnungsgrundlagen: Den alten um den neuen verlängern
				lastZeitabschnitt.getGueltigkeit().setGueltigBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
				// Die Bemerkungen hinzufügen
				lastZeitabschnitt.getBemerkungenDTOList().addAllBemerkungen(zeitabschnitt.getBemerkungenDTOList());
				validZeitabschnitte.remove(indexOfLast);
				validZeitabschnitte.add(lastZeitabschnitt);
			} else {
				// Unterschiedliche Daten -> hinzufügen
				validZeitabschnitte.add(zeitabschnitt);
			}
		} else {
			// Erster Eintrag -> hinzufügen
			validZeitabschnitte.add(zeitabschnitt);
		}
	}

	/**
	 * Mergt zwei Listen von Verfuegungszeitschnitten.
	 */
	@Nonnull
	private List<VerfuegungZeitabschnitt> mergeZeitabschnitte(@Nonnull List<VerfuegungZeitabschnitt> bestehendeEntities, @Nonnull List<VerfuegungZeitabschnitt> neueEntities) {
		List<VerfuegungZeitabschnitt> alles = new ArrayList<>();
		alles.addAll(bestehendeEntities);
		alles.addAll(neueEntities);
		return mergeZeitabschnitte(alles);
	}

	/**
	 * Erstellt aus der übergebenen Liste von VerfuegungsZeitabschnitten eine neue Liste, die keine Überschneidungen mehr
	 * enthält. Überschneiden sich zwei Entitäten in der Ursprungsliste, so werden daraus drei Zeiträume erstellt:
	 * <pre>
	 * |------------------------|
	 * 40
	 * 	           |-------------------------------------|
	 * 			   60
	 * ergibt:
	 * |-----------|------------|------------------------|
	 * 40          100                60
	 * </pre>
	 */
	@Nonnull
	protected List<VerfuegungZeitabschnitt> mergeZeitabschnitte(@Nonnull List<VerfuegungZeitabschnitt> entitiesToMerge) {
		List<VerfuegungZeitabschnitt> result = new ArrayList<>();
		Set<LocalDate> setOfPotentialZeitraumGrenzen = createSetOfPotentialZeitraumGrenzen(entitiesToMerge);
		if (setOfPotentialZeitraumGrenzen.isEmpty()) {
			return result;
		}
		Iterator<LocalDate> iterator = setOfPotentialZeitraumGrenzen.iterator();
		LocalDate datumVon = iterator.next();
		while (iterator.hasNext()) {
			LocalDate datumBis = iterator.next().minusDays(1);   //wir haben bei den Bis Daten jeweils einen Tag hinzugefuegt
			VerfuegungZeitabschnitt mergedZeitabschnitt = new VerfuegungZeitabschnitt(new DateRange(datumVon, datumBis));
			// Alle Zeitabschnitte suchen, die mit  diesem Range ueberlappen
			boolean foundOverlapping = false;
			for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : entitiesToMerge) {
				Optional<DateRange> optionalOverlap = verfuegungZeitabschnitt.getGueltigkeit().getOverlap(mergedZeitabschnitt.getGueltigkeit());
				if (optionalOverlap.isPresent()) {
					mergedZeitabschnitt.add(verfuegungZeitabschnitt); //Zeitabschnitt hinzumergen
					foundOverlapping = true;
				}
			}
			if (foundOverlapping) {
				result.add(mergedZeitabschnitt);
			}
			datumVon = datumBis.plusDays(1); //naechstes vondatum
		}
		return result;
	}

	/**
	 * Sammelt alle Start und Enddaten aus der Liste der Zeitabschnitte zusammen.
	 * Dabei ist es so, dass das bis Datum der Zeitabschnitte inklusiv - inklusiv ist. Wir moechten aber die Daten jeweils
	 * inklusv - exklusive enddatum. Daher wird zum endtaum jeweils ein Tag hinzugezaehlt
	 *
	 * @param entitiesUnmerged liste der Abschnitte
	 * @return Liste aller Dates die potentiell als Zeitraumgrenze dienen werden
	 */
	private Set<LocalDate> createSetOfPotentialZeitraumGrenzen(@Nonnull List<VerfuegungZeitabschnitt> entitiesUnmerged) {
		Set<LocalDate> setOfDates = new TreeSet<>();
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : entitiesUnmerged) {
			setOfDates.add(verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb());
			setOfDates.add(verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().plusDays(1));
		}
		return setOfDates;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("ruleKey", ruleKey)
			.append("ruleType", ruleType)
			.append("validityPeriod", validityPeriod)
			.toString();
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return false;
	}

	/**
	 * Berechnet das Datum, ab wann eine Regel aufgrund es übergebenen Datums angewendet werden soll.
	 * Aktuell ist dies der erste Tag des Folgemonats. Auch bei Ereignis am 1. wird der 1. des Folgemonats genommen.
	 * Achtung, dieser Stichtag kommt nicht zwingend schlussendlich zum Einsatz, z.B. bei verspäteter Einreichung
	 * des Gesuchs.
	 */
	@Nonnull
	public LocalDate getStichtagForEreignis(@Nonnull LocalDate ereignisdatum) {
		return RuleUtil.getStichtagForEreignis(ereignisdatum);
	}

	/**
	 * Gibt eine Liste von Input-Objekten zurück, welche in CalcRules berechnet werden sollen: Wenn Typ ASIV
	 * muss ASIV *und* Gemeinde berechnet werden, sonst nur Gemeinde.
	 */
	@Nonnull
	private List<BGCalculationInput> getInputData(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		if (this.ruleValidity == RuleValidity.ASIV) {
			return Arrays.asList(zeitabschnitt.getBgCalculationInputGemeinde(), zeitabschnitt.getBgCalculationInputAsiv());
		}

		return Collections.singletonList(zeitabschnitt.getBgCalculationInputGemeinde());
	}

	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		// Grundsaetzlich gehen wir davon aus, dass jede Regel fuer jede Gemeinde gueltig ist.
		// Ausnahme sind Regeln mit RuleValiditiy=GEMEINDE, fuer welche eine Einstellung gleich
		// ist (bzw. nicht ueberschrieben) wie bei ASIV
		if (RuleValidity.GEMEINDE == ruleValidity) {
			throw new EbeguRuntimeException("isRelevantForGemeinde",
				"Rule mit validity GEMEINDE muessen isRelevantForGemeinde ueberschreiben! " + this.getClass().getName());
		}
		return true;
	}
}
