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

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.reporting.MergeFieldZemis;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

public class KinderMitZemisNummerExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<KindMitZemisNummerDataRow> data, @Nonnull Integer lastenausgleichJahr) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();
		excelMerger.addValue(MergeFieldZemis.jahr, ""+ lastenausgleichJahr);

		data.forEach(dataRow -> {
			ExcelMergerDTO rowGroup = excelMerger.createGroup(MergeFieldZemis.repeatRow);
			rowGroup.addValue(MergeFieldZemis.fall, dataRow.getFall());
			rowGroup.addValue(MergeFieldZemis.periode, dataRow.getPeriode());
			rowGroup.addValue(MergeFieldZemis.gemeinde, dataRow.getGemeinde());
			rowGroup.addValue(MergeFieldZemis.name, dataRow.getName());
			rowGroup.addValue(MergeFieldZemis.kindNummer, dataRow.getKindNummer());
			rowGroup.addValue(MergeFieldZemis.vorname, dataRow.getVorname());
			rowGroup.addValue(MergeFieldZemis.geburtsdatum, dataRow.getGeburtsdatum());
			rowGroup.addValue(MergeFieldZemis.zemisNummer, dataRow.getZemisNummer());
			rowGroup.addValue(MergeFieldZemis.keinSelbstbehaltFuerGemeinde, dataRow.isKeinSelbstbehaltFuerGemeinde());
		});

		return excelMerger;
	}
}
