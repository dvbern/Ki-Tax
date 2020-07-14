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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule_;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.ReportTagesschuleService;
import ch.dvbern.ebegu.reporting.tagesschule.TagesschuleAnmeldungenExcelConverter;
import ch.dvbern.ebegu.reporting.tagesschule.TagesschuleDataRow;
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

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("PMD.NcssTypeCount")
@Stateless
@Local(ReportTagesschuleService.class)
public class ReportTagesschuleServiceBean extends AbstractReportServiceBean implements ReportTagesschuleService {

	private static final String ANMELDUNGEN_TAGESSCHULE_SIZE_EXCEPTION = "Ein Kind kann nur eine Anmeldung für eine "
		+ "bestimmte Tagesschule haben";
	private static final String NO_STAMMDATEN_FOUND = "Keine Stammdaten gefunden";

	@Inject
	private TagesschuleAnmeldungenExcelConverter tagesschuleExcelConverter;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private Persistence persistence;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_TS, SACHBEARBEITER_TS, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_INSTITUTION })
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
				"generateExcelReportTagesschuleOhneFinSit",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeID));

		InstitutionStammdaten institutionStammdaten =
			institutionStammdatenService.findInstitutionStammdaten(stammdatenID).orElseThrow(() -> new EbeguRuntimeException(
				"findEinstellungenTagesschule", NO_STAMMDATEN_FOUND));

		EinstellungenTagesschule einstellungenTagesschule =
			findEinstellungenTagesschuleByPeriode(institutionStammdaten, gesuchsperiode.getId());
		requireNonNull(einstellungenTagesschule, "EinstellungenTagesschule" + VALIDIERUNG_DARF_NICHT_NULL_SEIN);

		List<TagesschuleDataRow> reportData = getReportDataTagesschuleAnmeldungen(stammdatenID, gesuchsperiodeID);

		ExcelMergerDTO excelMergerDTO = tagesschuleExcelConverter.toExcelMergerDTO(reportData, locale, gesuchsperiode,
			einstellungenTagesschule, institutionStammdaten.getInstitution().getName());

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		tagesschuleExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	@Nonnull
	@Override
	public List<TagesschuleDataRow> getReportDataTagesschuleAnmeldungen(
		@Nonnull String stammdatenID,
		@Nonnull String gesuchsperiodeID) {

		requireNonNull(stammdatenID, "Das Argument 'stammdatenID' darf nicht leer sein");

		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<KindContainer> query = builder.createQuery(KindContainer.class);

		Root<KindContainer> root = query.from(KindContainer.class);
		Join<KindContainer, AnmeldungTagesschule> joinAnmeldungTagesschule =
			root.join(KindContainer_.anmeldungenTagesschule);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get(KindContainer_.gesuch).get(Gesuch_.gesuchsperiode).get(Gesuchsperiode_.id),
			gesuchsperiodeID));
		predicates.add(builder.equal(
			joinAnmeldungTagesschule.get(AnmeldungTagesschule_.institutionStammdaten).get(InstitutionStammdaten_.id),
			stammdatenID));

		query.where(CriteriaQueryHelper.concatenateExpressions(builder, predicates));
		List<KindContainer> kindContainerList = persistence.getCriteriaResults(query);
		requireNonNull(kindContainerList);

		return convertToTagesschuleDataRows(kindContainerList, stammdatenID);
	}

	@Nonnull
	private List<TagesschuleDataRow> convertToTagesschuleDataRows(@Nonnull List<KindContainer> kindContainerList, String stammdatenID) {
		ReportTagesschuleServiceBean self = this;
		return kindContainerList.stream()
			.map(kindContainer -> self.kindContainerToTagesschuleDataRow(kindContainer, stammdatenID))
			.collect(Collectors.toList());
	}

	@Nonnull
	private TagesschuleDataRow kindContainerToTagesschuleDataRow(@Nonnull KindContainer kindContainer, String stammdatenID) {

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

		TagesschuleDataRow tdr = new TagesschuleDataRow();
		tdr.setVornameKind(kindContainer.getKindJA().getVorname());
		tdr.setNachnameKind(kindContainer.getKindJA().getNachname());
		tdr.setGeburtsdatum(kindContainer.getKindJA().getGeburtsdatum());
		tdr.setStatus(anmeldungTagesschule.getBetreuungsstatus());
		tdr.setReferenznummer(anmeldungTagesschule.getBGNummer());
		tdr.setAnmeldungTagesschule(anmeldungTagesschule);

		BelegungTagesschule belegung = anmeldungTagesschule.getBelegungTagesschule();
		if (belegung != null) {
			tdr.setAb(anmeldungTagesschule.getBelegungTagesschule().getEintrittsdatum());
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
}
