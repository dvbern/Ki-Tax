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
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.pdfgenerator.DokumentAnFamilieGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.pdfgenerator.TableRowLabelValue;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.google.common.collect.Iterables;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;

public abstract class FinanzielleSituationPdfGenerator extends DokumentAnFamilieGenerator {

	private static final String TITLE = "PdfGeneration_FinSit_Title";
	private static final String NAME = "PdfGeneration_FinSit_Name";
	private static final String BASISJAHR = "PdfGeneration_FinSit_BasisJahr";
	protected static final String VON = "PdfGeneration_MassgEinkommen_Von";
	protected static final String BIS = "PdfGeneration_MassgEinkommen_Bis";
	protected static final String JAHR = "PdfGeneration_MassgEinkommen_Jahr";
	protected static final String MASSG_EINK = "PdfGeneration_MassgEinkommen_MassgEink";
	protected static final String MASSG_EINK_TITLE = "PdfGeneration_MassgEink_Title";
	protected static final String TOTAL_MASSG_EINK = "PdfGeneration_MassgEinkommen_MassgEinkTotal";
	private static final String ZUSAMMENZUG = "PdfGeneration_FinSit_Zusammenzug";
	protected static final String EKV_TITLE = "PdfGeneration_FinSit_Ekv_Title";
	protected static final String NETTOVERMOEGEN = "PdfGeneration_FinSit_Nettovermoegen";

	protected final Verfuegung verfuegungFuerMassgEinkommen;
	protected final LocalDate erstesEinreichungsdatum;
	protected boolean hasSecondGesuchsteller;
	@Nullable
	protected FinanzielleSituationResultateDTO finanzDatenDTO;
	@Nullable
	protected FinanzielleSituationResultateDTO ekvBasisJahrPlus1;
	@Nullable
	protected FinanzielleSituationResultateDTO ekvBasisJahrPlus2;
	@Nonnull
	protected AbstractFinanzielleSituationRechner finanzielleSituationRechner;

	protected FinanzielleSituationPdfGenerator(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum,
		@Nonnull AbstractFinanzielleSituationRechner finanzielleSituationRechner
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
		this.finanzielleSituationRechner = finanzielleSituationRechner;
	}

	@Nonnull
	@Override
	protected final CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();

			initializeValues();

			// Basisjahr
			createPageBasisJahr(generator, document);
			// Eventuelle Einkommenverschlechterung
			createPagesEkv(generator, document);
			// Massgebendes Einkommen
			createPageMassgebendesEinkommen(document);
		};
	}

	protected abstract void initializeValues();
	protected abstract void createPageBasisJahr(@Nonnull PdfGenerator generator, @Nonnull Document document);
	protected abstract void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document);
	protected abstract void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document);

	protected void initialzeEkv() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();

		if (ekvInfo != null) {
			if (ekvInfo.getEkvFuerBasisJahrPlus1()) {
				ekvBasisJahrPlus1 = finanzielleSituationRechner.calculateResultateEinkommensverschlechterung(gesuch,1, hasSecondGesuchsteller);
			}
			if (ekvInfo.getEkvFuerBasisJahrPlus2()) {
				ekvBasisJahrPlus2 = finanzielleSituationRechner.calculateResultateEinkommensverschlechterung(gesuch, 2, hasSecondGesuchsteller);
			}
		}
	}

	protected void createPageMassgebendesEinkommen(@Nonnull Document document) {
		List<String[]> values = new ArrayList<>();
		String[] titles = {
			translate(VON),
			translate(BIS),
			translate(JAHR),
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
				PdfUtil.printBigDecimal(abschnitt.getMassgebendesEinkommen())
			};
			values.add(data);
		}
		final float[] widthMassgebendesEinkommen = { 5, 5, 6, 10 };
		final int[] alignmentMassgebendesEinkommen = {
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT,
			Element.ALIGN_RIGHT
		};
//		document.setPageSize(PageSize.A4.rotate());
		document.newPage();
		document.add(PdfUtil.createBoldParagraph(translate(MASSG_EINK_TITLE), 2));
		document.add(createIntroMassgebendesEinkommen());
		document.add(PdfUtil.createTable(values, widthMassgebendesEinkommen, alignmentMassgebendesEinkommen, 0));
	};

	@Nonnull
	@Override
	protected final String getDocumentTitle() {
		return translate(TITLE);
	}

	@Nonnull
	protected final PdfPTable createIntroMassgebendesEinkommen() {
		List<TableRowLabelValue> introMassgEinkommen = new ArrayList<>();
		introMassgEinkommen.add(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introMassgEinkommen.add(new TableRowLabelValue(NAME, String.valueOf(gesuch.extractFullnamesString())));
		return PdfUtil.createIntroTable(introMassgEinkommen, sprache, mandant);
	}

	@Nonnull
	protected final PdfPTable createIntroBasisjahr() {
		List<TableRowLabelValue> introBasisjahr = new ArrayList<>();
		introBasisjahr.add(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		introBasisjahr.add(new TableRowLabelValue(
			BASISJAHR,
			String.valueOf(gesuch.getGesuchsperiode().getBasisJahr())));
		return PdfUtil.createIntroTable(introBasisjahr, sprache, mandant);
	}

	protected final boolean isAbschnittZuSpaetEingereicht(VerfuegungZeitabschnitt abschnitt) {
		return !abschnitt.getGueltigkeit().getGueltigAb().isAfter(erstesEinreichungsdatum);
	}

	protected void createTablezusammenzug(
		@Nonnull Document document,
		@Nonnull String gs1Name,
		@Nonnull String gs2Name
	) {
		Objects.requireNonNull(finanzDatenDTO);

		FinanzielleSituationTable zusammenzugTable =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				true);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(ZUSAMMENZUG, mandant), "");

		FinanzielleSituationRow massgebendesEinkommenGS1 = new FinanzielleSituationRow(
			translate(MASSG_EINK_TITLE, mandant) + " " + gs1Name,
			finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS1()
		);

		FinanzielleSituationRow massgebendesEinkommenGS2 = new FinanzielleSituationRow(
			translate(MASSG_EINK_TITLE, mandant) + " " + gs2Name,
			finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS2()
		);

		FinanzielleSituationRow zusammenzug = new FinanzielleSituationRow(
			translate(MASSG_EINK, mandant),
			finanzDatenDTO.getMassgebendesEinkVorAbzFamGr()
		);

		zusammenzugTable.addRow(title);
		zusammenzugTable.addRow(massgebendesEinkommenGS1);
		zusammenzugTable.addRow(massgebendesEinkommenGS2);
		zusammenzugTable.addRow(zusammenzug);

		document.add(zusammenzugTable.createTable());
	}

	@Nonnull
	protected PdfPTable createIntroEkv() {
		List<TableRowLabelValue> introEkv1 = new ArrayList<>();
		introEkv1.add(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));
		return PdfUtil.createIntroTable(introEkv1, sprache, mandant);
	}

	private void createPagesEkv(PdfGenerator generator, Document document) {
		EinkommensverschlechterungInfo ekvInfo = gesuch.extractEinkommensverschlechterungInfo();
		if (ekvInfo != null) {
			if (ekvInfo.getEkvFuerBasisJahrPlus1()) {
				createPageEkv1(generator, document);
			}
			if (ekvInfo.getEkvFuerBasisJahrPlus2()) {
				createPageEkv2(generator, document);
			}
		}
	}

	protected FinanzielleSituationRow createTableTitleForEkv(int basisJahr) {
	Objects.requireNonNull(gesuch.getGesuchsteller1());
	String gs1Name = gesuch.getGesuchsteller1().extractFullName();

	FinanzielleSituationRow row =  new FinanzielleSituationRow(
		translate(EKV_TITLE, String.valueOf(basisJahr)),
		gs1Name);

	if (hasSecondGesuchsteller) {
		Objects.requireNonNull(gesuch.getGesuchsteller2());
		String gs2Name = gesuch.getGesuchsteller2().extractFullName();
		row.setGs2(gs2Name);
	}

	return row;
}

	protected Element createTitleEkv(@Nullable Integer basisJahr) {
		String basisJahrString = basisJahr != null ? String.valueOf(basisJahr) : "";
	return PdfUtil.createBoldParagraph(
		translate(EKV_TITLE, basisJahrString),
		2);
}

	protected FinanzielleSituationTable createFinSitTable(boolean hasSecondGS) {
		return new FinanzielleSituationTable(
			getPageConfiguration(),
			hasSecondGS,
			EbeguUtil.isKorrekturmodusGemeinde(gesuch),
			true);
	}

	protected FinanzielleSituationRow createRow(
		String message,
		@Nullable BigDecimal value1,
		boolean hasSecondGS,
		@Nullable BigDecimal value2) {

		FinanzielleSituationRow row = new FinanzielleSituationRow(
			translate(message, mandant), value1);

		if(hasSecondGS) {
			row.setGs2(value2);
		}

		return row;
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
}
