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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.enums.ZahlungspositionStatus;
import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungAuftrag;
import ch.dvbern.ebegu.reporting.zahlungsauftrag.ZahlungDataRow;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class ZahlungAuftragDetailsExcelConverter implements ExcelConverter {

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
		@Nonnull List<ZahlungDataRow> data,
		@Nonnull Locale locale,
		@Nonnull String beschrieb,
		@Nonnull LocalDateTime datumGeneriert,
		@Nonnull LocalDate datumFaellig,
		@Nonnull Gemeinde gemeinde
	) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		addHeaders(excelMerger, locale, Objects.requireNonNull(gemeinde.getMandant()));

		excelMerger.addValue(MergeFieldZahlungAuftrag.beschrieb, beschrieb);
		excelMerger.addValue(MergeFieldZahlungAuftrag.generiertAm, datumGeneriert);
		excelMerger.addValue(MergeFieldZahlungAuftrag.faelligAm, datumFaellig);
		excelMerger.addValue(MergeFieldZahlungAuftrag.gemeinde, gemeinde.getName());

		data.stream()
			.sorted()
			.forEach(zahlungDataRow ->
				filterZahlungspositionenMitSummeUngleich0(zahlungDataRow.getZahlung().getZahlungspositionen()).stream()
					.filter(zahlungsposition -> MathUtil.isPositive(zahlungsposition.getVerfuegungZeitabschnitt().getBgPensum()))
					.sorted()
					.forEach(zahlungsposition -> {
						ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldZahlungAuftrag.repeatZahlungAuftragRow);
						final Zahlung zahlung = zahlungDataRow.getZahlung();
						final Betreuung betreuung = zahlungsposition.getVerfuegungZeitabschnitt().getVerfuegung().getBetreuung();
						Objects.requireNonNull(betreuung);
						final Institution institution = betreuung.getInstitutionStammdaten().getInstitution();

						excelRowGroup.addValue(MergeFieldZahlungAuftrag.institution, institution.getName());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.betreuungsangebotTyp,
							ServerMessageUtil.translateEnumValue(zahlung.getBetreuungsangebotTyp(), locale, gemeinde.getMandant()));
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.name, zahlungsposition.getKind().getNachname());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.vorname, zahlungsposition.getKind().getVorname());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.gebDatum, zahlungsposition.getKind().getGeburtsdatum());
						excelRowGroup.addValue(MergeFieldZahlungAuftrag.verfuegung, betreuung.getReferenzNummer());
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

	List<Zahlungsposition> filterZahlungspositionenMitSummeUngleich0(List<Zahlungsposition> zahlungspositionen) {
		List<Zahlungsposition>  resultat = new LinkedList<>();
		for (Zahlungsposition zahlungposition : zahlungspositionen) {
			Optional<Zahlungsposition> inverted = zahlungspositionen.stream()
				.filter(z ->
				{
					final Betreuung betreuungVerfuegung = z.getVerfuegungZeitabschnitt().getVerfuegung().getBetreuung();
					Objects.requireNonNull(betreuungVerfuegung);
					final Betreuung betreuungZahlungsposition = zahlungposition.getVerfuegungZeitabschnitt().getVerfuegung().getBetreuung();
					Objects.requireNonNull(betreuungZahlungsposition);
					return betreuungVerfuegung.getReferenzNummer()
						.equals(betreuungZahlungsposition.getReferenzNummer())
					&& z.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigAb()
						.equals(zahlungposition.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigAb())
					&& z.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigBis()
						.equals(zahlungposition.getVerfuegungZeitabschnitt().getGueltigkeit().getGueltigBis())
					&& MathUtil.isSame(z.getVerfuegungZeitabschnitt().getBgPensum(),
						zahlungposition.getVerfuegungZeitabschnitt().getBgPensum())
					&& MathUtil.isSame(z.getBetrag().multiply(BigDecimal.valueOf(-1)),
						zahlungposition.getBetrag())
					&& z.getStatus() == zahlungposition.getStatus()
					&& z.getStatus() == ZahlungspositionStatus.KORREKTUR;
				}).findFirst();
			if (!inverted.isPresent()) {
				resultat.add(zahlungposition);
			}
		}
		return resultat;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale, @Nonnull Mandant mandant) {
		excelMerger.addValue(MergeFieldZahlungAuftrag.generiertAmTitle, ServerMessageUtil.getMessage("Reports_generiertAmTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.faelligAmTitle, ServerMessageUtil.getMessage("Reports_faelligAmTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.gemeindeTitle, ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.institutionTitle, ServerMessageUtil.getMessage("Reports_institutionTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.betreuungsangebotTypTitle, ServerMessageUtil.getMessage("Reports_betreuungsangebotTypTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.verfuegungTitle, ServerMessageUtil.getMessage("Reports_verfuegungTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.vonTitle, ServerMessageUtil.getMessage("Reports_vonTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.bisTitle, ServerMessageUtil.getMessage("Reports_bisTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.bgPensumTitle, ServerMessageUtil.getMessage("Reports_bgPensumTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.betragCHFTitle, ServerMessageUtil.getMessage("Reports_betragCHFTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.korrekturTitle, ServerMessageUtil.getMessage("Reports_korrekturTitle", locale, mandant));
		excelMerger.addValue(MergeFieldZahlungAuftrag.zahlungIgnorierenTitle, ServerMessageUtil.getMessage("Reports_zahlungIgnorierenTitle", locale, mandant));
	}
}
