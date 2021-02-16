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
package ch.dvbern.ebegu.reporting.massenversand;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.reporting.MergeFieldMassenversand;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import com.google.common.base.Preconditions;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

//@Dependent
public class MassenversandExcelConverter implements ExcelConverter {

	public static final String EMPTY_STRING = "";
	private static final int MAX_KIND_COLS_IN_TEMPLATE = 10;

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<MassenversandDataRow> data,
		@Nonnull Locale locale,
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable Gesuchsperiode auswertungPeriode,
		boolean inklBgGesuche,
		boolean inklMischGesuche,
		boolean inklTsGesuche,
		boolean ohneErneuerungsgesuch,
		@Nullable String text) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		addHeaders(excelMerger, locale);

		excelMerger.addValue(MergeFieldMassenversand.auswertungVon, datumVon);
		excelMerger.addValue(MergeFieldMassenversand.auswertungBis, datumBis);
		if (auswertungPeriode != null) {
			excelMerger.addValue(MergeFieldMassenversand.auswertungPeriode, auswertungPeriode.getGesuchsperiodeString());
		}
		excelMerger.addValue(MergeFieldMassenversand.auswertungInklBgGesuche, inklBgGesuche);
		excelMerger.addValue(MergeFieldMassenversand.auswertungInklMischGesuche, inklMischGesuche);
		excelMerger.addValue(MergeFieldMassenversand.auswertungInklTsGesuche, inklTsGesuche);
		excelMerger.addValue(MergeFieldMassenversand.auswertungOhneFolgegesuch, ohneErneuerungsgesuch);
		excelMerger.addValue(MergeFieldMassenversand.auswertungText, text);

		insertRequiredColumns(data, excelMerger);

		data.forEach(dataRow -> {
			ExcelMergerDTO fallRowGroup = excelMerger.createGroup(MergeFieldMassenversand.repeatRow);
			fallRowGroup.addValue(MergeFieldMassenversand.gesuchsperiode, dataRow.getGesuchsperiode());
			fallRowGroup.addValue(MergeFieldMassenversand.gemeinde, dataRow.getGemeinde());
			fallRowGroup.addValue(MergeFieldMassenversand.fall, dataRow.getFall());
			fallRowGroup.addValue(MergeFieldMassenversand.gs1Name, dataRow.getGs1Name());
			fallRowGroup.addValue(MergeFieldMassenversand.gs1Vorname, dataRow.getGs1Vorname());
			fallRowGroup.addValue(MergeFieldMassenversand.gs1Mail, dataRow.getGs1Mail());
			fallRowGroup.addValue(MergeFieldMassenversand.gs2Name, dataRow.getGs2Name());
			fallRowGroup.addValue(MergeFieldMassenversand.gs2Vorname, dataRow.getGs2Vorname());
			fallRowGroup.addValue(MergeFieldMassenversand.gs2Mail, dataRow.getGs2Mail());
			fallRowGroup.addValue(MergeFieldMassenversand.adresse, dataRow.getAdresse());
			fallRowGroup.addValue(MergeFieldMassenversand.einreichungsart, dataRow.getEinreichungsart());
			fallRowGroup.addValue(MergeFieldMassenversand.status, dataRow.getStatus());
			fallRowGroup.addValue(MergeFieldMassenversand.typ, dataRow.getTyp());

			dataRow.getKinderCols().forEach(kindCol -> {
				fallRowGroup.addValue(MergeFieldMassenversand.kindName, kindCol.getKindName());
				fallRowGroup.addValue(MergeFieldMassenversand.kindVorname, kindCol.getKindVorname());
				fallRowGroup.addValue(MergeFieldMassenversand.kindGeburtsdatum, kindCol.getKindGeburtsdatum());
				fallRowGroup.addValue(MergeFieldMassenversand.kindInstitutionKita, kindCol.getKindInstitutionKita());
				fallRowGroup.addValue(MergeFieldMassenversand.kindInstitutionTagesfamilie, kindCol.getKindInstitutionTagesfamilie());
				fallRowGroup.addValue(MergeFieldMassenversand.kindInstitutionTagesschule, kindCol.getKindInstitutionTagesschule());
				fallRowGroup.addValue(MergeFieldMassenversand.kindInstitutionFerieninsel, kindCol.getKindInstitutionFerieninsel());
				fallRowGroup.addValue(MergeFieldMassenversand.kindInstitutionenWeitere, kindCol.getKindInstitutionenWeitere());
			});
		});

		return excelMerger;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale) {
		excelMerger.addValue(MergeFieldMassenversand.gemeindeTitle, ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.serienbriefeTitle, ServerMessageUtil.getMessage("Reports_serienbriefeTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.parameterTitle, ServerMessageUtil.getMessage("Reports_parameterTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.vonTitle, ServerMessageUtil.getMessage("Reports_vonTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.bisTitle, ServerMessageUtil.getMessage("Reports_bisTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.periodeTitle, ServerMessageUtil.getMessage("Reports_periodeTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.inklJAGesucheTitle, ServerMessageUtil.getMessage("Reports_inklJAGesucheTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.inklSCHGesucheTitle, ServerMessageUtil.getMessage("Reports_inklSCHGesucheTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.inklMischGesucheTitle, ServerMessageUtil.getMessage("Reports_inklMischGesucheTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.ohneFolgegesucheTitle, ServerMessageUtil.getMessage("Reports_ohneFolgegesucheTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.textTitle, ServerMessageUtil.getMessage("Reports_textTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.fallIdTitle, ServerMessageUtil.getMessage("Reports_fallIdTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.gesuchsteller1Title, ServerMessageUtil.getMessage("Reports_gesuchsteller1Title", locale));
		excelMerger.addValue(MergeFieldMassenversand.gesuchsteller2Title, ServerMessageUtil.getMessage("Reports_gesuchsteller2Title", locale));
		excelMerger.addValue(MergeFieldMassenversand.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.emailTitle, ServerMessageUtil.getMessage("Reports_emailTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.postanschriftTitle, ServerMessageUtil.getMessage("Reports_postanschriftTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.kindTitle, ServerMessageUtil.getMessage("Reports_kindTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.betreuungsartInstitutionenTitle, ServerMessageUtil.getMessage("Reports_betreuungsartInstitutionenTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.kitaTitel, ServerMessageUtil.getMessage("Reports_kitaTitel", locale));
		excelMerger.addValue(MergeFieldMassenversand.ferieninselTitle, ServerMessageUtil.getMessage("Reports_ferieninselTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.tagesfamilieTitle, ServerMessageUtil.getMessage("Reports_tagesfamilieTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.tagesschuleTitel, ServerMessageUtil.getMessage("Reports_tagesschuleTitel", locale));
		excelMerger.addValue(MergeFieldMassenversand.weitereInstitutionenTitle, ServerMessageUtil.getMessage("Reports_weitereInstitutionenTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.einreichungsartTitel, ServerMessageUtil.getMessage("Reports_einreichungsartTitel", locale));
		excelMerger.addValue(MergeFieldMassenversand.gesuchStatusTitle, ServerMessageUtil.getMessage("Reports_gesuchStatusTitle", locale));
		excelMerger.addValue(MergeFieldMassenversand.gesuchstypTitle, ServerMessageUtil.getMessage("Reports_gesuchstypTitle", locale));
	}

	private void insertRequiredColumns(List<MassenversandDataRow> data, ExcelMergerDTO sheet) {
		// Die maximale Anzahl Kinder ermitteln
		int maxKinder = 0;
		for (MassenversandDataRow familie : data) {
			int kinder = familie.getKinderCols().size();
			if (kinder > maxKinder) {
				maxKinder = kinder;
			}
		}
		Preconditions.checkState(
			maxKinder <= MAX_KIND_COLS_IN_TEMPLATE,
			"Es gibt mehr Kinder als Spalten (für Kinder) die im Template zur Verfügung stehen");

		IntStream.range(0, maxKinder).forEach(i ->
			IntStream.range(0, 11).forEach(j -> {
				// Pro Kind haben wir 11 Spalten
				sheet.addValue(MergeFieldMassenversand.repeatKind, EMPTY_STRING);
			})
		);
	}
}
