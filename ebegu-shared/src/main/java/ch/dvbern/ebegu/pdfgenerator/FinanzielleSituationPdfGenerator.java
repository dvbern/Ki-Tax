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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.pdfgenerator.FinSitTable.FinSitRow;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.lib.invoicegenerator.dto.PageConfiguration;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;
import static com.lowagie.text.Utilities.millimetersToPoints;

@SuppressWarnings("PMD.AvoidDuplicateLiterals") //TODO (team) Entfernen, wenn Dummydaten ersetzt!
public class FinanzielleSituationPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String BASISJAHR = "PdfGeneration_FinSit_BasisJahr";
	private static final String NAME = "PdfGeneration_FinSit_Name";
	private static final String EIKOMMEN_TITLE = "PdfGeneration_FinSit_EinkommenTitle";
	private static final String NETTOLOHN = "PdfGeneration_FinSit_Nettolohn";
	private static final String FAMILIENZULAGEN = "PdfGeneration_FinSit_Familienzulagen";
	private static final String ERSATZEINKOMMEN = "PdfGeneration_FinSit_Ersatzeinkommen";
	private static final String UNTERHALTSBEITRAEGE = "PdfGeneration_FinSit_Unterhaltsbeitraege";
	private static final String GESCHAEFTSGEWINN = "PdfGeneration_FinSit_Geschaeftsgewinn";
	private static final String EINKOMMEN_ZWISCHENTOTAL = "PdfGeneration_FinSit_EinkommenZwischentotal";
	private static final String EINKOMMEN_TOTAL = "PdfGeneration_FinSit_EinkommenTotal";
	private static final String FUSSZEILE_1 = "PdfGeneration_FinSit_Fusszeile1";
	private static final String FUSSZEILE_2 = "PdfGeneration_FinSit_Fusszeile2";

	private static final String EKV_TITLE = "PdfGeneration_FinSit_Ekv_Title";
	private static final String EKV_DATUM = "PdfGeneration_FinSit_Ekv_Datum";
	private static final String EKV_GRUND = "PdfGeneration_FinSit_Ekv_Grund";

	private static final String MASSG_EINK_TITLE = "PdfGeneration_MassgEink_Title";


	// TODO (hefr) Massgebendes Einkommen wird noch nicht berechnet
	// TODO (hefr) Tabelle Massgebendes Einkommen fehlt noch ganz
	// TODO (hefr) Diverse Texte noch nicht übersetzt
	// TODO (hefr) Darstellung/Abstände überprüfen

	public FinanzielleSituationPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		final boolean draft
	) {
		super(gesuch, stammdaten, draft);
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return "Berechnung der finanziellen Situation";
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();

			// Basisjahr
			createPageBasisJahr(generator, document);
			// Eventuelle Einkommenverschlechterung
			EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
			Objects.requireNonNull(ekvInfo);
			if (ekvInfo.getEkvFuerBasisJahrPlus1()) {
				createPageEkv1(generator, document, ekvInfo);
			}
			if (ekvInfo.getEkvFuerBasisJahrPlus2()) {
				createPageEkv2(generator, document, ekvInfo);
			}
			// Massgebendes Einkommen
			createPageMassgebendesEinkommen(generator, document);
		};
	}

	private boolean hasSecondGesuchsteller() {
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		return familiensituation != null && familiensituation.hasSecondGesuchsteller();
	}

	private void createPageBasisJahr(@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator, @Nonnull Document document) {
		createFusszeile(generator.getDirectContent());
		AbstractFinanzielleSituation basisJahrGS1;
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		basisJahrGS1 = gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();

		AbstractFinanzielleSituation basisJahrGS2 = null;
		if (hasSecondGesuchsteller()) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			Objects.requireNonNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
			basisJahrGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA();
		}

		document.add(createIntroBasisjahr());
		document.add(createTableEinkommen(basisJahrGS1, basisJahrGS2));
		document.add(createTableVermoegen(basisJahrGS1, basisJahrGS2));
		document.add(createTableAbzuege(basisJahrGS1, basisJahrGS2));
		document.add(createTableZusammenzug(basisJahrGS1, basisJahrGS2));
	}

	private void createPageEkv1(@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator, @Nonnull Document document, @Nonnull EinkommensverschlechterungInfo ekvInfo) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		EinkommensverschlechterungContainer ekvContainerGS1 = gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		Objects.requireNonNull(ekvContainerGS1);


		document.newPage();
		createFusszeile(generator.getDirectContent());
		document.add(PdfUtil.createBoldParagraph(ServerMessageUtil.getMessage(EKV_TITLE, gesuch.getGesuchsperiode().getBasisJahrPlus1()), 2));
		document.add(createIntroEkv1(ekvInfo));

		Einkommensverschlechterung ekv1GS1 = null;
		ekv1GS1 = ekvContainerGS1.getEkvJABasisJahrPlus1();

		Einkommensverschlechterung ekv1GS2 = null;
		if (hasSecondGesuchsteller()) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			Objects.requireNonNull(gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer());
			ekv1GS2 = gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1();
		}

		document.add(createTableEinkommen(ekv1GS1, ekv1GS2));
		document.add(createTableVermoegen(ekv1GS1, ekv1GS2));
		document.add(createTableAbzuege(ekv1GS1, ekv1GS2));
		document.add(createTableZusammenzug(ekv1GS1, ekv1GS2));
	}

	private void createPageEkv2(@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator, @Nonnull Document document, @Nonnull EinkommensverschlechterungInfo ekvInfo) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		EinkommensverschlechterungContainer ekvContainerGS1 = gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		Objects.requireNonNull(ekvContainerGS1);

		document.newPage();
		createFusszeile(generator.getDirectContent());
		document.add(PdfUtil.createBoldParagraph(ServerMessageUtil.getMessage(EKV_TITLE, gesuch.getGesuchsperiode().getBasisJahrPlus2()), 2));
		document.add(createIntroEkv2(ekvInfo));

		Einkommensverschlechterung ekv2GS1 = null;

		ekv2GS1 = ekvContainerGS1.getEkvJABasisJahrPlus2();

		Einkommensverschlechterung ekv2GS2 = null;
		if (hasSecondGesuchsteller()) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			Objects.requireNonNull(gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer());
			ekv2GS2 = gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2();
		}

		document.add(createTableEinkommen(ekv2GS1, ekv2GS2));
		document.add(createTableVermoegen(ekv2GS1, ekv2GS2));
		document.add(createTableAbzuege(ekv2GS1, ekv2GS2));
		document.add(createTableZusammenzug(ekv2GS1, ekv2GS2));
	}

	private void createPageMassgebendesEinkommen(@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator, @Nonnull Document document) {
		final String[][] valuesMassgebendesEinkommen = {
			{"von", "bis", "Einkommensjahr", "massgebendes Einkommen vor Abzug der Familiengrösse", "Familiengrösse", "Abzug der Familiengrösse", "massgebendes Einkommen nach Abzug der Familiengrösse"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"},
			{"01.08.2018", "31.08.2018", "2018", "119.00", "2", "0.00", "119.00"}
		};
		final float[] widthMassgebendesEinkommen = {5,5,6,10,5,10,10};
		final int[] alignementMassgebendesEinkommen = {Element.ALIGN_RIGHT, Element.ALIGN_RIGHT,Element.ALIGN_RIGHT,Element.ALIGN_RIGHT,Element.ALIGN_RIGHT,Element.ALIGN_RIGHT,Element.ALIGN_RIGHT};

		document.setPageSize(PageSize.A4.rotate());
		document.newPage();
		document.add(PdfUtil.createBoldParagraph(ServerMessageUtil.getMessage(MASSG_EINK_TITLE), 2));
		document.add(createIntroMassgebendesEinkommen());
		document.add(PdfUtil.createTable(valuesMassgebendesEinkommen, widthMassgebendesEinkommen, alignementMassgebendesEinkommen, 0));
	}

	@Nonnull
	private PdfPTable createIntroBasisjahr() {
		List<LabelValuePair> introBasisjahr = new ArrayList<>();
		introBasisjahr.add(new LabelValuePair(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introBasisjahr.add(new LabelValuePair(BASISJAHR, String.valueOf(gesuch.getGesuchsperiode().getBasisJahr())));
		return PdfUtil.creatreIntroTable(introBasisjahr);
	}

	@Nonnull
	private PdfPTable createIntroEkv1(@Nonnull EinkommensverschlechterungInfo ekvInfo) {
		List<LabelValuePair> introEkv1 = new ArrayList<>();
		introEkv1.add(new LabelValuePair(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introEkv1.add(new LabelValuePair(EKV_DATUM, PdfUtil.printLocalDate(ekvInfo.getStichtagFuerBasisJahrPlus1())));
		introEkv1.add(new LabelValuePair(EKV_GRUND, PdfUtil.printString(ekvInfo.getGrundFuerBasisJahrPlus1())));
		return PdfUtil.creatreIntroTable(introEkv1);
	}

	@Nonnull
	private PdfPTable createIntroEkv2(@Nonnull EinkommensverschlechterungInfo ekvInfo) {
		List<LabelValuePair> introEkv2 = new ArrayList<>();
		introEkv2.add(new LabelValuePair(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introEkv2.add(new LabelValuePair(EKV_DATUM, PdfUtil.printLocalDate(ekvInfo.getStichtagFuerBasisJahrPlus2())));
		introEkv2.add(new LabelValuePair(EKV_GRUND, PdfUtil.printString(ekvInfo.getGrundFuerBasisJahrPlus2())));
		return PdfUtil.creatreIntroTable(introEkv2);
	}

	@Nonnull
	private PdfPTable createIntroMassgebendesEinkommen() {
		List<LabelValuePair> introMassgEinkommen = new ArrayList<>();
		introMassgEinkommen.add(new LabelValuePair(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introMassgEinkommen.add(new LabelValuePair(NAME, String.valueOf(gesuch.extractFullnamesString())));
		return PdfUtil.creatreIntroTable(introMassgEinkommen);
	}

	@Nonnull
	private PdfPTable createTableEinkommen(@Nonnull AbstractFinanzielleSituation gs1, @Nullable AbstractFinanzielleSituation gs2) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal totalEinkommenBeiderGS = FinanzielleSituationRechner.calcTotalEinkommen(gs1, gs2);

		FinSitRow einkommenTitle = new FinSitRow(ServerMessageUtil.getMessage(EIKOMMEN_TITLE), gesuch.getGesuchsteller1().extractFullName());
		FinSitRow nettolohn = new FinSitRow(ServerMessageUtil.getMessage(NETTOLOHN), gs1.getNettolohn());
		FinSitRow familienzulagen = new FinSitRow(ServerMessageUtil.getMessage(FAMILIENZULAGEN), gs1.getFamilienzulage());
		FinSitRow ersatzeinkommen = new FinSitRow(ServerMessageUtil.getMessage(ERSATZEINKOMMEN), gs1.getErsatzeinkommen());
		FinSitRow unterhaltsbeitraege = new FinSitRow(ServerMessageUtil.getMessage(UNTERHALTSBEITRAEGE), gs1.getErhalteneAlimente());
		FinSitRow geschaftsgewinn = new FinSitRow(ServerMessageUtil.getMessage(GESCHAEFTSGEWINN), gs1.getGeschaeftsgewinnBasisjahr());
		FinSitRow zwischentotal = new FinSitRow(ServerMessageUtil.getMessage(EINKOMMEN_ZWISCHENTOTAL), gs1.getZwischentotalEinkommen());
		FinSitRow total = new FinSitRow(ServerMessageUtil.getMessage(EINKOMMEN_TOTAL), "");

		if (gs2 != null) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());

			einkommenTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			nettolohn.setGs2(gs2.getNettolohn());
			familienzulagen.setGs2(gs2.getFamilienzulage());
			ersatzeinkommen.setGs2(gs2.getErsatzeinkommen());
			unterhaltsbeitraege.setGs2(gs2.getErhalteneAlimente());
			geschaftsgewinn.setGs2(gs2.getGeschaeftsgewinnBasisjahr());
			zwischentotal.setGs2(gs2.getZwischentotalEinkommen());
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalEinkommenBeiderGS);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalEinkommenBeiderGS);
		}

		FinSitTable tableEinkommen = new FinSitTable(hasSecondGesuchsteller());
		tableEinkommen.addRow(einkommenTitle);
		tableEinkommen.addRow(nettolohn);
		tableEinkommen.addRow(familienzulagen);
		tableEinkommen.addRow(ersatzeinkommen);
		tableEinkommen.addRow(unterhaltsbeitraege);
		tableEinkommen.addRow(geschaftsgewinn);
		tableEinkommen.addRow(zwischentotal);
		tableEinkommen.addRow(total);
		return tableEinkommen.createTable();
	}

	@Nonnull
	private PdfPTable createTableVermoegen(@Nonnull AbstractFinanzielleSituation gs1, @Nullable AbstractFinanzielleSituation gs2) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal totalVermoegenBeiderGS = FinanzielleSituationRechner.calcTotalVermoegen(gs1, gs2);
		BigDecimal vermoegen5Prozent = FinanzielleSituationRechner.calcVermoegen5Prozent(gs1, gs2);

		FinSitRow vermoegenTitle = new FinSitRow("Nettovermögen", gesuch.getGesuchsteller1().extractFullName());
		FinSitRow bruttovermoegen = new FinSitRow("Bruttovermögen", gs1.getBruttovermoegen());
		FinSitRow schulden = new FinSitRow("Schulden", gs1.getSchulden());
		FinSitRow zwischentotal = new FinSitRow("Zwischentotal Nettovermögen", gs1.getZwischentotalVermoegen());
		FinSitRow total = new FinSitRow("Zwischentotal Nettovermögen insgesamt ²", "");
		FinSitRow vermoegen5Percent = new FinSitRow("5% Nettovermögen","");

		if (gs2 != null) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());

			vermoegenTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			bruttovermoegen.setGs2(gs2.getBruttovermoegen());
			schulden.setGs2(gs2.getSchulden());
			zwischentotal.setGs2(gs2.getZwischentotalVermoegen());
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalVermoegenBeiderGS);
			vermoegen5Percent.setGs2(vermoegen5Prozent);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalVermoegenBeiderGS);
			vermoegen5Percent.setGs1(vermoegen5Prozent);
		}
		FinSitTable table = new FinSitTable(hasSecondGesuchsteller());
		table.addRow(vermoegenTitle);
		table.addRow(bruttovermoegen);
		table.addRow(schulden);
		table.addRow(zwischentotal);
		table.addRow(total);
		table.addRow(vermoegen5Percent);
		return table.createTable();
	}

	@Nonnull
	private PdfPTable createTableAbzuege(@Nonnull AbstractFinanzielleSituation gs1, @Nullable AbstractFinanzielleSituation gs2) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal totalAbzuegeBeiderGS = FinanzielleSituationRechner.calcTotalAbzuege(gs1, gs2);

		FinSitRow abzuegeTitle = new FinSitRow("Abzüge", gesuch.getGesuchsteller1().extractFullName());
		FinSitRow unterhaltsbeitraege = new FinSitRow("Bezahlte Unterhaltsbeiträge", gs1.getGeleisteteAlimente());
		FinSitRow total = new FinSitRow("Total Abzüge", "");
		if (gs2 != null) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			abzuegeTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			unterhaltsbeitraege.setGs2(gs2.getGeleisteteAlimente());
			total.setGs1(MathUtil.DEFAULT.add(gs1.getZwischetotalAbzuege(), gs2.getZwischetotalAbzuege()));
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalAbzuegeBeiderGS);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalAbzuegeBeiderGS);
		}
		FinSitTable table = new FinSitTable(hasSecondGesuchsteller());
		table.addRow(abzuegeTitle);
		table.addRow(unterhaltsbeitraege);
		table.addRow(total);
		return table.createTable();
	}

	@Nonnull
	private PdfPTable createTableZusammenzug(@Nonnull AbstractFinanzielleSituation gs1, @Nullable AbstractFinanzielleSituation gs2) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());

		FinSitRow zusammenzugTitle = new FinSitRow("Zusammenzug", "");
		FinSitRow einkommen = new FinSitRow("Total Einkünfte", gs1.getZwischentotalEinkommen());
		FinSitRow vermoegen = new FinSitRow("5% Nettovermögen", FinanzielleSituationRechner.calcVermoegen5Prozent(gs1, gs2));
		FinSitRow abzuege = new FinSitRow("Total Abzüge", gs1.getZwischetotalAbzuege());
		FinSitRow total = new FinSitRow("Massgebendes Einkommen (vor Abzug für Familiengrösse", "TODO");

		if (gs2 != null) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			einkommen.setGs1(MathUtil.DEFAULT.add(gs1.getZwischentotalEinkommen(), gs2.getZwischentotalEinkommen()));
			abzuege.setGs1(MathUtil.DEFAULT.add(gs1.getZwischetotalAbzuege(), gs2.getZwischetotalAbzuege()));
		}
		FinSitTable table = new FinSitTable(false);
		table.addRow(zusammenzugTitle);
		table.addRow(einkommen);
		table.addRow(vermoegen);
		table.addRow(abzuege);
		table.addRow(total);
		return table.createTable();
	}

	private void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		ColumnText fz = new ColumnText(dirPdfContentByte);
		final float height = millimetersToPoints(20);
		final float width = millimetersToPoints(170);
		final float loverLeftX = millimetersToPoints(PageConfiguration.LEFT_PAGE_DEFAULT_MARGIN_MM);
		final float loverLeftY = millimetersToPoints(PdfLayoutConfiguration.LOGO_TOP_IN_MM / 4);
		fz.setSimpleColumn(loverLeftX, loverLeftY, loverLeftX + width, loverLeftY + height);
		fz.setLeading(0, DEFAULT_MULTIPLIED_LEADING);
		Font fontWithSize = PdfUtilities.createFontWithSize(8);
		fz.addText(new Phrase(ServerMessageUtil.getMessage(FUSSZEILE_1), fontWithSize));
		fz.addText(new Phrase('\n' + ServerMessageUtil.getMessage(FUSSZEILE_2), fontWithSize));
		fz.go();
	}
}
