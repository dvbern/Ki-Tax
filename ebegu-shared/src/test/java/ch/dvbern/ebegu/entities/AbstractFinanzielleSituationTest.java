package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AbstractFinanzielleSituationTest {

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "SOLOTHURN", "BERN", "BERN_FKJV" },
		mode = Mode.INCLUDE)
	public void isVollstaendig_BERN_Test(FinanzielleSituationTyp finanzielleSituationTyp) {
		FinanzielleSituation finSit = new FinanzielleSituation();
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setNettolohn(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setFamilienzulage(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setErsatzeinkommen(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setErhalteneAlimente(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setGeleisteteAlimente(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setSchulden(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setBruttovermoegen(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(true));
		// Steueranfrage
		finSit.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.FAILED);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(true));
		finSit.setBruttovermoegen(null);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.PROVISORISCH);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(false));
		finSit.setNettoVermoegen(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(finanzielleSituationTyp), is(true));
	}

	@Test
	public void isVollstaendig_LU_Test() {
		FinanzielleSituation finSit = new FinanzielleSituation();
		assertThat(finSit.isVollstaendig(FinanzielleSituationTyp.LUZERN), is(false));
		finSit.setSteuerbaresEinkommen(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(FinanzielleSituationTyp.LUZERN), is(false));
		finSit.setSteuerbaresVermoegen(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(FinanzielleSituationTyp.LUZERN), is(false));
		finSit.setAbzuegeLiegenschaft(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(FinanzielleSituationTyp.LUZERN), is(false));
		finSit.setGeschaeftsverlust(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(FinanzielleSituationTyp.LUZERN), is(false));
		finSit.setEinkaeufeVorsorge(BigDecimal.ONE);
		assertThat(finSit.isVollstaendig(FinanzielleSituationTyp.LUZERN), is(true));
	}
}
