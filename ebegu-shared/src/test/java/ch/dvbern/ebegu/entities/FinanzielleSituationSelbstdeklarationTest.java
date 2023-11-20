package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FinanzielleSituationSelbstdeklarationTest {

	@Test
	public void isVollstaendigTest() {
		FinanzielleSituationSelbstdeklaration finanzielleSituationSelbstdeklaration =
			new FinanzielleSituationSelbstdeklaration();
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setEinkunftErwerb(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setEinkunftVersicherung(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setEinkunftWertschriften(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setEinkunftUnterhaltsbeitragKinder(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setEinkunftUeberige(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setEinkunftLiegenschaften(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugBerufsauslagen(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugSchuldzinsen(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugUnterhaltsbeitragKinder(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugSaeule3A(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugVersicherungspraemien(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugKrankheitsUnfallKosten(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setSonderabzugErwerbstaetigkeitEhegatten(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugKinderVorschule(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugKinderSchule(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugEigenbetreuung(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugFremdbetreuung(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugErwerbsunfaehigePersonen(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setVermoegen(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugSteuerfreierBetragErwachsene(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(false));
		finanzielleSituationSelbstdeklaration.setAbzugSteuerfreierBetragKinder(BigDecimal.ONE);
		assertThat(finanzielleSituationSelbstdeklaration.isVollstaendig(), is(true));
	}
}
