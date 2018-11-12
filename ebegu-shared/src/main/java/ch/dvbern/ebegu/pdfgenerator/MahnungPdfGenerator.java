package ch.dvbern.ebegu.pdfgenerator;

import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class MahnungPdfGenerator {

	@Nonnull
	private final PdfGenerator pdfGenerator;

	public MahnungPdfGenerator(final byte[] gemeindeLogo, final List<String> gemeindeHeader, boolean draft) {
		this.pdfGenerator = PdfGenerator.create(gemeindeLogo, gemeindeHeader, draft);
	}

	@Nonnull
	public void generate(final OutputStream outputStream, boolean zweiteMahnung) throws InvoiceGeneratorException {

		final String title = "Gesuch für Simone Wälti\n" +
			"2018/2019 Referenznummer: 18.000126.001\n" +
			"Unvollständige Angaben/Unterlagen";
		final List<String> empfaengerAdresse = Arrays.asList(
			"Familie",
			"Muster Anna",
			"Muster Tina",
			"Nussbaumstrasse 35",
			"3006 Bern");
		final List<String> dokumente = Arrays.asList(
			"Verfügung zu Betreuungsangebot 18.000123.001.1.1",
			"Verfügung zu Betreuungsangebot 18.000123.001.1.2",
			"Verfügung zu Betreuungsangebot 18.000123.001.2.1",
			"Berechnung der finanziellen Situation");

		pdfGenerator.generate(outputStream, title, empfaengerAdresse, (pdfGenerator, ctx) -> {
			Document document = pdfGenerator.getDocument();
			document.add(PdfUtil.createParagraph("Sehr geehrte Familie"));
			if (zweiteMahnung) {
				document.add(PdfUtil.createParagraph("Sie haben von uns am 12.11.2018 eine Mahnung zur Vervollständigung Ihrer Anmeldung " +
					"erhalten.\n" +
					"Leider sind die eingereichten Angaben/Unterlagen immer noch nicht vollständig. Wir fordern Sie " +
					"daher ein letztes Mal auf, Ihr Gesuch bis am unter Angabe Ihrer Referenznummer mit den " +
					"nachfolgend aufgeführten Unterlagen zu ergänzen:"));
			} else {
				document.add(PdfUtil.createParagraph("Gerne bestätigen wir Ihnen, dass wir Ihr Gesuch für Simone Wälti (Weissenstein) und Simone " +
					"Wälti (Brünnen) am 15.02.2016 erhalten haben.\n\n" +
					"Leider sind die eingereichten Unterlagen gemäss einer ersten Vorprüfung unvollständig, daher " +
					"können wir die gewünschte Berechnung nicht vornehmen. Wir bitten Sie, Ihr Gesuch mit den " +
					"nachfolgend aufgeführten Unterlagen zu ergänzen:", 1));
			}
			document.add(PdfUtil.createList(dokumente));
			List<Element> seite2Paragraphs = Lists.newArrayList();
			if (zweiteMahnung) {
				seite2Paragraphs.add(PdfUtil.createParagraph("\nWenn Sie die geforderten Unterlagen nicht innerhalb der genannten Frist nachreichen, hat dies " +
					"je nach Betreuungsangebot eine Nichteintretensverfügung oder die Anwendung des " +
					"Maximaltarifs zur Folge.\n" +
					"Bitte wenden Sie sich an uns, falls Sie Fragen haben oder falls es Probleme mit der " +
					"Beschaffung der fehlenden Unterlagen gibt. Unsere Mitarbeitenden stehen Ihnen gerne " +
					"während der Bürozeiten zur Verfügung (Telefonnummer 031 321 51 15 und per E-Mail " +
					"kinderbetreuung@bern.ch)."));
			} else {
				seite2Paragraphs.add(PdfUtil.createParagraph("\nErst nach Eingang dieser zusätzlichen Unterlagen können wir Ihr Gesuch weiter bearbeiten. Wir" +
					"bitten Sie, die oben aufgeführten Dokumente bis am unter Angabe Ihrer Referenznummer " +
					"einzureichen.\n\n" +
				"Wenn Sie Fragen haben oder Probleme beim Beschaffen der Unterlagen, stehen Ihnen unsere " +
					"Mitarbeitenden gerne während der Bürozeiten zur Verfügung (Telefonnummer 031 321 51 15 " +
					"und per E-Mail kinderbetreuung@bern.ch)."));
			}
			seite2Paragraphs.add(PdfUtil.createParagraph("Wir danken Ihnen für Ihre Mitwirkung.\n"));
			seite2Paragraphs.add(PdfUtil.createParagraph("Freundliche Grüsse\n"));
			seite2Paragraphs.add(PdfUtil.createParagraph("\nsig. Xaver Weibel\nSachbearbeitung", 0));
			document.add(PdfUtil.createKeepTogetherTable(seite2Paragraphs, 1, 0));
		});
	}

}
