package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import org.junit.Assert;
import org.junit.Test;


public class VeraenderungBetreuungsgutscheinCalculatorTest {

	@Test
	public void useCorrectImplementation() {
		VeraenderungCalculator veraenderungCalculator = VeraenderungCalculator.getVeranderungCalculator(false);
		Assert.assertTrue(veraenderungCalculator instanceof  VeraenderungBetreuungsgutscheinCalculator);
	}

	@Test
	public void veraenderung() {
		List<VerfuegungZeitabschnitt> zeitaschnitteAktuell = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(10)),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(20))
		);

		List<VerfuegungZeitabschnitt> zeitaschnitteVorgaenger = Arrays.asList(
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(15)),
			createZeitabschnittMitVergunstigung(BigDecimal.valueOf(25))
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zeitaschnitteVorgaenger);

		BigDecimal veraenderung = VeraenderungCalculator
			.getVeranderungCalculator(false)
			.calculateVeraenderung(zeitaschnitteAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(10), veraenderung);
	}

	private VerfuegungZeitabschnitt createZeitabschnittMitVergunstigung(BigDecimal verguenstiung) {
		BGCalculationResult bgCalculationResult = new BGCalculationResult();
		bgCalculationResult.setVerguenstigung(verguenstiung);
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setBgCalculationResultAsiv(bgCalculationResult);
		return zeitabschnitt;
	}


}
