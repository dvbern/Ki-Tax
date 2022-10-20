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
package ch.dvbern.ebegu.reporting.lastenausgleich;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldLastenausgleichBGZeitabschnitte;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import org.apache.poi.ss.usermodel.Sheet;

public class LastenausgleichBGZeitabschnitteExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}


	@Nonnull
	public Sheet mergeHeaders(
		@Nonnull Sheet sheet,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant) throws ExcelMergeException {


		ExcelMergerDTO excelMergerDTO = new ExcelMergerDTO();
		List<MergeField<?>> mergeFields = new ArrayList<>();

		mergeFields.add(MergeFieldLastenausgleichBGZeitabschnitte.nameGemeindeTitle.getMergeField());
		excelMergerDTO.addValue(
			MergeFieldLastenausgleichBGZeitabschnitte.nameGemeindeTitle,
			ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale, mandant)
		);

		ExcelMerger.mergeData(sheet, mergeFields, excelMergerDTO);

		return sheet;
	}

	public void mergeRows(
		RowFiller rowFiller,
		@Nonnull List<LastenausgleichBGZeitabschnittDataRow> data,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();
			excelRowGroup.addValue(MergeFieldLastenausgleichBGZeitabschnitte.nameGemeinde, dataRow.getGemeinde());

			rowFiller.fillRow(excelRowGroup);
		});
	}
}
