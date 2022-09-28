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

package ch.dvbern.ebegu.services.reporting;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichBerechnungService;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBerechnungCSVConverter;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBerechnungDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBerechnungExcelConverter;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.LastenausgleichService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.StringUtil;
import org.infinispan.commons.dataconversion.MediaType;
import org.jboss.ejb3.annotation.TransactionTimeout;

@Stateless
@Local(ReportLastenausgleichBerechnungService.class)
public class ReportLastenausgleichBerechnungServiceBean extends AbstractReportServiceBean implements ReportLastenausgleichBerechnungService {

	private LastenausgleichBerechnungExcelConverter lastenausgleichExcelConverter = new LastenausgleichBerechnungExcelConverter();
	private LastenausgleichBerechnungCSVConverter lastenausgleichCSVConverter = new LastenausgleichBerechnungCSVConverter();

	@Inject
	private LastenausgleichService lastenausgleichService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private PrincipalBean principal;

	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportLastenausgleichKibon(
		@Nonnull String lastenausgleichId,
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException {
		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_LASTENAUSGLEICH_BERECHNUNG;

		try (
			InputStream is = ReportLastenausgleichBerechnungServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
			Workbook workbook = createWorkbook(is, reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			Lastenausgleich lastenausgleich = lastenausgleichService.findLastenausgleich(lastenausgleichId);

			LastenausgleichGrundlagen grundlagen =
				lastenausgleichService.findLastenausgleichGrundlagen(lastenausgleich.getJahr())
					.orElseThrow(() -> new EbeguEntityNotFoundException(
						"generateExcelReportLastenausgleichKibon",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						lastenausgleich.getJahr()));

			List<LastenausgleichDetail> lastenausgleichDetails =
				principal.getBenutzer().getCurrentBerechtigung().getRole().isRoleGemeindeOrBG() ?
					lastenausgleich.getLastenausgleichDetails()
						.stream()
						.filter(lastenausgleichDetail -> principal.getBenutzer()
							.getCurrentBerechtigung()
							.getGemeindeList()
							.contains(lastenausgleichDetail.getGemeinde()))
						.collect(Collectors.toList()) :
					lastenausgleich.getLastenausgleichDetails();

			List<LastenausgleichBerechnungDataRow> reportData =
				getReportLastenausgleichBerechnung(lastenausgleichDetails);

			ExcelMergerDTO excelMergerDTO = lastenausgleichExcelConverter
				.toExcelMergerDTO(
					reportData,
					lastenausgleich.getJahr(),
					lastenausgleich.getTimestampErstellt(),
					grundlagen.getSelbstbehaltPro100ProzentPlatz(),
					locale, Objects.requireNonNull(principal.getMandant()));

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());

			// Zeilen und Spalten ausblenden, die für Berechnung von Lastenausgleich ohne Selbstbehalt Gemeinde
			// (ab 2022) nicht nötig sind.
			if (sheet.getRow(8).getCell(1).getNumericCellValue() == 0) {
				sheet.getRow(8).setZeroHeight(true);
				sheet.setColumnHidden(10, true);
			}

			lastenausgleichExcelConverter.applyAutoSize(sheet);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				ServerMessageUtil.translateEnumValue(
					reportVorlage.getDefaultExportFilename(),
					locale,
					principal.getMandant()) + ".xlsx",
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport());
		}
	}

	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateCSVReportLastenausgleichKibon(@Nonnull String lastenausgleichId) {

		Lastenausgleich lastenausgleich = lastenausgleichService.findLastenausgleich(lastenausgleichId);
		List<LastenausgleichBerechnungDataRow> reportData = getReportLastenausgleichBerechnung(lastenausgleich.getLastenausgleichDetails());
		String lastenausgleichCSV = lastenausgleichCSVConverter.createLastenausgleichCSV(reportData);

		byte[] bytes = lastenausgleichCSV.getBytes(StringUtil.UTF8);
		MimeType mimeType;
		try {
			mimeType = new MimeType(MediaType.TEXT_CSV_TYPE);
		} catch (MimeTypeParseException e) {
			throw new EbeguRuntimeException("getContentTypeForExport", "could not parse mime type", e, MediaType.TEXT_CSV_TYPE);
		}

		return fileSaverService.save(bytes,
			"LastenausgleichBerechnung.csv",
			Constants.TEMP_REPORT_FOLDERNAME,
			mimeType);
	}

	private List<LastenausgleichBerechnungDataRow> getReportLastenausgleichBerechnung(
		@Nonnull Collection<LastenausgleichDetail> lastenausgleichDetails
	) {
		List<LastenausgleichBerechnungDataRow> allLastenausgleich = lastenausgleichDetails.stream()
			.map(detail -> {
				LastenausgleichGrundlagen grundlagenOfVerrechnungsjahr = lastenausgleichService.findLastenausgleichGrundlagen(detail.getJahr())
					.orElseThrow(() -> new EbeguEntityNotFoundException("generateExcelReportLastenausgleichKibon", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, detail.getJahr()));
				LastenausgleichBerechnungDataRow dataRow = new LastenausgleichBerechnungDataRow();
				dataRow.setGemeinde(detail.getGemeinde().getName());
				dataRow.setBfsNummer(String.valueOf(detail.getGemeinde().getBfsNummer()));
				dataRow.setVerrechnungsjahr(String.valueOf(detail.getJahr()));
				dataRow.setTotalBelegung(detail.getTotalBelegung());
				dataRow.setTotalGutscheine(detail.getTotalGutscheine());
				dataRow.setTotalEingabeLastenausgleich(detail.getTotalEingabeLastenausgleich());
				dataRow.setTotalBelegungMitSelbstbehalt(detail.getTotalBelegungenMitSelbstbehalt());
				dataRow.setTotalAnrechenbar(detail.getTotalAnrechenbar());
				dataRow.setTotalGutscheineMitSelbstbehalt(detail.getTotalBetragGutscheineMitSelbstbehalt());
				dataRow.setKostenPro100ProzentPlatz(grundlagenOfVerrechnungsjahr.getKostenPro100ProzentPlatz());
				dataRow.setSelbstbehaltGemeinde(detail.getSelbstbehaltGemeinde());
				dataRow.setEingabeLastenausgleich(detail.getBetragLastenausgleich());
				dataRow.setTotalGutscheineOhneSelbstbehalt(detail.getTotalBetragGutscheineOhneSelbstbehalt());
				dataRow.setTotalBelegungOhneSelbstbehalt(detail.getTotalBelegungenOhneSelbstbehalt());
				dataRow.setKostenFuerSelbstbehalt(detail.getKostenFuerSelbstbehalt());
				dataRow.setKorrektur(detail.isKorrektur());
				return dataRow;
			})
			.collect(Collectors.toList());

		return allLastenausgleich;
	}
}
