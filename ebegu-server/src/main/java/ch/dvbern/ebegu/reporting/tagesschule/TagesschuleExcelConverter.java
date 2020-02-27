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
package ch.dvbern.ebegu.reporting.tagesschule;

import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.enums.reporting.MergeFieldTagesschule;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class TagesschuleExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<TagesschuleDataRow> data,
		@Nonnull Locale locale,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull EinstellungenTagesschule einstellungenTagesschule) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		excelMerger.addValue(MergeFieldTagesschule.tagesschuleOhneFinSitTitle, data.get(0).getTagesschuleName());
		excelMerger.addValue(MergeFieldTagesschule.periode, gesuchsperiode.getGesuchsperiodeDisplayName(locale));

		addHeaders(excelMerger, locale, einstellungenTagesschule);

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldTagesschule.repeatRow);
			excelRowGroup.addValue(MergeFieldTagesschule.nachnameKind, dataRow.getNachnameKind());
			excelRowGroup.addValue(MergeFieldTagesschule.vornameKind, dataRow.getVornameKind());
			excelRowGroup.addValue(MergeFieldTagesschule.geburtsdatumKind, dataRow.getGeburtsdatum());
			excelRowGroup.addValue(MergeFieldTagesschule.referenznummer, dataRow.getReferenznummer());
//			excelRowGroup.addValue(MergeFieldTagesschule.ab, dataRow.getAb());
			excelRowGroup.addValue(MergeFieldTagesschule.status, dataRow.getStatus().toString());

			setAnmeldungenForModule(dataRow, einstellungenTagesschule, excelRowGroup);
		});

		return excelMerger;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale,
		@Nonnull EinstellungenTagesschule einstellungenTagesschule) {
		excelMerger.addValue(MergeFieldTagesschule.nachnameKindTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle",	locale));
		excelMerger.addValue(MergeFieldTagesschule.vornameKindTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.referenznummerTitle, ServerMessageUtil.getMessage(
			"Reports_bgNummerTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.abTitle, ServerMessageUtil.getMessage("Reports_abTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.statusTitle, ServerMessageUtil.getMessage("Reports_statusTitle", locale));

		einstellungenTagesschule.getModulTagesschuleGroups().forEach(group -> {
			boolean first = true;
			for (ModulTagesschule module : group.getModule()) {
				excelMerger.addValue(MergeFieldTagesschule.repeatCol, "");
				if (first) {
					excelMerger.addValue(MergeFieldTagesschule.modulName, group.getBezeichnung().findTextByLocale(locale));
					first = false;
				} else {
					excelMerger.addValue(MergeFieldTagesschule.modulName, "");
				}
				excelMerger.addValue(MergeFieldTagesschule.wochentag,
					module.getWochentag().getDisplayName(TextStyle.SHORT, locale));
			}
		});

	}

	private void setAnmeldungenForModule(
		@Nonnull TagesschuleDataRow dataRow,
		@Nonnull EinstellungenTagesschule einstellungenTagesschule,
		@Nonnull ExcelMergerDTO excelRowGroup) {
			einstellungenTagesschule.getModulTagesschuleGroups().forEach(group -> {
				group.getModule().forEach(module -> {
					if (isAngemeldet(module, dataRow)) {
						excelRowGroup.addValue(MergeFieldTagesschule.angemeldet, "X");
					} else {
						excelRowGroup.addValue(MergeFieldTagesschule.angemeldet, "");
					}
				});
			});
	}

	private boolean isAngemeldet(ModulTagesschule module, TagesschuleDataRow dataRow) {
		if (dataRow.getAnmeldungTagesschule().getBelegungTagesschule() != null) {
			for (BelegungTagesschuleModul m :
				dataRow.getAnmeldungTagesschule().getBelegungTagesschule().getBelegungTagesschuleModule()) {
				if (m.getModulTagesschule().getId().equals(module.getId())) {
					return true;
				}
			}
		}
		return false;
	}
}
