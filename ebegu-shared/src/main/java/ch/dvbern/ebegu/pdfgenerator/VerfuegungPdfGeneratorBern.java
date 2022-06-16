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

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import org.jetbrains.annotations.Nullable;

public class VerfuegungPdfGeneratorBern extends AbstractVerfuegungPdfGenerator {

	public VerfuegungPdfGeneratorBern(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte) {
		super(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte);
	}

	protected void addGruesseElements(@Nonnull List<Element> gruesseElements) {
		gruesseElements.add(createParagraphGruss());
		gruesseElements.add(createParagraphSignatur());
	}

	@Override
	protected void createDokumentNichtEintretten(
		@Nonnull Document document,
		@Nonnull PdfGenerator generator) {
		createDokumentNichtEintrettenDefault(document, generator);
	}

	@Override
	protected void addTitleGutscheinProStunde(PdfPTable table) {
		//defualt no-op: wird nur in Luzern angezeigt
	}

	@Override
	protected void addValueGutscheinProStunde(
		PdfPTable table,
		@Nullable BigDecimal verguenstigungProZeiteinheit) {
		//defualt no-op: wird nur in Luzern angezeigt
	}

	@Override
	protected float[] getVerfuegungColumnWidths() {
		return DEFAULT_COLUMN_WIDTHS_VERFUEGUNG_TABLE;
	}

	@Override
	protected Font getBgColorForUeberwiesenerBetragCell() {
		return fontTabelle;
	}
}
