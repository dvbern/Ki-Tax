/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.types.DateRange;

public class ErwebspensumSchwyzCalcRule extends ErwerbspensumCalcRule {

	protected ErwebspensumSchwyzCalcRule(
		@Nonnull DateRange validityPeriod,
		int minErwerbspensumNichtEingeschult,
		int minErwerbspensumEingeschult,
		int paramMinDauerKonkubinat,
		@Nonnull Locale locale) {
		super(
			RuleValidity.ASIV,
			validityPeriod,
			minErwerbspensumNichtEingeschult,
			minErwerbspensumEingeschult,
			paramMinDauerKonkubinat,
			locale);
	}

	@Override
	protected boolean isErwerbspensumRelevantForGS2(@Nonnull AbstractPlatz platz, @Nonnull BGCalculationInput inputData) {
		Kind kind = platz.getKind().getKindJA();
		final boolean has2GsOnFamiliensituation = hasSecondGSForZeit(platz.extractGesuch(), inputData.getParent().getGueltigkeit());
		// kinder von erstgesuch betrachten wenn vor famsit änderungsdatum
		final boolean has2GSOnKind = Boolean.TRUE.equals(kind.getGemeinsamesGesuch());
		return has2GsOnFamiliensituation && has2GSOnKind;
	}
}
