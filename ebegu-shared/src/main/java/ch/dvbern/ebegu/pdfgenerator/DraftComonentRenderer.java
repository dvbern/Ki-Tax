package ch.dvbern.ebegu.pdfgenerator;

import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent;
import ch.dvbern.lib.invoicegenerator.dto.component.ComponentRenderer;
import ch.dvbern.lib.invoicegenerator.dto.component.TextComponent;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.PdfContentByte;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.util.List;

import static com.lowagie.text.Utilities.millimetersToPoints;

public class DraftComonentRenderer extends ComponentRenderer<TextComponent, String> {

	private static final Logger LOG = LoggerFactory.getLogger(VerfügungPdfGenerator.class);

	protected DraftComonentRenderer(@Nonnull TextComponent componentConfiguration, @Nonnull String payload) {
		super(componentConfiguration, payload);
	}

	@Override
	public void render(
		@Nonnull PdfContentByte directContent,
		@Nonnull PageConfiguration layoutConfiguration) throws DocumentException {
		try {
			Image image = Image.getInstance(IOUtils.toByteArray(DraftComonentRenderer.class.getResourceAsStream("draft-watermark.png")));
			float factor = directContent.getPdfDocument().getPageSize().getWidth() / image.getWidth();
			float percent = 100.0F * factor;
			float height = image.getHeight() * factor;
			image.scalePercent(percent);
			//image.scaleAbsoluteWidth(directContent.getPdfDocument().getPageSize().getWidth());
			float y = (directContent.getPdfDocument().getPageSize().getHeight() - height);
			image.setAbsolutePosition(0,50);
			directContent.addImage(image);
		} catch (Exception e){
			LOG.error("Failed to read the Logo: {}", e.getMessage());
		}
	}
}
