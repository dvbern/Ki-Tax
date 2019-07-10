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

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.reporting.MergeFieldLastenausgleichKibon;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastenausgleichKibonExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<LastenausgleichKibonDataRow> data, int year) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		excelMerger.addValue(MergeFieldLastenausgleichKibon.jahr, BigDecimal.valueOf(year));

		data.forEach(dataRow -> {
			ExcelMergerDTO fallRowGroup = excelMerger.createGroup(MergeFieldLastenausgleichKibon.repeatRow);
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.bgNummer, dataRow.getBgNummer());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.kindName, dataRow.getKindName());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.kindVorname, dataRow.getKindVorname());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.kindGeburtsdatum, dataRow.getKindGeburtsdatum());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.zeitabschnittVon, dataRow.getZeitabschnittVon());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.zeitabschnittBis, dataRow.getZeitabschnittBis());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.bgPensum, dataRow.getBgPensum());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.institution, dataRow.getInstitution());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.betreuungsTyp, dataRow.getBetreuungsTyp());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.tarif, dataRow.getTarif());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.zusatz, dataRow.getZusatz());
			fallRowGroup.addValue(MergeFieldLastenausgleichKibon.gutschein, dataRow.getGutschein());
		});

		return excelMerger;
	}
}
