/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschuleZeitabschnitt;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.ebegu.util.AnmeldungTagesschuleZeitabschnittUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_FONT_SIZE;
import static ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities.DEFAULT_MULTIPLIED_LEADING;

public class AnmeldebestaetigungTSPDFGenerator extends DokumentAnFamilieGenerator {

	private static final String ANMELDUNG_BESTAETIGUNG_PFAD = "PdfGeneration_AnmeldungBestaetigung_";
	private static final String ANMELDUNG_BESTAETIGUNG_TITLE = "PdfGeneration_AnmeldungBestaetigung_Title";
	private static final String KIND_NAME = "PdfGeneration_AnmeldungBestaetigung_KindName";
	private static final String EINTRITTSDATUM = "PdfGeneration_AnmeldungBestaetigung_Eintrittsdatum";
	private static final String ANGEBOT = "PdfGeneration_AnmeldungBestaetigung_Angebot";
	private static final String ANGEBOT_TAGESSCHULE = "PdfGeneration_AnmeldungBestaetigung_AngebotTagesschule";
	private static final String KLASSE = "PdfGeneration_AnmeldungBestaetigung_Klasse";
	private static final String INSTITUTION = "PdfGeneration_AnmeldungBestaetigung_institution";
	private static final String BEMERKUNG = "PdfGeneration_AnmeldungBestaetigung_bemerkung";
	private static final String ANMELDUNG_BESTAETIGUNG_INTRO_MODULE =
		"PdfGeneration_AnmeldungBestaetigung_IntroModule";
	private static final String BESTAETIGUNG_OHNE_TARIF = "PdfGeneration_AnmeldungBestaetigung_ohneTarif";
	private static final String ABT_BILDUNG_BERN = "PdfGeneration_AnmeldungBestaetigung_AbteilungBildungBern";
	private static final String GEBUEHREN_MIT_PEDAGOGISCHER_BETREUUNG =
		"PdfGeneration_AnmeldungBestaetigung_gebuehrenMit";
	private static final String GEBUEHREN_OHNE_PEDAGOGISCHER_BETREUUNG =
		"PdfGeneration_AnmeldungBestaetigung_gebuehrenOhne";
	private static final String TEILANGEBOT = "PdfGeneration_AnmeldungBestaetigung_Teilangebot";
	private static final String ZEIT = "PdfGeneration_AnmeldungBestaetigung_Zeit";
	private static final String MONTAG = "PdfGeneration_Montag";
	private static final String DIENSTAG = "PdfGeneration_Dienstag";
	private static final String MITTWOCH = "PdfGeneration_Mittwoch";
	private static final String DONNERSTAG = "PdfGeneration_Donnerstag";
	private static final String FREITAG = "PdfGeneration_Freitag";

	private static final String VON = "PdfGeneration_AnmeldungBestaetigung_Von";
	private static final String BIS = "PdfGeneration_AnmeldungBestaetigung_Bis";
	private static final String BETREUNGSSTUNDEN_PRO_WOCHE = "PdfGeneration_AnmeldungBestaetigung_BetreuungsProWoche";
	private static final String GEBUEHR_PRO_STUNDE = "PdfGeneration_AnmeldungBestaetigung_GebuehrProStunde";
	private static final String VERPFLEGUNGSKOSTEN_PRO_WOCHE =
		"PdfGeneration_AnmeldungBestaetigung_VerplfegungsProWoche";
	private static final String TOTAL_PRO_WOCHE = "PdfGeneration_AnmeldungBestaetigung_TotalProWoche";
	private static final String ERSTE_RECHNUND_AUGUST = "PdfGeneration_AnmeldungBestaetigung_ErsteRechnungAugust";
	private static final String NICHT_EINVERSTANDEN_INFO =
		"PdfGeneration_AnmeldungBestaetigung_NichtEinverstandenInfo";
	private static final String CHF = "CHF ";

	public enum Art {
		OHNE_TARIF,
		MIT_TARIF
	}

	@Nonnull
	private final Art art;
	private final AnmeldungTagesschule anmeldungTagesschule;

	public AnmeldebestaetigungTSPDFGenerator(@Nonnull Gesuch gesuch, @Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, AnmeldungTagesschule anmeldungTagesschule) {
		super(gesuch, stammdaten);
		this.art = art;
		this.anmeldungTagesschule = anmeldungTagesschule;
	}

	@Nonnull
	@Override
	protected String getDocumentTitle() {
		return translate(ANMELDUNG_BESTAETIGUNG_TITLE);
	}

	@Nonnull
	@Override
	protected CustomGenerator getCustomGenerator() {
		return (generator, ctx) -> {
			Document document = generator.getDocument();
			document.add(createKindTSAnmeldungTable());
			document.add(PdfUtil.createParagraph(translate(ANMELDUNG_BESTAETIGUNG_INTRO_MODULE)));
			document.add(createBetreuungsangeboteTable());
			//Bemerkung
			if (anmeldungTagesschule.getBelegungTagesschule() != null &&
				StringUtils.isNotEmpty(anmeldungTagesschule.getBelegungTagesschule().getBemerkung())) {
				Paragraph bemerkungTitleParagraph = new Paragraph();
				bemerkungTitleParagraph.add(new Phrase(translate(BEMERKUNG), getPageConfiguration().getFont()));
				bemerkungTitleParagraph.setSpacingAfter(0);
				Paragraph bemerkungParagraph = new Paragraph();
				bemerkungParagraph.add(new Phrase("- " + anmeldungTagesschule.getBelegungTagesschule()
					.getBemerkung(), getPageConfiguration().getFont()));
				bemerkungParagraph.setSpacingAfter(PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
				document.add(bemerkungTitleParagraph);
				document.add(bemerkungParagraph);
			}
			List<Element> bestaetigungUndGruesseElements = Lists.newArrayList();
			if (art == Art.OHNE_TARIF) {
				Paragraph bestaetigungOhneTarifParagraph = new Paragraph();
				bestaetigungOhneTarifParagraph.setSpacingAfter(2 * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
				bestaetigungOhneTarifParagraph.add(new Phrase(translate(BESTAETIGUNG_OHNE_TARIF),
					getPageConfiguration().getFont()));
				bestaetigungUndGruesseElements.add(bestaetigungOhneTarifParagraph);
			} else {

				Verfuegung verfuegung = anmeldungTagesschule.getVerfuegungOrVerfuegungPreview();

				// TODO (hefr) die neue Berechnung; Später nur diese und Objects.requireNonNull(verfuegung);
				if (verfuegung != null) {
					Objects.requireNonNull(verfuegung);

					document.add(new Paragraph("NEUE BERECHNUNG"));
					createGebuehrenTabelle(verfuegung, document);
				}

				// TODO (hefr) delete
				berechnungBisher(document, bestaetigungUndGruesseElements);
			}
			bestaetigungUndGruesseElements.add(createParagraphGruss());
			Paragraph bernAmtParagraph = new Paragraph();
			bernAmtParagraph.add(new Phrase(translate(ABT_BILDUNG_BERN), getPageConfiguration().getFont()));
			bestaetigungUndGruesseElements.add(bernAmtParagraph);
			document.add(PdfUtil.createKeepTogetherTable(bestaetigungUndGruesseElements, 2, 0));
		};
	}

	private void createGebuehrenTabelle(@Nonnull Verfuegung verfuegung, @Nonnull Document document) {
		List<VerfuegungZeitabschnitt> abschnitteMitBetreuung =
			verfuegung.getZeitabschnitte()
				.stream()
				.filter(verfuegungZeitabschnitt ->
					verfuegungZeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung() != null)
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(abschnitteMitBetreuung)) {
			document.add(createGebuehrTabelleTitle(true, false));
			PdfPTable gebuehrenTable = createGebuehrenTableHeader();
			fillGebuehrenTable(gebuehrenTable, abschnitteMitBetreuung, true);
			document.add(gebuehrenTable);
		}

		List<VerfuegungZeitabschnitt> abschnitteOhneBetreuung =
			verfuegung.getZeitabschnitte()
				.stream()
				.filter(verfuegungZeitabschnitt ->
					verfuegungZeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung() != null)
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(abschnitteOhneBetreuung)) {
			document.add(createGebuehrTabelleTitle(false, false));
			PdfPTable gebuehrenTable = createGebuehrenTableHeader();
			fillGebuehrenTable(gebuehrenTable, abschnitteOhneBetreuung, false);
			document.add(gebuehrenTable);
		}
	}

	private void berechnungBisher(@Nonnull Document document, @Nonnull List<Element> bestaetigungUndGruesseElements) {
		document.add(new Paragraph("ALTE BERECHNUNG"));

		boolean hasZeitAbschnittMitPadagogicherBetreuung =
			AnmeldungTagesschuleZeitabschnittUtil.hasZeitabschnittMitPedagogischerBetreuung(anmeldungTagesschule);
		if (hasZeitAbschnittMitPadagogicherBetreuung) {
			document.add(createGebuehrTabelleTitle(true, false));
			PdfPTable gebuehrenTable = createGebuehrenTableHeader();
			fillGebuehrenTable(gebuehrenTable, true);
			document.add(gebuehrenTable);
		}
		if (AnmeldungTagesschuleZeitabschnittUtil.hasZeitabschnittOhnePedagogischeBetreuung(anmeldungTagesschule)) {
			document.add(createGebuehrTabelleTitle(false, AnmeldungTagesschuleZeitabschnittUtil.hasZeitabschnittMitPedagogischerBetreuung(anmeldungTagesschule)));
			PdfPTable gebuehrenTableOhnePedagogischeBetreuung = createGebuehrenTableHeader();
			fillGebuehrenTable(gebuehrenTableOhnePedagogischeBetreuung, hasZeitAbschnittMitPadagogicherBetreuung);
			document.add(gebuehrenTableOhnePedagogischeBetreuung);
		}
		Paragraph endCommunicationTitle = new Paragraph();
		endCommunicationTitle.add(new Phrase(translate(ERSTE_RECHNUND_AUGUST),
			getPageConfiguration().getFontBold()));
		endCommunicationTitle.setSpacingBefore(PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		endCommunicationTitle.setSpacingAfter(PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		document.add(endCommunicationTitle);
		Paragraph endCommunication = new Paragraph();
		endCommunication.add(new Phrase(translate(NICHT_EINVERSTANDEN_INFO),
			getPageConfiguration().getFont()));
		endCommunication.setSpacingAfter(2 * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		bestaetigungUndGruesseElements.add(endCommunication);
	}
	// todo homa review kibon-1016 folgendes ist weg
//	bestaetigungUndGruesseElements.add(createParagraphGruss());
//	Paragraph bernAmtParagraph = new Paragraph();
//	bernAmtParagraph.add(new Phrase(translate(ABT_BILDUNG_BERN), getPageConfiguration().getFont()));
//	bestaetigungUndGruesseElements.add(bernAmtParagraph);
//	document.add(PdfUtil.createKeepTogetherTable(bestaetigungUndGruesseElements, 2, 0));
//};


	private Paragraph createGebuehrTabelleTitle(boolean pedagogischerBetreut, boolean setSpacingBefore){
		Paragraph gebuhren = new Paragraph();
		gebuhren.add(new Phrase(pedagogischerBetreut ? translate(GEBUEHREN_MIT_PEDAGOGISCHER_BETREUUNG) :
			translate(GEBUEHREN_OHNE_PEDAGOGISCHER_BETREUUNG),
			getPageConfiguration().getFont()));
		if(setSpacingBefore){
			gebuhren.setSpacingBefore(PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		}
		gebuhren.setSpacingAfter(PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		return gebuhren;
	}

	@Nonnull
	private PdfPTable createKindTSAnmeldungTable() {
		PdfPTable table = new PdfPTable(4);
		// Init
		PdfUtil.setTableDefaultStyles(table);
		table.getDefaultCell().setPaddingBottom(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		//Row: Referenznummer + Institution
		table.addCell(new Phrase(translate(REFERENZNUMMER), getPageConfiguration().getFont()));
		table.addCell(new Phrase(getGesuch().getJahrFallAndGemeindenummer(), getPageConfiguration().getFont()));
		table.addCell(new Phrase(translate(INSTITUTION), getPageConfiguration().getFont()));
		table.addCell(new Phrase(anmeldungTagesschule.getInstitutionStammdaten().getInstitution().getName(),
			getPageConfiguration().getFont()));

		Kind kind = anmeldungTagesschule.getKind().getKindJA();
		// Row: Name + Eintrittsdatum
		table.addCell(new Phrase(translate(KIND_NAME), getPageConfiguration().getFont()));
		table.addCell(new Phrase(kind.getFullName(), getPageConfiguration().getFont()));
		table.addCell(new Phrase(translate(EINTRITTSDATUM), getPageConfiguration().getFont()));
		assert anmeldungTagesschule.getBelegungTagesschule() != null;
		table.addCell(new Phrase(Constants.DATE_FORMATTER.format(anmeldungTagesschule.getBelegungTagesschule().getEintrittsdatum()),
			getPageConfiguration().getFont()));
		// Row: Angebot + Klasse
		table.addCell(new Phrase(translate(ANGEBOT), getPageConfiguration().getFont()));
		table.addCell(new Phrase(translate(ANGEBOT_TAGESSCHULE), getPageConfiguration().getFont()));
		table.addCell(new Phrase(translate(KLASSE), getPageConfiguration().getFont()));
		EinschulungTyp einschulungTyp = kind.getEinschulungTyp();
		if (einschulungTyp != null) {
			String einschulungString = einschulungTyp.name();
			table.addCell(new Phrase(translate(ANMELDUNG_BESTAETIGUNG_PFAD + einschulungString),
				getPageConfiguration().getFont()));
		} else {
			table.addCell(new Phrase("", getPageConfiguration().getFont()));
		}
		return table;
	}

	@Nonnull
	private PdfPTable createBetreuungsangeboteTable() throws DocumentException {
		PdfPTable table = new PdfPTable(7);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setWidths(new int[] { 30, 30, 20, 20, 20, 20, 20 });
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(PdfUtil.createTitleCell(translate(TEILANGEBOT)));
		table.addCell(PdfUtil.createTitleCell(translate(ZEIT)));
		table.addCell(createCell(Element.ALIGN_CENTER, translate(MONTAG), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_CENTER, translate(DIENSTAG), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_CENTER, translate(MITTWOCH), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_CENTER, translate(DONNERSTAG), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_CENTER, translate(FREITAG), Color.LIGHT_GRAY));

		if (anmeldungTagesschule.getBelegungTagesschule() != null && anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule() != null) {
			Map<String, List<BelegungTagesschuleModul>> tagesschuleModuleMap = new HashMap();

			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().forEach(belegungTagesschuleModul -> {
				List<BelegungTagesschuleModul> modulTagesschuleList =
					tagesschuleModuleMap.get(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId());
				if (modulTagesschuleList == null) {
					modulTagesschuleList = new ArrayList();
				}
				modulTagesschuleList.add(belegungTagesschuleModul);
				tagesschuleModuleMap.put(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId(), modulTagesschuleList);
			});

			tagesschuleModuleMap.forEach((k, v) -> {
				boolean monday = false;
				boolean tuesday = false;
				boolean wednesday = false;
				boolean thursday = false;
				boolean friday = false;
				boolean mondayAlleZweiWoche = false;
				boolean tuesdayAlleZweiWoche = false;
				boolean wednesdayAlleZweiWoche = false;
				boolean thursdayAlleZweiWoche = false;
				boolean fridayAlleZweiWoche = false;
				ModulTagesschuleGroup mtg = null;
				for (BelegungTagesschuleModul btm : v) {
					if (mtg == null) {
						mtg = btm.getModulTagesschule().getModulTagesschuleGroup();
					}
					int day = btm.getModulTagesschule().getWochentag().getValue();
					switch (day) {
					case 1:
						monday = true;
						if(btm.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN){
							mondayAlleZweiWoche = true;
						}
						break;
					case 2:
						tuesday = true;
						if(btm.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN){
							tuesdayAlleZweiWoche = true;
						}
						break;
					case 3:
						wednesday = true;
						if(btm.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN){
							wednesdayAlleZweiWoche = true;
						}
						break;
					case 4:
						thursday = true;
						if(btm.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN){
							thursdayAlleZweiWoche = true;
						}
						break;
					case 5:
						friday = true;
						if(btm.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN){
							fridayAlleZweiWoche = true;
						}
						break;
					}
				}
				assert mtg != null;
				boolean isFrench = "fr".equalsIgnoreCase(sprache.getLanguage());
				table.addCell(new Phrase(isFrench ? mtg.getBezeichnung().getTextFranzoesisch() :
					mtg.getBezeichnung().getTextDeutsch(),
					getPageConfiguration().getFont()));
				table.addCell(new Phrase(mtg.getZeitVon().format(Constants.HOURS_FORMAT) + '-' + mtg.getZeitBis().format(Constants.HOURS_FORMAT), getPageConfiguration().getFont()));
				table.addCell(getCellForDay(monday, mondayAlleZweiWoche));
				table.addCell(getCellForDay(tuesday, tuesdayAlleZweiWoche));
				table.addCell(getCellForDay(wednesday, wednesdayAlleZweiWoche));
				table.addCell(getCellForDay(thursday, thursdayAlleZweiWoche));
				table.addCell(getCellForDay(friday, fridayAlleZweiWoche));
			});
		}

		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		return table;
	}

	private PdfPTable createGebuehrenTableHeader() {
		PdfPTable table = new PdfPTable(6);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setWidths(new int[] { 30, 30, 30, 30, 30, 30 });
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(VON), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(BIS), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(BETREUNGSSTUNDEN_PRO_WOCHE), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(GEBUEHR_PRO_STUNDE), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(VERPFLEGUNGSKOSTEN_PRO_WOCHE), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(TOTAL_PRO_WOCHE), Color.LIGHT_GRAY));
		return table;
	}

	private void fillGebuehrenTable(PdfPTable table, boolean mitPedagogischerBetreuug) {
		for (AnmeldungTagesschuleZeitabschnitt anmeldungTagesschuleZeitabschnitt :
			anmeldungTagesschule.getAnmeldungTagesschuleZeitabschnitts()) {
			if (anmeldungTagesschuleZeitabschnitt.isPedagogischBetreut() == mitPedagogischerBetreuug) {
				table.addCell(createCell(Element.ALIGN_RIGHT,
					Constants.DATE_FORMATTER.format(anmeldungTagesschuleZeitabschnitt.getGueltigkeit().getGueltigAb()),
					null));
				table.addCell(createCell(Element.ALIGN_RIGHT,
					Constants.DATE_FORMATTER.format(anmeldungTagesschuleZeitabschnitt.getGueltigkeit().getGueltigBis()),
					null));
				table.addCell(createCell(Element.ALIGN_RIGHT,
					anmeldungTagesschuleZeitabschnitt.getBetreuungszeitFormatted(),
					null));
				table.addCell(createCell(Element.ALIGN_RIGHT,
					CHF + anmeldungTagesschuleZeitabschnitt.getGebuehrProStunde(), null));
				table.addCell(createCell(Element.ALIGN_RIGHT,
					CHF + anmeldungTagesschuleZeitabschnitt.getVerpflegungskosten(),
					null));
				table.addCell(createCell(Element.ALIGN_RIGHT,
					CHF + anmeldungTagesschuleZeitabschnitt.getTotalKostenProWoche(),
					null));
			}
		}
	}

	private void fillGebuehrenTable(PdfPTable table, List<VerfuegungZeitabschnitt> abschnitte, boolean mitPedagogischerBetreuug) {
		for (VerfuegungZeitabschnitt anmeldungTagesschuleZeitabschnitt : abschnitte) {
			TSCalculationResult tsResult = mitPedagogischerBetreuug ?
				anmeldungTagesschuleZeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultMitPaedagogischerBetreuung() :
				anmeldungTagesschuleZeitabschnitt.getBgCalculationResultAsiv().getTsCalculationResultOhnePaedagogischerBetreuung();
			Objects.requireNonNull(tsResult);

			table.addCell(createCell(Element.ALIGN_RIGHT,
				Constants.DATE_FORMATTER.format(anmeldungTagesschuleZeitabschnitt.getGueltigkeit().getGueltigAb()),
				null));
			table.addCell(createCell(Element.ALIGN_RIGHT,
				Constants.DATE_FORMATTER.format(anmeldungTagesschuleZeitabschnitt.getGueltigkeit().getGueltigBis()),
				null));
			table.addCell(createCell(Element.ALIGN_RIGHT,
				tsResult.getBetreuungszeitProWocheFormatted(),
				null));
			table.addCell(createCell(Element.ALIGN_RIGHT,
				CHF + PdfUtil.printBigDecimal(tsResult.getGebuehrProStunde()), null));
			table.addCell(createCell(Element.ALIGN_RIGHT,
				CHF + PdfUtil.printBigDecimal(tsResult.getVerpflegungskosten()),
				null));
			table.addCell(createCell(Element.ALIGN_RIGHT,
				CHF + PdfUtil.printBigDecimal(tsResult.getTotalKostenProWoche()),
				null));
		}
	}

	private PdfPCell getCellForDay(boolean isSelected, boolean alleZweiWochen) {
		Paragraph dayParagraph =
			new Paragraph(new Phrase(isSelected ? "X" : "", getPageConfiguration().getFont()));
		dayParagraph.setSpacingBefore(0);
		dayParagraph.setAlignment(Element.ALIGN_CENTER);
		dayParagraph.setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		PdfPCell dayCell = new PdfPCell();
		dayCell.addElement(dayParagraph);
		if(isSelected && alleZweiWochen){
			Font paragraphFont = FontFactory.getFont("Proxima Nova Light", 6.0f);
			Paragraph alleZweiWocheParagraph =
				new Paragraph(new Phrase("alle zwei Wochen", paragraphFont));
			alleZweiWocheParagraph.setAlignment(Element.ALIGN_CENTER);
			dayCell.addElement(alleZweiWocheParagraph);
		}
		return dayCell;
	}

	private PdfPCell createCell(
		int alignment,
		String value,
		@Nullable Color bgColor
	) {
		PdfPCell cell;
		cell = new PdfPCell(new Phrase(value, getPageConfiguration().getFont()));
		cell.setHorizontalAlignment(alignment);
		if (bgColor != null) {
			cell.setBackgroundColor(bgColor);
		}
		return cell;
	}
}
