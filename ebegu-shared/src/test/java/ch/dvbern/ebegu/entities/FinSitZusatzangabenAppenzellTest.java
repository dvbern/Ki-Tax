package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FinSitZusatzangabenAppenzellTest {

	@Test
	public void isVollstaendigTest() {
		FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell = new FinSitZusatzangabenAppenzell();
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(false));
		finSitZusatzangabenAppenzell.setSaeule3a(BigDecimal.ONE);
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(false));
		finSitZusatzangabenAppenzell.setSaeule3aNichtBvg(BigDecimal.ONE);
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(false));
		finSitZusatzangabenAppenzell.setBeruflicheVorsorge(BigDecimal.ONE);
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(false));
		finSitZusatzangabenAppenzell.setLiegenschaftsaufwand(BigDecimal.ONE);
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(false));
		finSitZusatzangabenAppenzell.setEinkuenfteBgsa(BigDecimal.ONE);
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(false));
		finSitZusatzangabenAppenzell.setVorjahresverluste(BigDecimal.ONE);
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(false));
		finSitZusatzangabenAppenzell.setPolitischeParteiSpende(BigDecimal.ONE);
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(false));
		finSitZusatzangabenAppenzell.setLeistungAnJuristischePersonen(BigDecimal.ONE);
		assertThat(finSitZusatzangabenAppenzell.isVollstaendig(), is(true));
	}
}
