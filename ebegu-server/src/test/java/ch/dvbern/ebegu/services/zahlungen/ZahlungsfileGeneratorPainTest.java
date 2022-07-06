package ch.dvbern.ebegu.services.zahlungen;

import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class ZahlungsfileGeneratorPainTest {

	private final ZahlungsfileGeneratorPain zahlungsfileGeneratorPain = new ZahlungsfileGeneratorPain();

	@Test
	void ibanToUnformattedString() {
		Assertions.assertEquals("CH3909000000306638172", zahlungsfileGeneratorPain.ibanToUnformattedString(new IBAN("CH39 0900 0000 3066 3817 2")));
		Assertions.assertEquals("CH3909000000306638172", zahlungsfileGeneratorPain.ibanToUnformattedString(new IBAN("CH3909000000306638172")));
	}
}
