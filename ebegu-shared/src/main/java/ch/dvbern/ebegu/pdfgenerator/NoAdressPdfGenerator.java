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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdatenKorrespondenz;
import ch.dvbern.lib.invoicegenerator.dto.BaseLayoutConfiguration;

public class NoAdressPdfGenerator extends PdfGenerator{

	@Nonnull
	private final BaseLayoutConfiguration configuration;

	public NoAdressPdfGenerator(@Nonnull PdfLayoutConfiguration configuration) {
		super(configuration);
		this.configuration = configuration;
	}

	@Nonnull
	public static NoAdressPdfGenerator create() {
		PdfLayoutConfiguration layoutConfiguration = new PdfLayoutConfiguration(new GemeindeStammdatenKorrespondenz(), new ArrayList<>(), false);
		layoutConfiguration.setFooter(null);
		layoutConfiguration.setHeader(null);
		// Die Default-Schriften aus der Library ueberschreiben
		layoutConfiguration.getFonts().setFont(PdfUtil.DEFAULT_FONT);
		layoutConfiguration.getFonts().setFontBold(PdfUtil.DEFAULT_FONT_BOLD);
		layoutConfiguration.getFonts().setFontTitle(PdfUtil.FONT_TITLE);
		layoutConfiguration.getFonts().setFontH1(PdfUtil.FONT_H1);
		layoutConfiguration.getFonts().setFontH2(PdfUtil.FONT_H2);
		layoutConfiguration.setTopMarginInPoints(PdfUtil.NO_ADRESS_MARGIN_TOP);
		return new NoAdressPdfGenerator(layoutConfiguration);
	}
}
