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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

public abstract class MahnungPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String MAHNUNG_TITLE = "PdfGeneration_Mahnung_Title";
	private static final String ANREDE_FAMILIE = "PdfGeneration_AnredeFamilie";
	private static final String MAHNUNG_DANK = "PdfGeneration_Mahnung_Dank";

	protected Mahnung mahnung;


	protected MahnungPdfGenerator(
		@Nonnull Mahnung mahnung,
		@Nonnull GemeindeStammdaten stammdaten,
		final boolean draft
	) {
		super(mahnung.getGesuch(), stammdaten, draft);
		this.mahnung = mahnung;
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return ServerMessageUtil.getMessage(MAHNUNG_TITLE, gesuch.extractFullnamesString(), gesuch.getGesuchsperiode().getGesuchsperiodeString(), gesuch.getJahrFallAndGemeindenummer());
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();

			document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ANREDE_FAMILIE)));
			createSeite1(document);
			document.add(PdfUtil.createList(getFehlendeUnterlagen(), 1));

			List<Element> seite2Paragraphs = Lists.newArrayList();
			createSeite2(document, seite2Paragraphs);
			seite2Paragraphs.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(MAHNUNG_DANK), 2));
			seite2Paragraphs.add(createParagraphGruss());
			seite2Paragraphs.add(createParagraphSignatur());
			document.add(PdfUtil.createKeepTogetherTable(seite2Paragraphs, 1, 0));
		};
	}

	protected abstract void createSeite1(@Nonnull Document document);

	protected abstract void createSeite2(@Nonnull Document document, @Nonnull List<Element> seite2Paragraphs);

	@Nonnull
	protected String getFristdatum() {
		if (mahnung.getDatumFristablauf() != null) {
			return Constants.DATE_FORMATTER.format(mahnung.getDatumFristablauf());
		}
		// Im Status ENTWURF ist noch kein Datum Fristablauf gesetzt
		return "";
	}

	@Nonnull
	private List<String> getFehlendeUnterlagen() {
		List<String> fehlendeUnterlagen = new ArrayList<>();
		if (mahnung.getBemerkungen() != null) {
			String[] splitFehlendeUnterlagen = mahnung.getBemerkungen().split('[' + System.getProperty("line.separator") + "]+");
			fehlendeUnterlagen.addAll(Arrays.asList(splitFehlendeUnterlagen));
		}
		return fehlendeUnterlagen;
	}
}
