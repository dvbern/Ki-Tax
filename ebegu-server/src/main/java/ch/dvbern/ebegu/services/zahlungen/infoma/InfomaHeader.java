package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.DATE_FORMAT;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.NEWLINE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.SEPARATOR;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.TIME_FORMAT;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.ZEILENART_HEADER;

public class InfomaHeader {

	private final boolean devMode;
	private final String benutzer;

	private InfomaHeader(boolean devMode, @Nonnull String benutzer) {
		this.devMode = devMode;
		this.benutzer = benutzer;
	}

	@Nonnull
	public static String with(boolean devMode, @Nonnull String benutzer) {
		InfomaHeader header = new InfomaHeader(devMode, benutzer);
		return header.toString();
	}

	@Nonnull
	public String toString() {
		LocalDateTime timestamp = LocalDateTime.now();

		String[] args = new String[5];
		args[0] = ZEILENART_HEADER;
		args[1] = "kiBon-" + (devMode ? "DEV" : "PROD");
		args[2] = timestamp.format(DATE_FORMAT);
		args[3] = timestamp.format(TIME_FORMAT);
		args[4] = benutzer;
		return StringUtils.join(args, SEPARATOR) + NEWLINE;
	}
}
