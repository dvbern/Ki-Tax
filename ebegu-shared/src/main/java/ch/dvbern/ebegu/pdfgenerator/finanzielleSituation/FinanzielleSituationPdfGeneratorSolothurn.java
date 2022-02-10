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
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;

public class FinanzielleSituationPdfGeneratorSolothurn extends FinanzielleSituationPdfGenerator {

	private static final String EIKOMMEN_TITLE = "PdfGeneration_FinSit_EinkommenTitle";
	private static final String NETTOLOHN = "PdfGeneration_FinSit_Nettolohn";
	private static final String FAMILIENZULAGEN = "PdfGeneration_FinSit_Familienzulagen";
	private static final String ERSATZEINKOMMEN = "PdfGeneration_FinSit_Ersatzeinkommen";
	private static final String ERH_UNTERHALTSBEITRAEGE = "PdfGeneration_FinSit_ErhalteneUnterhaltsbeitraege";
	private static final String KINDER_IN_AUSBILDUNG = "PdfGeneration_KinderInAusbildung";
	private static final String GESCHAEFTSGEWINN = "PdfGeneration_FinSit_Geschaeftsgewinn";
	private static final String EINKOMMEN_ZWISCHENTOTAL = "PdfGeneration_FinSit_EinkommenZwischentotal";
	private static final String EINKOMMEN_TOTAL = "PdfGeneration_FinSit_EinkommenTotal";
	private static final String STEUERBARES_VERMOEGEN = "PdfGeneration_FinSit_SteuerbaresVermoegen";
	private static final String STEUERBARES_VERMOEGEN_5_PERCENT = "PdfGeneration_FinSit_SteuerbaresVermoegen5Percent";
	private static final String BRUTTOLOHN = "PdfGeneration_FinSit_Bruttolohn";
	private static final String VERMOEGEN = "PdfGeneration_FinSit_VermoegenTitle";
	private static final String BRUTTOVERMOEGEN = "PdfGeneration_FinSit_Bruttovermoegen";
	private static final String SCHULDEN = "PdfGeneration_FinSit_Schulden";
	private static final String NETTOVERMOEGEN_ZWISCHENTOTAL = "PdfGeneration_FinSit_Nettovermoegen_Zwischentotal";
	private static final String NETTOVERMOEGEN_TOTAL = "PdfGeneration_FinSit_Nettovermoegen_Total";
	private static final String NETTOVERMOEGEN_5_PROZENT = "PdfGeneration_FinSit_Nettovermoegen_5_Prozent";
	private static final String ABZUEGE = "PdfGeneration_FinSit_Abzuege";
	private static final String UNTERHALTSBEITRAEGE_BEZAHLT = "PdfGeneration_FinSit_UnterhaltsbeitraegeBezahlt";
	private static final String ABZUEGE_TOTAL = "PdfGeneration_FinSit_Abzuege_Total";
	private static final String ZUSAMMENZUG = "PdfGeneration_FinSit_Zusammenzug";
	private static final String FUSSZEILE_EINKOMMEN = "PdfGeneration_FinSit_Fusszeile_Einkuenfte";
	private static final String FUSSZEILE_VERMOEGEN = "PdfGeneration_FinSit_Fusszeile_Vermoegen";
	private static final String FUSSZEILE_ABZUEGE = "PdfGeneration_FinSit_Fusszeile_Abzuege";
	private static final String EKV_TITLE = "PdfGeneration_FinSit_Ekv_Title";

	@Nonnull
	private Gesuchsteller gs1;
	@Nonnull
	private Gesuchsteller gs2;
	@Nonnull
	private FinanzielleSituationContainer basisJahrGS1;
	@Nullable
	private FinanzielleSituationContainer basisJahrGS2;
	@Nullable
	private Einkommensverschlechterung ekv1GS1;
	@Nullable
	private Einkommensverschlechterung ekv1GS2;
	@Nullable
	private Einkommensverschlechterung ekv2GS1;
	@Nullable
	private Einkommensverschlechterung ekv2GS2;

	public FinanzielleSituationPdfGeneratorSolothurn(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum,
		@Nonnull AbstractFinanzielleSituationRechner finanzielleSituationRechner
	) {
		super(gesuch, verfuegungFuerMassgEinkommen, stammdaten, erstesEinreichungsdatum, finanzielleSituationRechner);
	}

	protected void initializeValues() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		basisJahrGS1 = gesuch.getGesuchsteller1().getFinanzielleSituationContainer();

		Objects.requireNonNull(gesuch.getGesuchsteller1().getGesuchstellerJA());
		gs1 = gesuch.getGesuchsteller1().getGesuchstellerJA();

		if (
			gesuch.getGesuchsteller2() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA() != null
		) {
			basisJahrGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer();
			Objects.requireNonNull(gesuch.getGesuchsteller2().getGesuchstellerJA());
			gs2 = gesuch.getGesuchsteller2().getGesuchstellerJA();
		}

	}

	protected void createPageBasisJahr(
		@Nonnull PdfGenerator generator,
		@Nonnull Document document
	) {
		createFusszeile(generator.getDirectContent());
		document.add(createIntroBasisjahr());

		if (finanzielleSituationRechner.calculateByVeranlagung(basisJahrGS1.getFinanzielleSituationJA())) {
			createTablesDeklarationByVeranlagung(document);
		} else {
			createTablesDeklarationByBruttolohn(document);
		}
		// TODO: continue
	}

	private void createTablesDeklarationByVeranlagung(
		@Nonnull Document document
	) {
		var einkommenTable = createEinkommenTable(
			basisJahrGS1.getFinanzielleSituationJA(),
			basisJahrGS1.getFinanzielleSituationGS()
		);
		var abzuegeTable = createAbzuegeTable(
			basisJahrGS1.getFinanzielleSituationJA(),
			basisJahrGS1.getFinanzielleSituationGS()
		);
		var vermoegenTable = createVermoegenTable(
			basisJahrGS1.getFinanzielleSituationJA(),
			basisJahrGS1.getFinanzielleSituationGS()
		);
		document.add(einkommenTable);
		document.add(abzuegeTable);
		document.add(vermoegenTable);
	}

	private PdfPTable createEinkommenTable(
		@Nonnull FinanzielleSituation finSit,
		@Nonnull FinanzielleSituation finSitUrsprunglich
	) {
		FinanzielleSituationTable tableEinkommen =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(EIKOMMEN_TITLE, mandant), gs1.getFullName());

		FinanzielleSituationRow nettolohn = createRow(
			translate(NETTOLOHN, mandant),
			FinanzielleSituation::getNettolohn,
			finSit,
			finSitUrsprunglich
		);

		FinanzielleSituationRow unterhaltsbeitraege = createRow(
			translate(ERH_UNTERHALTSBEITRAEGE, mandant),
			FinanzielleSituation::getUnterhaltsBeitraege,
			finSit,
			finSitUrsprunglich
		);

		tableEinkommen.addRow(title);
		tableEinkommen.addRow(nettolohn);
		tableEinkommen.addRow(unterhaltsbeitraege);
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
			translate(ABZUEGE, mandant), "");

		FinanzielleSituationRow abzuegeKinder = createRow(
			translate(KINDER_IN_AUSBILDUNG, mandant),
			FinanzielleSituation::getAbzuegeKinderAusbildung,
			finSit,
			finSitUrsprunglich
		);

		tableAbzuege.addRow(title);
		tableAbzuege.addRow(abzuegeKinder);
		return tableAbzuege.createTable();
	}

	private PdfPTable createVermoegenTable(
		@Nonnull FinanzielleSituation finSit,
		@Nonnull FinanzielleSituation finSitUrsprunglich
	) {
		FinanzielleSituationTable vermoegenTable =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				true);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(VERMOEGEN, mandant), "");

		FinanzielleSituationRow nettovermoegen = createRow(
			translate(STEUERBARES_VERMOEGEN, mandant),
			FinanzielleSituation::getSteuerbaresVermoegen,
			finSit,
			finSitUrsprunglich
		);

		var finSitCalculate =
			finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, false);

		FinanzielleSituationRow nettoVermoegen5Percent = new FinanzielleSituationRow(
			translate(STEUERBARES_VERMOEGEN_5_PERCENT, mandant),
			finSitCalculate.getMassgebendesEinkVorAbzFamGrGS1()
		);

		FinanzielleSituationRow massgEinkommen = new FinanzielleSituationRow(
			translate(MASSG_EINK, mandant),
			finSitCalculate.getMassgebendesEinkVorAbzFamGrGS1()
		);
		vermoegenTable.addRow(title);
		vermoegenTable.addRow(nettovermoegen);
		vermoegenTable.addRow(nettoVermoegen5Percent);
		vermoegenTable.addRow(massgEinkommen);
		return vermoegenTable.createTable();
	}

	private void createTablesDeklarationByBruttolohn(@Nonnull Document document) {
		var bruttolohnTable = createBruttolohnTable(
			basisJahrGS1.getFinanzielleSituationJA(),
			basisJahrGS1.getFinanzielleSituationGS()
		);

		var vermoegenTable = createVermoegenTable(
			basisJahrGS1.getFinanzielleSituationJA(),
			basisJahrGS1.getFinanzielleSituationGS()
		);

		document.add(bruttolohnTable);
		document.add(vermoegenTable);
	}

	@Nonnull
	private PdfPTable createBruttolohnTable(
		@Nonnull FinanzielleSituation finSit,
		@Nonnull FinanzielleSituation finSitUrsprunglich
	) {
		FinanzielleSituationTable bruttolohnTable =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(BRUTTOLOHN, mandant), gs1.getFullName());

		FinanzielleSituationRow bruttolohn = createRow(
			translate(BRUTTOLOHN, mandant),
			FinanzielleSituation::getBruttoLohn,
			finSit,
			finSitUrsprunglich
		);

		bruttolohnTable.addRow(title);
		bruttolohnTable.addRow(bruttolohn);
		return bruttolohnTable.createTable();
	}

	protected final FinanzielleSituationRow createRow(
		String message,
		Function<FinanzielleSituation, BigDecimal> getter,
		@Nullable FinanzielleSituation gs1,
		@Nullable FinanzielleSituation gs1Urspruenglich
	) {
		BigDecimal gs1BigDecimal = gs1 == null ? null : getter.apply(gs1);
		BigDecimal gs1UrspruenglichBigDecimal = gs1Urspruenglich == null ? null : getter.apply(gs1Urspruenglich);
		FinanzielleSituationRow row = new FinanzielleSituationRow(message, gs1BigDecimal);
		if (!MathUtil.isSameWithNullAsZero(gs1BigDecimal, gs1UrspruenglichBigDecimal)) {
			row.setGs1Urspruenglich(gs1UrspruenglichBigDecimal, sprache, mandant);
		}
		return row;
	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		// TODO: implement
	}

	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		// TODO: implement
	}

	private void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(dirPdfContentByte, Lists.newArrayList(
			translate(FUSSZEILE_EINKOMMEN),
			translate(FUSSZEILE_VERMOEGEN),
			translate(FUSSZEILE_ABZUEGE)));
	}
}
