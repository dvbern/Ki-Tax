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

import ch.dvbern.ebegu.enums.reporting.MergeFieldLastenausgleichBerechnung;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastenausgleichBerechnungExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<LastenausgleichBerechnungDataRow> data,
		int year,
		@Nonnull BigDecimal selbstbehaltPro100ProzentPlatz,
		@Nonnull Locale locale
	) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();
		excelMerger.addValue(MergeFieldLastenausgleichBerechnung.berechnungsjahr, ""+year);
		excelMerger.addValue(MergeFieldLastenausgleichBerechnung.selbstbehaltProHundertProzentPlatz, selbstbehaltPro100ProzentPlatz);

		data.forEach(dataRow -> {
			ExcelMergerDTO rowGroup = excelMerger.createGroup(MergeFieldLastenausgleichBerechnung.repeatRow);
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.gemeinde, dataRow.getGemeinde());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.bfsNummer, dataRow.getBfsNummer());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.verrechnungsjahr, dataRow.getVerrechnungsjahr());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalBelegung, dataRow.getTotalBelegungMitSelbstbehalt());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalGutscheine, dataRow.getTotalGutscheineMitSelbstbehalt());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.kostenProHundertProzentPlatz, dataRow.getKostenPro100ProzentPlatz());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.selbstbehaltGemeinde, dataRow.getSelbstbehaltGemeinde());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.eingabeLastenausgleich, dataRow.getEingabeLastenausgleich());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.korrektur, dataRow.isKorrektur());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalBelegungOhneSelbstbehalt, dataRow.getTotalBelegungOhneSelbstbehalt());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalGutscheineOhneSelbstbehalt, dataRow.getTotalGutscheineOhneSelbstbehalt());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.kostenFuerSelbstbehalt, dataRow.getKostenFuerSelbstbehalt());
		});

		return excelMerger;
	}
}
