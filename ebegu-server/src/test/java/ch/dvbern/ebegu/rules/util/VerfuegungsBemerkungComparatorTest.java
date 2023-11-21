package ch.dvbern.ebegu.rules.util;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Locale;

import org.hamcrest.Matchers;
import org.junit.Test;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkungDTO;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.VerfuegungsBemerkungComparator;

public class VerfuegungsBemerkungComparatorTest {
	private final VerfuegungsBemerkungDTO verfuegungsBemerkungFachstelle = new VerfuegungsBemerkungDTO(RuleValidity.ASIV, MsgKey.FACHSTELLE_MSG, Locale.GERMAN);
	private final VerfuegungsBemerkungDTO verfuegungsBemerkungFachstelleZwei = new VerfuegungsBemerkungDTO(RuleValidity.ASIV, MsgKey.FACHSTELLE_MSG, Locale.GERMAN);
	private final VerfuegungsBemerkungDTO verfuegungsBemerkungAbwesenheit = new VerfuegungsBemerkungDTO(RuleValidity.GEMEINDE, MsgKey.ABWESENHEIT_MSG, Locale.FRENCH);
	private final VerfuegungsBemerkungComparator verfuegungsBemerkungComparator = new VerfuegungsBemerkungComparator();

	@Test
	public void testVerfuegungGleichGross() {
		VerfuegungsBemerkungDTO verfuegungsBemerkungBemerkungZwei = new VerfuegungsBemerkungDTO(RuleValidity.ASIV, MsgKey.FACHSTELLE_MSG, Locale.GERMAN);

		int resultatVerfueungsBemerkung = verfuegungsBemerkungComparator.compare(verfuegungsBemerkungFachstelle, verfuegungsBemerkungFachstelleZwei);
		assertThat(resultatVerfueungsBemerkung, Matchers.is(0));
	}

	@Test
	public void testVerfuegungGroesser() {
		int resultatVerfueungsBemerkung = verfuegungsBemerkungComparator.compare(verfuegungsBemerkungFachstelle, verfuegungsBemerkungAbwesenheit);
		assertThat(resultatVerfueungsBemerkung, Matchers.is(1));
	}

	@Test
	public void testVerfuegungKleiner() {
		int resultatVerfueungsBemerkung = verfuegungsBemerkungComparator.compare(verfuegungsBemerkungAbwesenheit, verfuegungsBemerkungFachstelle);
		assertThat(resultatVerfueungsBemerkung, Matchers.is(-1));
	}

}
