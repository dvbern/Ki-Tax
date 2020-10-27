/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

package ch.dvbern.ebegu.services;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.BenutzerTableFilterDTO;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.AntragStatusHistory_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraum;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.ReportService;
import ch.dvbern.ebegu.reporting.benutzer.BenutzerDataRow;
import ch.dvbern.ebegu.reporting.benutzer.BenutzerExcelConverter;
import ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung.GesuchstellerKinderBetreuungDataRow;
import ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung.GesuchstellerKinderBetreuungExcelConverter;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GesuchStichtagDataRow;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GesuchStichtagExcelConverter;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GesuchZeitraumDataRow;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GesuchZeitraumExcelConverter;
import ch.dvbern.ebegu.reporting.kanton.KantonDataRow;
import ch.dvbern.ebegu.reporting.kanton.KantonExcelConverter;
import ch.dvbern.ebegu.reporting.kanton.institutionen.InstitutionenDataRow;
import ch.dvbern.ebegu.reporting.kanton.institutionen.InstitutionenExcelConverter;
import ch.dvbern.ebegu.reporting.kanton.mitarbeiterinnen.MitarbeiterinnenDataRow;
import ch.dvbern.ebegu.reporting.kanton.mitarbeiterinnen.MitarbeiterinnenExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ZahlungAuftragDetailsExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ZahlungAuftragPeriodeExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ZahlungAuftragTotalsExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungsauftrag.ZahlungDataRow;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setGemeindeFilterForCurrentUser;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("PMD.NcssTypeCount")
@Stateless
@Local(ReportService.class)
public class ReportServiceBean extends AbstractReportServiceBean implements ReportService {

	private static final String NO_USER_IS_LOGGED_IN = "No User is logged in";



	@Inject
	private BenutzerService benutzerService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceBean.class);

	// Excel kann nicht mit Datum vor 1800 umgehen. Wir setzen auf 1900, wie Minimum im datepicker
	private static final LocalDate MIN_DATE = LocalDate.of(1900, Month.JANUARY, 1);

	@Inject
	private GesuchStichtagExcelConverter gesuchStichtagExcelConverter;

	@Inject
	private GesuchZeitraumExcelConverter gesuchZeitraumExcelConverter;

	@Inject
	private KantonExcelConverter kantonExcelConverter;

	@Inject
	private MitarbeiterinnenExcelConverter mitarbeiterinnenExcelConverter;

	@Inject
	private BenutzerExcelConverter benutzerExcelConverter;

	@Inject
	private InstitutionenExcelConverter institutionenExcelConverter;

	@Inject
	private ZahlungAuftragDetailsExcelConverter zahlungAuftragDetailsExcelConverter;

	@Inject
	private ZahlungAuftragTotalsExcelConverter zahlungAuftragTotalsExcelConverter;

	@Inject
	private ZahlungAuftragPeriodeExcelConverter zahlungAuftragPeriodeExcelConverter;

	@Inject
	private GesuchstellerKinderBetreuungExcelConverter gesuchstellerKinderBetreuungExcelConverter;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private Persistence persistence;

	@Inject
	private ZahlungService zahlungService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private KindService kindService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private EinstellungService einstellungService;

	@SuppressWarnings("Duplicates")
	@Nonnull
	@Override
	public List<GesuchStichtagDataRow> getReportDataGesuchStichtag(
		@Nonnull LocalDate date,
		@Nullable String gesuchPeriodeID) {

		requireNonNull(date, "Das Argument 'date' darf nicht leer sein");

		EntityManager em = persistence.getEntityManager();

		//noinspection JpaQueryApiInspection
		TypedQuery<GesuchStichtagDataRow> query =
			em.createNamedQuery("GesuchStichtagNativeSQLQuery", GesuchStichtagDataRow.class);

		// Wir rechnen zum Stichtag einen Tag dazu, damit es bis 24.00 des Vorabends gilt.
		query.setParameter("stichTagDate", Constants.SQL_DATE_FORMAT.format(date.plusDays(1)));
		query.setParameter("gesuchPeriodeID", gesuchPeriodeID);
		query.setParameter("onlySchulamt", onlySchulamt());
		final List<String> berechtigteGemeinden = getListOfBerechtigteGemeinden();
		// we need to remove the extra - as in the query they are not working and we cannot use a REPLACE function on
		// a list in a native query
		final List<String> berechtigeGemeindenUnhex = new ArrayList<>();
		if (berechtigteGemeinden != null) {
			berechtigteGemeinden.forEach(s -> {
				berechtigeGemeindenUnhex.add(s.replace("-", ""));
			});
		}

		// pass a boolean param to indicate if it has to take all Gemeinden or just those of the user
		// this is easier than checking the list within the sql-query
		query.setParameter("allGemeinden", berechtigteGemeinden == null);
		query.setParameter("gemeindeIdList", berechtigteGemeinden == null ? null :
			berechtigeGemeindenUnhex);
		List<GesuchStichtagDataRow> glist = query.getResultList();

		return glist;
	}

	@Nullable
	private List<String> getListOfBerechtigteGemeinden() {
		Benutzer benutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getListOfBerechtigteGemeinden", "User not logged in"));

		if (!benutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			return null;
		}

		return benutzer.extractGemeindenForUser().stream()
			.map(AbstractEntity::getId)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("Duplicates")
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportGesuchStichtag(
		@Nonnull LocalDate date,
		@Nullable String gesuchPeriodeID,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		requireNonNull(date, "Das Argument 'date' darf nicht leer sein");

		final ReportVorlage reportVorlage = locale.equals(Locale.FRENCH)
			? ReportVorlage.VORLAGE_REPORT_GESUCH_STICHTAG_FR
			: ReportVorlage.VORLAGE_REPORT_GESUCH_STICHTAG_DE;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<GesuchStichtagDataRow> reportData = getReportDataGesuchStichtag(date, gesuchPeriodeID);
		ExcelMergerDTO excelMergerDTO = gesuchStichtagExcelConverter.toExcelMergerDTO(reportData, locale);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		gesuchStichtagExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@SuppressWarnings("Duplicates")
	@Nonnull
	@Override
	public List<GesuchZeitraumDataRow> getReportDataGesuchZeitraum(
		@Nonnull LocalDate dateVon,
		@Nonnull LocalDate dateBis,
		@Nullable String gesuchPeriodeID) {

		validateDateParams(dateVon, dateBis);

		// Bevor wir die Statistik starten, muessen gewissen Werte nachgefuehrt werden
		runStatisticsBetreuung();
		runStatisticsAbwesenheiten();
		runStatisticsKinder();

		EntityManager em = persistence.getEntityManager();

		//noinspection JpaQueryApiInspection
		TypedQuery<GesuchZeitraumDataRow> query =
			em.createNamedQuery("GesuchZeitraumNativeSQLQuery", GesuchZeitraumDataRow.class);

		query.setParameter("fromDateTime", Constants.SQL_DATE_FORMAT.format(dateVon));
		query.setParameter("fromDate", Constants.SQL_DATE_FORMAT.format(dateVon));
		query.setParameter("toDateTime", Constants.SQL_DATE_FORMAT.format(dateBis));
		query.setParameter("toDate", Constants.SQL_DATE_FORMAT.format(dateBis));
		query.setParameter("gesuchPeriodeID", gesuchPeriodeID);
		query.setParameter("onlySchulamt", onlySchulamt());
		final List<String> berechtigteGemeinden = getListOfBerechtigteGemeinden();
		// we need to remove the extra - as in the query they are not working and we cannot use a REPLACE function on
		// a list in a native query
		final List<String> berechtigeGemeindenUnhex = new ArrayList<>();
		if (berechtigteGemeinden != null) {
			berechtigteGemeinden.forEach(s -> {
				berechtigeGemeindenUnhex.add(s.replace("-", ""));
			});
		}
		// pass a boolean param to indicate if it has to take all Gemeinden are just those of the user
		// this is easier than checking the list within the sql-query
		query.setParameter("allGemeinden", berechtigteGemeinden == null);
		query.setParameter("gemeindeIdList", berechtigteGemeinden == null ? null :
			berechtigeGemeindenUnhex);

		return query.getResultList();
	}

	@SuppressWarnings("Duplicates")
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportGesuchZeitraum(
		@Nonnull LocalDate dateVon,
		@Nonnull LocalDate dateBis,
		@Nullable String gesuchPeriodeID,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		validateDateParams(dateVon, dateBis);
		validateDateParams(dateVon, dateBis);

		final ReportVorlage reportVorlage = locale.equals(Locale.FRENCH)
			? ReportVorlage.VORLAGE_REPORT_GESUCH_ZEITRAUM_FR
			: ReportVorlage.VORLAGE_REPORT_GESUCH_ZEITRAUM_DE;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<GesuchZeitraumDataRow> reportData = getReportDataGesuchZeitraum(dateVon, dateBis, gesuchPeriodeID);
		ExcelMergerDTO excelMergerDTO = gesuchZeitraumExcelConverter.toExcelMergerDTO(reportData, locale);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		gesuchZeitraumExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Nonnull
	@SuppressWarnings("PMD.NcssMethodCount, PMD.AvoidDuplicateLiterals")
	@Override
	public List<KantonDataRow> getReportDataKanton(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Locale locale
	) {
		validateDateParams(datumVon, datumBis);

		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getReportDataKanton", NO_USER_IS_LOGGED_IN));

		Collection<Gesuchsperiode> relevanteGesuchsperioden =
			gesuchsperiodeService.getGesuchsperiodenBetween(datumVon, datumBis);
		if (relevanteGesuchsperioden.isEmpty()) {
			return Collections.emptyList();
		}
		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt
		// verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(AbstractPlatz_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(Dossier_.gemeinde, JoinType.LEFT);

		List<Predicate> predicatesToUse = new ArrayList<>();

		// startAbschnitt <= datumBis && endeAbschnitt >= datumVon
		Predicate predicateStart = builder.lessThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			datumBis);
		predicatesToUse.add(predicateStart);
		Predicate predicateEnd = builder.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
			datumVon);
		predicatesToUse.add(predicateEnd);
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = builder.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);
		predicatesToUse.add(predicateGueltig);

		// Sichtbarkeit nach eingeloggtem Benutzer
		boolean isInstitutionsbenutzer =
			principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles());
		if (isInstitutionsbenutzer) {
			Collection<Institution> allowedInstitutionen =
				institutionService.getInstitutionenReadableForCurrentBenutzer(false);
			Predicate predicateAllowedInstitutionen = root.get(VerfuegungZeitabschnitt_.verfuegung)
				.get(Verfuegung_.betreuung)
				.get(Betreuung_.institutionStammdaten)
				.get(InstitutionStammdaten_.institution)
				.in(allowedInstitutionen);
			predicatesToUse.add(predicateAllowedInstitutionen);
		}

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, joinGemeinde, predicatesToUse);

		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicatesToUse));
		List<VerfuegungZeitabschnitt> zeitabschnittList = persistence.getCriteriaResults(query);
		List<KantonDataRow> kantonDataRowList = convertToKantonDataRow(zeitabschnittList);
		kantonDataRowList.sort(Comparator.comparing(KantonDataRow::getBgNummer)
			.thenComparing(KantonDataRow::getZeitabschnittVon));
		return kantonDataRowList;
	}

	@Nonnull
	private List<KantonDataRow> convertToKantonDataRow(List<VerfuegungZeitabschnitt> zeitabschnittList) {
		List<KantonDataRow> kantonDataRowList = new ArrayList<>();
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			KantonDataRow row = new KantonDataRow();
			Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
			Objects.requireNonNull(betreuung);
			final Gesuch gesuch = betreuung.extractGesuch();

			row.setGemeinde(gesuch.extractGemeinde().getName());
			row.setBgNummer(betreuung.getBGNummer());
			row.setGesuchId(gesuch.getId());
			row.setName(betreuung.getKind().getKindJA().getNachname());
			row.setVorname(betreuung.getKind().getKindJA().getVorname());
			row.setGeburtsdatum(betreuung.getKind().getKindJA().getGeburtsdatum());
			if (row.getGeburtsdatum() == null || row.getGeburtsdatum().isBefore(MIN_DATE)) {
				row.setGeburtsdatum(MIN_DATE);
			}
			row.setZeitabschnittVon(zeitabschnitt.getGueltigkeit().getGueltigAb());
			row.setZeitabschnittBis(zeitabschnitt.getGueltigkeit().getGueltigBis());

			// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
			BigDecimal pensumKanton = zeitabschnitt.getBgCalculationResultAsiv().getBgPensumProzent();
			BigDecimal pensumGemeinde = BigDecimal.ZERO;
			BigDecimal pensumTotal = pensumKanton;
			if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung() && zeitabschnitt.getBgCalculationResultGemeinde() != null) {
				// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
				BigDecimal pensumTotalGemeinde = zeitabschnitt.getBgCalculationResultGemeinde().getBgPensumProzent();
				pensumGemeinde = MathUtil.DEFAULT.subtractNullSafe(pensumTotalGemeinde, pensumKanton);
				pensumTotal = pensumTotalGemeinde;
			}
			row.setBgPensumKanton(pensumKanton);
			row.setBgPensumGemeinde(pensumGemeinde);
			row.setBgPensumTotal(pensumTotal);

			row.setElternbeitrag(zeitabschnitt.getElternbeitrag());

			// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
			BigDecimal verguenstigungKanton = zeitabschnitt.getBgCalculationResultAsiv().getVerguenstigung();
			BigDecimal verguenstigungGemeinde = BigDecimal.ZERO;
			BigDecimal verguenstigungTotal = verguenstigungKanton;
			if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung() && zeitabschnitt.getBgCalculationResultGemeinde() != null) {
				// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
				BigDecimal verguenstigungTotalGemeinde = zeitabschnitt.getBgCalculationResultGemeinde().getVerguenstigung();
				verguenstigungGemeinde = MathUtil.DEFAULT.subtractNullSafe(verguenstigungTotalGemeinde, verguenstigungKanton);
				verguenstigungTotal = verguenstigungTotalGemeinde;
			}
			row.setVerguenstigungKanton(verguenstigungKanton);
			row.setVerguenstigungGemeinde(verguenstigungGemeinde);
			row.setVerguenstigungTotal(verguenstigungTotal);

			row.setInstitution(betreuung.getInstitutionStammdaten().getInstitution().getName());
			row.setBetreuungsTyp(betreuung.getBetreuungsangebotTyp().name());
			kantonDataRowList.add(row);
		}
		return kantonDataRowList;
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportKanton(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		validateDateParams(datumVon, datumBis);

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_KANTON;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<KantonDataRow> reportData = getReportDataKanton(datumVon, datumBis, locale);
		ExcelMergerDTO excelMergerDTO =
			kantonExcelConverter.toExcelMergerDTO(reportData, locale, datumVon, datumBis);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		kantonExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	// MitarbeterInnen
	@Nonnull
	@Override
	public List<MitarbeiterinnenDataRow> getReportMitarbeiterinnen(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis) {
		validateDateParams(datumVon, datumBis);

		List<Tuple> numberVerantwortlicheGesuche = getAllVerantwortlicheGesuche();
		List<Tuple> numberVerfuegteGesuche = getAllVerfuegteGesuche(datumVon, datumBis);

		return convertToMitarbeiterinnenDataRow(numberVerantwortlicheGesuche, numberVerfuegteGesuche);
	}

	/**
	 * Gibt eine tuple zurueck mit dem ID, dem Nachnamen und Vornamen des Benutzers und die Anzahl Gesuche
	 * bei denen er verantwortlich ist. Group by Verantwortlicher und oder by Verantwortlicher-nachname
	 */
	@Nonnull
	private List<Tuple> getAllVerantwortlicheGesuche() {
		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getAllVerantwortlicheGesuche", NO_USER_IS_LOGGED_IN));

		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Tuple> query = builder.createTupleQuery();
		query.distinct(true);

		Root<Gesuch> root = query.from(Gesuch.class);

		final Join<Gesuch, Dossier> dossierJoin = root.join(Gesuch_.dossier, JoinType.INNER);
		final Join<Dossier, Benutzer> verantwortlicherJoin =
			dossierJoin.join(Dossier_.verantwortlicherBG, JoinType.INNER);
		SetJoin<Benutzer, Berechtigung> verantwortlicherBerechtigungenJoin =
			verantwortlicherJoin.join(Benutzer_.berechtigungen, JoinType.INNER);
		SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin = verantwortlicherBerechtigungenJoin
			.join(Berechtigung_.gemeindeList, JoinType.LEFT);

		query.multiselect(
			verantwortlicherJoin.get(AbstractEntity_.id).alias(AbstractEntity_.id.getName()),
			verantwortlicherJoin.get(Benutzer_.nachname).alias(Benutzer_.nachname.getName()),
			verantwortlicherJoin.get(Benutzer_.vorname).alias(Benutzer_.vorname.getName()),
			builder.count(root).alias("allVerantwortlicheGesuche")
		);

		query.groupBy(
			verantwortlicherJoin.get(AbstractEntity_.id),
			verantwortlicherJoin.get(Benutzer_.nachname),
			verantwortlicherJoin.get(Benutzer_.vorname)
		);
		query.orderBy(builder.asc(verantwortlicherJoin.get(Benutzer_.nachname)));

		List<Predicate> predicates = new ArrayList<>();
		// Der Benutzer muss eine aktive Berechtigung mit Rolle ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE oder
		// SACHBEARBEITER_GEMEINDE haben
		Path<DateRange> dateRange = verantwortlicherBerechtigungenJoin.get(AbstractDateRangedEntity_.gueltigkeit);
		Predicate predicateActive = builder.between(
			builder.literal(LocalDate.now()),
			dateRange.get(DateRange_.gueltigAb),
			dateRange.get(DateRange_.gueltigBis)
		);
		predicates.add(predicateActive);

		Set<UserRole> requiredRoles = Sets.newHashSet(
			UserRole.ADMIN_BG,
			UserRole.SACHBEARBEITER_BG,
			UserRole.ADMIN_GEMEINDE,
			UserRole.SACHBEARBEITER_GEMEINDE);

		Predicate isRolleCorrect =
			verantwortlicherBerechtigungenJoin.get(Berechtigung_.role).in(requiredRoles);
		predicates.add(isRolleCorrect);

		if (principalBean.discoverMostPrivilegedRole() != UserRole.SUPER_ADMIN) {
			// for others than superadmin, Superadmin cannot be listed
			predicates.add(builder.notEqual(
				verantwortlicherBerechtigungenJoin.get(Berechtigung_.role),
				UserRole.SUPER_ADMIN));
		}

		// Nur Benutzer von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, gemeindeSetJoin, predicates);

		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicates));
		return persistence.getCriteriaResults(query);
	}

	/**
	 * Gibt eine tuple zurueck mit dem ID, dem Nachnamen und Vornamen des Benutzers und die Anzahl Gesuche
	 * die er im gegebenen Zeitraum verfuegt hat. Group by Verantwortlicher und oder by Verantwortlicher-nachname
	 */
	@Nonnull
	private List<Tuple> getAllVerfuegteGesuche(LocalDate datumVon, LocalDate datumBis) {
		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getAllVerfuegteGesuche", NO_USER_IS_LOGGED_IN));

		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Tuple> query = builder.createTupleQuery();
		query.distinct(true);

		Root<AntragStatusHistory> root = query.from(AntragStatusHistory.class);
		final Join<AntragStatusHistory, Benutzer> benutzerJoin =
			root.join(AntragStatusHistory_.benutzer, JoinType.INNER);
		SetJoin<Benutzer, Berechtigung> joinBerechtigungen = benutzerJoin.join(Benutzer_.berechtigungen);
		SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin = joinBerechtigungen
			.join(Berechtigung_.gemeindeList, JoinType.LEFT);

		query.multiselect(
			benutzerJoin.get(AbstractEntity_.id).alias(AbstractEntity_.id.getName()),
			benutzerJoin.get(Benutzer_.nachname).alias(Benutzer_.nachname.getName()),
			benutzerJoin.get(Benutzer_.vorname).alias(Benutzer_.vorname.getName()),
			builder.count(root).alias("allVerfuegteGesuche"));

		List<Predicate> predicates = new ArrayList<>();
		if (principalBean.discoverMostPrivilegedRole() != UserRole.SUPER_ADMIN) {
			// for others than superadmin, Superadmin cannot be listed
			predicates.add(builder.notEqual(joinBerechtigungen.get(Berechtigung_.role), UserRole.SUPER_ADMIN));
		}
		// Status ist verfuegt
		predicates.add(root.get(AntragStatusHistory_.status).in(AntragStatus.getAllVerfuegtStates()));
		// Datum der Verfuegung muss nach (oder gleich) dem Anfang des Abfragezeitraums sein
		predicates.add(builder.greaterThanOrEqualTo(
			root.get(AntragStatusHistory_.timestampVon),
			datumVon.atStartOfDay()));
		// Datum der Verfuegung muss vor (oder gleich) dem Ende des Abfragezeitraums sein
		predicates.add(builder.lessThanOrEqualTo(
			root.get(AntragStatusHistory_.timestampVon),
			datumBis.atTime(LocalTime.MAX)));
		// Der Benutzer muss eine aktive Berechtigung mit Rolle ADMIN_BG oder SACHBEARBEITER_BG haben
		predicates.add(builder.between(
			builder.literal(LocalDate.now()),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			joinBerechtigungen.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis)));
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(UserRole.getJugendamtSuperadminRoles()));

		// Nur Benutzer von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, gemeindeSetJoin, predicates);

		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicates));

		query.groupBy(
			benutzerJoin.get(AbstractEntity_.id),
			benutzerJoin.get(Benutzer_.nachname),
			benutzerJoin.get(Benutzer_.vorname));
		query.orderBy(builder.asc(benutzerJoin.get(Benutzer_.nachname)));

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	private List<MitarbeiterinnenDataRow> convertToMitarbeiterinnenDataRow(
		List<Tuple> numberVerantwortlicheGesuche,
		List<Tuple> numberVerfuegteGesuche) {

		final Map<String, MitarbeiterinnenDataRow> result = new HashMap<>();

		for (Tuple tupleVerant : numberVerantwortlicheGesuche) {
			MitarbeiterinnenDataRow row = createMitarbeiterinnenDataRow(tupleVerant,
				new BigDecimal((Long) tupleVerant.get("allVerantwortlicheGesuche")), BigDecimal.ZERO);
			result.put((String) tupleVerant.get(AbstractEntity_.id.getName()), row);
		}

		for (Tuple tupleVerfuegte : numberVerfuegteGesuche) {
			final BigDecimal numberVerfuegte = new BigDecimal((Long) tupleVerfuegte.get("allVerfuegteGesuche"));
			final MitarbeiterinnenDataRow existingRow = result.get(tupleVerfuegte.get(AbstractEntity_.id.getName()));
			if (existingRow != null) {
				existingRow.setVerfuegungenAusgestellt(numberVerfuegte);
			} else {
				MitarbeiterinnenDataRow row =
					createMitarbeiterinnenDataRow(tupleVerfuegte, BigDecimal.ZERO, numberVerfuegte);
				result.put((String) tupleVerfuegte.get(AbstractEntity_.id.getName()), row);
			}
		}

		return new ArrayList<>(result.values());
	}

	@Nonnull
	private MitarbeiterinnenDataRow createMitarbeiterinnenDataRow(
		Tuple tuple,
		BigDecimal numberVerant,
		BigDecimal numberVerfuegte) {

		MitarbeiterinnenDataRow row = new MitarbeiterinnenDataRow();
		row.setName((String) tuple.get(Benutzer_.nachname.getName()));
		row.setVorname((String) tuple.get(Benutzer_.vorname.getName()));
		row.setVerantwortlicheGesuche(numberVerant);
		row.setVerfuegungenAusgestellt(numberVerfuegte);

		return row;
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportMitarbeiterinnen(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		validateDateParams(datumVon, datumBis);

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_MITARBEITERINNEN;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<MitarbeiterinnenDataRow> reportData = getReportMitarbeiterinnen(datumVon, datumBis);
		ExcelMergerDTO excelMergerDTO =
			mitarbeiterinnenExcelConverter.toExcelMergerDTO(reportData, locale, datumVon, datumBis);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		mitarbeiterinnenExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Nonnull
	private String getFileName(ReportVorlage reportVorlage, @Nonnull Locale locale) {
		return ServerMessageUtil.translateEnumValue(reportVorlage.getDefaultExportFilename(), locale) + ".xlsx";
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportZahlungAuftrag(
		@Nonnull String auftragId,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		Zahlungsauftrag zahlungsauftrag = zahlungService.findZahlungsauftrag(auftragId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"generateExcelReportZahlungAuftrag",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				auftragId));

		// Je nach Rolle duerfen im Excel nicht alle Institutionen aufgefuehrt werden
		final UserRole userRole = principalBean.discoverMostPrivilegedRole();
		Collection<Institution> allowedInst = institutionService.getInstitutionenReadableForCurrentBenutzer(false);

		final ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungsauftrag.getZahlungslaufTyp());
		List<ZahlungDataRow> zahlungDataRows = new ArrayList<>();
		for (Zahlung zahlung : zahlungsauftrag.getZahlungen()) {
			if (!EnumUtil.isOneOf(userRole, UserRole.getInstitutionTraegerschaftRoles()) ||
				allowedInst
					.stream()
					.anyMatch(institution -> institution.getId().equals(zahlung.getEmpfaengerId()))) {
				Adresse adresseKontoinhaber = zahlungslaufHelper.getAuszahlungsadresseOrDefaultadresse(zahlung);
				ZahlungDataRow row = new ZahlungDataRow(
					zahlung,
					adresseKontoinhaber
				);
				zahlungDataRows.add(row);
			}
		}

		return getUploadFileInfoZahlung(
			zahlungDataRows,
			zahlungsauftrag.getFilename(),
			zahlungsauftrag.getBeschrieb(),
			zahlungsauftrag.getDatumGeneriert(),
			zahlungsauftrag.getDatumFaellig(),
			zahlungsauftrag.getGemeinde(),
			locale);
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportZahlung(
		@Nonnull String zahlungId,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		List<ZahlungDataRow> reportData = new ArrayList<>();

		Zahlung zahlung = zahlungService.findZahlung(zahlungId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"generateExcelReportZahlung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				zahlungId));

		final ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlung.getZahlungsauftrag().getZahlungslaufTyp());
		Adresse adresseKontoinhaber = zahlungslaufHelper.getAuszahlungsadresseOrDefaultadresse(zahlung);
		ZahlungDataRow dataRow = new ZahlungDataRow(
			zahlung,
			adresseKontoinhaber
		);

		reportData.add(dataRow);

		Zahlungsauftrag zahlungsauftrag = zahlung.getZahlungsauftrag();

		String fileName = zahlungsauftrag.getFilename() + '_' + zahlung.getEmpfaengerName();

		return getUploadFileInfoZahlung(
			reportData,
			fileName,
			zahlungsauftrag.getBeschrieb(),
			zahlungsauftrag.getDatumGeneriert(),
			zahlungsauftrag.getDatumFaellig(),
			zahlungsauftrag.getGemeinde(),
			locale
		);
	}

	@Nonnull
	private UploadFileInfo getUploadFileInfoZahlung(
		@Nonnull List<ZahlungDataRow> reportData,
		@Nonnull String excelFileName,
		@Nonnull String bezeichnung,
		@Nonnull LocalDateTime datumGeneriert,
		@Nonnull LocalDate datumFaellig,
		@Nonnull Gemeinde gemeinde,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_ZAHLUNG_AUFTRAG;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		final UserRole userRole = principalBean.discoverMostPrivilegedRole();
		Collection<Institution> allowedInst = institutionService.getInstitutionenReadableForCurrentBenutzer(false);
		List<ZahlungDataRow> zahlungenBerechtigt = reportData.stream()
			.filter(zahlungDataRow -> {
				// Filtere nur die erlaubten Instituionsdaten
				// User mit der Rolle Institution oder Traegerschaft dürfen nur "Ihre" Institutionsdaten sehen.
				return !EnumUtil.isOneOf(userRole, UserRole.getInstitutionTraegerschaftRoles()) ||
					allowedInst.stream().anyMatch(institution -> institution.getId().equals(zahlungDataRow.getZahlung().getEmpfaengerId()));
			}).collect(Collectors.toList());

		// Blatt Details
		Sheet sheetDetails = workbook.getSheet(reportVorlage.getDataSheetName());
		ExcelMergerDTO excelMergerDTO = zahlungAuftragDetailsExcelConverter.toExcelMergerDTO(
			zahlungenBerechtigt,
			locale,
			ServerMessageUtil.getMessage("Reports_detailpositionenTitle", locale, bezeichnung),
			datumGeneriert,
			datumFaellig,
			gemeinde
		);
		mergeData(sheetDetails, excelMergerDTO, reportVorlage.getMergeFields());
		zahlungAuftragDetailsExcelConverter.applyAutoSize(sheetDetails);

		// Blatt Totals
		Sheet sheetTotals = workbook.getSheet("Totals");
		ExcelMergerDTO excelMergerTotalsDTO = zahlungAuftragTotalsExcelConverter.toExcelMergerDTO(
			zahlungenBerechtigt,
			locale,
			ServerMessageUtil.getMessage("Reports_totalZahlungenTitle", locale, bezeichnung),
			datumGeneriert,
			datumFaellig,
			gemeinde
		);
		mergeData(sheetTotals, excelMergerTotalsDTO, reportVorlage.getMergeFields());
		zahlungAuftragTotalsExcelConverter.applyAutoSize(sheetTotals);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			excelFileName + ".xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportZahlungPeriode(
		@Nonnull String gesuchsperiodeId,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"generateExcelReportZahlungPeriode",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId));

		final Collection<Zahlungsauftrag> zahlungsauftraegeInPeriode = zahlungService.getZahlungsauftraegeInPeriode(
			gesuchsperiode.getGueltigkeit().getGueltigAb(),
			gesuchsperiode.getGueltigkeit().getGueltigBis());

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_ZAHLUNG_AUFTRAG_PERIODE;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		final List<Zahlung> allZahlungen = zahlungsauftraegeInPeriode.stream()
			.flatMap(zahlungsauftrag -> zahlungsauftrag.getZahlungen().stream())
			.collect(Collectors.toList());

		ExcelMergerDTO excelMergerDTO = zahlungAuftragPeriodeExcelConverter.toExcelMergerDTO(
			allZahlungen,
			gesuchsperiode.getGesuchsperiodeString(),
			locale);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		zahlungAuftragPeriodeExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Nonnull
	private List<GesuchstellerKinderBetreuungDataRow> getReportDataGesuchstellerKinderBetreuung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable Gesuchsperiode gesuchsperiode,
		@Nonnull Locale locale
	) {

		List<VerfuegungZeitabschnitt> zeitabschnittList = getReportDataBetreuungen(datumVon, datumBis, gesuchsperiode);
		List<GesuchstellerKinderBetreuungDataRow> dataRows =
			convertToGesuchstellerKinderBetreuungDataRow(zeitabschnittList, locale);

		dataRows.sort(Comparator.comparing(GesuchstellerKinderBetreuungDataRow::getBgNummer)
			.thenComparing(GesuchstellerKinderBetreuungDataRow::getZeitabschnittVon));

		return dataRows;
	}

	@Nonnull
	private List<GesuchstellerKinderBetreuungDataRow> getReportDataKinder(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable Gesuchsperiode gesuchsperiode,
		@Nonnull Locale locale
	) {

		List<VerfuegungZeitabschnitt> zeitabschnittList = getReportDataBetreuungen(datumVon, datumBis, gesuchsperiode);
		List<GesuchstellerKinderBetreuungDataRow> dataRows = convertToKinderDataRow(zeitabschnittList, locale);

		dataRows.sort(Comparator.comparing(GesuchstellerKinderBetreuungDataRow::getBgNummer)
			.thenComparing(GesuchstellerKinderBetreuungDataRow::getZeitabschnittVon));

		return dataRows;
	}

	@Nonnull
	private List<GesuchstellerKinderBetreuungDataRow> getReportDataGesuchsteller(
		@Nonnull LocalDate stichtag,
		@Nonnull Locale locale
	) {
		List<VerfuegungZeitabschnitt> zeitabschnittList = getReportDataBetreuungen(stichtag);

		List<GesuchstellerKinderBetreuungDataRow> dataRows =
			convertToGesuchstellerKinderBetreuungDataRow(zeitabschnittList, locale);

		dataRows.sort(Comparator.comparing(GesuchstellerKinderBetreuungDataRow::getBgNummer)
			.thenComparing(GesuchstellerKinderBetreuungDataRow::getZeitabschnittVon));

		return dataRows;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Nonnull
	private List<VerfuegungZeitabschnitt> getReportDataBetreuungen(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable Gesuchsperiode gesuchsperiode) {
		validateDateParams(datumVon, datumBis);

		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getReportDataBetreuungen", NO_USER_IS_LOGGED_IN));

		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt
		// verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(Betreuung_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(Dossier_.gemeinde, JoinType.LEFT);

		List<Predicate> predicatesToUse = new ArrayList<>();

		// startAbschnitt <= datumBis && endeAbschnitt >= datumVon
		Path<DateRange> dateRangePath = root.get(AbstractDateRangedEntity_.gueltigkeit);
		Predicate predicateStart = builder.lessThanOrEqualTo(dateRangePath.get(DateRange_.gueltigAb), datumBis);
		predicatesToUse.add(predicateStart);
		Predicate predicateEnd = builder.greaterThanOrEqualTo(dateRangePath.get(DateRange_.gueltigBis), datumVon);
		predicatesToUse.add(predicateEnd);
		// Gesuchsperiode
		if (gesuchsperiode != null) {
			Predicate predicateGesuchsperiode = builder.equal(root.get(VerfuegungZeitabschnitt_.verfuegung)
				.get(Verfuegung_.betreuung)
				.get(Betreuung_.kind)
				.get(KindContainer_.gesuch)
				.get(Gesuch_.gesuchsperiode), gesuchsperiode);
			predicatesToUse.add(predicateGesuchsperiode);
		}
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = builder.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);
		predicatesToUse.add(predicateGueltig);

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, joinGemeinde, predicatesToUse);

		// Sichtbarkeit nach eingeloggtem Benutzer
		boolean isInstitutionsbenutzer =
			principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles());
		if (isInstitutionsbenutzer) {
			Collection<Institution> allowedInstitutionen =
				institutionService.getInstitutionenReadableForCurrentBenutzer(false);
			Predicate predicateAllowedInstitutionen = root.get(VerfuegungZeitabschnitt_.verfuegung)
				.get(Verfuegung_.betreuung)
				.get(Betreuung_.institutionStammdaten)
				.get(InstitutionStammdaten_.institution)
				.in(allowedInstitutionen);
			predicatesToUse.add(predicateAllowedInstitutionen);
		}
		Predicate predicateForBenutzerRole = getPredicateForBenutzerRole(builder, root);
		if (predicateForBenutzerRole != null) {
			predicatesToUse.add(predicateForBenutzerRole);
		}
		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicatesToUse));
		return persistence.getCriteriaResults(query);
	}

	@Nullable
	private Predicate getPredicateForBenutzerRole(
		@Nonnull CriteriaBuilder builder,
		@Nonnull Root<VerfuegungZeitabschnitt> root) {
		boolean isTSBenutzer = principalBean.isCallerInAnyOfRole(UserRole.SACHBEARBEITER_TS, UserRole.ADMIN_TS);
		boolean isBGBenutzer = principalBean.isCallerInAnyOfRole(UserRole.SACHBEARBEITER_BG, UserRole.ADMIN_BG);

		if (isTSBenutzer) {
			Predicate predicateSchulamt = builder.equal(root.get(VerfuegungZeitabschnitt_.verfuegung)
				.get(Verfuegung_.betreuung)
				.get(Betreuung_.institutionStammdaten)
				.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.TAGESSCHULE);
			return predicateSchulamt;
		}
		if (isBGBenutzer) {
			Predicate predicateNotSchulamt = builder.notEqual(root.get(VerfuegungZeitabschnitt_.verfuegung)
				.get(Verfuegung_.betreuung)
				.get(Betreuung_.institutionStammdaten)
				.get(InstitutionStammdaten_.betreuungsangebotTyp), BetreuungsangebotTyp.TAGESSCHULE);
			return predicateNotSchulamt;
		}
		return null;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Nonnull
	private List<VerfuegungZeitabschnitt> getReportDataBetreuungen(@Nonnull LocalDate stichtag) {
		validateStichtagParam(stichtag);

		Benutzer user = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getReportDataBetreuungen", NO_USER_IS_LOGGED_IN));

		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt
		// verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(Betreuung_.kind, JoinType.LEFT);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch, JoinType.LEFT);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier, JoinType.LEFT);
		Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(Dossier_.gemeinde, JoinType.LEFT);

		List<Predicate> predicatesToUse = new ArrayList<>();

		// Stichtag
		Predicate intervalPredicate = builder.between(
			builder.literal(stichtag),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis));
		predicatesToUse.add(intervalPredicate);
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = builder.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, joinGemeinde, predicatesToUse);

		predicatesToUse.add(predicateGueltig);

		// Sichtbarkeit nach eingeloggtem Benutzer
		boolean isInstitutionsbenutzer =
			principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles());
		if (isInstitutionsbenutzer) {
			Collection<Institution> allowedInstitutionen =
				institutionService.getInstitutionenReadableForCurrentBenutzer(false);
			Predicate predicateAllowedInstitutionen = root.get(VerfuegungZeitabschnitt_.verfuegung)
				.get(Verfuegung_.betreuung)
				.get(Betreuung_.institutionStammdaten)
				.get(InstitutionStammdaten_.institution)
				.in(allowedInstitutionen);
			predicatesToUse.add(predicateAllowedInstitutionen);
		}
		Predicate predicateForBenutzerRole = getPredicateForBenutzerRole(builder, root);
		if (predicateForBenutzerRole != null) {
			predicatesToUse.add(predicateForBenutzerRole);
		}
		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicatesToUse));
		return persistence.getCriteriaResults(query);
	}

	private void addStammdaten(
		GesuchstellerKinderBetreuungDataRow row,
		VerfuegungZeitabschnitt zeitabschnitt,
		Gesuch gesuch,
		@Nonnull Locale locale
	) {

		final Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
		Objects.requireNonNull(betreuung);
		row.setInstitution(betreuung
			.getInstitutionStammdaten()
			.getInstitution()
			.getName());

		row.setBetreuungsTyp(betreuung.getBetreuungsangebotTyp());
		row.setPeriode(gesuch.getGesuchsperiode().getGesuchsperiodeString());
		String messageKey = AntragStatus.class.getSimpleName() + '_' + gesuch.getStatus().name();
		row.setGesuchStatus(ServerMessageUtil.getMessage(messageKey, locale));
		row.setEingangsdatum(gesuch.getEingangsdatum());
		for (AntragStatusHistory antragStatusHistory : gesuch.getAntragStatusHistories()) {
			if (AntragStatus.getAllVerfuegtStates().contains(antragStatusHistory.getStatus())) {
				row.setVerfuegungsdatum(antragStatusHistory.getTimestampVon().toLocalDate());
			}
		}
		row.setFallId(Integer.parseInt(String.valueOf(gesuch.getFall().getFallNummer())));
		row.setGemeinde(gesuch.getDossier().getGemeinde().getName());
		row.setBgNummer(betreuung.getBGNummer());
	}

	private void addGesuchsteller1ToGesuchstellerKinderBetreuungDataRow(
		@Nonnull GesuchstellerKinderBetreuungDataRow row,
		@Nullable GesuchstellerContainer containerGS1,
		@Nonnull Integer freiwilligenArbeitMax
	) {
		if (containerGS1 == null) {
			return;
		}

		Gesuchsteller gs1 = containerGS1.getGesuchstellerJA();
		row.setGs1Name(gs1.getNachname());
		row.setGs1Vorname(gs1.getVorname());
		GesuchstellerAdresse gs1Adresse = containerGS1.getWohnadresseAm(row.getZeitabschnittVon());

		if (gs1Adresse != null) {
			row.setGs1Strasse(gs1Adresse.getStrasse());
			row.setGs1Hausnummer(gs1Adresse.getHausnummer());
			row.setGs1Zusatzzeile(gs1Adresse.getZusatzzeile());
			row.setGs1Plz(gs1Adresse.getPlz());
			row.setGs1Ort(gs1Adresse.getOrt());
		}
		row.setGs1Diplomatenstatus(gs1.isDiplomatenstatus());
		// EWP Gesuchsteller 1

		List<Erwerbspensum> erwerbspensenGS1 = containerGS1.getErwerbspensenAm(row.getZeitabschnittVon());
		for (Erwerbspensum erwerbspensumJA : erwerbspensenGS1) {
			if (Taetigkeit.ANGESTELLT == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpAngestellt(row.getGs1EwpAngestellt() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.AUSBILDUNG == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpAusbildung(row.getGs1EwpAusbildung() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.SELBSTAENDIG == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpSelbstaendig(row.getGs1EwpSelbstaendig() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.RAV == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpRav(row.getGs1EwpRav() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpGesundhtl(row.getGs1EwpGesundhtl() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.INTEGRATION_BESCHAEFTIGUNSPROGRAMM == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpIntegration(row.getGs1EwpIntegration() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.FREIWILLIGENARBEIT == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpFreiwillig(Math.min(
					row.getGs1EwpFreiwillig() + erwerbspensumJA.getPensum(),
					freiwilligenArbeitMax
				));
			}
		}
	}

	private void addGesuchsteller2ToGesuchstellerKinderBetreuungDataRow(
		@Nonnull GesuchstellerKinderBetreuungDataRow row,
		@Nonnull GesuchstellerContainer containerGS2,
		@Nonnull Integer freiwilligenArbeitMax
	) {

		Gesuchsteller gs2 = containerGS2.getGesuchstellerJA();
		row.setGs2Name(gs2.getNachname());
		row.setGs2Vorname(gs2.getVorname());
		GesuchstellerAdresse gs2Adresse = containerGS2.getWohnadresseAm(row.getZeitabschnittVon());

		if (gs2Adresse != null) {
			row.setGs2Strasse(gs2Adresse.getStrasse());
			row.setGs2Hausnummer(gs2Adresse.getHausnummer());
			row.setGs2Zusatzzeile(gs2Adresse.getZusatzzeile());
			row.setGs2Plz(gs2Adresse.getPlz());
			row.setGs2Ort(gs2Adresse.getOrt());
		}
		row.setGs2Diplomatenstatus(gs2.isDiplomatenstatus());
		// EWP Gesuchsteller 2
		List<Erwerbspensum> erwerbspensenGS2 = containerGS2.getErwerbspensenAm(row.getZeitabschnittVon());
		for (Erwerbspensum erwerbspensumJA : erwerbspensenGS2) {
			if (Taetigkeit.ANGESTELLT == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpAngestellt(row.getGs2EwpAngestellt() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.AUSBILDUNG == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpAusbildung(row.getGs2EwpAusbildung() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.SELBSTAENDIG == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpSelbstaendig(row.getGs2EwpSelbstaendig() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.RAV == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpRav(row.getGs2EwpRav() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpGesundhtl(row.getGs2EwpGesundhtl() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.INTEGRATION_BESCHAEFTIGUNSPROGRAMM == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpIntegration(row.getGs2EwpIntegration() + erwerbspensumJA.getPensum());
			}
			if (Taetigkeit.FREIWILLIGENARBEIT == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpFreiwillig(Math.min(
					row.getGs2EwpFreiwillig() + erwerbspensumJA.getPensum(),
					freiwilligenArbeitMax
				));
			}
		}
	}

	private void addKindToGesuchstellerKinderBetreuungDataRow(
		GesuchstellerKinderBetreuungDataRow row,
		Betreuung betreuung) {

		Kind kind = betreuung.getKind().getKindJA();
		row.setKindName(kind.getNachname());
		row.setKindVorname(kind.getVorname());
		row.setKindGeburtsdatum(kind.getGeburtsdatum());
		if (row.getKindGeburtsdatum() == null || row.getKindGeburtsdatum().isBefore(MIN_DATE)) {
			row.setKindGeburtsdatum(MIN_DATE);
		}
		row.setKindFachstelle(kind.getPensumFachstelle() != null
			? String.valueOf(kind.getPensumFachstelle().getFachstelle().getName())
			: StringUtils.EMPTY);

		row.setKindErwBeduerfnisse(betreuung.hasErweiterteBetreuung());
		row.setKindSprichtAmtssprache(kind.getSprichtAmtssprache());
		row.setKindEinschulungTyp(kind.getEinschulungTyp());
	}

	private void addBetreuungToGesuchstellerKinderBetreuungDataRow(
		GesuchstellerKinderBetreuungDataRow row,
		VerfuegungZeitabschnitt zeitabschnitt,
		Betreuung betreuung,
		@Nonnull Locale locale
	) {

		row.setZeitabschnittVon(zeitabschnitt.getGueltigkeit().getGueltigAb());
		row.setZeitabschnittBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
		row.setBetreuungsStatus(ServerMessageUtil.getMessage(
			Betreuungsstatus.class.getSimpleName()
				+ '_'
				+ betreuung.getBetreuungsstatus().name(),
			locale
			)
		);
		row.setBetreuungspensum(MathUtil.DEFAULT.from(zeitabschnitt.getBetreuungspensumProzent()));

		// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
		BigDecimal anspruchsPensumKanton =
			new BigDecimal(zeitabschnitt.getBgCalculationResultAsiv().getAnspruchspensumProzent());
		BigDecimal anspruchsPensumGemeinde = BigDecimal.ZERO;
		BigDecimal anspruchsPensumTotal = anspruchsPensumKanton;
		if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung() && zeitabschnitt.getBgCalculationResultGemeinde() != null) {
			// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
			BigDecimal anspruchsPensumTotalGemeinde =
				new BigDecimal(zeitabschnitt.getBgCalculationResultGemeinde().getAnspruchspensumProzent());
			anspruchsPensumGemeinde = MathUtil.DEFAULT.subtractNullSafe(anspruchsPensumTotalGemeinde, anspruchsPensumKanton);
			anspruchsPensumTotal = anspruchsPensumTotalGemeinde;
		}
		row.setAnspruchsPensumKanton(anspruchsPensumKanton);
		row.setAnspruchsPensumGemeinde(anspruchsPensumGemeinde);
		row.setAnspruchsPensumTotal(anspruchsPensumTotal);

		// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
		BigDecimal bgPensumKanton = zeitabschnitt.getBgCalculationResultAsiv().getBgPensumProzent();
		BigDecimal bgPensumGemeinde = BigDecimal.ZERO;
		BigDecimal bgPensumTotal = bgPensumKanton;
		if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung() && zeitabschnitt.getBgCalculationResultGemeinde() != null) {
			// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
			BigDecimal bgPensumTotalGemeinde = zeitabschnitt.getBgCalculationResultGemeinde().getBgPensumProzent();
			bgPensumGemeinde = MathUtil.DEFAULT.subtractNullSafe(bgPensumTotalGemeinde, bgPensumKanton);
			bgPensumTotal = bgPensumTotalGemeinde;
		}
		row.setBgPensumKanton(bgPensumKanton);
		row.setBgPensumGemeinde(bgPensumGemeinde);
		row.setBgPensumTotal(bgPensumTotal);
		row.setBgStunden(zeitabschnitt.getBetreuungspensumZeiteinheit());
		// Wir koennen nicht die gespeicherte Zeiteinheit nehmen, da diese entweder Prozent oder Tage/Stunden ist
		// Daher fix TAGE fuer Kita und STUNDEN fuer TFO
		PensumUnits zeiteinheit = betreuung.getBetreuungsangebotTyp() == BetreuungsangebotTyp.KITA ? PensumUnits.DAYS : PensumUnits.HOURS;
		row.setBgPensumZeiteinheit(ServerMessageUtil.translateEnumValue(zeiteinheit, locale));

		row.setVollkosten(zeitabschnitt.getVollkosten());
		row.setElternbeitrag(zeitabschnitt.getElternbeitrag());
		// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
		BigDecimal verguenstigungKanton = zeitabschnitt.getBgCalculationResultAsiv().getVerguenstigung();
		BigDecimal verguenstigungGemeinde = BigDecimal.ZERO;
		BigDecimal verguenstigungTotal = verguenstigungKanton;
		if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung() && zeitabschnitt.getBgCalculationResultGemeinde() != null) {
			// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
			BigDecimal verguenstigungTotalGemeinde = zeitabschnitt.getBgCalculationResultGemeinde().getVerguenstigung();
			verguenstigungGemeinde = MathUtil.DEFAULT.subtractNullSafe(verguenstigungTotalGemeinde, verguenstigungKanton);
			verguenstigungTotal = verguenstigungTotalGemeinde;
		}
		row.setVerguenstigungKanton(verguenstigungKanton);
		row.setVerguenstigungGemeinde(verguenstigungGemeinde);
		row.setVerguenstigungTotal(verguenstigungTotal);
	}

	@SuppressWarnings("Duplicates")
	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportGesuchstellerKinderBetreuung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable String gesuchPeriodeId,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		validateDateParams(datumVon, datumBis);

		final ReportVorlage reportResource = ReportVorlage.VORLAGE_REPORT_GESUCHSTELLER_KINDER_BETREUUNG;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		requireNonNull(is, VORLAGE + reportResource.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		Gesuchsperiode gesuchsperiode = null;
		if (gesuchPeriodeId != null) {
			Optional<Gesuchsperiode> gesuchsperiodeOptional =
				gesuchsperiodeService.findGesuchsperiode(gesuchPeriodeId);
			if (gesuchsperiodeOptional.isPresent()) {
				gesuchsperiode = gesuchsperiodeOptional.get();
			}
		}

		List<GesuchstellerKinderBetreuungDataRow> reportData =
			getReportDataGesuchstellerKinderBetreuung(datumVon, datumBis, gesuchsperiode, locale);

		final XSSFSheet xsslSheet =
			(XSSFSheet) gesuchstellerKinderBetreuungExcelConverter.mergeHeaderFieldsPeriode(
				reportData,
				sheet,
				datumVon,
				datumBis,
				gesuchsperiode,
				locale);

		final RowFiller rowFiller = fillAndMergeRows(reportResource, xsslSheet, reportData, locale);

		return saveExcelDokument(reportResource, rowFiller, locale);
	}

	private List<GesuchstellerKinderBetreuungDataRow> convertToGesuchstellerKinderBetreuungDataRow(
		List<VerfuegungZeitabschnitt> zeitabschnittList,
		@Nonnull Locale locale
	) {

		List<GesuchstellerKinderBetreuungDataRow> dataRowList = new ArrayList<>();

		Map<Long, Gesuch> neustesVerfuegtesGesuchCache = new HashMap<>();
		Map<String, Integer> maxFreiwilligenarbeitCache = new HashMap<>();

		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			GesuchstellerKinderBetreuungDataRow row =
				createRowForGesuchstellerKinderBetreuungReport(
					zeitabschnitt, neustesVerfuegtesGesuchCache, maxFreiwilligenarbeitCache, locale);
			dataRowList.add(row);
		}

		return dataRowList;
	}

	@Nonnull
	private String getMaxFreiwilligenarbeitCacheKey(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		return gemeinde.getId() + "_" + gesuchsperiode.getId();
	}

	@SuppressWarnings({ "Duplicates", "PMD.NcssMethodCount" })
	private GesuchstellerKinderBetreuungDataRow createRowForGesuchstellerKinderBetreuungReport(
		VerfuegungZeitabschnitt zeitabschnitt,
		Map<Long, Gesuch> neustesVerfuegtesGesuchCache,
		Map<String, Integer> maxFreiwilligenarbeitCache,
		@Nonnull Locale locale
	) {
		Betreuung gueltigeBetreuung = zeitabschnitt.getVerfuegung().getBetreuung();
		Objects.requireNonNull(gueltigeBetreuung);
		Gesuch gesuch = gueltigeBetreuung.extractGesuch();
		Gesuch gueltigeGesuch = null;

		final String maxFreiwilligenarbeitCacheKey = getMaxFreiwilligenarbeitCacheKey(gesuch.extractGemeinde(), gesuch.getGesuchsperiode());
		Integer maxFreiwilligenarbeit = null;
		if (maxFreiwilligenarbeitCache.containsKey(maxFreiwilligenarbeitCacheKey)) {
			maxFreiwilligenarbeit = maxFreiwilligenarbeitCache.get(maxFreiwilligenarbeitCacheKey);
		} else {
			Einstellung maxFreiwilligenarbeitEinstellung = einstellungService.findEinstellung(
				EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT,
				gesuch.getDossier().getGemeinde(),
				gesuch.getGesuchsperiode());
			maxFreiwilligenarbeit = maxFreiwilligenarbeitEinstellung.getValueAsInteger();
			maxFreiwilligenarbeitCache.put(maxFreiwilligenarbeitCacheKey, maxFreiwilligenarbeit);
		}

		//prüfen ob Gesuch ist gültig, und via GesuchService oder Cache holen, inkl. Kind & Betreuung
		if (!gesuch.isGueltig()) {

			gueltigeGesuch = getGueltigesGesuch(neustesVerfuegtesGesuchCache, gesuch);
			Optional<KindContainer> gueltigeKind = getGueltigesKind(zeitabschnitt, gueltigeGesuch);
			gueltigeBetreuung = getGueltigeBetreuung(zeitabschnitt, gueltigeBetreuung, gueltigeKind);

			neustesVerfuegtesGesuchCache.put(gesuch.getFall().getFallNummer(), gueltigeGesuch);
		} else {
			gueltigeGesuch = gesuch;
		}

		GesuchstellerKinderBetreuungDataRow row = new GesuchstellerKinderBetreuungDataRow();
		// Betreuung
		addBetreuungToGesuchstellerKinderBetreuungDataRow(row, zeitabschnitt, gueltigeBetreuung, locale);
		// Stammdaten
		addStammdaten(row, zeitabschnitt, gueltigeGesuch, locale);

		// Gesuchsteller 1: Prozent-Felder initialisieren, damit im Excel das Total sicher berechnet werden kann
		row.setGs1EwpAngestellt(0);
		row.setGs1EwpAusbildung(0);
		row.setGs1EwpSelbstaendig(0);
		row.setGs1EwpRav(0);
		row.setGs1EwpGesundhtl(0);
		row.setGs1EwpIntegration(0);
		row.setGs1EwpFreiwillig(0);
		GesuchstellerContainer gs1Container = gueltigeGesuch.getGesuchsteller1();
		if (gs1Container != null) {
			addGesuchsteller1ToGesuchstellerKinderBetreuungDataRow(row, gs1Container, maxFreiwilligenarbeit);
		}
		// Gesuchsteller 2: Prozent-Felder initialisieren, damit im Excel das Total sicher berechnet werden kann
		row.setGs2EwpAngestellt(0);
		row.setGs2EwpAusbildung(0);
		row.setGs2EwpSelbstaendig(0);
		row.setGs2EwpRav(0);
		row.setGs2EwpGesundhtl(0);
		row.setGs2EwpIntegration(0);
		row.setGs2EwpFreiwillig(0);
		if (gueltigeGesuch.getGesuchsteller2() != null) {
			addGesuchsteller2ToGesuchstellerKinderBetreuungDataRow(row, gueltigeGesuch.getGesuchsteller2(), maxFreiwilligenarbeit);
		}
		// Familiensituation / Einkommen
		FamiliensituationContainer familiensituationContainer = gueltigeGesuch.getFamiliensituationContainer();
		if (familiensituationContainer != null) {
			Familiensituation familiensituation =
				familiensituationContainer.getFamiliensituationAm(row.getZeitabschnittVon());
			row.setFamiliensituation(familiensituation.getFamilienstatus());
			row.setSozialhilfeBezueger(isSozialhilfeBezueger(zeitabschnitt, familiensituationContainer, familiensituation));
		}
		row.setFamiliengroesse(zeitabschnitt.getFamGroesse());
		row.setMassgEinkVorFamilienabzug(zeitabschnitt.getMassgebendesEinkommenVorAbzFamgr());
		row.setFamilienabzug(zeitabschnitt.getAbzugFamGroesse());
		row.setMassgEink(zeitabschnitt.getMassgebendesEinkommen());
		row.setEinkommensjahr(zeitabschnitt.getEinkommensjahr());
		if (gueltigeGesuch.getEinkommensverschlechterungInfoContainer() != null) {
			row.setEkvVorhanden(gueltigeGesuch.getEinkommensverschlechterungInfoContainer()
				.getEinkommensverschlechterungInfoJA()
				.getEinkommensverschlechterung());
		}
		row.setStvGeprueft(gesuch.isGeprueftSTV());
		if (gueltigeGesuch.getGesuchsteller1() != null &&
			gueltigeGesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null) {
			row.setVeranlagt(gueltigeGesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerveranlagungErhalten());
		} else {
			row.setVeranlagt(Boolean.FALSE);
		}

		// Kind
		addKindToGesuchstellerKinderBetreuungDataRow(row, gueltigeBetreuung);
		return row;
	}

	private boolean isSozialhilfeBezueger(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull FamiliensituationContainer familiensituationContainer,
		@Nonnull Familiensituation familiensituation
	) {
		if (familiensituation.getSozialhilfeBezueger() == null || !familiensituation.getSozialhilfeBezueger()) {
			return false;
		}

		// falls keine sozialhilfeContainer existieren, Sozialhilfe von Familiensituation nehmen
		Set<SozialhilfeZeitraumContainer> sozialhilfeZeitraumContainers = familiensituationContainer.getSozialhilfeZeitraumContainers();
		if (sozialhilfeZeitraumContainers.isEmpty()) {
			return familiensituation.getSozialhilfeBezueger();
		}

		// falls sozialhilfeContainer existieren, überprüfen ob diese für den aktuellen Zeitabschnitt gelten
		return sozialhilfeZeitraumContainers.stream().anyMatch(sozialhilfeZeitraumContainer -> {
			SozialhilfeZeitraum sozialhilfeZeitraumJA = sozialhilfeZeitraumContainer.getSozialhilfeZeitraumJA();
			return sozialhilfeZeitraumJA != null &&
				zeitabschnitt.getGueltigkeit().getGueltigAb().compareTo(sozialhilfeZeitraumJA.getGueltigkeit().getGueltigAb()) >= 0 &&
				zeitabschnitt.getGueltigkeit().getGueltigBis().compareTo(sozialhilfeZeitraumJA.getGueltigkeit().getGueltigBis()) <= 0;
		});
	}

	@SuppressWarnings("Duplicates")
	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportKinder(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable String gesuchPeriodeId,
		@Nonnull Locale locale
	) throws ExcelMergeException {

		validateDateParams(datumVon, datumBis);

		final ReportVorlage reportResource = ReportVorlage.VORLAGE_REPORT_KINDER;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		requireNonNull(is, VORLAGE + reportResource.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		Gesuchsperiode gesuchsperiode = null;
		if (gesuchPeriodeId != null) {
			Optional<Gesuchsperiode> gesuchsperiodeOptional =
				gesuchsperiodeService.findGesuchsperiode(gesuchPeriodeId);
			if (gesuchsperiodeOptional.isPresent()) {
				gesuchsperiode = gesuchsperiodeOptional.get();
			}
		}

		List<GesuchstellerKinderBetreuungDataRow> reportData =
			getReportDataKinder(datumVon, datumBis, gesuchsperiode, locale);

		final XSSFSheet xsslSheet =
			(XSSFSheet) gesuchstellerKinderBetreuungExcelConverter.mergeHeaderFieldsPeriode(
				reportData,
				sheet,
				datumVon,
				datumBis,
				gesuchsperiode,
				locale);

		final RowFiller rowFiller = fillAndMergeRows(reportResource, xsslSheet, reportData, locale);

		return saveExcelDokument(reportResource, rowFiller, locale);
	}

	private List<GesuchstellerKinderBetreuungDataRow> convertToKinderDataRow(
		List<VerfuegungZeitabschnitt> zeitabschnittList,
		@Nonnull Locale locale
	) {

		List<GesuchstellerKinderBetreuungDataRow> dataRowList = new ArrayList<>();

		Map<Long, Gesuch> neustesVerfuegtesGesuchCache = new HashMap<>();

		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			GesuchstellerKinderBetreuungDataRow row =
				createRowForKinderReport(zeitabschnitt, neustesVerfuegtesGesuchCache, locale);
			dataRowList.add(row);
		}

		return dataRowList;
	}

	@SuppressWarnings("Duplicates")
	private GesuchstellerKinderBetreuungDataRow createRowForKinderReport(
		VerfuegungZeitabschnitt zeitabschnitt,
		Map<Long, Gesuch> neustesVerfuegtesGesuchCache,
		@Nonnull Locale locale
	) {
		Betreuung gueltigeBetreuung = zeitabschnitt.getVerfuegung().getBetreuung();
		Objects.requireNonNull(gueltigeBetreuung);
		Gesuch gesuch = gueltigeBetreuung.extractGesuch();
		Gesuch gueltigeGesuch = null;


		//prüfen ob Gesuch ist gültig, und via GesuchService oder Cache holen, inkl. Kind & Betreuung
		if (!gesuch.isGueltig()) {

			gueltigeGesuch = getGueltigesGesuch(neustesVerfuegtesGesuchCache, gesuch);

			Optional<KindContainer> gueltigeKind = getGueltigesKind(zeitabschnitt, gueltigeGesuch);

			gueltigeBetreuung = getGueltigeBetreuung(zeitabschnitt, gueltigeBetreuung, gueltigeKind);

			neustesVerfuegtesGesuchCache.put(gesuch.getFall().getFallNummer(), gueltigeGesuch);
		} else {
			gueltigeGesuch = gesuch;
		}

		GesuchstellerKinderBetreuungDataRow row = new GesuchstellerKinderBetreuungDataRow();
		addStammdaten(row, zeitabschnitt, gueltigeGesuch, locale);

		// Gesuchsteller 1
		GesuchstellerContainer gs1Container = gueltigeGesuch.getGesuchsteller1();
		if (gs1Container != null) {
			Gesuchsteller gs1 = gs1Container.getGesuchstellerJA();
			row.setGs1Name(gs1.getNachname());
			row.setGs1Vorname(gs1.getVorname());
		}
		// Gesuchsteller 2
		if (gueltigeGesuch.getGesuchsteller2() != null) {
			Gesuchsteller gs2 = gueltigeGesuch.getGesuchsteller2().getGesuchstellerJA();
			row.setGs2Name(gs2.getNachname());
			row.setGs2Vorname(gs2.getVorname());
		}

		// Kind
		addKindToGesuchstellerKinderBetreuungDataRow(row, gueltigeBetreuung);

		// Betreuung
		addBetreuungToGesuchstellerKinderBetreuungDataRow(row, zeitabschnitt, gueltigeBetreuung, locale);

		return row;
	}

	private Gesuch getGueltigesGesuch(Map<Long, Gesuch> neustesVerfuegtesGesuchCache, Gesuch gesuch) {
		Gesuch gueltigeGesuch;
		gueltigeGesuch = neustesVerfuegtesGesuchCache.getOrDefault(
			gesuch.getFall().getFallNummer(),
			gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch.getGesuchsperiode(), gesuch.getDossier(), false)
				.orElse(gesuch));
		return gueltigeGesuch;
	}

	private Betreuung getGueltigeBetreuung(
		VerfuegungZeitabschnitt zeitabschnitt,
		Betreuung gueltigeBetreuung,
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<KindContainer> gueltigeKind) {

		return gueltigeKind
			.map(gk -> {
				final Betreuung betr = zeitabschnitt.getVerfuegung().getBetreuung();
				Objects.requireNonNull(betr);
				return gk.getBetreuungen().stream().filter(betreuung -> betreuung
					.getBetreuungNummer()
					.equals(betr.getBetreuungNummer()))
					.findFirst()
					.orElse(betr);
			})
			.orElse(gueltigeBetreuung);
	}

	private Optional<KindContainer> getGueltigesKind(VerfuegungZeitabschnitt zeitabschnitt, Gesuch gueltigeGesuch) {
		final Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
		Objects.requireNonNull(betreuung);
		Integer kindNummer = betreuung.getKind().getKindNummer();

		return gueltigeGesuch.getKindContainers().stream()
			.filter(kindContainer -> kindContainer.getKindNummer().equals(kindNummer))
			.findFirst();
	}

	@Nonnull
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportGesuchsteller(@Nonnull LocalDate stichtag, @Nonnull Locale locale)
		throws ExcelMergeException {
		validateStichtagParam(stichtag);

		final ReportVorlage reportResource = ReportVorlage.VORLAGE_REPORT_GESUCHSTELLER;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportResource.getTemplatePath());
		requireNonNull(is, VORLAGE + reportResource.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

		List<GesuchstellerKinderBetreuungDataRow> reportData = getReportDataGesuchsteller(stichtag, locale);

		final XSSFSheet xsslSheet =
			(XSSFSheet) gesuchstellerKinderBetreuungExcelConverter.mergeHeaderFieldsStichtag(
				reportData,
				sheet,
				stichtag,
				locale);

		final RowFiller rowFiller = fillAndMergeRows(reportResource, xsslSheet, reportData, locale);
		return saveExcelDokument(reportResource, rowFiller, locale);
	}

	/**
	 * fuegt die Daten der Excelsheet hinzu und gibt den Rowfiller zurueck
	 */
	@Nonnull
	private RowFiller fillAndMergeRows(
		ReportVorlage reportResource,
		XSSFSheet sheet,
		List<GesuchstellerKinderBetreuungDataRow> reportData,
		@Nonnull Locale locale
	) {

		RowFiller rowFiller = RowFiller.initRowFiller(
			sheet,
			MergeFieldProvider.toMergeFields(reportResource.getMergeFields()),
			reportData.size());

		gesuchstellerKinderBetreuungExcelConverter.mergeRows(
			rowFiller,
			reportData,
			locale
		);
		gesuchstellerKinderBetreuungExcelConverter.applyAutoSize(sheet);

		return rowFiller;
	}

	/**
	 * Erstellt das Dokument und speichert es im Filesystem
	 */
	@Nonnull
	private UploadFileInfo saveExcelDokument(
		ReportVorlage reportVorlage,
		RowFiller rowFiller,
		@Nonnull Locale locale
	) {
		byte[] bytes = createWorkbook(rowFiller.getSheet().getWorkbook());

		rowFiller.getSheet().getWorkbook().dispose();

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	private void runStatisticsBetreuung() {
		List<Betreuung> allBetreuungen = betreuungService.getAllBetreuungenWithMissingStatistics();
		for (Betreuung betreuung : allBetreuungen) {
			if (betreuung.hasVorgaenger()) {
				Betreuung vorgaengerBetreuung = persistence.find(Betreuung.class, betreuung.getVorgaengerId());
				if (!betreuung.isSame(vorgaengerBetreuung, false, false)) {
					betreuung.setBetreuungMutiert(Boolean.TRUE);
					LOGGER.info("Betreuung hat geändert: {}", betreuung.getId());
				} else {
					betreuung.setBetreuungMutiert(Boolean.FALSE);
					LOGGER.info("Betreuung hat nicht geändert: {}", betreuung.getId());
				}
			} else {
				// Betreuung war auf dieser Mutation neu
				LOGGER.info("Betreuung ist neu: {}", betreuung.getId());
				betreuung.setBetreuungMutiert(Boolean.TRUE);
			}
		}
	}

	private void runStatisticsAbwesenheiten() {
		List<Abwesenheit> allAbwesenheiten = betreuungService.getAllAbwesenheitenWithMissingStatistics();
		for (Abwesenheit abwesenheit : allAbwesenheiten) {
			Betreuung betreuung = abwesenheit.getAbwesenheitContainer().getBetreuung();
			if (abwesenheit.hasVorgaenger()) {
				Abwesenheit vorgaengerAbwesenheit = persistence.find(Abwesenheit.class, abwesenheit.getVorgaengerId());
				if (!abwesenheit.isSame(vorgaengerAbwesenheit)) {
					betreuung.setAbwesenheitMutiert(Boolean.TRUE);
					LOGGER.info("Abwesenheit hat geändert: {}", abwesenheit.getId());
				} else {
					betreuung.setAbwesenheitMutiert(Boolean.FALSE);
					LOGGER.info("Abwesenheit hat nicht geändert: {}", abwesenheit.getId());
				}
			} else {
				// Abwesenheit war auf dieser Mutation neu
				LOGGER.info("Abwesenheit ist neu: {}", abwesenheit.getId());
				betreuung.setAbwesenheitMutiert(Boolean.TRUE);
			}
		}
	}

	private void runStatisticsKinder() {
		List<KindContainer> allKindContainer = kindService.getAllKinderWithMissingStatistics();
		for (KindContainer kindContainer : allKindContainer) {
			Kind kind = kindContainer.getKindJA();
			if (kind.hasVorgaenger()) {
				Kind vorgaengerKind = persistence.find(Kind.class, kind.getVorgaengerId());
				if (!kind.isSame(vorgaengerKind)) {
					kindContainer.setKindMutiert(Boolean.TRUE);
					LOGGER.info("Kind hat geändert: {}", kindContainer.getId());
				} else {
					kindContainer.setKindMutiert(Boolean.FALSE);
					LOGGER.info("Kind hat nicht geändert: {}", kindContainer.getId());
				}
			} else {
				// Kind war auf dieser Mutation neu
				LOGGER.info("Kind ist neu: {}", kindContainer.getId());
				kindContainer.setKindMutiert(Boolean.TRUE);
			}
		}
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportBenutzer(@Nonnull Locale locale) throws ExcelMergeException {
		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_BENUTZER;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<BenutzerDataRow> reportData = getReportDataBenutzer(locale);

		ExcelMergerDTO excelMergerDTO = benutzerExcelConverter.toExcelMergerDTO(reportData, locale);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		benutzerExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public List<BenutzerDataRow> getReportDataBenutzer(@Nonnull Locale locale) {
		Pair<Long, List<Benutzer>> searchResultPair = benutzerService.searchBenutzer(new BenutzerTableFilterDTO());
		List<Benutzer> benutzerList = searchResultPair.getRight();

		Map<String, EnumSet<BetreuungsangebotTyp>> betreuungsangebotMap = new HashMap<>();
		return convertToBenutzerDataRow(benutzerList, betreuungsangebotMap, locale);
	}

	@Nonnull
	private List<BenutzerDataRow> convertToBenutzerDataRow(
		@Nonnull List<Benutzer> benutzerList,
		Map<String, EnumSet<BetreuungsangebotTyp>> betreuungsangebotMap,
		@Nonnull Locale locale
	) {
		return benutzerList.stream()
			.map(benutzer -> benutzerToDataRow(benutzer, betreuungsangebotMap, locale))
			.collect(Collectors.toList());
	}

	@Nonnull
	private BenutzerDataRow benutzerToDataRow(
		@Nonnull Benutzer benutzer,
		Map<String, EnumSet<BetreuungsangebotTyp>> betreuungsangebotMap,
		@Nonnull Locale locale
	) {
		BenutzerDataRow row = new BenutzerDataRow();
		row.setUsername(benutzer.getUsername());

		row.setNachname(benutzer.getNachname());
		row.setVorname(benutzer.getVorname());
		row.setEmail(benutzer.getEmail());
		row.setRole(ServerMessageUtil.translateEnumValue(benutzer.getRole(), locale));
		LocalDate gueltigAb = benutzer.getCurrentBerechtigung().getGueltigkeit().getGueltigAb();
		if (gueltigAb.isAfter(Constants.START_OF_TIME)) {
			row.setRoleGueltigAb(gueltigAb);
		}
		LocalDate gueltigBis = benutzer.getCurrentBerechtigung().getGueltigkeit().getGueltigBis();
		if (gueltigBis.isBefore(Constants.END_OF_TIME)) {
			row.setRoleGueltigBis(gueltigBis);
		}
		String institution = benutzer.getInstitution() != null ? benutzer.getInstitution().getName() : null;
		String traegerschaft = getTraegerschaftForBenutzer(benutzer);
		row.setGemeinden(benutzer.getCurrentBerechtigung().extractGemeindenForBerechtigungAsString());
		row.setInstitution(institution);
		row.setTraegerschaft(traegerschaft);
		row.setStatus(benutzer.getStatus());
		setBetreuungsangebote(row, benutzer, betreuungsangebotMap);

		return row;
	}

	/**
	 * The Traegerschaft comes directly from the user when it has one. If it has an Institution the tragerschaft will
	 * be the one
	 * the institution belongs to.
	 * Nuull is returned when the user has no traegerschaft and no institution or this one has no traegerschaft.
	 * The role isn't taken into account!
	 */
	@Nullable
	private String getTraegerschaftForBenutzer(@Nonnull Benutzer benutzer) {
		if (benutzer.getTraegerschaft() != null) {
			return benutzer.getTraegerschaft().getName();
		}
		if (benutzer.getInstitution() != null && benutzer.getInstitution().getTraegerschaft() != null) {
			return benutzer.getInstitution().getTraegerschaft().getName();
		}
		return null;
	}

	public void setBetreuungsangebote(
		@Nonnull BenutzerDataRow row,
		@Nonnull Benutzer benutzer,
		Map<String, EnumSet<BetreuungsangebotTyp>> betreuungsangebotMap
	) {
		// we go through all Traegerschaft/Inst/InstStammdaten and check which kind of Angebot they offer.
		// We don't get this information directly from the sql-query because it would be quite difficult and the
		// result very long
		// since it is a report and the users allow them to take long to execute, this shouldn't be any problem.

		// to improve performance we have a Map where we save already calculated results. We use the ID so we can have
		// a Map for both traegerschaft and Institutionen
		// traegerschaft has a higher priority than institution
		if (benutzer.getTraegerschaft() != null) {
			if (!betreuungsangebotMap.containsKey(benutzer.getTraegerschaft().getId())) {
				EnumSet<BetreuungsangebotTyp> allAngeboteTraegerschaft = traegerschaftService
					.getAllAngeboteFromTraegerschaft(benutzer.getTraegerschaft().getId());
				betreuungsangebotMap.put(benutzer.getTraegerschaft().getId(), allAngeboteTraegerschaft);
			}
			setBetreuungsangebotValues(row, betreuungsangebotMap.get(benutzer.getTraegerschaft().getId()));

		} else if (benutzer.getInstitution() != null) {
			if (!betreuungsangebotMap.containsKey(benutzer.getInstitution().getId())) {
				BetreuungsangebotTyp angebotInstitution = institutionService
					.getAngebotFromInstitution(benutzer.getInstitution().getId());
				betreuungsangebotMap.put(benutzer.getInstitution().getId(), EnumSet.of(angebotInstitution));
			}
			setBetreuungsangebotValues(row, betreuungsangebotMap.get(benutzer.getInstitution().getId()));
		}
	}

	public void setBetreuungsangebotValues(
		@Nonnull BenutzerDataRow row,
		@Nonnull EnumSet<BetreuungsangebotTyp> angebote) {

		row.setKita(angebote.stream().anyMatch(BetreuungsangebotTyp::isKita));
		row.setTagesfamilien(angebote.stream().anyMatch(BetreuungsangebotTyp::isTagesfamilien));
		row.setTagesschule(angebote.stream().anyMatch(BetreuungsangebotTyp::isTagesschule));
		row.setFerieninsel(angebote.stream().anyMatch(BetreuungsangebotTyp::isFerieninsel));
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportInstitutionen(@Nonnull Locale locale) throws ExcelMergeException {
		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_INSTITUTIONEN;

		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<InstitutionenDataRow> reportData = getReportDataInstitutionen(locale);

		ExcelMergerDTO excelMergerDTO = institutionenExcelConverter.toExcelMergerDTO(reportData, locale);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		institutionenExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	private List<InstitutionenDataRow> getReportDataInstitutionen(@Nonnull Locale locale) {
		Benutzer currentBenutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getReportDataInstitutionen", NO_USER_IS_LOGGED_IN));

		Collection<InstitutionStammdaten> stammdaten = institutionStammdatenService.getAllInstitutionStammdaten()
			.stream()
			.filter(institution -> isCurrentBenutzerZustaendigForInstitution(currentBenutzer, institution))
			.collect(Collectors.toList());
		return convertToInstitutionenDataRow(stammdaten, locale);
	}

	private boolean isCurrentBenutzerZustaendigForInstitution(@Nonnull Benutzer currentBenutzer,
		@Nonnull InstitutionStammdaten institution) {
		if (currentBenutzer.getRole().isRoleTsOnly()) {
			return institution.getBetreuungsangebotTyp().isSchulamt();
		}
		if (currentBenutzer.getRole().isRoleBgOnly()) {
			return institution.getBetreuungsangebotTyp().isJugendamt();
		}
		return true;
	}

	@Nonnull
	private List<InstitutionenDataRow> convertToInstitutionenDataRow(
		@Nonnull Collection<InstitutionStammdaten> stammdaten,
		@Nonnull Locale locale
	) {
		return stammdaten.stream()
			.map(institution -> institutionToDataRow(institution, locale))
			.collect(Collectors.toList());
	}

	@Nonnull
	private InstitutionenDataRow institutionToDataRow(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull Locale locale
	) {
		Institution institution = institutionStammdaten.getInstitution();
		Adresse adresse = institutionStammdaten.getAdresse();
		List<LocalDateTime> zuletztGeandertList = new ArrayList<>();
		LocalDateTime zuletztGeandert;
		InstitutionenDataRow row = new InstitutionenDataRow();

		String angebotTyp =
			ServerMessageUtil.translateEnumValue(institutionStammdaten.getBetreuungsangebotTyp(), locale);
		row.setTyp(angebotTyp);
		if (institution.getTraegerschaft() != null) {
			row.setTraegerschaft(institution.getTraegerschaft().getName());
		}
		row.setName(institution.getName());
		if (adresse.getOrganisation() != null) {
			row.setAnschrift(adresse.getOrganisation());
		}
		if (institutionStammdaten.getTelefon() != null) {
			row.setTelefon(institutionStammdaten.getTelefon());
		}
		if (institutionStammdaten.getWebseite() != null) {
			row.setUrl(institutionStammdaten.getWebseite());
		}
		row.setStrasse(adresse.getStrasseAndHausnummer());
		row.setPlz(adresse.getPlz());
		row.setOrt(adresse.getOrt());
		row.setEmail(institutionStammdaten.getMail());
		InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBG =
			institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine();
		if (institutionStammdatenBG != null) {
			if (institutionStammdatenBG.getOffenVon() != null && institutionStammdatenBG.getOffenBis() != null) {
				row.setOeffnungszeiten(
					institutionStammdatenBG.getOffenVon().toString()
						+ " - "
						+ institutionStammdatenBG.getOffenBis().toString()
				);
			}
			row.setOeffnungstage(institutionStammdatenBG.getOeffnungsTage().stream()
				.sorted()
				.map(tag -> tag.getDisplayName(TextStyle.FULL, locale))
				.collect(Collectors.joining(", ")));
			row.setOeffnungsAbweichungen(institutionStammdatenBG.getOeffnungsAbweichungen());
		}
		row.setBaby(institutionStammdatenBG != null && institutionStammdatenBG.getAlterskategorieBaby());
		row.setVorschulkind(institutionStammdatenBG != null && institutionStammdatenBG.getAlterskategorieVorschule());
		row.setKindergarten(institutionStammdatenBG != null && institutionStammdatenBG.getAlterskategorieKindergarten());
		row.setSchulkind(institutionStammdatenBG != null && institutionStammdatenBG.getAlterskategorieSchule());
		if (!institutionStammdaten.getBetreuungsangebotTyp().isTagesfamilien()) {
			row.setSubventioniert(institutionStammdatenBG != null && institutionStammdatenBG.getSubventioniertePlaetze());
			if (institutionStammdatenBG != null && institutionStammdatenBG.getAnzahlPlaetze() != null) {
				row.setKapazitaet(institutionStammdatenBG.getAnzahlPlaetze());
			}
			if (institutionStammdatenBG != null && institutionStammdatenBG.getAnzahlPlaetzeFirmen() != null &&
				institutionStammdatenBG.getAnzahlPlaetzeFirmen().compareTo(BigDecimal.ZERO) != 0) {
				row.setReserviertFuerFirmen(institutionStammdatenBG.getAnzahlPlaetzeFirmen());
			}
		}
		zuletztGeandertList.add(institutionStammdaten.getTimestampMutiert());
		zuletztGeandertList.add(institution.getTimestampMutiert());
		zuletztGeandertList.add(adresse.getTimestampMutiert());

		zuletztGeandert = zuletztGeandertList.stream().max(LocalDateTime::compareTo).get();
		row.setZuletztGeaendert(zuletztGeandert);

		return row;
	}

	private int onlySchulamt() {
		String[] schulamtRoles = { SACHBEARBEITER_TS, ADMIN_TS };

		return principalBean.isCallerInAnyOfRole(schulamtRoles) ? 1 : 0;
	}
}
