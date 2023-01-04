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

package ch.dvbern.ebegu.util;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

public abstract class ReportUtil {
	public static void createCellWithFormula(SXSSFRow targetRow, CellStyle cellStyle, int cellNbr, String formula) {
		SXSSFCell cellSumme = targetRow.createCell(cellNbr);
		cellSumme.setCellFormula(formula);
		cellSumme.setCellStyle(cellStyle);
		cellSumme.setCellType(CellType.FORMULA);
	}

	public static void fillXCellWithStyle(SXSSFRow targetRow, CellStyle cellStyle, int firstCellToFill, int lastCellToFill) {
		for (int i = firstCellToFill; i <= lastCellToFill; i++) {
			SXSSFCell cell = targetRow.createCell(i);
			cell.setCellValue("");
			cell.setCellStyle(cellStyle);
		}
	}

	public static CellStyle createBasicStyleSumRow(SXSSFSheet sheet) {
		CellStyle basicStyle = sheet.getWorkbook().createCellStyle();
		basicStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		basicStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		basicStyle.setBorderBottom(BorderStyle.THIN);
		basicStyle.setBorderLeft(BorderStyle.THIN);
		basicStyle.setBorderRight(BorderStyle.THIN);
		return basicStyle;
	}

	public static CellStyle createProcentStyle(SXSSFSheet sheet, CellStyle basicStyle) {
		basicStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat("0%"));
		return basicStyle;
	}

	public static CellStyle createNumberStyle(SXSSFSheet sheet, CellStyle basicStyle) {
		basicStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat("0.00"));
		return basicStyle;
	}
}
