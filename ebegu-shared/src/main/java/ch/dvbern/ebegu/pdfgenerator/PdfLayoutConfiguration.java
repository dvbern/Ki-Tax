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

import java.io.IOException;
import java.util.List;

import ch.dvbern.lib.invoicegenerator.dto.BaseLayoutConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.OnPage;
import ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent;
import ch.dvbern.lib.invoicegenerator.dto.component.Logo;
import ch.dvbern.lib.invoicegenerator.dto.component.PhraseRenderer;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Image;
import com.lowagie.text.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.ADRESSE_HEIGHT;
import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.ADRESSE_WIDTH;

public class PdfLayoutConfiguration extends BaseLayoutConfiguration {

	private static final int RECEIVER_LEFT_IN_MM = 123;
	private static final int RECEIVER_TOP_IN_MM = 47;

	public static final int LOGO_LEFT_IN_MM = RECEIVER_LEFT_IN_MM;
	public static final int LOGO_TOP_IN_MM = 15;
	public static final int BARCODE_TOP_IN_MM = 22;
	private static final int LOGO_MAX_WIDTH_IN_MM = 70;
	private static final int LOGO_MAX_HEIGHT_IN_MM = 25;
	private static final int LOGO_KANTON_MAX_WIDTH_IN_MM = 41;
	private static final int LOGO_KANTON_MAX_HEIGHT_IN_MM = 20;

	private static final Logger LOG = LoggerFactory.getLogger(PdfLayoutConfiguration.class);

	public PdfLayoutConfiguration(final byte[] logo, final List<String> absenderHeader, boolean isKanton) {
		super(new AddressComponent(
			null,
			RECEIVER_LEFT_IN_MM,
			RECEIVER_TOP_IN_MM,
			ADRESSE_WIDTH,
			ADRESSE_HEIGHT,
			OnPage.FIRST));
		applyLogo(logo, isKanton);
		if (absenderHeader != null && !absenderHeader.isEmpty()) {
			setHeader(new PhraseRenderer(absenderHeader,
				LEFT_PAGE_DEFAULT_MARGIN_MM,
				RECEIVER_TOP_IN_MM,
				ADRESSE_WIDTH, ADRESSE_HEIGHT,
				PdfUtil.DEFAULT_FONT));
		}
	}

	private void applyLogo(final byte[] logo, boolean isKanton) {
		if (logo == null || logo.length == 0) {
			return;
		}

		try {
			Image image = Image.getInstance(logo);
			final float imageWidthInMm = Utilities.pointsToMillimeters(image.getWidth());
			final float imageHeightInMm = Utilities.pointsToMillimeters(image.getHeight());
			float widthInMm = Math.min(isKanton ? LOGO_KANTON_MAX_WIDTH_IN_MM : LOGO_MAX_WIDTH_IN_MM, imageWidthInMm);
			if (imageHeightInMm > (isKanton ? LOGO_KANTON_MAX_HEIGHT_IN_MM : LOGO_MAX_HEIGHT_IN_MM)) {
				final float factor =
					(isKanton ? LOGO_KANTON_MAX_HEIGHT_IN_MM : LOGO_MAX_HEIGHT_IN_MM) / imageHeightInMm;
				widthInMm = Math.min(widthInMm, imageWidthInMm * factor);
			}
			Logo logoToApply = new Logo(
				logo,
				isKanton ? 8 : LOGO_LEFT_IN_MM,
				isKanton ? 5 : LOGO_TOP_IN_MM,
				widthInMm);
			setLogo(logoToApply);
		} catch (IOException | BadElementException e) {
			LOG.error("Failed to read the Logo: {}", e.getMessage(), e);
		}
	}
}
