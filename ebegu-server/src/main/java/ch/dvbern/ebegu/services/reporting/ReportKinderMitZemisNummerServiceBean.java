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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.reporting.ReportKinderMitZemisNummerService;
import ch.dvbern.ebegu.reporting.lastenausgleich.KindMitZemisNummerDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleich.KinderMitZemisNummerExcelConverter;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jboss.ejb3.annotation.TransactionTimeout;

@Stateless
@Local(ReportKinderMitZemisNummerService.class)
public class ReportKinderMitZemisNummerServiceBean extends AbstractReportServiceBean implements ReportKinderMitZemisNummerService {

	private final KinderMitZemisNummerExcelConverter kinderMitZemisNummerExcelConverter = new KinderMitZemisNummerExcelConverter();

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private KindService kindService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private MailService mailService;

	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateZemisReport(@Nonnull Integer lastenausgleichJahr, @Nonnull Locale locale) throws ExcelMergeException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_ZEMIS;

		InputStream is = ReportKinderMitZemisNummerServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<KindMitZemisNummerDataRow> reportData = getReportKinderMitZemisNummer(lastenausgleichJahr);

		ExcelMergerDTO excelMergerDTO = kinderMitZemisNummerExcelConverter.toExcelMergerDTO(reportData, lastenausgleichJahr);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		kinderMitZemisNummerExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			ServerMessageUtil.translateEnumValue(reportVorlage.getDefaultExportFilename(), locale) + ".xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Override
	public void setFlagAndSaveZemisExcel(@Nonnull byte[] fileContent) throws IOException, MailException {

		try (InputStream is = new ByteArrayInputStream(fileContent)) {
			Workbook workbook = WorkbookFactory.create(is);
			Sheet sheet = workbook.getSheetAt(0);

			final int firstRelevantRow = 6;
			int rowNumber = 0;

			try {
				for (Row row : sheet) {
					rowNumber = row.getRowNum();
					if (rowNumber >= firstRelevantRow) {
						int fallNummer = (int) row.getCell(0).getNumericCellValue();
						int kindNummer = (int) row.getCell(5).getNumericCellValue();
						boolean keinSelbstbehaltFuerGemeinde = row.getCell(8).getBooleanCellValue();
						String gesuchsperiodeStr = row.getCell(1).getStringCellValue();
						int gesuchsperiodeStartJahr = Integer.parseInt(gesuchsperiodeStr.split("/")[0]);
						kindService.updateKeinSelbstbehaltFuerGemeinde(
							fallNummer,
							kindNummer,
							gesuchsperiodeStartJahr,
							keinSelbstbehaltFuerGemeinde
						);
					}
				}
				sendMail("ZEMIS Excel verarbeitet", "Die Verarbeitung des ZEMIS Excels wurde "
					+ "erfolgreich abgeschlossen");

			} catch (IllegalStateException exception) {
				String message = "Falsches Format vom ZEMIS Excel in Zeile " + (rowNumber+1);
				sendMail("Fehler bei der Verarbeitung des ZEMIS Excels", message);
				throw new EbeguRuntimeException("setFlagAndSaveZemisExcel", message, message);
			}
		}
	}

	private void sendMail(@Nonnull String subject, @Nonnull String message) throws MailException {
		Benutzer benutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"sendMail", "No User is logged in"));
		mailService.sendMessage(subject, message, benutzer.getEmail());
	}

	private @Nonnull List<KindMitZemisNummerDataRow> getReportKinderMitZemisNummer(@Nonnull Integer lastenausgleichJahr) {
		List<Gesuch> gesuchList = gesuchService.findGesucheForZemisList(lastenausgleichJahr);
		List<KindMitZemisNummerDataRow> dataRows = new ArrayList<>();
		gesuchList.forEach(gesuch -> {
			List<KindMitZemisNummerDataRow> kinder = gesuch.getKindContainers().stream()
				// Abfrage gibt alle Gesuche mit mindesten einem Kind mit Zemis Nummer zurück. Darum müssen die Kinder nochmals gefiltert werden
				.filter(kindContainer -> kindContainer.getKindJA().getZemisNummer() != null)
				.map(kindContainer -> {
					KindMitZemisNummerDataRow dataRow = new KindMitZemisNummerDataRow();
					dataRow.setFall(gesuch.getFall().getFallNummer());
					dataRow.setPeriode(gesuch.getGesuchsperiode().getGesuchsperiodeString());
					dataRow.setGemeinde(gesuch.getDossier().getGemeinde().getName());
					dataRow.setName(kindContainer.getKindJA().getNachname());
					dataRow.setVorname(kindContainer.getKindJA().getVorname());
					dataRow.setKindNummer(kindContainer.getKindNummer());
					dataRow.setGeburtsdatum(kindContainer.getKindJA().getGeburtsdatum());
					dataRow.setZemisNummer(kindContainer.getKindJA().getZemisNummer());
					dataRow.setKeinSelbstbehaltFuerGemeinde(kindContainer.getKeinSelbstbehaltDurchGemeinde());
					return dataRow;
				}).collect(Collectors.toList());
			dataRows.addAll(kinder);
		});
		return dataRows;
	}
}
