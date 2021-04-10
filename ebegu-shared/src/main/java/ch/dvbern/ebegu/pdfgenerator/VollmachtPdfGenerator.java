/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.BaseGenerator;
import ch.dvbern.lib.invoicegenerator.OnPageHandler;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.google.common.collect.Lists;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import org.apache.commons.lang.StringUtils;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static com.lowagie.text.Utilities.millimetersToPoints;

public class VollmachtPdfGenerator extends BaseGenerator<VollmachtPdfLayoutConfiguration> {

	private static final String VOLLMACHT_TITLE = "PdfGeneration_Vollmacht_title";
	private static final String VOLLMACHT_SUBTITLE = "PdfGeneration_Vollmacht_subtitle";
	private static final String VOLLMACHT_ADRESSE_TITLE = "PdfGeneration_Vollmacht_adresse_title";
	private static final String VOLLMACHT_PARAGRAPH_1 = "PdfGeneration_Vollmacht_paragraph_1";
	private static final String VOLLMACHT_PARAGRAPH_2A = "PdfGeneration_Vollmacht_paragraph_2A";
	private static final String VOLLMACHT_PARAGRAPH_2B = "PdfGeneration_Vollmacht_paragraph_2B";
	private static final String VOLLMACHT_BEMERKUNG = "PdfGeneration_Vollmacht_bemerkung";
	private static final String VOLLMACHT_ORT_DATUM = "PdfGeneration_Vollmacht_ort_datum";
	private static final String VOLLMACHT_UNTERSCHRIFT = "PdfGeneration_Vollmacht_unterschrift";
	private static final String VOLLMACHT_FUSSZEILE_1 = "PdfGeneration_Vollmacht_fusszeile_1";

	private static final int SUPER_TEXT_SIZE = 6;
	private static final int SUPER_TEXT_RISE = 4;
	private static final int ABSENDER_Y_POSITION = 630;
	private static final int ABSENDER_2_X_POSITION = 100;
	private static final float ABSENDER_LINE_HEIGHT = PdfUtil.FONT_SIZE + 4;

	private static final String UNTERSCHRIFT_PLACEHOLDER = "X  ________________________________________________________";

	protected Locale sprache;
	private BaseGenerator<VollmachtPdfLayoutConfiguration> pdfGenerator;

	private SozialdienstFall sozialdienstFall;

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // Stimmt nicht, die Methode ist final
	public VollmachtPdfGenerator(Sprache sprache, @Nonnull SozialdienstFall sozialdienstFall) {
		initSprache(sprache);
		this.sozialdienstFall = sozialdienstFall;
		initGenerator();
	}

	private void initSprache(Sprache sprache) {
		if (sprache != null) {
			this.sprache = sprache.getLocale();
		} else {
			this.sprache = Sprache.DEUTSCH.getLocale();
		}
	}

	@Nonnull
	public PageConfiguration getPageConfiguration() {
		return pdfGenerator.getConfiguration();
	}

	@Nonnull
	protected BaseGenerator<VollmachtPdfLayoutConfiguration> getPdfGenerator() {
		return pdfGenerator;
	}

	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		OnPageHandler onPageHandler = new OnPageHandler(getPdfElementGenerator(), new ArrayList<>());
		getPdfGenerator().generate(outputStream, onPageHandler, (generator) -> {
			Document document = generator.getDocument();
			createContent(document, generator.getDirectContent());
		});
	}

	private void initGenerator() {
		this.pdfGenerator = new BaseGenerator<>(new VollmachtPdfLayoutConfiguration());
	}

	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			//document.add(createIntroAndInfoKontingentierung());
			createContent(document, generator.getDirectContent());
		};
	}

	public void createContent(
		@Nonnull final Document document,
		@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {

		createContentWhereIWant(
			dirPdfContentByte,
			translate(VOLLMACHT_TITLE),
			725,
			20,
			16,
			PdfUtil.DEFAULT_FONT_BOLD);

		createContentWhereIWant(
			dirPdfContentByte,
			translate(VOLLMACHT_SUBTITLE),
			690,
			20,
			11,
			PdfUtil.DEFAULT_FONT_BOLD);

		placeAbsender(dirPdfContentByte);
		placeAntragsteller2(dirPdfContentByte);

		Paragraph paragraph1 =
			PdfUtil.createParagraph(translate(VOLLMACHT_PARAGRAPH_1,
				this.sozialdienstFall.getSozialdienst().getName()),
				2,
				PdfUtil.DEFAULT_FONT);
		paragraph1.setSpacingAfter(15);
		document.add(paragraph1);
		Paragraph paragraph2 = PdfUtil.createParagraph(translate(VOLLMACHT_PARAGRAPH_2A), 2, PdfUtil.DEFAULT_FONT);
		paragraph2.add(PdfUtil.createSuperTextInText("1", SUPER_TEXT_SIZE, SUPER_TEXT_RISE));
		paragraph2.add(new Chunk(translate(VOLLMACHT_PARAGRAPH_2B)));
		paragraph2.setSpacingAfter(15);
		document.add(paragraph2);
		Paragraph bemerkung = PdfUtil.createParagraph(translate(VOLLMACHT_BEMERKUNG), 2, PdfUtil.DEFAULT_FONT);
		bemerkung.setSpacingAfter(45);
		document.add(bemerkung);
		Paragraph ortDatum = PdfUtil.createParagraph(translate(VOLLMACHT_ORT_DATUM), 2, PdfUtil.DEFAULT_FONT);
		ortDatum.setSpacingAfter(25);
		document.add(ortDatum);
		Paragraph unterschrift = PdfUtil.createParagraph(translate(VOLLMACHT_UNTERSCHRIFT), 2, PdfUtil.DEFAULT_FONT);
		unterschrift.setSpacingAfter(25);
		document.add(unterschrift);
		Paragraph unterschriftZeile1 = PdfUtil.createParagraph(UNTERSCHRIFT_PLACEHOLDER, 1, PdfUtil.DEFAULT_FONT);
		unterschrift.setSpacingAfter(0);
		document.add(unterschriftZeile1);
		if(isZweiAntragstellende()){
			Paragraph unterschriftZeile2 = PdfUtil.createParagraph(UNTERSCHRIFT_PLACEHOLDER, 2, PdfUtil.DEFAULT_FONT);
			unterschrift.setSpacingAfter(5);
			document.add(unterschriftZeile2);
		}
		createFusszeile(
			dirPdfContentByte,
			Lists.newArrayList(translate(VOLLMACHT_FUSSZEILE_1)),
			0, 0
		);
	}

	private void placeAbsender(
		@Nonnull PdfContentByte dirPdfContentByte
	) {
		float position = ABSENDER_Y_POSITION;
		for (String text : getAbsenderAdresse()) {
			createContentWhereIWant(
				dirPdfContentByte,
				text,
				position,
				20,
				PdfUtil.FONT_SIZE);
			position -= ABSENDER_LINE_HEIGHT;
		}
	}

	private void placeAntragsteller2(
		@Nonnull PdfContentByte dirPdfContentByte
	) {
		int position = ABSENDER_Y_POSITION;
		for (String text : getAbsenderAdresseAntragsteller2()) {
			createContentWhereIWant(
				dirPdfContentByte,
				text,
				position,
				ABSENDER_2_X_POSITION,
				PdfUtil.FONT_SIZE);
			position -= ABSENDER_LINE_HEIGHT;
		}
	}

	@Nonnull
	protected final List<String> getAbsenderAdresse() {
		List<String> absender = new ArrayList<>();
		absender.add(translate(VOLLMACHT_ADRESSE_TITLE));
		absender.add("");
		absender.add(this.sozialdienstFall.getName() + " " + this.sozialdienstFall.getVorname());
		absender.add(this.sozialdienstFall.getAdresse().getStrasseAndHausnummer());
		if (!StringUtils.isEmpty(this.sozialdienstFall.getAdresse().getZusatzzeile())) {
			absender.add(this.sozialdienstFall.getAdresse().getZusatzzeile());
		}
		absender.add(this.sozialdienstFall.getAdresse().getPlz() + " " + this.sozialdienstFall.getAdresse().getOrt());
		absender.add(Constants.DATE_FORMATTER.format(this.sozialdienstFall.getGeburtsdatum()));
		return absender;
	}

	@Nonnull
	protected final List<String> getAbsenderAdresseAntragsteller2() {
		List<String> absender = new ArrayList<>();
		if (isZweiAntragstellende()) {
			absender.add("");
			absender.add("");
			absender.add(this.sozialdienstFall.getNameGs2() + " " + this.sozialdienstFall.getVornameGs2());
			absender.add(Constants.DATE_FORMATTER.format(this.sozialdienstFall.getGeburtsdatum()));
		}
		return absender;
	}

	@Nonnull
	protected String translate(String key, Object... args) {
		return ServerMessageUtil.getMessage(key, sprache, args);
	}

	protected void createFusszeile(
		@Nonnull PdfContentByte dirPdfContentByte,
		@Nonnull List<String> content,
		int start,
		int anzeigeNummerStart
	) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(30);
		final float width = millimetersToPoints(170);
		final float loverLeftX = millimetersToPoints(PageConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM);
		final float loverLeftY = millimetersToPoints(PdfLayoutConfiguration.LOGO_TOP_IN_MM / 4.0f);
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtil.createFontWithSize(getPageConfiguration().getFonts().getFont(), 6.5f);
		for (int i = start; i < content.size(); i++) {
			Chunk chunk = new Chunk((i + anzeigeNummerStart + 1) + " ", PdfUtil.createFontWithSize(
				getPageConfiguration().getFonts().getFont(),
				5));
			chunk.setTextRise(2);
			fz.addText(chunk);
			fz.addText(new Phrase(content.get(i) + '\n', fontWithSize));
		}
		fz.go();
	}

	private void createContentWhereIWant(
		@Nonnull PdfContentByte dirPdfContentByte,
		String content,
		float y,
		float x,
		float size) {
		createContentWhereIWant(dirPdfContentByte, content, y, x, size, PdfUtil.DEFAULT_FONT);
	}

	/**
	 * Wenn man etwas ganz genau platzieren muss...
	 */
	protected void createContentWhereIWant(
		@Nonnull PdfContentByte dirPdfContentByte, String content, float y,
		float x, float size, Font font) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(20);
		final float width = millimetersToPoints(170);
		final float loverLeftX = millimetersToPoints(x);
		final float loverLeftY = y;
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtil.createFontWithSize(font, size);
		fz.addText(new Phrase(content + '\n', fontWithSize));
		fz.go();
	}

	private boolean isZweiAntragstellende() {
		if (this.sozialdienstFall.getNameGs2() != null && !this.sozialdienstFall.getNameGs2().isEmpty()) {
			return true;
		}
		return false;
	}
}
