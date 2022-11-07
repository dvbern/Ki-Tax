package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.util.List;

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

	public boolean calculateIgnorable(
			List<VerfuegungZeitabschnitt> zeitabschnitteAktuell,
			Verfuegung verfuegung,
			BigDecimal veraenderung) {
		if (veraenderung.compareTo(BigDecimal.ZERO) >= 0) {
			return false;
		}

		if (zeitabschnitteAktuell.size() != verfuegung.getZeitabschnitte().size()) {
			return false;
		}

		for (int i = 0; i < zeitabschnitteAktuell.size(); i++) {
			VerfuegungZeitabschnitt currentZeitabschnitt = zeitabschnitteAktuell.get(0);
			VerfuegungZeitabschnitt verfuegungZeitabschnitt = verfuegung.getZeitabschnitte().get(0);

			if (!currentZeitabschnitt.getGueltigkeit().equals(verfuegungZeitabschnitt.getGueltigkeit())) {
				return false;
			}

			if (!currentZeitabschnitt.getRelevantBgCalculationResult().differsIgnorableFrom(verfuegungZeitabschnitt.getRelevantBgCalculationResult())) {
				return false;
			}
		}


		return true;
	}
}
