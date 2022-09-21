package ch.dvbern.ebegu.services.zahlungen.infoma;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.BANKCODE;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.DIMENSIONSWERT_3_FINANZBUCHHALTUNG;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.KONTONUMMER_FINANZBUCHHALTUNG_ELTERN;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.KONTONUMMER_FINANZBUCHHALTUNG_INSTITUTION;
import static ch.dvbern.ebegu.services.zahlungen.infoma.InfomaConstants.decimalFormat;

public class InfomaStammdatenFinanzbuchhaltung extends InfomaStammdaten {

	private InfomaStammdatenFinanzbuchhaltung(
		@NonNull Zahlung zahlung,
		long belegnummer,
		Locale locale
	) {
		super(zahlung, belegnummer, locale);
	}

	@Nonnull
	public static String with(@NonNull Zahlung zahlung, long belegnummer, Locale locale) {
		InfomaStammdatenFinanzbuchhaltung stammdaten = new InfomaStammdatenFinanzbuchhaltung(zahlung, belegnummer, locale);
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

	@Nonnull
	@Override
	protected String getBankCode(@Nonnull Zahlung zahlung) {
		return BANKCODE;
	}

	@Nullable
	@Override
	protected String getDimensionswert3() {
		return DIMENSIONSWERT_3_FINANZBUCHHALTUNG;
	}

	@Nonnull
	@Override
	protected String getBetrag(@Nonnull Zahlung zahlung) {
		return decimalFormat().format(zahlung.getBetragTotalZahlung());
	}

	@Nullable
	@Override
	protected String getFaelligkeitsdatum(@Nonnull Zahlung zahlung) {
		return null;
	}
}
