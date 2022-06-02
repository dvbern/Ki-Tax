package ch.dvbern.ebegu.test;

import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.enums.AntragStatus;

/**
 * Vereinfachte Zusammenstellung von Testdaten. Kann weiter ergänzt werden
 * Verwendung:
 *
 * Gesuch gesuch = GesuchBuilder.create(builder -> builder
 * 			.withGesuchsteller1("Meier", "Thomas")
 * 			.withGesuchsteller2("Müller", "Anna"));
 */
public class GesuchBuilder {

	private final Gesuch gesuch;

	public GesuchBuilder() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		gesuch = betreuung.extractGesuch();
	}

	public GesuchBuilder withoutGesuchsteller1() {
		gesuch.setGesuchsteller1(null);
		return this;
	}

	public GesuchBuilder withGesuchsteller1() {
		return withGesuchsteller1("Muster", "Felix");
	}

	public GesuchBuilder withGesuchsteller1(@Nonnull String name, @Nonnull String vorname) {
		gesuch.extractGesuchsteller1().ifPresent(gesuchsteller -> {
			gesuchsteller.setNachname(name);
			gesuchsteller.setVorname(vorname);
		});
		return this;
	}

	public GesuchBuilder withoutGesuchsteller2() {
		gesuch.setGesuchsteller2(null);
		return this;
	}

	public GesuchBuilder withGesuchsteller2() {
		return withGesuchsteller2("Meier", "Anna");
	}

	public GesuchBuilder withGesuchsteller2(@Nonnull String name, @Nonnull String vorname) {
		TestDataUtil.addSecondGesuchsteller(gesuch);
		gesuch.extractGesuchsteller2().ifPresent(gesuchsteller -> {
			gesuchsteller.setNachname(name);
			gesuchsteller.setVorname(vorname);
		});
		return this;
	}

	public GesuchBuilder withSozialdienst(@Nonnull String nameGS1, @Nonnull String vornameGS1, @Nullable String nameGS2, @Nullable String vornameGS2) {
		TestDataUtil.createDefaultSozialdienstStammdaten(gesuch.getFall());
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_SOZIALDIENST);
		final SozialdienstFall sozialdienstFall = gesuch.getFall().getSozialdienstFall();
		Objects.requireNonNull(sozialdienstFall);
		sozialdienstFall.setName(nameGS1);
		sozialdienstFall.setVorname(vornameGS1);
		sozialdienstFall.setNameGs2(nameGS2);
		sozialdienstFall.setVornameGs2(vornameGS2);
		return this;
	}

	public static Gesuch create(Consumer<GesuchBuilder> block) {
		GesuchBuilder builder = new GesuchBuilder();
		block.accept(builder);
		return builder.gesuch;
	}
}
