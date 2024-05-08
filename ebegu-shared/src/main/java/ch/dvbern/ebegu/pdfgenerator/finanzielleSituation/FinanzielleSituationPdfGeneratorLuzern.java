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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.pdfgenerator.finanzielleSituation;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

import static java.util.Objects.requireNonNull;

public class FinanzielleSituationPdfGeneratorLuzern extends FinanzielleSituationPdfGenerator {

	private static final String STEUERBARES_VERMOEGEN = "PdfGeneration_FinSit_SteuerbaresVermoegen";
	private static final String STEUERBARES_EINKOMMEN = "PdfGeneration_FinSit_SteuerbaresEinkommen";
	private static final String NETTOEINKUENFTE_LIEGENSCHAFTEN = "PdfGeneration_FinSit_NettoeinkuenfteLiegenschaften";
	private static final String VERRECHENBARE_GESCHAEFTSVERLUSTE = "PdfGeneration_FinSit_VerrechenbareGeschaeftsverluste";
	private static final String EINKAEUFE_VORSORGE = "PdfGeneration_FinSit_EinkaeufeVorsorge";
	private static final String BERECHNUNG_GEMAESS_SELBSTDEKLARATION = "PdfGeneration_FinSit_BerechnungGemaessSelbstdeklaration";
	private static final String BERECHNUNG_GEMAESS_VERANLAGUNG = "PdfGeneration_FinSit_BerechnungGemaessVeanlagung";
	private static final String TOTAL_ABZUEGE = "PdfGeneration_FinSit_Abzuege_Total";
	private static final String TOTAL_EINKUENFTE = "PdfGeneration_FinSit_EinkommenTotal";
	private static final String ANRECHENBARES_VERMOEGEN_GEMAESS_SELBSTDEKLARATION = "PdfGeneration_FinSit_AnrechenbaresVermoegenGemaessSelbstdeklaration";

	@Nonnull
	private FinanzielleSituationContainer basisJahrGS1;
	@Nullable
	private FinanzielleSituationContainer basisJahrGS2;

	public FinanzielleSituationPdfGeneratorLuzern(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum
	) {
		super(gesuch, verfuegungFuerMassgEinkommen, stammdaten, erstesEinreichungsdatum);
	}

	@Override
	protected void initializeValues() {
		requireNonNull(gesuch.getGesuchsteller1());
		basisJahrGS1 = requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());

		hasSecondGesuchsteller = false;
		if (gesuchHasTwoFinSit()) {
			basisJahrGS2 = requireNonNull(gesuch.getGesuchsteller2()).getFinanzielleSituationContainer();
			hasSecondGesuchsteller = true;
		}

		finanzDatenDTO = finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, hasSecondGesuchsteller);
		initialzeEkv();
	}

	@Override
	protected void createPageBasisJahr(
		@Nonnull PdfGenerator generator,
		@Nonnull Document document
	) {
		document.add(createIntroBasisjahr());
		document.add(createMassgebendesEinkommenTableForGesuchsteller(basisJahrGS1, getNameForFinSit1(), 1));

		if (basisJahrGS2 != null) {
			addSpacing(document);
			String name = requireNonNull(gesuch.getGesuchsteller2()).extractFullName();
			document.add(createMassgebendesEinkommenTableForGesuchsteller(basisJahrGS2, name, 2));

			addSpacing(document);
			addTablezusammenzug(document);
		}
	}

	private String getNameForFinSit1() {
		requireNonNull(gesuch.getGesuchsteller1());
		// im konkubinat hat das gesuch zwei separate finSit
		if (gesuchHasTwoFinSit()) {
			return gesuch.getGesuchsteller1().extractFullName();
		}
		// bei verheiratet haben wir nur eine finSit. Als Titel brauchen wir aber beide Antragstellenden
		if (isVerheiratet()) {
			return bothNames();
		}
		// alleinerziehende person. nur eine finSit.
		return gesuch.getGesuchsteller1().extractFullName();
	}

	private PdfPTable createMassgebendesEinkommenTableForGesuchsteller(
		@Nonnull FinanzielleSituationContainer finSit,
		@Nonnull String gesuchstellerName,
		@Nonnull Integer gesuchstellerNumber
	) {
		var massgebendesEinkommen = requireNonNull(finanzDatenDTO).getMassgebendesEinkVorAbzFamGr(gesuchstellerNumber);

		return finanzielleSituationRechner.calculateByVeranlagung(finSit.getFinanzielleSituationJA())
			? createTablesDeklarationByVeranlagung(finSit, gesuchstellerName, requireNonNull(massgebendesEinkommen))
			: createTablesBySelbstdeklaration(gesuchstellerName, gesuchstellerNumber);

	}

	private PdfPTable createTablesDeklarationByVeranlagung(
		@Nonnull FinanzielleSituationContainer finSit,
		@Nonnull String gesuchstellerName,
		@Nonnull BigDecimal massgebendesEinkommen
	) {

		return createFinSitTableSingleGS(
			createRow(translate(BERECHNUNG_GEMAESS_VERANLAGUNG), gesuchstellerName),
			createRow(translate(STEUERBARES_EINKOMMEN), FinanzielleSituation::getSteuerbaresEinkommen, finSit),
			createRow(translate(STEUERBARES_VERMOEGEN), FinanzielleSituation::getSteuerbaresVermoegen, finSit),
			createRow(translate(NETTOEINKUENFTE_LIEGENSCHAFTEN), FinanzielleSituation::getAbzuegeLiegenschaft, finSit),
			createRow(translate(VERRECHENBARE_GESCHAEFTSVERLUSTE), FinanzielleSituation::getGeschaeftsverlust, finSit),
			createRow(translate(EINKAEUFE_VORSORGE), FinanzielleSituation::getEinkaeufeVorsorge, finSit),
			createRow(translate(MASSG_EINK), printCHF(massgebendesEinkommen))
				.bold()
		);
	}

	private PdfPTable createTablesBySelbstdeklaration(
		@Nonnull String gesuchstellerName,
		@Nonnull Integer gesuchstellerNumber
	) {
		FinanzielleSituationRow title = createRow(translate(BERECHNUNG_GEMAESS_SELBSTDEKLARATION), gesuchstellerName);

		return createTableSelbstdeklaration(false, gesuchstellerNumber, requireNonNull(finanzDatenDTO), title);
	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		requireNonNull(ekvBasisJahrPlus1);
		createPageEkv(ekvBasisJahrPlus1, gesuch.getGesuchsperiode().getBasisJahrPlus1(), document, true);
	}


	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		requireNonNull(ekvBasisJahrPlus2);
		//EKV2 only needs to be printed on new page if there is no ekv1
		boolean isPrintOnNewPage =  ekvBasisJahrPlus1 == null;
		createPageEkv(ekvBasisJahrPlus2, gesuch.getGesuchsperiode().getBasisJahrPlus2(), document, isPrintOnNewPage);
	}

	private void createPageEkv(@Nonnull FinanzielleSituationResultateDTO finSitDTO, int basisJahr, @Nonnull Document document, boolean isPrintOnNewPage) {
		if (isPrintOnNewPage) {
			document.newPage();
			document.add(createTitleEkv());
			document.add(createIntroEkv());
		} else {
			addSpacing(document);
		}

		String gesuchstellerName = getNameForFinSit1();

		FinanzielleSituationRow title = createRow(translate(EKV_TITLE,  printJahr(basisJahr)), gesuchstellerName);
		if (hasSecondGesuchsteller) {
			title.setGs2(requireNonNull(gesuch.getGesuchsteller2()).extractFullName());
		}
		//EKV Tabelle ist dieselbe wie die Selbstdeklarations-Tabelle
		document.add(createTableSelbstdeklaration(hasSecondGesuchsteller, 1, finSitDTO, title));
	}

	private PdfPTable createTableSelbstdeklaration(
		boolean hasSecondGS,
		int gesuchstellerNumberValue1,
		@Nonnull FinanzielleSituationResultateDTO finSitDTO,
		@Nullable FinanzielleSituationRow titleRow) {

		var table = createFinSitTable(hasSecondGS).addRows(
			titleRow,
			createRow(TOTAL_EINKUENFTE, finSitDTO.getEinkommen(gesuchstellerNumberValue1), hasSecondGS, finSitDTO.getEinkommenGS2()),
			createRow(TOTAL_ABZUEGE, finSitDTO.getAbzuege(gesuchstellerNumberValue1), hasSecondGS, finSitDTO.getAbzuegeGS2()),
			createRow(ANRECHENBARES_VERMOEGEN_GEMAESS_SELBSTDEKLARATION, finSitDTO.getVermoegenXPercentAnrechenbar(gesuchstellerNumberValue1), hasSecondGS, finSitDTO.getVermoegenXPercentAnrechenbarGS2()),
			createRow(
				MASSG_EINK,
				finSitDTO.getMassgebendesEinkVorAbzFamGr(gesuchstellerNumberValue1),
				hasSecondGS,
				finSitDTO.getMassgebendesEinkVorAbzFamGrGS2())
				.bold(!hasSecondGS),
			hasSecondGS ? createRow(TOTAL_MASSG_EINK, null, true, finSitDTO.getMassgebendesEinkVorAbzFamGr()).bold() : null
		);

		return table.createTable();
	}

	private void addSpacing(@Nonnull Document document) {
		Paragraph p = new Paragraph();
		p.setSpacingAfter(15);
		document.add(p);
	}

	private boolean gesuchHasTwoFinSit() {
		return gesuch.getGesuchsteller2() != null
			&& !isVerheiratet();
	}

	private boolean isVerheiratet() {
		requireNonNull(gesuch.getFamiliensituationContainer());
		Familiensituation familiensituation = requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());

		return familiensituation.getFamilienstatus() == EnumFamilienstatus.VERHEIRATET;
	}
}
