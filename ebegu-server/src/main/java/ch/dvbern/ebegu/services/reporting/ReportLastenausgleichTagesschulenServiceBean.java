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

package ch.dvbern.ebegu.services.reporting;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitution;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichTagesschulenService;
import ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen.LastenausgleichGemeindenDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen.LastenausgleichTagesschulenDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen.LastenausgleichTagesschulenExcelConverter;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import static java.util.Objects.requireNonNull;

@Stateless
@Local(ReportLastenausgleichTagesschulenService.class)
public class ReportLastenausgleichTagesschulenServiceBean extends AbstractReportServiceBean implements
	ReportLastenausgleichTagesschulenService {

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeService lastenausgleichTagesschuleAngabenGemeindeService;

	@Inject
	private LastenausgleichTagesschulenExcelConverter lastenausgleichTagesschulenExcelConverter;

	private static final String TAGESSCHULEN_SHEET_NAME = "Tagesschulen";

	@Nonnull
	@Override
	public UploadFileInfo generateExcelReportLastenausgleichTagesschulen() throws ExcelMergeException {

		ReportVorlage vorlage = ReportVorlage.VORLAGE_REPORT_LASTENAUSGLEICH_TAGESSCHULEN;
		InputStream is = ReportServiceBean.class.getResourceAsStream(vorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + vorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(vorlage.getDataSheetName());
		Sheet secondSheet = workbook.getSheet(TAGESSCHULEN_SHEET_NAME);

		List<LastenausgleichGemeindenDataRow> reportData = getReportDataLastenausgleichTagesschulen();

		ExcelMergerDTO excelMergerDTO = lastenausgleichTagesschulenExcelConverter.toExcelMergerDTO(reportData);
		mergeData(sheet, excelMergerDTO, vorlage.getMergeFields());
		mergeData(secondSheet, excelMergerDTO, vorlage.getMergeFields());
		lastenausgleichTagesschulenExcelConverter.applyAutoSize(sheet);
		lastenausgleichTagesschulenExcelConverter.applyAutoSize(secondSheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			"LastenausgleichTagesschulen.xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	private List<LastenausgleichGemeindenDataRow> getReportDataLastenausgleichTagesschulen() {

		List<LastenausgleichTagesschuleAngabenGemeindeContainer>
			lastenausgleichTagesschuleAngabenGemeindeContainerList =
			lastenausgleichTagesschuleAngabenGemeindeService.getAllLastenausgleicheTagesschulen();
		return lastenausgleichTagesschuleAngabenGemeindeContainerList.stream()
			.filter(lastenausgleichTagesschuleAngabenGemeindeContainer -> lastenausgleichTagesschuleAngabenGemeindeContainer
				.getStatus()
				.atLeastInPruefungKanton()).map(
				lastenausgleichTagesschuleAngabenGemeindeContainer -> {
					LastenausgleichGemeindenDataRow dataRow = new LastenausgleichGemeindenDataRow();
					dataRow.setNameGemeinde(lastenausgleichTagesschuleAngabenGemeindeContainer.getGemeinde().getName());
					dataRow.setBfsNummer(lastenausgleichTagesschuleAngabenGemeindeContainer.getGemeinde()
						.getBfsNummer());
					String gemeindeFallNummer =
						String.valueOf(lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode()
							.getBasisJahrPlus1()).substring(2) + "." + dataRow.getBfsNummer();

					dataRow.setGemeindeFallNummer(gemeindeFallNummer);
					dataRow.setPeriode(lastenausgleichTagesschuleAngabenGemeindeContainer.getGesuchsperiode()
						.getGesuchsperiodeString());
					dataRow.setStatus(lastenausgleichTagesschuleAngabenGemeindeContainer.getStatusString());
					dataRow.setAlleAnmeldungenKibon(lastenausgleichTagesschuleAngabenGemeindeContainer.getAlleAngabenInKibonErfasst());
					//wir nehmen die korrektur als in pruefung kanton mindestens
					LastenausgleichTagesschuleAngabenGemeinde lastenausgleichTagesschuleAngabenGemeindeKorrektur =
						lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenKorrektur();

					mapLastenausgleichTagesschuleAngabenGemeindeKorrekturToDataRow(
						lastenausgleichTagesschuleAngabenGemeindeKorrektur,
						dataRow);

					lastenausgleichTagesschuleAngabenGemeindeContainer.getAngabenInstitutionContainers().forEach(
						lastenausgleichTagesschuleAngabenInstitutionContainer -> {
							LastenausgleichTagesschulenDataRow lastenausgleichTagesschulenDataRow =
								new LastenausgleichTagesschulenDataRow();
							lastenausgleichTagesschulenDataRow.setGemeindeFallnummerTS(gemeindeFallNummer);
							lastenausgleichTagesschulenDataRow.setTagesschuleName(
								lastenausgleichTagesschuleAngabenInstitutionContainer.getInstitution().getName());
							lastenausgleichTagesschulenDataRow.setTagesschuleID(
								lastenausgleichTagesschuleAngabenInstitutionContainer.getInstitution().getId());
							LastenausgleichTagesschuleAngabenInstitution angabenTagesschuleKorrektur =
								lastenausgleichTagesschuleAngabenInstitutionContainer.getAngabenKorrektur();
							mapLastenausgleichTagesschuleAngabenInstitutionKorrekturToDataRow(
								angabenTagesschuleKorrektur,
								lastenausgleichTagesschulenDataRow);
							dataRow.getLastenausgleichTagesschulenDaten().add(lastenausgleichTagesschulenDataRow);
						}
					);

					return dataRow;
				}
			).collect(Collectors.toList());
	}

	private void mapLastenausgleichTagesschuleAngabenInstitutionKorrekturToDataRow(
		@Nullable
			LastenausgleichTagesschuleAngabenInstitution angabenTagesschuleKorrektur,
		LastenausgleichTagesschulenDataRow dataRow) {
		if (angabenTagesschuleKorrektur == null) {
			return;
		}
		dataRow.setLehrbetrieb(angabenTagesschuleKorrektur.getLehrbetrieb());
		dataRow.setKinderTotal(angabenTagesschuleKorrektur.getAnzahlEingeschriebeneKinder());
		dataRow.setKinderKindergarten(angabenTagesschuleKorrektur.getAnzahlEingeschriebeneKinderKindergarten());
		dataRow.setKinderPrimar(angabenTagesschuleKorrektur.getAnzahlEingeschriebeneKinderPrimarstufe());
		dataRow.setKinderSek(angabenTagesschuleKorrektur.getAnzahlEingeschriebeneKinderSekundarstufe());
		dataRow.setKinderFaktor(angabenTagesschuleKorrektur.getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen());
		dataRow.setKinderFrueh(angabenTagesschuleKorrektur.getDurchschnittKinderProTagFruehbetreuung());
		dataRow.setKinderMittag(angabenTagesschuleKorrektur.getDurchschnittKinderProTagMittag());
		dataRow.setKinderNachmittag1(angabenTagesschuleKorrektur.getDurchschnittKinderProTagNachmittag1());
		dataRow.setKinderNachmittag2(angabenTagesschuleKorrektur.getDurchschnittKinderProTagNachmittag2());
		dataRow.setBetreuungsstundenTagesschule(angabenTagesschuleKorrektur.getBetreuungsstundenEinschliesslichBesondereBeduerfnisse());
		dataRow.setKonzeptOrganisatorisch(angabenTagesschuleKorrektur.getSchuleAufBasisOrganisatorischesKonzept());
		dataRow.setKonzeptPaedagogisch(angabenTagesschuleKorrektur.getSchuleAufBasisPaedagogischesKonzept());
		dataRow.setRaeumeGeeignet(angabenTagesschuleKorrektur.getRaeumlicheVoraussetzungenEingehalten());
		dataRow.setBetreuungsVerhaeltnis(angabenTagesschuleKorrektur.getBetreuungsverhaeltnisEingehalten());
		dataRow.setErnaehrung(angabenTagesschuleKorrektur.getErnaehrungsGrundsaetzeEingehalten());
		dataRow.setBemerkungenTagesschule(angabenTagesschuleKorrektur.getBemerkungen());
	}

	private void mapLastenausgleichTagesschuleAngabenGemeindeKorrekturToDataRow(
		@Nullable LastenausgleichTagesschuleAngabenGemeinde angabenGemeindeKorrektur,
		LastenausgleichGemeindenDataRow dataRow) {
		if (angabenGemeindeKorrektur == null) {
			return;
		}
		dataRow.setBedarfAbgeklaert(angabenGemeindeKorrektur.getBedarfBeiElternAbgeklaert());
		dataRow.setFerienbetreuung(angabenGemeindeKorrektur.getAngebotFuerFerienbetreuungVorhanden());
		dataRow.setZugangAlle(angabenGemeindeKorrektur.getAngebotVerfuegbarFuerAlleSchulstufen());
		dataRow.setGrundZugangEingeschraenkt(angabenGemeindeKorrektur.getBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen());
		dataRow.setBetreuungsstundenFaktor1(angabenGemeindeKorrektur.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse());
		dataRow.setBetreuungsstundenFaktor15(angabenGemeindeKorrektur.getGeleisteteBetreuungsstundenBesondereBeduerfnisse());
		dataRow.setBetreuungsstundenPaed(angabenGemeindeKorrektur.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete());
		dataRow.setBetreuungsstundenNichtPaed(angabenGemeindeKorrektur.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete());
		dataRow.setNormlohnkostenNichtpaed(angabenGemeindeKorrektur.getNormlohnkostenBetreuungBerechnet());

		dataRow.setElterngebuehrenBetreuung(angabenGemeindeKorrektur.getEinnahmenElterngebuehren());
		dataRow.setSchliessungCovid(angabenGemeindeKorrektur.getTagesschuleTeilweiseGeschlossen());
		dataRow.setElterngebuehrenCovid(angabenGemeindeKorrektur.getRueckerstattungenElterngebuehrenSchliessung());
		dataRow.setErsteRate(angabenGemeindeKorrektur.getErsteRateAusbezahlt());
		dataRow.setGesamtkosten(angabenGemeindeKorrektur.getGesamtKostenTagesschule());
		dataRow.setElterngebuehrenVerpflegung(angabenGemeindeKorrektur.getEinnnahmenVerpflegung());

		dataRow.setEinnahmenDritte(angabenGemeindeKorrektur.getEinnahmenSubventionenDritter());
		dataRow.setUeberschussVorjahr(angabenGemeindeKorrektur.getUeberschussErzielt());
		dataRow.setUeberschussVerwendung(angabenGemeindeKorrektur.getUeberschussVerwendung());
		dataRow.setBemerkungenKosten(angabenGemeindeKorrektur.getBemerkungenWeitereKostenUndErtraege());

		dataRow.setBetreuungsstundenDokumentiert(angabenGemeindeKorrektur.getBetreuungsstundenDokumentiertUndUeberprueft());
		dataRow.setElterngebuehrenTSV(angabenGemeindeKorrektur.getElterngebuehrenGemaessVerordnungBerechnet());
		dataRow.setElterngebuehrenBelege(angabenGemeindeKorrektur.getEinkommenElternBelegt());
		dataRow.setElterngebuehrenMaximaltarif(angabenGemeindeKorrektur.getMaximalTarif());
		dataRow.setBetreuungPaedagogisch(angabenGemeindeKorrektur.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal());
		dataRow.setAusbildungBelegt(angabenGemeindeKorrektur.getAusbildungenMitarbeitendeBelegt());
		dataRow.setBemerkungenGemeinde(angabenGemeindeKorrektur.getBemerkungen());

		// dataRow.setBetreuungsstundenPrognoseKibon(); berechnete Wert
	}
}
