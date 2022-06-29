package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.NEWLINE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.SEPARATOR;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.ZEILENART_FOOTER;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.decimalFormat;

public class InfomaFooter {

	private final int anzahlBuchungen;
	private final BigDecimal summeAllerBuchungen;

	private InfomaFooter(int anzahlBuchungen, @Nonnull BigDecimal summeAllerBuchungen) {
		this.anzahlBuchungen = anzahlBuchungen;
		this.summeAllerBuchungen = summeAllerBuchungen;
	}

	@Nonnull
	public static String with(int anzahlBuchungen, @Nonnull BigDecimal summeAllerBuchungen) {
		InfomaFooter footer = new InfomaFooter(anzahlBuchungen, summeAllerBuchungen);
		return footer.toString();
	}

	@Nonnull
	public String toString() {
		String[] args = new String[3];
		args[0] = ZEILENART_FOOTER;
		args[1] = String.valueOf(anzahlBuchungen);
		args[2] = decimalFormat().format(summeAllerBuchungen);
		return StringUtils.join(args, SEPARATOR) + NEWLINE;
	}
}
