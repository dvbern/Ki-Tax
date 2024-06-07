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

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.types.DateRange;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Regel für die Fachstelle. Sucht das PensumFachstelle falls vorhanden und wenn ja wird ein entsprechender
 * Zeitabschnitt generiert
 * Verweis 16.13 Fachstelle
 */
public class FachstelleAbschnittRule extends AbstractAbschnittRule {

	public FachstelleAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.FACHSTELLE, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> betreuungspensumAbschnitte = new ArrayList<>();
		for (PensumFachstelle pensumFachstelle : platz.getKind().getKindJA().getPensumFachstelle()) {
			if (pensumFachstelle != null) {
				betreuungspensumAbschnitte.add(toVerfuegungZeitabschnitt(pensumFachstelle, platz));
			}
		}
		return betreuungspensumAbschnitte;
	}

	@Nonnull
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(
		@Nonnull PensumFachstelle pensumFachstelle,
		@Nonnull AbstractPlatz platz) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(pensumFachstelle.getGueltigkeit());
		zeitabschnitt.setFachstellenpensumForAsivAndGemeinde(pensumFachstelle.getPensum());
		zeitabschnitt.setBetreuungspensumMustBeAtLeastFachstellenpensumForAsivAndGemeinde(
			betreuungspensumMustBeAtLeastFachstellenpensum(pensumFachstelle.getIntegrationTyp(), platz)
		);
		zeitabschnitt.setIntegrationTypFachstellenPensumForAsivAndGemeinde(pensumFachstelle.getIntegrationTyp());
		return zeitabschnitt;
	}

	private boolean betreuungspensumMustBeAtLeastFachstellenpensum(
		@Nonnull IntegrationTyp integrationTyp,
		@Nonnull AbstractPlatz platz
	) {
		// nur bei sprachlicher integration relevant
		if (integrationTyp != IntegrationTyp.SPRACHLICHE_INTEGRATION) {
			return false;
		}
		// diese Regel ist nur anwendbar für Betreuungen. Siehe getAnwendbareAngebote(). Hier wird eine Exception geworfen,
		// falls nicht zu einer Betreuung gecastet werden kann.
		var betreuung = (Betreuung) platz;
		if (betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA() == null) {
			return false;
		}
		// gemeinde kann manuell bestätigen, dass der BG ausbezahlt wird, auch wenn das fachstellenpensum unterschritten ist.
		return !betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.isAnspruchFachstelleWennPensumUnterschritten();
	}
}
