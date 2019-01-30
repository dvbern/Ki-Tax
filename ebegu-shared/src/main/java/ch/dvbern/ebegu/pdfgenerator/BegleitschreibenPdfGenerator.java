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

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.EbeguUtil;
import com.lowagie.text.Document;

public class BegleitschreibenPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String BEGLEITSCHREIBEN_TITLE = "PdfGeneration_Begleitschreiben_Title";
	private static final String BEGLEITSCHREIBEN_CONTENT = "PdfGeneration_Begleitschreiben_Content";
	private static final String BEILAGEN = "PdfGeneration_Beilagen";
	private static final String BEILAGE_VERFUEGUNG = "PdfGeneration_BeilageVerfuegung";
	private static final String BEILAGE_FINANZIELLESITUATION = "PdfGeneration_BeilageFinanzielleSituation";
	private static final String BEILAGE_ERLAEUTERUNG = "PdfGeneration_BeilageErlaeuterung";

	public BegleitschreibenPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		super(gesuch, stammdaten);
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return translate(
			BEGLEITSCHREIBEN_TITLE,
			getGesuch().getJahrFallAndGemeindenummer(),
			getGesuch().getGesuchsperiode().getGesuchsperiodeString());
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(createAnrede());
			document.add(PdfUtil.createParagraph(translate(BEGLEITSCHREIBEN_CONTENT), 2));
			document.add(createParagraphGruss());
			document.add(PdfUtil.createParagraph(translate(DokumentAnFamilieGenerator.SACHBEARBEITUNG), 2));
			document.add(PdfUtil.createParagraph(translate(BEILAGEN), 0));
			document.add(PdfUtil.createListInParagraph(getBeilagen()));
		};
	}

	@Nonnull
	private List<String> getBeilagen() {
		// Verfügungen
		List<String> beilagen = getGesuch().extractAllBetreuungen().stream()
			.filter(this::isOrCanBeVerfuegt)
			.map(this::getBeilagenText).collect(Collectors.toList());
		// Finanzielle Situation
		if (EbeguUtil.isFinanzielleSituationRequired(gesuch) && getGesuch().getFinSitStatus() == FinSitStatus.AKZEPTIERT) {
			beilagen.add(translate(BEILAGE_FINANZIELLESITUATION));
		}
		beilagen.add(translate(BEILAGE_ERLAEUTERUNG));
		return beilagen;
	}

	/**
	 * Will return true when the given Betreuung is verfügbar, that means when it is kleinkind and hasn't been
	 * marked yet as GESCHLOSSEN_OHNE_VERFUEGUNG
	 */
	private boolean isOrCanBeVerfuegt(@Nonnull Betreuung betreuung) {
		return betreuung.getBetreuungsangebotTyp() != null
			&& betreuung.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()
			&& betreuung.getBetreuungsstatus() != Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG;
	}

	@Nonnull
	private String getBeilagenText(@Nonnull Betreuung betreuung) {
		return translate(BEILAGE_VERFUEGUNG, betreuung.getKind().getKindJA().getNachname() + " " + betreuung.getKind().getKindJA().getVorname(), betreuung.getBGNummer());
	}
}
