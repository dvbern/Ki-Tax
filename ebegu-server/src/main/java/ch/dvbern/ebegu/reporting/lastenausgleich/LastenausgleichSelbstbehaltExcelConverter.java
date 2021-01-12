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
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.reporting.MergeFieldLastenausgleichSelbstbehalt;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

public class LastenausgleichSelbstbehaltExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<LastenausgleichSelbstbehaltDataRow> data,
		int year,
		@Nonnull Locale locale
	) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		excelMerger.addValue(MergeFieldLastenausgleichSelbstbehalt.jahr, BigDecimal.valueOf(year));

		data.forEach(dataRow -> {
			ExcelMergerDTO fallRowGroup = excelMerger.createGroup(MergeFieldLastenausgleichSelbstbehalt.repeatRow);
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.bgNummer, dataRow.getBgNummer());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.kindName, dataRow.getKindName());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.kindVorname, dataRow.getKindVorname());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.kindGeburtsdatum, dataRow.getKindGeburtsdatum());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.zeitabschnittVon, dataRow.getZeitabschnittVon());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.zeitabschnittBis, dataRow.getZeitabschnittBis());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.bgPensum, dataRow.getBgPensum());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.institution, dataRow.getInstitution());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.betreuungsTyp,
				ServerMessageUtil.translateEnumValue(requireNonNull(dataRow.getBetreuungsTyp()), locale));
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.tarif,
				ServerMessageUtil.translateEnumValue(requireNonNull(dataRow.getTarif()), locale));
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.zusatz, dataRow.getZusatz());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.gutschein, dataRow.getGutschein());
			fallRowGroup.addValue(MergeFieldLastenausgleichSelbstbehalt.keinSelbstbehaltDurchGemeinde, dataRow.getKeinSelbstbehaltDurchGemeinde());
		});

		return excelMerger;
	}
}
