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
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationUtil.requireFinanzielleSituation;
import static java.util.Objects.requireNonNull;

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

	@Override
	protected void initializeValues() {
		basisJahrGS1 = requireFinanzielleSituation(gesuch.getGesuchsteller1());
		gs1 = requireNonNull(requireNonNull(gesuch.getGesuchsteller1()).getGesuchstellerJA());

		boolean hasSecondGS = false;
		if (
			gesuch.getGesuchsteller2() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA() != null
		) {
			basisJahrGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer();
			requireNonNull(gesuch.getGesuchsteller2().getGesuchstellerJA());
			gs2 = gesuch.getGesuchsteller2().getGesuchstellerJA();
			hasSecondGS = true;
		}

		finanzDatenDTO = finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, hasSecondGS);
		initialzeEkv();
	}

	@Override
	protected void initialzeEkv() {
		super.initialzeEkv();

		EinkommensverschlechterungContainer ekvContainerGS1 =
			requireNonNull(gesuch.getGesuchsteller1()).getEinkommensverschlechterungContainer();
		if (ekvContainerGS1 != null) {
			ekv1GS1 = ekvContainerGS1.getEkvJABasisJahrPlus1();
			ekv2GS1 = ekvContainerGS1.getEkvJABasisJahrPlus2();
		}

		if (hasSecondGesuchsteller) {
			requireNonNull(gesuch.getGesuchsteller2());
			EinkommensverschlechterungContainer ekvContainerGS2 =
				gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer();

			if (ekvContainerGS2 != null) {
				ekv1GS2 = ekvContainerGS2.getEkvJABasisJahrPlus1();
				ekv2GS2 = ekvContainerGS2.getEkvJABasisJahrPlus2();
			}
		}
	}

	@Override
	protected void createPageBasisJahr(
		@Nonnull PdfGenerator generator,
		@Nonnull Document document
	) {
		document.add(createIntroBasisjahr());

		addMassgebendesEinkommenTableForGesuchsteller(document, basisJahrGS1, gs1.getFullName());
		// vermoegen is the same in every deklaration typ
		document.add(createVermoegenTable(basisJahrGS1, requireNonNull(finanzDatenDTO).getMassgebendesEinkVorAbzFamGrGS1()));

		if (basisJahrGS2 != null) {
			addSpacing(document);
			addMassgebendesEinkommenTableForGesuchsteller(
				document,
				basisJahrGS2,
				gs2.getFullName()
			);
			// vermoegen is the same in every deklaration typ
			document.add(createVermoegenTable(basisJahrGS2, finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS2()));

			addSpacing(document);
			addTablezusammenzug(document);
		}

		var translatedFooters = footers.stream()
			.map(this::translate)
			.collect(Collectors.toList());

		createFusszeile(generator.getDirectContent(), translatedFooters);
	}

	private void addMassgebendesEinkommenTableForGesuchsteller(
		@Nonnull Document document,
		@Nonnull FinanzielleSituationContainer finSit,
		@Nonnull String gesuchstellerName
	) {
		if (finanzielleSituationRechner.calculateByVeranlagung(finSit.getFinanzielleSituationJA())) {
			document.add(createEinkommenTable(finSit, gesuchstellerName));
			document.add(createAbzuegeTable(finSit));
		} else {
			document.add(createTablesDeklarationByBruttolohn(finSit, gesuchstellerName));
		}
	}

	private PdfPTable createEinkommenTable(@Nonnull FinanzielleSituationContainer finSit, @Nonnull String gesuchstellerName) {
		return createFinSitTableSingleGS(
			createRow(translate(EIKOMMEN_TITLE), gesuchstellerName),
			createRow(translate(NETTOLOHN), FinanzielleSituation::getNettolohn, finSit)
		);
	}

	private PdfPTable createAbzuegeTable(@Nonnull FinanzielleSituationContainer finSit) {
		return createFinSitTableSingleGS(
			createRow(translate(ABZUEGE)),
			createRow(translate(KINDER_IN_AUSBILDUNG), FinanzielleSituation::getAbzuegeKinderAusbildung, finSit),
			createRow(translate(ENT_UNTERHALTSBEITRAEGE), FinanzielleSituation::getUnterhaltsBeitraege, finSit)
		);
	}

	private PdfPTable createVermoegenTable(
		@Nonnull FinanzielleSituationContainer finSit,
		@Nonnull BigDecimal massgebendesEinkommen
	) {
		return createFinSitTableSingleGS(
			createRow(translate(VERMOEGEN)),
			createRow(translate(STEUERBARES_VERMOEGEN), FinanzielleSituation::getSteuerbaresVermoegen, finSit)
				.withFooter(FOOTER_STEUERBARES_VERMOEGEN, footers),
			createRow(translate(MASSG_EINK), printCHF(massgebendesEinkommen))
				.withFooter(FOOTER_MASSG_EINK, footers)
				.bold()
		);
	}

	private PdfPTable createTablesDeklarationByBruttolohn(
		@Nonnull FinanzielleSituationContainer finSit,
		@Nonnull String gesuchstellerName
	) {
		return createFinSitTableSingleGS(
			createRow(translate(EIKOMMEN_TITLE), gesuchstellerName),
			createRow(translate(BRUTTOLOHN), FinanzielleSituation::getBruttoLohn, finSit)
				.withFooter(FOOTER_BRUTTOLOHN, footers)
		);
	}

	private void addSpacing(@Nonnull Document document) {
		Paragraph p = new Paragraph();
		p.setSpacingAfter(15);
		document.add(p);
	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		requireNonNull(ekvBasisJahrPlus1);
		requireNonNull(ekv1GS1);
		createPageEkv(ekvBasisJahrPlus1, ekv1GS1, ekv1GS2, gesuch.getGesuchsperiode().getBasisJahrPlus1(), document, true);
	}

	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		requireNonNull(ekvBasisJahrPlus2);
		requireNonNull(ekv2GS1);
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
		boolean isPrintOnNewPage
	) {
		if (isPrintOnNewPage) {
			document.newPage();
			document.add(createTitleEkv());
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
		int basisJahr
	) {
		return createFinSitTable()
			.addRows(
				createTableTitleForEkv(basisJahr),
				createRow(BRUTTOLOHN, ekvBasisJahr.getBruttolohnJahrGS1(), ekvBasisJahr.getBruttolohnJahrGS2()),
				createRow(NETTOVERMOEGEN, ekvGS1.getNettoVermoegen(), ekvGS2 != null ? ekvGS2.getNettoVermoegen() : null),
				createRow(
					MASSG_EINK,
					ekvBasisJahr.getMassgebendesEinkVorAbzFamGrGS1(),
					ekvBasisJahr.getMassgebendesEinkVorAbzFamGrGS2())
					.bold(!hasSecondGesuchsteller),
				hasSecondGesuchsteller ?
					createRow(TOTAL_MASSG_EINK, null, true, ekvBasisJahr.getMassgebendesEinkVorAbzFamGr())
						.bold() :
					null
			)
			.createTable();
	}
}
