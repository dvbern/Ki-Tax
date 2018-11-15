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

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

public class MahnungPdfGenerator extends DokumentAnFamilieGenerator {

	private boolean zweiteMahnung;

	public MahnungPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		final boolean draft,
		final boolean zweiteMahnung) {
		super(gesuch, stammdaten, draft);
		this.zweiteMahnung = zweiteMahnung;
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		final String title = "Gesuch für Simone Wälti\n" +
			"2018/2019 Referenznummer: 18.000126.001\n" +
			"Unvollständige Angaben/Unterlagen";
		return title;
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		final List<String> dokumente = Arrays.asList(
			"Verfügung zu Betreuungsangebot 18.000123.001.1.1",
			"Verfügung zu Betreuungsangebot 18.000123.001.1.2",
			"Verfügung zu Betreuungsangebot 18.000123.001.2.1",
			"Berechnung der finanziellen Situation");

		return (generator, ctx) -> {
			Document document = generator.getDocument();
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
				seite2Paragraphs.add(PdfUtil.createParagraph("\nErst nach Eingang dieser zusätzlichen Unterlagen können wir Ihr Gesuch weiter bearbeiten. Wir " +
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
		};
	}
}
