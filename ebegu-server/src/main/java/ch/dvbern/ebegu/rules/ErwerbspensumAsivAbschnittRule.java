/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Berechnet die hoehe des ErwerbspensumRule eines bestimmten Erwerbspensums
 * Diese Rule muss immer am Anfang kommen, d.h. sie setzt den initialen Anspruch
 * Die weiteren Rules müssen diesen Wert gegebenenfalls korrigieren.
 * Verweis 16.9.2
 */
public class ErwerbspensumAsivAbschnittRule extends ErwerbspensumAbschnittRule {

	public ErwerbspensumAsivAbschnittRule(
		@Nonnull DateRange validityPeriod,
		int erwerbspensumZuschlag,
		@Nonnull Locale locale
	) {
		super(RuleValidity.ASIV, validityPeriod, erwerbspensumZuschlag, locale);
	}

	@Override
	@Nonnull
	protected List<Taetigkeit> getValidTaetigkeiten() {
		return Taetigkeit.getTaetigkeitenForAsiv();
	}

	@Nullable
	@Override
	protected VerfuegungZeitabschnitt createZeitAbschnitt(@Nonnull DateRange gueltigkeit, @Nonnull Erwerbspensum erwerbspensum, boolean isGesuchsteller1) {
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		zeitabschnitt.addTaetigkeitForAsivAndGemeinde(erwerbspensum.getTaetigkeit());
		if (isGesuchsteller1) {
			zeitabschnitt.setErwerbspensumGS1ForAsivAndGemeinde(erwerbspensum.getPensum());
		} else {
			zeitabschnitt.setErwerbspensumGS2ForAsivAndGemeinde(erwerbspensum.getPensum());
		}
		return zeitabschnitt;
	}

	@Override
	protected void setErwerbspensumZuschlag(@Nonnull VerfuegungZeitabschnitt zeitabschnitt, int zuschlagErwerbspensum) {
		zeitabschnitt.setErwerbspensumZuschlagForAsivAndGemeinde(zuschlagErwerbspensum);
	}
}
