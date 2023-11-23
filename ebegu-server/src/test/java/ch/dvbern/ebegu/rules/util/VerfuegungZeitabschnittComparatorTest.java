package ch.dvbern.ebegu.rules.util;

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.VerfuegungZeitabschnittComparator;

public class VerfuegungZeitabschnittComparatorTest{
	private final VerfuegungZeitabschnittComparator verfuegungZeitabschnittComparator = new VerfuegungZeitabschnittComparator();

	private VerfuegungZeitabschnitt verfuegungZeitabschnittJanApr;
	private VerfuegungZeitabschnitt verfuegungZeitabschnittAprJun;
	private VerfuegungZeitabschnitt verfuegungZeitabschnittJanJun;
	@Before
	public void init() {
		DateRange januarBisApril = new DateRange(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 4, 1));
		DateRange aprilBisJuni = new DateRange(LocalDate.of(2023, 4, 1), LocalDate.of(2023, 6, 14));
		DateRange januarBisJuni = new DateRange(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 6, 14));

		verfuegungZeitabschnittJanApr = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnittJanApr.setGueltigkeit(januarBisApril);

		verfuegungZeitabschnittAprJun = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnittAprJun.setGueltigkeit(aprilBisJuni);

		verfuegungZeitabschnittJanJun = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnittJanJun.setGueltigkeit(januarBisJuni);
	}

	@Test
	public void verfuegungZeitabschnittGleichGross() {
		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittJanApr, verfuegungZeitabschnittJanApr);
		assertThat(resultat, Matchers.is(0));
	}

	@Test
	public void verfuegungZeitabschnittAbKleiner() {
		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittJanApr, verfuegungZeitabschnittAprJun);
		assertThat(resultat, Matchers.lessThan(0));
	}

	@Test
	public void verfuegungZeitabschnittBisKleiner() {
		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittJanApr, verfuegungZeitabschnittJanJun);
		assertThat(resultat, Matchers.lessThan(0));
	}

	@Test
	public void verfuegungZeitabschnittAbGroesser() {
		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittAprJun, verfuegungZeitabschnittJanApr);
		assertThat(resultat, Matchers.greaterThan(0));
	}

	@Test
	public void verfuegungZeitabschnittBisGroesser() {
		int resultat = verfuegungZeitabschnittComparator.compare(verfuegungZeitabschnittJanJun, verfuegungZeitabschnittJanApr);
		assertThat(resultat, Matchers.greaterThan(0));
	}
}
