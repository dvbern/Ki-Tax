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

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.*;

public abstract class KibonPdfGenerator {

	protected static final String REFERENZNUMMER = "PdfGeneration_Referenznummer";
	protected static final String ABSENDER_TELEFON = "PdfGeneration_Telefon";
	protected static final String EINSCHREIBEN = "PdfGeneration_VerfuegungEingeschrieben";
	protected static final String BETREUUNG_INSTITUTION = "PdfGeneration_Institution";


	@Nonnull
	private PdfGenerator pdfGenerator;

	@Nonnull
	protected final Gesuch gesuch;

	@Nonnull
	protected final GemeindeStammdaten gemeindeStammdaten;

	@Nonnull
	protected final Mandant mandant;

	protected Locale sprache;


	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // Stimmt nicht, die Methode ist final
	protected KibonPdfGenerator(@Nonnull Gesuch gesuch, @Nonnull GemeindeStammdaten stammdaten) {
		this.gesuch = gesuch;
		this.gemeindeStammdaten = stammdaten;
		this.mandant = Objects.requireNonNull(gemeindeStammdaten.getGemeinde().getMandant());
		initLocale(stammdaten);
		initGenerator(stammdaten);
	}

	@Nonnull
	protected abstract String getDocumentTitle();

	@Nonnull
	protected abstract List<String> getEmpfaengerAdresse();

	@Nonnull
	protected abstract CustomGenerator getCustomGenerator();


	public void generate(@Nonnull final OutputStream outputStream) throws InvoiceGeneratorException {
		getPdfGenerator().generate(outputStream, getDocumentTitle(), getEmpfaengerAdresse(), getCustomGenerator());
	}

	@Nonnull
	protected PdfGenerator getPdfGenerator() {
		return pdfGenerator;
	}

	@Nonnull
	public PageConfiguration getPageConfiguration() {
		return pdfGenerator.getConfiguration();
	}

	@Nonnull
	protected Gesuch getGesuch() {
		return gesuch;
	}

	@Nonnull
	protected GemeindeStammdaten getGemeindeStammdaten() {
		return gemeindeStammdaten;
	}

	private void initLocale(@Nonnull GemeindeStammdaten stammdaten) {
		this.sprache = Locale.GERMAN; // Default, falls nichts gesetzt ist
		Sprache[] korrespondenzsprachen = stammdaten.getKorrespondenzsprache().getSprache();
		if (korrespondenzsprachen.length == 1) {
			sprache = korrespondenzsprachen[0].getLocale();
		} else {
			if (gesuch.getGesuchsteller1() != null && gesuch.getGesuchsteller1().getGesuchstellerJA().getKorrespondenzSprache() != null) {
				sprache = gesuch.getGesuchsteller1().getGesuchstellerJA().getKorrespondenzSprache().getLocale();
			}
		}
	}

	private void initGenerator(@Nonnull GemeindeStammdaten stammdaten) {
		this.pdfGenerator = PdfGenerator.create(stammdaten.getGemeindeStammdatenKorrespondenz(),
			getAbsenderAdresse(),
			false,
			useAlternativeLogoIfPresent());
	}

	protected boolean useAlternativeLogoIfPresent() {
		return false;
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
		Adresse adresse = gemeindeStammdaten.getAdresseForGesuch(getGesuch());
		List<String> gemeindeHeader = Arrays.asList(
			adresse.getAddressAsString(),
			""
		);
		return gemeindeHeader;
	}

	@Nonnull
	protected List<String> getGemeindeKontaktdaten() {
		String email = gemeindeStammdaten.getEmailForGesuch(getGesuch());
		String telefon = gemeindeStammdaten.getTelefonForGesuch(getGesuch());
		Adresse adresse = gemeindeStammdaten.getAdresseForGesuch(getGesuch());
		String ort = adresse.getOrt();
		return Arrays.asList(
			translate(ABSENDER_TELEFON, telefon),
			PdfUtil.printString(email),
			PdfUtil.printString(gemeindeStammdaten.getWebseite()),
			"",
			"",
			ort + ", " + Constants.DATE_FORMATTER.format(LocalDate.now())
		);
	}

	@Nonnull
	protected List<String> getFamilieAdresse() {
		final List<String> empfaengerAdresse = new ArrayList<>();
		getOptionalEinschreibeHeader().ifPresent(empfaengerAdresse::add);

		empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerNameAsString(getGesuch().getGesuchsteller1()));
		if (hasSecondGesuchsteller() && getGesuch().getGesuchsteller2() != null) {
			empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerNameAsString(getGesuch().getGesuchsteller2()));
		}
		empfaengerAdresse.add(KibonPrintUtil.getGesuchstellerAddressAsString(getGesuch().getGesuchsteller1()));
		return empfaengerAdresse;
	}

	protected boolean hasSecondGesuchsteller() {
		return gesuch.hasSecondGesuchstellerAtEndOfGesuchsperiode();
	}

	@Nonnull
	protected String translateEnumValue(@Nullable final Enum<?> key) {
		return ServerMessageUtil.translateEnumValue(key, sprache, mandant);
	}

	@Nonnull
	protected String translate(String key) {
		return ServerMessageUtil.getMessage(key, sprache, mandant);
	}

	@Nonnull
	protected String translate(String key, Object... args) {
		return ServerMessageUtil.getMessage(key, sprache, mandant, args);
	}

	protected Optional<String> getOptionalEinschreibeHeader() {
		if (getGesuch().isVerfuegungEingeschrieben()) {
			return Optional.of(translate(EINSCHREIBEN));
		}
		return Optional.empty();
	}
}
