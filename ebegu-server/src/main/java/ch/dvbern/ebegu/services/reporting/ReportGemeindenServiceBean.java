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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.reporting.gemeinden.GemeindenDataRow;
import ch.dvbern.ebegu.reporting.gemeinden.GemeindenDatenDataRow;
import ch.dvbern.ebegu.reporting.gemeinden.GemeindenExcelConverter;
import ch.dvbern.ebegu.reporting.ReportGemeindenService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeKennzahlenService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import static java.util.Objects.requireNonNull;

@Stateless
@Local(ReportGemeindenService.class)
public class ReportGemeindenServiceBean extends AbstractReportServiceBean implements ReportGemeindenService {

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private GemeindenExcelConverter gemeindenExcelConverter;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeKennzahlenService gemeindeKennzahlenService;

	@Inject
	private PrincipalBean principal;
	private static final String GEMEINDE_PERIODEN_SHEET_NAME = "Angaben pro Periode";
	private static final String GEMEINDE_SHEET_NAME = "Angaben pro Gemeinde";

	@Nonnull
	@Override
	public UploadFileInfo generateExcelReportGemeinden(
		@Nonnull Locale locale) throws ExcelMergeException {
		ReportVorlage vorlage = ReportVorlage.VORLAGE_REPORT_GEMEINDEN;
		InputStream is = ReportServiceBean.class.getResourceAsStream(vorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + vorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(GEMEINDE_SHEET_NAME);
		Sheet secondSheet = workbook.getSheet(GEMEINDE_PERIODEN_SHEET_NAME);

		final Collection<Gemeinde> aktiveGemeinden = gemeindeService.getAktiveGemeinden();

		List<GemeindenDataRow> reportData = getReportDataGemeinden(aktiveGemeinden, locale);

		ExcelMergerDTO excelMergerDTO = gemeindenExcelConverter.toExcelMergerDTO(reportData,
			requireNonNull(principal.getMandant()), locale);
		mergeData(sheet, excelMergerDTO, vorlage.getMergeFields());
		mergeData(secondSheet, excelMergerDTO, vorlage.getMergeFields());
		gemeindenExcelConverter.applyAutoSize(sheet);
		gemeindenExcelConverter.applyAutoSize(secondSheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			"Gemeinden.xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	private List<GemeindenDataRow> getReportDataGemeinden(
		@Nonnull Collection<Gemeinde> gemeinden,
		@Nonnull Locale locale) {
		Collection<Gesuchsperiode> allActiveGesuchsperioden = gesuchsperiodeService.getAllActiveGesuchsperioden();

		Map<String, GemeindeKennzahlen> gemeindeAntragGesuchsperiodeCache = new HashMap<>();
		List<GemeindeKennzahlen> gemeindeAntrags = gemeindeKennzahlenService.findAllAbgeschlosseneGemeindeKennzahlen();
		gemeindeAntrags.forEach(
			gemeindeAntrag -> gemeindeAntragGesuchsperiodeCache.put(
				gemeindeAntrag.getGesuchsperiode().getId() + gemeindeAntrag.getGemeinde().getId(),
				gemeindeAntrag)
		);

		return gemeinden.stream()
			.map(gemeinde -> {
				GemeindenDataRow dataRow = new GemeindenDataRow();

				dataRow.setNameGemeinde(gemeinde.getName());
				dataRow.setBfsNummer(gemeinde.getBfsNummer());
				dataRow.setAngebotBG(gemeinde.isAngebotBG());
				dataRow.setAngebotTS(gemeinde.isAngebotTS());
				dataRow.setStartdatumBG(gemeinde.getBetreuungsgutscheineStartdatum());

				gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId())
					.ifPresent(gemeindeStammdaten -> {
						dataRow.setKorrespondenzspracheGemeinde(
							ServerMessageUtil.getMessage(
								"Reports_" + gemeindeStammdaten.getKorrespondenzsprache(),
								locale));
						if (!gemeindeStammdaten.getGutscheinSelberAusgestellt()
							&& gemeindeStammdaten.getGemeindeAusgabestelle() != null) {
							dataRow.setGutscheinausgabestelle(gemeindeStammdaten.getGemeindeAusgabestelle().getName());
						}
					});

				allActiveGesuchsperioden.forEach(gesuchsperiode -> {
					GemeindenDatenDataRow gemeindenDatenDataRow = new GemeindenDatenDataRow();

					gemeindenDatenDataRow.setGesuchsperiode(gesuchsperiode.getGesuchsperiodeString());

					Einstellung gmeindeBGBisUndMit =
						einstellungService.findEinstellung(
							EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
							gemeinde,
							gesuchsperiode);
					gemeindenDatenDataRow.setLimitierungKita(
						ServerMessageUtil.getMessage("EinschulungTyp_" + gmeindeBGBisUndMit.getValue(), locale));

					Einstellung erwerbspensumZuschlag =
						einstellungService.findEinstellung(
							EinstellungKey.ERWERBSPENSUM_ZUSCHLAG,
							gemeinde,
							gesuchsperiode);
					gemeindenDatenDataRow.setErwerbspensumZuschlag(erwerbspensumZuschlag.getValueAsBigDecimal());

					GemeindeKennzahlen gemeindeKennzahlen = gemeindeAntragGesuchsperiodeCache.get(gesuchsperiode.getId() + gemeinde.getId());
						if(gemeindeKennzahlen != null) {
							gemeindenDatenDataRow.setKontingentierung(gemeindeKennzahlen.getGemeindeKontingentiert());
							gemeindenDatenDataRow.setNachfrageErfuellt(gemeindeKennzahlen.getNachfrageErfuellt());
							gemeindenDatenDataRow.setNachfrageAnzahl(gemeindeKennzahlen.getNachfrageAnzahl());
							gemeindenDatenDataRow.setNachfrageDauer(gemeindeKennzahlen.getNachfrageDauer());
							gemeindenDatenDataRow.setLimitierungTfo(
									ServerMessageUtil.getMessage("EinschulungTyp_" + gemeindeKennzahlen.getLimitierungTfo(), locale)
							);
						}

					dataRow.getGemeindenDaten().add(gemeindenDatenDataRow);
				});

				return dataRow;
			})
			.collect(Collectors.toList());
	}
}
