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

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungen;
import ch.dvbern.oss.lib.excelmerger.*;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Dependent
public class ReportZahlungenExcelConverter implements ExcelConverter {

    @Override
    public void applyAutoSize(@Nonnull Sheet sheet) {
    }

    public void mergeRows(@Nonnull RowFiller rowFiller, @Nonnull List<ZahlungenDataRow> reportData) {
        reportData.forEach(row -> {
            ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();
            excelRowGroup.addValue(MergeFieldZahlungen.zahlungslaufTitle, row.getZahlungslaufTitle());
            excelRowGroup.addValue(MergeFieldZahlungen.faelligkeitsDatum, row.getZahlungsFaelligkeitsDatum());
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

    @Nonnull
    public XSSFSheet mergeHeaders(
        @Nonnull XSSFSheet sheet,
        @Nonnull Gesuchsperiode periode,
        @Nullable Gemeinde gemeinde,
        @Nullable Institution institution
    ) throws ExcelMergeException {

        ExcelMergerDTO excelMergerDTO = new ExcelMergerDTO();
        List<MergeField<?>> mergeFields = new ArrayList<>();

        mergeFields.add(MergeFieldZahlungen.periodeParam.getMergeField());
        excelMergerDTO.addValue(
            MergeFieldZahlungen.periodeParam,
            periode.getGesuchsperiodeString()
        );
        mergeFields.add(MergeFieldZahlungen.gemeindeParam.getMergeField());
        excelMergerDTO.addValue(
            MergeFieldZahlungen.gemeindeParam,
            gemeinde != null ? gemeinde.getName() : ""
        );
        mergeFields.add(MergeFieldZahlungen.institutionParam.getMergeField());
        excelMergerDTO.addValue(
            MergeFieldZahlungen.institutionParam,
            institution != null ? institution.getName() : ""
        );
        mergeFields.add(MergeFieldZahlungen.timestampParam.getMergeField());
        excelMergerDTO.addValue(
            MergeFieldZahlungen.timestampParam,
            LocalDateTime.now()
        );

        ExcelMerger.mergeData(sheet, mergeFields, excelMergerDTO);

        return sheet;
    }
}
