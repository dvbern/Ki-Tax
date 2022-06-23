package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

public class InfomaHeader {

	private static final String ZEILENART = "0";
	private final boolean devMode;
	private final LocalDateTime timestamp = LocalDateTime.now();
	private final String benutzer;

	public InfomaHeader(@Nonnull boolean devMode, @Nonnull String benutzer) {
		this.devMode = devMode;
		this.benutzer = benutzer;
	}

	@Nonnull
	public String toString() {
		String[] args = new String[5];
		args[0] = ZEILENART;
		args[1] = "kiBon-" + (devMode ? "DEV" : "PROD");
		args[2] = timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		args[3] = timestamp.format(DateTimeFormatter.ofPattern("HHmm"));
		args[4] = benutzer;
		return StringUtils.join(args, "|") + "/n";
	}
}
