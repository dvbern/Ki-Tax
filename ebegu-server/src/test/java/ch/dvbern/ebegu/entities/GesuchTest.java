package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.test.GesuchBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GesuchTest {

	@Test
	void familiennameNormalesGesuchEinGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withGesuchsteller1("Meier", "Thomas"));
		Assertions.assertEquals("Meier", gesuch.extractFamiliennamenString());
	}

	@Test
	void familiennameNormalesGesuchZweiGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withGesuchsteller1("Meier", "Thomas")
			.withGesuchsteller2("Müller", "Anna"));
		Assertions.assertEquals("Meier, Müller", gesuch.extractFamiliennamenString());
	}

	@Test
	void familiennameSozialfallGesuchEinGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withoutGesuchsteller1()
			.withoutGesuchsteller2()
			.withSozialdienst("Hostettler", "Jonas", null, null));
		Assertions.assertEquals("Hostettler", gesuch.extractFamiliennamenString());
	}

	@Test
	void familiennameSozialfallGesuchZweiGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withoutGesuchsteller1()
			.withoutGesuchsteller2()
			.withSozialdienst("Meier", "Thomas", "Schmied", "Katherina"));
		Assertions.assertEquals("Meier, Schmied", gesuch.extractFamiliennamenString());
	}

	@Test
	void familiennameSozialfallGesuchZweiGesuchstellerAusgefuellt() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withSozialdienst("Meier", "Thomas", "Schmied", "Katherina")
			.withGesuchsteller1("Muster", "Thomas")
			.withGesuchsteller2("Müller", "Anna"));
		Assertions.assertEquals("Muster, Müller", gesuch.extractFamiliennamenString());
	}
}
