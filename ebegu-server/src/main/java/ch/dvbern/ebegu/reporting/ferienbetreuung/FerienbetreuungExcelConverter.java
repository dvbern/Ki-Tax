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
package ch.dvbern.ebegu.reporting.ferienbetreuung;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.enums.reporting.MergeFieldFerienbetreuung;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class FerienbetreuungExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<FerienbetreuungDataRow> data) {
		checkNotNull(data);

		ExcelMergerDTO mergerDTO = new ExcelMergerDTO();
		mergerDTO.addValue(MergeFieldFerienbetreuung.datumGeneriert, LocalDate.now());

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = mergerDTO.createGroup(MergeFieldFerienbetreuung.repeatRow);
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeinde, dataRow.getGemeinde());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.bfsNummerGemeinde, dataRow.getBfsNummerGemeinde());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.periode, dataRow.getPeriode());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.status, ServerMessageUtil.translateEnumValue(dataRow.getStatus(), Locale.GERMAN));
			excelRowGroup.addValue(MergeFieldFerienbetreuung.traegerschaft, dataRow.getTraegerschaft());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.weitereGemeinden, dataRow.getWeitereGemeinden());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.seitWannFerienbetreuungen, dataRow.getSeitWannFerienbetreuungen());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindeAnschrift, dataRow.getGemeindeAnschrift());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindeStrasse, dataRow.getGemeindeStrasse());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindeHausnummer, dataRow.getGeimeindeHausnummer());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindeZusatz, dataRow.getGemeindeZusatz());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindePlz, dataRow.getGemeindePlz());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindeOrt, dataRow.getGemeindeOrt());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.stammdatenKontaktpersonVorname, dataRow.getStammdatenKontaktpersonVorname());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.stammdatenKontaktpersonName, dataRow.getStammdatenKontaktpersonName());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.stammdatenKontaktpersonTelefon, dataRow.getStammdatenKontaktpersonTelefon());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.stammdatenKontaktpersonEmail, dataRow.getStammdatenKontaktpersonEmail());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.stammdatenKontaktpersonFunktion, dataRow.getStammdatenKontaktpersonFunktion());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kontoInhaber, dataRow.getKontoInhaber());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kontoStrasse, dataRow.getKontoStrasse());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kontoHausnummer, dataRow.getKontoHausnummer());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kontoZusatz, dataRow.getKontoZusatz());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kontoPlz, dataRow.getKontoPlz());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kontoOrt, dataRow.getKontoOrt());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.iban, dataRow.getIban());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kontoVermerk, dataRow.getKontoVermerk());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebot, dataRow.getAngebot());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebotKontaktpersonVorname, dataRow.getAngebotKontaktpersonVorname());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebotKontaktpersonNachname, dataRow.getAngebotKontaktpersonNachname());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebotKontaktpersonStrasse, dataRow.getAngebotKontaktpersonStrasse());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebotKontaktpersonHausnummer, dataRow.getAngebotKontaktpersonHausnummer());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebotKontaktpersonZusatz, dataRow.getAngebotKontaktpersonZusatz());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebotKontaktpersonOrt, dataRow.getAngebotKontaktpersonOrt());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebotKontaktpersonPlz, dataRow.getAngebotKontaktpersonPlz());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlFerienwochenHerbstferien, dataRow.getAnzahlFerienwochenHerbstferien());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlFerienwochenWinterferien, dataRow.getAnzahlFerienwochenWinterferien());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlFerienwochenSportferien, dataRow.getAnzahlFerienwochenSportferien());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlFerienwochenFruehlingsferien, dataRow.getAnzahlFerienwochenFruehlingsferien());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlFerienwochenSommerferien, dataRow.getAnzahlFerienwochenSommerferien());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlTageGesamt, dataRow.getAnzahlTageGesamt());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.bemerkungAnzahlFerienwochen, dataRow.getBemerkungAnzahlFerienwochen());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlStundenProBetreuungstag, dataRow.getAnzahlStundenProBetreuungstag());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.betreuungErfolgtTagsueber, dataRow.getBetreuungErfolgtTagsueber());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.bemerkungOeffnungszeiten, dataRow.getBemerkungOeffnungszeiten());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.finanziellBeteiligteGemeinden, dataRow.getFinanziellBeteiligteGemeinden());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindeFuehrtAngebotSelber, dataRow.getGemeindeFuehrtAngebotSelber());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindeFuehrtAngebotInKooperation, dataRow.getGemeindeFuehrtAngebotInKooperation());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.gemeindeBeauftragtExterneAnbieter, dataRow.getGemeindeBeauftragtExterneAnbieter());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.angebotVereineUndPrivateIntegriert, dataRow.getAngebotVereineUndPrivateIntegriert());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.bemerkungenKooperation, dataRow.getBemerkungenKooperation());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.leitungDurchPersonMitAusbildung, dataRow.getLeitungDurchPersonMitAusbildung());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.betreuungDurchPersonenMitErfahrung, dataRow.getBetreuungDurchPersonenMitErfahrung());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlKinderAngemessen, dataRow.getAnzahlKinderAngemessen());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.betreuungsschluessel, dataRow.getBetreuungsschluessel());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.bemerkungenPersonal, dataRow.getBemerkungenPersonal());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.fixerTarifKinderDerGemeinde, dataRow.getFixerTarifKinderDerGemeinde());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.einkommensabhaengigerTarifKinderDerGemeinde, dataRow.getEinkommensabhaengigerTarifKinderDerGemeinde());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.tagesschuleTarifGiltFuerFerienbetreuung, dataRow.getTagesschuleTarifGiltFuerFerienbetreuung());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet, dataRow.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kinderAusAnderenGemeindenZahlenAnderenTarif,ServerMessageUtil.translateEnumValue(dataRow.getKinderAusAnderenGemeindenZahlenAnderenTarif(), Locale.GERMAN));
			excelRowGroup.addValue(MergeFieldFerienbetreuung.bemerkungenTarifsystem, dataRow.getBemerkungenTarifsystem());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlBetreuungstageKinderBern, dataRow.getAnzahlBetreuungstageKinderBern());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.betreuungstageKinderDieserGemeinde, dataRow.getBetreuungstageKinderDieserGemeinde());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.betreuungstageKinderDieserGemeindeSonderschueler, dataRow.getBetreuungstageKinderDieserGemeindeSonderschueler());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.davonBetreuungstageKinderAndererGemeinden, dataRow.getDavonBetreuungstageKinderAndererGemeinden());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.davonBetreuungstageKinderAndererGemeindenSonderschueler, dataRow.getDavonBetreuungstageKinderAndererGemeindenSonderschueler());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlBetreuteKinder, dataRow.getAnzahlBetreuteKinder());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlBetreuteKinderSonderschueler, dataRow.getAnzahlBetreuteKinderSonderschueler());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlBetreuteKinder1Zyklus, dataRow.getAnzahlBetreuteKinder1Zyklus());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlBetreuteKinder2Zyklus, dataRow.getAnzahlBetreuteKinder2Zyklus());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.anzahlBetreuteKinder3Zyklus, dataRow.getAnzahlBetreuteKinder3Zyklus());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.personalkosten, dataRow.getPersonalkosten());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.personalkostenLeitungAdmin, dataRow.getPersonalkostenLeitungAdmin());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.sachkosten, dataRow.getSachkosten());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.verpflegungskosten, dataRow.getVerpflegungskosten());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.weitereKosten, dataRow.getWeitereKosten());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.bemerkungenKosten, dataRow.getBemerkungenKosten());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.elterngebuehren, dataRow.getElterngebuehren());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.weitereEinnahmen, dataRow.getWeitereEinnahmen());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.totalKantonsbeitrag, dataRow.getTotalKantonsbeitrag());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.beitragKinderAnbietendenGemeinde, dataRow.getBeitragKinderAnbietendenGemeinde());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.beteiligungAnbietendenGemeinde, dataRow.getBeteiligungAnbietendenGemeinde());
			excelRowGroup.addValue(MergeFieldFerienbetreuung.kommentar, dataRow.getKommentar());
		});
		return mergerDTO;
	}

}
