/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static ch.dvbern.ebegu.enums.EinstellungKey.KINDERABZUG_TYP;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;


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
public class FamilienabzugAbschnittRuleBern extends AbstractFamilienabzugAbschnittRule {

	private static final Logger LOG = LoggerFactory.getLogger(FamilienabzugAbschnittRuleBern.class);

	private final BigDecimal pauschalabzugProPersonFamiliengroesse3;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse4;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse5;
	private final BigDecimal pauschalabzugProPersonFamiliengroesse6;
	private final KinderabzugTyp kinderabzugTyp;


	public FamilienabzugAbschnittRuleBern(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap,
		DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(einstellungMap, validityPeriod, locale);
		this.pauschalabzugProPersonFamiliengroesse3 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3).getValueAsBigDecimal();
		this.pauschalabzugProPersonFamiliengroesse4 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4).getValueAsBigDecimal();
		this.pauschalabzugProPersonFamiliengroesse5 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5).getValueAsBigDecimal();
		this.pauschalabzugProPersonFamiliengroesse6 = einstellungMap.get(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6).getValueAsBigDecimal();
		this.kinderabzugTyp = KinderabzugTyp.valueOf(einstellungMap.get(KINDERABZUG_TYP).getValue());
	}


	protected Map.Entry<Double, Integer> addAbzugFromKinder(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate stichtag,
		@Nonnull Double famGrBeruecksichtigungAbzug,
		int famGrAnzahlPersonen
	) {
		if (this.kinderabzugTyp.isFKJV()) {
			boolean isKinderabzugTypV2 = this.kinderabzugTyp == KinderabzugTyp.FKJV_2;
			return addAbzugFromKinderFkjv(gesuch, stichtag, famGrBeruecksichtigungAbzug, famGrAnzahlPersonen,
				isKinderabzugTypV2);
		}
		return addAbzugFromKinderAsiv(gesuch, stichtag, famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
	}

	private Map.Entry<Double, Integer> addAbzugFromKinderAsiv(
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
				Kinderabzug kinderabzug =
					isErstesHalbjahr ? kind.getKinderabzugErstesHalbjahr() : kind.getKinderabzugZweitesHalbjahr();
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

	private Map.Entry<Double, Integer> addAbzugFromKinderFkjv(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate stichtag,
		@Nonnull Double famGrBeruecksichtigungAbzug,
		int famGrAnzahlPersonen,
		boolean isKinderAbzugTypeVersion2
	) {
		LocalDate dateToCompare = getRelevantDateForKinder(gesuch.getGesuchsperiode(), stichtag);
		Familiensituation familiensituation = gesuch.extractFamiliensituation();

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			Kind kind = kindContainer.getKindJA();
			if (kind != null && (dateToCompare == null || kind.getGeburtsdatum().isBefore(dateToCompare))) {
				double beruecksichtigungAbzug = calculateFKJVKinderabzugForKind(kind, familiensituation, dateToCompare);
				famGrBeruecksichtigungAbzug += beruecksichtigungAbzug;
				famGrAnzahlPersonen += calculateFKJVAnzahlPersonen(beruecksichtigungAbzug, isKinderAbzugTypeVersion2);
			}
		}

		return new AbstractMap.SimpleEntry(famGrBeruecksichtigungAbzug, famGrAnzahlPersonen);
	}

	private int calculateFKJVAnzahlPersonen(double beruecksichtigungAbzug, boolean isKinderAbzugTypeVersion2) {
		// in der FKJV Periode 22/23 hatten wir einen Fehler, der die Kinder immer mitzählte, auch wenn es für ein Kind keinen
		// Kinderabzug gab.
		// dies wird auf die Periode 23/24 geändert. Kinder ohne Kinderabzug zählen nicht zur Familiengrösse und Kinder für die
		// der
		// halbe oder der ganze Pauschalabzug abgezogen werden kann, zählen ganz zur Familiengrösse
		if (!isKinderAbzugTypeVersion2) {
			return 1;
		}

		if (beruecksichtigungAbzug == 0) {
			return 0;
		}

		return 1;
	}

	private double calculateFKJVKinderabzugForKind(
		@Nonnull Kind kind,
		Familiensituation familiensituation,
		LocalDate dateToCompare) {
		if (kind.getPflegekind()) {
			Objects.requireNonNull(kind.getPflegeEntschaedigungErhalten());
			if (kind.getPflegeEntschaedigungErhalten()) {
				return 0;
			}
			return 1;
		}
		if (kind.getObhutAlternierendAusueben() != null) {
			if (!kind.getObhutAlternierendAusueben()) {
				return 1;
			}
			return calculateKinderabzugForObhutAlternierendAusueben(kind, familiensituation, dateToCompare);
		}
		if (kind.getInErstausbildung() != null) {
			return calculateKinderAbzugForInErstausbildungAnswered(kind, dateToCompare);
		}
		throw new EbeguRuntimeException("calculateFKJVKinderabzugForKind", "wrong properties for kind to calculate kinderabzug");
	}

	private Integer calculateKinderAbzugForInErstausbildungAnswered(@Nonnull Kind kind, LocalDate dateToCompare) throws EbeguRuntimeException {
		Objects.requireNonNull(kind.getInErstausbildung());
		if (!kind.getInErstausbildung()) {
			return is18GeburtstagBeforeDate(kind, dateToCompare) ? 0 : 1;
		}
		if (kind.getAlimenteBezahlen() != null) {
			if (!kind.getAlimenteBezahlen()) {
				return 0;
			}
			return is18GeburtstagBeforeDate(kind, dateToCompare) ? 1 : 0;
		}
		if (kind.getAlimenteErhalten() != null) {
			if (kind.getAlimenteErhalten()) {
				return is18GeburtstagBeforeDate(kind, dateToCompare) ? 0 : 1;
			}
			return 1;
		}
		throw new EbeguRuntimeException("calculateFKJVKinderabzugForKind", "wrong properties for kind to calculate kinderabzug");
	}

	private boolean is18GeburtstagBeforeDate(@Nonnull Kind kind, @Nonnull LocalDate date) {
		LocalDate dateWith18 = kind.getGeburtsdatum().plusYears(18);
		return dateWith18.isBefore(date);
	}

	private double calculateKinderabzugForObhutAlternierendAusueben(
		Kind kind,
		Familiensituation familiensituation,
		LocalDate dateToCompare) {
		Objects.requireNonNull(kind.getFamilienErgaenzendeBetreuung());
		Objects.requireNonNull(familiensituation);

		if (!kind.getFamilienErgaenzendeBetreuung()) {
			if (Boolean.TRUE.equals(kind.getGemeinsamesGesuch())) {
				return 1;
			}
			return 0.5;
		}

		if (isVerheiratetOrKonkubinatMitKind(familiensituation.getFamilienstatus())
			|| isMinDauerKonkubinatErreicht(familiensituation, dateToCompare)
			|| familiensituation.getGesuchstellerKardinalitaet() == EnumGesuchstellerKardinalitaet.ALLEINE) {
			return 0.5;
		}

		Objects.requireNonNull(kind.getGemeinsamesGesuch());
		if (kind.getGemeinsamesGesuch()) {
			return 1;
		}

		return 0.5;
	}

	public boolean isVerheiratetOrKonkubinatMitKind(EnumFamilienstatus familienstatus) {
		return familienstatus == EnumFamilienstatus.VERHEIRATET ||
			familienstatus == EnumFamilienstatus.KONKUBINAT;
	}

	/**
	 * Berechnete Familiengrösse (halber Abzug berücksichtigen) multipliziert mit dem ermittelten Personen-Haushalt-Pauschalabzug
	 * (Anzahl Personen in Familie)
	 *
	 * @return abzug aufgrund Familiengrösse
	 */
	public BigDecimal calculateAbzugAufgrundFamiliengroesse(double famGrBeruecksichtigungAbzug, int famGrAnzahlPersonen) {

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
