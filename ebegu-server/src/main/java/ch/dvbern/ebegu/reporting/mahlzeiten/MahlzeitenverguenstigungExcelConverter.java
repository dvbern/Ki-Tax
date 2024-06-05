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

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldMahlzeitenverguenstigung;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static java.util.Objects.requireNonNull;

public class MahlzeitenverguenstigungExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
		// No autosizing defined for columns
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<MahlzeitenverguenstigungDataRow> data,
		@Nonnull Locale locale,
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Mandant mandant)
	{
		ExcelMergerDTO excelMerger = new ExcelMergerDTO();
		//Headers
		addHeaders(excelMerger, locale, mandant);
		//Parameters
		excelMerger.addValue(MergeFieldMahlzeitenverguenstigung.auswertungVon.getMergeField(), datumVon);
		excelMerger.addValue(MergeFieldMahlzeitenverguenstigung.auswertungBis.getMergeField(), datumBis);
		//Inhalt
		mergeRows(excelMerger, data, locale, mandant);
		return excelMerger;
	}


	public void mergeRows(
		@Nonnull ExcelMergerDTO excelMerger,
		@Nonnull List<MahlzeitenverguenstigungDataRow> data,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldMahlzeitenverguenstigung.repeatRow);
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.referenzNummer, dataRow.getReferenzNummer());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.fallNummer, dataRow.getFallNummer());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.institution, dataRow.getInstitution());
			excelRowGroup.addValue(
				MergeFieldMahlzeitenverguenstigung.angebot,
				ServerMessageUtil.translateEnumValue(requireNonNull(dataRow.getBetreuungsTyp()), locale, mandant));
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.traegerschaft, dataRow.getTraegerschaft());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.kindName, dataRow.getKindName());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.kindVorname, dataRow.getKindVorname());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.kindGeburtsdatum, dataRow.getKindGeburtsdatum());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.gs1Name, dataRow.getGs1Name());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.gs1Vorname, dataRow.getGs1Vorname());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.gs2Name, dataRow.getGs2Name());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.gs2Vorname, dataRow.getGs2Vorname());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.sozialhilfebezueger, dataRow.getSozialhilfeBezueger());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.iban, dataRow.getIban());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.massgebendesEinkommenVorFamAbzug, dataRow.getMassgebendesEinkommenVorFamAbzug());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.famGroesse, dataRow.getFamGroesse());
			excelRowGroup.addValue(MergeFieldMahlzeitenverguenstigung.massgebendesEinkommenNachFamAbzug, dataRow.getMassgebendesEinkommenNachFamAbzug());
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
		@Nonnull Locale locale,
		@Nonnull Mandant mandant) {

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.mahlzeitenverguenstigungTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_mahlzeitenverguenstigungTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.parameterTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_parameterTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.vonTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_vonTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.bisTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_bisTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.institutionTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_institutionTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.traegerschaftTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_traegerschaftTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.angebotTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_angebotTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.vornameTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_vornameTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.nachnameTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_nachnameTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.sozialhilfebezuegerTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_sozialhilfebezuegerTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.ibanTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_ibanTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.massgebendesEinkommenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_massEinkommenTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.massgebendesEinkommenVorFamAbzugTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_massgebendesEinkommenVorFamAbzugTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.famGroesseTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_famGroesseTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.massgebendesEinkommenNachFamAbzugTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_massgebendesEinkommenNachFamAbzugTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.geburtsdatumTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.gesuchsteller1Title.getMergeField(),
			ServerMessageUtil.getMessage("Reports_gesuchsteller1Title", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.gesuchsteller2Title.getMergeField(),
			ServerMessageUtil.getMessage("Reports_gesuchsteller2Title", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.referenzNummerTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_referenzNummerTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.fallNummerTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_fallnummerTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.betreuungTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_betreuungTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.betreuungVonTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_betreuungVonTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.betreuungBisTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_betreuungBisTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.mahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_mahlzeitenTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.anzahlHauptmahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_anzahlHauptmahlzeitenTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.anzahlNebenmahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_anzahlNebenmahlzeitenTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.kostenHauptmahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_kostenHauptmahlzeitenTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.kostenNebenmahlzeitenTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_kostenNebenmahlzeitenTitle", locale, mandant));

		mergerDTO.addValue(
			MergeFieldMahlzeitenverguenstigung.berechneteMahlzeitenverguenstigungTitle.getMergeField(),
			ServerMessageUtil.getMessage("Reports_berechneteMahlzeitenverguenstigungTitle", locale, mandant));
	}
}
