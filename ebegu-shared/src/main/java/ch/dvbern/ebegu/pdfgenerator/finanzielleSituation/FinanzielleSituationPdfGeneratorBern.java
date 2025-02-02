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
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.ebegu.pdfgenerator.finanzielleSituation.MassgebendesEinkommenColumn.column;
import static java.util.Objects.requireNonNull;

public class FinanzielleSituationPdfGeneratorBern extends FinanzielleSituationPdfGenerator {

	private static final String EIKOMMEN_TITLE = "PdfGeneration_FinSit_EinkommenTitle";
	private static final String NETTOLOHN = "PdfGeneration_FinSit_Nettolohn";
	private static final String FAMILIENZULAGEN = "PdfGeneration_FinSit_Familienzulagen";
	private static final String ERSATZEINKOMMEN = "PdfGeneration_FinSit_Ersatzeinkommen";
	private static final String ERH_UNTERHALTSBEITRAEGE = "PdfGeneration_FinSit_ErhalteneUnterhaltsbeitraege";
	private static final String GESCHAEFTSGEWINN = "PdfGeneration_FinSit_Geschaeftsgewinn";
	private static final String BRUTTOERTRAEGE_VERMOEGEN = "PdfGeneration_FinSit_BruttoertraegeVermoegen";
	private static final String NETTOERTRAEGE_ERBENGEMEINSCHAFT =
		"PdfGeneration_FinSit_NettoertraegeErbengemeinschaft";
	private static final String AMOUNT_EINKOMMEN_IN_VEREINFACHTEM_VERFAHREN_ABGERECHNET =
		"PdfGeneration_FinSit_AmountEinkommenInVereinfachtemVerfahrenAbgerechnet";
	private static final String EINKOMMEN_ZWISCHENTOTAL = "PdfGeneration_FinSit_EinkommenZwischentotal";
	private static final String EINKOMMEN_TOTAL = "PdfGeneration_FinSit_EinkommenTotal";
	private static final String BRUTTOVERMOEGEN = "PdfGeneration_FinSit_Bruttovermoegen";
	private static final String SCHULDEN = "PdfGeneration_FinSit_Schulden";
	private static final String NETTOVERMOEGEN_ZWISCHENTOTAL = "PdfGeneration_FinSit_Nettovermoegen_Zwischentotal";
	private static final String NETTOVERMOEGEN_TOTAL = "PdfGeneration_FinSit_Nettovermoegen_Total";
	private static final String NETTOVERMOEGEN_5_PROZENT = "PdfGeneration_FinSit_Nettovermoegen_5_Prozent";
	private static final String ABZUEGE = "PdfGeneration_FinSit_Abzuege";
	private static final String UNTERHALTSBEITRAEGE_BEZAHLT = "PdfGeneration_FinSit_UnterhaltsbeitraegeBezahlt";
	private static final String SCHULDZINSEN = "PdfGeneration_FinSit_Schuldzinsen";
	private static final String GEWINNUNGSKOSTEN = "PdfGeneration_FinSit_Gewinnungskosten";
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

	private final FinanzielleSituationTyp finSitTyp;

	@Nonnull
	protected FinanzielleSituation basisJahrGS1;
	@Nullable
	protected FinanzielleSituation basisJahrGS2;
	@Nullable
	protected Einkommensverschlechterung ekv1GS1;
	@Nullable
	protected Einkommensverschlechterung ekv1GS2;
	@Nullable
	protected Einkommensverschlechterung ekv2GS1;
	@Nullable
	protected Einkommensverschlechterung ekv2GS2;

	public FinanzielleSituationPdfGeneratorBern(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum
	) {
		super(gesuch, verfuegungFuerMassgEinkommen, stammdaten, erstesEinreichungsdatum);
		this.finSitTyp = gesuch.getFinSitTyp();
	}

	@Override
	protected void initializeValues() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		basisJahrGS1 = gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();

		EinkommensverschlechterungContainer ekvContainerGS1 =
			gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		if (ekvContainerGS1 != null) {
			ekv1GS1 = ekvContainerGS1.getEkvJABasisJahrPlus1();
			ekv2GS1 = ekvContainerGS1.getEkvJABasisJahrPlus2();
		}

		basisJahrGS1.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittBasisjahr(basisJahrGS1));
		basisJahrGS2 = null;
		if (ekv1GS1 != null) {
			ekv1GS1.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittEkv(
				basisJahrGS1,
				ekv1GS1,
				ekv2GS1,
				1));
		}
		if (ekv2GS1 != null) {
			ekv2GS1.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittEkv(
				basisJahrGS1,
				ekv1GS1,
				ekv2GS1,
				2));
		}

		if (hasSecondGesuchsteller) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			Objects.requireNonNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
			EinkommensverschlechterungContainer ekvContainerGS2 =
				gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer();

			basisJahrGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA();
			Objects.requireNonNull(basisJahrGS2);
			if (ekvContainerGS2 != null) {
				ekv1GS2 = ekvContainerGS2.getEkvJABasisJahrPlus1();
				ekv2GS2 = ekvContainerGS2.getEkvJABasisJahrPlus2();
			}
			basisJahrGS2.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittBasisjahr(basisJahrGS2));
			if (ekv1GS2 != null) {
				ekv1GS2.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittEkv(
					basisJahrGS2,
					ekv1GS2,
					ekv2GS2,
					1));
			}
			if (ekv2GS2 != null) {
				ekv2GS2.setDurchschnittlicherGeschaeftsgewinn(getGeschaeftsgewinnDurchschnittEkv(
					basisJahrGS2,
					ekv1GS2,
					ekv2GS2,
					2));
			}
		}
	}

	@Override
	protected void createPageBasisJahr(
		@Nonnull PdfGenerator generator,
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
		BigDecimal durchschnitt = AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finanzielleSituation);
		return MathUtil.roundToFrankenRappen(durchschnitt);
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod") // FalsePositive: Die Methode ist benutzt
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

	@Override
	protected void createPageEkv1(
		@Nonnull PdfGenerator generator,
		@Nonnull Document document
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		EinkommensverschlechterungContainer ekvContainerGS1 =
			gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		requireNonNull(ekvContainerGS1);

		document.newPage();
		createFusszeile(generator.getDirectContent());
		document.add(createTitleEkv(gesuch.getGesuchsperiode().getBasisJahrPlus1()));
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

	@Override
	protected void createPageEkv2(
		@Nonnull PdfGenerator generator,
		@Nonnull Document document
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		EinkommensverschlechterungContainer ekvContainerGS1 =
			gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer();
		requireNonNull(ekvContainerGS1);

		document.newPage();
		createFusszeile(generator.getDirectContent());
		document.add(createTitleEkv(gesuch.getGesuchsperiode().getBasisJahrPlus2()));
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
		BigDecimal durchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finSit, ekv1, ekv2, ekvInfo, basisJahrPlus);
		return MathUtil.roundToFrankenRappen(durchschnitt);
	}

	@Override
	protected MassgebendesEinkommenTabelleConfig getMassgebendesEinkommenConfig() {
		return MassgebendesEinkommenTabelleConfig.of(
			PageSize.A4.rotate(),
			column(5, translate(VON), a -> Constants.DATE_FORMATTER.format(a.getGueltigkeit().getGueltigAb())),
			column(5, translate(BIS), a -> Constants.DATE_FORMATTER.format(a.getGueltigkeit().getGueltigBis())),
			column(6, translate(JAHR), a -> printJahr(a.getEinkommensjahr())),
			column(10, translate(MASSG_EINK_VOR_ABZUG), a -> printCHF(a.getMassgebendesEinkommenVorAbzFamgr())),
			column(5, translate(FAM_GROESSE), a -> printAnzahl(a.getFamGroesse())),
			column(10, translate(ABZUG_FAM_GROESSE), a -> printCHF(a.getAbzugFamGroesse())),
			column(10, translate(MASSG_EINK), a -> printCHF(a.getMassgebendesEinkommen()))
		);
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

		FinanzielleSituationRow einkommenTitle =
			createRow(translate(EIKOMMEN_TITLE), gesuch.getGesuchsteller1().extractFullName());
		einkommenTitle.setSupertext("1");

		FinanzielleSituationRow nettolohn = createRow(translate(NETTOLOHN, mandant),
			AbstractFinanzielleSituation::getNettolohn, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow familienzulagen = createRow(translate(FAMILIENZULAGEN, mandant),
			AbstractFinanzielleSituation::getFamilienzulage, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow ersatzeinkommen = createRowErsatzeinkommen(gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow unterhaltsbeitraege = createRow(translate(ERH_UNTERHALTSBEITRAEGE, mandant),
			AbstractFinanzielleSituation::getErhalteneAlimente, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow geschaftsgewinn = createRow(
			translate(GESCHAEFTSGEWINN, mandant),
			AbstractFinanzielleSituation::getDurchschnittlicherGeschaeftsgewinn,
			gs1,
			gs2,
			gs1Urspruenglich,
			gs2Urspruenglich);

		FinanzielleSituationRow zwischentotal =
			createRow(translate(EINKOMMEN_ZWISCHENTOTAL), printCHF(finanzielleSituationRechner.getZwischentotalEinkommen(gs1)));
		FinanzielleSituationRow total = createRow(translate(EINKOMMEN_TOTAL));

		if (gs2 != null) {
			requireNonNull(gesuch.getGesuchsteller2());
			einkommenTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			zwischentotal.setGs2(finanzielleSituationRechner.getZwischentotalEinkommen(gs2));
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalEinkommenBeiderGS);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalEinkommenBeiderGS);
		}
		FinanzielleSituationTable tableEinkommen = createFinSitTable().addRows(
			einkommenTitle,
			nettolohn,
			familienzulagen,
			ersatzeinkommen,
			unterhaltsbeitraege,
			geschaftsgewinn
		);

		if (finSitTyp == FinanzielleSituationTyp.BERN_FKJV) {
			createEinkommenFKJVRow(gs1, gs2, gs1Urspruenglich, gs2Urspruenglich).forEach(tableEinkommen::addRow);
		}

		return tableEinkommen.addRows(zwischentotal, total)
			.createTable();
	}

	private FinanzielleSituationRow createRowErsatzeinkommen(
		@Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich) {
		BigDecimal gs1ErsatzEinkommen = finanzielleSituationRechner.calcErsatzeinkommen(gs1);
		BigDecimal gs2ErsatzEinkommen = finanzielleSituationRechner.calcErsatzeinkommen(gs2);
		BigDecimal gs1UrsprunglichErsatzEinkommen = finanzielleSituationRechner.calcErsatzeinkommen(gs1Urspruenglich);
		BigDecimal gs2UrsprunglichErsatzEinkommen = finanzielleSituationRechner.calcErsatzeinkommen(gs2Urspruenglich);
		return createRow(translate(ERSATZEINKOMMEN, mandant), gs1ErsatzEinkommen, gs2ErsatzEinkommen, gs1UrsprunglichErsatzEinkommen, gs2UrsprunglichErsatzEinkommen);

	}

	private List<FinanzielleSituationRow> createEinkommenFKJVRow(
		@Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich
	) {
		FinanzielleSituationRow bruttoertraegeVermoegen = createRow(
			translate(BRUTTOERTRAEGE_VERMOEGEN),
			AbstractFinanzielleSituation::getBruttoertraegeVermoegen,
			gs1,
			gs2,
			gs1Urspruenglich,
			gs2Urspruenglich);

		FinanzielleSituationRow nettoertraegeErbengemeinschaft = createRow(
			translate(NETTOERTRAEGE_ERBENGEMEINSCHAFT),
			AbstractFinanzielleSituation::getNettoertraegeErbengemeinschaft,
			gs1,
			gs2,
			gs1Urspruenglich,
			gs2Urspruenglich);

		FinanzielleSituationRow amountEinkommenInVereinfachtemVerfahrenAbgerechnet = createRow(
			translate(AMOUNT_EINKOMMEN_IN_VEREINFACHTEM_VERFAHREN_ABGERECHNET),
			finanzielleSituation -> Boolean.TRUE.equals(finanzielleSituation.getEinkommenInVereinfachtemVerfahrenAbgerechnet())
				? finanzielleSituation.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet()
				: BigDecimal.ZERO,
			gs1,
			gs2,
			gs1Urspruenglich,
			gs2Urspruenglich);

		return List.of(
			bruttoertraegeVermoegen,
			nettoertraegeErbengemeinschaft,
			amountEinkommenInVereinfachtemVerfahrenAbgerechnet
		);
	}

	@Nonnull
	private PdfPTable createTableVermoegen(
		@Nonnull AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal totalVermoegenBeiderGS = finanzielleSituationRechner.calcTotalVermoegen(gs1, gs2);
		BigDecimal vermoegen5Prozent = AbstractFinanzielleSituationRechner.calcVermoegen5Prozent(gs1, gs2);

		FinanzielleSituationRow vermoegenTitle =
			createRow(translate(NETTOVERMOEGEN), gesuch.getGesuchsteller1().extractFullName());
		vermoegenTitle.setSupertext("2");

		FinanzielleSituationRow bruttovermoegen = createRow(translate(BRUTTOVERMOEGEN),
			AbstractFinanzielleSituation::getBruttovermoegen, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow schulden = createRow(translate(SCHULDEN),
			AbstractFinanzielleSituation::getSchulden, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow zwischentotal = createRow(
			translate(NETTOVERMOEGEN_ZWISCHENTOTAL),
			printCHF(finanzielleSituationRechner.getZwischentotalVermoegen(gs1))
		);

		FinanzielleSituationRow total = createRow(translate(NETTOVERMOEGEN_TOTAL));

		FinanzielleSituationRow vermoegen5Percent = createRow(translate(NETTOVERMOEGEN_5_PROZENT));

		if (gs2 != null) {
			requireNonNull(gesuch.getGesuchsteller2());

			vermoegenTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			zwischentotal.setGs2(finanzielleSituationRechner.getZwischentotalVermoegen(gs2));
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalVermoegenBeiderGS);
			vermoegen5Percent.setGs2(vermoegen5Prozent);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalVermoegenBeiderGS);
			vermoegen5Percent.setGs1(vermoegen5Prozent);
		}

		FinanzielleSituationTable table = createFinSitTable();
		table.addRow(vermoegenTitle);
		if (gs1.getNettoVermoegen() == null || (gs2 != null && gs2.getNettoVermoegen() == null)) {
			table.addRow(bruttovermoegen);
			table.addRow(schulden);
			table.addRow(zwischentotal);
			table.addRow(total);
		}
		if (gs1.getNettoVermoegen() != null || (gs2 != null && gs2.getNettoVermoegen() != null)) {
			FinanzielleSituationRow nettovermoegen = createRow(translate(NETTOVERMOEGEN),
				AbstractFinanzielleSituation::getNettoVermoegen, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);
			table.addRow(nettovermoegen);
		}

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
		BigDecimal totalAbzuegeBeiderGS = finanzielleSituationRechner.calcAbzuege(gs1, gs2);

		FinanzielleSituationRow abzuegeTitle = createRow(translate(ABZUEGE), gesuch.getGesuchsteller1().extractFullName());
		abzuegeTitle.setSupertext("3");

		FinanzielleSituationRow unterhaltsbeitraege = createRow(translate(UNTERHALTSBEITRAEGE_BEZAHLT),
			AbstractFinanzielleSituation::getGeleisteteAlimente, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);

		FinanzielleSituationRow total = createRow(translate(ABZUEGE_TOTAL));

		if (gs2 != null) {
			requireNonNull(gesuch.getGesuchsteller2());
			abzuegeTitle.setGs2(gesuch.getGesuchsteller2().extractFullName());
			// Total wird bei 2 GS beim 2. GS eingetragen
			total.setGs2(totalAbzuegeBeiderGS);
		} else {
			// Total wird bei 1 GS beim 1. GS eingetragen
			total.setGs1(totalAbzuegeBeiderGS);
		}
		FinanzielleSituationTable table = createFinSitTable();
		table.addRow(abzuegeTitle);
		table.addRow(unterhaltsbeitraege);

		if (finSitTyp == FinanzielleSituationTyp.BERN_FKJV) {
			addAbzuegeFKJVRow(table, gs1, gs2, gs1Urspruenglich, gs2Urspruenglich);
		}

		table.addRow(total);
		return table.createTable();
	}

	private void addAbzuegeFKJVRow(
		FinanzielleSituationTable tableAbzuege, @Nullable AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2,
		@Nullable AbstractFinanzielleSituation gs1Urspruenglich,
		@Nullable AbstractFinanzielleSituation gs2Urspruenglich) {
		FinanzielleSituationRow abzugSchuldzinsen = createRow(
			translate(SCHULDZINSEN),
			AbstractFinanzielleSituation::getAbzugSchuldzinsen,
			gs1,
			gs2,
			gs1Urspruenglich,
			gs2Urspruenglich);

		FinanzielleSituationRow gewinnungskosten = createRow(
			translate(GEWINNUNGSKOSTEN),
			AbstractFinanzielleSituation::getGewinnungskosten,
			gs1,
			gs2,
			gs1Urspruenglich,
			gs2Urspruenglich);

		tableAbzuege.addRow(abzugSchuldzinsen);
		tableAbzuege.addRow(gewinnungskosten);
	}

	@Nonnull
	private PdfPTable createTableZusammenzug(
		@Nonnull AbstractFinanzielleSituation gs1,
		@Nullable AbstractFinanzielleSituation gs2
	) {
		requireNonNull(gesuch.getGesuchsteller1());
		BigDecimal massgebendesEinkommenVorAbzugFamiliengroesse =
			finanzielleSituationRechner.calcMassgebendesEinkommenVorAbzugFamiliengroesse(gs1, gs2);

		FinanzielleSituationRow zusammenzugTitle = new FinanzielleSituationRow(
			translate(ZUSAMMENZUG), "");
		FinanzielleSituationRow einkommen = new FinanzielleSituationRow(
			translate(EINKOMMEN_TOTAL), finanzielleSituationRechner.getZwischentotalEinkommen(gs1));
		FinanzielleSituationRow vermoegen = new FinanzielleSituationRow(
			translate(NETTOVERMOEGEN_5_PROZENT), AbstractFinanzielleSituationRechner.calcVermoegen5Prozent(gs1, gs2));
		FinanzielleSituationRow abzuege = new FinanzielleSituationRow(
			translate(ABZUEGE_TOTAL), finanzielleSituationRechner.getZwischetotalAbzuege(gs1));
		FinanzielleSituationRow total = new FinanzielleSituationRow(
			translate(MASSG_EINKOMMEN_VOR_FAMILIENGROESSE), massgebendesEinkommenVorAbzugFamiliengroesse);

		if (gs2 != null) {
			requireNonNull(gesuch.getGesuchsteller2());
			einkommen.setGs1(MathUtil.DEFAULT.add(finanzielleSituationRechner.getZwischentotalEinkommen(gs1), finanzielleSituationRechner.getZwischentotalEinkommen(gs2)));
			abzuege.setGs1(MathUtil.DEFAULT.add(finanzielleSituationRechner.getZwischetotalAbzuege(gs1), finanzielleSituationRechner.getZwischetotalAbzuege(gs2)));
		}
		FinanzielleSituationTable table = new FinanzielleSituationTable(getPageConfiguration(), false, false);
		table.addRow(zusammenzugTitle);
		table.addRow(einkommen);
		table.addRow(vermoegen);
		table.addRow(abzuege);
		table.addRow(total.bold());
		return table.createTable();
	}

	private void createFusszeile(@Nonnull PdfContentByte dirPdfContentByte) throws DocumentException {
		if (finSitTyp == FinanzielleSituationTyp.BERN_FKJV) {
			return;
		}
		createFusszeile(dirPdfContentByte, Lists.newArrayList(
			translate(FUSSZEILE_EINKOMMEN),
			translate(FUSSZEILE_VERMOEGEN),
			translate(FUSSZEILE_ABZUEGE)));
	}

	protected final FinanzielleSituationRow createRow(
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
		return createRow(message, gs1BigDecimal, gs2BigDecimal, gs1UrspruenglichBigDecimal, gs2UrspruenglichBigDecimal);
	}

	protected final FinanzielleSituationRow createRow(
		String message,
		@Nullable BigDecimal gs1BigDecimal,
		@Nullable BigDecimal gs2BigDecimal,
		@Nullable BigDecimal gs1UrspruenglichBigDecimal,
		@Nullable BigDecimal gs2UrspruenglichBigDecimal
	) {
		FinanzielleSituationRow row = new FinanzielleSituationRow(message, gs1BigDecimal);
		row.setGs2(gs2BigDecimal);
		if (!MathUtil.isSameWithNullAsZero(gs1BigDecimal, gs1UrspruenglichBigDecimal)) {
			row.setGs1Urspruenglich(gs1UrspruenglichBigDecimal, sprache, mandant);
		}
		if (!MathUtil.isSameWithNullAsZero(gs2BigDecimal, gs2UrspruenglichBigDecimal)) {
			row.setGs2Urspruenglich(gs2UrspruenglichBigDecimal, sprache, mandant);
		}
		return row;
	}
}
