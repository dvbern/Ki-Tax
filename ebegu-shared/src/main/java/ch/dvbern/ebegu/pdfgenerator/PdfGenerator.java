/*
 * Copyright © 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.pdfgenerator;

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

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.util.List;


public class PdfGenerator extends BaseGenerator<PdfLayoutConfiguration> {

	@Nonnull
	public static PdfGenerator create(final byte[] gemeindeLogo, final List<String> gemeindeHeader, final PhraseRenderer footer, final boolean draft) {
		PdfLayoutConfiguration layoutConfiguration = new PdfLayoutConfiguration(gemeindeLogo, gemeindeHeader);
		layoutConfiguration.setFooter(footer);
		layoutConfiguration.getStaticComponents().stream()
			.map(ComponentRenderer::getComponentConfiguration)
			.forEach(componenConfiguratoin -> componenConfiguratoin.setOnPage(OnPage.FIRST));
		layoutConfiguration.getEmpfaengerAdresse().setOnPage(OnPage.FIRST);
		if (draft) {
			layoutConfiguration.getStaticComponents().add(new DraftComonentRenderer(new DraftComponent(), "DRAFT"));
		}
		return new PdfGenerator(layoutConfiguration);
	}

	@Nonnull
	public static PdfGenerator create(final byte[] gemeindeLogo, final List<String> gemeindeHeader, boolean draft) {
		return create(gemeindeLogo, gemeindeHeader, null, draft);
	}

	public PdfGenerator(@Nonnull PdfLayoutConfiguration configuration) {
		super(configuration);
	}

	@FunctionalInterface
	public interface CustomGenerator {
		void accept(@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator pdfGenerator, @Nonnull PdfGeneratorContext ctx) throws DocumentException;
	}

	@Nonnull
	public void generate(
		@Nonnull OutputStream outputStream,
		@Nonnull String title,
		@Nonnull List<String> empfaengerAdresse,
		@Nonnull CustomGenerator customGenerator) throws InvoiceGeneratorException {

		List<ComponentRenderer<? extends ComponentConfiguration, ?>> componentRenderers =
			getComponentRenderers(empfaengerAdresse);
		OnPageHandler onPageHandler = new OnPageHandler(getConfiguration(), componentRenderers);

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
