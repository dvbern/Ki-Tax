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

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;

import static java.util.Objects.requireNonNull;

/**
 * Berechnet die hoehe des ErwerbspensumRule eines bestimmten Erwerbspensums
 * Diese Rule muss immer am Anfang kommen, d.h. sie setzt den initialen Anspruch
 * Die weiteren Rules müssen diesen Wert gegebenenfalls korrigieren.
 * ACHTUNG! Diese Regel gilt nur fuer Angebote vom Typ isAngebotJugendamtKleinkind
 * Verweis 16.9.2
 */
public class ErwerbspensumCalcRule extends AbstractCalcRule {

	private final int maxZuschlagValue;
	private final int minErwerbspensumNichtEingeschult;
	private final int minErwerbspensumEingeschult;

	public ErwerbspensumCalcRule(DateRange validityPeriod, int maxZuschlagValue, int minErwerbspensumNichtEingeschult, int minErwerbspensumEingeschult) {
		super(RuleKey.ERWERBSPENSUM, RuleType.GRUNDREGEL_CALC, validityPeriod);
		this.maxZuschlagValue = maxZuschlagValue;
		this.minErwerbspensumNichtEingeschult = minErwerbspensumNichtEingeschult;
		this.minErwerbspensumEingeschult = minErwerbspensumEingeschult;
	}

	@Override
	protected void executeRule(@Nonnull Betreuung betreuung, @Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		if (requireNonNull(betreuung.getBetreuungsangebotTyp()).isAngebotJugendamtKleinkind()) {
			requireNonNull(betreuung.extractGesuch(), "Gesuch muss gesetzt sein");
			requireNonNull(betreuung.extractGesuch().extractFamiliensituation(), "Familiensituation muss gesetzt sein");
			boolean hasSecondGesuchsteller = hasSecondGSForZeit(betreuung, verfuegungZeitabschnitt.getGueltigkeit());
			int erwerbspensumOffset = hasSecondGesuchsteller ? 100 : 0;
			// Erwerbspensum ist immer die erste Rule, d.h. es wird das Erwerbspensum mal als Anspruch angenommen
			// Das Erwerbspensum muss PRO GESUCHSTELLER auf 100% limitiert werden
			Integer erwerbspensum1 = calculateErwerbspensumGS1(verfuegungZeitabschnitt);
			Integer zuschlag1 = calculateZuschlagGS1(erwerbspensum1, verfuegungZeitabschnitt);
			Integer erwerbspensum2 = 0;
			Integer zuschlag2 = 0;
			if (hasSecondGesuchsteller) {
				erwerbspensum2 = calculateErwerbspensumGS2(verfuegungZeitabschnitt);
				zuschlag2 = calculateZuschlagGS2(erwerbspensum2, zuschlag1, verfuegungZeitabschnitt);
			}
			int totalZuschlag = zuschlag1 + zuschlag2;
			int anspruch = erwerbspensum1 + erwerbspensum2 + totalZuschlag - erwerbspensumOffset;
			int minimum = getMinimumErwerbspensum(betreuung);
			int roundedAnspruch = checkAndRoundAnspruch(verfuegungZeitabschnitt, anspruch, minimum);
			verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(roundedAnspruch);
		}
	}

	private int getMinimumErwerbspensum(@Nonnull Betreuung betreuung) {
		EinschulungTyp einschulungTyp = betreuung.getKind().getKindJA().getEinschulungTyp();
		Objects.requireNonNull(einschulungTyp);
		int mininum = einschulungTyp.isEingeschult() ? minErwerbspensumEingeschult : minErwerbspensumNichtEingeschult;
		return mininum;
	}

	private Integer calculateZuschlagGS1(int erwerbspensum, VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		int zuschlag = verfuegungZeitabschnitt.getZuschlagErwerbspensumGS1() != null ? verfuegungZeitabschnitt.getZuschlagErwerbspensumGS1() : 0;
		if ((erwerbspensum + zuschlag) > 100) {
			verfuegungZeitabschnitt.addBemerkung(RuleKey.ERWERBSPENSUM, MsgKey.ERWERBSPENSUM_ZUSCHLAG_GS1_MSG);
			zuschlag = 100 - erwerbspensum;    //zuschlag ist maximal die differenz zu 100%
		}
		if (zuschlag > maxZuschlagValue) {
			verfuegungZeitabschnitt.addBemerkung(RuleKey.ERWERBSPENSUM, MsgKey.ERWERBSPENSUM_MAX_ZUSCHLAG, maxZuschlagValue);
			zuschlag = maxZuschlagValue;
		}
		return zuschlag;
	}

	/**
	 * pfueft dass Erwerbspensum + Zuschlag fuer den GS2  100% nicht uebschreitet. Zudem darf der Zuschlag
	 * von Gesuchsteller2 plus der Zuschlag von Gesuchsteler1 den maximale zugelassenen Wert nicht ueberschreiten.
	 *
	 * @param erwerbspensum2 erwerbspensum von GS2
	 * @param zuschlagGS1 bereits verbrauchter zuschlag von GS1 (ist immer kleiner gleich 'maxvalue')
	 * @param verfuegungZeitabschnitt verfuegungsabschnitt aus dem der zuschlag vom GS2 extrahiert wird
	 * @return maximaler Zuschlag der GS2 angerechnet werden kann
	 */
	private Integer calculateZuschlagGS2(Integer erwerbspensum2, Integer zuschlagGS1, VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		Integer zuschlag2 = verfuegungZeitabschnitt.getZuschlagErwerbspensumGS2() != null ? verfuegungZeitabschnitt.getZuschlagErwerbspensumGS2() : 0;
		int maximalerZuschlagGS2 = zuschlag2;
		if ((erwerbspensum2 + zuschlag2) > 100) {
			verfuegungZeitabschnitt.addBemerkung(RuleKey.ERWERBSPENSUM, MsgKey.ERWERBSPENSUM_ZUSCHLAG_GS2_MSG);
			maximalerZuschlagGS2 = 100 - erwerbspensum2;
		}
		//wenn gs1 schon einen Uuschlag beansprucht, darf hier nur noch der Rest vergeben werden
		if (zuschlagGS1 + maximalerZuschlagGS2 > maxZuschlagValue) {
			verfuegungZeitabschnitt.addBemerkung(RuleKey.ERWERBSPENSUM, MsgKey.ERWERBSPENSUM_MAX_ZUSCHLAG, maxZuschlagValue);
			maximalerZuschlagGS2 = maxZuschlagValue - zuschlagGS1;
		}
		return maximalerZuschlagGS2;
	}

	/**
	 * Sollte der Anspruch weniger als 0 sein, wird dieser auf 0 gesetzt und eine Bemerkung eingefuegt.
	 * Wenn der Anspruch groesser als 100 ist, wird dieser auf 100 gesetzt. Hier braucht es keine Bemerkung, denn sie
	 * wurde bereits in calculateErwerbspensum eingefuegt.
	 * Am Ende wird der Wert gerundet und zurueckgegeben
	 */
	private int checkAndRoundAnspruch(@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt, int anspruch, int minimum) {
		if (anspruch <= 0) {
			anspruch = 0;
			verfuegungZeitabschnitt.addBemerkung(RuleKey.ERWERBSPENSUM, MsgKey.ERWERBSPENSUM_ANSPRUCH);
			verfuegungZeitabschnitt.setKategorieKeinPensum(true);
		} else if (anspruch > 100) { // das Ergebniss darf nie mehr als 100 sein
			anspruch = 100;
		}
		// Minimum pruefen
		if (anspruch < minimum) {
			anspruch = 0;
			verfuegungZeitabschnitt.addBemerkung(RuleKey.ERWERBSPENSUM, MsgKey.ERWERBSPENSUM_MINIMUM_MSG, minimum);
		}
		// Der Anspruch wird immer auf 5-er Schritten gerundet.
		return MathUtil.roundIntToFives(anspruch);
	}

	@Nonnull
	private Integer calculateErwerbspensumGS1(@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		Integer erwerbspensum = verfuegungZeitabschnitt.getErwerbspensumGS1() != null ? verfuegungZeitabschnitt.getErwerbspensumGS1() : 0;
		return calculateErwerbspensum(verfuegungZeitabschnitt, erwerbspensum, MsgKey.ERWERBSPENSUM_GS1_MSG);
	}

	@Nonnull
	private Integer calculateErwerbspensumGS2(@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		Integer erwerbspensum = verfuegungZeitabschnitt.getErwerbspensumGS2() != null ? verfuegungZeitabschnitt.getErwerbspensumGS2() : 0;
		return calculateErwerbspensum(verfuegungZeitabschnitt, erwerbspensum, MsgKey.ERWERBSPENSUM_GS2_MSG);
	}

	@Nonnull
	private Integer calculateErwerbspensum(VerfuegungZeitabschnitt verfuegungZeitabschnitt, Integer erwerbspensum, MsgKey bemerkung) {
		if (erwerbspensum > 100) {
			erwerbspensum = 100;
			verfuegungZeitabschnitt.addBemerkung(RuleKey.ERWERBSPENSUM, bemerkung);
		}
		return erwerbspensum;
	}

	private boolean hasSecondGSForZeit(@Nonnull Betreuung betreuung, @Nonnull DateRange gueltigkeit) {
		final Gesuch gesuch = betreuung.extractGesuch();
		final Familiensituation familiensituation = requireNonNull(gesuch.extractFamiliensituation());
		final Familiensituation familiensituationErstGesuch = gesuch.extractFamiliensituationErstgesuch();

		if (familiensituation.getAenderungPer() != null
			&& familiensituationErstGesuch != null
			&& gueltigkeit.getGueltigBis().isBefore(familiensituation.getAenderungPer())) {

			return familiensituationErstGesuch.hasSecondGesuchsteller();
		}
		return familiensituation.hasSecondGesuchsteller();
	}
}
