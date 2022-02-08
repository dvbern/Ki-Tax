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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;

import static java.util.Objects.requireNonNull;

public class FinanzielleSituationPdfGeneratorSolothurn extends FinanzielleSituationPdfGenerator {

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
	@Nonnull
	private AbstractFinanzielleSituationRechner finanzielleSituationRechner;

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
	}

	protected void createPageBasisJahr(
		@Nonnull PdfGenerator generator,
		@Nonnull Document document
	) {
		createFusszeile(generator.getDirectContent());
		document.add(createIntroBasisjahr());
		// TODO: continue
	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		// TODO: implement
	}

	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		// TODO: implement
	}


	protected void createPageMassgebendesEinkommen(@Nonnull Document document) {
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

	@Nonnull
	private PdfPTable createTableEinkommen(
		@Nonnull AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal totalEinkommenBeiderGS = finanzielleSituationRechner.calcTotalEinkommen(gs1, gs2);

		FinanzielleSituationRow einkommenTitle = new FinanzielleSituationRow(
			translate(EIKOMMEN_TITLE, mandant), gesuch.getGesuchsteller1().extractFullName());
		einkommenTitle.setSupertext("1");

		FinanzielleSituationRow nettolohn = createRow(translate(NETTOLOHN, mandant),
			AbstractFinanzielleSituation::getNettolohn, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow familienzulagen = createRow(translate(FAMILIENZULAGEN, mandant),
			AbstractFinanzielleSituation::getFamilienzulage, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow ersatzeinkommen = createRow(translate(ERSATZEINKOMMEN, mandant),
			AbstractFinanzielleSituation::getErsatzeinkommen, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow unterhaltsbeitraege = createRow(translate(ERH_UNTERHALTSBEITRAEGE, mandant),
			AbstractFinanzielleSituation::getErhalteneAlimente, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow geschaftsgewinn = createRow(
			translate(GESCHAEFTSGEWINN, mandant),
			AbstractFinanzielleSituation::getDurchschnittlicherGeschaeftsgewinn,
			gs1,
			gs2,
			gs1Urspruenglich,
			gs2Urspruenglich);

		FinanzielleSituationRow zwischentotal = new FinanzielleSituationRow(
			translate(EINKOMMEN_ZWISCHENTOTAL, mandant), gs1.getZwischentotalEinkommen());
		FinanzielleSituationRow total = new FinanzielleSituationRow(
			translate(EINKOMMEN_TOTAL, mandant), "");

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

	private void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		createFusszeile(dirPdfContentByte, Lists.newArrayList(
			translate(FUSSZEILE_EINKOMMEN),
			translate(FUSSZEILE_VERMOEGEN),
			translate(FUSSZEILE_ABZUEGE)));
	}
}
