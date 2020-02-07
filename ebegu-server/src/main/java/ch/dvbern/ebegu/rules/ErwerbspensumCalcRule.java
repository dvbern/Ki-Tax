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

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
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
public class ErwerbspensumCalcRule extends AbstractCalcRule {

	private final int zuschlagErwerbspensum;
	private final int minErwerbspensumNichtEingeschult;
	private final int minErwerbspensumEingeschult;

	public ErwerbspensumCalcRule(
		DateRange validityPeriod,
		int zuschlagErwerbspensum,
		int minErwerbspensumNichtEingeschult,
		int minErwerbspensumEingeschult,
		@Nonnull Locale locale
	) {
		super(RuleKey.ERWERBSPENSUM, RuleType.GRUNDREGEL_CALC, validityPeriod, locale);
		this.zuschlagErwerbspensum = zuschlagErwerbspensum;
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
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		requireNonNull(platz.extractGesuch(), "Gesuch muss gesetzt sein");
		requireNonNull(platz.extractGesuch().extractFamiliensituation(), "Familiensituation muss gesetzt sein");
		boolean hasSecondGesuchsteller = hasSecondGSForZeit(platz, verfuegungZeitabschnitt.getGueltigkeit());
		int erwerbspensumOffset = hasSecondGesuchsteller ? 100 : 0;
		// Erwerbspensum ist immer die erste Rule, d.h. es wird das Erwerbspensum mal als Anspruch angenommen
		// Das Erwerbspensum muss PRO GESUCHSTELLER auf 100% limitiert werden
		Integer erwerbspensum1 = calculateErwerbspensumGS1(verfuegungZeitabschnitt, getLocale());
		Integer erwerbspensum2 = 0;
		if (hasSecondGesuchsteller) {
			erwerbspensum2 = calculateErwerbspensumGS2(verfuegungZeitabschnitt, getLocale());
		}
		int anspruch = erwerbspensum1 + erwerbspensum2 - erwerbspensumOffset;
		int minimum = getMinimumErwerbspensum(platz);
		int roundedAnspruch = checkAndRoundAnspruch(verfuegungZeitabschnitt, anspruch, minimum, erwerbspensumOffset, getLocale());
		verfuegungZeitabschnitt.getBgCalculationInputAsiv().setAnspruchspensumProzent(roundedAnspruch);
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
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		int anspruch,
		int minimum,
		int erwerbspensumOffset,
		@Nonnull Locale locale
	) {
		if (anspruch <= 0) {
			anspruch = 0;
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().setMinimalesEwpUnterschritten(true);
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().setKategorieKeinPensum(true);
		}
		// Minimum pruefen
		if (anspruch < minimum) {
			anspruch = 0;
			// Fuer die Bemerkung muss das Minimum fuer 2 GS 100 + x betragen!
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().addBemerkung(RuleKey.ERWERBSPENSUM, MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH, locale, minimum + erwerbspensumOffset);
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().setMinimalesEwpUnterschritten(true);
		} else {
			// Wir haben das Minimum erreicht. Der Anspruch wird daher um den Default-Zuschlag erhöht
			anspruch += zuschlagErwerbspensum;
			// Es wird eine Default-Bemerkung hinzugefügt, welche sagt, weswegen ein Anspruch besteht
			String vorhandeneBeschaeftigungen = getBeschaeftigungsTypen(verfuegungZeitabschnitt, locale);
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().addBemerkung(RuleKey.ERWERBSPENSUM, MsgKey.ERWERBSPENSUM_ANSPRUCH, locale, vorhandeneBeschaeftigungen);
		}
		if (anspruch > 100) { // das Ergebniss darf nie mehr als 100 sein
			anspruch = 100;
		}
		// Der Anspruch wird immer auf 5-er Schritten gerundet.
		return MathUtil.roundIntToFives(anspruch);
	}

	@Nonnull
	private Integer calculateErwerbspensumGS1(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull Locale locale
	) {
		Integer erwerbspensum = verfuegungZeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1() != null ? verfuegungZeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS1() : 0;
		return calculateErwerbspensum(verfuegungZeitabschnitt, erwerbspensum, MsgKey.ERWERBSPENSUM_GS1_MSG, locale);
	}

	@Nonnull
	private Integer calculateErwerbspensumGS2(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull Locale locale
	) {
		Integer erwerbspensum = verfuegungZeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS2() != null ? verfuegungZeitabschnitt.getBgCalculationInputAsiv().getErwerbspensumGS2() : 0;
		return calculateErwerbspensum(verfuegungZeitabschnitt, erwerbspensum, MsgKey.ERWERBSPENSUM_GS2_MSG, locale);
	}

	@Nonnull
	private Integer calculateErwerbspensum(@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt, @Nonnull Integer erwerbspensum, @Nonnull MsgKey bemerkung,
		@Nonnull Locale locale) {
		if (erwerbspensum > 100) {
			erwerbspensum = 100;
			verfuegungZeitabschnitt.getBgCalculationInputAsiv().addBemerkung(RuleKey.ERWERBSPENSUM, bemerkung, locale);
		}
		return erwerbspensum;
	}

	private boolean hasSecondGSForZeit(@Nonnull AbstractPlatz betreuung, @Nonnull DateRange gueltigkeit) {
		final Gesuch gesuch = betreuung.extractGesuch();
		final Familiensituation familiensituation = requireNonNull(gesuch.extractFamiliensituation());
		final Familiensituation familiensituationErstGesuch = gesuch.extractFamiliensituationErstgesuch();

		LocalDate familiensituationGueltigAb = familiensituation.getAenderungPer();
		if (familiensituationGueltigAb != null
			&& familiensituationErstGesuch != null
			&& gueltigkeit.getGueltigAb().isBefore(familiensituationGueltigAb)) {
				return familiensituationErstGesuch.hasSecondGesuchsteller(gueltigkeit.getGueltigBis());
		}
		return familiensituation.hasSecondGesuchsteller(gueltigkeit.getGueltigBis());
	}

	private String getBeschaeftigungsTypen(@Nonnull VerfuegungZeitabschnitt abschnitt, @Nonnull Locale locale) {
		StringBuilder sb = new StringBuilder();
		for (Taetigkeit taetigkeit : abschnitt.getBgCalculationInputAsiv().getTaetigkeiten()) {
			sb.append(ServerMessageUtil.translateEnumValue(taetigkeit, locale));
			sb.append(", ");
		}
		// Das letzte Komma entfernen
		String taetigkeitenAsString = sb.toString();
		taetigkeitenAsString = StringUtils.removeEnd(taetigkeitenAsString, ", ");
		return taetigkeitenAsString;
	}
}
