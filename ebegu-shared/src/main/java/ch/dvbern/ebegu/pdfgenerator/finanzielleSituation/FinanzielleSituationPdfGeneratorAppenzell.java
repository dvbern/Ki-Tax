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
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
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

import static java.util.Objects.requireNonNull;

public class FinanzielleSituationPdfGeneratorAppenzell extends FinanzielleSituationPdfGenerator {

	private static final String SAEULE3A_ROW = "PdfGeneration_FinSit_Saeule3aTitle";
	private static final String SAEULE3ANICHTBVG_ROW = "PdfGeneration_FinSit_Saeule3aNichtBvgTitle";
	private static final String BERUFLICHE_VORSORGE_ROW = "PdfGeneration_FinSit_BeruflicheVorsorgeTitle";
	private static final String LIEGENSCHAFTSAUFWAND_ROW = "PdfGeneration_FinSit_LiegenschaftsauswandTitle";
	private static final String EINKUENFTE_BGSA_ROW = "PdfGeneration_FinSit_EinfkuenfteBgsaTitle";
	private static final String VORJAHRESVERLUSTE_ROW = "PdfGeneration_FinSit_VorjahresverlusteTitle";
	private static final String POLITISCHE_PARTEI_SPENDE_ROW = "PdfGeneration_FinSit_PolitischeParteiSpendeTitle";
	private static final String LEISTUNG_AN_JURISTISCHE_PERSONEN_ROW = "PdfGeneration_FinSit_LeistungJurPersTitle";
	private static final String STEUERBARES_EINKOMMEN_ROW = "PdfGeneration_FinSit_SteuerbaresEinkommenTitle";
	private static final String EINKOMMEN_TOTAL = "PdfGeneration_FinSit_EinkommenTotal";
	private static final String EINKOMMEN_ZWISCHENTOTAL = "PdfGeneration_FinSit_EinkommenZwischentotal";
	private static final String STEUERBARES_VERMOEGEN_ROW = "PdfGeneration_FinSit_SteuerbaresVermoegenTitle";
	private static final String STEUERBARES_VERMOEGEN_15_PROZENT_ROW = "PdfGeneration_FinSit_SteuerbaresVermoegen15ProzentTitle";
	private static final String STEUERBARES_VERMOEGEN_TOTAL_ROW = "PdfGeneration_FinSit_SteuerbaresVermoegenTotalTitle";
	private static final String MASSG_EINKOMMEN_VOR_FAMILIENGROESSE =
		"PdfGeneration_MassgEinkommen_MassgEink";
	private static final String EINKOMMEN_TITLE = "PdfGeneration_FinSit_EinkommenTitle";
	private static final String VERMOEGEN = "PdfGeneration_FinSit_VermoegenTitle";
	private static final String PARTNERIN = "PdfGeneration_Partnerin";
	private static final String EINKOMMENSAENDERUNG = "Reports_einkommensverschlechterungTitle";

	private FinSitZusatzangabenAppenzell angabenGS1Bj = null;
	@Nullable
	private FinSitZusatzangabenAppenzell angabenGS2Bj = null;
	@Nullable
	private FinSitZusatzangabenAppenzell angabenGS1BjUrspruenglich = null;
	@Nullable
	private FinSitZusatzangabenAppenzell angabenGS2BjUrspruenglich = null;

	public FinanzielleSituationPdfGeneratorAppenzell(
			@Nonnull Gesuch gesuch,
			@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
			@Nonnull GemeindeStammdaten stammdaten,
			@Nonnull LocalDate erstesEinreichungsdatum,
			@Nonnull AbstractFinanzielleSituationRechner finanzielleSituationRechner
	) {
		super(gesuch, verfuegungFuerMassgEinkommen, stammdaten, erstesEinreichungsdatum, finanzielleSituationRechner);
		hasSecondGesuchsteller = calculateHasSecondGesuchsteller(gesuch);
		finanzDatenDTO = finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, hasSecondGesuchsteller);

	}

	@Override
	protected void initializeValues() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());

		angabenGS1Bj = requireNonNull(getAngabenGS1(gesuch, false));
		Objects.requireNonNull(angabenGS1Bj);

		angabenGS2Bj = getAngabenGS2(gesuch, false);
		angabenGS1BjUrspruenglich = getAngabenGS1(gesuch, true);
		angabenGS2BjUrspruenglich = getAngabenGS2(gesuch, true);
		initialzeEkv();
	}

	@Override
	protected void createPageBasisJahr(
			@Nonnull PdfGenerator generator,
			@Nonnull Document document
	) {
		Objects.requireNonNull(finanzDatenDTO);
		document.add(createIntroBasisjahr());

		document.add(createTableEinkommen(angabenGS1Bj, angabenGS2Bj, angabenGS1BjUrspruenglich, angabenGS2BjUrspruenglich, finanzDatenDTO));
		document.add(createTableVermoegen(angabenGS1Bj, angabenGS2Bj, angabenGS1BjUrspruenglich, angabenGS2BjUrspruenglich, finanzDatenDTO));

	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		createPageEkv(document, 1);
	}

	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		createPageEkv(document, 2);
	}

	@Override
	protected void createPageMassgebendesEinkommen(@Nonnull Document document) {
		List<String[]> values = new ArrayList<>();
		String[] titles = {
				translate(VON),
				translate(BIS),
				translate(EINKOMMENSAENDERUNG),
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
			boolean ekvEinkommen = abschnitt.getEinkommensjahr() != gesuch.getGesuchsperiode().getBasisJahr();
			String[] data = {
					Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigAb()),
					Constants.DATE_FORMATTER.format(abschnitt.getGueltigkeit().getGueltigBis()),
					ekvEinkommen ? "x" : "",
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
		document.newPage();
		document.add(PdfUtil.createBoldParagraph(translate(MASSG_EINK_TITLE), 2));
		document.add(createIntroMassgebendesEinkommen());
		document.add(PdfUtil.createTable(values, widthMassgebendesEinkommen, alignmentMassgebendesEinkommen, 0));
	}

	private Element createTableEinkommen(
			FinSitZusatzangabenAppenzell gs1Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs2Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs1AngabenUrspruenglich,
			@Nullable FinSitZusatzangabenAppenzell gs2AngabenUrspruenglich,
			FinanzielleSituationResultateDTO finSitDTO
	) {
		FinanzielleSituationTable einkommenTable = new FinanzielleSituationTable(
				getPageConfiguration(),
				hasSecondGesuchsteller,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false
		);

		FinanzielleSituationRow steurbaresEinkommenRow = createRow(
				translate(STEUERBARES_EINKOMMEN_ROW, mandant),
				FinSitZusatzangabenAppenzell::getSteuerbaresEinkommen,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow saeule3aRow = createRow(
				translate(SAEULE3A_ROW, mandant),
				FinSitZusatzangabenAppenzell::getSaeule3a,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow saeule3aNichtBvgRow = createRow(
				translate(SAEULE3ANICHTBVG_ROW, mandant),
				FinSitZusatzangabenAppenzell::getSaeule3aNichtBvg,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow beruflicheVorsorgeRow = createRow(
				translate(BERUFLICHE_VORSORGE_ROW, mandant),
				FinSitZusatzangabenAppenzell::getBeruflicheVorsorge,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow liegenschaftsaufwandRow = createRow(
				translate(LIEGENSCHAFTSAUFWAND_ROW, mandant),
				FinSitZusatzangabenAppenzell::getLiegenschaftsaufwand,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow einkuenfteBgsaRow = createRow(
				translate(EINKUENFTE_BGSA_ROW, mandant),
				FinSitZusatzangabenAppenzell::getEinkuenfteBgsa,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow vorjahresverlustRow = createRow(
				translate(VORJAHRESVERLUSTE_ROW, mandant),
				FinSitZusatzangabenAppenzell::getVorjahresverluste,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow politischeParteiSpendeRow = createRow(
				translate(POLITISCHE_PARTEI_SPENDE_ROW, mandant),
				FinSitZusatzangabenAppenzell::getPolitischeParteiSpende,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow leistungJurPersRow = createRow(
				translate(LEISTUNG_AN_JURISTISCHE_PERSONEN_ROW, mandant),
				FinSitZusatzangabenAppenzell::getLeistungAnJuristischePersonen,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow totalRow = createRow(
				EINKOMMEN_TOTAL,
				hasSecondGesuchsteller ? null : finSitDTO.getEinkommenBeiderGesuchsteller(),
				hasSecondGesuchsteller,
				hasSecondGesuchsteller ? finSitDTO.getEinkommenBeiderGesuchsteller() : null
		);

		FinanzielleSituationRow zwischenTotalRow = createRow(
				EINKOMMEN_ZWISCHENTOTAL,
				finSitDTO.getEinkommenGS1(),
				hasSecondGesuchsteller,
				finSitDTO.getEinkommenGS2());

		FinanzielleSituationRow einkommenTitle = new FinanzielleSituationRow(
				translate(EINKOMMEN_TITLE), extractFullnameGS1());

		if (gs2Angaben != null) {
			setPartnerInNameNullsafe(einkommenTitle);
		}

		einkommenTable.addRow(einkommenTitle);
		einkommenTable.addRow(steurbaresEinkommenRow);
		einkommenTable.addRow(saeule3aRow);
		einkommenTable.addRow(saeule3aNichtBvgRow);
		einkommenTable.addRow(beruflicheVorsorgeRow);
		einkommenTable.addRow(liegenschaftsaufwandRow);
		einkommenTable.addRow(einkuenfteBgsaRow);
		einkommenTable.addRow(vorjahresverlustRow);
		einkommenTable.addRow(politischeParteiSpendeRow);
		einkommenTable.addRow(leistungJurPersRow);
		if (hasSecondGesuchsteller) {
			einkommenTable.addRow(zwischenTotalRow);
		}
		einkommenTable.addRow(totalRow);
		return einkommenTable.createTable();
	}

	private Element createTableVermoegen(
			FinSitZusatzangabenAppenzell gs1Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs2Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs1AngabenUrspruenglich,
			@Nullable FinSitZusatzangabenAppenzell gs2AngabenUrspruenglich,
			FinanzielleSituationResultateDTO finSitDTO
	) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		FinanzielleSituationTable vermoegenTable = new FinanzielleSituationTable(
				getPageConfiguration(),
				hasSecondGesuchsteller,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				true
		);

		FinanzielleSituationRow vermoegenTitle = new FinanzielleSituationRow(
				translate(VERMOEGEN, mandant), extractFullnameGS1());


		FinanzielleSituationRow vermoegenRow = createRow(translate(STEUERBARES_VERMOEGEN_ROW, mandant),
				FinSitZusatzangabenAppenzell::getSteuerbaresVermoegen,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrspruenglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow vermoegen15ProzentRow = new FinanzielleSituationRow(translate(STEUERBARES_VERMOEGEN_15_PROZENT_ROW, mandant),"");
		FinanzielleSituationRow vermoegenTotalRow = new FinanzielleSituationRow(translate(STEUERBARES_VERMOEGEN_TOTAL_ROW, mandant),"");
		if (gs2Angaben != null) {
			setPartnerInNameNullsafe(vermoegenTitle);

			vermoegenTotalRow.setGs2(getVermoegenTotal(gs1Angaben, gs2Angaben));
			vermoegen15ProzentRow.setGs2(getVermoegen15Prozent(finSitDTO));
		} else {
			vermoegenTotalRow.setGs1(getVermoegenTotal(gs1Angaben, null));
			vermoegen15ProzentRow.setGs1(getVermoegen15Prozent(finSitDTO));
		}
		vermoegenTable.addRow(vermoegenTitle);
		vermoegenTable.addRow(vermoegenRow);
		// Zwischentotal ist unnötig, wenn es keinen GS2 gibt
		if (gs2Angaben !=null) {
			vermoegenTable.addRow(vermoegenTotalRow);
		}
		vermoegenTable.addRow(vermoegen15ProzentRow);

		FinanzielleSituationRow total = createRow(
			MASSG_EINKOMMEN_VOR_FAMILIENGROESSE,
			hasSecondGesuchsteller ? null : finSitDTO.getMassgebendesEinkVorAbzFamGr(),
			hasSecondGesuchsteller,
			hasSecondGesuchsteller ? finSitDTO.getMassgebendesEinkVorAbzFamGr() : null);
		vermoegenTable.addRow(total);


		return vermoegenTable.createTable();
	}

	private void createPageEkv(@Nonnull Document document, int jahrPlus) {
		FinSitZusatzangabenAppenzell ekv1GS1 = getEkvGS(gesuch.getGesuchsteller1(), jahrPlus);
		FinSitZusatzangabenAppenzell ekv1GS1Urspruenglich = getEkvGSUrspruenglich(gesuch.getGesuchsteller1(), jahrPlus);
		FinSitZusatzangabenAppenzell ekv1GS2 = getEkvGS2(jahrPlus);
		FinSitZusatzangabenAppenzell ekv1GS2Urspruenglich = getEkvGS2Urspruenglich(jahrPlus);
		var finSitDTO = jahrPlus == 1 ? ekvBasisJahrPlus1 : ekvBasisJahrPlus2;
		Objects.requireNonNull(finSitDTO);

		Objects.requireNonNull(ekv1GS1);

		document.newPage();
		document.add(createTitleEkv(jahrPlus == 1 ?
				gesuch.getGesuchsperiode().getBasisJahrPlus1() :
				gesuch.getGesuchsperiode().getBasisJahrPlus2()));
		document.add(createIntroEkv());

		document.add(createTableEinkommen(ekv1GS1, ekv1GS2, ekv1GS1Urspruenglich, ekv1GS2Urspruenglich, finSitDTO));
		document.add(createTableVermoegen(ekv1GS1, ekv1GS2, ekv1GS1Urspruenglich, ekv1GS2Urspruenglich, finSitDTO));
	}

	private FinanzielleSituationRow createRow(
			String message,
			Function<FinSitZusatzangabenAppenzell, BigDecimal> getter,
			FinSitZusatzangabenAppenzell gs1,
			@Nullable FinSitZusatzangabenAppenzell gs2,
			@Nullable FinSitZusatzangabenAppenzell gs1Urspruenglich,
			@Nullable FinSitZusatzangabenAppenzell gs2Urspruenglich) {
		BigDecimal gs1BigDecimal = gs1 == null ? null : getter.apply(gs1);
		BigDecimal gs2BigDecimal = gs2 == null ? null : getter.apply(gs2);
		BigDecimal gs1UrspruenglichBigDecimal = gs1Urspruenglich == null ? null : getter.apply(gs1Urspruenglich);
		BigDecimal gs2UrspruenglichBigDecimal = gs2Urspruenglich == null ? null : getter.apply(gs2Urspruenglich);
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

	@Nonnull
	private String extractFullnameGS1() {
		Familiensituation famSitGS1 = getFamSitNullSafe();
		requireNonNull(gesuch.getGesuchsteller1());
		if (Boolean.FALSE.equals(famSitGS1.getGemeinsameSteuererklaerung())) {
			return gesuch.getGesuchsteller1().extractFullName();
		}
		return gesuch.getGesuchsteller1().extractFullName() + (gesuch.getGesuchsteller2() != null ?
				" + " + gesuch.getGesuchsteller2().extractFullName() : "");
	}

	private void setPartnerInNameNullsafe(FinanzielleSituationRow row) {
		if (getFamSitNullSafe().isSpezialFallAR()) {
			row.setGs2(translate(PARTNERIN));
		} else {
			requireNonNull(gesuch.getGesuchsteller2());
			row.setGs2(gesuch.getGesuchsteller2().extractFullName());
		}
	}

	private Familiensituation getFamSitNullSafe() {
		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		return gesuch.getFamiliensituationContainer().getFamiliensituationJA();
	}

	@Nullable
	private BigDecimal getVermoegen15Prozent(FinanzielleSituationResultateDTO finSitDTO) {
		if (finSitDTO == null) {
			return null;
		}
		return MathUtil.DEFAULT.add(finSitDTO.getVermoegenXPercentAnrechenbarGS1(), finSitDTO.getVermoegenXPercentAnrechenbarGS2());
	}

	@Nullable
	private BigDecimal getVermoegenTotal(
			FinSitZusatzangabenAppenzell gs1Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs2Angaben) {
		if(gs1Angaben.getSteuerbaresVermoegen() == null) {
			return null;
		}
		if (gs2Angaben != null) {
			return MathUtil.DEFAULT.addNullSafe(gs1Angaben.getSteuerbaresVermoegen(), gs2Angaben.getSteuerbaresVermoegen());
		}
		return gs1Angaben.getSteuerbaresVermoegen();
	}

	@Nullable
	private FinSitZusatzangabenAppenzell getAngabenGS1(Gesuch gesuchToUse, boolean urspruenglich) {
		if (gesuchToUse.getGesuchsteller1() == null
				|| gesuchToUse.getGesuchsteller1().getFinanzielleSituationContainer() == null) {
			return null;
		}
		GesuchstellerContainer gsContainer = gesuchToUse.getGesuchsteller1();
		if (urspruenglich) {
			if (gsContainer.getFinanzielleSituationContainer().getFinanzielleSituationGS() == null) {
				return null;
			}
			return gsContainer
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationGS()
					.getFinSitZusatzangabenAppenzell();
		}
		return gsContainer
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getFinSitZusatzangabenAppenzell();

	}

	@Nullable
	private FinSitZusatzangabenAppenzell getAngabenGS2(Gesuch gesuchToUse, boolean urspruenglich) {
		if (getFamSitNullSafe().isSpezialFallAR()) {
			var angabenGS1 = getAngabenGS1(gesuchToUse, urspruenglich);
			if (angabenGS1 != null) {
				return angabenGS1.getZusatzangabenPartner();
			}
			return null;
		}
		if (gesuchToUse.getGesuchsteller2() == null
				|| gesuchToUse.getGesuchsteller2().getFinanzielleSituationContainer() == null) {
			return null;
		}
		final GesuchstellerContainer gsContainer = gesuchToUse.getGesuchsteller2();
		if (urspruenglich) {
			if (gsContainer.getFinanzielleSituationContainer().getFinanzielleSituationGS() == null) {
				return null;
			}
			return gsContainer
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationGS()
					.getFinSitZusatzangabenAppenzell();
		}
		return gsContainer
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getFinSitZusatzangabenAppenzell();
	}


	@Nullable
	private static FinSitZusatzangabenAppenzell getEkvGS(@Nullable GesuchstellerContainer gesuchstellerContainer, int plusJahr) {
		EinkommensverschlechterungContainer ekvContainer =
				getEinkommensverschlechterungContainer(gesuchstellerContainer);
		if (ekvContainer == null) {
			return null;
		}
		if (plusJahr == 1) {
			return ekvContainer.getEkvJABasisJahrPlus1() == null ? null : ekvContainer.getEkvJABasisJahrPlus1().getFinSitZusatzangabenAppenzell();
		}
		return ekvContainer.getEkvJABasisJahrPlus2() == null ? null : ekvContainer.getEkvJABasisJahrPlus2().getFinSitZusatzangabenAppenzell();
	}

	@Nullable
	private static FinSitZusatzangabenAppenzell getEkvGSUrspruenglich(@Nullable GesuchstellerContainer gesuchstellerContainer, int plusJahr) {
		EinkommensverschlechterungContainer ekvContainer = getEinkommensverschlechterungContainer(gesuchstellerContainer);
		if (ekvContainer == null) {
			return null;
		}
		if (plusJahr == 1) {
			return ekvContainer.getEkvGSBasisJahrPlus1() == null ? null : ekvContainer.getEkvGSBasisJahrPlus1().getFinSitZusatzangabenAppenzell();
		}
		return ekvContainer.getEkvGSBasisJahrPlus2() == null ? null : ekvContainer.getEkvGSBasisJahrPlus2().getFinSitZusatzangabenAppenzell();
	}

	@Nullable
	private FinSitZusatzangabenAppenzell getEkvGS2(int plusJahr) {
		if (getFamSitNullSafe().isSpezialFallAR()) {
			FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell = getEkvGS(gesuch.getGesuchsteller1(), plusJahr);
			return finSitZusatzangabenAppenzell == null ? null : finSitZusatzangabenAppenzell.getZusatzangabenPartner();
		}
		return getEkvGS(gesuch.getGesuchsteller2(), plusJahr);
	}
	@Nullable
	private FinSitZusatzangabenAppenzell getEkvGS2Urspruenglich(int plusJahr) {
		if (getFamSitNullSafe().isSpezialFallAR()) {
			FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell = getEkvGSUrspruenglich(gesuch.getGesuchsteller1(), plusJahr);
			return finSitZusatzangabenAppenzell == null ? null : finSitZusatzangabenAppenzell.getZusatzangabenPartner();
		}
		return getEkvGSUrspruenglich(gesuch.getGesuchsteller2(), plusJahr);
	}

	@Nullable
	private static EinkommensverschlechterungContainer getEinkommensverschlechterungContainer(
			@Nullable GesuchstellerContainer gesuchstellerContainer) {
		if (gesuchstellerContainer == null) {
			return null;
		}
		EinkommensverschlechterungContainer ekvContainer = gesuchstellerContainer.getEinkommensverschlechterungContainer();
		return ekvContainer;
	}

	private static boolean calculateHasSecondGesuchsteller(@Nonnull Gesuch gesuch) {
		requireNonNull(gesuch.getFamiliensituationContainer());
		requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		return gesuch.getGesuchsteller2() != null && Boolean.FALSE.equals(gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getGemeinsameSteuererklaerung())
				|| gesuch.getFamiliensituationContainer().getFamiliensituationJA().isSpezialFallAR();
	}

	@Nonnull
	@Override
	protected PdfPTable createIntroBasisjahr() {
		List<TableRowLabelValue> introBasisjahr = new ArrayList<>();
		introBasisjahr.add(new TableRowLabelValue(REFERENZ_NUMMER, gesuch.getJahrFallAndGemeindenummer()));
		return PdfUtil.createIntroTable(introBasisjahr, sprache, mandant);
	}
}
