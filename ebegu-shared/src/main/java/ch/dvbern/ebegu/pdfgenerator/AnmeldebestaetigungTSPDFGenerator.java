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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
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
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.google.common.collect.Lists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
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
	private static final String ALLE_2_WOCHEN = "PdfGeneration_AnmeldungBestaetigung_alle2wochen";

	private static final String VON = "PdfGeneration_AnmeldungBestaetigung_Von";
	private static final String BIS = "PdfGeneration_AnmeldungBestaetigung_Bis";
	private static final String BETREUNGSSTUNDEN_PRO_WOCHE = "PdfGeneration_AnmeldungBestaetigung_BetreuungsProWoche";
	private static final String GEBUEHR_PRO_STUNDE = "PdfGeneration_AnmeldungBestaetigung_GebuehrProStunde";
	private static final String VERPFLEGUNGSKOSTEN_PRO_WOCHE =
		"PdfGeneration_AnmeldungBestaetigung_VerplfegungsProWoche";
	private static final String VERPFLEGUNGSKOSTEN_VERGUENSTIGUNG_PRO_WOCHE =
		"PdfGeneration_AnmeldungBestaetigung_VerplfegungsVerguenstigungProWoche";
	private static final String TOTAL_PRO_WOCHE = "PdfGeneration_AnmeldungBestaetigung_TotalProWoche";
	private static final String NICHT_EINVERSTANDEN = "PdfGeneration_AnmeldungBestaetigung_NichtEinverstandenInfo";
	private static final String CHF = "CHF ";

	public enum Art {
		OHNE_TARIF,
		MIT_TARIF
	}

	@Nonnull
	private final Art art;
	private final AnmeldungTagesschule anmeldungTagesschule;
	private boolean mahlzeitenverguenstigung;

	public AnmeldebestaetigungTSPDFGenerator(@Nonnull Gesuch gesuch, @Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, AnmeldungTagesschule anmeldungTagesschule, boolean mahlzeitenverguenstigung) {
		super(gesuch, stammdaten);
		this.art = art;
		this.anmeldungTagesschule = anmeldungTagesschule;
		this.mahlzeitenverguenstigung = mahlzeitenverguenstigung;
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
				document.add(PdfUtil.createParagraph(translate(BEMERKUNG), 0));
				Paragraph bemerkungParagraph = new Paragraph();
				bemerkungParagraph.setSpacingAfter(1 * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
				bemerkungParagraph.add(PdfUtil.createListInParagraph(Arrays.asList(anmeldungTagesschule.getBelegungTagesschule().getBemerkung().split("\\r?\\n")).stream().filter(string -> string != null && string.trim().length() > 0).collect(Collectors.toList())));
				document.add(bemerkungParagraph);
			}
			List<Element> abschlussElemente = Lists.newArrayList();
			if (art == Art.OHNE_TARIF) {
				abschlussElemente.add(PdfUtil.createParagraph(translate(BESTAETIGUNG_OHNE_TARIF)));
			} else {
				Verfuegung verfuegung = anmeldungTagesschule.getVerfuegungOrVerfuegungPreview();
				Objects.requireNonNull(verfuegung);
				createGebuehrenTabelle(verfuegung, document);
			}
			abschlussElemente.add(PdfUtil.createParagraph(translate(NICHT_EINVERSTANDEN)));
			abschlussElemente.add(createParagraphGruss());
			abschlussElemente.add(createParagraphSignatur());
			document.add(PdfUtil.createKeepTogetherTable(abschlussElemente, 2, 0));
		};
	}

	private void createGebuehrenTabelle(@Nonnull Verfuegung verfuegung, @Nonnull Document document) {
		List<VerfuegungZeitabschnitt> abschnitteMitBetreuung =
			verfuegung.getZeitabschnitte()
				.stream()
				.filter(verfuegungZeitabschnitt ->
					verfuegungZeitabschnitt.getRelevantBgCalculationResult().getTsCalculationResultMitPaedagogischerBetreuung() != null)
				.filter(verfuegungZeitabschnitt ->
					verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().compareTo(
						Objects.requireNonNull(Objects.requireNonNull(verfuegung.getAnmeldungTagesschule())
							.getBelegungTagesschule()).getEintrittsdatum()) >= 0
				)
				.peek(verfuegungZeitabschnitt -> {
					if(verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().compareTo(
						Objects.requireNonNull(verfuegung.getAnmeldungTagesschule().getBelegungTagesschule()).getEintrittsdatum()) < 0) {
						verfuegungZeitabschnitt.getGueltigkeit().setGueltigAb(verfuegung.getAnmeldungTagesschule().getBelegungTagesschule().getEintrittsdatum());
					}
				})
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
					verfuegungZeitabschnitt.getRelevantBgCalculationResult().getTsCalculationResultOhnePaedagogischerBetreuung() != null)
				.filter(verfuegungZeitabschnitt ->
					verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().compareTo(
						Objects.requireNonNull(Objects.requireNonNull(verfuegung.getAnmeldungTagesschule())
							.getBelegungTagesschule()).getEintrittsdatum()) >= 0
				)
				.peek(verfuegungZeitabschnitt -> {
					if(verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().compareTo(
						Objects.requireNonNull(verfuegung.getAnmeldungTagesschule().getBelegungTagesschule()).getEintrittsdatum()) < 0) {
						verfuegungZeitabschnitt.getGueltigkeit().setGueltigAb(verfuegung.getAnmeldungTagesschule().getBelegungTagesschule().getEintrittsdatum());
					}
				})
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(abschnitteOhneBetreuung)) {
			document.add(createGebuehrTabelleTitle(false, false));
			PdfPTable gebuehrenTable = createGebuehrenTableHeader();
			fillGebuehrenTable(gebuehrenTable, abschnitteOhneBetreuung, false);
			document.add(gebuehrenTable);
		}
	}

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
	private PdfPTable createKindTSAnmeldungTable() throws DocumentException {
		PdfPTable table = new PdfPTable(4);
		float[] columnWidths = { 55, 100, 50, 100 };
		table.setWidths(columnWidths);
		// Init
		PdfUtil.setTableDefaultStyles(table);
		table.setSpacingAfter(2 * DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
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
		Objects.requireNonNull(anmeldungTagesschule.getBelegungTagesschule());
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
		table.setWidths(new int[] { 30, 25, 20, 20, 20, 20, 20 });
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(createCell(Element.ALIGN_LEFT, translate(TEILANGEBOT), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(ZEIT), Color.LIGHT_GRAY));
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

			tagesschuleModuleMap.values().stream().sorted((oneList, otherList) -> {
				ModulTagesschuleGroup one = oneList.get(0).getModulTagesschule().getModulTagesschuleGroup();
				ModulTagesschuleGroup other = otherList.get(0).getModulTagesschule().getModulTagesschuleGroup();;
				return one.compareTo(other);
			}).forEach(v -> {
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
				Objects.requireNonNull(mtg);
				boolean isFrench = "fr".equalsIgnoreCase(sprache.getLanguage());
				String bezeichnungStr;
				if (mtg.getBezeichnung().getTextDeutsch() != null && mtg.getBezeichnung().getTextFranzoesisch() != null) {
					bezeichnungStr = isFrench ? mtg.getBezeichnung().getTextFranzoesisch() :
						mtg.getBezeichnung().getTextDeutsch();
				} else {
					bezeichnungStr = mtg.getModulTagesschuleName().toString();
				}
				table.addCell(createCell(Element.ALIGN_LEFT, bezeichnungStr, null));
				table.addCell(createCell(Element.ALIGN_RIGHT, mtg.getZeitVon().format(Constants.HOURS_FORMAT) + '-' + mtg.getZeitBis().format(Constants.HOURS_FORMAT), null));
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

		int rows = mahlzeitenverguenstigung ? 7 : 6;
		int[] columnWidths = mahlzeitenverguenstigung ? new int[] { 20, 20, 30, 18, 30, 30, 25 } : new int [] { 20, 20,	30,	18,	30,	25 };

		PdfPTable table = new PdfPTable(rows);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setWidths(columnWidths);
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(VON), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(BIS), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(BETREUNGSSTUNDEN_PRO_WOCHE), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(GEBUEHR_PRO_STUNDE), Color.LIGHT_GRAY));
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(VERPFLEGUNGSKOSTEN_PRO_WOCHE), Color.LIGHT_GRAY));
		if (mahlzeitenverguenstigung) {
			table.addCell(createCell(Element.ALIGN_RIGHT, translate(VERPFLEGUNGSKOSTEN_VERGUENSTIGUNG_PRO_WOCHE), Color.LIGHT_GRAY));
		}
		table.addCell(createCell(Element.ALIGN_RIGHT, translate(TOTAL_PRO_WOCHE), Color.LIGHT_GRAY));
		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		return table;
	}

	private void fillGebuehrenTable(PdfPTable table, List<VerfuegungZeitabschnitt> abschnitte, boolean mitPedagogischerBetreuug) {
		for (VerfuegungZeitabschnitt anmeldungTagesschuleZeitabschnitt : abschnitte) {
			TSCalculationResult tsResult = mitPedagogischerBetreuug ?
				anmeldungTagesschuleZeitabschnitt.getRelevantBgCalculationResult().getTsCalculationResultMitPaedagogischerBetreuung() :
				anmeldungTagesschuleZeitabschnitt.getRelevantBgCalculationResult().getTsCalculationResultOhnePaedagogischerBetreuung();
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
			if (mahlzeitenverguenstigung) {
				table.addCell(createCell(Element.ALIGN_RIGHT,
					CHF + PdfUtil.printBigDecimal(tsResult.getVerpflegungskostenVerguenstigt()),
					null));
			}
			table.addCell(createCell(Element.ALIGN_RIGHT,
				CHF + PdfUtil.printBigDecimal(tsResult.getTotalKostenProWoche()),
				null));
		}
	}

	private PdfPCell getCellForDay(boolean isSelected, boolean alleZweiWochen) {
		if (!isSelected) {
			return createCell(Element.ALIGN_CENTER, "", null);
		}
		Paragraph dayParagraph =
			new Paragraph(new Phrase("\uF058", PdfUtil.FONT_AWESOME));
		dayParagraph.setSpacingBefore(0);
		dayParagraph.setAlignment(Element.ALIGN_CENTER);
		dayParagraph.setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		PdfPCell dayCell = new PdfPCell();
		dayCell.addElement(dayParagraph);
		if(alleZweiWochen){
			Paragraph alleZweiWocheParagraph =
				new Paragraph(new Phrase(translate(ALLE_2_WOCHEN), PdfUtilities.createFontWithSize(getPageConfiguration().getFont(), 6.0f)));
			alleZweiWocheParagraph.setAlignment(Element.ALIGN_CENTER);
			dayCell.addElement(alleZweiWocheParagraph);
		}
		dayCell.setPadding(0);
		dayCell.setPaddingBottom(4);
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
		cell.setPaddingBottom(4);
		return cell;
	}
}
