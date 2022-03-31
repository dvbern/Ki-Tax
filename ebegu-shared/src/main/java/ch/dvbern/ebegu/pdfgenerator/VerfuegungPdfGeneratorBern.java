/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import com.lowagie.text.Font;

public class VerfuegungPdfGeneratorBern extends AbstractVerfuegungPdfGenerator {

	public VerfuegungPdfGeneratorBern(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, boolean kontingentierungEnabledAndEntwurf, boolean stadtBernAsivConfigured) {
		super(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured);
	}

	@Override
	protected Font getBgColorForUeberwiesenerBetragCell() {
		return fontTabelle;
	}

	@Override
	protected String getTextGutschein() {
		return translate(GUTSCHEIN_AN_INSTITUTION);
	}
}