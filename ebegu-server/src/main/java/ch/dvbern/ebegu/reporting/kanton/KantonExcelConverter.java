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
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.enums.reporting.MergeFieldKanton;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class KantonExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public Sheet mergeHeaderFieldsStichtag(
		@Nonnull List<KantonDataRow> data,
		@Nonnull Sheet sheet,
		@Nonnull Locale locale,
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis
	) throws ExcelMergeException {

		checkNotNull(data);

		ExcelMergerDTO excelMergerDTO = new ExcelMergerDTO();
		List<MergeField<?>> mergeFields = new ArrayList<>();
		mergeFields.add(MergeFieldKanton.auswertungVon.getMergeField());
		excelMergerDTO.addValue(MergeFieldKanton.auswertungVon, datumVon);
		mergeFields.add(MergeFieldKanton.auswertungBis.getMergeField());
		excelMergerDTO.addValue(MergeFieldKanton.auswertungBis, datumBis);
		addHeaders(excelMergerDTO, mergeFields, locale);

		ExcelMerger.mergeData(sheet, mergeFields, excelMergerDTO);

		return sheet;
	}

	public void mergeRows(
		RowFiller rowFiller,
		@Nonnull List<KantonDataRow> data
	) {
		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();
			excelRowGroup.addValue(MergeFieldKanton.gemeinde, dataRow.getGemeinde());
			excelRowGroup.addValue(MergeFieldKanton.bgNummer, dataRow.getBgNummer());
			excelRowGroup.addValue(MergeFieldKanton.gesuchId, dataRow.getGesuchId());
			excelRowGroup.addValue(MergeFieldKanton.name, dataRow.getName());
			excelRowGroup.addValue(MergeFieldKanton.vorname, dataRow.getVorname());
			excelRowGroup.addValue(MergeFieldKanton.geburtsdatum, dataRow.getGeburtsdatum());
			excelRowGroup.addValue(MergeFieldKanton.zeitabschnittVon, dataRow.getZeitabschnittVon());
			excelRowGroup.addValue(MergeFieldKanton.zeitabschnittBis, dataRow.getZeitabschnittBis());
			BigDecimal bgPensumTotal = dataRow.getBgPensumTotal();
			excelRowGroup.addValue(MergeFieldKanton.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldKanton.betreuungsTyp, dataRow.getBetreuungsTyp());
			if (bgPensumTotal != null && bgPensumTotal.compareTo(BigDecimal.ZERO) > 0) {
				excelRowGroup.addValue(MergeFieldKanton.bgPensumKanton, dataRow.getBgPensumKanton());
				excelRowGroup.addValue(MergeFieldKanton.bgPensumGemeinde, dataRow.getBgPensumGemeinde());
				excelRowGroup.addValue(MergeFieldKanton.bgPensumTotal, bgPensumTotal);
				excelRowGroup.addValue(MergeFieldKanton.elternbeitrag, dataRow.getElternbeitrag());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungKanton, dataRow.getVerguenstigungKanton());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungGemeinde, dataRow.getVerguenstigungGemeinde());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungTotal, dataRow.getVerguenstigungTotal());
			} else {
				excelRowGroup.addValue(MergeFieldKanton.bgPensumKanton, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.bgPensumGemeinde, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.bgPensumTotal, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.elternbeitrag, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungKanton, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungGemeinde, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungTotal, BigDecimal.ZERO);
			}
			rowFiller.fillRow(excelRowGroup);
		});
		this.addTotalRow(rowFiller, data.size());
	}

	private void addTotalRow(RowFiller rowFiller, int nbrCell) {
		//Create Total Row
		SXSSFSheet sheet = rowFiller.getSheet();
		SXSSFRow targetRow = sheet.createRow(sheet.getLastRowNum() + 1);
		SXSSFCell cell = targetRow.createCell(0);
		CellStyle basicStyle = this.createBasicStyle(sheet);
		cell.setCellValue("Total");
		cell.setCellStyle(basicStyle);
		this.fillXRowWithStyle(targetRow, basicStyle, 1, 6);
		CellStyle procentStyle = this.createBasicStyle(sheet);
		procentStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat("0.0%"));
		nbrCell = nbrCell + 8;
		this.createCellWithFormula(targetRow, procentStyle, 7, "SUM(H9:H" + nbrCell + ")");
		this.createCellWithFormula(targetRow, procentStyle, 8, "SUM(I9:I" + nbrCell + ")");
		this.createCellWithFormula(targetRow, procentStyle, 9, "SUM(J9:J" + nbrCell + ")");
		this.fillXRowWithStyle(targetRow, basicStyle, 10, 14);
		this.createCellWithFormula(targetRow, procentStyle, 15, "SUM(P9:P" + nbrCell + ")");
		CellStyle zahlStyle = this.createBasicStyle(sheet);
		zahlStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat("0.00"));
		this.createCellWithFormula(targetRow, zahlStyle, 16, "SUM(Q9:Q" + nbrCell + ")");
		this.createCellWithFormula(targetRow, zahlStyle, 17, "SUM(R9:R" + nbrCell + ")");
		this.createCellWithFormula(targetRow, zahlStyle, 18,"SUM(S9:S" + nbrCell + ")");
		this.createCellWithFormula(targetRow, zahlStyle, 19, "SUM(T9:T" + nbrCell + ")");
		this.createCellWithFormula(targetRow, zahlStyle, 20,"SUM(U9:U" + nbrCell + ")");
		this.fillXRowWithStyle(targetRow, basicStyle, 21, 22);
	}

	private void createCellWithFormula(SXSSFRow targetRow, CellStyle cellStyle, int cellNbr, String formula) {
		SXSSFCell cellSumme = targetRow.createCell(cellNbr);
		cellSumme.setCellFormula(formula);
		cellSumme.setCellStyle(cellStyle);
		cellSumme.setCellType(CellType.FORMULA);
	}

	private void fillXRowWithStyle(SXSSFRow targetRow, CellStyle cellStyle, int firstCellToFill, int lastCellToFill) {
		for (int i = firstCellToFill; i <= lastCellToFill; i++) {
			SXSSFCell cell = targetRow.createCell(i);
			cell.setCellValue("");
			cell.setCellStyle(cellStyle);
		}
	}

	private CellStyle createBasicStyle(SXSSFSheet sheet) {
		CellStyle basicStyle = sheet.getWorkbook().createCellStyle();
		basicStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		basicStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		basicStyle.setBorderBottom(BorderStyle.THIN);
		basicStyle.setBorderLeft(BorderStyle.THIN);
		basicStyle.setBorderRight(BorderStyle.THIN);
		return basicStyle;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void addHeaders(
		@Nonnull ExcelMergerDTO excelMerger,
		@Nonnull List<MergeField<?>> mergeFields,
		@Nonnull Locale locale) {
		mergeFields.add(MergeFieldKanton.kantonTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.kantonTitle, ServerMessageUtil.getMessage("Reports_kantonTitle",
			locale));
		mergeFields.add(MergeFieldKanton.parameterTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.parameterTitle,
			ServerMessageUtil.getMessage("Reports_parameterTitle", locale));
		mergeFields.add(MergeFieldKanton.vonTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.vonTitle, ServerMessageUtil.getMessage("Reports_vonTitle", locale));
		mergeFields.add(MergeFieldKanton.bisTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.bisTitle, ServerMessageUtil.getMessage("Reports_bisTitle", locale));
		mergeFields.add(MergeFieldKanton.gemeindeTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.gemeindeTitle,
			ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale));
		mergeFields.add(MergeFieldKanton.fallIdTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.fallIdTitle, ServerMessageUtil.getMessage("Reports_fallIdTitle",
			locale));
		mergeFields.add(MergeFieldKanton.vornameTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.vornameTitle,
			ServerMessageUtil.getMessage("Reports_vornameTitle", locale));
		mergeFields.add(MergeFieldKanton.nachnameTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.nachnameTitle,
			ServerMessageUtil.getMessage("Reports_nachnameTitle", locale));
		mergeFields.add(MergeFieldKanton.geburtsdatumTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.geburtsdatumTitle,
			ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale));
		mergeFields.add(MergeFieldKanton.betreuungVonTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.betreuungVonTitle,
			ServerMessageUtil.getMessage("Reports_betreuungVonTitle", locale));
		mergeFields.add(MergeFieldKanton.betreuungBisTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.betreuungBisTitle,
			ServerMessageUtil.getMessage("Reports_betreuungBisTitle", locale));
		mergeFields.add(MergeFieldKanton.bgPensumKantonTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.bgPensumKantonTitle,
			ServerMessageUtil.getMessage("Reports_bgPensumKantonTitle", locale));
		mergeFields.add(MergeFieldKanton.bgPensumGemeindeTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.bgPensumGemeindeTitle,
			ServerMessageUtil.getMessage("Reports_bgPensumGemeindeTitle", locale));
		mergeFields.add(MergeFieldKanton.bgPensumTotalTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.bgPensumTotalTitle,
			ServerMessageUtil.getMessage("Reports_bgPensumTotalTitle", locale));
		mergeFields.add(MergeFieldKanton.monatsanfangTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.monatsanfangTitle,
			ServerMessageUtil.getMessage("Reports_monatsanfangTitle", locale));
		mergeFields.add(MergeFieldKanton.monatsendeTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.monatsendeTitle,
			ServerMessageUtil.getMessage("Reports_monatsendeTitle", locale));
		mergeFields.add(MergeFieldKanton.platzbelegungTageTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.platzbelegungTageTitle,
			ServerMessageUtil.getMessage("Reports_platzbelegungTageTitle", locale));
		mergeFields.add(MergeFieldKanton.kostenCHFTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.kostenCHFTitle,
			ServerMessageUtil.getMessage("Reports_kostenCHFTitle", locale));
		mergeFields.add(MergeFieldKanton.vollkostenTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.vollkostenTitle,
			ServerMessageUtil.getMessage("Reports_vollkostenTitle", locale));
		mergeFields.add(MergeFieldKanton.elternbeitragTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.elternbeitragTitle,
			ServerMessageUtil.getMessage("Reports_elternbeitragTitle", locale));
		mergeFields.add(MergeFieldKanton.gutscheinKantonTitel.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.gutscheinKantonTitel,
			ServerMessageUtil.getMessage("Reports_gutscheinKantonTitel", locale));
		mergeFields.add(MergeFieldKanton.gutscheinGemeindeTitel.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.gutscheinGemeindeTitel,
			ServerMessageUtil.getMessage("Reports_gutscheinGemeindeTitel", locale));
		mergeFields.add(MergeFieldKanton.gutscheinTotalTitel.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.gutscheinTotalTitel,
			ServerMessageUtil.getMessage("Reports_gutscheinTotalTitel", locale));
		mergeFields.add(MergeFieldKanton.babyFaktorTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.babyFaktorTitle,
			ServerMessageUtil.getMessage("Reports_babyFaktorTitle", locale));
		mergeFields.add(MergeFieldKanton.institutionTitle.getMergeField());
		excelMerger.addValue(
			MergeFieldKanton.institutionTitle,
			ServerMessageUtil.getMessage("Reports_institutionTitle", locale));
		mergeFields.add(MergeFieldKanton.totalTitle.getMergeField());
		excelMerger.addValue(MergeFieldKanton.totalTitle, ServerMessageUtil.getMessage("Reports_totalTitle", locale));
	}
}
