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

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

public class FinanzielleSituationPdfGeneratorLuzern extends FinanzielleSituationPdfGenerator {

	private static final String STEUERBARES_VERMOEGEN = "PdfGeneration_FinSit_SteuerbaresVermoegen";
	private static final String STEUERBARES_EINKOMMEN = "PdfGeneration_FinSit_SteuerbaresEinkommen";
	private static final String NETTOEINKUENFTE_LIEGENSCHAFTEN = "PdfGeneration_FinSit_NettoeinkuenfteLiegenschaften";
	private static final String VERRECHENBARE_GESCHAEFTSVERLUSTE = "PdfGeneration_FinSit_VerrechenbareGeschaeftsverluste";
	private static final String EINKAEUFE_VORSORGE = "PdfGeneration_FinSit_EinkaeufeVorsorge";
	private static final String BERECHNUNG_GEMAESS_SELBSTDEKLARATION = "PdfGeneration_FinSit_BerechnungGemaessSelbstdeklaration";
	private static final String BERECHNUNG_GEMAESS_VERANLAGUNG = "PdfGeneration_FinSit_BerechnungGemaessVeanlagung";
	private static final String TOTAL_ABZUEGE = "PdfGeneration_FinSit_Abzuege_Total";
	private static final String TOTAL_EINKUENFTE = "PdfGeneration_FinSit_EinkommenTotal";
	private static final String ANRECHENBARES_VERMOEGEN_GEMAESS_SELBSTDEKLARATION = "PdfGeneration_FinSit_AnrechenbaresVermoegenGemaessSelbstdeklaration";

	@Nonnull
	private Gesuchsteller gs1;
	@Nonnull
	private Gesuchsteller gs2;
	@Nonnull
	private FinanzielleSituationContainer basisJahrGS1;
	@Nullable
	private FinanzielleSituationContainer basisJahrGS2;

	public FinanzielleSituationPdfGeneratorLuzern(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum,
		@Nonnull AbstractFinanzielleSituationRechner finanzielleSituationRechner
	) {
		super(gesuch, verfuegungFuerMassgEinkommen, stammdaten, erstesEinreichungsdatum, finanzielleSituationRechner);
	}

	@Override
	protected void initializeValues() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		basisJahrGS1 = gesuch.getGesuchsteller1().getFinanzielleSituationContainer();

		Objects.requireNonNull(gesuch.getGesuchsteller1().getGesuchstellerJA());
		gs1 = gesuch.getGesuchsteller1().getGesuchstellerJA();

		hasSecondGesuchsteller = false;
		if (gesuchHasTwoFinSit()) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			basisJahrGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer();
			Objects.requireNonNull(gesuch.getGesuchsteller2().getGesuchstellerJA());
			gs2 = gesuch.getGesuchsteller2().getGesuchstellerJA();
			hasSecondGesuchsteller = true;
		}

		finanzDatenDTO = finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, hasSecondGesuchsteller);
		initialzeEkv();
	}

	@Override
	protected void createPageBasisJahr(
		@Nonnull PdfGenerator generator,
		@Nonnull Document document
	) {
		Objects.requireNonNull(finanzDatenDTO);
		document.add(createIntroBasisjahr());

		createMassgebendesEinkommenTableForGesuchsteller(
			document,
			basisJahrGS1.getFinanzielleSituationJA(),
			basisJahrGS1.getFinanzielleSituationGS(),
			getNameForFinSit1(),
			1
		);

		if (basisJahrGS2 != null) {
			addSpacing(document);
			createMassgebendesEinkommenTableForGesuchsteller(
				document,
				basisJahrGS2.getFinanzielleSituationJA(),
				basisJahrGS2.getFinanzielleSituationGS(),
				gs2.getFullName(),
				2
			);

			addSpacing(document);
			createTablezusammenzug(document, gs1.getFullName(), gs2.getFullName());
		}
	}

	private String getNameForFinSit1() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		// im konkubinat hat das gesuch zwei separate finSit
		if (gesuchHasTwoFinSit()) {
			return gesuch.getGesuchsteller1().extractFullName();
		}
		// bei verheiratet haben wir nur eine finSit. Als Titel brauchen wir aber beide Antragstellenden
		if (isVerheiratet()) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			return  gesuch.getGesuchsteller1().extractFullName() + " \n& " + gesuch.getGesuchsteller2().extractFullName();
		}
		// alleinerziehende person. nur eine finSit.
		return gesuch.getGesuchsteller1().extractFullName();
	}

	private void createMassgebendesEinkommenTableForGesuchsteller(
		@Nonnull Document document,
		@Nonnull FinanzielleSituation finanzielleSituationJA,
		@Nonnull FinanzielleSituation finanzielleSituationGS,
		@Nonnull String gesuchstellerName,
		@Nonnull Integer gesuchstellerNumber
	) {
		if (finanzielleSituationRechner.calculateByVeranlagung(finanzielleSituationJA)) {
			createTablesDeklarationByVeranlagung(document, finanzielleSituationJA, finanzielleSituationGS, gesuchstellerName, gesuchstellerNumber);
		} else {
			createTablesBySelbstdeklaration(document, gesuchstellerName, gesuchstellerNumber);
		}
	}

	private void createTablesDeklarationByVeranlagung(
		@Nonnull Document document,
		@Nonnull FinanzielleSituation finanzielleSituationJA,
		@Nonnull FinanzielleSituation finanzielleSituationGS,
		@Nonnull String gesuchstellerName,
		@Nonnull Integer gesuchstellerNumber
	) {
		FinanzielleSituationTable table = createFinSitTable(false);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(BERECHNUNG_GEMAESS_VERANLAGUNG, mandant), gesuchstellerName);

		FinanzielleSituationRow steuerbaresEinkommen = createRow(
			translate(STEUERBARES_EINKOMMEN, mandant),
			FinanzielleSituation::getSteuerbaresEinkommen,
			finanzielleSituationJA,
			finanzielleSituationGS
		);

		FinanzielleSituationRow steuerbaresVermoegen = createRow(
			translate(STEUERBARES_VERMOEGEN, mandant),
			FinanzielleSituation::getSteuerbaresVermoegen,
			finanzielleSituationJA,
			finanzielleSituationGS
		);

		FinanzielleSituationRow nettoLiegenschaften = createRow(
			translate(NETTOEINKUENFTE_LIEGENSCHAFTEN, mandant),
			FinanzielleSituation::getAbzuegeLiegenschaft,
			finanzielleSituationJA,
			finanzielleSituationGS
		);

		FinanzielleSituationRow verrechenbareGeschaeftsverluste = createRow(
			translate(VERRECHENBARE_GESCHAEFTSVERLUSTE, mandant),
			FinanzielleSituation::getGeschaeftsverlust,
			finanzielleSituationJA,
			finanzielleSituationGS
		);

		FinanzielleSituationRow einkaeufeVorsorge = createRow(
			translate(EINKAEUFE_VORSORGE, mandant),
			FinanzielleSituation::getEinkaeufeVorsorge,
			finanzielleSituationJA,
			finanzielleSituationGS
		);

		Objects.requireNonNull(finanzDatenDTO);
		var massgebendesEinkommen =  finanzDatenDTO.getMassgebendesEinkVorAbzFamGr(gesuchstellerNumber);
		FinanzielleSituationRow massgebendesEinkommenRow = new FinanzielleSituationRow(
			translate(MASSG_EINK, mandant), massgebendesEinkommen);

		table.addRow(title);
		table.addRow(steuerbaresEinkommen);
		table.addRow(steuerbaresVermoegen);
		table.addRow(nettoLiegenschaften);
		table.addRow(verrechenbareGeschaeftsverluste);
		table.addRow(einkaeufeVorsorge);
		table.addRow(massgebendesEinkommenRow);

		document.add(table.createTable());
	}

	private void createTablesBySelbstdeklaration(
		@Nonnull Document document,
		@Nonnull String gesuchstellerName,
		@Nonnull Integer gesuchstellerNumber
	) {

		Objects.requireNonNull(finanzDatenDTO);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(BERECHNUNG_GEMAESS_SELBSTDEKLARATION), gesuchstellerName);

		document.add(createTableSelbstdeklaration(false, gesuchstellerNumber, finanzDatenDTO, title));
	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		Objects.requireNonNull(ekvBasisJahrPlus1);
		createPageEkv(ekvBasisJahrPlus1, gesuch.getGesuchsperiode().getBasisJahrPlus1(), document, true);
	}


	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		Objects.requireNonNull(ekvBasisJahrPlus2);
		//EKV2 only needs to be printed on new page if there is no ekv1
		boolean isPrintOnNewPage =  ekvBasisJahrPlus1 == null;
		createPageEkv(ekvBasisJahrPlus2, gesuch.getGesuchsperiode().getBasisJahrPlus2(), document, isPrintOnNewPage);
	}

	private void createPageEkv(@Nonnull FinanzielleSituationResultateDTO finSitDTO, int basisJahr, @Nonnull Document document, boolean isPrintOnNewPage) {
		if(isPrintOnNewPage) {
			document.newPage();
			document.add(createTitleEkv(null));
			document.add(createIntroEkv());
		} else {
			addSpacing(document);
		}

		FinanzielleSituationRow title = createTableTitleForEkv(basisJahr);
		//EKV Tabelle ist dieselbe wie die Selbstdeklarations-Tabelle
		document.add(createTableSelbstdeklaration(hasSecondGesuchsteller, 1, finSitDTO, title));
	}

	private PdfPTable createTableSelbstdeklaration(
		boolean hasSecondGS,
		int gesuchstellerNumberValue1,
		@Nonnull FinanzielleSituationResultateDTO finSitDTO,
		@Nullable FinanzielleSituationRow titleRow) {

		FinanzielleSituationTable table = createFinSitTable(hasSecondGS);

		if (titleRow != null) {
			table.addRow(titleRow);
		}

		table.addRow(createRow(TOTAL_EINKUENFTE, finSitDTO.getEinkommen(gesuchstellerNumberValue1), hasSecondGS, finSitDTO.getEinkommenGS2()));
		table.addRow(createRow(TOTAL_ABZUEGE, finSitDTO.getAbzuege(gesuchstellerNumberValue1), hasSecondGS, finSitDTO.getAbzuegeGS2()));
		table.addRow(createRow(ANRECHENBARES_VERMOEGEN_GEMAESS_SELBSTDEKLARATION, finSitDTO.getVermoegenXPercentAnrechenbar(gesuchstellerNumberValue1), hasSecondGS, finSitDTO.getVermoegenXPercentAnrechenbarGS2()));
		table.addRow(createRow(MASSG_EINK, finSitDTO.getMassgebendesEinkVorAbzFamGr(gesuchstellerNumberValue1), hasSecondGS, finSitDTO.getMassgebendesEinkVorAbzFamGrGS2()));

		return table.createTable();
	}

	private void addSpacing(@Nonnull Document document) {
		Paragraph p = new Paragraph();
		p.setSpacingAfter(15);
		document.add(p);
	}

	private boolean gesuchHasTwoFinSit() {
		return gesuch.getGesuchsteller2() != null
			&& !isVerheiratet();
	}

	private boolean isVerheiratet() {
		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		return gesuch.getFamiliensituationContainer().getFamiliensituationJA().getFamilienstatus() == EnumFamilienstatus.VERHEIRATET;
	}
}
