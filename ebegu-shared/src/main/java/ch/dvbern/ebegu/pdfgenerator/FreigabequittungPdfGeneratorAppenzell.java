/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.pdfgenerator;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

public class FreigabequittungPdfGeneratorAppenzell extends AbstractFreigabequittungPdfGenerator {

	private static final String ERMAECHTIGUNG_EINVERSTAENDNIS_TITLE = "PdfGeneration_ErmaechtigungEinverstaendnis_Title";
	private static final String ERMAECHTIGUNG_AR_1 = "PdfGeneration_ErmaechtigungAR_1";
	private static final String ERMAECHTIGUNG_AR_2 = "PdfGeneration_ErmaechtigungAR_2";
	private static final String ERMAECHTIGUNG_AR_3 = "PdfGeneration_ErmaechtigungAR_3";
	private static final String ERMAECHTIGUNG_AR_4 = "PdfGeneration_ErmaechtigungAR_4";
	private static final String ERMAECHTIGUNG_AR_5 = "PdfGeneration_ErmaechtigungAR_5";
	private static final String ERMAECHTIGUNG_AR_6 = "PdfGeneration_ErmaechtigungAR_6";
	private static final String ERMAECHTIGUNG_AR_7 = "PdfGeneration_ErmaechtigungAR_7";
	private static final String ERMAECHTIGUNG_AR_8 = "PdfGeneration_ErmaechtigungAR_8";
	private static final String ERMAECHTIGUNG_AR_9 = "PdfGeneration_ErmaechtigungAR_9";
	private static final String ERMAECHTIGUNG_AR_10 = "PdfGeneration_ErmaechtigungAR_10";
	private static final String ERMAECHTIGUNG_AR_11 = "PdfGeneration_ErmaechtigungAR_11";
	private static final String ERMAECHTIGUNG_AR_12 = "PdfGeneration_ErmaechtigungAR_12";
	private static final String ERMAECHTIGUNG_AR_13 = "PdfGeneration_ErmaechtigungAR_13";

	public FreigabequittungPdfGeneratorAppenzell(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull List<DokumentGrund> benoetigteUnterlagen) {
		super(gesuch, stammdaten, benoetigteUnterlagen);
	}

	@Override
	protected void createSeite2(@Nonnull Document document) {
		document.add(PdfUtil.createSubTitle(translate(ERMAECHTIGUNG_EINVERSTAENDNIS_TITLE)));
		document.add(PdfUtil.createParagraph(translate(ERMAECHTIGUNG_AR_1)));
		document.add(PdfUtil.createParagraph(translate(ERMAECHTIGUNG_AR_2)));

		var list = new ArrayList<String>();
		list.add(translate(ERMAECHTIGUNG_AR_3));
		list.add(translate(ERMAECHTIGUNG_AR_4));
		list.add(translate(ERMAECHTIGUNG_AR_5));

		document.add(PdfUtil.createListInParagraph(list));
		document.add(PdfUtil.createParagraph(""));

		document.add(PdfUtil.createParagraph(translate(ERMAECHTIGUNG_AR_6)));

		var list2 = new ArrayList<String>();
		list2.add(translate(ERMAECHTIGUNG_AR_7));
		list2.add(translate(ERMAECHTIGUNG_AR_8));

		document.add(PdfUtil.createListInParagraph(list2));
		document.add(PdfUtil.createParagraph("", 2));

		document.add(PdfUtil.createParagraph(translate(ERMAECHTIGUNG_AR_9)));

		var list3 = new ArrayList<String>();
		list3.add(translate(ERMAECHTIGUNG_AR_10));
		list3.add(translate(ERMAECHTIGUNG_AR_11));
		list3.add(translate(ERMAECHTIGUNG_AR_12));
		list3.add(translate(ERMAECHTIGUNG_AR_13));

		document.add(PdfUtil.createListInParagraph(list3));
		document.add(PdfUtil.createParagraph(""));

		List<Element> seite2Paragraphs = new ArrayList<>();
		createVollstaendigkeitUndSignatur(seite2Paragraphs);
		document.add(PdfUtil.createKeepTogetherTable(seite2Paragraphs, 1, 0));

	}

	@Override
	protected void createParagraphBitteAusdrucken(Document document) {

	}

	@Override
	protected void createParagraphBenoetigteUnterlagenInfo(Document document) {

	}

	@Override
	protected void createParagraphSofortEinrichten(List<Element> paragraphlist) {

	}

}
