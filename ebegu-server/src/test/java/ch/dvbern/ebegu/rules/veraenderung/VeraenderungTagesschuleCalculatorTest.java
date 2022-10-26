package ch.dvbern.ebegu.rules.veraenderung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.Assert;
import org.junit.Test;

public class VeraenderungTagesschuleCalculatorTest {

	private final VeraenderungCalculator veraenderungCalculator =
		VeraenderungCalculator.getVeranderungCalculator(true);

	private final DateRange gueltigkeitAugust = new DateRange(
		LocalDate.of(2022, Month.AUGUST, 1),
		LocalDate.of(2022, Month.AUGUST, 31)
	);

	private final DateRange gueltigkeitSeptember = new DateRange(
		LocalDate.of(2022, Month.SEPTEMBER, 1),
		LocalDate.of(2022, Month.SEPTEMBER, 30)
	);

	private final DateRange gueltigkeit2022 = new DateRange(
		LocalDate.of(2022, Month.JANUARY, 1),
		LocalDate.of(2022, Month.DECEMBER, 31)
	);

	@Test
	public void useCorrectImplementation() {
		Assert.assertTrue(veraenderungCalculator instanceof  VeraenderungTagesschuleTarifCalculator);
	}

	@Test
	public void einZeitabschnitt_EinTarif_MitBetruung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarif(BigDecimal.valueOf(3.5), null));

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarif(BigDecimal.valueOf(3), null));

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-0.5), veranderung);
	}

	@Test
	public void einZeitabschnitt_EinTarif_OhneBetruung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarif(null, BigDecimal.valueOf(3.5)));

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarif(null, BigDecimal.valueOf(3)));

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-0.5), veranderung);
	}

	@Test
	public void einZeitabschnitt_TarifAktuell0_MitBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarif(BigDecimal.ZERO, null));

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarif(BigDecimal.valueOf(3), null));

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(3), veranderung);
	}

	@Test
	public void einZeitabschnitt_Vorgaenger0_MitBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarif(BigDecimal.valueOf(2.5), null));

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarif(BigDecimal.ZERO, null));

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-2.5), veranderung);
	}

	@Test
	public void einZeitabschnitt_AktuellNull_MitBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarif(null, null));

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarif(BigDecimal.valueOf(2.5), null));

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(2.5), veranderung);
	}

	@Test
	public void einZeitabschnitt_VorgaengerNull_MitBetruung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarif(BigDecimal.valueOf(2.5), null));

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarif(null, null));

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-2.5), veranderung);
	}

	@Test
	public void einZeitabschnitt_AktuellNull_OhneBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarif(null, null));

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarif(null, BigDecimal.valueOf(2.5)));

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(2.5), veranderung);
	}

	@Test
	public void einZeitabschnitt_VorgaengerNull_OhneBetruung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarif(null, BigDecimal.valueOf(2.5)));

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarif(null,null));

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-2.5), veranderung);
	}

	@Test
	public void multipleZeitabschnitte_sameGueltigkeit_positiveVeraenderung_MitBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, BigDecimal.valueOf(1.2), null),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, BigDecimal.valueOf(3.7), null)
		);

		List<VerfuegungZeitabschnitt> zaVorgaenger = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, BigDecimal.valueOf(3.3), null),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, BigDecimal.valueOf(3.5), null)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(2.1), veranderung);
	}

	@Test
	public void multipleZeitabschnitte_sameGueltigkeit_positiveVeraenderung_OhneBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, null, BigDecimal.valueOf(1.2)),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, null, BigDecimal.valueOf(3.7))
		);

		List<VerfuegungZeitabschnitt> zaVorgaenger = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, null, BigDecimal.valueOf(3.3)),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, null, BigDecimal.valueOf(3.5))
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(2.1), veranderung);
	}

	@Test
	public void multipleZeitabschnitte_sameGueltigkeit_negativeVeraenderung_MitBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, BigDecimal.valueOf(3.3), null),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, BigDecimal.valueOf(3.7), null)
		);

		List<VerfuegungZeitabschnitt> zaVorgaenger = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, BigDecimal.valueOf(1.2), null),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, BigDecimal.valueOf(3.5), null)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-2.1), veranderung);
	}

	@Test
	public void multipleZeitabschnitte_differentGueltigkitenAktuell_MitBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, BigDecimal.valueOf(3.3), null),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, BigDecimal.valueOf(3.8), null)
		);

		List<VerfuegungZeitabschnitt> zaVorgaenger = List.of(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeit2022, BigDecimal.valueOf(3.5), null)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(-0.3), veranderung);
	}

	@Test
	public void multipleZeitabschnitte_differentGueltigkitenInVorganger_MitBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = List.of(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeit2022, BigDecimal.valueOf(3.5), null)
		);

		List<VerfuegungZeitabschnitt> zaVorgaenger = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, BigDecimal.valueOf(3.3), null),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, BigDecimal.valueOf(3.8), null)
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(0.3), veranderung);
	}

	@Test
	public void multipleZeitabschnitte_MitBetreuung_OhneBetreuung() {
		List<VerfuegungZeitabschnitt> zaAktuell = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, BigDecimal.valueOf(1.2), BigDecimal.valueOf(2.7)),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, BigDecimal.valueOf(3.7), BigDecimal.valueOf(0.5))
		);

		List<VerfuegungZeitabschnitt> zaVorgaenger = Arrays.asList(
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitAugust, BigDecimal.valueOf(3.1), BigDecimal.valueOf(2.2)),
			createZeitabschnittMitTarifAndGueltigkeit(gueltigkeitSeptember, BigDecimal.valueOf(3.5), BigDecimal.valueOf(1.6))
		);

		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(zaVorgaenger);

		BigDecimal veranderung = veraenderungCalculator.calculateVeraenderung(zaAktuell, verfuegung);

		Assert.assertEquals(BigDecimal.valueOf(1.9), veranderung);
	}

	private VerfuegungZeitabschnitt createZeitabschnittMitTarif(@Nullable BigDecimal tarifMitBetreuung, @Nullable BigDecimal tarifOhneBetreuung) {
		BGCalculationResult bgCalculationResult = new BGCalculationResult();

		if (tarifMitBetreuung != null) {
			TSCalculationResult tsCalculationResult = new TSCalculationResult();
			tsCalculationResult.setGebuehrProStunde(tarifMitBetreuung);
			bgCalculationResult.setTsCalculationResultMitPaedagogischerBetreuung(tsCalculationResult);
		}
		if (tarifOhneBetreuung != null) {
			TSCalculationResult tsCalculationResult = new TSCalculationResult();
			tsCalculationResult.setGebuehrProStunde(tarifOhneBetreuung);
			bgCalculationResult.setTsCalculationResultOhnePaedagogischerBetreuung(tsCalculationResult);
		}

		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setBgCalculationResultAsiv(bgCalculationResult);
		return zeitabschnitt;
	}

	private VerfuegungZeitabschnitt createZeitabschnittMitTarifAndGueltigkeit(
		DateRange gueltigkeit,
		@Nullable BigDecimal tarifMitBetreuung,
		@Nullable BigDecimal tarifOhneBetreuung) {
		VerfuegungZeitabschnitt za = createZeitabschnittMitTarif(tarifMitBetreuung, tarifOhneBetreuung);
		za.setGueltigkeit(gueltigkeit);
		return za;
	}

}
