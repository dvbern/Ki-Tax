/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.reporting.zahlungen;

import java.util.List;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungen;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import org.apache.poi.ss.usermodel.Sheet;

@Dependent
public class ReportZahlungenExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	public void mergeRows(@Nonnull RowFiller rowFiller, @Nonnull List<ZahlungenDataRow> reportData) {
		reportData.forEach(row -> {
			ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();
			excelRowGroup.addValue(MergeFieldZahlungen.zahlungslaufTitle, row.getZahlungslaufTitle());
			excelRowGroup.addValue(MergeFieldZahlungen.gemeinde, row.getGemeinde());
			excelRowGroup.addValue(MergeFieldZahlungen.institution, row.getInstitution());
			excelRowGroup.addValue(MergeFieldZahlungen.timestampZahlungslauf, row.getTimestampZahlungslauf());
			excelRowGroup.addValue(MergeFieldZahlungen.kindVorname, row.getKindVorname());
			excelRowGroup.addValue(MergeFieldZahlungen.kindNachname, row.getKindNachname());
			excelRowGroup.addValue(MergeFieldZahlungen.referenznummer, row.getReferenznummer());
			excelRowGroup.addValue(MergeFieldZahlungen.zeitabschnittVon, row.getZeitabschnittVon());
			excelRowGroup.addValue(MergeFieldZahlungen.zeitabschnittBis, row.getZeitabschnittBis());
			excelRowGroup.addValue(MergeFieldZahlungen.bgPensum, row.getBgPensum());
			excelRowGroup.addValue(MergeFieldZahlungen.betrag, row.getBetrag());
			excelRowGroup.addValue(MergeFieldZahlungen.korrektur, row.getKorrektur());
			excelRowGroup.addValue(MergeFieldZahlungen.ignorieren, row.getIgnorieren());
			excelRowGroup.addValue(MergeFieldZahlungen.ibanEltern, row.getIbanEltern());
			excelRowGroup.addValue(MergeFieldZahlungen.kontoEltern, row.getKontoEltern());
			rowFiller.fillRow(excelRowGroup);
		});
	}
}
