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
package ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldGesuchstellerKinderBetreuung;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.*;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeField;
import org.apache.poi.ss.usermodel.Sheet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

@Dependent
public class GesuchstellerKinderBetreuungExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public Sheet mergeHeaderFieldsStichtag(
			@Nonnull List<GesuchstellerKinderBetreuungDataRow> data,
			@Nonnull Sheet sheet,
			@Nonnull LocalDate stichtag,
			@Nonnull Locale locale,
			@Nonnull Mandant mandant) throws ExcelMergeException {

		checkNotNull(data);

		ExcelMergerDTO excelMergerDTO = new ExcelMergerDTO();
		List<MergeField<?>> mergeFields = new ArrayList<>();

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.stichtag.getMergeField());
		excelMergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.stichtag.getMergeField(), stichtag);

		addHeaders(excelMergerDTO, mergeFields, locale, mandant);

		ExcelMerger.mergeData(sheet, mergeFields, excelMergerDTO);

		return sheet;
	}

	@Nonnull
	public Sheet mergeHeaderFieldsPeriode(
			@Nonnull List<GesuchstellerKinderBetreuungDataRow> data,
			@Nonnull Sheet sheet,
			@Nonnull LocalDate auswertungVon,
			@Nonnull LocalDate auswertungBis,
			@Nullable Gesuchsperiode auswertungPeriode,
			@Nonnull Locale locale,
			@Nonnull Mandant mandant) throws ExcelMergeException {

		checkNotNull(data);

		ExcelMergerDTO excelMergerDTO = new ExcelMergerDTO();
		List<MergeField<?>> mergeFields = new ArrayList<>();

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.auswertungVon.getMergeField());
		excelMergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.auswertungVon.getMergeField(), auswertungVon);

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.auswertungBis.getMergeField());
		excelMergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.auswertungBis.getMergeField(), auswertungBis);

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.auswertungPeriode.getMergeField());
		if (auswertungPeriode != null) {
			excelMergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.auswertungPeriode.getMergeField(),
				auswertungPeriode.getGesuchsperiodeString());
		}

		addHeaders(excelMergerDTO, mergeFields, locale, mandant);

		ExcelMerger.mergeData(sheet, mergeFields, excelMergerDTO);

		return sheet;
	}

	public void mergeRows(
		RowFiller rowFiller,
		@Nonnull List<GesuchstellerKinderBetreuungDataRow> data,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = new ExcelMergerDTO();
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgNummer, dataRow.getBgNummer());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.institution, dataRow.getInstitution());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.betreuungsTyp,
				ServerMessageUtil.translateEnumValue(requireNonNull(dataRow.getBetreuungsTyp()), locale, mandant));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.periode, dataRow.getPeriode());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gesuchStatus, dataRow.getGesuchStatus());

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.eingangsdatum, dataRow.getEingangsdatum());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.verfuegungsdatum, dataRow.getVerfuegungsdatum());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.fallId, dataRow.getFallId());

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gemeinde, dataRow.getGemeinde());

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.iban, dataRow.getIban());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.kontoInhaber, dataRow.getKontoInhaber());

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1Name, dataRow.getGs1Name());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1Vorname, dataRow.getGs1Vorname());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1Strasse, dataRow.getGs1Strasse());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1Hausnummer, dataRow.getGs1Hausnummer());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1Zusatzzeile, dataRow.getGs1Zusatzzeile());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1Plz, dataRow.getGs1Plz());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1Ort, dataRow.getGs1Ort());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1Diplomatenstatus, dataRow.getGs1Diplomatenstatus());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1EwpAngestellt, MathUtil.GANZZAHL.from(dataRow.getGs1EwpAngestellt()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1EwpAusbildung, MathUtil.GANZZAHL.from(dataRow.getGs1EwpAusbildung()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1EwpSelbstaendig, MathUtil.GANZZAHL.from(dataRow.getGs1EwpSelbstaendig()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1EwpRav, MathUtil.GANZZAHL.from(dataRow.getGs1EwpRav()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1EwpGesundhtl, MathUtil.GANZZAHL.from(dataRow.getGs1EwpGesundhtl()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1EwpIntegration, MathUtil.GANZZAHL.from(dataRow.getGs1EwpIntegration()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs1EwpFreiwillig, MathUtil.GANZZAHL.from(dataRow.getGs1EwpFreiwillig()));

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2Name, dataRow.getGs2Name());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2Vorname, dataRow.getGs2Vorname());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2Strasse, dataRow.getGs2Strasse());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2Hausnummer, dataRow.getGs2Hausnummer());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2Zusatzzeile, dataRow.getGs2Zusatzzeile());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2Plz, dataRow.getGs2Plz());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2Ort, dataRow.getGs2Ort());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2Diplomatenstatus, dataRow.getGs2Diplomatenstatus());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2EwpAngestellt, MathUtil.GANZZAHL.from(dataRow.getGs2EwpAngestellt()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2EwpAusbildung, MathUtil.GANZZAHL.from(dataRow.getGs2EwpAusbildung()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2EwpSelbstaendig, MathUtil.GANZZAHL.from(dataRow.getGs2EwpSelbstaendig()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2EwpRav, MathUtil.GANZZAHL.from(dataRow.getGs2EwpRav()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2EwpGesundhtl, MathUtil.GANZZAHL.from(dataRow.getGs2EwpGesundhtl()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2EwpIntegration, MathUtil.GANZZAHL.from(dataRow.getGs2EwpIntegration()));
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.gs2EwpFreiwillig, MathUtil.GANZZAHL.from(dataRow.getGs2EwpFreiwillig()));

			String familiensituation = dataRow.getFamiliensituation() != null
				? ServerMessageUtil.translateEnumValue(dataRow.getFamiliensituation(), locale, mandant)
				: "";
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.familiensituation, familiensituation);
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.familiengroesse, dataRow.getFamiliengroesse());

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.massgEinkVorFamilienabzug, dataRow.getMassgEinkVorFamilienabzug());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.familienabzug, dataRow.getFamilienabzug());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.massgEink, dataRow.getMassgEink());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.einkommensjahr, dataRow.getEinkommensjahr());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.ekvVorhandenBasisJahr1, dataRow.getEkvVorhandenBasisJahr1());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.ekvVorhandenBasisJahr2, dataRow.getEkvVorhandenBasisJahr2());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.ekvAnnuliertBasisJahr1, dataRow.getEkvAnnuliertBasisJahr1());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.ekvAnnuliertBasisJahr2, dataRow.getEkvAnnuliertBasisJahr2());

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.stvGeprueft, dataRow.getStvGeprueft());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.veranlagt, dataRow.getVeranlagt());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.mzvBeantragt, dataRow.getMzvBeantragt());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.sozialhilfebezueger, dataRow.isSozialhilfeBezueger());

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.kindName, dataRow.getKindName());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.kindVorname, dataRow.getKindVorname());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.kindGeburtsdatum, dataRow.getKindGeburtsdatum());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.kindFachstelle, dataRow.getKindFachstelle());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.kindIntegration, dataRow.getKindIntegration());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.kindErwBeduerfnisse, dataRow.getKindErwBeduerfnisse());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.kindSprichtAmtssprache, dataRow.getKindSprichtAmtssprache());

			String einschulungTyp = dataRow.getKindEinschulungTyp() != null ?
				ServerMessageUtil.translateEnumValue(dataRow.getKindEinschulungTyp(), locale, mandant) : "";
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.eingeschult, einschulungTyp);
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.keinPlatzImSchulhort, dataRow.getKeinPlatzImSchulhort());

			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.zeitabschnittVon, dataRow.getZeitabschnittVon());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.zeitabschnittBis, dataRow.getZeitabschnittBis());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.betreuungsStatus, dataRow.getBetreuungsStatus());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.betreuungsPensum, dataRow.getBetreuungspensum());
			BigDecimal anspruchsPensumTotal = dataRow.getAnspruchsPensumTotal();
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.anspruchsPensumKanton, dataRow.getAnspruchsPensumKanton());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.anspruchsPensumGemeinde, dataRow.getAnspruchsPensumGemeinde());
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.anspruchsPensumTotal, anspruchsPensumTotal);
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumZeiteinheit, dataRow.getBgPensumZeiteinheit());
			if (anspruchsPensumTotal != null && anspruchsPensumTotal.compareTo(BigDecimal.ZERO) > 0) {
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumKanton, dataRow.getBgPensumKanton());
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumGemeinde, dataRow.getBgPensumGemeinde());
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumTotal, dataRow.getBgPensumTotal());
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgStunden, dataRow.getBgStunden());
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.vollkosten, dataRow.getVollkosten());
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.elternbeitrag, dataRow.getElternbeitrag());
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.verguenstigungKanton, dataRow.getVerguenstigungKanton());
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.verguenstigungGemeinde, dataRow.getVerguenstigungGemeinde());
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.verguenstigungTotal, dataRow.getVerguenstigungTotal());
			} else {
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumKanton, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumGemeinde, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumTotal, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.bgStunden, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.vollkosten, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.elternbeitrag, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.verguenstigungKanton, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.verguenstigungGemeinde, BigDecimal.ZERO);
				excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.verguenstigungTotal, BigDecimal.ZERO);
			}
			excelRowGroup.addValue(MergeFieldGesuchstellerKinderBetreuung.ausserordentlicherAnspruch, dataRow.getAusserordentlicherAnspruch());

			rowFiller.fillRow(excelRowGroup);
		});
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void addHeaders(@Nonnull ExcelMergerDTO mergerDTO, @Nonnull List<MergeField<?>> mergeFields, @Nonnull Locale locale, @Nonnull
			Mandant mandant) {
		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gesuchstellerTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gesuchstellerTitle.getMergeField(), ServerMessageUtil.getMessage("Reports_gesuchstellerTitleTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.stichtagTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.stichtagTitle.getMergeField(), ServerMessageUtil.getMessage("Reports_stichtagTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.institutionTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.institutionTitle, ServerMessageUtil.getMessage("Reports_institutionTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.angebotTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.angebotTitle, ServerMessageUtil.getMessage("Reports_angebotTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.parameterTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.parameterTitle.getMergeField(), ServerMessageUtil.getMessage("Reports_parameterTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.periodeTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.periodeTitle, ServerMessageUtil.getMessage("Reports_periodeTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.eingangsdatumTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.eingangsdatumTitle, ServerMessageUtil.getMessage("Reports_eingangsdatumTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.nachnameTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.vornameTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.verfuegungsdatumTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.verfuegungsdatumTitle, ServerMessageUtil.getMessage("Reports_verfuegungsdatumTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.fallIdTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.fallIdTitle, ServerMessageUtil.getMessage("Reports_fallIdTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gemeindeTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gemeindeTitle, ServerMessageUtil.getMessage("Reports_gemeindeTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.auszahlungTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.auszahlungTitle, ServerMessageUtil.getMessage("Reports_auszahlungTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.ibanTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.ibanTitle, ServerMessageUtil.getMessage("Reports_ibanTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.kontoInhaberTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.kontoInhaberTitle, ServerMessageUtil.getMessage("Reports_kontoinhaberTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gesuchsteller1Title.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gesuchsteller1Title, ServerMessageUtil.getMessage("Reports_gesuchsteller1Title",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gesuchsteller2Title.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gesuchsteller2Title, ServerMessageUtil.getMessage("Reports_gesuchsteller2Title",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.strasseTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.strasseTitle, ServerMessageUtil.getMessage("Reports_strasseTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.strasseNrTitel.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.strasseNrTitel, ServerMessageUtil.getMessage("Reports_strasseNrTitel",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.zusatzTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.zusatzTitle, ServerMessageUtil.getMessage("Reports_zusatzTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.plzTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.plzTitle, ServerMessageUtil.getMessage("Reports_plzTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.ortTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.ortTitle, ServerMessageUtil.getMessage("Reports_ortTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.diplomatTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.diplomatTitle, ServerMessageUtil.getMessage("Reports_diplomatTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.beschaeftigungspensumTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.beschaeftigungspensumTitle, ServerMessageUtil.getMessage("Reports_beschaeftigungspensumTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.totalTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.totalTitle, ServerMessageUtil.getMessage("Reports_totalTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.angestelltTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.angestelltTitle, ServerMessageUtil.getMessage("Reports_angestelltTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.weiterbildungTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.weiterbildungTitle, ServerMessageUtil.getMessage("Reports_weiterbildungTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.selbstaendigTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.selbstaendigTitle, ServerMessageUtil.getMessage("Reports_selbstaendigTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.arbeitssuchendTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.arbeitssuchendTitle, ServerMessageUtil.getMessage("Reports_arbeitssuchendTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.integrationTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.integrationTitle, ServerMessageUtil.getMessage("Reports_integrationTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gesIndikationTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gesIndikationTitle, ServerMessageUtil.getMessage("Reports_gesIndikationTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.freiwilligenarbeitTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.freiwilligenarbeitTitle, ServerMessageUtil.getMessage("Reports_freiwilligenarbeitTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.familieTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.familieTitle, ServerMessageUtil.getMessage("Reports_familieTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.famSituationTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.famSituationTitle, ServerMessageUtil.getMessage("Reports_famSituationTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.famGroesseTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.famGroesseTitle, ServerMessageUtil.getMessage("Reports_famGroesseTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.einkommenTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.einkommenTitle, ServerMessageUtil.getMessage("Reports_einkommenTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.einkommenVorAbzugTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.einkommenVorAbzugTitle, ServerMessageUtil.getMessage("Reports_einkommenVorAbzugTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.famAbzugTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.famAbzugTitle, ServerMessageUtil.getMessage("Reports_famAbzugTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.massEinkommenTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.massEinkommenTitle, ServerMessageUtil.getMessage("Reports_massEinkommenTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.einkommensjahrTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.einkommensjahrTitle, ServerMessageUtil.getMessage("Reports_einkommensjahrTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.einkommensverschlechterungBasisJahr1Title.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.einkommensverschlechterungBasisJahr1Title, ServerMessageUtil.getMessage("Reports_einkommensverschlechterungBasisJahr1Title",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.einkommensverschlechterungBasisJahr2Title.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.einkommensverschlechterungBasisJahr2Title, ServerMessageUtil.getMessage("Reports_einkommensverschlechterungBasisJahr2Title",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.einkommensverschlechterungBasisJahr1AnnuliertTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.einkommensverschlechterungBasisJahr1AnnuliertTitle, ServerMessageUtil.getMessage("Reports_einkommensverschlechterungBasisJahr1AnnuliertTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.einkommensverschlechterungBasisJahr2AnnuliertTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.einkommensverschlechterungBasisJahr2AnnuliertTitle, ServerMessageUtil.getMessage("Reports_einkommensverschlechterungBasisJahr2AnnuliertTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.sozialhilfebezuegerTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.sozialhilfebezuegerTitle, ServerMessageUtil.getMessage("Reports_sozialhilfebezuegerTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.geprueftSTVTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.geprueftSTVTitle, ServerMessageUtil.getMessage("Reports_geprueftSTVTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.veranlagtTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.veranlagtTitle, ServerMessageUtil.getMessage("Reports_veranlagtTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.mzvBeantragtTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.mzvBeantragtTitle, ServerMessageUtil.getMessage("Reports_mzvBeantragtTitle",locale, mandant));




		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.kinderTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.kinderTitle, ServerMessageUtil.getMessage("Reports_kinderTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.kindTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.kindTitle, ServerMessageUtil.getMessage("Reports_kindTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.vonTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.vonTitle, ServerMessageUtil.getMessage("Reports_vonTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bisTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bisTitle, ServerMessageUtil.getMessage("Reports_bisTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.geburtsdatumTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.fachstelleTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.fachstelleTitle, ServerMessageUtil.getMessage("Reports_fachstelleTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.kindIntegrationTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.kindIntegrationTitle, ServerMessageUtil.getMessage("Reports_kindIntegrationTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.babyFaktorTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.babyFaktorTitle, ServerMessageUtil.getMessage("Reports_babyFaktorTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.besonderebeduerfnisseTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.besonderebeduerfnisseTitle, ServerMessageUtil.getMessage("Reports_besonderebeduerfnisseTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.sprichtAmtsspracheTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.sprichtAmtsspracheTitle, ServerMessageUtil.getMessage("Reports_sprichtAmtsspracheTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.schulstufeTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.schulstufeTitle, ServerMessageUtil.getMessage("Reports_schulstufeTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bis1MonateTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bis1MonateTitle, ServerMessageUtil.getMessage("Reports_bis1MonateTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bis2MonateTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bis2MonateTitle, ServerMessageUtil.getMessage("Reports_bis2MonateTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bis3MonateTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bis3MonateTitle, ServerMessageUtil.getMessage("Reports_bis3MonateTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.abMonateTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.abMonateTitle, ServerMessageUtil.getMessage("Reports_abMonateTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.betreuungVonTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.betreuungVonTitle, ServerMessageUtil.getMessage("Reports_betreuungVonTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.betreuungBisTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.betreuungBisTitle, ServerMessageUtil.getMessage("Reports_betreuungBisTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.betreuungStatus.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.betreuungStatus, ServerMessageUtil.getMessage("Reports_betreuungStatus",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.anteilMonatTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.anteilMonatTitle, ServerMessageUtil.getMessage("Reports_anteilMonatTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.betreuungTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.betreuungTitle, ServerMessageUtil.getMessage("Reports_betreuungTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.pensumTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.pensumTitle, ServerMessageUtil.getMessage("Reports_pensumTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.kostenTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.kostenTitle, ServerMessageUtil.getMessage("Reports_kostenTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.anspruchberechtigtKantonTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.anspruchberechtigtKantonTitle, ServerMessageUtil.getMessage(			"Reports_anspruchberechtigtKantonTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.anspruchberechtigtGemeindeTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.anspruchberechtigtGemeindeTitle, ServerMessageUtil.getMessage(			"Reports_anspruchberechtigtGemeindeTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.anspruchberechtigtTotalTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.anspruchberechtigtTotalTitle, ServerMessageUtil.getMessage(			"Reports_anspruchberechtigtTotalTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bgPensumKantonTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumKantonTitle, ServerMessageUtil.getMessage("Reports_bgPensumKantonTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bgPensumGemeindeTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumGemeindeTitle, ServerMessageUtil.getMessage("Reports_bgPensumGemeindeTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bgPensumTotalTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumTotalTitle, ServerMessageUtil.getMessage("Reports_bgPensumTotalTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bgPensumStdTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumStdTitle, ServerMessageUtil.getMessage("Reports_bgPensumStdTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bgPensumZeiteinheitTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bgPensumZeiteinheitTitle, ServerMessageUtil.getMessage("Reports_bgPensumZeiteinheitTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bgMonatspensumTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bgMonatspensumTitle, ServerMessageUtil.getMessage("Reports_bgMonatspensumTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.elternbeitragTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.elternbeitragTitle, ServerMessageUtil.getMessage("Reports_elternbeitragTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gutscheinKantonTitel.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gutscheinKantonTitel, ServerMessageUtil.getMessage("Reports_gutscheinKantonTitel",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gutscheinGemeindeTitel.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gutscheinGemeindeTitel, ServerMessageUtil.getMessage("Reports_gutscheinGemeindeTitel",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gutscheinTotalTitel.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gutscheinTotalTitel, ServerMessageUtil.getMessage("Reports_gutscheinTotalTitel",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.statusTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.statusTitle, ServerMessageUtil.getMessage("Reports_statusTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.gesuchstellerKinderBetreuungTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.gesuchstellerKinderBetreuungTitle, ServerMessageUtil.getMessage("Reports_gesuchstellerKinderBetreuungTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.bgNummerTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.bgNummerTitle, ServerMessageUtil.getMessage("Reports_bgNummerTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.ausserordentlicherAnspruchTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.ausserordentlicherAnspruchTitle, ServerMessageUtil.getMessage("Reports_ausserordentlicherAnspruchTitle",locale, mandant));

		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.zusatzFelderGemeinden.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.zusatzFelderGemeinden, ServerMessageUtil.getMessage("Reports_zusatzFelderGemeinden",locale, mandant));

		addSpecialValuesForLuzern(mergerDTO, mergeFields, locale, mandant);

	}

	private static void addSpecialValuesForLuzern(ExcelMergerDTO mergerDTO, List<MergeField<?>> mergeFields, Locale locale, Mandant mandant) {
		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.repeatSchulhort.getMergeField());
		var keinPlatzImSchulhortTitleStr = ServerMessageUtil.getMessage("Reports_keinPlatzImSchulhortTitle", locale, mandant);
		mergeFields.add(MergeFieldGesuchstellerKinderBetreuung.keinPlatzImSchulhortTitle.getMergeField());
		mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.keinPlatzImSchulhortTitle, keinPlatzImSchulhortTitleStr);
		// falls die Translations keine Übersetzung für den Titel haben, bedeutet das, dass wir die Spalte beim Mandanten nicht
		// anzeigen wollen. Falls wir sie anzeigen wollen, muss in das repateCol Mergefield ein leerer String geschrieben werden.
		if (!keinPlatzImSchulhortTitleStr.equals("")) {
			mergerDTO.addValue(MergeFieldGesuchstellerKinderBetreuung.repeatSchulhort, "");
		}
	}

}


