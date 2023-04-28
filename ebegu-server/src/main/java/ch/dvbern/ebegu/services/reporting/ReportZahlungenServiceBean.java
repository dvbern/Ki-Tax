/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.reporting;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlung_;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsauftrag_;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.ZahlungspositionStatus;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.reporting.ReportZahlungenService;
import ch.dvbern.ebegu.reporting.zahlungen.ReportZahlungenExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungen.ZahlungenDataRow;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.FileSaverService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

@Stateless
@Local(ReportZahlungenService.class)
public class ReportZahlungenServiceBean extends AbstractReportServiceBean implements ReportZahlungenService {

	private ReportZahlungenExcelConverter zahlungenConverter = new ReportZahlungenExcelConverter();

	@Inject
	private Persistence persistence;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	public UploadFileInfo generateExcelReportZahlungen(
		@Nonnull ReportVorlage reportVorlage,
		@Nonnull Locale locale,
		@Nonnull String gesuchsperiodeId,
		@Nullable String gemeindeId,
		@Nullable String institutionId
	) throws ExcelMergeException, IOException {

		try (
			InputStream is = ReportServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
			Workbook workbook = createWorkbook(is, reportVorlage);
		) {
			XSSFSheet sheet = (XSSFSheet) workbook.getSheet(reportVorlage.getDataSheetName());

			var periode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
				.orElseThrow(() -> new EbeguEntityNotFoundException("generateExcelReportZahlungen", gesuchsperiodeId));

			Gemeinde gemeinde = null;
			if (gemeindeId != null) {
				gemeinde = gemeindeService.findGemeinde(gemeindeId)
					.orElseThrow(() -> new EbeguEntityNotFoundException("generateExcelReportZahlungen", gemeindeId));
			}
			Institution institution = null;
			if (institutionId != null) {
				institution = institutionService.findInstitution(institutionId, true)
					.orElseThrow(() -> new EbeguEntityNotFoundException("generateExcelReportZahlungen", institutionId));
			}

			sheet = zahlungenConverter.mergeHeaders(
				sheet,
				periode,
				gemeinde,
				institution
			);

			var zahlungsauftrage = findZahlungsauftrageWithAuszahlungsTypInstitution(periode, gemeinde, institution);
			var reportData = filterZahlungenAndConvertToDataRows(zahlungsauftrage, institutionId);
			final RowFiller rowFiller = fillAndMergeRows(reportVorlage, sheet, reportData);

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

	private RowFiller fillAndMergeRows(
		ReportVorlage reportResource,
		XSSFSheet sheet,
		List<ZahlungenDataRow> reportData
	) {
		RowFiller rowFiller = RowFiller.initRowFiller(
			sheet,
			MergeFieldProvider.toMergeFields(reportResource.getMergeFields()),
			reportData.size());

		zahlungenConverter.mergeRows(rowFiller, reportData);
		return rowFiller;
	}

	private List<Zahlungsauftrag> findZahlungsauftrageWithAuszahlungsTypInstitution(
		@Nonnull Gesuchsperiode periode,
		@Nullable Gemeinde gemeinde,
		@Nullable Institution institution
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsauftrag> query = cb.createQuery(Zahlungsauftrag.class);
		List<Predicate> predicates = new ArrayList<>();

		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);
		Predicate predicateInPeriode = cb.between(
			root.get(Zahlungsauftrag_.timestampErstellt),
			periode.getGueltigkeit().getGueltigAb().atStartOfDay(),
			periode.getGueltigkeit().getGueltigBis().atStartOfDay()
		);
		predicates.add(predicateInPeriode);

		Predicate zahlungslaufTyp = cb.equal(root.get(Zahlungsauftrag_.zahlungslaufTyp), ZahlungslaufTyp.GEMEINDE_INSTITUTION);
		predicates.add(zahlungslaufTyp);

		if (gemeinde != null) {
			Predicate predicateGemeinde = cb.equal(root.get(Zahlungsauftrag_.gemeinde), gemeinde);
			predicates.add(predicateGemeinde);
		}

		if (institution != null) {
			var joinZahlungen = root.join(Zahlungsauftrag_.zahlungen);
			var predicateInstitution = cb.equal(joinZahlungen.get(Zahlung_.empfaengerId), institution.getId());
			predicates.add(predicateInstitution);
		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}


	private List<ZahlungenDataRow> filterZahlungenAndConvertToDataRows(
		@Nonnull List<Zahlungsauftrag> zahlungsauftrage,
		@Nullable String institutionId
	) {
		var rows = new ArrayList<ZahlungenDataRow>();
		for (Zahlungsauftrag zahlungsauftrag : zahlungsauftrage) {
			authorizer.checkReadAuthorizationZahlungsauftrag(zahlungsauftrag);

			var zahlungen = zahlungsauftrag.getZahlungen();
			if (institutionId != null) {
				zahlungen = filterZahlungenByInstitution(zahlungen, institutionId);
			}

			for (Zahlung zahlung : zahlungen) {
				authorizer.checkReadAuthorizationZahlung(zahlung);

				for (Zahlungsposition zahlungsposition : zahlung.getZahlungspositionen()) {
					rows.add(zahlungToDataRow(zahlungsauftrag, zahlung, zahlungsposition));
				}
			}
		}
		return rows;
	}

	public static ZahlungenDataRow zahlungToDataRow(
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Zahlung zahlung,
		@Nonnull Zahlungsposition zahlungsposition
	) {

		var row = new ZahlungenDataRow();
		row.setZahlungslaufTitle(zahlungsauftrag.getBeschrieb());
		row.setGemeinde(zahlungsauftrag.getGemeinde().getName());
		row.setInstitution(zahlung.extractInstitution().getName());
		row.setTimestampZahlungslauf(zahlungsauftrag.getTimestampErstellt());
		row.setKindVorname(zahlungsposition.getKind().getVorname());
		row.setKindNachname(zahlungsposition.getKind().getNachname());
		row.setReferenznummer(zahlungsposition.getVerfuegungZeitabschnitt().getVerfuegung().getBetreuung().getBGNummer());
		row.setZeitabschnittVon(zahlungsposition.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigAb());
		row.setZeitabschnittBis(zahlungsposition.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigBis());
		var pensum = MathUtil.EXACT.divide(zahlungsposition.getVerfuegungZeitabschnitt().getBgPensum(), BigDecimal.valueOf(100));
		row.setBgPensum(pensum);
		row.setBetrag(zahlungsposition.getBetrag());
		row.setKorrektur(ZahlungspositionStatus.NORMAL != zahlungsposition.getStatus());
		row.setIgnorieren(zahlungsposition.isIgnoriert());

		// auszahlungsdaten nur bei ignorierten zeitabschnitten zeigen
		if (!zahlungsposition.isIgnoriert()) {
			return row;
		}

		var famSitContainer = zahlungsposition
			.getVerfuegungZeitabschnitt()
			.getVerfuegung()
			.getBetreuung()
			.getKind()
			.getGesuch()
			.getFamiliensituationContainer();

		if (famSitContainer == null
			|| famSitContainer.getFamiliensituationJA() == null
			|| famSitContainer.getFamiliensituationJA().getAuszahlungsdaten() == null) {
			return row;
		}

		var auszahlungsdaten = famSitContainer
			.getFamiliensituationJA()
			.getAuszahlungsdaten();

		if (auszahlungsdaten.getIban() != null) {
			row.setIbanEltern(auszahlungsdaten.getIban().getIban());
		}
		row.setKontoEltern(auszahlungsdaten.getKontoinhaber());

		return row;
	}

	private List<Zahlung> filterZahlungenByInstitution(
		@Nonnull List<Zahlung> zahlungen,
		@Nonnull String institutionId
	) {
		return zahlungen.stream()
			.filter(z -> z.extractInstitution().getId().equals(institutionId))
			.collect(Collectors.toList());
	}

}
