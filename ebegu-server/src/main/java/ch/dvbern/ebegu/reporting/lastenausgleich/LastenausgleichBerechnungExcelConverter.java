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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldLastenausgleichBerechnung;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
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
		@Nonnull LocalDateTime timestampLastenausgleichErstellt,
		@Nullable BigDecimal selbstbehaltPro100ProzentPlatz,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();
		excelMerger.addValue(MergeFieldLastenausgleichBerechnung.berechnungsjahr, ""+year);
		excelMerger.addValue(MergeFieldLastenausgleichBerechnung.selbstbehaltProHundertProzentPlatz, selbstbehaltPro100ProzentPlatz);

		//Titel
		this.setHeaders(excelMerger, locale, mandant, timestampLastenausgleichErstellt);

		data.forEach(dataRow -> {
			ExcelMergerDTO rowGroup = excelMerger.createGroup(MergeFieldLastenausgleichBerechnung.repeatRow);
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.gemeinde, dataRow.getGemeinde());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.bfsNummer, dataRow.getBfsNummer());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.verrechnungsjahr, dataRow.getVerrechnungsjahr());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.korrektur, dataRow.isKorrektur());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalBelegung, dataRow.getTotalBelegung());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalGutscheine, dataRow.getTotalGutscheine());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.selbstbehaltGemeinde, dataRow.getSelbstbehaltGemeinde());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalEingabeLastenausgleich, dataRow.getTotalEingabeLastenausgleich());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalBelegungMitSelbstbehalt, dataRow.getTotalBelegungMitSelbstbehalt());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalGutscheineMitSelbstbehalt, dataRow.getTotalGutscheineMitSelbstbehalt());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.kostenProHundertProzentPlatzMitSelbstbehalt, dataRow.getKostenPro100ProzentPlatz());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.eingabeLastenausgleichMitSelbstbehalt, dataRow.getEingabeLastenausgleich());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalBelegungOhneSelbstbehalt, dataRow.getTotalBelegungOhneSelbstbehalt());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.totalGutscheineOhneSelbstbehalt, dataRow.getTotalGutscheineOhneSelbstbehalt());
			rowGroup.addValue(MergeFieldLastenausgleichBerechnung.kostenFuerSelbstbehalt, dataRow.getKostenFuerSelbstbehalt());
		});

		return excelMerger;
	}

	private void setHeaders(
		ExcelMergerDTO excelMerger,
		Locale locale,
		Mandant mandant,
		@Nonnull LocalDateTime timestampLastenausgleichErstellt
	) {
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.lastenausgleichTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_lastenausgleichTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.parameterTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_parameterTitle", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.jahrTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_jahrTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.selbstbehaltProHundertProzentPlatzTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_selbstbehaltProHundertProzentPlatzTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.gemeindeTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.bfsNummerTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_bfsNummerTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.bgTotalGemaessStichtagTitle.getMergeField(),
			ServerMessageUtil.getMessage(
				"Reports_bgTotalGemaessStichtagTitle",
				locale,
				mandant,
				timestampLastenausgleichErstellt.format(Constants.DATE_FORMATTER)
			)
		);
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.totalBelegungTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_totalBelegungTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.totalGutscheineTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_totalGutscheineTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.bgMitSelbstbehaltTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_bgMitSelbstbehaltTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.kostenProPlatzTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_kostenProPlatzTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.selbstbehaltGemeindeTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_selbstbehaltGemeindeTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.eingabeLastenausgleichTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_eingabeLastenausgleichTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.korrekturTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_korrekturTitle", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.bgOhneSelbstbehaltTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_bgOhneSelbstbehaltTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.totalGutscheineEingabeLastTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_totalGutscheineEingabeLastTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.kostenFuerSelbstbehaltTitel.getMergeField(),
			ServerMessageUtil.getMessage("Reports_kostenFuerSelbstbehaltTitel", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ1.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ1", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ2.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ2", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ3.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ3", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ4.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ4", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ5_1.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ5_1", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ6_1.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ6_1", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ7_1.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ7_1", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ8_1.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ8_1", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ9_1.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ9_1", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ10_1.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ10_1", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ11_1.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ11_1", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ5_2.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ5_2", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ6_2.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ6_2", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ7_2.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ7_2", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ8_2.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ8_2", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ9_2.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ9_2", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ10_2.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ10_2", locale, mandant));
		excelMerger.addValue(
			MergeFieldLastenausgleichBerechnung.erlaeuterungZ11_2.getMergeField(),
			ServerMessageUtil.getMessage("Reports_erlaeuterungZ11_2", locale, mandant));
	}
}
