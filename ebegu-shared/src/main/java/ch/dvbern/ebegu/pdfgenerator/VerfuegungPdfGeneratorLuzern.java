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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;

public class VerfuegungPdfGeneratorLuzern extends AbstractVerfuegungPdfGenerator {

	public VerfuegungPdfGeneratorLuzern(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte
	) {
		super(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte);
	}

	@Override
	protected void addGruesseElements(@Nonnull List<Element> gruesseElements) {
		gruesseElements.add(createParagraphGruss());
		gruesseElements.add(createParagraphSignatur());
	}

	@Override
	protected void createDokumentNichtEintretten(
		@Nonnull Document document,
		@Nonnull PdfGenerator generator) {
		createDokumentNichtEintrettenDefault(document, generator);
	}

	@Override
	protected Font getBgColorForUeberwiesenerBetragCell() {
		return fontTabelleBold;
	}

	@Override
	protected Font getBgColorForBetreuungsgutscheinCell() {
		return fontTabelle;
	}

	@Override
	protected String getTextGutschein() {
		String messageKey =  betreuung.isAuszahlungAnEltern() ? GUTSCHEIN_AN_ELTERN : GUTSCHEIN_AN_INSTITUTION;
		return translate(messageKey);
	}
}
