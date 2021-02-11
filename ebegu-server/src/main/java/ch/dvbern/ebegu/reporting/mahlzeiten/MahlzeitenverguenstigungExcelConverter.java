/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.reporting.mahlzeiten;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.reporting.MergeFieldMahlzeitenverguenstigung;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static java.util.Objects.requireNonNull;

public class MahlzeitenverguenstigungExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<MahlzeitenverguenstigungDataRow> data,
		@Nonnull Locale locale,
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis)
	{
		ExcelMergerDTO excelMerger = new ExcelMergerDTO();
		//Headers
		addHeaders(excelMerger, locale);
		//Parameters
		excelMerger.addValue(MergeFieldMahlzeitenverguenstigung.auswertungVon.getMergeField(), datumVon);
		excelMerger.addValue(MergeFieldMahlzeitenverguenstigung.auswertungBis.getMergeField(), datumBis);
		//Inhalt
		mergeRows(excelMerger, data, locale);
		return excelMerger;
	}


	public void mergeRows(
		@Nonnull ExcelMergerDTO excelMerger,
		@Nonnull List<MahlzeitenverguenstigungDataRow> data,
		@Nonnull Locale locale
	) {
		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldMahlzeitenverguenstigung.repeatRow);
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.bgNummer, dataRow.getBgNummer());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.institution, dataRow.getInstitution());
			excelRowGroup.addValue(
				MergeFieldMahlzeitenverguenstigung.angebot,
				ServerMessageUtil.translateEnumValue(requireNonNull(dataRow.getBetreuungsTyp()), locale));
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.traegerschaft, dataRow.getTraegerschaft());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.kindName, dataRow.getKindName());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.kindVorname, dataRow.getKindVorname());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.kindGeburtsdatum, dataRow.getKindGeburtsdatum());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.gs1Name, dataRow.getGs1Name());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.gs1Vorname, dataRow.getGs1Vorname());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.gs2Name, dataRow.getGs2Name());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.gs2Vorname, dataRow.getGs2Vorname());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.zeitabschnittVon, dataRow.getZeitabschnittVon());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.zeitabschnittBis, dataRow.getZeitabschnittBis());

			excelRowGroup.addValue(
				MergeFieldMahlzeitenverguenstigung.anzahlHauptmahlzeiten,
				dataRow.getAnzahlHauptmahlzeiten() != null ? dataRow.getAnzahlHauptmahlzeiten() :
					BigDecimal.ZERO);
			excelRowGroup.addValue(
				MergeFieldMahlzeitenverguenstigung.anzahlNebenmahlzeiten,
				dataRow.getAnzahlNebenmahlzeiten() != null ? dataRow.getAnzahlNebenmahlzeiten() : BigDecimal.ZERO);
			excelRowGroup.addValue(
				MergeFieldMahlzeitenverguenstigung.kostenHauptmahlzeiten,
				dataRow.getKostenHauptmahlzeiten() != null ? dataRow.getKostenHauptmahlzeiten() : BigDecimal.ZERO);
			excelRowGroup.addValue(
				MergeFieldMahlzeitenverguenstigung.kostenNebenmahlzeiten,
				dataRow.getKostenNebenmahlzeiten() != null ? dataRow.getKostenNebenmahlzeiten() : BigDecimal.ZERO);
			excelRowGroup.addValue(
				MergeFieldMahlzeitenverguenstigung.berechneteMahlzeitenverguenstigung,
				dataRow.getBerechneteMahlzeitenverguenstigung() != null ?
					dataRow.getBerechneteMahlzeitenverguenstigung() :
					BigDecimal.ZERO);
		});
	}

	private void addHeaders(
		@Nonnull ExcelMergerDTO mergerDTO,
		@Nonnull Locale locale) {

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.mahlzeitenverguenstigungTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_mahlzeitenverguenstigungTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.parameterTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_parameterTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.vonTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_vonTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.bisTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_bisTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.institutionTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_institutionTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.traegerschaftTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_traegerschaftTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.angebotTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_angebotTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.vornameTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_vornameTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.nachnameTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_nachnameTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.sozialhilfebezuegerTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_sozialhilfebezuegerTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.ibanTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_ibanTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.massgebendesEinkommenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_massgebendesEinkommenTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.massgebendesEinkommenVorFamAbzugTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_massgebendesEinkommenVorFamAbzugTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.famGroesseTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_famGroesseTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.massgebendesEinkommenNachFamAbzugTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_massgebendesEinkommenNachFamAbzugTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.geburtsdatumTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.gesuchsteller1Title.getMergeField(),
			ServerMessageUtil.getMessage("Reports_gesuchsteller1Title", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.gesuchsteller2Title.getMergeField(),
			ServerMessageUtil.getMessage("Reports_gesuchsteller2Title", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.bgNummerTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_bgNummerTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.betreuungTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_betreuungTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.betreuungVonTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_betreuungVonTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.betreuungBisTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_betreuungBisTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.mahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_mahlzeitenTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.anzahlHauptmahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_anzahlHauptmahlzeitenTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.anzahlNebenmahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_anzahlNebenmahlzeitenTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.kostenHauptmahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_kostenHauptmahlzeitenTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.kostenNebenmahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_kostenNebenmahlzeitenTitle", locale));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.berechneteMahlzeitenverguenstigungTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_berechneteMahlzeitenverguenstigungTitle", locale));
	}
}
