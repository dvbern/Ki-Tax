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
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;

public class MusterPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String BEGLEITSCHREIBEN_TITLE = "PdfGeneration_Muster_Title";

	public MusterPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		super(gesuch, stammdaten);
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return translate(BEGLEITSCHREIBEN_TITLE);
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(createAnrede());
			document.add(PdfUtil.createParagraph(getSampleText(), 2));
			document.add(createParagraphGruss());
			document.add(PdfUtil.createParagraph(translate(DokumentAnFamilieGenerator.SACHBEARBEITUNG), 2));
		};
	}

	@Override
	@Nonnull
	protected Paragraph createAnrede() {
		String anrede = translate(ANREDE_HERR) + " Muster";
		return PdfUtil.createParagraph(anrede);
	}

	@Nonnull
	@Override
	protected List<String> getEmpfaengerAdresse() {
		String plzAndOrt = gemeindeStammdaten.getAdresse().getPlz() + ' ' + gemeindeStammdaten.getAdresse().getOrt();
		List<String> empfaengerAdresse = new ArrayList<>();
		empfaengerAdresse.add("Thomas Muster");
		empfaengerAdresse.add("Testweg 10");
		empfaengerAdresse.add(plzAndOrt);
		return empfaengerAdresse;
	}

	private String getSampleText() {
		return "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et "
			+ "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid"
			+ " ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat "
			+ "nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt "
			+ "mollit anim id est laborum.\n"
			+ '\n'
			+ "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum "
			+ "dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent"
			+ " luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, "
			+ "consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam "
			+ "erat volutpat.\n"
			+ '\n'
			+ "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip "
			+ "ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie "
			+ "consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim"
			+ " qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.\n"
			+ '\n'
			+ "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat "
			+ "facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh "
			+ "euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis "
			+ "nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.";
	}
}
