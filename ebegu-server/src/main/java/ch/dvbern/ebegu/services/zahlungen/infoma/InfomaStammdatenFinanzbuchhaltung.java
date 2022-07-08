package ch.dvbern.ebegu.services.zahlungen.infoma;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.DIMENSIONSWERT_3_FINANZBUCHHALTUNG;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.KONTONUMMER_FINANZBUCHHALTUNG_ELTERN;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.KONTONUMMER_FINANZBUCHHALTUNG_INSTITUTION;

public class InfomaStammdatenFinanzbuchhaltung extends InfomaStammdaten {

	public InfomaStammdatenFinanzbuchhaltung(
		@NonNull Zahlung zahlung,
		long belegnummer
	) {
		super(zahlung, belegnummer);
	}

	@Nonnull
	public static String with(@NonNull Zahlung zahlung, long belegnummer) {
		InfomaStammdatenFinanzbuchhaltung stammdaten = new InfomaStammdatenFinanzbuchhaltung(zahlung, belegnummer);
		return stammdaten.toString();
	}

	@Override
	@Nonnull
	protected String getKontoart() {
		return InfomaConstants.KONTOART_FINANZBUCHHALTUNG;
	}

	@Override
	@Nonnull
	protected String getKontonummer(@Nonnull Zahlung zahlung) {
		if (zahlung.getZahlungsauftrag().getZahlungslaufTyp() == ZahlungslaufTyp.GEMEINDE_INSTITUTION) {
			return KONTONUMMER_FINANZBUCHHALTUNG_INSTITUTION;
		}
		return KONTONUMMER_FINANZBUCHHALTUNG_ELTERN;
	}

	@Nullable
	@Override
	protected String getDimensionswert3() {
		return DIMENSIONSWERT_3_FINANZBUCHHALTUNG;
	}

	@Nullable
	@Override
	protected String getFaelligkeitsdatum(@Nonnull Zahlung zahlung) {
		return null;
	}
}
