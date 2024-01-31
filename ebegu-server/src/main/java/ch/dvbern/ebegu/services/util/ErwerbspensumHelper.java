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

package ch.dvbern.ebegu.services.util;

import java.util.Objects;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;

public final class ErwerbspensumHelper {

	private ErwerbspensumHelper() {
	}

	public static boolean isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(Familiensituation familiensituation, Gesuchsperiode gesuchsperiode) {
		if (familiensituation.getFamilienstatus() != EnumFamilienstatus.KONKUBINAT_KEIN_KIND) {
			return false;
		}
		return familiensituation.isKonkubinatReachingMinDauerIn(gesuchsperiode)
			&& Objects.equals(familiensituation.getGeteilteObhut(), Boolean.FALSE)
			&& familiensituation.getUnterhaltsvereinbarung() == UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
	}
}
