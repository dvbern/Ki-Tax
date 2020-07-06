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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;

import static java.util.Objects.requireNonNull;

public class FinanzielleSituationPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String TITLE = "PdfGeneration_FinSit_Title";
	private static final String BASISJAHR = "PdfGeneration_FinSit_BasisJahr";
	private static final String NAME = "PdfGeneration_FinSit_Name";
	private static final String EIKOMMEN_TITLE = "PdfGeneration_FinSit_EinkommenTitle";
	private static final String NETTOLOHN = "PdfGeneration_FinSit_Nettolohn";
	private static final String FAMILIENZULAGEN = "PdfGeneration_FinSit_Familienzulagen";
	private static final String ERSATZEINKOMMEN = "PdfGeneration_FinSit_Ersatzeinkommen";
	private static final String ERH_UNTERHALTSBEITRAEGE = "PdfGeneration_FinSit_ErhalteneUnterhaltsbeitraege";
	private static final String GESCHAEFTSGEWINN = "PdfGeneration_FinSit_Geschaeftsgewinn";
	private static final String EINKOMMEN_ZWISCHENTOTAL = "PdfGeneration_FinSit_EinkommenZwischentotal";
	private static final String EINKOMMEN_TOTAL = "PdfGeneration_FinSit_EinkommenTotal";
	private static final String NETTOVERMOEGEN = "PdfGeneration_FinSit_Nettovermoegen";
	private static final String BRUTTOVERMOEGEN = "PdfGeneration_FinSit_Bruttovermoegen";
	private static final String SCHULDEN = "PdfGeneration_FinSit_Schulden";
	private static final String NETTOVERMOEGEN_ZWISCHENTOTAL = "PdfGeneration_FinSit_Nettovermoegen_Zwischentotal";
	private static final String NETTOVERMOEGEN_TOTAL = "PdfGeneration_FinSit_Nettovermoegen_Total";
	private static final String NETTOVERMOEGEN_5_PROZENT = "PdfGeneration_FinSit_Nettovermoegen_5_Prozent";
	private static final String ABZUEGE = "PdfGeneration_FinSit_Abzuege";
	private static final String UNTERHALTSBEITRAEGE_BEZAHLT = "PdfGeneration_FinSit_UnterhaltsbeitraegeBezahlt";
	private static final String ABZUEGE_TOTAL = "PdfGeneration_FinSit_Abzuege_Total";
	private static final String ZUSAMMENZUG = "PdfGeneration_FinSit_Zusammenzug";
	private static final String MASSG_EINKOMMEN_VOR_FAMILIENGROESSE =
		"PdfGeneration_FinSit_MassgebendesEinkommenVorFamiliengroesse";
	private static final String VON = "PdfGeneration_MassgEinkommen_Von";
	private static final String BIS = "PdfGeneration_MassgEinkommen_Bis";
	private static final String JAHR = "PdfGeneration_MassgEinkommen_Jahr";
	private static final String MASSG_EINK_VOR_ABZUG = "PdfGeneration_MassgEinkommen_MassgEinkVorAbzugFamGroesse";
	private static final String FAM_GROESSE = "PdfGeneration_MassgEinkommen_FamGroesse";
	private static final String ABZUG_FAM_GROESSE = "PdfGeneration_MassgEinkommen_AbzugFamGroesse";
	private static final String MASSG_EINK = "PdfGeneration_MassgEinkommen_MassgEink";
	private static final String FUSSZEILE_EINKOMMEN = "PdfGeneration_FinSit_Fusszeile_Einkuenfte";
	private static final String FUSSZEILE_VERMOEGEN = "PdfGeneration_FinSit_Fusszeile_Vermoegen";
	private static final String FUSSZEILE_ABZUEGE = "PdfGeneration_FinSit_Fusszeile_Abzuege";
	private static final String EKV_TITLE = "PdfGeneration_FinSit_Ekv_Title";
	private static final String MASSG_EINK_TITLE = "PdfGeneration_MassgEink_Title";

	private final Verfuegung verfuegungFuerMassgEinkommen;
	private final LocalDate erstesEinreichungsdatum;
	private final boolean hasSecondGesuchsteller;

	@Nonnull
	private FinanzielleSituation basisJahrGS1;
	@Nullable
	private FinanzielleSituation basisJahrGS2;
	@Nullable
	private Einkommensverschlechterung ekv1GS1;
	@Nullable
	private Einkommensverschlechterung ekv1GS2;
	@Nullable
	private Einkommensverschlechterung ekv2GS1;
	@Nullable
	private Einkommensverschlechterung ekv2GS2;

	public FinanzielleSituationPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum
	) {
		super(gesuch, stammdaten);
		this.verfuegungFuerMassgEinkommen = verfuegungFuerMassgEinkommen;
		this.erstesEinreichungsdatum = erstesEinreichungsdatum;

		// Der zweite GS wird gedruckt, wenn er Ende Gesuchsperiode zwingend war ODER es sich um eine Mutation handelt
		// und
		// der zweite GS bereits existiert.
		boolean hasSecondGsEndeGP = hasSecondGesuchsteller();
		boolean isMutationWithSecondGs = gesuch.isMutation() && gesuch.getGesuchsteller2() != null;
		this.hasSecondGesuchsteller = hasSecondGsEndeGP || isMutationWithSecondGs;
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return translate(TITLE);
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();

			initializeValues();

			// Basisjahr
			createPageBasisJahr(generator, document);
			// Eventuelle Einkommenverschlechterung
			EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
			if (ekvInfo != null) {
				if (ekvInfo.getEkvFuerBasisJahrPlus1()) {
					createPageEkv1(generator, document);
				}
				if (ekvInfo.getEkvFuerBasisJahrPlus2()) {
					createPageEkv2(generator, document);
				}
			}
			// Massgebendes Einkommen
			createPageMassgebendesEinkommen(document);
		};
	}

	private void initializeValues() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		basisJahrGS1 = gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();

		EinkommensverschlechterungContainer ekvContainerGS1 = gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		if (ekvContainerGS1 != null) {
			ekv1GS1 = ekvContainerGS1.getEkvJABasisJahrPlus1();
			ekv2GS1 = ekvContainerGS1.getEkvJABasisJahrPlus2();
		}

		basisJahrGS1.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittBasisjahr(basisJahrGS1));
		basisJahrGS2 = null;
		if (ekv1GS1 != null) {
			ekv1GS1.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittEkv(basisJahrGS1, ekv1GS1, ekv2GS1, 1));
		}
		if (ekv2GS1 != null) {
			ekv2GS1.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittEkv(basisJahrGS1, ekv1GS1, ekv2GS1, 2));
		}

		if (hasSecondGesuchsteller) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			Objects.requireNonNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
			EinkommensverschlechterungContainer ekvContainerGS2 = gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer();

			basisJahrGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA();
			Objects.requireNonNull(basisJahrGS2);
			if (ekvContainerGS2 != null) {
				ekv1GS2 = ekvContainerGS2.getEkvJABasisJahrPlus1();
				ekv2GS2 = ekvContainerGS2.getEkvJABasisJahrPlus2();
			}
			basisJahrGS2.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittBasisjahr(basisJahrGS2));
			if (ekv1GS2 != null) {
				ekv1GS2.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittEkv(basisJahrGS2, ekv1GS2, ekv2GS2, 1));
			}
			if (ekv2GS2 != null) {
				ekv2GS2.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittEkv(basisJahrGS2, ekv1GS2, ekv2GS2, 2));
			}
		}
	}

	private void createPageBasisJahr(
		@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator,
		@Nonnull Document document
	) {
		createFusszeile(generator.getDirectContent());
		requireNonNull(gesuch.getGesuchsteller1());
		requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		AbstractFinanzielleSituation basisJahrGS1Urspruenglich =
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationGS();

		AbstractFinanzielleSituation basisJahrGS2Urspruenglich = null;
		if (hasSecondGesuchsteller) {
			requireNonNull(gesuch.getGesuchsteller2());
			requireNonNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
			basisJahrGS2Urspruenglich =
				gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationGS();
		}

		document.add(createIntroBasisjahr());
		addTablesToDocument(document, basisJahrGS1, basisJahrGS2, basisJahrGS1Urspruenglich,
			basisJahrGS2Urspruenglich);
	}

	@Nullable
	private BigDecimal getGeschaeftsgewinnDurchschnittBasisjahr(@Nullable FinanzielleSituation finanzielleSituation) {
		if (finanzielleSituation == null) {
			return null;
		}
		BigDecimal durchschnitt = FinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(
			finanzielleSituation.getGeschaeftsgewinnBasisjahr(),
			finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1(),
			finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2());
		return MathUtil.roundToFrankenRappen(durchschnitt);
	}

	private void addTablesToDocument(
		@Nonnull Document document,
		@Nonnull AbstractFinanzielleSituation basisJahrGS1,
		@Nullable AbstractFinanzielleSituation basisJahrGS2,
		@Nullable AbstractFinanzielleSituation basisJahrGS1Urspruenglich,
		@Nullable AbstractFinanzielleSituation basisJahrGS2Urspruenglich
	) {
		document.add(createTableEinkommen(
			basisJahrGS1,
			basisJahrGS2,
			basisJahrGS1Urspruenglich,
			basisJahrGS2Urspruenglich));
		document.add(createTableVermoegen(
			basisJahrGS1,
			basisJahrGS2,
			basisJahrGS1Urspruenglich,
			basisJahrGS2Urspruenglich));
		document.add(createTableAbzuege(
			basisJahrGS1,
			basisJahrGS2,
			basisJahrGS1Urspruenglich,
			basisJahrGS2Urspruenglich));
		document.add(createTableZusammenzug(basisJahrGS1, basisJahrGS2));
	}

	private void createPageEkv1(
		@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator,
		@Nonnull Document document
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		EinkommensverschlechterungContainer ekvContainerGS1 =
			gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		requireNonNull(ekvContainerGS1);

		document.newPage();
		createFusszeile(generator.getDirectContent());
		document.add(PdfUtil.createBoldParagraph(
			translate(EKV_TITLE, String.valueOf(gesuch.getGesuchsperiode().getBasisJahrPlus1())),
			2)
		);
		document.add(createIntroEkv());

		Einkommensverschlechterung ekv1GS1Urspruenglich = ekvContainerGS1.getEkvGSBasisJahrPlus1();
		Einkommensverschlechterung ekv1GS2Urspruenglich = null;
		if (hasSecondGesuchsteller) {
			requireNonNull(gesuch.getGesuchsteller2());
			requireNonNull(gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer());
			ekv1GS2Urspruenglich =
				gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvGSBasisJahrPlus1();
		}

		Objects.requireNonNull(ekv1GS1);
		addTablesToDocument(document, ekv1GS1, ekv1GS2, ekv1GS1Urspruenglich, ekv1GS2Urspruenglich);
	}

	private void createPageEkv2(
		@Nonnull ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator generator,
		@Nonnull Document document
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		EinkommensverschlechterungContainer ekvContainerGS1 =
			gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		requireNonNull(ekvContainerGS1);

		document.newPage();
		createFusszeile(generator.getDirectContent());
		document.add(PdfUtil.createBoldParagraph(
			translate(EKV_TITLE, String.valueOf(gesuch.getGesuchsperiode().getBasisJahrPlus2())),
			2)
		);
		document.add(createIntroEkv());

		Einkommensverschlechterung ekv2GS1Urspruenglich = ekvContainerGS1.getEkvGSBasisJahrPlus2();
		Einkommensverschlechterung ekv2GS2Urspruenglich = null;
		if (hasSecondGesuchsteller) {
			requireNonNull(gesuch.getGesuchsteller2());
			requireNonNull(gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer());
			ekv2GS2Urspruenglich =
				gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvGSBasisJahrPlus2();
		}

		Objects.requireNonNull(ekv2GS1);
		addTablesToDocument(document, ekv2GS1, ekv2GS2, ekv2GS1Urspruenglich, ekv2GS2Urspruenglich);
	}

	private BigDecimal getGeschaeftsgewinnDurchschnittEkv(
		@Nullable FinanzielleSituation finSit,
		@Nullable Einkommensverschlechterung ekv1,
		@Nullable Einkommensverschlechterung ekv2,
		int basisJahrPlus
	) {
		EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
		BigDecimal durchschnitt = FinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finSit, ekv1, ekv2, ekvInfo, basisJahrPlus);
		return MathUtil.roundToFrankenRappen(durchschnitt);
	}

	private void createPageMassgebendesEinkommen(@Nonnull Document document) {
		List<String[]> values = new ArrayList<>();
		String[] titles = {
			translate(VON),
			translate(BIS),
			translate(JAHR),
			translate(MASSG_EINK_VOR_ABZUG),
			translate(FAM_GROESSE),
			translate(ABZUG_FAM_GROESSE),
			translate(MASSG_EINK) };
		values.add(titles);
		// Falls alle Abschnitte *nach* dem ersten Einreichungsdatum liegen, wird das ganze Dokument nicht gedruckt
		if (isAbschnittZuSpaetEingereicht(Iterables.getLast(verfuegungFuerMassgEinkommen.getZeitabschnitte()))) {
			return;
		}
		for (VerfuegungZeitabschnitt abschnitt : verfuegungFuerMassgEinkommen.getZeitabschnitte()) {
			// Wir drucken nur diejenigen Abschnitte, für die überhaupt ein Anspruch besteht
			if (isAbschnittZuSpaetEingereicht(abschnitt)) {
				continue;
			}
			String[] data = {
				Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigAb()),
				Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigBis()),
				String.valueOf(abschnitt.getEinkommensjahr()),
				PdfUtil.printBigDecimal(abschnitt.getMassgebendesEinkommenVorAbzFamgr()),
				PdfUtil.printBigDecimalOneNachkomma(abschnitt.getFamGroesse()),
				PdfUtil.printBigDecimal(abschnitt.getAbzugFamGroesse()),
				PdfUtil.printBigDecimal(abschnitt.getMassgebendesEinkommen())
			};
			values.add(data);
		}
		final float[] widthMassgebendesEinkommen = { 5, 5, 6, 10, 5, 10, 10 };
		final int[] alignmentMassgebendesEinkommen = {
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT
		};
		document.setPageSize(PageSize.A4.rotate());
		document.newPage();
		document.add(PdfUtil.createBoldParagraph(translate(MASSG_EINK_TITLE), 2));
		document.add(createIntroMassgebendesEinkommen());
		document.add(PdfUtil.createTable(values, widthMassgebendesEinkommen, alignmentMassgebendesEinkommen, 0));
	}

	private boolean isAbschnittZuSpaetEingereicht(VerfuegungZeitabschnitt abschnitt) {
		return !abschnitt.getGueltigkeit().getGueltigAb().isAfter(erstesEinreichungsdatum);
	}

	@Nonnull
	private PdfPTable createIntroBasisjahr() {
		List<TableRowLabelValue> introBasisjahr = new ArrayList<>();
		introBasisjahr.add(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introBasisjahr.add(new TableRowLabelValue(
			BASISJAHR,
			String.valueOf(gesuch.getGesuchsperiode().getBasisJahr())));
		return PdfUtil.creatreIntroTable(introBasisjahr, sprache);
	}

	@Nonnull
	private PdfPTable createIntroEkv() {
		List<TableRowLabelValue> introEkv1 = new ArrayList<>();
		introEkv1.add(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		return PdfUtil.creatreIntroTable(introEkv1, sprache);
	}

	@Nonnull
	private PdfPTable createIntroMassgebendesEinkommen() {
		List<TableRowLabelValue> introMassgEinkommen = new ArrayList<>();
		introMassgEinkommen.add(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introMassgEinkommen.add(new TableRowLabelValue(NAME, String.valueOf(gesuch.extractFullnamesString())));
		return PdfUtil.creatreIntroTable(introMassgEinkommen, sprache);
	}

	@Nonnull
	private PdfPTable createTableEinkommen(
		@Nonnull AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal totalEinkommenBeiderGS = FinanzielleSituationRechner.calcTotalEinkommen(gs1, gs2);

		FinanzielleSituationRow einkommenTitle = new FinanzielleSituationRow(
			translate(EIKOMMEN_TITLE), gesuch.getGesuchsteller1().extractFullName());
		einkommenTitle.setSupertext("1");

		FinanzielleSituationRow nettolohn = createRow(translate(NETTOLOHN),
			AbstractFinanzielleSituation::getNettolohn, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow familienzulagen = createRow(translate(FAMILIENZULAGEN),
			AbstractFinanzielleSituation::getFamilienzulage, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow ersatzeinkommen = createRow(translate(ERSATZEINKOMMEN),
			AbstractFinanzielleSituation::getErsatzeinkommen, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow unterhaltsbeitraege = createRow(translate(ERH_UNTERHALTSBEITRAEGE),
			AbstractFinanzielleSituation::getErhalteneAlimente, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow geschaftsgewinn = createRow(
			translate(GESCHAEFTSGEWINN),
			AbstractFinanzielleSituation::getDurchschnittlicherGeschaeftsgewinn,
			gs1,
			gs2,
			gs1Urspruenglich,
			gs2Urspruenglich);

		FinanzielleSituationRow zwischentotal = new FinanzielleSituationRow(
			translate(EINKOMMEN_ZWISCHENTOTAL), gs1.getZwischentotalEinkommen());
		FinanzielleSituationRow total = new FinanzielleSituationRow(
			translate(EINKOMMEN_TOTAL), "");

		if (gs2 != null) {
			requireNonNull(gesuch.getGesuchsteller2());
			einkommenTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			zwischentotal.setGs2(gs2.getZwischentotalEinkommen());
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalEinkommenBeiderGS);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalEinkommenBeiderGS);
		}
		FinanzielleSituationTable tableEinkommen =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				hasSecondGesuchsteller,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false);
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

	private FinanzielleSituationRow createRow(
		String message,
		Function<AbstractFinanzielleSituation, BigDecimal> getter,
		@Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich
	) {
		BigDecimal gs1BigDecimal = gs1 == null ? null : getter.apply(gs1);
		BigDecimal gs2BigDecimal = gs2 == null ? null : getter.apply(gs2);
		BigDecimal gs1UrspruenglichBigDecimal = gs1Urspruenglich == null ? null : getter.apply(gs1Urspruenglich);
		BigDecimal gs2UrspruenglichBigDecimal = gs2Urspruenglich == null ? null : getter.apply(gs2Urspruenglich);
		FinanzielleSituationRow row = new FinanzielleSituationRow(message, gs1BigDecimal);
		row.setGs2(gs2BigDecimal);
		if (!MathUtil.isSameWithNullAsZero(gs1BigDecimal, gs1UrspruenglichBigDecimal)) {
			row.setGs1Urspruenglich(gs1UrspruenglichBigDecimal, sprache);
		}
		if (!MathUtil.isSameWithNullAsZero(gs2BigDecimal, gs2UrspruenglichBigDecimal)) {
			row.setGs2Urspruenglich(gs2UrspruenglichBigDecimal, sprache);
		}
		return row;
	}

	@Nonnull
	private PdfPTable createTableVermoegen(
		@Nonnull AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal totalVermoegenBeiderGS = FinanzielleSituationRechner.calcTotalVermoegen(gs1, gs2);
		BigDecimal vermoegen5Prozent = FinanzielleSituationRechner.calcVermoegen5Prozent(gs1, gs2);

		FinanzielleSituationRow vermoegenTitle = new FinanzielleSituationRow(
			translate(NETTOVERMOEGEN), gesuch.getGesuchsteller1().extractFullName());
		vermoegenTitle.setSupertext("2");

		FinanzielleSituationRow bruttovermoegen = createRow(translate(BRUTTOVERMOEGEN),
			AbstractFinanzielleSituation::getBruttovermoegen, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow schulden = createRow(translate(SCHULDEN),
			AbstractFinanzielleSituation::getSchulden, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow zwischentotal = new FinanzielleSituationRow(
			translate(NETTOVERMOEGEN_ZWISCHENTOTAL), gs1.getZwischentotalVermoegen());

		FinanzielleSituationRow total = new FinanzielleSituationRow(
			translate(NETTOVERMOEGEN_TOTAL), "");

		FinanzielleSituationRow vermoegen5Percent = new FinanzielleSituationRow(
			translate(NETTOVERMOEGEN_5_PROZENT), "");

		if (gs2 != null) {
			requireNonNull(gesuch.getGesuchsteller2());

			vermoegenTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			zwischentotal.setGs2(gs2.getZwischentotalVermoegen());
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalVermoegenBeiderGS);
			vermoegen5Percent.setGs2(vermoegen5Prozent);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalVermoegenBeiderGS);
			vermoegen5Percent.setGs1(vermoegen5Prozent);
		}

		FinanzielleSituationTable table =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				hasSecondGesuchsteller,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false);
		table.addRow(vermoegenTitle);
		table.addRow(bruttovermoegen);
		table.addRow(schulden);
		table.addRow(zwischentotal);
		table.addRow(total);
		table.addRow(vermoegen5Percent);
		return table.createTable();
	}

	@Nonnull
	private PdfPTable createTableAbzuege(
		@Nonnull AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal totalAbzuegeBeiderGS = FinanzielleSituationRechner.calcTotalAbzuege(gs1, gs2);

		FinanzielleSituationRow abzuegeTitle = new FinanzielleSituationRow(
			translate(ABZUEGE), gesuch.getGesuchsteller1().extractFullName());
		abzuegeTitle.setSupertext("3");

		FinanzielleSituationRow unterhaltsbeitraege = createRow(translate(UNTERHALTSBEITRAEGE_BEZAHLT),
			AbstractFinanzielleSituation::getGeleisteteAlimente, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow total = new FinanzielleSituationRow(
			translate(ABZUEGE_TOTAL), "");

		if (gs2 != null) {
			requireNonNull(gesuch.getGesuchsteller2());
			abzuegeTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalAbzuegeBeiderGS);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalAbzuegeBeiderGS);
		}
		FinanzielleSituationTable table =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				hasSecondGesuchsteller,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false);
		table.addRow(abzuegeTitle);
		table.addRow(unterhaltsbeitraege);
		table.addRow(total);
		return table.createTable();
	}

	@Nonnull
	private PdfPTable createTableZusammenzug(
		@Nonnull AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal massgebendesEinkommenVorAbzugFamiliengroesse =
			FinanzielleSituationRechner.calcMassgebendesEinkommenVorAbzugFamiliengroesse(gs1, gs2);

		FinanzielleSituationRow zusammenzugTitle = new FinanzielleSituationRow(
			translate(ZUSAMMENZUG), "");
		FinanzielleSituationRow einkommen = new FinanzielleSituationRow(
			translate(EINKOMMEN_TOTAL), gs1.getZwischentotalEinkommen());
		FinanzielleSituationRow vermoegen = new FinanzielleSituationRow(
			translate(NETTOVERMOEGEN_5_PROZENT), FinanzielleSituationRechner.calcVermoegen5Prozent(gs1, gs2));
		FinanzielleSituationRow abzuege = new FinanzielleSituationRow(
			translate(ABZUEGE_TOTAL), gs1.getZwischetotalAbzuege());
		FinanzielleSituationRow total = new FinanzielleSituationRow(
			translate(MASSG_EINKOMMEN_VOR_FAMILIENGROESSE), massgebendesEinkommenVorAbzugFamiliengroesse);

		if (gs2 != null) {
			requireNonNull(gesuch.getGesuchsteller2());
			einkommen.setGs1(MathUtil.DEFAULT.add(gs1.getZwischentotalEinkommen(), gs2.getZwischentotalEinkommen()));
			abzuege.setGs1(MathUtil.DEFAULT.add(gs1.getZwischetotalAbzuege(), gs2.getZwischetotalAbzuege()));
		}
		FinanzielleSituationTable table = new FinanzielleSituationTable(getPageConfiguration(), false, false, true);
		table.addRow(zusammenzugTitle);
		table.addRow(einkommen);
		table.addRow(vermoegen);
		table.addRow(abzuege);
		table.addRow(total);
		return table.createTable();
	}

	private void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(dirPdfContentByte, Lists.newArrayList(
			translate(FUSSZEILE_EINKOMMEN),
			translate(FUSSZEILE_VERMOEGEN),
			translate(FUSSZEILE_ABZUEGE)));
	}
}
