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

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.lowagie.text.Document;

public class BegleitschreibenPdfGenerator {

	@Nonnull
	private final PdfGenerator pdfGenerator;

	public BegleitschreibenPdfGenerator(@Nonnull final byte[] gemeindeLogo, @Nonnull final List<String> gemeindeHeader, boolean draft) {
		this.pdfGenerator = PdfGenerator.create(gemeindeLogo, gemeindeHeader, draft);
	}

	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {

		final String title = "Referenznummer: 18.000123.001 - 2018/2019";
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

		pdfGenerator.generate(outputStream, title, empfaengerAdresse, (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(PdfUtil.createParagraph("Sehr geehrte Familie"));
			document.add(PdfUtil.createParagraph("Wir haben Ihre Unterlagen, mit denen Sie Unterstützung für die Kinderbetreuung beantragen, " +
				"geprüft. Die Ergebnisse sind in der Beilage ersichtlich. Teilen Sie uns bitte Veränderungen der " +
				"persönlichen und wirtschaftlichen Verhältnisse (Familiengrösse, Umzug, Erwerbspensum usw.) " +
				"unverzüglich mit.", 2));
			document.add(PdfUtil.createParagraph("Freundliche Grüsse", 2));
			document.add(PdfUtil.createParagraph("sig. Xaver Weibel", 0));
			document.add(PdfUtil.createParagraph("Sachbearbeitung", 2));
			document.add(PdfUtil.createParagraph("Beilagen:", 0));
			document.add(PdfUtil.createList(dokumente));
		});
	}

}
