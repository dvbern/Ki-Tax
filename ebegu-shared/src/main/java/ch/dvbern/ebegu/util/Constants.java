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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Interface fuer Konstanten.
 */
public final class Constants {

	// 100% = 20 days => 1% = 0.2 days
	public static final BigDecimal MULTIPLIER_KITA = MathUtil.EINE_NACHKOMMASTELLE.from(0.2);
	// 100% = 220 hours => 1% = 2.2 hours
	public static final BigDecimal MULTIPLIER_TAGESFAMILIEN = MathUtil.EINE_NACHKOMMASTELLE.from(2.2);

	public static final int DB_DEFAULT_MAX_LENGTH = 255;
	public static final int DB_TEXTAREA_LENGTH = 4000;
	public static final int DB_RUECKFORDERUNGSMITTEILUNG_LENGTH = 10000;
	public static final int DB_DEFAULT_SHORT_LENGTH = 100;
	public static final int ONE_MB = 1048576;
	public static final int TEN_MB = 10485760;

	public static final int UUID_LENGTH = 36;

	public static final int LOGIN_TIMEOUT_SECONDS = 4 * 60 * 60; //aktuell 4h

	public static final int MAX_TIMEOUT_MINUTES = 360; // minutes
	public static final int STATISTIK_TIMEOUT_MINUTES = 180; // minutes

	public static final String LOG_MDC_EBEGUUSER = "ebeguuser";
	public static final String LOG_MDC_AUTHUSERID = "ebeguauthuserid";

	public static final Locale DEFAULT_LOCALE = new Locale("de", "CH");
	public static final Locale DEUTSCH_LOCALE = DEFAULT_LOCALE;
	public static final Locale FRENCH_LOCALE = new Locale("fr", "CH");

	public static final Character LINE_BREAK = '\n';
	public static final String DATA = "Data";
	public static final String REGEX_UUID = "(^.*)([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})(.*$)";
	public static final String REGEX_EMAIL = "[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}";
	public static final String REGEX_TELEFON = "(0|\\+41|0041)[ ]*[\\d]{2}[ ]*[\\d]{3}[ ]*[\\d]{2}[ ]*[\\d]{2}";
	public static final String REGEX_TELEFON_MOBILE = "(0|\\+41|0041)[ ]*(74|75|76|77|78|79)[ ]*[\\d]{3}[ ]*[\\d]{2}[ ]*[\\d]{2}";
	public static final String REGEX_URL =
		"^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
	public static final String REGEX_ZEMIS = "^[0-9]{8}\\.[0-9]$";
	public static final String PATTERN_DATE = "dd.MM.yyyy";
	public static final String PATTERN_FILENAME_DATE_TIME = "dd.MM.yyyy_HH.mm.ss";
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(Constants.PATTERN_DATE);
	public static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(Constants.PATTERN_FILENAME_DATE_TIME);

	public static final String SQL_PATTERN_DATE = "yyyy-MM-dd";
	public static final DateTimeFormatter SQL_DATE_FORMAT = DateTimeFormatter.ofPattern(SQL_PATTERN_DATE);

	public static final String HOURS_PATTERN = "HH:mm";
	public static final DateTimeFormatter HOURS_FORMAT = DateTimeFormatter.ofPattern(HOURS_PATTERN);

	public static final String CURRENCY_PATTERN = "#,##0.00";
	public static final DecimalFormatSymbols SYMBOLS_DE_CH = new DecimalFormatSymbols(new Locale("de", "CH"));
	public static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat(CURRENCY_PATTERN, SYMBOLS_DE_CH);

	public static final String SERVER_MESSAGE_BUNDLE_NAME = "ch.dvbern.ebegu.i18n.server-messages";
	public static final String VALIDATION_MESSAGE_BUNDLE_NAME = "ValidationMessages";
	public static final LocalDate END_OF_TIME = LocalDate.of(9999, 12, 31);
	public static final LocalDate START_OF_TIME = LocalDate.of(1000, 1, 1);

	public static final LocalDate GESUCHSPERIODE_17_18_AB = LocalDate.of(2017, 8, 1);
	public static final LocalDate GESUCHSPERIODE_17_18_BIS = LocalDate.of(2018, 7, 31);
	public static final DateRange GESUCHSPERIODE_17_18 = new DateRange(Constants.GESUCHSPERIODE_17_18_AB, Constants.GESUCHSPERIODE_17_18_BIS);

	public static final LocalDate GESUCHSPERIODE_18_19_AB = LocalDate.of(2018, 8, 1);
	public static final LocalDate GESUCHSPERIODE_18_19_BIS = LocalDate.of(2019, 7, 31);
	public static final DateRange GESUCHSPERIODE_18_19 = new DateRange(Constants.GESUCHSPERIODE_18_19_AB, Constants.GESUCHSPERIODE_18_19_BIS);

	public static final LocalDateTime START_OF_DATETIME = LocalDateTime.of(1000, 1, 1, 0, 0, 0);

	public static final DateRange DEFAULT_GUELTIGKEIT = new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME);

	public static final long MAX_SHORT_TEMP_DOWNLOAD_AGE_MINUTES = 3L;
	public static final long MAX_LONGER_TEMP_DOWNLOAD_AGE_MINUTES = 1440L; //24 * 60
	public static final int FALLNUMMER_LENGTH = 6;
	public static final int GEMEINDENUMMER_LENGTH = 3;
	public static final long MAX_LUCENE_QUERY_RUNTIME = 500L;

	public static final int MAX_LUCENE_QUICKSEARCH_RESULTS = 25; // hier gibt es ein Problem, wenn wir fuer keines der Resultate berechtigt sind wird unser resultset leer sein auf client

	public static final String DEFAULT_MANDANT_ID = "e3736eb8-6eef-40ef-9e52-96ab48d8f220";
	public static final String AUTH_TOKEN_SUFFIX_FOR_NO_TOKEN_REFRESH_REQUESTS = "NO_REFRESH";
	public static final String PATH_DESIGNATOR_NO_TOKEN_REFRESH = "notokenrefresh";

	public static final String TEMP_REPORT_FOLDERNAME = "tempReports";
	public static final String TEMP_NOTVERORDNUNG = "tempNotverordnung";

	public static final String SYSTEM_USER_USERNAME = "System";
	public static final String ANONYMOUS_USER_USERNAME = "anonymous";
	public static final String LOGINCONNECTOR_USER_USERNAME = "LoginConnector";
	public static final String UNKNOWN = "UNKNOWN";

	public static final String PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS = "3800";
	public static final String PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS = "6000";
	public static final String PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS = "7000";
	public static final String PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS = "7700";

	public static final int ZUSCHLAG_ERWERBSPENSUM_FUER_TESTS = 20;

	public static final int DAYS_BEFORE_INSTITUTION_CHECK = 100;

	public static final String UNKNOWN_INSTITUTION_NAME = "";

	// ID der statischen, unbekannten Institution Stammdaten. Wird verwendet um eine provisorische Berechnung zu generieren
	// und darf dem Benutzer <b>nie>/b> angezeigt werden
	public static final String ID_UNKNOWN_INSTITUTION_STAMMDATEN_KITA = "00000000-0000-0000-0000-000000000000";
	public static final String ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESFAMILIE = "00000000-0000-0000-0000-000000000001";
	public static final String ID_UNKNOWN_INSTITUTION_STAMMDATEN_TAGESSCHULE= "00000000-0000-0000-0000-000000000002";

	public static final String CSV_DELIMITER = ";";
	public static final String CSV_NEW_LINE = "\n";

	public static final int MAX_MODULGROUPS_TAGESSCHULE = 20;
	public static final int LATS_NUMBER_WEEKS_PER_YEAR = 39;

	private Constants() {
		//this prevents even the native class from
		//calling this ctor as well :
		throw new AssertionError();
	}

	// Alle diese Werte sollten nicht hier sein, da sie nur fuer Tests verwendet werden duerfen
	// Sollte ins TestDataUtil verschoben werden oder in ein TestConstants, damit sie nicht aus
	// Versehen in produktivem Code verwendet werden
	public interface EinstellungenDefaultWerteAsiv {
		BigDecimal MAX_EINKOMMEN = MathUtil.DEFAULT.fromNullSafe(160000);
		String EINSTELLUNG_MAX_EINKOMMEN = String.valueOf(MAX_EINKOMMEN);
		String PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3 = "3800";
		String PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4 = "6000";
		String PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5 = "7000";
		String PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6 = "7700";
		String EINSTELLUNG_MAX_TAGE_ABWESENHEIT = "30";
		String EINSTELLUNG_BG_BIS_UND_MIT_SCHULSTUFE = EinschulungTyp.VORSCHULALTER.name();
		int MAX_ERWERBSPENSUM_FREIWILLIGENARBEIT = 0;
		int MIN_ERWERBSPENSUM_NICHT_EINGESCHULT = 20;
		String EINSTELLUNG_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT = String.valueOf(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT);
		int MIN_ERWERBSPENSUM_EINGESCHULT = 40;
		String EINSTELLUNG_MIN_ERWERBSPENSUM_EINGESCHULT = String.valueOf(MIN_ERWERBSPENSUM_EINGESCHULT);
		int ZUSCHLAG_ERWERBSPENSUM = 20;
		String EINSTELLUNG_ZUSCHLAG_ERWERBSPENSUM = String.valueOf(ZUSCHLAG_ERWERBSPENSUM);
	}
}
