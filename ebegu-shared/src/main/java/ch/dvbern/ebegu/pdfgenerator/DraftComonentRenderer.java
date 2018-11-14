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

import javax.annotation.Nonnull;

import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.component.ComponentRenderer;
import ch.dvbern.lib.invoicegenerator.dto.component.TextComponent;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfContentByte;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraftComonentRenderer extends ComponentRenderer<TextComponent, String> {

	private static final Logger LOG = LoggerFactory.getLogger(DraftComonentRenderer.class);

	protected DraftComonentRenderer(@Nonnull TextComponent componentConfiguration, @Nonnull String payload) {
		super(componentConfiguration, payload);
	}

	@Override
	public void render(
		@Nonnull PdfContentByte directContent,
		@Nonnull PageConfiguration layoutConfiguration) {
		try {
			Image image = Image.getInstance(IOUtils.toByteArray(DraftComonentRenderer.class.getResourceAsStream("draft-watermark.png")));
			float factor = directContent.getPdfDocument().getPageSize().getWidth() / image.getWidth();
			float percent = 100.0F * factor;
			image.scalePercent(percent);
			image.setAbsolutePosition(0,50);
			directContent.addImage(image);
		} catch (IOException | DocumentException e){
			LOG.error("Failed to read the Logo: {}", e.getMessage());
		}
	}
}
