/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.kanton.institutionen;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldInstitutionen;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class InstitutionenExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<InstitutionenDataRow> data, @Nonnull Locale locale, @Nonnull Mandant mandant) {
		checkNotNull(data);

		ExcelMergerDTO mergerDTO = new ExcelMergerDTO();

		addHeaders(mergerDTO, locale, mandant);

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = mergerDTO.createGroup(MergeFieldInstitutionen.repeatInstitutionenRow);
			excelRowGroup.addValue(MergeFieldInstitutionen.typ, dataRow.getTyp());
			excelRowGroup.addValue(MergeFieldInstitutionen.traegerschaft, dataRow.getTraegerschaft());
			excelRowGroup.addValue(MergeFieldInstitutionen.traegerschaftEmail, dataRow.getTraegerschaftEmail());
			excelRowGroup.addValue(MergeFieldInstitutionen.email, dataRow.getEmail());
			excelRowGroup.addValue(MergeFieldInstitutionen.familienportalEmail, dataRow.getFamilienportalEmail());
			excelRowGroup.addValue(
					MergeFieldInstitutionen.emailBenachrichtigungKiBon,
					dataRow.getEmailBenachrichtigungenKiBon());
			excelRowGroup.addValue(
					MergeFieldInstitutionen.emailBenachrichtigungKiBonMail,
					dataRow.getEmailBenachrichtigungKiBonMail());
			excelRowGroup.addValue(MergeFieldInstitutionen.name, dataRow.getName());
			excelRowGroup.addValue(MergeFieldInstitutionen.anschrift, dataRow.getAnschrift());
			excelRowGroup.addValue(MergeFieldInstitutionen.strasse, dataRow.getStrasse());
			excelRowGroup.addValue(MergeFieldInstitutionen.plz, dataRow.getPlz());
			excelRowGroup.addValue(MergeFieldInstitutionen.ort, dataRow.getOrt());
			excelRowGroup.addValue(MergeFieldInstitutionen.standortgemeinde, dataRow.getTraegergemeinde());
			excelRowGroup.addValue(MergeFieldInstitutionen.traegergemeinde, dataRow.getTraegergemeinde());
			excelRowGroup.addValue(MergeFieldInstitutionen.bfsGemeinde, dataRow.getBfsGemeinde());
			excelRowGroup.addValue(MergeFieldInstitutionen.telefon, dataRow.getTelefon());
			excelRowGroup.addValue(MergeFieldInstitutionen.url, dataRow.getUrl());
			excelRowGroup.addValue(MergeFieldInstitutionen.gueltigAb, dataRow.getGueltigAb());
			excelRowGroup.addValue(MergeFieldInstitutionen.gueltigBis, dataRow.getGueltigBis());
			excelRowGroup.addValue(MergeFieldInstitutionen.grundSchliessung, dataRow.getGrundSchliessung());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungstage, dataRow.getOeffnungstage());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungstageProJahr, dataRow.getOeffnungstageProJahr());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungszeitAb, dataRow.getOeffnungszeitAb());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungszeitenBis, dataRow.getOeffnungszeitBis());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungVor630, dataRow.getOeffnungVor630());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungNach1830, dataRow.getOeffnungNach1830());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungAnWochenenden, dataRow.getOeffnungAnWochenenden());
			excelRowGroup.addValue(MergeFieldInstitutionen.uebernachtungMoeglich, dataRow.getUebernachtungMoeglich());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungsAbweichungen, dataRow.getOeffnungsAbweichungen());
			excelRowGroup.addValue(MergeFieldInstitutionen.isBaby, dataRow.getBaby());
			excelRowGroup.addValue(MergeFieldInstitutionen.isVorschulkind, dataRow.getVorschulkind());
			excelRowGroup.addValue(MergeFieldInstitutionen.isKindergarten, dataRow.getKindergarten());
			excelRowGroup.addValue(MergeFieldInstitutionen.isSchulkind, dataRow.getSchulkind());
			excelRowGroup.addValue(MergeFieldInstitutionen.subventioniert, dataRow.getSubventioniert());
			excelRowGroup.addValue(MergeFieldInstitutionen.kapazitaet, dataRow.getKapazitaet());
			excelRowGroup.addValue(MergeFieldInstitutionen.reserviertFuerFirmen, dataRow.getReserviertFuerFirmen());
			excelRowGroup.addValue(MergeFieldInstitutionen.zuletztGeaendert, dataRow.getZuletztGeaendert());
			excelRowGroup.addValue(MergeFieldInstitutionen.auslastung, dataRow.getAuslastung());
			excelRowGroup.addValue(MergeFieldInstitutionen.anzahlKinderWarteliste,
					dataRow.getAnzahlKinderWarteliste());
			excelRowGroup.addValue(MergeFieldInstitutionen.summePensumWarteliste, dataRow.getSummePensumWarteliste());
			excelRowGroup.addValue(MergeFieldInstitutionen.dauerWarteliste, dataRow.getDauerWarteliste());
		});
		return mergerDTO;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO mergerDTO, @Nonnull Locale locale, @Nonnull Mandant mandant) {
		mergerDTO.addValue(MergeFieldInstitutionen.typTitle, ServerMessageUtil.getMessage("Reports_typTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.traegerschaftTitle,
			ServerMessageUtil.getMessage("Reports_traegerschaftTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.traegerschaftEmailTitle,
			ServerMessageUtil.getMessage("Reports_traegerschaftEmailTitle", locale, mandant));
		mergerDTO.addValue(
				MergeFieldInstitutionen.emailTitle,
				ServerMessageUtil.getMessage("Reports_emailKitaTitle", locale, mandant));
		mergerDTO.addValue(
				MergeFieldInstitutionen.familienportalEmailTitle,
				ServerMessageUtil.getMessage("Reports_familienportalEmailTitle", locale, mandant));
		mergerDTO.addValue(
				MergeFieldInstitutionen.emailBenachrichtigungKiBonTitle,
				ServerMessageUtil.getMessage("Reports_emailBenachrichtigungKiBonTitle", locale, mandant));
		mergerDTO.addValue(
				MergeFieldInstitutionen.emailBenachrichtigungKiBonMailTitle,
				ServerMessageUtil.getMessage("Reports_emailBenachrichtigungKiBonMailTitle", locale, mandant));
		mergerDTO.addValue(MergeFieldInstitutionen.nameTitle, ServerMessageUtil.getMessage("Reports_nameTitle", locale, mandant));
		mergerDTO.addValue(MergeFieldInstitutionen.anschriftTitle, ServerMessageUtil.getMessage("Reports_anschriftTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.strasseTitle,
			ServerMessageUtil.getMessage("Reports_strasseTitle", locale, mandant));
		mergerDTO.addValue(MergeFieldInstitutionen.plzTitle, ServerMessageUtil.getMessage("Reports_plzTitle", locale, mandant));
		mergerDTO.addValue(MergeFieldInstitutionen.ortTitle, ServerMessageUtil.getMessage("Reports_ortTitle", locale, mandant));
		mergerDTO.addValue(MergeFieldInstitutionen.traegergemeindeTitle, ServerMessageUtil.getMessage("Reports_traegergemeindeTitle", locale, mandant));
		mergerDTO.addValue(MergeFieldInstitutionen.standortgemeindeTitle, ServerMessageUtil.getMessage("Reports_stadortgemeindeTitle", locale, mandant));
		mergerDTO.addValue(MergeFieldInstitutionen.bfsGemeindeTitle, ServerMessageUtil.getMessage("Reports_bfsNummerTitel", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.telefonTitle,
			ServerMessageUtil.getMessage("Reports_telefonTitle", locale, mandant));
		mergerDTO.addValue(MergeFieldInstitutionen.urlTitle, ServerMessageUtil.getMessage("Reports_urlTitle", locale, mandant));
		mergerDTO.addValue(
				MergeFieldInstitutionen.oeffnungstageProJahrTitle,
				ServerMessageUtil.getMessage("Reports_oeffnungstageProJahrTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.gueltigAbTitle,
			ServerMessageUtil.getMessage("Reports_gueltigAbTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.gueltigBisTitle,
			ServerMessageUtil.getMessage("Reports_gueltigBisTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungVor630Title,
			ServerMessageUtil.getMessage("Reports_oeffnungVor630Title", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungNach1830Title,
			ServerMessageUtil.getMessage("Reports_oeffnungNach1830Title", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungAnWochenendenTitle,
			ServerMessageUtil.getMessage("Reports_oeffnungAnWochenendenTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.uebernachtungMoeglichTitle,
			ServerMessageUtil.getMessage("Reports_uebernachtungMoeglichTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.grundSchliessungTitle,
			ServerMessageUtil.getMessage("Reports_grundSchliessungTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungstageTitle,
			ServerMessageUtil.getMessage("Reports_oeffnungstageTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungszeitAbTitle,
			ServerMessageUtil.getMessage("Reports_oeffnungszeitAbTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungszeitenBisTitle,
			ServerMessageUtil.getMessage("Reports_oeffnungszeitBisTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungsAbweichungenTitle,
			ServerMessageUtil.getMessage("Reports_oeffnungsabweichungenTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.babyTitle,
			ServerMessageUtil.getMessage("Reports_babyTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.vorschulkindTitle,
			ServerMessageUtil.getMessage("Reports_vorschulkindTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.kindergartenTitle,
			ServerMessageUtil.getMessage("Reports_kindergartenTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.schulkindTitle,
			ServerMessageUtil.getMessage("Reports_schulkindTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.subventioniertTitle,
			ServerMessageUtil.getMessage("Reports_subventioniertTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.kapazitaetTitle,
			ServerMessageUtil.getMessage("Reports_kapazitaetTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.reserviertFuerFirmenTitle,
			ServerMessageUtil.getMessage("Reports_reserviertFuerFirmenTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.zuletztGeaendertTitle,
			ServerMessageUtil.getMessage("Reports_zuletztGeaendertTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.auslastungTitle,
			ServerMessageUtil.getMessage("Reports_auslastungTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.anzahlKinderWartelisteTitle,
			ServerMessageUtil.getMessage("Reports_anzahlKinderWartelisteTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.summePensumWartelisteTitle,
			ServerMessageUtil.getMessage("Reports_summePensumWarteliste", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.dauerWartelisteTitle,
			ServerMessageUtil.getMessage("Reports_dauerWartelisteTitle", locale, mandant));
		mergerDTO.addValue(
			MergeFieldInstitutionen.reportInstitutionenTitle,
			ServerMessageUtil.getMessage("Reports_reportInstitutionenTitle", locale, mandant));
	}
}
