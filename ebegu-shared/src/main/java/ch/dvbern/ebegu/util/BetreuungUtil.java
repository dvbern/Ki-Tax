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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.services.EinstellungService;

/**
 * Allgemeine Utils fuer Betreuung
 */
public final class BetreuungUtil {

	private static final Pattern COMPILE = Pattern.compile("^0+(?!$)");

	public static final BigDecimal ANZAHL_STUNDEN_PRO_TAG_KITA = new BigDecimal(10);

	private BetreuungUtil() {
	}

	/**
	 * Returns the corresponding minimum value for the given betreuungsangebotTyp.
	 *
	 * @param betreuungsangebotTyp betreuungsangebotTyp
	 * @param gesuchsperiode defines which parameter to load. We only look for params that are valid on this day
	 * @return The minimum value for the betreuungsangebotTyp. Default value is -1: This means if the given betreuungsangebotTyp doesn't match any
	 * recorded type, the min value will be 0 and any positive value will be then accepted
	 */
	public static BigDecimal getMinValueFromBetreuungsangebotTyp(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde,
		@Nullable BetreuungsangebotTyp betreuungsangebotTyp,
		@Nonnull EinstellungService einstellungService,
		@Nullable final EntityManager em) {

		EinstellungKey key = null;
		if (betreuungsangebotTyp == BetreuungsangebotTyp.KITA) {
			key = EinstellungKey.PARAM_PENSUM_KITA_MIN;
		} else if (betreuungsangebotTyp == BetreuungsangebotTyp.TAGESSCHULE) {
			key = EinstellungKey.PARAM_PENSUM_TAGESSCHULE_MIN;
		} else if (betreuungsangebotTyp == BetreuungsangebotTyp.TAGESFAMILIEN) {
			key = EinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN;
		}
		if (key != null) {
			Einstellung parameter = einstellungService.findEinstellung(key, gemeinde, gesuchsperiode, em);
			return parameter.getValueAsBigDecimal();
		}
		return BigDecimal.ZERO;
	}

	public static Long getFallnummerFromBGNummer(String bgNummer) {
		// 17.000120.003.1.2 -> 120 (long)
		return Long.valueOf(COMPILE.matcher(bgNummer.substring(3, 9)).replaceFirst(""));
	}

	public static int getYearFromBGNummer(String bgNummer) {
		// 17.000120.003.1.2 -> 17 (int)
		return Integer.valueOf(bgNummer.substring(0, 2)) + 2000;
	}

	public static int getGemeindeFromBGNummer(String bgNummer) {
		// 17.000120.003.1.2 -> 3 (int)
		return Integer.valueOf(bgNummer.split("\\.", -1)[2]);
	}

	public static int getKindNummerFromBGNummer(String bgNummer) {
		// 17.000120.003.1.2 -> 1 (int) can have more than 9 Kind
		return Integer.valueOf(bgNummer.split("\\.", -1)[3]);
	}

	public static int getBetreuungNummerFromBGNummer(String bgNummer) {
		// 17.000120.003.1.2 -> 2 (int)
		return Integer.valueOf(bgNummer.split("\\.", -1)[4]);
	}

	public static boolean validateBGNummer(String bgNummer) {
		return bgNummer.matches("^\\d{2}\\.\\d{6}.\\d{3}\\.\\d+\\.\\d+$");
	}

	@Nonnull
	public static BigDecimal calculateOeffnungszeitPerMonthProcentual(@Nonnull BigDecimal oeffnungszeitProJahr) {
		return MathUtil.EXACT.divide(
			MathUtil.EXACT.divide(oeffnungszeitProJahr, MathUtil.EXACT.from(12)),
			MathUtil.EXACT.from(100));
	}
}
