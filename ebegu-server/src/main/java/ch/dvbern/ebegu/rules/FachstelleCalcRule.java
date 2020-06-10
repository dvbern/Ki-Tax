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
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Regel für die Betreuungspensen. Sie beachtet:
 * - Anspruch aus Betreuungspensum darf nicht höher sein als Erwerbspensum
 * - Nur relevant für Kita, Tageseltern-Kleinkinder, die anderen bekommen so viel wie sie wollen
 * - Falls Kind eine Fachstelle hat, gilt das Pensum der Fachstelle, sofern dieses höher ist als der Anspruch aus sonstigen Regeln
 * Verweis 16.9.3
 */
public class FachstelleCalcRule extends AbstractCalcRule {

	public FachstelleCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.FACHSTELLE, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
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
		// Ohne Fachstelle: Wird in einer separaten Rule behandelt
		Betreuung betreuung = (Betreuung) platz;
		int pensumFachstelle = inputData.getFachstellenpensum();
		boolean betreuungspensumMustBeAtLeastFachstellenpensum = inputData.isBetreuungspensumMustBeAtLeastFachstellenpensum();
		BigDecimal pensumBetreuung = inputData.getBetreuungspensumProzent();
		int pensumAnspruch = inputData.getAnspruchspensumProzent();

		// Das Fachstellen-Pensum wird immer auf 5-er Schritte gerundet
		int roundedPensumFachstelle = MathUtil.roundIntToFives(pensumFachstelle);
		if (roundedPensumFachstelle > 0 && roundedPensumFachstelle > pensumAnspruch) {
			if (!betreuungspensumMustBeAtLeastFachstellenpensum
				|| pensumBetreuung.compareTo(BigDecimal.valueOf(roundedPensumFachstelle)) >= 0) {

				// Anspruch ist immer mindestens das Pensum der Fachstelle, ausser das Restpensum lässt dies nicht mehr zu
				inputData.setAnspruchspensumProzent(roundedPensumFachstelle);
				inputData.addBemerkung(
					MsgKey.FACHSTELLE_MSG,
					getLocale(),
					getIndikation(betreuung),
					getFachstelle(betreuung));
			} else {
				// Es gibt ein Fachstelle Pensum, aber das Betreuungspensum ist zu tief. Wir muessen uns das Fachstelle Pensum als
				// Restanspruch merken, damit es für eine eventuelle andere Betreuung dieses Kindes noch gilt!
				int verfuegbarerRestanspruch = inputData.getAnspruchspensumRest();
				// wir muessen nur was machen wenn wir schon einen Restanspruch gesetzt haben
				if (verfuegbarerRestanspruch < roundedPensumFachstelle) {
					inputData.setAnspruchspensumRest(roundedPensumFachstelle);
				}
				inputData.addBemerkung(
					MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG,
					getLocale(),
					roundedPensumFachstelle);
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
