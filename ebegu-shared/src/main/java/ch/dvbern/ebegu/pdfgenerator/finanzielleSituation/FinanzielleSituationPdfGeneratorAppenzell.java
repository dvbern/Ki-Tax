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
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Element;

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
	private static final String STEUERBARES_VERMOEGEN_ROW = "PdfGeneration_FinSit_SteuerbaresVermoegenTitle";
	private static final String STEUERBARES_VERMOEGEN_15_PROZENT_ROW = "PdfGeneration_FinSit_SteuerbaresVermoegen15ProzentTitle";
	private static final String STEUERBARES_VERMOEGEN_TOTAL_ROW = "PdfGeneration_FinSit_SteuerbaresVermoegenTotalTitle";
	private static final String EIKOMMEN_TITLE = "PdfGeneration_FinSit_EinkommenTitle";
	private static final String VERMOEGEN = "PdfGeneration_FinSit_VermoegenTitle";
	private static final String PARTNERIN = "PdfGeneration_Partnerin";

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
		finanzDatenDTO = finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, hasSecondGesuchsteller);
		hasSecondGesuchsteller = calculateHasSecondGesuchsteller(gesuch);
		
	}

	private static boolean calculateHasSecondGesuchsteller(@Nonnull Gesuch gesuch) {
		requireNonNull(gesuch.getFamiliensituationContainer());
		requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		return gesuch.getGesuchsteller2() != null && Boolean.FALSE.equals(gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getGemeinsameSteuererklaerung())
				|| gesuch.getFamiliensituationContainer().getFamiliensituationJA().isSpezialFallAR();
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
		document.add(createTableVermoegen(angabenGS1Bj, angabenGS2Bj, angabenGS1BjUrspruenglich, angabenGS2BjUrspruenglich));

	}

	private Element createTableEinkommen(
			FinSitZusatzangabenAppenzell gs1Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs2Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs1AngabenUrsprünglich,
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
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow saeule3aRow = createRow(
				translate(SAEULE3A_ROW, mandant),
				FinSitZusatzangabenAppenzell::getSaeule3a,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow saeule3aNichtBvgRow = createRow(
				translate(SAEULE3ANICHTBVG_ROW, mandant),
				FinSitZusatzangabenAppenzell::getSaeule3aNichtBvg,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow beruflicheVorsorgeRow = createRow(
				translate(BERUFLICHE_VORSORGE_ROW, mandant),
				FinSitZusatzangabenAppenzell::getBeruflicheVorsorge,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow liegenschaftsaufwandRow = createRow(
				translate(LIEGENSCHAFTSAUFWAND_ROW, mandant),
				FinSitZusatzangabenAppenzell::getLiegenschaftsaufwand,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow einkuenfteBgsaRow = createRow(
				translate(EINKUENFTE_BGSA_ROW, mandant),
				FinSitZusatzangabenAppenzell::getEinkuenfteBgsa,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow vorjahresverlustRow = createRow(
				translate(VORJAHRESVERLUSTE_ROW, mandant),
				FinSitZusatzangabenAppenzell::getVorjahresverluste,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow politischeParteiSpendeRow = createRow(
				translate(POLITISCHE_PARTEI_SPENDE_ROW, mandant),
				FinSitZusatzangabenAppenzell::getPolitischeParteiSpende,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow leistungJurPersRow = createRow(
				translate(LEISTUNG_AN_JURISTISCHE_PERSONEN_ROW, mandant),
				FinSitZusatzangabenAppenzell::getLeistungAnJuristischePersonen,
				gs1Angaben,
				gs2Angaben,
				gs1AngabenUrsprünglich,
				gs2AngabenUrspruenglich);

		FinanzielleSituationRow totalRow = createRow(translate(EINKOMMEN_TOTAL),
				hasSecondGesuchsteller ? null : finSitDTO.getMassgebendesEinkVorAbzFamGrGS1(),
				hasSecondGesuchsteller,
				hasSecondGesuchsteller ? finSitDTO.getMassgebendesEinkVorAbzFamGrGS1() : null);

		FinanzielleSituationRow einkommenTitle = new FinanzielleSituationRow(
				translate(EIKOMMEN_TITLE, mandant), extractFullnameGS1());

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
		einkommenTable.addRow(totalRow);
		return einkommenTable.createTable();
	}

	private void setPartnerInNameNullsafe(FinanzielleSituationRow row) {
		if (getFamSitNullSafe().isSpezialFallAR()) {
			row.setGs2(translate(PARTNERIN));
		} else {
			requireNonNull(gesuch.getGesuchsteller2());
			row.setGs2(gesuch.getGesuchsteller2().extractFullName());
		}
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

	private Familiensituation getFamSitNullSafe() {
		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		return gesuch.getFamiliensituationContainer().getFamiliensituationJA();
	}

	private Element createTableVermoegen(
			FinSitZusatzangabenAppenzell gs1Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs2Angaben,
			@Nullable FinSitZusatzangabenAppenzell gs1AngabenUrspruenglich,
			@Nullable FinSitZusatzangabenAppenzell gs2AngabenUrspruenglich
	) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		FinanzielleSituationTable vermoegenTable = new FinanzielleSituationTable(
				getPageConfiguration(),
				hasSecondGesuchsteller,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				false
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

			vermoegenTotalRow.setGs2(getVermoegenTotal());
			vermoegen15ProzentRow.setGs2(getVermoegen15Prozent());
		} else {
			vermoegenTotalRow.setGs1(getVermoegenTotal());
			vermoegen15ProzentRow.setGs1(getVermoegen15Prozent());
		}
		vermoegenTable.addRow(vermoegenTitle);
		vermoegenTable.addRow(vermoegenRow);
		// Zwischentotal ist unnötig, wenn es keinen GS2 gibt
		if (gs2Angaben !=null) {
			vermoegenTable.addRow(vermoegenTotalRow);
		}
		vermoegenTable.addRow(vermoegen15ProzentRow);



		return vermoegenTable.createTable();
	}

	@Nullable
	private BigDecimal getVermoegen15Prozent() {
		if (getVermoegenTotal() == null) {
			return null;
		}
		return MathUtil.DEFAULT.multiply(BigDecimal.valueOf(0.15), getVermoegenTotal());
	}

	@Nullable
	private BigDecimal getVermoegenTotal() {
		Objects.requireNonNull(angabenGS1Bj);
		if(angabenGS1Bj.getSteuerbaresVermoegen() == null) {
			return null;
		}
		if (angabenGS2Bj != null) {
			return MathUtil.DEFAULT.addNullSafe(angabenGS1Bj.getSteuerbaresVermoegen(), angabenGS2Bj.getSteuerbaresVermoegen());
		}
		return angabenGS1Bj.getSteuerbaresVermoegen();
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

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {

	}

	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {

	}
}
