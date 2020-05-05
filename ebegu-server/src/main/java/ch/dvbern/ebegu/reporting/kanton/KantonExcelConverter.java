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

import ch.dvbern.ebegu.enums.reporting.MergeFieldKanton;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class KantonExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<KantonDataRow> data,
		@Nonnull Locale locale,
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis
	) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		addHeaders(excelMerger, locale);

		excelMerger.addValue(MergeFieldKanton.auswertungVon, datumVon);
		excelMerger.addValue(MergeFieldKanton.auswertungBis, datumBis);

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldKanton.repeatKantonRow);
			excelRowGroup.addValue(MergeFieldKanton.gemeinde, dataRow.getGemeinde());
			excelRowGroup.addValue(MergeFieldKanton.bgNummer, dataRow.getBgNummer());
			excelRowGroup.addValue(MergeFieldKanton.gesuchId, dataRow.getGesuchId());
			excelRowGroup.addValue(MergeFieldKanton.name, dataRow.getName());
			excelRowGroup.addValue(MergeFieldKanton.vorname, dataRow.getVorname());
			excelRowGroup.addValue(MergeFieldKanton.geburtsdatum, dataRow.getGeburtsdatum());
			excelRowGroup.addValue(MergeFieldKanton.zeitabschnittVon, dataRow.getZeitabschnittVon());
			excelRowGroup.addValue(MergeFieldKanton.zeitabschnittBis, dataRow.getZeitabschnittBis());
			BigDecimal bgPensumTotal = dataRow.getBgPensumTotal();
			excelRowGroup.addValue(MergeFieldKanton.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldKanton.betreuungsTyp, dataRow.getBetreuungsTyp());
			if (bgPensumTotal.compareTo(BigDecimal.ZERO) > 0) {
				excelRowGroup.addValue(MergeFieldKanton.bgPensumKanton, dataRow.getBgPensumKanton());
				excelRowGroup.addValue(MergeFieldKanton.bgPensumGemeinde, dataRow.getBgPensumGemeinde());
				excelRowGroup.addValue(MergeFieldKanton.bgPensumTotal, bgPensumTotal);
				excelRowGroup.addValue(MergeFieldKanton.elternbeitrag, dataRow.getElternbeitrag());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungKanton, dataRow.getVerguenstigungKanton());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungGemeinde, dataRow.getVerguenstigungGemeinde());
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungTotal, dataRow.getVerguenstigungTotal());
			} else {
				excelRowGroup.addValue(MergeFieldKanton.bgPensumKanton, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.bgPensumGemeinde, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.bgPensumTotal, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.elternbeitrag, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungKanton, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungGemeinde, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldKanton.verguenstigungTotal, BigDecimal.ZERO);
			}
		});

		return excelMerger;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale) {
		excelMerger.addValue(MergeFieldKanton.kantonTitle, ServerMessageUtil.getMessage("Reports_kantonTitle", locale));
		excelMerger.addValue(MergeFieldKanton.parameterTitle, ServerMessageUtil.getMessage("Reports_parameterTitle", locale));
		excelMerger.addValue(MergeFieldKanton.vonTitle, ServerMessageUtil.getMessage("Reports_vonTitle", locale));
		excelMerger.addValue(MergeFieldKanton.bisTitle, ServerMessageUtil.getMessage("Reports_bisTitle", locale));
		excelMerger.addValue(MergeFieldKanton.gemeindeTitle, ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale));
		excelMerger.addValue(MergeFieldKanton.fallIdTitle, ServerMessageUtil.getMessage("Reports_fallIdTitle", locale));
		excelMerger.addValue(MergeFieldKanton.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale));
		excelMerger.addValue(MergeFieldKanton.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle", locale));
		excelMerger.addValue(MergeFieldKanton.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale));
		excelMerger.addValue(MergeFieldKanton.betreuungVonTitle, ServerMessageUtil.getMessage("Reports_betreuungVonTitle", locale));
		excelMerger.addValue(MergeFieldKanton.betreuungBisTitle, ServerMessageUtil.getMessage("Reports_betreuungBisTitle", locale));
		excelMerger.addValue(MergeFieldKanton.bgPensumKantonTitle, ServerMessageUtil.getMessage("Reports_bgPensumKantonTitle", locale));
		excelMerger.addValue(MergeFieldKanton.bgPensumGemeindeTitle, ServerMessageUtil.getMessage("Reports_bgPensumGemeindeTitle", locale));
		excelMerger.addValue(MergeFieldKanton.bgPensumTotalTitle, ServerMessageUtil.getMessage("Reports_bgPensumTotalTitle", locale));
		excelMerger.addValue(MergeFieldKanton.monatsanfangTitle, ServerMessageUtil.getMessage("Reports_monatsanfangTitle", locale));
		excelMerger.addValue(MergeFieldKanton.monatsendeTitle, ServerMessageUtil.getMessage("Reports_monatsendeTitle", locale));
		excelMerger.addValue(MergeFieldKanton.platzbelegungTageTitle, ServerMessageUtil.getMessage("Reports_platzbelegungTageTitle", locale));
		excelMerger.addValue(MergeFieldKanton.kostenCHFTitle, ServerMessageUtil.getMessage("Reports_kostenCHFTitle", locale));
		excelMerger.addValue(MergeFieldKanton.vollkostenTitle, ServerMessageUtil.getMessage("Reports_vollkostenTitle", locale));
		excelMerger.addValue(MergeFieldKanton.elternbeitragTitle, ServerMessageUtil.getMessage("Reports_elternbeitragTitle", locale));
		excelMerger.addValue(MergeFieldKanton.gutscheinKantonTitel, ServerMessageUtil.getMessage("Reports_gutscheinKantonTitel", locale));
		excelMerger.addValue(MergeFieldKanton.gutscheinGemeindeTitel, ServerMessageUtil.getMessage("Reports_gutscheinGemeindeTitel", locale));
		excelMerger.addValue(MergeFieldKanton.gutscheinTotalTitel, ServerMessageUtil.getMessage("Reports_gutscheinTotalTitel", locale));
		excelMerger.addValue(MergeFieldKanton.babyFaktorTitle, ServerMessageUtil.getMessage("Reports_babyFaktorTitle", locale));
		excelMerger.addValue(MergeFieldKanton.institutionTitle, ServerMessageUtil.getMessage("Reports_institutionTitle", locale));
		excelMerger.addValue(MergeFieldKanton.totalTitle, ServerMessageUtil.getMessage("Reports_totalTitle", locale));
	}
}
