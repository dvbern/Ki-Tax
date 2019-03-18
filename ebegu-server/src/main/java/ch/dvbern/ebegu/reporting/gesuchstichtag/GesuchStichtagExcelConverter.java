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
package ch.dvbern.ebegu.reporting.gesuchstichtag;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.poi.ss.usermodel.Sheet;

import ch.dvbern.ebegu.enums.reporting.MergeFieldGesuchStichtag;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class GesuchStichtagExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
		sheet.autoSizeColumn(0); // bgNummer
		sheet.autoSizeColumn(1); // institution
		sheet.autoSizeColumn(2); // betreuungsTyp
		sheet.autoSizeColumn(3); // periode
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<GesuchStichtagDataRow> data, @Nonnull Locale locale) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		addHeaders(excelMerger, locale);

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldGesuchStichtag.repeatGesuchStichtagRow);
			excelRowGroup.addValue(MergeFieldGesuchStichtag.gemeinde, dataRow.getGemeinde());
			excelRowGroup.addValue(MergeFieldGesuchStichtag.bgNummer, dataRow.getBgNummer());
			excelRowGroup.addValue(MergeFieldGesuchStichtag.gesuchLaufNr, dataRow.getGesuchLaufNr());
			excelRowGroup.addValue(MergeFieldGesuchStichtag.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldGesuchStichtag.betreuungsTyp,
				ServerMessageUtil.translateEnumValue(BetreuungsangebotTyp.valueOf(dataRow.getBetreuungsTyp()), locale));
			excelRowGroup.addValue(MergeFieldGesuchStichtag.periode, dataRow.getPeriode());
			excelRowGroup.addValue(MergeFieldGesuchStichtag.nichtFreigegeben, dataRow.getNichtFreigegeben());
			excelRowGroup.addValue(MergeFieldGesuchStichtag.mahnungen, dataRow.getMahnungen());
			excelRowGroup.addValue(MergeFieldGesuchStichtag.beschwerde, dataRow.getBeschwerde());
		});

		return excelMerger;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale) {
		excelMerger.addValue(MergeFieldGesuchStichtag.gemeindeTitle, ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale));
		excelMerger.addValue(MergeFieldGesuchStichtag.bgNummerTitle, ServerMessageUtil.getMessage("Reports_bgNummerTitle", locale));
		excelMerger.addValue(MergeFieldGesuchStichtag.institutionTitle, ServerMessageUtil.getMessage("Reports_institutionTitle", locale));
		excelMerger.addValue(MergeFieldGesuchStichtag.angebotTitle, ServerMessageUtil.getMessage("Reports_angebotTitle", locale));
		excelMerger.addValue(MergeFieldGesuchStichtag.periodeTitle, ServerMessageUtil.getMessage("Reports_periodeTitle", locale));
		excelMerger.addValue(MergeFieldGesuchStichtag.gesuchLaufNrTitle, ServerMessageUtil.getMessage("Reports_gesuchLaufNrTitle", locale));
		excelMerger.addValue(MergeFieldGesuchStichtag.nichtFreigegebenTitle, ServerMessageUtil.getMessage("Reports_nichtFreigegebenTitle", locale));
		excelMerger.addValue(MergeFieldGesuchStichtag.mahnungenTitle, ServerMessageUtil.getMessage("Reports_mahnungenTitle", locale));
		excelMerger.addValue(MergeFieldGesuchStichtag.beschwerdeTitle, ServerMessageUtil.getMessage("Reports_beschwerdeTitle", locale));
	}
}
