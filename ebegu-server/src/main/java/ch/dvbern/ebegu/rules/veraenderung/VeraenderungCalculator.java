package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

public abstract class VeraenderungCalculator {

	public static VeraenderungCalculator getVeranderungCalculator(boolean isTagesschule) {
		if (isTagesschule) {
			return new VeraenderungTagesschuleTarifCalculator();
		}

		return new VeraenderungBetreuungsgutscheinCalculator();
	}

	public abstract BigDecimal calculateVeraenderung(
		@NotNull List<VerfuegungZeitabschnitt> zeitabschnitte,
		@NotNull Verfuegung vorgaengerVerfuegung);
}
