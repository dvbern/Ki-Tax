/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.util;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;

public final class BetreuungUtil {

	private BetreuungUtil() {
	}

	public static boolean hasDuplicateBetreuung(JaxBetreuung betreuungJAXP, @Nullable Set<Betreuung> betreuungen) {
		if (betreuungen != null) {
			return betreuungen.stream().anyMatch(betreuung -> {
				if (!Objects.equals(betreuung.getId(), betreuungJAXP.getId())) {
					return !betreuung.getBetreuungsstatus().isStorniert() &&
						isSameInstitution(betreuungJAXP, betreuung);
				}
				return false;
			});
		}
		return false;
	}

	public static boolean hasDuplicateAnmeldungTagesschule(JaxBetreuung betreuungJAXP, @Nullable Set<AnmeldungTagesschule> betreuungen) {
		if (betreuungen != null) {
			return betreuungen.stream().anyMatch(betreuung -> {
				if (!Objects.equals(betreuung.getId(), betreuungJAXP.getId())) {
					return !betreuung.getBetreuungsstatus().isSchulamtAnmeldungStorniert() &&
						isSameInstitution(betreuungJAXP, betreuung);
				}
				return false;
			});
		}
		return false;
	}

	public static boolean hasDuplicateAnmeldungFerieninsel(JaxBetreuung betreuungJAXP, @Nullable Set<AnmeldungFerieninsel> betreuungen) {
		if (betreuungen != null) {
			return betreuungen.stream().anyMatch(betreuung -> {
				if (!Objects.equals(betreuung.getId(), betreuungJAXP.getId())) {
					return !betreuung.getBetreuungsstatus().isStorniert() &&
						isSameInstitution(betreuungJAXP, betreuung) &&
						isSameFerien(betreuungJAXP, betreuung);
				}
				return false;
			});
		}
		return false;
	}

	private static boolean isSameFerien(JaxBetreuung betreuungJAXP, AnmeldungFerieninsel betreuung) {
		Objects.requireNonNull(betreuung.getBelegungFerieninsel());
		Objects.requireNonNull(betreuungJAXP.getBelegungFerieninsel());
		return betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp() == BetreuungsangebotTyp.FERIENINSEL &&
			Objects.equals(betreuung.getBelegungFerieninsel().getFerienname(), betreuungJAXP.getBelegungFerieninsel().getFerienname());
	}

	private static boolean isSameInstitution(JaxBetreuung betreuungJAXP, AbstractPlatz betreuung) {
		return betreuung.getInstitutionStammdaten().getId().equals(betreuungJAXP.getInstitutionStammdaten().getId());
	}
}
