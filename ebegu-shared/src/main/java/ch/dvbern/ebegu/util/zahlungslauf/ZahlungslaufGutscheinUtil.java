package ch.dvbern.ebegu.util.zahlungslauf;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.util.MathUtil;

public final class ZahlungslaufGutscheinUtil {

	private ZahlungslaufGutscheinUtil() {
	}

	@Nonnull
	public static BigDecimal getAuszahlungsbetrag(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		return zeitabschnitt.getVerguenstigung();
	}

	public static boolean isSameAusbezahlterBetrag(
		@Nonnull BGCalculationResult resultNeu,
		@Nonnull BGCalculationResult resultBisher
	) {
		return MathUtil.isSame(resultNeu.getVerguenstigung(), resultBisher.getVerguenstigung());
	}

	public static boolean isSamePersistedValues(
		@Nonnull VerfuegungZeitabschnitt abschnitt,
		@Nonnull VerfuegungZeitabschnitt otherAbschnitt
	) {
		// Im Fall der Institutionszahlungen koennen wir die "normale" Berechnung von isSamePersistedValues verwenden:
		return abschnitt.isSamePersistedValues(otherAbschnitt);
	}
}
