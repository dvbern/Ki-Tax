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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichBGZeitabschnitteService;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBGZeitabschnittDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichBGZeitabschnitteExcelConverter;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.LastenausgleichService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static java.util.Objects.requireNonNull;

@Stateless
@Local(ReportLastenausgleichBGZeitabschnitteService.class)
public class ReportLastenausgleichBGZeitabschnitteServiceBean extends AbstractReportServiceBean implements ReportLastenausgleichBGZeitabschnitteService {

	private LastenausgleichBGZeitabschnitteExcelConverter lastenausgleichBGZeitabschnitteExcelConverter = new LastenausgleichBGZeitabschnitteExcelConverter();

	@Inject
	private LastenausgleichService lastenausgleichService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportLastenausgleichBGZeitabschnitte(
		@Nonnull Locale locale,
		@Nonnull String gemeindeId,
		@Nonnull Integer lastenausgleichJahr
	) throws ExcelMergeException, IOException {
		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_LASTENAUSGLEICH_BG_ZEITABSCHNITTE;

		try (
			InputStream is = ReportLastenausgleichBGZeitabschnitteServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
			Workbook workbook = createWorkbook(is, reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			Lastenausgleich lastenausgleich = lastenausgleichService.findLastenausgleich(lastenausgleichJahr)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"generateExcelReportLastenausgleichBGZeitabschnitte",
					lastenausgleichJahr)
				);

			List<LastenausgleichDetail> lastenausgleichDetails =
				lastenausgleich.getLastenausgleichDetails()
					.stream()
					.filter(detail -> filterLastenausgleichDetail(detail, gemeindeId))
					.collect(Collectors.toList());

			List<LastenausgleichBGZeitabschnittDataRow> reportData =
				getReportLastenausgleichZeitabschnitte(lastenausgleichDetails, locale);

			final XSSFSheet xsslSheet =
				(XSSFSheet) lastenausgleichBGZeitabschnitteExcelConverter.mergeHeaders(
					sheet,
					lastenausgleichJahr,
					locale,
					requireNonNull(principalBean.getMandant())
				);

			final RowFiller rowFiller = fillAndMergeRows(reportVorlage, xsslSheet, reportData);

			byte[] bytes = createWorkbook(rowFiller.getSheet().getWorkbook());
			rowFiller.getSheet().getWorkbook().dispose();

			return fileSaverService.save(
				bytes,
				ServerMessageUtil.translateEnumValue(
					reportVorlage.getDefaultExportFilename(),
					locale,
					principalBean.getMandant()) + ".xlsx",
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport());
		}
	}

	private boolean filterLastenausgleichDetail(@Nonnull LastenausgleichDetail lastenausgleichDetail, String gemeindeId) {
		boolean isGemeinde = lastenausgleichDetail.getGemeinde().getId().equals(gemeindeId);
		if (!isGemeinde) {
			return false;
		}
		authorizer.checkReadAuthorization(lastenausgleichDetail.getGemeinde());
		return true;
	}

	/**
	 * fuegt die Daten der Excelsheet hinzu und gibt den Rowfiller zurueck
	 */
	@Nonnull
	private RowFiller fillAndMergeRows(
		ReportVorlage reportResource,
		XSSFSheet sheet,
		List<LastenausgleichBGZeitabschnittDataRow> reportData
	) {

		RowFiller rowFiller = RowFiller.initRowFiller(
			sheet,
			MergeFieldProvider.toMergeFields(reportResource.getMergeFields()),
			reportData.size());

		lastenausgleichBGZeitabschnitteExcelConverter.mergeRows(
			rowFiller,
			reportData
		);
		lastenausgleichBGZeitabschnitteExcelConverter.applyAutoSize(sheet);

		return rowFiller;
	}

	private List<LastenausgleichBGZeitabschnittDataRow> getReportLastenausgleichZeitabschnitte(
		@Nonnull Collection<LastenausgleichDetail> lastenausgleichDetails,
		@Nonnull Locale locale)
	{
		List<LastenausgleichBGZeitabschnittDataRow> rows = new ArrayList<>();

		var mandant = principalBean.getMandant();

		lastenausgleichDetails.forEach(lastenausgleichDetail -> {
			lastenausgleichDetail.getLastenausgleichDetailZeitabschnitte().forEach(detailZeitabschnitt -> {
				// wir haben erst ab dem Jahr 2022 die zum Lastenausgleich dazugehörenden Zeitabschnitte in der Datenbank persistiert
				// deshalb können wir in der Statistik erst ab 2022 die Korrekturwerte der Zeitabschnitte ausgeben.
				if (detailZeitabschnitt.getZeitabschnitt().getGueltigkeit().getGueltigAb().getYear() < Constants.FIRST_YEAR_LASTENAUSGLEICH_WITHOUT_SELBSTBEHALT) {
					return;
				}
				var zeitabschnitt = detailZeitabschnitt.getZeitabschnitt();
				var row = new LastenausgleichBGZeitabschnittDataRow();

				/* Ein Zeitabschnitt kann entweder ein regulärer Zeitabschnitt des aktuellen Lastenausgleichjahres
				sein oder eine Korrektur eines Vorjahres. Bei einer Korrektur gibt es jeweils zwei Zeitabschnitte,
				einmal der negierte Betrag, der vor der Mutation gegolten hat und einmal der Betrag nach der Mutation.
				Bei einem negierten Zeitabschnitt müssen wir dies auch im Excel Report entsprechend ausweisen.
				*/
				BigDecimal multiplyer;
				if (detailZeitabschnitt.getLastenausgleichDetail().getTotalBelegung().compareTo(BigDecimal.ZERO) < 0) {
					multiplyer = BigDecimal.valueOf(-1);
				} else {
					multiplyer = BigDecimal.ONE;
				}

				Objects.requireNonNull(zeitabschnitt.getVerfuegung().getBetreuung());
				var kind = zeitabschnitt.getVerfuegung().getBetreuung().getKind().getKindJA();
				var betreuung = zeitabschnitt.getVerfuegung().getBetreuung();

				row.setReferenznummer(betreuung.getBGNummer());
				row.setNameGemeinde(lastenausgleichDetail.getGemeinde().getName());
				row.setBfsNummer(lastenausgleichDetail.getGemeinde().getBfsNummer());
				row.setNachname(kind.getNachname());
				row.setVorname(kind.getVorname());
				row.setGeburtsdatum(kind.getGeburtsdatum());
				row.setVon(zeitabschnitt.getGueltigkeit().getGueltigAb());
				row.setBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
				row.setInstitution(zeitabschnitt.getVerfuegung().getBetreuung().getInstitutionStammdaten().getInstitution().getName());
				row.setBetreuungsangebotTyp(
					ServerMessageUtil.translateEnumValue(betreuung.getBetreuungsangebotTyp(), locale, mandant)
				);
				row.setBgPensum(
					zeitabschnitt.getBgCalculationResultAsiv().getBgPensumProzent()
						.multiply(multiplyer)
				);
				row.setKeinSelbstbehaltDurchGemeinde(betreuung.getKind().getKeinSelbstbehaltDurchGemeinde());
				row.setGutschein(
					zeitabschnitt.getBgCalculationResultAsiv().getVerguenstigung()
						.multiply(multiplyer)
				);
				row.setKorrektur(detailZeitabschnitt.getLastenausgleichDetail().isKorrektur());

				rows.add(row);
			});
		});

		rows.sort(
			Comparator
				.comparing(LastenausgleichBGZeitabschnittDataRow::getKorrektur)
				.thenComparing(LastenausgleichBGZeitabschnittDataRow::getNameGemeinde)
				.thenComparing(LastenausgleichBGZeitabschnittDataRow::getReferenznummer)
				.thenComparing(LastenausgleichBGZeitabschnittDataRow::getVon)
		);

		return rows;
	}
}
