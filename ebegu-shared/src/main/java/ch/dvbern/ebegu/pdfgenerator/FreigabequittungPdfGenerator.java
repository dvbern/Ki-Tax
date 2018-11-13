package ch.dvbern.ebegu.pdfgenerator;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT_SIZE;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.FULL_WIDTH;

public class FreigabequittungPdfGenerator {

	@Nonnull
	private final PdfGenerator pdfGenerator;

	private static final Logger LOG = LoggerFactory.getLogger(FreigabequittungPdfGenerator.class);

	public  FreigabequittungPdfGenerator(final byte[] gemeindeLogo, final List<String> gemeindeHeader, final boolean draft) {
		this.pdfGenerator = PdfGenerator.create(gemeindeLogo, gemeindeHeader, draft);
	}

	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {

		final String title = "Freigabequittung für die Periode 2018/2019";
		final List<String> empfaengerAdresse = Arrays.asList(
			"Familie",
			"Anna Muster",
			"Max Muster",
			"Nussbaumstrasse 35",
			"3006 Bern");
		final List<String> gesuchstellerNamen = Arrays.asList(
			"Anna Muster",
			"Max Muster");
		final List<String> gesuchsteller = Arrays.asList(
			"Anna Muster\nNussbaumstrasse 35\n3006 Bern",
			"Max Muster\nNussbaumstrasse 35\n3006 Bern");
		String[] angebot1 = {"Simon Muster", "Weissenstein (Kita)", "18.000117.001"};
		String[] angebot2 = {"Simon Muster", "Brünnen (Kita)", "18.000117.002"};
		final List<String[]> angebote = Arrays.asList(angebot1, angebot2);
		final List<String> dokumente = Arrays.asList(
			"Arbeitsvertrag",
			"Steuerveranlagung");
		pdfGenerator.generate(outputStream, title, empfaengerAdresse, (generator, ctx) -> {
			Document document = generator.getDocument();
			addBarcode(document);
			document.add(createGesuchstellerTable("18.000117.001", gesuchsteller));
			document.add(PdfUtil.createSubTitle("Betreuungsangebote"));
			document.add(createBetreuungsangeboteTable(angebote));
			document.add(PdfUtil.createSubTitle("Benötigte Unterlagen"));
			Paragraph dokumenteParagraph = new Paragraph();
			dokumenteParagraph.setSpacingAfter(1 * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
			dokumenteParagraph.add(PdfUtil.createList(dokumente));
			document.add(dokumenteParagraph);
			List<Element> seite2Paragraphs = Lists.newArrayList();
			seite2Paragraphs.add(PdfUtil.createSubTitle("Einwilligung zur Auskunftserteilung zu Steuerdaten"));
			seite2Paragraphs.add(PdfUtil.createParagraph("Die unterzeichnende/n Person/en erteilt/erteilen der Steuerverwaltung der Stadt Bern und " +
				"der/n für die oben aufgeführten Betreuungsangebote zuständigen Stelle/n die Einwilligung, " +
				"einander wechselseitig schriftlich und mündlich die zur Berechnung des massgebenden " +
				"Einkommens notwendigen Informationen zukommen zu lassen. Sie entbindet/entbinden dazu " +
				"die Steuerverwaltung der Stadt Bern von der Geheimhaltungspflicht gemäss Artikel 110 des " +
				"Bundesgesetzes über die direkte Bundessteuer (DBG, SR 642.11) und Artikel 153 des " +
				"Steuergesetzes des Kantons Bern (StG, BSG 661.11)."));
			seite2Paragraphs.add(new Paragraph());
			seite2Paragraphs.add(PdfUtil.createSubTitle("Kenntnissnahme"));
			seite2Paragraphs.add(PdfUtil.createParagraph("Änderungen der massgebenden Verhältnisse (z.B. Familiengrösse, Wegzug oder Ihr " +
				"Erwerbspensum, falls erforderlich) sind unaufgefordert und unverzüglich zu melden. Ihre " +
				"Mitteilung können Sie uns direkt übe r Ki-Tax oder an die im Briefkopf aufgeführte Dienststelle " +
				"zukommen lassen."));
			seite2Paragraphs.add(PdfUtil.createParagraph("Ihre Anmeldung gilt erst als eingereicht, wenn Sie die unterschriebene Freigabequittung " +
				"zusammen mit den aufgeführten Belegen an die obenstehende Adresse zugestellt haben"));
			seite2Paragraphs.add(PdfUtil.createParagraph("Ich bestätige / Wir bestätigen, dass alle erforderlichen Angaben vollständig und " +
				"wahrheitsgemäss erfasst sind.", 0));
			seite2Paragraphs.add(createUnterschriftenTable(gesuchstellerNamen));
			document.add(PdfUtil.createKeepTogetherTable(seite2Paragraphs, 1, 0));
		});
	}

	@Nonnull
	public static PdfPTable createGesuchstellerTable(@Nonnull final String referenzNummer, List<String> gesuchsteller) {
		PdfPTable table = new PdfPTable(3);
		table.setSpacingBefore(0);
		table.setWidthPercentage(FULL_WIDTH);
		table.setKeepTogether(true);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.getDefaultCell().setPadding(0);
		table.getDefaultCell().setLeading(0,PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		table.getDefaultCell().setPaddingBottom(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		table.addCell(new Phrase("Referenznummer", DEFAULT_FONT));
		table.addCell(new Phrase(referenzNummer, DEFAULT_FONT));
		table.addCell(new Phrase());
		table.addCell(new Phrase("Gesuchsteller/in", DEFAULT_FONT));
		gesuchsteller.forEach(item-> table.addCell(new Phrase(item, DEFAULT_FONT)));
		return table;
	}

	public static void addBarcode(Document document) {
		try {
			Image image = Image.getInstance(IOUtils.toByteArray(FreigabequittungPdfGenerator.class.getResourceAsStream("barcode.png")));
			image.setAbsolutePosition(document.leftMargin(), document.getPageSize().getHeight() - 2 * Utilities.millimetersToPoints(PdfLayoutConfiguration.LOGO_TOP_IN_MM));
			document.add(image);
		} catch (Exception e) {
			LOG.error("Failed to read the Logo: {}", e.getMessage());
		}
	}

	@Nonnull
	public static PdfPTable createBetreuungsangeboteTable(@Nonnull List<String[]> angebote) {
		PdfPTable table = new PdfPTable(3);
		table.setWidthPercentage(FULL_WIDTH);
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(PdfUtil.createTitleCell("Kind"));
		table.addCell(PdfUtil.createTitleCell("Institution"));
		table.addCell(PdfUtil.createTitleCell("BG-Nummer"));
		angebote.forEach(angebot->{
			table.addCell(new Phrase(angebot[0], DEFAULT_FONT));
			table.addCell(new Phrase(angebot[1], DEFAULT_FONT));
			table.addCell(new Phrase(angebot[2], DEFAULT_FONT));
		});
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		return table;
	}

	@Nonnull
	public static PdfPTable createUnterschriftenTable(@Nonnull final List<String> gesuchsteller) {
		PdfPTable table = new PdfPTable(2);
		table.setSpacingBefore(0);
		table.setWidthPercentage(FULL_WIDTH);
		table.setKeepTogether(true);
		table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		table.getDefaultCell().setPadding(0);
		table.getDefaultCell().setPaddingTop(4 * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		gesuchsteller.forEach(item->{
			table.addCell(new Phrase("Ort, Datum", DEFAULT_FONT));
			table.addCell(new Phrase(item, DEFAULT_FONT));
		});
		return table;
	}
}
