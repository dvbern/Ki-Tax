/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.gemeinden;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldGemeinden;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class GemeindenExcelConverter implements ExcelConverter {
	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {

	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<GemeindenDataRow> data, @Nonnull Mandant mandant, @Nonnull Locale locale) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		addHeaders(excelMerger, locale);

		excelMerger.addValue(MergeFieldGemeinden.mandant, mandant.getName());

		data.forEach(gemeindenDataRow -> {
			ExcelMergerDTO gemeindeInfoRegisterGroup = excelMerger.createGroup(MergeFieldGemeinden.rowGemeindeInfoRepeat);
			gemeindeInfoRegisterGroup.addValue(MergeFieldGemeinden.nameGemeinde, gemeindenDataRow.getNameGemeinde());
			gemeindeInfoRegisterGroup.addValue(MergeFieldGemeinden.bfsNummer, gemeindenDataRow.getBfsNummer());
			gemeindeInfoRegisterGroup.addValue(MergeFieldGemeinden.korrespondenzspracheGemeinde, gemeindenDataRow.getKorrespondenzspracheGemeinde());
			gemeindeInfoRegisterGroup.addValue(MergeFieldGemeinden.angebotBG, gemeindenDataRow.getAngebotBG());
			gemeindeInfoRegisterGroup.addValue(MergeFieldGemeinden.angebotTS, gemeindenDataRow.getAngebotTS());
			gemeindeInfoRegisterGroup.addValue(MergeFieldGemeinden.startdatumBG, gemeindenDataRow.getStartdatumBG());

			gemeindenDataRow.getGemeindenDaten().forEach(gemeindenDatenDataRow -> {
				ExcelMergerDTO gemeindeDatenRegisterGroup = excelMerger.createGroup(MergeFieldGemeinden.rowGemeindenZahlenRepeat);
				gemeindeDatenRegisterGroup.addValue(MergeFieldGemeinden.nameGemeinde, gemeindenDataRow.getNameGemeinde());
				gemeindeDatenRegisterGroup.addValue(MergeFieldGemeinden.bfsNummer, gemeindenDataRow.getBfsNummer());

				gemeindeDatenRegisterGroup.addValue(MergeFieldGemeinden.gesuchsperiode, gemeindenDatenDataRow.getGesuchsperiode());
				gemeindeDatenRegisterGroup.addValue(MergeFieldGemeinden.nachfrageErfuellt, gemeindenDatenDataRow.getNachfrageErfuellt());
				gemeindeDatenRegisterGroup.addValue(MergeFieldGemeinden.nachfrageAnzahl, gemeindenDatenDataRow.getNachfrageAnzahl());
				gemeindeDatenRegisterGroup.addValue(MergeFieldGemeinden.nachfrageDauer, gemeindenDatenDataRow.getNachfrageDauer());
				gemeindeDatenRegisterGroup.addValue(MergeFieldGemeinden.kostenlenkungAndere, gemeindenDatenDataRow.getKostenlenkungAndere());
				gemeindeDatenRegisterGroup.addValue(MergeFieldGemeinden.welcheKostenlenkungsmassnahmen, gemeindenDatenDataRow.getWelcheKostenlenkungsmassnahmen());
			});
		});

		return excelMerger;
	}

	private void addHeaders(ExcelMergerDTO excelMerger, Locale locale) {
		excelMerger.addValue(MergeFieldGemeinden.gemeindenTitle, ServerMessageUtil.getMessage("Reports_gemeindenTitle", locale));

		excelMerger.addValue(MergeFieldGemeinden.nameGemeindeTitle, ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.bfsNummerTitle, ServerMessageUtil.getMessage("Reports_bfsNummerTitle", locale));

		excelMerger.addValue(MergeFieldGemeinden.gutscheinausgabestelleTitle, ServerMessageUtil.getMessage("Reports_gutscheinausgabestelleTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.korrespondenzspracheGemeindeTitle, ServerMessageUtil.getMessage("Reports_korrespondenzspracheTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.angebotBGTitle, ServerMessageUtil.getMessage("Reports_angebotBGTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.angebotTSTitle, ServerMessageUtil.getMessage("Reports_angebotTSTitle", locale));

		excelMerger.addValue(MergeFieldGemeinden.limitierungKitaTitle, ServerMessageUtil.getMessage("Reports_limitierungKitaTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.erwerbspensumZuschlagTitle, ServerMessageUtil.getMessage("Reports_erwerbspensumZuschlagTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.startdatumBGTitle, ServerMessageUtil.getMessage("Reports_erwerbspensumZuschlagTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.kontingentierungTitle, ServerMessageUtil.getMessage("Reports_kontingentierungTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.gesuchsperiodeTitle, ServerMessageUtil.getMessage("Reports_periodeTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.nachfrageErfuelltTitle, ServerMessageUtil.getMessage("Reports_nachfrageErfuelltTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.nachfrageAnzahlTitle, ServerMessageUtil.getMessage("Reports_nachfrageAnzahlTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.nachfrageDauerTitle, ServerMessageUtil.getMessage("Reports_nachfrageDauerTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.kostenlenkungAndereTitle, ServerMessageUtil.getMessage("Reports_kostenlenkungAndereTitle", locale));
		excelMerger.addValue(MergeFieldGemeinden.welcheKostenlenkungsmassnahmenTitle, ServerMessageUtil.getMessage("Reports_wecheKostenlenkungsmassnahmenTitle", locale));
	}
}
