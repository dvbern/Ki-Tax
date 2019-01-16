/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

package ch.dvbern.ebegu.reporting.benutzer;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.enums.reporting.MergeFieldBenutzer;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Excel Converter fuer die Statistik von Benutzern
 */
@Dependent
public class BenutzerExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<BenutzerDataRow> data, @Nonnull Locale locale) {
		checkNotNull(data);

		ExcelMergerDTO sheet = new ExcelMergerDTO();

		addHeaders(sheet, locale);

		sheet.addValue(MergeFieldBenutzer.stichtag, LocalDate.now());

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = sheet.createGroup(MergeFieldBenutzer.repeatBenutzerRow);
			excelRowGroup.addValue(MergeFieldBenutzer.nachname, dataRow.getNachname());
			excelRowGroup.addValue(MergeFieldBenutzer.vorname, dataRow.getVorname());
			excelRowGroup.addValue(MergeFieldBenutzer.username, dataRow.getUsername());
			excelRowGroup.addValue(MergeFieldBenutzer.email, dataRow.getEmail());
			excelRowGroup.addValue(MergeFieldBenutzer.role, dataRow.getRole());
			excelRowGroup.addValue(MergeFieldBenutzer.roleGueltigAb, dataRow.getRoleGueltigAb());
			excelRowGroup.addValue(MergeFieldBenutzer.roleGueltigBis, dataRow.getRoleGueltigBis());
			excelRowGroup.addValue(MergeFieldBenutzer.gemeinden, dataRow.getGemeinden());
			excelRowGroup.addValue(MergeFieldBenutzer.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldBenutzer.traegerschaft, dataRow.getTraegerschaft());
			String status = checkNotNull(dataRow.getStatus()).name().toLowerCase(locale);
			excelRowGroup.addValue(MergeFieldBenutzer.status, status);

			excelRowGroup.addValue(MergeFieldBenutzer.isKita, dataRow.isKita());
			excelRowGroup.addValue(MergeFieldBenutzer.isTagesfamilien, dataRow.isTagesfamilien());
			excelRowGroup.addValue(MergeFieldBenutzer.isTagesschule, dataRow.isTagesschule());
			excelRowGroup.addValue(MergeFieldBenutzer.isFerieninsel, dataRow.isFerieninsel());

			excelRowGroup.addValue(MergeFieldBenutzer.isJugendamt, dataRow.isJugendamt());
			excelRowGroup.addValue(MergeFieldBenutzer.isSchulamt, dataRow.isSchulamt());
		});
		return sheet;
	}

	private void addHeaders(ExcelMergerDTO sheet, Locale locale) {
		sheet.addValue(MergeFieldBenutzer.usernameTitle, ServerMessageUtil.getMessage("Reports_usernameTitle", locale));
		sheet.addValue(MergeFieldBenutzer.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale));
		sheet.addValue(MergeFieldBenutzer.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle", locale));
		sheet.addValue(MergeFieldBenutzer.emailTitle, ServerMessageUtil.getMessage("Reports_emailTitle", locale));
		sheet.addValue(MergeFieldBenutzer.roleTitle, ServerMessageUtil.getMessage("Reports_roleTitle", locale));
		sheet.addValue(MergeFieldBenutzer.roleGueltigBisTitel, ServerMessageUtil.getMessage("Reports_roleGueltigBisTitel", locale));
		sheet.addValue(MergeFieldBenutzer.gemeindenTitle, ServerMessageUtil.getMessage("Reports_gemeindenTitle", locale));
		sheet.addValue(MergeFieldBenutzer.institutionTitle, ServerMessageUtil.getMessage("Reports_institutionTitle", locale));
		sheet.addValue(MergeFieldBenutzer.traegerschaftTitle, ServerMessageUtil.getMessage("Reports_traegerschaftTitle", locale));
		sheet.addValue(MergeFieldBenutzer.kitaTitel, ServerMessageUtil.getMessage("Reports_kitaTitel", locale));
		sheet.addValue(MergeFieldBenutzer.tagesfamilienTitle, ServerMessageUtil.getMessage("Reports_tagesfamilienTitle", locale));
		sheet.addValue(MergeFieldBenutzer.tagesschulenTitel, ServerMessageUtil.getMessage("Reports_tagesschulenTitel", locale));
		sheet.addValue(MergeFieldBenutzer.ferieninselTitle, ServerMessageUtil.getMessage("Reports_ferieninselTitle", locale));
		sheet.addValue(MergeFieldBenutzer.isJugendamtTitle, ServerMessageUtil.getMessage("Reports_isJugendamtTitle", locale));
		sheet.addValue(MergeFieldBenutzer.isSchulamtTitle, ServerMessageUtil.getMessage("Reports_isSchulamtTitle", locale));
		sheet.addValue(MergeFieldBenutzer.statusTitle, ServerMessageUtil.getMessage("Reports_statusTitle", locale));
		sheet.addValue(MergeFieldBenutzer.stichtagTitle, ServerMessageUtil.getMessage("Reports_stichtagTitle", locale));
		sheet.addValue(MergeFieldBenutzer.reportBenutzerTitle, ServerMessageUtil.getMessage("Reports_reportBenutzerTitle", locale));
	}
}
