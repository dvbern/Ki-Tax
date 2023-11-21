package ch.dvbern.ebegu.rules.util;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.Test;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.VerfuegungZeitabschnittComparator;

public class VerfuegungZeitabschnittComparatorTest{

	private final VerfuegungZeitabschnittComparator verfuegungZeitabschnittComparator = new VerfuegungZeitabschnittComparator();
	private final VerfuegungZeitabschnitt verfuegungZeitabschnittJanApr = new VerfuegungZeitabschnitt();
	private final VerfuegungZeitabschnitt verfuegungZeitabschnittJanAprZwei = new VerfuegungZeitabschnitt();
	private final VerfuegungZeitabschnitt verfuegungZeitabschnittAprJun = new VerfuegungZeitabschnitt();
	private final VerfuegungZeitabschnitt verfuegungZeitabschnittJanJun = new VerfuegungZeitabschnitt();

	private final LocalDate januar = LocalDate.of(2023, 1, 1);
	private final LocalDate april = LocalDate.of(2023, 4, 1);
	private final LocalDate juni = LocalDate.of(2023, 6, 14);

	private final DateRange januarBisApril = new DateRange(januar, april);
	private final DateRange aprilBisJuni = new DateRange(april, juni);
	private final DateRange januarBisJuni = new DateRange(januar, juni);

	@Test
	public void verfuegungZeitabschnittGleichGross() {
		verfuegungZeitabschnittJanApr.setGueltigkeit(januarBisApril);
		verfuegungZeitabschnittJanAprZwei.setGueltigkeit(januarBisApril);

		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittJanApr, verfuegungZeitabschnittJanAprZwei);
		assertThat(resultat, Matchers.is(0));
	}

	@Test
	public void verfuegungZeitabschnittAbKleiner() {
		verfuegungZeitabschnittJanApr.setGueltigkeit(januarBisApril);
		verfuegungZeitabschnittAprJun.setGueltigkeit(aprilBisJuni);

		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittJanApr, verfuegungZeitabschnittAprJun);
		assertThat(resultat, Matchers.lessThan(0));
	}

	@Test
	public void verfuegungZeitabschnittBisKleiner() {
		verfuegungZeitabschnittJanApr.setGueltigkeit(januarBisApril);
		verfuegungZeitabschnittJanJun.setGueltigkeit(januarBisJuni);

		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittJanApr, verfuegungZeitabschnittJanJun);
		assertThat(resultat, Matchers.lessThan(0));
	}

	@Test
	public void verfuegungZeitabschnittAbGroesser() {
		verfuegungZeitabschnittAprJun.setGueltigkeit(aprilBisJuni);
		verfuegungZeitabschnittJanApr.setGueltigkeit(januarBisApril);

		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittAprJun, verfuegungZeitabschnittJanApr);
		assertThat(resultat, Matchers.greaterThan(0));
	}

	@Test
	public void verfuegungZeitabschnittBisGroesser() {
		verfuegungZeitabschnittJanJun.setGueltigkeit(januarBisJuni);
		verfuegungZeitabschnittJanApr.setGueltigkeit(januarBisApril);

		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittJanJun, verfuegungZeitabschnittJanApr);
		assertThat(resultat, Matchers.greaterThan(0));
	}
}
