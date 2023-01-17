/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.reporting.lastenausgleich;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldLastenausgleichBGZeitabschnitte;
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
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

public class LastenausgleichBGZeitabschnitteExcelConverter implements ExcelConverter {

	private static final Integer TITLE_ROW_NUMBER = 7;

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}


	@Nonnull
	public Sheet mergeHeaders(
		@Nonnull Sheet sheet,
		@Nonnull Integer lastenausgleichJahr,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant) throws ExcelMergeException {

		ExcelMergerDTO excelMergerDTO = new ExcelMergerDTO();
		List<MergeField<?>> mergeFields = new ArrayList<>();

		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.lastenausgleichDatenTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.lastenausgleichDatenTitle,
			ServerMessageUtil.getMessage("Reports_lastenausgleichDatenTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.jahrTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.jahrTitle,
			ServerMessageUtil.getMessage("Reports_jahrTitel", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.jahr.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.jahr,
			lastenausgleichJahr
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.parameterTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.parameterTitle,
			ServerMessageUtil.getMessage("Reports_parameterTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.referenznummerTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.referenznummerTitle,
			ServerMessageUtil.getMessage("Reports_bgNummerTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.bfsNummerTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.bfsNummerTitle,
			ServerMessageUtil.getMessage("Reports_bfsNummerTitel", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.nameGemeindeTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.nameGemeindeTitle,
			ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.nachnameTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.nachnameTitle,
			ServerMessageUtil.getMessage("Reports_nachnameTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.vornameTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.vornameTitle,
			ServerMessageUtil.getMessage("Reports_vornameTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.geburtsdatumTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.geburtsdatumTitle,
			ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.vonTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.vonTitle,
			ServerMessageUtil.getMessage("Reports_betreuungVonTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.bisTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.bisTitle,
			ServerMessageUtil.getMessage("Reports_betreuungBisTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.institutionTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.institutionTitle,
			ServerMessageUtil.getMessage("Reports_institutionTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.betreuungsangebotTypTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.betreuungsangebotTypTitle,
			ServerMessageUtil.getMessage("Reports_betreuungsangebotTypTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.bgPensumTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.bgPensumTitle,
			ServerMessageUtil.getMessage("Reports_bgPensumTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.jaehrlichesBgPensumTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.jaehrlichesBgPensumTitle,
			ServerMessageUtil.getMessage("Reports_jaehrlichesBGPensumTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.keinSelbstbehaltDurchGemeindeTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.keinSelbstbehaltDurchGemeindeTitle,
			ServerMessageUtil.getMessage("Reports_keinSelbstbehaltDurchGemeindeTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.gutscheinTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.gutscheinTitle,
			ServerMessageUtil.getMessage("Reports_gutscheinTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.selbstbehaltGemeindeTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.selbstbehaltGemeindeTitle,
			ServerMessageUtil.getMessage("Reports_SelbstbehaltGemeindeTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.eingabeLastenausgleichTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.eingabeLastenausgleichTitle,
			ServerMessageUtil.getMessage("Reports_EingabeLastenausgleichTitle", locale, mandant)
		);
		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.korrekturTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.korrekturTitle,
			ServerMessageUtil.getMessage("Reports_korrekturTitle", locale, mandant)
		);

		ExcelMerger.mergeData(sheet, mergeFields, excelMergerDTO);

		return sheet;
	}

	public void mergeRows(
		RowFiller rowFiller,
		@Nonnull List<LastenausgleichBGZeitabschnittDataRow> data
	) {

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();

			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.referenznummer, dataRow.getReferenznummer());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.bfsNummer, dataRow.getBfsNummer());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.nameGemeinde, dataRow.getNameGemeinde());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.nachname, dataRow.getNachname());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.vorname, dataRow.getVorname());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.geburtsdatum, dataRow.getGeburtsdatum());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.von, dataRow.getVon());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.bis, dataRow.getBis());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.betreuungsangebotTyp, dataRow.getBetreuungsangebotTyp());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.bgPensum, dataRow.getBgPensum());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.keinSelbstbehaltDurchGemeinde, dataRow.getKeinSelbstbehaltDurchGemeinde());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.gutschein, dataRow.getGutschein());
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.isKorrektur, dataRow.getKorrektur());

			rowFiller.fillRow(excelRowGroup);
		});
		addTotalRow(rowFiller, data.size());
	}

	private void addTotalRow(RowFiller rowFiller, int nbrRow) {
		SXSSFSheet sheet = rowFiller.getSheet();
		SXSSFRow targetRow = sheet.createRow(sheet.getLastRowNum() + 1);
		CellStyle basicStyle = ReportUtil.createBasicStyleSumRow(sheet);
		CellStyle procentStyle = ReportUtil.createProcentStyle(sheet, basicStyle);
		CellStyle zahlStyle = ReportUtil.createNumberStyle(sheet, basicStyle);

		int firstRow = TITLE_ROW_NUMBER + 1;
		int lastRow = nbrRow + TITLE_ROW_NUMBER;
		ReportUtil.createCellWithFormula(targetRow, procentStyle, 16, "SUM(Q" + firstRow + ":Q" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 20, "SUM(U" + firstRow + ":U" + lastRow + ")");
		ReportUtil.createCellWithFormula(targetRow, zahlStyle, 21, "SUM(V" + firstRow + ":V" + lastRow + ")");
	}
}
