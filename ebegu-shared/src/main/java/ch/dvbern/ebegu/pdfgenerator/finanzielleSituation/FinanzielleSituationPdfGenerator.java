/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationRechnerFactory;
import ch.dvbern.ebegu.finanziellesituation.AbstractFinanzielleSituationContainer;
import ch.dvbern.ebegu.pdfgenerator.DokumentAnFamilieGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.pdfgenerator.TableRowLabelValue;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Floats;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.ebegu.pdfgenerator.finanzielleSituation.MassgebendesEinkommenColumn.column;
import static java.util.Objects.requireNonNull;

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
		this.finanzielleSituationRechner = FinanzielleSituationRechnerFactory.getRechner(gesuch);
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
		requireNonNull(gesuch.getGesuchsteller1());
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
		MassgebendesEinkommenTabelleConfig config = getMassgebendesEinkommenConfig();

		String[] titles = config.getColumns().stream()
			.map(MassgebendesEinkommenColumn::getTitle)
			.toArray(String[]::new);

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
			String[] data = config.getColumns().stream()
				.map(c -> c.getDataMapper().apply(abschnitt))
				.toArray(String[]::new);
			values.add(data);
		}

		document.setPageSize(config.getPageSize());
		document.newPage();
		document.add(PdfUtil.createBoldParagraph(translate(MASSG_EINK_TITLE), 2));
		document.add(createIntroMassgebendesEinkommen());

		List<Float> columnWidths = config.getColumns().stream()
			.map(MassgebendesEinkommenColumn::getWidth)
			.collect(Collectors.toUnmodifiableList());

		int[] alignment = config.getColumns().stream()
			.mapToInt(MassgebendesEinkommenColumn::getAlignment)
			.toArray();

		document.add(PdfUtil.createTable(values, Floats.toArray(columnWidths), alignment, 0));
	};

	protected MassgebendesEinkommenTabelleConfig getMassgebendesEinkommenConfig() {
		return MassgebendesEinkommenTabelleConfig.of(
			PageSize.A4,
			column(5, translate(VON), a -> Constants.DATE_FORMATTER.format(a.getGueltigkeit().getGueltigAb())),
			column(5, translate(BIS), a -> Constants.DATE_FORMATTER.format(a.getGueltigkeit().getGueltigBis())),
			column(6, translate(JAHR), a -> printJahr(a.getEinkommensjahr())),
			column(10, translate(MASSG_EINK), a -> printCHF(a.getMassgebendesEinkommen()))
		);
	}

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
	protected PdfPTable createIntroBasisjahr() {
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

	protected void addTablezusammenzug(@Nonnull Document document) {
		document.add(createTableZusammenzug(requireNonNull(finanzDatenDTO)));
	}

	protected PdfPTable createTableZusammenzug(
		@Nonnull FinanzielleSituationResultateDTO dto
	) {
		String gs1Name = requireNonNull(gesuch.getGesuchsteller1()).extractFullName();
		String gs2Name = requireNonNull(gesuch.getGesuchsteller2()).extractFullName();

		return createFinSitTableSingleGS(
			createRow(translate(ZUSAMMENZUG)),
			createRow(translate(MASSG_EINK_TITLE) + ' ' + gs1Name, printCHF(dto.getMassgebendesEinkVorAbzFamGrGS1())),
			createRow(translate(MASSG_EINK_TITLE) + ' ' + gs2Name, printCHF(dto.getMassgebendesEinkVorAbzFamGrGS2())),
			createRow(translate(MASSG_EINK), printCHF(dto.getMassgebendesEinkVorAbzFamGr()))
				.bold()
		);
	}

	@Nonnull
	protected PdfPTable createIntroEkv() {
		var introEkv1 = List.of(new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()));

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
		String gs1Name = requireNonNull(gesuch.getGesuchsteller1()).extractFullName();

		FinanzielleSituationRow row = createRow(translate(EKV_TITLE, printJahr(basisJahr)), gs1Name);

		if (hasSecondGesuchsteller) {
			row.setGs2(requireNonNull(gesuch.getGesuchsteller2()).extractFullName());
		}

		return row;
	}

	protected Element createTitleEkv(@Nullable Integer basisJahr) {
		String basisJahrString = basisJahr == null ? "" : printJahr(basisJahr);

		return PdfUtil.createBoldParagraph(translate(EKV_TITLE, basisJahrString), 2);
	}

	protected FinanzielleSituationTable createFinSitTable() {
		return createFinSitTable(hasSecondGesuchsteller);
	}

	protected FinanzielleSituationTable createFinSitTable(boolean hasSecondGS) {
		return new FinanzielleSituationTable(
			getPageConfiguration(),
			hasSecondGS,
			EbeguUtil.isKorrekturmodusGemeinde(gesuch));
	}

	protected PdfPTable createFinSitTableSingleGS(@Nonnull FinanzielleSituationRow... rows) {
		return createFinSitTable(false)
			.addRows(rows)
			.createTable();
	}

	protected FinanzielleSituationRow createRow(
		String message,
		@Nullable BigDecimal value1,
		boolean hasSecondGS,
		@Nullable BigDecimal value2) {

		FinanzielleSituationRow row = new FinanzielleSituationRow(translate(message), value1);

		if (hasSecondGS) {
			row.setGs2(value2);
		}

		return row;
	}

	protected FinanzielleSituationRow createRow(String message, @Nullable BigDecimal value1, @Nullable BigDecimal value2) {
		return createRow(message, value1, hasSecondGesuchsteller, value2);
	}

	protected final <T extends AbstractFinanzielleSituation> FinanzielleSituationRow createRow(
		String message,
		Function<T, BigDecimal> getter,
		@Nullable T gs1,
		@Nullable T gs1Urspruenglich
	) {
		BigDecimal gs1BigDecimal = gs1 == null ? null : getter.apply(gs1);
		BigDecimal gs1UrspruenglichBigDecimal = gs1Urspruenglich == null ? null : getter.apply(gs1Urspruenglich);
		FinanzielleSituationRow row = new FinanzielleSituationRow(message, gs1BigDecimal);
		if (!MathUtil.isSameWithNullAsZero(gs1BigDecimal, gs1UrspruenglichBigDecimal)) {
			row.setGs1Urspruenglich(gs1UrspruenglichBigDecimal, sprache, mandant);
		}
		return row;
	}

	protected final <T extends AbstractFinanzielleSituation, S extends AbstractFinanzielleSituationContainer<T>>
	FinanzielleSituationRow createRow(String message, Function<T, BigDecimal> getter, S finanzielleSituation) {
		return createRow(message, getter, finanzielleSituation.getFinSitJA(), finanzielleSituation.getFinSitGS());
	}

	protected final FinanzielleSituationRow createRow(String message) {
		return createRow(message, "");
	}

	protected final FinanzielleSituationRow createRow(String message, String gs1) {
		return new FinanzielleSituationRow(message, gs1);
	}

	protected final String printJahr(Integer jahr) {
		return String.valueOf(jahr);
	}

	protected final String printCHF(@Nullable BigDecimal value) {
		return PdfUtil.printBigDecimal(value);
	}

	protected final String printAnzahl(@Nullable BigDecimal anzahl) {
		return PdfUtil.printBigDecimalOneNachkomma(anzahl);
	}

	protected String bothNames() {
		return String.join(
			" \n& ",
			requireNonNull(gesuch.getGesuchsteller1()).extractFullName(),
			requireNonNull(gesuch.getGesuchsteller2()).extractFullName()
		);
	}
}
