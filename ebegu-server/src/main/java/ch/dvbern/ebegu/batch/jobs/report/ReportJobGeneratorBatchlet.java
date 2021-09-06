/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.batch.jobs.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.batch.api.AbstractBatchlet;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichSelbstbehaltService;
import ch.dvbern.ebegu.reporting.ReportMahlzeitenService;
import ch.dvbern.ebegu.reporting.ReportMassenversandService;
import ch.dvbern.ebegu.reporting.ReportNotrechtService;
import ch.dvbern.ebegu.reporting.ReportService;
import ch.dvbern.ebegu.reporting.ReportTagesschuleService;
import ch.dvbern.ebegu.reporting.ReportVerrechnungKibonService;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ReportGemeindenService;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.WorkJobConstants.REPORT_VORLAGE_TYPE_PARAM;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
@Named("reportJobGeneratorBatchlet")
@Dependent
public class ReportJobGeneratorBatchlet extends AbstractBatchlet {

	private static final Logger LOG = LoggerFactory.getLogger(ReportJobGeneratorBatchlet.class);

	@Inject
	private ReportService reportService;

	@Inject
	private ReportTagesschuleService reportTagesschuleService;

	@Inject
	private ReportMassenversandService reportMassenversandService;

	@Inject
	private ReportVerrechnungKibonService reportVerrechnungKibonService;

	@Inject
	private ReportLastenausgleichSelbstbehaltService reportLastenausgleichKibonService;

	@Inject
	private ReportNotrechtService reportNotrechtService;

	@Inject
	private ReportMahlzeitenService reportMahlzeitenService;

	@Inject
	private ReportGemeindenService reportGemeindenService;

	@Inject
	private JobContext jobCtx;

	@Inject
	private JobDataContainer jobDataContainer;

	@Override
	public String process() {
		String typeProp = getParameters().getProperty(REPORT_VORLAGE_TYPE_PARAM);
		LOG.info("processing report generation job for type {}", typeProp);
		final ReportVorlage reportType = ReportVorlage.valueOf(typeProp);
		try {
			final UploadFileInfo uploadFileInfo = triggerReportGeneration(reportType); //gespeichertes file
			jobDataContainer.setResult(uploadFileInfo);
			LOG.debug("Report File was successfully generated for workjob {}", jobCtx.getExecutionId());
			return BatchStatus.COMPLETED.toString(); // success

		} catch (ExcelMergeException | MergeDocException e) {
			LOG.error("ExcelMergeException occured while creating a report in a batch process ", e);
		} catch (URISyntaxException | IOException e) {
			LOG.error("IOException occured while creating a report in a batch process, maybe template could not be loaded?", e);
		}
		return BatchStatus.FAILED.toString();
	}

	@Nonnull
	private UploadFileInfo triggerReportGeneration(ReportVorlage workJobType) throws ExcelMergeException, MergeDocException, URISyntaxException, IOException {

		final String datumVonOrStichtag = getParameters().getProperty(WorkJobConstants.DATE_FROM_PARAM);
		LocalDate dateFrom = DateUtil.parseStringToDateOrReturnNow(datumVonOrStichtag);
		final String datumToStichtag = getParameters().getProperty(WorkJobConstants.DATE_TO_PARAM);
		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(datumToStichtag);
		final String gesuchPeriodeID = getParameters().getProperty(WorkJobConstants.GESUCH_PERIODE_ID_PARAM);
		final String zahlungsauftragId = getParameters().getProperty(WorkJobConstants.ZAHLUNGSAUFTRAG_ID_PARAM);
		final String language = getParameters().getProperty(WorkJobConstants.LANGUAGE);
		return generateReport(workJobType, dateFrom, dateTo, gesuchPeriodeID, zahlungsauftragId, Locale.forLanguageTag(language));
	}

	@Nonnull
	@SuppressWarnings("PMD.NcssMethodCount")
	private UploadFileInfo generateReport(
		@Nonnull ReportVorlage workJobType,
		@Nonnull LocalDate dateFrom,
		@Nonnull LocalDate dateTo,
		@Nullable String gesuchPeriodeId,
		@Nullable String zahlungsauftragId,
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException, MergeDocException, URISyntaxException {

		switch (workJobType) {

			case VORLAGE_REPORT_GESUCH_STICHTAG_DE:
			case VORLAGE_REPORT_GESUCH_STICHTAG_FR: {
				return this.reportService.generateExcelReportGesuchStichtag(dateFrom, gesuchPeriodeId, locale);
			}
			case VORLAGE_REPORT_GESUCH_ZEITRAUM_DE:
			case VORLAGE_REPORT_GESUCH_ZEITRAUM_FR: {
				return this.reportService.generateExcelReportGesuchZeitraum(dateFrom, dateTo, gesuchPeriodeId, locale);
			}
			case VORLAGE_REPORT_KANTON: {
				return this.reportService.generateExcelReportKanton(dateFrom, dateTo, locale);
			}
			case VORLAGE_REPORT_MITARBEITERINNEN: {
				return this.reportService.generateExcelReportMitarbeiterinnen(dateFrom, dateTo, locale);
			}
			case VORLAGE_REPORT_BENUTZER: {
				return this.reportService.generateExcelReportBenutzer(locale);
			}
			case VORLAGE_REPORT_ZAHLUNG_AUFTRAG: {
				Objects.requireNonNull(zahlungsauftragId, "Zahlungsauftrag ID must be passed as param");
				return this.reportService.generateExcelReportZahlungAuftrag(zahlungsauftragId, locale);
			}
			case VORLAGE_REPORT_ZAHLUNG_AUFTRAG_PERIODE: {
				Objects.requireNonNull(gesuchPeriodeId);
				return this.reportService.generateExcelReportZahlungPeriode(gesuchPeriodeId, locale);
			}
			case VORLAGE_REPORT_GESUCHSTELLER_KINDER_BETREUUNG: {
				return this.reportService.generateExcelReportGesuchstellerKinderBetreuung(dateFrom, dateTo, gesuchPeriodeId, locale);
			}
			case VORLAGE_REPORT_KINDER: {
				return this.reportService.generateExcelReportKinder(dateFrom, dateTo, gesuchPeriodeId, locale);
			}
			case VORLAGE_REPORT_GESUCHSTELLER: {
				return this.reportService.generateExcelReportGesuchsteller(dateFrom, locale);
			}
			case VORLAGE_REPORT_MASSENVERSAND: {
				Objects.requireNonNull(gesuchPeriodeId);
				return generateReportMassenversand(dateFrom, dateTo, gesuchPeriodeId, locale);
			}
			case VORLAGE_REPORT_INSTITUTIONEN: {
				return this.reportService.generateExcelReportInstitutionen(locale);
			}
			case VORLAGE_REPORT_VERRECHNUNG_KIBON: {
				boolean doSave = Boolean.parseBoolean(getParameters().getProperty(WorkJobConstants.DO_SAVE));
				BigDecimal betragProKind = MathUtil.DEFAULT.from(getParameters().getProperty(WorkJobConstants.BETRAG_PRO_KIND));
				return this.reportVerrechnungKibonService.generateExcelReportVerrechnungKibon(doSave, betragProKind, locale);
			}
			case VORLAGE_REPORT_LASTENAUSGLEICH_SELBSTBEHALT: {
				return this.reportLastenausgleichKibonService.generateExcelReportLastenausgleichKibon(dateFrom, locale);
			}
			case VORLAGE_REPORT_TAGESSCHULE_ANMELDUNGEN: {
				Objects.requireNonNull(gesuchPeriodeId);
				final String stammdatenId = getParameters().getProperty(WorkJobConstants.STAMMDATEN_ID_PARAM);
				return this.reportTagesschuleService.generateExcelReportTagesschuleAnmeldungen(stammdatenId, gesuchPeriodeId, locale);
			}
			case VORLAGE_REPORT_TAGESSCHULE_RECHNUNGSSTELLUNG: {
				return this.reportTagesschuleService.generateExcelReportTagesschuleRechnungsstellung(locale);
			}
			case VORLAGE_REPORT_NOTRECHT: {
				return generateReportNotrecht();
			}
			case VORLAGE_REPORT_MAHLZEITENVERGUENSTIGUNG: {
				final String gemeindeId = getParameters().getProperty(WorkJobConstants.GEMEINDE_ID_PARAM);
				if (gemeindeId == null) {
					throw new EbeguRuntimeException("generateReport", "gemeindeId not defined");
				}
				return this.reportMahlzeitenService.generateExcelReportMahlzeiten(dateFrom, dateTo, locale, gemeindeId);
			}
		case VORLAGE_REPORT_GEMEINDEN: {
			return this.reportGemeindenService.generateExcelReportGemeinden(locale);
		}
		}
		throw new IllegalArgumentException("No Report generated: Unknown ReportType: " + workJobType);
	}

	private UploadFileInfo generateReportMassenversand(
		@Nonnull LocalDate dateFrom, @Nonnull LocalDate dateTo, @Nonnull String gesuchPeriodeId, @Nonnull Locale locale
	) throws ExcelMergeException {
		boolean inklBgGesuche = Boolean.parseBoolean(getParameters().getProperty(WorkJobConstants.INKL_BG_GESUCHE));
		boolean inklMischGesuche = Boolean.parseBoolean(getParameters().getProperty(WorkJobConstants.INKL_MISCH_GESUCHE));
		boolean inklTsGesuche = Boolean.parseBoolean(getParameters().getProperty(WorkJobConstants.INKL_TS_GESUCHE));
		boolean ohneFolgegesuche = Boolean.parseBoolean(getParameters().getProperty(WorkJobConstants.OHNE_ERNEUERUNGSGESUCHE));
		final String text = getParameters().getProperty(WorkJobConstants.TEXT);
		UploadFileInfo uploadFileInfo = reportMassenversandService.generateExcelReportMassenversand(
			dateFrom,
			dateTo,
			gesuchPeriodeId,
			inklBgGesuche,
			inklMischGesuche,
			inklTsGesuche,
			ohneFolgegesuche,
			text,
			locale
		);
		return uploadFileInfo;
	}

	private UploadFileInfo generateReportNotrecht() throws ExcelMergeException {
		boolean zahlungenAusloesen = Boolean.parseBoolean(getParameters().getProperty(WorkJobConstants.DO_SAVE));
		return this.reportNotrechtService.generateExcelReportNotrecht(zahlungenAusloesen);
	}

	private Properties getParameters() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getParameters(jobCtx.getExecutionId());
	}
}
