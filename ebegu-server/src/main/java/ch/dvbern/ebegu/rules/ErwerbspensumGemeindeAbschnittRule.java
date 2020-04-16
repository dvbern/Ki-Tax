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
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Diese Rule macht genau das gleiche wie die ErwerbspensumAbschnittRule. Sie gilt aber fuer die Gemeinde und
 * beachtet nur noch den Typ "Freiwilligenarbeit" (zusaetzlich zur anderen Regel). Dieser Typ ist bei ASIV ausgeschlossen.
 */
public class ErwerbspensumGemeindeAbschnittRule extends ErwerbspensumAbschnittRule {

	private final Integer maximalpensumFreiwilligenarbeit;

	public ErwerbspensumGemeindeAbschnittRule(
		@Nonnull DateRange validityPeriod,
		int erwerbspensumZuschlag,
		@Nonnull Integer maximalpensumFreiwilligenarbeit,
		@Nonnull Locale locale
	) {
		super(RuleValidity.GEMEINDE, validityPeriod, erwerbspensumZuschlag, locale);
		this.maximalpensumFreiwilligenarbeit = maximalpensumFreiwilligenarbeit;
	}

	@Override
	@Nonnull
	protected List<Taetigkeit> getValidTaetigkeiten() {
		List<Taetigkeit> freiwilligenarbeit = new ArrayList<>();
		freiwilligenarbeit.add(Taetigkeit.FREIWILLIGENARBEIT);
		return freiwilligenarbeit;
	}

	@Override
	@Nullable
	protected VerfuegungZeitabschnitt createZeitAbschnitt(
		@Nonnull DateRange gueltigkeit, @Nonnull Erwerbspensum erwerbspensum, boolean isGesuchsteller1
	) {
		Optional<DateRange> gueltigkeitOverlap = gueltigkeit.getOverlap(new DateRange(validFrom(), validTo()));
		if (gueltigkeitOverlap.isPresent()) {
			VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(gueltigkeitOverlap.get());
			zeitabschnitt.getBgCalculationInputGemeinde().getTaetigkeiten().add(erwerbspensum.getTaetigkeit());
			Integer limitedPensum = erwerbspensum.getPensum();
			if (limitedPensum > maximalpensumFreiwilligenarbeit) {
				limitedPensum = maximalpensumFreiwilligenarbeit;
			}
			if (limitedPensum > 0) {
				if (isGesuchsteller1) {
					zeitabschnitt.getBgCalculationInputGemeinde().setErwerbspensumGS1(limitedPensum);
				} else {
					zeitabschnitt.getBgCalculationInputGemeinde().setErwerbspensumGS2(limitedPensum);
				}
				zeitabschnitt.getBemerkungenList().addBemerkung(MsgKey.ERWERBSPENSUM_FREIWILLIGENARBEIT, getLocale(), limitedPensum);
			}
			return zeitabschnitt;
		}
		return null;
	}

	@Override
	protected void setErwerbspensumZuschlag(@Nonnull VerfuegungZeitabschnitt zeitabschnitt, int zuschlagErwerbspensum) {
		zeitabschnitt.getBgCalculationInputGemeinde().setErwerbspensumZuschlag(zuschlagErwerbspensum);
	}
}
