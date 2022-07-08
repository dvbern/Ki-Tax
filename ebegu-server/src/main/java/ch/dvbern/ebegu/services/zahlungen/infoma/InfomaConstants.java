package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;

import static java.util.Locale.GERMAN;

public final class InfomaConstants {

	public static final String NEWLINE = "\n";
	public static final String SEPARATOR = "|";

	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

	public static final String ZEILENART_HEADER = "0";
	public static final String ZEILENART_FOOTER = "9";
	public static final String ZEILENART_STAMMDATEN = "1";

	public static final String STAMMDATEN_BELEGART = "2";
	public static final String KONTOART_ZAHLUNG = "2";
	public static final String KONTOART_FINANZBUCHHALTUNG = "0";

	public static final String KONTONUMMER_FINANZBUCHHALTUNG_INSTITUTION = "3636,022";
	public static final String KONTONUMMER_FINANZBUCHHALTUNG_ELTERN = "3637,014";

	public static final String BUCHUNGSKREIS = "1";
	public static final String INSTITUTIONELLE_GLIEDERUNG = "215";
	public static final String DIMENSIONSWERT_3_FINANZBUCHHALTUNG = "2158303";
	public static final String BANKCODE = "RB IBAN";

	public static final String BELEGNUMMER_PRAEFIX = "BGR";

	private InfomaConstants() {
	}

	@Nonnull
	public static DecimalFormat decimalFormat() {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(GERMAN);
		symbols.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setGroupingUsed(false);
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setDecimalFormatSymbols(symbols);
		return decimalFormat;
	}
}
