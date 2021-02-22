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
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.util.Constants;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

public class ErsteMahnungPdfGenerator extends MahnungPdfGenerator {


	private static final String ERSTE_MAHNUNG_SEITE_1_PARAGRAPH_1 = "PdfGeneration_ErsteMahnung_Seite1_Paragraph1";
	private static final String ERSTE_MAHNUNG_SEITE_1_PARAGRAPH_2 = "PdfGeneration_ErsteMahnung_Seite1_Paragraph2";
	private static final String ERSTE_MAHNUNG_SEITE_2_PARAGRAPH_1 = "PdfGeneration_ErsteMahnung_Seite2_Paragraph1";
	private static final String ERSTE_MAHNUNG_SEITE_2_PARAGRAPH_2 = "PdfGeneration_ErsteMahnung_Seite2_Paragraph2";
	private static final String UND = "PdfGeneration_Und";


	public ErsteMahnungPdfGenerator(
		@Nonnull Mahnung mahnung,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		super(mahnung, stammdaten);
	}


	@Override
	protected void createSeite1(@Nonnull Document document) {
		document.add(PdfUtil.createParagraph(translate(ERSTE_MAHNUNG_SEITE_1_PARAGRAPH_1, getKinderAndAngebote(), getEingangsdatum()), 1));
		document.add(PdfUtil.createParagraph(translate(ERSTE_MAHNUNG_SEITE_1_PARAGRAPH_2), 1));
	}

	@Override
	protected void createSeite2(@Nonnull Document document, @Nonnull List<Element> seite2Paragraphs) {
		seite2Paragraphs.add(PdfUtil.createParagraph(translate(ERSTE_MAHNUNG_SEITE_2_PARAGRAPH_1, getFristdatum())));
		String paragraph2 = translate(
			ERSTE_MAHNUNG_SEITE_2_PARAGRAPH_2,
			gemeindeStammdaten.getTelefonForGesuch(getGesuch()),
			gemeindeStammdaten.getEmailForGesuch(getGesuch())
		);
		seite2Paragraphs.add(PdfUtil.createParagraph(paragraph2));
	}

	@Nonnull
	private String getKinderAndAngebote() {
		List<String> listAngebot = new ArrayList<>();
		for (KindContainer kindContainer : getGesuch().getKindContainers()) {
			List<String> betreuungenList = new ArrayList<>();

			betreuungenList.addAll(
				kindContainer.getBetreuungen().stream()
					.map(betreuung -> betreuung.getInstitutionStammdaten().getInstitution().getName())
					.collect(Collectors.toList()));
			betreuungenList.addAll(
				kindContainer.getAnmeldungenTagesschule().stream()
					.map(anmeldungTagesschule -> anmeldungTagesschule.getInstitutionStammdaten().getInstitution().getName())
					.collect(Collectors.toList()));
			betreuungenList.addAll(
				kindContainer.getAnmeldungenFerieninsel().stream()
					.map(anmeldungFerieninsel -> anmeldungFerieninsel.getInstitutionStammdaten().getInstitution().getName())
					.collect(Collectors.toList()));

			String betreuungStr = kindContainer.getKindJA().getFullName() + " (";
			betreuungStr += String.join(", ", betreuungenList);
			betreuungStr += ")";
			listAngebot.add(betreuungStr);
		}
		// we need to separate elements by COMMA and the last one by AND
		StringBuilder angebot = new StringBuilder();
		for (int i = 0; i < listAngebot.size(); i++) {
			angebot.append(listAngebot.get(i));
			if (i + 2 == listAngebot.size()) {
				angebot.append(' ').append(translate(UND)).append(' ');
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
}
