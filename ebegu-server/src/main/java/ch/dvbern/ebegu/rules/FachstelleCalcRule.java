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

import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;

/**
 * Regel für die Betreuungspensen. Sie beachtet:
 * - Anspruch aus Betreuungspensum darf nicht höher sein als Erwerbspensum
 * - Nur relevant für Kita, Tageseltern-Kleinkinder, die anderen bekommen so viel wie sie wollen
 * - Falls Kind eine Fachstelle hat, gilt das Pensum der Fachstelle, sofern dieses höher ist als der Anspruch aus sonstigen Regeln
 * Verweis 16.9.3
 */
public class FachstelleCalcRule extends AbstractCalcRule {

	public FachstelleCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.FACHSTELLE, RuleType.GRUNDREGEL_CALC, validityPeriod, locale);
	}

	@Override
	protected void executeRule(
		@Nonnull Betreuung betreuung,
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		// Ohne Fachstelle: Wird in einer separaten Rule behandelt
		if (betreuung.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
			int pensumFachstelle = verfuegungZeitabschnitt.getFachstellenpensum();
			int pensumAnspruch = verfuegungZeitabschnitt.getAnspruchberechtigtesPensum();
			// Das Fachstellen-Pensum wird immer auf 5-er Schritte gerundet
			int roundedPensumFachstelle = MathUtil.roundIntToFives(pensumFachstelle);
			if (roundedPensumFachstelle > 0 && roundedPensumFachstelle > pensumAnspruch) {
				// Anspruch ist immer mindestens das Pensum der Fachstelle, ausser das Restpensum lässt dies nicht mehr zu
				verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(roundedPensumFachstelle);
				verfuegungZeitabschnitt.addBemerkung(
					RuleKey.FACHSTELLE,
					MsgKey.FACHSTELLE_MSG,
					getLocale(),
					getIndikation(betreuung),
					getFachstelle(betreuung));
			}
		}
	}

	private String getIndikation(@Nonnull Betreuung betreuung) {
		if (betreuung.getKind().getKindJA().getPensumFachstelle() == null) {
			return "";
		}
		// we cannot translate the Enum directly because we need another translation specific for this Bemerkung
		return betreuung.getKind().getKindJA().getPensumFachstelle().getIntegrationTyp() == IntegrationTyp.SOZIALE_INTEGRATION ?
			ServerMessageUtil.getMessage("Sozialen_Indikation", getLocale()) :
			ServerMessageUtil.getMessage("Sprachlichen_Indikation", getLocale());
	}

	private String getFachstelle(@Nonnull Betreuung betreuung) {
		if (betreuung.getKind().getKindJA().getPensumFachstelle() == null) {
			return "";
		}
		return ServerMessageUtil.translateEnumValue(
			betreuung.getKind().getKindJA().getPensumFachstelle().getFachstelle().getName(),
			getLocale());
	}
}
