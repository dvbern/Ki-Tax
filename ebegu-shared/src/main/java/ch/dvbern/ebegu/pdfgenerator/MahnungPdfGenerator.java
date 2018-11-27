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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

public class MahnungPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String MAHNUNG_TITLE = "PdfGeneration_Mahnung_Title";
	private static final String ANREDE_FAMILIE = "PdfGeneration_AnredeFamilie";

	private static final String ERSTE_MAHNUNG_SEITE_1_PARAGRAPH_1 = "PdfGeneration_ErsteMahnung_Seite1_Paragraph1";
	private static final String ERSTE_MAHNUNG_SEITE_1_PARAGRAPH_2 = "PdfGeneration_ErsteMahnung_Seite1_Paragraph2";
	private static final String ERSTE_MAHNUNG_SEITE_2_PARAGRAPH_1 = "PdfGeneration_ErsteMahnung_Seite2_Paragraph1";
	private static final String ERSTE_MAHNUNG_SEITE_2_PARAGRAPH_2 = "PdfGeneration_ErsteMahnung_Seite2_Paragraph2";

	private static final String ZWEITE_MAHNUNG_SEITE_1_PARAGRAPH_1 = "PdfGeneration_ZweiteMahnung_Seite1_Paragraph1";
	private static final String ZWEITE_MAHNUNG_SEITE_1_PARAGRAPH_2 = "PdfGeneration_ZweiteMahnung_Seite1_Paragraph2";
	private static final String ZWEITE_MAHNUNG_SEITE_2_PARAGRAPH_1 = "PdfGeneration_ZweiteMahnung_Seite2_Paragraph1";
	private static final String ZWEITE_MAHNUNG_SEITE_2_PARAGRAPH_2 = "PdfGeneration_ZweiteMahnung_Seite2_Paragraph2";

	private static final String MAHNUNG_DANK = "PdfGeneration_Mahnung_Dank";
	private static final String UND = "PdfGeneration_Und";


	private boolean zweiteMahnung;
	private Mahnung mahnung;
	private Mahnung ersteMahnung;


	public MahnungPdfGenerator(
		@Nonnull Mahnung mahnung,
		@Nullable Mahnung ersteMahnung,
		@Nonnull GemeindeStammdaten stammdaten,
		final boolean draft,
		final boolean zweiteMahnung
	) {
		super(mahnung.getGesuch(), stammdaten, draft);
		this.zweiteMahnung = zweiteMahnung;
		this.mahnung = mahnung;
		this.ersteMahnung = ersteMahnung;
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
			if (zweiteMahnung) {
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ZWEITE_MAHNUNG_SEITE_1_PARAGRAPH_1, getMahndatum())));
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ZWEITE_MAHNUNG_SEITE_1_PARAGRAPH_2, getFristdatum())));
			} else {
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ERSTE_MAHNUNG_SEITE_1_PARAGRAPH_1, getKinderUndAngebote(), getEingangsdatum()), 1));
				document.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ERSTE_MAHNUNG_SEITE_1_PARAGRAPH_2), 1));
			}
			document.add(PdfUtil.createList(getFehlendeUnterlagen(), 1));
			List<Element> seite2Paragraphs = Lists.newArrayList();
			if (zweiteMahnung) {
				seite2Paragraphs.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ZWEITE_MAHNUNG_SEITE_2_PARAGRAPH_1)));
				seite2Paragraphs.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ZWEITE_MAHNUNG_SEITE_2_PARAGRAPH_2, gemeindeStammdaten.getTelefon(), gemeindeStammdaten.getMail())));
			} else {
				seite2Paragraphs.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ERSTE_MAHNUNG_SEITE_2_PARAGRAPH_1, getFristdatum())));
				seite2Paragraphs.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(ERSTE_MAHNUNG_SEITE_2_PARAGRAPH_2, gemeindeStammdaten.getTelefon(), gemeindeStammdaten.getMail())));
			}
			seite2Paragraphs.add(PdfUtil.createParagraph(ServerMessageUtil.getMessage(MAHNUNG_DANK), 2));
			seite2Paragraphs.add(createParagraphGruss());
			seite2Paragraphs.add(createParagraphSignatur());
			document.add(PdfUtil.createKeepTogetherTable(seite2Paragraphs, 1, 0));
		};
	}

	@Nonnull
	private String getKinderUndAngebote() {
		List<String> listAngebot = new ArrayList<>();
		for (KindContainer kindContainer : getGesuch().getKindContainers()) {
			listAngebot.addAll(
				kindContainer.getBetreuungen().stream()
					.map(betreuung -> betreuung.getKind().getKindJA().getFullName() + " (" + betreuung.getInstitutionStammdaten().getInstitution().getName() + ')')
					.collect(Collectors.toList()));
		}
		StringBuilder angebot = new StringBuilder();
		for (int i = 0; i < listAngebot.size(); i++) {
			angebot.append(listAngebot.get(i));
			if (i + 2 == listAngebot.size() && listAngebot.size() > 1) {
				angebot.append(' ').append(ServerMessageUtil.getMessage(UND)).append(' ');
			} else if (i + 1 < listAngebot.size()) {
				angebot.append(", ");
			}
		}
		return angebot.toString();
	}

	@Nonnull
	private String getEingangsdatum() {
		LocalDate eingangsdatum = gesuch.getEingangsdatum() != null ? gesuch.getEingangsdatum() : LocalDate.now();
		return Constants.DATE_FORMATTER.format(eingangsdatum);
	}

	@Nonnull
	private String getMahndatum() {
		if (mahnung.getMahnungTyp() == MahnungTyp.ZWEITE_MAHNUNG && ersteMahnung != null && ersteMahnung.getTimestampErstellt() != null) {
			return Constants.DATE_FORMATTER.format(ersteMahnung.getTimestampErstellt());
		}
		return "";
	}

	@Nonnull
	private String getFristdatum() {
		if (mahnung.getDatumFristablauf() != null) {
			return Constants.DATE_FORMATTER.format(mahnung.getDatumFristablauf());
		}
		// Im Status ENTWURF ist noch kein Datum Fristablauf gesetzt
		return "";
	}

	@Nonnull
	public List<String> getFehlendeUnterlagen() {
		List<String> fehlendeUnterlagen = new ArrayList<>();
		if (mahnung.getBemerkungen() != null) {
			String[] splitFehlendeUnterlagen = mahnung.getBemerkungen().split('[' + System.getProperty("line.separator") + "]+");
			fehlendeUnterlagen.addAll(Arrays.asList(splitFehlendeUnterlagen));
		}
		return fehlendeUnterlagen;
	}
}
