package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
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
		if (getZahlung().getZahlungsauftrag().getZahlungslaufTyp() == ZahlungslaufTyp.GEMEINDE_INSTITUTION) {
			// TODO hier muss die infomaKreditorennummer rein!
			return getZahlung().getAuszahlungsdaten().getIban().getIban();
		} else {
			// TODO ist es automatsich schon immer diejenige des empfängers?
			return getZahlung().getAuszahlungsdaten().getIban().getIban();
		}
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
