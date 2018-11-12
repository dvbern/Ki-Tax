package ch.dvbern.ebegu.pdfgenerator;

import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.*;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.*;
import static com.lowagie.text.Utilities.millimetersToPoints;

public class FinanzielleSituationPdfGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(FinanzielleSituationPdfGenerator.class);

	@Nonnull
	private final PdfGenerator pdfGenerator;

	public FinanzielleSituationPdfGenerator(final byte[] gemeindeLogo, final List<String> gemeindeHeader, final boolean draft) {
		this.pdfGenerator = PdfGenerator.create(gemeindeLogo, gemeindeHeader, draft);
	}

	@Nonnull
	public void generate(final OutputStream outputStream) throws InvoiceGeneratorException {

		final String title = "Berechnung der finanziellen Situation";
		final List<String> empfaengerAdresse = Arrays.asList(
			"Jugendamt",
			"Effingerstrasse 21",
			"3008 Bern");
		final String[][] intro1 = {
			{"Referenznummer", "18.000123.001.1.1"},
			{"Berechnungsjahr", "2017"}
		};
		final String[][] intro2 = {
			{"Referenznummer", "18.000123.001.1.1"},
			{"Ereigniseintritt", "01.01.2018"},
			{"Grund", "Neuer Job"}
		};
		final String[][] intro3 = {
			{"Referenznummer", "18.000123.001.1.1"},
			{"Name", "Dagmar Wälti"}
		};
		final float[] width1Gs = {10,2};
		final float[] width2Gs = {10,2,2};
		final int[] alignement1Gs = {Element.ALIGN_LEFT,Element.ALIGN_RIGHT};
		final int[] alignement2Gs = {Element.ALIGN_LEFT,Element.ALIGN_RIGHT, Element.ALIGN_RIGHT};
		final String[][] values2017_1 = {
			{"Einkünfte", "Dagmar Wälti"},
			{"Nettolohn gemäss Lohnausweis / Steuererklärung", "53'265.00"},
			{"Erhaltene Familienzulagen (sofern nicht bereits im Nettolohn vorhanden)", ""},
			{"Steuerpflichtiges Ersatzeinkommen (Leistungen aus AHV, IV, ALV, KV, UV, EO usw.)", ""},
			{"Erhaltene Unterhaltsbeiträge (Alimente)", ""},
			{"In der Steuererklärung ausgewiesener Geschäftsgewinn ¹", ""},
			{"Zwischentotal Einkünfte", "53'265.00"},
			{"Total Einkünfte", "53'265.00"}
		};
		final String[][] values2017_2 = {
			{"Nettovermögen", "Dagmar Wälti"},
			{"Bruttovermögen", "12'147.00"},
			{"Schulden", ""},
			{"Zwischentotal Nettovermögen", "12'147.00"},
			{"Zwischentotal Nettovermögen insgesamt ²", "12'147.00"},
			{"5% Nettovermögen", "607.00"}
		};
		final String[][] values2017_3 = {
			{"Abzüge", "Dagmar Wälti"},
			{"Bezahlte Unterhaltsbeiträge", ""},
			{"Total Abzüge", "0"}
		};
		final String[][] values2017_4 = {
			{"Zusammenzug", "Dagmar Wälti"},
			{"Total Einkünfte", "53'265.00"},
			{"5% Nettovermögen", "607.00"},
			{"Total Abzüge", "0.00"},
			{"Massgebendes Einkommen (vor Abzug für Familiengrösse)", "53'872.00"}
		};
		final String[][] values2018_1 = {
			{"Einkünfte", "Dagmar Wälti", "Simon Wälti"},
			{"Nettolohn gemäss Lohnausweis / Steuererklärung", "53'265.00", ""},
			{"Erhaltene Familienzulagen (sofern nicht bereits im Nettolohn vorhanden)", "", ""},
			{"Steuerpflichtiges Ersatzeinkommen (Leistungen aus AHV, IV, ALV, KV, UV, EO usw.)", "", ""},
			{"Erhaltene Unterhaltsbeiträge (Alimente)", "", ""},
			{"In der Steuererklärung ausgewiesener Geschäftsgewinn ¹", "", ""},
			{"Zwischentotal Einkünfte", "53'265.00", ""},
			{"Total Einkünfte", "53'265.00", ""}
		};
		final String[][] values2018_2 = {
			{"Nettovermögen", "Dagmar Wälti", "Simon Wälti"},
			{"Bruttovermögen", "12'147.00", ""},
			{"Schulden", "", ""},
			{"Zwischentotal Nettovermögen", "12'147.00", ""},
			{"Zwischentotal Nettovermögen insgesamt ²", "12'147.00", ""},
			{"5% Nettovermögen", "607.00", ""}
		};
		final String[][] values2018_3 = {
			{"Abzüge", "Dagmar Wälti", "Simon Wälti"},
			{"Bezahlte Unterhaltsbeiträge", "", ""},
			{"Total Abzüge", "0", ""}
		};
		final String[][] values2018_4 = {
			{"Zusammenzug", "Dagmar Wälti", "Simon Wälti"},
			{"Total Einkünfte", "53'265.00", ""},
			{"5% Nettovermögen", "607.00", ""},
			{"Total Abzüge", "0.00", ""},
			{"Massgebendes Einkommen (vor Abzug für Familiengrösse)", "53'872.00"}
		};
		final String[][] valuesMassgebendesEinkommen = {
			{"von", "bis", "Einkommensjahr", "massgebendes Einkommen vor Abzug der Familiengrösse", "Familiengrösse", "Abzug der Familiengrösse", "massgebendes Einkommen nach Abzug der Familiengrösse"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"}
		};
		final float[] widthMassgebendesEinkommen = {5,5,6,10,5,10,10};
		final int[] alignementMassgebendesEinkommen = {Element.ALIGN_RIGHT, Element.ALIGN_RIGHT,Element.ALIGN_RIGHT,Element.ALIGN_RIGHT,Element.ALIGN_RIGHT,Element.ALIGN_RIGHT,Element.ALIGN_RIGHT};
		final String fusszeile = "¹ Negative Jahresabschlüsse werden in der Berechnung des Durchschnitts berücksichtigt. Wenn der Gesamtwert negativ ist, " +
			"beträgt der zu berücksichtigende Wert 0 Franken.\n" +
			"² Vermögen und Schulden von Partnerin / Partner I und II werden miteinander verrechnet werden. Wenn der Gesamtwert " +
			"negativ ist, beträgt der zu berücksichtigende Wert 0 Franken.";

		pdfGenerator.generate(outputStream, title, empfaengerAdresse, (pdfGenerator, ctx) -> {
			Document document = pdfGenerator.getDocument();
			createFusszeile(pdfGenerator.getDirectContent(), fusszeile);
			document.add(PdfUtil.creatreIntroTable(intro1));
			document.add(PdfUtil.createTable(values2017_1, width1Gs, alignement1Gs, 1));
			document.add(PdfUtil.createTable(values2017_2, width1Gs, alignement1Gs, 1));
			document.add(PdfUtil.createTable(values2017_3, width1Gs, alignement1Gs, 1));
			document.add(PdfUtil.createTable(values2017_4, width1Gs, alignement1Gs, 1, true));
			document.newPage();
			document.add(PdfUtil.createBoldParagraph("Provisorisches Einkommen 2018", 2));
			createFusszeile(pdfGenerator.getDirectContent(), fusszeile);
			document.add(PdfUtil.creatreIntroTable(intro2));
			document.add(PdfUtil.createTable(values2018_1, width2Gs, alignement2Gs, 1));
			document.add(PdfUtil.createTable(values2018_2, width2Gs, alignement2Gs, 1));
			document.add(PdfUtil.createTable(values2018_3, width2Gs, alignement2Gs, 1));
			document.add(PdfUtil.createTable(values2018_4, width2Gs, alignement2Gs, 1, true));
			document.setPageSize(PageSize.A4.rotate());
			document.newPage();
			document.add(PdfUtil.createBoldParagraph("Massgebendes Einkommen nach Abzug der Familiengrösse", 2));
			document.add(PdfUtil.creatreIntroTable(intro3));
			document.add(PdfUtil.createTable(valuesMassgebendesEinkommen, widthMassgebendesEinkommen, alignementMassgebendesEinkommen, 0));
		});
	}

	private void createFusszeile(PdfContentByte dirPdfContentByte, String fusszeile) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(20);
		final float width = millimetersToPoints(170);
		final float loverLeftX = millimetersToPoints(PdfLayoutConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM);
		final float loverLeftY = millimetersToPoints(PdfLayoutConfiguration.LOGO_TOP_IN_MM / 4);
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtilities.createFontWithSize(8);
		fz.setText(new Phrase(fusszeile, fontWithSize));
		fz.go();
	}
}
