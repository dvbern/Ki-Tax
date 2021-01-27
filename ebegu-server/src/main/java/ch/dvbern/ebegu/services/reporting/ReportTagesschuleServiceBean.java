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

package ch.dvbern.ebegu.services.reporting;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule_;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.reporting.ReportTagesschuleService;
import ch.dvbern.ebegu.reporting.tagesschule.TagesschuleAnmeldungenDataRow;
import ch.dvbern.ebegu.reporting.tagesschule.TagesschuleAnmeldungenExcelConverter;
import ch.dvbern.ebegu.reporting.tagesschule.TagesschuleRechnungsstellungDataRow;
import ch.dvbern.ebegu.reporting.tagesschule.TagesschuleRechnungsstellungExcelConverter;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("PMD.NcssTypeCount")
@Stateless
@Local(ReportTagesschuleService.class)
public class ReportTagesschuleServiceBean extends AbstractReportServiceBean implements ReportTagesschuleService {

	private static final String ANMELDUNGEN_TAGESSCHULE_SIZE_EXCEPTION = "Ein Kind kann nur eine Anmeldung für eine "
		+ "bestimmte Tagesschule haben";
	private static final String NO_STAMMDATEN_FOUND = "Keine Stammdaten gefunden";

	@Inject
	private TagesschuleAnmeldungenExcelConverter tagesschuleAnmeldungenExcelConverter;

	@Inject
	private TagesschuleRechnungsstellungExcelConverter tagesschuleRechnungsstellungExcelConverter;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private Persistence persistence;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportTagesschuleAnmeldungen(
		@Nonnull String stammdatenID,
		@Nonnull String gesuchsperiodeID,
		@Nonnull Locale locale) throws ExcelMergeException {

		requireNonNull(stammdatenID, "stammdatenID" + VALIDIERUNG_DARF_NICHT_NULL_SEIN);
		requireNonNull(gesuchsperiodeID, "gesuchsperiodeID" + VALIDIERUNG_DARF_NICHT_NULL_SEIN);

		ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_TAGESSCHULE_ANMELDUNGEN;
		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeID)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"generateExcelReportTagesschuleAnmeldungen",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeID));

		InstitutionStammdaten institutionStammdaten =
			institutionStammdatenService.findInstitutionStammdaten(stammdatenID).orElseThrow(() -> new EbeguRuntimeException(
				"generateExcelReportTagesschuleAnmeldungen", NO_STAMMDATEN_FOUND));

		EinstellungenTagesschule einstellungenTagesschule =
			findEinstellungenTagesschuleByPeriode(institutionStammdaten, gesuchsperiode.getId());
		requireNonNull(einstellungenTagesschule, "EinstellungenTagesschule" + VALIDIERUNG_DARF_NICHT_NULL_SEIN);

		List<TagesschuleAnmeldungenDataRow> reportData = getReportDataTagesschuleAnmeldungen(stammdatenID, gesuchsperiodeID);

		ExcelMergerDTO excelMergerDTO = tagesschuleAnmeldungenExcelConverter.toExcelMergerDTO(reportData, locale, gesuchsperiode,
			einstellungenTagesschule, institutionStammdaten.getInstitution().getName());

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		tagesschuleAnmeldungenExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Nonnull
	@Override
	public List<TagesschuleAnmeldungenDataRow> getReportDataTagesschuleAnmeldungen(
		@Nonnull String stammdatenID,
		@Nonnull String gesuchsperiodeID) {

		requireNonNull(stammdatenID, "Das Argument 'stammdatenID' darf nicht leer sein");

		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<KindContainer> query = builder.createQuery(KindContainer.class);

		Root<KindContainer> root = query.from(KindContainer.class);
		Join<KindContainer, AnmeldungTagesschule> joinAnmeldungTagesschule =
			root.join(KindContainer_.anmeldungenTagesschule);

		final Predicate predicateGesuch = builder.equal(root.get(KindContainer_.gesuch).get(Gesuch_.gesuchsperiode).get(Gesuchsperiode_.id),
			gesuchsperiodeID);
		final Predicate predicateStammdaten = builder.equal(
			joinAnmeldungTagesschule.get(AnmeldungTagesschule_.institutionStammdaten).get(InstitutionStammdaten_.id),
			stammdatenID);
		final Predicate predicateGueltig = builder.equal(joinAnmeldungTagesschule.get(AnmeldungTagesschule_.gueltig),
			Boolean.TRUE);

		query.where(predicateGesuch, predicateStammdaten, predicateGueltig);
		List<KindContainer> kindContainerList = persistence.getCriteriaResults(query);
		requireNonNull(kindContainerList);

		return convertToTagesschuleDataRows(kindContainerList, stammdatenID);
	}

	@Nonnull
	private List<TagesschuleAnmeldungenDataRow> convertToTagesschuleDataRows(@Nonnull List<KindContainer> kindContainerList, String stammdatenID) {
		ReportTagesschuleServiceBean self = this;
		return kindContainerList.stream()
			.map(kindContainer -> self.kindContainerToTagesschuleDataRow(kindContainer, stammdatenID))
			.collect(Collectors.toList());
	}

	@Nonnull
	private TagesschuleAnmeldungenDataRow kindContainerToTagesschuleDataRow(@Nonnull KindContainer kindContainer, String stammdatenID) {

		Iterator<AnmeldungTagesschule> anmeldungTagesschuleIterator =
			kindContainer.getAnmeldungenTagesschule()
				.stream()
				.filter(anmeldungTagesschule -> anmeldungTagesschule.getInstitutionStammdaten().getId().equals(stammdatenID))
				.iterator();
		AnmeldungTagesschule anmeldungTagesschule = anmeldungTagesschuleIterator.next();

		// es darf hier nur einge Anmeldung geben. Ist bereits nach Gesuchsperiode gefiltert.
		if (anmeldungTagesschule == null || anmeldungTagesschuleIterator.hasNext()) {
			throw new EbeguRuntimeException("kindContainerToTagesschuleDataRow",
				ANMELDUNGEN_TAGESSCHULE_SIZE_EXCEPTION);
		}

		TagesschuleAnmeldungenDataRow tdr = new TagesschuleAnmeldungenDataRow();
		tdr.setVornameKind(kindContainer.getKindJA().getVorname());
		tdr.setNachnameKind(kindContainer.getKindJA().getNachname());
		tdr.setGeburtsdatum(kindContainer.getKindJA().getGeburtsdatum());

		GesuchstellerContainer gesuchsteller1 = kindContainer.getGesuch().getGesuchsteller1();
		if (gesuchsteller1 != null && gesuchsteller1.getGesuchstellerJA() != null) {
			Gesuchsteller gesuchsteller1JA = gesuchsteller1.getGesuchstellerJA();
			tdr.setVornameAntragsteller1(gesuchsteller1JA.getVorname());
			tdr.setNachnameAntragsteller1(gesuchsteller1JA.getNachname());
			tdr.setEmailAntragsteller1(gesuchsteller1JA.getMail());
		}

		GesuchstellerContainer gesuchsteller2 = kindContainer.getGesuch().getGesuchsteller2();
		if (gesuchsteller2 != null && gesuchsteller2.getGesuchstellerJA() != null) {
			Gesuchsteller gesuchsteller2JA = gesuchsteller2.getGesuchstellerJA();
			tdr.setVornameAntragsteller2(gesuchsteller2JA.getVorname());
			tdr.setNachnameAntragsteller2(gesuchsteller2JA.getNachname());
			tdr.setEmailAntragsteller2(gesuchsteller2JA.getMail());
		}

		tdr.setStatus(anmeldungTagesschule.getBetreuungsstatus());
		tdr.setReferenznummer(anmeldungTagesschule.getBGNummer());
		tdr.setAnmeldungTagesschule(anmeldungTagesschule);

		BelegungTagesschule belegung = anmeldungTagesschule.getBelegungTagesschule();
		if (belegung != null) {
			tdr.setEintrittsdatum(anmeldungTagesschule.getBelegungTagesschule().getEintrittsdatum());
		}

		return tdr;
	}

	@Nonnull
	private EinstellungenTagesschule findEinstellungenTagesschuleByPeriode(@Nonnull InstitutionStammdaten stammdaten,
		@Nonnull String gesuchsperiodeId) {

		requireNonNull(stammdaten, "Das Argument 'stammdatenID' darf nicht leer sein");
		requireNonNull(gesuchsperiodeId, "Das Argument 'gesuchsperiodeId' darf nicht leer sein");

		if (stammdaten.getInstitutionStammdatenTagesschule() != null) {
			for (EinstellungenTagesschule e :
				stammdaten.getInstitutionStammdatenTagesschule().getEinstellungenTagesschule()) {
				if (e.getGesuchsperiode().getId().equals(gesuchsperiodeId)) {
					return e;
				}
			}
		}
		throw new EbeguEntityNotFoundException("findEinstellungenTagesschuleByPeriode",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
	}

	@Nonnull
	private String getFileName(ReportVorlage reportVorlage, @Nonnull Locale locale) {
		return ServerMessageUtil.translateEnumValue(reportVorlage.getDefaultExportFilename(), locale) + ".xlsx";
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportTagesschuleRechnungsstellung(@Nonnull Locale locale) throws ExcelMergeException {
		ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_TAGESSCHULE_RECHNUNGSSTELLUNG;
		InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		requireNonNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		LocalDate stichtag = LocalDate.now();
		final List<TagesschuleRechnungsstellungDataRow> reportData = getReportDataTagesschuleRechnungsstellung(stichtag);

		ExcelMergerDTO excelMergerDTO = tagesschuleRechnungsstellungExcelConverter.toExcelMergerDTO(reportData, stichtag, locale);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		tagesschuleRechnungsstellungExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Nonnull
	private List<TagesschuleRechnungsstellungDataRow> getReportDataTagesschuleRechnungsstellung(@Nonnull LocalDate stichtag) {

		// Wir suchen alle vergangenen Monate im Sinne von "in der aktuellen Gesuchsperiode vergangen"
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.getGesuchsperiodeAm(stichtag)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getReportDataTagesschuleRechnungsstellung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				stichtag));
		final Collection<InstitutionStammdaten> allowedTagesschulen = institutionStammdatenService.getTagesschulenForCurrentBenutzer();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = cb.createQuery(VerfuegungZeitabschnitt.class);
		final Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		final Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung, JoinType.LEFT);
		final Join<Verfuegung, AnmeldungTagesschule> joinAnmeldungTagesschule = joinVerfuegung.join(Verfuegung_.anmeldungTagesschule, JoinType.LEFT);

		ParameterExpression<LocalDate> datumVonParam = cb.parameter(LocalDate.class, "datumVon");
		ParameterExpression<LocalDate> datumBisParam = cb.parameter(LocalDate.class, "datumBis");
		ParameterExpression<LocalDate> stichtagParam = cb.parameter(LocalDate.class, "stichtag");
		ParameterExpression<Collection> allowedTagesschulenParam = cb.parameter(Collection.class, "allowedTagesschulen");

		// Eingeloggter Benutzer ist berechtigt fuer die Institution
		Predicate predicateBerechtigt = joinAnmeldungTagesschule.get(AnmeldungTagesschule_.institutionStammdaten).in(allowedTagesschulenParam);

		// Datum ab Zeitabschnitt muss groesse/gleich GP Start sein
		// Datum bis Zeitabschnitt muss kleiner/gleich GP Start sein
		final Predicate predicateAktuelleGesuchsperiode = cb.between(
			root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			datumVonParam,
			datumBisParam
		);
		// Datum ab Zeitabschnitt muss kleiner/gleich dem Stichtag sein
		final Predicate predicateNurVergangene = cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), stichtagParam);
		// Nur der letzte Abschnitt
		final Predicate predicateGueltig = cb.equal(joinAnmeldungTagesschule.get(AnmeldungTagesschule_.gueltig), Boolean.TRUE);

		query.where(predicateBerechtigt, predicateAktuelleGesuchsperiode, predicateNurVergangene, predicateGueltig);

		TypedQuery<VerfuegungZeitabschnitt> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(datumVonParam, gesuchsperiode.getGueltigkeit().getGueltigAb());
		typedQuery.setParameter(datumBisParam, gesuchsperiode.getGueltigkeit().getGueltigBis());
		typedQuery.setParameter(stichtagParam, stichtag);
		typedQuery.setParameter(allowedTagesschulenParam, allowedTagesschulen);
		final List<VerfuegungZeitabschnitt> zeitabschnitteList = typedQuery.getResultList();
		List<TagesschuleRechnungsstellungDataRow> dataRows = new ArrayList<>();
		zeitabschnitteList
			.stream()
			.map(verfuegungZeitabschnitt -> TagesschuleRechnungsstellungDataRow.createRows(verfuegungZeitabschnitt, stichtag))
			.forEach(dataRows::addAll);
		return dataRows.stream()
			.sorted(Comparator.naturalOrder())
			.collect(Collectors.toList());
	}
}
