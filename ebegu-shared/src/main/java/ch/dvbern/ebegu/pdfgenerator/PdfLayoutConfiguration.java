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

import ch.dvbern.lib.invoicegenerator.dto.BaseLayoutConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.OnPage;
import ch.dvbern.lib.invoicegenerator.dto.component.*;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import com.lowagie.text.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;

import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.*;

public class PdfLayoutConfiguration extends BaseLayoutConfiguration {

	private static final int RECEIVER_LEFT_IN_MM = 123;
	private static final int RECEIVER_TOP_IN_MM = 47;

	public static final int LOGO_LEFT_IN_MM = RECEIVER_LEFT_IN_MM;
	public static final int LOGO_TOP_IN_MM = 15;
	private static final int LOGO_MAX_WIDTH_IN_MM = 70;
	private static final int LOGO_MAX_HEIGHT_IN_MM = 25;

	private static final Logger LOG = LoggerFactory.getLogger(PdfLayoutConfiguration.class);

	public PdfLayoutConfiguration(final byte[] gemeindeLogo, final List<String> gemeindeHeader) {
		super(new AddressComponent(
			null,
			RECEIVER_LEFT_IN_MM,
			RECEIVER_TOP_IN_MM,
			ADRESSE_WIDTH,
			ADRESSE_HEIGHT,
			OnPage.FIRST));
		applyLogo(gemeindeLogo);
		if (gemeindeHeader != null && !gemeindeHeader.isEmpty()) {
			setHeader(new PhraseRenderer(gemeindeHeader, LEFT_PAGE_DEFAULT_MARGIN_MM, RECEIVER_TOP_IN_MM, ADRESSE_WIDTH, ADRESSE_HEIGHT));
		}
	}

	private void applyLogo(final byte[] gemeindeLogo) {
		if (gemeindeLogo == null) {
			return;
		}

		try {
			Image image = Image.getInstance(gemeindeLogo);
			final float imageWidthInMm = Utilities.pointsToMillimeters(image.getWidth());
			final float imageHeightInMm = Utilities.pointsToMillimeters(image.getHeight());
			float widthInMm = Math.min(LOGO_MAX_WIDTH_IN_MM, imageWidthInMm);
			if (imageHeightInMm > LOGO_MAX_HEIGHT_IN_MM) {
				final float factor = LOGO_MAX_HEIGHT_IN_MM / imageHeightInMm;
				widthInMm = Math.min(widthInMm, imageWidthInMm * factor);
			}
			Logo logo = new Logo(
				gemeindeLogo,
				LOGO_LEFT_IN_MM,
				LOGO_TOP_IN_MM,
				widthInMm);
			setLogo(logo);
		} catch (IOException e) {
			LOG.error("Failed to read the Logo: {}", e.getMessage());
		}
		catch (BadElementException e) {
			LOG.error("Failed to read the Logo: {}", e.getMessage());
		}
	}

}
