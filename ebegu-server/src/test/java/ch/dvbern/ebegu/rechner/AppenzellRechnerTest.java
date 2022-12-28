package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.Assert;
import org.junit.Test;

public class AppenzellRechnerTest extends AbstractBGRechnerTest {

	private static final AppenzellRechner APPENZELL_RECHNER = new AppenzellRechner();
	private static final DateRange AUGUST = new DateRange(LocalDate.of(2022, Month.AUGUST, 1), LocalDate.of(2022, Month.AUGUST, 31));

	@Test
	public void zeiteinheitHours() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		Assert.assertEquals(PensumUnits.HOURS, result.getZeiteinheit());
	}

	@Test
	public void anspruchPensumInStundenZero() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(0);
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		Assert.assertEquals(0, result.getAnspruchspensumProzent());
		Assert.assertEquals(0, result.getAnspruchspensumProzent());
	}

	@Test
	public void anspruchPensumInStundenVorschulalter() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(100);
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		Assert.assertEquals(100, result.getAnspruchspensumProzent());
		assertEquals(BigDecimal.valueOf(200), result.getAnspruchspensumZeiteinheit());
	}

	@Test
	public void anspruchPensumInStundenEingeschult() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.KLASSE1);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(100);
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		Assert.assertEquals(100, result.getAnspruchspensumProzent());
		assertEquals(BigDecimal.valueOf(158.33), result.getAnspruchspensumZeiteinheit());
	}

	@Test
	public void betreuungsPensumInStundenZero() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER
		);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.ZERO);
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.ZERO, result.getBetreuungspensumProzent());
		assertEquals(BigDecimal.ZERO, result.getBetreuungspensumZeiteinheit());
	}

	@Test
	public void betreuungsPensumInStundenVorschulalter() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(50));
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(50), result.getBetreuungspensumProzent());
		assertEquals(BigDecimal.valueOf(100), result.getBetreuungspensumZeiteinheit());
	}

	@Test
	public void betreuungsPensumInStundenEingeschult() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.KLASSE1);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(50));
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(50), result.getBetreuungspensumProzent());
		assertEquals(BigDecimal.valueOf(79.17), result.getBetreuungspensumZeiteinheit());
	}

	@Test
	public void bgPensumAnspruchHoeherAlsBetreuung() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(40));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(60);
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		Assert.assertEquals(60, result.getAnspruchspensumProzent());
		assertEquals(BigDecimal.valueOf(120), result.getAnspruchspensumZeiteinheit());
		assertEquals(BigDecimal.valueOf(40), result.getBetreuungspensumProzent());
		assertEquals(BigDecimal.valueOf(80), result.getBetreuungspensumZeiteinheit());

		assertEquals(BigDecimal.valueOf(80), result.getBgPensumZeiteinheit());
		assertEquals(BigDecimal.valueOf(40), result.getBgPensumProzent());
	}

	@Test
	public void bgPensumAnspruchTieferAlsBetreuung() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(60));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(40);
		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		Assert.assertEquals(40, result.getAnspruchspensumProzent());
		assertEquals(BigDecimal.valueOf(80), result.getAnspruchspensumZeiteinheit());
		assertEquals(BigDecimal.valueOf(60), result.getBetreuungspensumProzent());
		assertEquals(BigDecimal.valueOf(120), result.getBetreuungspensumZeiteinheit());

		assertEquals(BigDecimal.valueOf(80), result.getBgPensumZeiteinheit());
		assertEquals(BigDecimal.valueOf(40), result.getBgPensumProzent());
	}

	@Test
	public void vollkostenFullMonth() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.setGueltigkeit(AUGUST);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(50));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(50);

		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(2000), result.getVollkosten());
	}

	@Test
	public void vollkostenFullMonthAnspruchTieferAlsBetreuung() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.setGueltigkeit(AUGUST);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(50));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(40);

		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(1600), result.getVollkosten());
	}

	@Test
	public void vollkostenHalfMonth() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		DateRange gueltigkeit = new DateRange(LocalDate.of(2022, Month.AUGUST, 1), LocalDate.of(2022, Month.AUGUST, 15)); 		//48.387% des Monats
		verfuegungZeitabschnitt.setGueltigkeit(gueltigkeit);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(50));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(60);

		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(967.75), result.getVollkosten());
	}

	@Test
	public void gutscheinMinEinkommen() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.setGueltigkeit(AUGUST);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(100));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(100);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(30000));

		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(1720), result.getVerguenstigung());
	}

	@Test
	public void gutscheinEinkommenGrenze() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.setGueltigkeit(AUGUST);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(100));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(100);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(44000));

		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(1620), result.getVerguenstigung());
	}

	@Test
	public void gutscheinEinkommenMaxUeberschritten() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.setGueltigkeit(AUGUST);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(100));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(100);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(100001));

		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(0), result.getVerguenstigung());
	}

	@Test
	public void gutscheinMaxStundenSatzUeberschrittenBaby() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.setGueltigkeit(AUGUST);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(BigDecimal.valueOf(5000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(100));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(100);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(40000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBabyTarif(true);

		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(2700), result.getVerguenstigung());
	}

	@Test
	public void gutscheinMaxStundenSatzUeberschrittenKind() {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt = prepareVerfuegungZeitabschnitt(EinschulungTyp.VORSCHULALTER);
		verfuegungZeitabschnitt.setGueltigkeit(AUGUST);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMonatlicheBetreuungskosten(BigDecimal.valueOf(5000));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setBetreuungspensumProzent(BigDecimal.valueOf(100));
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setAnspruchspensumProzent(100);
		verfuegungZeitabschnitt.getRelevantBgCalculationInput().setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(40000));

		BGCalculationResult result = calculateResult(verfuegungZeitabschnitt);
		assertEquals(BigDecimal.valueOf(2300), result.getVerguenstigung());
	}



	private void assertEquals(BigDecimal expected, BigDecimal actual) {
		Assert.assertEquals(expected.stripTrailingZeros(), actual.stripTrailingZeros());
	}

	private BGCalculationResult calculateResult(VerfuegungZeitabschnitt zeitabschnitt) {
		APPENZELL_RECHNER.calculate(zeitabschnitt, getRechnerParamterAppenzell());
		return zeitabschnitt.getRelevantBgCalculationResult();
	}

	private VerfuegungZeitabschnitt prepareVerfuegungZeitabschnitt(EinschulungTyp einschulungTyp) {
		VerfuegungZeitabschnitt zeitabschnitt =  new VerfuegungZeitabschnitt();
		zeitabschnitt.getRelevantBgCalculationInput().setEinschulungTyp(einschulungTyp);
		return zeitabschnitt;
	}
}
