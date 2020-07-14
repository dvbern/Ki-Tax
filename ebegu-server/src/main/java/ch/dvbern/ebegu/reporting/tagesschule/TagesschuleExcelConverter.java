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

package ch.dvbern.ebegu.reporting.tagesschule;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.reporting.MergeFieldTagesschule;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class TagesschuleExcelConverter implements ExcelConverter {

	public static final int IDENTIFIER_WOECHENTLICHES_MODUL = 1;
	public static final int IDENTIFIER_ZWEIWOECHENTLICHES_MODUL = 1;

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<TagesschuleDataRow> data,
		@Nonnull Locale locale,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull EinstellungenTagesschule einstellungenTagesschule,
		@Nonnull String tagesschuleName) {

		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		excelMerger.addValue(MergeFieldTagesschule.tagesschuleAnmeldungenTitle, tagesschuleName);

		String gesuchsPeriodeStr = gesuchsperiode.getGesuchsperiodeString() + " (" + gesuchsperiode.getGesuchsperiodeDisplayName(locale) + ')';
		excelMerger.addValue(MergeFieldTagesschule.periode, gesuchsPeriodeStr);

		List<ModulTagesschuleGroup> sortedGroups =
			einstellungenTagesschule.getModulTagesschuleGroups().stream().sorted(Comparator.naturalOrder())
				.collect(Collectors.toList());

		List<TagesschuleRepeatColGroup> repeatColGroupList =
			generateWeekdayModuleGroups(sortedGroups);

		addHeaders(excelMerger, locale, repeatColGroupList);

		List<TagesschuleDataRow> nichtFreigegebeneGesuche = new ArrayList<>();
		List<TagesschuleDataRow> freigegebeneGesuche = new ArrayList<>();

		for (TagesschuleDataRow row : data) {
			if (row.getStatus() == Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST) {
				nichtFreigegebeneGesuche.add(row);
			} else {
				freigegebeneGesuche.add(row);
			}
		}

		freigegebeneGesuche.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldTagesschule.repeatRow);
			excelRowGroup.addValue(MergeFieldTagesschule.nachnameKind, dataRow.getNachnameKind());
			excelRowGroup.addValue(MergeFieldTagesschule.vornameKind, dataRow.getVornameKind());
			excelRowGroup.addValue(MergeFieldTagesschule.geburtsdatumKind, dataRow.getGeburtsdatum());
			excelRowGroup.addValue(MergeFieldTagesschule.referenznummer, dataRow.getReferenznummer());
			excelRowGroup.addValue(MergeFieldTagesschule.eintrittsdatum, dataRow.getEintrittsdatum());
			excelRowGroup.addValue(MergeFieldTagesschule.status, ServerMessageUtil.translateEnumValue(dataRow.getStatus(), locale));

			setAnmeldungenForModule(dataRow, repeatColGroupList, excelRowGroup);
		});
		// wenn das Gesuch noch nicht freigegeben wurde, soll das Kind anonym erscheinen
		nichtFreigegebeneGesuche.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldTagesschule.repeatRow2);
			excelRowGroup.addValue(MergeFieldTagesschule.nachnameKind, ServerMessageUtil.getMessage("Reports_anonym", locale));
			excelRowGroup.addValue(MergeFieldTagesschule.status, ServerMessageUtil.translateEnumValue(dataRow.getStatus(), locale));

			setAnmeldungenForModule(dataRow, repeatColGroupList, excelRowGroup);
		});

		return excelMerger;
	}

	/**
	 * erstellt eine Liste allen Modulgruppen pro Wochentag. Z.B.:
	 * [
	 * 	montag: [modulGroup1, modulGroup2, ...],
	 * 	dienstag: [modulGroup2, modulGroup3, ...]
	 * ]
	 * Wochentage ohne Module werden gefiltert.
	 */
	@Nonnull
	private List<TagesschuleRepeatColGroup> generateWeekdayModuleGroups(@Nonnull List<ModulTagesschuleGroup> modulTagesschuleGroups) {

		List<TagesschuleRepeatColGroup> repeatColGroupList = new ArrayList<>();
		repeatColGroupList.add(new TagesschuleRepeatColGroup(DayOfWeek.MONDAY, "repeatCol1"));
		repeatColGroupList.add(new TagesschuleRepeatColGroup(DayOfWeek.TUESDAY, "repeatCol2"));
		repeatColGroupList.add(new TagesschuleRepeatColGroup(DayOfWeek.WEDNESDAY, "repeatCol3"));
		repeatColGroupList.add(new TagesschuleRepeatColGroup(DayOfWeek.THURSDAY, "repeatCol4"));
		repeatColGroupList.add(new TagesschuleRepeatColGroup(DayOfWeek.FRIDAY, "repeatCol5"));

		for (TagesschuleRepeatColGroup repeatColGroup : repeatColGroupList) {
			for (ModulTagesschuleGroup moduleGroup : modulTagesschuleGroups) {
				for (ModulTagesschule module : moduleGroup.getModule()) {
					if (module.getWochentag().compareTo(repeatColGroup.getWochentag()) == 0) {
						repeatColGroup.appendModulGroup(moduleGroup);
					}
				}
			}
		}
		return repeatColGroupList;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale,
		@Nonnull List<TagesschuleRepeatColGroup> repeatColGroups) {

		excelMerger.addValue(MergeFieldTagesschule.generiertAmTitle, ServerMessageUtil.getMessage("Reports_generiertAmTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.generiertAm, LocalDate.now());

		excelMerger.addValue(MergeFieldTagesschule.nachnameKindTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle",	locale));
		excelMerger.addValue(MergeFieldTagesschule.vornameKindTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.referenznummerTitle, ServerMessageUtil.getMessage(
			"Reports_bgNummerTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.eintrittsdatumTitle, ServerMessageUtil.getMessage("Reports_eintrittsdatumTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.statusTitle, ServerMessageUtil.getMessage("Reports_statusTitle", locale));

		excelMerger.addValue(MergeFieldTagesschule.wochentagMo, ServerMessageUtil.getMessage("Reports_MontagShort", locale));
		excelMerger.addValue(MergeFieldTagesschule.wochentagDi, ServerMessageUtil.getMessage("Reports_DienstagShort", locale));
		excelMerger.addValue(MergeFieldTagesschule.wochentagMi, ServerMessageUtil.getMessage("Reports_MittwochShort", locale));
		excelMerger.addValue(MergeFieldTagesschule.wochentagDo, ServerMessageUtil.getMessage("Reports_DonnerstagShort", locale));
		excelMerger.addValue(MergeFieldTagesschule.wochentagFr, ServerMessageUtil.getMessage("Reports_FreitagShort", locale));

		excelMerger.addValue(MergeFieldTagesschule.summeStundenTitle, ServerMessageUtil.getMessage("Reports_summeStundenTitle", locale));
		excelMerger.addValue(MergeFieldTagesschule.summeVerpflegungTitle, ServerMessageUtil.getMessage("Reports_summeVerpflegungTitle", locale));

		excelMerger.addValue(MergeFieldTagesschule.legende, ServerMessageUtil.getMessage("Reports_legende", locale));
		excelMerger.addValue(MergeFieldTagesschule.legendeVolleKosten, ServerMessageUtil.getMessage(
			"Reports_legendeVolleKosten", locale));
		excelMerger.addValue(MergeFieldTagesschule.legendeZweiwoechentlich, ServerMessageUtil.getMessage(
			"Reports_legendeZweiwoechentlich", locale));
		excelMerger.addValue(MergeFieldTagesschule.legendeOhneVerpflegung, ServerMessageUtil.getMessage(
			"Reports_legendeOhneVerpflegung", locale));


		fillModulHeaders(repeatColGroups, excelMerger, locale);
	}

	// Modulnamen, Anzahl Stunden und Verpflegungskosten ausfüllen
	private void fillModulHeaders(List<TagesschuleRepeatColGroup> repeatColGroups, ExcelMergerDTO excelMerger, Locale locale) {
		repeatColGroups.forEach(group -> {
			int counter = Constants.MAX_MODULGROUPS_TAGESSCHULE;
			for (ModulTagesschuleGroup moduleGroup : group.getModulTagesschuleList()) {
				excelMerger.addValue(MergeFieldTagesschule.valueOf(group.getRepeatColName()), null);
				excelMerger.addValue(MergeFieldTagesschule.modulName,
					moduleGroup.getBezeichnung().findTextByLocale(locale));
				long modulStunden = Duration.between(moduleGroup.getZeitVon(), moduleGroup.getZeitBis()).toHours();
				excelMerger.addValue(MergeFieldTagesschule.modulStunden, modulStunden);
				excelMerger.addValue(MergeFieldTagesschule.verpflegungskosten, moduleGroup.getVerpflegungskosten());
				counter--;
			}
			// Eine maximale anzahl Spalten wurden im Excel vorbereitet. Diese müssen ausgefüllt leer ausgefüllt
			// werden, damit sie ausgeblendet werden.
			while (counter > 0) {
				excelMerger.addValue(MergeFieldTagesschule.modulName, null);
				excelMerger.addValue(MergeFieldTagesschule.modulStunden, null);
				excelMerger.addValue(MergeFieldTagesschule.verpflegungskosten, null);
				counter--;
			}
		});
	}

	// Anmeldungen für die Module pro Kind ausfüllen
	private void setAnmeldungenForModule(
		@Nonnull TagesschuleDataRow dataRow,
		@Nonnull List<TagesschuleRepeatColGroup> repeatColGroups,
		@Nonnull ExcelMergerDTO excelRowGroup) {
			repeatColGroups.forEach(weekday -> {
				int counter = Constants.MAX_MODULGROUPS_TAGESSCHULE;
				for (ModulTagesschuleGroup moduleGroup : weekday.getModulTagesschuleList()) {
					for (ModulTagesschule module : moduleGroup.getModule()) {
						if (module.getWochentag() == weekday.getWochentag()) {
							excelRowGroup.addValue(MergeFieldTagesschule.angemeldet, getAnmeldungCode(module, dataRow));
							counter--;
						}
					}
				}
				while (counter > 0) {
					excelRowGroup.addValue(MergeFieldTagesschule.angemeldet, null);
					counter--;
				}
			});
	}

	/**
	 *
	 * @param module: das Modul, für das die Anmeldungen überprüft werden
	 * @param dataRow: für diese Zeile im Excel Report wird überprüft, ob eine Anmeldung für das genannte modul
	 *                  existiert
	 * @return IDENTIFIER_WOECHENTLICHES_MODUL, IDENTIFIER_ZWEIWOECHENTLICHES_MODUL oder null falls nicht angemeldet
	 */
	@Nullable
	private Integer getAnmeldungCode(ModulTagesschule module, TagesschuleDataRow dataRow) {
		if (dataRow.getAnmeldungTagesschule().getBelegungTagesschule() != null) {
			for (BelegungTagesschuleModul m :
				dataRow.getAnmeldungTagesschule().getBelegungTagesschule().getBelegungTagesschuleModule()) {
				if (m.getModulTagesschule().getId().equals(module.getId())) {
					return (m.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN)
						? IDENTIFIER_ZWEIWOECHENTLICHES_MODUL
						: IDENTIFIER_WOECHENTLICHES_MODUL;
				}
			}
		}
		return null;
	}
}
