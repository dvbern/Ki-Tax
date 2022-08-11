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

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;

public abstract class GemeindeAntragReportPdfGenerator extends MandantPdfGenerator {

	protected static final float TABLE_SPACING_AFTER = 20;
	protected static final float SUB_HEADER_SPACING_AFTER = 10;

	@Nonnull
	private NoAdressPdfGenerator pdfGenerator;

	public GemeindeAntragReportPdfGenerator(
			@Nonnull GemeindeAntrag gemeindeAntrag,
			@Nullable GemeindeStammdaten gemeindeStammdaten
	) {
		super(Sprache.DEUTSCH, gemeindeAntrag.getGemeinde().getMandant());
		initLocale(gemeindeStammdaten);
		initGenerator();
	}

	private void initLocale(@Nullable GemeindeStammdaten stammdaten) {
		this.sprache = Locale.GERMAN; // Default, falls nichts gesetzt ist
		if (stammdaten != null) {
			Sprache[] korrespondenzsprachen = stammdaten.getKorrespondenzsprache().getSprache();
			if (korrespondenzsprachen.length > 0) {
				sprache = korrespondenzsprachen[0].getLocale();
			}
		}
	}

	private void initGenerator() {
		this.pdfGenerator =
				NoAdressPdfGenerator.create();
	}

	@Override
	@Nonnull
	protected abstract String getDocumentTitle();

	@Override
	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}

	@Override
	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(outputStream, getDocumentTitle(), getEmpfaengerAdresse(), getCustomGenerator());
	}

	@Override
	@Nonnull
	protected abstract CustomGenerator getCustomGenerator();

	@Override
	@Nonnull
	protected List<String> getEmpfaengerAdresse() {
		return List.of("");
	}

	@Nonnull
	protected String getBooleanAsString(@Nullable Boolean value) {
		if (value == null) {
			return "";
		}
		if (Boolean.TRUE.equals(value)) {
			return translate("label_true", mandant);
		}
		return translate("label_false", mandant);
	}

	@Nullable
	protected Integer getIntValue(@Nullable BigDecimal value) {
		return value == null ? null : value.intValue();
	}


	@Nonnull
	protected final List<String> getAbsenderAdresse() {
		List<String> absender = new ArrayList<>();
		absender.addAll(getGemeindeAdresse());
		absender.addAll(getGemeindeKontaktdaten());
		return absender;
	}

	@Nonnull
	protected List<String> getGemeindeAdresse() {
		List<String> gemeindeHeader = Arrays.asList(
				""
		);
		return gemeindeHeader;
	}

	@Nonnull
	protected List<String> getGemeindeKontaktdaten() {
		return Arrays.asList(
				"",
				""
		);
	}
}
