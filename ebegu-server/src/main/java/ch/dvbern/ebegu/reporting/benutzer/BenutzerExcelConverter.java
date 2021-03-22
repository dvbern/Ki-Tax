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

		ExcelMergerDTO mergerDTO = new ExcelMergerDTO();

		addHeaders(mergerDTO, locale);

		mergerDTO.addValue(MergeFieldBenutzer.stichtag, LocalDate.now());

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = mergerDTO.createGroup(MergeFieldBenutzer.repeatBenutzerRow);
			excelRowGroup.addValue(MergeFieldBenutzer.nachname, dataRow.getNachname());
			excelRowGroup.addValue(MergeFieldBenutzer.vorname, dataRow.getVorname());
			excelRowGroup.addValue(MergeFieldBenutzer.username, dataRow.getUsername());
			excelRowGroup.addValue(MergeFieldBenutzer.email, dataRow.getEmail());
			excelRowGroup.addValue(MergeFieldBenutzer.role, dataRow.getRole());
			excelRowGroup.addValue(MergeFieldBenutzer.roleGueltigAb, dataRow.getRoleGueltigAb());
			excelRowGroup.addValue(MergeFieldBenutzer.roleGueltigBis, dataRow.getRoleGueltigBis());
			excelRowGroup.addValue(MergeFieldBenutzer.gemeinden, dataRow.getGemeinden());
			excelRowGroup.addValue(MergeFieldBenutzer.angebotGemeinden, dataRow.getAngebotGemeinden());
			excelRowGroup.addValue(MergeFieldBenutzer.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldBenutzer.traegerschaft, dataRow.getTraegerschaft());
			excelRowGroup.addValue(MergeFieldBenutzer.status, ServerMessageUtil.translateEnumValue(checkNotNull(dataRow.getStatus()), locale));

			excelRowGroup.addValue(MergeFieldBenutzer.isKita, dataRow.isKita());
			excelRowGroup.addValue(MergeFieldBenutzer.isTagesfamilien, dataRow.isTagesfamilien());
			excelRowGroup.addValue(MergeFieldBenutzer.isTagesschule, dataRow.isTagesschule());
			excelRowGroup.addValue(MergeFieldBenutzer.isFerieninsel, dataRow.isFerieninsel());

			excelRowGroup.addValue(MergeFieldBenutzer.isJugendamt, dataRow.isJugendamt());
			excelRowGroup.addValue(MergeFieldBenutzer.isSchulamt, dataRow.isSchulamt());
		});
		return mergerDTO;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO mergerDTO, @Nonnull Locale locale) {
		mergerDTO.addValue(MergeFieldBenutzer.usernameTitle, ServerMessageUtil.getMessage("Reports_usernameTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.emailTitle, ServerMessageUtil.getMessage("Reports_emailTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.roleTitle, ServerMessageUtil.getMessage("Reports_roleTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.roleGueltigBisTitel, ServerMessageUtil.getMessage("Reports_roleGueltigBisTitel", locale));
		mergerDTO.addValue(MergeFieldBenutzer.gemeindenTitle, ServerMessageUtil.getMessage("Reports_gemeindenTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.angebotGemeindenTitle, ServerMessageUtil.getMessage("Reports_angebotGemeindenTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.institutionTitle, ServerMessageUtil.getMessage("Reports_institutionTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.traegerschaftTitle, ServerMessageUtil.getMessage("Reports_traegerschaftTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.kitaTitel, ServerMessageUtil.getMessage("Reports_kitaTitel", locale));
		mergerDTO.addValue(MergeFieldBenutzer.tagesfamilienTitle, ServerMessageUtil.getMessage("Reports_tagesfamilienTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.tagesschulenTitel, ServerMessageUtil.getMessage("Reports_tagesschulenTitel", locale));
		mergerDTO.addValue(MergeFieldBenutzer.ferieninselTitle, ServerMessageUtil.getMessage("Reports_ferieninselTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.isJugendamtTitle, ServerMessageUtil.getMessage("Reports_isJugendamtTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.isSchulamtTitle, ServerMessageUtil.getMessage("Reports_isSchulamtTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.statusTitle, ServerMessageUtil.getMessage("Reports_statusTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.stichtagTitle, ServerMessageUtil.getMessage("Reports_stichtagTitle", locale));
		mergerDTO.addValue(MergeFieldBenutzer.reportBenutzerTitle, ServerMessageUtil.getMessage("Reports_reportBenutzerTitle", locale));
	}
}
