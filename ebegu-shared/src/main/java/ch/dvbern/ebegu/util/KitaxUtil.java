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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KitaxUebergangsloesungInstitutionOeffnungszeiten;

/**
 * Util fuer Kitax-Belange
 */
public final class KitaxUtil {

	// DEV: d254c221-0c93-4151-aa87-6d32cd0a4d9e, PROD: b195d1a4-81e5-4233-8d7e-d99c4f1eb452
	private static final List<String> IDS_TO_ACCEPT_FEBR_BUT_NOT_ASIV =
		Arrays.asList("d254c221-0c93-4151-aa87-6d32cd0a4d9e", "b195d1a4-81e5-4233-8d7e-d99c4f1eb452");

	private KitaxUtil() {
	}

	public static boolean isGemeindeWithKitaxUebergangsloesung(@Nonnull Gemeinde gemeinde) {
		// Zum Testen behandeln wir Paris wie Bern
		long bfsNummer = gemeinde.getBfsNummer();
		return bfsNummer == 351 || bfsNummer == 99998;
	}

	public static boolean isInstitutionAcceptingFebrButNotAsiv(@Nonnull InstitutionStammdaten institution) {
		return IDS_TO_ACCEPT_FEBR_BUT_NOT_ASIV.contains(institution.getInstitution().getId());
	}

	public static BigDecimal recalculatePensumKonvertierung(
		@Nonnull String kitaName,
		@Nonnull KitaxUebergangsloesungParameter kitaxParameter,
		@Nonnull Betreuungspensum betreuungspensum
	) {
		switch (betreuungspensum.getUnitForDisplay()) {
		case DAYS:
			KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten = kitaxParameter.getOeffnungszeiten(kitaName);
			BigDecimal faktor = MathUtil.EXACT.divide(BigDecimal.valueOf(240), oeffnungszeiten.getOeffnungstage());
			BigDecimal prozent = MathUtil.EXACT.multiply(betreuungspensum.getPensum(), faktor);
			return prozent;
		case HOURS:
			BigDecimal pensumStundenAsiv = MathUtil.EXACT.multiply(betreuungspensum.getPensum(),
				BigDecimal.valueOf(2.2));


			BigDecimal anzahlTageProMonat = MathUtil.EXACT.divide(kitaxParameter.getMaxTageKita(), BigDecimal.valueOf(12));
			BigDecimal maxBetreuungsstundenProMonat = MathUtil.EXACT.multiply(anzahlTageProMonat,
				kitaxParameter.getMaxStundenProTagKita());

			BigDecimal stunden = MathUtil.EXACT.multiply(maxBetreuungsstundenProMonat,
				betreuungspensum.getPensum()).divide(BigDecimal.valueOf(100));

			BigDecimal pensumEffektiv =
				MathUtil.EXACT.divide(pensumStundenAsiv, stunden).multiply(betreuungspensum.getPensum());
			return pensumEffektiv;
		default:
			return betreuungspensum.getPensum();
		}
	}

	public static boolean isCompletePensumFEBR(@Nonnull KitaxUebergangsloesungParameter kitaxParameter, @Nonnull Betreuungspensum betreuungspensum) {
		return kitaxParameter.getStadtBernAsivStartDate().isAfter(betreuungspensum.getGueltigkeit().getGueltigBis());
	}

	public static boolean isCompletePensumASIV(@Nonnull KitaxUebergangsloesungParameter kitaxParameter, @Nonnull Betreuungspensum betreuungspensum) {
		return !kitaxParameter.getStadtBernAsivStartDate().isAfter(betreuungspensum.getGueltigkeit().getGueltigAb());
	}

	public static boolean isPensumMixedFEBRandASIV(@Nonnull KitaxUebergangsloesungParameter kitaxParameter, @Nonnull Betreuungspensum betreuungspensum) {
		return !isCompletePensumFEBR(kitaxParameter, betreuungspensum)
			&& !isCompletePensumASIV(kitaxParameter, betreuungspensum);
	}
}
