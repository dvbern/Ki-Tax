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

package ch.dvbern.ebegu.reporting.tagesschule;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldTagesschuleRechnungsstellung;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class TagesschuleRechnungsstellungExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<TagesschuleRechnungsstellungDataRow> data,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant) {

		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();
		addHeaders(excelMerger, locale, mandant);

		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.datumErstellt, LocalDate.now());

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldTagesschuleRechnungsstellung.repeatRow);

			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.tagesschule, dataRow.getTagesschule());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.nachnameKind, dataRow.getNachnameKind());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.vornameKind, dataRow.getVornameKind());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.geburtsdatumKind, dataRow.getGeburtsdatumKind());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.referenzNummer, dataRow.getReferenzNummer());

			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseVorname, dataRow.getRechnungsadresseVorname());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseNachname, dataRow.getRechnungsadresseNachname());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseOrganisation, dataRow.getRechnungsadresseOrganisation());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseStrasse, dataRow.getRechnungsadresseStrasse());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseHausnummer, dataRow.getRechnungsadresseHausnummer());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadressePlz, dataRow.getRechnungsadressePlz());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseOrt, dataRow.getRechnungsadresseOrt());

			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.monat, dataRow.getDatumAb());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.massgebendesEinkommenVorFamAbzug, dataRow.getMassgebendesEinkommenVorFamAbzug());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.famGroesse, dataRow.getFamGroesse());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.massgebendesEinkommenNachFamAbzug, dataRow.getMassgebendesEinkommenNachFamAbzug());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.einkommensverschlechterung, dataRow.getEkvVorhanden());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.einkommensverschlechterungAnnuliert, dataRow.getEkvAnnuliert());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.erklaerungEinkommen, ServerMessageUtil.translateEnumValue(dataRow.getErklaerungEinkommen(), locale, mandant));
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.eintrittsdatum, dataRow.getEintrittsdatum());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.gebuehrProStundeMitBetreuung, dataRow.getGebuehrProStundeMitBetreuung());
			excelRowGroup.addValue(MergeFieldTagesschuleRechnungsstellung.gebuehrProStundeOhneBetreuung, dataRow.getGebuehrProStundeOhneBetreuung());
		});

		return excelMerger;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale, @Nonnull Mandant mandant) {
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.tagesschuleRechungsstellungTitle, ServerMessageUtil.getMessage("Reports_tagesschuleRechungsstellungTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.datumErstelltTitle, ServerMessageUtil.getMessage("Reports_datumErstelltTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.tagesschuleTitle, ServerMessageUtil.getMessage("Reports_tagesschuleTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.kindTitle, ServerMessageUtil.getMessage("Reports_kindTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.nachnameKindTitle, ServerMessageUtil.getMessage("Reports_nachnameKindTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.vornameKindTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.referenzNummerTitle, ServerMessageUtil.getMessage("Reports_referenzNummerTitle", locale, mandant));

		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.rechungsadresseTitle, ServerMessageUtil.getMessage("Reports_rechungsadresseTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseVornameTitle, ServerMessageUtil.getMessage("Reports_rechnungsadresseVornameTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseNachnameTitle, ServerMessageUtil.getMessage("Reports_rechnungsadresseNachnameTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseOrganisationTitle, ServerMessageUtil.getMessage("Reports_rechnungsadresseOrganisationTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseStrasseTitle, ServerMessageUtil.getMessage("Reports_rechnungsadresseStrasseTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseHausnummerTitle, ServerMessageUtil.getMessage("Reports_rechnungsadresseHausnummerTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadressePlzTitle, ServerMessageUtil.getMessage("Reports_rechnungsadressePlzTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.rechnungsadresseOrtTitle, ServerMessageUtil.getMessage("Reports_rechnungsadresseOrtTitle", locale, mandant));

		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.monatTitle, ServerMessageUtil.getMessage("Reports_monatTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.massgebendesEinkommenVorFamAbzugTitle, ServerMessageUtil.getMessage("Reports_massgebendesEinkommenVorFamAbzugTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.famGroesseTitle, ServerMessageUtil.getMessage("Reports_famGroesseTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.massgebendesEinkommenNachFamAbzugTitle, ServerMessageUtil.getMessage("Reports_massgebendesEinkommenNachFamAbzugTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.einkommensverschlechterungTitle, ServerMessageUtil.getMessage("Reports_einkommensverschlechterungTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.einkommensverschlechterungAnnuliertTitle, ServerMessageUtil.getMessage("Reports_einkommensverschlechterungAnnuliertTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.erklaerungEinkommenTitle, ServerMessageUtil.getMessage("Reports_erklaerungEinkommenTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.eintrittsdatumTitle, ServerMessageUtil.getMessage("Reports_eintrittsdatumTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.gebuehrProStundeMitBetreuungTitle, ServerMessageUtil.getMessage("Reports_gebuehrProStundeMitBetreuungTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleRechnungsstellung.gebuehrProStundeOhneBetreuungTitle, ServerMessageUtil.getMessage("Reports_gebuehrProStundeOhneBetreuungTitle", locale, mandant));
	}
}
