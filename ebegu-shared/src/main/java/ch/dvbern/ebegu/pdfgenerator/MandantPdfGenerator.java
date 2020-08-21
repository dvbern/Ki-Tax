/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static com.lowagie.text.Utilities.millimetersToPoints;

public abstract class MandantPdfGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(MandantPdfGenerator.class);

	protected static final String DIREKTION = "PdfGeneration_ProvisorischeVerfuegung_Direktion";
	protected static final String AMT = "PdfGeneration_ProvisorischeVerfuegung_Amt";
	protected static final String DIVISION = "PdfGeneration_ProvisorischeVerfuegung_Division";
	protected static final String ABSENDER_TELEFON = "PdfGeneration_Telefon";
	protected static final String EINSCHREIBEN = "PdfGeneration_VerfuegungEingeschrieben"; //wird bei der Definitiv
	// verwendet werden
	protected Locale sprache;

	@Nonnull
	private PdfGenerator pdfGenerator;

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // Stimmt nicht, die Methode ist final
	public MandantPdfGenerator(Sprache sprache) {
		byte[] mandantLogo = new byte[0];
		try {
			mandantLogo = IOUtils.toByteArray(MandantPdfGenerator.class.getResourceAsStream(
				"/pdfgenerator/KantonBernLogo.png"));
		}
		catch (IOException e) {
			LOG.error("KantonBernLogo.png koennte nicht geladen werden: {}", e.getMessage());
		}
		initSprache(sprache);
		initGenerator(mandantLogo);
	}

	private void initSprache(Sprache sprache){
		if(sprache != null) {
			this.sprache = sprache.getLocale();
		} else{
			this.sprache = Sprache.DEUTSCH.getLocale();
		}
	}

	@Nonnull
	public PageConfiguration getPageConfiguration() {
		return pdfGenerator.getConfiguration();
	}

	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}

	@Nonnull
	protected abstract String getDocumentTitle();

	@Nonnull
	protected abstract List<String> getEmpfaengerAdresse();

	@Nonnull
	protected abstract CustomGenerator getCustomGenerator();

	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(outputStream, getDocumentTitle(), getEmpfaengerAdresse(), getCustomGenerator());
	}

	private void initGenerator(@Nonnull final byte[] mandantLogo) {
		this.pdfGenerator = PdfGenerator.create(mandantLogo, getAbsenderAdresse(), true);
	}

	@Nonnull
	protected final List<String> getAbsenderAdresse() {
		List<String> absender = new ArrayList<>();
		absender.add(getMandantAddressAsString());
		absender.addAll(getMandantKontaktdaten());
		return absender;
	}

	@Nonnull
	public String getMandantAddressAsString() {
		StringBuilder sb = new StringBuilder();
		sb.append(translate(DIREKTION));
		sb.append(Constants.LINE_BREAK);
		sb.append(translate(AMT));
		sb.append(Constants.LINE_BREAK);
		sb.append(translate(DIVISION));
		sb.append(Constants.LINE_BREAK);
		sb.append(Constants.LINE_BREAK);
		sb.append("Rathausgasse 1");
		sb.append(Constants.LINE_BREAK);
		sb.append("Postfach");
		sb.append(Constants.LINE_BREAK);
		sb.append("3000 Bern 8");
		sb.append(Constants.LINE_BREAK);
		return sb.toString();
	}

	@Nonnull
	private List<String> getMandantKontaktdaten() {
		String email = "info.fam@be.ch";
		String telefon = "+41 31 633 78 83";
		String webseite = "www.be.ch/gsi";
		return Arrays.asList(
			translate(ABSENDER_TELEFON, telefon),
			PdfUtil.printString(email),
			PdfUtil.printString(webseite),
			"",
			""
		);
	}

	@Nonnull
	protected String translate(String key, Object... args) {
		return ServerMessageUtil.getMessage(key, sprache, args);
	}

	protected void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte, List<String> content, int start) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(30);
		final float width = millimetersToPoints(170);
		final float loverLeftX = millimetersToPoints(PageConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM);
		final float loverLeftY = millimetersToPoints(PdfLayoutConfiguration.LOGO_TOP_IN_MM / 4.0f);
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtilities.createFontWithSize(getPageConfiguration().getFont(), 6.5f);
		for (int i = start; i < content.size(); i++) {
			Chunk chunk = new Chunk((i + 1) + " ", PdfUtilities.createFontWithSize(getPageConfiguration().getFont(),
				5));
			chunk.setTextRise(2);
			fz.addText(chunk);
			fz.addText(new Phrase(content.get(i) + '\n', fontWithSize));
		}
		fz.go();
	}

	/**
	 * Wenn man etwas ganz genau platzieren muss...
	 * @param dirPdfContentByte
	 * @param content
	 * @param y
	 * @param x
	 * @param font
	 * @param size
	 * @throws DocumentException
	 */
	protected void createContentWhereIWant(@Nonnull PdfContentByte dirPdfContentByte, String content, float y,
		float x, Font font, float size) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(20);
		final float width = millimetersToPoints(75);
		final float loverLeftX = millimetersToPoints(x);
		final float loverLeftY = y;
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtilities.createFontWithSize(font, size);
		fz.addText(new Phrase(content + '\n', fontWithSize));
		fz.go();
	}
}
