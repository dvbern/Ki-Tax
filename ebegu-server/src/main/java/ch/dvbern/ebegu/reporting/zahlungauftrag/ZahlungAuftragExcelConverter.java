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
package ch.dvbern.ebegu.reporting.zahlungauftrag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.ZahlungspositionStatus;
import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungAuftrag;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class ZahlungAuftragExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
		sheet.autoSizeColumn(0); // institution
		//		sheet.autoSizeColumn(1); // name
		sheet.autoSizeColumn(2); // vorname
		sheet.autoSizeColumn(3); // gebDatum
		sheet.autoSizeColumn(4); // verfuegung
		sheet.autoSizeColumn(5); // vonDatum
		sheet.autoSizeColumn(6); // bisDatum
		sheet.autoSizeColumn(7); // bgPensum
		sheet.autoSizeColumn(8); // betragCHF
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<Zahlung> data,
		@Nonnull Locale locale,
		@Nullable UserRole userRole,
		@Nonnull Collection<Institution> allowedInst,
		@Nonnull String beschrieb,
		@Nonnull LocalDateTime datumGeneriert,
		@Nonnull LocalDate datumFaellig,
		@Nonnull Gemeinde gemeinde
	) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		addHeaders(excelMerger, locale);

		excelMerger.addValue(MergeFieldZahlungAuftrag.beschrieb, beschrieb);
		excelMerger.addValue(MergeFieldZahlungAuftrag.generiertAm, datumGeneriert);
		excelMerger.addValue(MergeFieldZahlungAuftrag.faelligAm, datumFaellig);
		excelMerger.addValue(MergeFieldZahlungAuftrag.gemeinde, gemeinde.getName());

		data.stream()
			.filter(zahlung -> {
				// Filtere nur die erlaubten Instituionsdaten
				// User mit der Rolle Institution oder Traegerschaft dürfen nur "Ihre" Institutionsdaten sehen.
				return !EnumUtil.isOneOf(userRole, UserRole.getInstitutionTraegerschaftRoles()) ||
					allowedInst.stream().anyMatch(institution -> institution.getId().equals(zahlung.getInstitutionStammdaten().getInstitution().getId()));
			})
			.sorted()
			.forEach(zahlung ->
				zahlung.getZahlungspositionen().stream()
					.filter(zahlungsposition -> MathUtil.isPositive(zahlungsposition.getVerfuegungZeitabschnitt().getBgPensum()))
					.sorted()
					.forEach(zahlungsposition -> {
						ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldZahlungAuftrag.repeatZahlungAuftragRow);
						InstitutionStammdaten institutionStammdaten = zahlung.getInstitutionStammdaten();
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.institution, institutionStammdaten.getInstitution().getName());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.betreuungsangebotTyp,
							ServerMessageUtil.translateEnumValue(institutionStammdaten.getBetreuungsangebotTyp(), locale));
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.name, zahlungsposition.getKind().getNachname());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.vorname, zahlungsposition.getKind().getVorname());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.gebDatum, zahlungsposition.getKind().getGeburtsdatum());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.verfuegung,
							zahlungsposition.getVerfuegungZeitabschnitt().getVerfuegung().getBetreuung().getBGNummer());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.vonDatum,
							zahlungsposition.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigAb());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.bisDatum,
							zahlungsposition.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigBis());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.bgPensum, zahlungsposition.getVerfuegungZeitabschnitt().getBgPensum()
							.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.betragCHF, zahlungsposition.getBetrag());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.isKorrektur, ZahlungspositionStatus.NORMAL != zahlungsposition.getStatus());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.isIgnoriert, zahlungsposition.isIgnoriert());
					}));

		return excelMerger;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale) {
		excelMerger.addValue(MergeFieldZahlungAuftrag.generiertAmTitle, ServerMessageUtil.getMessage("Reports_generiertAmTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.faelligAmTitle, ServerMessageUtil.getMessage("Reports_faelligAmTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.gemeindeTitle, ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.institutionTitle, ServerMessageUtil.getMessage("Reports_institutionTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.betreuungsangebotTypTitle, ServerMessageUtil.getMessage("Reports_betreuungsangebotTypTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.verfuegungTitle, ServerMessageUtil.getMessage("Reports_verfuegungTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.vonTitle, ServerMessageUtil.getMessage("Reports_vonTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.bisTitle, ServerMessageUtil.getMessage("Reports_bisTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.bgPensumTitle, ServerMessageUtil.getMessage("Reports_bgPensumTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.betragCHFTitle, ServerMessageUtil.getMessage("Reports_betragCHFTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.korrekturTitle, ServerMessageUtil.getMessage("Reports_korrekturTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.zahlungIgnorierenTitle, ServerMessageUtil.getMessage("Reports_zahlungIgnorierenTitle", locale));
	}
}
