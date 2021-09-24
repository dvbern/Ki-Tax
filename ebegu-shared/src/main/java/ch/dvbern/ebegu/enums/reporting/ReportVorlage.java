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

package ch.dvbern.ebegu.enums.reporting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.ReportFileName;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;

/**
 * Enum fuer ReportVorlage
 */
public enum ReportVorlage {

	// Achtung mit Filename, da mehrere Dokumente mit gleichem Namen aber unterschiedlichem Inhalt gespeichert werden.
	// Falls der Name geaendert wuerde, muesste das File wieder geloescht werden.
	VORLAGE_REPORT_GESUCH_STICHTAG_DE(
		"/reporting/GesuchStichtag_DE.xlsx",
		ReportFileName.GESUCH_STICHTAG,
		Constants.DATA,
		MergeFieldGesuchStichtag.class
	),
	VORLAGE_REPORT_GESUCH_STICHTAG_FR(
		"/reporting/GesuchStichtag_FR.xlsx",
		ReportFileName.GESUCH_STICHTAG,
		Constants.DATA,
		MergeFieldGesuchStichtag.class
	),
	VORLAGE_REPORT_GESUCH_ZEITRAUM_DE(
		"/reporting/GesuchZeitraum_DE.xlsx",
		ReportFileName.GESUCH_ZEITRAUM,
		Constants.DATA,
		MergeFieldGesuchZeitraum.class
	),
	VORLAGE_REPORT_GESUCH_ZEITRAUM_FR(
		"/reporting/GesuchZeitraum_FR.xlsx",
		ReportFileName.GESUCH_ZEITRAUM,
		Constants.DATA,
		MergeFieldGesuchZeitraum.class
	),
	VORLAGE_REPORT_KANTON(
		"/reporting/Kanton.xlsx",
		ReportFileName.KANTON,
		Constants.DATA,
		MergeFieldKanton.class
	),
	VORLAGE_REPORT_MITARBEITERINNEN(
		"/reporting/Mitarbeiterinnen.xlsx",
		ReportFileName.MITARBEITERINNEN,
		Constants.DATA,
		MergeFieldMitarbeiterinnen.class
	),
	VORLAGE_REPORT_BENUTZER(
		"/reporting/Benutzer.xlsx",
		ReportFileName.BENUTZER,
		Constants.DATA,
		MergeFieldBenutzer.class
	),
	VORLAGE_REPORT_ZAHLUNG_AUFTRAG(
		"/reporting/ZahlungAuftrag.xlsx",
		ReportFileName.ZAHLUNG_AUFTRAG,
		Constants.DATA,
		MergeFieldZahlungAuftrag.class
	),
	VORLAGE_REPORT_ZAHLUNG_AUFTRAG_PERIODE(
		"/reporting/ZahlungAuftragPeriode.xlsx",
		ReportFileName.ZAHLUNG_AUFTRAG_PERIODE,
		Constants.DATA,
		MergeFieldZahlungAuftragPeriode.class
	),
	VORLAGE_REPORT_GESUCHSTELLER_KINDER_BETREUUNG(
		"/reporting/GesuchstellerKinderBetreuung.xlsx",
		ReportFileName.GESUCHSTELLER_KINDER_BETREUUNG,
		Constants.DATA,
		MergeFieldGesuchstellerKinderBetreuung.class
	),
	VORLAGE_REPORT_KINDER(
		"/reporting/Kinder.xlsx",
		ReportFileName.KINDER,
		Constants.DATA,
		MergeFieldGesuchstellerKinderBetreuung.class
	),
	VORLAGE_REPORT_GESUCHSTELLER(
		"/reporting/Gesuchsteller.xlsx",
		ReportFileName.GESUCHSTELLER,
		Constants.DATA,
		MergeFieldGesuchstellerKinderBetreuung.class
	),
	VORLAGE_REPORT_MASSENVERSAND(
		"/reporting/Massenversand.xlsx",
		ReportFileName.MASSENVERSAND,
		Constants.DATA,
		MergeFieldMassenversand.class
	),
	VORLAGE_REPORT_INSTITUTIONEN(
		"/reporting/Institutionen.xlsx",
		ReportFileName.INSTITUTIONEN,
		Constants.DATA,
		MergeFieldInstitutionen.class
	),
	VORLAGE_REPORT_VERRECHNUNG_KIBON(
		"/reporting/VerrechnungKibon.xlsx",
		ReportFileName.VERRECHNUNG_KIBON,
		Constants.DATA,
		MergeFieldVerrechnungKibon.class
	),
	VORLAGE_REPORT_LASTENAUSGLEICH_SELBSTBEHALT(
		"/reporting/LastenausgleichSelbstbehalt.xlsx",
		ReportFileName.LASTENAUSGLEICH_SELBSTBEHALT,
		Constants.DATA,
		MergeFieldLastenausgleichSelbstbehalt.class
	),
	VORLAGE_REPORT_LASTENAUSGLEICH_BERECHNUNG(
		"/reporting/LastenausgleichBerechnung.xlsx",
		ReportFileName.LASTENAUSGLEICH_BERECHNUNG,
		Constants.DATA,
		MergeFieldLastenausgleichBerechnung.class
	),
	VORLAGE_REPORT_ZEMIS(
		"/reporting/KinderMitZemisNummer.xlsx",
		ReportFileName.KINDER_MIT_ZEMIS_NUMMER,
		Constants.DATA,
		MergeFieldZemis.class
	),
	VORLAGE_REPORT_TAGESSCHULE_ANMELDUNGEN(
		"/reporting/TagesschuleAnmeldungen.xlsx",
		ReportFileName.TAGESSCHULE_ANMELDUNGEN,
		Constants.DATA,
		MergeFieldTagesschuleAnmeldungen.class
	),
	VORLAGE_REPORT_TAGESSCHULE_RECHNUNGSSTELLUNG(
		"/reporting/TagesschuleRechnungsstellung.xlsx",
		ReportFileName.TAGESSCHULE_RECHNUNGSSTELLUNG,
		Constants.DATA,
		MergeFieldTagesschuleRechnungsstellung.class
	),
	VORLAGE_REPORT_NOTRECHT(
		"/reporting/Notrecht.xlsx",
		ReportFileName.NOTRECHT,
		Constants.DATA,
		MergeFieldNotrecht.class
	),
	VORLAGE_REPORT_MAHLZEITENVERGUENSTIGUNG(
		"/reporting/Mahlzeitenverguenstigung.xlsx",
		ReportFileName.MAHLZEITENVERGUENSTIGUNG,
		Constants.DATA,
		MergeFieldMahlzeitenverguenstigung.class
	),
	VORLAGE_REPORT_GEMEINDEN(
			"/reporting/Gemeinden.xlsx",
			ReportFileName.GEMEINDEN,
			Constants.DATA,
			MergeFieldGemeinden.class
	),
	VORLAGE_REPORT_FERIENBETREUUNG(
		"/reporting/Ferienbetreuung.xlsx",
		ReportFileName.FERIENBETREUUNG,
		Constants.DATA,
		MergeFieldFerienbetreuung.class
	);

	@Nonnull
	private final String templatePath;
	@Nonnull
	private final ReportFileName defaultExportFilename;
	@Nonnull
	private final Class<? extends MergeFieldProvider> mergeFields;
	@Nonnull
	private final String dataSheetName;

	ReportVorlage(@Nonnull String templatePath, @Nonnull ReportFileName defaultExportFilename,
		@Nonnull String dataSheetName, @Nonnull Class<? extends MergeFieldProvider> mergeFields) {
		this.templatePath = templatePath;
		this.defaultExportFilename = defaultExportFilename;
		this.mergeFields = mergeFields;
		this.dataSheetName = dataSheetName;
	}

	@Nonnull
	public String getTemplatePath() {
		return templatePath;
	}

	@Nonnull
	public ReportFileName getDefaultExportFilename() {
		return defaultExportFilename;
	}

	@Nonnull
	public MergeFieldProvider[] getMergeFields() {
		return mergeFields.getEnumConstants();
	}

	@Nonnull
	public String getDataSheetName() {
		return dataSheetName;
	}

	public static boolean checkAllowed(@Nullable UserRole role, ReportVorlage vorlage) {
		if (role == null) {
			return false;
		}

		if (UserRole.getInstitutionTraegerschaftAdminRoles().contains(role)) {
			return vorlage == VORLAGE_REPORT_KINDER || vorlage == VORLAGE_REPORT_KANTON
				|| vorlage == VORLAGE_REPORT_BENUTZER  || vorlage == VORLAGE_REPORT_TAGESSCHULE_ANMELDUNGEN;
		}

		if (UserRole.getInstitutionTraegerschaftRoles().contains(role)) {
			return vorlage == VORLAGE_REPORT_KINDER || vorlage == VORLAGE_REPORT_KANTON  || vorlage == VORLAGE_REPORT_TAGESSCHULE_ANMELDUNGEN;
		}

		if (UserRole.getTsOnlyRoles().contains(role)) {
			return vorlage == VORLAGE_REPORT_GESUCH_STICHTAG_DE || vorlage == VORLAGE_REPORT_GESUCH_STICHTAG_FR
				|| vorlage == VORLAGE_REPORT_GESUCH_ZEITRAUM_DE || vorlage == VORLAGE_REPORT_GESUCH_ZEITRAUM_FR
				|| vorlage == VORLAGE_REPORT_KINDER || vorlage == VORLAGE_REPORT_GESUCHSTELLER
				|| vorlage == VORLAGE_REPORT_BENUTZER || vorlage == VORLAGE_REPORT_MASSENVERSAND
				|| vorlage == VORLAGE_REPORT_INSTITUTIONEN || vorlage == VORLAGE_REPORT_TAGESSCHULE_ANMELDUNGEN
				|| vorlage == VORLAGE_REPORT_TAGESSCHULE_RECHNUNGSSTELLUNG;
		}

		return UserRole.GESUCHSTELLER != role && UserRole.STEUERAMT != role && UserRole.JURIST != role;
	}
}
