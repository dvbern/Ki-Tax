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

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Regel für die Betreuungspensen. Sie beachtet:
 * - Anspruch aus Betreuungspensum darf nicht höher sein als Erwerbspensum
 * - Nur relevant für Kita, Tageseltern-Kleinkinder, die anderen bekommen so viel wie sie wollen
 * - Falls Kind eine Fachstelle hat, gilt das Pensum der Fachstelle, sofern dieses höher ist als der Anspruch aus sonstigen Regeln
 * Verweis 16.9.3
 */
public class FachstelleBernCalcRule extends AbstractFachstellenCalcRule {
	private boolean sprachefoerderungBestaetigenAktiviert;

	public FachstelleBernCalcRule(
		boolean sprachefoerderungBestaetigenAktiviert,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale) {
		super(RuleKey.FACHSTELLE, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
		this.sprachefoerderungBestaetigenAktiviert = sprachefoerderungBestaetigenAktiviert;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		// Ohne Fachstelle: Wird in einer separaten Rule behandelt
		Betreuung betreuung = (Betreuung) platz;
		int pensum = inputData.getFachstellenpensum();
		boolean betreuungspensumMustBeAtLeastFachstellenpensum = inputData.isBetreuungspensumMustBeAtLeastFachstellenpensum();
		BigDecimal pensumBetreuung = inputData.getBetreuungspensumProzent();
		int pensumAnspruch = inputData.getAnspruchspensumProzent();
		// Das Fachstellen-Pensum wird immer auf 5-er Schritte gerundet
		int roundedPensumFachstelle = MathUtil.roundIntToFives(pensum);
		if (roundedPensumFachstelle > 0) {
			// Bei Sprachliche Integration muss die sprachfoerderung bestaetigt werden
			if (showFachstelleSprachlicheIntegrationNichtBestaetigtBemerkung(
				inputData.getIntegrationTypFachstellenPensum(),
				platz)) {
				inputData.addBemerkung(
					MsgKey.FACHSTELLE_SPRACHEFOEDERUNG_NICHT_BESTAETIGT_MSG,
					getLocale());
				return;
			}
			if (roundedPensumFachstelle > pensumAnspruch) {
				if (!betreuungspensumMustBeAtLeastFachstellenpensum
					|| pensumBetreuung.compareTo(BigDecimal.valueOf(roundedPensumFachstelle)) >= 0) {
					// Anspruch ist immer mindestens das Pensum der Fachstelle, ausser das Restpensum lässt dies nicht mehr zu
					inputData.setAnspruchspensumProzent(roundedPensumFachstelle);
					PensumFachstelle pensumFachstelle =
						findPensumFachstelleForGueltigkeit(
							betreuung.getKind().getKindJA(),
							inputData.getParent().getGueltigkeit());
					inputData.addBemerkung(
						MsgKey.FACHSTELLE_MSG,
						getLocale(),
						getIndikationName(pensumFachstelle, betreuung),
						getFachstelleName(pensumFachstelle.getFachstelle())
					);
				} else {
					handlePensumTooLow(inputData, betreuung, roundedPensumFachstelle);
				}
			}
		}
	}

	private void handlePensumTooLow(@Nonnull BGCalculationInput inputData, Betreuung betreuung, int roundedPensumFachstelle) {
		// Es gibt ein Fachstelle Pensum, aber das Betreuungspensum ist zu tief. Wir muessen uns das Fachstelle
		// Pensum als
		// Restanspruch merken, damit es für eine eventuelle andere Betreuung dieses Kindes noch gilt!
		int verfuegbarerRestanspruch = inputData.getAnspruchspensumRest();
		// wir muessen nur was machen wenn wir schon einen Restanspruch gesetzt haben
		if (verfuegbarerRestanspruch < roundedPensumFachstelle) {
			inputData.setAnspruchspensumRest(roundedPensumFachstelle);
		}
		if (intersectsAnyBetreuungspensum(inputData.getParent(), betreuung)) {
			inputData.addBemerkung(
				MsgKey.FACHSTELLE_SPRACHLICHE_INTEGRATION_ZU_TIEF_MSG,
				getLocale(),
				roundedPensumFachstelle);
		}
	}

	private boolean intersectsAnyBetreuungspensum(VerfuegungZeitabschnitt verfuegungZeitabschnitt, Betreuung betreuung) {
		DateRange zeitabschnittGueltigkeit = verfuegungZeitabschnitt.getGueltigkeit();
		return betreuung.getBetreuungspensumContainers()
			.stream()
			.map(BetreuungspensumContainer::getBetreuungspensumJA)
			.anyMatch(betreuungspensum -> zeitabschnittGueltigkeit.intersects(betreuungspensum.getGueltigkeit()));
	}


	@Override
	public boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap) {
		return super.getFachstellenTypFromEinstellungen(einstellungMap) == FachstellenTyp.BERN;
	}

	private boolean showFachstelleSprachlicheIntegrationNichtBestaetigtBemerkung(
		@Nonnull IntegrationTyp integrationTyp,
		@Nonnull AbstractPlatz platz) {
		if (!integrationTyp.equals(IntegrationTyp.SPRACHLICHE_INTEGRATION) || !sprachefoerderungBestaetigenAktiviert) {
			return false;
		}
		var betreuung = (Betreuung) platz;
		if (betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA() == null) {
			return true;
		}
		return !betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA().isSprachfoerderungBestaetigt();
	}
}
