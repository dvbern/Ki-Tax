package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.DATE_FORMAT;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.NEWLINE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.SEPARATOR;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.TIME_FORMAT;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.ZEILENART_HEADER;

public class InfomaHeader {

	private String zeilenart = ZEILENART_HEADER;
	private String herkunft;
	private String datum = LocalDate.now().format(DATE_FORMAT);
	private String zeit = LocalDateTime.now().format(TIME_FORMAT);
	private String benutzer;

	private InfomaHeader(boolean devMode, @Nonnull String benutzer) {
		this.herkunft = "kiBon-" + (devMode ? "DEV" : "PROD");
		this.benutzer = benutzer;
	}

	@Nonnull
	public static String with(boolean devMode, @Nonnull String benutzer) {
		InfomaHeader header = new InfomaHeader(devMode, benutzer);
		return header.toString();
	}

	@Nonnull
	public String toString() {
		String[] args = new String[5];
		args[0] = zeilenart;
		args[1] = StringUtils.abbreviate(herkunft, 20);
		args[2] = datum;
		args[3] = zeit;
		args[4] = StringUtils.abbreviate(benutzer, 20);
		return StringUtils.join(args, SEPARATOR) + NEWLINE;
	}
}
