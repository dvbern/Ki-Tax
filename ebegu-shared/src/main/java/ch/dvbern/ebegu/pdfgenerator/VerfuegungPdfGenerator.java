package ch.dvbern.ebegu.pdfgenerator;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.lib.invoicegenerator.dto.Alignment;
import ch.dvbern.lib.invoicegenerator.dto.OnPage;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.component.PhraseRenderer;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.FULL_WIDTH;

public class VerfuegungPdfGenerator {

	public enum Art {
		NORMAL,
		KEIN_ANSPRUCH,
		NICHT_EINTRETTEN
	}

	@Nonnull
	private final PdfGenerator pdfGenerator;

	@Nonnull
	private final PhraseRenderer footer;

	private final List<String> footerLines = Arrays.asList(
		"¹ Gesetz vom 23. Mai 1989 über die Verwaltungsrechtspflege (VRPG; BSG 155.21)",
		"² Verordnung vom 6. November 2013 über die familienergänzende Betreuung von Kindern und Jugendlichen (Betreuungsverordnung; " +
			"FEBVO; SSSB 862.311)");

	public VerfuegungPdfGenerator(final byte[] gemeindeLogo, final List<String> gemeindeHeader, boolean draft) {

		footer = new PhraseRenderer(footerLines, PageConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM, 280,
			165, 20, OnPage.FIRST, 8, Alignment.LEFT);
		this.pdfGenerator = PdfGenerator.create(gemeindeLogo, gemeindeHeader, footer, draft);
	}

	public void generate(final OutputStream outputStream, final Art art) throws InvoiceGeneratorException {
		final String[][] daten = {
			{"von", "bis", "effektive Betreuung", "Anspruch", "vergünstigt", "Vollkosten in CHF", "Vergünstigung in CHF", "Elternbeitrag in CHF"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"},
			{"01.08.2019", "31.08.2019", "100.00%", "80%", "80.00%", "2'000", "725.65", "1'274.35"}
		};

		final String title = "Verfügung";
		final List<String> empfaengerAdresse = Arrays.asList(
			"Familie",
			"Anna Muster",
			"Max Muster",
			"Nussbaumstrasse 35",
			"3006 Bern");
		final String[][] intro = {
			{"Referenznummer", "18.000123.001.1.1"},
			{"Name", "Dagmar Wälti"},
			{"Angebot", "Tagesstätte für Kleinkinder"},
			{"Institution", "Brünnen"},
		};
		final List<String> bemerkungen = Arrays.asList(
			"[01.08.2018 - 31.01.2019] RESTANSPRUCH: Anspruch nach unten korrigiert von 80% auf 0% da das Kind weitere Betreuungsangebote beansprucht"
			);
		pdfGenerator.generate(outputStream, title, empfaengerAdresse, (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(PdfUtil.creatreIntroTable(intro));
			document.add(PdfUtil.createParagraph("Sehr geehrte Familie"));
			createContent(document, daten, bemerkungen, art);
		});
	}

	public void createContent(@Nonnull final Document document, String[][] daten, List<String> bemerkungen, Art art) throws DocumentException {
		List<Element> bemerkungenElements = Lists.newArrayList();
		List<Element> gruesseElements = Lists.newArrayList();
		switch (art) {
			case NORMAL:
				footer.setPayload(Collections.emptyList());
				document.add(PdfUtil.createParagraph("Für die Betreuung von Simon Wälti, geboren am 13.04.2014, gewähren wir Ihnen einen " +
					"Betreuungsgutschein mit nachfolgender monatlicher Vergünstigung:", 2));
				float[] columnWidths = {10, 10, 10, 10, 10, 10, 12, 12};
				int[] alignement = {Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT, Element.ALIGN_RIGHT };
				document.add(PdfUtil.createTable(daten, columnWidths, alignement, 2));
				bemerkungenElements.add(PdfUtil.createParagraph("Bemerkungen:"));
				bemerkungenElements.add(PdfUtil.createList(bemerkungen));
				document.add(PdfUtil.createKeepTogetherTable(bemerkungenElements, 0, 2));
				break;
			case KEIN_ANSPRUCH:
				footer.setPayload(Collections.emptyList());
				document.add(PdfUtil.createParagraph("Simone Wälti, geboren am 13.04.2014, hat für den Zeitraum von 01.08.2018 bis 31.07.2019\n" +
					"keinen Anspruch auf einen Betreuungsgutschein.", 2));
				bemerkungenElements.add(PdfUtil.createParagraph("Bemerkungen:"));
				bemerkungenElements.add(PdfUtil.createList(bemerkungen));
				document.add(PdfUtil.createKeepTogetherTable(bemerkungenElements, 0, 2));
				break;
			case NICHT_EINTRETTEN:
				footer.setPayload(footerLines);
				document.add(PdfUtil.createParagraph("Sie beantragen einen städtischen Beitrag an die familienergänzende Betreuung für den " +
					"Zeitraum vom 01.08.2018 bis 31.07.2019 für Simone Wälti, Angebot Weissenstein " +
					"(18.000126.001.1.1)."));
				document.add(PdfUtil.createParagraph("Sie haben uns zu diesem Zweck am 15.02.2016 ein entsprechendes Gesuch eingereicht."));
				document.add(PdfUtil.createParagraph("Weil die Unterlagen/Angaben unvollständig sind, haben wir Sie mit Fristansetzung zweimal " +
					"gemahnt, um namentlich bezeichnete zusätzliche Unterlagen/Angaben nachzuliefern. Wir " +
					"haben darauf hingewiesen, dass ohne Ihre Mitwirkung keine Vergünstigungen gewährt werden " +
					"können. Sie haben die Fristen unbenutzt verstreichen lassen. Aufgrund der " +
					"fehlenden/unvollständigen Daten ist heute eine materielle Beurteilung Ihres Gesuchs " +
					"ausgeschlossen."));
				document.add(PdfUtil.createParagraph("Im Verwaltungsverfahren gilt der Untersuchungsgrundsatz, d.h. die Behörden stellen den " +
					"Sachverhalt von Amtes wegen fest (Art. 18 Abs. 1 VRPG¹). Der Untersuchungsgrundsatz wird " +
					"indessen durch die Mitwirkungspflicht der Parteien eingeschränkt. Danach sind die Parteien " +
					"verpflichtet, aktiv zur Ermittlung des Sachverhalts beizutragen. Die verantwortliche Behörde " +
					"muss nicht Abklärungen treffen, wenn ein Sachumstand von einer Partei (durch Auskünfte, " +
					"Unterlagen usw.) geklärt werden könnte, die Partei aber die mögliche und zumutbare Mitarbeit " +
					"unterlässt. Die Mitwirkungspflicht gilt allgemein, wenn eine Partei aus einem Begehren Rechte " +
					"ableitet (Art. 20 Abs. 1 VRPG), und sie ist als spezifische und umfassende Mitwirkungspflicht " +
					"der Eltern im Rahmen der vergünstigten familienergänzenden Kinderbetreuung in der " +
					"Betreuungsverordnung verankert. Danach sind die Eltern verpflichtet, die erforderlichen " +
					"Angaben zu machen, die nötigen Unterlagen vorzulegen sowie Änderungen der Verhältnisse " +
					"unverzüglich zu melden (Art. 16 Abs. 1 FEBVO²)."));
				document.add(PdfUtil.createParagraph("Der Mitwirkungspflicht der Eltern steht eine Aufklärungspflicht des Jugendamts gegenüber. " +
					"Dieses hat die Eltern darauf hinzuweisen, welche Beweismittel sie zwecks Prüfung der " +
					"Anspruchsberechtigung beizubringen haben und mit welchen Rechtsfolgen sie im " +
					"Unterlassungsfall zu rechnen hat. Das Jugendamt hat seine Aufklärungspflicht mit dem oben " +
					"aufgeführten Schreiben wahrgenommen."));
				document.add(PdfUtil.createParagraph("Wird die Mitwirkung verweigert, so kann auf das Gesuch nicht eingetreten werden (Art. 20 Abs." +
					"2 VRPG)", 2));
				document.newPage();
				document.add(PdfUtil.createParagraph("Aus diesen Gründen wird verfügt:"));
				document.add(PdfUtil.createBoldParagraph("Auf Ihr Gesuch vom 15.02.2016 wird nicht eingetreten.", 2));
				break;
		}
		gruesseElements.add(PdfUtil.createParagraph("Freundliche Grüsse:", 0));
		gruesseElements.add(PdfUtil.createParagraph("sig. Anna Müller\nSachbearbeitung", 0));
		document.add(PdfUtil.createKeepTogetherTable(gruesseElements, 2, 0));
		document.add(createRechtsmittelBelehrung());
	}

	@Nonnull
	public PdfPTable createRechtsmittelBelehrung() {
		PdfPTable table = new PdfPTable(1);
		table.getDefaultCell().setLeading(0,PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		table.setWidthPercentage(FULL_WIDTH);
		PdfPTable innerTable = new PdfPTable(1);
		innerTable.setWidthPercentage(FULL_WIDTH);
		innerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		innerTable.getDefaultCell().setLeading(0,PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		innerTable.addCell(PdfUtil.createBoldParagraph("Rechtsmittelbelehrung", 0));
		innerTable.addCell(PdfUtil.createParagraph("Gegen diese Verfügung kann innert 30 Tagen Beschwerde erhoben werden. Die Beschwerdefrist " +
			"kann nicht verlängert werden. Die Beschwerde ist der Direktion für Bildung, Soziales und Sport, " +
			"Generalsekretariat, Predigergasse 5, Postfach 3368, 3001 Bern, zuzustellen. Sie muss (a) " +
			"angeben, welche Entscheidung anstelle der angefochtenen Verfügung beantragt wird; (b) aus " +
			"welchen Gründen diese andere Entscheidung verlangt wird, (c) die Unterschrift der " +
			"beschwerdeführenden Partei oder der sie vertretenden Person enthalten. Der Beschwerdeschrift " +
			"beizulegen sind die Beweismittel, soweit sie greifbar sind, und die angefochtene Verfügung."));
		table.addCell(innerTable);
		return table;
	}
}
