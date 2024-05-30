/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator.finanzielleSituation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.familiensituation.FamiliensituationUtil;
import ch.dvbern.ebegu.finanziellesituation.AbstractFinanzielleSituationContainer;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.pdfgenerator.TableRowLabelValue;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

import static ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationUtil.findEinkommensverschlechterung;
import static ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationUtil.findFinanzielleSituation;
import static ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationUtil.requireFinanzielleSituation;
import static ch.dvbern.ebegu.pdfgenerator.finanzielleSituation.MassgebendesEinkommenColumn.column;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

public class FinanzielleSituationPdfGeneratorSchwyz extends FinanzielleSituationPdfGenerator {

	private static final String GESUCHSPERIODE = "PdfGeneration_FinSit_Gesuchsperiode";

	private static final String EIKOMMEN_TITLE = "PdfGeneration_FinSit_EinkommenTitle";
	private static final String STEUERBARES_EINKOMMEN = "PdfGeneration_FinSit_SteuerbaresEinkommen";
	private static final String EINKAEUFE_VORSORGE = "PdfGeneration_FinSit_EinkaeufeVorsorge";

	private static final String ABZUEGE_TITLE = "PdfGeneration_FinSit_Abzuege";
	private static final String ABZUEGE_LIEGENSCHAFTEN = "PdfGeneration_FinSit_AbzuegeLiegenschaften";
	private static final String ABZUEGE_BRUTTOPAUSCHALE = "PdfGeneration_FinSit_AbzuegeBruttopauschale";

	private static final String VERMOEGEN_TITLE = "PdfGeneration_FinSit_VermoegenTitle";
	private static final String STEUERBARES_VERMOEGEN = "PdfGeneration_FinSit_SteuerbaresVermoegen";
	private static final String FOOTER_STEUERBARES_VERMOEGEN = "PdfGeneration_FinSit_FooterSteuerbaresVermoegen";

	private static final String BRUTTOLOHN = "PdfGeneration_FinSit_Bruttolohn";

	private static final String SOZIALABZUG = "PdfGeneration_MassgEinkommen_Sozialabzug";
	private static final String ANSBRUCHBERECHTIGTES_EINKOMMEN = "PdfGeneration_MassgEinkommen_AnsbruchberechtigtesEinkommen";

	private final List<String> footers = new ArrayList<>();

	public FinanzielleSituationPdfGeneratorSchwyz(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung verfuegungFuerMassgEinkommen,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull LocalDate erstesEinreichungsdatum
	) {
		super(gesuch, verfuegungFuerMassgEinkommen, stammdaten, erstesEinreichungsdatum);
	}

	@Override
	protected void initializeValues() {
		boolean hasFinSitGS2 = findFinanzielleSituation(gesuch.getGesuchsteller2()).isPresent();

		finanzDatenDTO = finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, hasFinSitGS2);
		initialzeEkv();
	}

	@Override
	@Nonnull
	protected PdfPTable createIntroBasisjahr() {
		List<TableRowLabelValue> introBasisjahr = List.of(
			new TableRowLabelValue(REFERENZNUMMER, gesuch.getJahrFallAndGemeindenummer()),
			new TableRowLabelValue(GESUCHSPERIODE, gesuch.getGesuchsperiode().getGesuchsperiodeString())
		);
		return PdfUtil.createIntroTable(introBasisjahr, sprache, mandant);
	}

	@Override
	protected void createPageBasisJahr(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		var gesuchsteller1 = requireNonNull(gesuch.getGesuchsteller1());
		var finSit1 = requireFinanzielleSituation(gesuchsteller1);

		final boolean gemeinsameSteuererklaerung = FamiliensituationUtil.isGemeinsameSteuererklaerung(gesuch);
		String name = gemeinsameSteuererklaerung ?
			bothNames() :
			gesuchsteller1.extractFullName();

		BigDecimal massgebendesEinkommen = requireNonNull(finanzDatenDTO).getMassgebendesEinkVorAbzFamGrGS1();

		var tablesGs1 =
			createMassgebendesEinkommenTableForGesuchsteller(finSit1, name, massgebendesEinkommen, isQuellenbesteuert(finSit1));

		var tablesGs2 = Optional.ofNullable(gesuch.getGesuchsteller2())
			.filter(gesuchstellerContainer -> !gemeinsameSteuererklaerung)
			.flatMap(gesuchsteller2 -> findFinanzielleSituation(gesuchsteller2)
				.map(finSit2 -> createMassgebendesEinkommenTableForGesuchsteller(
					finSit2,
					gesuchsteller2.extractFullName(),
					finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS2(),
					isQuellenbesteuert(finSit2)))
			)
			.orElseGet(Collections::emptyList);

		document.add(createIntroBasisjahr());
		tablesGs1.forEach(document::add);

		if (!tablesGs2.isEmpty()) {
			document.add(createSpacing());
			tablesGs2.forEach(document::add);
			document.add(createSpacing());
			document.add(createTableZusammenzug(finanzDatenDTO));
		}

		printFooters(generator);
	}

	private void printFooters(@Nonnull PdfGenerator generator) {
		var translatedFooters = footers.stream()
			.map(this::translate)
			.collect(Collectors.toList());

		createFusszeile(generator.getDirectContent(), translatedFooters);
	}

	@Nonnull
	private Boolean isQuellenbesteuert(FinanzielleSituationContainer finSit) {
		return requireNonNullElse(requireNonNull(finSit.getFinanzielleSituationJA()).getQuellenbesteuert(), false);
	}

	private <T extends AbstractFinanzielleSituation> List<PdfPTable> createMassgebendesEinkommenTableForGesuchsteller(
		@Nonnull AbstractFinanzielleSituationContainer<T> finSit,
		@Nonnull String gesuchstellerName,
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean isQuellenbesteuert
	) {
		return isQuellenbesteuert
			? createTableDeklarationByBruttolohn(finSit, gesuchstellerName, massgebendesEinkommen)
			: createTablesDeklarationByVeranlagung(finSit, gesuchstellerName, massgebendesEinkommen);
	}

	private <T extends AbstractFinanzielleSituation> List<PdfPTable> createTablesDeklarationByVeranlagung(
		@Nonnull AbstractFinanzielleSituationContainer<T> finSit,
		@Nonnull String gesuchstellerName,
		@Nonnull BigDecimal massgebendesEinkommen
	) {
		var einkommenTable = createFinSitTableSingleGS(
			createRow(translate(EIKOMMEN_TITLE), gesuchstellerName),
			createRow(translate(STEUERBARES_EINKOMMEN), T::getSteuerbaresEinkommen, finSit),
			createRow(translate(EINKAEUFE_VORSORGE), T::getEinkaeufeVorsorge, finSit)
		);
		var abzuegeTable = createFinSitTableSingleGS(
			createRow(translate(ABZUEGE_TITLE)),
			createRow(translate(ABZUEGE_LIEGENSCHAFTEN), T::getAbzuegeLiegenschaft, finSit)
		);
		var vermoegenTable = createFinSitTableSingleGS(
			createRow(translate(VERMOEGEN_TITLE)),
			createRow(translate(STEUERBARES_VERMOEGEN), T::getSteuerbaresVermoegen, finSit)
				.withFooter(FOOTER_STEUERBARES_VERMOEGEN, footers),
			createRow(translate(MASSG_EINK), printCHF(massgebendesEinkommen))
				.bold()
		);

		return List.of(einkommenTable, abzuegeTable, vermoegenTable);
	}

	private <T extends AbstractFinanzielleSituation> List<PdfPTable> createTableDeklarationByBruttolohn(
		@Nonnull AbstractFinanzielleSituationContainer<T> finSit,
		@Nonnull String gesuchstellerName,
		@Nonnull BigDecimal massgebendesEinkommen
	) {
		// see ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationSchwyzRechner.calcBruttopauschale
		// it's Schwyz specific and not stored on FinanzielleSituationResultateDTO. Chose to not increase the public properties,
		// since FinanzielleSituationResultateDTO is also a REST API model.
		BigDecimal bruttopauschale = MathUtil.positiveNonNullAndRound(requireNonNull(finSit.getFinSitJA()).getBruttoLohn())
			.subtract(massgebendesEinkommen);

		var einkommenTable = createFinSitTableSingleGS(
			createRow(translate(EIKOMMEN_TITLE), gesuchstellerName),
			createRow(translate(BRUTTOLOHN), T::getBruttoLohn, finSit)
		);

		var abzuegeTable = createFinSitTableSingleGS(
			createRow(translate(ABZUEGE_TITLE)),
			createRow(translate(ABZUEGE_BRUTTOPAUSCHALE), printCHF(bruttopauschale)),
			createRow(translate(MASSG_EINK), printCHF(massgebendesEinkommen))
				.bold()
		);

		return List.of(einkommenTable, abzuegeTable);
	}

	@Nonnull
	private Paragraph createSpacing() {
		Paragraph p = new Paragraph();
		p.setSpacingAfter(15);

		return p;
	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		createPageEkv(generator, document, 1);
	}

	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		createPageEkv(generator, document, 2);
	}

	private void createPageEkv(@Nonnull PdfGenerator generator, @Nonnull Document document, int jahrOffset) {
		GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		FinanzielleSituationResultateDTO resultateDTO = requireNonNull(jahrOffset == 1 ? ekvBasisJahrPlus1 : ekvBasisJahrPlus2);

		final boolean gemeinsameSteuererklaerung = FamiliensituationUtil.isGemeinsameSteuererklaerung(gesuch);
		String name = gemeinsameSteuererklaerung ?
			bothNames() :
			requireNonNull(gesuchsteller1).extractFullName();

		var tablesGS1 = findEinkommensverschlechterung(gesuchsteller1, jahrOffset)
			.map(ekv1 -> createMassgebendesEinkommenTableForGesuchsteller(
				ekv1,
				name,
				resultateDTO.getMassgebendesEinkVorAbzFamGrGS1(),
				isQuellenbesteuert(requireFinanzielleSituation(gesuchsteller1)))
			)
			.orElseGet(Collections::emptyList);

		GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();
		var tablesGS2 = findEinkommensverschlechterung(gesuchsteller2, jahrOffset)
			.filter(gesuchstellerCont -> !gemeinsameSteuererklaerung)
			.map(ekv2 -> createMassgebendesEinkommenTableForGesuchsteller(
				ekv2,
				requireNonNull(gesuchsteller2).extractFullName(),
				resultateDTO.getMassgebendesEinkVorAbzFamGrGS2(),
				isQuellenbesteuert(requireFinanzielleSituation(gesuchsteller2)))
			)
			.orElseGet(Collections::emptyList);

		if (!tablesGS1.isEmpty() || !tablesGS2.isEmpty()) {
			document.newPage();
			document.add(createTitleEkv());
			document.add(createIntroEkv());
			tablesGS1.forEach(document::add);
			if (!tablesGS1.isEmpty()) {
				document.add(createSpacing());
			}
			tablesGS2.forEach(document::add);
			if (!tablesGS2.isEmpty()) {
				document.add(createSpacing());
				document.add(createTableZusammenzug(resultateDTO));
			}
			printFooters(generator);
		}
	}

	@Override
	protected MassgebendesEinkommenTabelleConfig getMassgebendesEinkommenConfig() {
		return MassgebendesEinkommenTabelleConfig.of(
			PageSize.A4,
			column(5, translate(VON), a -> Constants.DATE_FORMATTER.format(a.getGueltigkeit().getGueltigAb())),
			column(5, translate(BIS), a -> Constants.DATE_FORMATTER.format(a.getGueltigkeit().getGueltigBis())),
			column(10, translate(MASSG_EINK), a -> printCHF(a.getMassgebendesEinkommenVorAbzFamgr())),
			column(10, translate(SOZIALABZUG), a -> printCHF(a.getAbzugFamGroesse())),
			column(10, translate(ANSBRUCHBERECHTIGTES_EINKOMMEN), a -> printCHF(a.getMassgebendesEinkommen()))
		);
	}
}
