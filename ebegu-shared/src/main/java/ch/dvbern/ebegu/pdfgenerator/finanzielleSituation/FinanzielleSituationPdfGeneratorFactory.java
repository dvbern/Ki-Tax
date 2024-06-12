/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.pdfgenerator.finanzielleSituation;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.pdfgenerator.DokumentAnFamilieGenerator;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class FinanzielleSituationPdfGeneratorFactory {

	public static DokumentAnFamilieGenerator getGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum
	) {
		switch (gesuch.getFinSitTyp()) {
		case BERN_FKJV:
		case BERN:
			return new FinanzielleSituationPdfGeneratorBern(
				gesuch,
				verfuegungFuerMassgEinkommen,
				stammdaten,
				erstesEinreichungsdatum
			);
		case SCHWYZ:
			return new FinanzielleSituationPdfGeneratorSchwyz(
				gesuch,
				verfuegungFuerMassgEinkommen,
				stammdaten,
				erstesEinreichungsdatum
			);
		case SOLOTHURN:
				return new FinanzielleSituationPdfGeneratorSolothurn(
				gesuch,
				verfuegungFuerMassgEinkommen,
				stammdaten,
				erstesEinreichungsdatum
			);
		case LUZERN:
			return new FinanzielleSituationPdfGeneratorLuzern(
				gesuch,
				verfuegungFuerMassgEinkommen,
				stammdaten,
				erstesEinreichungsdatum
			);
		case APPENZELL:
		case APPENZELL_FOLGEMONAT:
			return new FinanzielleSituationPdfGeneratorAppenzell(
				gesuch,
				verfuegungFuerMassgEinkommen,
				stammdaten,
				erstesEinreichungsdatum
			);
			default:
			throw new EbeguRuntimeException("getGenerator", "No PDF Generator found for finSitTyp: " + gesuch.getFinSitTyp());
		}
	}
}
