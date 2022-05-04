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

package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.IntegrationTyp;

public class LuzernKindDokumente extends BernKindDokumente {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale
	) {
		super.getAllDokumente(gesuch, anlageVerzeichnis, locale);
	}

	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable Kind kind) {

		// Für Luzern muss eine andere Prüfung gemacht werden, ob eine Fachstellenbestätigung gefordert ist als für Bern
		// da Luzern keine Dokumente für die sprachliche Integration fordert
		if(dokumentTyp == DokumentTyp.FACHSTELLENBESTAETIGUNG) {
			if(kind != null && kind.getPensumFachstelle() != null) {
				return kind.getPensumFachstelle().getIntegrationTyp() != IntegrationTyp.SPRACHLICHE_INTEGRATION;
			}

			return false;
		}

		return super.isDokumentNeeded(dokumentTyp, kind);
	}

}
