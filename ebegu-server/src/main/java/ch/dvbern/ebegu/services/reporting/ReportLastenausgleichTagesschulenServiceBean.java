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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.gemeindeantrag.OeffnungszeitenTagesschuleDTO;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitution;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichTagesschulenService;
import ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen.LastenausgleichGemeindenDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen.LastenausgleichTagesschulenDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleichTagesschulen.LastenausgleichTagesschulenExcelConverter;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

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
	public UploadFileInfo generateExcelReportLastenausgleichTagesschulen(String gesuchPeriodeId) throws ExcelMergeException, IOException {

		ReportVorlage vorlage = ReportVorlage.VORLAGE_REPORT_LASTENAUSGLEICH_TAGESSCHULEN;
		try (
			InputStream is = ReportServiceBean.class.getResourceAsStream(vorlage.getTemplatePath());
			Workbook workbook = createWorkbook(is, vorlage);
		) {
			Sheet sheet = workbook.getSheet(vorlage.getDataSheetName());
			Sheet secondSheet = workbook.getSheet(TAGESSCHULEN_SHEET_NAME);

			List<LastenausgleichGemeindenDataRow> reportData = getReportDataLastenausgleichTagesschulen(gesuchPeriodeId);

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
	}

	private List<LastenausgleichGemeindenDataRow> getReportDataLastenausgleichTagesschulen(String gesuchPeriodeId) {

		List<LastenausgleichTagesschuleAngabenGemeindeContainer>
			lastenausgleichTagesschuleAngabenGemeindeContainerList =
			lastenausgleichTagesschuleAngabenGemeindeService.getAllLastenausgleicheTagesschulen(gesuchPeriodeId);
		return lastenausgleichTagesschuleAngabenGemeindeContainerList.stream()
			.filter(LastenausgleichTagesschuleAngabenGemeindeContainer::isAtLeastInPruefungKantonOrZurueckgegeben).map(
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
					dataRow.setTimestampMutiert(lastenausgleichTagesschuleAngabenGemeindeContainer.getTimestampMutiert());
					dataRow.setAlleAnmeldungenKibon(lastenausgleichTagesschuleAngabenGemeindeContainer.getAlleAngabenInKibonErfasst());
					dataRow.setBetreuungsstundenPrognose(lastenausgleichTagesschuleAngabenGemeindeContainer.getBetreuungsstundenPrognose());
					//wir nehmen die korrektur als in pruefung kanton mindestens
					LastenausgleichTagesschuleAngabenGemeinde lastenausgleichTagesschuleAngabenGemeinde =
						getLastenausgleichTagesschuleAngabenBasedOnStatus(lastenausgleichTagesschuleAngabenGemeindeContainer);

					mapLastenausgleichTagesschuleAngabenGemeindeToDataRow(
						lastenausgleichTagesschuleAngabenGemeinde,
						dataRow);

					dataRow.setBetreuungsstundenPrognoseBemerkungen(
						lastenausgleichTagesschuleAngabenGemeindeContainer.getBemerkungenBetreuungsstundenPrognose()
					);

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

	// falls der Antrag zurück an die Gemeinde gegeben wurde, ist dieser zwar in der Statistik sichtbar, allerdings
	// sollen dort nur die Werte der Deklaration erscheinen. Ansonsten returnieren wir die Angaben Korrektur
	private LastenausgleichTagesschuleAngabenGemeinde getLastenausgleichTagesschuleAngabenBasedOnStatus(@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container) {
		if (container.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.ZURUECK_AN_GEMEINDE) {
			return container.getAngabenDeklaration();
		}
		return container.getAngabenKorrektur();
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
		dataRow.setKinderFaktor15(angabenTagesschuleKorrektur.getAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen());
		dataRow.setKinderFaktor3(angabenTagesschuleKorrektur.getAnzahlEingeschriebeneKinderVolksschulangebot());
		dataRow.setKinderFrueh(angabenTagesschuleKorrektur.getDurchschnittKinderProTagFruehbetreuung());
		dataRow.setKinderMittag(angabenTagesschuleKorrektur.getDurchschnittKinderProTagMittag());
		dataRow.setKinderNachmittag1(angabenTagesschuleKorrektur.getDurchschnittKinderProTagNachmittag1());
		dataRow.setKinderNachmittag2(angabenTagesschuleKorrektur.getDurchschnittKinderProTagNachmittag2());
		dataRow.setKinderBasisstufe(angabenTagesschuleKorrektur.getAnzahlEingeschriebeneKinderBasisstufe());
		dataRow.setBetreuungsstundenTagesschule(angabenTagesschuleKorrektur.getBetreuungsstundenEinschliesslichBesondereBeduerfnisse());
		dataRow.setKonzeptOrganisatorisch(angabenTagesschuleKorrektur.getSchuleAufBasisOrganisatorischesKonzept());
		dataRow.setKonzeptPaedagogisch(angabenTagesschuleKorrektur.getSchuleAufBasisPaedagogischesKonzept());
		dataRow.setRaeumeGeeignet(angabenTagesschuleKorrektur.getRaeumlicheVoraussetzungenEingehalten());
		dataRow.setBetreuungsVerhaeltnis(angabenTagesschuleKorrektur.getBetreuungsverhaeltnisEingehalten());
		dataRow.setErnaehrung(angabenTagesschuleKorrektur.getErnaehrungsGrundsaetzeEingehalten());
		dataRow.setBemerkungenTagesschule(angabenTagesschuleKorrektur.getBemerkungen());

		mapLastenausgleichTagesschuleOeffnungszeitenToDataRow(dataRow, angabenTagesschuleKorrektur.getOeffnungszeiten());
	}

	private void mapLastenausgleichTagesschuleOeffnungszeitenToDataRow(
		@Nonnull LastenausgleichTagesschulenDataRow dataRow,
		@Nullable String oeffnungszeitenStr
	) {
		if (oeffnungszeitenStr == null) {
			return;
		}
		OeffnungszeitenTagesschuleDTO[] oeffnungszeiten;
		try {
			oeffnungszeiten = EbeguUtil.convertOeffnungszeiten(oeffnungszeitenStr);
		} catch (JsonProcessingException e) {
			throw new EbeguRuntimeException(
				"mapLastenausgleichTagesschuleOeffnungszeitenToDataRow",
				"Problem while converting oeffnungszeiten",
				e);
		}
		for (var oeffnungszeit : oeffnungszeiten) {
			mapLastenausgleichTagesschuleOeffnungszeitToDataRow(dataRow, oeffnungszeit);
		}
	}

	private void mapLastenausgleichTagesschuleOeffnungszeitToDataRow(
		@Nonnull LastenausgleichTagesschulenDataRow dataRow,
		@Nonnull OeffnungszeitenTagesschuleDTO oeffnungszeit
	) {
		switch (oeffnungszeit.getType()) {
		case FRUEHBETREUUNG:
			dataRow.setFruehBetMo(oeffnungszeit.isMontag());
			dataRow.setFruehBetDi(oeffnungszeit.isDienstag());
			dataRow.setFruehBetMi(oeffnungszeit.isMittwoch());
			dataRow.setFruehBetDo(oeffnungszeit.isDonnerstag());
			dataRow.setFruehBetFr(oeffnungszeit.isFreitag());
			break;
		case MITTAGSBETREUUNG:
			dataRow.setMittagsBetMo(oeffnungszeit.isMontag());
			dataRow.setMittagsBetDi(oeffnungszeit.isDienstag());
			dataRow.setMittagsBetMi(oeffnungszeit.isMittwoch());
			dataRow.setMittagsBetDo(oeffnungszeit.isDonnerstag());
			dataRow.setMittagsBetFr(oeffnungszeit.isFreitag());
			break;
		case NACHMITTAGSBETREUUNG_1:
			dataRow.setNachmittags1BetMo(oeffnungszeit.isMontag());
			dataRow.setNachmittags1BetDi(oeffnungszeit.isDienstag());
			dataRow.setNachmittags1BetMi(oeffnungszeit.isMittwoch());
			dataRow.setNachmittags1BetDo(oeffnungszeit.isDonnerstag());
			dataRow.setNachmittags1BetFr(oeffnungszeit.isFreitag());
			break;
		case NACHMITTAGSBETREUUNG_2:
			dataRow.setNachmittags2BetMo(oeffnungszeit.isMontag());
			dataRow.setNachmittags2BetDi(oeffnungszeit.isDienstag());
			dataRow.setNachmittags2BetMi(oeffnungszeit.isMittwoch());
			dataRow.setNachmittags2BetDo(oeffnungszeit.isDonnerstag());
			dataRow.setNachmittags2BetFr(oeffnungszeit.isFreitag());
			break;
		default:
			throw new EbeguRuntimeException("Oeffnungszeit type not implemented", oeffnungszeit.getType().name());
		}
	}

	private void mapLastenausgleichTagesschuleAngabenGemeindeToDataRow(
		@Nullable LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde,
		LastenausgleichGemeindenDataRow dataRow) {
		if (angabenGemeinde == null) {
			return;
		}
		dataRow.setBedarfAbgeklaert(angabenGemeinde.getBedarfBeiElternAbgeklaert());
		dataRow.setFerienbetreuung(angabenGemeinde.getAngebotFuerFerienbetreuungVorhanden());
		dataRow.setZugangAlle(angabenGemeinde.getAngebotVerfuegbarFuerAlleSchulstufen());
		dataRow.setGrundZugangEingeschraenkt(angabenGemeinde.getBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen());
		dataRow.setBetreuungsstundenFaktor1(angabenGemeinde.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse());
		dataRow.setBetreuungsstundenFaktor15(angabenGemeinde.getGeleisteteBetreuungsstundenBesondereBeduerfnisse());
		dataRow.setBetreuungsstundenFaktor3(angabenGemeinde.getGeleisteteBetreuungsstundenBesondereVolksschulangebot());
		dataRow.setBetreuungsstundenPaed(angabenGemeinde.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete());
		dataRow.setBetreuungsstundenNichtPaed(angabenGemeinde.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete());

		dataRow.setElterngebuehrenBetreuung(angabenGemeinde.getEinnahmenElterngebuehren());
		dataRow.setElterngebuehrenVolksschulangebot(angabenGemeinde.getEinnahmenElterngebuehrenVolksschulangebot());
		dataRow.setSchliessungCovid(angabenGemeinde.getTagesschuleTeilweiseGeschlossen());
		dataRow.setElterngebuehrenCovid(angabenGemeinde.getRueckerstattungenElterngebuehrenSchliessung());
		dataRow.setErsteRate(angabenGemeinde.getErsteRateAusbezahlt());
		dataRow.setGesamtkosten(angabenGemeinde.getGesamtKostenTagesschule());
		dataRow.setElterngebuehrenVerpflegung(angabenGemeinde.getEinnnahmenVerpflegung());

		dataRow.setEinnahmenDritte(angabenGemeinde.getEinnahmenSubventionenDritter());
		dataRow.setUeberschussVorjahr(angabenGemeinde.getUeberschussErzielt());
		dataRow.setUeberschussVerwendung(angabenGemeinde.getUeberschussVerwendung());
		dataRow.setBemerkungenKosten(angabenGemeinde.getBemerkungenWeitereKostenUndErtraege());

		dataRow.setBetreuungsstundenDokumentiert(angabenGemeinde.getBetreuungsstundenDokumentiertUndUeberprueft());
		dataRow.setElterngebuehrenTSV(angabenGemeinde.getElterngebuehrenGemaessVerordnungBerechnet());
		dataRow.setElterngebuehrenBelege(angabenGemeinde.getEinkommenElternBelegt());
		dataRow.setElterngebuehrenMaximaltarif(angabenGemeinde.getMaximalTarif());
		dataRow.setBetreuungPaedagogisch(angabenGemeinde.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal());
		dataRow.setAusbildungBelegt(angabenGemeinde.getAusbildungenMitarbeitendeBelegt());
		dataRow.setBemerkungenGemeinde(angabenGemeinde.getBemerkungen());
		dataRow.setBemerkungStarkeVeraenderung(angabenGemeinde.getBemerkungStarkeVeraenderung());
		dataRow.setBemerkungMindestens50ProzentAusgebildet(angabenGemeinde.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung());

		// dataRow.setBetreuungsstundenPrognoseKibon(); berechnete Wert
	}
}
