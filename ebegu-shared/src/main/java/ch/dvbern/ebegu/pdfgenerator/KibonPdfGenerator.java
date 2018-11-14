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

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;

public abstract class KibonPdfGenerator {

	private static final String ABSENDER_TELEFON = "PdfGeneration_Telefon";
	private static final String FAMILIE = "PdfGeneration_Familie";

	@Nonnull
	private PdfGenerator pdfGenerator;

	@Nonnull
	private final Gesuch gesuch;

	private Locale sprache;


	protected KibonPdfGenerator(@Nonnull Gesuch gesuch, @Nonnull GemeindeStammdaten stammdaten, final boolean draft) {
		this.gesuch = gesuch;
		initLocale(stammdaten);
		initGenerator(stammdaten, draft);
	}

	@Nonnull
	protected abstract String getDocumentTitle();

	@Nonnull
	protected abstract CustomGenerator getCustomGenerator();


	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(outputStream, getDocumentTitle(), getEmpfaengerHeader(), getCustomGenerator());
	}

	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}

	@Nonnull
	protected Gesuch getGesuch() {
		return gesuch;
	}

	private void initLocale(@Nonnull GemeindeStammdaten stammdaten) {
		this.sprache = Locale.GERMAN; // Default, falls nichts gesetzt ist
		Sprache[] korrespondenzsprachen = stammdaten.getKorrespondenzsprache().getSprache();
		if (korrespondenzsprachen.length == 1) {
			sprache = korrespondenzsprachen[0].getLocale();
		} else {
			sprache = Objects.requireNonNull(Objects.requireNonNull(gesuch.getGesuchsteller1())
				.getGesuchstellerJA()
				.getKorrespondenzSprache()).getLocale();
		}
	}

	private void initGenerator(@Nonnull GemeindeStammdaten stammdaten, final boolean draft) {
		this.pdfGenerator = PdfGenerator.create(stammdaten.getLogoContent(), getAbsenderHeader(stammdaten), draft);
	}

	@Nonnull
	protected List<String> getAbsenderHeader(@Nonnull GemeindeStammdaten stammdaten) {
		Adresse adresse = stammdaten.getAdresse();
		List<String> gemeindeHeader = Arrays.asList(
			KibonPrintUtil.getAddressAsString(adresse),
			"",
			translate(ABSENDER_TELEFON, stammdaten.getTelefon()),
			stammdaten.getMail(),
			stammdaten.getWebseite(),
			"",
			"",
			stammdaten.getGemeinde().getName() + ", " + Constants.DATE_FORMATTER.format(LocalDate.now()));
		return gemeindeHeader;
	}

	@Nonnull
	protected List<String> getEmpfaengerHeader() {
		final List<String> empfaengerAdresse = new ArrayList<>();
		empfaengerAdresse.add(translate(FAMILIE));
		empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerNameAsString(getGesuch().getGesuchsteller1()));
		if (getGesuch().getGesuchsteller2() != null) {
			empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerNameAsString(getGesuch().getGesuchsteller2()));
		}
		empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerAddressAsString(getGesuch().getGesuchsteller1()));
		return empfaengerAdresse;
	}

	protected String translate(String key) {
		return ServerMessageUtil.getMessage(key, sprache);
	}

	protected String translate(String key, Object... args) {
		return ServerMessageUtil.getMessage(key, sprache, args);
	}
}
