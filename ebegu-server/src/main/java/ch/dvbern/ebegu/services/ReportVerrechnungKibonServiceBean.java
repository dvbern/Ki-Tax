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

package ch.dvbern.ebegu.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Kind_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.reporting.ReportVerrechnungKibonService;
import ch.dvbern.ebegu.reporting.verrechnungKibon.VerrechnungKibonDataRow;
import ch.dvbern.ebegu.reporting.vrerechnungKibon.VerrechnungKibonExcelConverter;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Stateless
@Local(ReportVerrechnungKibonService.class)
@PermitAll
public class ReportVerrechnungKibonServiceBean extends AbstractReportServiceBean implements ReportVerrechnungKibonService {

	private static final Logger LOG = LoggerFactory.getLogger(ReportVerrechnungKibonServiceBean.class);

	private VerrechnungKibonExcelConverter verrechnungKibonExcelConverter = new VerrechnungKibonExcelConverter();

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private Persistence persistence;

	@Inject
	private EbeguConfiguration ebeguConfiguration;


	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS })
	public List<VerrechnungKibonDataRow> getReportVerrechnungKibon(
		@Nonnull Locale locale
	) {

		List<VerrechnungKibonDataRow> allGemeindenUndPerioden = new ArrayList<>();

		Collection<Gesuchsperiode> gesuchsperioden = gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden();
		Collection<Gemeinde> gemeinden = gemeindeService.getAktiveGemeinden();

		for (Gesuchsperiode gesuchsperiode : gesuchsperioden) {
			Map<Gemeinde, List<Gesuch>> gemeindeListMap = handleGesuchsperiode(gesuchsperiode);
			// Sicherstellen, dass alle Gemeinden in der Liste sind, auch wenn sie keine Kinder (in dieser Gesuchsperiode) haben
			for (Gemeinde gemeinde : gemeinden) {
				if (!gemeindeListMap.containsKey(gemeinde)) {
					gemeindeListMap.put(gemeinde, new ArrayList<>());
				}
			}

			List<VerrechnungKibonDataRow> reportDataVerrechnungKibon = createReportDataVerrechnungKibon(gesuchsperiode, gemeindeListMap);
			allGemeindenUndPerioden.addAll(reportDataVerrechnungKibon);
		}

		Collections.sort(allGemeindenUndPerioden);
		return allGemeindenUndPerioden;
	}

	private Map<Gemeinde, List<Gesuch>> handleGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<KindContainer> root = query.from(KindContainer.class);
		Join<KindContainer, Gesuch> joinGesuch = root.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<KindContainer, Kind> joinKind = root.join(KindContainer_.KIND_JA, JoinType.LEFT);

		Predicate predicateBetreuung = cb.equal(joinKind.get(Kind_.familienErgaenzendeBetreuung), Boolean.TRUE);
		Predicate predicateStatus = joinGesuch.get(Gesuch_.status).in(AntragStatus.getAllFreigegebeneStatus());
		Predicate predicateGesuchsperiode = cb.equal(joinGesuch.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		query.where(predicateBetreuung, predicateStatus, predicateGesuchsperiode);
		query.select(root.get(KindContainer_.gesuch));
		List<Gesuch> allGesuche = persistence.getCriteriaResults(query);

		Map<Gemeinde, List<Gesuch>> newestGesucheFuerDossierUndGesuchsperiode = EbeguUtil.groupByDossierAndSelectNewestAntrag(allGesuche);
		return newestGesucheFuerDossierUndGesuchsperiode;
	}

	private List<VerrechnungKibonDataRow> createReportDataVerrechnungKibon(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Map<Gemeinde, List<Gesuch>> gemeindeListMap
	) {
		List<VerrechnungKibonDataRow> result = new ArrayList<>();
		for (Gemeinde gemeinde : gemeindeListMap.keySet()) {
			VerrechnungKibonDataRow row = new VerrechnungKibonDataRow();
			row.setGemeinde(gemeinde.getName());
			row.setGesuchsperiode(gesuchsperiode.getGesuchsperiodeString());
			List<Gesuch> gesuchList = gemeindeListMap.get(gemeinde);

			long countAlleKinder = 0;
			for (Gesuch gesuch : gesuchList) {
				long countKinderOfGesuch = 0;

				StringBuilder kindernamen = new StringBuilder();
				for (KindContainer kindContainer : gesuch.getKindContainers()) {
					if (kindContainer.getKindJA().getFamilienErgaenzendeBetreuung()) {
						countKinderOfGesuch++;
						kindernamen.append(kindContainer.getKindJA().getVorname()).append(' ');
					}
				}
				if (ebeguConfiguration.getIsDevmode()) {
					String debug = gemeinde.getName() + ';'
						+ gesuchsperiode.getGesuchsperiodeString() + ';'
						+ gesuch.getFall().getFallNummer() + ';'
						+ kindernamen + ';'
						+ countKinderOfGesuch + ';';
					LOG.info(debug);
				}

				countAlleKinder += countKinderOfGesuch;
			}
			row.setKinderTotal(countAlleKinder);
			result.add(row);
		}
		return result;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS })
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportVerrechnungKibon(
		@Nonnull Locale locale
	) throws ExcelMergeException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_VERRECHNUNG_KIBON;

		InputStream is = ReportMassenversandServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<VerrechnungKibonDataRow> reportData = getReportVerrechnungKibon(locale);
		ExcelMergerDTO excelMergerDTO = verrechnungKibonExcelConverter.toExcelMergerDTO(reportData);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		verrechnungKibonExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			ServerMessageUtil.translateEnumValue(reportVorlage.getDefaultExportFilename(), locale) + ".xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}
}
