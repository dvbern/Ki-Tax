/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT_SIZE;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;

public class AnmeldebestaetigungTSPDFGenerator extends DokumentAnFamilieGenerator {

	private static final String ANMELDUNG_BESTAETIGUNG_TITLE = "PdfGeneration_AnmeldungBestaetigung_Title";
	private static final String KIND_NAME = "PdfGeneration_AnmeldungBestaetigung_KindName";
	private static final String EINTRITTSDATUM = "PdfGeneration_AnmeldungBestaetigung_Eintrittsdatum";

	public enum Art {
		OHNE_TARIF,
		MIT_TARIF
	}

	@Nonnull
	private final Art art;
	private final KindContainer kindContainer;
	private final AnmeldungTagesschule anmeldungTagesschule;

	protected AnmeldebestaetigungTSPDFGenerator(@Nonnull Gesuch gesuch, @Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, @Nonnull KindContainer kindContainer, AnmeldungTagesschule anmeldungTagesschule) {
		super(gesuch, stammdaten);
		this.art = art;
		this.kindContainer = kindContainer;
		this.anmeldungTagesschule = anmeldungTagesschule;
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return translate(ANMELDUNG_BESTAETIGUNG_TITLE);
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		//TODO sehr ähnlich als der Freigabequittung
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(createKindTSAnmeldungTable());

		};
	}

	@Nonnull
	public PdfPTable createKindTSAnmeldungTable() {
		PdfPTable table = new PdfPTable(4);
		// Init
		PdfUtil.setTableDefaultStyles(table);
		table.getDefaultCell().setPaddingBottom(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		// Row: Referenznummer
		table.addCell(new Phrase(translate(KIND_NAME), getPageConfiguration().getFont()));
		table.addCell(new Phrase(kindContainer.getKindGS() != null ? kindContainer.getKindGS().getFullName() :
			kindContainer.getKindJA().getFullName(),
			getPageConfiguration().getFont()));
		table.addCell(new Phrase(translate(EINTRITTSDATUM), getPageConfiguration().getFont()));
		assert anmeldungTagesschule.getBelegungTagesschule() != null;
		table.addCell(new Phrase(anmeldungTagesschule.getBelegungTagesschule().getEintrittsdatum().toString(),
			getPageConfiguration().getFont()));

		return table;
	}



}
