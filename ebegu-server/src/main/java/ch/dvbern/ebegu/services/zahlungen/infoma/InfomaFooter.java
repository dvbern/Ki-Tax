package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.NEWLINE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.SEPARATOR;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.ZEILENART_FOOTER;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.decimalFormat;

public class InfomaFooter {

	private final String zeilenart = ZEILENART_FOOTER;
	private final String anzahlBuchungen;
	private final String summeAllerBuchungen;

	private InfomaFooter(int anzahlBuchungen, @Nonnull BigDecimal summeAllerBuchungen) {
		this.anzahlBuchungen = String.valueOf(anzahlBuchungen);
		this.summeAllerBuchungen = decimalFormat().format(summeAllerBuchungen);
	}

	@Nonnull
	public static String with(int anzahlBuchungen, @Nonnull BigDecimal summeAllerBuchungen) {
		InfomaFooter footer = new InfomaFooter(anzahlBuchungen, summeAllerBuchungen);
		return footer.toString();
	}

	@Nonnull
	public String toString() {
		String[] args = new String[3];
		args[0] = zeilenart;
		args[1] = anzahlBuchungen;
		args[2] = summeAllerBuchungen;
		return StringUtils.join(args, SEPARATOR) + NEWLINE;
	}
}
