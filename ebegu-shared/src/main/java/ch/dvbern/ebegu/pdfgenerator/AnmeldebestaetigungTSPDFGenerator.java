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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.util.Constants;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.pdfgenerator.PdfGenerator.CustomGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfElementGenerator;
import ch.dvbern.lib.invoicegenerator.pdf.PdfUtilities;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
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
	private static final String GEBUEHREN = "PdfGeneration_AnmeldungBestaetigung_gebuehren";
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
	private final KindContainer kindContainer;
	private final AnmeldungTagesschule anmeldungTagesschule;

	protected AnmeldebestaetigungTSPDFGenerator(@Nonnull Gesuch gesuch, @Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art, @Nonnull KindContainer kindContainer, AnmeldungTagesschule anmeldungTagesschule) {
		super(gesuch, stammdaten);
		this.art = art;
		this.kindContainer = kindContainer;
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
				StringUtils.isNotEmpty(anmeldungTagesschule.getBelegungTagesschule().getBemerkung()))
			{
				Paragraph bemerkungTitleParagraph = new Paragraph();
				bemerkungTitleParagraph.add(new Phrase(translate(BEMERKUNG), getPageConfiguration().getFont()));
				bemerkungTitleParagraph.setSpacingAfter(0);
				Paragraph bemerkungParagraph = new Paragraph();
				bemerkungParagraph.add(new Phrase( "- " + anmeldungTagesschule.getBelegungTagesschule().getBemerkung(),
					getPageConfiguration().getFont()));
				document.add(bemerkungTitleParagraph);
				document.add(bemerkungParagraph);
			}

			if (art == Art.OHNE_TARIF) {
				Paragraph bestaetigungOhneTarifParagraph = new Paragraph();
				bestaetigungOhneTarifParagraph.setSpacingBefore(PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
				bestaetigungOhneTarifParagraph.setSpacingAfter(2 * PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
				bestaetigungOhneTarifParagraph.add(new Phrase(translate(BESTAETIGUNG_OHNE_TARIF),
					getPageConfiguration().getFont()));
				document.add(bestaetigungOhneTarifParagraph);
			} else {
				Paragraph gebuhren = new Paragraph();
				gebuhren.add(new Phrase(translate(GEBUEHREN), getPageConfiguration().getFont()));
				gebuhren.setSpacingBefore(PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
				gebuhren.setSpacingAfter(PdfUtilities.DEFAULT_FONT_SIZE * PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
				document.add(gebuhren);
				PdfPTable gebuehrenTable = createGebuehrenTableHeader();
				fillGebuehrenTable(gebuehrenTable);
				document.add(gebuehrenTable);
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
				document.add(endCommunication);
			}
			document.add(createParagraphGruss());
			Paragraph bernAmtParagraph = new Paragraph();
			bernAmtParagraph.add(new Phrase(translate(ABT_BILDUNG_BERN), getPageConfiguration().getFont()));
			document.add(bernAmtParagraph);
		};
	}

	@Nonnull
	public PdfPTable createKindTSAnmeldungTable() {
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
		// Row: Name + Eintrittsdatum
		table.addCell(new Phrase(translate(KIND_NAME), getPageConfiguration().getFont()));
		table.addCell(new Phrase(kindContainer.getKindGS() != null ? kindContainer.getKindGS().getFullName() :
			kindContainer.getKindJA().getFullName(),
			getPageConfiguration().getFont()));
		table.addCell(new Phrase(translate(EINTRITTSDATUM), getPageConfiguration().getFont()));
		assert anmeldungTagesschule.getBelegungTagesschule() != null;
		table.addCell(new Phrase(anmeldungTagesschule.getBelegungTagesschule().getEintrittsdatum().toString(),
			getPageConfiguration().getFont()));
		// Row: Angebot + Klasse
		table.addCell(new Phrase(translate(ANGEBOT), getPageConfiguration().getFont()));
		table.addCell(new Phrase(translate(ANGEBOT_TAGESSCHULE), getPageConfiguration().getFont()));
		table.addCell(new Phrase(translate(KLASSE), getPageConfiguration().getFont()));
		EinschulungTyp einschulungTyp = kindContainer.getKindGS() != null ?
			kindContainer.getKindGS().getEinschulungTyp() : kindContainer.getKindJA().getEinschulungTyp();
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
	public PdfPTable createBetreuungsangeboteTable() throws DocumentException {
		PdfPTable table = new PdfPTable(7);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setWidths(new int[] { 30, 30, 20, 20, 20, 20, 20 });
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(PdfUtil.createTitleCell(translate(TEILANGEBOT)));
		table.addCell(PdfUtil.createTitleCell(translate(ZEIT)));
		table.addCell(PdfUtil.createTitleCell(translate(MONTAG)));
		table.addCell(PdfUtil.createTitleCell(translate(DIENSTAG)));
		table.addCell(PdfUtil.createTitleCell(translate(MITTWOCH)));
		table.addCell(PdfUtil.createTitleCell(translate(DONNERSTAG)));
		table.addCell(PdfUtil.createTitleCell(translate(FREITAG)));

		if (anmeldungTagesschule.getBelegungTagesschule() != null && anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule() != null) {
			Map<String, List<ModulTagesschule>> tagesschuleModuleMap = new HashMap();
			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().forEach(belegungTagesschuleModul -> {
				List<ModulTagesschule> modulTagesschuleList =
					tagesschuleModuleMap.get(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId());
				if (modulTagesschuleList == null) {
					modulTagesschuleList = new ArrayList();
				}
				modulTagesschuleList.add(belegungTagesschuleModul.getModulTagesschule());
				tagesschuleModuleMap.put(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId(), modulTagesschuleList);
			});

			tagesschuleModuleMap.forEach((k, v) -> {
				boolean monday = false;
				boolean tuesday = false;
				boolean wednesday = false;
				boolean thursday = false;
				boolean friday = false;
				ModulTagesschuleGroup mtg = null;
				for (ModulTagesschule mt : v) {
					if (mtg == null) {
						mtg = mt.getModulTagesschuleGroup();
					}
					int day = mt.getWochentag().getValue();
					switch (day) {
					case 1:
						monday = true;
						break;
					case 2:
						tuesday = true;
						break;
					case 3:
						wednesday = true;
						break;
					case 4:
						thursday = true;
						break;
					case 5:
						friday = true;
						break;
					}
				}
				assert mtg != null;
				boolean isFrench = "fr".equalsIgnoreCase(sprache.getLanguage());
				table.addCell(new Phrase(isFrench ? mtg.getBezeichnung().getTextFranzoesisch() :
					mtg.getBezeichnung().getTextDeutsch(),
					getPageConfiguration().getFont()));
				table.addCell(new Phrase(mtg.getZeitVon().format(Constants.HOURS_FORMAT) + "-" + mtg.getZeitBis().format(Constants.HOURS_FORMAT), getPageConfiguration().getFont()));
				table.addCell(getCellForDay(monday));
				table.addCell(getCellForDay(tuesday));
				table.addCell(getCellForDay(wednesday));
				table.addCell(getCellForDay(thursday));
				table.addCell(getCellForDay(friday));
			});
		}

		table.setSpacingAfter(DEFAULT_MULTIPLIED_LEADING * DEFAULT_FONT_SIZE);
		return table;
	}

	public PdfPTable createGebuehrenTableHeader(){
		PdfPTable table = new PdfPTable(6);
		table.setWidthPercentage(PdfElementGenerator.FULL_WIDTH);
		table.setWidths(new int[] { 30, 30, 30, 30, 30, 30 });
		table.setHeaderRows(1);
		table.setKeepTogether(true);
		table.addCell(PdfUtil.createTitleCell(translate(VON)));
		table.addCell(PdfUtil.createTitleCell(translate(BIS)));
		table.addCell(PdfUtil.createTitleCell(translate(BETREUNGSSTUNDEN_PRO_WOCHE)));
		table.addCell(PdfUtil.createTitleCell(translate(GEBUEHR_PRO_STUNDE)));
		table.addCell(PdfUtil.createTitleCell(translate(VERPFLEGUNGSKOSTEN_PRO_WOCHE)));
		table.addCell(PdfUtil.createTitleCell(translate(TOTAL_PRO_WOCHE)));
		return table;
	}

	public void fillGebuehrenTable(PdfPTable table) {

		int startJahr = this.getGesuch().getGesuchsperiode().getBasisJahr();

		assert anmeldungTagesschule.getBelegungTagesschule() != null;
		int stundenProWoche = 0;
		int minutesProWoche = 0;
		BigDecimal verpflegKostenProWoche = new BigDecimal("0.0");
		for (BelegungTagesschuleModul belegungTagesschuleModul :
			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule()) {
			ModulTagesschule modulTagesschule = belegungTagesschuleModul.getModulTagesschule();
			int hours = modulTagesschule.getModulTagesschuleGroup().getZeitVon().getHour();
			int minutes = modulTagesschule.getModulTagesschuleGroup().getZeitVon().getMinute();
			LocalTime zeitBis = modulTagesschule.getModulTagesschuleGroup().getZeitBis();
			zeitBis = zeitBis.minusHours(hours);
			zeitBis = zeitBis.minusMinutes(minutes);
			stundenProWoche += zeitBis.getHour();
			minutesProWoche += zeitBis.getMinute();

			if (modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten() != null) {
				verpflegKostenProWoche =
					verpflegKostenProWoche.add(modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten());
			}
		}

		Double additionalHours = minutesProWoche / 60.0;
		if (additionalHours >= 1.0) {
			int hoursToAdd = additionalHours.intValue();
			stundenProWoche += hoursToAdd;
			minutesProWoche -= hoursToAdd * 60;
		}
		// TODO wir wissen noch nicht von wo kommen diese Werten
		Double gebuehrProStundeFirstPeriod = 3.5;
		Double gebuehrProStundeSecondPeriod = 3.5;
		Double totalKostenFirstPeriod =
			gebuehrProStundeFirstPeriod * stundenProWoche + ((gebuehrProStundeFirstPeriod * minutesProWoche) / 60) + verpflegKostenProWoche.doubleValue();

		Double totalKostenSecondPeriod =
			gebuehrProStundeFirstPeriod * stundenProWoche + ((gebuehrProStundeSecondPeriod * minutesProWoche) / 60) + verpflegKostenProWoche.doubleValue();

		table.addCell(new Phrase("01.08." + startJahr, getPageConfiguration().getFont()));
		table.addCell(new Phrase("31.12." + startJahr, getPageConfiguration().getFont()));
		table.addCell(new Phrase(stundenProWoche + ":" + minutesProWoche, getPageConfiguration().getFont()));
		table.addCell(new Phrase(CHF + gebuehrProStundeFirstPeriod.toString(), getPageConfiguration().getFont()));
		table.addCell(new Phrase(CHF + verpflegKostenProWoche.toString(), getPageConfiguration().getFont()));
		table.addCell(new Phrase(CHF + totalKostenFirstPeriod.toString(), getPageConfiguration().getFont()));

		table.addCell(new Phrase("01.01." + (startJahr + 1), getPageConfiguration().getFont()));
		table.addCell(new Phrase("31.07." + (startJahr + 1), getPageConfiguration().getFont()));
		table.addCell(new Phrase(stundenProWoche + ":" + minutesProWoche, getPageConfiguration().getFont()));
		table.addCell(new Phrase(CHF + gebuehrProStundeSecondPeriod.toString(), getPageConfiguration().getFont()));
		table.addCell(new Phrase(CHF + verpflegKostenProWoche.toString(), getPageConfiguration().getFont()));
		table.addCell(new Phrase(CHF + totalKostenSecondPeriod.toString(), getPageConfiguration().getFont()));
	}

	private PdfPCell getCellForDay(boolean isSelected) {
		Paragraph dayParagraph =
			new Paragraph(new Phrase(isSelected ? "X" : "", getPageConfiguration().getFont()));
		dayParagraph.setSpacingBefore(0);
		dayParagraph.setAlignment(Element.ALIGN_CENTER);
		dayParagraph.setLeading(0, PdfUtilities.DEFAULT_MULTIPLIED_LEADING);
		PdfPCell dayCell = new PdfPCell();
		dayCell.addElement(dayParagraph);
		return dayCell;
	}
}
