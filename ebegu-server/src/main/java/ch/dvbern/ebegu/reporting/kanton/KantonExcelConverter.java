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
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import org.apache.poi.ss.usermodel.Sheet;

import ch.dvbern.ebegu.enums.reporting.MergeFieldKanton;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class KantonExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<KantonDataRow> data, @Nonnull Locale lang, @Nonnull LocalDate datumVon, @Nonnull LocalDate datumBis) {
		checkNotNull(data);

		ExcelMergerDTO sheet = new ExcelMergerDTO();
		sheet.addValue(MergeFieldKanton.auswertungVon, datumVon);
		sheet.addValue(MergeFieldKanton.auswertungBis, datumBis);

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = sheet.createGroup(MergeFieldKanton.repeatKantonRow);
			excelRowGroup.addValue(MergeFieldKanton.bgNummer, dataRow.getBgNummer());
			excelRowGroup.addValue(MergeFieldKanton.gesuchId, dataRow.getGesuchId());
			excelRowGroup.addValue(MergeFieldKanton.name, dataRow.getName());
			excelRowGroup.addValue(MergeFieldKanton.vorname, dataRow.getVorname());
			excelRowGroup.addValue(MergeFieldKanton.geburtsdatum, dataRow.getGeburtsdatum());
			excelRowGroup.addValue(MergeFieldKanton.zeitabschnittVon, dataRow.getZeitabschnittVon());
			excelRowGroup.addValue(MergeFieldKanton.zeitabschnittBis, dataRow.getZeitabschnittBis());
			BigDecimal anspruchsPensum = dataRow.getBgPensum();
			excelRowGroup.addValue(MergeFieldKanton.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldKanton.betreuungsTyp, dataRow.getBetreuungsTyp());
			if (anspruchsPensum.compareTo(BigDecimal.ZERO) > 0) {
				excelRowGroup.addValue(MergeFieldKanton.bgPensum, anspruchsPensum);
				excelRowGroup.addValue(MergeFieldKanton.elternbeitrag, dataRow.getElternbeitrag());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigung, dataRow.getVerguenstigung());
			} else {
				excelRowGroup.addValue(MergeFieldKanton.bgPensum, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.elternbeitrag, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.verguenstigung, BigDecimal.ZERO);
			}
		});

		return sheet;
	}
}
