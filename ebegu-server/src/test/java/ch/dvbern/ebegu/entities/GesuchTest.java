package ch.dvbern.ebegu.entities;

import ch.dvbern.ebegu.test.GesuchBuilder;
import org.junit.Assert;
import org.junit.Test;


public class GesuchTest {

	@Test
	public void familiennameNormalesGesuchEinGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withGesuchsteller1("Meier", "Thomas"));
		Assert.assertEquals("Meier", gesuch.extractFamiliennamenString());
	}

	@Test
	public void familiennameNormalesGesuchZweiGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withGesuchsteller1("Meier", "Thomas")
			.withGesuchsteller2("Müller", "Anna"));
		Assert.assertEquals("Meier, Müller", gesuch.extractFamiliennamenString());
	}

	@Test
	public void familiennameSozialfallGesuchEinGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withoutGesuchsteller1()
			.withoutGesuchsteller2()
			.withSozialdienst("Hostettler", "Jonas", null, null));
		Assert.assertEquals("Hostettler", gesuch.extractFamiliennamenString());
	}

	@Test
	public void familiennameSozialfallGesuchZweiGesuchsteller() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withoutGesuchsteller1()
			.withoutGesuchsteller2()
			.withSozialdienst("Meier", "Thomas", "Schmied", "Katherina"));
		Assert.assertEquals("Meier, Schmied", gesuch.extractFamiliennamenString());
	}

	@Test
	public void familiennameSozialfallGesuchZweiGesuchstellerAusgefuellt() {
		Gesuch gesuch = GesuchBuilder.create(builder -> builder
			.withSozialdienst("Meier", "Thomas", "Schmied", "Katherina")
			.withGesuchsteller1("Muster", "Thomas")
			.withGesuchsteller2("Müller", "Anna"));
		Assert.assertEquals("Muster, Müller", gesuch.extractFamiliennamenString());
	}
}
