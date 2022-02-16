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
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.invoicegenerator.pdf.PdfGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;

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

	protected void initializeValues() {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		basisJahrGS1 = gesuch.getGesuchsteller1().getFinanzielleSituationContainer();

		Objects.requireNonNull(gesuch.getGesuchsteller1().getGesuchstellerJA());
		gs1 = gesuch.getGesuchsteller1().getGesuchstellerJA();

		boolean hasSecondGS = false;
		if (gesuchHasTwoFinSit()) {
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			basisJahrGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer();
			Objects.requireNonNull(gesuch.getGesuchsteller2().getGesuchstellerJA());
			gs2 = gesuch.getGesuchsteller2().getGesuchstellerJA();
			hasSecondGS = true;
		}

		finanzDatenDTO = finanzielleSituationRechner.calculateResultateFinanzielleSituation(gesuch, hasSecondGS);
	}

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
		FinanzielleSituationTable table =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				true);

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
		var massgebendesEinkommen =  gesuchstellerNumber == 1
			? finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS1()
			: finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS2();
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

		FinanzielleSituationTable table =
			new FinanzielleSituationTable(
				getPageConfiguration(),
				false,
				EbeguUtil.isKorrekturmodusGemeinde(gesuch),
				true);

		FinanzielleSituationRow title = new FinanzielleSituationRow(
			translate(BERECHNUNG_GEMAESS_SELBSTDEKLARATION, mandant), gesuchstellerName);

		var einkuenfte =  gesuchstellerNumber == 1
			? finanzDatenDTO.getEinkommenGS1()
			: finanzDatenDTO.getEinkommenGS2();
		FinanzielleSituationRow einkuenfteRow = new FinanzielleSituationRow(
			translate(TOTAL_EINKUENFTE, mandant), einkuenfte);

		var abzuege =  gesuchstellerNumber == 1
			? finanzDatenDTO.getAbzuegeGS1()
			: finanzDatenDTO.getAbzuegeGS2();
		FinanzielleSituationRow abzuegeRow = new FinanzielleSituationRow(
			translate(TOTAL_ABZUEGE, mandant), abzuege);

		var vermoegen =  gesuchstellerNumber == 1
			? finanzDatenDTO.getVermoegenXPercentAnrechenbarGS1()
			: finanzDatenDTO.getVermoegenXPercentAnrrechenbarGS2();
		FinanzielleSituationRow vermoegenRow = new FinanzielleSituationRow(
			translate(ANRECHENBARES_VERMOEGEN_GEMAESS_SELBSTDEKLARATION, mandant), vermoegen);

		var massgebendesEinkommen =  gesuchstellerNumber == 1
			? finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS1()
			: finanzDatenDTO.getMassgebendesEinkVorAbzFamGrGS2();
		FinanzielleSituationRow massgebendesEinkommenRow = new FinanzielleSituationRow(
			translate(MASSG_EINK, mandant), massgebendesEinkommen);

		table.addRow(title);
		table.addRow(einkuenfteRow);
		table.addRow(abzuegeRow);
		table.addRow(vermoegenRow);
		table.addRow(massgebendesEinkommenRow);

		document.add(table.createTable());

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

	private void addSpacing(@Nonnull Document document) {
		Paragraph p = new Paragraph();
		p.setSpacingAfter(15);
		document.add(p);
	}

	@Override
	protected void createPageEkv1(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		// TODO: implement
	}

	@Override
	protected void createPageEkv2(@Nonnull PdfGenerator generator, @Nonnull Document document) {
		// TODO: implement
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
