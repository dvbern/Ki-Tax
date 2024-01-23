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

import ch.dvbern.ebegu.entities.GemeindeStammdatenKorrespondenz;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.ADRESSE_HEIGHT;
import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.ADRESSE_WIDTH;

public class PdfLayoutConfiguration extends BaseLayoutConfiguration {

	public static final int LOGO_TOP_IN_MM = 15;
	public static final int BARCODE_TOP_IN_MM = 22;
	private static final int LOGO_MAX_WIDTH_IN_MM = 70;
	private static final int LOGO_MAX_HEIGHT_IN_MM = 25;
	private static final int LOGO_KANTON_MAX_WIDTH_IN_MM = 41;
	private static final int LOGO_KANTON_MAX_HEIGHT_IN_MM = 20;

	private static final Logger LOG = LoggerFactory.getLogger(PdfLayoutConfiguration.class);

	public PdfLayoutConfiguration(
		@Nonnull GemeindeStammdatenKorrespondenz stammdaten,
		@Nullable final List<String> absenderHeader,
		boolean isKanton,
		boolean useAlternativeLogoIfPresent
	) {
		super(new AddressComponent(
			null,
			stammdaten.getReceiverAddressSpacingLeft(),
			stammdaten.getReceiverAddressSpacingTop(),
			ADRESSE_WIDTH,
			ADRESSE_HEIGHT,
			OnPage.FIRST));
		applyLogo(stammdaten, isKanton, useAlternativeLogoIfPresent);
		if (absenderHeader != null && !absenderHeader.isEmpty()) {
			setHeader(new PhraseRenderer(absenderHeader,
				stammdaten.getSenderAddressSpacingLeft(),
				stammdaten.getSenderAddressSpacingTop(),
				ADRESSE_WIDTH, ADRESSE_HEIGHT,
				PdfUtil.DEFAULT_FONT));
		}
	}

	private void applyLogo(GemeindeStammdatenKorrespondenz stammdaten, boolean isKanton, boolean useAlternativLogoIfPresent) {
		byte[] logoForPDF = getLogoContentToUse(stammdaten, useAlternativLogoIfPresent);

		if (logoForPDF.length == 0) {
			return;
		}
		try {
			// Falls eine gewuenschte Logo-Breite gesetzt ist, nehmen wir diese ungeprueft.
			// Ansonsten Berechnung gemaess Regeln von kiBon
			float widthInMm;
			if (stammdaten.getLogoWidth() != null) {
				widthInMm = stammdaten.getLogoWidth().floatValue();
			} else {
				Image image = Image.getInstance(logoForPDF);
				widthInMm = getImageWidthDefault(image, isKanton);
			}
			int logoLeft = stammdaten.getLogoSpacingLeft();
			Logo logoToApply = new Logo(
				logoForPDF,
				logoLeft,
				isKanton ? 5 : stammdaten.getLogoSpacingTop(),
				widthInMm);
			setLogo(logoToApply);
		} catch (IOException | BadElementException e) {
			LOG.error("Failed to read the Logo: {}", e.getMessage(), e);
		}
	}

	private byte[] getLogoContentToUse(GemeindeStammdatenKorrespondenz stammdaten,
									   boolean useAlternativLogoIfPresent) {
		if (useAlternativLogoIfPresent && stammdaten.getAlternativesLogoTagesschuleContent().length != 0) {
			return stammdaten.getAlternativesLogoTagesschuleContent();
		}

		return stammdaten.getLogoContent();
	}

	private float getImageWidthDefault(@Nonnull Image image, boolean isKanton) {
		final float imageWidthInMm = Utilities.pointsToMillimeters(image.getWidth()); // 20
		final float imageHeightInMm = Utilities.pointsToMillimeters(image.getHeight());
		float widthInMm = Math.min(isKanton ? LOGO_KANTON_MAX_WIDTH_IN_MM : LOGO_MAX_WIDTH_IN_MM, imageWidthInMm);
		if (imageHeightInMm > (isKanton ? LOGO_KANTON_MAX_HEIGHT_IN_MM : LOGO_MAX_HEIGHT_IN_MM)) {
			final float factor =
				(isKanton ? LOGO_KANTON_MAX_HEIGHT_IN_MM : LOGO_MAX_HEIGHT_IN_MM) / imageHeightInMm;
			widthInMm = Math.min(widthInMm, imageWidthInMm * factor);
		}
		return widthInMm;
	}
}
