/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
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
import ch.dvbern.ebegu.enums.VerrechnungKibonKategorie;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.reporting.ReportVerrechnungKibonService;
import ch.dvbern.ebegu.reporting.verrechnungKibon.VerrechnungKibonDataRow;
import ch.dvbern.ebegu.reporting.verrechnungKibon.VerrechnungKibonExcelConverter;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
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

@Stateless
@Local(ReportVerrechnungKibonService.class)
public class ReportVerrechnungKibonServiceBean extends AbstractReportServiceBean implements ReportVerrechnungKibonService {

	private VerrechnungKibonExcelConverter verrechnungKibonExcelConverter = new VerrechnungKibonExcelConverter();

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private Persistence persistence;


	@SuppressWarnings("SimplifyStreamApiCallChains")
	@Nonnull
	@Override
	public List<VerrechnungKibonDataRow> getReportVerrechnungKibon(
		boolean doSave, @Nonnull BigDecimal betragProKind, @Nonnull Locale locale
	) {
		List<VerrechnungKibonDataRow> allGemeindenUndPerioden = new ArrayList<>();

		// Es müssen alle aktiven Gesuchsperioden und alle aktiven Gemeinden berücksichtigt werden
		Collection<Gesuchsperiode> gesuchsperioden = gesuchsperiodeService.getAllAktivUndInaktivGesuchsperioden();
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
				createVerrechnungDetailsForGesuchsperiode(
					gesuchsperiode,
					gemeindeListMap,
					aktuelleVerrechnung
				);
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

			Map<VerrechnungKibonKategorie, Long> alleKinder = initVerrechnungskategorieMap();
			for (Gesuch gesuch : gesuchList) {
				Map<VerrechnungKibonKategorie, Long> kinderOfGesuch = initVerrechnungskategorieMap();
				for (KindContainer kindContainer : gesuch.getKindContainers()) {
					// Das Kind muss mindestens die Checkbox angeklickt haben, auch wenn dann vielleicht
					// kein Angebot ausgewaehlt wird
					if (kindContainer.getKindJA().getFamilienErgaenzendeBetreuung()) {
						VerrechnungKibonKategorie kategorie = evaluateVerechnungskategorie(kindContainer);
						kinderOfGesuch.put(kategorie, kinderOfGesuch.get(kategorie) + 1);
					}
				}
				for (VerrechnungKibonKategorie value : VerrechnungKibonKategorie.values()) {
					alleKinder.put(value, alleKinder.get(value) + kinderOfGesuch.get(value));
				}
			}
			verrechnungskategorieMapToDetail(alleKinder, row);
			result.add(row);
		}
		return result;
	}

	private void verrechnungskategorieMapToDetail(
		@Nonnull Map<VerrechnungKibonKategorie, Long> allKinder,
		@Nonnull VerrechnungKibonDetail detail
	) {
		detail.setTotalBg(allKinder.get(VerrechnungKibonKategorie.BG));
		detail.setTotalTs(allKinder.get(VerrechnungKibonKategorie.TS));
		detail.setTotalBgTs(allKinder.get(VerrechnungKibonKategorie.BG_TS));
		detail.setTotalFi(allKinder.get(VerrechnungKibonKategorie.FI));
		detail.setTotalTagi(allKinder.get(VerrechnungKibonKategorie.TAGI));
		detail.setTotalFiTagi(allKinder.get(VerrechnungKibonKategorie.FI_TAGI));
		detail.setTotalKeinAngebot(allKinder.get(VerrechnungKibonKategorie.KEIN_ANGEBOT));
	}

	private Map<VerrechnungKibonKategorie, Long> initVerrechnungskategorieMap() {
		Map<VerrechnungKibonKategorie, Long> map = new HashMap<>();
		for (VerrechnungKibonKategorie value : VerrechnungKibonKategorie.values()) {
			map.put(value, 0L);
		}
		return map;
	}

	private VerrechnungKibonKategorie evaluateVerechnungskategorie(@Nonnull KindContainer kind) {
		Set<Betreuung> betreuungen = kind.getBetreuungen();
		Set<AnmeldungTagesschule> anmeldungenTagesschule = kind.getAnmeldungenTagesschule();
		Set<AnmeldungFerieninsel> anmeldungenFerieninsel = kind.getAnmeldungenFerieninsel();
		boolean keinAngebot = betreuungen.isEmpty() && anmeldungenTagesschule.isEmpty() && anmeldungenFerieninsel.isEmpty();
		if (keinAngebot) {
			return VerrechnungKibonKategorie.KEIN_ANGEBOT;
		}
		boolean hasBG = !betreuungen.isEmpty();
		boolean hasTS = false;
		boolean hasTagi = false;
		boolean hasFI = !anmeldungenFerieninsel.isEmpty();
		for (AnmeldungTagesschule anmeldungTagesschule : anmeldungenTagesschule) {
			if (anmeldungTagesschule.isTagesschuleTagi()) {
				hasTagi = true;
			} else {
				hasTS = true;
			}
			// Sobald beide TRUE sind, muessen wir nicht weitersuchen
			if (hasTS && hasTagi) {
				break;
			}
		}
		if (hasBG && hasTS) {
			return VerrechnungKibonKategorie.BG_TS;
		}
		if (hasBG) {
			return VerrechnungKibonKategorie.BG;
		}
		if (hasTS) {
			return VerrechnungKibonKategorie.TS;
		}
		if (hasFI && hasTagi) {
			return VerrechnungKibonKategorie.FI_TAGI;
		}
		if (hasFI) {
			return VerrechnungKibonKategorie.FI;
		}
		return VerrechnungKibonKategorie.TAGI;
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
		row.setKinderKantonTotal(currentVerrechnungDetail.getTotalKanton());
		row.setKinderBgTotal(currentVerrechnungDetail.getTotalBgAndBgTs());
		row.setKinderTsTotal(currentVerrechnungDetail.getTotalTsAndBgTs());
		row.setKinderKeinAngebotTotal(currentVerrechnungDetail.getTotalKeinAngebot());
		row.setKinderGemeindeTotal(currentVerrechnungDetail.getTotalGemeinde());
		row.setKinderFiTotal(currentVerrechnungDetail.getTotalFiAndFiTagi());
		row.setKinderTagiTotal(currentVerrechnungDetail.getTotalTagiAndFiTagi());

		if (lastVerrechnungDetail != null) {
			row.setKinderKantonBereitsVerrechnet(lastVerrechnungDetail.getTotalKanton());
			row.setKinderBgBereitsVerrechnet(lastVerrechnungDetail.getTotalBgAndBgTs());
			row.setKinderTsBereitsVerrechnet(lastVerrechnungDetail.getTotalTsAndBgTs());
			row.setKinderKeinAngebotBereitsVerrechnet(lastVerrechnungDetail.getTotalKeinAngebot());
			row.setKinderGemeindeBereitsVerrechnet(lastVerrechnungDetail.getTotalGemeinde());
			row.setKinderFiBereitsVerrechnet(lastVerrechnungDetail.getTotalFiAndFiTagi());
			row.setKinderTagiBereitsVerrechnet(lastVerrechnungDetail.getTotalTagiAndFiTagi());
		} else {
			row.setKinderKantonBereitsVerrechnet(0L);
			row.setKinderBgBereitsVerrechnet(0L);
			row.setKinderTsBereitsVerrechnet(0L);
			row.setKinderKeinAngebotBereitsVerrechnet(0L);
			row.setKinderGemeindeBereitsVerrechnet(0L);
			row.setKinderFiBereitsVerrechnet(0L);
			row.setKinderTagiBereitsVerrechnet(0L);
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
