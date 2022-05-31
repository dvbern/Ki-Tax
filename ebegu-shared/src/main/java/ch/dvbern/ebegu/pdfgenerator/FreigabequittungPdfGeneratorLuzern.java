/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

import static ch.dvbern.ebegu.pdfgenerator.PdfUtil.createBoldParagraph;
import static ch.dvbern.ebegu.pdfgenerator.PdfUtil.createParagraph;

public class FreigabequittungPdfGeneratorLuzern extends AbstractFreigabequittungPdfGenerator {

	private static final String BITTE_AUSDRUCKEN = "PdfGeneration_BitteAusdrucken";
	private static final String BITTE_SOFORT_EINREICHEN = "PdfGeneration_BitteSofortEinreichen";
	private static final String BENOETIGTE_UNTERLAGEN_INFO = "PdfGeneration_BenoetigteUnterlagen_Info";

	public FreigabequittungPdfGeneratorLuzern(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull List<DokumentGrund> benoetigteUnterlagen) {
		super(gesuch, stammdaten, benoetigteUnterlagen);
	}

	@Override
	protected void createParagraphBitteAusdrucken(Document document) {
		document.add(createParagraph(translate(BITTE_AUSDRUCKEN)));
		document.add(createParagraph(""));
	}

	@Override
	protected void createParagraphBenoetigteUnterlagenInfo(Document document) {
		document.add(createParagraph(translate(BENOETIGTE_UNTERLAGEN_INFO)));
	}

	@Override
	protected void createParagraphSofortEinrichten(List<Element> paragraphlist) {
		paragraphlist.add(createBoldParagraph(translate(BITTE_SOFORT_EINREICHEN), 1));
	}

}
