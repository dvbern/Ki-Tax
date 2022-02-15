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

package ch.dvbern.ebegu.reporting.kanton.mitarbeiterinnen;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.poi.ss.usermodel.Sheet;

import ch.dvbern.ebegu.enums.reporting.MergeFieldMitarbeiterinnen;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Excel Converter fuer die Statistik von MitarbeiterInnen
 */
@Dependent
public class MitarbeiterinnenExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<MitarbeiterinnenDataRow> data,
		@Nonnull Locale locale,
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Mandant mandant
	) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		addHeaders(excelMerger, locale, mandant);

		excelMerger.addValue(MergeFieldMitarbeiterinnen.auswertungVon, datumVon);
		excelMerger.addValue(MergeFieldMitarbeiterinnen.auswertungBis, datumBis);

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldMitarbeiterinnen.repeatMitarbeiterinnenRow);
			excelRowGroup.addValue(MergeFieldMitarbeiterinnen.name, dataRow.getName());
			excelRowGroup.addValue(MergeFieldMitarbeiterinnen.vorname, dataRow.getVorname());
			excelRowGroup.addValue(MergeFieldMitarbeiterinnen.verantwortlicheGesuche, dataRow.getVerantwortlicheGesuche());
			excelRowGroup.addValue(MergeFieldMitarbeiterinnen.verfuegungenAusgestellt, dataRow.getVerfuegungenAusgestellt());
		});

		return excelMerger;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale, @Nonnull Mandant mandant) {
		excelMerger.addValue(MergeFieldMitarbeiterinnen.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle", locale, mandant));
		excelMerger.addValue(MergeFieldMitarbeiterinnen.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale, mandant));
		excelMerger.addValue(MergeFieldMitarbeiterinnen.anzahlVerGesucheTitle, ServerMessageUtil.getMessage("Reports_anzahlVerGesucheTitle", locale, mandant));
		excelMerger.addValue(MergeFieldMitarbeiterinnen.verfuegungAusgestelltTitle, ServerMessageUtil.getMessage("Reports_verfuegungAusgestelltTitle", locale, mandant));
		excelMerger.addValue(MergeFieldMitarbeiterinnen.vonTitle, ServerMessageUtil.getMessage("Reports_vonTitle", locale, mandant));
		excelMerger.addValue(MergeFieldMitarbeiterinnen.bisTitle, ServerMessageUtil.getMessage("Reports_bisTitle", locale, mandant));
		excelMerger.addValue(MergeFieldMitarbeiterinnen.mitarbeiterinnenTitle, ServerMessageUtil.getMessage("Reports_mitarbeiterinnenTitle", locale, mandant));
	}
}
