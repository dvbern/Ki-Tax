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

package ch.dvbern.ebegu.reporting.notrecht;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.enums.reporting.MergeFieldNotrecht;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class NotrechtExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<NotrechtDataRow> data, boolean zahlungenausloesen) {

		checkNotNull(data);

		Locale locale = Locale.GERMAN;
		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		excelMerger.addValue(MergeFieldNotrecht.datumErstellt, LocalDate.now());
		String zahlungenAusloesenKey = zahlungenausloesen ? "label_true" : "label_false";
		excelMerger.addValue(MergeFieldNotrecht.flagZahlungenAusloesen, ServerMessageUtil.getMessage(zahlungenAusloesenKey, locale));

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldNotrecht.repeatRow);

			excelRowGroup.addValue(MergeFieldNotrecht.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldNotrecht.status, ServerMessageUtil.translateEnumValue(dataRow.getStatus(), locale));
			excelRowGroup.addValue(MergeFieldNotrecht.betreuungsangebotTyp, ServerMessageUtil.translateEnumValue(dataRow.getBetreuungsangebotTyp(), locale));
			excelRowGroup.addValue(MergeFieldNotrecht.traegerschaft, dataRow.getTraegerschaft());
			excelRowGroup.addValue(MergeFieldNotrecht.email, dataRow.getEmail());
			excelRowGroup.addValue(MergeFieldNotrecht.adresseOrganisation, dataRow.getAdresseOrganisation());
			excelRowGroup.addValue(MergeFieldNotrecht.adresseStrasse, dataRow.getAdresseStrasse());
			excelRowGroup.addValue(MergeFieldNotrecht.adresseHausnummer, dataRow.getAdresseHausnummer());
			excelRowGroup.addValue(MergeFieldNotrecht.adressePlz, dataRow.getAdressePlz());
			excelRowGroup.addValue(MergeFieldNotrecht.adresseOrt, dataRow.getAdresseOrt());
			excelRowGroup.addValue(MergeFieldNotrecht.telefon, dataRow.getTelefon());

			excelRowGroup.addValue(MergeFieldNotrecht.stufe1InstitutionKostenuebernahmeAnzahlTage, dataRow.getStufe1InstitutionKostenuebernahmeAnzahlTage());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe1InstitutionKostenuebernahmeAnzahlStunden, dataRow.getStufe1InstitutionKostenuebernahmeAnzahlStunden());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe1InstitutionKostenuebernahmeBetreuung, dataRow.getStufe1InstitutionKostenuebernahmeBetreuung());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe1KantonKostenuebernahmeAnzahlTage, dataRow.getStufe1KantonKostenuebernahmeAnzahlTage());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe1KantonKostenuebernahmeAnzahlStunden, dataRow.getStufe1KantonKostenuebernahmeAnzahlStunden());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe1KantonKostenuebernahmeBetreuung, dataRow.getStufe1KantonKostenuebernahmeBetreuung());

			excelRowGroup.addValue(MergeFieldNotrecht.stufe1FreigabeBetrag, dataRow.getStufe1FreigabeBetrag());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe1FreigabeDatum, dataRow.getStufe1FreigabeDatum());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe1FreigabeAusbezahltAm, dataRow.getStufe1FreigabeAusbezahltAm());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe1ZahlungJetztAusgeloest, dataRow.getStufe1ZahlungJetztAusgeloest());

			excelRowGroup.addValue(MergeFieldNotrecht.institutionTyp, ServerMessageUtil.translateEnumValue(dataRow.getInstitutionTyp(), locale));

			excelRowGroup.addValue(MergeFieldNotrecht.stufe2InstitutionKostenuebernahmeAnzahlTage, dataRow.getStufe2InstitutionKostenuebernahmeAnzahlTage());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe2InstitutionKostenuebernahmeAnzahlStunden, dataRow.getStufe2InstitutionKostenuebernahmeAnzahlStunden());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe2InstitutionKostenuebernahmeBetreuung, dataRow.getStufe2InstitutionKostenuebernahmeBetreuung());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe2KantonKostenuebernahmeAnzahlTage, dataRow.getStufe2KantonKostenuebernahmeAnzahlTage());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe2KantonKostenuebernahmeAnzahlStunden, dataRow.getStufe2KantonKostenuebernahmeAnzahlStunden());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe2KantonKostenuebernahmeBetreuung, dataRow.getStufe2KantonKostenuebernahmeBetreuung());

			excelRowGroup.addValue(MergeFieldNotrecht.stufe2VerfuegungBetrag, dataRow.getStufe2VerfuegungBetrag());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe2VerfuegungDatum, dataRow.getStufe2VerfuegungDatum());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe2VerfuegungAusbezahltAm, dataRow.getStufe2VerfuegungAusbezahltAm());
			excelRowGroup.addValue(MergeFieldNotrecht.stufe2ZahlungJetztAusgeloest, dataRow.getStufe2ZahlungJetztAusgeloest());

			excelRowGroup.addValue(MergeFieldNotrecht.iban, dataRow.getIban());
			excelRowGroup.addValue(MergeFieldNotrecht.kontoinhaber, dataRow.getKontoinhaber());
			excelRowGroup.addValue(MergeFieldNotrecht.auszahlungOrganisation, dataRow.getAuszahlungOrganisation());
			excelRowGroup.addValue(MergeFieldNotrecht.auszahlungStrasse, dataRow.getAuszahlungStrasse());
			excelRowGroup.addValue(MergeFieldNotrecht.auszahlungHausnummer, dataRow.getAuszahlungHausnummer());
			excelRowGroup.addValue(MergeFieldNotrecht.auszahlungPlz, dataRow.getAuszahlungPlz());
			excelRowGroup.addValue(MergeFieldNotrecht.auszahlungOrt, dataRow.getAuszahlungOrt());
		});

		return excelMerger;
	}
}
