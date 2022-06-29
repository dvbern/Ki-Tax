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

	private InfomaConstants() {
	}

	@Nonnull
	public static DecimalFormat decimalFormat() {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(GERMAN);
		symbols.setDecimalSeparator(',');
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setGroupingUsed(false);
		decimalFormat.setMinimumFractionDigits(2);
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setDecimalFormatSymbols(symbols);
		return decimalFormat;
	}
}
