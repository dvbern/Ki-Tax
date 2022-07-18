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
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaUtil.normalizeAndAbbreviate;

public class InfomaHeader {

	private final String herkunft;
	private final String datum;
	private final String zeit;
	private final String benutzer;

	private InfomaHeader(boolean devMode, @Nonnull String benutzer) {
		this.herkunft = "kiBon-" + (devMode ? "DEV" : "PROD");
		this.datum = LocalDate.now().format(DATE_FORMAT);
		this.zeit = LocalDateTime.now().format(TIME_FORMAT);
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
		args[0] = ZEILENART_HEADER;
		args[1] = normalizeAndAbbreviate(herkunft, 20);
		args[2] = datum;
		args[3] = zeit;
		args[4] = normalizeAndAbbreviate(benutzer, 20);
		return StringUtils.join(args, SEPARATOR) + NEWLINE;
	}
}
