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

package ch.dvbern.ebegu.rechner;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.KitaxUebergangsloesungInstitutionOeffnungszeiten;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.kitax.KitaKitaxRechner;
import ch.dvbern.ebegu.rechner.kitax.TageselternKitaxRechner;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;

/**
 * Factory, welche für eine Betreuung den richtigen BG-Rechner ermittelt
 */
public final class BGRechnerFactory {

	private BGRechnerFactory() {
	}

	@Nullable
	public static AbstractRechner getRechner(@Nonnull AbstractPlatz betreuung, @Nonnull List<RechnerRule> rechnerRulesForGemeinde) {
		BetreuungsangebotTyp betreuungsangebotTyp = betreuung.getBetreuungsangebotTyp();
		if (BetreuungsangebotTyp.KITA == betreuungsangebotTyp) {
			return new KitaRechner(rechnerRulesForGemeinde);
		}
		if (BetreuungsangebotTyp.TAGESFAMILIEN == betreuungsangebotTyp) {
			return new TageselternRechner(rechnerRulesForGemeinde);
		}
		if (BetreuungsangebotTyp.TAGESSCHULE == betreuungsangebotTyp) {
			return new TagesschuleRechner();
		}
		// Alle anderen Angebotstypen werden nicht berechnet
		return null;
	}

	@Nullable
	public static AbstractRechner getKitaxRechner(
		@Nonnull AbstractPlatz betreuung,
		@Nonnull KitaxUebergangsloesungParameter kitaxParameterDTO,
		@Nonnull KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten,
		@Nonnull Locale locale
	) {
		BetreuungsangebotTyp betreuungsangebotTyp = betreuung.getBetreuungsangebotTyp();
		if (BetreuungsangebotTyp.KITA == betreuungsangebotTyp) {
			return new KitaKitaxRechner(kitaxParameterDTO, oeffnungszeiten, locale);
		}
		if (BetreuungsangebotTyp.TAGESFAMILIEN == betreuungsangebotTyp) {
			return new TageselternKitaxRechner(kitaxParameterDTO, oeffnungszeiten, locale);
		}
		if (BetreuungsangebotTyp.TAGESSCHULE == betreuungsangebotTyp) {
			// Tagesschulen werden von Anfang an mit dem ASIV-Rechner berechnet
			return new TagesschuleRechner();
		}
		// Alle anderen Angebotstypen werden nicht berechnet
		return null;
	}
}
