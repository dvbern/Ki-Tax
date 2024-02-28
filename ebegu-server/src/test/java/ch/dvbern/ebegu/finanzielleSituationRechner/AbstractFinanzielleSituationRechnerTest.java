package ch.dvbern.ebegu.finanzielleSituationRechner;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import org.junit.Test;

import javax.annotation.Nullable;
import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class AbstractFinanzielleSituationRechnerTest {

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_finSitNull() {
		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(null);
		assertThat(geschaeftsGewinnDurchschnitt, is(nullValue()));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_gewinnBasisJahrNull() {
		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(new FinanzielleSituation());
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.ZERO));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_gewinnBasisJahrNull_ErsatzeinkommenNotNull() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahr(BigDecimal.ONE);
		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finanzielleSituation);
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.ZERO));
	}


	@Test
	public void calcGeschaeftsgewinnDurchschnitt_gewinnBasisJahr() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(3500));

		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finanzielleSituation);

		// Expected Result 3500 CHF / 1
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(3500)));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_gewinnBasisJahrAndMinus1() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(1400));
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(6800));

		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finanzielleSituation);

		// Expected Result 1400+6800 / 2
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(4100)));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_gewinnBasisJahrAndMinus1AndMinus2() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(7600));
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(10685));
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus2(BigDecimal.valueOf(4305));

		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finanzielleSituation);

		// Expected Result 7600 + 10685 + 4305 / 3
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(7530)));
	}


	@Test
	public void calcGeschaeftsgewinnDurchschnitt_gewinnBasisJahr_Ersatzeinkommen() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(6804));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahr(BigDecimal.valueOf(1365));

		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finanzielleSituation);

		// Expected Result 6804 + 1365 / 1
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(8169)));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_gewinnBasisJahr_Ersatzeinkommen_Minus1() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(6800));
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(350));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahr(BigDecimal.valueOf(3000));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(BigDecimal.valueOf(768));
		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finanzielleSituation);

		// Expected Result 6800+350+3000+768 / 2
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(5459)));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_gewinnBasisJahr_Ersatzeinkommen_Minus1_Minus2() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(1680));
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(5823));
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus2(BigDecimal.valueOf(7962));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahr(BigDecimal.valueOf(905));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(BigDecimal.valueOf(486));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(BigDecimal.valueOf(1872));

		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(finanzielleSituation);

		// Expected Result 1680+5823+7962+905+486+1872 / 3
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(6243)));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_ekv_invalidBasisjahr() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		Einkommensverschlechterung ekv = new Einkommensverschlechterung();

		BigDecimal geschaeftsGewinnDurchschnitt =
			AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(
				finanzielleSituation,
				ekv,
				null,
				null,
				3);

		assertThat(geschaeftsGewinnDurchschnitt, is(nullValue()));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_ekv_basisjahr1() {
		EinkommensverschlechterungInfo ekvInfo = new EinkommensverschlechterungInfo();
		ekvInfo.setEkvFuerBasisJahrPlus1(true);
		ekvInfo.setEkvFuerBasisJahrPlus2(false);

		BigDecimal geschaeftsGewinnDurchschnitt = calculateGeschaftsGwinnWithCompleteFinSitAndEKV(ekvInfo, 1);

                // Expected Result (Example Antrag 2023/24)
		// 9803 (EKVBJ1 Geschäftsgewinn Basisjahr (2023))
		// + 3480 (FinSit Geschäftsgewinn Basisjahr (2022))
		// + 685 (FinSit ErsatzEinkommen Basisjahr (2022))
		// + 540 (FinSit Geschäftsgewinn Basisjahr Minus 1 (2021))
		// + 963 (FinSit ErsatzEinkommen Basisjahr Minus 1(2021))
		// + 1000 (Ersatzeinkommensbasisjahr EKV)
		// / 3 (Jahre)
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(5490)));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_ekv_basisjahr2_ekvFuerBasisJah1() {
		EinkommensverschlechterungInfo ekvInfo = new EinkommensverschlechterungInfo();
		ekvInfo.setEkvFuerBasisJahrPlus1(true);

		BigDecimal geschaeftsGewinnDurchschnitt = calculateGeschaftsGwinnWithCompleteFinSitAndEKV(ekvInfo, 2);
		// Expected Result (Example Antrag 2023/24
		// 2700 (EKVBJ2 Basisjahr (2024))
		// + 9803 (EKVBJ1 Basisjahr(2023))
		// + 3480 (FinSit Geschäftsgewinn Basisjahr (2022))
        // + 685 (FinSit ErsatzEinkommen Basisjahr(2022))
		// + 1000 (Ersatzeinkommensbasisjahr EKV1)
		// + 1000 (Ersatzeinkommensbasisjahr EKV2)
		// / 3 (Jahre)
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(6223)));
	}

	@Test
	public void calcGeschaeftsgewinnDurchschnitt_ekv_basisjahr2_ekvFuerBasisJah2() {
		EinkommensverschlechterungInfo ekvInfo = new EinkommensverschlechterungInfo();
		ekvInfo.setEkvFuerBasisJahrPlus1(false);
		ekvInfo.setEkvFuerBasisJahrPlus2(true);

		BigDecimal geschaeftsGewinnDurchschnitt = calculateGeschaftsGwinnWithCompleteFinSitAndEKV(ekvInfo, 2);

		// Expected Result (Example Antrag 2023/24)
		// 2700 (EKVBJ2 Basisjahr (2024))
		// + 6200 (EKVBJ2 Basisjahr Minus 1 (2023))
		// + 3480 (FinSit Geschäftsgewinn Basisjahr (2022))
		// + 685 (FinSit ErsatzEinkommen Basisjahr (2022))
		// + 500 (ErsatzeinkommensbasisjahrMinus1 EKV2)
		// + 1000 (Ersatzeinkommensbasisjahr EKV2)
		// / 3 (Jahre)
		assertThat(geschaeftsGewinnDurchschnitt, is(BigDecimal.valueOf(4855)));
	}

	@Nullable
	private BigDecimal calculateGeschaftsGwinnWithCompleteFinSitAndEKV(
		EinkommensverschlechterungInfo ekvInfo,
		int basisjahr) {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(3480));
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(540));
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus2(BigDecimal.valueOf(7235));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahr(BigDecimal.valueOf(685));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(BigDecimal.valueOf(963));
		finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(BigDecimal.valueOf(1039));

		Einkommensverschlechterung ekvBj1 = new Einkommensverschlechterung();
		ekvBj1.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(9803));
		ekvBj1.setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(7890));
		ekvBj1.setErsatzeinkommenSelbststaendigkeitBasisjahr(BigDecimal.valueOf(1000));

		Einkommensverschlechterung ekvBj2 = new Einkommensverschlechterung();
		ekvBj2.setGeschaeftsgewinnBasisjahr(BigDecimal.valueOf(2700));
		ekvBj2.setGeschaeftsgewinnBasisjahrMinus1(BigDecimal.valueOf(6200));
		ekvBj2.setErsatzeinkommenSelbststaendigkeitBasisjahr(BigDecimal.valueOf(1000));
		ekvBj2.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(BigDecimal.valueOf(500));

		return AbstractFinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(
				finanzielleSituation,
				ekvBj1,
				ekvBj2,
				ekvInfo,
				basisjahr);
	}

}
