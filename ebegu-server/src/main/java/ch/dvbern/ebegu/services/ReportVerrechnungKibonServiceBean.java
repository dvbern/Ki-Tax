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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Kind_;
import ch.dvbern.ebegu.entities.VerrechnungKibon;
import ch.dvbern.ebegu.entities.VerrechnungKibonDetail;
import ch.dvbern.ebegu.entities.VerrechnungKibonDetail_;
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


	@SuppressWarnings("SimplifyStreamApiCallChains")
	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS })
	public List<VerrechnungKibonDataRow> getReportVerrechnungKibon(
		boolean doSave, @Nonnull BigDecimal betragProKind, @Nonnull Locale locale
	) {
		List<VerrechnungKibonDataRow> allGemeindenUndPerioden = new ArrayList<>();

		// Es müssen alle aktiven Gesuchsperioden und alle aktiven Gemeinden berücksichtigt werden
		Collection<Gesuchsperiode> gesuchsperioden = gesuchsperiodeService.getAllNichtAbgeschlosseneGesuchsperioden();
		Collection<Gemeinde> gemeinden = gemeindeService.getAktiveGemeinden();

		// Die Daten der letzten Verrechnung lesen
		List<VerrechnungKibonDetail> letzteVerrechnungDetails = getLetzteVerrechnungDetails();

		// Die neue Verrechnung erstellen
		VerrechnungKibon aktuelleVerrechnung = new VerrechnungKibon();
		List<VerrechnungKibonDetail> aktuelleVerrechnungDetails = new ArrayList<>();

		// über alle Gesuchsperioden und Gemeinden iterieren
		for (Gesuchsperiode gesuchsperiode : gesuchsperioden) {
			Map<Gemeinde, List<Gesuch>> gemeindeListMap = getRelevanteGesucheFuerGesuchsperiode(gesuchsperiode);
			// Sicherstellen, dass alle Gemeinden in der Liste sind, auch wenn sie keine Kinder (in dieser Gesuchsperiode) haben
			for (Gemeinde gemeinde : gemeinden) {
				if (!gemeindeListMap.containsKey(gemeinde)) {
					gemeindeListMap.put(gemeinde, new ArrayList<>());
				}
			}
			// Fuer diese Gesuchsperiode: Alle Gemeinden verarbeiten
			List<VerrechnungKibonDetail> verrechnungDetailsForGesuchsperiode =
				createVerrechnungDetailsForGesuchsperiode(gesuchsperiode, gemeindeListMap, aktuelleVerrechnung);
			aktuelleVerrechnungDetails.addAll(verrechnungDetailsForGesuchsperiode);
		}

		// Aus den Verrechnungsdetails Report-Rows generieren, die Verrechnungsdetails gegebenenfalls speichern
		for (VerrechnungKibonDetail verrechnungKibonDetail : aktuelleVerrechnungDetails) {
			// Das Detail der entsprechenden letzten Verrechnung suchen
			Gemeinde gemeinde = verrechnungKibonDetail.getGemeinde();
			Gesuchsperiode gesuchsperiode = verrechnungKibonDetail.getGesuchsperiode();
			VerrechnungKibonDetail lastVerrechnungDetail = letzteVerrechnungDetails.stream()
				.filter(detail -> detail.getGemeinde().equals(gemeinde))
				.filter(detail -> detail.getGesuchsperiode().equals(gesuchsperiode))
				.collect(Collectors.reducing((a, b) -> {
					throw new IllegalStateException("Zu viele Verrechnungsdetails gefunden für Gemeinde "
						+ gemeinde.getName() + " und Gesuchsperiode " + gesuchsperiode.getGesuchsperiodeString());
				}))
				.orElse(null);
			// In Report-Rows konvertieren
			allGemeindenUndPerioden.add(toDataRow(verrechnungKibonDetail, lastVerrechnungDetail, betragProKind));
			if (doSave) {
				// Speichern, falls nicht im Entwurfs-Modus
				persistence.persist(verrechnungKibonDetail);
			}
		}

		// Nach Gemeinde aufsteigend sortiert zurueckgeben
		Collections.sort(allGemeindenUndPerioden);
		return allGemeindenUndPerioden;
	}

	private Map<Gemeinde, List<Gesuch>> getRelevanteGesucheFuerGesuchsperiode(
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

	private List<VerrechnungKibonDetail> createVerrechnungDetailsForGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Map<Gemeinde, List<Gesuch>> gemeindeListMap,
		@Nonnull VerrechnungKibon verrechnungAktuell
	) {
		List<VerrechnungKibonDetail> result = new ArrayList<>();
		for (Entry<Gemeinde, List<Gesuch>> gemeindeListEntry : gemeindeListMap.entrySet()) {
			VerrechnungKibonDetail row = new VerrechnungKibonDetail();
			row.setVerrechnungKibon(verrechnungAktuell);
			row.setGemeinde(gemeindeListEntry.getKey());
			row.setGesuchsperiode(gesuchsperiode);
			List<Gesuch> gesuchList = gemeindeListEntry.getValue();

			StringBuilder details = new StringBuilder();
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
				String debug = (gemeindeListEntry.getKey()).getName() + ';'
					+ gesuchsperiode.getGesuchsperiodeString() + ';'
					+ gesuch.getFall().getFallNummer() + ';'
					+ kindernamen + ';'
					+ countKinderOfGesuch + ';';
				// Zur Kontrolle die Details loggen
				if (ebeguConfiguration.getIsDevmode()) {
					LOG.info(debug);
				}
				details.append(debug).append('\n');
				countAlleKinder += countKinderOfGesuch;
			}
			row.setTotalKinderVerrechnet(countAlleKinder);
			row.setDetails(details.toString());
			result.add(row);
		}
		return result;
	}

	@Nonnull
	private VerrechnungKibonDataRow toDataRow(
		@Nonnull VerrechnungKibonDetail currentVerrechnungDetail,
		@Nullable VerrechnungKibonDetail lastVerrechnungDetail,
		@Nonnull BigDecimal betragProKind
	) {
		VerrechnungKibonDataRow row = new VerrechnungKibonDataRow();
		row.setGemeinde(currentVerrechnungDetail.getGemeinde().getName());
		row.setGesuchsperiode(currentVerrechnungDetail.getGesuchsperiode().getGesuchsperiodeString());
		row.setKinderTotal(currentVerrechnungDetail.getTotalKinderVerrechnet());
		if (lastVerrechnungDetail != null) {
			row.setKinderBereitsVerrechnet(lastVerrechnungDetail.getTotalKinderVerrechnet());
		} else {
			row.setKinderBereitsVerrechnet(0L);
		}
		row.setBetragProKind(betragProKind);
		return row;
	}

	@Nonnull
	private Optional<VerrechnungKibon> getLetzteVerrechnung() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerrechnungKibon> query = cb.createQuery(VerrechnungKibon.class);

		Root<VerrechnungKibon> root = query.from(VerrechnungKibon.class);

		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<VerrechnungKibon> criteriaResults = persistence.getCriteriaResults(query, 1);

		return criteriaResults.isEmpty()
			? Optional.empty()
			: Optional.ofNullable(criteriaResults.get(0));
	}

	@Nonnull
	private List<VerrechnungKibonDetail> getLetzteVerrechnungDetails() {
		Optional<VerrechnungKibon> letzteVerrechnungOptional = getLetzteVerrechnung();
		if (letzteVerrechnungOptional.isPresent()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<VerrechnungKibonDetail> query = cb.createQuery(VerrechnungKibonDetail.class);

			Root<VerrechnungKibonDetail> root = query.from(VerrechnungKibonDetail.class);
			Predicate predicate = cb.equal(root.get(VerrechnungKibonDetail_.verrechnungKibon), letzteVerrechnungOptional.get());
			query.where(predicate);
			return persistence.getCriteriaResults(query);
		}
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	@RolesAllowed(SUPER_ADMIN)
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportVerrechnungKibon(
		boolean doSave, @Nonnull BigDecimal betragProKind, @Nonnull Locale locale
	) throws ExcelMergeException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_VERRECHNUNG_KIBON;

		InputStream is = ReportMassenversandServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<VerrechnungKibonDataRow> reportData = getReportVerrechnungKibon(doSave, betragProKind, locale);
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
