package ch.dvbern.ebegu.services.zahlungen;

import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.needle4j.annotation.ObjectUnderTest;
import org.needle4j.junit.NeedleRule;

@SuppressWarnings("unused")
class ZahlungsfileGeneratorPainTest {

	@Rule
	public NeedleRule needleRule = new NeedleRule();

	@ObjectUnderTest
	private ZahlungsfileGeneratorPain zahlungsfileGeneratorBern;

	@Test
	void ibanToUnformattedString() {
		Assertions.assertEquals("CH3909000000306638172", zahlungsfileGeneratorBern.ibanToUnformattedString(new IBAN("CH39 0900 0000 3066 3817 2")));
		Assertions.assertEquals("CH3909000000306638172", zahlungsfileGeneratorBern.ibanToUnformattedString(new IBAN("CH3909000000306638172")));
	}
}
