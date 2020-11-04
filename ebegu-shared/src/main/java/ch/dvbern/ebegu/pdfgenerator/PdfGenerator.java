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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.lib.invoicegenerator.BaseGenerator;
import ch.dvbern.lib.invoicegenerator.OnPageHandler;
import ch.dvbern.lib.invoicegenerator.dto.OnPage;
import ch.dvbern.lib.invoicegenerator.dto.component.ComponentConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.component.ComponentRenderer;
import ch.dvbern.lib.invoicegenerator.dto.component.PhraseRenderer;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Utilities;

public class PdfGenerator extends BaseGenerator<PdfLayoutConfiguration> {

	@Nonnull
	public static PdfGenerator create(@Nonnull final byte[] gemeindeLogo, @Nonnull final List<String> absenderHeader, @Nullable final PhraseRenderer footer,@Nonnull boolean isKanton) {
		PdfLayoutConfiguration layoutConfiguration = new PdfLayoutConfiguration(gemeindeLogo, absenderHeader, isKanton);
		layoutConfiguration.setFooter(footer);
		layoutConfiguration.getStaticComponents().stream()
			.map(ComponentRenderer::getComponentConfiguration)
			.forEach(componenConfiguratoin -> componenConfiguratoin.setOnPage(OnPage.FIRST));
		layoutConfiguration.getEmpfaengerAdresse().setOnPage(OnPage.FIRST);
		// Die Default-Schriften aus der Library ueberschreiben
		layoutConfiguration.getFonts().setFont(PdfUtil.DEFAULT_FONT);
		layoutConfiguration.getFonts().setFontBold(PdfUtil.DEFAULT_FONT_BOLD);
		layoutConfiguration.getFonts().setFontTitle(PdfUtil.FONT_TITLE);
		layoutConfiguration.getFonts().setFontH1(PdfUtil.FONT_H1);
		layoutConfiguration.getFonts().setFontH2(PdfUtil.FONT_H2);
		return new PdfGenerator(layoutConfiguration);
	}

	@Nonnull
	public static PdfGenerator create(
		@Nonnull final byte[] gemeindeLogo,
		@Nonnull final List<String> absenderHeader,
		@Nonnull boolean isKanton) {
		return create(gemeindeLogo, absenderHeader, null, isKanton);
	}

	public PdfGenerator(@Nonnull PdfLayoutConfiguration configuration) {
		super(configuration);
	}

	@FunctionalInterface
	public interface CustomGenerator {
		void accept(@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator pdfGenerator, @Nonnull PdfGeneratorContext ctx) throws DocumentException;
	}

	public void generate(
		@Nonnull OutputStream outputStream,
		@Nonnull String title,
		@Nonnull List<String> empfaengerAdresse,
		@Nonnull CustomGenerator customGenerator) throws InvoiceGeneratorException {

		List<ComponentRenderer<? extends ComponentConfiguration, ?>> componentRenderers =
			getComponentRenderers(empfaengerAdresse);
		OnPageHandler onPageHandler = new OnPageHandler(getPdfElementGenerator(), componentRenderers);

		generate(outputStream, onPageHandler, pdfGenerator -> {
			Document document = pdfGenerator.getDocument();
			document.add(PdfUtil.createTitle(title));

			// In the following you witness the "margin-hack": the title was already added to the document. Thus the
			// 1st page exists with the preconfigured margins. For all the following pages, the margin set below
			// will be applied (which uses the bottom margin also as the top margin).
			document.setMargins(
				getConfiguration().getLeftPageMarginInPoints(),
				getConfiguration().getRightPageMarginInPoints(),
				Utilities.millimetersToPoints(30),
				getConfiguration().getBottomMarginInPoints()
			);

			customGenerator.accept(pdfGenerator, new PdfGeneratorContext());
		});
	}
}
