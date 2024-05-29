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

package ch.dvbern.ebegu.pdfgenerator;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class BegleitschreibenPdfGeneratorVisitor  implements MandantVisitor<BegleitschreibenPdfGenerator> {

	private Gesuch gesuch;
	private GemeindeStammdaten gemeindeStammdaten;


	public BegleitschreibenPdfGeneratorVisitor(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten gemeindeStammdaten
	) {
		this.gesuch = gesuch;
		this.gemeindeStammdaten = gemeindeStammdaten;
	}
	@Override
	public BegleitschreibenPdfGenerator visitBern() {
		return new BegleitschreibenPdfGenerator(gesuch, gemeindeStammdaten);
	}

	@Override
	public BegleitschreibenPdfGenerator visitLuzern() {
		return new BegleitschreibenPdfGenerator(gesuch, gemeindeStammdaten);
	}

	@Override
	public BegleitschreibenPdfGenerator visitSolothurn() {
		return new BegleitschreibenPdfGenerator(gesuch, gemeindeStammdaten);
	}

	@Override
	public BegleitschreibenPdfGenerator visitAppenzellAusserrhoden() {
		return new BegleitschreibenPdfGenerator(gesuch, gemeindeStammdaten);
	}

	@Override
	public BegleitschreibenPdfGenerator visitSchwyz() {
		return new BegleitschreibenPdfGeneratorSchwyz(gesuch, gemeindeStammdaten);
	}
}
