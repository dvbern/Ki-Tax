package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

public class VeraenderungBetreuungsgutscheinCalculator extends VeraenderungCalculator {

	@Override
	public BigDecimal calculateVeraenderung(
		@NotNull List<VerfuegungZeitabschnitt> zeitabschnitte,
		@NotNull Verfuegung vorgaengerVerfuegung) {

		BigDecimal totalVerguenstigungVorgaenger = addUpVerguenstigung(vorgaengerVerfuegung.getZeitabschnitte());
		BigDecimal totalVerguenstigungAktuell = addUpVerguenstigung(zeitabschnitte);

		return totalVerguenstigungAktuell.subtract(totalVerguenstigungVorgaenger);
	}

	@Override
	protected boolean isVerfuegungIgnorable(BigDecimal veraenderung) {
		//Wenn der Betreuungsgutschein sinkt, darf die Verfügung ignoriert werden (BG Veränderung zu Ungusten der Eltern)
		return veraenderung.compareTo(BigDecimal.ZERO) <= 0;
	}

	private BigDecimal addUpVerguenstigung(List<VerfuegungZeitabschnitt> zeitabschnitte) {
		return zeitabschnitte.stream()
			.map(zeitabschnitt -> zeitabschnitt.getRelevantBgCalculationResult().getVerguenstigung())
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
