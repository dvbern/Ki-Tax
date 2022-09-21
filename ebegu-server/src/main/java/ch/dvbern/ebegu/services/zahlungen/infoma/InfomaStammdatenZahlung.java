package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Zahlung;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.DATE_FORMAT;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.decimalFormat;

public class InfomaStammdatenZahlung extends InfomaStammdaten {

	private InfomaStammdatenZahlung(
		@NonNull Zahlung zahlung,
		long belegnummer,
		Locale locale
	) {
		super(zahlung, belegnummer, locale);
	}

	@Nonnull
	public static String with(@NonNull Zahlung zahlung, long belegnummer, Locale locale) {
		InfomaStammdatenZahlung stammdaten = new InfomaStammdatenZahlung(zahlung, belegnummer, locale);
		return stammdaten.toString();
	}

	@Override
	@Nonnull
	protected String getKontoart() {
		return InfomaConstants.KONTOART_ZAHLUNG;
	}

	@Override
	@Nonnull
	protected String getKontonummer(@Nonnull Zahlung zahlung) {
		final String infomaKontonummer = zahlung.getAuszahlungsdaten().getInfomaKreditorennummer();
		Objects.requireNonNull(infomaKontonummer);
		return infomaKontonummer;
	}

	@Nonnull
	@Override
	protected String getBankCode(@Nonnull Zahlung zahlung) {
		final String infomaBankcode = zahlung.getAuszahlungsdaten().getInfomaBankcode();
		Objects.requireNonNull(infomaBankcode);
		return infomaBankcode;
	}

	@Nullable
	@Override
	protected String getDimensionswert3() {
		return null;
	}

	@Nonnull
	@Override
	protected String getBetrag(@Nonnull Zahlung zahlung) {
		return decimalFormat().format(zahlung.getBetragTotalZahlung().negate());
	}

	@Nullable
	@Override
	protected String getFaelligkeitsdatum(@Nonnull Zahlung zahlung) {
		final LocalDate datumFaellig = zahlung.getZahlungsauftrag().getDatumFaellig();
		return datumFaellig != null
			? DATE_FORMAT.format(datumFaellig)
			: "";
	}
}
