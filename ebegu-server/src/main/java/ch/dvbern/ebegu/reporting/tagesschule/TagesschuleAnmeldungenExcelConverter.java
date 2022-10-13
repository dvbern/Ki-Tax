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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.reporting.MergeFieldTagesschuleAnmeldungen;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class TagesschuleAnmeldungenExcelConverter implements ExcelConverter {

	public static final int IDENTIFIER_WOECHENTLICHES_MODUL = 1;
	public static final int IDENTIFIER_ZWEIWOECHENTLICHES_MODUL = 2;

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<TagesschuleAnmeldungenDataRow> data,
		@Nonnull Locale locale,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull EinstellungenTagesschule einstellungenTagesschule,
		@Nonnull String tagesschuleName) {

		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.tagesschuleAnmeldungenTitle, tagesschuleName);

		String gesuchsPeriodeStr = gesuchsperiode.getGesuchsperiodeString() + " (" + gesuchsperiode.getGesuchsperiodeDisplayName(locale) + ')';
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.periode, gesuchsPeriodeStr);

		List<ModulTagesschuleGroup> sortedGroups =
			einstellungenTagesschule.getModulTagesschuleGroups().stream().sorted(Comparator.naturalOrder())
				.collect(Collectors.toList());

		List<TagesschuleRepeatColGroup> repeatColGroupList =
			generateWeekdayModuleGroups(sortedGroups);

		addHeaders(excelMerger, locale, repeatColGroupList, Objects.requireNonNull(gesuchsperiode.getMandant()));

		List<TagesschuleAnmeldungenDataRow> nichtFreigegebeneGesuche = new ArrayList<>();
		List<TagesschuleAnmeldungenDataRow> freigegebeneGesuche = new ArrayList<>();

		for (TagesschuleAnmeldungenDataRow row : data) {
			if (row.getStatus() == Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST) {
				nichtFreigegebeneGesuche.add(row);
			} else {
				freigegebeneGesuche.add(row);
			}
		}

		freigegebeneGesuche.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldTagesschuleAnmeldungen.repeatRow);
			setStammdaten(excelRowGroup, dataRow, locale, gesuchsperiode);
			setAnmeldungenForModule(dataRow, repeatColGroupList, excelRowGroup);
		});
		// wenn das Gesuch noch nicht freigegeben wurde, soll das Kind anonym erscheinen
		nichtFreigegebeneGesuche.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldTagesschuleAnmeldungen.repeatRow2);
			setStammdaten(excelRowGroup, dataRow, locale, gesuchsperiode);
			setAnmeldungenForModule(dataRow, repeatColGroupList, excelRowGroup);
		});

		return excelMerger;
	}

	private void setStammdaten(ExcelMergerDTO excelRowGroup, TagesschuleAnmeldungenDataRow dataRow, Locale locale, Gesuchsperiode gesuchsperiode) {
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.nachnameKind, dataRow.getNachnameKind());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.vornameKind, dataRow.getVornameKind());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.geburtsdatumKind, dataRow.getGeburtsdatum());

		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.fleischOption, ServerMessageUtil.translateEnumValue(
			dataRow.getFleischOption(), locale, gesuchsperiode.getMandant())
		);
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.allergienUndUnvertraeglichkeiten, dataRow.getAllergienUndUnvertraeglichkeiten());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.notfallnummer, dataRow.getNotfallnummer());

		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.vornameAntragsteller1, dataRow.getVornameAntragsteller1());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.nachnameAntragsteller1, dataRow.getNachnameAntragsteller1());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.emailAntragsteller1, dataRow.getEmailAntragsteller1());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.mobileAntragsteller1, dataRow.getMobileAntragsteller1());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.telefonAntragsteller1, dataRow.getTelefonAntragsteller1());

		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.vornameAntragsteller2, dataRow.getVornameAntragsteller2());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.nachnameAntragsteller2, dataRow.getNachnameAntragsteller2());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.emailAntragsteller2, dataRow.getEmailAntragsteller2());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.mobileAntragsteller2, dataRow.getMobileAntragsteller2());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.telefonAntragsteller2, dataRow.getTelefonAntragsteller2());

		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.referenznummer, dataRow.getReferenznummer());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.eintrittsdatum, dataRow.getEintrittsdatum());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.status, ServerMessageUtil.translateEnumValue(
			dataRow.getStatus(), locale, gesuchsperiode.getMandant())
		);
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.abweichung, dataRow.getAbweichung());
		excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.bemerkung, dataRow.getBemerkung());
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
		@Nonnull List<TagesschuleRepeatColGroup> repeatColGroups, @Nonnull Mandant mandant) {

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.generiertAmTitle, ServerMessageUtil.getMessage("Reports_generiertAmTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.generiertAm, LocalDate.now());

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.kindTitle, ServerMessageUtil.getMessage("Reports_kindTitle",	locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.essensOptionTitle, ServerMessageUtil.getMessage("Reports_essensOptionTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.antragsteller1Title, ServerMessageUtil.getMessage("Reports_gesuchsteller1Title",	locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.antragsteller2Title, ServerMessageUtil.getMessage("Reports_gesuchsteller2Title",	locale, mandant));

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.nachnameTitle, ServerMessageUtil.getMessage("Reports_nachnameTitle",	locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.vornameTitle, ServerMessageUtil.getMessage("Reports_vornameTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.geburtsdatumTitle, ServerMessageUtil.getMessage("Reports_geburtsdatumTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.emailTitle, ServerMessageUtil.getMessage("Reports_emailTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.mobileTitle, ServerMessageUtil.getMessage("Reports_mobileTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.telefonTitle, ServerMessageUtil.getMessage("Reports_telefonTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.referenznummerTitle, ServerMessageUtil.getMessage(
			"Reports_bgNummerTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.eintrittsdatumTitle, ServerMessageUtil.getMessage("Reports_eintrittsdatumTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.statusTitle, ServerMessageUtil.getMessage("Reports_statusTitle", locale, mandant));

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.abweichungTitle, ServerMessageUtil.getMessage("Reports_abweichungTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.bemerkungTitle, ServerMessageUtil.getMessage("Reports_bemerkungTitle", locale, mandant));

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.wochentagMo, ServerMessageUtil.getMessage("Reports_MontagShort", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.wochentagDi, ServerMessageUtil.getMessage("Reports_DienstagShort", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.wochentagMi, ServerMessageUtil.getMessage("Reports_MittwochShort", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.wochentagDo, ServerMessageUtil.getMessage("Reports_DonnerstagShort", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.wochentagFr, ServerMessageUtil.getMessage("Reports_FreitagShort", locale, mandant));

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.summeStundenTitle, ServerMessageUtil.getMessage("Reports_summeStundenTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.summeVerpflegungTitle, ServerMessageUtil.getMessage("Reports_summeVerpflegungTitle", locale, mandant));

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.legende, ServerMessageUtil.getMessage("Reports_legende", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.legendeVolleKosten, ServerMessageUtil.getMessage(
			"Reports_legendeVolleKosten", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.legendeZweiwoechentlich, ServerMessageUtil.getMessage(
			"Reports_legendeZweiwoechentlich", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.legendeOhneVerpflegung, ServerMessageUtil.getMessage(
			"Reports_legendeOhneVerpflegung", locale, mandant));

		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.fleischOptionTitle, ServerMessageUtil.getMessage("Reports_fleischOptionTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.allergienUndUnvertraeglichkeitenTitle, ServerMessageUtil.getMessage("Reports_allergienUndUnvertraeglichkeitenTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.notfallnummerTitle, ServerMessageUtil.getMessage("Reports_notfallnummerTitle", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.anzahlBestaetigteAnmeldungen, ServerMessageUtil.getMessage(
			"Reports_anzahlBestaetigteAnmeldungen", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.anzahlAbgelehnteAnmeldungen, ServerMessageUtil.getMessage(
			"Reports_anzahlAbgelehnteAnmeldungen", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.anzahlOffeneAnmeldungen, ServerMessageUtil.getMessage(
			"Reports_anzahlOffeneAnmeldungen", locale, mandant));
		excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.anzahlNichtFreigegebeneAnmeldungen, ServerMessageUtil.getMessage(
			"Reports_anzahlNichtFreigegebeneAnmeldungen", locale, mandant));

		fillModulHeaders(repeatColGroups, excelMerger, locale);
	}

	// Modulnamen, Anzahl Stunden und Verpflegungskosten ausfüllen
	private void fillModulHeaders(List<TagesschuleRepeatColGroup> repeatColGroups, ExcelMergerDTO excelMerger, Locale locale) {
		repeatColGroups.forEach(group -> {
			int counter = Constants.MAX_MODULGROUPS_TAGESSCHULE;
			for (ModulTagesschuleGroup moduleGroup : group.getModulTagesschuleList()) {
				excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.valueOf(group.getRepeatColName()), null);
				excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.modulName,
					moduleGroup.getBezeichnung().findTextByLocale(locale));
				long modulMinutes = Duration.between(moduleGroup.getZeitVon(), moduleGroup.getZeitBis()).toMinutes();
				BigDecimal modulStunden = MathUtil.ZWEI_NACHKOMMASTELLE.divideNullSafe(new BigDecimal(modulMinutes), new BigDecimal(60));
				excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.modulStunden, modulStunden);
				excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.verpflegungskosten, moduleGroup.getVerpflegungskosten());
				counter--;
			}
			// Eine maximale anzahl Spalten wurden im Excel vorbereitet. Diese müssen ausgefüllt leer ausgefüllt
			// werden, damit sie ausgeblendet werden.
			while (counter > 0) {
				excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.modulName, null);
				excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.modulStunden, null);
				excelMerger.addValue(MergeFieldTagesschuleAnmeldungen.verpflegungskosten, null);
				counter--;
			}
		});
	}

	// Anmeldungen für die Module pro Kind ausfüllen
	private void setAnmeldungenForModule(
		@Nonnull TagesschuleAnmeldungenDataRow dataRow,
		@Nonnull List<TagesschuleRepeatColGroup> repeatColGroups,
		@Nonnull ExcelMergerDTO excelRowGroup) {
			repeatColGroups.forEach(weekday -> {
				int counter = Constants.MAX_MODULGROUPS_TAGESSCHULE;
				for (ModulTagesschuleGroup moduleGroup : weekday.getModulTagesschuleList()) {
					for (ModulTagesschule module : moduleGroup.getModule()) {
						if (module.getWochentag() == weekday.getWochentag()) {
							excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.angemeldet, getAnmeldungCode(module, dataRow));
							counter--;
						}
					}
				}
				while (counter > 0) {
					excelRowGroup.addValue(MergeFieldTagesschuleAnmeldungen.angemeldet, null);
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
	private Integer getAnmeldungCode(ModulTagesschule module, TagesschuleAnmeldungenDataRow dataRow) {
		if (dataRow.getAnmeldungTagesschule().getBelegungTagesschule() != null) {
			for (BelegungTagesschuleModul m :
				dataRow.getAnmeldungTagesschule().getBelegungTagesschule().getBelegungTagesschuleModule()) {
				if (m.getModulTagesschule().getId().equals(module.getId())) {
					if (m.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN) {
						return IDENTIFIER_ZWEIWOECHENTLICHES_MODUL;
					}
					return IDENTIFIER_WOECHENTLICHES_MODUL;
				}
			}
		}
		return null;
	}

	public void hideExtraFieldsColumnsIfNecessary(@Nonnull Sheet sheet, Boolean extraFieldAktiviert) {
		if (extraFieldAktiviert) {
			sheet.setColumnHidden(4, true); // column Fleisch Option
			sheet.setColumnHidden(5, true); // column Allergien
			sheet.setColumnHidden(6, true); // column Notfallnummer
		}
	}
}
