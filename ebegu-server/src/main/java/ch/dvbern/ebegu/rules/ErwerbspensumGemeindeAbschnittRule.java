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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Diese Rule macht genau das gleiche wie die ErwerbspensumAbschnittRule. Sie gilt aber fuer die Gemeinde und
 * beachtet nur noch den Typ "Freiwilligenarbeit" (zusaetzlich zur anderen Regel). Dieser Typ ist bei ASIV ausgeschlossen.
 */
public class ErwerbspensumGemeindeAbschnittRule extends ErwerbspensumAbschnittRule {

	public ErwerbspensumGemeindeAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleValidity.GEMEINDE, validityPeriod, locale);
	}

	@Override
	@Nonnull
	protected List<Taetigkeit> getValidTaetigkeiten() {
		List<Taetigkeit> freiwilligenarbeit = new ArrayList<>();
		freiwilligenarbeit.add(Taetigkeit.FREIWILLIGENARBEIT);
		return freiwilligenarbeit;
	}

	@Override
	@Nonnull
	protected VerfuegungZeitabschnitt createZeitAbschnittForGS1(@Nonnull DateRange gueltigkeit, @Nonnull Erwerbspensum erwerbspensum) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
		zeitabschnitt.addTaetigkeitForAsivAndGemeinde(erwerbspensum.getTaetigkeit());
		zeitabschnitt.getBgCalculationInputGemeinde().setErwerbspensumGS1(erwerbspensum.getPensum());
		return zeitabschnitt;
	}

	@Override
	@Nonnull
	protected VerfuegungZeitabschnitt createZeitAbschnittForGS2(@Nonnull DateRange gueltigkeit, @Nonnull Erwerbspensum erwerbspensum) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt(gueltigkeit);
		zeitabschnitt.addTaetigkeitForAsivAndGemeinde(erwerbspensum.getTaetigkeit());
		zeitabschnitt.getBgCalculationInputGemeinde().setErwerbspensumGS2(erwerbspensum.getPensum());
		return zeitabschnitt;
	}
}
