/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Regel, welche den Anspruch für das Angebot Kita für die gesamte Periode auf 0 stellen kann
 * Die Regel hat einen Parameter, welcher besagt, bis wann BGs ausgestellt werden. Dabei gibt es folgende Auswahl:
 * - Kindergarten 2 (Standard respektive ASIV-Umsetzung): Bei dieser Auswahl werden BG für Kinder bis und mit Kindergarten
 * 		ausgestellt. D.h. ab der ersten Klasse wird kein Gutschein ausgestellt.
 *		Meldung auf Verfügung: Kein Anspruch für Kinder ab der ersten Klasse.
 * - Kindergarten 1: Bei dieser Auswahl werden BG für Kinder bis und mit Kindergarten 1 ausgestellt. D.h. ein Kind, welches
 * 		das zweite Jahr im Kindergarten ist hat KEINEN Anspruch
 * 		Meldung auf Verfügung: Kein Anspruch für Kinder ab dem zweiten Kindergartenjahr
 * - Vorschulalter: Bei dieser Auswahl werden BG nur für Kinder im Vorschulalter ausgestellt. D.h. ein Kind, welches das
 * 		erste Jahr im Kindergarten ist hat KEINEN Anspruch
 * 		Meldung auf Verfügung: Kein Anspruch für Kinder ab dem Kindergarten
 */
public class SchulstufeCalcRule extends AbstractCalcRule {

	@Nonnull
	private final EinschulungTyp einschulungsTypAnspruchsgrenze;

	public SchulstufeCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull EinschulungTyp einschulungsTypAnspruchsgrenze,
		@Nonnull Locale locale
	) {
		super(RuleKey.SCHULSTUFE, RuleType.REDUKTIONSREGEL, validityPeriod, locale);
		this.einschulungsTypAnspruchsgrenze = einschulungsTypAnspruchsgrenze;
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Override
	protected void executeRule(@Nonnull Betreuung betreuung, @Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		// Die Regel gilt nur fuer Kita
		if (betreuung.getBetreuungsangebotTyp() != null && betreuung.getBetreuungsangebotTyp().isKita()) {
			if (betreuung.getKind() != null && betreuung.getKind().getKindJA() != null) {
				final Kind kindJA = betreuung.getKind().getKindJA();
				EinschulungTyp einschulungTyp = kindJA.getEinschulungTyp();
				if (einschulungTyp != null) {
					if (einschulungTyp.ordinal() > einschulungsTypAnspruchsgrenze.ordinal()) {
						// Der Anspruch wird (nur fuer diese Betreuung!) auf 0 gesetzt. Dafuer wird der vorher berechnete Anspruch wieder als Restanspruch
						// gefuehrt
						int anspruchVorRegel = verfuegungZeitabschnitt.getAnspruchberechtigtesPensum();
						verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(0);
						verfuegungZeitabschnitt.setAnspruchspensumRest(anspruchVorRegel);
						verfuegungZeitabschnitt.addBemerkung(RuleKey.SCHULSTUFE, getMsgKey(), getLocale());
					}
				}
			}
		}
	}

	private MsgKey getMsgKey() {
		switch (einschulungsTypAnspruchsgrenze) {
		case VORSCHULALTER:
			return MsgKey.SCHULSTUFE_VORSCHULE_MSG;
		case KINDERGARTEN1:
			return MsgKey.SCHULSTUFE_KINDERGARTEN_1_MSG;
		default:
			return MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG;
		}
	}
}
