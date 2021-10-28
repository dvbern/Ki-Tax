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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;
import static java.util.Objects.requireNonNull;

/**
 * Berechnet die hoehe des ErwerbspensumRule eines bestimmten Erwerbspensums
 * Diese Rule muss immer am Anfang kommen, d.h. sie setzt den initialen Anspruch
 * Die weiteren Rules müssen diesen Wert gegebenenfalls korrigieren.
 * ACHTUNG! Diese Regel gilt nur fuer Angebote vom Typ isAngebotJugendamtKleinkind
 * Verweis 16.9.2
 */
public abstract class ErwerbspensumCalcRule extends AbstractCalcRule {

	private final int minErwerbspensumNichtEingeschult;
	private final int minErwerbspensumEingeschult;

	protected ErwerbspensumCalcRule(
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		int minErwerbspensumNichtEingeschult,
		int minErwerbspensumEingeschult,
		@Nonnull Locale locale
	) {
		super(RuleKey.ERWERBSPENSUM, RuleType.GRUNDREGEL_CALC, ruleValidity, validityPeriod, locale);
		this.minErwerbspensumNichtEingeschult = minErwerbspensumNichtEingeschult;
		this.minErwerbspensumEingeschult = minErwerbspensumEingeschult;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		requireNonNull(platz.extractGesuch(), "Gesuch muss gesetzt sein");
		requireNonNull(platz.extractGesuch().extractFamiliensituation(), "Familiensituation muss gesetzt sein");
		boolean hasSecondGesuchsteller = hasSecondGSForZeit(platz, inputData.getParent().getGueltigkeit());
		int erwerbspensumOffset = hasSecondGesuchsteller ? 100 : 0;
		// Erwerbspensum ist immer die erste Rule, d.h. es wird das Erwerbspensum mal als Anspruch angenommen
		// Das Erwerbspensum muss PRO GESUCHSTELLER auf 100% limitiert werden
		Integer erwerbspensum1 = calculateErwerbspensumGS1(inputData, getLocale());
		Integer erwerbspensum2 = 0;
		if (hasSecondGesuchsteller) {
			erwerbspensum2 = calculateErwerbspensumGS2(inputData, getLocale());
		}
		int anspruch = erwerbspensum1 + erwerbspensum2 - erwerbspensumOffset;
		int minimum = getMinimumErwerbspensum(platz);
		int roundedAnspruch = checkAndRoundAnspruch(inputData, anspruch, minimum, erwerbspensumOffset, getLocale());
		inputData.setAnspruchspensumProzent(roundedAnspruch);
		inputData.setMinimalErforderlichesPensum(minimum);
	}

	private int getMinimumErwerbspensum(@Nonnull AbstractPlatz betreuung) {
		EinschulungTyp einschulungTyp = betreuung.getKind().getKindJA().getEinschulungTyp();
		Objects.requireNonNull(einschulungTyp);
		int mininum = einschulungTyp.isEingeschult() ? minErwerbspensumEingeschult : minErwerbspensumNichtEingeschult;
		return mininum;
	}

	/**
	 * Sollte der Anspruch weniger als 0 sein, wird dieser auf 0 gesetzt und eine Bemerkung eingefuegt.
	 * Wenn der Anspruch groesser als 100 ist, wird dieser auf 100 gesetzt. Hier braucht es keine Bemerkung, denn sie
	 * wurde bereits in calculateErwerbspensum eingefuegt.
	 * Am Ende wird der Wert gerundet und zurueckgegeben
	 */
	private int checkAndRoundAnspruch(
		@Nonnull BGCalculationInput inputData,
		int anspruch,
		int minimum,
		int erwerbspensumOffset,
		@Nonnull Locale locale
	) {
		if (anspruch <= 0) {
			anspruch = 0;
			inputData.setMinimalesEwpUnterschritten(true);
			inputData.setKategorieKeinPensum(true);
		}
		// Minimum pruefen
		if (anspruch < minimum) {
			anspruch = 0;
			// Falls schon eine Bemerkung mit einem eventuell anderen Minimum erstellt wurde, dieses zuerst entfernen
			inputData.getParent().getBemerkungenList().removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH);
			// Fuer die Bemerkung muss das Minimum fuer 2 GS 100 + x betragen!
			inputData.addBemerkung(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH, locale, minimum + erwerbspensumOffset);
			inputData.setMinimalesEwpUnterschritten(true);
		} else {
			// Wir haben das Minimum erreicht. Der Anspruch wird daher um den Default-Zuschlag erhöht
			if (inputData.getErwerbspensumZuschlag() != null) {
				anspruch += inputData.getErwerbspensumZuschlag();
			}
			// Es wird eine Default-Bemerkung hinzugefügt, welche sagt, weswegen ein Anspruch besteht
			addVerfuegungsBemerkung(inputData);
			// Falls durch eine vorherige Erwerbspensum-Regel bereits auf KEIN-ANSPRUCH gesetzt war, muss sowohl
			// das Flag wie auch die Bemerkung zurueckgesetzt werden (umgekehrt kann es nicht vorkommen)
			inputData.setMinimalesEwpUnterschritten(false);
			inputData.getParent().getBemerkungenList().removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH);
		}
		if (anspruch > 100) { // das Ergebniss darf nie mehr als 100 sein
			anspruch = 100;
		}
		// Der Anspruch wird immer auf 5-er Schritten gerundet.
		return MathUtil.roundIntToFives(anspruch);
	}

	protected void addVerfuegungsBemerkung(@Nonnull BGCalculationInput inputData) {
		String vorhandeneBeschaeftigungen = getBeschaeftigungsTypen(inputData, getLocale());
		inputData.addBemerkung(MsgKey.ERWERBSPENSUM_ANSPRUCH, getLocale(), vorhandeneBeschaeftigungen);
	}

	@Nonnull
	private Integer calculateErwerbspensumGS1(
		@Nonnull BGCalculationInput inputData,
		@Nonnull Locale locale
	) {
		Integer erwerbspensum = inputData.getErwerbspensumGS1() != null ? inputData.getErwerbspensumGS1() : 0;
		return calculateErwerbspensum(inputData, erwerbspensum, MsgKey.ERWERBSPENSUM_GS1_MSG, locale);
	}

	@Nonnull
	private Integer calculateErwerbspensumGS2(
		@Nonnull BGCalculationInput inputData,
		@Nonnull Locale locale
	) {
		Integer erwerbspensum = inputData.getErwerbspensumGS2() != null ? inputData.getErwerbspensumGS2() : 0;
		return calculateErwerbspensum(inputData, erwerbspensum, MsgKey.ERWERBSPENSUM_GS2_MSG, locale);
	}

	@Nonnull
	private Integer calculateErwerbspensum(@Nonnull BGCalculationInput inputData, @Nonnull Integer erwerbspensum, @Nonnull MsgKey bemerkung,
		@Nonnull Locale locale) {
		if (erwerbspensum > 100) {
			erwerbspensum = 100;
			inputData.addBemerkung(bemerkung, locale);
		}
		return erwerbspensum;
	}

	/**
	 * Monat Rule, der GS2 ist nach aenderung die Famsit ab Anfang naechste Monat erst berucksichtig
	 * @param betreuung
	 * @param gueltigkeit
	 * @return
	 */
	private boolean hasSecondGSForZeit(@Nonnull AbstractPlatz betreuung, @Nonnull DateRange gueltigkeit) {
		final Gesuch gesuch = betreuung.extractGesuch();
		final Familiensituation familiensituation = requireNonNull(gesuch.extractFamiliensituation());
		final Familiensituation familiensituationErstGesuch = gesuch.extractFamiliensituationErstgesuch();

		LocalDate familiensituationGueltigAb = familiensituation.getAenderungPer();
		if (familiensituationGueltigAb != null
			&& familiensituationErstGesuch != null
			&& gueltigkeit.getGueltigAb().isBefore(familiensituationGueltigAb.plusMonths(1).withDayOfMonth(1))) {
				return familiensituationErstGesuch.hasSecondGesuchsteller(gueltigkeit.getGueltigBis());
		}
		return familiensituation.hasSecondGesuchsteller(gueltigkeit.getGueltigBis());
	}

	@Nonnull
	private String getBeschaeftigungsTypen(@Nonnull BGCalculationInput inputData, @Nonnull Locale locale) {
		StringBuilder sb = new StringBuilder();
		for (Taetigkeit taetigkeit : inputData.getTaetigkeiten()) {
			sb.append(ServerMessageUtil.translateEnumValue(taetigkeit, locale));
			sb.append(", ");
		}
		// Das letzte Komma entfernen
		String taetigkeitenAsString = sb.toString();
		taetigkeitenAsString = StringUtils.removeEnd(taetigkeitenAsString, ", ");
		return taetigkeitenAsString;
	}
}
