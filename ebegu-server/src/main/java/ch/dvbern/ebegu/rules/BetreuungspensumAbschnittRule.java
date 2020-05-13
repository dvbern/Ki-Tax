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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Regel für die Erstellung der Zeitabschnitte der Betreuungspensen
 * Verweis 16.9.3
 */
public class BetreuungspensumAbschnittRule extends AbstractAbschnittRule {

	public BetreuungspensumAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BETREUUNGSPENSUM, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		Betreuung betreuung = (Betreuung) platz;
		List<VerfuegungZeitabschnitt> betreuungspensumAbschnitte = new ArrayList<>();
		Set<BetreuungspensumContainer> betreuungspensen = betreuung.getBetreuungspensumContainers();
		for (BetreuungspensumContainer betreuungspensumContainer : betreuungspensen) {
			Betreuungspensum betreuungspensum = betreuungspensumContainer.getBetreuungspensumJA();
			betreuungspensumAbschnitte.add(toVerfuegungZeitabschnitt(betreuungspensum, betreuung));
		}
		return betreuungspensumAbschnitte;
	}

	/**
	 * @param betreuungspensum zu konvertiertendes Betreuungspensum
	 * @return VerfuegungZeitabschnitt mit gleicher gueltigkeit und uebernommenem betreuungspensum
	 */
	@Nonnull
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(
		@Nonnull Betreuungspensum betreuungspensum,
		@Nonnull Betreuung betreuung
	) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(betreuungspensum.getGueltigkeit());
		// Eigentliches Betreuungspensum
		zeitabschnitt.setBetreuungspensumProzentForAsivAndGemeinde(betreuungspensum.getPensum());
		zeitabschnitt.setMonatlicheBetreuungskostenForAsivAndGemeinde(betreuungspensum.getMonatlicheBetreuungskosten());

		// Anzahl Haupt und Nebenmahlzeiten übernehmen
		zeitabschnitt.setMonatlicheHauptmahlzeitenForAsivAndGemeinde(BigDecimal.valueOf(betreuungspensum.getMonatlicheHauptmahlzeiten()));
		zeitabschnitt.setMonatlicheNebenmahlzeitenForAsivAndGemeinde(BigDecimal.valueOf(betreuungspensum.getMonatlicheNebenmahlzeiten()));

		// ErweiterteBetreuung-Flag gesetzt?
		boolean besondereBeduerfnisse = betreuung.hasErweiterteBetreuung();

		// Falls die Betreuung im Status UNBEKANNTE_INSTITUTION ist, soll die Pauschale immer berechnet werden
		boolean besondereBeduerfnisseBestaetigt =
			besondereBeduerfnisse
			&& (betreuung.isErweiterteBeduerfnisseBestaetigt()
				|| betreuung.getBetreuungsstatus() == Betreuungsstatus.UNBEKANNTE_INSTITUTION);

		zeitabschnitt.setBesondereBeduerfnisseBestaetigtForAsivAndGemeinde(besondereBeduerfnisseBestaetigt);

		// Betreuung in Gemeinde?
		ErweiterteBetreuung erweiterteBetreuung = betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA();
		if (erweiterteBetreuung != null && erweiterteBetreuung.getBetreuungInGemeinde() != null) {
			zeitabschnitt.setBetreuungInGemeindeForAsivAndGemeinde(erweiterteBetreuung.getBetreuungInGemeinde());
		}

		// Die Institution muss die besonderen Bedürfnisse bestätigt haben
		if (besondereBeduerfnisseBestaetigt) {
			zeitabschnitt.getBgCalculationInputAsiv().addBemerkung(
				MsgKey.ERWEITERTE_BEDUERFNISSE_MSG,
				getLocale());
		}
		return zeitabschnitt;
	}
}
