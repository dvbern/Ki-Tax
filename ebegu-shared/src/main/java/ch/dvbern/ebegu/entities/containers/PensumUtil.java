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

package ch.dvbern.ebegu.entities.containers;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.BetreuungUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class PensumUtil {

	public static void transformBetreuungsPensumContainers(@Nonnull BetreuungAndPensumContainer container) {
		container.findBetreuung()
			.filter(Betreuung::isAngebotMittagstisch)
			.ifPresent(b -> {
				container.getBetreuungenGS().forEach(PensumUtil::transformMittagstischPensum);
				container.getBetreuungenJA().forEach(PensumUtil::transformMittagstischPensum);
			});
	}

	public static void transformMittagstischPensum(@Nonnull AbstractMahlzeitenPensum pensum) {
		pensum.setTarifProNebenmahlzeit(BigDecimal.ZERO);
		pensum.setMonatlicheNebenmahlzeiten(BigDecimal.ZERO);
		pensum.setUnitForDisplay(PensumUnits.PERCENTAGE);
		pensum.setStuendlicheVollkosten(null);
		pensum.setPensum(BetreuungUtil.derivePensumMittagstisch(pensum));
		pensum.setMonatlicheBetreuungskosten(BetreuungUtil.deriveKostenMittagstisch(pensum));
	}
}
