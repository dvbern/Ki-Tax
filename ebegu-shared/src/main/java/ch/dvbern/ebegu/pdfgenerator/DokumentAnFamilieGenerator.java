/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.Element;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static ch.dvbern.ebegu.pdfgenerator.PdfUtil.DEFAULT_FONT_SIZE;
import static com.lowagie.text.Utilities.millimetersToPoints;

public abstract class DokumentAnFamilieGenerator extends KibonPdfGenerator {

	protected static final String ANREDE_HERR = "PdfGeneration_AnredeHerr";
	protected static final String ANREDE_FRAU = "PdfGeneration_AnredeFrau";
	protected static final String SACHBEARBEITUNG = "PdfGeneration_Sachbearbeitung";

	private static final String GRUSS = "PdfGeneration_Gruss";
	private static final String SIGNIERT = "PdfGeneration_Signiert";

	protected DokumentAnFamilieGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		super(gesuch, stammdaten);
	}

	@Nonnull
	@Override
	protected List<String> getEmpfaengerAdresse() {
		return getFamilieAdresse();
	}

	@Nonnull
	protected Paragraph createParagraphGruss() {
		return PdfUtil.createParagraph(translate(GRUSS), 2);
	}

	@Nonnull
	protected Paragraph createAnrede() {
		final StringBuilder anrede = new StringBuilder();
		addAnrede(anrede, gesuch.getGesuchsteller1(), true);
		if (hasSecondGesuchsteller()) {
			addAnrede(anrede, gesuch.getGesuchsteller2(), false);
		}
		return PdfUtil.createParagraph(anrede.toString());
	}

	private void addAnrede(@Nonnull final StringBuilder anrede, @Nullable final GesuchstellerContainer gesuchsteller, final boolean first) {
		boolean isFrench = "fr".equalsIgnoreCase(sprache.getLanguage());
		if (gesuchsteller != null) {
			final String singleAnrede = gesuchsteller.getGesuchstellerJA().getGeschlecht() == Geschlecht.MAENNLICH ? translate(ANREDE_HERR) : translate(ANREDE_FRAU);
			if (first) {
				anrede.append(singleAnrede);
			} else {
				if (!singleAnrede.isEmpty()) {
					if (isFrench) {
						anrede.append(' ');
						anrede.append(singleAnrede);
					} else {
						anrede.append(", ");
						anrede.append(Character.toLowerCase(singleAnrede.charAt(0)));
						anrede.append(singleAnrede.substring(1));
					}
				}
			}
			// Auf Französisch schreibt man nur "Monsieur, Madame,"
			if (!isFrench) {
				anrede.append(' ');
				anrede.append(gesuchsteller.getGesuchstellerJA().getNachname());
			}
		}
	}

	@Nonnull
	protected Element createParagraphSignatur() {
		if (gemeindeStammdaten.getStandardDokSignature() || !isVerfuegung()) {
			String signiert = getSachbearbeiterSigniert();
			if (signiert != null) {
				return PdfUtil.createParagraph('\n' + signiert + '\n' + translate(SACHBEARBEITUNG), 2);
			}
			return PdfUtil.createParagraph(translate(SACHBEARBEITUNG), 2);
		} else {
			return createAlternativSignatureTable();
		}
	}

	public boolean isVerfuegung() {
		return false;
	}

	@Nonnull
	public PdfPTable createAlternativSignatureTable() {
		PdfPTable table = new PdfPTable(3);
		// Init
		PdfUtil.setTableDefaultStyles(table);
		table.getDefaultCell().setPaddingBottom(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		PdfPCell titelCell = new PdfPCell(new Phrase(gemeindeStammdaten.getStandardDokTitle(),
			getPageConfiguration().getFonts().getFontBold()));
		titelCell.setPaddingBottom(2);
		titelCell.setPaddingLeft(0);
		titelCell.setBorder(0);
		table.addCell(titelCell);
		final Font defaultFont = getPageConfiguration().getFonts().getFont();
		table.addCell(new Phrase("", defaultFont));
		table.addCell(new Phrase("", defaultFont));
		PdfPCell unterschriftTitelCell = new PdfPCell(new Phrase(gemeindeStammdaten.getStandardDokUnterschriftTitel(),
			defaultFont));
		unterschriftTitelCell.setPaddingBottom(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE * 2);
		unterschriftTitelCell.setPaddingLeft(0);
		unterschriftTitelCell.setBorder(0);
		table.addCell(unterschriftTitelCell);
		table.addCell(new Phrase(gemeindeStammdaten.getStandardDokUnterschriftTitel2(),
			defaultFont));
		table.addCell(new Phrase("", defaultFont));
		table.addCell(new Phrase(gemeindeStammdaten.getStandardDokUnterschriftName(),
			defaultFont));
		table.addCell(new Phrase(gemeindeStammdaten.getStandardDokUnterschriftName2(),
			defaultFont));
		table.addCell(new Phrase("", defaultFont));
		return table;
	}

	@Nullable
	private String getSachbearbeiterSigniert() {
		Benutzer hauptVerantwortlicher = getGesuch().getVerantwortlicherAccordingToBetreuungen();
		return hauptVerantwortlicher != null ? translate(SIGNIERT, hauptVerantwortlicher.getFullName()) : null;
	}

	protected void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte, List<String> content) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(13);
		final float width = millimetersToPoints(170);
		final float loverLeftX = millimetersToPoints(PageConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM);
		final float loverLeftY = millimetersToPoints(PdfLayoutConfiguration.LOGO_TOP_IN_MM / 4.0f);
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		final Font defaultFont = getPageConfiguration().getFonts().getFont();
		Font fontWithSize = PdfUtil.createFontWithSize(defaultFont, 8);
		for (int i = 0; i < content.size(); i++) {
			Chunk chunk = new Chunk((i + 1) + " ", PdfUtil.createFontWithSize(defaultFont, 6));
			chunk.setTextRise(2);
			fz.addText(chunk);
			fz.addText(new Phrase(content.get(i) + '\n', fontWithSize));
		}
		fz.go();
	}
}
