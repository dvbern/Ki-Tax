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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

public class FinanzielleSituationPdfGeneratorSolothurn extends FinanzielleSituationPdfGenerator {

	private static final String EIKOMMEN_TITLE = "PdfGeneration_FinSit_EinkommenTitle";
	private static final String NETTOLOHN = "PdfGeneration_FinSit_Nettolohn";
	private static final String ENT_UNTERHALTSBEITRAEGE = "PdfGeneration_FinSit_EntrichteteUnterhaltsbeitraege";
	private static final String KINDER_IN_AUSBILDUNG = "PdfGeneration_KinderInAusbildung";
	private static final String STEUERBARES_VERMOEGEN = "PdfGeneration_FinSit_SteuerbaresVermoegen";
	private static final String FOOTER_STEUERBARES_VERMOEGEN = "PdfGeneration_FinSit_FooterSteuerbaresVermoegen";
	private static final String FOOTER_MASSG_EINK = "PdfGeneration_FinSit_FooterMassgEink";
	private static final String BRUTTOLOHN = "PdfGeneration_FinSit_Bruttolohn";
	private static final String FOOTER_BRUTTOLOHN = "PdfGeneration_FinSit_FooterBruttolohn";
	private static final String VERMOEGEN = "PdfGeneration_FinSit_VermoegenTitle";
	private static final String ABZUEGE = "PdfGeneration_FinSit_Abzuege";

	@Nonnull
	private Gesuchsteller gs1;
	@Nonnull
	private Gesuchsteller gs2;
	@Nonnull
	private FinanzielleSituationContainer basisJahrGS1;
	@Nullable
	private FinanzielleSituationContainer basisJahrGS2;
	@Nullable
	protected Einkommensverschlechterung ekv1GS1;
	@Nullable
	protected Einkommensverschlechterung ekv1GS2;
	@Nullable
	protected Einkommensverschlechterung ekv2GS1;
	@Nullable
	protected Einkommensverschlechterung ekv2GS2;

	private final List<String> footers = new ArrayList<>();

	public FinanzielleSituationPdfGeneratorSolothurn(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum
	) {
		super(gesuch, verfuegungFuerMassgEinkommen, stammdaten, erstesEinreichungsdatum);
	}

	protected void initializeValues() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		basisJahrGS1 = gesuch.getGesuchsteller1().getFinanzielleSituationContainer();

		Objects.requireNonNull(gesuch.getGesuchsteller1().getGesuchstellerJA());
		gs1 = gesuch.getGesuchsteller1().getGesuchstellerJA();

		boolean hasSecondGS = false;
		if (
			gesuch.getGesuchsteller2() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA() != null
		) {
			basisJahrGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer();
			Objects.requireNonNull(gesuch.getGesuchsteller2().getGesuchstellerJA());
			gs2 = gesuch.getGesuchsteller2().getGesuchstellerJA();
			hasSecondGS = true;
		}

		finanzDatenDTO = finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, hasSecondGS);
		initialzeEkv();
	}

	@Override
	protected void initialzeEkv() {
		super.initialzeEkv();

		Objects.requireNonNull(gesuch.getGesuchsteller1());
		EinkommensverschlechterungContainer ekvContainerGS1 =
			gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		if (ekvContainerGS1 != null) {
			ekv1GS1 = ekvContainerGS1.getEkvJABasisJahrPlus1();
			ekv2GS1 = ekvContainerGS1.getEkvJABasisJahrPlus2();
		}

		if(hasSecondGesuchsteller) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			EinkommensverschlechterungContainer ekvContainerGS2 =
				gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer();

			if(ekvContainerGS2 != null) {
				ekv1GS2 = ekvContainerGS2.getEkvJABasisJahrPlus1();
				ekv2GS2 = ekvContainerGS2.getEkvJABasisJahrPlus2();
			}
		}
	}

	protected void createPageBasisJahr(
		@Nonnull PdfGenerator generator,
		@Nonnull Document document
	) {
		Objects.requireNonNull(finanzDatenDTO);
		document.add(createIntroBasisjahr());

		createMassgebendesEinkommenTableForGesuchsteller(
			document,
			basisJahrGS1.getFinanzielleSituationJA(),
			basisJahrGS1.getFinanzielleSituationGS(),
			gs1.getFullName()
		);
		// vermoegen is the same in every deklaration typ
		addVermoegenTable(
			document,
			basisJahrGS1.getFinanzielleSituationJA(),
			basisJahrGS1.getFinanzielleSituationGS(),
			finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS1()
		);

		if (basisJahrGS2 != null) {
			addSpacing(document);
			createMassgebendesEinkommenTableForGesuchsteller(
				document,
				basisJahrGS2.getFinanzielleSituationJA(),
				basisJahrGS2.getFinanzielleSituationGS(),
				gs2.getFullName()
			);
			// vermoegen is the same in every deklaration typ
			addVermoegenTable(
				document,
				basisJahrGS2.getFinanzielleSituationJA(),
				basisJahrGS2.getFinanzielleSituationGS(),
				finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS2()
			);

			addSpacing(document);
			createTablezusammenzug(document, gs1.getFullName(), gs2.getFullName());
		}

		var translatedFooters = footers.stream()
			.map(this::translate)
			.collect(Collectors.toList());

		createFusszeile(generator.getDirectContent(), translatedFooters);
	}

	private void createMassgebendesEinkommenTableForGesuchsteller(
		@Nonnull Document document,
		@Nonnull FinanzielleSituation finanzielleSituationJA,
		@Nonnull FinanzielleSituation finanzielleSituationGS,
		@Nonnull String gesuchstellerName
	) {
		if (finanzielleSituationRechner.calculateByVeranlagung(finanzielleSituationJA)) {
			createTablesDeklarationByVeranlagung(document, finanzielleSituationJA, finanzielleSituationGS, gesuchstellerName);
		} else {
			createTablesDeklarationByBruttolohn(document, finanzielleSituationJA, finanzielleSituationGS, gesuchstellerName);
		}
	}

	private void createTablesDeklarationByVeranlagung(
		@Nonnull Document document,
		@Nonnull FinanzielleSituation finanzielleSituationJA,
		@Nonnull FinanzielleSituation finanzielleSituationGS,
		String gesuchstellerName
	) {
		var einkommenTable = createEinkommenTable(
			finanzielleSituationJA,
			finanzielleSituationGS,
			gesuchstellerName
		);
		var abzuegeTable = createAbzuegeTable(
			finanzielleSituationJA,
			finanzielleSituationGS
		);
		document.add(einkommenTable);
		document.add(abzuegeTable);
	}

	private PdfPTable createEinkommenTable(
		@Nonnull FinanzielleSituation finSit,
		@Nonnull FinanzielleSituation finSitUrsprunglich,
		@Nonnull String gesuchstellerName
	) {
		FinanzielleSituationTable tableEinkommen =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(EIKOMMEN_TITLE), gesuchstellerName);

		FinanzielleSituationRow nettolohn = createRow(
			translate(NETTOLOHN),
			FinanzielleSituation::getNettolohn,
			finSit,
			finSitUrsprunglich
		);

		tableEinkommen.addRow(title);
		tableEinkommen.addRow(nettolohn);
		return tableEinkommen.createTable();
	}

	private PdfPTable createAbzuegeTable(
		@Nonnull FinanzielleSituation finSit,
		@Nonnull FinanzielleSituation finSitUrsprunglich
	) {
		FinanzielleSituationTable tableAbzuege =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(ABZUEGE), "");

		FinanzielleSituationRow abzuegeKinder = createRow(
			translate(KINDER_IN_AUSBILDUNG),
			FinanzielleSituation::getAbzuegeKinderAusbildung,
			finSit,
			finSitUrsprunglich
		);

		FinanzielleSituationRow unterhaltsbeitraege = createRow(
			translate(ENT_UNTERHALTSBEITRAEGE),
			FinanzielleSituation::getUnterhaltsBeitraege,
			finSit,
			finSitUrsprunglich
		);

		tableAbzuege.addRow(title);
		tableAbzuege.addRow(abzuegeKinder);
		tableAbzuege.addRow(unterhaltsbeitraege);
		return tableAbzuege.createTable();
	}

	private void addVermoegenTable(
		@Nonnull Document document,
		@Nonnull FinanzielleSituation finSit,
		@Nonnull FinanzielleSituation finSitUrsprunglich,
		@Nonnull BigDecimal massgebendesEinkommen
	) {
		FinanzielleSituationTable vermoegenTable =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				true);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(VERMOEGEN), "");

		FinanzielleSituationRow nettovermoegen = createRow(
			translate(STEUERBARES_VERMOEGEN),
			FinanzielleSituation::getSteuerbaresVermoegen,
			finSit,
			finSitUrsprunglich
		);

		addFooter(nettovermoegen, FOOTER_STEUERBARES_VERMOEGEN);

		FinanzielleSituationRow massgEinkommen = new FinanzielleSituationRow(
			translate(MASSG_EINK),
			massgebendesEinkommen
		);
		addFooter(massgEinkommen, FOOTER_MASSG_EINK);
		vermoegenTable.addRow(title);
		vermoegenTable.addRow(nettovermoegen);
		vermoegenTable.addRow(massgEinkommen);

		document.add(vermoegenTable.createTable());
	}

	private void createTablesDeklarationByBruttolohn(
		@Nonnull Document document,
		@Nonnull FinanzielleSituation finanzielleSituationJA,
		@Nonnull FinanzielleSituation finanzielleSituationGS,
		@Nonnull String gesuchstellerName
	) {

		FinanzielleSituationTable bruttolohnTable =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(EIKOMMEN_TITLE), gesuchstellerName);

		FinanzielleSituationRow bruttolohn = createRow(
			translate(BRUTTOLOHN),
			FinanzielleSituation::getBruttoLohn,
			finanzielleSituationJA,
			finanzielleSituationGS
		);

		addFooter(bruttolohn, FOOTER_BRUTTOLOHN);
		bruttolohnTable.addRow(title);
		bruttolohnTable.addRow(bruttolohn);

		document.add(bruttolohnTable.createTable());

	}

	private void addFooter(@Nonnull FinanzielleSituationRow row, @Nonnull String footer) {
		if (!footers.contains(footer)) {
			footers.add(footer);
		}
		int supertext = footers.indexOf(footer) + 1;
		row.setSupertext(" " + supertext);
	}

	private void addSpacing(@Nonnull Document document) {
		Paragraph p = new Paragraph();
		p.setSpacingAfter(15);
		document.add(p);
	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		Objects.requireNonNull(ekvBasisJahrPlus1);
		Objects.requireNonNull(ekv1GS1);
		createPageEkv(ekvBasisJahrPlus1, ekv1GS1, ekv1GS2, gesuch.getGesuchsperiode().getBasisJahrPlus1(), document, true);
	}

	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		Objects.requireNonNull(ekvBasisJahrPlus2);
		Objects.requireNonNull(ekv2GS1);
		createPageEkv(ekvBasisJahrPlus2, ekv2GS1, ekv2GS2, gesuch.getGesuchsperiode().getBasisJahrPlus2(), document, isEkv2PrintOnNewPage());
	}

	private boolean isEkv2PrintOnNewPage() {
		//EKV2 only needs to be printed on new page if there is no ekv1
		return ekvBasisJahrPlus1 == null;
	}

	private void createPageEkv(
		FinanzielleSituationResultateDTO ekvBasisJahr,
		Einkommensverschlechterung ekvGS1,
		@Nullable Einkommensverschlechterung ekvGS2,
		int basisJahr,
		Document document,
		boolean isPrintOnNewPage) {

		if(isPrintOnNewPage) {
			document.newPage();
			document.add(createTitleEkv(null));
			document.add(createIntroEkv());
		} else {
			addSpacing(document);
		}

		document.add(createTableEkv(ekvBasisJahr, ekvGS1, ekvGS2, basisJahr));
	}

	private Element createTableEkv(
		FinanzielleSituationResultateDTO ekvBasisJahr,
		Einkommensverschlechterung ekvGS1,
		@Nullable Einkommensverschlechterung ekvGS2,
		int basisJahr) {
		FinanzielleSituationRow title = createTableTitleForEkv(basisJahr);
		FinanzielleSituationTable table = createFinSitTable(hasSecondGesuchsteller);
		table.addRow(title);

		table.addRow(createRow(BRUTTOLOHN, ekvBasisJahr.getBruttolohnJahrGS1(), hasSecondGesuchsteller, ekvBasisJahr.getBruttolohnJahrGS2()));

		BigDecimal nettovermoegenGS2 = ekvGS2 != null ? ekvGS2.getNettoVermoegen() : null;
		table.addRow(createRow(NETTOVERMOEGEN, ekvGS1.getNettoVermoegen(), hasSecondGesuchsteller, nettovermoegenGS2));

		table.addRow(createRow(MASSG_EINK, ekvBasisJahr.getMassgebendesEinkVorAbzFamGrGS1(), hasSecondGesuchsteller, ekvBasisJahr.getMassgebendesEinkVorAbzFamGrGS2()));

		if(hasSecondGesuchsteller) {
			table.addRow(createRow(TOTAL_MASSG_EINK, null, true, ekvBasisJahr.getMassgebendesEinkVorAbzFamGr()));
		}

		return table.createTable();
	}
}
