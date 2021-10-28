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

package ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen;

import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.enums.reporting.MergeFieldLastenausgleichTS;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class LastenausgleichTagesschulenExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {

	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<LastenausgleichGemeindenDataRow> data) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		excelMerger.addValue(MergeFieldLastenausgleichTS.datumGeneriert, LocalDate.now());

		data.forEach( lastenausgleichGemeindenDataRow -> {
			ExcelMergerDTO gemeindenAngabenGroup = excelMerger.createGroup(MergeFieldLastenausgleichTS.rowGemeindenRepeat);
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.nameGemeinde, lastenausgleichGemeindenDataRow.getNameGemeinde());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.bfsNummer, lastenausgleichGemeindenDataRow.getBfsNummer());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.gemeindeFallNummer, lastenausgleichGemeindenDataRow.getGemeindeFallNummer());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.periode, lastenausgleichGemeindenDataRow.getPeriode());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.status, lastenausgleichGemeindenDataRow.getStatus());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.alleAnmeldungenKibon, lastenausgleichGemeindenDataRow.getAlleAnmeldungenKibon());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.bedarfAbgeklaert, lastenausgleichGemeindenDataRow.getBedarfAbgeklaert());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.ferienbetreuung, lastenausgleichGemeindenDataRow.getFerienbetreuung());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.zugangAlle, lastenausgleichGemeindenDataRow.getZugangAlle());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.grundZugangEingeschraenkt, lastenausgleichGemeindenDataRow.getGrundZugangEingeschraenkt());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsstundenFaktor1, lastenausgleichGemeindenDataRow.getBetreuungsstundenFaktor1());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsstundenFaktor15, lastenausgleichGemeindenDataRow.getBetreuungsstundenFaktor15());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsstundenPaed, lastenausgleichGemeindenDataRow.getBetreuungsstundenPaed());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsstundenNichtPaed, lastenausgleichGemeindenDataRow.getBetreuungsstundenNichtPaed());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.elterngebuehrenBetreuung, lastenausgleichGemeindenDataRow.getElterngebuehrenBetreuung());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.schliessungCovid, lastenausgleichGemeindenDataRow.getSchliessungCovid());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.elterngebuehrenCovid, lastenausgleichGemeindenDataRow.getElterngebuehrenCovid());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.ersteRate, lastenausgleichGemeindenDataRow.getErsteRate());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.gesamtkosten, lastenausgleichGemeindenDataRow.getGesamtkosten());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.elterngebuehrenVerpflegung, lastenausgleichGemeindenDataRow.getElterngebuehrenVerpflegung());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.einnahmenDritte, lastenausgleichGemeindenDataRow.getEinnahmenDritte());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.ueberschussVorjahr, lastenausgleichGemeindenDataRow.getUeberschussVorjahr());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.ueberschussVerwendung, lastenausgleichGemeindenDataRow.getUeberschussVerwendung());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.bemerkungenKosten, lastenausgleichGemeindenDataRow.getBemerkungenKosten());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsstundenDokumentiert, lastenausgleichGemeindenDataRow.getBetreuungsstundenDokumentiert());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.ElterngebuehrenTSV, lastenausgleichGemeindenDataRow.getElterngebuehrenTSV());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.elterngebuehrenBelege, lastenausgleichGemeindenDataRow.getElterngebuehrenBelege());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.elterngebuehrenMaximaltarif, lastenausgleichGemeindenDataRow.getElterngebuehrenMaximaltarif());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungPaedagogisch, lastenausgleichGemeindenDataRow.getBetreuungPaedagogisch());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.ausbildungBelegt, lastenausgleichGemeindenDataRow.getAusbildungBelegt());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.bemerkungenGemeinde, lastenausgleichGemeindenDataRow.getBemerkungenGemeinde());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsstundenPrognose, lastenausgleichGemeindenDataRow.getBetreuungsstundenPrognose());
			gemeindenAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsstundenPrognoseKibon, lastenausgleichGemeindenDataRow.getBetreuungsstundenPrognoseKibon());

			lastenausgleichGemeindenDataRow.getLastenausgleichTagesschulenDaten().forEach(lastenausgleichTagesschulenDataRow -> {
				ExcelMergerDTO tagesschuleAngabenGroup = excelMerger.createGroup(MergeFieldLastenausgleichTS.rowTagesschulenRepeat);
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.gemeindeFallnummerTS, lastenausgleichTagesschulenDataRow.getGemeindeFallnummerTS());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.tagesschuleName, lastenausgleichTagesschulenDataRow.getTagesschuleName());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.tagesschuleID, lastenausgleichTagesschulenDataRow.getTagesschuleID());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.lehrbetrieb, lastenausgleichTagesschulenDataRow.getLehrbetrieb());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderTotal, lastenausgleichTagesschulenDataRow.getKinderTotal());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderKindergarten, lastenausgleichTagesschulenDataRow.getKinderKindergarten());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderPrimar, lastenausgleichTagesschulenDataRow.getKinderPrimar());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderSek, lastenausgleichTagesschulenDataRow.getKinderSek());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderFaktor, lastenausgleichTagesschulenDataRow.getKinderFaktor());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderFrueh, lastenausgleichTagesschulenDataRow.getKinderFrueh());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderMittag, lastenausgleichTagesschulenDataRow.getKinderMittag());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderNachmittag1, lastenausgleichTagesschulenDataRow.getKinderNachmittag1());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.kinderNachmittag2, lastenausgleichTagesschulenDataRow.getKinderNachmittag2());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsstundenTagesschule, lastenausgleichTagesschulenDataRow.getBetreuungsstundenTagesschule());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.konzeptOrganisatorisch, lastenausgleichTagesschulenDataRow.getKonzeptOrganisatorisch());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.konzeptPaedagogisch, lastenausgleichTagesschulenDataRow.getKonzeptPaedagogisch());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.raeumeGeeignet, lastenausgleichTagesschulenDataRow.getRaeumeGeeignet());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.betreuungsVerhaeltnis, lastenausgleichTagesschulenDataRow.getBetreuungsVerhaeltnis());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.ernaehrung, lastenausgleichTagesschulenDataRow.getErnaehrung());
				tagesschuleAngabenGroup.addValue(MergeFieldLastenausgleichTS.bemerkungenTagesschule, lastenausgleichTagesschulenDataRow.getBemerkungenTagesschule());
			});
		});

		return excelMerger;
	}
}
