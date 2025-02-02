/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.reporting.kanton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldKanton;
import ch.dvbern.ebegu.util.ReportUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class KantonExcelConverter implements ExcelConverter {

	private static final String EMPTY_STRING = " ";
	private static final Integer TITLE_ROW_NUMBER = 9;

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
		// No autosizing defined for columns
	}

	@Nonnull
	public Sheet mergeHeaderFieldsStichtag(
		@Nonnull List<KantonDataRow> data,
		@Nonnull Sheet sheet,
		@Nonnull Locale locale,
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable BigDecimal kantonSelbstbehalt,
		@Nonnull Mandant mandant
	) throws ExcelMergeException {

		checkNotNull(data);

		ExcelMergerDTO excelMergerDTO = new ExcelMergerDTO();
		List<MergeField<?>> mergeFields = new ArrayList<>();
		mergeFields.add(MergeFieldKanton.auswertungVon.getMergeField());
		excelMergerDTO.addValue(MergeFieldKanton.auswertungVon, datumVon);
		mergeFields.add(MergeFieldKanton.auswertungBis.getMergeField());
		excelMergerDTO.addValue(MergeFieldKanton.auswertungBis, datumBis);
		mergeFields.add(MergeFieldKanton.kantonSelbstbehalt.getMergeField());
		excelMergerDTO.addValue(MergeFieldKanton.kantonSelbstbehalt, kantonSelbstbehalt != null ? kantonSelbstbehalt : BigDecimal.ZERO);

		addHeaders(excelMergerDTO, mergeFields, locale, mandant);

		ExcelMerger.mergeData(sheet, mergeFields, excelMergerDTO);

		if (kantonSelbstbehalt == null) {
			sheet.getRow(5).setZeroHeight(true);
			sheet.setColumnHidden(11, true);
		}

		return sheet;
	}

	public void mergeRows(
		RowFiller rowFiller,
		@Nonnull List<KantonDataRow> data
	) {
		if (data.isEmpty()) {
			addEmptyRow(rowFiller);
			return;
		}
		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();
			excelRowGroup.addValue(MergeFieldKanton.gemeinde, dataRow.getGemeinde());
			excelRowGroup.addValue(MergeFieldKanton.referenzNummer, dataRow.getReferenzNummer());
			excelRowGroup.addValue(MergeFieldKanton.gesuchId, dataRow.getGesuchId());
			excelRowGroup.addValue(MergeFieldKanton.name, dataRow.getName());
			excelRowGroup.addValue(MergeFieldKanton.vorname, dataRow.getVorname());
			excelRowGroup.addValue(MergeFieldKanton.geburtsdatum, dataRow.getGeburtsdatum());
			excelRowGroup.addValue(MergeFieldKanton.zeitabschnittVon, dataRow.getZeitabschnittVon());
			excelRowGroup.addValue(MergeFieldKanton.zeitabschnittBis, dataRow.getZeitabschnittBis());
			BigDecimal bgPensumTotal = dataRow.getBgPensumTotal();
			excelRowGroup.addValue(MergeFieldKanton.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldKanton.betreuungsTyp, dataRow.getBetreuungsTyp());
			excelRowGroup.addValue(MergeFieldKanton.babyTarif, dataRow.getBabyTarif());
			if (bgPensumTotal != null && bgPensumTotal.compareTo(BigDecimal.ZERO) > 0) {
				excelRowGroup.addValue(MergeFieldKanton.bgPensumKanton, dataRow.getBgPensumKanton());
				excelRowGroup.addValue(MergeFieldKanton.bgPensumGemeinde, dataRow.getBgPensumGemeinde());
				excelRowGroup.addValue(MergeFieldKanton.bgPensumTotal, bgPensumTotal);
				excelRowGroup.addValue(MergeFieldKanton.elternbeitrag, dataRow.getElternbeitrag());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungKanton, dataRow.getVerguenstigungKanton());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungGemeinde, dataRow.getVerguenstigungGemeinde());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungTotal, dataRow.getVerguenstigungTotal());
			} else {
				addEmptyCalculations(excelRowGroup);
			}
			rowFiller.fillRow(excelRowGroup);
		});
		this.addTotalRow(rowFiller, data.size());
	}

	private void addEmptyRow(RowFiller rowFiller) {
		ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();
		excelRowGroup.addValue(MergeFieldKanton.gemeinde, EMPTY_STRING);
		excelRowGroup.addValue(MergeFieldKanton.referenzNummer, EMPTY_STRING);
		excelRowGroup.addValue(MergeFieldKanton.gesuchId, EMPTY_STRING);
		excelRowGroup.addValue(MergeFieldKanton.name, EMPTY_STRING);
		excelRowGroup.addValue(MergeFieldKanton.vorname, EMPTY_STRING);
		excelRowGroup.addValue(MergeFieldKanton.geburtsdatum, null);
		excelRowGroup.addValue(MergeFieldKanton.zeitabschnittVon, null);
		excelRowGroup.addValue(MergeFieldKanton.zeitabschnittBis, null);
		addEmptyCalculations(excelRowGroup);
		rowFiller.fillRow(excelRowGroup);
	}

	private void addEmptyCalculations(ExcelMergerDTO excelRowGroup) {
		excelRowGroup.addValue(MergeFieldKanton.bgPensumKanton, BigDecimal.ZERO);
		excelRowGroup.addValue(MergeFieldKanton.bgPensumGemeinde, BigDecimal.ZERO);
		excelRowGroup.addValue(MergeFieldKanton.bgPensumTotal, BigDecimal.ZERO);
		excelRowGroup.addValue(MergeFieldKanton.elternbeitrag, BigDecimal.ZERO);
		excelRowGroup.addValue(MergeFieldKanton.verguenstigungKanton, BigDecimal.ZERO);
		excelRowGroup.addValue(MergeFieldKanton.verguenstigungGemeinde, BigDecimal.ZERO);
		excelRowGroup.addValue(MergeFieldKanton.verguenstigungTotal, BigDecimal.ZERO);
	}

	private void addTotalRow(RowFiller rowFiller, int nbrRow) {
		//Create Total Row
		SXSSFSheet sheet = rowFiller.getSheet();
		SXSSFRow targetRow = sheet.createRow(sheet.getLastRowNum() + 1);
		SXSSFCell cell = targetRow.createCell(0);
		CellStyle basicStyle = ReportUtil.createBasicStyleSumRow(sheet);
		cell.setCellValue("Total");
		cell.setCellStyle(basicStyle);
		ReportUtil.fillXCellWithStyle(targetRow, basicStyle, 1, 6);
		CellStyle procentStyle = ReportUtil.createProcentStyle(sheet, basicStyle);
		CellStyle zahlStyle = ReportUtil.createNumberStyle(sheet, basicStyle);

		int lastRow = nbrRow + TITLE_ROW_NUMBER;
		int totalRow = lastRow + 1;
		ReportUtil.createCellWithFormula(targetRow, procentStyle, 7, "SUM(H10:H" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, procentStyle, 8, "SUM(I10:I" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, procentStyle, 9, "SUM(J10:J" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 10, "SUM(K10:K" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 11, "=K"+ totalRow +"*$B$6");
		ReportUtil.fillXCellWithStyle(targetRow, basicStyle, 12, 16);
		ReportUtil.createCellWithFormula(targetRow, procentStyle, 17, "SUM(R10:R" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 18, "SUM(S10:S" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 19, "SUM(T10:T" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 20,"SUM(U10:U" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 21, "SUM(V10:V" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 22,"SUM(W10:W" + lastRow + ")");
		ReportUtil.fillXCellWithStyle(targetRow, basicStyle, 23, 24);
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void addHeaders(
		@Nonnull ExcelMergerDTO excelMerger,
		@Nonnull List<MergeField<?>> mergeFields,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant) {
		mergeFields.add(MergeFieldKanton.kantonTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.kantonTitle, ServerMessageUtil.getMessage("Reports_kantonTitle",
			locale, mandant));
		mergeFields.add(MergeFieldKanton.parameterTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.parameterTitle,
			ServerMessageUtil.getMessage("Reports_parameterTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.vonTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.vonTitle, ServerMessageUtil.getMessage("Reports_vonTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.bisTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.bisTitle, ServerMessageUtil.getMessage("Reports_bisTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.kantonSelbstbehaltTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.kantonSelbstbehaltTitle, ServerMessageUtil.getMessage("Reports_kantonSelbstbehaltTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.gemeindeTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.gemeindeTitle,
			ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.fallIdTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.fallIdTitle, ServerMessageUtil.getMessage("Reports_fallIdTitle",
			locale, mandant));
		mergeFields.add(MergeFieldKanton.vornameTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.vornameTitle,
			ServerMessageUtil.getMessage("Reports_vornameTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.nachnameTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.nachnameTitle,
			ServerMessageUtil.getMessage("Reports_nachnameTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.geburtsdatumTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.geburtsdatumTitle,
			ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.betreuungVonTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.betreuungVonTitle,
			ServerMessageUtil.getMessage("Reports_betreuungVonTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.betreuungBisTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.betreuungBisTitle,
			ServerMessageUtil.getMessage("Reports_betreuungBisTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.bgPensumKantonTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.bgPensumKantonTitle,
			ServerMessageUtil.getMessage("Reports_bgPensumKantonTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.bgPensumGemeindeTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.bgPensumGemeindeTitle,
			ServerMessageUtil.getMessage("Reports_bgPensumGemeindeTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.bgPensumTotalTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.bgPensumTotalTitle,
			ServerMessageUtil.getMessage("Reports_bgPensumTotalTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.monatsanfangTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.monatsanfangTitle,
			ServerMessageUtil.getMessage("Reports_monatsanfangTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.monatsendeTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.monatsendeTitle,
			ServerMessageUtil.getMessage("Reports_monatsendeTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.tageMonatTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.tageMonatTitle,
			ServerMessageUtil.getMessage("Reports_tageMonatTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.tageIntervallTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.tageIntervallTitle,
			ServerMessageUtil.getMessage("Reports_tageIntervallTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.anteilMonatKantonTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.anteilMonatKantonTitle,
			ServerMessageUtil.getMessage("Reports_anteilMonatKantonTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.platzbelegungTageTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.platzbelegungTageTitle,
			ServerMessageUtil.getMessage("Reports_platzbelegungTageTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.kostenCHFTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.kostenCHFTitle,
			ServerMessageUtil.getMessage("Reports_kostenCHFTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.vollkostenTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.vollkostenTitle,
			ServerMessageUtil.getMessage("Reports_vollkostenTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.elternbeitragTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.elternbeitragTitle,
			ServerMessageUtil.getMessage("Reports_elternbeitragTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.gutscheinKantonTitel.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.gutscheinKantonTitel,
			ServerMessageUtil.getMessage("Reports_gutscheinKantonTitel", locale, mandant));
		mergeFields.add(MergeFieldKanton.gutscheinGemeindeTitel.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.gutscheinGemeindeTitel,
			ServerMessageUtil.getMessage("Reports_gutscheinGemeindeTitel", locale, mandant));
		mergeFields.add(MergeFieldKanton.gutscheinTotalTitel.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.gutscheinTotalTitel,
			ServerMessageUtil.getMessage("Reports_gutscheinTotalTitel", locale, mandant));
		mergeFields.add(MergeFieldKanton.babyFaktorTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.babyFaktorTitle,
			ServerMessageUtil.getMessage("Reports_babyFaktorTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.institutionTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.institutionTitle,
			ServerMessageUtil.getMessage("Reports_institutionTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.totalTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.totalTitle, ServerMessageUtil.getMessage("Reports_totalTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.selbstbehaltTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.selbstbehaltTitle,
			ServerMessageUtil.getMessage("Reports_selbstbehaltTitle", locale, mandant));
		mergeFields.add(MergeFieldKanton.anteilKalenderjahrTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.anteilKalenderjahrTitle,
			ServerMessageUtil.getMessage("Reports_anteilKalenderjahrTitle", locale, mandant));
	}
}
