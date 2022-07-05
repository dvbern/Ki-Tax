package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Zahlung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class InfomaStammdatenZahlung extends InfomaStammdaten {

	public InfomaStammdatenZahlung(
		@NonNull Zahlung zahlung,
		@NonNull String belegnummer
	) {
		super(zahlung, belegnummer);
	}

	@Nonnull
	public static String with(@NonNull Zahlung zahlung, @NonNull String belegnummer) {
		InfomaStammdatenZahlung stammdaten = new InfomaStammdatenZahlung(zahlung, belegnummer);
		return stammdaten.toString();
	}

	@Override
	@Nonnull
	protected String getKontoart() {
		return InfomaConstants.KONTOART_ZAHLUNG;
	}

	@Override
	@Nonnull
	protected String getKontonummer() {
		final String infomaKontonummer = getZahlung().getAuszahlungsdaten().getInfomaKreditorennummer();
		Objects.requireNonNull(infomaKontonummer);
		return infomaKontonummer;
	}

	@Nullable
	@Override
	protected String getDimensionswert3() {
		return null;
	}

	@Nullable
	@Override
	protected LocalDate getFaelligkeitsdatum() {
		return getZahlung().getZahlungsauftrag().getDatumFaellig();
	}
}
