package ch.dvbern.ebegu.pdfgenerator;

import javax.annotation.Nonnull;

import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.component.ComponentRenderer;
import ch.dvbern.lib.invoicegenerator.dto.component.TextComponent;
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
		} catch (Exception e){
			LOG.error("Failed to read the Logo: {}", e.getMessage());
		}
	}
}
